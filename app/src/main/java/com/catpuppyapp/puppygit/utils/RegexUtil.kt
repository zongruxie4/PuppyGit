package com.catpuppyapp.puppygit.utils

import com.catpuppyapp.puppygit.constants.Cons

object RegexUtil {
    private const val extMatchFlag = "*."
    private const val slash = Cons.slashChar

    /**
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


        //匹配后缀名
        val extensionIdx = keyword.lastIndexOf(extMatchFlag)
        val hasExtensionFlag = extensionIdx >= 0
        if(hasExtensionFlag) {
            val extension = keyword.substring(extensionIdx + extMatchFlag.length)
//        println(extension)  // e.g. "mp4", no "*."
            if(extension.isNotEmpty() && !target.endsWith(extension)) {
                return false
            }
        }

        //匹配后缀名前面的部分
        val keywordNoSuffix = if(hasExtensionFlag) keyword.substring(0, extensionIdx) else keyword
        if(keywordNoSuffix.isEmpty()) {
            //匹配 "*.md" 这种只有后缀无其他关键词的情况
            return hasExtensionFlag
        }

        val splitKeyword = keywordNoSuffix.split(" ").filter { it.isNotEmpty() }
        if(splitKeyword.isEmpty()) {
            return false
        }

        for (k in splitKeyword) {
            if(!target.contains(k)) {
                return false
            }
        }

        return true
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

