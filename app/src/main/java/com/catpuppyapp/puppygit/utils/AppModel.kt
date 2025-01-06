package com.catpuppyapp.puppygit.utils

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.StorageDirCons
import com.catpuppyapp.puppygit.data.AppContainer
import com.catpuppyapp.puppygit.data.AppDataContainer
import com.catpuppyapp.puppygit.dev.FlagFileName
import com.catpuppyapp.puppygit.dev.dev_EnableUnTestedFeature
import com.catpuppyapp.puppygit.dto.DeviceWidthHeight
import com.catpuppyapp.puppygit.jni.LibLoader
import com.catpuppyapp.puppygit.play.pro.BuildConfig
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.app.upgrade.migrator.AppMigrator
import com.catpuppyapp.puppygit.utils.app.upgrade.migrator.AppVersionMan
import com.catpuppyapp.puppygit.utils.cert.CertMan
import com.catpuppyapp.puppygit.utils.fileopenhistory.FileOpenHistoryMan
import com.catpuppyapp.puppygit.utils.snapshot.SnapshotUtil
import com.catpuppyapp.puppygit.utils.storagepaths.StoragePathsMan
import com.catpuppyapp.puppygit.utils.time.TimeZoneMode
import com.catpuppyapp.puppygit.utils.time.TimeZoneUtil
import com.github.git24j.core.Libgit2
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap

private const val TAG ="AppModel"

object AppModel {


    private val inited_1 = mutableStateOf(false)
    private val inited_2 = mutableStateOf(false)
//        private val inited_3 = mutableStateOf(false)


    /**
     * 加密凭据用到的主密码，若为空且设置项中的主密码hash不为空，将弹窗请求用户输入主密码，若用户拒绝，将无法使用凭据
     */
    var masterPassword:MutableState<String> = mutableStateOf("")

    lateinit var deviceWidthHeight: DeviceWidthHeight

    /**
     * long long ago, this is applicationContext get from Activity, but now, this maybe is baseContext of Activity,
     * baseContext bundled with Activity, save it's reference may cause memory leak;
     * applicationContext bundled with App (process maybe?), save it's reference more time is safe, but it can't get properly resources in some cases,
     * e.g. when call context.getString(), baseContext can get string resource with correct language, but applicationContext maybe can't,
     * that's why I save baseContext rather than applicationContext
     *
     * update this reference in Activity#onCreate can reduce risk of mem leak, but maybe still will make mem clean delay than usual
     *
     * now , actually this is Activity's Context, not the App
     */
    @Deprecated("use `LocalContext.current` instead, but this already many usages, so, keep it for now")
    lateinit var activityContext:Context

    /**
     * real the App context, not activity, this may not be get strings resource with expect language, but for show Toast or load raw resource stream, is fine
     */
    lateinit var realAppContext:Context
    //mainActivity
//    lateinit var mainActivity:Activity

    lateinit var dbContainer: AppContainer

    @Deprecated("用 `LocalHapticFeedback.current` 替代")
    lateinit var haptic:HapticFeedback

    @Deprecated("用 `rememberCoroutineScope()` 替代，remember的貌似会随页面创建，随页面释放")
    lateinit var coroutineScope:CoroutineScope  //这个scope是全局的，生命周期几乎等于app的生命周期(？有待验证，不过因为是在根Compose创建的所以多半差不多是这样)，如果要执行和当前compose生命周期一致的任务，应该用 rememberCoroutineScope() 在对应compose重新获取一个scope

    lateinit var navController:NavHostController


    /**
     * 用来保存导航状态，在旋转屏幕后恢复目标页面
     */
    var lastNavController:NavHostController? = null


    // 废弃
//    var lastNavState:Bundle? = null

    /**
     * 编辑器最后编辑的文件，在Activity销毁时使用此变量更新`lastEditFileWhenDestroy`的值
     */
    val lastEditFile:MutableState<String> = mutableStateOf("")

    /**
     * Activity销毁时最后编辑的文件，用来在旋转屏幕后恢复
     * 此变量应确保仅消费一次，不然可能会在不应该打开文件的时候打开文件
     */
    val lastEditFileWhenDestroy:MutableState<String> = mutableStateOf("")

    /**
     * 系统时区偏移量，单位: 分钟
     * 注意这是系统偏移量，不一定等于App实际使用的时区偏移量！
     */
    private var systemTimeZoneOffsetInMinutes:Int? = null

    /**
     * App实际使用的时区偏移量对象
     */
    private var timeZoneOffset:ZoneOffset?=null

    /**
     * App 实际使用的时区偏移分钟数，和 `timeZoneOffset` 对应
     */
    private var timeZoneOffsetInMinutes:Int?=null
    private var timeZoneMode:TimeZoneMode?=null

