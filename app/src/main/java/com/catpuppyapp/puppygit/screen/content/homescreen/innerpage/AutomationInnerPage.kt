package com.catpuppyapp.puppygit.screen.content.homescreen.innerpage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.PaddingRow
import com.catpuppyapp.puppygit.compose.SettingsContent
import com.catpuppyapp.puppygit.compose.SoftkeyboardVisibleListener
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.service.HttpService
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.settings.util.AutomationSettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.ActivityUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ComposeHelper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.listToLines
import com.catpuppyapp.puppygit.utils.parseInt
import com.catpuppyapp.puppygit.utils.splitLines
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf

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
    listState:ScrollState
){

    // softkeyboard show/hidden relate start

    val view = LocalView.current
    val density = LocalDensity.current

    val isKeyboardVisible = rememberSaveable { mutableStateOf(false) }
    //indicate keyboard covered component
    val isKeyboardCoveredComponent = rememberSaveable { mutableStateOf(false) }
    // which component expect adjust heghit or padding when softkeyboard shown
    val componentHeight = rememberSaveable { mutableIntStateOf(0) }
    // the padding value when softkeyboard shown
    val keyboardPaddingDp = rememberSaveable { mutableIntStateOf(0) }

    // softkeyboard show/hidden relate end

    val activityContext = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val settingsState = mutableCustomStateOf(stateKeyTag, "settingsState", SettingsUtil.getSettingsSnapshot())


    val getServiceStatus = { AutomationSettingsUtil.isAccessibilityServiceEnabled(AppModel.realAppContext) }

    val runningStatus = rememberSaveable { mutableStateOf<Boolean?>(getServiceStatus()) }

    val updateServiceStatus = {
        runningStatus.value = getServiceStatus()
    }


    val launchOnAppStartup = rememberSaveable { mutableStateOf(settingsState.value.httpService.launchOnAppStartup) }
    val launchOnSystemStartUp = rememberSaveable { mutableStateOf(HttpService.launchOnSystemStartUpEnabled(activityContext)) }
    val progressNotify = rememberSaveable { mutableStateOf(settingsState.value.httpService.showNotifyWhenProgress) }
    val errNotify = rememberSaveable { mutableStateOf(settingsState.value.httpService.showNotifyWhenErr) }
    val successNotify = rememberSaveable { mutableStateOf(settingsState.value.httpService.showNotifyWhenSuccess) }

    val ipWhitelist = mutableCustomStateListOf(stateKeyTag, "ipWhitelist") { settingsState.value.httpService.ipWhiteList }
    val ipWhitelistBuf = rememberSaveable { mutableStateOf("") }
    val showSetIpWhiteListDialog = rememberSaveable { mutableStateOf(false) }
    val initSetIpWhitelistDialog = {
        ipWhitelistBuf.value = listToLines(ipWhitelist.value)
        showSetIpWhiteListDialog.value = true
    }

    if(showSetIpWhiteListDialog.value) {
        ConfirmDialog2(
            title = stringResource(R.string.ip_whitelist),
            requireShowTextCompose = true,
            textCompose =  {
                Column(
                    // get height for add bottom padding when showing softkeyboard
                    modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
//                                println("layoutCoordinates.size.height:${layoutCoordinates.size.height}")
                        // 获取组件的高度
                        // unit is px ( i am not very sure)
                        componentHeight.intValue = layoutCoordinates.size.height
                    }
                ) {
                    Text(stringResource(R.string.per_line_one_ip), fontWeight = FontWeight.Light)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(stringResource(R.string.if_empty_will_reject_all_requests), fontWeight = FontWeight.Light)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(stringResource(R.string.use_asterisk_to_match_all_ips), fontWeight = FontWeight.Light)
                    Spacer(modifier = Modifier.height(5.dp))
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isKeyboardCoveredComponent.value) Modifier.padding(bottom = keyboardPaddingDp.intValue.dp) else Modifier
                            ),
                        value = ipWhitelistBuf.value,
                        onValueChange = {
                            ipWhitelistBuf.value = it
                        },
                        label = {
                            Text(stringResource(R.string.list_of_ips))
                        },
                    )

                    Spacer(Modifier.height(10.dp))
                }

            },
            okBtnText = stringResource(id = R.string.save),
            cancelBtnText = stringResource(id = R.string.cancel),
            onCancel = { showSetIpWhiteListDialog.value = false }
        ) {
            showSetIpWhiteListDialog.value = false

            doJobThenOffLoading {
                val newValue = ipWhitelistBuf.value
                val newList = splitLines(newValue)
                ipWhitelist.value.clear()
                ipWhitelist.value.addAll(newList)
                SettingsUtil.update {
                    it.httpService.ipWhiteList = newList
                }

                Msg.requireShow(activityContext.getString(R.string.success))
            }
        }
    }


    val tokenList = mutableCustomStateListOf(stateKeyTag, "tokenList") { settingsState.value.httpService.tokenList }
    val tokenListBuf = rememberSaveable { mutableStateOf("") }
    val showSetTokenListDialog = rememberSaveable { mutableStateOf(false) }
    val initSetTokenListDialog = {
        tokenListBuf.value = listToLines(tokenList.value)
        showSetTokenListDialog.value = true
    }

    if(showSetTokenListDialog.value) {
        ConfirmDialog2(
            title = stringResource(R.string.tokens),
            requireShowTextCompose = true,
            textCompose =  {
                Column(
                    // get height for add bottom padding when showing softkeyboard
                    modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
//                                println("layoutCoordinates.size.height:${layoutCoordinates.size.height}")
                        // 获取组件的高度
                        // unit is px ( i am not very sure)
                        componentHeight.intValue = layoutCoordinates.size.height
                    }
                ) {
                    Text(stringResource(R.string.per_line_one_token), fontWeight = FontWeight.Light)
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(stringResource(R.string.if_empty_will_reject_all_requests), fontWeight = FontWeight.Light)
                    Spacer(modifier = Modifier.height(5.dp))
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isKeyboardCoveredComponent.value) Modifier.padding(bottom = keyboardPaddingDp.intValue.dp) else Modifier
                            ),
                        value = tokenListBuf.value,
                        onValueChange = {
                            tokenListBuf.value = it
                        },
                        label = {
                            Text(stringResource(R.string.list_of_tokens))
                        },
                    )

                    Spacer(Modifier.height(10.dp))
                }

            },
            okBtnText = stringResource(id = R.string.save),
            cancelBtnText = stringResource(id = R.string.cancel),
            onCancel = { showSetTokenListDialog.value = false }
        ) {
            showSetTokenListDialog.value = false

            doJobThenOffLoading {
                val newValue = tokenListBuf.value
                val newList = splitLines(newValue)
                tokenList.value.clear()
                tokenList.value.addAll(newList)
                SettingsUtil.update {
                    it.httpService.tokenList = newList
                }

                Msg.requireShow(activityContext.getString(R.string.success))
            }
        }
    }



    val listenHost = rememberSaveable { mutableStateOf(settingsState.value.httpService.listenHost) }
    val listenHostBuf = rememberSaveable { mutableStateOf(listenHost.value) }
    val showSetHostDialog = rememberSaveable { mutableStateOf(false) }
    val initSetHostDialog = {
        listenHostBuf.value = listenHost.value
        showSetHostDialog.value=true
    }
    if(showSetHostDialog.value) {
        ConfirmDialog2(title = stringResource(R.string.host),
            requireShowTextCompose = true,
            textCompose = {
                Column(
                    modifier= Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                    ,
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        value = listenHostBuf.value,
                        onValueChange = {
                            //直接更新，不管用户输入什么，点确定后再检查值是否有效
                            listenHostBuf.value = it
                        },
                        label = {
                            Text(stringResource(R.string.port))
                        },
//                        placeholder = {}
                    )

                }


            },
            okBtnText = stringResource(id = R.string.save),
            cancelBtnText = stringResource(id = R.string.cancel),
            onCancel = { showSetHostDialog.value = false }
        ) {
            showSetHostDialog.value = false
            doJobThenOffLoading {
                //解析
                val newValue = listenHostBuf.value

                //检查
                if(newValue.isBlank()) {
                    Msg.requireShowLongDuration(activityContext.getString(R.string.err_host_invalid))
                    return@doJobThenOffLoading
                }

                //保存
                listenHost.value = newValue
                SettingsUtil.update {
                    it.httpService.listenHost = newValue
                }

                Msg.requireShow(activityContext.getString(R.string.saved))
            }
        }
    }

    val listenPort = rememberSaveable { mutableStateOf(settingsState.value.httpService.listenPort.toString()) }
    val listenPortBuf = rememberSaveable { mutableStateOf(listenPort.value) }
    val showSetPortDialog = rememberSaveable { mutableStateOf(false) }

    val initSetPortDialog = {
        listenPortBuf.value = listenPort.value

        showSetPortDialog.value = true
    }

    if(showSetPortDialog.value) {
        ConfirmDialog2(title = stringResource(R.string.port),
            requireShowTextCompose = true,
            textCompose = {
                Column(
                    modifier= Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                    ,
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        value = listenPortBuf.value,
                        onValueChange = {
                            //直接更新，不管用户输入什么，点确定后再检查值是否有效
                            listenPortBuf.value = it
                        },
                        label = {
                            Text(stringResource(R.string.port))
                        },
//                        placeholder = {}
                    )

                }


            },
            okBtnText = stringResource(id = R.string.save),
            cancelBtnText = stringResource(id = R.string.cancel),
            onCancel = { showSetPortDialog.value = false }
        ) {
            showSetPortDialog.value = false
            doJobThenOffLoading {
                //解析
                val newValue = parseInt(listenPortBuf.value)

                //检查
                if(newValue == null || newValue < 0 || newValue > 65535) {
                    Msg.requireShowLongDuration(activityContext.getString(R.string.err_port_invalid))
                    return@doJobThenOffLoading
                }

                //保存
                listenPort.value = newValue.toString()
                SettingsUtil.update {
                    it.httpService.listenPort = newValue
                }

                Msg.requireShow(activityContext.getString(R.string.saved))
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


        SettingsContent(onClick = {
            val newValue = !progressNotify.value

            //save
            progressNotify.value = newValue
            SettingsUtil.update {
                it.httpService.showNotifyWhenProgress = newValue
            }
        }) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.progress_notification), fontSize = itemFontSize)
                Text(stringResource(R.string.require_restart_service), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(progressNotify.value),
                contentDescription = if(progressNotify.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(progressNotify.value),
            )
        }


        SettingsContent(onClick = {
            val newValue = !successNotify.value

            //save
            successNotify.value = newValue
            SettingsUtil.update {
                it.httpService.showNotifyWhenSuccess = newValue
            }
        }) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.success_notification), fontSize = itemFontSize)
                Text(stringResource(R.string.require_restart_service), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(successNotify.value),
                contentDescription = if(successNotify.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(successNotify.value),
            )
        }


        SettingsContent(onClick = {
            val newValue = !errNotify.value

            //save
            errNotify.value = newValue
            SettingsUtil.update {
                it.httpService.showNotifyWhenErr = newValue
            }
        }) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.err_notification), fontSize = itemFontSize)
                Text(stringResource(R.string.require_restart_service), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(errNotify.value),
                contentDescription = if(errNotify.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(errNotify.value),
            )
        }


        PaddingRow()
    }


    LaunchedEffect(needRefreshPage.value) {
        settingsState.value = SettingsUtil.getSettingsSnapshot()
        updateServiceStatus()
    }


    SoftkeyboardVisibleListener(
        view = view,
        isKeyboardVisible = isKeyboardVisible,
        isKeyboardCoveredComponent = isKeyboardCoveredComponent,
        componentHeight = componentHeight,
        keyboardPaddingDp = keyboardPaddingDp,
        density = density,
        skipCondition = {
            showSetTokenListDialog.value.not() && showSetIpWhiteListDialog.value.not()
        }
    )

}

