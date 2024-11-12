package com.catpuppyapp.puppygit.jni

data class SshCert(
    val libgit2ThinkIsValid:Boolean=false,
    val domain:String="",
    val md5:String="",
    val sha1:String="",
    val sha256:String="",
    val hostKey:String="",

)
