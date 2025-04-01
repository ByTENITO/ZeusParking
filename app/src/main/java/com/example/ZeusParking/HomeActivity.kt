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
    private var cambioAnimacion = true

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
        crearCanalNotificacion(this)


        //Menu de Navegaion
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNavigationView.selectedItemId) {
                return@setOnItemSelectedListener true  // Evita recargar la misma actividad
            }

            when (item.itemId) {

                R.id.home -> {
                    startActivity(Intent(this, HomeActivity::class.java))  // Llama a AuthActivity
                    overridePendingTransition(0, 0)  // Evita la animación de transición
                    finish()  // Finaliza la actividad actual si no deseas que quede en la pila
                }
                R.id.localizacion -> {
                    startActivity(Intent(this, Localizacion::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.registro -> {
                    startActivity(Intent(this, RegistrarBiciActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.qr -> {
                    startActivity(Intent(this, QrActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
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
                                database.collection("Disponibilidad")
                                    .document("0ctYNlFXwtVw9ylURFXi")
                                    .addSnapshotListener { document, e ->
                                        if (e != null) {
                                            Log.d(
                                                "FireStore",
                                                "Error al escuchar los datos: 0ctYNlFXwtVw9ylURFXi",
                                                e
                                            )
                                            return@addSnapshotListener
                                        }
                                        if (document != null && document.exists()) {
                                            val espacios = document.getLong("Furgon") ?: 0
                                            Log.d(
                                                "FireStore",
                                                "Valor modificado:'$tipoVehiculo -> $espacios'"
                                            )
                                            if (espacios.toInt() == 0 ) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, no quedan espacios para $tipoVehiculo "
                                                )
                                            }
                                            if (espacios.toInt() >= 1 && espacios.toInt() <= 2) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, quedan pocos espacios en el parquedero para $tipoVehiculo, quedan: $espacios"
                                                )
                                            }
                                            if (espacios.toInt() >= 3 && espacios.toInt() <= 4) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, quedan $espacios espacios para $tipoVehiculo"
                                                )
                                            }
                                        }
                                    }
                            }
                            if (tipoVehiculo == "Vehiculo Particular") {
                                database.collection("Disponibilidad")
                                    .document("UF0tfabGHGitcj7En6Wy")
                                    .addSnapshotListener { document, e ->
                                        if (e != null) {
                                            Log.d(
                                                "FireStore",
                                                "Error al escuchar los datos: UF0tfabGHGitcj7En6Wy",
                                                e
                                            )
                                            return@addSnapshotListener
                                        }
                                        if (document != null && document.exists()) {
                                            val espacios =
                                                document.getLong(tipoVehiculo) ?: 0
                                            Log.d(
                                                "FireStore",
                                                "Valor modificado:'$tipoVehiculo -> $espacios'"
                                            )
                                            if (espacios.toInt() == 0) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, no quedan espacios para $tipoVehiculo "
                                                )
                                            }
                                            if (espacios.toInt() >= 1 && espacios.toInt() <= 4) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, quedan pocos espacios en el parquedero para $tipoVehiculo, quedan: $espacios"
                                                )
                                            }
                                            if (espacios.toInt() >= 5 && espacios.toInt() <= 7) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, quedan $espacios espacios para $tipoVehiculo"
                                                )
                                            }
                                        }
                                    }
                            }
                            if (tipoVehiculo == "Bicicleta") {
                                database.collection("Disponibilidad")
                                    .document("IuDC5XlTyhxhqU4It8SD")
                                    .addSnapshotListener { document, e ->
                                        if (e != null) {
                                            Log.d(
                                                "FireStore",
                                                "Error al escuchar los datos: IuDC5XlTyhxhqU4It8SD",
                                                e
                                            )
                                            return@addSnapshotListener
                                        }
                                        if (document != null && document.exists()) {
                                            val espacios = document.getLong(tipoVehiculo) ?: 0
                                            Log.d(
                                                "FireStore",
                                                "Valor modificado:'$tipoVehiculo -> $espacios'"
                                            )
                                            if (espacios.toInt() == 0) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, no quedan espacios para $tipoVehiculo "
                                                )
                                            }
                                            if (espacios.toInt() >= 1 && espacios.toInt() <= 4) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, corre quedan pocos espacios en el parquedero para $tipoVehiculo, quedan: $espacios"
                                                )
                                            }
                                            if (espacios.toInt() >= 5 && espacios.toInt() <= 8) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, quedan $espacios espacios para $tipoVehiculo"
                                                )
                                            }
                                        }
                                    }
                            }
                            if (tipoVehiculo == "Motocicleta") {
                                database.collection("Disponibilidad")
                                    .document("ntHgnXs4Qbz074siOrvz")
                                    .addSnapshotListener { document, e ->
                                        if (e != null) {
                                            Log.d(
                                                "FireStore",
                                                "Error al escuchar los datos: ntHgnXs4Qbz074siOrvz",
                                                e
                                            )
                                            return@addSnapshotListener
                                        }
                                        if (document != null && document.exists()) {
                                            val espacios = document.getLong(tipoVehiculo) ?: 0
                                            Log.d(
                                                "FireStore",
                                                "Valor modificado:'$tipoVehiculo -> $espacios'"
                                            )
                                            if (espacios.toInt() == 0 ) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, no quedan espacios para $tipoVehiculo "
                                                )
                                            }
                                            if (espacios.toInt() >= 1 && espacios.toInt() <= 4) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, quedan pocos espacios en el parquedero para $tipoVehiculo, quedan: $espacios"
                                                )
                                            }
                                            if (espacios.toInt() >= 5 && espacios.toInt() <= 8) {
                                                mostrarNotificacion(
                                                    this,
                                                    "ZeusParking",
                                                    "Hola, quedan $espacios espacios para $tipoVehiculo"
                                                )
                                            }
                                        }
                                    }
                            }
                        }
                    }
                }
            }

        val ancho = resources.displayMetrics.widthPixels
        val alto = resources.displayMetrics.heightPixels

        overridePendingTransition(0, 0)

        if (alto >= 3001) {
            responsividad(menuOp, 200, 200)
        }
        if (alto in 2501..3000) {
            responsividad(menuOp, 200, 200)
        }
        if (alto in 1301..2500) {
            responsividad(menuOp, 140, 140)
        }
        if (alto in 1081..1300) {
            responsividad(menuOp, 80, 80)
        }
        if (alto <= 1080) {
            responsividad(menuOp, 60, 60)
        }

        if (ancho >= 3001) {
            responsividadText(Correo_TV, 3500)
        }
        if (ancho in 2501..3000) {
            responsividadText(Correo_TV, 2500)
        }
        if (ancho in 1081..2500) {
            responsividadText(Correo_TV, 2000)
        }
        if (ancho in 721..1080) {
            responsividadText(Correo_TV, 900)
        }
        if (ancho <= 720) {
            responsividadText(Correo_TV, 580)
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
                menuOp
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
                if (altura >= 3001) {
                    animacionAnchoLinear(opciones, 1, 930, 200L)
                    responsividad(entradaButton, grande, grande)
                    responsividad(localizacionButton, grande, grande)
                    responsividad(registrarBiciButton, grande, grande)
                    responsividad(cerrarSesion, grande, grande)
                }
                if (altura in 2501..3000) {
                    animacionAnchoLinear(opciones, 1, 505, 200L)
                    responsividad(entradaButton, Alto, Alto)
                    responsividad(localizacionButton, Alto, Alto)
                    responsividad(registrarBiciButton, Alto, Alto)
                    responsividad(cerrarSesion, Alto, Alto)
                }
                if (altura in 1301..2500) {
                    animacionAnchoLinear(opciones, 1, 630, 200L)
                    responsividad(entradaButton, medianoAlto, medianoAlto)
                    responsividad(localizacionButton, medianoAlto, medianoAlto)
                    responsividad(registrarBiciButton, medianoAlto, medianoAlto)
                    responsividad(cerrarSesion, medianoAlto, medianoAlto)
                }
                if (altura in 1081..1300) {
                    animacionAnchoLinear(opciones, 1, 310, 200L)
                    responsividad(entradaButton, mediano, mediano)
                    responsividad(localizacionButton, mediano, mediano)
                    responsividad(registrarBiciButton, mediano, mediano)
                    responsividad(cerrarSesion, mediano, mediano)
                }
                if (altura <= 1080) {
                    animacionAnchoLinear(opciones, 1, 210, 200L)
                    responsividad(entradaButton, pequeño, pequeño)
                    responsividad(localizacionButton, pequeño, pequeño)
                    responsividad(registrarBiciButton, pequeño, pequeño)
                    responsividad(cerrarSesion, pequeño, pequeño)
                }
            }
            if (!cambioAnimacion) {
                if (altura >= 3001) {
                    animacionAnchoLinear(opciones, 930, 1, 200L)
                }
                if (altura in 2501..3000) {
                    animacionAnchoLinear(opciones, 505, 1, 200L)
                }
                if (altura in 1301..2500) {
                    animacionAnchoLinear(opciones, 630, 1, 200L)
                }
                if (altura in 1081..1300) {
                    animacionAnchoLinear(opciones, 310, 1, 200L)
                }
                if (altura <= 1080) {
                    animacionAnchoLinear(opciones, 210, 1, 200L)
                }
            }
            cambioAnimacion = !cambioAnimacion
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
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(Color.BLUE)
            .setAutoCancel(true)

        val notificationId =
            System.currentTimeMillis().toInt() // Genera un ID único basado en el tiempo
        notificationManager.notify(notificationId, builder.build())
    }
}
