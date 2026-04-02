package com.xigong.xiaozhuan.channel.vivo

import com.xigong.xiaozhuan.channel.VersionParams
import com.xigong.xiaozhuan.log.AppLogger
import com.xigong.xiaozhuan.log.action
import com.xigong.xiaozhuan.util.ApkInfo
import com.xigong.xiaozhuan.util.ImageSize
import com.xigong.xiaozhuan.util.validateScreenshotFiles
import java.io.File
import kotlin.math.roundToInt

class VIVOMarketClient(
    accessKey: String,
    accessSecret: String,
) {
    private val marketApi = VIVOMarketApi(accessKey, accessSecret)

    suspend fun submit(
        file: File, apkInfo: ApkInfo, versionParams: VersionParams, progress: (Int) -> Unit
    ) = AppLogger.action(LOG_TAG, "提交新版本") {
        val appInfo = getAppInfo(apkInfo.applicationId)
        val apkResult = uploadApk(file, apkInfo, progress)
        val screenshotSerials = uploadScreenshots(apkInfo, versionParams)
        performSubmit(apkResult, versionParams, appInfo, screenshotSerials)
    }

    suspend fun getAppInfo(
        applicationId: String
    ): VIVOAppInfo = AppLogger.action(LOG_TAG, "获取App信息") {
        marketApi.getAppInfo(applicationId)
    }

    private suspend fun uploadApk(
        file: File, apkInfo: ApkInfo, progress: (Int) -> Unit
    ): VIVOApkResult = AppLogger.action(LOG_TAG, "上传Apk文件") {
        marketApi.uploadApk(file, apkInfo.applicationId) {
            progress((it * 100).roundToInt())
        }
    }

    private suspend fun uploadScreenshots(
        apkInfo: ApkInfo,
        versionParams: VersionParams
    ): List<String> {
        val screenshots = versionParams.screenshots
        if (screenshots.isEmpty()) return emptyList()
        validateScreenshotFiles(
            screenshots,
            minCount = 3,
            maxCount = 5,
            maxBytes = 2L * 1024 * 1024,
            requireSize = ImageSize(1080, 1920),
            allowedExtensions = setOf("jpg", "jpeg", "png")
        )
        return screenshots.map { file -> marketApi.uploadScreenshot(file, apkInfo.applicationId) }
    }

    private suspend fun performSubmit(
        apkResult: VIVOApkResult,
        versionParams: VersionParams,
        appInfo: VIVOAppInfo,
        screenshots: List<String>
    ): Unit = AppLogger.action(LOG_TAG, "提交审核") {
        marketApi.submit(apkResult, versionParams, appInfo, screenshots)

    }

    companion object {
        private const val LOG_TAG = "VIVO应用市场Api"
    }
}