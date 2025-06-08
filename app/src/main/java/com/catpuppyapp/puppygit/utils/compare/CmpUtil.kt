package com.catpuppyapp.puppygit.utils.compare

import com.catpuppyapp.puppygit.dto.Box
import com.catpuppyapp.puppygit.utils.compare.param.CompareParam
import com.catpuppyapp.puppygit.utils.compare.result.IndexModifyResult

object CmpUtil {

    /**
     * @param swap (20250210之后)我调换后用了段时间发现很多时候add在前del在后匹配率更高，可能差不多，没必要调换了，调换反而浪费性能。
     *   (20250210之前)我发现del在前add在后匹配率更高，可能因为修改某行一般不会完全删除旧行内容？
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

    /**
     * @param targetMatchCount if has these chars matched, treat startsWith or endsWith be true, else treat as false
     */
    fun roughlyMatch(str1NoLineBreak:String, str2NoLineBreak:String, targetMatchCount:Int = 5): Boolean {
        if(str1NoLineBreak.isEmpty() || str2NoLineBreak.isEmpty()) {
            return false
        }

        // long str contains part of short str
        if(longerContainsPartOfShorter(str1NoLineBreak, str2NoLineBreak)) {
            return true
        }

        // match starts with
        if(matchStartsOrEndsWith(str1NoLineBreak, str2NoLineBreak, false, targetMatchCount)) {
            return true
        }

        // match ends with
        if(matchStartsOrEndsWith(str1NoLineBreak, str2NoLineBreak, true, targetMatchCount)) {
            return true
        }

        return false
    }

    /**
     * @param reverse true ends with else starts with
     * @param targetMatchCount match how many chars as matched success, if this value greater than str1 or str2 's length after trimStarts/Ends, this method will return false as not matched
     */
    private fun matchStartsOrEndsWith(str1:String, str2:String, reverse: Boolean, targetMatchCount:Int): Boolean {
        // use box as pointer to avoid value capture by closure
        var str1Index = Box(getFirstNonBlankIndexOfStr(str1, reverse))
        if(str1Index.value == -1){
            return false
        }
        var str2Index = Box(getFirstNonBlankIndexOfStr(str2, reverse))
        if(str2Index.value == -1){
            return false
        }

        var matchCount = 0

        val condition = if(reverse) {
            { str1Index.value >= 0 && str2Index.value >= 0 }
        }else {
            { str1Index.value < str1.length && str2Index.value < str2.length }
        }

        val updateStr1Index = if(reverse) {
            { str1Index.value-- }
        }else {
            { str1Index.value++ }
        }

        val updateStr2Index = if(reverse) {
            { str2Index.value-- }
        }else {
            { str2Index.value++ }
        }

        while (condition()) {
            if(str1[updateStr1Index()] == str2[updateStr2Index()]) {
                matchCount++

                if(matchCount >= targetMatchCount) {
                    return true
                }
            }else {
                break
            }
        }

        return false
    }

    private fun getFirstNonBlankIndexOfStr(str:String, reverse: Boolean):Int {
        val endIndex = str.length-1
        // empty string
        if(endIndex < 0) {
            return -1
        }

        val idxRange = IntRange(0, endIndex).let { if(reverse) it.reversed() else it }
        for(idx in idxRange) {
            if(str[idx].isWhitespace().not()) {
                return idx
            }
        }

        return -1
    }

    private fun longerContainsPartOfShorter(str1: String, str2:String): Boolean {
        val (longer, shorter) = if(str1.length >= str2.length) {
            Pair(str1, str2)
        }else {
            Pair(str2, str1)
        }

        // get a range of center of the shorter string, e.g. shorter string is "abcdefjkl", may got range "cdefjk"
        val center = shorter.length / 2
        val start = (center-3).coerceAtLeast(0)
        val end = (center+3).coerceAtMost(shorter.length)
        val subShorter = shorter.substring(start, end)

        // treat empty as not contains
        return if(subShorter.isEmpty()) false else longer.contains(subShorter)
    }
}