    /**
     * key 分钟数
     * value UTC时区例如：UTC+8 UTC-7:30 UTC+0
     */
    val timezoneCacheMap:MutableMap<Int, String> = ConcurrentHashMap()


    @OptIn(ExperimentalMaterial3Api::class)
    lateinit var homeTopBarScrollBehavior: TopAppBarScrollBehavior

    lateinit var allRepoParentDir: File  // this is internal storage, early version doesn't support clone repo to external path, so this name not indicate this path is internal path, but actually it is
    lateinit var exitApp: ()->Unit
    lateinit var externalFilesDir: File
    lateinit var externalCacheDir: File

    // app 的内部目录， /data/data/app包名 或者 /data/user/0/app包名，这俩目录好像其中一个是另一个的符号链接
    lateinit var innerDataDir: File
    //存储app内置证书的目录
    lateinit var certBundleDir: File
    //存储用户证书的目录（例如自签证书
    lateinit var certUserDir: File

    //内部StorageDir存储目录，所有类型为“内部”的StorageDir都存储在这个路径下，默认在用户空间 Android/data/xxxxxx包名/files/StorageDirs 路径。里面默认有之前的 allRepoParentDir 和 LogData 目录，且这两个目录不能删除
    //废弃，直接存到 Android/包名/files目录即， 不必再新建一层目录，存files没什么缺点，而且还能兼容旧版，何乐而不为？
    //    lateinit var internalStorageDirsParentDir:File  //20240527：禁用，sd相关

    //对用户可见的app工作目录，存储在allRepos目录下
    private lateinit var appDataUnderAllReposDir: File
    private lateinit var fileSnapshotDir: File  //改用：AppModel.getFileSnapshotDir()
    private lateinit var editCacheDir: File
    private lateinit var patchDir: File
    private lateinit var settingsDir: File

    //20240505:这个变量实际上，半废弃了，只在初始化的时候用一下，然后把路径传给MyLog之后，MyLog就自己维护自己的logDir对象了，就不再使用这个变量了
    private lateinit var logDir: File
    private lateinit var submoduleDotGitBackupDir: File

    /**
     * 存储App当前主题，自动，明亮，暗黑，不过这变量好像实际没用到？
     */
    var theme:MutableState<String>? = null

    //外部不应该直接获取此文件，此文件应通过DebugModeManager的setOn/Off方法维护
    private lateinit var debugModeFlagFile:File
    //外部应通过获取此文件来判断是否开启debug模式并通过DebugModeManager的set方法维护此变量和debugModeFlagFile
    //此变量为true，则设置页面的debug模式状态为开启；false则为关闭。注：设置页面的debug模式开启或关闭仅与此变量有关，与debug flag文件是否存在无关。例如：用户打开了debug模式，app创建了debug flag文件，但随后，用户手动删除了flag文件，这时设置页面debug模式仍为开启，直到下次启动app时才会更新为关闭
    @Deprecated("use `DevFlag.isDebugModeOn` instead")
    var debugModeOn = false  //这个变量改成让用户可配置，所以弄成变量，放到init_1里初始化，然后后面的日志之类的会用到它
        private set  //应通过AppModel.DebugModeManager修改debugModeOn的值，那个方法可持久化debug模式开启或关闭，也可临时设置此变量，一举两得



    object DebugModeManager {
        const val debugModeFlagFileName = FlagFileName.enableDebugMode

        //用户在设置页面开启debug模式时，调用此方法，创建flag文件，下次app启动检测到flag文件存在，就会自动开启debug模式了（不过模式开启关闭可直接改AppModel相关变量，并不需要重启app就能立即生效，直接改变量相当于本次有效，创建flag文件相当于把修改持久化了）
        fun setDebugModeOn(requirePersist:Boolean){
            AppModel.debugModeOn = true

            //如果请求持久化，则创建相关文件，否则修改仅针对本次会话有效，一重启app就根据flag文件是否存在重置debugModeOn变量了
            if(requirePersist && !AppModel.isDebugModeFlagFileExists()) {
                AppModel.debugModeFlagFile.createNewFile()
            }
        }

        //用户在设置页面关闭debug模式时，调用此方法，删除flag文件，下次启动就会自动关闭debug模式
        fun setDebugModeOff(requirePersist: Boolean) {
            AppModel.debugModeOn = false

            if(requirePersist && AppModel.isDebugModeFlagFileExists()){
                AppModel.debugModeFlagFile.delete()
            }
        }
    }

    object PuppyGitUnderGitDirManager {
        const val dirName = "PuppyGit"

