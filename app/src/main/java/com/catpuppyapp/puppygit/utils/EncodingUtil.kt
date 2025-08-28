package com.catpuppyapp.puppygit.utils

import org.mozilla.universalchardet.Constants
import org.mozilla.universalchardet.UniversalDetector
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


/**
 * File encoding util class
 */
object EncodingUtil {
    private const val TAG = "EncodingUtil"

    /**
     * note: CHARSET_GB18030 include GBK, GBK include GB2312
     */
    val supportedCharsetList = listOf(
        // order by a-z
        Constants.CHARSET_BIG5,

        Constants.CHARSET_EUC_JP,
        Constants.CHARSET_EUC_KR,
        Constants.CHARSET_EUC_TW,

        Constants.CHARSET_GB18030,  // include gbk
        Constants.CHARSET_GBK,  // include gb2312

        Constants.CHARSET_IBM855,
        Constants.CHARSET_IBM866,

        Constants.CHARSET_ISO_2022_JP,
        Constants.CHARSET_ISO_2022_CN,
        Constants.CHARSET_ISO_2022_KR,
        Constants.CHARSET_ISO_8859_5,
        Constants.CHARSET_ISO_8859_7,
        Constants.CHARSET_ISO_8859_8,


        Constants.CHARSET_KOI8_R,

        Constants.CHARSET_MACCYRILLIC,

        Constants.CHARSET_SHIFT_JIS,

        Constants.CHARSET_TIS620,

        Constants.CHARSET_UTF_8,
        Constants.CHARSET_UTF_16BE,
        Constants.CHARSET_UTF_16LE,
        Constants.CHARSET_UTF_32BE,
        Constants.CHARSET_UTF_32LE,

        // utf8 fully covered ASCII, so no need this encoding
//        Constants.CHARSET_US_ASCII,

        Constants.CHARSET_WINDOWS_1251,
        Constants.CHARSET_WINDOWS_1252,
        Constants.CHARSET_WINDOWS_1253,
        Constants.CHARSET_WINDOWS_1255,
    )

    val defaultCharset = Constants.CHARSET_UTF_8

    private fun makeSureUseASupportedCharset(originCharset: String?): String {
        if(originCharset == null) {
            return defaultCharset
        }

        // java doesn't support this, use gbk instead, gbk included gb2312, so should be ok
        // java不支持这个，用gbk替代，gbk包含gb2312，所以应该不会乱码
        if(originCharset == Constants.CHARSET_HZ_GB_2312) {
            return Constants.CHARSET_GBK
        }

        // java doesn't support them, just use default charset
        // java不支持的编码，使用默认即可，不过可能乱码
        if(originCharset == Constants.CHARSET_X_ISO_10646_UCS_4_3412
            || originCharset == Constants.CHARSET_X_ISO_10646_UCS_4_2143
        ) {
            return defaultCharset
        }

        if(supportedCharsetList.contains(originCharset)) {
            return originCharset
        }

        return defaultCharset
    }


    fun detectEncoding(inputStream: InputStream): Charset {
        return try {
            detectEncodingNoCatch(inputStream)
        }catch (e: Exception) {
            MyLog.e(TAG, "#detectEncoding() err, will use '$defaultCharset', err msg=${e.localizedMessage}")
            e.printStackTrace()
            StandardCharsets.UTF_8
        }
    }

    private fun detectEncodingNoCatch(inputStream: InputStream): Charset {
        // most time only need read few bytes to detect encoding, maybe thousands
        val buf = ByteArray(4096)


        // (1)
        val detector = UniversalDetector()


        // (2)
        var nread: Int = -1
        // read until detector detected the encoding
        while ((inputStream.read(buf).also { nread = it }) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread)
        }

        // (3)
        detector.dataEnd()


        // (4)
        val encoding = detector.getDetectedCharset()


        // (5)
        detector.reset()

        return Charset.forName(makeSureUseASupportedCharset(encoding))
    }

    fun resolveCharset(name:String?): Charset {
        return try {
            Charset.forName(makeSureUseASupportedCharset(name))
        }catch (e: Exception) {
            MyLog.e(TAG, "#resolveCharset err, name=$name, err=${e.localizedMessage}")
            e.printStackTrace()
            StandardCharsets.UTF_8
        }
    }

}
