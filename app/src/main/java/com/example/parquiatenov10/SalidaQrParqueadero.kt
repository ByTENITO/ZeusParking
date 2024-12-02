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

class SalidaQrParqueadero : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salida_qr_parqueadero)

        val CodigoQRParqu: ImageView = findViewById(R.id.CodigoSalidaParqu)
        val DatosParqu: EditText = findViewById(R.id.etentradaParqu)
        val btnGenerarParqu: Button = findViewById(R.id.btnSalidaParqu)

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
}
