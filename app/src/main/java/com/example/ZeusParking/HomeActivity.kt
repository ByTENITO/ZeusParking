package com.example.parquiatenov10

import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.ZeusParking.BaseNavigationActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class HomeActivity : BaseNavigationActivity() {
    // Variables y vistas
    private var database = FirebaseFirestore.getInstance()

    private lateinit var perfilImageView: ImageView
    private lateinit var perfilImageStorage: ImageView
    private lateinit var perfilImageStorageGrande: ImageView

    private lateinit var notifiFurgon: TextView
    private lateinit var notifiVehiculoParticular: TextView
    private lateinit var notifiBicicleta: TextView
    private lateinit var notifiMotocicleta: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        enableEdgeToEdge()

        //Navegacion
        setupNavigation()

        // Setup de vistas
        perfilImageView = findViewById(R.id.FotoPerfil_ImageView)
        perfilImageStorage = findViewById(R.id.FotoPerfil_Storage)
        perfilImageStorageGrande = findViewById(R.id.perfil_grande)

        notifiFurgon = findViewById(R.id.notificacionFurgon)
        notifiVehiculoParticular = findViewById(R.id.notificacionVehiculoParticular)
        notifiBicicleta = findViewById(R.id.notificacionBicicleta)
        notifiMotocicleta = findViewById(R.id.notificacionMotocicleta)

        val perfilMenuLayout = findViewById<LinearLayout>(R.id.menuPerfil)
        val imagenGrande = findViewById<ShapeableImageView>(R.id.perfil_grande)
        val nombreTV = findViewById<TextView>(R.id.usuario_tv)
        val correoTV = findViewById<TextView>(R.id.email_tv)
        val gestionarBtn = findViewById<Button>(R.id.gestionar_btn)
        val cerrarSesionBtn = findViewById<Button>(R.id.cerrar_sesion_btn)
        val seccionDisponibilidad = findViewById<LinearLayout>(R.id.seccionDisponibilidad)
        val fondoDesactivado = findViewById<View>(R.id.fondoDesactivado)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val correo = intent.getStringExtra("email") ?: "No disponible"

        crearCanalNotificacion(this)

        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", correo)
            .addSnapshotListener { documents, e ->
                if (documents != null) {
                    for (document in documents) {
                        val cedula = document.getString("cedula")
                        buscarImagenUser(userId, cedula)
                        Log.d("FireStorage", "id -> ${document.id} ")
                        Log.d("FireStorage", "usuario -> $cedula")
                    }
                }
            }

        // Mostrar menú al hacer click en la imagen de perfil
        perfilImageView.setOnClickListener {
            cargarImagen(fondoDesactivado, perfilMenuLayout, nombreTV, correoTV, imagenGrande)
        }

        perfilImageStorage.setOnClickListener {
            cargarImagen(fondoDesactivado, perfilMenuLayout, nombreTV, correoTV, imagenGrande)
        }

        // Cerrar menú al tocar fuera (fondo oscuro)
        fondoDesactivado.setOnClickListener {
            // Animación de cierre del menú
            perfilMenuLayout.animate()
                .alpha(0f)
                .translationY(-50f)
                .setDuration(200)
                .withEndAction {
                    perfilMenuLayout.visibility = View.GONE
                }
                .start()

            // Ocultar fondo y mostrar de nuevo la imagen de perfil
            fondoDesactivado.visibility = View.GONE
            perfilImageView.alpha = 0f
            perfilImageView.visibility = View.VISIBLE
            perfilImageView.animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        }

        // Mostrar/ocultar sección de disponibilidad
        gestionarBtn.setOnClickListener {
            seccionDisponibilidad.visibility = if (seccionDisponibilidad.visibility == View.VISIBLE)
                View.GONE else View.VISIBLE
        }

        // Cerrar sesión
        cerrarSesionBtn.setOnClickListener {
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Obtener datos del intent
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val inputCorreo: String? = bundle?.getString("inputCorreo")
        val provider = bundle?.getString("provider")
        val fotoPerfilUrl: String? = bundle?.getString("foto_perfil_url")
        val sharedPref = getSharedPreferences("MisDatos", MODE_PRIVATE)
        with(sharedPref.edit()) {
            if (inputCorreo == "vigilanteuniminuto@gmail.com") {
                putString("nombreUsuario", inputCorreo)
            } else {
                putString("nombreUsuario", email)
            }
            apply()
        }
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", email)
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    Toast.makeText(
                        this,
                        "El usuario no tiene vinculados vehiculos",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (documents != null && !documents.isEmpty) {
                        for (document in documents.documents) {
                            val tipoVehiculo = document.getString("tipo")
                            if (tipoVehiculo == "Furgon") {
                                escucharDisponibilidad(
                                    "0ctYNlFXwtVw9ylURFXi",
                                    "Furgon",
                                    notifiFurgon,
                                    listOf(
                                        (0..0) to "Hola, no quedan espacios para Furgon",
                                        (1..2) to "Hola, quedan pocos espacios en el parqueadero de Furgon, quedan: {espacios}",
                                        (3..4) to "Hola, quedan {espacios} espacios para Furgon"
                                    )
                                )
                            }
                            if (tipoVehiculo == "Vehiculo Particular") {
                                escucharDisponibilidad(
                                    "UF0tfabGHGitcj7En6Wy",
                                    "Vehiculo Particular",
                                    notifiVehiculoParticular,
                                    listOf(
                                        (0..0) to "Hola, no quedan espacios para Vehiculo Particular",
                                        (1..5) to "Hola, quedan pocos espacios en el parqueadero de Vehiculo Particular, quedan: {espacios}",
                                        (6..10) to "Hola, quedan {espacios} espacios para Vehiculo Particular"
                                    )
                                )
                            }
                            if (tipoVehiculo == "Bicicleta") {
                                escucharDisponibilidad(
                                    "IuDC5XlTyhxhqU4It8SD",
                                    "Bicicleta",
                                    notifiBicicleta,
                                    listOf(
                                        (0..0) to "Hola, no quedan espacios para Bicicleta",
                                        (1..5) to "Hola, quedan pocos espacios en el parqueadero de Bicicleta, quedan: {espacios}",
                                        (6..10) to "Hola, quedan {espacios} espacios para Bicicleta"
                                    )
                                )
                            }
                            if (tipoVehiculo == "Motocicleta") {
                                escucharDisponibilidad(
                                    "ntHgnXs4Qbz074siOrvz",
                                    "Motocicleta",
                                    notifiMotocicleta,
                                    listOf(
                                        (0..0) to "Hola, no quedan espacios para Motocicleta",
                                        (1..5) to "Hola, quedan pocos espacios en el parqueadero de Motocicleta, quedan: {espacios}",
                                        (6..10) to "Hola, quedan {espacios} espacios para Motocicleta"
                                    )
                                )
                            }
                        }
                    }
                }
            }

        // Comprobar si el proveedor es Google
        if (provider == ProviderType.GOOGLE.name && email != null && fotoPerfilUrl != null) {
            setup(email)
            loadProfilePicture(fotoPerfilUrl)
        } else if (email != null) {
            setup(email)
        }
        if (fotoPerfilUrl.isNullOrEmpty()) {
            Log.e("CargaImagen", "La URL de la imagen es nula o vacía.")
        } else {
            Log.d("CargaImagen", "URL de la imagen recibida: $fotoPerfilUrl")
        }

        // Guardar email en preferencias
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("email", email)
        editor.apply()
    }

    //Navegacion del Sistema
    override fun getCurrentNavigationItem(): Int = R.id.home

    private fun cargarImagen(
        fondoDesactivado: View,
        perfilMenuLayout: View,
        nombreTV: TextView,
        correoTV: TextView,
        imagenGrande: ImageView
    ) {
        // Mostrar fondo oscuro y menú
        fondoDesactivado.visibility = View.VISIBLE
        perfilMenuLayout.visibility = View.VISIBLE

        // Animación del menú
        perfilMenuLayout.apply {
            alpha = 0f
            translationY = -80f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
        // Datos del usuario
        val fotoUrl = intent.getStringExtra("foto_perfil_url")
        val nombre = FirebaseAuth.getInstance().currentUser?.displayName ?: "Usuario"
        val correo = FirebaseAuth.getInstance().currentUser?.email ?: "correo@ejemplo.com"

        nombreTV.text = nombre
        correoTV.text = correo

        if (!fotoUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(fotoUrl)
                .placeholder(R.drawable.fondo_vigilante)
                .error(R.drawable.fondo_vigilante)
                .into(imagenGrande)
        }
    }

    private fun buscarImagenUser(userId: String?, cedula: String?) {
        val storageImagenUser = FirebaseStorage.getInstance().reference.child("$userId/$cedula.png")
        storageImagenUser.metadata.addOnSuccessListener { metadata ->
            val actualizacionRemota = metadata.updatedTimeMillis
            val carpeta = File(getExternalFilesDir(null), "70T05_U$3R")

            Log.d("FireStorage", "$userId/$cedula.png")

            if (!carpeta.exists() || carpeta.lastModified() < actualizacionRemota) {
                carpeta.mkdirs()
                val file = File(carpeta, "$cedula.png")
                storageImagenUser.getFile(file).addOnSuccessListener {
                    Log.d("FireStorage", "Imagen descargada en: ${file.absolutePath}")
                    val bitMap = BitmapFactory.decodeFile(file.absolutePath)
                    perfilImageStorage.setImageBitmap(bitMap)
                    perfilImageStorageGrande.setImageBitmap(bitMap)
                }.addOnFailureListener {
                    Log.e("FireStorage", "Error al descargar la imagen:", it)
                }
            } else {
                val archivo = File(getExternalFilesDir(null), "70T05_U$3R/$cedula.png")
                if (archivo.exists()) {
                    Log.d("FireStorage", "Imagen en: ${archivo.absolutePath}")
                    val bitMap = BitmapFactory.decodeFile(archivo.absolutePath)
                    perfilImageStorage.setImageBitmap(bitMap)
                    perfilImageStorageGrande.setImageBitmap(bitMap)
                } else {
                    storageImagenUser.getFile(archivo).addOnSuccessListener {
                        Log.d("FireStorage", "Imagen descargada en: ${archivo.absolutePath}")
                        val bitMap = BitmapFactory.decodeFile(archivo.absolutePath)
                        perfilImageStorage.setImageBitmap(bitMap)
                        perfilImageStorageGrande.setImageBitmap(bitMap)
                    }.addOnFailureListener {
                        Log.e("FireStorage", "Error al descargar la imagen:", it)
                    }
                }
            }
        }
    }

    // Función para cargar la foto de perfil desde la URL
    private fun loadProfilePicture(fotoUrl: String) {
        // Usar Picasso para cargar la imagen desde la URL
        Picasso.get().load(fotoUrl).into(perfilImageView)
    }

    // Configuración inicial del correo y bienvenida
    private fun setup(email: String) {
        title = "Inicio"
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

}
