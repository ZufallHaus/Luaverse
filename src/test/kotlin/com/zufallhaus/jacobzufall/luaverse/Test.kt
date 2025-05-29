package com.zufallhaus.jacobzufall.luaverse

class Vector1(private val x: Int) {
    infix fun distanceTo(other: Vector1): Int {
        return x - other.x
    }
}

fun main() {
    val pointOne: Vector1 = Vector1(5)
    val pointTwo: Vector1 = Vector1(2)

    println(pointOne distanceTo pointTwo) // Output: 3
}
