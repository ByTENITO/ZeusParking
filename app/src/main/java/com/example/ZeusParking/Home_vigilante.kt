package com.example.parquiatenov10

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import android.content.Intent
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.ZeusParking.BaseNavigationActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


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

    // Firebase
    private val database = FirebaseFirestore.getInstance()
    private val listeners = mutableListOf<ListenerRegistration>()

    private val documentosDisponibilidad = mapOf(
        "Furgon" to "0ctYNlFXwtVw9ylURFXi",
        "Vehiculo Particular" to "UF0tfabGHGitcj7En6Wy",
        "Bicicleta" to "IuDC5XlTyhxhqU4It8SD",
        "Motocicleta" to "ntHgnXs4Qbz074siOrvz"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_vigilante)

        setupNavigation()
        inicializarVistas()
        configurarPerfilUsuario()
        configurarListenersDisponibilidad()
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
                                val espacios = documents.getLong(tipo)?:0
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

    // Navegación
    override fun getCurrentNavigationItem(): Int = R.id.home_vigi
}