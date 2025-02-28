package com.catpuppyapp.puppygit.utils

import android.content.Context
import android.util.DisplayMetrics
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.dto.DeviceWidthHeight
import com.catpuppyapp.puppygit.fileeditor.texteditor.view.ExpectConflictStrDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.github.git24j.core.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "UIHelper"

object UIHelper {

    object Size {
        @Composable
        fun height(): Int {
            val configuration = LocalConfiguration.current
            return configuration.screenHeightDp
        }
        @Composable
        fun width(): Int {
            val configuration = LocalConfiguration.current
            return configuration.screenWidthDp
        }
        @Composable
        fun heightDp(): Dp {
            return height().dp
        }
        @Composable
        fun widthDp():Dp {
            return width().dp
        }

        //编辑器的虚拟空间，用来把最后一行顶上去的，返回一个Pair，值1是宽，值2是高
        @Composable
        fun editorVirtualSpace():Pair<Dp, Dp> {
            //注：高度如果减的值太小，TopBar固定时，内容会被TopBar盖住，经我测试减100无论隐藏还是显示TopBar都能正常显示内容
            return Pair(widthDp(), (height()-100).dp)
        }

        /**
         * 获取键盘高度。
         * 注：(?) 需要在Activity#onCreate()执行 `WindowCompat.setDecorFitsSystemWindows(window, false)` 才能获取到有效高度，
         * 否则只能获取到0，但我没充分验证。
         */
        @Composable
        fun getImeHeightInDp():Dp {
            val imeHeightInDp = with(LocalDensity.current) { WindowInsets.ime.getBottom(this).toDp() }
            return imeHeightInDp
        }
    }

    fun getFontColor(inDarkTheme:Boolean=Theme.inDarkTheme): Color {
        return if(inDarkTheme) MyStyleKt.TextColor.darkThemeFontColor else MyStyleKt.TextColor.fontColor
    }

    fun getDisableBtnColor(inDarkTheme: Boolean=Theme.inDarkTheme):Color {
        return if(inDarkTheme) MyStyleKt.IconColor.disable_DarkTheme else MyStyleKt.IconColor.disable
    }

    fun getDisableTextColor(inDarkTheme: Boolean=Theme.inDarkTheme):Color {
        return if(inDarkTheme) MyStyleKt.TextColor.disable_DarkTheme else MyStyleKt.TextColor.disable
    }

    //不太重要的字体颜色
    fun getSecondaryFontColor(inDarkTheme:Boolean=Theme.inDarkTheme): Color {
        return if(inDarkTheme) MyStyleKt.TextColor.darkThemeSecondaryFontColor else MyStyleKt.TextColor.secondaryFontColor
    }

    /**
     * enable 为true返回启用颜色，否则返回null
     */
    @Composable
    fun getIconEnableColorOrNull(enable:Boolean):Color? {
        if(!enable) return null

        //darkTheme用反转颜色；否则用主颜色
        val color = if(Theme.inDarkTheme) MaterialTheme.colorScheme.inversePrimary else MaterialTheme.colorScheme.primary
//        val enableColor = Color(red=(color.red+0.3f).coerceAtMost(1f), green = (color.green+0.3f).coerceAtMost(1f), blue = (color.blue+0.3f).coerceAtMost(1f), alpha = 1f)
        //如果是darkTheme，提升整体亮度；否则只增加蓝色
        val enableColor = if(Theme.inDarkTheme) Color(red=(color.red+0.3f).coerceAtMost(1f), green = (color.green+0.3f).coerceAtMost(1f), blue = (color.blue+0.3f).coerceAtMost(1f), alpha = 1f) else Color(red=color.red, green = color.green, blue = (color.blue+0.5f).coerceAtMost(1f), alpha = 1f)
        return enableColor
    }

