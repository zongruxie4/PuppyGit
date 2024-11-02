package com.catpuppyapp.puppygit.screen.functions

import android.content.Context
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.AppContainer
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.git.Upstream
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.getSecFromTime
import com.catpuppyapp.puppygit.utils.showErrAndSaveLog
import com.catpuppyapp.puppygit.utils.state.CustomStateListSaveable
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.github.git24j.core.Repository

private val TAG = "ChangeListFunctions"

object ChangeListFunctions {

    suspend fun doCommit(
        requireShowCommitMsgDialog:Boolean,
        cmtMsg:String,
        requireCloseBottomBar:Boolean,
        requireDoSync:Boolean,
        curRepoFromParentPage:CustomStateSaveable<RepoEntity>,
        refreshRequiredByParentPage:MutableState<String>,
        username:MutableState<String>,
        email:MutableState<String>,
        requireShowToast:(String)->Unit,
        pleaseSetUsernameAndEmailBeforeCommit:String,
        showUserAndEmailDialog:MutableState<Boolean>,
        amendCommit: MutableState<Boolean>,
        overwriteAuthor: MutableState<Boolean>,
        showCommitMsgDialog: MutableState<Boolean>,
        repoState:MutableIntState,
        appContext:Context,
        loadingText:MutableState<String>,
        repoId:String,
        bottomBarActDoneCallback:(String)->Unit,
        fromTo:String,
        itemList:CustomStateListSaveable<StatusTypeEntrySaver>,
        successCommitStrRes:String
    ):Boolean{
        //可以用context获取string resource
//        requireShowToast("test context getsTring:"+appContext.getString(R.string.n_files_staged))
//            MyLog.d(TAG, "#doCommit, start")

        // commit
        //检查选中列表是否为空，这个需要 doStage检查，commit不管
//            if(selectedListIsEmpty()) {
//                MyLog.d(TAG, "#doCommit, selected item list is empty")
//                requireShowToast(noItemSelectedStrRes)
//                return@doCommit false
//            }

        //更新这个变量，供输入提交信息后的回调用来判断接下来执行提交还是sync
        Cache.set(Cache.Key.changeListInnerPage_RequireDoSyncAfterCommit, requireDoSync)

        //执行commit
        Repository.open(curRepoFromParentPage.value.fullSavePath).use { repo ->
            //检查是否存在冲突条目，有可能已经stage了，就不存在了，就能提交，否则，不能提交
            val readyCreateCommit = Libgit2Helper.isReadyCreateCommit(repo)
            if(readyCreateCommit.hasError()) {
                //显示错误信息
                Msg.requireShowLongDuration(readyCreateCommit.msg)

                //20240819 支持index为空时创建提交，因为目前实现了amend而amend可仅修改之前的提交且不提交新内容，所以index非空检测就不能强制了，显示个提示就够了，但仍允许提交
                if(readyCreateCommit.code != Ret.ErrCode.indexIsEmpty) {  //若错误不是index为空，则结束提交
                    //若有错，必刷新页面
                    changeStateTriggerRefreshPage(refreshRequiredByParentPage)
                    return@doCommit false
                }

            }

            //执行到这说明通过检测了，可创建提交了

            //检查仓库配置文件是否有email，若没有，检查全局git配置项中是否有email，若没有，询问是否现在设置，三个选项：1设置全局email，2设置仓库email，点取消则放弃设置并终止提交
            val (usernameFromConfig, emailFromConfig) = Libgit2Helper.getGitUsernameAndEmail(repo)


            //没必要执行这个判断，正常流程应该是存到全局或仓库配置文件里，然后在上面的代码就能取出用户名和邮箱，若执行到这还没有效邮箱，应该弹窗让用户设置
//                //如果仓库和全局都没username和email，尝试从状态变量获取，状态变量可能有用户在弹窗输入的username和email，不过，如果成功保存了username和email，应该不会执行到这里，这段代码其实是可有可无的
//                if(usernameFromConfig.isBlank() || emailFromConfig.isBlank()) {
//                    usernameFromConfig = username.value
//                    emailFromConfig = email.value
//                }

            //更新下状态，这样用户在输入的时候如果之前设置过用户名或密码，就不用重新输了
            if(usernameFromConfig.isNotBlank()) {
                username.value = usernameFromConfig
            }
            if(emailFromConfig.isNotBlank()) {
                email.value = emailFromConfig
            }

            //如果仓库、全局、状态变量(用户刚输入的)都没有username和email，显示弹窗请求用户输入
            if(usernameFromConfig.isBlank() || emailFromConfig.isBlank()){
                MyLog.d(TAG, "#doCommit, username and email not set, will show dialog for set them")
                //显示提示信息
                requireShowToast(pleaseSetUsernameAndEmailBeforeCommit)
                //弹窗提示没用户名和邮箱，询问是否设置，用户设置好再重新调用此方法即可
                showUserAndEmailDialog.value=true
                return@doCommit false
            }

            //提交信息
            //执行到这，用户名和邮箱就有了，接下来获取提交信息
            var tmpCommitMsg = ""
            //还没设置过提交信息，显示弹窗，请求用户输入提交信息
            if(requireShowCommitMsgDialog) {  //显示弹窗，结束
//                    MyLog.d(TAG, "#doCommit, require show commit msg dialog")
                amendCommit.value = false
                overwriteAuthor.value = false

                showCommitMsgDialog.value = true
                return@doCommit false
            }else { // 已经请求过输入提交信息了，这里直接获取
                tmpCommitMsg = cmtMsg
            }

            //20240815:改成由createCommit生成提交信息了
            //如果用户输入的commitMsg全是空白字符或者没填，将会自动生成一个
//                if(tmpCommitMsg.isBlank()) {
//                    val genCommitMsgRet = Libgit2Helper.genCommitMsg(repo,
//                        //如果是在changelist页面，需要查询实际index条目来生成提交信息不然只包含你选中的文件列表不包含已经在index区的文件列表，这时传null，方法内部会去查；如果是在index页面，直接把当前所有条目传过去即可，就不用查了
//                        if(fromTo == Cons.gitDiffFromHeadToIndex) itemList.value else null
//                    )
//                    val cmtmsg = genCommitMsgRet.data
//                    if(genCommitMsgRet.hasError() || cmtmsg.isNullOrBlank()) {
//                        MyLog.d(TAG, "#doCommit, genCommitMsg Err, commit abort!")
//                        //显示提示信息
//                        requireShowToast(appContext.getString(R.string.gen_commit_msg_err_commit_abort))
//
//                        //出错，刷新页面
//                        changeStateTriggerRefreshPage(needRefreshChangeListPage)
//                        return@doCommit false
//                    }
//
//                    //执行到这说明生成提交信息成功，取出提交信息
//                    tmpCommitMsg = cmtmsg
//                }

            //开始创建提交
            MyLog.d(TAG, "#doCommit, before createCommit")
            //do commit
            val ret = if(repoState.intValue== Repository.StateT.REBASE_MERGE.bit) {    //执行rebase continue
                loadingText.value = appContext.getString(R.string.rebase_continue)

                Libgit2Helper.rebaseContinue(
                    repo,
                    usernameFromConfig,
                    emailFromConfig,
                    commitMsgForFirstCommit = tmpCommitMsg,
                    overwriteAuthorForFirstCommit = overwriteAuthor.value
                )

            }else if(repoState.intValue == Repository.StateT.CHERRYPICK.bit) {
                loadingText.value = appContext.getString(R.string.cherrypick_continue)

                Libgit2Helper.cherrypickContinue(repo,
                    msg=tmpCommitMsg,
                    username = usernameFromConfig,
                    email=emailFromConfig,
                    autoClearState = false,  //这里后续会检查若操作成功会清状态，所以此处传false，不然会清两次状态
                    overwriteAuthor = overwriteAuthor.value
                )
            }else {
                loadingText.value = appContext.getString(R.string.committing)

                Libgit2Helper.createCommit(
                    repo = repo,
                    msg = tmpCommitMsg,
                    username = username.value,
                    email = email.value,
                    indexItemList = if(fromTo == Cons.gitDiffFromHeadToIndex) itemList.value else null,
                    amend = amendCommit.value,
                    overwriteAuthorWhenAmend = overwriteAuthor.value
                )

            }

            if(ret.hasError()) {  //创建commit失败
                MyLog.d(TAG, "#doCommit, createCommit failed, has error:"+ret.msg)

                Msg.requireShowLongDuration(ret.msg)

                //显示的时候只显示简短错误信息，例如"请先解决冲突！"，存的时候存详细点，存上什么操作导致的错误，例如：“merge continue err:请先解决冲突”
                val errPrefix= appContext.getString(R.string.commit_err)
                createAndInsertError(repoId, "$errPrefix:${ret.msg}")

                //若出错，必刷新页面
                if(requireCloseBottomBar) {
                    bottomBarActDoneCallback("")
                }else {
                    //刷新页面，之所以放到else里是因为如果请求关闭底栏，关闭时会自动刷新，如果不放else里就重复刷新了，无意义
                    changeStateTriggerRefreshPage(refreshRequiredByParentPage)
                }
                return@doCommit false
            }else {  //创建成功
                MyLog.d(TAG, "#doCommit, createCommit success")
                //如果没stage所有冲突条目，不能执行commit，函数入口有判断，所以如果能执行到这里，肯定是stage了所有冲突条目
                Libgit2Helper.cleanRepoState(repo)  //清理仓库状态，例如存在冲突的时候如果创建完不清理状态，会一直处于merge state，就是pc git 显示 merging的情况

                //更新仓库状态变量，要不然标题可能还是merge时的红色
                repoState.intValue = repo.state()?.bit?: Cons.gitRepoStateInvalid  //如果state是null，返回一个无效值

                // 更新db
                val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                val shortNewCommitHash = ret.data.toString().substring(Cons.gitShortCommitHashRange)
                //更新db
                repoDb.updateCommitHash(
                    repoId=curRepoFromParentPage.value.id,
                    lastCommitHash = shortNewCommitHash,
                )
                // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
                repoDb.updateLastUpdateTime(curRepoFromParentPage.value.id, getSecFromTime())


                requireShowToast(successCommitStrRes)
                //操作成功不一定刷新页面，因为可能commit和其他操作组合，一般在最后一个操作完成后才刷新页面，例如commit then sync，应由最后的sync负责关底栏和刷新页面
                if(requireCloseBottomBar) {
                    bottomBarActDoneCallback("")
                }
                return@doCommit true
            }


            //弹窗确认是否提交，有个勾选框“我想编辑提交信息”，勾选显示一个输入框让用户输入提交信息，否则自动生成提交信息(文案提醒用户不输入就自动生成)
            //获取提交信息后，先stage选中的文件，然后再创建commit
        }
        //设置好email后，弹窗，提示将创建提交，是否确定，有个勾选框“我想填写提交信息”，勾选显示一个输入框可填写提交信息，不勾选将自动生成提交信息“更新了n个文件，列举前3个，最后加句 commit msg generated by PuppyGit”
        //创建提交

        //提示操作完成
//        val msg = replaceStringResList(revertStrRes,
//            listOf(pathspecList.size.toString(),untrakcedFileList.size.toString()))
//        bottomBarActDoneCallback(msg)
//        showRevertAlert.value=false

    }

