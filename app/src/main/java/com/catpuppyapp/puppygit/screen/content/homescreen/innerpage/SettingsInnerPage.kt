package com.catpuppyapp.puppygit.screen.content.homescreen.innerpage

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.CheckBoxNoteText
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.MyCheckBox
import com.catpuppyapp.puppygit.compose.PaddingRow
import com.catpuppyapp.puppygit.compose.PasswordTextFiled
import com.catpuppyapp.puppygit.compose.ScrollableColumn
import com.catpuppyapp.puppygit.compose.SingleSelectList
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ComposeHelper
import com.catpuppyapp.puppygit.utils.HashUtil
import com.catpuppyapp.puppygit.utils.LanguageUtil
import com.catpuppyapp.puppygit.utils.Lg2HomeUtils
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.PrefMan
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.fileopenhistory.FileOpenHistoryMan
import com.catpuppyapp.puppygit.utils.getStoragePermission
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.catpuppyapp.puppygit.utils.storagepaths.StoragePathsMan

private const val stateKeyTag = "SettingsInnerPage"
private const val TAG = "SettingsInnerPage"

@Composable
fun SettingsInnerPage(
    contentPadding: PaddingValues,
    needRefreshPage:MutableState<String>,
//    appContext:Context,
    openDrawer:()->Unit,
    exitApp:()->Unit,
    listState:ScrollState
){

    val activityContext = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val settingsState = mutableCustomStateOf(stateKeyTag, "settingsState", SettingsUtil.getSettingsSnapshot())

    val themeList = Theme.themeList
    val selectedTheme = rememberSaveable { mutableIntStateOf(PrefMan.getInt(activityContext, PrefMan.Key.theme, Theme.defaultThemeValue)) }

    val languageList = LanguageUtil.languageCodeList
    val selectedLanguage = rememberSaveable { mutableStateOf(LanguageUtil.getLangCode(activityContext)) }

    val logLevelList = MyLog.logLevelList
    val selectedLogLevel = rememberSaveable { mutableStateOf(MyLog.getCurrentLogLevel()) }

    val enableEditCache = rememberSaveable { mutableStateOf(settingsState.value.editor.editCacheEnable) }
    val showNaviButtons = rememberSaveable { mutableStateOf(settingsState.value.showNaviButtons) }
    val enableSnapshot_File = rememberSaveable { mutableStateOf(settingsState.value.editor.enableFileSnapshot) }
    val enableSnapshot_Content = rememberSaveable { mutableStateOf(settingsState.value.editor.enableContentSnapshot) }
    val diff_CreateSnapShotForOriginFileBeforeSave = rememberSaveable { mutableStateOf(settingsState.value.diff.createSnapShotForOriginFileBeforeSave) }

//    val groupContentByLineNum = rememberSaveable { mutableStateOf(settingsState.value.diff.groupDiffContentByLineNum) }

    val showCleanDialog = rememberSaveable { mutableStateOf(false) }
    val cleanCacheFolder = rememberSaveable { mutableStateOf(true) }
    val cleanEditCache = rememberSaveable { mutableStateOf(true) }
    val cleanSnapshot = rememberSaveable { mutableStateOf(true) }
    val cleanLog = rememberSaveable { mutableStateOf(true) }
//    val cleanContentSnapshot = rememberSaveable { mutableStateOf(true) }
    val cleanStoragePath = rememberSaveable { mutableStateOf(false) }
    val cleanFileOpenHistory = rememberSaveable { mutableStateOf(false) }

    val allowUnknownHosts = rememberSaveable { mutableStateOf(settingsState.value.sshSetting.allowUnknownHosts) }
//    val showResetKnownHostsDialog = rememberSaveable { mutableStateOf(false) }
    val showForgetHostKeysDialog = rememberSaveable { mutableStateOf(false) }
    if(showForgetHostKeysDialog.value) {
        ConfirmDialog2(
            title = stringResource(R.string.confirm),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    Text(stringResource(R.string.after_forgetting_the_host_keys_may_ask_confirm_again))
                }
            },
            okBtnText = stringResource(R.string.forget),
            okTextColor = MyStyleKt.TextColor.danger(),
            onCancel = {showForgetHostKeysDialog.value = false}
        ) {
            showForgetHostKeysDialog.value = false
            doJobThenOffLoading {
                try {
                    Lg2HomeUtils.resetUserKnownHostFile()
                    Msg.requireShow(activityContext.getString(R.string.success))
                }catch (e:Exception) {
                    Msg.requireShowLongDuration(e.localizedMessage ?:"err")
                    MyLog.e(TAG, "ForgetHostKeysDialog err: ${e.stackTraceToString()}")
                }
            }
        }
    }

