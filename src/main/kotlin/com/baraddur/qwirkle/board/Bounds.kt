package com.baraddur.qwirkle.board

data class Bounds(var top: Int = -1, var left: Int = -1, var bottom: Int = -1, var right: Int = -1) {
    fun reset() {
        top = -1
        left = -1
        bottom = -1
        right = -1
    }

    fun expand(xy: XY) {
        if (xy.x > right) right = xy.x
        if (xy.x < left) left = xy.x
        if (xy.y > bottom) bottom = xy.y
        if (xy.y < top) top = xy.y
    }
}
