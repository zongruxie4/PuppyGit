package com.catpuppyapp.puppygit.screen.content.homescreen.innerpage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.InLineIcon
import com.catpuppyapp.puppygit.compose.PaddingRow
import com.catpuppyapp.puppygit.compose.SettingsContent
import com.catpuppyapp.puppygit.compose.SettingsTitle
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.service.http.server.HttpServer
import com.catpuppyapp.puppygit.service.http.server.HttpService
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ComposeHelper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.NetUtils
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.genHttpHostPortStr
import com.catpuppyapp.puppygit.utils.parseInt
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf

private const val stateKeyTag = "ServiceInnerPage"
private const val TAG = "ServiceInnerPage"

@Composable
fun ServiceInnerPage(
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

    val runningStatus = rememberSaveable { mutableStateOf(HttpServer.isServerRunning()) }
    val launchOnAppStartup = rememberSaveable { mutableStateOf(SettingsUtil.getSettingsSnapshot().httpService.launchOnAppStartup) }
    val launchOnSystemStartUp = rememberSaveable { mutableStateOf(HttpService.launchOnSystemStartUpEnabled(activityContext)) }
    val errNotify = rememberSaveable { mutableStateOf(SettingsUtil.getSettingsSnapshot().httpService.showNotifyWhenErr) }
    val successNotify = rememberSaveable { mutableStateOf(SettingsUtil.getSettingsSnapshot().httpService.showNotifyWhenSuccess) }

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
            val newValue = !runningStatus.value
            if(newValue) {
                HttpService.start(AppModel.realAppContext)
            }else {
                HttpService.stop(AppModel.realAppContext)
            }

            //save
            runningStatus.value = newValue
        }) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.status), fontSize = itemFontSize)
                Text(if(runningStatus.value) stringResource(R.string.running) else stringResource(R.string.stopped), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, color = if(runningStatus.value) MyStyleKt.TextColor.highlighting_green else Color.Unspecified)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(runningStatus.value),
                contentDescription = if(runningStatus.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(runningStatus.value),
            )
        }

        SettingsContent(onClick = {
            doJobThenOffLoading {
                if(NetUtils.checkPuppyGitHttpServiceRunning(genHttpHostPortStr(listenHost.value, listenPort.value))) {
                    Msg.requireShow(activityContext.getString(R.string.success))
                }else {
                    Msg.requireShow(activityContext.getString(R.string.failed))
                }
            }
        }) {
            Column {
                Text(stringResource(R.string.test), fontSize = itemFontSize)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(genHttpHostPortStr(listenHost.value, listenPort.value), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
                    InLineIcon(
                        icon = Icons.Filled.ContentCopy,
                        tooltipText = stringResource(R.string.copy),
                    ){
                        clipboardManager.setText(AnnotatedString(genHttpHostPortStr(listenHost.value, listenPort.value)))
                        Msg.requireShow(activityContext.getString(R.string.copied))
                    }
                }
            }
        }

        SettingsTitle(stringResource(R.string.settings))

        SettingsContent(onClick = {
            initSetHostDialog()
        }) {
            Column {
                Text(stringResource(R.string.host), fontSize = itemFontSize)
                Text(listenHost.value, fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
            }
        }

        SettingsContent(onClick = {
            initSetPortDialog()
        }) {
            Column {
                Text(stringResource(R.string.port), fontSize = itemFontSize)
                Text(listenPort.value, fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
            }
        }



        SettingsContent(onClick = {
            val newValue = !launchOnAppStartup.value

            //save
            launchOnAppStartup.value = newValue
            SettingsUtil.update {
                it.httpService.launchOnAppStartup = newValue
            }
        }) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.launch_on_app_startup), fontSize = itemFontSize)
//                Text(stringResource(R.string.go_to_top_bottom_buttons), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
//                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(launchOnAppStartup.value),
                contentDescription = if(launchOnAppStartup.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(launchOnAppStartup.value),
            )
        }


        SettingsContent(onClick = {
            val newValue = !launchOnSystemStartUp.value

            //save
            launchOnSystemStartUp.value = newValue
            HttpService.setLaunchOnSystemStartUp(activityContext, newValue)
        }) {
            Column(modifier = Modifier.fillMaxWidth(itemLeftWidthForSwitcher)) {
                Text(stringResource(R.string.launch_on_system_startup), fontSize = itemFontSize)
//                Text(stringResource(R.string.go_to_top_bottom_buttons), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
//                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(launchOnSystemStartUp.value),
                contentDescription = if(launchOnSystemStartUp.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(launchOnSystemStartUp.value),
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
//                Text(stringResource(R.string.go_to_top_bottom_buttons), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
//                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(errNotify.value),
                contentDescription = if(errNotify.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(errNotify.value),
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
//                Text(stringResource(R.string.go_to_top_bottom_buttons), fontSize = itemDescFontSize, fontWeight = FontWeight.Light)
//                Text(stringResource(R.string.require_restart_app), fontSize = itemDescFontSize, fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
            }

            Icon(
                modifier = Modifier.size(switcherIconSize),
                imageVector = UIHelper.getIconForSwitcher(successNotify.value),
                contentDescription = if(successNotify.value) stringResource(R.string.enable) else stringResource(R.string.disable),
                tint = UIHelper.getColorForSwitcher(successNotify.value),
            )
        }



        PaddingRow()
    }


    LaunchedEffect(needRefreshPage.value) {
        settingsState.value = SettingsUtil.getSettingsSnapshot()
        runningStatus.value = HttpServer.isServerRunning()
    }
}
