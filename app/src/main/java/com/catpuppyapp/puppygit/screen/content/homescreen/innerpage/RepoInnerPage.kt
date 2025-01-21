package com.catpuppyapp.puppygit.screen.content.homescreen.innerpage

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.AskGitUsernameAndEmailDialog
import com.catpuppyapp.puppygit.compose.BottomBar
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.CheckBoxNoteText
import com.catpuppyapp.puppygit.compose.ClickableText
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.RepoCardError
import com.catpuppyapp.puppygit.compose.MyCheckBox
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.MySelectionContainer
import com.catpuppyapp.puppygit.compose.RepoCard
import com.catpuppyapp.puppygit.compose.ScrollableColumn
import com.catpuppyapp.puppygit.compose.SystemFolderChooser
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.data.AppContainer
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.dev_EnableUnTestedFeature
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.dev.reflogTestPassed
import com.catpuppyapp.puppygit.dev.repoRenameTestPassed
import com.catpuppyapp.puppygit.dev.stashTestPassed
import com.catpuppyapp.puppygit.dev.submoduleTestPassed
import com.catpuppyapp.puppygit.dev.tagsTestPassed
import com.catpuppyapp.puppygit.etc.RepoAction
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.git.Upstream
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.ActivityUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ComposeHelper
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.RepoStatusUtil
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.dbIntToBool
import com.catpuppyapp.puppygit.utils.doActIfIndexGood
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getSecFromTime
import com.catpuppyapp.puppygit.utils.getStoragePermission
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.showErrAndSaveLog
import com.catpuppyapp.puppygit.utils.state.CustomStateListSaveable
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.catpuppyapp.puppygit.utils.strHasIllegalChars
import com.github.git24j.core.Repository
import kotlinx.coroutines.sync.withLock
import java.io.File

private const val TAG = "RepoInnerPage"
private const val stateKeyTag = "RepoInnerPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoInnerPage(
    showBottomSheet: MutableState<Boolean>,
    sheetState: SheetState,
    curRepo: CustomStateSaveable<RepoEntity>,
    curRepoIndex: MutableIntState,
    contentPadding: PaddingValues,
    repoPageListState: LazyListState,
    showSetGlobalGitUsernameAndEmailDialog:MutableState<Boolean>,
    needRefreshRepoPage:MutableState<String>,
//    changeListCurRepo:CustomStateSaveable<RepoEntity>,
//    currentHomeScreen:MutableIntState,
//    changeListNeedRefresh:MutableState<String>,
//    repoList:CustomStateSaveable<MutableList<RepoEntity>>
    repoList:CustomStateListSaveable<RepoEntity>,
//    filesPageCurrentPath:MutableState<String>,
//    filesPageNeedRefresh:MutableState<String>,
    goToFilesPage:(path:String) -> Unit,
    goToChangeListPage:(repoWillShowInChangeListPage: RepoEntity) -> Unit,
    repoPageScrolled:MutableState<Boolean>,
    repoPageFilterModeOn:MutableState<Boolean>,
    repoPageFilterKeyWord:CustomStateSaveable<TextFieldValue>,
    filterListState:LazyListState,
    openDrawer:()->Unit,
    showImportRepoDialog:MutableState<Boolean>,
    goToThisRepoId:MutableState<String>,
    enableFilterState:MutableState<Boolean>,
    filterList:CustomStateListSaveable<RepoEntity>,
    isSelectionMode:MutableState<Boolean>,
    selectedItems:CustomStateListSaveable<RepoEntity>,

) {
    val activityContext = LocalContext.current
    val exitApp = AppModel.exitApp;
    val navController = AppModel.navController;
    val scope = rememberCoroutineScope()

    val settings = remember {
        val s = SettingsUtil.getSettingsSnapshot()
        repoPageScrolled.value = s.showNaviButtons
        s
    }

    // 这两个变量不用 rememberSaveable，如果设备配置改变，就希望这两个值重新计算
    val itemWidth = remember { UIHelper.getRepoItemWidth() }
    val repoCountEachRow = remember { UIHelper.getRepoItemsCountEachRow() }


    val clipboardManager = LocalClipboardManager.current

    val cloningText = stringResource(R.string.cloning)
    val unknownErrWhenCloning = stringResource(R.string.unknown_err_when_cloning)

    val dbContainer = AppModel.dbContainer;
//    val repoDtoList = remember { mutableStateListOf<RepoEntity>() }

    val activity = ActivityUtil.getCurrentActivity()

    val inDarkTheme = Theme.inDarkTheme

    val requireBlinkIdx = rememberSaveable{mutableIntStateOf(-1)}

    val isLoading = rememberSaveable { mutableStateOf(true)}
    val loadingText = rememberSaveable { mutableStateOf(activityContext.getString(R.string.loading))}
    val loadingOn = {text:String->
        loadingText.value = text

        // disable this feel better, else screen will blank then restore, feel sick
//        isLoading.value=true
    }
    val loadingOff = {
        isLoading.value=false
        loadingText.value = ""
    }


//    val requireShowToast = { msg:String->
//        showToast.value = true;
//        toastMsg.value = msg
//    }
//    ShowToast(showToast, toastMsg)
    val requireShowToast:(String)->Unit = Msg.requireShowLongDuration


    val errWhenQuerySettingsFromDbStrRes = stringResource(R.string.err_when_querying_settings_from_db)
    val saved = stringResource(R.string.saved)

//    val showSetGlobalGitUsernameAndEmailDialog = rememberSaveable { mutableStateOf(false) }
    val setGlobalGitUsernameAndEmailStrRes = stringResource(R.string.set_global_username_and_email)
    val globalUsername = rememberSaveable { mutableStateOf("")}
    val globalEmail = rememberSaveable { mutableStateOf("")}


    val pageRequest = rememberSaveable { mutableStateOf("")}

    // global username and email dialog
    if(showSetGlobalGitUsernameAndEmailDialog.value) {
        AskGitUsernameAndEmailDialog(
            title = stringResource(R.string.user_info),
            text=setGlobalGitUsernameAndEmailStrRes,
            username=globalUsername,
            email=globalEmail,
            isForGlobal=true,
            curRepo=curRepo,
            onOk={
                doJobThenOffLoading(
                    //loadingOn = loadingOn, loadingOff=loadingOff,
//                    loadingText=appContext.getString(R.string.saving)
                ){
                    //save email and username
                    Libgit2Helper.saveGitUsernameAndEmailForGlobal(
                        requireShowErr=requireShowToast,
                        errText=errWhenQuerySettingsFromDbStrRes,
                        errCode1="15569470",  // for noticed where caused error
                        errCode2="10405847",
                        username=globalUsername.value,
                        email=globalEmail.value
                    )
                    showSetGlobalGitUsernameAndEmailDialog.value=false
                    requireShowToast(saved)
                }
            },
            onCancel={
                showSetGlobalGitUsernameAndEmailDialog.value=false
                globalUsername.value=""
                globalEmail.value=""
            },

            //字段都可为空，所以确定键总是启用
            enableOk={true},
        )
    }



    val showSetCurRepoGitUsernameAndEmailDialog = rememberSaveable { mutableStateOf( false)}
    val curRepoUsername = rememberSaveable { mutableStateOf("")}
    val curRepoEmail = rememberSaveable { mutableStateOf( "")}
    // repo username and email dialog
    if(showSetCurRepoGitUsernameAndEmailDialog.value) {
        AskGitUsernameAndEmailDialog(
            title=curRepo.value.repoName,
            text=stringResource(R.string.set_username_and_email_for_repo),
            username=curRepoUsername,
            email=curRepoEmail,
            isForGlobal=false,
            curRepo=curRepo,
            onOk={
                // save email and username
                doJobThenOffLoading(
                    //loadingOn = loadingOn, loadingOff=loadingOff,
//                    loadingText=appContext.getString(R.string.saving)
                ){
//                    MyLog.d(TAG, "curRepo.value.fullSavePath::"+curRepo.value.fullSavePath)
                    Repository.open(curRepo.value.fullSavePath).use { repo ->
                        //save email and username
                        Libgit2Helper.saveGitUsernameAndEmailForRepo(
                            repo = repo,
                            requireShowErr=requireShowToast,
                            username=curRepoUsername.value,
                            email=curRepoEmail.value
                        )
                    }
                    showSetCurRepoGitUsernameAndEmailDialog.value=false
                    requireShowToast(saved)
                }

            },
            onCancel={
                showSetCurRepoGitUsernameAndEmailDialog.value=false
                curRepoUsername.value=""
                curRepoEmail.value=""
            },

            //字段都可为空，所以确定键总是启用
            enableOk={true},
        )

    }

    val importRepoPath = rememberSaveable { mutableStateOf("") }
    val isReposParentFolderForImport = rememberSaveable { mutableStateOf(false) }

    if(showImportRepoDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.import_repo),
            requireShowTextCompose = true,
            textCompose = {
                MySelectionContainer {
                    Column(modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(5.dp)
                    ) {
                        Row(modifier = Modifier.padding(bottom = 15.dp)) {
                            ClickableText(
                                text = stringResource(R.string.please_grant_permission_before_import_repo),
                                overflow = TextOverflow.Visible,
                                fontWeight = FontWeight.Light,
                                modifier = MyStyleKt.ClickableText.modifier.clickable {
                                    // grant permission for read/write external storage
                                    if (activity == null) {
                                        Msg.requireShowLongDuration(activityContext.getString(R.string.please_go_to_system_settings_allow_manage_storage))
                                    } else {
                                        activity.getStoragePermission()
                                    }
                                },
                            )

                        }

                        SystemFolderChooser(path = importRepoPath)

                        Spacer(Modifier.height(15.dp))

                        MyCheckBox(text = stringResource(R.string.the_path_is_a_repos_parent_dir), value = isReposParentFolderForImport)

                        Spacer(Modifier.height(5.dp))

                        if(isReposParentFolderForImport.value) {
                            CheckBoxNoteText(stringResource(R.string.will_scan_repos_under_this_folder))
                        }
                    }
                }
            },
            okBtnText = stringResource(R.string.ok),
            cancelBtnText = stringResource(R.string.cancel),
            okBtnEnabled = importRepoPath.value.isNotBlank(),
            onCancel = { showImportRepoDialog.value = false },
        ) {

            doJobThenOffLoading(loadingOn, loadingOff, activityContext.getString(R.string.importing)) {
                try {
                    val newPath = importRepoPath.value

                    if(newPath.isNotBlank()) {
                        val f = File(newPath)

                        if(!f.canRead()) {
                            Msg.requireShowLongDuration(activityContext.getString(R.string.cant_read_path))
                            return@doJobThenOffLoading
                        }

                        if(!f.isDirectory) {
                            Msg.requireShowLongDuration(activityContext.getString(R.string.path_is_not_a_dir))
                            return@doJobThenOffLoading
                        }


                        showImportRepoDialog.value = false

                        val importRepoResult = AppModel.dbContainer.repoRepository.importRepos(dir=newPath, isReposParent=isReposParentFolderForImport.value)

                        // show a result dialog may better?

                        Msg.requireShowLongDuration(replaceStringResList(activityContext.getString(R.string.n_imported), listOf(""+importRepoResult.success)))

                    }else {
                        Msg.requireShow(activityContext.getString(R.string.invalid_path))
                    }
                }catch (e:Exception) {
                    MyLog.e(TAG, "import repo from ReposPage err: "+e.stackTraceToString())
                    Msg.requireShowLongDuration("err:${e.localizedMessage}")
                }finally {
                    changeStateTriggerRefreshPage(needRefreshRepoPage)

                    //这个判断可能不准
//                    if(importRepoResult.success>0) {
//                        changeStateTriggerRefreshPage(needRefreshRepoPage)
//                    }
                }
            }

        }

    }

