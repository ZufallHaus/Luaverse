package com.zufallhaus.luaverse.lua

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import kotlin.collections.iterator

/**
 * A singleton object that handles interacting with lua.org to obtain different versions of Lua.
 */
object VersionHandler {
    // I think this should be private eventually?
    var luaVersionFiles: MutableMap<String, String> = mutableMapOf()

    init {
        skrape(HttpFetcher) {
            request { url = "https://www.lua.org/ftp/" }

            response {
                htmlDocument {
                    val files = findAll("a")
                        .filter { it.hasAttribute("href") }
                        .map { element ->
                            val name: String = element.text
                            val href: String = element.attribute("href")
                            name to href // Never heard of infix functions before.
                        }

                    files.forEach { (name, href) ->
                        if (name.startsWith("lua") && !name.startsWith("lua-all")) {
                            /*
                            By default, name appears as "lua-5.4.7.tar.gz. So the first thing we do is split lua off from
                            the rest of the string, and then take that string and split it at the dots so it looks like
                            ["5", "4", "7", "tar", "gz"].
                            */
                            val splitName: List<String> = name.split("-")[1].split(".")
                            /*
                            This adds href to luaVersionFiles with the key being its version. We know that the first index
                            will always be the first number of the version. But since some versions look like x.x.x while
                            others look like x.x, we need to know where the last number is. splitName.size - 2 will equal
                            either 4 or 5, which is where the last version number is.
                            */
                            luaVersionFiles[splitName.subList(0, splitName.size - 2).joinToString(".")] =
                                "https://www.lua.org/ftp/$href"
                        }
                    }
                }
            }
        }
    }

    fun getAvailableLuaVersions() {
        print("\n")
        println("If you do not see your desired Lua version, please navigate to https://www.lua.org/ftp/ and download the file manually.")
        println("--- AVAILABLE LUA VERSIONS ---")

        for ((name, _) in luaVersionFiles) {
            println(name)
        }

        println("--- END OF LIST ---")
        print("\n")
    }
}