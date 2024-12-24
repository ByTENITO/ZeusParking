package com.example.parquiatenov10

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.lang.Exception

class SalidaQrActivity : AppCompatActivity() {
    private lateinit var ivCodigoQR: ImageView
    private lateinit var etDatos: EditText
    private lateinit var btnGenerar: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        startAnimationsWithDelay()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salida_qr)
        overridePendingTransition(0, 0)

        ivCodigoQR = findViewById(R.id.ivCodigoSalida)
        etDatos = findViewById(R.id.etentrada)
        btnGenerar = findViewById(R.id.btnSalida)

        btnGenerar.setOnClickListener(View.OnClickListener {
            try {
                val barcodeEncoder = BarcodeEncoder()
                val bitmap: Bitmap = barcodeEncoder.encodeBitmap(
                    etDatos.text.toString(),
                    BarcodeFormat.QR_CODE,
                    750,
                    750
                )

                ivCodigoQR.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            listOf(
                ivCodigoQR,
                etDatos,
                btnGenerar
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 0) // Ajusta el tiempo de retraso si es necesario
    }
}
