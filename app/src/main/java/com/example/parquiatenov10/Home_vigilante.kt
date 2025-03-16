package com.example.parquiatenov10

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import com.squareup.picasso.Picasso  // Si usas Picasso

class Home_vigilante : AppCompatActivity() {
    // Variables y vistas
    private lateinit var cerrarSesion_vigi: ImageView
    private lateinit var entrada_vigi: ImageView
    private lateinit var disponibilidad_vigi: ImageView
    private lateinit var salida_vigi: ImageView
    private lateinit var perfil_vigi: ImageView
    private lateinit var menuVig: ImageView
    private lateinit var consultaVigi: ImageView
    private lateinit var Bienvenida_vigi: TextView
    private lateinit var usuario_vigi:TextView
    private lateinit var texto:TextView
    private lateinit var opcionesVigi: LinearLayout
    private var cambioAnimacion = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startAnimationsWithDelay()
        setContentView(R.layout.activity_home_vigilante)
        // Setup de vistas
        perfil_vigi = findViewById(R.id.FotoPerfil_vigi)
        Bienvenida_vigi= findViewById(R.id.Bienvenida_vigi)
        usuario_vigi = findViewById(R.id.Usuario_vigi)
        texto = findViewById(R.id.textView2_vigi)
        consultaVigi = findViewById(R.id.consulta)
        entrada_vigi = findViewById(R.id.Entrada_vigi)
        salida_vigi = findViewById(R.id.Salida_vigi)
        disponibilidad_vigi = findViewById(R.id.Disponibilidad_vigi)
        cerrarSesion_vigi = findViewById(R.id.CerrarSesion_vigi)
        menuVig = findViewById(R.id.menuVigi)
        opcionesVigi = findViewById(R.id.menuOpcionesVigi)

        // Obtener datos del intent
        val bundle: Bundle? = intent.extras
        val inputCorreo: String? = bundle?.getString("inputCorreo")
        val provider = bundle?.getString("provider")
        val fotoPerfilUrl: String? = bundle?.getString("foto_perfil_url")
        val sharedPref = getSharedPreferences("MisDatos", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("nombreUsuario", inputCorreo)
            apply()
        }
        val alto = resources.displayMetrics.heightPixels
        overridePendingTransition( 0,0)

        if (alto>=3001){
            responsividad(menuVig,200,200)
        }
        if (alto in 2501..3000){
            responsividad(menuVig,200,200)
        }
        if (alto in 1301..2500){
            responsividad(menuVig,140,140)
        }
        if (alto in 1081..1300){
            responsividad(menuVig,80,80)
        }
        if (alto<=1080){
            responsividad(menuVig,60,60)
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

    private fun responsividad(view: View, width: Int, heigth: Int){
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

        menuVig.setOnClickListener{
            val altura = resources.displayMetrics.heightPixels
            val pequeño = 60
            val mediano = 80
            val medianoAlto = 140
            val Alto = 200
            val grande = 200
            if(cambioAnimacion){
                if (altura>=3001){
                    animacionAnchoLinear(opcionesVigi,1, 1130, 200L)
                    responsividad(consultaVigi,grande,grande)
                    responsividad(entrada_vigi,grande,grande)
                    responsividad(salida_vigi,grande,grande)
                    responsividad(disponibilidad_vigi,grande,grande)
                    responsividad(cerrarSesion_vigi,grande,grande)
                }
                if (altura in 2501..3000){
                    animacionAnchoLinear(opcionesVigi,1, 705, 200L)
                    responsividad(consultaVigi,Alto,Alto)
                    responsividad(entrada_vigi,Alto,Alto)
                    responsividad(salida_vigi,Alto,Alto)
                    responsividad(disponibilidad_vigi,Alto,Alto)
                    responsividad(cerrarSesion_vigi,Alto,Alto)
                }
                if (altura in 1301..2500){
                    animacionAnchoLinear(opcionesVigi,1, 770, 200L)
                    responsividad(consultaVigi,medianoAlto,medianoAlto)
                    responsividad(entrada_vigi,medianoAlto,medianoAlto)
                    responsividad(salida_vigi,medianoAlto,medianoAlto)
                    responsividad(disponibilidad_vigi,medianoAlto,medianoAlto)
                    responsividad(cerrarSesion_vigi,medianoAlto,medianoAlto)
                }
                if (altura in 1081..1300){
                    animacionAnchoLinear(opcionesVigi,1,390 , 200L)
                    responsividad(consultaVigi,mediano,mediano)
                    responsividad(entrada_vigi,mediano,mediano)
                    responsividad(salida_vigi,mediano,mediano)
                    responsividad(disponibilidad_vigi,mediano,mediano)
                    responsividad(cerrarSesion_vigi,mediano,mediano)
                }
                if (altura<=1080){
                    animacionAnchoLinear(opcionesVigi,1,270 , 200L)
                    responsividad(consultaVigi,pequeño,pequeño)
                    responsividad(entrada_vigi,pequeño,pequeño)
                    responsividad(salida_vigi,pequeño,pequeño)
                    responsividad(disponibilidad_vigi,pequeño,pequeño)
                    responsividad(cerrarSesion_vigi,pequeño,pequeño)
                }
            }
            if(!cambioAnimacion){
                if (altura>=3001){
                    animacionAnchoLinear(opcionesVigi,1130, 1, 200L)
                }
                if (altura in 2501..3000){
                    animacionAnchoLinear(opcionesVigi,705, 1, 200L)
                }
                if (altura in 1301..2500){
                    animacionAnchoLinear(opcionesVigi,770,1, 200L)
                }
                if (altura in 1081..1300){
                    animacionAnchoLinear(opcionesVigi,390,1, 200L)
                }
                if (altura<=1080){
                    animacionAnchoLinear(opcionesVigi,270,1 , 200L)
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
            finish()
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

        disponibilidad_vigi.setOnClickListener {
            startActivity(Intent(this, DisponibilidadParqueadero::class.java))
        }
    }
}
