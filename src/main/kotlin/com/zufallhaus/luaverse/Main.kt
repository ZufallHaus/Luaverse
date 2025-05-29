package com.zufallhaus.luaverse

fun main() {
    println("Luaverse v0.0.1 : Lua version manager for Windows systems.")
    println("Created by Jacob Zufall.")
    println("Maintained and distributed by ZufallHaus.")
    println("") // Spacer
    println("Say \"help\" for a list of commands.")

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