    //长按选中连续条目
    fun<T> doSelectSpan(itemIdxOfItemList:Int, item: T, selectedItems:List<T>, itemList:List<T>, switchItemSelected:(T)->Unit, selectIfNotInSelectedListElseNoop:(T)->Unit) {
        //如果 已选条目列表为空 或 索引无效，选中条目，然后返回
        if(selectedItems.isEmpty() || itemIdxOfItemList<0 || itemIdxOfItemList>itemList.lastIndex) {
            switchItemSelected(item)
            return
        }

        //如果不为空，执行连续选中

        //取出最后一个选择的条目
        val lastSelectedItem = selectedItems.last()

        //在源list中查找最后一个条目的位置（索引）
        val lastSelectedItemIndexOfItemList = itemList.indexOf(lastSelectedItem)

        //itemList查无选中列表的最后一个元素，发生这种情况的场景举例：完整列表，选中条目abc，过滤列表不包含abc，长按选择，过滤列表被传入此函数的itemList，这时，itemList就会查无abc，indexOf返回-1
        if(lastSelectedItemIndexOfItemList == -1) {
            switchItemSelected(item)
            return
        }

        //如果长按的条目就是之前选中的条目，什么都不做（选中一个条目，然后长按它即可触发此条件）
        if(lastSelectedItemIndexOfItemList == itemIdxOfItemList) {
            return
        }

        //min()
        val startIndex = Math.min(lastSelectedItemIndexOfItemList, itemIdxOfItemList)
        //max()
        val endIndexExclusive = Math.max(lastSelectedItemIndexOfItemList, itemIdxOfItemList) + 1

        //检查索引是否有效
        if(startIndex >= endIndexExclusive
            || startIndex<0 || startIndex>itemList.lastIndex
            || endIndexExclusive<0 || endIndexExclusive>itemList.size
        ) {
            return
        }

        //选中范围内的条目 左闭右开 [startIndex, endIndexExclusive)
        //list.forEach(selectIfNotInSelectedListElseNoop) 等于 list.forEach{selectIfNotInSelectedListElseNoop(it)}
//        itemList.subList(startIndex, endIndexExclusive).forEach {selectIfNotInSelectedListElseNoop(it)}  //需要拷贝列表，bad
        for(i in startIndex..<endIndexExclusive) {
            selectIfNotInSelectedListElseNoop(itemList[i])  //不需要拷贝列表，good
        }
    }

    //如果没在已选中列表，执行选中，否则什么都不做
    fun<T> selectIfNotInSelectedListElseNoop(
        item: T,
        selectedItems:MutableList<T>,
        contains:(srcList:List<T>, T)->Boolean = {list, i -> list.contains(i)}
    ) {
        if(!contains(selectedItems, item)) {
            selectedItems.add(item)
        }
    }

    fun<T> selectIfNotInSelectedListElseRemove(
        item:T,
        selectedItems:MutableList<T>,
        contains:(srcList:List<T>, curItem:T)->Boolean = {srcList, curItem -> srcList.contains(curItem)},
        remove:(srcList:MutableList<T>, curItem:T) -> Boolean = {srcList, curItem -> srcList.remove(curItem)}
    ) {
        if(contains(selectedItems, item)) {
            remove(selectedItems, item)
        }else{
            selectedItems.add(item)
        }
    }

    fun scrollToItem(coroutineScope: CoroutineScope, listState: LazyListState, index:Int)  {
        coroutineScope.launch { listState.scrollToItem(Math.max(0, index)) }
    }

    fun scrollTo(coroutineScope: CoroutineScope, listState: ScrollState, index:Int)  {
        coroutineScope.launch { listState.scrollTo(Math.max(0, index)) }
    }

    fun switchBetweenTopAndLastVisiblePosition(coroutineScope: CoroutineScope, listState: LazyListState, lastPosition:MutableState<Int>)  {
        val lastVisibleLine = listState.firstVisibleItemIndex
        val notAtTop = lastVisibleLine != 0
        // if 不在顶部，go to 顶部 else go to 上次编辑位置
        val position = if(notAtTop) {
            lastPosition.value = lastVisibleLine
            0
        } else {
            lastPosition.value
        }

        scrollToItem(coroutineScope, listState, position)
    }


    fun switchBetweenTopAndLastVisiblePosition(coroutineScope: CoroutineScope, listState: ScrollState, lastPosition:MutableState<Int>)  {
        val lastVisibleLine = listState.value
        val notAtTop = lastVisibleLine != 0
        // if 不在顶部，go to 顶部 else go to 上次编辑位置
        val position = if(notAtTop) {
            lastPosition.value = lastVisibleLine
            0
        } else {
            lastPosition.value
        }

        scrollTo(coroutineScope, listState, position)
    }


