package com.catpuppyapp.puppygit.utils.compare

import com.catpuppyapp.puppygit.utils.compare.param.CompareParam
import com.catpuppyapp.puppygit.utils.compare.result.IndexModifyResult
import com.catpuppyapp.puppygit.utils.compare.result.IndexStringPart
import com.catpuppyapp.puppygit.utils.compare.search.Search
import com.catpuppyapp.puppygit.utils.compare.search.SearchDirection

class SimilarCompareImpl: SimilarCompare {
    override fun doCompare(
        add: CompareParam,
        del: CompareParam,
        emptyAsMatch:Boolean,
        emptyAsModified:Boolean,
        onlyLineSeparatorAsEmpty:Boolean,
        searchDirection: SearchDirection,
        requireBetterMatching: Boolean,
        search: Search,
        betterSearch: Search,
        matchByWords:Boolean
    ): IndexModifyResult {
        // empty check
        if(add.isEmpty() || del.isEmpty() || (onlyLineSeparatorAsEmpty && (add.isOnlyLineSeparator() || del.isOnlyLineSeparator()))){ //其中一个为空或只有换行符，不比较，直接返回结果，当作无匹配
            return IndexModifyResult(matched = emptyAsMatch, matchedByReverseSearch = false,
                listOf(IndexStringPart(0, add.getLen(), emptyAsModified)),
                listOf(IndexStringPart(0, del.getLen(), emptyAsModified)))
        }


        // match by words
        if(matchByWords) {
            //如果按单词比较为真，尝试以单词比较，如果有匹配，直接返回，如果无匹配，则继续往下执行普通比较
            val matchByWordsResult = doMatchByWords(add, del, requireBetterMatching)
            if(matchByWordsResult.matched) {
                return matchByWordsResult
            }
        }


        // match by chars
        val reverse = searchDirection == SearchDirection.REVERSE || searchDirection == SearchDirection.REVERSE_FIRST

        val reverseMatchIfNeed = searchDirection == SearchDirection.REVERSE_FIRST || searchDirection == SearchDirection.FORWARD_FIRST

        //用On算法最坏的情况是正向匹配一次，然后逆向匹配一次，时间复杂度为 O(2n)，正常情况时间复杂度为O(n)，O(nm)若匹配两次，也会翻倍，最坏时间复杂度变成O(2nm)
        var result = if(requireBetterMatching) {
            betterSearch.doSearch(add, del, reverse)
        }else {
            search.doSearch(add, del, reverse)
        }

        //反向查找，如果需要的话。判断条件是：如果 “允许反向匹配 且 正向没匹配到”
        if(reverseMatchIfNeed && !result.matched) {
            result = if(requireBetterMatching) betterSearch.doSearch(add, del, !reverse) else search.doSearch(add, del, !reverse)
        }

        return result

    }

