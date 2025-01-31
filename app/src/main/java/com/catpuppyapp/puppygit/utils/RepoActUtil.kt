package com.catpuppyapp.puppygit.utils

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.Libgit2Helper.Companion.getAheadBehind
import com.catpuppyapp.puppygit.utils.encrypt.MasterPassUtil
import com.github.git24j.core.Oid
import com.github.git24j.core.Repository
import kotlinx.coroutines.sync.Mutex


private const val TAG = "RepoActUtil"

object RepoActUtil {
    //若获取锁失败，等5秒，再获取，若还获取不到就返回仓库忙
    private const val waitInMillSecIfApiBusy = 5000L  //5s

    private val throwRepoBusy = { _: Mutex ->
        throw RuntimeException("repo busy, plz try later")
    }


    suspend fun pullRepoList(
        repoList:List<RepoEntity>,
        routeName: String,
        gitUsernameFromUrl:String,  // leave empty to read from config
        gitEmailFromUrl:String,  // leave empty to read from config
        sendSuccessNotification:(title:String?, msg:String?, startPage:Int?, startRepoId:String?)->Unit,
        sendNotification:(title:String, msg:String, startPage:Int, startRepoId:String)->Unit,
        sendProgressNotification:(repoNameOrId:String, progress:String)->Unit
    ) {
        val db = AppModel.dbContainer
        val settings = SettingsUtil.getSettingsSnapshot()
        val masterPassword = MasterPassUtil.get(AppModel.realAppContext)

        repoList.forEach { repoFromDb ->
            doJobThenOffLoading {
                try {
                    sendProgressNotification(repoFromDb.repoName, "pulling...")

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


                        sendSuccessNotification(repoFromDb.repoName, "pull successfully", Cons.selectedItem_ChangeList, repoFromDb.id)

                    }
                }catch (e:Exception) {
                    val errMsg = e.localizedMessage ?: "unknown err"

                    createAndInsertError(repoFromDb.id, "pull by api $routeName err: $errMsg")

                    sendNotification("pull err", errMsg, Cons.selectedItem_ChangeList, repoFromDb.id)


                    MyLog.e(TAG, "route:$routeName, repoName=${repoFromDb.repoName}, err=${e.stackTraceToString()}")
                }
            }

        }

    }

    suspend fun pushRepoList(
        repoList:List<RepoEntity>,
        routeName: String,
        gitUsernameFromUrl:String,    // leave empty to read from config
        gitEmailFromUrl:String,    // leave empty to read from config
        autoCommit:Boolean,
        force:Boolean,
        sendSuccessNotification:(title:String?, msg:String?, startPage:Int?, startRepoId:String?)->Unit,
        sendNotification:(title:String, msg:String, startPage:Int, startRepoId:String)->Unit,
        sendProgressNotification:(repoNameOrId:String, progress:String)->Unit

    ) {
        val db = AppModel.dbContainer
        val settings = SettingsUtil.getSettingsSnapshot()
        val masterPassword = MasterPassUtil.get(AppModel.realAppContext)

        repoList.forEach { repoFromDb ->
            //每仓库一协程并发执行
            doJobThenOffLoading {
                try {
                    sendProgressNotification(repoFromDb.repoName, "pushing...")

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
                                    sendSuccessNotification(repoFromDb.repoName, "Already up-to-date", Cons.selectedItem_ChangeList, repoFromDb.id)


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


                        sendSuccessNotification(repoFromDb.repoName, "push successfully", Cons.selectedItem_ChangeList, repoFromDb.id)


                    }
                }catch (e:Exception) {
                    val errMsg = e.localizedMessage ?: "unknown err"

                    createAndInsertError(repoFromDb.id, "push by api $routeName err: $errMsg")

                    sendNotification("push err", errMsg, Cons.selectedItem_ChangeList, repoFromDb.id)


                    MyLog.e(TAG, "route:$routeName, repoName=${repoFromDb.repoName}, err=${e.stackTraceToString()}")
                }
            }

        }
    }

}
