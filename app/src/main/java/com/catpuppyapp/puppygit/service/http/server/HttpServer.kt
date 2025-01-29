package com.catpuppyapp.puppygit.service.http.server

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.IntentCons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dto.genConfigDto
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.notification.NormalNotify
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.JsonUtil
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Libgit2Helper.Companion.getAheadBehind
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.dbIntToBool
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.encrypt.MasterPassUtil
import com.catpuppyapp.puppygit.utils.genHttpHostPortStr
import com.catpuppyapp.puppygit.utils.getSecFromTime
import com.github.git24j.core.Oid
import com.github.git24j.core.Repository
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.host
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

private const val TAG = "HttpServer"
private const val waitInMillSecIfApiBusy = 3000L


object HttpServer {
    private val lock = Mutex()
    private var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine. Configuration>? = null

    /**
     * expect do start/stop/restart server in `act`
     */
    suspend fun <T> doActWithLock(act:suspend HttpServer.() -> T):T {
        lock.withLock {
            return this.act()
        }
    }

    suspend fun startServer(settings: AppSettings):Exception? {
        if(isServerRunning()) return null

        try {
            server = embeddedServer(Netty, host = settings.httpService.listenHost, port = settings.httpService.listenPort) {
                install(ContentNegotiation) {
                    //忽略对象里没有的key；编码默认值；紧凑格式
                    json(Json{ ignoreUnknownKeys = true; encodeDefaults=true; prettyPrint = false})
                }
                routing {
                    get("/status") {
                        call.respond(createSuccessResult("online"))
                    }

                    // for test
                    get("/echo/{msg}") {
                        call.respond(createSuccessResult(call.parameters.get("msg") ?: ""))
                    }

                    // not work, service launch app may require float window permission
//                    get("/launchApp") {
//                        HttpService.launchApp()
//                    }

                    /**
                     * query params:
                     *  repoNameOrId: repo name or id, match by name first, if none, will match by id
                     *  gitUsername: using for create commit, if null, will use PuppyGit settings
                     *  gitEmail: using for create commit, if null, will use PuppyGit settings
                     *  forceUseIdMatchRepo: 1 enable or 0 disable, default 0, if enable, will force match repo by repo id, else will match by name first, if no match, then match by id
                     *  token: token is required

                     * e.g.
                     * request: http://127.0.0.1/pull?repoNameOrId=abc
                     */
                    get("/pull") {
                        var repoNameOrIdForLog:String? = null
                        var repoForLog:RepoEntity? = null
                        val routeName = "'/pull'"

                        try {
                            val settings = SettingsUtil.getSettingsSnapshot()
                            tokenPassedOrThrowException(call, routeName, settings)

                            val repoNameOrId = call.request.queryParameters.get("repoNameOrId")
                            if(repoNameOrId == null || repoNameOrId.isBlank()) {
                                throw RuntimeException("invalid repo name or id")
                            }

                            repoNameOrIdForLog = repoNameOrId


                            if(settings.httpService.showNotifyWhenProgress) {
                                sendProgressNotification(repoNameOrId, "pulling...")
                            }


                            val forceUseIdMatchRepo = call.request.queryParameters.get("forceUseIdMatchRepo") == "1"
                            // get git username and email for merge, if request doesn't contains them, will use PuppyGit app settings
                            val gitUsernameFromUrl = call.request.queryParameters.get("gitUsername") ?:""
                            val gitEmailFromUrl = call.request.queryParameters.get("gitEmail") ?:""


                            val db = AppModel.dbContainer
                            val repoRet = db.repoRepository.getByNameOrId(repoNameOrId, forceUseIdMatchRepo)
                            if(repoRet.hasError()) {
                                throw RuntimeException(repoRet.msg)
                            }

                            val repoFromDb = repoRet.data!!
                            repoForLog = repoFromDb

                            //执行请求，可能时间很长，所以开个协程，直接返回响应即可
                            doJobThenOffLoading {
                                pullRepoList(listOf(repoFromDb), routeName, gitUsernameFromUrl, gitEmailFromUrl)
                            }

                            call.respond(createSuccessResult())
                        }catch (e:Exception) {
                            val errMsg = e.localizedMessage ?: "unknown err"
                            call.respond(createErrResult(errMsg))

                            if(repoForLog!=null) {
                                createAndInsertError(repoForLog.id, "pull by api $routeName err: $errMsg")
                            }

                            if(settings.httpService.showNotifyWhenErr) {
                                sendNotification("$routeName err", errMsg, Cons.selectedItem_ChangeList, repoForLog?.id ?: "")
                            }

                            MyLog.e(TAG, "method:GET, route:$routeName, repoNameOrId=$repoNameOrIdForLog, err=${e.stackTraceToString()}")
                        }

                    }


                    /**
                     * query params:
                     *  repoNameOrId: repo name or id，优先查name，若无匹配，查id
                     *  gitUsername: using for create commit, if null, will use PuppyGit settings
                     *  gitEmail: using for create commit, if null, will use PuppyGit settings
                     *  force: force push, 1 enable , 0 disable, if null, will disable (as 0)
                     *  forceUseIdMatchRepo: 1 enable or 0 disable, default 0, if enable, will force match repo by repo id, else will match by name first, if no match, then match by id
                     *  token: token is required
                     *  autoCommit: 1 enable or 0 disable, default 1: if enable and no conflict items exists, will auto commit all changes,
                     *    and will check index, if index empty, will not pushing;
                     *    if disable, will only do push, no commit changes,
                     *    no index empty check, no conflict items check.
                     *
                     * e.g.
                     * request: http://127.0.0.1/push?repoNameOrId=abc
                     */
                    get("/push") {
                        var repoNameOrIdForLog:String? = null
                        var repoForLog:RepoEntity? = null
                        val routeName = "'/push'"

                        try {
                            val settings = SettingsUtil.getSettingsSnapshot()
                            tokenPassedOrThrowException(call, routeName, settings)

                            val repoNameOrId = call.request.queryParameters.get("repoNameOrId")
                            if(repoNameOrId == null || repoNameOrId.isBlank()) {
                                throw RuntimeException("invalid repo name or id")
                            }

                            repoNameOrIdForLog = repoNameOrId


                            if(settings.httpService.showNotifyWhenProgress) {
                                sendProgressNotification(repoNameOrId, "pushing...")
                            }


                            val forceUseIdMatchRepo = call.request.queryParameters.get("forceUseIdMatchRepo") == "1"


                            //这个只要不明确传0，就是启用
                            val autoCommit = call.request.queryParameters.get("autoCommit") != "0"

                            // force push or no
                            val force = call.request.queryParameters.get("force") == "1"


                            // get git username and email for merge
                            val gitUsernameFromUrl = call.request.queryParameters.get("gitUsername") ?:""
                            val gitEmailFromUrl = call.request.queryParameters.get("gitEmail") ?:""

                            // 查询仓库是否存在
                            // 尝试获取仓库锁，若获取失败，返回仓库正在执行其他操作

                            val db = AppModel.dbContainer
                            val repoRet = db.repoRepository.getByNameOrId(repoNameOrId, forceUseIdMatchRepo)
                            if(repoRet.hasError()) {
                                throw RuntimeException(repoRet.msg)
                            }

                            val repoFromDb = repoRet.data!!
                            repoForLog = repoFromDb

                            doJobThenOffLoading {
                                pushRepoList(listOf(repoFromDb),routeName, gitUsernameFromUrl, gitEmailFromUrl, autoCommit, force)
                            }

                            call.respond(createSuccessResult())

                        }catch (e:Exception) {
                            val errMsg = e.localizedMessage ?: "unknown err"
                            call.respond(createErrResult(errMsg))

                            if(repoForLog!=null) {
                                createAndInsertError(repoForLog!!.id, "push by api $routeName err: $errMsg")
                            }

                            if(settings.httpService.showNotifyWhenErr) {
                                sendNotification("$routeName err", errMsg, Cons.selectedItem_ChangeList, repoForLog?.id ?: "")
                            }

                            MyLog.e(TAG, "method:GET, route:$routeName, repoNameOrId=$repoNameOrIdForLog, err=${e.stackTraceToString()}")
                        }

                    }

                    /**
                     * query params:
                     *  gitUsername: using for create commit, if null, will use PuppyGit settings
                     *  gitEmail: using for create commit, if null, will use PuppyGit settings
                     *  token: token is required

                     * e.g.
                     * request: http://127.0.0.1/pullAll?gitUsername=username&gitEmail=email&token=your_token
                     */
                    get("/pullAll") {
                        val routeName = "'/pullAll'"

                        try {
                            val settings = SettingsUtil.getSettingsSnapshot()
                            tokenPassedOrThrowException(call, routeName, settings)


                            if(settings.httpService.showNotifyWhenProgress) {
                                sendProgressNotification("PuppyGit", "pulling all...")
                            }


                            // get git username and email for merge, if request doesn't contains them, will use PuppyGit app settings
                            val gitUsernameFromUrl = call.request.queryParameters.get("gitUsername") ?:""
                            val gitEmailFromUrl = call.request.queryParameters.get("gitEmail") ?:""


                            //执行请求，可能时间很长，所以开个协程，直接返回响应即可
                            doJobThenOffLoading {
                                pullRepoList(AppModel.dbContainer.repoRepository.getAll(), routeName, gitUsernameFromUrl, gitEmailFromUrl)
                            }

                            call.respond(createSuccessResult())
                        }catch (e:Exception) {
                            val errMsg = e.localizedMessage ?: "unknown err"
                            call.respond(createErrResult(errMsg))

                            if(settings.httpService.showNotifyWhenErr) {
                                sendNotification("$routeName err", errMsg, Cons.selectedItem_Repos ,"")
                            }

                            MyLog.e(TAG, "method:GET, route:$routeName, err=${e.stackTraceToString()}")
                        }
                    }

                    /**
                     * query params:
                     *  gitUsername: using for create commit, if null, will use PuppyGit settings
                     *  gitEmail: using for create commit, if null, will use PuppyGit settings
                     *  autoCommit: same as '/push'
                     *  force: 1 enable , 0 disable, default 0
                     *  token: token is required
                     *
                     * e.g.
                     * request: http://127.0.0.1/pushAll?token=your_token
                     */
                    get("/pushAll") {
                        val routeName = "'/pushAll'"

                        try {
                            val settings = SettingsUtil.getSettingsSnapshot()
                            tokenPassedOrThrowException(call, routeName, settings)

                            if(settings.httpService.showNotifyWhenProgress) {
                                sendProgressNotification("PuppyGit", "pushing all...")
                            }

                            //这个只要不明确传0，就是启用
                            val autoCommit = call.request.queryParameters.get("autoCommit") != "0"

                            // force push or no
                            val force = call.request.queryParameters.get("force") == "1"


                            // get git username and email for merge
                            val gitUsernameFromUrl = call.request.queryParameters.get("gitUsername") ?:""
                            val gitEmailFromUrl = call.request.queryParameters.get("gitEmail") ?:""

                            // 查询仓库是否存在
                            // 尝试获取仓库锁，若获取失败，返回仓库正在执行其他操作
                            doJobThenOffLoading {
                                pushRepoList(AppModel.dbContainer.repoRepository.getAll(),routeName, gitUsernameFromUrl, gitEmailFromUrl, autoCommit, force)
                            }

                            call.respond(createSuccessResult())

                        }catch (e:Exception) {
                            val errMsg = e.localizedMessage ?: "unknown err"
                            call.respond(createErrResult(errMsg))

                            if(settings.httpService.showNotifyWhenErr) {
                                sendNotification("$routeName err", errMsg, Cons.selectedItem_Repos, "")
                            }

                            MyLog.e(TAG, "method:GET, route:$routeName, err=${e.stackTraceToString()}")
                        }


                    }
                }
            }.start(wait = false) // 不能传true，会block整个程序

            MyLog.w(TAG, "Http Server started on '${genHttpHostPortStr(settings.httpService.listenHost, settings.httpService.listenPort.toString())}'")
            return null
        }catch (e:Exception) {
            //端口占用之类的能捕获到错误，看来即使非阻塞也并非一启动就立即返回，应该是成功绑定端口ip后才返回
            MyLog.e(TAG, "Http Server start failed, err=${e.stackTraceToString()}")
            return e
        }
    }

