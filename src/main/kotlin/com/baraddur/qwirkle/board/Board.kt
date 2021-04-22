package com.baraddur.qwirkle.board

import org.slf4j.LoggerFactory

class Board {
    private val log = LoggerFactory.getLogger(Board::class.java)
    val tiles = ArrayList<PlacedTile>()
    val bounds: Bounds = Bounds()

    fun play(tiles: List<Tile>, position: XY, direction: Direction): Int {
        //rollback any incomplete play
        rollback()
        //if this is the first move of the game, force a position of (0,0)
        val xy = if (this.tiles.isEmpty()) {
            XY(0, 0)
        } else {
            position
        }
        //put all tiles on the board, with a pending flag
        tiles.forEach { tile ->
            add(tile, xy.clone(), direction)
            xy.inc(direction)
        }
        updateBounds()
        return score()
    }

    private fun add(tile: Tile, position: XY, direction: Direction) {
        while (get(position) != null) {
            position.inc(direction)
        }
        tiles.add(PlacedTile(tile, position))
    }

    fun score(): Int {
        log.debug("Starting scoring")
        var error = false
        var score = 0
        var playAxis: Int? = null
        var connected = false
        //take each pending tile, and explore all 4 directions
        tiles.filter { it.pending }.forEach { pending ->
            Direction.pairs().forEachIndexed { i, axis ->
                if (playAxis != i) {
                    var lineScore = 1
                    var group: MatchingGroup? = null
                    axis.forEach { direction ->
                        val xy = pending.location.clone()
                        var neighbor: PlacedTile?
                        var more = true
                        do {
                            xy.inc(direction)
                            neighbor = get(xy)
                            if (neighbor != null) {
                                //make sure the pending tiles are connected to existing tiles
                                if (!neighbor.pending) {
                                    connected = true
                                }
                                //we keep track of the axis on which the play was made - this assumes tiles are on the same axis
                                if (playAxis == null && neighbor.pending) {
                                    playAxis = i
                                    log.debug("Detected play axis $i")
                                }
                                if (group == null) {
                                    group = MatchingGroup.of(pending.tile, neighbor.tile)
                                    log.debug("Matcher type: ${group?.javaClass?.simpleName}")
                                }
                                if (group?.tryMatch(neighbor.tile) == true) {
                                    lineScore += 1
                                } else {
                                    pending.error = true
                                    error = true
                                    log.debug("Found an invalid tile")
                                }
                            } else {
                                more = false
                            }
                        } while (more)
                    }
                    //a score of 1 means the tile is not connected to anything on this axis - it actually means 0
                    val correctedLineScore = when (lineScore) {
                        1 -> 0
                        6 -> 12
                        else -> lineScore
                    }
                    score += correctedLineScore
                    log.debug("Score this axis ($i): +$correctedLineScore -> $score")
                } else {
                    log.debug("Skipping axis $i")
                }
            }
            log.debug("Score after tile: $score")
        }
        if (isNotEmpty() && !connected) {
            error = true
            tiles.filter { it.pending }.forEach { it.error = true }
        }
        log.debug("Total score: $score (error=$error)")
        return if (error) 0 else score
    }

    fun isNotEmpty() = tiles.any { !it.pending }

    fun get(position: XY): PlacedTile? = tiles.find { it.location == position }

    fun getPendingTiles() = tiles.filter { it.pending }.map { it.tile }

    fun commit() {
        if (isPending() && !hasError()) {
            tiles.filter { it.pending }.forEach { it.pending = false }
        } else {
            throw RuntimeException("Cannot commit this play, it is invalid.")
        }
    }

    fun rollback() {
        tiles.removeIf { it.pending }
        tiles.filter { it.error }.forEach { it.error = false }
    }

    fun isPending() = tiles.any { it.pending }

    fun hasError() = tiles.any { it.error }

    private fun updateBounds() {
        //compute min/max for this matrix
        bounds.reset()
        tiles.forEach {
            bounds.expand(it.location)
        }
    }

    fun print() {
        updateBounds()
        //sort the tiles by y and then x
        val sorted = tiles.sortedWith { a, b ->
            if (a.location.y == b.location.y) {
                a.location.x - b.location.x
            } else {
                a.location.y - b.location.y
            }
        }.toMutableList()
        //iterate on all the tiles
        val xy = XY(bounds.left, bounds.top)
        while (xy.y <= bounds.bottom) {
            xy.x = bounds.left
            while (xy.x <= bounds.right) {
                if (sorted.first().location == xy) {
                    print(sorted.first().tile.shape.representation)
                    sorted.removeFirst()
                } else {
                    print(" ")
                }
                xy.x++
            }
            print("\n")
            xy.y++
        }
    }
}
