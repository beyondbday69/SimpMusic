package com.maxrave.simpmusic

import android.annotation.SuppressLint
import android.app.Application
import android.database.CursorWindow
import android.os.Build
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import cat.ereza.customactivityoncrash.config.CaocConfig
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.crossfade
import com.maxrave.common.AppIdentity
import com.maxrave.data.di.loader.loadAllModules
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.di.viewModelModule
import com.maxrave.simpmusic.service.backup.AutoBackupScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import multiplatform.network.cmptoast.AppContext
import okhttp3.OkHttpClient
import okio.FileSystem
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.simpmusic.crashlytics.configCrashlytics
import org.simpmusic.lastfm.configLastfm
import java.lang.reflect.Field

class SimpMusicApplication :
    Application(),
    KoinComponent,
    SingletonImageLoader.Factory {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val dataStoreManager: DataStoreManager by inject()
    private lateinit var autoBackupScheduler: AutoBackupScheduler

    override fun onCreate() {
        super.onCreate()
        if (BuildKonfig.sentryDsn.isNotEmpty()) {
            try {
                configCrashlytics(this, BuildKonfig.sentryDsn)
            } catch (e: Throwable) {
                Logger.e("SimpMusicApplication", "Crashlytics init failed: ${e.message}")
            }
        }
        if (BuildKonfig.lastfmApiKey.isNotEmpty() && BuildKonfig.lastfmSecret.isNotEmpty()) {
            try {
                configLastfm(BuildKonfig.lastfmApiKey, BuildKonfig.lastfmSecret)
            } catch (e: Throwable) {
                Logger.e("SimpMusicApplication", "Lastfm init failed: ${e.message}")
            }
        }
        startKoin {
            androidLogger(level = Level.INFO)
            androidContext(this@SimpMusicApplication)
            loadAllModules(
                AppIdentity(
                    applicationId = BuildConfig.APPLICATION_ID,
                    versionName = BuildConfig.VERSION_NAME,
                    platform = "Android ${Build.VERSION.RELEASE}",
                ),
            )
            loadKoinModules(viewModelModule)
        }
        // provide custom configuration
        val workConfig =
            Configuration
                .Builder()
                .setMinimumLoggingLevel(Log.INFO)
                .build()

        // initialize WorkManager safely
        try {
            if (!WorkManager.isInitialized()) {
                WorkManager.initialize(this, workConfig)
            }
        } catch (e: Throwable) {
            Logger.e("SimpMusicApplication", "WorkManager init failed: ${e.message}")
        }

        // Initialize and start AutoBackupScheduler
        try {
            autoBackupScheduler = AutoBackupScheduler(this, dataStoreManager)
            applicationScope.launch {
                autoBackupScheduler.observeAndSchedule()
            }
        } catch (e: Throwable) {
            Logger.e("SimpMusicApplication", "AutoBackupScheduler init failed: ${e.message}")
        }

        CaocConfig.Builder
            .create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM) // default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) // default: true
            .showErrorDetails(true) // default: true
            .showRestartButton(true) // default: true
            .errorDrawable(R.mipmap.ic_launcher_round)
            .logErrorOnRestart(true) // default: true
            .trackActivities(true) // default: false
            .minTimeBetweenCrashesMs(2000) // default: 3000 //default: bug image
            .restartActivity(MainActivity::class.java) // default: null (your app's launch activity)
            .apply()

        try {
            @SuppressLint("DiscouragedPrivateApi")
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            val expectSize = 100 * 1024 * 1024
            field.set(null, expectSize)
        } catch (e: Throwable) {
            Logger.w("SimpMusicApplication", "CursorWindow sCursorWindowSize reflection unavailable on this Android version: ${e.message}")
        }

        try {
            AppContext.apply {
                set(applicationContext)
            }
        } catch (e: Throwable) {
            Logger.e("SimpMusicApplication", "AppContext init failed: ${e.message}")
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        Logger.w("Terminate", "Checking")
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader
            .Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            OkHttpClient()
                        },
                    ),
                )
            }.diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCache(
                DiskCache
                    .Builder()
                    .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build(),
            ).crossfade(true)
            .build()
}