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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class Home_vigilante : AppCompatActivity() {
    // Variables y vistas
    private var database = FirebaseFirestore.getInstance()
    private lateinit var perfil_vigi: ImageView
    private lateinit var Bienvenida_vigi: TextView
    private lateinit var usuario_vigi: TextView
    private lateinit var texto: TextView
    private lateinit var notificacionLinear: LinearLayout
    private lateinit var notificaciones: ImageView
    private lateinit var notifiFurgon: TextView
    private lateinit var notifiVehiculoParticular: TextView
    private lateinit var notifiBicicleta: TextView
    private lateinit var notifiMotocicleta: TextView

    private var cambioAnimacionNoti = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startAnimationsWithDelay()
        setContentView(R.layout.activity_home_vigilante)
        // Setup de vistas
        perfil_vigi = findViewById(R.id.FotoPerfil_vigi)
        Bienvenida_vigi = findViewById(R.id.Bienvenida_vigi)
        usuario_vigi = findViewById(R.id.Usuario_vigi)
        texto = findViewById(R.id.textView2_vigi)
        notificacionLinear = findViewById(R.id.notificacionesVigi)
        notificaciones = findViewById(R.id.notiVigi)
        notifiFurgon = findViewById(R.id.notiFurgon)
        notifiVehiculoParticular = findViewById(R.id.notiVehiculo)
        notifiBicicleta = findViewById(R.id.notiBicicleta)
        notifiMotocicleta = findViewById(R.id.notiMoto)

        crearCanalNotificacion(this)

        //Menu de Navegacion
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_vigi)

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId == bottomNavigationView.selectedItemId) {
                return@setOnItemSelectedListener true
            }

            when (item.itemId) {
                R.id.Entrada -> {
                    val intent = Intent(this, EntradaQrParqueadero::class.java)
                    startActivity(intent)
                }

                R.id.Salida -> {
                    val intent = Intent(this, SalidaQrParqueadero::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        // Obtener datos del intent
        val bundle: Bundle? = intent.extras
        val inputCorreo: String? = bundle?.getString("inputCorreo")
        val email: String? = bundle?.getString("email")
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

        // Comprobar si el proveedor es Google
        if (provider == ProviderType.GOOGLE.name && inputCorreo != null && fotoPerfilUrl != null) {
            setup_vigi(inputCorreo)
            loadProfilePicture_vigi(fotoPerfilUrl)
        } else if (inputCorreo != null) {
            setup_vigi(inputCorreo)
        }
        if (fotoPerfilUrl.isNullOrEmpty()) {
            Log.e("CargaImagen", "La URL de la imagen es nula o vacía.")
        } else {
            Log.d("CargaImagen", "URL de la imagen recibida: $fotoPerfilUrl")
        }

        // Guardar email en preferencias
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("email", inputCorreo)
        editor.apply()
    }

    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            listOf(
                usuario_vigi,
                texto,
                Bienvenida_vigi,
                notificaciones
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 0) // Ajusta el tiempo de retraso si es necesario
    }

    // Función para cargar la foto de perfil desde la URL
    private fun loadProfilePicture_vigi(fotoUrl: String) {
        // Usar Picasso para cargar la imagen desde la URL
        Picasso.get().load(fotoUrl).into(perfil_vigi)
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
    private fun setup_vigi(inputCorreo: String) {
        title = "Inicio"
        usuario_vigi.text = inputCorreo

        // Obtener el nombre completo del usuario desde Firebase si el proveedor es Google
        val user = FirebaseAuth.getInstance().currentUser
        val displayName = user?.displayName
        val nameParts = displayName?.split(" ")

        // Si tiene nombre y apellido, muestra el saludo
        if (nameParts != null && nameParts.size >= 2) {
            val firstName = nameParts[0]
            val lastName = nameParts[1]
            Bienvenida_vigi.text = "Bienvenido, $firstName $lastName"
        } else {
            // Si no tiene nombre, mostrar saludo por defecto
            Bienvenida_vigi.text = "Bienvenido, Bici Usuario"
        }

        notificaciones.setOnClickListener {
            val altura = resources.displayMetrics.heightPixels
            if (cambioAnimacionNoti) {
                when {
                    altura >= 3001 ->{
                        animacionAnchoLinear(notificacionLinear, 1, 725, 200L)
                        mostrarVista(
                            notifiFurgon,
                            notifiVehiculoParticular,
                            notifiMotocicleta,
                            notifiBicicleta
                        )
                    }
                    altura in 2501..3000 -> {
                        animacionAnchoLinear(notificacionLinear, 1, 520, 200L)
                        mostrarVista(
                            notifiFurgon,
                            notifiVehiculoParticular,
                            notifiMotocicleta,
                            notifiBicicleta
                        )
                    }
                    altura in 1301..2500 -> {
                        animacionAnchoLinear(notificacionLinear, 1, 500, 200L)
                        mostrarVista(
                            notifiFurgon,
                            notifiVehiculoParticular,
                            notifiMotocicleta,
                            notifiBicicleta
                        )
                    }
                    altura in 1081..1300 -> {
                        animacionAnchoLinear(notificacionLinear, 1, 260, 200L)
                        mostrarVista(
                            notifiFurgon,
                            notifiVehiculoParticular,
                            notifiMotocicleta,
                            notifiBicicleta
                        )
                    }
                    altura in 0..1080 -> {
                        animacionAnchoLinear(notificacionLinear, 1, 260, 200L)
                        mostrarVista(
                            notifiFurgon,
                            notifiVehiculoParticular,
                            notifiMotocicleta,
                            notifiBicicleta
                        )
                    }
                }
            }
            if (!cambioAnimacionNoti) {
                when{
                    altura >= 3001 -> animacionAnchoLinear(notificacionLinear, 725, 1, 200L)
                    altura in 2501..3000 -> animacionAnchoLinear(notificacionLinear, 520, 1, 200L)
                    altura in 1301..2500 -> animacionAnchoLinear(notificacionLinear, 520, 1, 200L)
                    altura in 1081..1300 -> animacionAnchoLinear(notificacionLinear, 260, 1, 200L)
                    altura in 0..1080 -> animacionAnchoLinear(notificacionLinear, 260, 1, 200L)
                }
            }
            cambioAnimacionNoti = !cambioAnimacionNoti
        }

    }

    fun mostrarVista(vararg views: View) {
        val anchor = resources.displayMetrics.widthPixels
        views.forEach { view ->
            when{
                anchor <= 590 -> {
                    view.layoutParams.width = 300
                    view.layoutParams.height = 60
                    view.requestLayout()
                }
                anchor in 591 .. 1300 -> {
                    view.layoutParams.width = 600
                    view.layoutParams.height = 110
                    view.requestLayout()
                }
                anchor in 0..1301 -> {
                    view.layoutParams.width = 650
                    view.layoutParams.height = 155
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
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
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