//    val needRefreshRepoPage = rememberSaveable { mutableStateOf(false) }
//    val needRefreshRepoPage = rememberSaveable { mutableStateOf("") }

    //执行完doFetch/doMerge/doPush/doSync记得刷新页面，刷新页面不会改变列表滚动位置，所以放心刷，不用怕一刷新列表元素又滚动回第一个，让正在浏览仓库列表的用户困扰
    val doFetch:suspend (String?,RepoEntity)->Boolean = doFetch@{remoteNameParam:String?,curRepo:RepoEntity ->  //参数的remoteNameParam如果有效就用参数的，否则自己查当前head分支对应的remote
        //x 废弃，逻辑已经改了) 执行isReadyDoSync检查之前要先do fetch，想象一下，如果远程创建了一个分支，正好和本地的关联，但如果我没先fetch，那我检查时就get不到那个远程分支是否存在，然后就会先执行push，但可能远程仓库已经领先本地了，所以push也可能失败，但如果先fetch，就不会有这种问题了
        //fetch成功返回true，否则返回false
        var retVal = false
        try {
            Repository.open(curRepo.fullSavePath).use { repo ->
                var remoteName = remoteNameParam
                if(remoteName == null || remoteName.isBlank()) {
                    val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)
                    val upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)
                    remoteName = upstream.remote
                    if(remoteName == null || remoteName.isBlank()) {  //fetch不需合并，只需remote有效即可，所以只检查remote
                        throw RuntimeException(activityContext.getString(R.string.err_upstream_invalid_plz_go_branches_page_set_it_then_try_again))
//                        return@doFetch false
                    }
                }

                //执行到这，upstream的remote有效，执行fetch
//            只fetch当前分支关联的remote即可，获取仓库当前remote和credential的关联，组合起来放到一个pair里，pair放到一个列表里，然后调用fetch
                val credential = Libgit2Helper.getRemoteCredential(
                    dbContainer.remoteRepository,
                    dbContainer.credentialRepository,
                    curRepo.id,
                    remoteName,
                    trueFetchFalsePush = true
                )

                //执行fetch
                Libgit2Helper.fetchRemoteForRepo(repo, remoteName, credential, curRepo)

            }

            // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
            val repoDb = AppModel.dbContainer.repoRepository
            repoDb.updateLastUpdateTime(curRepo.id, getSecFromTime())

            retVal = true
        }catch (e:Exception) {
            //记录到日志
            //显示提示
            //保存数据库(给用户看的，消息尽量简单些)
            showErrAndSaveLog(TAG, "#doFetch() from Repo Page err:"+e.stackTraceToString(), "fetch err:"+e.localizedMessage, requireShowToast, curRepo.id)

            retVal = false
        }

        return@doFetch retVal
    }

    suspend fun doMerge(upstreamParam: Upstream?, curRepo:RepoEntity, trueMergeFalseRebase:Boolean=true):Boolean {
        try {
            //这的repo不能共享，不然一释放就要完蛋了，这repo不是rc是box单指针
            Repository.open(curRepo.fullSavePath).use { repo ->
                var upstream = upstreamParam
                if(Libgit2Helper.isUpstreamInvalid(upstream)) {  //如果调用者没传有效的upstream，查一下
                    val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)  //获取当前分支短名，例如 main
                    upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)  //获取当前分支的上游，例如 remote=origin 和 merge=refs/heads/main，参见配置文件 branch.yourbranchname.remote 和 .merge 字段
                    //如果查出的upstream还是无效，终止操作
                    if(Libgit2Helper.isUpstreamInvalid(upstream)) {
                        throw RuntimeException(activityContext.getString(R.string.err_upstream_invalid_plz_go_branches_page_set_it_then_try_again))
//                        return@doMerge false
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
                    throw RuntimeException(activityContext.getString(R.string.plz_set_username_and_email_first))
//                    return@doMerge false
                }

                val mergeResult = if(trueMergeFalseRebase) {
                    Libgit2Helper.mergeOneHead(
                        repo,
                        remoteRefSpec,
                        usernameFromConfig,
                        emailFromConfig,
                        settings = settings
                    )
                }else {
                    Libgit2Helper.mergeOrRebase(
                        repo,
                        targetRefName = remoteRefSpec,
                        username = usernameFromConfig,
                        email = emailFromConfig,
                        requireMergeByRevspec = false,
                        revspec = "",
                        trueMergeFalseRebase = false,
                        settings = settings
                    )
                }

                if (mergeResult.hasError()) {
                    //检查是否存在冲突条目
                    //如果调用者想自己判断是否有冲突，可传showMsgIfHasConflicts为false
                    if (mergeResult.code == Ret.ErrCode.mergeFailedByAfterMergeHasConfilts) {
                        throw RuntimeException(activityContext.getString(R.string.has_conflicts))

//                        if(trueMergeFalseRebase) {
//                            throw RuntimeException(appContext.getString(R.string.merge_has_conflicts))
//                        }else {
//                            throw RuntimeException(appContext.getString(R.string.rebase_has_conflicts))
//                        }
                    }

                    //显示错误提示
                    throw RuntimeException(mergeResult.msg)

                    //记到数据库error日志
//                    createAndInsertError(curRepo.id, mergeResult.msg)

//                    return@doMerge false
                }

                //执行到这就合并成功了

                //清下仓库状态
                Libgit2Helper.cleanRepoState(repo)

                //更新db显示通知
                Libgit2Helper.updateDbAfterMergeSuccess(mergeResult, activityContext, curRepo.id, {}, trueMergeFalseRebase)  //最后一个参数是合并成功或者不需要合并(uptodate)的信息提示函数，这个页面就不要在成功时提示了，合并完刷新下页面显示在仓库卡片上就行了

                return true
            }
        }catch (e:Exception) {
            //log
            showErrAndSaveLog(
                logTag = TAG,
                logMsg = "#doMerge(trueMergeFalseRebase=$trueMergeFalseRebase) from Repo Page err:"+e.stackTraceToString(),
                showMsg = e.localizedMessage ?:"err",
                showMsgMethod = requireShowToast,
                repoId = curRepo.id,
                errMsgForErrDb = "${if(trueMergeFalseRebase) "merge" else "rebase"} err: "+e.localizedMessage
            )

            return false
        }

    }

    val doPush:suspend (Upstream?,RepoEntity) -> Boolean  = doPush@{upstreamParam:Upstream?,curRepo:RepoEntity ->
        var retVal =false
        try {
//            MyLog.d(TAG, "#doPush: start")
            Repository.open(curRepo.fullSavePath).use { repo ->

                if(repo.headDetached()) {
                    throw RuntimeException(activityContext.getString(R.string.push_failed_by_detached_head))
//                    return@doPush false
                }


                var upstream:Upstream? = upstreamParam
                if(Libgit2Helper.isUpstreamInvalid(upstream)) {  //如果调用者没传有效的upstream，查一下
                    val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)  //获取当前分支短名，例如 main
                    upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)  //获取当前分支的上游，例如 remote=origin 和 merge=refs/heads/main，参见配置文件 branch.yourbranchname.remote 和 .merge 字段
                    //如果查出的upstream还是无效，终止操作
                    if(Libgit2Helper.isUpstreamInvalid(upstream)) {
                        throw RuntimeException(activityContext.getString(R.string.err_upstream_invalid_plz_go_branches_page_set_it_then_try_again))
//                        return@doPush false
                    }
                }
                MyLog.d(TAG, "#doPush: upstream.remote="+upstream!!.remote+", upstream.branchFullRefSpec="+upstream!!.branchRefsHeadsFullRefSpec)

                //执行到这里，必定有上游，push
                val credential = Libgit2Helper.getRemoteCredential(
                    dbContainer.remoteRepository,
                    dbContainer.credentialRepository,
                    curRepo.id,
                    upstream!!.remote,
                    trueFetchFalsePush = false
                )

                val ret = Libgit2Helper.push(repo, upstream!!.remote, upstream!!.pushRefSpec, credential)
                if(ret.hasError()) {
                    throw RuntimeException(ret.msg)
                }

                // 更新修改workstatus的时间，只更新时间就行，状态会在查询repo时更新
                val repoDb = AppModel.dbContainer.repoRepository
                repoDb.updateLastUpdateTime(curRepo.id, getSecFromTime())

                retVal =  true
            }
        }catch (e:Exception) {
            //log
            showErrAndSaveLog(TAG, "#doPush() err:"+e.stackTraceToString(), "push error:"+e.localizedMessage, requireShowToast, curRepo.id)

            retVal =  false
        }


