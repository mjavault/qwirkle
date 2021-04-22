package com.baraddur.qwirkle

import com.baraddur.qwirkle.board.Game
import com.baraddur.qwirkle.utils.NameGenerator
import org.slf4j.LoggerFactory
import java.time.OffsetDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class GameManager {
    companion object {
        private val log = LoggerFactory.getLogger(GameManager::class.java)
        private val instances: MutableMap<String, Game> = ConcurrentHashMap()
        private val executor = Executors.newScheduledThreadPool(1)

        init {
            executor.scheduleAtFixedRate({
                //purge any old games
                try {
                    val now = OffsetDateTime.now()
                    val it = instances.iterator()
                    while (it.hasNext()) {
                        val kv = it.next()
                        if (kv.value.isGameOver() || now.isAfter(kv.value.created.plusHours(24))) {
                            log.info("Purging old game ${kv.key} (gameOver=${kv.value.isGameOver()})")
                            it.remove()
                        }
                    }
                } catch (ex: Exception) {
                    log.error("Exception while performing cleanup task: $ex")
                }
            }, 1, 1, TimeUnit.MINUTES)
        }

        fun get(key: String) = instances[key]

        @Synchronized
        fun create(): String {
            var key: String
            do {
                key = NameGenerator.get()
            } while (instances.keys.contains(key))
            instances[key] = Game()
            return key
        }
    }
}