    /**
     * 获取高亮条目闪烁时间（换个角度来说，即“解除高亮倒计时”）。
     * 应用场景：在列表定位某个条目后，短暂高亮那个条目以便用户发现
     */
    fun getHighlightingTimeInMills(): Long {
//        return 2000L  //注：1000等于1秒
        return 800L
    }

    @Composable
    fun getHighlightingBackgroundColor(baseColor:Color = MaterialTheme.colorScheme.inversePrimary): Color {
        return baseColor.copy(alpha = 0.4f)
    }

    @Composable
    fun defaultCardColor():Color {
        // light blue `Color(0xFFDBE9F3)`

        return if(Theme.inDarkTheme) MaterialTheme.colorScheme.surfaceBright else MaterialTheme.colorScheme.surfaceDim
//        return MaterialTheme.colorScheme.surfaceBright
    }


    fun getConflictOursBlockBgColor():Color {
        return if(Theme.inDarkTheme) Theme.Orange.copy(alpha = 0.1f) else Theme.Orange.copy(alpha = 0.2f)
    }

    fun getConflictTheirsBlockBgColor():Color {
        return if(Theme.inDarkTheme) Color.Magenta.copy(alpha = 0.1f) else Color.Magenta.copy(alpha = 0.2f)
    }

    fun getConflictStartLineBgColor():Color {
        return if(Theme.inDarkTheme) Theme.Orange.copy(alpha = 0.2f) else Theme.Orange.copy(alpha = 0.4f)
    }

    fun getConflictSplitLineBgColor():Color {
        return if(Theme.inDarkTheme) Theme.darkLightBlue.copy(alpha = 0.4f) else Color.Blue.copy(alpha = 0.2f)
    }

    fun getConflictEndLineBgColor():Color {
        return if(Theme.inDarkTheme) Color.Magenta.copy(alpha = 0.2f) else Color.Magenta.copy(alpha = 0.4f)
    }

    fun getAcceptOursIconColor():Color {
        return if(Theme.inDarkTheme) Theme.Orange.copy(.4f) else Theme.Orange.copy(.8f)
    }

    fun getAcceptTheirsIconColor():Color {
        return if(Theme.inDarkTheme) Color.Magenta.copy(.4f) else Color.Magenta.copy(.8f)
    }
    fun getAcceptBothIconColor():Color {
        return if(Theme.inDarkTheme) Theme.darkLightBlue.copy(alpha = 0.6f) else Color.Blue.copy(.8f)
    }
    fun getRejectBothIconColor():Color {
        return if(Theme.inDarkTheme) Color.Red.copy(.4f) else Color.Red.copy(.8f)
    }


    fun getBackgroundColorForMergeConflictSplitText(
        text: String,
        settings: AppSettings,
        expectConflictStrDto: ExpectConflictStrDto,

        oursBgColor:Color,
        theirsBgColor:Color,
        startLineBgColor:Color,
        splitLineBgColor:Color,
        endLineBgColor:Color,
        normalBgColor:Color = Color.Unspecified,
    ): Color {
        val nextExpectConflictStr = expectConflictStrDto.getNextExpectConflictStr()
        val curExpectConflictStr = expectConflictStrDto.curConflictStr
        val curExpectConflictStrMatched = expectConflictStrDto.curConflictStrMatched


        val (curExpect, nextExcept) = expectConflictStrDto.getCurAndNextExpect()

        val retColor = if(curExpectConflictStrMatched) {
            if(text.startsWith(nextExpectConflictStr)) {
                expectConflictStrDto.curConflictStr = if(nextExcept==0) settings.editor.conflictStartStr else if(nextExcept==1) settings.editor.conflictSplitStr else settings.editor.conflictEndStr

                // matched end
                if(nextExcept==2) {
                    expectConflictStrDto.reset()
//                   // expectConflictStrDto.curConflictStrMatched = false
//                   // expectConflictStrDto.curConflictStr = settings.editor.conflictStartStr
                }

                if(nextExcept==0) startLineBgColor else if(nextExcept==1) splitLineBgColor else endLineBgColor
            }else {
//                //if(curExpect==0) startBgColor else if(curExpect==1) splitBgColor else endBgColor

                // split line only colored itself
                //分割行只为自己着色，后续使用结束行的颜色（accept theirs)
                if(curExpect==0) oursBgColor else theirsBgColor
            }
        }else {
            // first match, should matched start ever
            if(text.startsWith(curExpectConflictStr)) {
                expectConflictStrDto.curConflictStrMatched = true
                if(curExpect==0) startLineBgColor else if(curExpect==1) splitLineBgColor else endLineBgColor
//                startLineBgColor  // this should be fine too
            }else {
                normalBgColor
            }
        }


        return retColor

    }

