package com.catpuppyapp.puppygit.etc

import com.catpuppyapp.puppygit.utils.FsUtils.absolutePathPrefix
import com.catpuppyapp.puppygit.utils.FsUtils.contentUriPathPrefix
import com.catpuppyapp.puppygit.utils.FsUtils.fileUriPathPrefix

enum class PathType {
   INVALID,
   CONTENT_URI, // starts with "content://"
   FILE_URI,  // starts with "file://"
   ABSOLUTE  // stars with "/"

   ;

   companion object {
      fun getType(originPath:String): PathType {
         return if(originPath.startsWith(absolutePathPrefix)) {
            ABSOLUTE
         }else if(originPath.startsWith(contentUriPathPrefix)) {
            CONTENT_URI
         }else if(originPath.startsWith(fileUriPathPrefix)) {
            FILE_URI
         }else {
            INVALID
         }
      }
   }
}

