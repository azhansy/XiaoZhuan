package com.xigong.xiaozhuan.channel.huawei

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class HWRefreshApk(
    @Json(name = "lang")
    val lang: String? = null,
    @Json(name = "imgShowType")
    val imgShowType: Int? = null,
    @Json(name = "fileType")
    val fileType: Int = 5,
    val files: List<FileInfo>
) {
    @JsonClass(generateAdapter = false)
    data class FileInfo(
        @Json(name = "fileName")
        val fileName: String,
        @Json(name = "fileDestUrl")
        val fileDestUrl: String
    )
}