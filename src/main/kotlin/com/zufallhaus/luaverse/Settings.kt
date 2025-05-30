package com.zufallhaus.luaverse

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.collections.iterator

import kotlin.io.path.isDirectory

/**
 * Global settings objects for this CLI.
 */
object Settings {
    private val appDataPath: Path = Paths.get(System.getenv("APPDATA")).parent
    /*
    I'm leaning towards a special directory for installs using this tool, that way it's easier to manage and other
    programs are less likely to get in the way.
    */
    var luaverseDir: Path = appDataPath.resolve("Local/Programs/Luaverse")

    /*
    Each map index is a unique nickname of the directory. Each map value is a list containing [0] a description of
    the directory, and [1] the directory itself, as a String.

    TODO: Figure out some form of type safety for the inner MutableMap. "Any" doesn't exactly cut it for me.
    */
    val directories: Map<String, MutableMap<String, Any>> = mapOf(
        /*
        The idea behind a builds folder is so that a version of Lua can be built and stored here until the user is
        ready to install it.
        */
        "builds" to mutableMapOf(
            "desc" to "The directory where Lua is built to.",
            "dir" to luaverseDir.resolve("Builds")
        ),

        "backups" to mutableMapOf(
            "desc" to "The directory where backups of the Path environment variable are stored.",
            "dir" to luaverseDir.resolve("Backups")
        ),

        "downloads" to mutableMapOf(
            "desc" to "The directory where Lua's source code is downloaded to prior to being extracted and built.",
            "dir" to luaverseDir.resolve("Downloads")
        ),

        "extracts" to mutableMapOf(
            "desc" to "The directory where Lua's source code is extracted to.",
            "dir" to luaverseDir.resolve("Extracts")
        ),

        "lua" to mutableMapOf(
            "desc" to "The directory where Lua is installed.",
            "dir" to luaverseDir.resolve("Lua")
        )
    )
}