    suspend fun doFetch(
        remoteNameParam:String?,
        curRepoFromParentPage:CustomStateSaveable<RepoEntity>,
        requireShowToast:(String)->Unit,
        appContext:Context,
        loadingText:MutableState<String>,
        dbContainer:AppContainer,

    ):Boolean{   //参数的remoteNameParam如果有效就用参数的，否则自己查当前head分支对应的remote
        //x 废弃，逻辑已经改了) 执行isReadyDoSync检查之前要先do fetch，想象一下，如果远程创建了一个分支，正好和本地的关联，但如果我没先fetch，那我检查时就get不到那个远程分支是否存在，然后就会先执行push，但可能远程仓库已经领先本地了，所以push也可能失败，但如果先fetch，就不会有这种问题了
        Repository.open(curRepoFromParentPage.value.fullSavePath).use { repo ->
            //fetch成功返回true，否则返回false
            try {
                var remoteName = remoteNameParam
                if(remoteName == null || remoteName.isBlank()) {
                    val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)
                    val upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)
                    remoteName = upstream.remote
                    if(remoteName == null || remoteName.isBlank()) {  //fetch不需合并，只需remote有效即可，所以只检查remote
                        requireShowToast(appContext.getString(R.string.err_upstream_invalid_plz_try_sync_first))
                        return@doFetch false
                    }
                }

                loadingText.value = appContext.getString(R.string.fetching)

                //执行到这，upstream的remote有效，执行fetch
                //            只fetch当前分支关联的remote即可，获取仓库当前remote和credential的关联，组合起来放到一个pair里，pair放到一个列表里，然后调用fetch
                val credential = Libgit2Helper.getRemoteCredential(
                    dbContainer.remoteRepository,
                    dbContainer.credentialRepository,
                    curRepoFromParentPage.value.id,
                    remoteName,
                    trueFetchFalsePush = true
                )
                Libgit2Helper.fetchRemoteForRepo(repo, remoteName, credential, curRepoFromParentPage.value)

                // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
                val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                repoDb.updateLastUpdateTime(curRepoFromParentPage.value.id, getSecFromTime())


                return@doFetch true
            }catch (e:Exception) {
                //记录到日志
                //显示提示
                //保存数据库(给用户看的，消息尽量简单些)
                showErrAndSaveLog(TAG, "#doFetch() err:"+e.stackTraceToString(), "fetch err:"+e.localizedMessage, requireShowToast, curRepoFromParentPage.value.id)

                return@doFetch false
            }
        }
    }


    suspend fun doMerge(requireCloseBottomBar:Boolean,
                        upstreamParam: Upstream?,
                        showMsgIfHasConflicts:Boolean,
                        trueMergeFalseRebase:Boolean=true,
                        curRepoFromParentPage:CustomStateSaveable<RepoEntity>,
                        requireShowToast:(String)->Unit,
                        appContext:Context,
                        loadingText:MutableState<String>,
                        bottomBarActDoneCallback:(String)->Unit,
    ):Boolean {
        try {
            //这的repo不能共享，不然一释放就要完蛋了，这repo不是rc是box单指针
            Repository.open(curRepoFromParentPage.value.fullSavePath).use { repo ->
                var upstream = upstreamParam
                if(Libgit2Helper.isUpstreamInvalid(upstream)) {  //如果调用者没传有效的upstream，查一下
                    val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)  //获取当前分支短名，例如 main
                    upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)  //获取当前分支的上游，例如 remote=origin 和 merge=refs/heads/main，参见配置文件 branch.yourbranchname.remote 和 .merge 字段
                    //如果查出的upstream还是无效，终止操作
                    if(Libgit2Helper.isUpstreamInvalid(upstream)) {
                        requireShowToast(appContext.getString(R.string.err_upstream_invalid_plz_try_sync_first))
                        return false
                    }
                }

                // doMerge
                val remoteRefSpec = Libgit2Helper.getUpstreamRemoteBranchShortNameByRemoteAndBranchRefsHeadsRefSpec(
                    upstream!!.remote,
                    upstream.branchRefsHeadsFullRefSpec
                )
                MyLog.d(TAG, "doMerge: remote="+upstream.remote+", branchFullRefSpec=" + upstream.branchRefsHeadsFullRefSpec +", trueMergeFalseRebase=$trueMergeFalseRebase")
                val (usernameFromConfig, emailFromConfig) = Libgit2Helper.getGitUsernameAndEmail(repo)

                //如果用户名或邮箱无效，无法创建commit，merge无法完成，所以，直接终止操作
                if(Libgit2Helper.isUsernameAndEmailInvalid(usernameFromConfig,emailFromConfig)) {
                    requireShowToast(appContext.getString(R.string.plz_set_username_and_email_first))
                    return false
                }

                val mergeResult = if(trueMergeFalseRebase) {
                    loadingText.value = appContext.getString(R.string.merging)

                    Libgit2Helper.mergeOneHead(
                        repo,
                        remoteRefSpec,
                        usernameFromConfig,
                        emailFromConfig
                    )
                }else {
                    loadingText.value = appContext.getString(R.string.rebasing)

                    Libgit2Helper.mergeOrRebase(
                        repo,
                        targetRefName = remoteRefSpec,
                        username = usernameFromConfig,
                        email = emailFromConfig,
                        requireMergeByRevspec = false,
                        revspec = "",
                        trueMergeFalseRebase = false
                    )
                }

                if (mergeResult.hasError()) {
                    //检查是否存在冲突条目
                    //如果调用者想自己判断是否有冲突，可传showMsgIfHasConflicts为false
                    if (mergeResult.code == Ret.ErrCode.mergeFailedByAfterMergeHasConfilts) {
                        if(showMsgIfHasConflicts){
                            requireShowToast(appContext.getString(R.string.has_conflicts))

//                            if(trueMergeFalseRebase) {
//                                requireShowToast(appContext.getString(R.string.merge_has_conflicts))
//                            }else {
//                                requireShowToast(appContext.getString(R.string.rebase_has_conflicts))
//                            }

                        }
                    }else {
                        //显示错误提示
                        requireShowToast(mergeResult.msg)
                    }

                    //记到数据库error日志
                    createAndInsertError(curRepoFromParentPage.value.id, mergeResult.msg)
                    //关闭底栏，如果需要的话
                    if (requireCloseBottomBar) {
                        bottomBarActDoneCallback("")
                    }
                    return false
                }

                //执行到这就合并成功了

                //清下仓库状态
                Libgit2Helper.cleanRepoState(repo)

                //更新db显示成功通知
                Libgit2Helper.updateDbAfterMergeSuccess(mergeResult, appContext, curRepoFromParentPage.value.id, requireShowToast, trueMergeFalseRebase)

                //关闭底栏，如果需要的话
                if (requireCloseBottomBar) {
                    bottomBarActDoneCallback("")
                }
                return true
            }
        }catch (e:Exception) {
            //log
            showErrAndSaveLog(
                logTag = TAG,
                logMsg = "#doMerge(trueMergeFalseRebase=$trueMergeFalseRebase) err:"+e.stackTraceToString(),
                showMsg = e.localizedMessage ?: "err",
                showMsgMethod = requireShowToast,
                repoId = curRepoFromParentPage.value.id,
                errMsgForErrDb = "${if(trueMergeFalseRebase) "merge" else "rebase"} err: "+e.localizedMessage
            )

            //关闭底栏，如果需要的话
            if (requireCloseBottomBar) {
                bottomBarActDoneCallback("")
            }
            return false
        }

    }
    suspend fun doPush(requireCloseBottomBar:Boolean,
                       upstreamParam:Upstream?,
                       force:Boolean=false,
                       curRepoFromParentPage:CustomStateSaveable<RepoEntity>,
                       requireShowToast:(String)->Unit,
                       appContext:Context,
                       loadingText:MutableState<String>,
                       bottomBarActDoneCallback:(String)->Unit,
                       dbContainer: AppContainer
   ) : Boolean {
        try {
//            MyLog.d(TAG, "#doPush: start")
            Repository.open(curRepoFromParentPage.value.fullSavePath).use { repo ->
                if(repo.headDetached()) {
                    requireShowToast(appContext.getString(R.string.push_failed_by_detached_head))
                    return@doPush false
                }

                var upstream:Upstream? = upstreamParam
                if(Libgit2Helper.isUpstreamInvalid(upstream)) {  //如果调用者没传有效的upstream，查一下
                    val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)  //获取当前分支短名，例如 main
                    upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)  //获取当前分支的上游，例如 remote=origin 和 merge=refs/heads/main，参见配置文件 branch.yourbranchname.remote 和 .merge 字段
                    //如果查出的upstream还是无效，终止操作
                    if(Libgit2Helper.isUpstreamInvalid(upstream)) {
                        requireShowToast(appContext.getString(R.string.err_upstream_invalid_plz_try_sync_first))
                        return@doPush false
                    }
                }
                MyLog.d(TAG, "#doPush: upstream.remote="+upstream!!.remote+", upstream.branchFullRefSpec="+upstream!!.branchRefsHeadsFullRefSpec)

                loadingText.value = appContext.getString(R.string.pushing)

                //执行到这里，必定有上游，push
                val credential = Libgit2Helper.getRemoteCredential(
                    dbContainer.remoteRepository,
                    dbContainer.credentialRepository,
                    curRepoFromParentPage.value.id,
                    upstream!!.remote,
                    trueFetchFalsePush = false
                )

                val ret = Libgit2Helper.push(repo, upstream!!.remote, upstream!!.pushRefSpec, credential, force)
                if(ret.hasError()) {
                    throw RuntimeException(ret.msg)
                }

                // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
                val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                repoDb.updateLastUpdateTime(curRepoFromParentPage.value.id, getSecFromTime())


                //关闭底栏，如果需要的话
                if (requireCloseBottomBar) {
                    bottomBarActDoneCallback("")
                }
                return@doPush true
            }
        }catch (e:Exception) {
            //log
            showErrAndSaveLog(TAG, "#doPush(force=$force) err:"+e.stackTraceToString(), "${if(force) "Push(Force)" else "Push"} error:"+e.localizedMessage, requireShowToast, curRepoFromParentPage.value.id)

            //关闭底栏，如果需要的话
            if (requireCloseBottomBar) {
                bottomBarActDoneCallback("")
            }
            return@doPush false
        }

    }

    suspend fun doSync(
        requireCloseBottomBar:Boolean,
        trueMergeFalseRebase:Boolean=true,
        curRepoFromParentPage:CustomStateSaveable<RepoEntity>,
        requireShowToast:(String)->Unit,
        appContext:Context,
        bottomBarActDoneCallback:(String)->Unit,
        plzSetUpStreamForCurBranch:String,
        upstreamRemoteOptionsList:CustomStateListSaveable<String>,
        upstreamSelectedRemote:MutableIntState,
        upstreamBranchSameWithLocal:MutableState<Boolean>,
        upstreamBranchShortRefSpec:MutableState<String>,
        upstreamCurBranchShortName:MutableState<String>,
        upstreamDialogOnOkText:MutableState<String>,
        showSetUpstreamDialog:MutableState<Boolean>,
        loadingText:MutableState<String>,
        dbContainer:AppContainer,
    ) {
        Repository.open(curRepoFromParentPage.value.fullSavePath).use { repo ->
            if(repo.headDetached()) {
                requireShowToast(appContext.getString(R.string.sync_failed_by_detached_head))
                return@doSync
            }


            //检查是否有upstream，如果有，do fetch do merge，然后do push,如果没有，请求设置upstream，然后do push
            val hasUpstream = Libgit2Helper.isBranchHasUpstream(repo)
            val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)
            if (!hasUpstream) {  //不存在上游，弹窗设置一下
                requireShowToast(plzSetUpStreamForCurBranch)  //显示请设置上游的提示

                //为弹窗设置相关属性
                //设置远程名
                val remoteList = Libgit2Helper.getRemoteList(repo)
                upstreamRemoteOptionsList.value.clear()
                upstreamRemoteOptionsList.value.addAll(remoteList)
//                upstreamRemoteOptionsList.requireRefreshView()

                upstreamSelectedRemote.intValue = 0  //默认选中第一个remote，每个仓库至少有一个origin remote，应该不会出错

                //默认选中为上游设置和本地分支相同名
                upstreamBranchSameWithLocal.value = true
                //把远程分支名设成当前分支的完整名
                upstreamBranchShortRefSpec.value = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)

                //设置当前分支，用来让用户知道自己在为哪个分支设置上游
                upstreamCurBranchShortName.value = shortBranchName

                upstreamDialogOnOkText.value = appContext.getString(R.string.save_and_sync)
                //修改状态，显示弹窗，在弹窗设置完后，应该就不会进入这个判断了
                showSetUpstreamDialog.value = true
            }else {  //存在上游
                try {
//取出上游
                    val upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)
                    val fetchSuccess = doFetch(
                        upstream.remote,
                        curRepoFromParentPage = curRepoFromParentPage,
                        requireShowToast = requireShowToast,
                        appContext = appContext,
                        loadingText = loadingText,
                        dbContainer = dbContainer
                    )
                    if(!fetchSuccess) {
                        requireShowToast(appContext.getString(R.string.fetch_failed))
                        if(requireCloseBottomBar) {
                            bottomBarActDoneCallback("")
                        }
                        return@doSync
                    }

                    //检查配置文件设置的remote和branch是否实际存在，
                    val isUpstreamExistOnLocal = Libgit2Helper.isUpstreamActuallyExistOnLocal(
                        repo,
                        upstream.remote,
                        upstream.branchRefsHeadsFullRefSpec
                    )

                    //如果存在上游，执行merge 若没冲突则push，否则终止操作直接return
                    //如果不存在上游，只需执行push，相当于pc的 git push with -u (--set-upstream)，但上面已经把remote和branch设置到gitconfig里了(执行到这里都能取出来upstream所以肯定设置过了，在 对话框或分支管理页面设置的)，所以这里正常推送即可，不用再设置什么
                    MyLog.d(TAG, "@doSync: isUpstreamExistOnLocal="+isUpstreamExistOnLocal)
                    if(isUpstreamExistOnLocal) {  //上游分支在本地存在
                        // doMerge
                        val mergeSuccess = doMerge(
                            false, upstream, false, trueMergeFalseRebase,
                            curRepoFromParentPage = curRepoFromParentPage,
                            requireShowToast = requireShowToast,
                            appContext = appContext,
                            loadingText = loadingText,
                            bottomBarActDoneCallback = bottomBarActDoneCallback,
                        )
                        if(!mergeSuccess) {  //merge 失败，终止操作
                            //如果merge完存在冲突条目，就不要执行push了
                            if(Libgit2Helper.hasConflictItemInRepo(repo)) {  //检查失败原因是否是存在冲突，若是则显示提示
                                requireShowToast(appContext.getString(R.string.has_conflicts_abort_sync))
                                if(requireCloseBottomBar) {
                                    bottomBarActDoneCallback("")
                                }
                            }

                            return@doSync
                        }
                    }

                    //如果执行到这，要么不存在上游，直接push(新建远程分支)；要么存在上游，但fetch/merge成功完成，需要push，所以，执行push
                    //doPush
                    val pushSuccess = doPush(
                        requireCloseBottomBar = false,
                        upstreamParam = upstream,
                        force = false,
                        curRepoFromParentPage = curRepoFromParentPage,
                        requireShowToast = requireShowToast,
                        appContext = appContext,
                        loadingText = loadingText,
                        bottomBarActDoneCallback = bottomBarActDoneCallback,
                        dbContainer = dbContainer
                    )
                    if(pushSuccess) {
                        requireShowToast(appContext.getString(R.string.sync_success))
                    }else {
                        requireShowToast(appContext.getString(R.string.sync_failed))
                    }

                    if(requireCloseBottomBar) {
                        bottomBarActDoneCallback("")
                    }
                }catch (e:Exception) {
                    //log
                    showErrAndSaveLog(TAG, "#doSync() err:"+e.stackTraceToString(), "sync err:"+e.localizedMessage, requireShowToast, curRepoFromParentPage.value.id)

                    //close if require
                    if(requireCloseBottomBar) {
                        bottomBarActDoneCallback("")
                    }
                }

            }
        }

    }


}
