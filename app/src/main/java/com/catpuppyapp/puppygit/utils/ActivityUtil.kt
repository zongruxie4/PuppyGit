package com.catpuppyapp.puppygit.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings


object ActivityUtil {

//    @Composable
//    fun getCurrentActivity(): Activity? {
//        val context = LocalContext.current
        // 必须再递归查找一下，不然有可能无法转换为Activity，例如在弹窗获取LocalContext.current，可能就会无法直接转换为Activity
//        return context.findActivity()
//    }

    //貌似必须在主线程执行此方法
    //这个不一定能显示文件是否保存的toast
    //注：recreate适用于 Build.VERSION.SDK_INT >= 11
    fun restartActivityByRecreate(activity: Activity) {
        activity.recreate()
    }

    //这个重启几乎能百分百显示是否保存的Toast
    fun restartActivityByIntent(activity:Activity) {
        activity.apply {
            val intent = getIntent()
//            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)  //禁用切换动画，不知道禁用有什么意义，所以注释了
            finish()
//            overridePendingTransition(0, 0)  //禁用切换动画
            startActivity(intent)
//            overridePendingTransition(0, 0)  //禁用切换动画
        }

    }

    //打开网址、mailto链接等url，但因为url和uri太像，所以参数名改成linkUrl了
    fun openUrl(context: Context, linkUrl:String) {
        val uri = Uri.parse(linkUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(intent)
    }

    /**
     * this method may throw exception when packageName invalid, better try catch when using it
     */
    fun openSpecifedAppInfoPage(context: Context, packageName:String) {
        if(packageName.isBlank()) {
            return
        }

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        context.startActivity(intent)
    }

    fun openThisAppInfoPage(context: Context) {
        openSpecifedAppInfoPage(context, AppModel.appPackageName)
    }
}
