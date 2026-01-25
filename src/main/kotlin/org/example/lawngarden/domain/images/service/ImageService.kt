package org.example.lawngarden.domain.images.service

import net.coobird.thumbnailator.Thumbnails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDate
import java.util.UUID

@Service
class ImageService(
    @Value("\${image.path}") private val uploadPath: String
) {

    fun upload(file: MultipartFile) : String? {
        if (file.isEmpty) return null

        val dir = File(uploadPath)
        if(!dir.exists()) {
            dir.mkdirs()
        }

        val fileName = "${LocalDate.now()}_${UUID.randomUUID()}.jpg"
        val targetFile = File(uploadPath, fileName)

        Thumbnails.of(file.inputStream)
            .size(1280, 720) // 최대 해상도 제한
            .outputQuality(0.8) // 화질 80%로 압축
            .toFile(targetFile)

        return fileName
    }
}