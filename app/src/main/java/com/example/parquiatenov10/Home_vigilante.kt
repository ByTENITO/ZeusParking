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

class Home_vigilante : AppCompatActivity() {
    // Variables y vistas
    private lateinit var cerrarSesion_vigi: Button
    private lateinit var entrada_vigi: Button
    private lateinit var disponibilidad_vigi: Button
    private lateinit var salida_vigi: Button
    private lateinit var perfil_vigi: ImageView
    private lateinit var Bienvenida_vigi: TextView
    private lateinit var usuario_vigi:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_vigilante)

        // Setup de vistas
        cerrarSesion_vigi = findViewById(R.id.CerrarSesion_vigi)
        entrada_vigi = findViewById(R.id.Entrada_vigi)
        disponibilidad_vigi = findViewById(R.id.Disponibilidad_vigi)
        salida_vigi = findViewById(R.id.Salida_vigi)
        perfil_vigi = findViewById(R.id.FotoPerfil_vigi)
        Bienvenida_vigi= findViewById(R.id.Bienvenida_vigi)
        usuario_vigi = findViewById(R.id.Usuario_vigi)

        // Obtener datos del intent
        val bundle: Bundle? = intent.extras
        val inputCorreo: String? = bundle?.getString("inputCorreo")
        val provider = bundle?.getString("provider")
        val fotoPerfilUrl: String? = bundle?.getString("foto_perfil_url")

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

    // Función para cargar la foto de perfil desde la URL
    private fun loadProfilePicture_vigi(fotoUrl: String) {
        // Usar Picasso para cargar la imagen desde la URL
        Picasso.get().load(fotoUrl).into(perfil_vigi)
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
