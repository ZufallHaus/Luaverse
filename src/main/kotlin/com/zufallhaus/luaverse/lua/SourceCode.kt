package com.zufallhaus.luaverse.lua

import com.zufallhaus.luaverse.Settings
import com.zufallhaus.luaverse.utility.VersionString

import java.io.File
import java.nio.file.Path

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Handles downloading, extracting, and building Lua.
 *
 * Download and extraction is done automatically on object initialization. Building is done by invoking the build()
 * method.
 */
class SourceCode(val version: VersionString) {
    var sourceFile: File? = null
        private set
    var extractedFiles: File? = null
        private set


    init {
        // Downloads Lua.
        println("Downloading...")
        val downloadSuccess: Boolean = this.download()

        if (downloadSuccess) {
            println("Download complete!")

            // Extracts Lua.
            println("Extracting...")
            val extractSuccess: Boolean = this.extract()

            if (extractSuccess) {
                println("Extraction complete!")
            } else {
                println("Extraction failed!")
            }

        } else {
            println("Download failed!")
        }
    }

    fun download(): Boolean {
        // Downloads the requested version of Lua from https://www.lua.org/ftp/.
        when (version.rawVersion) {
            "latest" -> sourceFile = fetchFileFromFtp(VersionHandler.luaVersionFiles.firstNotNullOf { VersionString(it.key) })
            else -> VersionHandler.luaVersionFiles[version.delimitedVersion]?.let { sourceFile = fetchFileFromFtp(VersionString(it)) } ?: println("Version does not exist.")
        }

        return sourceFile != null
    }

    fun extract(): Boolean {
        if (sourceFile != null) {
            // Kotlin is complaining about sourceFile. Is there a better way to do this?
            GzipCompressorInputStream(FileInputStream(sourceFile)).use { gzipIn ->
                TarArchiveInputStream(gzipIn).use { tarIn ->
                    var entry = tarIn.nextEntry
                    while (entry != null) {
                        val destination = File(Settings.directories["extracts"]!!["dir"].toString(), entry.name)

                        if (entry.isDirectory) {
                            destination.mkdirs()
                            extractedFiles = destination
                        } else {
                            destination.parentFile.mkdirs()
                            FileOutputStream(destination).use { out ->
                                tarIn.copyTo(out)
                            }
                        }

                        entry = tarIn.nextEntry
                    }
                }

            }
        } else {
            println("Could not find sourceFile.")
        }

        return extractedFiles != null
    }

    // Need to add handling for when a version is already built/installed.
    fun build() {
        // Creates a folder to build this specific version of Lua in.
        val buildDir: Path = (Settings.directories["Builds"] as Path).resolve("lua-" + version.rawVersion)


        // Create a folder to install this specific version of Lua to.
        val installDir: Path = (Settings.directories["Lua"] as Path).resolve("lua-" + version.rawVersion)
    }

    // Maybe make a private function wrapped by this?
    fun findDownloadedFiles() {
        val downloadedFiles = File(Settings.directories["downloads"]!!["dir"].toString()).listFiles() ?: arrayOf()

        for (file in downloadedFiles) {
            if (file.isFile && file.name == "lua-" + version.rawVersion + ".tar.gz") {
                sourceFile = file
                break
            }
        }
    }

    fun findExtractedFiles() {}

    // TODO: Add handling for multiple downloads.
    // I forgot what I meant by the TODO above...
    private fun fetchFileFromFtp(version: VersionString): File? {
        val client: OkHttpClient = OkHttpClient()
        val request: Request = Request.Builder().url("https://www.lua.org/ftp/lua-${version.withDelimiter(".")}.tar.gz").build()
        val response: Response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val outputFile: File = File(Settings.directories["downloads"]!!["dir"].toString(), "lua-${version.rawVersion}.tar.gz")

            response.body?.byteStream().use { input ->
                outputFile.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }

            return outputFile
        }

        /*
        I'm not sure if this is the best approach. Would it be better to return "response"? Or, would it be better to
        raise an exception?
        */
        return null
    }
}