    /**
     * will throw exception if token bad
     */
    private fun tokenPassedOrThrowException(call: RoutingCall, routeName: String, settings: AppSettings) {
        val callerIp = call.request.host()
        val token = call.request.queryParameters.get("token")
        val tokenCheckRet = tokenPassedOrThrowException(token, callerIp, settings)
        if (tokenCheckRet.hasError()) {
            // log the query params maybe better?
            MyLog.e(TAG, "request rejected: route=$routeName, ip=$callerIp, token=$token, reason=${tokenCheckRet.msg}")
            throw RuntimeException(tokenCheckRet.msg)
        }
    }

    suspend fun stopServer():Exception? {
        if(server == null) {
            MyLog.w(TAG, "server is null, stop canceled")
            return null
        }

        try {
            server?.stop(0, 0)  //立即停止服务器
            server = null

            MyLog.w(TAG, "Http Server stopped")
            return null
        }catch (e:Exception) {
            MyLog.e(TAG, "Http Server stop failed: ${e.stackTraceToString()}")
            return e
        }
    }

    suspend fun restartServer(settings: AppSettings):Exception? {
        stopServer()
        return startServer(settings)
    }

    fun isServerRunning():Boolean {
        //这检查的是协程是否Active，协程还在运行，服务器就在运行，大概是这个逻辑吧？
        return server?.application?.isActive == true

        //这个不行，服务器正在启动，连接不通，但不久就上线了，用这个会误认为服务器不在线，误启动
//        val settings = SettingsUtil.getSettingsSnapshot()
//        return checkApiRunning("${genHttpHostPortStr(settings.httpService.listenHost, settings.httpService.listenPort)}/ping", 2)
    }

