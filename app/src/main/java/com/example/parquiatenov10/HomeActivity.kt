package com.example.parquiatenov10

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        setContentView(R.layout.activity_home)

        // Setup de vistas
        Correo_TV = findViewById(R.id.Correo_TV)
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

        // Comprobar si el proveedor es Google
        if (provider == ProviderType.GOOGLE.name && email != null && fotoPerfilUrl != null) {
            setup(email)
            loadProfilePicture(fotoPerfilUrl)
        } else if (email != null) {
            setup(email)
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
            finish()
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
