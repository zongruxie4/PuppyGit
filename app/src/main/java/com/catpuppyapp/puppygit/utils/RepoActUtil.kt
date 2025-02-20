package com.catpuppyapp.puppygit.utils

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.AppContainer
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.server.bean.NotificationSender
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.Libgit2Helper.Companion.getAheadBehind
import com.catpuppyapp.puppygit.utils.cache.NotifySenderMap
import com.catpuppyapp.puppygit.utils.encrypt.MasterPassUtil
import com.github.git24j.core.Oid
import com.github.git24j.core.Repository


private const val TAG = "RepoActUtil"

object RepoActUtil {
    //若获取锁失败，等5秒，再获取，若还获取不到就返回仓库忙
    private const val waitInMillSecIfApiBusy = 5000L  //5s

    private fun throwRepoBusy(prefix:String, repoName:String) {
        throwWithPrefix(prefix, "repo '$repoName' busy, plz try later")
    }

    private fun getNotifySender(repoId:String, sessionId: String):NotificationSender? {
        return NotifySenderMap.getByType<NotificationSender>(NotifySenderMap.genKey(repoId, sessionId))
    }

    private fun removeNotifySender(repoId:String, sessionId: String) {
        NotifySenderMap.del(NotifySenderMap.genKey(repoId, sessionId))
    }

    suspend fun syncRepoList(
        sessionId:String,
        repoList:List<RepoEntity>,
        routeName: String,
        gitUsernameFromUrl:String,    // leave empty to read from config
        gitEmailFromUrl:String,    // leave empty to read from config
        autoCommit:Boolean,
        force:Boolean,

    ){
        val prefix = "sync"
        val db = AppModel.dbContainer
        val settings = SettingsUtil.getSettingsSnapshot()
        val masterPassword = MasterPassUtil.get(AppModel.realAppContext)

        repoList.forEach { repoFromDb ->
            doJobThenOffLoading {
                val notiSender = getNotifySender(repoFromDb.id, sessionId)

                try {

                    notiSender?.sendProgressNotification?.invoke(repoFromDb.repoName, "syncing...")

                    Libgit2Helper.doActWithRepoLock(repoFromDb, waitInMillSec = waitInMillSecIfApiBusy, onLockFailed = { throwRepoBusy(prefix, repoFromDb.repoName) }) {
                        syncSingle(
                            sendProgressNotification = notiSender?.sendProgressNotification,
                            repoFromDb = repoFromDb,
                            db = db,
                            masterPassword = masterPassword,
                            gitUsernameFromUrl = gitUsernameFromUrl,
                            gitEmailFromUrl = gitEmailFromUrl,
                            settings = settings,
                            sendSuccessNotification = notiSender?.sendSuccessNotification,
                            autoCommit = autoCommit,
                            routeName = routeName,
                            sendErrNotification = notiSender?.sendErrNotification,
                            force = force
                        )

                    }
                }catch (e:Exception) {
                    val errMsg = e.localizedMessage ?: "unknown err"

                    createAndInsertError(repoFromDb.id, "sync by api $routeName err: $errMsg")

                    notiSender?.sendErrNotification?.invoke("sync err", errMsg, Cons.selectedItem_ChangeList, repoFromDb.id)


                    MyLog.e(TAG, "route:$routeName, repoName=${repoFromDb.repoName}, err=${e.stackTraceToString()}")
                }finally {
                    removeNotifySender(repoFromDb.id, sessionId)
                }
            }

        }

    }

    private suspend fun syncSingle(
        sendProgressNotification: ((repoNameOrId: String, progress: String) -> Unit)?,
        repoFromDb: RepoEntity,
        db: AppContainer,
        masterPassword: String,
        gitUsernameFromUrl: String,
        gitEmailFromUrl: String,
        settings: AppSettings,
        sendSuccessNotification: ((title: String?, msg: String?, startPage: Int?, startRepoId: String?) -> Unit)?,
        autoCommit: Boolean,
        routeName: String,
        sendErrNotification: ((title: String, msg: String, startPage: Int, startRepoId: String) -> Unit)?,
        force: Boolean
    ) {
        sendProgressNotification?.invoke(repoFromDb.repoName, "pulling...")

        pullSingle(
            repoFromDb = repoFromDb,
            db = db,
            masterPassword = masterPassword,
            gitUsernameFromUrl = gitUsernameFromUrl,
            gitEmailFromUrl = gitEmailFromUrl,
            settings = settings,
            sendSuccessNotification = sendSuccessNotification
        )

        sendProgressNotification?.invoke(repoFromDb.repoName, "pushing...")

        pushSingle(
            repoFromDb = repoFromDb,
            autoCommit = autoCommit,
            gitUsernameFromUrl = gitUsernameFromUrl,
            gitEmailFromUrl = gitEmailFromUrl,
            routeName = routeName,
            sendErrNotification = sendErrNotification,
            settings = settings,
            force = force,
            sendSuccessNotification = sendSuccessNotification,
            db = db,
            masterPassword = masterPassword
        )

        sendProgressNotification?.invoke(repoFromDb.repoName, "sync successfully")

    }

