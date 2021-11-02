package org.example.test.workflow

import java.io.File

object AttachmentUtils {

    const val filename = "Large-Sample-test1.png"
    private val largeFile = File("../../../../Large-Sample-test1.png")
    val contentByteArray = largeFile.readBytes()
}
