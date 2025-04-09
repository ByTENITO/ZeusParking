package com.example.parquiatenov10

import android.animation.ValueAnimator
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
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

class HomeActivity : AppCompatActivity() {
    // Variables y vistas
    private var database = FirebaseFirestore.getInstance()
    private lateinit var Correo_TV: TextView
    private lateinit var Usuario: TextView
    private lateinit var cerrarSesion: ImageView
    private lateinit var registrarBiciButton: ImageView
    private lateinit var entradaButton: ImageView
    private lateinit var localizacionButton: ImageView
    private lateinit var perfilImageView: ImageView
    private lateinit var BienvenidaTextView: TextView
    private lateinit var opciones: LinearLayout
    private lateinit var menuOp: ImageView
    private lateinit var notificacionLinear: LinearLayout
    private lateinit var notificaciones: ImageView
    private lateinit var notifiFurgon: TextView
    private lateinit var notifiVehiculoParticular: TextView
    private lateinit var notifiBicicleta: TextView
    private lateinit var notifiMotocicleta: TextView

    private var cambioAnimacion = true
    private var cambioAnimacionNoti = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startAnimationsWithDelay()
        setContentView(R.layout.activity_home)

        // Setup de vistas
        Correo_TV = findViewById(R.id.Correo_TV)
        Usuario = findViewById(R.id.textView2)
        cerrarSesion = findViewById(R.id.CerrarSesion)
        registrarBiciButton = findViewById(R.id.RegistrarBici_BTN)
        entradaButton = findViewById(R.id.Entrada_BTN)
        localizacionButton = findViewById(R.id.Localizacion_BTN)
        perfilImageView = findViewById(R.id.FotoPerfil_ImageView)
        BienvenidaTextView = findViewById(R.id.Bienvenida_TV)
        opciones = findViewById(R.id.menuOpciones)
        menuOp = findViewById(R.id.menu)
        notificacionLinear = findViewById(R.id.notificaciones)
        notificaciones = findViewById(R.id.notificacionVC)
        notifiFurgon = findViewById(R.id.notificacionFurgon)
        notifiVehiculoParticular = findViewById(R.id.notificacionVehiculoParticular)
        notifiBicicleta = findViewById(R.id.notificacionBicicleta)
        notifiMotocicleta = findViewById(R.id.notificacionMotocicleta)

        crearCanalNotificacion(this)


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

        val ancho = resources.displayMetrics.widthPixels
        val alto = resources.displayMetrics.heightPixels

        overridePendingTransition(0, 0)

        when {
            alto >= 3001 -> responsividad(menuOp, 200, 200)
            alto in 2501..3000 -> responsividad(menuOp, 200, 200)
            alto in 1301..2500 -> responsividad(menuOp, 140, 140)
            alto in 1081..1300 -> responsividad(menuOp, 80, 80)
            alto in 0..1080 -> responsividad(menuOp, 60, 60)
            else -> return
        }