    suspend fun pullRepoList(
        sessionId:String,
        repoList:List<RepoEntity>,
        routeName: String,
        gitUsernameFromUrl:String,  // leave empty to read from config
        gitEmailFromUrl:String,  // leave empty to read from config
    ) {
        val prefix = "pull"
        val db = AppModel.dbContainer
        val settings = SettingsUtil.getSettingsSnapshot()
        val masterPassword = MasterPassUtil.get(AppModel.realAppContext)

        repoList.forEach { repoFromDb ->
            doJobThenOffLoading {
                val notiSender = getNotifySender(repoFromDb.id, sessionId)

                try {
                    notiSender?.sendProgressNotification?.invoke(repoFromDb.repoName, "pulling...")

                    Libgit2Helper.doActWithRepoLock(repoFromDb, waitInMillSec = waitInMillSecIfApiBusy, onLockFailed = { throwRepoBusy(prefix, repoFromDb.repoName) }) {
                        pullSingle(
                            repoFromDb = repoFromDb,
                            db = db,
                            masterPassword = masterPassword,
                            gitUsernameFromUrl = gitUsernameFromUrl,
                            gitEmailFromUrl = gitEmailFromUrl,
                            settings = settings,
                            sendSuccessNotification = notiSender?.sendSuccessNotification
                        )

                    }
                }catch (e:Exception) {
                    val errMsg = e.localizedMessage ?: "unknown err"

                    createAndInsertError(repoFromDb.id, "pull by api $routeName err: $errMsg")

                    notiSender?.sendErrNotification?.invoke("pull err", errMsg, Cons.selectedItem_ChangeList, repoFromDb.id)


                    MyLog.e(TAG, "route:$routeName, repoName=${repoFromDb.repoName}, err=${e.stackTraceToString()}")
                }finally {
                    removeNotifySender(repoFromDb.id, sessionId)

                }
            }

        }

    }

