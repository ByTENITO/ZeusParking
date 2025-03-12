package com.example.parquiatenov10

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
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
import com.squareup.picasso.Picasso  // Si usas Picasso

class HomeActivity : AppCompatActivity() {
    // Variables y vistas
    private lateinit var Correo_TV: TextView
    private lateinit var Usuario : TextView
    private lateinit var cerrarSesion: ImageView
    private lateinit var registrarBiciButton: ImageView
    private lateinit var entradaButton: ImageView
    private lateinit var disponibilidadButton: ImageView
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
        disponibilidadButton = findViewById(R.id.Disponibilidad_BTN)
        localizacionButton = findViewById(R.id.Localizacion_BTN)
        perfilImageView = findViewById(R.id.FotoPerfil_ImageView)
        BienvenidaTextView = findViewById(R.id.Bienvenida_TV)
        opciones = findViewById(R.id.menuOpciones)
        menuOp = findViewById(R.id.menu)

        // Obtener datos del intent
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        val fotoPerfilUrl: String? = bundle?.getString("foto_perfil_url")
        val sharedPref = getSharedPreferences("MisDatos", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("nombreUsuario", email)
            apply()
        }
        val ancho = resources.displayMetrics.widthPixels
        val alto = resources.displayMetrics.heightPixels

        overridePendingTransition( 0,0)

        if (alto>=3001){
            responsividad(menuOp,200,200)
        }
        if (alto in 2501..3000){
            responsividad(menuOp,200,200)
        }
        if (alto in 1301..2500){
            responsividad(menuOp,140,140)
        }
        if (alto in 1081..1300){
            responsividad(menuOp,80,80)
        }
        if (alto<=1080){
            responsividad(menuOp,60,60)
        }

        if (ancho >= 3001){
            responsividadText(Correo_TV,3500)
        }
        if (ancho in 2501..3000){
            responsividadText(Correo_TV,2500)
        }
        if (ancho in 1081..2500){
            responsividadText(Correo_TV,2000)
        }
        if (ancho in 721..1080){
            responsividadText(Correo_TV,900)
        }
        if (ancho <= 720){
            responsividadText(Correo_TV,580)
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

    private fun responsividadText(view: View,width: Int){
        val textoUsuario = ValueAnimator.ofInt(width)
        textoUsuario.addUpdateListener { animation ->
            val params = view.layoutParams
            params.width = animation.animatedValue as Int
            view.layoutParams = params
        }
        textoUsuario.start()
    }

    private fun responsividad(view: View,width: Int,heigth: Int){
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
            if(cambioAnimacion){
                if (altura>=3001){
                    animacionAnchoLinear(opciones,1, 1130, 200L)
                    responsividad(entradaButton,grande,grande)
                    responsividad(localizacionButton,grande,grande)
                    responsividad(disponibilidadButton,grande,grande)
                    responsividad(registrarBiciButton,grande,grande)
                    responsividad(cerrarSesion,grande,grande)
                }
                if (altura in 2501..3000){
                    animacionAnchoLinear(opciones,1, 705, 200L)
                    responsividad(entradaButton,Alto,Alto)
                    responsividad(localizacionButton,Alto,Alto)
                    responsividad(disponibilidadButton,Alto,Alto)
                    responsividad(registrarBiciButton,Alto,Alto)
                    responsividad(cerrarSesion,Alto,Alto)
                }
                if (altura in 1301..2500){
                    animacionAnchoLinear(opciones,1, 770, 200L)
                    responsividad(entradaButton,medianoAlto,medianoAlto)
                    responsividad(localizacionButton,medianoAlto,medianoAlto)
                    responsividad(disponibilidadButton,medianoAlto,medianoAlto)
                    responsividad(registrarBiciButton,medianoAlto,medianoAlto)
                    responsividad(cerrarSesion,medianoAlto,medianoAlto)
                }
                if (altura in 1081..1300){
                    animacionAnchoLinear(opciones,1,390 , 200L)
                    responsividad(entradaButton,mediano,mediano)
                    responsividad(localizacionButton,mediano,mediano)
                    responsividad(disponibilidadButton,mediano,mediano)
                    responsividad(registrarBiciButton,mediano,mediano)
                    responsividad(cerrarSesion,mediano,mediano)
                }
                if (altura<=1080){
                    animacionAnchoLinear(opciones,1,270 , 200L)
                    responsividad(entradaButton,pequeño,pequeño)
                    responsividad(localizacionButton,pequeño,pequeño)
                    responsividad(disponibilidadButton,pequeño,pequeño)
                    responsividad(registrarBiciButton,pequeño,pequeño)
                    responsividad(cerrarSesion,pequeño,pequeño)
                }
            }
            if(!cambioAnimacion){
                if (altura>=3001){
                    animacionAnchoLinear(opciones,1130, 1, 200L)
                }
                if (altura in 2501..3000){
                    animacionAnchoLinear(opciones,705, 1, 200L)
                }
                if (altura in 1301..2500){
                    animacionAnchoLinear(opciones,770,1, 200L)
                }
                if (altura in 1081..1300){
                    animacionAnchoLinear(opciones,390,1, 200L)
                }
                if (altura<=1080){
                    animacionAnchoLinear(opciones,270,1 , 200L)
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
            //se reemplazo esta accion ya que si se utiliza el funcion finish() esta volvera a la anterior actiidad utilizada, por lo que esta volvera a la actividad que le apuntamos
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }

        // Otros botones
        entradaButton.setOnClickListener {
            startActivity(Intent(this, QrActivity::class.java))
        }

        disponibilidadButton.setOnClickListener {
            startActivity(Intent(this, Disponibilidad::class.java))
        }

        registrarBiciButton.setOnClickListener {
            startActivity(Intent(this, RegistrarBiciActivity::class.java))
        }

        localizacionButton.setOnClickListener {
            startActivity(Intent(this, Localizacion::class.java))
        }
    }
}