    /**
     * @param requireBetterMatching if true, will try index of for not-matched words
     */
    private fun doMatchByWords(
        add: CompareParam,
        del: CompareParam,
        requireBetterMatching: Boolean
    ):IndexModifyResult {
        val addWordAndIndexList = getWordAndIndexList(add)
        val delWordAndIndexList = getWordAndIndexList(del)

        var matched = false

        val addIndexResultList = mutableListOf<IndexStringPart>()
        val delIndexResultList = mutableListOf<IndexStringPart>()

        val addNotMatchedList = mutableListOf<WordAndIndex>()
        val delNotMatchedList = mutableListOf<WordAndIndex>()
        addNotMatchedList.addAll(addWordAndIndexList)
        delNotMatchedList.addAll(delWordAndIndexList)

        for((addIndex, addWord) in addWordAndIndexList.withIndex()) {
            val addStr = addWord.getWordStr()
            for((delIndex, delWord) in delWordAndIndexList.withIndex()) {
                if(delWord.matched) {
                    continue
                }

                val delStr = delWord.getWordStr()

                if(addStr == delStr) {
                    matched = true

                    addWord.matched = true
                    delWord.matched = true

                    addIndexResultList.add(
                        IndexStringPart(
                            start = addWord.index,
                            end = addWord.index+addStr.length,
                            modified = false
                        )
                    )

                    delIndexResultList.add(
                        IndexStringPart(
                            start = delWord.index,
                            end = delWord.index+delStr.length,
                            modified = false
                        )
                    )

                    addNotMatchedList.removeAt(addIndex)
                    delNotMatchedList.removeAt(delIndex)

                    break
                }
            }
        }

        //if requireBetterMatching is true, try use indexOf matching the not-matched items
        var addStillNotMatchedList = mutableListOf<WordAndIndex>()
        var delStillNotMatchedList = mutableListOf<WordAndIndex>()
        if(requireBetterMatching && addNotMatchedList.isNotEmpty() && delNotMatchedList.isNotEmpty()) {
            for(a in addNotMatchedList) {
                val addStr = a.getWordStr()
                for(d in delNotMatchedList) {
                    if(d.matched) {
                        continue
                    }

                    val delStr = d.getWordStr()
                    if(addStr.length > delStr.length) {
                        val indexOf = addStr.indexOf(delStr)
                        if(indexOf >= 0) {
                            matched = true

                            a.matched = true
                            d.matched = true

                            val aStartIndex = a.index+indexOf
                            val aEndIndex = aStartIndex+delStr.length
                            addIndexResultList.add(
                                IndexStringPart(
                                    start = aStartIndex,
                                    end = aEndIndex,
                                    modified = false
                                )
                            )

                            delIndexResultList.add(
                                IndexStringPart(
                                    start = d.index,
                                    end = d.index+delStr.length,
                                    modified = false
                                )
                            )

                            val beforeMatched = addStr.substring(0, indexOf)
                            val afterMatched = addStr.substring(indexOf+delStr.length)
                            if(beforeMatched.isNotEmpty()) {
                                val w = WordAndIndex(index = a.index)
                                w.word.append(beforeMatched)
                                addStillNotMatchedList.add(w)
                            }
                            if(afterMatched.isNotEmpty()) {
                                val w = WordAndIndex(index = aEndIndex)
                                w.word.append(afterMatched)
                                addStillNotMatchedList.add(w)
                            }
                        }
                    }else {
                        val indexOf = delStr.indexOf(addStr)
                        if(indexOf >= 0) {
                            matched = true

                            a.matched = true
                            d.matched = true

                            val dStartIndex = d.index+indexOf
                            val dEndIndex = dStartIndex+addStr.length
                            addIndexResultList.add(
                                IndexStringPart(
                                    start = a.index,
                                    end = a.index+addStr.length,
                                    modified = false
                                )
                            )

                            delIndexResultList.add(
                                IndexStringPart(
                                    start = dStartIndex,
                                    end = dEndIndex,
                                    modified = false
                                )
                            )

                            val beforeMatched = delStr.substring(0, indexOf)
                            val afterMatched = delStr.substring(indexOf+addStr.length)
                            if(beforeMatched.isNotEmpty()) {
                                val w = WordAndIndex(index = d.index)
                                w.word.append(beforeMatched)
                                delStillNotMatchedList.add(w)
                            }
                            if(afterMatched.isNotEmpty()) {
                                val w = WordAndIndex(index = dEndIndex)
                                w.word.append(afterMatched)
                                delStillNotMatchedList.add(w)
                            }
                        }
                    }
                }
            }
        }else {  // not require better matching
            addStillNotMatchedList = addNotMatchedList
            delStillNotMatchedList = delNotMatchedList
        }

        // at last, may still have not matched items, they are `modified`
        for(item in addStillNotMatchedList) {
            addIndexResultList.add(
                IndexStringPart(
                    start = item.index,
                    end = item.index + item.getWordStr().length,
                    modified = true
                )
            )
        }

        for(item in delStillNotMatchedList) {
            delIndexResultList.add(
                IndexStringPart(
                    start = item.index,
                    end = item.index + item.getWordStr().length,
                    modified = true
                )
            )
        }


        // create comparator for sort list by index
        val comparator = { o1:IndexStringPart, o2:IndexStringPart ->
            o1.start.compareTo(o2.start)
        }

        // sortedWith return new List and keep origin list unchanged, sortWith sort in place, here no need new list, so using sortWith
        addIndexResultList.sortWith(comparator)
        delIndexResultList.sortWith(comparator)


        return IndexModifyResult(
            matched = matched,
            matchedByReverseSearch = false,
            add = addIndexResultList,
            del = delIndexResultList
        )
    }

    private fun getWordAndIndexList(compareParam:CompareParam):List<WordAndIndex> {
        var wordMatching = false
        var spaceMatching = false
        var wordAndIndex:WordAndIndex? = null
        var spaceAndIndex:WordAndIndex? = null


        val wordAndIndexList= mutableListOf<WordAndIndex>()
        val spaceAndIndexList = mutableListOf<WordAndIndex>()

        for(i in 0 until compareParam.getLen()) {
            val char = compareParam.getChar(i)
            if(char.isWhitespace()) {
                wordMatching = false

                if(spaceMatching) {
                    spaceAndIndex!!.word.append(char)
                }else {
                    spaceAndIndex = WordAndIndex(index = i)
                    spaceAndIndex.word.append(char)
                    spaceAndIndexList.add(spaceAndIndex)
                    spaceMatching = true
                }
            }else {  // not a blank char
                spaceMatching = false

                if(wordMatching) {
                    wordAndIndex!!.word.append(char)
                }else {
                    wordAndIndex = WordAndIndex(index = i)
                    wordAndIndex.word.append(char)
                    wordAndIndexList.add(wordAndIndex)
                    wordMatching = true
                }
            }
        }

        wordAndIndexList.addAll(spaceAndIndexList)

        return wordAndIndexList
    }


}

private data class WordAndIndex(
    val index:Int=0,
    val word:StringBuilder=StringBuilder(),

    // full match(by equals) or part match(by indexof)
    //完整匹配或部分匹配，只要匹配过，就为true
    var matched:Boolean=false,
) {
    private var wordStrCached:String? = null

    fun getWordStr():String {
        if(wordStrCached==null) {
            wordStrCached = word.toString()
        }

        return wordStrCached!!
    }

}