    private suspend fun pullSingle(
        repoFromDb: RepoEntity,
        db: AppContainer,
        masterPassword: String,
        gitUsernameFromUrl: String,
        gitEmailFromUrl: String,
        settings: AppSettings,
        sendSuccessNotification: ((title: String?, msg: String?, startPage: Int?, startRepoId: String?) -> Unit)?
    ) {
        val prefix = "pull"
        if (dbIntToBool(repoFromDb.isDetached)) {
            throwWithPrefix(prefix, "repo is detached")
        }

        if (!Libgit2Helper.isValidGitRepo(repoFromDb.fullSavePath)) {
            throwWithPrefix(prefix, "invalid git repo")
        }

        Repository.open(repoFromDb.fullSavePath).use { gitRepo ->
            val upstream = Libgit2Helper.getUpstreamOfBranch(gitRepo, repoFromDb.branch)
            if (upstream.remote.isBlank() || upstream.branchRefsHeadsFullRefSpec.isBlank()) {
                throwWithPrefix(prefix, "invalid upstream")
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
            val (username, email) = if (gitUsernameFromUrl.isNotBlank() && gitEmailFromUrl.isNotBlank()) {
                Pair(gitUsernameFromUrl, gitEmailFromUrl)
            } else {
                val (gitUsernameFromConfig, gitEmailFromConfig) = Libgit2Helper.getGitUsernameAndEmail(gitRepo)
                val finallyUsername = gitUsernameFromUrl.ifBlank { gitUsernameFromConfig }

                val finallyEmail = gitEmailFromUrl.ifBlank { gitEmailFromConfig }

                Pair(finallyUsername, finallyEmail)
            }

            if (username == null || username.isBlank()) {
                throwWithPrefix(prefix, "git username invalid")
            }

            if (email == null || email.isBlank()) {
                throwWithPrefix(prefix, "git email invalid")
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

            if (mergeRet.hasError()) {
                throwWithPrefix(prefix, mergeRet.msg)
            }

            //merge没报错，基本算是成功了，不过可能是up to date啥也没干，也可能fast forward，也可能发生了merge，后面不会提示太详细，仅区分up to date和pull successfully(fast-forwarded or merged)

            val successMsg = if(mergeRet.code == Ret.SuccessCode.upToDate) { // up to date, no fast-forward or merge
                //显示 already up to date之类的，btw 如果显示already up to date，代表没发生merge，文件应该没修改，不需要重新加载(20250216 markor不会在文件在外部被修改后自动重载，如果文件变化，需要手动重载，不过obsidian会，所以若嫌麻烦可以用obsidian)
                "$prefix: Already up-to-date"
            }else { // fast-forwarded or merged
                "pull successfully"
            }

            sendSuccessNotification?.invoke(repoFromDb.repoName, successMsg, Cons.selectedItem_ChangeList, repoFromDb.id)
        }


    }

    suspend fun pushRepoList(
        sessionId: String,
        repoList:List<RepoEntity>,
        routeName: String,
        gitUsernameFromUrl:String,    // leave empty to read from config
        gitEmailFromUrl:String,    // leave empty to read from config
        autoCommit:Boolean,
        force:Boolean,

    ) {
        val prefix = "push"
        val db = AppModel.dbContainer
        val settings = SettingsUtil.getSettingsSnapshot()
        val masterPassword = MasterPassUtil.get(AppModel.realAppContext)

        repoList.forEach { repoFromDb ->
            //每仓库一协程并发执行
            doJobThenOffLoading {
                val notiSender = getNotifySender(repoFromDb.id, sessionId)
                try {
                    notiSender?.sendProgressNotification?.invoke(repoFromDb.repoName, "pushing...")

                    Libgit2Helper.doActWithRepoLock(repoFromDb, waitInMillSec = waitInMillSecIfApiBusy, onLockFailed = { throwRepoBusy(prefix, repoFromDb.repoName) }) {
                        pushSingle(
                            repoFromDb = repoFromDb,
                            autoCommit = autoCommit,
                            gitUsernameFromUrl = gitUsernameFromUrl,
                            gitEmailFromUrl = gitEmailFromUrl,
                            routeName = routeName,
                            sendErrNotification = notiSender?.sendErrNotification,
                            settings = settings,
                            force = force,
                            sendSuccessNotification = notiSender?.sendSuccessNotification,
                            db = db,
                            masterPassword = masterPassword
                        )


                    }
                }catch (e:Exception) {
                    val errMsg = e.localizedMessage ?: "unknown err"

                    createAndInsertError(repoFromDb.id, "push by api $routeName err: $errMsg")

                    notiSender?.sendErrNotification?.invoke("push err", errMsg, Cons.selectedItem_ChangeList, repoFromDb.id)


                    MyLog.e(TAG, "route:$routeName, repoName=${repoFromDb.repoName}, err=${e.stackTraceToString()}")
                }finally {
                    removeNotifySender(repoFromDb.id, sessionId)

                }
            }

        }
    }

    private suspend fun pushSingle(
        repoFromDb: RepoEntity,
        autoCommit: Boolean,
        gitUsernameFromUrl: String,
        gitEmailFromUrl: String,
        routeName: String,
        sendErrNotification: ((title: String, msg: String, startPage: Int, startRepoId: String) -> Unit)?,
        settings: AppSettings,
        force: Boolean,
        sendSuccessNotification: ((title: String?, msg: String?, startPage: Int?, startRepoId: String?) -> Unit)?,
        db: AppContainer,
        masterPassword: String
    ) {
        val prefix = "push"

        if (dbIntToBool(repoFromDb.isDetached)) {
            throwWithPrefix(prefix, "repo is detached")
        }

        if (!Libgit2Helper.isValidGitRepo(repoFromDb.fullSavePath)) {
            throwWithPrefix(prefix, "invalid git repo")
        }

        Repository.open(repoFromDb.fullSavePath).use { gitRepo ->
            val repoState = gitRepo.state()
            if (repoState != Repository.StateT.NONE) {
                throwWithPrefix(prefix, "repository state is '$repoState', expect 'NONE'")
            }

            val upstream = Libgit2Helper.getUpstreamOfBranch(gitRepo, repoFromDb.branch)
            if (upstream.remote.isBlank() || upstream.branchRefsHeadsFullRefSpec.isBlank()) {
                throwWithPrefix(prefix, "invalid upstream")
            }

            //提交，就算失败也会尝试push，因为就算没提交也有可能本地比远程新，比如在其他地方提交过，就有可能会这样
            if (autoCommit) {
                val (username, email) = if (gitUsernameFromUrl.isNotBlank() && gitEmailFromUrl.isNotBlank()) {
                    Pair(gitUsernameFromUrl, gitEmailFromUrl)
                } else {
                    val (gitUsernameFromConfig, gitEmailFromConfig) = Libgit2Helper.getGitUsernameAndEmail(gitRepo)
                    val finallyUsername = gitUsernameFromUrl.ifBlank { gitUsernameFromConfig }

                    val finallyEmail = gitEmailFromUrl.ifBlank { gitEmailFromConfig }

                    Pair(finallyUsername, finallyEmail)
                }

                if (username == null || username.isBlank() || email == null || email.isBlank()) {
                    MyLog.e(TAG, "api $routeName: commit abort by username or email invalid")
                } else {
                    //检查是否存在冲突，如果存在，将不会创建提交
                    if (Libgit2Helper.hasConflictItemInRepo(gitRepo)) {
                        MyLog.e(TAG, "api=$routeName, repoName=${repoFromDb.repoName}, err=conflict abort the commit")
                        // 显示个手机通知，点击进入ChangeList并定位到对应仓库
                        sendErrNotification?.invoke(repoFromDb.repoName, "$prefix: auto commit aborted by conflicts", Cons.selectedItem_ChangeList, repoFromDb.id)
                    } else {
                        // 有username 和 email，且无冲突

                        // stage worktree changes
                        Libgit2Helper.stageAll(gitRepo, repoFromDb.id)


                        //如果index不为空，则创建提交
                        if (!Libgit2Helper.indexIsEmpty(gitRepo)) {
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

                            if (ret.hasError()) {
                                MyLog.e(TAG, "api=$routeName, repoName=${repoFromDb.repoName}, create commit err: ${ret.msg}, exception=${ret.exception?.stackTraceToString()}")
                                // 显示个手机通知，点击进入ChangeList并定位到对应仓库
                                sendErrNotification?.invoke(repoFromDb.repoName, "$prefix: auto commit err: ${ret.msg}", Cons.selectedItem_ChangeList, repoFromDb.id)
                            } else if(ret.data != null){
                                //更新本地oid，不然后面会误认为up to date，然后不推送
                                upstream.localOid = ret.data!!.toString()
                            }
                        }

                    }
                }
            }


            // 检查 ahead和behind
            if (!force) {  //只有非force才检查，否则强推
                val (ahead, behind) = getAheadBehind(gitRepo, Oid.of(upstream.localOid), Oid.of(upstream.remoteOid))

                //非force push的情况下，如果本地落后远程，必然推送失败，所以就不用推了，直接报错
                if (behind > 0) {  //本地落后远程（远程领先本地）
                    throwWithPrefix(prefix, "upstream ahead of local")
                }

                //如果本地不领先远程，不需要推送，加上已经通过上面的behind检测，执行到这里，若通过if，则代表，本地不落后远程，同时也不领先远程，也就是两者相同，ahead和behind都是0，这时就是 Already up-to-date，可以返回了，不需要执行后续操作
                if (ahead < 1) {
                    //通过behind检测后，如果进入此代码块，代表本地不落后也不领先，即“up to date”，并不需要推送，可返回了
                    sendSuccessNotification?.invoke(repoFromDb.repoName, "$prefix: Already up-to-date", Cons.selectedItem_ChangeList, repoFromDb.id)


                    return
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
            if (ret.hasError()) {
                throwWithPrefix(prefix, ret.msg)
            }

            // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
            val repoDb = AppModel.dbContainer.repoRepository
            repoDb.updateLastUpdateTime(repoFromDb.id, getSecFromTime())
        }


        sendSuccessNotification?.invoke(repoFromDb.repoName, "push successfully", Cons.selectedItem_ChangeList, repoFromDb.id)
    }


    private fun throwWithPrefix(prefix:String, msg:String) {
        throw RuntimeException("$prefix: $msg")
    }
}
