package org.example.test.contract.input.state

import net.corda.core.schemas.MappedSchema
import org.example.test.contract.input.state.entity.TestAttachment
import org.example.test.contract.input.state.entity.TestAttachmentInputStateEntity

object TestAttachmentInputStateSchemaV1 : MappedSchema(
    schemaFamily = TestAttachmentInputStateSchema::class.java,
    version = 1,
    mappedTypes = listOf(TestAttachmentInputStateEntity::class.java, TestAttachment::class.java)
) {
    override val migrationResource = "test.changelog-master"

    fun toEntity(state: TestAttachmentInputState) = with(state) {
        TestAttachmentInputStateEntity(
            search = search,
            participants = participants.map { it.toString() },
            attachments = attachments.map {
                TestAttachment(
                    attachmentId = it.attachmentId,
                    name = it.attachmentName,
                    uploadedBy = it.uploadedBy,
                    cordaId = it.attachmentCordaId,
                    uploadedDate = it.uploadedDate
                )
            }
        )
    }
}

object TestAttachmentInputStateSchema