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

class EntradaQrActivity : AppCompatActivity() {
    private lateinit var CodSalida:ImageView
    private lateinit var entrada:EditText
    private lateinit var Salida:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startAnimationsWithDelay()
        setContentView(R.layout.activity_entrada_qr)
        CodSalida = findViewById(R.id.ivCodigoSalida)
        entrada = findViewById(R.id.etentrada)
        Salida = findViewById(R.id.btnSalida)
        overridePendingTransition( 0,0)
        val ivCodigoQR: ImageView = findViewById(R.id.ivCodigoSalida)
        val etDatos: EditText = findViewById(R.id.etentrada)
        val btnGenerar: Button = findViewById(R.id.btnSalida)

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
                CodSalida,
                entrada,
                Salida
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 0) // Ajusta el tiempo de retraso si es necesario
    }
}
