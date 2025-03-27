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
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso  // Si usas Picasso

class Home_vigilante : AppCompatActivity() {
    // Variables y vistas
    private var database = FirebaseFirestore.getInstance()
    private lateinit var cerrarSesion_vigi: ImageView
    private lateinit var entrada_vigi: ImageView
    private lateinit var salida_vigi: ImageView
    private lateinit var perfil_vigi: ImageView
    private lateinit var menuVig: ImageView
    private lateinit var consultaVigi: ImageView
    private lateinit var Bienvenida_vigi: TextView
    private lateinit var usuario_vigi: TextView
    private lateinit var texto: TextView
    private lateinit var opcionesVigi: LinearLayout
    private var cambioAnimacion = true

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
        consultaVigi = findViewById(R.id.consulta)
        entrada_vigi = findViewById(R.id.Entrada_vigi)
        salida_vigi = findViewById(R.id.Salida_vigi)
        cerrarSesion_vigi = findViewById(R.id.CerrarSesion_vigi)
        menuVig = findViewById(R.id.menuVigi)
        opcionesVigi = findViewById(R.id.menuOpcionesVigi)
        crearCanalNotificacion(this)

        database.collection("Disponibilidad")
            .document("0ctYNlFXwtVw9ylURFXi")
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.d("FireStore", "Error al escuchar los datos: 0ctYNlFXwtVw9ylURFXi", e)
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val espacios = document.getLong("Furgon") ?: 0
                    Log.d("FireStore", "Valor modificado:'Furgon -> $espacios'")
                    if (espacios.toInt() == 0) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking",
                            "Hola, no quedan espacios para Furgon"
                        )
                    }
                    if (espacios.toInt() >= 1 && espacios.toInt() <= 2) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking",
                            "Hola, quedan pocos espacios en el parquedero de Furgon\n quedan: $espacios"
                        )
                    }
                    if (espacios.toInt() >= 3 && espacios.toInt() <= 4) {
                        mostrarNotificacion(this, "ZeusParking", "Hola, quedan $espacios espacios para Furgon")
                    }
                }
            }
        database.collection("Disponibilidad")
            .document("UF0tfabGHGitcj7En6Wy")
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.d("FireStore", "Error al escuchar los datos: UF0tfabGHGitcj7En6Wy", e)
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val espacios = document.getLong("Vehiculo Particular") ?: 0
                    Log.d("FireStore", "Valor modificado:'Vehiculo Particular -> $espacios'")
                    if (espacios.toInt() == 0 ) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking",
                            "Hola, no quedan espacios para Vehiculos Particulares"
                        )
                    }
                    if (espacios.toInt() >= 1 && espacios.toInt() <= 4) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking",
                            "Hola, quedan pocos espacios en el parquedero de Vehiculo Particular\n quedan: $espacios"
                        )
                    }
                    if (espacios.toInt() >= 5 && espacios.toInt() <= 8) {
                        mostrarNotificacion(this, "ZeusParking", "Hola, quedan $espacios espacios para Vehiculo Particular")
                    }
                }
            }
        database.collection("Disponibilidad")
            .document("IuDC5XlTyhxhqU4It8SD")
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.d("FireStore", "Error al escuchar los datos: IuDC5XlTyhxhqU4It8SD", e)
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val espacios = document.getLong("Bicicleta") ?: 0
                    Log.d("FireStore", "Valor modificado:'Bicicleta -> $espacios'")
                    if (espacios.toInt() == 0 ) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking",
                            "Hola, no quedan espacios para Bicicleta, quedan: $espacios"
                        )
                    }
                    if (espacios.toInt() >= 1 && espacios.toInt() <= 5) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking",
                            "Hola, corre quedan pocos espacios en el parquedero de Bicicleta, quedan: $espacios"
                        )
                    }
                    if (espacios.toInt() >= 6 && espacios.toInt() <= 10) {
                        mostrarNotificacion(this, "ZeusParking", "Hola, quedan $espacios espacios para Bicicleta")
                    }
                }
            }
        database.collection("Disponibilidad")
            .document("ntHgnXs4Qbz074siOrvz")
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.d("FireStore", "Error al escuchar los datos: ntHgnXs4Qbz074siOrvz", e)
                    return@addSnapshotListener
                }
                if (document != null && document.exists()) {
                    val espacios = document.getLong("Motocicleta") ?: 0
                    Log.d("FireStore", "Valor modificado:'Motocicleta -> $espacios'")
                    if (espacios.toInt() == 0 ) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking",
                            "Hola, no quedan espacios para Motocicletas, quedan: $espacios"
                        )
                    }
                    if (espacios.toInt() >= 1 && espacios.toInt() <= 3) {
                        mostrarNotificacion(
                            this,
                            "ZeusParking",
                            "Hola, quedan pocos espacios en el parquedero de Motocicleta, quedan: $espacios"
                        )
                    }
                    if (espacios.toInt() >= 4 && espacios.toInt() <= 7) {
                        mostrarNotificacion(this, "ZeusParking", "Hola, quedan $espacios espacios para Motocicleta")
                    }
                }
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

        val alto = resources.displayMetrics.heightPixels
        overridePendingTransition(0, 0)

        if (alto >= 3001) {
            responsividad(menuVig, 200, 200)
        }
        if (alto in 2501..3000) {
            responsividad(menuVig, 200, 200)
        }
        if (alto in 1301..2500) {
            responsividad(menuVig, 140, 140)
        }
        if (alto in 1081..1300) {
            responsividad(menuVig, 80, 80)
        }
        if (alto <= 1080) {
            responsividad(menuVig, 60, 60)
        }

        // Comprobar si el proveedor es Google
        if (provider == ProviderType.GOOGLE.name && inputCorreo != null && fotoPerfilUrl != null) {
            setup_vigi(inputCorreo)
            loadProfilePicture_vigi(fotoPerfilUrl)
        } else if (inputCorreo != null) {
            setup_vigi(inputCorreo)
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
                menuVig
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

        menuVig.setOnClickListener {
            val altura = resources.displayMetrics.heightPixels
            val pequeño = 60
            val mediano = 80
            val medianoAlto = 140
            val Alto = 200
            val grande = 200
            if (cambioAnimacion) {
                if (altura >= 3001) {
                    animacionAnchoLinear(opcionesVigi, 1, 930, 200L)
                    responsividad(consultaVigi, grande, grande)
                    responsividad(entrada_vigi, grande, grande)
                    responsividad(salida_vigi, grande, grande)
                    responsividad(cerrarSesion_vigi, grande, grande)
                }
                if (altura in 2501..3000) {
                    animacionAnchoLinear(opcionesVigi, 1, 505, 200L)
                    responsividad(consultaVigi, Alto, Alto)
                    responsividad(entrada_vigi, Alto, Alto)
                    responsividad(salida_vigi, Alto, Alto)
                    responsividad(cerrarSesion_vigi, Alto, Alto)
                }
                if (altura in 1301..2500) {
                    animacionAnchoLinear(opcionesVigi, 1, 630, 200L)
                    responsividad(consultaVigi, medianoAlto, medianoAlto)
                    responsividad(entrada_vigi, medianoAlto, medianoAlto)
                    responsividad(salida_vigi, medianoAlto, medianoAlto)
                    responsividad(cerrarSesion_vigi, medianoAlto, medianoAlto)
                }
                if (altura in 1081..1300) {
                    animacionAnchoLinear(opcionesVigi, 1, 310, 200L)
                    responsividad(consultaVigi, mediano, mediano)
                    responsividad(entrada_vigi, mediano, mediano)
                    responsividad(salida_vigi, mediano, mediano)
                    responsividad(cerrarSesion_vigi, mediano, mediano)
                }
                if (altura <= 1080) {
                    animacionAnchoLinear(opcionesVigi, 1, 210, 200L)
                    responsividad(consultaVigi, pequeño, pequeño)
                    responsividad(entrada_vigi, pequeño, pequeño)
                    responsividad(salida_vigi, pequeño, pequeño)
                    responsividad(cerrarSesion_vigi, pequeño, pequeño)
                }
            }
            if (!cambioAnimacion) {
                if (altura >= 3001) {
                    animacionAnchoLinear(opcionesVigi, 930, 1, 200L)
                }
                if (altura in 2501..3000) {
                    animacionAnchoLinear(opcionesVigi, 505, 1, 200L)
                }
                if (altura in 1301..2500) {
                    animacionAnchoLinear(opcionesVigi, 630, 1, 200L)
                }
                if (altura in 1081..1300) {
                    animacionAnchoLinear(opcionesVigi, 310, 1, 200L)
                }
                if (altura <= 1080) {
                    animacionAnchoLinear(opcionesVigi, 210, 1, 200L)
                }
            }
            cambioAnimacion = !cambioAnimacion
        }

        // Configuración de los botones
        cerrarSesion_vigi.setOnClickListener {
            // Borrar datos guardados
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            editor.apply()

            // Cerrar sesión en Firebase y finalizar la actividad
            FirebaseAuth.getInstance().signOut()
            //se reemplazo esta accion ya que si se utiliza el funcion finish() esta volvera a la anterior actiidad utilizada, por lo que esta volvera a la actividad que le apuntamos
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }


        // Otros botones
        consultaVigi.setOnClickListener {
            startActivity(Intent(this, QrActivity::class.java))
        }

        entrada_vigi.setOnClickListener {
            startActivity(Intent(this, EntradaQrParqueadero::class.java))
        }

        salida_vigi.setOnClickListener {
            startActivity(Intent(this, SalidaQrParqueadero::class.java))
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
