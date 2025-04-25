package com.catpuppyapp.puppygit.screen.shared

import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.dto.UndoStack
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage


private const val TAG = "SharedState"

object SharedState {
    val gotNewIntent = mutableStateOf("")
    val homeScreenNeedRefresh = mutableStateOf("")
    val intentConsumed = mutableStateOf(false)
    val intent = mutableStateOf<Intent?>(null)

    // indexToWorktree or headToIndex页面的列表，无论从上述哪个页面跳转到diff页面，都会把那个页面的列表地址存到这个变量中。(所以，导航链不能是 indexToWorktree ChangeList->Diff->headToIndex ChangeList->Diff，不过正常来说，并没从Diff前进到Index的路径，所以无此bug)
    var homeChangeList_itemList = mutableListOf<StatusTypeEntrySaver>()

    //用于在indexToWorkTree那个页面指示index是否有条目
    var homeChangeList_indexHasItem = mutableStateOf(false)

    val homeChangeList_LastClickedItemKey = mutableStateOf(Cons.init_last_clicked_item_key)
    val index_LastClickedItemKey = mutableStateOf(Cons.init_last_clicked_item_key)
    val treeToTree_LastClickedItemKey = mutableStateOf(Cons.init_last_clicked_item_key)
    val fileHistory_LastClickedItemKey = mutableStateOf(Cons.init_last_clicked_item_key)

    val homeChangeList_Refresh = mutableStateOf("IndexToWorkTree_ChangeList_refresh_init_value_5hpn")
    val indexChangeList_Refresh = mutableStateOf("HeadToIndex_ChangeList_refresh_init_value_ts7n")

    val editorPreviewNavStack = EditorPreviewNavStack("")
    val subEditorPreviewNavStack = EditorPreviewNavStack("")

    val editorUndoStack = mutableStateOf(UndoStack(""))
    val subEditorUndoStack = mutableStateOf(UndoStack(""))

    //用来在请求选择和执行选择的FileChooser页面之间共享选中的路径
    val fileChooser_DirPath = mutableStateOf("")  //如果用app内置 File Picker 选目录，用这个做state
    val fileChooser_FilePath = mutableStateOf("")  //如果用app内置 File Picker 选文件，用这个做state





    fun setNewIntent(newIntent: Intent?) {
        intent.value = newIntent
        //刷新，触发请求处理intent
        changeStateTriggerRefreshPage(gotNewIntent)
    }

    //检查intent，若有效，请求HomeScreen处理
    fun requireHandleNewIntent() {
        val intent = intent.value ?: return;

        //data 或 extras是必须条件，若两者任有其一，则此intent需要消费
        if(intent.extras != null || intent.data != null) {
            MyLog.d(TAG, "will navigate to HomeScreen to handle new Intent")

            SharedState.intentConsumed.value = false
            changeStateTriggerRefreshPage(SharedState.homeScreenNeedRefresh)

            //弹出栈直到顶级页面(HomeScreen)
            AppModel.navController.let {
                // false表示不弹出目标路径，目标路径是起始路径，若弹出会出问题，导航栈会错乱，所以传false
                it.popBackStack(it.graph.startDestinationId, inclusive = false)
            }

            //导航到home再重启activity，不好，废弃
//            AppModel.navController.navigate(Cons.nav_HomeScreen)
            //用新intent重启Activity
//            ActivityUtil.restartActivityByIntent(this, intent)
        }
    }

}
