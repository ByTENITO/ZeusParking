package com.example.parquiatenov10

import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
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
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView

class HomeActivity : AppCompatActivity() {
    // Variables y vistas
    private var database = FirebaseFirestore.getInstance()

    private lateinit var perfilImageView: ImageView
    private lateinit var notificacionLinear: LinearLayout

    private lateinit var notifiFurgon: TextView
    private lateinit var notifiVehiculoParticular: TextView
    private lateinit var notifiBicicleta: TextView
    private lateinit var notifiMotocicleta: TextView
    private var cambioAnimacionNoti = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Setup de vistas
        perfilImageView = findViewById(R.id.FotoPerfil_ImageView)

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

        crearCanalNotificacion(this)

        // Mostrar menú al hacer click en la imagen de perfil
        perfilImageView.setOnClickListener {
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
            } else {
                imagenGrande.setImageResource(R.drawable.fondo_vigilante)
            }
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


        //Menu de Navegaion
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNavigationView.selectedItemId) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.home -> {
                    if (this::class.java != HomeActivity::class.java as Class<*>) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                }
                R.id.localizacion -> {
                    if (this::class.java != Localizacion::class.java as Class<*>) {
                        startActivity(Intent(this, Localizacion::class.java))
                        overridePendingTransition(0, 0)
                    }
                }
                R.id.registro -> {
                    if (this::class.java != RegistrarBiciActivity::class.java as Class<*>) {
                        startActivity(Intent(this, RegistrarBiciActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                }
                R.id.qr -> {
                    if (this::class.java != QrActivity::class.java as Class<*>) {
                        startActivity(Intent(this, QrActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                }
            }

            true
        }

        // Obtener datos del intent
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val inputCorreo: String? = bundle?.getString("inputCorreo")
        val provider = bundle?.getString("provider")
        val fotoPerfilUrl: String? = bundle?.getString("foto_perfil_url")
        val sharedPref = getSharedPreferences("MisDatos", MODE_PRIVATE)
        with(sharedPref.edit()) {
            if (inputCorreo == "vigilante@uniminuto.edu.co") {
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



    // Función para cargar la foto de perfil desde la URL
    private fun loadProfilePicture(fotoUrl: String) {
        // Usar Picasso para cargar la imagen desde la URL
        Picasso.get().load(fotoUrl).into(perfilImageView)
    }

    private fun animacionAnchoLinear(view: View, startHeight: Int, endHeight: Int, duration: Long) {
        val animacionAncho = ValueAnimator.ofInt(startHeight, endHeight)
        animacionAncho.duration = duration
        animacionAncho.addUpdateListener { animation ->
            val params = view.layoutParams
            params.height = animation.animatedValue as Int
            view.layoutParams = params
        }
        animacionAncho.start()
    }

    private fun responsividadText(view: View, width: Int) {
        val textoUsuario = ValueAnimator.ofInt(width)
        textoUsuario.addUpdateListener { animation ->
            val params = view.layoutParams
            params.width = animation.animatedValue as Int
            view.layoutParams = params
        }
        textoUsuario.start()
    }

    private fun responsividad(view: View, width: Int, heigth: Int) {
        val anchoComponente = ValueAnimator.ofInt(width)
        val altoComponente = ValueAnimator.ofInt(heigth)
        anchoComponente.addUpdateListener { animation ->
            val params = view.layoutParams
            params.width = animation.animatedValue as Int
            view.layoutParams = params
        }

        altoComponente.addUpdateListener { animation ->
            val params = view.layoutParams
            params.height = animation.animatedValue as Int
            view.layoutParams = params
        }
        altoComponente.start()
        anchoComponente.start()
    }

    // Configuración inicial del correo y bienvenida
    private fun setup(email: String) {
        title = "Inicio"




    }

    fun notifiRepliegue(notiUno: Int, notiDos: Int, notiTres: Int, notiCuatro: Int) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", FirebaseAuth.getInstance().currentUser?.email ?: "correo@ejemplo.com"
            )
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    Toast.makeText(
                        this,
                        "El usuario no tiene vinculados vehiculos",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (documents != null && !documents.isEmpty) {
                        val tiposUsuario = mutableSetOf<String>()
                        for (document in documents) {
                            if (document.exists()) {
                                document.getString("tipo")?.let { tiposUsuario.add(it) }
                            }
                        }
                        if (tiposUsuario.isEmpty()) {
                            notifiFurgon.text = "No tiene un vehiculo asignado"
                            mostrarVista(notifiFurgon)
                            animacionAnchoLinear(notificacionLinear, notiUno, 1, 100L)
                        }
                        val heigth = when {
                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Vehiculo Particular",
                                    "Bicicleta",
                                    "Motocicleta"
                                )
                            ) -> notiCuatro

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Vehiculo Particular",
                                    "Bicicleta"
                                )
                            ) -> notiTres

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Vehiculo Particular",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiBicicleta)
                                notiTres
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Bicicleta",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiVehiculoParticular)
                                notiTres
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Vehiculo Particular",
                                    "Bicicleta",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiFurgon)
                                notiTres
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Vehiculo Particular"
                                )
                            ) -> notiDos

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Bicicleta"
                                )
                            ) -> {
                                ocultarVista(notifiVehiculoParticular)
                                notiDos
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiBicicleta, notifiVehiculoParticular)
                                notiDos
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Vehiculo Particular",
                                    "Bicicleta"
                                )
                            ) -> {
                                ocultarVista(notifiFurgon)
                                notiDos
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Vehiculo Particular",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiFurgon, notifiBicicleta)
                                notiDos
                            }

                            tiposUsuario.containsAll(setOf("Bicicleta", "Motocicleta")) -> {
                                ocultarVista(notifiFurgon, notifiVehiculoParticular)
                                notiDos
                            }

                            tiposUsuario.contains("Furgon") -> {
                                mostrarVista(notifiFurgon)
                                notiUno
                            }

                            tiposUsuario.contains("Vehiculo Particular") -> {
                                mostrarVista(notifiVehiculoParticular)
                                notiUno
                            }

                            tiposUsuario.contains("Motocicleta") -> {
                                mostrarVista(notifiMotocicleta)
                                notiUno
                            }

                            tiposUsuario.contains("Bicicleta") -> {
                                mostrarVista(notifiBicicleta)
                                notiUno
                            }

                            else -> 0
                        }
                        if (heigth > 0) {
                            animacionAnchoLinear(
                                notificacionLinear,
                                heigth,
                                1,
                                100L
                            )
                        }
                        val nombre = FirebaseAuth.getInstance().currentUser?.displayName ?: "Usuario"
                        Log.d("FireStore", "Tipos de vehiculos: $nombre, Ancho:$heigth")

                    }
                }
            }
    }

    fun notifiDesplegue(notiUno: Int, notiDos: Int, notiTres: Int, notiCuatro: Int) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", FirebaseAuth.getInstance().currentUser?.displayName ?: "Usuario"
            )
            .addSnapshotListener { documents, e ->
                if(e != null){
                    Toast.makeText(
                        this,
                        "El usuario no tiene vinculados vehiculos",
                        Toast.LENGTH_SHORT
                    ).show()
                }else {
                    if (documents != null && !documents.isEmpty) {
                        val tiposUsuario = mutableSetOf<String>()
                        for (document in documents) {
                            if (document.exists()) {
                                document.getString("tipo")?.let { tiposUsuario.add(it) }
                            }
                        }
                        if (tiposUsuario.isEmpty()) {
                            notifiFurgon.text = "No tiene un vehiculo asignado"
                            mostrarVista(notifiFurgon)
                            animacionAnchoLinear(notificacionLinear, 1, notiUno, 100L)
                            return@addSnapshotListener
                        }
                        val heigth = when {
                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Vehiculo Particular",
                                    "Bicicleta",
                                    "Motocicleta"
                                )
                            ) -> notiCuatro

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Vehiculo Particular",
                                    "Bicicleta"
                                )
                            ) -> notiTres

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Vehiculo Particular",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiBicicleta)
                                notiTres
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Bicicleta",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiVehiculoParticular)
                                notiTres
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Vehiculo Particular",
                                    "Bicicleta",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiFurgon)
                                notiTres
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Vehiculo Particular"
                                )
                            ) -> notiDos

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Bicicleta"
                                )
                            ) -> {
                                ocultarVista(notifiVehiculoParticular)
                                notiDos
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Furgon",
                                    "Motocicleta")
                            ) -> {
                                ocultarVista(notifiBicicleta, notifiVehiculoParticular)
                                notiDos
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Vehiculo Particular",
                                    "Bicicleta"
                                )
                            ) -> {
                                ocultarVista(notifiFurgon)
                                notiDos
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Vehiculo Particular",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiFurgon, notifiBicicleta)
                                notiDos
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Bicicleta",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiFurgon, notifiVehiculoParticular)
                                notiDos
                            }

                            tiposUsuario.containsAll(
                                setOf(
                                    "Bicicleta",
                                    "Motocicleta"
                                )
                            ) -> {
                                ocultarVista(notifiFurgon, notifiVehiculoParticular)
                                notiDos
                            }

                            tiposUsuario.contains("Furgon") -> {
                                mostrarVista(notifiFurgon)
                                notiUno
                            }

                            tiposUsuario.contains("Vehiculo Particular") -> {
                                mostrarVista(notifiVehiculoParticular)
                                notiUno
                            }

                            tiposUsuario.contains("Motocicleta") -> {
                                mostrarVista(notifiMotocicleta)
                                notiUno
                            }

                            tiposUsuario.contains("Bicicleta") -> {
                                mostrarVista(notifiBicicleta)
                                notiUno
                            }
                            else -> 0
                        }
                        if (heigth > 0) {
                            animacionAnchoLinear(notificacionLinear, 1, heigth, 100L)
                        }
                    }
                }
            }
    }

    fun ocultarVista(vararg views: View) {
        views.forEach { view ->
            view.layoutParams.width = 0
            view.layoutParams.height = 0
            view.requestLayout()
        }
    }

    fun mostrarVista(vararg views: View) {
        val anchor = resources.displayMetrics.widthPixels
        views.forEach { view ->
            when{
                anchor <= 590 -> {
                    view.layoutParams.width = 300
                    view.layoutParams.height = 55
                    view.requestLayout()
                }
                anchor in 591 .. 1300 -> {
                    view.layoutParams.width = 600
                    view.layoutParams.height = 105
                    view.requestLayout()
                }
                anchor in 0..1301 -> {
                    view.layoutParams.width = 650
                    view.layoutParams.height = 150
                    view.requestLayout()
                }
                else -> return
            }
        }
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
