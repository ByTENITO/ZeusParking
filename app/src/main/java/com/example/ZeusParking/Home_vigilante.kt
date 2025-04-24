package com.example.parquiatenov10

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import com.google.android.material.button.MaterialButton
import android.content.Intent
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

    // Capacidades máximas
    private val capacidadesMaximas = mapOf(
        "Furgon" to 5,
        "Vehiculo Particular" to 15,
        "Bicicleta" to 50,
        "Motocicleta" to 25
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_vigilante)

        setupNavigation()
        inicializarVistas()
        configurarPerfilUsuario()
        configurarListenersDisponibilidad()
        crearCanalNotificacion()


        findViewById<MaterialButton>(R.id.scan_entrada_btn).setOnClickListener {
            startActivity(Intent(this, EntradaQrParqueadero::class.java))
        }

        findViewById<MaterialButton>(R.id.scan_salida_btn).setOnClickListener {
            startActivity(Intent(this, SalidaQrParqueadero::class.java))
        }

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
        val capacidadMaxima = capacidadesMaximas[tipo] ?: 0

        val listener = database.collection("Disponibilidad")
            .document(docId)
            .addSnapshotListener { document, error ->
                error?.let {
                    Toast.makeText(this, "Error al obtener disponibilidad", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                document?.let { doc ->
                    val disponibles = doc.getLong(tipo)?.toInt() ?: 0

                    // Actualizar UI
                    runOnUiThread {
                        // Mostrar "disponibles/total"
                        textView.text = "$disponibles/$capacidadMaxima espacios para $tipo"

                        // Calcular y actualizar progress bar
                        val porcentaje = (disponibles.toFloat() / capacidadMaxima.toFloat() * 100).toInt()
                        progressBar.progress = porcentaje

                        // color según disponibilidad
                        progressBar.setIndicatorColor(
                            when {
                                porcentaje < 20 -> Color.RED
                                porcentaje < 50 -> Color.YELLOW
                                else -> Color.GREEN
                            }
                        )

                        // Notificación si quedan pocos espacios
                        if (disponibles <= 2) {
                            mostrarNotificacion(
                                "Alerta de Disponibilidad",
                                "Quedan pocos espacios para $tipo ($disponibles)"
                            )
                        }
                    }
                }
            }

        listeners.add(listener)
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Canal ZeusParking"
            val descripcion = "Notificaciones de disponibilidad"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val canal = NotificationChannel("ZeusParking", nombre, importancia).apply {
                description = descripcion
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    private fun mostrarNotificacion(titulo: String, mensaje: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, "ZeusParking")
            .setSmallIcon(R.drawable.icon_zeusparking)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(Color.BLUE)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Navegación
    override fun getCurrentNavigationItem(): Int = R.id.home_vigi
}