//        如果push失败，有必要更新这个时间吗？没，所以我后来把更新时间放到成功代码块里了


        return@doPush retVal

    }

    val doClone = doClone@{repoList:List<RepoEntity> ->
        if(repoList.isEmpty()) {
            return@doClone
        }
//                        repoDtoList[idx].tmpStatus=""  //err状态，tmpStatus本来就没值，不用设
        doJobThenOffLoading {
            //更新仓库状态为待克隆
            repoList.forEach { curRepo ->
                val repoLock = Libgit2Helper.getRepoLock(curRepo.id)
                if(repoLock.isLocked) {
                    return@doJobThenOffLoading
                }

                repoLock.withLock {
                    val repoRepository = dbContainer.repoRepository
                    val repoFromDb = repoRepository.getById(curRepo.id)?:return@withLock
                    if(repoFromDb.workStatus == Cons.dbRepoWorkStatusCloneErr) {
                        repoFromDb.workStatus = Cons.dbRepoWorkStatusNotReadyNeedClone
//                                    repoFromDb.tmpStatus = appContext.getString(R.string.cloning)
                        repoFromDb.createErrMsg = ""
                        repoRepository.update(repoFromDb)

                        //这个可有可无，反正后面会刷新页面，刷新页面必须有，否则会变成cloning状态，然后就不更新了，还得手动刷新页面，麻烦
//                    repoList[idx]=repoFromDb  //刷新页面后会重新查询
                    }
                }
            }

            //刷新页面，然后就会执行克隆
            changeStateTriggerRefreshPage(needRefreshRepoPage)
        }

        Unit
    }

    val doCloneSingle = { targetRepo:RepoEntity ->
        doClone(listOf(targetRepo))
    }

    //sync之前，先执行stage，然后执行提交，如果成功，执行fetch/merge/push (= pull/push = sync)
    val doSync:suspend (RepoEntity)->Unit = doSync@{curRepo:RepoEntity ->
        try {
            Repository.open(curRepo.fullSavePath).use { repo ->
                if(repo.headDetached()) {
                    throw RuntimeException(activityContext.getString(R.string.sync_failed_by_detached_head))
                }


                //检查是否有upstream，如果有，do fetch do merge，然后do push,如果没有，请求设置upstream，然后do push
                val hasUpstream = Libgit2Helper.isBranchHasUpstream(repo)
                val shortBranchName = Libgit2Helper.getRepoCurBranchShortRefSpec(repo)
                if (!hasUpstream) {  //不存在上游，提示先去分支页面设置
                    //应该支持在仓库页面设置
                    throw RuntimeException(activityContext.getString(R.string.err_upstream_invalid_plz_go_branches_page_set_it_then_try_again))
                }


                //存在上游

                //取出上游
                val upstream = Libgit2Helper.getUpstreamOfBranch(repo, shortBranchName)
                val fetchSuccess = doFetch(upstream.remote, curRepo)
                if(!fetchSuccess) {
                    throw RuntimeException(activityContext.getString(R.string.fetch_failed))
//                    return@doSync
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
                    val mergeSuccess = doMerge(upstream, curRepo)
                    if(!mergeSuccess) {  //merge 失败，终止操作
                        //如果merge完存在冲突条目，就不要执行push了
                        if(Libgit2Helper.hasConflictItemInRepo(repo)) {  //检查失败原因是否是存在冲突，若是则显示提示
                            throw RuntimeException(activityContext.getString(R.string.has_conflicts_abort_sync))
                        }

                        throw RuntimeException(activityContext.getString(R.string.merge_failed))
//                        return@doSync
                    }
                }

                //如果执行到这，要么不存在上游，直接push(新建远程分支)；要么存在上游，但fetch/merge成功完成，需要push，所以，执行push
                //doPush
                val pushSuccess = doPush(upstream, curRepo)
                if(!pushSuccess) {
                    throw RuntimeException(activityContext.getString(R.string.push_failed))
                }
//                    requireShowToast(appContext.getString(R.string.sync_success))  //这个页面如果成功就不要提示了
            }
        }catch (e:Exception) {
            //log
            showErrAndSaveLog(TAG, "#doSync() err:"+e.stackTraceToString(), "sync err:"+e.localizedMessage, requireShowToast, curRepo.id)

        }

    }

    val doPull:suspend (RepoEntity)->Unit = {curRepo ->
        try {
            val fetchSuccess = doFetch(null, curRepo)
            if(!fetchSuccess) {
                throw RuntimeException(activityContext.getString(R.string.fetch_failed))
            }else {
                val mergeSuccess = doMerge(null, curRepo)
                if(!mergeSuccess){
                    throw RuntimeException(activityContext.getString(R.string.merge_failed))
                }
            }
        }catch (e:Exception){
            showErrAndSaveLog(TAG,"require pull error:"+e.stackTraceToString(), activityContext.getString(R.string.pull_err)+":"+e.localizedMessage, requireShowToast, curRepo.id)
        }
    }


    val getCurActiveList = {
        if(enableFilterState.value) filterList.value else repoList.value
    }

    val getCurActiveListState = {
        if(enableFilterState.value) filterListState else repoPageListState
    }

    /**
     * the `_idx` was `idx`, but then changed to find idx by repoId, so deprecated it, kept for backward compatible
    */
    val doActAndSetRepoStatus:suspend (Int, String, String, suspend ()->Unit) -> Unit = {_idx:Int, repoId:String, status:String, act: suspend ()->Unit ->
        // should update repoList ever, the filtered list will follow the repoList
        //应该总是更新repoList，过滤后的列表变化总是跟随repoList变化而变化
        val repoList = repoList.value
        // find idx by id, instead param idx
        val idx = repoList.indexOfFirst { it.id == repoId }
        //设置数组中元素的临时状态(重新查列表后消失，但因为上面设置状态到缓存了，所以重查时可通过缓存恢复)，但无需重新加载页面，轻量级操作，就能让用户看到临时状态
        doActIfIndexGood(idx,repoList) {
//            it.tmpStatus = status
            //必须copy一下，要不然还得刷新页面才能显示状态（ps：刷新页面显示状态是通过map存临时状态实现的，比这个操作重量级，应能避免则避免）
            repoList[idx]=it.copy(tmpStatus = status)
//            repoList.requireRefreshView()
        }
        //设置仓库临时状态(把临时状态设置到缓存里，不退出app都有效，目的是为了使重新查列表后临时状态亦可见)，这样重新加载页面时依然能看到临时状态
        RepoStatusUtil.setRepoStatus(repoId, status)
        //刷新以显示刚设置的状态
//        changeStateTriggerRefreshPage(needRefreshRepoPage)
        //执行操作
        act()

        //test 有时候操作执行太快，但我需要执行慢点来测试某些东西，所以加个delay方便测试
        //delay(3000)
        //test

        //清除缓存中的仓库状态
        RepoStatusUtil.clearRepoStatus(repoId)
        //重查下repo数据
        doActIfIndexGood(idx,repoList) {
            //无法确定执行什么操作，也无法确定会影响到什么，所以无法精准更新某字段，干脆在操作成功时，重查下数据，拿到最新状态就行了
            doJobThenOffLoading {
                //重查数据
                val repoDb = dbContainer.repoRepository
                val reQueriedRepoInfo = repoDb.getById(it.id)?:return@doJobThenOffLoading

                //检查仓库是否有未提交的修改
                if(reQueriedRepoInfo.requireAction == RepoAction.NEED_CHECK_UNCOMMITED_CHANGES) {
                    //捕获当前页面刷新状态值，相当于session id
                    val curRefreshValue = needRefreshRepoPage.value
                    checkGitStatusAndUpdateItemInList(reQueriedRepoInfo, idx, repoList, activityContext.getString(R.string.loading), pageChanged = {
                        needRefreshRepoPage.value != curRefreshValue
                    })
                }else { //不需要检查git status，直接更新卡片条目
                    repoList[idx] = reQueriedRepoInfo
                }


                //检查下如果当前长按菜单显示的是当前仓库，更新下和菜单项相关的字段。（这里不要赋值curRepo.value，以免并发冲突覆盖用户长按的仓库）
                val curRepoInMenu = curRepo.value  //这样修改的话，即使在下面赋值时用户长按了其他仓库也能正常工作，只是下面的赋值操作失去意义而已，但不会并发冲突，也不会显示或执行错误，但如果直接给curRepo.value赋值，则就有可能出错了，比如可能覆盖用户长按的仓库，发生用户长按了仓库a，但显示的却是仓库b的状态的情况
                if(curRepoInMenu.id == reQueriedRepoInfo.id) {  //如果，当前长按菜单显示的是当前修改的仓库，则更新下其tmpStatus以更新其菜单项的启用/禁用状态，例如执行fetch时，pull/push/fetch都禁用，但操作执行完后应重新启用，若不做这个检测，则只有重新长按菜单才会更新启用/禁用状态，有点不方便
                    //注意这里更新的只有和长按菜单相关的字段而已，其他字段无需更新
                    curRepoInMenu.tmpStatus = reQueriedRepoInfo.tmpStatus  //不出意外的话，这的tmpStatus应为空字符串，但保险起见，还是不要直接用空字符串赋值比较好
                    curRepoInMenu.isDetached = reQueriedRepoInfo.isDetached
                    curRepoInMenu.isShallow = reQueriedRepoInfo.isShallow
                }
//                repoList.requireRefreshView()
            }
        }

    }

    val showDelRepoDialog = rememberSaveable { mutableStateOf(false)}
    val willDeleteRepo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "willDeleteRepo", initValue = RepoEntity(id=""))
    val requireDelFilesOnDisk = rememberSaveable { mutableStateOf(false)}
    val requireDelRepo = {expectDelRepo:RepoEntity ->
        willDeleteRepo.value = expectDelRepo
        requireDelFilesOnDisk.value = false
        showDelRepoDialog.value = true
    }
    if(showDelRepoDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.del_repo),