        fun getDir(gitRepoDotGitDir:String):File {
            val puppyGitUnderGit = File(gitRepoDotGitDir, dirName)
            if(!puppyGitUnderGit.exists()) {
                puppyGitUnderGit.mkdirs()
            }
            return puppyGitUnderGit
        }
    }

//        /**
//         * run before onCreate called, this method do below steps:
//         * init log;
//         * init settings;
//         * update log fields by settings
//         */
//        fun init_0(AppModel: AppModel = singleInstanceHolder){
//            val funName = "init_0"
//
//        }

    /**
     * TODO 实现避免重复执行init的机制，并且如果app崩溃后recreate Activity，确保代码能继续正常工作。
     * TODO 啊，对了，注意appContext这变量肯定是要在recreate Activity后重新赋值的，如果实现避免重入init，需要考虑哪些需要在recreate时重新赋值，哪些不需要
     * 考虑下，要不要为每个init步骤建一个initdone变量，init前检查，如果done为true，就不再执行init，避免重复init，这本来不需要考虑，建比较好，但是，我不确定某些操作是否会失效，例如app崩溃，libgit2加载的证书是否还能用？我不知道，是否需要重新加载lib，我也不知道

    若想避免重复执行init：要么就为AppModel的每个init建初始化flag，要么就为AppModel内部的每个不可重复执行的init函数内部建flag，要么两者都实现。
     */



    /**
     * 执行必须且无法显示界面的操作。
     * 中量级，应该不会阻塞很久
     */
    fun init_1(activityContext:Context, realAppContext:Context, exitApp:()->Unit) {
        val funName = "init_1"

        // run once in app process life time
        // 在app进程生命周期内仅运行一次
        if(inited_1.value.not()) {
            inited_1.value = true
            //加载libgit2等库
            LibLoader.load()

            //必须先初始化libgit2，不然会报segment错误
            Libgit2.init();

            //            LibgitTwo.jniTestAccessExternalStorage()

            //disable dirs owner validation for libgit2, make it support access external storage path like /sdcard or /storage/emulated/storage
            Libgit2.optsGitOptSetOwnerValidation(false)

            //set dbHolder ，如果以后使用依赖注入框架，这个需要修改
            AppModel.dbContainer = AppDataContainer(realAppContext)

        }


        AppModel.deviceWidthHeight = UIHelper.getDeviceWidthHeightInDp(activityContext)

        AppModel.realAppContext = realAppContext

        // every time run after Activity destory and re create

        AppModel.activityContext = activityContext;
//            AppModel.mainActivity = mainActivity  //忘了这个干嘛的了，后来反正没用了，IDE提示什么Activity内存泄漏之类的，所以就注释了

        //设置app工作目录，如果获取不到目录，app无法工作，会在这抛出异常
        val externalFilesDir = getExternalFilesIfErrGetInnerIfStillErrThrowException(activityContext)
        val externalCacheDir = getExternalCacheDirIfErrGetInnerIfStillErrThrowException(activityContext)
        val innerDataDir = getInnerDataDirOrThrowException(activityContext)
        AppModel.externalFilesDir = externalFilesDir
        AppModel.externalCacheDir = externalCacheDir
        AppModel.innerDataDir = innerDataDir


//            AppModel.logDir = createLogDirIfNonexists(externalCacheDir, Cons.defaultLogDirName);

        //20240527：禁用，sd相关 ，开始
//            AppModel.internalStorageDirsParentDir = createDirIfNonexists(externalFilesDir, Cons.defaultInternalStorageDirsParentDirName)

        //设置repodir
//            AppModel.allRepoParentDir = createDirIfNonexists(AppModel.internalStorageDirsParentDir, StorageDirCons.DefaultStorageDir.repoStorage1.name)
//            StorageDirCons.DefaultStorageDir.repoStorage1.fullPath = AppModel.allRepoParentDir.canonicalPath
//
//            //设置对用户可见的app工作目录
//            AppModel.appDataUnderAllReposDir = createDirIfNonexists(AppModel.internalStorageDirsParentDir, StorageDirCons.DefaultStorageDir.puppyGitDataDir.name)
//            StorageDirCons.DefaultStorageDir.puppyGitDataDir.fullPath = AppModel.appDataUnderAllReposDir.canonicalPath
        //20240527：禁用，sd相关 ，结束


        //与sd相关代码互斥，开始
        //设置repodir
        AppModel.allRepoParentDir = createDirIfNonexists(externalFilesDir, Cons.defaultAllRepoParentDirName)
        //test access external storage, passed
//            AppModel.allRepoParentDir = createDirIfNonexists(File("/sdcard"), "puppygit-repos")


        StorageDirCons.DefaultStorageDir.puppyGitRepos.fullPath = AppModel.allRepoParentDir.canonicalPath

        //设置对用户可见的app工作目录
        AppModel.appDataUnderAllReposDir = createDirIfNonexists(AppModel.allRepoParentDir, Cons.defalutPuppyGitDataUnderAllReposDirName)
        //与sd相关代码互斥，结束


        //存放app内置证书的路径
        AppModel.certBundleDir = createDirIfNonexists(AppModel.appDataUnderAllReposDir, CertMan.defaultCertBundleDirName)
        AppModel.certUserDir = createDirIfNonexists(AppModel.appDataUnderAllReposDir, CertMan.defaultCertUserDirName)


        AppModel.fileSnapshotDir = createDirIfNonexists(AppModel.appDataUnderAllReposDir, Cons.defaultFileSnapshotDirName)
        //创建editor cache目录
        AppModel.editCacheDir = createDirIfNonexists(AppModel.appDataUnderAllReposDir, Cons.defaultEditCacheDirName)

        //创建git pathch 导出目录
        AppModel.patchDir = createDirIfNonexists(AppModel.appDataUnderAllReposDir, Cons.defaultPatchDirName)

        //create settings folder
        AppModel.settingsDir = createDirIfNonexists(AppModel.appDataUnderAllReposDir, Cons.defaultSettingsDirName)

        // log dir，必须在初始化log前初始化这个变量
        AppModel.logDir = createDirIfNonexists(AppModel.appDataUnderAllReposDir, Cons.defaultLogDirName)
        AppModel.submoduleDotGitBackupDir = createDirIfNonexists(AppModel.appDataUnderAllReposDir, Cons.defaultSubmoduleDotGitFileBakDirName)

        //设置文件快照目录
//            AppModel.fileSnapshotDir = createFileSnapshotDirIfNonexists(AppModel.allRepoParentDir, Cons.defaultFileSnapshotDirName)

        //设置退出app的函数
        AppModel.exitApp = exitApp

        //debug mode相关变量
        //必须先初始化此变量再去查询isDebugModeOn()
        AppModel.debugModeFlagFile = File(AppModel.appDataUnderAllReposDir, DebugModeManager.debugModeFlagFileName)  //debugMode检测模式是如果在特定目录下存在名为`debugModeFlagFileName`变量值的文件，则debugModeOn，否则off
        //初始化debugModeOn。注：app运行期间若需修改此变量，应通过DebugModeManager来修改；获取则直接通过AppModel.debugModeOn来获取即可
        AppModel.debugModeOn = AppModel.isDebugModeFlagFileExists()  //TODO 在设置页面添加相关选项“开启调试模式”，开启则在上面的目录创建debugModeOn文件，否则删除文件，这样每次启动app就能通过检查文件是否存在来判断是否开了debugMode了。(btw: 因为要在Settings初始化之前就读取到这个变量，所以不能放到Settings里)


        //for test unstable features
        dev_EnableUnTestedFeature = try {
            File(AppModel.appDataUnderAllReposDir, FlagFileName.enableUnTestedFeature).exists()
        }catch (_:Exception) {
            false
        }

    }

