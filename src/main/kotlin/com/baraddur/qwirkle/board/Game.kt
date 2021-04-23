package com.baraddur.qwirkle.board

import com.baraddur.qwirkle.utils.pollRandom
import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.*

class Game {
    private val log = LoggerFactory.getLogger(Game::class.java)
    val board: Board = Board()
    val bag: LinkedList<Tile> = LinkedList()
    val created = OffsetDateTime.now()
    var ended: OffsetDateTime? = null
    val players = LinkedList<Player>()
    var currentPlayerId: Int = 0
    var pendingScore: Int = 0

    init {
        Tile.Color.values().forEach { color ->
            Tile.Shape.values().forEach { type ->
                repeat(3) {
                    bag.add(Tile(type, color))
                }
            }
        }
    }

    @JsonIgnore
    fun isExpired(): Boolean {
        val now = OffsetDateTime.now()
        return when {
            OffsetDateTime.now().isAfter(created.plusHours(24)) -> true
            ended != null -> now.isAfter(created.plusMinutes(10))
            else -> false
        }
    }

    fun isGameOver() = players.isNotEmpty() && bag.isEmpty() && players.any { it.hand.isEmpty() }

    fun getCurrentPlayer() = if (players.isNotEmpty()) players[currentPlayerId] else null

    fun addPlayer(name: String, color: PlayerColor) {
        if (players.size < 4) {
            addPlayer(Player(name, color))
        } else {
            throw RuntimeException("A player with the same color already exist.")
        }
    }

    private fun addPlayer(player: Player) {
        players.add(player)
        while (player.hand.size < 6 && bag.isNotEmpty()) {
            player.hand.add(bag.pollRandom())
        }
    }

    fun play(tileIds: List<String>, position: XY, direction: Direction) {
        val player = players[currentPlayerId]
        //make sure these tiles belong to the player, and map their id to actual objects
        val tiles = tileIds.mapNotNull { id -> player.hand.find { it.id == id } }
        if (tiles.size != tileIds.size) {
            throw RuntimeException("Invalid tiles.")
        }
        //play the tiles
        pendingScore = board.play(tiles, position, direction)
    }

    fun trade(tileIds: List<String>) {
        //prevent the user from accidentally clicking trade when they meant to click commit
        if (!board.isPending()) {
            val player = players[currentPlayerId]
            val aside = LinkedList<Tile>()
            tileIds.forEach { id ->
                player.hand.find { it.id == id }?.let { tile ->
                    aside.add(tile)
                    player.hand.remove(tile)
                }
            }
            //make sure we have enough tiles in the bag
            if (bag.size < aside.size) {
                player.hand.addAll(aside)
                throw RuntimeException("Not enough tiles in the bag - you can trade at most ${bag.size} tiles.")
            }
            //draw more tiles
            while (player.hand.size < 6) {
                player.hand.add(bag.pollRandom())
            }
            //put back the tiles that were put aside
            bag.addAll(aside)
            endTurn()
        } else {
            throw RuntimeException("You have tiles on the board, please clear them first before trading.")
        }
    }

    fun abort() {
        board.rollback()
    }

    fun commit() {
        if (board.isPending() && !board.hasError()) {
            val player = players[currentPlayerId]
            player.score += pendingScore
            player.hand.removeAll(board.getPendingTiles())
            board.commit()
            endTurn()
        } else {
            throw RuntimeException("This play is invalid.")
        }
    }

    private fun endTurn() {
        val player = players[currentPlayerId]
        log.info("Pass ($player)")
        //draw more tiles
        while (player.hand.size < 6 && bag.isNotEmpty()) {
            player.hand.add(bag.pollRandom())
        }
        //clean up state
        pendingScore = 0
        //move on to the next active player (or current player if game over)
        do {
            currentPlayerId++
            if (currentPlayerId >= players.size) {
                currentPlayerId = 0
            }
        } while (players[currentPlayerId] != player && players[currentPlayerId].hand.isEmpty())
        val newPlayer = players[currentPlayerId]
        log.info("New player ($newPlayer)")
        //update game over status
        if (isGameOver() && ended == null) {
            ended = OffsetDateTime.now()
        }
    }

    fun print() {
        board.print()
    }
}
