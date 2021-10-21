package org.example.test.workflow

import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.utilities.ProgressTracker

@Suppress("ClassName")
object CommonFlowSteps {

    object SET_UP : ProgressTracker.Step("Initialising flow.")
    object BUILDING_TRANSACTION : ProgressTracker.Step("Building transaction.")
    object VERIFYING_TRANSACTION : ProgressTracker.Step("Verifying contract constraints.")
    object SIGNING_TRANSACTION : ProgressTracker.Step("Signing transaction with our private key.")
    object GATHERING_SIGNS : ProgressTracker.Step("Gathering the counterparties signature.") {
        override fun childProgressTracker() = CollectSignaturesFlow.tracker()
    }

    object FINALISING_TRANSACTION : ProgressTracker.Step("Finalising transaction.") {
        override fun childProgressTracker() = FinalityFlow.tracker()
    }

    fun commonTracker() = ProgressTracker(
        SET_UP,
        BUILDING_TRANSACTION,
        VERIFYING_TRANSACTION,
        SIGNING_TRANSACTION,
        GATHERING_SIGNS,
        FINALISING_TRANSACTION
    )
}
