package com.example.wardenchat.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeUtil {
    fun generateQrBitmap(content: String, size: Int = 768): ImageBitmap {
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
            put(EncodeHintType.MARGIN, 1)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
        }

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        return bmp.asImageBitmap()
    }
}