//            text = stringResource(id = R.string.are_you_sure_to_delete)+": '"+willDeleteRepo.value.repoName+"' ?"+"\n"+ stringResource(R.string.will_delete_repo_and_all_its_files_on_disk),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    Row {
                        Text(text = stringResource(id = R.string.delete_repo)+":")
                    }
                    MySelectionContainer {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = willDeleteRepo.value.repoName,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(start = 16.dp),
                                //                                color = Color.Unspecified
                            )

                        }

                    }

                    Column {
                        Text(text = stringResource(R.string.are_you_sure))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(MyStyleKt.CheckoutBox.height)
                                .toggleable(
                                    enabled = true,
                                    value = requireDelFilesOnDisk.value,
                                    onValueChange = {
                                        requireDelFilesOnDisk.value = !requireDelFilesOnDisk.value
                                    },
                                    role = Role.Checkbox
                                )
                                .padding(horizontal = MyStyleKt.CheckoutBox.horizontalPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                enabled = true,
                                checked = requireDelFilesOnDisk.value,
                                onCheckedChange = null // null recommended for accessibility with screenreaders
                            )
                            Text(
                                text = stringResource(R.string.del_files_on_disk),
                                style = MaterialTheme.typography.bodyLarge,
                                //如果根据某些条件禁用这个勾选框，则用这行，把条件替换到enable里
//                                color = if(enable) MyStyleKt.TextColor.enable else if(inDarkTheme) MyStyleKt.TextColor.disable_DarkTheme else MyStyleKt.TextColor.disable,
                                //如果不需要禁用勾选框，则用这行，保持启用的颜色即可
                                color = MyStyleKt.TextColor.enable,

                                modifier = Modifier.padding(start = 16.dp),
                            )
                        }
                        if(requireDelFilesOnDisk.value) {
                            Text(text = "("+stringResource(R.string.will_delete_repo_and_all_its_files_on_disk)+")",
                                color = MyStyleKt.TextColor.danger()
                            )
                        }
                    }
                }

            },
            okTextColor = MyStyleKt.TextColor.danger(),
            onCancel = { showDelRepoDialog.value=false }
        ) {
            //关闭弹窗
            showDelRepoDialog.value=false

            val willDeleteRepo = willDeleteRepo.value
            val requireDelFilesOnDisk = requireDelFilesOnDisk.value
            val requireTransaction = true

            //执行删除
            doJobThenOffLoading {
                try {
                    val repoDb = AppModel.dbContainer.repoRepository
                    //删除仓库
                    repoDb.delete(
                        item = willDeleteRepo,
                        requireDelFilesOnDisk = requireDelFilesOnDisk,
                        requireTransaction = requireTransaction
                    )

                    Msg.requireShow(activityContext.getString(R.string.success))
                }catch (e:Exception){
                    Msg.requireShowLongDuration(e.localizedMessage ?:"err")
                    MyLog.e(TAG, "del repo in ReposPage err: ${e.stackTraceToString()}")
                }finally {
                    //请求刷新列表
                    changeStateTriggerRefreshPage(needRefreshRepoPage)
                }
            }
        }

    }

    val showRenameDialog = rememberSaveable { mutableStateOf(false)}
    val repoNameForRenameDialog = rememberSaveable { mutableStateOf( "")}
    val errMsgForRenameDialog = rememberSaveable { mutableStateOf("")}
    if(showRenameDialog.value) {
        val curRepo = curRepo.value

        ConfirmDialog(
            title = stringResource(R.string.rename_repo),
            requireShowTextCompose = true,
            textCompose = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                        ,
                        value = repoNameForRenameDialog.value,
                        singleLine = true,
                        isError = errMsgForRenameDialog.value.isNotBlank(),
                        supportingText = {
                            if (errMsgForRenameDialog.value.isNotBlank()) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = errMsgForRenameDialog.value,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        trailingIcon = {
                            if (errMsgForRenameDialog.value.isNotBlank()) {
                                Icon(imageVector=Icons.Filled.Error,
                                    contentDescription=null,
                                    tint = MaterialTheme.colorScheme.error)
                            }
                        },
                        onValueChange = {
                            repoNameForRenameDialog.value = it

                            // clear err msg
                            errMsgForRenameDialog.value = ""
                        },
                        label = {
                            Text(stringResource(R.string.new_name))
                        }
                    )
                }
            },
            okBtnText = stringResource(R.string.ok),
            cancelBtnText = stringResource(R.string.cancel),
            okBtnEnabled = repoNameForRenameDialog.value.isNotBlank() && errMsgForRenameDialog.value.isEmpty() && repoNameForRenameDialog.value != curRepo.repoName,
            onCancel = {showRenameDialog.value = false}
        ) {
            val newName = repoNameForRenameDialog.value
            val repoId = curRepo.id

            doJobThenOffLoading {
                try {
                    val repoDb = AppModel.dbContainer.repoRepository
                    if(strHasIllegalChars(newName)) {
                        errMsgForRenameDialog.value = activityContext.getString(R.string.name_has_illegal_chars)
                        return@doJobThenOffLoading
                    }

                    if(repoDb.isRepoNameExist(newName)) {
                        errMsgForRenameDialog.value = activityContext.getString(R.string.name_already_exists)
                        return@doJobThenOffLoading
                    }

                    showRenameDialog.value = false

                    repoDb.updateRepoName(repoId, newName)

                    Msg.requireShow(activityContext.getString(R.string.success))

                    changeStateTriggerRefreshPage(needRefreshRepoPage)
                }catch (e:Exception) {
                    val errmsg = e.localizedMessage ?: "rename repo err"
                    Msg.requireShowLongDuration(errmsg)
                    createAndInsertError(curRepo.id, "err: rename repo '${curRepo.repoName}' to ${repoNameForRenameDialog.value} failed, err is $errmsg")

                    changeStateTriggerRefreshPage(needRefreshRepoPage)
                }
            }
        }
    }


    val showUnshallowDialog = rememberSaveable { mutableStateOf(false)}
    if(showUnshallowDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.unshallow),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    Row {
                        Text(text = stringResource(R.string.will_do_unshallow_for_repo) + ":")
                    }
                    MySelectionContainer {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = curRepo.value.repoName,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(start = 16.dp),
                                //                                color = Color.Unspecified
                            )

                        }

                    }

                    Column {
                        Text(
                            text = stringResource(R.string.are_you_sure),
                        )
                        Text(
                            text = "(" + stringResource(R.string.unshallow_success_cant_back) + ")",
                            color = MyStyleKt.TextColor.danger()
                        )
                    }
                }
            },
            onCancel = { showUnshallowDialog.value=false}) {
            showUnshallowDialog.value=false
            doJobThenOffLoading {
                val curRepoId = curRepo.value.id
                val curRepoIdx = curRepoIndex.intValue
                val curRepoFullPath = curRepo.value.fullSavePath
                val curRepoVal =  curRepo.value
                doActAndSetRepoStatus(curRepoIdx, curRepoId, activityContext.getString(R.string.Unshallowing)) {
                    Repository.open(curRepoFullPath).use { repo->
                        val ret = Libgit2Helper.unshallowRepo(repo, curRepoVal,
                            AppModel.dbContainer.repoRepository,
                            AppModel.dbContainer.remoteRepository,
                            AppModel.dbContainer.credentialRepository
                        )
                        if(ret.hasError()) {
                            Msg.requireShow(ret.msg)
                        }

                    }
                }
            }

        }
    }

    //点击某个仓库卡片上的status文案，把仓库存上，方便弹窗执行后续操作
    val statusClickedRepo =mutableCustomStateOf(keyTag = stateKeyTag, keyName = "statusClickedRepo", RepoEntity(id=""))

    val showRequireActionsDialog = rememberSaveable { mutableStateOf(false)}
    if(showRequireActionsDialog.value) {
        val targetRepo = statusClickedRepo.value

        // invalid id
        if(targetRepo.id.isBlank()) {
            showRequireActionsDialog.value = false
            Msg.requireShow(stringResource(R.string.repo_id_invalid))
        }else {  // good repo id, show dialog
            ConfirmDialog(
                title = stringResource(R.string.require_actions),
                text = stringResource(R.string.will_go_to_changelist_then_you_can_continue_or_abort_your_merge_rebase_cherrpick),
                okBtnText = stringResource(id = R.string.ok),
                cancelBtnText = stringResource(id = R.string.cancel),
                onCancel = { showRequireActionsDialog.value = false }
            ) {
                showRequireActionsDialog.value = false

                //跳转到ChangeList页面
                goToChangeListPage(targetRepo)
            }
        }
    }



    val goToThisRepoAndHighlightingIt = goTo@{ targetId:String ->
        if(targetId.isBlank()) {
            return@goTo
        }

        try {
            val list = getCurActiveList()
            val listState = getCurActiveListState()
            val targetIndex = list.toList().indexOfFirst { it.id == targetId }
            if(targetIndex != -1) {  // found in current active list
                // index / perRowCount 即条目所在的chunked后的索引行
                UIHelper.scrollToItem(scope, listState, targetIndex / repoCountEachRow)
                requireBlinkIdx.intValue = targetIndex
            }else{
                if(repoPageFilterModeOn.value) {
                    //从源列表找
                    val indexInOriginList = repoList.value.toList().indexOfFirst { it.id == targetId }

                    if(indexInOriginList != -1){  // found in origin list
                        repoPageFilterModeOn.value = false  //关闭过滤模式
                        showBottomSheet.value = false  //关闭菜单

                        //定位条目
                        UIHelper.scrollToItem(scope, repoPageListState, indexInOriginList / repoCountEachRow)
                        requireBlinkIdx.intValue = indexInOriginList  //设置条目闪烁以便用户发现
                    }else {
                        Msg.requireShow(activityContext.getString(R.string.not_found))
                    }
                }else {
                    Msg.requireShow(activityContext.getString(R.string.not_found))
                }
            }
        }catch (_:Exception) {

        }
    }

    // bottom bar block start
    val quitSelectionMode = {
        isSelectionMode.value = false
        selectedItems.value.clear()
    }

    val getSelectedFilesCount = {
        selectedItems.value.size
    }

    val selectedSingle = {
        getSelectedFilesCount() == 1
    }

    val hasSelectedItems = {
        getSelectedFilesCount() > 0
    }

    val containsForSelected = { srcList:List<RepoEntity>, item:RepoEntity ->
        srcList.indexOfFirst { it.equalsForSelected(item) } != -1
    }

    val switchItemSelected = { item: RepoEntity ->
        isSelectionMode.value = true
        UIHelper.selectIfNotInSelectedListElseRemove(item, selectedItems.value, contains = containsForSelected)
    }

    val selectItem = { item:RepoEntity ->
        isSelectionMode.value = true
        UIHelper.selectIfNotInSelectedListElseNoop(item, selectedItems.value, contains = containsForSelected)
    }

    val repoCardTitleOnClick = { item:RepoEntity ->
        switchItemSelected(item)
    }

    val selectionModeIconList = listOf(
        Icons.Filled.Downloading,  // fetch
        Icons.Filled.Download,  // pull
        Icons.Filled.Publish,  // push
        ImageVector.vectorResource(R.drawable.two_way_sync),  //sync
        Icons.Filled.SelectAll,  //Select All
    )
    val selectionModeIconTextList = listOf(
        stringResource(R.string.fetch),
        stringResource(R.string.pull),
        stringResource(R.string.push),
        stringResource(R.string.sync),
        stringResource(R.string.select_all),
    )

    val doActIfRepoGood = { curRepo:RepoEntity, act: suspend ()->Unit ->
        val repoStatusGood = curRepo.gitRepoState!=null && !Libgit2Helper.isRepoStatusNotReadyOrErr(curRepo)

        val isDetached = dbIntToBool(curRepo.isDetached)
        val hasTmpStatus = curRepo.tmpStatus.isNotBlank()  //如果有设临时状态，说明在执行某个操作，比如正在fetching，所以这时应该不允许再执行fetch或pull之类的操作，我做了处理，即使用户去cl页面执行，也无法绕过此限制
        val actionEnabled = !isDetached && !hasTmpStatus

        if(repoStatusGood && actionEnabled) {
            doJobThenOffLoading {
                val lock = Libgit2Helper.getRepoLock(curRepo.id)
                //maybe do other jobs
                if(lock.isLocked) {
                    return@doJobThenOffLoading
                }

                lock.withLock {
                    act()
                }
            }
        }
    }

    val invalidIdx = remember { -1 }

    val selectionModeIconOnClickList = listOf<()->Unit>(
        fetch@{
            selectedItems.value.toList().forEach { curRepo ->
                doActIfRepoGood(curRepo) {
                    //fetch 当前仓库上游的remote
                    doActAndSetRepoStatus(invalidIdx, curRepo.id, activityContext.getString(R.string.fetching)) {
                        doFetch(null, curRepo)
                    }
                }
            }
        },
        pull@{
            selectedItems.value.toList().forEach { curRepo ->
                doActIfRepoGood(curRepo) {
                    doActAndSetRepoStatus(invalidIdx, curRepo.id, activityContext.getString(R.string.pulling)) {
                        doPull(curRepo)
                    }
                }
            }
        },
        push@{
            selectedItems.value.toList().forEach { curRepo ->
                doActIfRepoGood(curRepo) {
                    doActAndSetRepoStatus(invalidIdx, curRepo.id, activityContext.getString(R.string.pushing)) {
                        doPush(null, curRepo)
                    }
                }
            }

        },
        sync@{
            selectedItems.value.toList().forEach { curRepo ->
                doActIfRepoGood(curRepo) {
                    doActAndSetRepoStatus(invalidIdx, curRepo.id, activityContext.getString(R.string.syncing)) {
                        doSync(curRepo)
                    }
                }
            }
        },
        selectAll@{
            val list = if(enableFilterState.value) filterList.value else repoList.value

            selectedItems.value.clear()
            selectedItems.value.addAll(list)
            Unit
        }
    )

    val selectionModeIconEnableList = listOf(
        // 点击后再检查取出可执行fetch的仓库
        fetchEnable@{ hasSelectedItems() },
        pullEnable@{ hasSelectedItems() },
        pushEnable@{ hasSelectedItems() },
        syncEnable@{ hasSelectedItems() },
        selectAllEnable@{ true },
    )

    val selectionModeMoreItemTextList = listOf(
        stringResource(R.string.clone), // multi
        stringResource(R.string.remotes), // single
        stringResource(R.string.tags),  // single
        stringResource(R.string.submodules), // single
        stringResource(R.string.user_info), // multi
        stringResource(R.string.unshallow), // multi
        stringResource(R.string.set_upstream), // single
        stringResource(R.string.changelist), // single
        stringResource(R.string.stash), // single
        stringResource(R.string.reflog), // single
        stringResource(R.string.rename), // single
        stringResource(R.string.delete), // multi
    )

    val selectionModeMoreItemOnClickList = listOf(
        // retry clone for cloned err repos
        clone@{
            doClone(selectedItems.value.filter { it.workStatus == Cons.dbRepoWorkStatusCloneErr })
        },
        remotes@{
            TODO()
        },
        tags@{
            TODO()
        },
        submodules@{

        },

        userInfo@{

        },
        unshallow@{

        },
        setUpstream@{

        },
        changelist@{

        },
        stash@{

        },
        reflog@{

        },
        rename@{

        },
        delete@{

        }
    )

    val selectionModeMoreItemEnableList = listOf(
        clone@{
            hasSelectedItems()
        },
        remotes@{
            selectedSingle()
        },
        tags@{
            selectedSingle()
        },
        submodules@{
            selectedSingle()
        },

        userInfo@{
            hasSelectedItems()
        },
        unshallow@{
            hasSelectedItems()
        },
        setUpStream@{
            selectedSingle()
        },
        changelist@{
            selectedSingle()
        },
        stash@{
            selectedSingle()
        },
        reflog@{
            selectedSingle()
        },
        rename@{
            selectedSingle()
        },
        delete@{
            hasSelectedItems()
        }
    )

    val showSelectedItemsShortDetailsDialog = rememberSaveable { mutableStateOf(false)}
    val selectedItemsShortDetailsStr = rememberSaveable { mutableStateOf("")}
    if(showSelectedItemsShortDetailsDialog.value) {
        CopyableDialog(
            title = stringResource(id = R.string.selected_str),
            text = selectedItemsShortDetailsStr.value,
            onCancel = { showSelectedItemsShortDetailsDialog.value = false }
        ) {
            showSelectedItemsShortDetailsDialog.value = false
            clipboardManager.setText(AnnotatedString(selectedItemsShortDetailsStr.value))
            Msg.requireShow(activityContext.getString(R.string.copied))
        }
    }

    val showSelectedItems = {
        val list = selectedItems.value.toList()
        val sb = StringBuilder()
        list.toList().forEach {
            sb.append(it.repoName).append("\n\n")
        }
        selectedItemsShortDetailsStr.value = sb.removeSuffix("\n").toString()
        showSelectedItemsShortDetailsDialog.value = true
    }

    // bottom bar block end


    //back handler block start
    val isBackHandlerEnable = rememberSaveable { mutableStateOf(true)}
    val backHandlerOnBack = ComposeHelper.getDoubleClickBackHandler(context = activityContext, openDrawer = openDrawer, exitApp = exitApp)
    //注册BackHandler，拦截返回键，实现双击返回和返回上级目录
    BackHandler(enabled = isBackHandlerEnable.value, onBack = {
        if(isSelectionMode.value){
            quitSelectionMode()
        } else if(repoPageFilterModeOn.value) {
            repoPageFilterModeOn.value = false
        }else {
            backHandlerOnBack()
        }
    })
    //back handler block end

    if(showBottomSheet.value) {
        val repoDto = curRepo.value
        val repoStatusGood = repoDto.gitRepoState!=null && !Libgit2Helper.isRepoStatusNotReadyOrErr(repoDto)

        val isDetached = dbIntToBool(curRepo.value.isDetached)
        val hasTmpStatus = curRepo.value.tmpStatus.isNotBlank()  //如果有设临时状态，说明在执行某个操作，比如正在fetching，所以这时应该不允许再执行fetch或pull之类的操作，我做了处理，即使用户去cl页面执行，也无法绕过此限制
        val actionEnabled = !isDetached && !hasTmpStatus
        BottomSheet(showBottomSheet, sheetState, curRepo.value.repoName) {
            if(repoStatusGood) {

    //            BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.sync), enabled = actionEnabled) {
    //                doJobThenOffLoading {
    //                    try {
    //                        doSync(curRepo.value)
    //
    //                    }finally {
    //                        changeStateTriggerRefreshPage(needRefreshRepoPage)
    //                    }
    //                }
    //            }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.remotes)) {
                    //管理remote，右上角有个fetch all可fetch所有remote
                    navController.navigate(Cons.nav_RemoteListScreen+"/"+curRepo.value.id)
                }

                if(dev_EnableUnTestedFeature || tagsTestPassed) {
                    val isPro = UserUtil.isPro()
                    val text = if(isPro) stringResource(R.string.tags) else stringResource(R.string.tags_pro)

                    //非pro用户能看到这个选项但不能用
                    BottomSheetItem(sheetState, showBottomSheet, text, enabled = isPro) {
                        //跳转到tags页面
                        navController.navigate(Cons.nav_TagListScreen + "/" + curRepo.value.id)
                    }

                }

                if(proFeatureEnabled(submoduleTestPassed)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.submodules)) {
                        navController.navigate(Cons.nav_SubmoduleListScreen + "/" + curRepo.value.id)
                    }
                }

    //            BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.reflog)) {
    //             //日后实现
    //            }
    //            BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.tags)) {
    //              日后实现
    //            }
    //            BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.settings)) {
    //              日后实现
    //            }
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.user_info)) {
                    showSetCurRepoGitUsernameAndEmailDialog.value = true
                }
                //对shallow(克隆时设置了depth)的仓库提供一个unshallow选项
                if(dbIntToBool(curRepo.value.isShallow)) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.unshallow),
                        textDesc = stringResource(R.string.cancel_clone_depth),
                        enabled = !hasTmpStatus  //unshallow是针对仓库的行为，会对仓库所有remote执行unshallow fetch，而不管仓库是否detached HEAD，由于仓库remotes总是可用，所以，这里不用判断是否detached
                    ) {
                        showUnshallowDialog.value = true
                    }
                }

