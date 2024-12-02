package com.example.parquiatenov10

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.lang.Exception

class EntradaQrParqueadero : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada_qr_parqueadero)

        val ivCodigoQR: ImageView = findViewById(R.id.CodigoSalidaParqu)
        val etDatos: EditText = findViewById(R.id.etentradaParqu)
        val btnGenerar: Button = findViewById(R.id.btnSalidaParqu)

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
}
