package com.example.parquiatenov10

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.ZeusParking.BaseNavigationActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.Duration

@RequiresApi(Build.VERSION_CODES.O)
class Home_vigilante : BaseNavigationActivity() {

    // Vistas de UI
    private lateinit var notiFurgon: MaterialTextView
    private lateinit var notiVehiculo: MaterialTextView
    private lateinit var notiBicicleta: MaterialTextView
    private lateinit var notiMoto: MaterialTextView
    private lateinit var progressFurgon: LinearProgressIndicator
    private lateinit var progressVehiculo: LinearProgressIndicator
    private lateinit var progressBicicleta: LinearProgressIndicator
    private lateinit var progressMoto: LinearProgressIndicator
    private lateinit var ReservasTabla: TableLayout
    private lateinit var Buscador: EditText
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable


    // Firebase
    private val database = FirebaseFirestore.getInstance()
    private val listeners = mutableListOf<ListenerRegistration>()

    //Lista Buscador
    private val datosOriginales = mutableListOf<ReservData>()

    private val documentosDisponibilidad = mapOf(
        "Furgon" to "0ctYNlFXwtVw9ylURFXi",
        "Vehiculo Particular" to "UF0tfabGHGitcj7En6Wy",
        "Bicicleta" to "IuDC5XlTyhxhqU4It8SD",
        "Motocicleta" to "ntHgnXs4Qbz074siOrvz"
    )

