package com.baraddur.qwirkle.board

import java.util.*

data class Player(val name: String, val color: PlayerColor) {
    val hand = LinkedList<Tile>()
    var score: Int = 0
}
