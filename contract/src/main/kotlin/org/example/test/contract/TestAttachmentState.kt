package org.example.test.contract

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.ContractState
import net.corda.core.identity.Party
import org.example.test.contract.TestAttachmentContract

@BelongsToContract(TestAttachmentContract::class)
data class TestAttachmentState(
    val attachmentId: String,
    override val participants: List<Party>
): ContractState