//                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.explorer_files)) {
//                    showBottomSheet.value=false  //不知道为什么，常规的关闭菜单不太好使，一跳转页面就废了，所以手动隐藏下菜单
//                    goToFilesPage(curRepo.value.fullSavePath)
//                }


                //go to changelist，避免侧栏切换到changelist时刚好某个仓库加载很慢导致无法切换其他仓库
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.changelist)) {
                    showBottomSheet.value=false  //不知道为什么，常规的关闭菜单不太好使，一跳转页面就废了，所以手动隐藏下菜单
                    goToChangeListPage(curRepo.value)
                }

                //非pro这两个选项直接不可见，弄成能看不能用有点麻烦，直接非pro隐藏算了
                if(UserUtil.isPro()) {
                    if(dev_EnableUnTestedFeature || stashTestPassed) {
                        BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.stash)) {
                            showBottomSheet.value=false
                            navController.navigate(Cons.nav_StashListScreen+"/"+curRepo.value.id)
                        }
                    }

                    if(dev_EnableUnTestedFeature || reflogTestPassed){
                        BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.reflog)) {
                            showBottomSheet.value=false  //不知道为什么，常规的关闭菜单不太好使，一跳转页面就废了，所以手动隐藏下菜单
                            navController.navigate(Cons.nav_ReflogListScreen+"/"+curRepo.value.id)
                        }
                    }
                }
            }

            if(proFeatureEnabled(repoRenameTestPassed)) {
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.rename)) {
                    repoNameForRenameDialog.value = curRepo.value.repoName
                    showRenameDialog.value = true
                }
            }


            //show jump to parent repo if has parentRepoId
            if(curRepo.value.parentRepoId.isNotBlank()) {
                BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.go_parent)) {
                    goToThisRepoAndHighlightingIt(curRepo.value.parentRepoId)
                }
            }

            BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.delete), textColor = MyStyleKt.TextColor.danger()) {
                requireDelRepo(curRepo.value)
            }
        }
    }

    // this feel sick, screen flash then return, just sick, so disbale
