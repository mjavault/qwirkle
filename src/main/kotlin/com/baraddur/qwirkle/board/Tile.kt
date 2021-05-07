package com.baraddur.qwirkle.board

import java.util.*

data class Tile(var shape: Shape, var color: Color) {
    val id = UUID.randomUUID().toString()

    enum class Color(val rgb: String) {
        RED("#FF0000"),
        ORANGE("#FFA500"),
        YELLOW("#FFFF00"),
        GREEN("#00FF00"),
        BLUE("#0000FF"),
        PURPLE("#800080");

        companion object {
            fun of(char: Char) = values().find { it.name[0] == char }
        }
    }

    enum class Shape(val representation: Char) {
        CIRCLE('●'),
        CRISSCROSS('⨯'),
        DIAMOND('◆'),
        SQUARE('■'),
        STARBURST('✸'),
        CLOVER('♣');

        companion object {
            fun of(char: Char) = values().find { it.representation == char }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return id == (other as Tile).id
    }

    override fun hashCode(): Int = id.hashCode()
}
