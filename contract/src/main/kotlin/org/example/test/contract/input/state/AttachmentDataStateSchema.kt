package org.example.test.contract.input.state

import net.corda.core.schemas.MappedSchema
import org.example.test.contract.input.state.entity.AttachmentDataEntity

object AttachmentDataStateSchemaV1 : MappedSchema(
    schemaFamily = AttachmentDataStateSchema::class.java,
    version = 1,
    mappedTypes = listOf(AttachmentDataEntity::class.java)
) {
    override val migrationResource = "test.changelog-master"

    fun toEntity(state: AttachmentDataState) = with(state) {
        AttachmentDataEntity(
            attachmentId = attachmentId,
            content = content,
            participants = participants.map { it.toString() }
        )
    }
}

object AttachmentDataStateSchema