    fun getApiJson(repoEntity:RepoEntity, settings: AppSettings):String {
        return JsonUtil.j2PrettyPrint.let {
            it.encodeToString(
                it.serializersModule.serializer(),

                genConfigDto(repoEntity, settings)
            )
        }
    }


}

/**
 * 如果ip同时在白名单和黑名单，将允许连接
 * 如果设置项中的token为空字符串，将允许所有连接
 */
private fun tokenPassedOrThrowException(token:String?, ip:String, settings: AppSettings): Ret<Unit?> {
    //这个错误信息不要写太具体哪个出错，不然别人可以探测你的ip和token名单
    val errMsg = "invalid token or ip blocked"
    val errRet:Ret<Unit?> = Ret.createError(null, errMsg)

    // check token
    val tokenList = settings.httpService.tokenList
    //全空格token不被允许，不过"a b c"这种可以允许，首尾空格将被去除，但中间的会保留
    if(token == null || tokenList.isEmpty() || token.isBlank() || tokenList.contains(token).not()) {
        // token empty will reject all requests
        return errRet
    }

    val whiteList = settings.httpService.ipWhiteList
    //匹配到通配符 或者 匹配到请求者的ip 则 放行
    if(whiteList.isEmpty() || ip.isBlank() || whiteList.find { it == "*" || it == ip } == null) {
        return errRet
    }

    return Ret.createSuccess(null)
}

