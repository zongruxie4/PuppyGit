package com.catpuppyapp.puppygit.utils

import com.catpuppyapp.puppygit.constants.Cons

object RegexUtil {
    private const val extMatchFlag = "*."
    private const val slash = Cons.slashChar
    private const val spaceChar = ' '

    /**
     *
     * *.ext 可匹配后缀，多个*.可匹配不同后缀，只要目标匹配任一后缀就返回真，若有其他关键词例如"*.md *.txt filename_part1 part2"则会变成目标匹配任一后缀且包含其他所有关键词则返回真
     *
     *
     * example:
     *     val input = "TARGET_FILE.md".lowercase()
     *     val pattern = listOf(
     *         "tar",  // match, true
     *         "tar fi*.md",   // match, true
     *         "tar file *.md",  // match, true
     *         "t?r",   // no-match, false, '?' is not treat as placeholder here, 这个不会把?当作占位符
     *         "*.md",  // match, true
     *     )
     *
     *     for (p in pattern) {
     *         println(matchWildcard(input, p))
     *     }
     *
     */
    fun matchWildcard(target: String, keyword: String): Boolean {
        //若input为空，不管pattern是什么（即使也为空），都返回false
        if(target.isEmpty() || keyword.isEmpty()) {
            return false
        }

        //完全匹配
        if(target == keyword) {
            return true
        }


        //不完全匹配，尝试匹配后缀名和关键词，若target匹配任一后缀名且包含所有关键词则返回true
        val splitKeyword = keyword.split(spaceChar)
        if(splitKeyword.isEmpty()) {
            return false
        }

        val extFlagLen = extMatchFlag.length
        var needMatchExt = false
        var extMatched = false
        var validKeyword = false  //指示keyword是否有效（非空即有效）
        for (k in splitKeyword) {
            if(k.isEmpty()) {
                continue
            }

            validKeyword = true

            if(k.length > extFlagLen && k.startsWith(extMatchFlag)) {  //有后缀名。（注意："*." 会被认为无后缀名，然后去匹配else的 keyword part部分）
                needMatchExt = true

                if(!extMatched) {  //若已匹配后缀成功则不需要再匹配
                    extMatched = target.endsWith(k.substring(extFlagLen-1))  // `extFlagLen - 1` to keep '.', e.g. "*.txt" will got ".txt"
                }
            }else {  // keyword part，部分关键字
                if(target.contains(k).not()) {  //不包含某一非后缀名的关键词则直接返回假
                    return false
                }
            }
        }


        //执行到这里，非后缀名的关键字片段必然全部匹配或者不需要匹配（注：若关键字仅包含 "*.txt" 之类的后缀名则不需要匹配关键字片段），后面仅需要判断是否匹配后缀即可。


        //若需要匹配后缀且匹配成功，则返回真（keyword必然包含"*.txt"之类的后缀）；否则若关键字有效（包含非空的片段），则返回真（keyword无"*.txt"之类后缀，但包含有效关键字且全部匹配）。
        //配合上述已经全部匹配关键字的逻辑，这个return的含义为：后缀和关键字均匹配成功则返回真；关键字为空或匹配失败则返回假。
        // 若需要匹配ext，则返回ext是否匹配（实际为ext和关键词片段是否全部匹配）；否则返回关键词是否有效（关键词片段非空且是否全部匹配）
        return if(needMatchExt) extMatched else validKeyword
    }



