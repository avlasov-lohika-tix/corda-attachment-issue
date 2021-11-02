package org.example.test.workflow.input.state.repository

import net.corda.core.contracts.StateAndRef
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.SingletonSerializeAsToken
import org.example.test.contract.input.state.TestAttachmentInputState
import org.example.test.contract.input.state.entity.TestAttachmentInputStateEntity
import java.util.UUID

@CordaService
class AttachmentInputStateTestRepository(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {

    fun findUnconsumedState(search: String): StateAndRef<TestAttachmentInputState>? =
        serviceHub.vaultService.queryBy<TestAttachmentInputState>(QueryCriteria.VaultCustomQueryCriteria(TestAttachmentInputStateEntity::search.equal(search)))
            .states
            .firstOrNull()

}