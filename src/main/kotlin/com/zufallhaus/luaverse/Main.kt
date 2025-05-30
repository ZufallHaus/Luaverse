package com.zufallhaus.luaverse

import com.zufallhaus.luaverse.Settings.directories

import java.nio.file.Files
import java.nio.file.Path

import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.io.path.isDirectory

fun main() {
    println("Luaverse v0.0.1 : Lua version manager for Windows systems.")
    println("Created by Jacob Zufall.")
    println("Maintained and distributed by ZufallHaus.")
    println("") // Spacer
    println("Say \"help\" for a list of commands.")

    // This initializes the directories used by the program.
    // TODO: Should we tell the user that this is happening?
    for ((_, dirInfo) in directories) {
        val relevantPath: Path = dirInfo["dir"] as Path

        if (!relevantPath.isDirectory()) {
            Files.createDirectories(relevantPath)
        }
    }

    // Main loop
    while (true) {
        val input: String? = readlnOrNull()

        if (input != null) {
            if (input.lowercase() == "exit") {
                break
            }

            // I don't know if I should use List or Array.
            Command(input.split(" ").toList())

        } else {
            println("Usage: luaverse <command> <args>")
        }
    }

    println("Luaverse : Process ending...")
}