    /**
     * 执行必须但已经可以显示界面的操作，所以这时候可以看到开发者设置的loading页面了，如果有的话。
     * 可重可轻，有可能阻塞很久
     */
    suspend fun init_2(editCacheDirPath:String=AppModel.editCacheDir.canonicalPath) {
        val funName = "init_2"
        val applicationContext = AppModel.realAppContext

        // one time task in one time app process life time
        if(inited_2.value.not()) {
            inited_2.value = true
            /*
                init log
             */
            //初始化日志
            //设置 日志保存时间和日志等级，(考虑：以后把这个改成从配置文件读取相关设置项的值，另外，用runBlocking可以实现阻塞调用suspend方法查db，但不推荐)
            //            MyLog.init(saveDays=3, logLevel='w', logDirPath=AppModel.logDir.canonicalPath);
            MyLog.init(
                logKeepDays=PrefMan.getInt(applicationContext, PrefMan.Key.logKeepDays, MyLog.defaultLogKeepDays),
                logLevel=PrefMan.getChar(applicationContext, PrefMan.Key.logLevel, MyLog.defaultLogLevel),
                logDirPath=AppModel.logDir.canonicalPath
            )

            /*
               init settings
             */
            //            val settingsSaveDir = AppModel.innerDataDir  // deprecated, move to use-visible puppygit-data folder
            val settingsSaveDir = AppModel.getOrCreateSettingsDir()

            /*
             * init settings
             * step: try origin settings file first, if failed, try backup file, if failed, remove settings file, create a new settings, it will lost all settings
             */
            //初始化设置项
            try {
                //init settings, it shouldn't blocking long time
                SettingsUtil.init(settingsSaveDir, useBak = false)
            }catch (e:Exception) {
                //用原始设置文件初始化异常
                try {
                    //初始化设置，用备用设置文件，若成功则恢复备用到原始设置文件
                    MyLog.e(TAG, "#$funName init settings err:"+e.stackTraceToString())
                    MyLog.w(TAG, "#$funName init origin settings err, will try use backup")

                    SettingsUtil.init(settingsSaveDir, useBak = true)

                    MyLog.w(TAG, "#$funName init bak settings success, will restore it to origin")

                    SettingsUtil.copyBakToOrigin()  //init成功，所以这里肯定初始化了原始和备用配置文件的File对象，因此不用传参数

                    MyLog.w(TAG, "#$funName restore bak settings to origin success")
                }catch (e2:Exception) {
                    //用备用文件初始化设置也异常，尝试重建设置项，用户设置会丢失
                    MyLog.e(TAG, "#$funName init settings with bak err:"+e2.stackTraceToString())
                    MyLog.w(TAG, "#$funName init bak settings err, will clear origin settings, user settings will lost!")

                    // delete settings files
                    SettingsUtil.delSettingsFile(settingsSaveDir)

                    MyLog.w(TAG, "#$funName del settings success, will reInit settings, if failed, app will not work...")

                    // re init
                    SettingsUtil.init(settingsSaveDir, useBak = false)

                    MyLog.w(TAG, "#$funName reInit settings success")
                }
            }

            val settings = SettingsUtil.getSettingsSnapshot()

            reloadTimeZone(settings)

            //加载证书 for TLS (https
            CertMan.init(applicationContext, AppModel.certBundleDir, AppModel.certUserDir)  //加载app 内嵌证书捆绑包(app cert bundle)
            //加载系统证书，不然jni里c直接访问网络，openssl不知道证书在哪，导致访问https时报ssl verify错误
            //            CertMan.loadSysCerts()  //加载系统证书(改用app内嵌证书了，这个默认不用了，会导致启动很慢

            // now this only for init "know_hosts" for ssh
            Lg2HomeUtils.init(AppModel.appDataUnderAllReposDir, applicationContext)


            //实际上，在20241205之后发布的版本都不会再执行此函数了，改成用主密码了，以后默认密码就写死了，不会再改，版本号也不会再变，自然也不再需要迁移
            //执行会suspend的初始化操作
            //检查是否需要迁移密码
            // 20241206: 添加主密码后，废弃了，只有最初公开发布的前3个版本可能会用到这段代码，没必要留着了，那几个版本估计都没人用，就算有人用，让他们升级新版就完了
//                try {
//                    AppModel.dbContainer.passEncryptRepository.migrateIfNeed(AppModel.dbContainer.credentialRepository)
//                }catch (e:Exception) {
//                    MyLog.e(TAG, "#$funName migrate password err:"+e.stackTraceToString())
//                    MyLog.w(TAG, "#$funName migrate password err, user's password may will be invalid :(")
//                }


            //           // val settingsSaveDir = AppModel.getOrCreateSettingsDir()



            //初始化EditCache
            try {
                val editCacheKeepInDays = settings.editor.editCacheKeepInDays
                //当设置项中启用功能时，进一步检查是否存在disable文件，不存在则启用，否则禁用，这样做是为了方便在实现设置页面前进行测试，直接把功能设为开启，然后通过创建删除disable文件控制实际是否开启，测试很方便
                val editCacheEnable = settings.editor.editCacheEnable || File(AppModel.appDataUnderAllReposDir, FlagFileName.enableEditCache).exists()

                EditCache.init(keepInDays = editCacheKeepInDays, cacheDirPath = editCacheDirPath, enableCache = editCacheEnable)
            }catch (e:Exception) {
                MyLog.e(TAG, "#$funName init EditCache err:"+e.stackTraceToString())
            }



            //初始化SnapshotUtil，例如是否启用文件快照和内容快照之类的
            try {
                SnapshotUtil.init(
                    enableContentSnapshotForEditorInitValue = settings.editor.enableContentSnapshot || File(AppModel.appDataUnderAllReposDir, FlagFileName.enableContentSnapshot).exists(),
                    enableFileSnapshotForEditorInitValue = settings.editor.enableFileSnapshot || File(AppModel.appDataUnderAllReposDir, FlagFileName.enableFileSnapshot).exists(),
                    enableFileSnapshotForDiffInitValue = settings.diff.createSnapShotForOriginFileBeforeSave
                )
            }catch (e:Exception) {
                MyLog.e(TAG, "#$funName init SnapshotUtil err:"+e.stackTraceToString())
            }

            try {
                //clear old settings
                //                val limit = settings.editor.fileOpenHistoryLimit
                //                val requireClearSettingsEditedHistory = settings.editor.filesLastEditPosition.isNotEmpty()
                //                FileOpenHistoryMan.init(limit, requireClearSettingsEditedHistory)

                // no migrate, because settings will move to user-visible puppygit-data dir
                FileOpenHistoryMan.init(
                    saveDir = settingsSaveDir,
                    limit = settings.editor.fileOpenHistoryLimit,
                    requireClearOldSettingsEditedHistory = false
                )
            }catch (e:Exception) {
                MyLog.e(TAG, "#$funName init FileOpenHistoryMan err:"+e.stackTraceToString())
            }

            try {
                //migrate old settings
                //                val oldPaths = settings.storagePaths.ifEmpty { null }
                //                val oldSelectedPath = settings.storagePathLastSelected.ifBlank { null }
                //                StoragePathsMan.init(oldPaths, oldSelectedPath)

                // no migrate, because setting moved
                StoragePathsMan.init(
                    saveDir = settingsSaveDir,
                    oldSettingsStoragePaths = null,
                    oldSettingsLastSelectedPath = null
                )
            }catch (e:Exception) {
                MyLog.e(TAG, "#$funName init StoragePathsMan err:"+e.stackTraceToString())
            }


            doJobThenOffLoading {
                try {
                    //删除过期日志文件
                    MyLog.delExpiredLogs()
                }catch (e:Exception) {
                    MyLog.e(TAG, "#$funName del expired log files err:"+e.stackTraceToString())
                }

                //删除过期的编辑缓存文件
                try {
                    EditCache.delExpiredFiles()
                }catch (e:Exception) {
                    MyLog.e(TAG, "#$funName del expired edit cache files err:"+e.stackTraceToString())
                }

                //删除过期的快照文件
                try {
                    val snapshotKeepInDays = settings.snapshotKeepInDays
                    val snapshotSaveFolder = AppModel.getOrCreateFileSnapshotDir()
                    FsUtils.delFilesOverKeepInDays(snapshotKeepInDays, snapshotSaveFolder, "snapshot folder")

                    //                 //   AppModel.getOrCreateFileSnapshotDir()  // is delete expired files, is not del the folder, so no need call this make sure folder exist
                }catch (e:Exception) {
                    MyLog.e(TAG, "#$funName del expired snapshot files err:"+e.stackTraceToString())
                }
            }




            //根据App版本号执行迁移代码
            AppVersionMan.init migrate@{ oldVer ->
                //如果文件不存在或解析失败或已经是当前最新版本，直接返回true
                if(oldVer == AppVersionMan.err_fileNonExists || oldVer == AppVersionMan.err_parseVersionFailed
                    || oldVer==AppVersionMan.currentVersion
                ) {
                    return@migrate true
                }

                //不是最新版本，执行迁移, if ver==1 do sth, else if ==2, do sth else ... 最好用try...catch包裹，并且将迁移代码设置为幂等的，这样出错可再次重新调用

                if(oldVer < 48 && AppVersionMan.currentVersion >= 48) {
                    val success = AppMigrator.sinceVer48()
                    if(!success) {
                        return@migrate false
                    }
                }

                //这里不应该用else，应该用多个if，而且迁移器应有序执行，越旧版本的越先执行，例如先从47到48，再从48到50，分别执行两个迁移器，若从47升到50，则两个都需要执行

                //如果迁移失败，应在上面的if 里返回 false，但必须确保迁移操作幂等，否则不要轻易返回false

                //迁移成功后返回true
                return@migrate true
            }

        }else {
            //这里放第一次启动app进程会执行，后续再重建Activity依然需要重新执行的代码，
            // 注意：如果并不紧急，可放到if else外面，不过有些操作，例如时区，依赖盘根错节，我不确定都哪里用到了，所以需要在上面的代码准备好相关变量后立即初始化，但重启app后依然需要初始化，于是，为了避免在第一次启动时初始化两次，就写了这个else代码块
            reloadTimeZone(SettingsUtil.getSettingsSnapshot())
        }

        //这里放只要app Activity创建就需要执行的代码



        //初始化与谷歌play的连接，查询支付信息之类的
        //            Billing.init(AppModel.appContext)

        //20240527：禁用，sd相关
        //            StorageDirUtil.init()
//            }

    }

