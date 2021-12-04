package com.defrag.client.util

import java.nio.file.Paths
import java.nio.file.LinkOption
import java.nio.file.StandardOpenOption
import java.io.IOException
import com.defrag.client.util.FileUtil
import java.nio.charset.StandardCharsets
import java.nio.file.Files

object FileUtil {
    fun appendTextFile(data: String, file: String): Boolean {
        try {
            val path = Paths.get(file, *arrayOfNulls(0))
            Files.write(
                path,
                listOf(data),
                StandardCharsets.UTF_8,
                if (Files.exists(path, *arrayOfNulls(0))) StandardOpenOption.APPEND else StandardOpenOption.CREATE
            )
        } catch (e: IOException) {
            println("WARNING: Unable to write file: $file")
            return false
        }
        return true
    }

    fun readTextFileAllLines(file: String): List<String> {
        return try {
            val path = Paths.get(file, *arrayOfNulls(0))
            Files.readAllLines(path, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            println("WARNING: Unable to read file, creating new file: $file")
            appendTextFile("", file)
            emptyList()
        }
    }
}