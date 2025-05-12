package com.example.parquiatenov10

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.ZeusParking.BaseNavigationActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QrActivity : BaseNavigationActivity() {
    private var database = FirebaseFirestore.getInstance()
    private lateinit var ivCodigoQR: ImageView
    private lateinit var tvDateTime: TextView
    private lateinit var marcoNum: EditText
    private lateinit var tiposSpinner: Spinner
    private lateinit var reserva: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada_qr)

        //Responsividad
        Responsividad.inicializar(this)

        ivCodigoQR = findViewById(R.id.ivCodigoSalida)
        tvDateTime = findViewById(R.id.tvDateTime)
        tiposSpinner = findViewById(R.id.Tipos_SpinnerQR)
        reserva = findViewById(R.id.Reserva_BTN)
        marcoNum = findViewById(R.id.numero)

        val adapter = ArrayAdapter.createFromResource(this, R.array.items, R.layout.estilo_spinner)
        adapter.setDropDownViewResource(R.layout.estilo_spinner)
        tiposSpinner.adapter = adapter

        tiposSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    1, 2 -> {
                        marcoNum.hint = "4 Ultimos Numeros"
                        marcoNum.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(20))
                    }

                    3, 4, 5 -> {
                        marcoNum.hint = when (position) {
                            3 -> "Placa (Ej. abc123)"
                            4 -> "Placa (Ej. abc123)"
                            5 -> "Número de Furgón"
                            else -> "Número de Marco"
                        }
                        marcoNum.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(7))
                    }

                    else -> {
                        marcoNum.hint = ""
                        marcoNum.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(20))
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        setupNavigation()
        generateAndDisplayQrCode()
        updateDateTime()
        startAnimationsWithDelay()

        reserva.setOnClickListener {
            val tipoVehi = tiposSpinner.selectedItem.toString()
            val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val numero = marcoNum.text.toString()
            if (tipoVehi == "Tipo de Vehiculo" || userId.isEmpty() || numero == "" || numero.isEmpty()) {
                Toast.makeText(
                    this,
                    "Debe llenar todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                database.collection("Bici_Usuarios")
                    .whereEqualTo("id", userId)
                    .whereEqualTo("tipo", tipoVehi)
                    .whereEqualTo("numero",numero)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            database.collection("Reservas")
                                .whereEqualTo("id", userId)
                                .get()
                                .addOnSuccessListener { documents ->
                                    if (documents.isEmpty) {
                                        val intent = Intent(this, Reservacion::class.java).apply {
                                            putExtra("ID", userId)
                                            putExtra("Tipo", tipoVehi)
                                            putExtra("numero",numero)
                                            Log.d("tipo de vehiculo", tipoVehi)
                                        }
                                        startActivity(intent)
                                    } else {
                                        Toast.makeText(
                                            this,
                                            "La reserva ya fue realizada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "No hay registro de este vehiculo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
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