    /**
     * 执行组件相关变量的初始化操作
     * 轻量级，基本可以说不会阻塞
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun init_3(){

        // nav controller, start
        AppModel.navController = rememberNavController()
        //restore nav controller state
        //恢复上次导航状态，如果有的话，不然一旋转屏幕就强制回到顶级页面了，用户体验差
        if(AppModel.lastNavController != null) {
            AppModel.navController.restoreState(AppModel.lastNavController!!.saveState())
        }

        AppModel.lastNavController = AppModel.navController
        // nav controller, end

        AppModel.coroutineScope = rememberCoroutineScope()

//            TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())  //上推隐藏，下拉出现，TopAppBarState 可放到外部以保存状态，如果需要的话
        //TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())  //常驻TopBar，固定显示，不会隐藏
//            AppModel.homeTopBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        AppModel.homeTopBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

        AppModel.haptic = LocalHapticFeedback.current

    }

    fun getAppPackageName(context: Context):String {
        return context.packageName
    }

    fun getAppIcon(context: Context) :ImageBitmap{
        return context.packageManager.getApplicationIcon(getAppPackageName(context)).toBitmap().asImageBitmap()
    }

    fun getAppVersionCode():Int {
        return BuildConfig.VERSION_CODE
    }

    fun getAppVersionName():String {
        return BuildConfig.VERSION_NAME
    }

    //根据资源键名获取值而不是用 R.string.xxx 的id
    fun getStringByResKey(context: Context, resKey: String): String {
        val funName = "getStringByResKey"
        try {
            val res = context.resources
            val resType = "string"  //TODO 需要测试是否支持多语言
            return res.getString(res.getIdentifier(resKey, resType, getAppPackageName(context)))

        }catch (e:Exception) {
            //可能没对应资源之类的
            MyLog.e(TAG, "#$funName err: ${e.stackTraceToString()}")
            return ""
        }
    }

    fun destroyer() {
//            inited_3.value = false
//            inited_2.value = false
//            inited_1.value = false

        //改成在创建完后恢复并保存导航器状态了
//            AppModel.lastNavState = AppModel.navController.saveState()

        AppModel.lastEditFileWhenDestroy.value = AppModel.lastEditFile.value

    }




    // allRepoDir/PuppyGit-Data
    //这个目录虽然是app内部使用的目录，但对用户可见，里面存用户文件快照和日志之类的东西，作用类似电脑上的 user/AppData/Roaming 目录
    fun getOrCreatePuppyGitDataUnderAllReposDir():File{
        if(!appDataUnderAllReposDir.exists()) {
            appDataUnderAllReposDir.mkdirs()
        }
        return appDataUnderAllReposDir
    }

    /**
     * 获取快照目录，因为这个目录在用户对用户可见，万一用户删掉，那在下次重开app之前都无法创建文件快照，所以将获取快照目录设置为一个函数，每次获取时检测，若不存在则创建
     *
     * 不过：如果用户创建与文件夹同名文件，快照机制就作废了，我不准备删除用户创建的文件，如果是其有意为之，可能就是不想使用快照机制，我这里不做处理，顶多每次需要创建快照的时候报个错，反正程序也不会崩。
     *
     * 快照目录路径：allRepoDir/PuppyGit-Data/FileSnapshot, 目前(20240421)仅当文件保存失败且内存中的文件不为空时，会把文件存到这个目录，日后可能扩展使用范围
     */
    // allRepoDir/PuppyGit-Data/FileSnapshot
    fun getOrCreateFileSnapshotDir():File{
        if(!fileSnapshotDir.exists()) {
            fileSnapshotDir.mkdirs()
        }
        return fileSnapshotDir
    }

