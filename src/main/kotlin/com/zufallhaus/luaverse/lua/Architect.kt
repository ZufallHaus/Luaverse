package com.zufallhaus.luaverse.lua

import com.zufallhaus.luaverse.Settings
import com.zufallhaus.luaverse.system.PathEnvironment

import java.io.File
import java.nio.file.Files
import java.nio.file.Path


class Architect(sourceCode: SourceCode) {
    // This needs to do what build.bat does but in Kotlin.
    init {
        // Safeguard, before anything else happens.
        PathEnvironment.backup()


        // This is where TDM-GCC is required. I haven't tested it on any version prior to 10.3.0.
        val process: Process = ProcessBuilder("mingw32-make", "PLAT=mingw")
            .directory(File(Settings.directories["builds"]!!["dir"].toString()))
            .inheritIO()
            .start()

        // Isn't this already a Path? Why does IntelliJ want to cast it to Path?
        val luaInstallDir: Path = Settings.directories["lua"]!!["dir"] as Path

        // Initial Files
        Files.createDirectories(luaInstallDir.resolve("bin"))
        Files.createDirectories(luaInstallDir.resolve("doc"))
        Files.createDirectories(luaInstallDir.resolve("include"))
        Files.createDirectories(luaInstallDir.resolve("lib"))
        // At this level, I also want to create a JSON file that contains information that Luaverse can read.


    }
}