/**
 * 启动app并定位到ChangeList和指定仓库
 * @param startPage 是页面id, `Cons.selectedItem_` 开头的那几个变量
 * @param startRepoId 虽然是repo id，但实际上查询的时候可能会匹配id和repoName，但是，这里还是应该尽量传id而不是repoName
 */
private fun sendNotification(title:String, msg:String, startPage:Int, startRepoId:String) {
    NormalNotify.sendNotification(
        null,
        title,
        msg,
        NormalNotify.createPendingIntent(
            null,
            mapOf(
                IntentCons.ExtrasKey.startPage to startPage.toString(),
                IntentCons.ExtrasKey.startRepoId to startRepoId
            )
        )
    )
}

private fun sendSuccessNotification(title:String?, msg:String?, startPage:Int?, repoId:String?) {
    //Never页面永远不会被处理，变相等于启动app时回到上次退出时的页面
    sendNotification(title ?: "PuppyGit", msg ?: "Success", startPage ?: Cons.selectedItem_Never, repoId ?: "")
}

private fun sendProgressNotification(repoNameOrId:String, progress:String) {
    sendNotification(repoNameOrId, progress, Cons.selectedItem_Never, "")
}

val throwRepoBusy = { _:Mutex ->
    throw RuntimeException("repo busy, plz try later")
}


