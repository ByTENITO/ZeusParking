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

class SalidaQrParqueadero : AppCompatActivity() {
    private lateinit var CodigoQRParqu: ImageView
    private lateinit var DatosParqu: EditText
    private lateinit var btnGenerarParqu: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        startAnimationsWithDelay()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salida_qr_parqueadero)
        overridePendingTransition(0, 0)

        CodigoQRParqu = findViewById(R.id.CodigoSalidaParqu)
        DatosParqu = findViewById(R.id.etentradaParqu)
        btnGenerarParqu = findViewById(R.id.btnSalidaParqu)

        btnGenerarParqu.setOnClickListener(View.OnClickListener {
            try {
                val barcodeEncoder = BarcodeEncoder()
                val bitmap: Bitmap = barcodeEncoder.encodeBitmap(
                    DatosParqu.text.toString(),
                    BarcodeFormat.QR_CODE,
                    750,
                    750
                )

                CodigoQRParqu.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            listOf(
                CodigoQRParqu,
                DatosParqu,
                btnGenerarParqu
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 0) // Ajusta el tiempo de retraso si es necesario
    }
}
