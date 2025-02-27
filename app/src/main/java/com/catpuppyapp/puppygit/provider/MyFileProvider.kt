package com.catpuppyapp.puppygit.provider

import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import java.io.FileDescriptor
import java.io.PrintWriter

class MyFileProvider: FileProvider() {
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        println("fileprovider的update被调用了")
        return super.update(uri, values, selection, selectionArgs)
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        若文件修改，这个会被调用，所以只需要在这里把uri转成完整路径再判断路径是否处于safbuf下，若处于再调用safapi去修改saf目标文件即可！
        println("fileprovider的openFile被调用了")

        return super.openFile(uri, mode)
    }

    override fun openFile(uri: Uri, mode: String, signal: CancellationSignal?): ParcelFileDescriptor? {
        println("fileprovider的openFile2被调用了")

        return super.openFile(uri, mode, signal)
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        println("fileprovider的query被调用了")

        return super.query(uri, projection, selection, selectionArgs, sortOrder)
    }
}