//    val updateMasterPassFailedList = mutableCustomStateOf(stateKeyTag, "updateMasterPassFailedList", listOf<String>())
    val updateMasterPassFailedListStr = rememberSaveable { mutableStateOf("") }
    val showFailedUpdateMasterPasswordsCredentialList = rememberSaveable { mutableStateOf(false) }
    if(showFailedUpdateMasterPasswordsCredentialList.value) {
        CopyableDialog(
            title = stringResource(R.string.warn),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    Text(stringResource(R.string.below_credential_password_update_failed))
                    Spacer(Modifier.height(10.dp))
                    Text(updateMasterPassFailedListStr.value)
                }
            },
            onCancel = { showFailedUpdateMasterPasswordsCredentialList.value = false }
        ) {
            showFailedUpdateMasterPasswordsCredentialList.value = false

            doJobThenOffLoading {
                clipboardManager.setText(AnnotatedString(updateMasterPassFailedListStr.value))
                Msg.requireShow(activityContext.getString(R.string.copied))
            }
        }
    }

    val oldMasterPassword = rememberSaveable { mutableStateOf("") }
    val newMasterPassword = rememberSaveable { mutableStateOf("") }
    val oldMasterPasswordErrMsg = rememberSaveable { mutableStateOf("") }
    val newMasterPasswordErrMsg = rememberSaveable { mutableStateOf("") }
    val oldMasterPasswordVisible = rememberSaveable { mutableStateOf(false) }
    val newMasterPasswordVisible = rememberSaveable { mutableStateOf(false) }

    val masterPassEnabled = rememberSaveable { mutableStateOf(AppModel.singleInstanceHolder.masterPasswordEnabled()) }
    val masterPassStatus = rememberSaveable { mutableStateOf(if(masterPassEnabled.value) activityContext.getString(R.string.enabled) else activityContext.getString(R.string.disabled)) }

    val showSetMasterPasswordDialog = rememberSaveable { mutableStateOf(false) }
    if (showSetMasterPasswordDialog.value)  {
        val requireOldPass = AppModel.singleInstanceHolder.requireMasterPassword()
        ConfirmDialog2(
            title = stringResource(R.string.set_master_password),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    // require old master password if exist
                    if(requireOldPass) {
                        PasswordTextFiled(oldMasterPassword, oldMasterPasswordVisible, stringResource(R.string.old_password), errMsg = oldMasterPasswordErrMsg)
                    }

                    Spacer(Modifier.height(15.dp))

                    // new password
                    PasswordTextFiled(newMasterPassword, newMasterPasswordVisible, stringResource(R.string.new_password), errMsg = newMasterPasswordErrMsg, paddingValues = PaddingValues(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 2.dp))
                    Row(modifier = Modifier.padding(horizontal = 10.dp)) {
                        Text(stringResource(R.string.leave_new_password_empty_if_dont_want_to_use_master_password), color = MyStyleKt.TextColor.highlighting_green)
                    }

                    if(newMasterPassword.value.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.padding(horizontal = 10.dp)) {
                            Text(stringResource(R.string.please_make_sure_you_can_remember_your_master_password), color = MyStyleKt.TextColor.danger())
                        }
                    }
                }
            },
            okBtnText = stringResource(R.string.save),
            onCancel = {showSetMasterPasswordDialog.value = false}
        ) {
            doJobThenOffLoading job@{
                try {
                    //一般来说不会需要用户在这输入密码的，在启动app的时候就输过了
                    val oldPass  = if(requireOldPass) {
                        if(oldMasterPassword.value.isEmpty()){
                            oldMasterPasswordErrMsg.value = activityContext.getString(R.string.require_old_password)
                            return@job
                        }

                        if(HashUtil.verify(oldMasterPassword.value, settingsState.value.masterPasswordHash).not()) {
                            oldMasterPasswordErrMsg.value = activityContext.getString(R.string.wrong_password)
                            return@job
                        }

                        oldMasterPassword.value
                    }else {
                        AppModel.singleInstanceHolder.masterPassword.value
                    }

                    //验证旧密码成功就可关闭弹窗了
                    showSetMasterPasswordDialog.value = false

                    val updatingStr = activityContext.getString(R.string.updating)
                    Msg.requireShow(updatingStr)

                    //新密码可以为空，不验证，若空等于清除凭据
                    val newPass = newMasterPassword.value
                    //密码一样，啥也不用干
                    if(newPass == oldPass) {
                        Msg.requireShow(activityContext.getString(R.string.old_and_new_passwords_are_the_same))
                        return@job
                    }

                    masterPassStatus.value = updatingStr


                    val credentialDb = AppModel.singleInstanceHolder.dbContainer.credentialRepository
                    val failedList = credentialDb.updateMasterPassword(oldPass, newPass)

                    // update master password hash
                    SettingsUtil.update {
                        if(newPass.isEmpty()) { // 空字符串不必算hash
                            it.masterPasswordHash = newPass
                        }else {  // 非空字符串，计算hash
                            it.masterPasswordHash = HashUtil.hash(newPass)
                        }
                    }
                    // update in-memory master password
                    AppModel.singleInstanceHolder.masterPassword.value = newPass

                    if(failedList.isEmpty()) { //全部解密然后加密成功
                        Msg.requireShow(activityContext.getString(R.string.success))
                    }else {  //有解密失败的
                        val suffix = ", "
                        val sb = StringBuilder()
                        for (i in failedList) {
                            sb.append(i).append(suffix)
                        }

                        //显示弹窗，让用户知道哪些解密失败
                        updateMasterPassFailedListStr.value = sb.removeSuffix(suffix).toString()
                        showFailedUpdateMasterPasswordsCredentialList.value = true
                    }

                    //只要执行到这，不管是否有解密失败的凭据，主密码都已经更新了

                    //更新下主密码状态，让用户知道更新成功了
                    masterPassEnabled.value = AppModel.singleInstanceHolder.masterPasswordEnabled()
                    masterPassStatus.value = if(masterPassEnabled.value) activityContext.getString(R.string.updated) else activityContext.getString(R.string.disabled)

                }catch (e:Exception) {
                    Msg.requireShowLongDuration(e.localizedMessage ?:"err")
                    MyLog.e(TAG, "SetMasterPasswordDialog err: ${e.stackTraceToString()}")
                }
            }
        }
    }