private suspend fun pullRepoList(
    repoList:List<RepoEntity>,
    routeName: String,
    gitUsernameFromUrl:String,
    gitEmailFromUrl:String
) {
    val db = AppModel.dbContainer
    val settings = SettingsUtil.getSettingsSnapshot()
    val masterPassword = MasterPassUtil.get(AppModel.realAppContext)

    repoList.forEach { repoFromDb ->
        try {
            Libgit2Helper.doActWithRepoLock(repoFromDb, waitInMillSec = waitInMillSecIfApiBusy, onLockFailed = throwRepoBusy) {
                if(dbIntToBool(repoFromDb.isDetached)) {
                    throw RuntimeException("repo is detached")
                }

                if(!Libgit2Helper.isValidGitRepo(repoFromDb.fullSavePath)) {
                    throw RuntimeException("invalid git repo")
                }

                Repository.open(repoFromDb.fullSavePath).use { gitRepo ->
                    val upstream = Libgit2Helper.getUpstreamOfBranch(gitRepo, repoFromDb.branch)
                    if(upstream.remote.isBlank() || upstream.branchRefsHeadsFullRefSpec.isBlank()) {
                        throw RuntimeException("invalid upstream")
                    }

                    // get fetch credential
                    val credential = Libgit2Helper.getRemoteCredential(
                        db.remoteRepository,
                        db.credentialRepository,
                        repoFromDb.id,
                        upstream.remote,
                        trueFetchFalsePush = true,
                        masterPassword = masterPassword
                    )

                    // fetch
                    Libgit2Helper.fetchRemoteForRepo(gitRepo, upstream.remote, credential, repoFromDb)


                    // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
                    val repoDb = AppModel.dbContainer.repoRepository
                    repoDb.updateLastUpdateTime(repoFromDb.id, getSecFromTime())

                    // get git username and email for merge
                    val (username, email) = if(gitUsernameFromUrl.isNotBlank() && gitEmailFromUrl.isNotBlank()) {
                        Pair(gitUsernameFromUrl, gitEmailFromUrl)
                    }else{
                        val (gitUsernameFromConfig, gitEmailFromConfig) = Libgit2Helper.getGitUsernameAndEmail(gitRepo)
                        val finallyUsername = gitUsernameFromUrl.ifBlank { gitUsernameFromConfig }

                        val finallyEmail = gitEmailFromUrl.ifBlank { gitEmailFromConfig }

                        Pair(finallyUsername, finallyEmail)
                    }

                    if(username == null || username.isBlank()) {
                        throw RuntimeException("git username invalid")
                    }

                    if(email == null || email.isBlank()) {
                        throw RuntimeException("git email invalid")
                    }

                    val remoteRefSpec = Libgit2Helper.getUpstreamRemoteBranchShortNameByRemoteAndBranchRefsHeadsRefSpec(
                        upstream!!.remote,
                        upstream.branchRefsHeadsFullRefSpec
                    )

                    //merge
                    val mergeRet = Libgit2Helper.mergeOneHead(
                        gitRepo,
                        remoteRefSpec,
                        username,
                        email,
                        settings = settings
                    )

                    if(mergeRet.hasError()) {
                        throw RuntimeException(mergeRet.msg)
                    }

                }


                if(settings.httpService.showNotifyWhenSuccess) {
                    sendSuccessNotification(repoFromDb.repoName, "pull successfully", Cons.selectedItem_ChangeList, repoFromDb.id)
                }
            }
        }catch (e:Exception) {
            val errMsg = e.localizedMessage ?: "unknown err"

            createAndInsertError(repoFromDb.id, "pull by api $routeName err: $errMsg")

            if(settings.httpService.showNotifyWhenErr) {
                sendNotification("pull err", errMsg, Cons.selectedItem_ChangeList, repoFromDb.id)
            }

            MyLog.e(TAG, "route:$routeName, repoName=${repoFromDb.repoName}, err=${e.stackTraceToString()}")
        }

    }

}

