package com.example.parquiatenov10

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog

class HomeActivity : AppCompatActivity() {
    private lateinit var Correo_TV: TextView
    private lateinit var cerrarSesion: Button
    private lateinit var registrarBiciButton: Button // Declara el botón de registrar bici
    private lateinit var entradaButton: Button // Declara el botón Entrada_BTN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Asegúrate de que este método esté bien implementado o quítalo si no es necesario
        setContentView(R.layout.activity_home)

        // Setup
        Correo_TV = findViewById(R.id.Correo_TV) // Referencia al TextView del correo
        cerrarSesion = findViewById(R.id.CerrarSesion)
        registrarBiciButton = findViewById(R.id.RegistrarBici_BTN) // Referencia al botón de registrar bici
        entradaButton = findViewById(R.id.Entrada_BTN) // Referencia al botón Entrada_BTN

        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")

        if (email != null) {
            setup(email)
        } else {
            showAlert()  // Si los datos son nulos, muestra una alerta
        }

        // Guardado de Datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("email", email)
        editor.apply()

        // Agregar el listener al botón para redirigir a RegistrarBiciActivity
        registrarBiciButton.setOnClickListener {
            val intent = Intent(this, RegistrarBiciActivity::class.java)
            startActivity(intent) // Navegar a RegistrarBiciActivity
        }

        // Listener para el botón Entrada_BTN que navega a EntradaQrActivity
        entradaButton.setOnClickListener {
            val intent = Intent(this, EntradaQrActivity::class.java)
            startActivity(intent) // Navegar a EntradaQrActivity
        }
    }

    private fun setup(email: String) {
        title = "Inicio"
        Correo_TV.text = email

        cerrarSesion.setOnClickListener {
            // Borrado de Datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()
            editor.apply()

            FirebaseAuth.getInstance().signOut()
            finish() // Cierra la actividad y regresa a AuthActivity
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Los datos no se recibieron correctamente")
        builder.setPositiveButton("OK", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}