        when{
            ancho >= 3001 -> responsividadText(Correo_TV, 3500)
            ancho in 2501..3000 -> responsividadText(Correo_TV, 2500)
            ancho in 1081..2500 -> responsividadText(Correo_TV, 2000)
            ancho in 721..1080 -> responsividadText(Correo_TV, 900)
            ancho in 0..720 -> responsividadText(Correo_TV, 580)
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

    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            listOf(
                Correo_TV,
                Usuario,
                BienvenidaTextView,
                menuOp,
                notificaciones
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 0) // Ajusta el tiempo de retraso si es necesario
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
        Correo_TV.text = email

        // Obtener el nombre completo del usuario desde Firebase si el proveedor es Google
        val user = FirebaseAuth.getInstance().currentUser
        val displayName = user?.displayName
        val nameParts = displayName?.split(" ")

        // Si tiene nombre y apellido, muestra el saludo
        if (nameParts != null && nameParts.size >= 2) {
            val firstName = nameParts[0]
            val lastName = nameParts[1]
            BienvenidaTextView.text = "Bienvenido, $firstName $lastName"
        } else {
            // Si no tiene nombre, mostrar saludo por defecto
            BienvenidaTextView.text = "Bienvenido, Bici Usuario"
        }

        // Configuración de los botones
        menuOp.setOnClickListener {
            val altura = resources.displayMetrics.heightPixels
            val pequeño = 60
            val mediano = 80
            val medianoAlto = 140
            val Alto = 200
            val grande = 200
            if (cambioAnimacion) {
                when {
                    altura >= 3001 ->{
                        animacionAnchoLinear(opciones, 1, 900, 100L)
                        responsividad(entradaButton, grande, grande)
                        responsividad(localizacionButton, grande, grande)
                        responsividad(registrarBiciButton, grande, grande)
                        responsividad(cerrarSesion, grande, grande)
                    }
                    altura in 2501..3000 -> {
                        animacionAnchoLinear(opciones, 1, 860, 100L)
                        responsividad(entradaButton, Alto, Alto)
                        responsividad(localizacionButton, Alto, Alto)
                        responsividad(registrarBiciButton, Alto, Alto)
                        responsividad(cerrarSesion, Alto, Alto)
                    }
                    altura in 1301..2500 -> {
                        animacionAnchoLinear(opciones, 1, 630, 100L)
                        responsividad(entradaButton, medianoAlto, medianoAlto)
                        responsividad(localizacionButton, medianoAlto, medianoAlto)
                        responsividad(registrarBiciButton, medianoAlto, medianoAlto)
                        responsividad(cerrarSesion, medianoAlto, medianoAlto)
                    }
                    altura in 1081..1300 -> {
                        animacionAnchoLinear(opciones, 1, 350, 100L)
                        responsividad(entradaButton, mediano, mediano)
                        responsividad(localizacionButton, mediano, mediano)
                        responsividad(registrarBiciButton, mediano, mediano)
                        responsividad(cerrarSesion, mediano, mediano)
                    }
                    altura in 0..1080 -> {
                        animacionAnchoLinear(opciones, 1, 270, 100L)
                        responsividad(entradaButton, pequeño, pequeño)
                        responsividad(localizacionButton, pequeño, pequeño)
                        responsividad(registrarBiciButton, pequeño, pequeño)
                        responsividad(cerrarSesion, pequeño, pequeño)
                    }
                }
            }
            if (!cambioAnimacion) {
                when{
                    altura >= 3001 -> animacionAnchoLinear(opciones, 900, 1, 100L)
                    altura in 2501..3000 -> animacionAnchoLinear(opciones, 860, 1, 100L)
                    altura in 1301..2500 -> animacionAnchoLinear(opciones, 630, 1, 100L)
                    altura in 1081..1300 -> animacionAnchoLinear(opciones, 350, 1, 100L)
                    altura in 0..1080 -> animacionAnchoLinear(opciones, 270, 1, 100L)
                }
            }
            cambioAnimacion = !cambioAnimacion
        }

        notificaciones.setOnClickListener {
            val altura = resources.displayMetrics.heightPixels
            val pequeñoNoti = listOf(80,140,195,260)
            val medianoNoti = listOf(140,265,390,520)
            val grandeNoti = listOf(230,430,500,725)
            if (cambioAnimacionNoti) {
                when{
                    altura >= 3001 -> notifiDesplegue(grandeNoti[0],grandeNoti[1],grandeNoti[2],grandeNoti[3])
                    altura in 2501..3000 -> notifiDesplegue(medianoNoti[0], medianoNoti[1], medianoNoti[2], medianoNoti[3])
                    altura in 1301..2500 -> notifiDesplegue(medianoNoti[0], medianoNoti[1], medianoNoti[2], medianoNoti[3])
                    altura in 1081..1300 -> notifiDesplegue(pequeñoNoti[0], pequeñoNoti[1], pequeñoNoti[2], pequeñoNoti[3])
                    altura in 0..1080 -> notifiDesplegue(pequeñoNoti[0], pequeñoNoti[1], pequeñoNoti[2], pequeñoNoti[3])
                }
            }
            if (!cambioAnimacionNoti) {
                when{
                    altura >= 3001 -> notifiRepliegue(grandeNoti[0],grandeNoti[1],grandeNoti[2],grandeNoti[3])
                    altura in 2501..3000 -> notifiRepliegue(medianoNoti[0], medianoNoti[1], medianoNoti[2], medianoNoti[3])
                    altura in 1301..2500 -> notifiRepliegue(medianoNoti[0], medianoNoti[1], medianoNoti[2], medianoNoti[3])
                    altura in 1081..1300 -> notifiRepliegue(pequeñoNoti[0], pequeñoNoti[1], pequeñoNoti[2], pequeñoNoti[3])
                    altura in 0..1080 -> notifiRepliegue(pequeñoNoti[0], pequeñoNoti[1], pequeñoNoti[2], pequeñoNoti[3])
                }
            }
            cambioAnimacionNoti = !cambioAnimacionNoti
        }

        cerrarSesion.setOnClickListener {
            // Borrar datos guardados
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            editor.apply()

            // Cerrar sesión en Firebase y finalizar la actividad
            FirebaseAuth.getInstance().signOut()
            /*se reemplazo esta accion ya que si se utiliza el funcion finish() esta
            volvera a la anterior actiidad utilizada, por lo que esta volvera a la actividad
            que le apuntamos
             */
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }


    }

    fun notifiRepliegue(notiUno: Int, notiDos: Int, notiTres: Int, notiCuatro: Int) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", Correo_TV.text.toString())
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
                        Log.d("FireStore", "Tipos de vehiculos: $Usuario, Ancho:$heigth")
                    }
                }
            }
    }

    fun notifiDesplegue(notiUno: Int, notiDos: Int, notiTres: Int, notiCuatro: Int) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", Correo_TV.text.toString())
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

    fun escucharDisponibilidad(documentId: String, campo: String, textoView: TextView, umbrales: List<Pair<IntRange, String>>) {
        database.collection("Disponibilidad")
            .document(documentId)
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.d("Firestore", "Error al escuchar: $documentId", e)
                    return@addSnapshotListener
                }
                val espacios = document?.getLong(campo)?.toInt() ?: 0
                Log.d("Firestore", "Actualizado $campo -> $espacios")
                textoView.text = "Quedan $espacios espacios para $campo"
                for ((rango, mensaje) in umbrales) {
                    if (espacios in rango) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking",
                            mensaje.replace("{espacios}", espacios.toString())
                        )
                        break
                    }
                }
            }
    }
}
