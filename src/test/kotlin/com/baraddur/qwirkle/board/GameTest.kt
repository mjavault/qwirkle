package com.baraddur.qwirkle.board

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class GameTest {
    private val log = LoggerFactory.getLogger(GameTest::class.java)

    fun Board.play(x: Int, y: Int, direction: Char, vararg tiles: String): Int {
        val score = play(
            tiles.map { Tile(Tile.Shape.of(it[0])!!, Tile.Color.of(it[1])!!) },
            XY(x, y),
            direction.toDirection()
        )
        print()
        return score
    }

    fun Char.toDirection(): Direction {
        return when (this) {
            '^' -> Direction.UP
            'v' -> Direction.DOWN
            '<' -> Direction.LEFT
            '>' -> Direction.RIGHT
            else -> throw RuntimeException("Invalid char $this")
        }
    }

//    CIRCLE('●'),
//    CRISSCROSS('⨯'),
//    DIAMOND('◆'),
//    SQUARE('■'),
//    STARBURST('✸'),
//    CLOVER('♣');

    @Test
    fun testScoring() {
        log.info("Step A")
        val board = Board()
        var score = board.play(10, 10, 'v', "♣R", "◆R", "●R")
        assertEquals(false, board.hasError())
        assertEquals(3, score)
        board.commit()
        log.info("Step B")
        score = board.play(0, 3, '>', "■R", "■B", "■P")
        assertEquals(false, board.hasError())
        assertEquals(7, score)
        board.commit()
        log.info("Step C")
        score = board.play(1, 2, '>', "●B")
        assertEquals(false, board.hasError())
        assertEquals(4, score)
        board.commit()
        log.info("Step D")
        score = board.play(-1, 0, 'v', "♣G", "◆G")
        assertEquals(false, board.hasError())
        assertEquals(6, score)
        board.commit()
        log.info("Step E")
        score = board.play(-1, -1, 'v', "✸G", "●G")
        assertEquals(false, board.hasError())
        assertEquals(7, score)
        board.commit()
        log.info("Step F")
        score = board.play(3, 3, 'v', "■O", "■R")
        assertEquals(false, board.hasError())
        assertEquals(6, score)
        board.commit()
        log.info("Step G")
        score = board.play(-2, -1, '<', "✸Y", "✸O")
        assertEquals(false, board.hasError())
        assertEquals(3, score)
        board.commit()
        log.info("Step H")
        score = board.play(-3, 0, 'v', "⨯O", "◆O")
        assertEquals(false, board.hasError())
        assertEquals(3, score)
        board.commit()
        log.info("Step I")
        score = board.play(-2, 1, 'v', "◆Y", "●Y")
        assertEquals(false, board.hasError())
        assertEquals(10, score)
        board.commit()
        log.info("Step J")
        score = board.play(0, -1, 'v', "✸R")
        assertEquals(false, board.hasError())
        assertEquals(9, score)
        board.commit()
        log.info("Step K")
        score = board.play(-1, 4, '>', "⨯O", "⨯R", "⨯B")
        assertEquals(false, board.hasError())
        assertEquals(18, score)
        board.commit()
        log.info("Step L")
        score = board.play(4, 3, 'v', "■Y", "■B")
        assertEquals(false, board.hasError())
        assertEquals(9, score)
        board.commit()
    }

    @Test
    fun testMismatch() {
        log.info("Initial play:")
        val board = Board()
        var score = board.play(0, 0, 'v', "♣R", "◆R", "●R")
        assertEquals(false, board.hasError())
        assertEquals(3, score)
        board.commit()
        log.info("Second play:")
        score = board.play(1, 0, 'v', "■R", "■B", "■P")
        assertEquals(true, board.hasError())
        assertEquals(0, score)
        try {
            board.commit()
            fail("Commit must fail with an exception when there are pending errors")
        } catch (ex: RuntimeException) {
        }
    }

    @Test
    fun testTwiceSameColor() {
        log.info("Initial play:")
        val board = Board()
        var score = board.play(0, 0, 'v', "♣R", "♣G", "♣B")
        assertEquals(false, board.hasError())
        assertEquals(3, score)
        board.commit()
        log.info("Second play:")
        score = board.play(0, -1, '^', "♣R")
        assertEquals(true, board.hasError())
        assertEquals(0, score)
    }

    @Test
    fun testTwiceSameShape() {
        log.info("Initial play:")
        val board = Board()
        var score = board.play(0, 0, 'v', "♣R", "◆R", "●R")
        assertEquals(false, board.hasError())
        assertEquals(3, score)
        board.commit()
        log.info("Second play:")
        score = board.play(0, -1, '^', "♣R")
        assertEquals(true, board.hasError())
        assertEquals(0, score)
    }
}