//    val showForgetMasterPasswordDialog = rememberSaveable { mutableStateOf(false) }




//    if(showResetKnownHostsDialog.value) {
//        ConfirmDialog2(
//            title = stringResource(R.string.confirm),
//            requireShowTextCompose = true,
//            textCompose = {
//                ScrollableColumn {
//                    Text(stringResource(R.string.will_reset_the_unknown_hosts_file))
//                }
//            },
//            okBtnText = stringResource(R.string.reset),
//            okTextColor = MyStyleKt.TextColor.danger(),
//            onCancel = {showResetKnownHostsDialog.value = false}
//        ) {
//            showResetKnownHostsDialog.value = false
//            doJobThenOffLoading {
//                try {
//                    Lg2HomeUtils.resetKnownHostFile(appContext)
//                    Msg.requireShow(appContext.getString(R.string.success))
//                }catch (e:Exception) {
//                    Msg.requireShowLongDuration(e.localizedMessage ?:"err")
//                    MyLog.e(TAG, "ResetKnownHostsDialog err: ${e.stackTraceToString()}")
//                }
//            }
//        }
//    }

    if(showCleanDialog.value) {
        ConfirmDialog2(
            title = stringResource(R.string.clean),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    MyCheckBox(stringResource(R.string.log), cleanLog)
                    MyCheckBox(stringResource(R.string.cache), cleanCacheFolder)
                    MyCheckBox(stringResource(R.string.edit_cache), cleanEditCache)
                    MyCheckBox(stringResource(R.string.all_snapshots), cleanSnapshot)  // file snapshots and content snapshots all will be delete
                    MyCheckBox(stringResource(R.string.storage_paths), cleanStoragePath)
                    if(cleanStoragePath.value) {
                        CheckBoxNoteText(stringResource(R.string.the_storage_path_are_the_paths_you_chosen_and_added_when_cloning_repo))
                    }
                    MyCheckBox(stringResource(R.string.file_opened_history), cleanFileOpenHistory)
                    if(cleanFileOpenHistory.value) {
                        CheckBoxNoteText(stringResource(R.string.this_include_editor_opened_files_history_and_their_last_edited_position))
                    }
                }
            },
            onCancel = {showCleanDialog.value = false}
        ) {
            showCleanDialog.value=false

            doJobThenOffLoading {
                Msg.requireShow(activityContext.getString(R.string.cleaning))

                if(cleanLog.value) {
                    try {
                        AppModel.singleInstanceHolder.getOrCreateLogDir().deleteRecursively()
                        AppModel.singleInstanceHolder.getOrCreateLogDir()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean log err: ${e.localizedMessage}")
                    }
                }

                if(cleanCacheFolder.value) {
                    try {
                        AppModel.singleInstanceHolder.externalCacheDir.deleteRecursively()
                        AppModel.singleInstanceHolder.externalCacheDir.mkdirs()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean cache folder err: ${e.localizedMessage}")
                    }
                }

                if(cleanEditCache.value) {
                    try {
                        AppModel.singleInstanceHolder.getOrCreateEditCacheDir().deleteRecursively()
                        AppModel.singleInstanceHolder.getOrCreateEditCacheDir()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean edit cache err: ${e.localizedMessage}")
                    }
                }

                if(cleanSnapshot.value) {
                    try {
                        AppModel.singleInstanceHolder.getOrCreateFileSnapshotDir().deleteRecursively()
                        AppModel.singleInstanceHolder.getOrCreateFileSnapshotDir()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean file and content snapshot err: ${e.localizedMessage}")
                    }
                }


                if(cleanStoragePath.value) {
                    try {
                        StoragePathsMan.reset()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean storage paths err: ${e.localizedMessage}")
                    }
                }

                if(cleanFileOpenHistory.value) {
                    try {
                        FileOpenHistoryMan.reset()
                    }catch (e:Exception) {
                        MyLog.e(TAG, "clean file opened history err: ${e.localizedMessage}")
                    }
                }

                Msg.requireShow(activityContext.getString(R.string.success))
            }
        }
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

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .verticalScroll(listState)
    ) {
        SettingsTitle(stringResource(R.string.general))

        SettingsContent {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSelector)) {
                Text(stringResource(R.string.theme), fontSize = itemFontSize)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Column(modifier = Modifier.width(selectorWidth)) {
                SingleSelectList(
                    optionsList = themeList,
                    selectedOptionIndex = null,
                    selectedOptionValue = selectedTheme.intValue,
                    menuItemSelected = {_, value -> value == selectedTheme.intValue },
                    menuItemFormatter = {_, value -> Theme.getThemeTextByCode(value, activityContext)},
                    menuItemOnClick = { _, value ->
                        selectedTheme.intValue = value

                        if(value != PrefMan.getInt(activityContext, PrefMan.Key.theme, Theme.defaultThemeValue)) {
                            PrefMan.set(activityContext, PrefMan.Key.theme, ""+value)
                        }
                    }
                )
            }
        }


        SettingsContent {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSelector)) {
                Text(stringResource(R.string.language), fontSize = itemFontSize)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Column(modifier = Modifier.width(selectorWidth)) {
                SingleSelectList(
                    optionsList = languageList,
                    selectedOptionIndex = null,
                    selectedOptionValue = selectedLanguage.value,
                    menuItemOnClick = { index, value ->
                        selectedLanguage.value = value

                        if(value != LanguageUtil.getLangCode(activityContext)) {
                            LanguageUtil.setLangCode(activityContext, value)
                        }
                    },
                    menuItemSelected = {index, value ->
                        value == selectedLanguage.value
                    },
                    menuItemFormatter = { index, value ->
                        LanguageUtil.getLanguageTextByCode(value?:"", activityContext)
                    }
                )
            }
        }
        SettingsContent {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSelector)) {
                Text(stringResource(R.string.log_level), fontSize = itemFontSize)
//                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Column(modifier = Modifier.width(selectorWidth)) {
                SingleSelectList(
                    optionsList = logLevelList,
                    selectedOptionIndex = null,
                    selectedOptionValue = selectedLogLevel.value,
                    menuItemOnClick = { index, value ->
                        selectedLogLevel.value = value

                        if(value != MyLog.getCurrentLogLevel()) {
                            // set in memory
                            MyLog.setLogLevel(value.get(0))

                            // save to disk
                            PrefMan.set(activityContext, PrefMan.Key.logLevel, value)
                        }
                    },
                    menuItemSelected = {index, value ->
                        value == selectedLogLevel.value
                    },
                    menuItemFormatter = { index, value ->
                        MyLog.getTextByLogLevel(value?:"", activityContext)
                    }
                )
            }
        }


        SettingsContent(onClick = {
            val newValue = !showNaviButtons.value

            //save
            showNaviButtons.value = newValue
            SettingsUtil.update {
                it.showNaviButtons = newValue
            }
        }) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.show_navi_buttons), fontSize = itemFontSize)
                Text(stringResource(R.string.go_to_top_bottom_buttons), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
//                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(showNaviButtons.value),
                contentDescription = if(showNaviButtons.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(showNaviButtons.value),
            )
        }

        SettingsContent(onClick = {
            showCleanDialog.value = true
        }) {
            Text(stringResource(R.string.clean), fontSize = itemFontSize)
        }


        SettingsTitle(stringResource(R.string.editor))

        SettingsContent(onClick = {
            val newValue = !enableEditCache.value

            //save
            enableEditCache.value = newValue
            SettingsUtil.update {
                it.editor.editCacheEnable = newValue
            }
        }) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.edit_cache), fontSize = itemFontSize)
                Text(replaceStringResList(stringResource(R.string.cache_your_input_into_editcache_dir_path), listOf("${Cons.defalutPuppyGitDataUnderAllReposDirName}/${Cons.defaultEditCacheDirName}")), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(enableEditCache.value),
                contentDescription = if(enableEditCache.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(enableEditCache.value),
            )
        }

        SettingsContent(
            onClick = {
                val newValue = !enableSnapshot_File.value
                enableSnapshot_File.value = newValue
                SettingsUtil.update {
                    it.editor.enableFileSnapshot = newValue
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.file_snapshot), fontSize = itemFontSize)
                Text(stringResource(R.string.file_snapshot_desc), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)

            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(enableSnapshot_File.value),
                contentDescription = if(enableSnapshot_File.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(enableSnapshot_File.value),

            )
        }
        SettingsContent(
            onClick = {
                val newValue = !enableSnapshot_Content.value
                enableSnapshot_Content.value = newValue
                SettingsUtil.update {
                    it.editor.enableContentSnapshot = newValue
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.content_snapshot), fontSize = itemFontSize)
                Text(stringResource(R.string.content_snapshot_desc), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)

            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(enableSnapshot_Content.value),
                contentDescription = if(enableSnapshot_Content.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(enableSnapshot_Content.value),

            )
        }

        // diff settings block start
