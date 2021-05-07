package com.baraddur.qwirkle.board

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class TileTest {

    @Test
    fun testEquals() {
        val t1 = Tile(Tile.Shape.CRISSCROSS, Tile.Color.ORANGE)
        val t2 = Tile(Tile.Shape.CRISSCROSS, Tile.Color.ORANGE)
        val list = LinkedList<Tile>()
        list.add(t1)
        list.add(t2)
        assertEquals(t1.id, list[0].id)
        list.remove(t2)
        assertEquals(t1.id, list[0].id)
    }
}