//    if(isLoading.value) {
//
////        Column(
////            modifier = Modifier
////                .fillMaxSize()
////                .padding(contentPadding)
////                .verticalScroll(StateUtil.getRememberScrollState())
////            ,
////            verticalArrangement = Arrangement.Center,
////            horizontalAlignment = Alignment.CenterHorizontally,
////
////        ) {
////            Text(text = loadingText.value)
////        }
//
//        LoadingDialog(loadingText.value)
//
//    }

    if (!isLoading.value && repoList.value.isEmpty()) {  //无仓库，显示添加按钮
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())

                ,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            //interactionSource和indication的作用是隐藏按下时的背景半透明那个按压效果，很难看，所以隐藏
            Column(modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                navController.navigate(Cons.nav_CloneScreen+"/null")  //不传repoId，就是null，等于新建模式
            },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ){
                Row{
                    Icon(modifier = Modifier.size(50.dp),
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add),
                        tint = MyStyleKt.IconColor.normal
                    )
                }
                Row {
                    Text(text = stringResource(id = R.string.add_a_repo),
                        style = MyStyleKt.ClickableText.style,
                        color = MyStyleKt.ClickableText.color,
                        fontSize = MyStyleKt.TextSize.default
                    )
                }
            }

        }
    }


    // 向下滚动监听，开始
