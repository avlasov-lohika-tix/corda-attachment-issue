package org.example.test.contract.input.state.entity

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.OneToMany
import javax.persistence.Table
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import org.example.test.contract.input.state.NoArgConstructor
import org.example.test.contract.input.state.converter.ParticipantConverter
import org.hibernate.Hibernate
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.io.Serializable
import java.time.Instant
import java.util.Objects
import java.util.UUID

@Entity
@Table(name = "test_attachment_entity", schema = "test")
@CordaSerializable
data class TestAttachmentInputStateEntity(
    @Column(name = "search")
    val search: String,

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumns(
        JoinColumn(name = "output_index", referencedColumnName = "output_index"),
        JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id")
    )
    val attachments: List<TestAttachment>,

    @Column(name = "participants")
    @Convert(converter = ParticipantConverter::class)
    val participants: List<String>
) : PersistentState()

@Entity
@Table(name = "test_attachment", schema = "test")
@CordaSerializable
@IdClass(AttachmentCompositeKey::class)
data class TestAttachment(
    @Id
    @Column(name = "attachment_id")
    val attachmentId: UUID,
    @Id
    @Column(name = "updated_date")
    val updatedDate: Instant = Instant.now(),
    @Column(name = "attachment_name")
    val name: String,
    @Column(name = "uploaded_by")
    val uploadedBy: String,
    @Column(name = "uploaded_date")
    val uploadedDate: Instant
) : Serializable

@CordaSerializable
@NoArgConstructor
data class AttachmentCompositeKey
    (val attachmentId: UUID, val updatedDate: Instant) : Serializable