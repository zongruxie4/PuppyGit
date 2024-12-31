package com.catpuppyapp.puppygit.utils.compare

import com.catpuppyapp.puppygit.utils.compare.param.CompareParam
import com.catpuppyapp.puppygit.utils.compare.result.IndexModifyResult

object CmpUtil {

    /**
     * @param swap 我发现del在前add在后匹配率更高，可能因为修改某行一般不会完全删除旧行内容？
     *   总之，因为感觉匹配率高所以我加了这个参数，若传true，则会在比较前调用add和del，
     *   并在返回结果前再调换回来，调用者只需正常传add和del即可，无需再手动调换；
     *   若传false，则在比较时不会调用add和del的顺序。
     */
    fun <T:CharSequence> compare(
        add: CompareParam<T>,
        del: CompareParam<T>,
        requireBetterMatching: Boolean,
        matchByWords: Boolean,
        swap: Boolean = false,

    ): IndexModifyResult {
        val (add, del) = if(swap) {
            Pair(del, add)
        }else {
            Pair(add, del)
        }

        val result = SimilarCompare.INSTANCE.doCompare(
            add = add,
            del = del,

            //为true则对比更精细，但是，时间复杂度乘积式增加，不开 O(n)， 开了 O(nm)
            requireBetterMatching = requireBetterMatching,

            //根据单词匹配
            matchByWords = matchByWords
        )

        return if(swap) {
            result.copy(add = result.del, del = result.add)
        }else {
            result
        }
    }

}
