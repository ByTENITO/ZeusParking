package com.example.parquiatenov10

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import com.squareup.picasso.Picasso  // Si usas Picasso

class HomeActivity : AppCompatActivity() {
    // Variables y vistas
    private lateinit var Correo_TV: TextView
    private lateinit var Usuario : TextView
    private lateinit var cerrarSesion: Button
    private lateinit var registrarBiciButton: Button
    private lateinit var entradaButton: Button
    private lateinit var disponibilidadButton: Button
    private lateinit var localizacionButton: Button
    private lateinit var salidaButton: Button
    private lateinit var perfilImageView: ImageView
    private lateinit var BienvenidaTextView: TextView

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
        salidaButton = findViewById(R.id.Salida_BTN)
        perfilImageView = findViewById(R.id.FotoPerfil_ImageView)
        BienvenidaTextView = findViewById(R.id.Bienvenida_TV)

        // Obtener datos del intent
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        val fotoPerfilUrl: String? = bundle?.getString("foto_perfil_url")

        overridePendingTransition( 0,0)

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
    private fun loadProfilePicture(fotoPerfilUrl: String) {
        // Usar Picasso para cargar la imagen desde la URL
        Picasso.get().load(fotoPerfilUrl).into(perfilImageView)
    }

    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            listOf(
                Correo_TV,
                Usuario,
                cerrarSesion,
                registrarBiciButton,
                entradaButton,
                disponibilidadButton,
                localizacionButton,
                salidaButton,
                BienvenidaTextView
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 0) // Ajusta el tiempo de retraso si es necesario
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
            startActivity(Intent(this, EntradaQrActivity::class.java))
        }

        salidaButton.setOnClickListener {
            startActivity(Intent(this, SalidaQrActivity::class.java))
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
