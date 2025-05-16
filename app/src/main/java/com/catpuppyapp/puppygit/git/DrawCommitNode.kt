package com.catpuppyapp.puppygit.git

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.catpuppyapp.puppygit.utils.UIHelper
import io.ktor.util.collections.ConcurrentMap


// 0-255，越小越透明
private const val alpha = 128;

// 避免并发冲突，没用普通的list
private val cachedColors = ConcurrentMap<Int, Color>().apply {
    put(0, Color(red = 0xF, green = 0xA2, blue = 0x72, alpha = alpha))
    put(1, Color(red = 0x1E, green = 0x33, blue = 0xBD, alpha = alpha))
    put(2, Color(red = 0x4D, green = 0xB6, blue = 0xB, alpha = alpha))
    put(3, Color(red = 0xB6, green = 0xB, blue = 0x69, alpha = alpha))
    put(4, Color(red = 0xB6, green = 0x55, blue = 0xB, alpha = alpha))
    put(5, Color(red = 0x5C, green = 0x18, blue = 0x8C, alpha = alpha))
    put(6, Color(red = 0xB6, green = 0x3B, blue = 0xB, alpha = alpha))
    put(7, Color(red = 0x3B, green = 0xAB, blue = 0x95, alpha = alpha))
    put(8, Color(red = 0x66, green = 0xA, blue = 0x88, alpha = alpha))
    put(9, Color(red = 0x1C, green = 0x4C, blue = 0xC7, alpha = alpha))
    put(10, Color(red = 0x25, green = 0xB6, blue = 0xB, alpha = alpha))
}


@Stable
@Immutable
data class DrawCommitNode (
    //顺序
    // 用集合中的索引替代
//    val order:Int,

    //如果当前节点endAtHere但其位于列表中间，
    // 前后皆有条目，则此节点仍保持，但仅占位，不再绘制线条，
    // 如果当前节点有多个parent，可占用空节点的位置
    val outputIsEmpty:Boolean,

    //不需要画线，仅占位
    val inputIsEmpty:Boolean,

    //需要在这个节点这一列画个圈
    val circleAtHere:Boolean,

    //这条线是否在当前commit结束（线连接到圆圈）
    val endAtHere:Boolean,

    //这条线是否从当前提交开始（从圆圈起始）
    val startAtHere:Boolean,

    //线的颜色
//    val color: Color,

    //关联的commit的完整hash
    val fromCommitHash:String,

    val toCommitHash:String,
) {
    // 如果是 node，会在条目中间画的圆圈。
    // 在以单head为基准的视图中，只有提交本身是node，在以仓库所有引用为基准的视图中，每个引用的起点也是node（也要画圆圈）
//    fun isNode() = order == 0;

    // 不是node就是line，line就是直接顺着路径往下画，中间没圆圈的那种
//    fun isLine() = isNode().not();

    companion object {
        /**
         * 找一个可插入的节点，如果中间有empty节点，会返回那个节点的索引（占它的位置，继续画线），否则返回 -1
         */
        fun getAnInsertableIndex(list:List<DrawCommitNode>):Int {
            for((idx, node) in list.withIndex()) {
                if(node.outputIsEmpty) {
                    return idx
                }
            }

            return -1
        }

        fun getNodeColorByIndex(i: Int): Color {
            //缓存颜色的集合得是可变的，运行时如果发现颜色索引不存在，会插入个新颜色存进去
            // alpha越大越不透明，范围 0-255，128是半透明
            return cachedColors.get(i) ?: UIHelper.getRandomColor(alpha = alpha).let {
                cachedColors.set(i, it)

                it
            }
        }
    }
}
