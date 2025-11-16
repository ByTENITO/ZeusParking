package com.example.parquiatenov10

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.ZeusParking.BaseNavigationActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.drawable.GradientDrawable
import android.graphics.Typeface
import android.view.View

class QrActivity : BaseNavigationActivity() {
    private lateinit var ivCodigoQR: ImageView
    private lateinit var tvDateTime: TextView
    private lateinit var btnGoToReserva: Button
    private lateinit var layoutQrContainer: com.google.android.material.card.MaterialCardView
    private lateinit var layoutEmptyState: com.google.android.material.card.MaterialCardView
    private lateinit var btnTryAgain: Button
    private lateinit var tvEmptyMessage: TextView
    private var database = FirebaseFirestore.getInstance()
    private var qrGenerado = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada_qr)

        Log.d("QrActivity", "onCreate iniciado")

        // Instrucciones de QR (comentado temporalmente para debug)
        // mostrarInstruccionesQR()

        // Responsividad
        Responsividad.inicializar(this)

        inicializarVistas()
        setupNavigation()
        updateDateTime()
        startAnimationsWithDelay()

        // Cargar vehículos del usuario y generar QR
        cargarVehiculosYGenerarQR()

        btnGoToReserva.setOnClickListener {
            iraReserva()
        }

        Log.d("QrActivity", "onCreate completado")
    }

    private fun inicializarVistas() {
        try {
            ivCodigoQR = findViewById(R.id.ivCodigoSalida)
            tvDateTime = findViewById(R.id.tvDateTime)
            btnGoToReserva = findViewById(R.id.irReserva)
            layoutQrContainer = findViewById(R.id.qrCard)
            layoutEmptyState = findViewById(R.id.layoutEmptyState)
            btnTryAgain = findViewById(R.id.btnTryAgain)
            tvEmptyMessage = findViewById(R.id.tvEmptyMessage)

            // Configurar botón de reintento
            btnTryAgain.setOnClickListener {
                Log.d("QrActivity", "Botón reintento presionado")
                cargarVehiculosYGenerarQR()
            }

            // Mostrar estado vacío inicialmente
            mostrarEstadoVacio("Cargando vehículos...")

            Log.d("QrActivity", "Vistas inicializadas correctamente")
        } catch (e: Exception) {
            Log.e("QrActivity", "Error al inicializar vistas: ${e.message}")
            Toast.makeText(this, "Error al inicializar la actividad", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarVehiculosYGenerarQR() {
        val correo = FirebaseAuth.getInstance().currentUser?.email

        if (correo.isNullOrEmpty()) {
            mostrarError("No se pudo obtener el correo del usuario")
            return
        }

        Log.d("QrActivity", "Buscando vehículos para: $correo")

        // Primero verificar si tiene reservas activas
        verificarReservasActivas(correo)
    }

    private fun verificarReservasActivas(correo: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId.isNullOrEmpty()) {
            mostrarError("No se pudo obtener el ID del usuario")
            return
        }

        database.collection("Reservas")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No tiene reservas activas, mostrar botón de reserva
                    mostrarBotonReserva(true)
                    Log.d("QrActivity", "No hay reservas activas, verificando vehículos en parqueadero")
                    verificarVehiculosEnParqueadero(correo)
                } else {
                    // Tiene reservas activas, ocultar botón de reserva
                    mostrarBotonReserva(false)
                    mostrarErrorReservaActiva(documents)
                }
            }
            .addOnFailureListener { e ->
                Log.e("QrActivity", "Error al verificar reservas: ${e.message}")
                // En caso de error, mostrar botón por defecto
                mostrarBotonReserva(true)
                verificarVehiculosEnParqueadero(correo)
            }
    }
    private fun mostrarBotonReserva(mostrar: Boolean) {
        runOnUiThread {
            if (mostrar) {
                btnGoToReserva.visibility = View.VISIBLE
                Log.d("QrActivity", "Botón de reserva: VISIBLE")
            } else {
                btnGoToReserva.visibility = View.GONE
                Log.d("QrActivity", "Botón de reserva: OCULTO")
            }
        }
    }

    private fun mostrarErrorReservaActiva(documents: com.google.firebase.firestore.QuerySnapshot) {
        val reservasInfo = StringBuilder()
        reservasInfo.append("Tienes reservas activas:\n\n")

        for (document in documents) {
            val tipo = document.getString("tipo") ?: "Vehículo"
            val numero = document.getString("numero") ?: "Sin número"
            val horaReserva = document.getString("horaReserva") ?: "Hora no especificada"

            reservasInfo.append("• $tipo - ${numero.uppercase()}\n")
            reservasInfo.append("  Hora: $horaReserva\n\n")
        }

        reservasInfo.append("Debes cancelar tu reserva activa antes de generar un nuevo QR de entrada.")

        mostrarEstadoVacio(reservasInfo.toString())

        // Mostrar Toast adicional
        showSweetToast("Tienes reservas activas. Cancélalas primero.", false)
    }

    // El resto del código se mantiene igual...
    private fun verificarVehiculosEnParqueadero(correo: String) {
        database.collection("Entradas")
            .whereEqualTo("correoUsuario", correo)
            .whereEqualTo("estado", "activo")
            .get()
            .addOnSuccessListener { entradas ->
                Log.d("QrActivity", "Verificación parqueadero: ${entradas.size()} entradas activas")
                if (!entradas.isEmpty) {
                    // Tiene vehículo(s) dentro del parqueadero
                    mostrarErrorVehiculoEnParqueadero(entradas.size())
                } else {
                    // No tiene vehículos dentro, puede generar QR
                    cargarVehiculosDisponibles(correo)
                }
            }
            .addOnFailureListener { e ->
                Log.e("QrActivity", "Error al verificar vehículos en parqueadero: ${e.message}")
                // En caso de error, intentar cargar vehículos de todos modos
                cargarVehiculosDisponibles(correo)
            }
    }

    private fun cargarVehiculosDisponibles(correo: String) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("QrActivity", "Vehículos encontrados: ${documents.size()}")

                if (documents.isEmpty) {
                    mostrarEstadoVacio("No tienes vehículos registrados")
                    return@addOnSuccessListener
                }

                // Mostrar diálogo para seleccionar vehículo si hay más de uno
                if (documents.size() > 1) {
                    mostrarSeleccionVehiculoElegante(documents)
                } else {
                    // Solo tiene un vehículo, generar QR con ese ID
                    val document = documents.documents[0]
                    val vehiculoId = document.id
                    val tipo = document.getString("tipo") ?: "Vehículo"
                    val numero = document.getString("numero") ?: "Sin número"

                    generarYMostrarQR(vehiculoId, tipo, numero)
                }
            }
            .addOnFailureListener { e ->
                mostrarError("Error al cargar vehículos: ${e.message}")
                Log.e("QrActivity", "Error al cargar vehículos: ${e.message}")
            }
    }

    private fun mostrarErrorVehiculoEnParqueadero(cantidad: Int) {
        mostrarEstadoVacio(
            "Tienes $cantidad vehículo(s) actualmente en el parqueadero.\n\n" +
                    "Debes salir con el vehículo actual antes de generar un nuevo QR."
        )

        showSweetToast("Vehículo en parqueadero. Debes salir primero.", false)
    }

    private fun mostrarSeleccionVehiculoElegante(documents: com.google.firebase.firestore.QuerySnapshot) {
        val vehiculos = mutableListOf<Triple<String, String, String>>()

        for (document in documents) {
            val tipo = document.getString("tipo") ?: "Vehículo"
            val numero = document.getString("numero") ?: "Sin número"
            vehiculos.add(Triple(document.id, tipo, numero))
        }

        val dialog = AlertDialog.Builder(this).create()

        // Crear layout principal
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 20)
            background = createRoundedDrawable(ContextCompat.getColor(this@QrActivity, R.color.Blanco), 25f)
        }

        // Título
        val titleView = TextView(this).apply {
            text = "Seleccionar Vehículo"
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@QrActivity, R.color.Principal))
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 20)
        }

        // Mensaje
        val messageView = TextView(this).apply {
            text = "Selecciona el vehículo que vas a usar:"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@QrActivity, R.color.Negro))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 10)
        }

        // Crear lista personalizada
        val scrollView = ScrollView(this)
        val listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 10, 10, 10)
        }

        vehiculos.forEachIndexed { index, vehiculo ->
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(20, 15, 20, 15)
                background = createRoundedDrawable(ContextCompat.getColor(this@QrActivity, R.color.Texto_pastel), 12f)
                setOnClickListener {
                    generarYMostrarQR(vehiculo.first, vehiculo.second, vehiculo.third)
                    dialog.dismiss()
                    showSweetToast("QR listo para: ${vehiculo.second} - ${vehiculo.third}", true)
                }
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> v.alpha = 0.7f
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.alpha = 1.0f
                    }
                    false
                }
            }

            // Icono del vehículo
            val vehicleIcon = TextView(this).apply {
                text = getVehicleIcon(vehiculo.second)
                textSize = 18f
                setPadding(0, 0, 15, 0)
            }

            // Información del vehículo
            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val typeView = TextView(this).apply {
                text = vehiculo.second
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@QrActivity, R.color.Negro))
                setTypeface(typeface, Typeface.BOLD)
            }

            val numberView = TextView(this).apply {
                text = vehiculo.third
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@QrActivity, R.color.Negro))
            }

            infoLayout.addView(typeView)
            infoLayout.addView(numberView)

            itemLayout.addView(vehicleIcon)
            itemLayout.addView(infoLayout)

            listLayout.addView(itemLayout)

            // Separador
            if (index < vehiculos.size - 1) {
                val separator = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    ).apply {
                        setMargins(10, 5, 10, 5)
                    }
                    setBackgroundColor(ContextCompat.getColor(this@QrActivity, R.color.Tercero))
                }
                listLayout.addView(separator)
            }
        }

        scrollView.addView(listLayout)

        // Botones
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 0)
        }

        val cancelButton = Button(this).apply {
            text = "Cancelar"
            setTextColor(ContextCompat.getColor(this@QrActivity, R.color.Blanco))
            setBackgroundColor(ContextCompat.getColor(this@QrActivity, R.color.Secundario))
            setPadding(30, 15, 30, 15)
            setOnClickListener {
                dialog.dismiss()
                if (!qrGenerado) {
                    mostrarEstadoVacio("Selecciona un vehículo para generar tu QR")
                }
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(5, 0, 5, 0)
            }
            background = createRoundedDrawable(ContextCompat.getColor(this@QrActivity, R.color.Secundario), 20f)
        }

        val defaultButton = Button(this).apply {
            text = "Usar primer vehículo"
            setTextColor(ContextCompat.getColor(this@QrActivity, R.color.Blanco))
            setBackgroundColor(ContextCompat.getColor(this@QrActivity, R.color.Verde_bien))
            setPadding(30, 15, 30, 15)
            setOnClickListener {
                if (vehiculos.isNotEmpty()) {
                    val primerVehiculo = vehiculos[0]
                    generarYMostrarQR(primerVehiculo.first, primerVehiculo.second, primerVehiculo.third)
                    dialog.dismiss()
                    showSweetToast("QR listo para: ${primerVehiculo.second} - ${primerVehiculo.third}", true)
                }
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(5, 0, 5, 0)
            }
            background = createRoundedDrawable(ContextCompat.getColor(this@QrActivity, R.color.Verde_bien), 20f)
        }

        buttonLayout.addView(cancelButton)
        buttonLayout.addView(defaultButton)

        // Agregar todos los views al layout principal
        mainLayout.addView(titleView)
        mainLayout.addView(messageView)
        mainLayout.addView(scrollView)
        mainLayout.addView(buttonLayout)

        dialog.setView(mainLayout)
        dialog.show()
    }

    private fun createRoundedDrawable(color: Int, cornerRadius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            this.cornerRadius = cornerRadius
            setColor(color)
        }
    }

    private fun getVehicleIcon(tipo: String): String {
        return when {
            tipo.contains("bici", true) -> "🚲"
            tipo.contains("moto", true) -> "🏍️"
            tipo.contains("carro", true) -> "🚗"
            tipo.contains("furgon", true) -> "🚐"
            tipo.contains("patineta", true) -> "🛴"
            else -> "🚗"
        }
    }

    private fun showSweetToast(message: String, isSuccess: Boolean) {
        runOnUiThread {
            val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
            val toastView = TextView(this).apply {
                text = message
                setTextColor(ContextCompat.getColor(this@QrActivity, R.color.Blanco))
                gravity = Gravity.CENTER
                setPadding(40, 20, 40, 20)
                val backgroundColor = if (isSuccess) R.color.Verde_bien else R.color.Secundario
                background = createRoundedDrawable(ContextCompat.getColor(this@QrActivity, backgroundColor), 25f)
            }
            toast.view = toastView
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    private fun generarYMostrarQR(vehiculoId: String, tipo: String, numero: String) {
        try {
            Log.d("QrActivity", "Generando QR para: $tipo - $numero")
            val qrBitmap = generateQRCode(vehiculoId, 600)
            runOnUiThread {
                ivCodigoQR.setImageBitmap(qrBitmap)
                qrGenerado = true
                mostrarContenedorQR()
                showSweetToast("QR generado para: $tipo - $numero", true)
            }
            Log.d("QrActivity", "QR generado exitosamente")
        } catch (e: Exception) {
            Log.e("QrActivity", "Error al generar QR: ${e.message}")
            runOnUiThread {
                mostrarError("Error al generar el código QR")
            }
        }
    }

    private fun mostrarContenedorQR() {
        runOnUiThread {
            layoutQrContainer.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun mostrarEstadoVacio(mensaje: String) {
        runOnUiThread {
            layoutQrContainer.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
            tvEmptyMessage.text = mensaje
            qrGenerado = false
        }
    }

    private fun mostrarError(mensaje: String) {
        Log.e("QrActivity", mensaje)
        mostrarEstadoVacio(mensaje)
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
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            bitmap
        } catch (e: WriterException) {
            Log.e("QrActivity", "WriterException al generar QR: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("QrActivity", "Exception al generar QR: ${e.message}")
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
        2. Contiene el ID único de tu vehículo
        3. Con él podrás entrar y salir del parqueadero
        4. Presenta este QR al ingresar al parqueadero
        5. Si tienes múltiples vehículos, selecciona el correcto
        """.trimIndent())
        builder.setPositiveButton("Entendido", null)
        builder.show()
    }

    override fun getCurrentNavigationItem(): Int {
        return R.id.qr
    }
}