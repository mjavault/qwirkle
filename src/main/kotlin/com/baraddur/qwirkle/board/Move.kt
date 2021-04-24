package com.baraddur.qwirkle.board

data class Move(val type: Type, val player: Player, val tiles: List<Tile>, val score: Int) {
    enum class Type {
        PLAY,
        TRADE
    }
}
