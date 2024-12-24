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

class EntradaQrParqueadero : AppCompatActivity() {
    private lateinit var ivCodigoQR: ImageView
    private lateinit var etDatos: EditText
    private lateinit var btnGenerar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startAnimationsWithDelay()
        setContentView(R.layout.activity_entrada_qr_parqueadero)
        overridePendingTransition( 0,0)
        ivCodigoQR = findViewById(R.id.CodigoSalidaParqu)
        etDatos = findViewById(R.id.etentradaParqu)
        btnGenerar = findViewById(R.id.btnSalidaParqu)

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
        }, 1) // Ajusta el tiempo de retraso si es necesario
    }
}
