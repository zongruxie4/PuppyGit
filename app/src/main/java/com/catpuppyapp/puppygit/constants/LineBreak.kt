package com.catpuppyapp.puppygit.constants

enum class LineBreak(val value: String) {
    // old mac
    CR("\r"),

    // unix and modern mac
    LF("\n"),

    // windows
    CRLF("\r\n")

    ;

    companion object {
        fun getType(value:String, default: LineBreak?): LineBreak? {
            return if(value == CR.value) {
                CR
            }else if(value == LF.value) {
                LF
            }else if(value == CRLF.value) {
                CRLF
            }else {
                default
            }
        }
    }
}
