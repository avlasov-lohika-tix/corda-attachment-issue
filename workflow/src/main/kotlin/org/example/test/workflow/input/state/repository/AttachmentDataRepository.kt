package org.example.test.workflow.input.state.repository

import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.util.UUID

@CordaService
class AttachmentDataRepository(
    private val serviceHub: AppServiceHub
) : SingletonSerializeAsToken() {

    fun findAttachmentDataIds(search: String): List<UUID> =
        serviceHub.withEntityManager {
            val query = """
                SELECT * FROM TestAttachmentInputStateEntity ae
                    LEFT join ae.attachments as a
                    JOIN AttachmentDataEntity ade
                    ON ade.attachmentId = a
                    WHERE ae.search
                
            """.trimIndent()

            createQuery(query, UUID::class.java)
                .resultList
        }

}