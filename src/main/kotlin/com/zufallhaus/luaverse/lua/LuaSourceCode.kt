package com.zufallhaus.luaverse.lua

import com.zufallhaus.luaverse.Settings
import com.zufallhaus.luaverse.utility.VersionString

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import java.io.File

/**
 * Handles downloading, extracting, and building Lua.
 *
 * Download and extraction is done automatically on object initialization. Building is done by invoking the build()
 * method.
 */
class LuaSourceCode(val version: VersionString) {
    var sourceFile: File? = null
        private set
    var extractedFiles: File? = null
        private set

    fun download(): Boolean {
        // Downloads the requested version of Lua from https://www.lua.org/ftp/.
        when (version.rawVersion) {
            "latest" -> sourceFile = fetchFileFromFtp(LuaVersionHandler.luaVersionFiles.firstNotNullOf { VersionString(it.key) })
            else -> LuaVersionHandler.luaVersionFiles[version.delimitedVersion]?.let { sourceFile = fetchFileFromFtp(VersionString(it)) } ?: println("Version does not exist.")
        }

        return sourceFile != null
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