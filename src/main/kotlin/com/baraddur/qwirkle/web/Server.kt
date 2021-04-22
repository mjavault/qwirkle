package com.baraddur.qwirkle.web

import com.baraddur.qwirkle.Configuration
import com.baraddur.qwirkle.GameManager
import com.baraddur.qwirkle.utils.render
import com.baraddur.qwirkle.utils.respondJson
import org.slf4j.LoggerFactory
import spark.Spark.webSocket
import spark.kotlin.*

class Server {
    private val log = LoggerFactory.getLogger(Server::class.java)

    fun run() {
        port(Configuration.port)
        staticFiles.location("/public")
        ignite()
        webSocket("/ws/game", WebSocketHandler::class.java)

        //add support for gzip compression
        after { response.header("Content-Encoding", "gzip") }
        get("/") {
            render("intro")
        }
        get("/new") {
            render("intro")
        }
        get("/error") {
            render("error")
        }
        get("/game") {
            val gameId = queryParams("id")
            val game = GameManager.get(gameId)
            if (game == null) {
                render("error")
            } else {
                render("game", mapOf("gameId" to gameId))
            }
        }
        post("/api/game") {
            val gameId = GameManager.create()
            respondJson { mapOf("gameId" to gameId) }
        }
    }
}