    fun getOrCreateEditCacheDir():File{
        if(!editCacheDir.exists()) {
            editCacheDir.mkdirs()
        }
        return editCacheDir
    }


    fun getOrCreatePatchDir():File{
        if(!patchDir.exists()) {
            patchDir.mkdirs()
        }
        return patchDir
    }

    fun getOrCreateSettingsDir():File{
        if(!settingsDir.exists()) {
            settingsDir.mkdirs()
        }
        return settingsDir
    }

    // allRepoDir/PuppyGit-Data/Log
    fun getOrCreateLogDir():File {
        if(!logDir.exists()) {
            logDir.mkdirs()
        }
        return logDir
    }

    fun getOrCreateSubmoduleDotGitBackupDir():File {
        if(!submoduleDotGitBackupDir.exists()) {
            submoduleDotGitBackupDir.mkdirs()
        }
        return submoduleDotGitBackupDir
    }

    //这个方法应该仅在初始化时调用一次，以后应通过AppModel.singleInstance.debugModeOn来获取debugMode是否开启，并且通过setDebugModeOn/Off来开启或关闭debug模式
    private fun isDebugModeFlagFileExists():Boolean {
        return debugModeFlagFile.exists()
    }

    fun getOrCreateExternalCacheDir():File{
        if(externalCacheDir.exists().not()) {
            externalCacheDir.mkdirs()
        }

        return externalCacheDir
    }

