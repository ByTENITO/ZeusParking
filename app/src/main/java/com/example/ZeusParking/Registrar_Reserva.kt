package com.example.parquiatenov10

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.parquiatenov10.Home_vigilante
import com.example.parquiatenov10.SalidaQrParqueadero
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
import java.util.regex.Pattern

@RequiresApi(Build.VERSION_CODES.O)
class Registrar_Reserva : AppCompatActivity() {

    private var database = FirebaseFirestore.getInstance()
    private lateinit var tiposSpinner: Spinner
    private lateinit var marcoNum: EditText
    private lateinit var reservaBtn: Button
    private lateinit var timePicker: TimePicker
    private lateinit var auth: FirebaseAuth
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val HORA_INICIO = 6 // 6am
        private const val HORA_FIN = 22 // 10pm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_reserva)

        Responsividad.inicializar(this)
        auth = FirebaseAuth.getInstance()

        tiposSpinner = findViewById(R.id.Tipos_SpinnerQR)
        marcoNum = findViewById(R.id.numero)
        reservaBtn = findViewById(R.id.Reserva_BTN)
        timePicker = findViewById(R.id.timePicker)

        configurarTimePicker()
        configurarSpinner()
        configurarBotonReserva()

        crearCanalNotificacion(this)
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

    private fun validarFormatoPlaca(numero: String, tipoVehi: String): Boolean {
        return when (tipoVehi) {
            "Motocicleta", "Vehículo" -> {
                val placaPattern = Pattern.compile("^[a-zA-Z]{3,4}[0-9]{2,3}[a-zA-Z]?$")
                placaPattern.matcher(numero).matches()
            }
            "Furgón" -> {
                numero.length == 7
            }
            "Bicicleta", "Patineta" -> {
                numero.length == 4 && numero.all { it.isDigit() }
            }
            else -> true
        }
    }

    private fun configurarTimePicker() {
        timePicker.setIs24HourView(false)
        timePicker.hour = HORA_INICIO
        timePicker.minute = 0
    }

    private fun configurarSpinner() {
        val adapter = ArrayAdapter.createFromResource(this, R.array.items, R.layout.estilo_spinner)
        adapter.setDropDownViewResource(R.layout.estilo_spinner)
        tiposSpinner.adapter = adapter

        tiposSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    1, 2 -> { // Bicicleta, Patineta
                        marcoNum.hint = "4 Últimos Números"
                        marcoNum.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(4))
                    }
                    3, 4, 5 -> { // Motocicleta, Vehículo, Furgón
                        marcoNum.hint = when (position) {
                            3 -> "Placa (Ej. abc123)"
                            4 -> "Placa (Ej. abc123)"
                            5 -> "Número de Furgón"
                            else -> "Número"
                        }
                        marcoNum.inputType = InputType.TYPE_CLASS_TEXT
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(6))
                    }
                    else -> {
                        marcoNum.hint = "Seleccione un tipo de vehiculo"
                        marcoNum.inputType = InputType.TYPE_CLASS_TEXT
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun configurarBotonReserva() {
        reservaBtn.setOnClickListener {
            if (hayConexionInternet(this)) {
                Log.d("conexion", "¡Hay conexión a Internet!")
                val tipoVehi = tiposSpinner.selectedItem.toString()
                val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                val numero = marcoNum.text.toString()

                if (!validarCampos(tipoVehi, userId, numero)) return@setOnClickListener

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

                verificarDisponibilidad(userId, tipoVehi, numero, horaReserva)
            } else {
                Toast.makeText(this, "¡Se ha perdido la conexion!", Toast.LENGTH_SHORT).show()
                finish()
                Log.d("conexion", "No hay conexión")
            }
        }
    }

    private fun validarCampos(tipoVehi: String, userId: String, numero: String): Boolean {
        return when {
            tipoVehi == "Tipo de Vehiculo" -> {
                Toast.makeText(this, "Seleccione un tipo de vehículo", Toast.LENGTH_SHORT).show()
                false
            }
            userId.isEmpty() -> {
                Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                false
            }
            numero.isEmpty() -> {
                Toast.makeText(this, "Ingrese el número/placa del vehículo", Toast.LENGTH_SHORT).show()
                false
            }
            !validarFormatoPlaca(numero, tipoVehi) -> {
                val mensaje = when (tipoVehi) {
                    "Motocicleta", "Vehículo" -> "Formato de placa inválido. Ejemplos válidos:\nabc123, abc12c, abcd12"
                    "Furgón" -> "Número de furgón debe tener 7 caracteres"
                    "Bicicleta", "Patineta" -> "Debe ingresar los 4 últimos dígitos"
                    else -> "Número/placa inválido"
                }
                Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                false
            }
            else -> true
        }
    }

    private fun verificarDisponibilidad(userId: String, tipoVehi: String, numero: String, horaReserva: LocalTime) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("id", userId)
            .whereEqualTo("tipo", tipoVehi)
            .whereEqualTo("numero", numero)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "No hay registro de este vehículo", Toast.LENGTH_SHORT).show()
                } else {
                    verificarReservaExistente(userId, tipoVehi, numero, horaReserva)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar vehículo: ", e)
                Toast.makeText(this, "Error al verificar vehículo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verificarReservaExistente(userId: String, tipoVehi: String, numero: String, horaReserva: LocalTime) {
        database.collection("Reservas")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    iniciarReserva(userId, tipoVehi, numero, horaReserva)
                } else {
                    mostrarErrorReservaExistente(documents)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar reserva: ", e)
                Toast.makeText(this, "Error al verificar reserva", Toast.LENGTH_SHORT).show()
            }
    }
    private fun iniciarReserva(userId: String, tipoVehi: String, numero: String, horaReserva: LocalTime) {
        val fechaActual = LocalDate.now()
        val fechaHoraCompleta = LocalDateTime.of(fechaActual, horaReserva)
        val formatoCompleto = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fechaHoraFormateada = fechaHoraCompleta.format(formatoCompleto)

        val intent = Intent(this, Reservacion::class.java).apply {
            putExtra("ID", userId)
            putExtra("Tipo", tipoVehi)
            putExtra("numero", numero)
            putExtra("horaReserva", fechaHoraFormateada)
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