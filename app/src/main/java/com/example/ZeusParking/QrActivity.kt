package com.example.parquiatenov10

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

class QrActivity : AppCompatActivity() {
    private lateinit var CodSalida: ImageView
    private lateinit var Salida: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startAnimationsWithDelay()
        setContentView(R.layout.activity_entrada_qr)
        CodSalida = findViewById(R.id.ivCodigoSalida)
        Salida = findViewById(R.id.Buttonsalir)
        overridePendingTransition(0, 0)
        val ivCodigoQR: ImageView = findViewById(R.id.ivCodigoSalida)
        val sharedPref = getSharedPreferences("MisDatos", MODE_PRIVATE)
        val correo = sharedPref.getString("nombreUsuario", "Desconocido")
        //val barcodeEncoder = BarcodeEncoder()
        //val bitmap: Bitmap = barcodeEncoder.encodeBitmap(correo.toString(), BarcodeFormat.QR_CODE, 750, 750)
        //try {
        //    ivCodigoQR.setImageBitmap(bitmap)
        //} catch (e: Exception) {
        //    e.printStackTrace()
        //}
        ivCodigoQR.setBackgroundColor(Color.TRANSPARENT)
        ivCodigoQR.setImageBitmap(generateQRCodeTransparent(correo, 750))
        Salida.setOnClickListener {
            finish()
        }
    }

    private fun generateQRCodeTransparent(qr: String?, size: Int): Bitmap? {
        try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 0 // Eliminar márgenes
            )
            val QR = MultiFormatWriter().encode(qr, BarcodeFormat.QR_CODE, size, size, hints)
            val width = QR.width
            val height = QR.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (QR[x, y]) Color.YELLOW else Color.TRANSPARENT)
                }
            }

            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return null
    }

    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            listOf(
                CodSalida,
                Salida
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 0) // Ajusta el tiempo de retraso si es necesario
    }

}
