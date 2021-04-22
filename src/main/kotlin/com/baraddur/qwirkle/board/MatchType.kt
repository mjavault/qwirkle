package com.baraddur.qwirkle.board

import org.slf4j.LoggerFactory


abstract class MatchingGroup {
    abstract fun tryMatch(tile: Tile): Boolean

    companion object {
        fun of(a: Tile, b: Tile): MatchingGroup? {
            return when {
                a.shape == b.shape -> ShapeMatchingGroup(a)
                a.color == b.color -> ColorMatchingGroup(a)
                else -> null
            }
        }
    }
}

class ColorMatchingGroup(tile: Tile) : MatchingGroup() {
    private val set = HashSet<Tile.Shape>()
    private val color: Tile.Color = tile.color

    init {
        set.add(tile.shape)
    }

    override fun tryMatch(tile: Tile): Boolean {
        return if (tile.color == color) {
            set.add(tile.shape)
        } else false
    }
}

class ShapeMatchingGroup(tile: Tile) : MatchingGroup() {
    private val log = LoggerFactory.getLogger(ShapeMatchingGroup::class.java)
    private val set = HashSet<Tile.Color>()
    private val shape: Tile.Shape = tile.shape

    init {
        set.add(tile.color)
    }

    override fun tryMatch(tile: Tile): Boolean {
        log.debug("Trying: ${tile.shape} / ${tile.color} against $set")
        return if (tile.shape == shape) {
            set.add(tile.color)
        } else false
    }
}
