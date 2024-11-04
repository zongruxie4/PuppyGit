package com.catpuppyapp.puppygit.constants

import androidx.compose.runtime.MutableState

/**
 * 页面之间通信用的请求指令
 */
object PageRequest {
    //request值 开始

    //注意：同一条渲染链上不同组件不可使用相同的请求值，否则只有第一个匹配的组件会执行request，换句话说，再同一渲染链上请求值必须唯一且只有一个消费者


//    const val requirePrependLine = "requirePrependLine"  // request#cacheKeyOfData
//    const val requireAppendLine = "requireAppendLine"  // request#cacheKeyOfData
//    const val requireEditLine = "requireEditLine"  // request#cacheKeyOfData
//    const val requireDelLine = "requireDelLine"  // request#cacheKeyOfData
//    const val requireRestoreLine = "requireRestoreLine"  // request#cacheKeyOfData
    const val goToIndex ="goToIndex"  //goToIndex#Index，#后面是要goto的index


    const val showOther = "showOther"
    const val goParent = "goParent"
    const val showInRepos = "showInRepos"
    const val editIgnoreFile = "editIgnoreFile"
    const val goToInternalStorage = "goToInternalStorage"
    const val goToExternalStorage = "goToExternalStorage"
    const val showDetails = "showDetails"
    const val editorSwitchSelectMode = "editorSwitchSelectMode"
    const val requireSaveFontSizeAndQuitAdjust = "requireSaveFontSizeAndQuitAdjust"
    const val requireSaveLineNumFontSizeAndQuitAdjust = "requireSaveLineNumFontSizeAndQuitAdjust"
    const val showInFiles ="showInFiles"
    const val goToPath ="goToPath"
    const val copyPath="copyPath"
    const val copyRealPath="copyRealPath"
    const val cherrypickContinue ="cherrypickContinue"
    const val cherrypickAbort ="cherrypickAbort"
    const val rebaseContinue ="rebaseContinue"
    const val rebaseAbort ="rebaseAbort"
    const val rebaseSkip ="rebaseSkip"
    const val fetch ="fetch"
    const val pull ="pull"
    const val pullRebase ="pullRebase"
    const val push ="push"
    const val pushForce ="pushForce"
    const val sync ="sync"
    const val syncRebase ="syncRebase"
    const val commit ="commit"
    const val mergeAbort ="mergeAbort"
    const val mergeContinue ="mergeContinue"
    const val stageAll ="stageAll"
    const val goToTop ="goToTop"
    const val createFileOrFolder ="createFileOrFolder"
    const val goToLine ="goToLine"
    const val backFromExternalAppAskReloadFile = "backFromExternalAppAskReloadFile"  //在内置编辑器请求打开外部文件，再返回，会发出此请求，询问用户是否想重新加载文件
    const val needNotReloadFile = "needNotReloadFile"  //保存文件后，不需要加载文件，用此变量告知init函数不要重载文件
    const val requireSave = "requireSave"
    const val requireOpenAs = "requireOpenAs"  //editor的open as功能
    const val requireSearch = "requireSearch"
    const val findPrevious = "findPrevious"
    const val findNext = "findNext"
    const val showFindNextAndAllCount = "showFindNextAndAllCount"
    const val previousConflict = "previousConflict"
    const val nextConflict = "nextConflict"
    const val showNextConflictAndAllConflictsCount = "showNextConflictAndAllConflictsCount"
    const val doSaveIfNeedThenSwitchReadOnly = "doSaveIfNeedThenSwitchReadOnly"

    const val backLastEditedLine = "backLastEditedLine"  //用于 Editor页面 返回上次编辑行，以实现双击时在返回顶部和返回上次编辑行之间切换(20240507 废弃，改用 `switchBetweenFirstLineAndLastEditLine`)

    const val showOpenAsDialog = "showOpenAsDialog"
    const val switchBetweenFirstLineAndLastEditLine = "switchBetweenFirstLineAndLastEditLine"   //实现双击时在返回顶部和返回上次编辑行之间切换

    fun clearStateThenDoAct(state:MutableState<String>, act:(request:String)->Unit) {
        val request = state.value
        state.value=""
        act(request)
    }

    object DataRequest{
        //注：匹配带数据的request应用request.startsWith(request#)来判断

        //"request#data"
        const val dataSplitBy = "#"


        // maybe has data, so use startsWith check
        fun isDataRequest(actually:String, expect:String):Boolean {
            return actually.startsWith(expect)
        }

        /**
         * 返回携带了data的request
         */
        fun build(request:String, data:String):String {
            //"request#data"
            return request+dataSplitBy+data
        }
//
        // better dont payload data over 1
//        fun build(request:StringBuilder, data:String):StringBuilder {
//            //"request#data"
//            return request.append(dataSplitBy).append(data)
//        }

        /**
         * 返回request中的data
         */
        fun getDataFromRequest(request:String):String {
            val splitIndex = request.indexOf(dataSplitBy)

            if(splitIndex == -1 || splitIndex == request.lastIndex) {
                return ""
            }else {
                return request.substring(splitIndex+1)
            }
        }

    }

    // better dont payload data over 1
//    class Builder(
//        val initRequest:String,
//    ) {
//        private var requestSb: StringBuilder = StringBuilder(initRequest)
//
//        fun toRequest():String {
//            return requestSb.toString()
//        }
//
//        fun build(data:String):Builder {
//            requestSb = DataRequest.build(requestSb, data)
//            return this
//        }
//    }


}
