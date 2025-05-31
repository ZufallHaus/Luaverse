package com.zufallhaus.luaverse.system

import com.zufallhaus.luaverse.Settings

import java.io.File

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Handles interaction with the Windows Registry for the purpose of modifying the path environment variable.
 */
object PathEnvironment {
    private const val REGISTRY_KEY: String = "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment"
    private const val ENVIRONMENT_VARIABLE: String = "Path"

    private val pathVarValues: List<String>
        // This method is a mess.
        get() {
            val process: Process = ProcessBuilder("cmd.exe", "/c", "reg query \"$REGISTRY_KEY\"").start()

            val output: MutableList<String> = process.inputStream.bufferedReader().readText().split("    ").toMutableList()
            process.waitFor()

            val pathValues: MutableList<String> = output[output.indexOf(ENVIRONMENT_VARIABLE) + 2].split(";").toMutableList()
            // Sometimes it shows escape characters at the end, which we don't want.
            if (pathValues.last() == "\r\n") pathValues.removeLast()
            // Removes any break-lines and such off the end.
            for (value in pathValues) value.trim()

            return pathValues.toList()
        }

    /**
     * Appends a new value to the path variable.
     * @param[textToAppend] the new value to be appended.
     * @return the exit value of the process represented by this Process object. By convention, the value 0 indicates normal termination.
     */
    private fun appendToSystemPath(textToAppend: String): Int {
        /**
         * Checks if the given String ends with a semicolon, and adds it to the end if it does not.
         * @param[text] The String to check.
         * @return The String with a semicolon at the end.
         */
        fun checkForSc(text: String): String {
            var validatedText: String = text
            if (!validatedText.endsWith(";")) validatedText += ";"
            return validatedText
        }

        var newPathValue: String = pathVarValues.joinToString(";")
        // Not sure how necessary this function call is.
        checkForSc(newPathValue)

        newPathValue += textToAppend
        checkForSc(newPathValue)

        val process: Process = ProcessBuilder("reg", "add", REGISTRY_KEY, "/v",
            ENVIRONMENT_VARIABLE, "/t", "REG_EXPAND_SZ", "/d", newPathValue, "/f").inheritIO().start()

        return process.waitFor()
    }

    /**
     * Sets the path variable to a new value.
     * @param[newPath] the new value to change the path to. This must be properly formatted (delineated by semicolons, usually).
     * @return the exit value of the process represented by this Process object. By convention, the value 0 indicates normal termination.
     */
    private fun overrideSystemPath(newPath: String): Int {
        val process: Process = ProcessBuilder("reg", "add", REGISTRY_KEY, "/v",
            ENVIRONMENT_VARIABLE, "/t", "REG_EXPAND_SZ", "/d", newPath,
            "/f").inheritIO().start()
        return process.waitFor()
    }

    /**
     * Creates a backup of the current Path system environment variable and stores it in a JSON file. This works as a
     * fail-safe in case something goes wrong so that a user can revert any unwanted changes. Ideally, this function
     * should be run before any changes are made.
     * @return if the backup was completed successfully or not.
     */
    fun backup(): Boolean {
        val file: File = File("${Settings.directories["backup"]!!["dir"].toString()}\\luaverse_path-backup_${System.currentTimeMillis()}.json")
        val prettyJson: Json = Json { prettyPrint = true }
        file.writeText(prettyJson.encodeToString(pathVarValues))

        if (file.exists()) {
            println("Backup created at $file")
            return true

        } else {
            println("Failed to create backup file in ${Settings.directories["backup"]!!["dir"]}.")
            return false
        }
    }