//
        SettingsTitle(stringResource(R.string.diff))

        SettingsContent(
            onClick = {
                val newValue = !diff_CreateSnapShotForOriginFileBeforeSave.value
                diff_CreateSnapShotForOriginFileBeforeSave.value = newValue
                SettingsUtil.update {
                    it.diff.createSnapShotForOriginFileBeforeSave = newValue
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.file_snapshot), fontSize = itemFontSize)
                Text(stringResource(R.string.file_snapshot_desc), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)

            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(diff_CreateSnapShotForOriginFileBeforeSave.value),
                contentDescription = if(diff_CreateSnapShotForOriginFileBeforeSave.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(diff_CreateSnapShotForOriginFileBeforeSave.value),

            )
        }

//
//        SettingsContent(onClick = {
//            val newValue = !groupContentByLineNum.value
//
//            groupContentByLineNum.value = newValue
//            SettingsUtil.update {
//                it.diff.groupDiffContentByLineNum = newValue
//            }
//        }) {
//            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
//                Text(stringResource(R.string.group_content_by_line_num), fontSize = itemFontSize)
////                Text(stringResource(R.string.before_saving_a_file_create_a_snapshot_first), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
////                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
//
//            }
//
//            Icon(
//                modifier = Modifier.size(switcherIconSize),
//                imageVector = UIHelper.getIconForSwitcher(groupContentByLineNum),
//                contentDescription = if(groupContentByLineNum.value) stringResource(R.string.enable) else stringResource(R.string.disable),
//                tint = UIHelper.getColorForSwitcher(groupContentByLineNum),
//
//            )
//        }
        // diff settings block end