    fun requireMasterPassword(settings: AppSettings = SettingsUtil.getSettingsSnapshot()):Boolean {
        return (
                //设置了主密码，但没输入
                (settings.masterPasswordHash.isNotEmpty() && masterPassword.value.isEmpty())
                // 用户输入的主密码和密码hash不匹配
            || (settings.masterPasswordHash.isNotEmpty() && masterPassword.value.isNotEmpty() && !HashUtil.verify(masterPassword.value, settings.masterPasswordHash))
        )
    }

    fun masterPasswordEnabled(): Boolean {
        return masterPassword.value.isNotEmpty()
    }



    /**
     * 获取App使用的时区ZoneOffset对象。
     * 注意，这个是App实际使用的时区对象，并不是系统时区对象，若在设置页面为App指定了时区，系统和App使用的时区可能会不同
     *
     * @param settings 一般不用传此参数，只有当更新过AppSettings但不确定SettingsUtil.getSettingsSnapshot()能否立刻获取到最新值时，才有必要传，传的一般是SettingsUtil.update(requireReturnUpdatedSettings=true)的返回值
     */
    fun getAppTimeZoneOffsetCached(settings: AppSettings? = null) : ZoneOffset {
        if(timeZoneOffset == null) {
            reloadTimeZone(settings ?: SettingsUtil.getSettingsSnapshot())
        }

        return timeZoneOffset!!
    }

