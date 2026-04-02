package com.xigong.xiaozhuan.channel.oppo

import com.xigong.xiaozhuan.channel.VersionParams
import com.xigong.xiaozhuan.log.AppLogger
import com.xigong.xiaozhuan.log.action
import com.xigong.xiaozhuan.util.ApkInfo
import com.xigong.xiaozhuan.util.ImageSize
import com.xigong.xiaozhuan.util.validateScreenshotFiles
import java.io.File
import kotlin.math.roundToInt

class OPPOMarketClient(
    clientId: String,
    clientSecret: String,
) {
    private val marketApi = OPPOMaretApi(clientId, clientSecret)

    suspend fun submit(
        file: File, apkInfo: ApkInfo, versionParams: VersionParams, progress: (Int) -> Unit
    ): Unit = AppLogger.action(LOG_TAG, "提交新版本") {
        val token = getToken()
        val appInfo = getAppInfo(token, apkInfo.applicationId)
        val uploadUrl = getUploadUrl(token)
        val apkResult = uploadApk(uploadUrl, token, file, progress)
        val screenshotUrls = uploadScreenshots(token, versionParams)
        performSubmit(token, apkInfo, appInfo, versionParams, apkResult, screenshotUrls)
    }

    suspend fun getAppInfo(
        applicationId: String
    ): OPPOAppInfo = AppLogger.action(LOG_TAG, "获取审核状态") {
        val token = getToken()
        getAppInfo(token, applicationId)
    }

    private suspend fun getToken(): String = AppLogger.action(LOG_TAG, "获取token") {
        marketApi.getToken()
    }

    private suspend fun getAppInfo(
        token: String, applicationId: String
    ): OPPOAppInfo = AppLogger.action(LOG_TAG, "获取App信息") {
        marketApi.getAppInfo(token, applicationId)
    }

    private suspend fun getUploadUrl(
        token: String
    ): OPPOUploadUrl = AppLogger.action(LOG_TAG, "获取Apk上传地址") {
        marketApi.getUploadUrl(token)
    }


    private suspend fun uploadApk(
        uploadUrl: OPPOUploadUrl, token: String, file: File, progress: (Int) -> Unit
    ): OPPOApkResult = AppLogger.action(LOG_TAG, "上传Apk文件") {
        marketApi.uploadApk(uploadUrl, token, file) {
            progress((it * 100).roundToInt())
        }
    }

    private suspend fun performSubmit(
        token: String,
        apkInfo: ApkInfo,
        appInfo: OPPOAppInfo,
        versionParams: VersionParams,
        apkResult: OPPOApkResult,
        screenshotUrls: List<String>
    ): Unit = AppLogger.action(LOG_TAG, "提交审核") {
        marketApi.submit(token, apkInfo, appInfo, versionParams, apkResult, screenshotUrls)
    }

    private suspend fun uploadScreenshots(
        token: String,
        versionParams: VersionParams
    ): List<String> {
        val screenshots = versionParams.screenshots
        if (screenshots.isEmpty()) return emptyList()
        validateScreenshotFiles(
            screenshots,
            minCount = 3,
            maxCount = 5,
            maxBytes = 5L * 1024 * 1024,
            requireSize = ImageSize(1080, 1920),
            allowedExtensions = setOf("jpg", "jpeg", "png")
        )
        val uploadUrl = getUploadUrl(token)
        return screenshots.map { file ->
            marketApi.uploadScreenshot(uploadUrl, token, file).url
        }
    }

    companion object {
        private const val LOG_TAG = "OPPO应用市场Api"
    }
}