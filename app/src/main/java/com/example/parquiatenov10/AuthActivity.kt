package com.example.parquiatenov10

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import android.widget.Button
import android.widget.EditText
import android.util.Patterns

enum class ProviderType {
    GOOGLE,
    EMAIL
}

class AuthActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    private lateinit var Google_BTN: Button
    private lateinit var Acceder_BTN: Button
    private lateinit var Registrarse_BTN: Button
    private lateinit var Correo_ED: EditText
    private lateinit var Contraseña_ED: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        // Firebase Analytics
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "Integración de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        // Setup de UI y sesión
        auth = FirebaseAuth.getInstance()
        setup()
        session()  // Verifica si ya existe una sesión activa
    }

    private fun session() {
        // Recupera la sesión almacenada en SharedPreferences
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email != null && provider != null) {
            // Si hay una sesión activa, redirige a HomeActivity
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup() {
        title = "Autenticación"

        Google_BTN = findViewById(R.id.Google_BTN)
        Acceder_BTN = findViewById(R.id.Acceder_BTN)
        Registrarse_BTN = findViewById(R.id.Registrarse_BTN)
        Correo_ED = findViewById(R.id.Correo_ED)
        Contraseña_ED = findViewById(R.id.Contraseña_ED)

        // Inicio de sesión con Google
        Google_BTN.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        // Inicio de sesión con email y contraseña
        Acceder_BTN.setOnClickListener {
            val email = Correo_ED.text.toString()
            val password = Contraseña_ED.text.toString()

            if (validateEmail(email) && validatePassword(password)) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            if (user.isEmailVerified) {
                                saveSession(email, ProviderType.EMAIL)
                                showHome(user.email ?: "", ProviderType.EMAIL)
                            } else {
                                showAlert("Por favor, verifica tu correo electrónico.")
                                auth.signOut()
                            }
                        }
                    } else {
                        showAlert("Error en la autenticación. Revisa los datos.")
                    }
                }
            }
        }

        // Registro de usuario
        Registrarse_BTN.setOnClickListener {
            val email = Correo_ED.text.toString()
            val password = Contraseña_ED.text.toString()

            if (validateEmail(email) && validatePassword(password)) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                showAlert("Registro exitoso, ingresa estos mismos datos para iniciar sesión.")
                                Correo_ED.text.clear()
                                Contraseña_ED.text.clear()
                            } else {
                                showAlert("Ups, hubo un problema.")
                            }
                        }
                    } else {
                        showAlert("Error en el registro. Revisa los datos.")
                    }
                }
            }
        }
    }

    private fun saveSession(email: String, provider: ProviderType) {
        // Guarda la sesión en SharedPreferences
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider.name)
        prefs.apply()
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            true
        } else {
            showAlert("Por favor, ingresa un correo electrónico válido.")
            false
        }
    }

    private fun validatePassword(password: String): Boolean {
        return if (password.length >= 6) {
            true
        } else {
            showAlert("La contraseña debe tener al menos 6 caracteres.")
            false
        }
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alerta")
        builder.setMessage(message)
        builder.setPositiveButton("OK", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val authResult = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(authResult.idToken, null)

                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        saveSession(authResult.email ?: "", ProviderType.GOOGLE)
                        showHome(authResult.email ?: "", ProviderType.GOOGLE)
                    } else {
                        showAlert("Error de autenticación con Google")
                    }
                }
            } catch (e: ApiException) {
                showAlert("Error de Google Sign-In: ${e.message}")
            }
        }
    }
}