//        SettingsTitle(stringResource(R.string.clean))


        SettingsTitle(stringResource(R.string.ssh))

        SettingsContent(
            onClick = {
                val newValue = !allowUnknownHosts.value
                allowUnknownHosts.value = newValue
                SettingsUtil.update {
                    it.sshSetting.allowUnknownHosts = newValue
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.allow_unknown_hosts), fontSize = itemFontSize)
//                Text("If enable, can connect host not in the 'known_hosts' file, else will reject, if you want to more safe, add trusted hosts info into the 'known_hosts' and disable this feature", fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
                Text(stringResource(R.string.if_enable_will_allow_unknown_hosts_as_default_else_will_ask), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)

            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(allowUnknownHosts.value),
                contentDescription = if(allowUnknownHosts.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(allowUnknownHosts.value),

                )
        }
//
//        SettingsContent(onClick = {
//            openFileWithInnerSubPageEditor(
//                filePath = Lg2Utils.getKnownHostsFile(appContext).canonicalPath,
//                mergeMode = false,
//                readOnly = false
//            )
//        }) {
//            Column {
//                Text(stringResource(R.string.edit_known_hosts_file), fontSize = itemFontSize)
//            }
//        }
//        SettingsContent(onClick = {
//            showResetKnownHostsDialog.value =true
//        }) {
//            Column {
//                Text(stringResource(R.string.reset_known_hosts), fontSize = itemFontSize)
//            }
//        }
        SettingsContent(onClick = {
            showForgetHostKeysDialog.value =true
        }) {
            Column {
                Text(stringResource(R.string.forget_hostkeys), fontSize = itemFontSize)
            }
        }

        SettingsTitle(stringResource(R.string.master_password))

        SettingsContent(onClick = {
            //初始化变量
            oldMasterPassword.value = ""
            newMasterPassword.value = ""
            oldMasterPasswordErrMsg.value = ""
            newMasterPasswordErrMsg.value = ""
            oldMasterPasswordVisible.value = false
            newMasterPasswordVisible.value = false

            //显示弹窗
            showSetMasterPasswordDialog.value = true
        }) {
            Column {
                Text(stringResource(R.string.set_master_password), fontSize = itemFontSize)
                Text(masterPassStatus.value, fontSize = itemDescFontSize, fontWeight = FontWeight.Light, color = if(masterPassEnabled.value) MyStyleKt.TextColor.highlighting_green else MyStyleKt.TextColor.danger())
                Text(stringResource(R.string.if_set_will_require_master_password_when_launching_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)

            }
        }

//        SettingsContent(onClick = {
//            oldMasterPassword.value = ""
//            newMasterPassword.value = ""
//            showForgetMasterPasswordDialog.value = true
//        }) {
//            Column {
//                Text(stringResource(R.string.i_forgot_my_master_password), fontSize = itemFontSize)
//            }
//        }


        SettingsTitle(stringResource(R.string.permissions))
        SettingsContent(onClick = {
            // grant permission for read/write external storage
            val activity = activityContext as? Activity
            if (activity == null) {
                Msg.requireShowLongDuration(activityContext.getString(R.string.please_go_to_system_settings_allow_manage_storage))
            }else {
                activity.getStoragePermission()
            }
        }) {
            Column {
                Text(stringResource(R.string.manage_storage), fontSize = itemFontSize)
                Text(stringResource(R.string.if_you_want_to_clone_repo_into_external_storage_this_permission_is_required), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
            }
        }

        PaddingRow()
    }


    LaunchedEffect(needRefreshPage) {
        settingsState.value = SettingsUtil.getSettingsSnapshot()

    }
}


@Composable
private fun SettingsTitle(text:String){
    val inDarkTheme = Theme.inDarkTheme
    Row(modifier = Modifier
        .background(color = if (inDarkTheme) Color.DarkGray else Color.LightGray)
        .fillMaxWidth()
        .padding(start = 5.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
    }
}

@Composable
private fun SettingsContent(onClick:(()->Unit)?=null, content:@Composable ()->Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(10.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        content()
    }
    HorizontalDivider()
}
