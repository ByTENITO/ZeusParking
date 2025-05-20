package com.example.parquiatenov10

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.ZeusParking.BaseNavigationActivity
import com.google.firebase.auth.FirebaseAuth
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
    private lateinit var btnGoToReserva: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada_qr)

        // Instrucciones de QR
        mostrarInstruccionesQR()

        // Responsividad
        Responsividad.inicializar(this)

        ivCodigoQR = findViewById(R.id.ivCodigoSalida)
        tvDateTime = findViewById(R.id.tvDateTime)
        btnGoToReserva = findViewById(R.id.irReserva)

        setupNavigation()
        generateAndDisplayQrCode()
        updateDateTime()
        startAnimationsWithDelay()

        btnGoToReserva.setOnClickListener {
            iraReserva()
        }
    }

    private fun iraReserva() {
        val intent = Intent(this, Registrar_Reserva::class.java)
        startActivity(intent)
    }

    private fun updateDateTime() {
        val sdf = SimpleDateFormat("d/MMMM/yyyy - HH:mm", Locale("es", "ES"))
        val currentDateTime = sdf.format(Date())
        tvDateTime.text = currentDateTime
    }

    private fun generateAndDisplayQrCode() {
        val correo = FirebaseAuth.getInstance().currentUser?.email.toString()

        try {
            val qrBitmap = generateQRCode(correo, 700)
            ivCodigoQR.setImageBitmap(qrBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al generar QR", Toast.LENGTH_SHORT).show()
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

    private fun mostrarInstruccionesQR() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Instrucciones de Uso del QR")
        builder.setMessage("""
        1. Este QR es personal e intransferible
        2. Con él podrás entrar y salir del parqueadero
        3. Presenta este QR al ingresar al parqueadero
        """.trimIndent())
        builder.setPositiveButton("Entendido", null)
        builder.show()
    }

    override fun getCurrentNavigationItem(): Int {
        return R.id.qr
    }
}