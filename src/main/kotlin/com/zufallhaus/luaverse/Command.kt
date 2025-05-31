/*
All commands return "true" or "false" based on if their execution was successful or not. Currently, the value
returned is unused, but I am implementing it in case it is needed in the future.
*/

package com.zufallhaus.luaverse

import com.zufallhaus.luaverse.lua.SourceCode
import com.zufallhaus.luaverse.lua.VersionHandler
import com.zufallhaus.luaverse.system.PathEnvironment
import com.zufallhaus.luaverse.utility.VersionString

import java.awt.Desktop
import java.io.File
import java.nio.file.Path

import kotlin.collections.iterator

class Command(command: List<String>) {
    // Map commands here.
    /*
    TODO: Possible commands to consider.
        Some sort of command to list off the versions currently installed. Maybe one single list command that can be
        used to list of directories as well?
    */
    private val rootCommands = mapOf(
        "help" to ::helpCommand,
        "registry" to ::registryCommand,
        "build" to ::buildCommand,
        "dir" to ::dirCommand,
        "debug" to ::debugCommand
    )

    init { rootCommands[command[0].lowercase()]?.invoke(command) ?: invalidateCommand(command) }

    /**
     * Debug commands for testing purposes.
     */
    private fun debugCommand(command: List<String>): Boolean {
        if (command.size == 1) return invalidateCommand(command)

        when (command[1].lowercase()) {
            "versions" -> {
                VersionHandler.getAvailableLuaVersions()
            }

            "download" -> {
                // I'll add safety later.
                val version: VersionString = try {
                    VersionString(command[2])
                } catch(exception: IndexOutOfBoundsException) {
                    VersionString("latest")
                }

                val sourceCode: SourceCode = SourceCode(version)
                println("Downloading...")
                val downloadSuccess: Boolean = sourceCode.download()

                if (downloadSuccess) {
                    println("Download complete!")
                    println("Extracting...")
                    val extractSuccess: Boolean = sourceCode.extract()

                    if (extractSuccess) {
                        println("Extraction complete!")
                    } else {
                        println("Extraction failed!")
                    }

                } else {
                    println("Download failed!")
                }
            }
        }

        return true
    }

    /**
     * Designed to be called whenever any command cannot be completed.
     * @param[command] An array containing each command argument.
     * @return false
     */
    private fun invalidateCommand(command: List<String>): Boolean {
        println("${command.joinToString(separator = " ")} is not a valid command.")
        // Always returns false so that it may be called functionally, if desired.
        return false
    }

    /**
     * Provides a list of commands.
     * @param[command] An array containing each command argument.
     * @return If the command was executed successfully or not.
     */
    private fun helpCommand(command: List<String>): Boolean {
        // ???
        try {
            command[1]
            /*
            Eventually, I want to have more detailed information on each command, so that one could say "help build",
            and it would show them how to use that command.
            */

        } catch (e: IndexOutOfBoundsException) {
            println("Supported commands:")
            for ((name, _) in rootCommands) println("  - $name")
        }

        return true
    }

    /**
     * This command and all commands contained handle the registry on Windows systems. Functions may include backing up,
     * modifying, and restoring the registry to a previous state.
     * @param[command] An array containing each command argument.
     * @return If the command was executed successfully or not.
     */
    private fun registryCommand(command: List<String>): Boolean {
        if (command.size == 1) return invalidateCommand(command)

        when (command[1].lowercase()) {
            // Example use of this command: "registry backup"
            "backup" -> {
                if (command.size > 2) {
                    println("Too many arguments entered for command \"registry backup\". Ignoring the following:")

                    for (argument in command.subList(2, command.size - 1)) {
                        println(argument)
                    }
                }
                return PathEnvironment.backup()
            }

            // Example use of this command: "registry restore luaverse_path-backup_1734551704987.json"
            "restore" -> {
                when (command.size) {
                    2 -> {
                        println("Please specify the path to the backup. Backups can be found by invoking the \"dir backup\" " +
                                "command or navigating to ${Settings.directories["backup"]?.get("dir")}.")
                        return false
                    }

                    3, 4 -> {
                        val hardRestore: Boolean = command.getOrNull(2) == "hard"
                        return PathEnvironment.restore(command[1], hardRestore)
                    }

                    else -> return invalidateCommand(command)
                }
            }

            else -> return invalidateCommand(command)
        }
    }

    /**
     * Builds Lua to the build directory. Users can specify the location of an already downloaded Lua binary, or enter
     * the version they wish to download.
     * @param[command] An array containing each command argument.
     * @return If the command was executed successfully or not.
     */
    private fun buildCommand(command: List<String>): Boolean {
        // Attempts to find a specified version number.
        val version: VersionString = try {
            VersionString(command[2])
        } catch(exception: IndexOutOfBoundsException) {
            VersionString("latest")
        }

        val sourceCode: SourceCode = SourceCode(version)

        // Downloads the files.
        println("Downloading...")
        val downloadSuccess: Boolean = sourceCode.download()

        if (downloadSuccess) {
            println("Download complete!")

            // Extracts the files.
            println("Extracting...")
            val extractSuccess: Boolean = sourceCode.extract()

            if (extractSuccess) {
                println("Extraction complete!")
            } else {
                println("Extraction failed!")
            }

        } else {
            println("Download failed!")
        }

        return true
    }

    /**
     * Lists of the current directories and a description of each.
     * @param[command] An array containing each command argument.
     * @return If the command was executed successfully or not.
     */
    private fun dirCommand(command: List<String>): Boolean {
        when (command.size) {
            /*
            If the command is simply "dir", the program will list every directory used.
            This also creates the directories if they don't exist, inadvertently.
            */
            1 -> {
                print("\n")
                println("--- DIRECTORY LISTING ---")
                println("You can open any of the following directories by typing in \"dir <name>\".`")
                print("\n")

                for ((dirName, dirInfo) in Settings.directories) {
                    println(dirName)
                    println(dirInfo["desc"])
                    println(dirInfo["dir"])
                    print("\n")
                }

                println("--- END OF LISTING ---")
                print("\n")

                return true
            }

            // Optionally, the user can specify a directory name after dir, and we'll open that directory as a convenience.
            2 -> {
                for ((dirName, dirInfo) in Settings.directories) {
                    if (command[1] == dirName.lowercase()) {
                        val directory: Path = dirInfo["dir"] as Path
                        val dirAsFile: File = directory.toFile()

                        if (dirAsFile.exists() && dirAsFile.isDirectory) {
                            Desktop.getDesktop().open(dirAsFile)
                            return true
                        }
                    }
                }

                return false
            }

            else -> return invalidateCommand(command)
        }
    }
}