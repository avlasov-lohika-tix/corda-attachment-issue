package org.example.test.contract.input.state

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import java.time.Instant
import java.util.UUID

@CordaSerializable
@BelongsToContract(TestAttachmentInputStateContract::class)
class TestAttachmentInputState(
    val search: String,
    val attachmentHash: String,
    val attachments: List<AttachmentInfo>,
    override val participants: List<AbstractParty>,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, QueryableState {

    override fun generateMappedObject(schema: MappedSchema) = when (schema) {
        is TestAttachmentInputStateSchemaV1 -> TestAttachmentInputStateSchemaV1.toEntity(this)
        else -> throw IllegalArgumentException("Unrecognised schema $schema")
    }

    override fun supportedSchemas() = listOf(TestAttachmentInputStateSchemaV1)
}

@CordaSerializable
data class AttachmentInfo(
    val attachmentId: UUID = UUID.randomUUID(),
    val uploadedBy: String,
    val attachmentName: String,
    val attachmentCordaId: String,
    val uploadedDate: Instant = Instant.now(),
    val updatedDate: Instant = Instant.now()
)