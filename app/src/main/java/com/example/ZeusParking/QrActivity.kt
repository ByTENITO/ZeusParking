package com.example.parquiatenov10

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
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

        //Menu de Navegaion
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNavigationView.selectedItemId) {
                return@setOnItemSelectedListener true  // Evita recargar la misma actividad
            }

            when (item.itemId) {

                R.id.home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)  // Evita la animación de transición
                    finish()  // Finaliza la actividad actual para evitar que quede en la pila
                }
                R.id.localizacion -> {
                    startActivity(Intent(this, Localizacion::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.registro -> {
                    startActivity(Intent(this, RegistrarBiciActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.qr -> {
                    startActivity(Intent(this, QrActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
            }
            true
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
        }, 0)
    }

}
