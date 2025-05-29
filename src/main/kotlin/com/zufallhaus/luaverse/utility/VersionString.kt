package com.zufallhaus.luaverse.utility

class VersionString(version: String) {
    /*
    A user can input a version number in any format, so long as it delimited consistently. For example, 1.2.3 is okay,
    since it is delimited, and it only has one delimiter. 1.2-3 and 123 are not okay, but 1.24.1 and 1-24-1 are.
    */

    private var isLatest: Boolean = version.lowercase() == "latest"
    /*
    Converts it to a set so there's only one of each value, and then removes the numbers from that set to return
    a list with a size of 1, which we then get the 0th index of to find the delimiter.
    */
    private val delimiter: String = version.toSet().filter { !it.isDigit() }[0].toString()
    // Then, we turn "1.2.10" into "[1, 2, 10]".
    private val versionNumbers: List<String> = version.split(delimiter)

    /**
     * The version in delimited format (1.2.3).
     */
    var delimitedVersion: String

    /**
     * The version in raw format (123).
     */
    var rawVersion: String

    init {
        delimitedVersion = formatVersion(delimiter)
        rawVersion = formatVersion("")
    }

    private fun formatVersion(delimiter: String): String = if (isLatest) "latest" else versionNumbers.joinToString(delimiter)

    fun withDelimiter(delimiter: String): String = versionNumbers.joinToString(delimiter)
}