//    val firstVisible = remember { derivedStateOf { if(enableFilterState.value) filterListState.value.firstVisibleItemIndex else repoPageListState.firstVisibleItemIndex } }
//    ScrollListener(
//        nowAt = firstVisible.value,
//        onScrollUp = {repoPageScrollingDown.value = false}
//    ) { // onScrollDown
//        repoPageScrollingDown.value = true
//    }

//
//    val lastAt = remember { mutableIntStateOf(0) }
//    val lastIsScrollDown = remember { mutableStateOf(false) }
//    val forUpdateScrollState = remember {
//        derivedStateOf {
//            val nowAt = if(enableFilterState.value) {
//                filterListState.firstVisibleItemIndex
//            } else {
//                repoPageListState.firstVisibleItemIndex
//            }
//
//            val scrolledDown = nowAt > lastAt.intValue  // scroll down
////            val scrolledUp = nowAt < lastAt.intValue
//
//            val scrolled = nowAt != lastAt.intValue  // scrolled
//            lastAt.intValue = nowAt
//
//            // only update state when this scroll down and last is not scroll down, or this is scroll up and last is not scroll up
//            if(scrolled && ((lastIsScrollDown.value && !scrolledDown) || (!lastIsScrollDown.value && scrolledDown))) {
//                repoPageScrolled.value = true
//            }
//
//            lastIsScrollDown.value = scrolledDown
//        }
//    }.value
    // 向下滚动监听，结束


    if(!isLoading.value && repoList.value.isNotEmpty()) {  //有仓库

        //根据关键字过滤条目
        val k = repoPageFilterKeyWord.value.text.lowercase()  //关键字
        val enableFilter = repoPageFilterModeOn.value && k.isNotEmpty()
        val filteredListTmp = if(enableFilter){
            val tmpList = repoList.value.filter {
                it.repoName.lowercase().contains(k)
                        || it.branch.lowercase().contains(k)
                        || it.gitRepoState.toString().lowercase().contains(k)
                        || it.cloneUrl.lowercase().contains(k)
                        || it.lastCommitHash.lowercase().contains(k)
                        || it.latestUncheckedErrMsg.lowercase().contains(k)
                        || it.pullRemoteName.lowercase().contains(k)
                        || it.pushRemoteName.lowercase().contains(k)
                        || it.pullRemoteUrl.lowercase().contains(k)
                        || it.pushRemoteUrl.lowercase().contains(k)
                        || it.tmpStatus.lowercase().contains(k)
                        || it.upstreamBranch.lowercase().contains(k)
                        || it.createErrMsg.lowercase().contains(k)
                        || it.fullSavePath.lowercase().contains(k)
                        || it.parentRepoName.lowercase().contains(k)
                        || it.getOther().lowercase().contains(k)
            }
            filterList.value.clear()
            filterList.value.addAll(tmpList)
            tmpList
        }else {
            repoList.value
        }


        //若一行只有一个条目，fillMaxWidth()
        val requireFillMaxWidth = repoCountEachRow == 1
        //如果repoCountEachRow==1，永远不需要padding，因为list.size是整数，而任何整数以1取模结果都为0
        val paddingItemCount = filteredListTmp.size % repoCountEachRow
        val needPaddingItems = paddingItemCount != 0

        val filteredList =  filteredListTmp.chunked(repoCountEachRow)
        val lastChunkListIndex = filteredList.lastIndex


        val listState = if(enableFilter) filterListState else repoPageListState

        //更新是否启用filter
        enableFilterState.value = enableFilter

        MyLazyColumn(
            contentPadding = contentPadding,
            list = filteredList,
            listState = listState,
            requireForEachWithIndex = true,
            requirePaddingAtBottom = true
        ) {chunkedListIdx, chunkedList ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                chunkedList.forEachIndexed { subListIdx, element ->
                    val idx = chunkedListIdx * repoCountEachRow + subListIdx
                    //状态小于errValStart意味着一切正常；状态大于等于errValStart，意味着出错，禁用长按功能，直接把可以执行的操作例如删除仓库和编辑仓库之类的显示在卡片上，方便用户处置出错的仓库
                    // 如果有必要细分状态，可以改成这样: if(it.workStatus==cloningStatus) go cloningCard, else if other status, go other card, else go normal RepoCard
                    if (Libgit2Helper.isRepoStatusNoErr(element)) {
                        //未出错的仓库
                        RepoCard(
                            itemWidth = itemWidth,
                            requireFillMaxWidth = requireFillMaxWidth,
                            showBottomSheet = showBottomSheet,
                            curRepo = curRepo,
                            curRepoIndex = curRepoIndex,
                            repoDto = element,
                            repoDtoIndex = idx,

                            itemSelected = containsForSelected(selectedItems.value, element),
                            titleOnClick = repoCardTitleOnClick,

                            goToFilesPage = goToFilesPage,
                            requireBlinkIdx = requireBlinkIdx,
                            pageRequest = pageRequest,
                        ) workStatusOnclick@{ clickedRepo, status ->  //这个是点击status的callback，这个status其实可以不传，因为这里的lambda能捕获到数组的元素，就是当前仓库

                            //把点击状态的仓库存下来
                            statusClickedRepo.value = clickedRepo  //其实这个clickedRepo直接用这里element替代也可，但用回调里参数感觉更合理

                            //目前status就三种状态：up-to-date/has conflicts/need sync，第1种不用处理
                            if(status == Cons.dbRepoWorkStatusMerging
                                || status == Cons.dbRepoWorkStatusRebasing
                                || status == Cons.dbRepoWorkStatusCherrypicking
                            ){ //merge/rebase/cherrypick弹窗提示需要continue或abort
                                showRequireActionsDialog.value = true
                            } else if (
                                status == Cons.dbRepoWorkStatusHasConflicts
                                || status == Cons.dbRepoWorkStatusNeedCommit
                            ) {
                                //导航到changelist并定位到当前仓库
                                goToChangeListPage(clickedRepo)
                            } else if (status == Cons.dbRepoWorkStatusNeedSync) {
                                // do sync
                                doJobThenOffLoading {
                                    doActAndSetRepoStatus(idx, clickedRepo.id, activityContext.getString(R.string.syncing)) {
                                        doSync(clickedRepo)
                                    }
                                }
                            }
                        }
                    } else {
                        //show Clone error repo card，显示克隆错误，有重试和编辑按钮，编辑可重新进入克隆页面编辑当前仓库的信息，然后重新克隆
                        RepoCardError(
                            itemWidth = itemWidth,
                            requireFillMaxWidth = requireFillMaxWidth,
//                        showBottomSheet = showBottomSheet,
//                        curRepo = curRepo,
                            repoDto = element,
                            repoDtoList = repoList.value,
                            idx = idx,

                            itemSelected = containsForSelected(selectedItems.value, element),
                            titleOnClick = repoCardTitleOnClick,

                            doCloneSingle = doCloneSingle,
                            requireDelRepo = requireDelRepo,
                            requireBlinkIdx = requireBlinkIdx,
                            copyErrMsg = {msg->
                                clipboardManager.setText(AnnotatedString(msg))
                                Msg.requireShow(activityContext.getString(R.string.copied))
                            }

                        )
                        //                            if(it.workStatus == Cons.dbRepoWorkStatusCloneErr){  //克隆错误
                        //                            } // else if(other type err happened) ，显示其他类型的ErrRepoCard ,这里还能细分不同的错误显示不同的界面，例如克隆错误和初始化错误可以显示不同界面，后面加else if 即可
                    }


                }

                // padding for make item alight to start(left or right)
                // repoCountEachRow为1时，永远不需要padding，因为needPaddingItems在其为1时肯定为假
                if(needPaddingItems && chunkedListIdx == lastChunkListIndex) {
                    for(i in 0 until (repoCountEachRow - chunkedList.size)) {
                        Column(modifier = Modifier.width(itemWidth.dp)) {}
                    }
                }
            }
        }


    }


    if(pageRequest.value == PageRequest.goParent) {
        PageRequest.clearStateThenDoAct(pageRequest) {
            goToThisRepoAndHighlightingIt(curRepo.value.parentRepoId)
        }
    }



    if(isSelectionMode.value) {
        BottomBar(
            quitSelectionMode=quitSelectionMode,
            iconList=selectionModeIconList,
            iconTextList=selectionModeIconTextList,
            iconDescTextList=selectionModeIconTextList,
            iconOnClickList=selectionModeIconOnClickList,
            iconEnableList=selectionModeIconEnableList,
            enableMoreIcon=hasSelectedItems(),
            visibleMoreIcon=true,
            moreItemTextList=selectionModeMoreItemTextList,
            moreItemOnClickList=selectionModeMoreItemOnClickList,
            getSelectedFilesCount = getSelectedFilesCount,
            moreItemEnableList = selectionModeMoreItemEnableList,
            moreItemVisibleList = selectionModeMoreItemEnableList,  //禁用即隐藏
            countNumOnClickEnabled = true,
            countNumOnClick = showSelectedItems,
        )

    }


    //没换页面，但需要刷新页面，这时LaunchedEffect不会执行，就靠这个变量控制刷新页面了
