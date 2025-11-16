package com.example.parquiatenov10

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class Registrar_Reserva : AppCompatActivity() {

    private var database = FirebaseFirestore.getInstance()
    private lateinit var timePicker: TimePicker
    private lateinit var reservaBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var btnSelectVehicle: Button
    private lateinit var selectedVehicleInfo: LinearLayout
    private lateinit var tvVehicleType: TextView
    private lateinit var tvVehicleNumber: TextView
    private lateinit var layoutEmptyState: com.google.android.material.card.MaterialCardView
    private lateinit var btnTryAgain: Button
    private lateinit var tvEmptyMessage: TextView
    private lateinit var vehicleSelectionCard: com.google.android.material.card.MaterialCardView

    private var selectedVehicleId: String? = null
    private var selectedVehicleType: String? = null
    private var selectedVehicleNumber: String? = null

    companion object {
        private const val HORA_INICIO = 6 // 6am
        private const val HORA_FIN = 22 // 10pm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_reserva)

        Responsividad.inicializar(this)
        auth = FirebaseAuth.getInstance()

        inicializarVistas()
        configurarTimePicker()
        configurarBotonReserva()
        crearCanalNotificacion(this)

        // Cargar vehículos al iniciar
        cargarVehiculosUsuario()
    }

    private fun inicializarVistas() {
        timePicker = findViewById(R.id.timePicker)
        reservaBtn = findViewById(R.id.Reserva_BTN)
        btnSelectVehicle = findViewById(R.id.btnSelectVehicle)
        selectedVehicleInfo = findViewById(R.id.selectedVehicleInfo)
        tvVehicleType = findViewById(R.id.tvVehicleType)
        tvVehicleNumber = findViewById(R.id.tvVehicleNumber)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        btnTryAgain = findViewById(R.id.btnTryAgain)
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage)
        vehicleSelectionCard = findViewById(R.id.vehicleSelectionCard)

        btnSelectVehicle.setOnClickListener {
            cargarVehiculosYMostrarSeleccion()
        }

        btnTryAgain.setOnClickListener {
            cargarVehiculosUsuario()
        }
    }

    private fun cargarVehiculosUsuario() {
        val correo = auth.currentUser?.email

        if (correo.isNullOrEmpty()) {
            mostrarEstadoVacio("No se pudo obtener el correo del usuario")
            return
        }

        mostrarEstadoVacio("Cargando vehículos...")

        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    mostrarEstadoVacio("No tienes vehículos registrados")
                } else {
                    mostrarInterfazSeleccion()
                    // Si solo tiene un vehículo, seleccionarlo automáticamente
                    if (documents.size() == 1) {
                        val document = documents.documents[0]
                        seleccionarVehiculo(
                            document.id,
                            document.getString("tipo") ?: "Vehículo",
                            document.getString("numero") ?: "Sin número"
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                mostrarEstadoVacio("Error al cargar vehículos: ${e.message}")
            }
    }

    private fun cargarVehiculosYMostrarSeleccion() {
        val correo = auth.currentUser?.email

        if (correo.isNullOrEmpty()) {
            mostrarSweetToast("No se pudo obtener el correo del usuario", false)
            return
        }

        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    mostrarSweetToast("No tienes vehículos registrados", false)
                } else if (documents.size() == 1) {
                    val document = documents.documents[0]
                    seleccionarVehiculo(
                        document.id,
                        document.getString("tipo") ?: "Vehículo",
                        document.getString("numero") ?: "Sin número"
                    )
                    mostrarSweetToast("Vehículo seleccionado automáticamente", true)
                } else {
                    mostrarSeleccionVehiculoElegante(documents)
                }
            }
            .addOnFailureListener { e ->
                mostrarSweetToast("Error al cargar vehículos: ${e.message}", false)
            }
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
            background = createRoundedDrawable(ContextCompat.getColor(this@Registrar_Reserva, R.color.Blanco), 25f)
        }

        // Título
        val titleView = TextView(this).apply {
            text = "Seleccionar Vehículo"
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@Registrar_Reserva, R.color.Principal))
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 20)
        }

        // Mensaje
        val messageView = TextView(this).apply {
            text = "Selecciona el vehículo para la reserva:"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@Registrar_Reserva, R.color.Negro))
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
                background = createRoundedDrawable(ContextCompat.getColor(this@Registrar_Reserva, R.color.Texto_pastel), 12f)
                setOnClickListener {
                    seleccionarVehiculo(vehiculo.first, vehiculo.second, vehiculo.third)
                    dialog.dismiss()
                    mostrarSweetToast("Vehículo seleccionado: ${vehiculo.second} - ${vehiculo.third}", true)
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
                setTextColor(ContextCompat.getColor(this@Registrar_Reserva, R.color.Negro))
                setTypeface(typeface, Typeface.BOLD)
            }

            val numberView = TextView(this).apply {
                text = vehiculo.third
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@Registrar_Reserva, R.color.Negro))
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
                    setBackgroundColor(ContextCompat.getColor(this@Registrar_Reserva, R.color.Tercero))
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
            setTextColor(ContextCompat.getColor(this@Registrar_Reserva, R.color.Blanco))
            setBackgroundColor(ContextCompat.getColor(this@Registrar_Reserva, R.color.Secundario))
            setPadding(30, 15, 30, 15)
            setOnClickListener {
                dialog.dismiss()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(5, 0, 5, 0)
            }
            background = createRoundedDrawable(ContextCompat.getColor(this@Registrar_Reserva, R.color.Secundario), 20f)
        }

        buttonLayout.addView(cancelButton)

        // Agregar todos los views al layout principal
        mainLayout.addView(titleView)
        mainLayout.addView(messageView)
        mainLayout.addView(scrollView)
        mainLayout.addView(buttonLayout)

        dialog.setView(mainLayout)
        dialog.show()
    }

    private fun seleccionarVehiculo(vehicleId: String, tipo: String, numero: String) {
        selectedVehicleId = vehicleId
        selectedVehicleType = tipo
        selectedVehicleNumber = numero

        tvVehicleType.text = "Tipo: $tipo"
        tvVehicleNumber.text = "Número: ${numero.uppercase()}"
        selectedVehicleInfo.visibility = View.VISIBLE

        mostrarSweetToast("Vehículo seleccionado: $tipo - ${numero.uppercase()}", true)
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

    private fun mostrarSweetToast(message: String, isSuccess: Boolean) {
        runOnUiThread {
            val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
            val toastView = TextView(this).apply {
                text = message
                setTextColor(ContextCompat.getColor(this@Registrar_Reserva, R.color.Blanco))
                gravity = Gravity.CENTER
                setPadding(40, 20, 40, 20)
                val backgroundColor = if (isSuccess) R.color.Verde_bien else R.color.Secundario
                background = createRoundedDrawable(ContextCompat.getColor(this@Registrar_Reserva, backgroundColor), 25f)
            }
            toast.view = toastView
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    private fun mostrarInterfazSeleccion() {
        runOnUiThread {
            vehicleSelectionCard.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun mostrarEstadoVacio(mensaje: String) {
        runOnUiThread {
            vehicleSelectionCard.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
            tvEmptyMessage.text = mensaje
            selectedVehicleInfo.visibility = View.GONE
            selectedVehicleId = null
            selectedVehicleType = null
            selectedVehicleNumber = null
        }
    }

    fun hayConexionInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val redActiva = connectivityManager.activeNetwork ?: return false
        val capacidades = connectivityManager.getNetworkCapabilities(redActiva) ?: return false

        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun validarHorario(horaReserva: LocalTime): Boolean {
        val zonaColombia = ZoneId.of("America/Bogota")
        val ahoraColombia = ZonedDateTime.now(zonaColombia).toLocalTime()
        val horaInicio = LocalTime.of(HORA_INICIO, 0)
        val horaFin = LocalTime.of(HORA_FIN, 0)

        if (horaReserva !in horaInicio..horaFin) {
            return false
        }

        if (horaReserva.isBefore(ahoraColombia)) {
            return false
        }

        return true
    }

    private fun configurarTimePicker() {
        timePicker.setIs24HourView(false)
        timePicker.hour = HORA_INICIO
        timePicker.minute = 0
    }

    private fun configurarBotonReserva() {
        reservaBtn.setOnClickListener {
            if (hayConexionInternet(this)) {
                Log.d("conexion", "¡Hay conexión a Internet!")
                val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()

                if (!validarCampos(userId)) return@setOnClickListener

                val horaSeleccionada = timePicker.hour
                val minutoSeleccionado = timePicker.minute
                val horaReserva = LocalTime.of(horaSeleccionada, minutoSeleccionado)

                // Obtener hora actual en Colombia
                val zonaColombia = ZoneId.of("America/Bogota")
                val ahoraColombia = ZonedDateTime.now(zonaColombia).toLocalTime()

                if (!validarHorario(horaReserva)) {
                    val mensaje = if (horaReserva.isBefore(ahoraColombia)) {
                        "No puede hacer reservas en horas pasadas"
                    } else {
                        "El horario de reserva debe ser entre ${HORA_INICIO}am y ${HORA_FIN}pm (hora Colombia)"
                    }
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                verificarReservaExistente(userId, horaReserva)
            } else {
                Toast.makeText(this, "¡Se ha perdido la conexion!", Toast.LENGTH_SHORT).show()
                finish()
                Log.d("conexion", "No hay conexión")
            }
        }
    }

    private fun validarCampos(userId: String): Boolean {
        return when {
            userId.isEmpty() -> {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                false
            }
            selectedVehicleId == null -> {
                Toast.makeText(this, "Seleccione un vehículo", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun verificarReservaExistente(userId: String, horaReserva: LocalTime) {
        database.collection("Reservas")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    iniciarReserva(userId, horaReserva)
                } else {
                    mostrarErrorReservaExistente(documents)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar reserva: ", e)
                Toast.makeText(this, "Error al verificar reserva", Toast.LENGTH_SHORT).show()
            }
    }

    private fun iniciarReserva(userId: String, horaReserva: LocalTime) {
        val fechaActual = LocalDate.now()
        val fechaHoraCompleta = LocalDateTime.of(fechaActual, horaReserva)
        val formatoCompleto = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fechaHoraFormateada = fechaHoraCompleta.format(formatoCompleto)

        val intent = Intent(this, Reservacion::class.java).apply {
            putExtra("ID", userId)
            putExtra("Tipo", selectedVehicleType)
            putExtra("numero", selectedVehicleNumber)
            putExtra("horaReserva", fechaHoraFormateada)
            putExtra("vehicleId", selectedVehicleId)
        }
        startActivity(intent)
    }

    private fun mostrarErrorReservaExistente(documents: QuerySnapshot) {
        for (document in documents) {
            val tipo = document.getString("tipo") ?: ""
            val numero = document.getString("numero") ?: ""
            val mensaje = "Ya tienes una reserva activa para $tipo (${numero.uppercase()})"
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
        }
    }

    private fun crearCanalNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Canal ZeusParking"
            val descripcion = "Notificaciones de reservas"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val canal = NotificationChannel("ZeusParking", nombre, importancia).apply {
                description = descripcion
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    private fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, "ZeusParking")
            .setSmallIcon(R.drawable.icon_zeusparking)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(Color.BLUE)
            .setAutoCancel(true)

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, builder.build())
    }
}