    fun getCheckBoxByState(state: Boolean) = if (state) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank

    fun getIconForSwitcher(state: Boolean) = if (state) Icons.Filled.ToggleOn else Icons.Filled.ToggleOff

    fun getColorForSwitcher(state: Boolean) = if (state) Color(0xFF0090FF) else if(Theme.inDarkTheme) Color.LightGray else Color.Gray

    fun getChangeListTitleColor(repoStateValue: Int):Color {
        return if(repoStateValue == Repository.StateT.MERGE.bit || repoStateValue == Repository.StateT.REBASE_MERGE.bit || repoStateValue == Repository.StateT.CHERRYPICK.bit) MyStyleKt.TextColor.danger() else Color.Unspecified
    }


    fun getChangeTypeColor(type: String):Color {
        if(type == Cons.gitStatusNew) {
            return if(Theme.inDarkTheme) MyStyleKt.ChangeListItemColor.changeTypeAdded_darkTheme else MyStyleKt.ChangeListItemColor.changeTypeAdded
        }
        if(type == Cons.gitStatusDeleted) {
            return if(Theme.inDarkTheme) MyStyleKt.ChangeListItemColor.changeTypeDeleted_darkTheme else MyStyleKt.ChangeListItemColor.changeTypeDeleted
        }
        if(type == Cons.gitStatusModified) {
            return if(Theme.inDarkTheme) MyStyleKt.ChangeListItemColor.changeTypeModified_darkTheme else MyStyleKt.ChangeListItemColor.changeTypeModified
        }
        if(type == Cons.gitStatusConflict) {
            return if(Theme.inDarkTheme) MyStyleKt.ChangeListItemColor.changeTypeConflict_darkTheme else MyStyleKt.ChangeListItemColor.changeTypeConflict
        }

        return Color.Unspecified
    }


    fun getDeviceWidthHeightInDp(context: Context): DeviceWidthHeight {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        val widthInDp = metrics.widthPixels / metrics.density
        val heightInDp = metrics.heightPixels / metrics.density
        MyLog.d(TAG, "#getDeviceWidthHeightInDp: widthInDp=$widthInDp, heightInDp=$heightInDp")
        return DeviceWidthHeight(widthInDp, heightInDp)
    }

    fun getRepoItemWidth(): Float {
        return 392f
    }

    fun getRepoItemsCountEachRow():Int {
        val count = AppModel.deviceWidthHeight.width / getRepoItemWidth()
        return count.toInt().coerceAtLeast(1)
    }

    fun getLastClickedColor(): Color {
        return if(Theme.inDarkTheme) Color.DarkGray.copy(alpha = .2f) else Color.LightGray.copy(alpha=.2f)
    }


    fun getRunningStateColor(runningStatus: Boolean?):Color {
        return if (runningStatus == null) MyStyleKt.TextColor.error() else if (runningStatus) MyStyleKt.TextColor.highlighting_green else Color.Unspecified
    }


    fun getRunningStateText(context: Context, runningStatus: Boolean?):String{
        return if (runningStatus == null) context.getString(R.string.unknown_state) else if (runningStatus) context.getString(R.string.running) else context.getString(R.string.stopped)
    }

    fun getTextForSwitcher(context: Context, runningStatus: Boolean?):String {
        return if (runningStatus == true) context.getString(R.string.enable) else context.getString(R.string.disable)
    }


    fun getCardButtonTextColor(enabled: Boolean, inDarkTheme: Boolean):Color {
        return if (enabled) MyStyleKt.TextColor.enable else if (inDarkTheme) MyStyleKt.TextColor.disable_DarkTheme else MyStyleKt.TextColor.disable
    }

}