//    if(needRefreshRepoPage.value) {
//        initRepoPage()
//        needRefreshRepoPage.value=false
//    }
    //compose创建时的副作用
    LaunchedEffect(needRefreshRepoPage.value) {
        try {
            // 仓库页面检查仓库状态，对所有状态为notReadyNeedClone的仓库执行clone，卡片把所有状态为notReadyNeedClone的仓库都设置成不可操作，显示正在克隆loading信息
            doInit(
                dbContainer = dbContainer,
                repoDtoList = repoList,
                cloningText = cloningText,
                unknownErrWhenCloning = unknownErrWhenCloning,
                loadingOn = loadingOn,
                loadingOff = loadingOff,
                activityContext = activityContext,
                goToThisRepoId = goToThisRepoId,
                goToThisRepoAndHighlightingIt = goToThisRepoAndHighlightingIt,
                settings=settings,
                refreshId=needRefreshRepoPage.value,
                latestRefreshId = needRefreshRepoPage
            )

        } catch (cancel: Exception) {
//            LaunchedEffect job cancelled
        }
    }
}

private fun doInit(
    dbContainer: AppContainer,
    repoDtoList: CustomStateListSaveable<RepoEntity>,
    cloningText: String,
    unknownErrWhenCloning: String,
    loadingOn:(String)->Unit,
    loadingOff:()->Unit,
    activityContext:Context,
    goToThisRepoId: MutableState<String>,
    goToThisRepoAndHighlightingIt:(id:String) ->Unit,
    settings:AppSettings,
    refreshId:String,
    latestRefreshId:MutableState<String>,
){
    val pageChanged = {
        refreshId != latestRefreshId.value
    }

    val loadingText = activityContext.getString(R.string.loading)

    doJobThenOffLoading(loadingOn, loadingOff, loadingText) {
        //执行仓库页面的初始化操作
        val repoRepository = dbContainer.repoRepository
        //貌似如果用Flow，后续我更新数据库，不需要再次手动更新State数据就会自动刷新，也就是Flow会观测数据，如果改变，重新执行sql获取最新的数据，但最好还是手动更新，避免资源浪费
        val repoListFromDb = repoRepository.getAll();

        repoDtoList.value.clear()
        repoDtoList.value.addAll(repoListFromDb)

        // complex code for update item and remove non-exist item, but the effect just no-difference with clear then addAll, so, disable it
//        if(repoDtoList.value.isEmpty()) {
//            repoDtoList.value.addAll(repoListFromDb)
//        }else if(repoListFromDb.isEmpty()){
//            repoDtoList.value.clear()
//        }else {
//            val needRemoveRepoId = mutableListOf<String>()
//            val repoListInPageCopy = repoDtoList.value.toList()
//            repoListInPageCopy.forEachIndexed { index, it->
//                var stillExist = false
//                for(repoFromDb in repoListFromDb) {
//                    if(repoFromDb.id == it.id) {
//                        // update repo info
//                        repoDtoList.value[index] = repoFromDb
//                        stillExist = true
//                        break
//                    }
//                }
//
//                if(stillExist.not()) {
//                    needRemoveRepoId.add(it.id)
//                }
//            }
//
//            needRemoveRepoId.forEach { removeId ->
//                repoDtoList.value.removeIf { removeId == it.id }
//            }
//        }




        if(goToThisRepoId.value.isNotBlank()) {
            val target = goToThisRepoId.value
            goToThisRepoId.value = ""

            goToThisRepoAndHighlightingIt(target)
        }

//        repoDtoList.requireRefreshView()
//        repoDtoList.requireRefreshView()
        for ((idx,item) in repoDtoList.value.toList().withIndex()) {
            //对需要克隆的仓库执行克隆
            if (item.workStatus == Cons.dbRepoWorkStatusNotReadyNeedClone) {
                //设置临时状态为 正在克隆...
                //我严重怀疑这里不需要拷贝元素赋值就可立即看到修改后的元素状态是因为上面addAll和这里的赋值操作刚好在视图的同一个刷新间隔里，所以到下一个刷新操作时，可看到在这修改后的状态
                repoDtoList.value[idx].tmpStatus = cloningText
//                repoDtoList.requireRefreshView()

                Libgit2Helper.cloneSingleRepo(item, repoRepository, settings, unknownErrWhenCloning, repoDtoList.value, idx)


            }else if(item.requireAction == RepoAction.NEED_CHECK_UNCOMMITED_CHANGES) {
                checkGitStatusAndUpdateItemInList(item, idx, repoDtoList.value, loadingText, pageChanged)
            } else {
                //TODO: check git status with lock of every repo, get lock then query repo info from db,
                // if updatetime field changed, then update item in repodtolist, else do git status,
                // then update db and repodtolist
            }
        }


        //在这clear()很可能不管用，因为上面的闭包捕获了repoDtoList当时的值，而当时是有数据的，也就是数据在这被清，然后在上面的闭包被回调的时候，又被填充上了闭包创建时的数据，同时加上了闭包执行后的数据，所以，在这清这个list就“不管用”了，实际不是不管用，只是清了又被填充了
//                repoDtoList.clear()
    }
}

private fun updateRepoListByIndexOrId(newItem:RepoEntity, idx: Int, list:MutableList<RepoEntity>, expectListSize:Int) {
    if(list.size == expectListSize) { //列表很可能没改变，直接赋值，若出错，刷新页面即可解决
        list[idx] = newItem
    }else {  //列表一定改变了，根据id查找仓库，若找到，更新，否则忽略
        val targetIdx = list.indexOfFirst { it.id == newItem.id }
        if(targetIdx != -1) {  // found
            list[targetIdx] = newItem
        }
    }
}

/**
 * 检查仓库是否有未提交修改并在检查完毕且页面没刷新时更新list中的对应条目
 */
private fun checkGitStatusAndUpdateItemInList(item:RepoEntity, idx:Int, repoList:MutableList<RepoEntity>, loadingText:String, pageChanged:()->Boolean) {
    //用来粗略检查仓库是否已经改变
    val repoListSizeSnapshot = repoList.size

    val needUpdateTmpStatus = item.tmpStatus.isBlank()

    if(needUpdateTmpStatus) {
        val newRepo = item.copyAllFields()

        //更新临时状态
        newRepo.tmpStatus = loadingText

        //列表列表条目
        updateRepoListByIndexOrId(newRepo, idx, repoList, repoListSizeSnapshot)
    }

    doJobThenOffLoading {
        Repository.open(item.fullSavePath).use { repo ->
            MyLog.d(TAG, "#checkRepoGitStatus: checking git status for repo '${item.repoName}'")

            val needCommit = Libgit2Helper.hasUncommittedChanges(repo)

            MyLog.d(TAG, "#checkRepoGitStatus: repoName=${item.repoName}, repoId=${item.id}, needCommit=$needCommit, pageChanged=${pageChanged()}")

            //如果页面没改变（没重新刷新），更新列表
            if(!pageChanged()) {
                val newRepo = item.copyAllFields()
                //操作已经执行完毕，清空需要执行的操作
                newRepo.requireAction = RepoAction.NONE

                //清空临时状态
                if(needUpdateTmpStatus) {
                    newRepo.tmpStatus = ""
                }

                //如果需要提交，则更新状态为需要提交；否则检查ahead和behind状态，可能是up to date也可能需要sync
                if(needCommit) {
                    newRepo.workStatus = Cons.dbRepoWorkStatusNeedCommit
                }

                updateRepoListByIndexOrId(newRepo, idx, repoList, repoListSizeSnapshot)
            }

        }
    }


}

