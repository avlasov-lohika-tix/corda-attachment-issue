package org.example.test.workflow.input.state

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.reissuance.flows.RequestReissuanceAndShareRequiredTransactions
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowException
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.ReceiveFinalityFlow
import net.corda.core.flows.SignTransactionFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.CordaX500Name
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.loggerFor
import org.example.test.contract.input.state.AttachmentDataState
import org.example.test.contract.input.state.AttachmentDataStateContract
import org.example.test.contract.input.state.TestAttachmentInputState
import org.example.test.contract.input.state.TestAttachmentInputStateContract
import org.example.test.workflow.CommonFlowSteps
import org.example.test.workflow.input.state.repository.AttachmentInputStateTestRepository

object AttachmentSharingFlow {

    @InitiatingFlow
    @StartableByRPC
    @CordaSerializable
    class AttachmentSharingFlow(
        private val search: String,
        private val party: List<String>
    ) : FlowLogic<SignedTransaction>() {

        companion object {
            val logger = loggerFor<AttachmentSharingFlow>()
        }

        override val progressTracker = CommonFlowSteps.commonTracker()

        private fun attachmentInputStateTestRepository() = serviceHub.cordaService(AttachmentInputStateTestRepository::class.java)

        @Suspendable
        override fun call(): SignedTransaction {
            progressTracker.currentStep = CommonFlowSteps.SET_UP
            logger.info("AttachmentSharingFlow: ${CommonFlowSteps.SET_UP.label}")

            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            val participants = party.mapNotNull {
                serviceHub.identityService.wellKnownPartyFromX500Name(CordaX500Name.parse(it))
            } + ourIdentity

            progressTracker.currentStep = CommonFlowSteps.BUILDING_TRANSACTION
            logger.info("AttachmentSharingFlow: ${CommonFlowSteps.BUILDING_TRANSACTION.label}")

            val txCommand = Command(TestAttachmentInputStateContract.Commands.Share(), participants.map { it.owningKey })
            val builder = TransactionBuilder(notary)

            val unconsumedState = attachmentInputStateTestRepository().findUnconsumedState(search) ?: throw FlowException("State for the search $search is not found")

            builder.addInputState(unconsumedState)

            builder.addOutputState(
                TestAttachmentInputState(
                    search = search,
                    attachments = unconsumedState.state.data.attachments,
                    participants = participants
                ), TestAttachmentInputStateContract.ID)

            builder.addCommand(txCommand)

            subFlow(RequestReissuanceAndShareRequiredTransactions())

            progressTracker.currentStep = CommonFlowSteps.VERIFYING_TRANSACTION
            logger.info("AttachmentSharingFlow: ${CommonFlowSteps.VERIFYING_TRANSACTION.label}")
            builder.verify(serviceHub)

            progressTracker.currentStep = CommonFlowSteps.SIGNING_TRANSACTION
            logger.info("AttachmentSharingFlow: ${CommonFlowSteps.SIGNING_TRANSACTION.label}")
            val selfSignedTx = serviceHub.signInitialTransaction(builder)

            progressTracker.currentStep = CommonFlowSteps.GATHERING_SIGNS
            logger.info("AttachmentSharingFlow: ${CommonFlowSteps.GATHERING_SIGNS.label}")

            val me = serviceHub.myInfo.legalIdentities.first()
            val otherPartiesSessions = participants
                .filterNot { it == me }
                .distinct()
                .map { initiateFlow(it) }
                .toSet()

            val fullySignedTx = subFlow(
                CollectSignaturesFlow(
                    selfSignedTx,
                    otherPartiesSessions,
                    CommonFlowSteps.GATHERING_SIGNS.childProgressTracker()
                )
            )

            progressTracker.currentStep = CommonFlowSteps.FINALISING_TRANSACTION
            logger.info("AttachmentSharingFlow: ${CommonFlowSteps.FINALISING_TRANSACTION.label}")

            return subFlow(
                FinalityFlow(
                    fullySignedTx,
                    otherPartiesSessions,
                    CommonFlowSteps.FINALISING_TRANSACTION.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(AttachmentSharingFlow::class)
    class AttachmentSharingFlowAcceptor(val session: FlowSession) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            logger.info("Called AttachmentSharingFlow responder for $session")

            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    "Flow should has one command." using (stx.tx.commands.size == 1)

                    "Single command should be Issue." using (stx.tx.commands.single().value is AttachmentDataStateContract.Commands.Issue)

                    val outputs = stx.tx.outputs.map { it.data }
                    "All output states should be the TestAttachmentInputState." using (outputs.all { it is AttachmentDataState })
                }
            }
            val txId = subFlow(signTransactionFlow).id
            return subFlow(ReceiveFinalityFlow(session, expectedTxId = txId))
        }
    }
    
}