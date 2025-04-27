package com.catpuppyapp.puppygit.screen.shared

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.dto.UndoStack
import com.catpuppyapp.puppygit.git.FileHistoryDto
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver


private const val TAG = "SharedState"

object SharedState {
    val homeScreenNeedRefresh = mutableStateOf("")


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

    val treeToTreeChangeList_title = mutableStateOf("")

    val commitList_fullOid = mutableStateOf("")
    val commitList_shortBranchName = mutableStateOf("")

    val diffScreen_relativePathUnderRepo = mutableStateOf("")
    //这个列表其实应该按页面区分，否则有可能冲突，不过问题不大并不会导致向前导航的时候出错，只是返回的时候会显示错误，改起来有点恶心，不想改
    val diffScreen_diffableItemList_of_Worktree = mutableStateListOf<StatusTypeEntrySaver>()
    val diffScreen_diffableItemList_of_Index = mutableStateListOf<StatusTypeEntrySaver>()
    val diffScreen_diffableItemList_of_TreeToTree = mutableStateListOf<StatusTypeEntrySaver>()
    val diffScreen_diffableItemList_of_FileHistory = mutableStateListOf<FileHistoryDto>()

    fun setDiffableListByFromTo(
        diffableListOfChangeList:List<StatusTypeEntrySaver>?,
        fromTo: String
    ) {
        if(diffableListOfChangeList != null) {
            if(fromTo == Cons.gitDiffFromIndexToWorktree) {
                SharedState.diffScreen_diffableItemList_of_Worktree.let {
                    it.clear()
                    it.addAll(diffableListOfChangeList)
                }
            }else if(fromTo == Cons.gitDiffFromHeadToIndex) {
                SharedState.diffScreen_diffableItemList_of_Index.let {
                    it.clear()
                    it.addAll(diffableListOfChangeList)
                }
            }else if(fromTo == Cons.gitDiffFromTreeToTree) {
                SharedState.diffScreen_diffableItemList_of_TreeToTree.let {
                    it.clear()
                    it.addAll(diffableListOfChangeList)
                }
            }
        }
    }

    fun getDiffableListByFromTo(fromTo: String):List<StatusTypeEntrySaver> {
        return if(fromTo == Cons.gitDiffFromIndexToWorktree) {
            SharedState.diffScreen_diffableItemList_of_Worktree
        }else if(fromTo == Cons.gitDiffFromHeadToIndex) {
            SharedState.diffScreen_diffableItemList_of_Index
        }else if(fromTo == Cons.gitDiffFromTreeToTree) {
            SharedState.diffScreen_diffableItemList_of_TreeToTree
        }else {
            listOf()
        }
    }

    val fileHistory_fileRelativePath = mutableStateOf("")
    val subPageEditor_filePath = mutableStateOf("")

}
