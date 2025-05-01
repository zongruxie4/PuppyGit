package com.catpuppyapp.puppygit.utils

import androidx.compose.ui.graphics.Color

object PatchUtil {

    // 判断一段字符串有没有可能是 patch文件的内容。（也有叫diff文件的，总之就是git 导出的patch）
    fun maybeIsPatchLine(str:String) = isFileHeader(str) || isAdd(str) || isDelete(str) || isHunkHeader(str);

    fun isAdd(str: String) = str.startsWith("+");
    fun isDelete(str: String) = str.startsWith("-") || str.trimEnd() == "\\ No newline at end of file";
    fun isHunkHeader(str: String) = str.startsWith("@@");
    fun isFileHeader(str: String) = str.startsWith("diff --git") || str.startsWith("---") || str.startsWith("+++") || str.startsWith("index") || str.startsWith("new file");

    fun getColorOfLine(str: String, inDarkTheme:Boolean):Color? = if(isFileHeader(str)) {
        if(inDarkTheme) {
            Color(0xFF4D4414)
        }else {
            Color(0xFFC7BB81)
        }
    } else if(isAdd(str)) {
        if(inDarkTheme) {
            Color(0xFF1B5E20)
        }else {
            Color(0xFF81C784)
        }
    }  else if(isDelete(str)) {
        if(inDarkTheme) {
            Color(0xFF484848)
        }else {
            Color(0xFFA2A2A2)
        }
    } else if(isHunkHeader(str)) {
        if(inDarkTheme) {
            Color(0xFF01579B)
        }else {
            Color(0xFF81B1C7)
        }
    }else null;

}