    data class ReservData(
        val nombre: String,
        val apellido: String,
        val fecha: String,
        val vehiculo: String,
        val numero: String
    ) : java.io.Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_vigilante)

        //Responsividad
        Responsividad.inicializar(this)

        setupNavigation()
        inicializarVistas()
        configurarPerfilUsuario()
        configurarListenersDisponibilidad()
        consultaReserva()
        crearCanalNotificacion(this)


        findViewById<MaterialButton>(R.id.scan_entrada_btn).setOnClickListener {
            startActivity(Intent(this, EntradaQrParqueadero::class.java))
        }

        findViewById<MaterialButton>(R.id.scan_salida_btn).setOnClickListener {
            startActivity(Intent(this, SalidaQrParqueadero::class.java))
        }
        escucharDisponibilidad(
            "0ctYNlFXwtVw9ylURFXi",
            "Furgon",
            notiFurgon,
            listOf(
                (0..0) to "Hola, no quedan espacios para Furgon",
                (1..2) to "Hola, quedan pocos espacios en el parqueadero de Furgon, quedan: {espacios}",
                (3..4) to "Hola, quedan {espacios} espacios para Furgon"
            )
        )
        escucharDisponibilidad(
            "UF0tfabGHGitcj7En6Wy",
            "Vehiculo Particular",
            notiVehiculo,
            listOf(
                (0..0) to "Hola, no quedan espacios para Vehiculo Particular",
                (1..5) to "Hola, quedan pocos espacios en el parqueadero de Vehiculo Particular, quedan: {espacios}",
                (6..10) to "Hola, quedan {espacios} espacios para Vehiculo Particular"
            )
        )
        escucharDisponibilidad(
            "IuDC5XlTyhxhqU4It8SD",
            "Bicicleta",
            notiBicicleta,
            listOf(
                (0..0) to "Hola, no quedan espacios para Bicicleta",
                (1..5) to "Hola, quedan pocos espacios en el parqueadero de Bicicleta, quedan: {espacios}",
                (6..10) to "Hola, quedan {espacios} espacios para Bicicleta"
            )
        )
        escucharDisponibilidad(
            "ntHgnXs4Qbz074siOrvz",
            "Motocicleta",
            notiMoto,
            listOf(
                (0..0) to "Hola, no quedan espacios para Motocicleta",
                (1..5) to "Hola, quedan pocos espacios en el parqueadero de Motocicleta, quedan: {espacios}",
                (6..10) to "Hola, quedan {espacios} espacios para Motocicleta"
            )
        )

        Buscador.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString().lowercase()
                if (texto.length >= 7) {
                    val filtradosNombre = datosOriginales.filter {
                        "${it.apellido},${it.nombre}"
                            .lowercase()
                            .contains(texto)
                    }
                    mostrarFiltro(filtradosNombre)
                    Log.d("lista", "$filtradosNombre")
                }

                if (texto.length <= 6) {
                    val filtradoNumero = datosOriginales.filter {
                        "${it.numero}"
                            .lowercase()
                            .contains(texto)
                    }
                    mostrarFiltro(filtradoNumero)
                    Log.d("lista", "$filtradoNumero")
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

    }


    override fun onDestroy() {
        super.onDestroy()
        listeners.forEach { it.remove() }
    }

    private fun inicializarVistas() {
        // TextViews de disponibilidad
        notiFurgon = findViewById(R.id.notiFurgon)
        notiVehiculo = findViewById(R.id.notiVehiculo)
        notiBicicleta = findViewById(R.id.notiBicicleta)
        notiMoto = findViewById(R.id.notiMoto)

        // Progress bars
        progressFurgon = findViewById(R.id.progressFurgon)
        progressVehiculo = findViewById(R.id.progressVehiculo)
        progressBicicleta = findViewById(R.id.progressBicicleta)
        progressMoto = findViewById(R.id.progressMoto)

        //Tabla
        ReservasTabla = findViewById(R.id.TablaReservas)

        //Buscador
        Buscador = findViewById(R.id.Buscador)
    }

    private fun configurarPerfilUsuario() {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        val welcomeText = findViewById<MaterialTextView>(R.id.welcome_text)
        val emailText = findViewById<MaterialTextView>(R.id.email_text)

        welcomeText.text = "Bienvenido, Vigilante"
        emailText.text = "Vigilanteuniminuto@gmail.com"
    }

    private fun configurarListenersDisponibilidad() {
        // Configurar listener para cada tipo de vehículo
        configurarListenerParaTipo("Furgon", notiFurgon, progressFurgon)
        configurarListenerParaTipo("Vehiculo Particular", notiVehiculo, progressVehiculo)
        configurarListenerParaTipo("Bicicleta", notiBicicleta, progressBicicleta)
        configurarListenerParaTipo("Motocicleta", notiMoto, progressMoto)
    }

    private fun configurarListenerParaTipo(
        tipo: String,
        textView: MaterialTextView,
        progressBar: LinearProgressIndicator
    ) {
        val docId = documentosDisponibilidad[tipo] ?: return
        val FijosId = when (tipo) {
            "Furgon" -> "NLRmedawc0M0nrpDt9Ci"
            "Vehiculo Particular" -> "edYUNbYSmPtvu1H6dI93"
            "Bicicleta" -> "sPcLdzFgRF2eAY5BWvFC"
            "Motocicleta" -> "AQjYvV224T01lrSEeQQY"
            else -> return
        }

        val listener = database.collection("Disponibilidad")
            .document(docId)
            .addSnapshotListener { document, error ->
                error?.let {
                    Toast.makeText(this, "Error al obtener disponibilidad", Toast.LENGTH_SHORT)
                        .show()
                    return@addSnapshotListener
                }

                database.collection("EspaciosFijos").document(FijosId)
                    .get().addOnSuccessListener { documents ->
                        document?.let { doc ->
                            val disponibles = doc.getLong(tipo)?.toInt() ?: 0

                            // Actualizar UI
                            runOnUiThread {
                                // Mostrar "disponibles/total"
                                val espacios = documents.getLong(tipo) ?: 0
                                textView.text = "$disponibles/$espacios espacios para $tipo"

                                // Calcular y actualizar progress bar
                                val porcentaje =
                                    (disponibles.toFloat() / espacios.toFloat() * 100).toInt()
                                progressBar.progress = porcentaje

                                // color según disponibilidad
                                progressBar.setIndicatorColor(
                                    when {
                                        porcentaje < 20 -> Color.RED
                                        porcentaje < 50 -> Color.YELLOW
                                        else -> Color.GREEN
                                    }
                                )
                            }
                        }
                    }
            }

        listeners.add(listener)
    }

    fun crearCanalNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Canal ZeusParking"
            val descripcion = "Descripción del canal ZeusParking"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val canal = NotificationChannel("ZeusParking", nombre, importancia).apply {
                description = descripcion
            }
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, "ZeusParking")
            .setSmallIcon(R.drawable.icon_zeusparking)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(Color.BLUE)
            .setAutoCancel(true)

        val notificationId =
            System.currentTimeMillis().toInt() // Genera un ID único basado en el tiempo
        notificationManager.notify(notificationId, builder.build())
    }

    fun escucharDisponibilidad(
        documentId: String,
        campo: String,
        textoView: TextView,
        umbrales: List<Pair<IntRange, String>>
    ) {
        database.collection("Disponibilidad")
            .document(documentId)
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.d("Firestore", "Error al escuchar: $documentId", e)
                    return@addSnapshotListener
                }

                val espacios = document?.getLong(campo)?.toInt() ?: 0
                Log.d("Firestore", "Actualizado $campo -> $espacios")

                // 🧩 Seleccionamos el emoji según el tipo de vehículo
                val emoji = when (campo.lowercase()) {
                    "furgon" -> "🚐"
                    "bicicleta" -> "🚲"
                    "motocicleta" -> "🏍️"
                    "vehiculo particular" -> "🚗"
                    else -> "NADA"
                }

                // 🧾 Mostramos mensaje en el TextView con emoji
                textoView.text = "$emoji Quedan $espacios espacios para $campo"

                for ((rango, mensaje) in umbrales) {
                    if (espacios in rango) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking",
                            "$emoji " + mensaje.replace("{espacios}", espacios.toString())
                        )
                        break
                    }
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun consultaReserva() {
        val inicializador = "inicializador"
        database.collection("Reservas")
            .addSnapshotListener { documents, e ->
                if (documents != null) {
                    for (i in ReservasTabla.childCount - 1 downTo 1) {
                        ReservasTabla.removeViewAt(i)
                    }
                    datosOriginales.clear()
                    for (document in documents) {
                        if (document.id != inicializador && document.exists()) {

                            val nombres = document.getString("nombre").toString()
                            val apellidos = document.getString("apellidos").toString()
                            val fecha = document.getString("fecha").toString()
                            val vehiculo = document.getString("tipo").toString()
                            val idVehi = document.getString("numero").toString()
                            val hora = document.getString("horaReserva").toString()

                            Tiempo(hora, vehiculo, idVehi)

                            Log.d(
                                "Reservas",
                                "Nombres:$nombres,Apellidos:$apellidos,Fecha:$fecha,Vehiculo:$vehiculo,Numero:$idVehi"
                            )

                            val datos = ReservData(
                                nombres,
                                apellidos,
                                hora,
                                vehiculo,
                                idVehi
                            )
                            datosOriginales.add(datos)
                            tabla(datos)
                        }
                    }
                }

            }
    }

    private fun tabla(datos: ReservData) {
        val celdaNombre = MaterialTextView(this)
        acortarTexto("${datos.apellido},${datos.nombre}", celdaNombre)
        celdaNombre.textSize = 10F
        celdaNombre.setTextColor(ContextCompat.getColor(this, R.color.Texto_pastel))
        celdaNombre.background = ContextCompat.getDrawable(this, R.drawable.borde_celda)

        val celdaFecha = TextView(this)
        acortarTexto(datos.fecha, celdaFecha)
        celdaFecha.textSize = 10F
        celdaFecha.setTextColor(ContextCompat.getColor(this, R.color.Texto_pastel))
        celdaFecha.background = ContextCompat.getDrawable(this, R.drawable.borde_celda)

        val celdaVehi = TextView(this)
        acortarTexto(datos.vehiculo, celdaVehi)
        celdaVehi.textSize = 10F
        celdaVehi.setTextColor(ContextCompat.getColor(this, R.color.Texto_pastel))
        celdaVehi.background = ContextCompat.getDrawable(this, R.drawable.borde_celda)

        val celdaIdVehi = TextView(this)
        acortarTexto(datos.numero, celdaIdVehi)
        celdaIdVehi.textSize = 10F
        celdaIdVehi.setTextColor(ContextCompat.getColor(this, R.color.Texto_pastel))
        celdaIdVehi.background = ContextCompat.getDrawable(this, R.drawable.borde_celda)

        val fila = TableRow(this)
        fila.addView(celdaNombre)
        fila.addView(celdaFecha)
        fila.addView(celdaVehi)
        fila.addView(celdaIdVehi)

        ReservasTabla.addView(fila)
    }

    private fun mostrarFiltro(lista: List<ReservData>) {
        for (i in ReservasTabla.childCount - 1 downTo 1) {
            ReservasTabla.removeViewAt(i)
        }

        for (usuario in lista) {
            val fila = TableRow(this)

            val celdaNombre = TextView(this)
            celdaNombre.text = "${usuario.apellido},${usuario.nombre}"
            acortarTexto("${usuario.apellido},${usuario.nombre}", celdaNombre)
            celdaNombre.textSize = 10F
            celdaNombre.setTextColor(ContextCompat.getColor(this, R.color.Texto_pastel))
            celdaNombre.background = ContextCompat.getDrawable(this, R.drawable.borde_celda)

            val celdaFecha = TextView(this)
            celdaFecha.text = usuario.fecha
            acortarTexto(usuario.fecha, celdaFecha)
            celdaFecha.textSize = 10F
            celdaFecha.setTextColor(ContextCompat.getColor(this, R.color.Texto_pastel))
            celdaFecha.background = ContextCompat.getDrawable(this, R.drawable.borde_celda)

            val celdaVehi = TextView(this)
            celdaVehi.text = usuario.vehiculo
            acortarTexto(usuario.vehiculo, celdaVehi)
            celdaVehi.textSize = 10F
            celdaVehi.setTextColor(ContextCompat.getColor(this, R.color.Texto_pastel))
            celdaVehi.background = ContextCompat.getDrawable(this, R.drawable.borde_celda)

            val celdaIdVehi = TextView(this)
            celdaIdVehi.text = usuario.numero
            acortarTexto(usuario.numero, celdaIdVehi)
            celdaIdVehi.textSize = 10F
            celdaIdVehi.setTextColor(ContextCompat.getColor(this, R.color.Texto_pastel))
            celdaIdVehi.background = ContextCompat.getDrawable(this, R.drawable.borde_celda)

            fila.addView(celdaNombre)
            fila.addView(celdaFecha)
            fila.addView(celdaVehi)
            fila.addView(celdaIdVehi)

            ReservasTabla.addView(fila)
        }
    }

    private fun acortarTexto(texto: String, textView: TextView) {
        val textoCorto = if (texto.length > 11) {
            texto.substring(0, 11) + "..."
        } else {
            texto
        }
        textView.text = textoCorto
    }

    fun Tiempo(fechaInicio: String, vehiculo: String, idVehiculo: String) {
        runnable = object : Runnable {
            override fun run() {
                val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                try {
                    val inicio = LocalDateTime.parse(fechaInicio, formato)
                    var fechaHoraActual = LocalDateTime.now()

                    fechaHoraActual = LocalDateTime.now()

                    val duracion = Duration.between(inicio, fechaHoraActual)

                    val horas = duracion.toHours()
                    val minutos = duracion.toMinutes() % 60
                    val segundos = duracion.seconds % 60

                    if (minutos > 40) {
                        actualizarDisponibilidad(vehiculo, idVehiculo)
                    }
                    Log.d("tiempo actual","Tiempo: $fechaHoraActual")
                    handler.postDelayed(this, 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("hora", "Error al leer la hora: ${e.message}")
                }
            }
        }
        handler.post(runnable)
    }

    private fun actualizarDisponibilidad(tipoVehiculo: String, idVehiculo: String) {
        val documentId = when (tipoVehiculo) {
            "Furgon" -> "0ctYNlFXwtVw9ylURFXi"
            "Vehiculo Particular" -> "UF0tfabGHGitcj7En6Wy"
            "Bicicleta" -> "IuDC5XlTyhxhqU4It8SD"
            "Patineta Electrica" -> "IuDC5XlTyhxhqU4It8SD"
            "Motocicleta" -> "ntHgnXs4Qbz074siOrvz"
            else -> return
        }
        val FijosId = when (tipoVehiculo) {
            "Furgon" -> "NLRmedawc0M0nrpDt9Ci"
            "Vehiculo Particular" -> "edYUNbYSmPtvu1H6dI93"
            "Bicicleta" -> "sPcLdzFgRF2eAY5BWvFC"
            "Patineta Electrica" -> "sPcLdzFgRF2eAY5BWvFC"
            "Motocicleta" -> "AQjYvV224T01lrSEeQQY"
            else -> return
        }
        if (tipoVehiculo == "Patineta Electrica") {
            verificarEntrada(documentId, "Bicicleta", FijosId, idVehiculo)
        } else {
            verificarEntrada(documentId, tipoVehiculo, FijosId, idVehiculo)
        }
    }

    private fun verificarEntrada(
        documentId: String,
        tipoVehiculo: String,
        FijosId: String,
        idVehiculo: String
    ) {
        database.collection("Entrada")
            .whereEqualTo("numero",idVehiculo)
            .get()
            .addOnSuccessListener{ documents->
                if (documents.isEmpty){
                    VerifcarDisponibilidad(documentId, tipoVehiculo, FijosId, idVehiculo)
                }
            }
    }

    private fun VerifcarDisponibilidad(
        documentId: String,
        tipoVehiculo: String,
        FijosId: String,
        idVehiculo: String
    ) {
        database.collection("Disponibilidad")
            .document(documentId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val espacios = document.getLong(tipoVehiculo) ?: 0
                    VerificarEspaciosFijos(FijosId,documentId,espacios,tipoVehiculo,idVehiculo)
                } else {
                    Log.d("Firestore", "No se encontró el documento para $tipoVehiculo")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener el documento de disponibilidad: ", e)
            }
    }

    private fun VerificarEspaciosFijos(FijosId: String, documentId: String, espacios: Long, tipoVehiculo: String, idVehiculo: String){
        database.collection("EspaciosFijos").document(FijosId).get()
            .addOnSuccessListener { document ->
                val espaciosFijos = document.getLong(tipoVehiculo) ?: 0
                if (espacios != espaciosFijos) {
                    incrementarDisponibilidad(documentId,idVehiculo,tipoVehiculo)
                }
            }
    }

    private fun incrementarDisponibilidad(documentId: String,idVehiculo: String,tipoVehiculo: String){
        database.collection("Disponibilidad").document(documentId)
            .update(tipoVehiculo, FieldValue.increment(1))
            .addOnSuccessListener {
                eliminarReserva(idVehiculo,tipoVehiculo)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al actualizar el campo: ", e)
            }
    }

    private fun eliminarReserva(idVehiculo: String,tipoVehiculo: String){
        database.collection("Reservas")
            .whereEqualTo("numero", idVehiculo)
            .addSnapshotListener { documents, e ->
                if (documents != null) {
                    for (document in documents) {
                        database.collection("Reservas")
                            .document(document.id).delete()
                    }
                }
            }
        Log.d("Firestore", "Campo '$tipoVehiculo' aumentado")
    }

    // Navegación
    override fun getCurrentNavigationItem(): Int = R.id.home_vigi
}