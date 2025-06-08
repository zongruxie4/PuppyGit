package com.catpuppyapp.puppygit.utils.compare

import com.catpuppyapp.puppygit.dev.DevFeature
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
        degradeMatchByWordsToMatchByCharsIfNonMatched: Boolean = DevFeature.degradeMatchByWordsToMatchByCharsIfNonMatched.state.value,

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
            matchByWords = matchByWords,

            // 降级按单词匹配为按字符匹配，如果按单词匹配无匹配
            degradeToCharMatchingIfMatchByWordFailed = degradeMatchByWordsToMatchByCharsIfNonMatched,
        )

        return if(swap) {
            result.copy(add = result.del, del = result.add)
        }else {
            result
        }
    }

    /**
     * @param targetMatchCount if has these chars matched, treat startsWith or endsWith be true, else treat as false.
     *   larger for more text matching(bad performance), smaller for more faster handling(good performance)
     *   , when reach this count, this method will abort the compare and return the result
     */
    fun roughlyMatch(str1NoLineBreak:String, str2NoLineBreak:String, targetMatchCount:Int = 5): Boolean {
        if(str1NoLineBreak.isEmpty() || str2NoLineBreak.isEmpty()) {
            return false
        }

        // use box as pointer to avoid value capture by closure
        var str1NonBlankStartIndex = getFirstNonBlankIndexOfStr(str1NoLineBreak, false)
        if(str1NonBlankStartIndex == -1){
            return false
        }

        var str1NonBlankEndIndex = getFirstNonBlankIndexOfStr(str1NoLineBreak, true)
        if(str1NonBlankEndIndex == -1){
            return false
        }

        var str2NonBlankStartIndex = getFirstNonBlankIndexOfStr(str2NoLineBreak, false)
        if(str2NonBlankStartIndex == -1){
            return false
        }

        var str2NonBlankEndIndex = getFirstNonBlankIndexOfStr(str2NoLineBreak, true)
        if(str2NonBlankEndIndex == -1){
            return false
        }

        // not enough to expected match count
        if(((str1NonBlankEndIndex - str1NonBlankStartIndex) < targetMatchCount) || ((str2NonBlankEndIndex - str2NonBlankStartIndex) < targetMatchCount)) {
            return false
        }

        // long str contains part of short str
        if(longerContainsPartOfShorter(str1NoLineBreak, str2NoLineBreak, targetMatchCount, IntRange(str1NonBlankStartIndex, str1NonBlankEndIndex), IntRange(str2NonBlankStartIndex, str2NonBlankEndIndex))) {
            return true
        }

        // match starts with
        if(matchStartsOrEndsWith(str1NoLineBreak, str2NoLineBreak, false, targetMatchCount, str1NonBlankStartIndex, str2NonBlankStartIndex)) {
            return true
        }

        // match ends with
        if(matchStartsOrEndsWith(str1NoLineBreak, str2NoLineBreak, true, targetMatchCount, str1NonBlankEndIndex, str2NonBlankEndIndex)) {
            return true
        }

        return false
    }

    /**
     * @param reverse true ends with else starts with
     * @param targetMatchCount match how many chars as matched success, if this value greater than str1 or str2 's length after trimStarts/Ends, this method will return false as not matched
     */
    private fun matchStartsOrEndsWith(str1:String, str2:String, reverse: Boolean, targetMatchCount:Int, str1InitIndex:Int, str2InitIndex:Int): Boolean {
        val str1Index = Box(str1InitIndex)
        val str2Index = Box(str2InitIndex)

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

    private fun longerContainsPartOfShorter(str1: String, str2:String, targetMatchCount:Int, str1NonBlankRange: IntRange, str2NonBlankRange: IntRange): Boolean {
        var longerRange = str1NonBlankRange
        var shorterRange = str2NonBlankRange

        val (longer, shorter) = if(str1NonBlankRange.let { it.endInclusive - it.start } >= str2NonBlankRange.let { it.endInclusive - it.start }) {
            Pair(str1, str2)
        }else {
            longerRange = str2NonBlankRange
            shorterRange = str1NonBlankRange

            Pair(str2, str1)
        }

        val start = (shorterRange.start + 4).coerceAtMost(shorterRange.endInclusive)
        val end = (shorterRange.endInclusive - 4).coerceAtLeast(shorterRange.start)

        // treat sub string that less than expected match count as not contains
        //不足期望的匹配字符数，当作无匹配
        // targetMatchCount通常大于0，所以如果end - start小于等于0，会在这返回，而等于0即涵盖了subString为空字符串的情况，所以后面不需要专门在sub string为empty时专门返回false了，
        // 另一个这里不判断empty的原因是为了完全遵从targetMatchCount，不然当期望匹配0则当作两个字符串匹配时（只要调用此函数就当作匹配而不管是否真的匹配），代码逻辑会不对
        if((end - start) < targetMatchCount) {
            return false
        }

        val subShorter = shorter.substring(IntRange(start, end))

        return longer.contains(subShorter)
    }
}
