package com.baraddur.qwirkle.web

import com.baraddur.qwirkle.GameManager
import com.baraddur.qwirkle.board.Direction
import com.baraddur.qwirkle.board.PlayerColor
import com.baraddur.qwirkle.board.XY
import com.baraddur.qwirkle.utils.objectMapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set


@WebSocket
class WebSocketHandler {

    @OnWebSocketConnect
    fun onConnect(session: Session) {
        log.info("New session from ${session.remoteAddress}")
    }

    @OnWebSocketClose
    fun onClose(session: Session, statusCode: Int, reason: String?) {
        log.info("Closing session from ${session.remoteAddress}: $reason")
        removeSession(session)
    }

    @OnWebSocketMessage
    fun onMessage(session: Session, message: String) {
        log.debug("Session ${session.remoteAddress}: $message")
        val event = objectMapper().readValue<Event>(message)
        when (event.topic) {
            "/api/ping" -> {
                send(session, mapOf("ping" to "pong"))
            }
            "/api/register" -> {
                val data = objectMapper().treeToValue<RegisterEvent>(event.data)!!
                GameManager.get(data.gameId)?.also { game ->
                    register(session, data.gameId)
                    send(session, mapOf("game" to game))
                } ?: send(session, mapOf("reload" to "error"))
            }
            "/api/game/join" -> {
                val data = objectMapper().treeToValue<JoinEvent>(event.data)!!
                sessions[session]?.let { gameId ->
                    GameManager.get(gameId)?.also { game ->
                        try {
                            game.addPlayer(data.name, data.color)
                        } catch (ex: Exception) {
                            send(session, mapOf("error" to ex.message))
                        }
                        send(gameId, mapOf("game" to game))
                    } ?: send(session, mapOf("reload" to "error"))
                }
            }
            "/api/game/refresh" -> {
                sessions[session]?.let { gameId ->
                    GameManager.get(gameId)?.also { game ->
                        send(session, mapOf("game" to game))
                    } ?: send(session, mapOf("reload" to "error"))
                }
            }
            "/api/game/turn/play" -> {
                val data = objectMapper().treeToValue<PlayEvent>(event.data)!!
                sessions[session]?.let { gameId ->
                    GameManager.get(gameId)?.also { game ->
                        try {
                            game.play(data.tiles, XY(data.x, data.y), data.direction)
                        } catch (ex: Exception) {
                            send(session, mapOf("error" to ex.message))
                        }
                        send(gameId, mapOf("game" to game))
                    } ?: send(session, mapOf("reload" to "error"))
                }
            }
            "/api/game/turn/commit" -> {
                sessions[session]?.let { gameId ->
                    GameManager.get(gameId)?.also { game ->
                        try {
                            game.commit()
                        } catch (ex: Exception) {
                            send(session, mapOf("error" to ex.message))
                        }
                        send(gameId, mapOf("game" to game))
                    } ?: send(session, mapOf("reload" to "error"))
                }
            }
            "/api/game/turn/abort" -> {
                sessions[session]?.let { gameId ->
                    GameManager.get(gameId)?.also { game ->
                        try {
                            game.abort()
                        } catch (ex: Exception) {
                            send(session, mapOf("error" to ex.message))
                        }
                        send(gameId, mapOf("game" to game))
                    } ?: send(session, mapOf("reload" to "error"))
                }
            }
            "/api/game/turn/trade" -> {
                val data = objectMapper().treeToValue<TradeEvent>(event.data)!!
                sessions[session]?.let { gameId ->
                    GameManager.get(gameId)?.also { game ->
                        try {
                            game.trade(data.tiles)
                        } catch (ex: Exception) {
                            send(session, mapOf("error" to ex.message))
                        }
                        send(gameId, mapOf("game" to game))
                    } ?: send(session, mapOf("reload" to "error"))
                }
            }
            "/api/game/ui/hover" -> {
                val data = objectMapper().treeToValue<HoverEvent>(event.data)!!
                sessions[session]?.let { gameId ->
                    GameManager.get(gameId)?.also { game ->
                        val player = game.players.find { it.color == data.color }!!
                        send(
                            gameId, mapOf(
                                "hover" to mapOf(
                                    "color" to data.color,
                                    "name" to player.name,
                                    "target" to data.target
                                )
                            ), session
                        )
                    } ?: send(session, mapOf("reload" to "error"))
                }
            }
        }
    }

    data class Event(val topic: String, val data: JsonNode)
    data class RegisterEvent(val gameId: String)
    data class JoinEvent(val color: PlayerColor, val name: String)
    data class PlayEvent(val tiles: List<String>, val x: Int, val y: Int, val direction: Direction)
    data class TradeEvent(val tiles: List<String>)
    data class HoverEvent(val target: String?, val color: PlayerColor)

    companion object {
        private val log = LoggerFactory.getLogger(WebSocketHandler::class.java)
        private val sessions = ConcurrentHashMap<Session, String>()

        @Synchronized
        fun send(gameId: String, map: Map<String, Any?>, exceptFor: Session? = null) {
            val it = sessions.iterator()
            while (it.hasNext()) {
                val kv = it.next()
                if (kv.value == gameId && kv.key != exceptFor) {
                    try {
                        send(kv.key, map)
                    } catch (t: Throwable) {
                        log.debug("Error: $t")
                        try {
                            kv.key.close()
                        } catch (t: Throwable) {
                            log.debug("Error: $t")
                        }
                        it.remove()
                    }
                }
            }
        }

        fun send(session: Session, map: Map<String, Any?>) {
            session.remote.sendString(objectMapper().writeValueAsString(map))
        }

        @Synchronized
        fun register(session: Session, gameId: String) {
            sessions[session] = gameId
            log.info("Registering session ${session.remoteAddress} to game $gameId")
        }

        @Synchronized
        fun removeSession(session: Session) {
            sessions.remove(session)
        }
    }
}
