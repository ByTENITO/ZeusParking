package com.example.parquiatenov10

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import com.example.ZeusParking.BaseNavigationActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class HomeActivity : BaseNavigationActivity() {
    private var database = FirebaseFirestore.getInstance()
    private lateinit var perfilImageView: com.google.android.material.imageview.ShapeableImageView
    private lateinit var welcomeText: TextView
    private lateinit var userEmail: TextView
    private lateinit var vehiculosContainer: LinearLayout
    private lateinit var portatilesContainer: LinearLayout

    private lateinit var notifiFurgon: TextView
    private lateinit var notifiVehiculoParticular: TextView
    private lateinit var notifiBicicleta: TextView
    private lateinit var notifiMotocicleta: TextView
    private lateinit var disponibilidadContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        enableEdgeToEdge()

        // Responsividad
        Responsividad.inicializar(this)

        // Navegación
        setupNavigation()

        // Setup de vistas
        perfilImageView = findViewById(R.id.FotoPerfil_ImageView)
        welcomeText = findViewById(R.id.welcome_text)
        userEmail = findViewById(R.id.user_email)
        vehiculosContainer = findViewById(R.id.vehiculos_container)
        portatilesContainer = findViewById(R.id.portatiles_container)
        disponibilidadContainer = findViewById(R.id.disponibilidad_container)

        // Configurar botones de registro
        findViewById<Button>(R.id.btnRegistroVehi).setOnClickListener {
            startActivity(Intent(this, RegistrarBiciActivity::class.java))
        }

        findViewById<Button>(R.id.btnRegistroPC).setOnClickListener {
            startActivity(Intent(this, RegistroPC::class.java))
        }

        // Obtener datos del usuario
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            welcomeText.text = "Bienvenido, ${it.displayName ?: "Usuario"}"
            userEmail.text = it.email ?: "correo@ejemplo.com"

            // Cargar foto de perfil si existe
            if (it.photoUrl != null) {
                Picasso.get().load(it.photoUrl).into(perfilImageView)
            }

            // Cargar datos del usuario
            cargarVehiculosUsuario(it.email)
            cargarPortatilesUsuario(it.uid)
        }

        crearCanalNotificacion(this)
    }

    private fun cargarVehiculosUsuario(email: String?) {
        if (email.isNullOrEmpty()) return

        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", email)
            .get()
            .addOnSuccessListener { documents ->
                vehiculosContainer.removeAllViews()
                disponibilidadContainer.removeAllViews()

                if (documents.isEmpty) {
                    val emptyView = TextView(this).apply {
                        text = "No tienes vehículos registrados"
                        setTextColor(Color.WHITE)
                    }
                    vehiculosContainer.addView(emptyView)
                    return@addOnSuccessListener
                }

                // Mapa para controlar qué tipos de vehículo tenemos
                val tiposVehiculos = mutableSetOf<String>()

                for (document in documents) {
                    val tipo = document.getString("tipo") ?: "Vehículo"
                    val numero = document.getString("numero") ?: "N/A"
                    val color = document.getString("color") ?: "N/A"

                    tiposVehiculos.add(tipo)

                    val vehiculoView = LayoutInflater.from(this)
                        .inflate(R.layout.item_vehiculo, vehiculosContainer, false)

                    vehiculoView.findViewById<TextView>(R.id.tipo_vehiculo).text = tipo
                    vehiculoView.findViewById<TextView>(R.id.numero_vehiculo).text = "Número: $numero"
                    vehiculoView.findViewById<TextView>(R.id.color_vehiculo).text = "Color: $color"

                    vehiculosContainer.addView(vehiculoView)
                }

                // Crear tarjetas de disponibilidad solo para los vehículos registrados
                if (tiposVehiculos.contains("Furgon")) {
                    agregarTarjetaDisponibilidad(
                        "Furgon",
                        "0ctYNlFXwtVw9ylURFXi",
                        "#E8F5E9",
                        listOf(
                            (0..0) to "No quedan espacios para Furgon",
                            (1..2) to "Quedan pocos espacios en el parqueadero de Furgon, quedan: {espacios}",
                            (3..4) to "Quedan {espacios} espacios para Furgon"
                        )
                    )
                }

                if (tiposVehiculos.contains("Vehiculo Particular")) {
                    agregarTarjetaDisponibilidad(
                        "Vehiculo Particular",
                        "UF0tfabGHGitcj7En6Wy",
                        "#E3F2FD",
                        listOf(
                            (0..0) to "No quedan espacios para Vehiculo Particular",
                            (1..5) to "Quedan pocos espacios en el parqueadero de Vehiculo Particular, quedan: {espacios}",
                            (6..10) to "Quedan {espacios} espacios para Vehiculo Particular"
                        )
                    )
                }

                if (tiposVehiculos.contains("Bicicleta")) {
                    agregarTarjetaDisponibilidad(
                        "Bicicleta",
                        "IuDC5XlTyhxhqU4It8SD",
                        "#FFF3E0",
                        listOf(
                            (0..0) to "No quedan espacios para Bicicleta",
                            (1..5) to "Quedan pocos espacios en el parqueadero de Bicicleta, quedan: {espacios}",
                            (6..10) to "Quedan {espacios} espacios para Bicicleta"
                        )
                    )
                }

                if (tiposVehiculos.contains("Motocicleta")) {
                    agregarTarjetaDisponibilidad(
                        "Motocicleta",
                        "ntHgnXs4Qbz074siOrvz",
                        "#F3E5F5",
                        listOf(
                            (0..0) to "No quedan espacios para Motocicleta",
                            (1..5) to "Quedan pocos espacios en el parqueadero de Motocicleta, quedan: {espacios}",
                            (6..10) to "Quedan {espacios} espacios para Motocicleta"
                        )
                    )
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar vehículos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun agregarTarjetaDisponibilidad(tipo: String, docId: String, color: String, umbrales: List<Pair<IntRange, String>>) {
        val cardView = LayoutInflater.from(this).inflate(
            R.layout.item_disponibilidad,
            disponibilidadContainer,
            false
        ) as CardView

        val textView = cardView.findViewById<TextView>(R.id.notificacion_item)
        cardView.setCardBackgroundColor(Color.parseColor(color))

        escucharDisponibilidad(docId, tipo, textView, umbrales)

        disponibilidadContainer.addView(cardView)
    }

    private fun cargarPortatilesUsuario(userId: String?) {
        if (userId.isNullOrEmpty()) return

        database.collection("Portatiles")
            .whereEqualTo("usuarioId", userId)
            .get()
            .addOnSuccessListener { documents ->
                portatilesContainer.removeAllViews()

                if (documents.isEmpty) {
                    val emptyView = TextView(this).apply {
                        text = "No tienes portátiles registrados"
                        setTextColor(Color.WHITE)
                    }
                    portatilesContainer.addView(emptyView)
                    return@addOnSuccessListener
                }

                for (document in documents) {
                    val marca = document.getString("marca") ?: "N/A"
                    val modelo = document.getString("modelo") ?: "N/A"
                    val serial = document.getString("serial") ?: "N/A"

                    val portatilView = LayoutInflater.from(this)
                        .inflate(R.layout.item_portatil, portatilesContainer, false)

                    portatilView.findViewById<TextView>(R.id.marca_portatil).text = "Marca: $marca"
                    portatilView.findViewById<TextView>(R.id.modelo_portatil).text = "Modelo: $modelo"
                    portatilView.findViewById<TextView>(R.id.serial_portatil).text = "Serial: $serial"

                    portatilesContainer.addView(portatilView)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar portátiles: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Navegación del Sistema
    override fun getCurrentNavigationItem(): Int = R.id.home

    private fun crearCanalNotificacion(context: Context) {
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

    private fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
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

    private fun escucharDisponibilidad(
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

                val emoji = when (campo.lowercase()) {
                    "furgon" -> "🚐"
                    "bicicleta" -> "🚲"
                    "motocicleta" -> "🏍️"
                    "vehiculo particular" -> "🚗"
                    else -> ""
                }

                textoView.text = "$emoji Quedan $espacios espacios para $campo"

                for ((rango, mensaje) in umbrales) {
                    if (espacios in rango) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking - $campo",
                            "$emoji " + mensaje.replace("{espacios}", espacios.toString())
                        )
                        break
                    }
                }
            }
    }
}