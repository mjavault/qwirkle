package com.baraddur.qwirkle.board

data class XY(var x: Int, var y: Int) {
    fun eq(x: Int, y: Int) = this.x == x && this.y == y

    fun inc(direction: Direction) {
        when (direction) {
            Direction.UP -> y--
            Direction.DOWN -> y++
            Direction.LEFT -> x--
            Direction.RIGHT -> x++
        }
    }

    fun clone(): XY = XY(x, y)
}

enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    companion object {
        fun pairs() = listOf(listOf(UP, DOWN), listOf(RIGHT, LEFT))
    }
}

data class PlacedTile(val tile: Tile, val location: XY) {
    var pending: Boolean = true
    var error: Boolean = false
}
