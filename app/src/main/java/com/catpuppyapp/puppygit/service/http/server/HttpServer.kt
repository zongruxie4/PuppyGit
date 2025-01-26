package com.catpuppyapp.puppygit.service.http.server

import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
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

private const val TAG = "HttpServer"

object HttpServer {
    private val lock = Mutex()
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine. Configuration>? = null

    /**
     * expect do start/stop/restart server in `act`
     */
    suspend fun doActWithLock(act:suspend HttpServer.() -> Unit) {
        lock.withLock {
            this.act()
        }
    }

    suspend fun startServer(settings: AppSettings) {
        if(isServerRunning()) return

        server = embeddedServer(Netty, host = settings.httpService.listenHost, port = settings.httpService.listenPort) {
            install(ContentNegotiation) {
                //忽略对象里没有的key；编码默认值；紧凑格式
                json(Json{ ignoreUnknownKeys = true; encodeDefaults=true; prettyPrint = false})
            }
            routing {
                get("/ping") {
                    call.respond(createSuccessResult("pong"))
                }

                /**
                 * query params:
                 *  repoNameOrId: repo name or id，优先查name，若无匹配，查id
                 *  username: using for create commit, if null, will use PuppyGit settings
                 *  email: using for create commit, if nul, will use PuppyGit settings
                 *  masterPass: your master password, if have
                 *  all: 1 or 0, if 1 will do action to all repos and ignore repoName, else, will only do action by repoName and do nothing if repoName non-matched
                 *  forceUseIdMatchRepo: 1 or 0, if true, will force match repo by repo id, else will match by name first, if no match, will match by id
                 *
                 * e.g.
                 * request: http://127.0.0.1/pull?repoNameOrId=abc&username=username&email=email&masterPass=your_master_pass_if_have
                 */
                get("/pull") {
                    val repoNameOrId = call.request.queryParameters.get("repoNameOrId")
                    if(repoNameOrId == null || repoNameOrId.isBlank()) {
                        call.respond(createErrResult("invalid repo name or id"))
                        return@get
                    }

                    val forceUseIdMatchRepo = call.request.queryParameters.get("forceUseIdMatchRepo") == "1"


                    // 查询仓库是否存在
                    // 尝试获取仓库锁，若获取失败，返回仓库正在执行其他操作
                    doJobThenOffLoading {
                        val db = AppModel.dbContainer
                        val repoRet = db.repoRepository.getByNameOrId(repoNameOrId, forceUseIdMatchRepo)
                        if(repoRet.hasError()) {
                            call.respond(createErrResult(repoRet.msg))
                            return@doJobThenOffLoading
                        }

                        val repo = repoRet.data!!

                        Libgit2Helper.doActWithRepoLock(repo) {

                        }

                    }
                }

                /**
                 * query params:
                 *  repoNameOrId: repo name or id，优先查name，若无匹配，查id
                 *  masterPass: your master password, if have
                 *  force: 1 enable , 0 disable, if null, will disable (as 0)
                 *  all: 1 or 0, 1 do act to all Repos, else do act to which matched the repoName
                 *  forceUseIdMatchRepo: 1 or 0, if true, will force match repo by repo id, else will match by name first, if no match, will match by id
                 *
                 * e.g.
                 * request: http://127.0.0.1/push?repoNameOrId=abc&masterPass=your_master_pass_if_have
                 */
                get("/push") {
                    // 查询仓库是否存在
                    // 尝试获取仓库锁，若获取失败，返回仓库正在执行其他操作
                    doJobThenOffLoading {

                    }
                }
            }
        }.start(wait = false)

        MyLog.w(TAG, "Http Server started")
    }

    suspend fun stopServer() {
        server?.stop(1000, 1500)
        delay(1500)
        MyLog.w(TAG, "Http Server stopped")

    }

    suspend fun restartServer(settings: AppSettings) {
        stopServer()
        server = null
        startServer(settings)

        MyLog.w(TAG, "Http Server restarted")
    }

    fun isServerRunning():Boolean {
        return server != null
    }

}
