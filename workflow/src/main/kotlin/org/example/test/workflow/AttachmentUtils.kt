package org.example.test.workflow

import net.corda.core.crypto.SecureHash
import net.corda.core.flows.FlowLogic
import org.apache.commons.io.FilenameUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object AttachmentUtils {

    private const val filename = "Large-Sample-test1.png"
    private val largeFile = File("../../../../Large-Sample-test1.png")

    private fun zipContent(): ByteArrayInputStream {
        var fileContent: ByteArray? = largeFile.readBytes()
        var zipContent: ByteArray? = null
        try {
            ByteArrayOutputStream(contentByteArray.size).let {
                ZipOutputStream(it).use { zos ->
                    val entry = ZipEntry(prepareZipArchiveFileName())
                    zos.putNextEntry(entry)
                    zos.write(contentByteArray)
                }

                zipContent = it.toByteArray()

                zipContent!!.inputStream()
            }
        } finally {
            fileContent = null
            zipContent = null
        }
    }

    private fun prepareZipArchiveFileName() =
        "${FilenameUtils.getName(filename)}-${UUID.randomUUID()}.${FilenameUtils.getExtension(filename)}"

    fun FlowLogic<Any>.uploadAttachment(): SecureHash =
        zipContent()
            .let { serviceHub.attachments.importAttachment(it, ourIdentity.toString(), filename) }
}
