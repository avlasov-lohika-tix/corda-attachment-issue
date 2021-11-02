package org.example.test.contract.input.state.entity

import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Lob
import javax.persistence.Table
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import org.example.test.contract.input.state.converter.ParticipantConverter
import org.hibernate.Hibernate
import java.util.Objects
import java.util.UUID

@Entity
@Table(name = "attachment_data", schema = "test")
@CordaSerializable
data class AttachmentDataEntity(
    @Column(name = "id")
    val attachmentId: UUID,

    @Column(name = "content")
    @Lob
    val content: ByteArray,

    @Column(name = "participants")
    @Convert(converter = ParticipantConverter::class)
    val participants: List<String>
): PersistentState() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as AttachmentDataEntity

        return stateRef != null && stateRef == other.stateRef
    }

    override fun hashCode(): Int = Objects.hash(stateRef);

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(attachmentId = $attachmentId , content = $content , participants = $participants )"
    }
}