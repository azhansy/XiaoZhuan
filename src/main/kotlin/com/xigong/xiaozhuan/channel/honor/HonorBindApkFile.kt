package com.xigong.xiaozhuan.channel.honor

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HonorBindApkFile(
    @Json(name = "bindingFileList")
    val items: List<Item>,
){
    data class Item(
        @Json(name = "objectId")
        val objectId: Long,
        @Json(name = "languageId")
        val languageId: String? = null,
        @Json(name = "order")
        val order: Int? = null,
    )
}