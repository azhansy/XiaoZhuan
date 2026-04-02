package com.xigong.xiaozhuan.util

import java.io.File
import javax.imageio.ImageIO

data class ImageSize(val width: Int, val height: Int)

fun getImageSize(file: File): ImageSize? {
    val image = ImageIO.read(file) ?: return null
    return ImageSize(image.width, image.height)
}

fun validateScreenshotFiles(
    files: List<File>,
    minCount: Int,
    maxCount: Int,
    maxBytes: Long,
    requireSize: ImageSize,
    allowedExtensions: Set<String>
) {
    if (files.isEmpty()) return
    require(files.size in minCount..maxCount) {
        "截图数量需为${minCount}-${maxCount}张"
    }
    files.forEach { file ->
        require(file.exists()) { "截图文件不存在: ${file.absolutePath}" }
        val ext = file.extension.lowercase()
        require(ext in allowedExtensions) { "截图仅支持${allowedExtensions.joinToString("/")}格式: ${file.name}" }
        require(file.length() <= maxBytes) { "截图大小超过限制: ${file.name}" }
        val size = getImageSize(file) ?: error("无法读取图片尺寸: ${file.name}")
        require(size.width == requireSize.width && size.height == requireSize.height) {
            "截图尺寸需为${requireSize.width}x${requireSize.height}: ${file.name}"
        }
    }
}
