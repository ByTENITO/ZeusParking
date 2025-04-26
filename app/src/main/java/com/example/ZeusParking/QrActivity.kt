package com.example.parquiatenov10

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.example.ZeusParking.BaseNavigationActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QrActivity : BaseNavigationActivity() {
    private lateinit var ivCodigoQR: ImageView
    private lateinit var tvDateTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada_qr)


        ivCodigoQR = findViewById(R.id.ivCodigoSalida)
        tvDateTime = findViewById(R.id.tvDateTime)


        setupNavigation()
        generateAndDisplayQrCode()
        updateDateTime()
        startAnimationsWithDelay()
    }

    private fun updateDateTime() {
        val sdf = SimpleDateFormat("d/MMMM/yyyy - HH:mm:ss", Locale("es", "ES"))
        val currentDateTime = sdf.format(Date())
        tvDateTime.text = currentDateTime
    }

    private fun generateAndDisplayQrCode() {
        val sharedPref = getSharedPreferences("MisDatos", MODE_PRIVATE)
        val correo = sharedPref.getString("nombreUsuario", "Desconocido") ?: "DefaultValue"

        try {
            val qrBitmap = generateQRCode(correo, 700)
            ivCodigoQR.setImageBitmap(qrBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateQRCode(content: String, size: Int): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.ERROR_CORRECTION to "L"
            )

            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
    }

    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            ivCodigoQR.startAnimation(fadeIn)
        }, 100)
    }

    override fun getCurrentNavigationItem(): Int {
        return R.id.qr
    }
}