package com.catpuppyapp.puppygit.service.http.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

object HttpServer {
    private val lock = Mutex()
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine. Configuration>? = null

    suspend fun startServer() {
        lock.withLock {
            if(isServerRunning()) return

            server = embeddedServer(Netty, port = 8080) {
                install(ContentNegotiation) {
                    json(Json { prettyPrint = true }) // 设置 JSON 序列化
                }
                routing {
                    get("/ping") {
                        call.respond(mapOf("msg" to "pong")) // 返回 JSON 响应
                    }
                }
            }.start(wait = false)
        }
    }

    suspend fun stopServer() {
        lock.withLock {
            server?.stop(1000, 1500)
            delay(1500)
            server = null
            startServer()
        }
    }

    fun isServerRunning():Boolean {
        return server != null
    }

}
