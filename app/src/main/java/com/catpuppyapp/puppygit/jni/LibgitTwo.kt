package com.catpuppyapp.puppygit.jni

import java.nio.charset.StandardCharsets

object LibgitTwo {
//    private val jniCloneOptionsPtr: Long = 0

    //指向clone后产生的git仓库对象的指针，c里一般用int做返回值来指示函数执行是否成功。然后传入一个指针对象用来存储需要返回的对象，例如：int createStudent(Student* ret)，其中，int是返回值，但函数创建的Student结构体存储在ret指向的内存中
//    private val jniCRepoPtr: Long = 0

    external fun jniLibgitTwoInit()
    external fun jniClone(url: String?, local_path: String?, jniCloneOptionsPtr: Long, allowInsecure: Boolean): Long

    /**
     * 返回值为指向clone_options结构体的指针
     */
    external fun jniCreateCloneOptions(version: Int): Long

    external fun jniTestClone(url: String?, local_path: String?, jniCloneOptionsPtr: Long): Long


    //    static {
    //        System.loadLibrary("crypto");
    //        System.loadLibrary("ssl");
    //        System.loadLibrary("ssh2");
    //        System.loadLibrary("git2");
    //        System.loadLibrary("git24j");
    //        System.loadLibrary("puppygit");
    //    }
    var cloneVersion: Int = 1

    /*
 * example:
     @Throws(SyscallException::class) //等于java的throws语句，在c里抛java异常时用得到这个
     external fun access(path: ByteString, mode: Int): Boolean
 */
    external fun hello(a: Int, b: Int): String?


    //TODO 写public方法调用native方法，并把返回的指针存到实例变量里
    /**
     * jniCRetParamRepoPtr:
     * url: 要clone的url
     * local_path: 仓库本地路径
     * jniOptionsPtr: clone命令的选项，是个指针，指向jni创建的options对象
     *
     * 返回值指向git仓库的指针，正常来说返回值是非0正数
     */
    external fun jniSetCertFileAndOrPath(certFile: String?, certPath: String?): Long
    external fun jniSetCredentialCbTest(remoteCallbacks: Long)
    external fun jniSetCertCheck(remoteCallbacks: Long)
    external fun jniLineGetContent(linePtr: Long): String?
    external fun jniTestAccessExternalStorage()

    /**
     *
     * @param treePtr
     * @param filename
     * @return entry ptr
     */
    external fun jniEntryByName(treePtr: Long, filename: String?): Long?
    external fun jniGetDataOfSshCert(certPtr: Long, libgit2ThinkIsValid:Boolean, domain:String): SshCert?

    fun getContent(contentLen: Int, content: String): String {
        // content.length() is "chars count", not "bytes count"!
        // so this code is wrong in some cases! sometimes it will give you more lines than you wanted.
//            if (content.length() >= contentLen) {
//                return content.substring(0, contentLen);
//            }
        val src = content.toByteArray(StandardCharsets.UTF_8)
        // bytes.length < contentLen maybe not happen, because contentLen should be a part of content
        if (src.size > contentLen) {  //if content length bigger than contentLen, create a new sub array
            val dest = ByteArray(contentLen)
            System.arraycopy(src, 0, dest, 0, contentLen)
            return String(dest)
        }

        // if content length equals contentLen, just return it, no more operations required
        return content
    }
}