    fun getAppTimeZoneOffsetInMinutesCached(settings: AppSettings? = null) : Int {
        if(timeZoneOffsetInMinutes == null) {
            reloadTimeZone(settings ?: SettingsUtil.getSettingsSnapshot())
        }

        return timeZoneOffsetInMinutes!!
    }

    fun getAppTimeZoneModeCached(settings: AppSettings? = null) : TimeZoneMode {
        if(timeZoneMode == null) {
            reloadTimeZone(settings ?: SettingsUtil.getSettingsSnapshot())
        }

        return timeZoneMode!!
    }



    /**
     * 更新App时区相关变量，然后返回一个包含新偏移量的对象
     *
     */
    fun reloadTimeZone(settings: AppSettings){
        //更新系统时区分钟数
        systemTimeZoneOffsetInMinutes = try {
            // 这个是有可能负数的，如果是 UTC-7 之类的，就会负数
            getSystemDefaultTimeZoneOffset().totalSeconds / 60
        }catch (e:Exception) {
            MyLog.e(TAG, "#reloadTimeZone() get system timezone offset in minutes err, will use UTC+0, err is: ${e.stackTraceToString()}")
            // offset = 0, 即 UTC+0
            0
        }

        MyLog.d(TAG, "#reloadTimeZone(): new value of systemTimeZoneOffsetInMinutes=$systemTimeZoneOffsetInMinutes")


        //注：这里不能调用getSystemTimeZoneOffsetInMinutesCached，因为如果那个方法如果无结果时会调用此方法查询，若出bug，就死循环了
        //更新App实际使用的时区对象
        timeZoneOffsetInMinutes = readTimeZoneOffsetInMinutesFromSettingsOrDefault(settings, systemTimeZoneOffsetInMinutes!!)
        timeZoneOffset = ZoneOffset.ofTotalSeconds(timeZoneOffsetInMinutes!! * 60)
        timeZoneMode = TimeZoneUtil.getAppTimeZoneMode(settings)

        //打印偏移量，格式："+08:00"
        MyLog.d(TAG, "#reloadTimeZone(): new value of App TimeZone: timeZoneMode=$timeZoneMode, timeZoneOffsetInMinutes=$timeZoneOffsetInMinutes, timeZoneOffset=$timeZoneOffset")

    }

    /**
     * 获取系统时区偏移量，单位分钟，结果会缓存以提高性能
     * @param settings 一般不用传此参数，只有当更新过AppSettings但不确定SettingsUtil.getSettingsSnapshot()能否立刻获取到最新值时，才有必要传，传的一般是SettingsUtil.update(requireReturnUpdatedSettings=true)的返回值
     */
    fun getSystemTimeZoneOffsetInMinutesCached(settings: AppSettings? = null):Int {
      if(systemTimeZoneOffsetInMinutes == null) {
          reloadTimeZone(settings ?: SettingsUtil.getSettingsSnapshot())
      }

      return systemTimeZoneOffsetInMinutes!!
    }
}
