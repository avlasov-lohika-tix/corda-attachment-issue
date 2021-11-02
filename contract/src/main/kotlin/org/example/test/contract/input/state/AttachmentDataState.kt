package org.example.test.contract.input.state

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import java.util.UUID

@CordaSerializable
@BelongsToContract(AttachmentDataStateContract::class)
class AttachmentDataState(
    val attachmentId: UUID,
    val content: ByteArray,
    override val participants: List<AbstractParty>,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
): LinearState, QueryableState {
    override fun generateMappedObject(schema: MappedSchema) = when (schema) {
        is AttachmentDataStateSchemaV1 -> AttachmentDataStateSchemaV1.toEntity(this)
        else -> throw IllegalArgumentException("Unrecognised schema $schema")
    }

    override fun supportedSchemas() = listOf(AttachmentDataStateSchemaV1)
}