private suspend fun pushRepoList(
    repoList:List<RepoEntity>,
    routeName: String,
    gitUsernameFromUrl:String,
    gitEmailFromUrl:String,
    autoCommit:Boolean,
    force:Boolean,
) {
    val db = AppModel.dbContainer
    val settings = SettingsUtil.getSettingsSnapshot()
    val masterPassword = MasterPassUtil.get(AppModel.realAppContext)

    repoList.forEach {repoFromDb ->
        try {
            Libgit2Helper.doActWithRepoLock(repoFromDb, waitInMillSec = waitInMillSecIfApiBusy, onLockFailed = throwRepoBusy) {
                if(dbIntToBool(repoFromDb.isDetached)) {
                    throw RuntimeException("repo is detached")
                }

                if(!Libgit2Helper.isValidGitRepo(repoFromDb.fullSavePath)) {
                    throw RuntimeException("invalid git repo")
                }

                Repository.open(repoFromDb.fullSavePath).use { gitRepo ->
                    val repoState = gitRepo.state()
                    if(repoState != Repository.StateT.NONE) {
                        throw RuntimeException("repository state is '$repoState', expect 'NONE'")
                    }

                    val upstream = Libgit2Helper.getUpstreamOfBranch(gitRepo, repoFromDb.branch)
                    if(upstream.remote.isBlank() || upstream.branchRefsHeadsFullRefSpec.isBlank()) {
                        throw RuntimeException("invalid upstream")
                    }

                    if(autoCommit) {
                        val (username, email) = if(gitUsernameFromUrl.isNotBlank() && gitEmailFromUrl.isNotBlank()) {
                            Pair(gitUsernameFromUrl, gitEmailFromUrl)
                        }else{
                            val (gitUsernameFromConfig, gitEmailFromConfig) = Libgit2Helper.getGitUsernameAndEmail(gitRepo)
                            val finallyUsername = gitUsernameFromUrl.ifBlank { gitUsernameFromConfig }

                            val finallyEmail = gitEmailFromUrl.ifBlank { gitEmailFromConfig }

                            Pair(finallyUsername, finallyEmail)
                        }

                        if(username == null || username.isBlank() || email == null || email.isBlank()) {
                            MyLog.w(TAG, "http server: api $routeName: commit abort by username or email invalid")
                        }else {
                            //检查是否存在冲突，如果存在，将不会创建提交
                            if(Libgit2Helper.hasConflictItemInRepo(gitRepo)) {
                                MyLog.w(TAG, "http server: api=$routeName, repoName=${repoFromDb.repoName}, err=conflict abort the commit")
                                // 显示个手机通知，点击进入ChangeList并定位到对应仓库
                                sendNotification("Error", "Conflict abort the commit", Cons.selectedItem_ChangeList, repoFromDb.id)
                            }else {
                                // 有username 和 email，且无冲突

                                // stage worktree changes
                                Libgit2Helper.stageAll(gitRepo, repoFromDb.id)



                                //如果index不为空，则创建提交
                                if(!Libgit2Helper.indexIsEmpty(gitRepo)) {
                                    val ret = Libgit2Helper.createCommit(
                                        repo = gitRepo,
                                        msg = "",
                                        username = username,
                                        email = email,
                                        indexItemList = null,
                                        amend = false,
                                        overwriteAuthorWhenAmend = false,
                                        settings = settings
                                    )

                                    if(ret.hasError()) {
                                        MyLog.w(TAG, "http server: api=$routeName, repoName=${repoFromDb.repoName}, create commit err: ${ret.msg}, exception=${ret.exception?.printStackTrace()}")
                                        // 显示个手机通知，点击进入ChangeList并定位到对应仓库
                                        sendNotification("Error", "Commit err: ${ret.msg}", Cons.selectedItem_ChangeList, repoFromDb.id)
                                    }else {
                                        //更新本地oid，不然后面会误认为up to date，然后不推送
                                        upstream.localOid = ret.data!!.toString()
                                    }
                                }

                            }
                        }
                    }


                    // 检查 ahead和behind
                    if(!force) {  //只有非force才检查，否则强推
                        val (ahead, behind) = getAheadBehind(gitRepo, Oid.of(upstream.localOid), Oid.of(upstream.remoteOid))

                        if(behind > 0) {  //本地落后远程（远程领先本地）
                            throw RuntimeException("upstream ahead of local")
                        }

                        if(ahead < 1) {
                            //通过behind检测后，如果进入此代码块，代表本地不落后也不领先，即“up to date”，并不需要推送，可返回了
                            if(settings.httpService.showNotifyWhenSuccess) {
                                sendSuccessNotification(repoFromDb.repoName, "Already up-to-date", Cons.selectedItem_ChangeList, repoFromDb.id)
                            }

                            return@doActWithRepoLock
                        }

                    }


                    // get push credential
                    val credential = Libgit2Helper.getRemoteCredential(
                        db.remoteRepository,
                        db.credentialRepository,
                        repoFromDb.id,
                        upstream.remote,
                        trueFetchFalsePush = false,
                        masterPassword = masterPassword
                    )

                    val ret = Libgit2Helper.push(gitRepo, upstream.remote, upstream.pushRefSpec, credential, force)
                    if(ret.hasError()) {
                        throw RuntimeException(ret.msg)
                    }

                    // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
                    val repoDb = AppModel.dbContainer.repoRepository
                    repoDb.updateLastUpdateTime(repoFromDb.id, getSecFromTime())
                }


                if(settings.httpService.showNotifyWhenSuccess) {
                    sendSuccessNotification(repoFromDb.repoName, "push successfully", Cons.selectedItem_ChangeList, repoFromDb.id)
                }

            }
        }catch (e:Exception) {
            val errMsg = e.localizedMessage ?: "unknown err"

            createAndInsertError(repoFromDb.id, "push by api $routeName err: $errMsg")

            if(settings.httpService.showNotifyWhenErr) {
                sendNotification("push err", errMsg, Cons.selectedItem_ChangeList, repoFromDb.id)
            }

            MyLog.e(TAG, "route:$routeName, repoName=${repoFromDb.repoName}, err=${e.stackTraceToString()}")
        }

    }
}