    /**
     * Restores a backup created by backup().
     * @param[backupFile] the file to restore, created by backup().
     * @param[hardRestore] if the environment variable should be restored EXACTLY as it was. If not, the missing values
     * will be simply added.
     * @return if the restore was completed successfully or not.
     */
    fun restore(backupFile: String, hardRestore: Boolean = false): Boolean {
        // Extra backup, in case someone restores the wrong file.
        println("Creating redundant backup.")
        backup()
        
        val fileToRestore: File = File("${Settings.directories["backup"]!!["dir"]}\\$backupFile")
        
        if (fileToRestore.exists()) {
            val jsonString: String = fileToRestore.readText()
            val json = Json { ignoreUnknownKeys = true }
            val restoredEnvValues: List<String> = json.decodeFromString<List<String>>(jsonString)

            // IntelliJ insists that this should be val, so I'm doing something wrong.
            val exitCode: Int

            /*
            There are two options for restoring the Path variable from a backup.
                1. Hard restore
                2. Soft restore

            A hard restore completely overwrites the Path variable so that it matches the backup EXACTLY. This means if
                any values have been added since the backup, they will not be there.

            A soft restore, on the other hand, doesn't check if values are in the Path variable that aren't in the backup.
                It only cares if there's values in the backup that aren't in the path variable. Therefore, any values
                you've removed since the backup will be restored, and any values you've added since the backup will be
                left alone.

            By default, a soft restore is performed. However, users can run a hard restore by adding "hard" to the end
                of the restore command, like "restore <backup_file.json> hard".
            */
            if (hardRestore) {
                println("Performing hard restore of $backupFile.")
                /*
                Takes the restoredEnvValues and converts it to a string delineated by semicolons, with a trailing
                semicolon since that's what Windows does. This is then loaded into the Path environment variable.
                */
                exitCode = overrideSystemPath(restoredEnvValues.joinToString(separator = ";") + ";")

            } else {
                println("Performing soft restore of $backupFile.")
                val valuesToRestore: MutableList<String> = mutableListOf()

                for (value in restoredEnvValues) {
                    if (value !in pathVarValues) {
                        valuesToRestore.add(value)
                    }
                }

                exitCode = appendToSystemPath(valuesToRestore.joinToString(";"))
            }

            if (exitCode != 0) {
                println("Failed to restore $backupFile.")

            } else {
                println("$backupFile was successfully restored.")
            }

            return true

        } else {
            println("Could not locate $backupFile in ${Settings.directories["backup"]!!["dir"]}.")
            return false
        }
    }

    /*
    TODO: The following four methods should be implemented to manage Lua in the Path.
        1. findVersion()
            This method should search the Path for the Lua version specified. It should return true or false, based on
            if the specified Lua version is in the Path.
        2. addVersion()
            This method should add the specified Lua version to the path if it does not already exist, which can be
            discovered by using the findVersion() method.
        3. removeVersion()
            This method should remove the specified Lua version from the path if it exists, which can be discovered by
            using the findVersion method().
        4. removeAllVersions()
            This method should remove all versions of Lua from the Path.
    */
    /**
     * Checks if the given version of Lua is already included in the path or not.
     * @param[version] the version of Lua to look for.
     * @return if the specified version of Lua is included already or not.
     */
    private fun findVersion(version: String): Boolean {
        for (pathVariable in pathVarValues) {
            val pathObjects: List<String> = pathVariable.split("\\")

            if (pathObjects[pathObjects.size - 2] == "lua$version") {
                return true
            }
        }

        return false
    }

    /**
     * Tries to include the given Lua version in the system Path. Note that this method does not build Lua, and should
     * only be called after building Lua.
     * @param[version] the version of Lua to add to the path.
     * @return if the version was added to the Path or not.
     */
    fun addVersion(version: String): Boolean {
        // Converts x.x.x into xxx.
        val cleanVersion: String = version.split(".").joinToString("")
        val binPath: String = "${Settings.directories["build"]!!["dir"]}\\Lua$cleanVersion\\bin"

        if (!findVersion(cleanVersion)) {
            return appendToSystemPath(binPath) == 0
        }

        return false
    }

    fun removeVersion(version: String) {
        // Versions are usually displayed as "x.x.x" when downloaded, but they are installed as "xxx".
        val cleanVersion: String = version.split(".").joinToString("")
        val binPath: String = "${Settings.directories["build"]!!["dir"]}\\Lua$cleanVersion\\bin"
        val newPathValues: MutableList<String> = pathVarValues.toMutableList()

        // Is this for loop necessary? Or can kotlin.collections.MutableList.remove() stand on its own?
        for (value in newPathValues) {
            if (binPath == value) {
                newPathValues.remove(value)
            }
        }

        overrideSystemPath(newPathValues.joinToString(";") + ";")
    }
}