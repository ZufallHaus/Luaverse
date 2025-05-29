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
        "build" to mutableMapOf(
            "desc" to "The directory where Lua is built to.",
            "dir" to luaverseDir.resolve("Lua")
        ),

        "backup" to mutableMapOf(
            "desc" to "The directory where backups of the Path environment variable are stored.",
            "dir" to luaverseDir.resolve("Backups")
        ),

        "download" to mutableMapOf(
            "desc" to "The directory where Lua's source code is downloaded to prior to being extracted and built.",
            "dir" to luaverseDir.resolve("Source")
        ),

        "extract" to mutableMapOf(
            "desc" to "The directory where Lua's source code is extracted to.",
            "dir" to luaverseDir.resolve("Extracts")
        )
    )

    // Essentially validates all the directories listed above.
    init { for ((_, dirInfo) in directories) validateDirectory(dirInfo["dir"] as Path) }

    /**
     * Checks if a directory exists and attempts to create it if it doesn't.
     *
     * @param[directory] Path The directory to validate.
     * @return If the directory was successfully created or not.
     */
    private fun validateDirectory(directory: Path): Boolean {
        var result: Boolean = directory.isDirectory()

        if (!result) {
            Files.createDirectories(directory)
            result = directory.isDirectory()
        }

        return result
    }
}