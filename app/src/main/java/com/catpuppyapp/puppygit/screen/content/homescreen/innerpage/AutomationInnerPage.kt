package com.catpuppyapp.puppygit.screen.content.homescreen.innerpage

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.AppItem
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.ItemListIsEmpty
import com.catpuppyapp.puppygit.compose.LoadingText
import com.catpuppyapp.puppygit.compose.PaddingRow
import com.catpuppyapp.puppygit.compose.RepoNameAndIdItem
import com.catpuppyapp.puppygit.compose.SelectedUnSelectedDialog
import com.catpuppyapp.puppygit.compose.SettingsContent
import com.catpuppyapp.puppygit.compose.SettingsTitle
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dto.AppInfo
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.settings.util.AutomationUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.ActivityUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ComposeHelper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val stateKeyTag = "AutomationInnerPage"
private const val TAG = "AutomationInnerPage"

@Composable
fun AutomationInnerPage(
    contentPadding: PaddingValues,
    needRefreshPage:MutableState<String>,
    refreshPage:()->Unit,
//    appContext:Context,
    openDrawer:()->Unit,
    exitApp:()->Unit,
    listState: LazyListState
){
    val scope = rememberCoroutineScope()
    val activityContext = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val settingsState = mutableCustomStateOf(stateKeyTag, "settingsState", SettingsUtil.getSettingsSnapshot())


    val getServiceStatus = { AutomationUtil.isAccessibilityServiceEnabled(AppModel.realAppContext) }

    val runningStatus = rememberSaveable { mutableStateOf<Boolean?>(getServiceStatus()) }

    val updateServiceStatus = {
        runningStatus.value = getServiceStatus()
    }

    val appsFilterKeyword = mutableCustomStateOf(stateKeyTag, "appsFilterKeyword", TextFieldValue(""))
    val reposFilterKeyword = mutableCustomStateOf(stateKeyTag, "reposFilterKeyword", TextFieldValue(""))


    val addedAppList = mutableCustomStateListOf(stateKeyTag, "addedAppList") { listOf<AppInfo>() }
    val notAddedAppList = mutableCustomStateListOf(stateKeyTag, "notAddedAppList") { listOf<AppInfo>() }
    val appListLoading = rememberSaveable { mutableStateOf(true) }

    val progressNotify = rememberSaveable { mutableStateOf(settingsState.value.automation.showNotifyWhenProgress) }
    val errNotify = rememberSaveable { mutableStateOf(settingsState.value.automation.showNotifyWhenErr) }
    val successNotify = rememberSaveable { mutableStateOf(settingsState.value.automation.showNotifyWhenSuccess) }

    val packageNameForSelectReposDialog = rememberSaveable { mutableStateOf("") }
    val appNameForSelectReposDialog = rememberSaveable { mutableStateOf("") }
    val selectedRepoList = mutableCustomStateListOf(stateKeyTag, "selectedRepoList") { listOf<RepoEntity>() }
    val unselectedRepoList = mutableCustomStateListOf(stateKeyTag, "unselectedRepoList") { listOf<RepoEntity>() }
    val selectedRepoListLoading = rememberSaveable { mutableStateOf(false) }
    val showSelectReposDialog = rememberSaveable { mutableStateOf(false) }
    val initSelectReposDialog = { packageName:String, appName:String ->
        packageNameForSelectReposDialog.value = packageName
        appNameForSelectReposDialog.value = appName

        doJobThenOffLoading {
            selectedRepoListLoading.value = true

            val packageNameLinkedRepos = AutomationUtil.getRepoIds(SettingsUtil.getSettingsSnapshot().automation, packageName)
            val repos = AppModel.dbContainer.repoRepository.getAll(updateRepoInfo = false)
            selectedRepoList.value.clear()
            unselectedRepoList.value.clear()
            repos.forEach {
                if(packageNameLinkedRepos.contains(it.id)) {
                    selectedRepoList.value.add(it)
                }else {
                    unselectedRepoList.value.add(it)
                }
            }

            selectedRepoListLoading.value = false
        }

        showSelectReposDialog.value = true
    }

    val filterRepos = { keyword:String, list:List<RepoEntity> ->
        list.filter { it.repoName.lowercase().contains(keyword) || it.id.lowercase().contains(keyword) }
    }

    val filterApps = { keyword:String, list:List<AppInfo> ->
        list.filter { it.appName.lowercase().contains(keyword) || it.packageName.lowercase().contains(keyword) }
    }

    if(showSelectReposDialog.value) {
        SelectedUnSelectedDialog(
            title = appNameForSelectReposDialog.value,
            loading = selectedRepoListLoading.value,
            selectedTitleText = stringResource(R.string.linked_repos),
            unselectedTitleText = stringResource(R.string.unlinked_repos),
            selectedItemList = selectedRepoList.value,
            unselectedItemList = unselectedRepoList.value,
            filterKeyWord = reposFilterKeyword,
            selectedItemFormatter={ clickedRepo ->
                RepoNameAndIdItem(
                    clickedRepo,
                    trailIcon = { iconInitModifier ->
                        IconButton(
                            modifier = iconInitModifier,

                            onClick = {
                                selectedRepoList.value.remove(clickedRepo)

                                val tmp = unselectedRepoList.value.toList()
                                //添加到未选中列表头部
                                unselectedRepoList.value.clear()
                                unselectedRepoList.value.add(clickedRepo)
                                unselectedRepoList.value.addAll(tmp)

                                //保存
                                SettingsUtil.update {
                                    it.automation.packageNameAndRepoIdsMap.let {
                                        it.put(
                                            packageNameForSelectReposDialog.value,
                                            //按仓库名排序，然后把id存上
                                            selectedRepoList.value.toList().sortedBy { it.repoName }.map { it.id }
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = stringResource(R.string.trash_bin_icon_for_delete_item)
                            )
                        }
                    }
                    ,
                    onClick = null
                )

                HorizontalDivider()
            },
            unselectedItemFormatter = { clickedRepo ->
                RepoNameAndIdItem(
                    clickedRepo,
                    trailIcon = { iconInitModifier ->
                        IconButton(
                            modifier = iconInitModifier,
                            onClick = {
                                unselectedRepoList.value.remove(clickedRepo)
                                //添加到已选中列表末尾
                                selectedRepoList.value.add(clickedRepo)

                                //保存
                                SettingsUtil.update {
                                    it.automation.packageNameAndRepoIdsMap.let {
                                        it.put(
                                            packageNameForSelectReposDialog.value,
                                            selectedRepoList.value.toList().sortedBy { it.repoName }.map { it.id }
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.add)
                            )
                        }
                    },
                    onClick = null
                )

                HorizontalDivider()
            },
            filterSelectedItemList = {keyword -> filterRepos(keyword, selectedRepoList.value)},
            filterUnselectedItemList = {keyword -> filterRepos(keyword, unselectedRepoList.value)},
            cancel = {showSelectReposDialog.value = false}
        )
    }


    //back handler block start
    val isBackHandlerEnable = rememberSaveable { mutableStateOf(true)}
    val backHandlerOnBack = ComposeHelper.getDoubleClickBackHandler(context = activityContext, openDrawer = openDrawer, exitApp= exitApp)
    //注册BackHandler，拦截返回键，实现双击返回和返回上级目录
    BackHandler(enabled = isBackHandlerEnable.value, onBack = {backHandlerOnBack()})
    //back handler block end

    val itemFontSize = 20.sp
    val itemDescFontSize = 15.sp
    val switcherIconSize = 60.dp
    val selectorWidth = MyStyleKt.DropDownMenu.minWidth.dp

    val itemLeftWidthForSwitcher = .8f
    val itemLeftWidthForSelector = .6f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
        ,

        contentPadding = contentPadding,
        state = listState,
    ) {

        item {
            SettingsContent(onClick = {
                //跳转到无障碍服务页面
                Msg.requireShowLongDuration(activityContext.getString(R.string.please_find_and_enable_disable_service_for_app))

                ActivityUtil.openAccessibilitySettings(activityContext)
            }) {
                val runningStatus = runningStatus.value
                Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                    Text(stringResource(R.string.status), fontSize = itemFontSize)
                    Text(UIHelper.getRunningStateText(activityContext, runningStatus), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, color = UIHelper.getRunningStateColor(runningStatus))
                }

                Icon(
                    modifier = Modifier.size(switcherIconSize),
                    imageVector = UIHelper.getIconForSwitcher(runningStatus == true),
                    contentDescription = UIHelper.getTextForSwitcher(activityContext, runningStatus),
                    tint = UIHelper.getColorForSwitcher(runningStatus == true),
                )
            }
        }

        item {
            SettingsContent(onClick = {
                val newValue = !progressNotify.value

                //save
                progressNotify.value = newValue
                SettingsUtil.update {
                    it.automation.showNotifyWhenProgress = newValue
                }
            }) {
                Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                    Text(stringResource(R.string.progress_notification), fontSize = itemFontSize)
                }

                Icon(
                    modifier = Modifier.size(switcherIconSize),
                    imageVector = UIHelper.getIconForSwitcher(progressNotify.value),
                    contentDescription = if(progressNotify.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                    tint = UIHelper.getColorForSwitcher(progressNotify.value),
                )
            }


        }

        item {
            SettingsContent(onClick = {
                val newValue = !successNotify.value

                //save
                successNotify.value = newValue
                SettingsUtil.update {
                    it.automation.showNotifyWhenSuccess = newValue
                }
            }) {
                Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                    Text(stringResource(R.string.success_notification), fontSize = itemFontSize)
                }

                Icon(
                    modifier = Modifier.size(switcherIconSize),
                    imageVector = UIHelper.getIconForSwitcher(successNotify.value),
                    contentDescription = if(successNotify.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                    tint = UIHelper.getColorForSwitcher(successNotify.value),
                )
            }

        }


        item {
            SettingsContent(onClick = {
                val newValue = !errNotify.value

                //save
                errNotify.value = newValue
                SettingsUtil.update {
                    it.automation.showNotifyWhenErr = newValue
                }
            }) {
                Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                    Text(stringResource(R.string.err_notification), fontSize = itemFontSize)
                }

                Icon(
                    modifier = Modifier.size(switcherIconSize),
                    imageVector = UIHelper.getIconForSwitcher(errNotify.value),
                    contentDescription = if(errNotify.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                    tint = UIHelper.getColorForSwitcher(errNotify.value),
                )
            }

        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 10.dp),

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(stringResource(R.string.app_list), fontSize = 30.sp)
                Spacer(Modifier.height(10.dp))
                Text(stringResource(R.string.will_auto_pull_push_linked_repos_when_selected_apps_enter_exit))
            }

        }

        val addItemBarHeight = 40.dp
        item {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)) {
                val keyWordIsEmpty = appsFilterKeyword.value.text.isEmpty()
                //普通的过滤，加不加清空无所谓，一按返回就清空了，但这个常驻显示，得加个清空按钮
                FilterTextField(
                    appsFilterKeyword,
                    trailingIconTooltipText = stringResource(R.string.clear),
                    trailingIcon = if(keyWordIsEmpty) null else Icons.Filled.Close,
                    trailingIconDesc = stringResource(R.string.clear),
                    trailingIconOnClick = { appsFilterKeyword.value = TextFieldValue("") }
                )
            }

        }

        if(appListLoading.value){
            item {
                LoadingText(stringResource(R.string.loading), PaddingValues(top = addItemBarHeight+30.dp), enableScroll = false)
            }
        }

        //根据关键字过滤条目
        val k = appsFilterKeyword.value.text.lowercase()  //关键字
        val enableFilter = k.isNotEmpty()
        val filteredAddedAppList = if(enableFilter){
            val tmpList = filterApps(k, addedAppList.value)
            tmpList
        }else {
            addedAppList.value
        }

        val filteredNotAddedAppList = if(enableFilter){
            val tmpList = filterApps(k, notAddedAppList.value)
            tmpList
        }else {
            notAddedAppList.value
        }


        // 旧版compose有bug，用else有可能会忽略条件，所以这里直接if判断下反条件
        if(appListLoading.value.not()) {
            item {
                SettingsTitle(stringResource(R.string.selected_str)+"("+filteredAddedAppList.size+")")
            }

            if(filteredAddedAppList.isEmpty()) {
                item {
                    ItemListIsEmpty()
                }

            }

            if(filteredAddedAppList.isNotEmpty()) {
                filteredAddedAppList.toList().forEach { appInfo ->
                    item {
                        AppItem(
                            appInfo,
                            trailIcon = { iconInitModifier ->
                                IconButton(
                                    modifier = iconInitModifier,

                                    onClick = {
                                        addedAppList.value.remove(appInfo)

                                        val tmp = notAddedAppList.value.toList()
                                        //添加到未选中列表头部
                                        notAddedAppList.value.clear()
                                        notAddedAppList.value.add(appInfo)
                                        notAddedAppList.value.addAll(tmp)

                                        //保存，从列表移除
                                        SettingsUtil.update {
                                            it.automation.packageNameAndRepoIdsMap.remove(appInfo.packageName)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DeleteOutline,
                                        contentDescription = stringResource(R.string.trash_bin_icon_for_delete_item)
                                    )
                                }
                            }

                        ) { clickedApp ->
                            initSelectReposDialog(clickedApp.packageName, clickedApp.appName)
                        }

                        HorizontalDivider()
                    }

                }
            }


            item {
                SettingsTitle(stringResource(R.string.unselected)+"("+filteredNotAddedAppList.size+")")
            }

            if(filteredNotAddedAppList.isEmpty()) {
                item {
                    ItemListIsEmpty()
                }

            }


            if(filteredNotAddedAppList.isNotEmpty()) {
                filteredNotAddedAppList.toList().forEach { appInfo ->
                    item {
                        AppItem(
                            appInfo,
                            trailIcon = { iconInitModifier ->
                                IconButton(
                                    modifier = iconInitModifier,
                                    onClick = {
                                        notAddedAppList.value.remove(appInfo)
                                        //添加到已选中列表末尾
                                        addedAppList.value.add(appInfo)

                                        //保存，添加到列表
                                        SettingsUtil.update {
                                            it.automation.packageNameAndRepoIdsMap.put(appInfo.packageName, listOf())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = stringResource(R.string.add)
                                    )
                                }
                            },
                            onClick = null
                        )

                        HorizontalDivider()
                    }
                }
            }
        }


        item {
            PaddingRow()

        }
    }


    LaunchedEffect(needRefreshPage.value) {
        val newSettings = SettingsUtil.getSettingsSnapshot()
        settingsState.value = newSettings

        doJobThenOffLoading {
            appListLoading.value = true

            // update app list
            val (userAddedAppList, userNotAddedAppList) = AutomationUtil.getSelectedAndUnSelectedAppList(activityContext, newSettings.automation)

            addedAppList.value.clear()
            addedAppList.value.addAll(userAddedAppList)
            notAddedAppList.value.clear()
            notAddedAppList.value.addAll(userNotAddedAppList)
            appListLoading.value = false
        }
    }

    LaunchedEffect(Unit) {
        //定时检查状态，不然从无障碍页面返回后状态不会更新
        scope.launch {
            while (true) {
                updateServiceStatus()
                delay(1000)
            }
        }
    }

}