    //这个不能用斜杠星号注释，会无法判断注释范围。
    // examples:
    // val path = "/a/b/c/TARGET_FILE.md";
    //
    // match results:
    //    /a/b/c/TAR: false
    //    /a/b/c/TAR FI*.md: false
    //    /a/b/c/TAR FILE *.md: false
    //    /a/b/c/target_FILE.md: false
    //    /a/b/*.md: false
    //
    //    /a/b/c/*.md: true
    //    *.md: true
    //    /a/b/c/TARGET_FILE.md: true
    //    /a/b/c/*.md: true
    //
    // examples2（特殊情况，"/*.ext"这种"/*."开头的模式，仅匹配根目录下有相同后缀的文件）:
    // pattern="/*.txt": path="abc.txt"，返回 true；path="a/abc.txt"，返回false。（实际匹配均不包含引号）
    @Deprecated("改用git自己的忽略机制了，remove from git + add path to .gitignore能实现和我的ignore机制相同的功能")
    fun matchForIgnoreFile(path:String, pattern:String):Boolean {
        //这种比较特殊，仅匹配仓库根目录下匹配通配符的文件
        val patternStarsWithSlashAndExtFlag = pattern.startsWith("$slash$extMatchFlag")  // starts with "/*."

        val target = path.trim(slash)
        val keyword = pattern.trim(slash)

        if(target.isEmpty() || keyword.isEmpty()) {
            return false
        }

        //完全匹配
        if(target == keyword) {
            return true
        }

        val extensionIdx = keyword.indexOf(extMatchFlag)

        if(extensionIdx < 0) { // no "*."
            return target.startsWith("$keyword/")  //添加 "/" 是为了避免 "abcd".startsWith("abc")返回真，这期望的条件是 target == keyword 或 target starts with "keyword/"，不过前面已经判断了是否相等，所以这里只需判断是否starts with，若是，则target是keyword子目录
        }else {  //后缀名在开头，或不在开头（在中间或末尾，两种情况都已经处理）
            //后缀名，例如 mp4，没.也没*，只有后缀名
            val extension = keyword.substring(extensionIdx + extMatchFlag.length)
            if(extension.isEmpty()) {  //空后缀名，又带了 "*."，无效，返回假
                return false
            }

            // 忽略通配符 "*.ext" 将忽略所有目录下的ext文件
            if(extensionIdx == 0) { //后缀名在开头，不限目录层级匹配同后缀文件
                val targetEndsWithExt = target.endsWith(".$extension")  // ends with ".ext"
                if(patternStarsWithSlashAndExtFlag) {  //仅匹配根目录下的文件
                    //例如：path="abc.txt", pattern="/*.txt"，返回true；但path="a/b/c/abc.txt"或"a/def.txt"之类的非仓库根目录路径，则返回false
                    return targetEndsWithExt && target.contains(slash).not()  //目标以后缀名结尾，并且不包含任何其他的"/"，说明目标既在仓库根目录又匹配了后缀名
                }else {
                    return targetEndsWithExt
                }
            }

            // "*." 不在开头
            val indexOfSlash = keyword.indexOf(slash)
            // "*." 没在路径末尾，例如："abc/*./def"，这种情况返回假
            if(indexOfSlash > extensionIdx) {  //后缀名通配符在路径中间，无效
                return false
            }

            val targetLastIndexOfSlash = target.lastIndexOf(slash)
            if(targetLastIndexOfSlash < 0) { // keyword包含路径分隔符，但target不包含，必然不匹配，返回假。（另外，因为上面先移除了keyword和target首尾的/，所以，这里若有/必然在中间
                return false
            }

            val fileNameIdx = targetLastIndexOfSlash+1  //路径的文件名部分
            val targetParentPath = target.substring(0, fileNameIdx)  //分割出的字符串包含"/"，但不包含后缀，例如："path/to/file/"
            val targetName = target.substring(fileNameIdx) //文件名部分，例如 "abc.txt"
            val keywordParentPath = keyword.substring(0, extensionIdx)  // keyword的路径部分，一定有，因为若在开头，上面已经处理

            return targetParentPath == keywordParentPath && targetName.endsWith(".$extension")
        }
    }

    fun matchWildcardList(target: String, keywordList:List<String>, ignoreCase:Boolean):Boolean {
        return matchByPredicate(target, keywordList, ignoreCase) { target, keyword ->  //这个lambda内的target是转换过大小写的
            matchWildcard(target, keyword)
        }
    }

    /**
     * @param ignoreCase if false, will pass origin input and pattern to predicate , else pass lowercased input and pattern.
     */
    fun matchByPredicate(target: String, keywordList:List<String>, ignoreCase:Boolean, predicate:(target:String, keyword:String)->Boolean):Boolean {
        if(target.isEmpty() || keywordList.isEmpty()) {
            return false
        }

        val target = if(ignoreCase) target.lowercase() else target

        for (keyword in keywordList) {
            if(predicate(target, keyword.let { if(ignoreCase) keyword.lowercase() else keyword })) {
                return true
            }
        }

        return false
    }
}

