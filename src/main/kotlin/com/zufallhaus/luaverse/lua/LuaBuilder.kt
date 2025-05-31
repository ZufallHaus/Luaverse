package com.zufallhaus.luaverse.lua

import com.zufallhaus.luaverse.Settings
import com.zufallhaus.luaverse.systemInteraction.PathEnvironment

import java.io.File


class LuaBuilder(sourceCode: LuaSourceCode) {
    // This needs to do what build.bat does but in Kotlin.
    init {
        // Safeguard, before anything else happens.
        PathEnvironment.backup()



        // This is where TDM-GCC is required. I haven't tested it on any version prior to 10.3.0.
        val process: Process = ProcessBuilder("mingw32-make", "PLAT=mingw")
            .directory(File(Settings.directories["builds"]!!["dir"].toString()))
            .inheritIO()
            .start()
    }
}