package org.example.test.workflow.input.state

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
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
import org.example.test.workflow.CommonFlowSteps
import java.util.UUID

object AttachmentDataFlow {

    @InitiatingFlow
    @StartableByRPC
    @CordaSerializable
    class AttachmentDataUploadFlow(
        private val attachmentMetadataId: UUID,
        private val content: ByteArray,
        private val party: String
    ) : FlowLogic<SignedTransaction>() {

        companion object {
            val logger = loggerFor<AttachmentDataUploadFlow>()
        }

        override val progressTracker = CommonFlowSteps.commonTracker()

        @Suspendable
        override fun call(): SignedTransaction {
            progressTracker.currentStep = CommonFlowSteps.SET_UP
            logger.info("AttachmentDataUploadFlow: ${CommonFlowSteps.SET_UP.label}")

            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            val participants = listOfNotNull(
                ourIdentity,
                serviceHub.identityService.wellKnownPartyFromX500Name(CordaX500Name.parse(party))
            )

            progressTracker.currentStep = CommonFlowSteps.BUILDING_TRANSACTION
            logger.info("AttachmentDataUploadFlow: ${CommonFlowSteps.BUILDING_TRANSACTION.label}")

            val txCommand = Command(AttachmentDataStateContract.Commands.Issue(), participants.map { it.owningKey })
            val builder = TransactionBuilder(notary)

            builder.addOutputState(AttachmentDataState(
                attachmentId = attachmentMetadataId,
                content = content,
                participants = participants
            ), AttachmentDataStateContract.ID)

            builder.addCommand(txCommand)

            progressTracker.currentStep = CommonFlowSteps.VERIFYING_TRANSACTION
            logger.info("AttachmentDataUploadFlow: ${CommonFlowSteps.VERIFYING_TRANSACTION.label}")
            builder.verify(serviceHub)

            progressTracker.currentStep = CommonFlowSteps.SIGNING_TRANSACTION
            logger.info("AttachmentDataUploadFlow: ${CommonFlowSteps.SIGNING_TRANSACTION.label}")
            val selfSignedTx = serviceHub.signInitialTransaction(builder)

            progressTracker.currentStep = CommonFlowSteps.GATHERING_SIGNS
            logger.info("AttachmentDataUploadFlow: ${CommonFlowSteps.GATHERING_SIGNS.label}")

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
            logger.info("AttachmentDataUploadFlow: ${CommonFlowSteps.FINALISING_TRANSACTION.label}")

            return subFlow(
                FinalityFlow(
                    fullySignedTx,
                    otherPartiesSessions,
                    CommonFlowSteps.FINALISING_TRANSACTION.childProgressTracker()
                )
            )
        }
    }

    @InitiatedBy(AttachmentDataUploadFlow::class)
    class AttachmentDataUploadFlowAcceptor(val session: FlowSession) : FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            logger.info("Called AttachmentDataUploadFlow responder for $session")

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