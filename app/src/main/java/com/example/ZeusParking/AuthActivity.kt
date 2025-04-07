@file:Suppress("DEPRECATION")

package com.example.parquiatenov10

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


enum class ProviderType {
    GOOGLE,
    EMAIL
}

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100
    private val inputCorreo = "vigilante@uniminuto.edu.co" //Correo para ingreso a modulo de vigilante
    private val contraseña = "Vigilante123*"
    private lateinit var Google_BTN: Button
    private lateinit var Acceder_BTN: Button
    private lateinit var Registrarse_BTN: Button
    private lateinit var Correo_ED: EditText
    private lateinit var Contraseña_ED: EditText
    private lateinit var ForgotPassword_TV: TextView
    private lateinit var auth: FirebaseAuth
    private var eye = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        //Inicio de firebase
        FirebaseApp.initializeApp(this)

        //ID de botones
        val Google_BTN = findViewById<Button>(R.id.Google_BTN)
        val Acceder_BTN = findViewById<Button>(R.id.Acceder_BTN)
        val Registrarse_BTN = findViewById<Button>(R.id.Registrarse_BTN)
        val Correo_ED = findViewById<EditText>(R.id.Correo_ED)
        val Contraseña_ED = findViewById<EditText>(R.id.Contraseña_ED)
        val ForgotPassword_TV = findViewById<TextView>(R.id.OlvidasteContrasena_TV)
        val text5 = findViewById<TextView>(R.id.textView5)
        val text6 = findViewById<TextView>(R.id.textView6)
        val text7 = findViewById<TextView>(R.id.textView7)
        val logo = findViewById<ImageView>(R.id.imageView8)
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val alto = resources.displayMetrics.heightPixels


        Google_BTN.startAnimation(fadeIn)
        Acceder_BTN.startAnimation(fadeIn)
        Registrarse_BTN.startAnimation(fadeIn)
        Correo_ED.startAnimation(fadeIn)
        Contraseña_ED.startAnimation(fadeIn)
        ForgotPassword_TV.startAnimation(fadeIn)
        text5.startAnimation(fadeIn)
        text6.startAnimation(fadeIn)
        text7.startAnimation(fadeIn)

        bundle.putString("message", "Integración de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        // Solicitar permisos al abrir la app
        if (
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
        ) {
            val permisos = mutableListOf<String>()
            permisos.add(Manifest.permission.CAMERA)
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permisos.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            ActivityCompat.requestPermissions(this, permisos.toTypedArray(), 123)
        }

        if (alto >= 3001) {
            val params = logo.layoutParams
            params.width = 1160 // Ancho en píxeles
            params.height = 1160 // Alto en píxeles
            logo.layoutParams = params
        }

        if (alto in 1301..2500) {
            if (alto >= 2400) {
                val params = logo.layoutParams
                params.width = 761 // Ancho en píxeles
                params.height = 761 // Alto en píxeles
                logo.layoutParams = params
            }
            if (alto in 1841..2399) {
                val params = logo.layoutParams
                params.width = 870 // Ancho en píxeles
                params.height = 870 // Alto en píxeles
                logo.layoutParams = params
            }
            if (alto <= 1840) {
                val params = logo.layoutParams
                params.width = 761 // Ancho en píxeles
                params.height = 761 // Alto en píxeles
                logo.layoutParams = params
            }
        }

        if (alto in 1081..1300) {
            val params = logo.layoutParams
            params.width = 500 // Ancho en píxeles
            params.height = 500 // Alto en píxeles
            logo.layoutParams = params
        }
        if (alto <= 1080) {
            val params = logo.layoutParams
            params.width = 300 // Ancho en píxeles
            params.height = 300 // Alto en píxeles
            logo.layoutParams = params
        }
        overridePendingTransition(0, 0)
        auth = FirebaseAuth.getInstance()
        setup()
        session()
    }


    // Función que calcula un tamaño en píxeles según un porcentaje del alto de la pantalla
    private fun tamañoPantalla(porcentaje: Float): Int {
        val alto = resources.displayMetrics.heightPixels // Obtiene el alto de la pantalla en píxeles
        return (alto * porcentaje).toInt() // Retorna el valor calculado
    }

    // Función que verifica si hay una sesión guardada en las preferencias
    private fun session() {
        val prefs = getSharedPreferences("Sesion", MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val inputCorreo = prefs.getString("inputCorreo", null)
        val provider = prefs.getString("provider", null)

        // Si hay sesión con inputCorreo, lleva al home del vigilante
        if (inputCorreo != null && provider != null) {
            showHomevigi(inputCorreo, ProviderType.valueOf(provider))
        }

        // Si hay sesión con email, lleva al home normal
        if (email != null && provider != null) {
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    // Anima el botón de Google para cambiar su altura visualmente
    private fun tamaños(Google_BTN: Button, startHeight: Int) {
        val animator = ValueAnimator.ofInt(startHeight)

        animator.addUpdateListener { animation ->
            val newHeight = animation.animatedValue as Int
            val params = Google_BTN.layoutParams as ViewGroup.MarginLayoutParams

            params.height = newHeight
            params.bottomMargin = 0
            Google_BTN.layoutParams = params
        }
        animator.start()
    }

    // Configura la vista inicial y eventos de los botones
    private fun setup() {
        val tamañoGoogle = tamañoPantalla(0.05f) // Altura del botón Google según pantalla
        title = "Autenticación"

        // Referencias a los componentes del layout
        Google_BTN = findViewById(R.id.Google_BTN)
        Acceder_BTN = findViewById(R.id.Acceder_BTN)
        Registrarse_BTN = findViewById(R.id.Registrarse_BTN)
        Correo_ED = findViewById(R.id.Correo_ED)
        Contraseña_ED = findViewById(R.id.Contraseña_ED)
        ForgotPassword_TV = findViewById(R.id.OlvidasteContrasena_TV)

        // Aplica la animación de tamaño al botón de Google
        tamaños(Google_BTN, tamañoGoogle)

        // Evento al hacer clic en botón de Google
        Google_BTN.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut() // Asegura cerrar sesión antes
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN) // Inicia login
        }

        // Evento al hacer clic en botón de Acceder
        Acceder_BTN.setOnClickListener {
            val email = Correo_ED.text.toString()
            val password = Contraseña_ED.text.toString()

            // Muestra barra de carga personalizada
            val progressBar = ProgressBar(this).apply {
                isIndeterminate = true
                indeterminateTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this@AuthActivity, R.color.Botones)
                )
                setPadding(0, 30, 0, 30)
            }

            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle("🚦Espera un momento")
                .setMessage("Verificando usuario...")
                .setView(progressBar)
                .setCancelable(false)
                .create()

            dialog.show()

            // Si los datos son válidos...
            if (validateEmail(email) && validatePassword(password)) {
                Acceder_BTN.isEnabled = false

                // Si coinciden con los datos previamente guardados localmente
                if (email == inputCorreo && password == contraseña) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.dismiss()
                        saveSession(inputCorreo, ProviderType.EMAIL)
                        showHomevigi(inputCorreo, ProviderType.EMAIL)
                    }, 2500)
                } else {
                    // Login con Firebase
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        Handler(Looper.getMainLooper()).postDelayed({
                            dialog.dismiss()

                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                // Verifica si el correo está verificado
                                if (user != null && user.isEmailVerified) {
                                    saveSession(email, ProviderType.EMAIL)
                                    showHome(user.email ?: "", ProviderType.EMAIL)
                                } else {
                                    Toast.makeText(this, "Verifica tu correo electrónico", Toast.LENGTH_SHORT).show()
                                    Acceder_BTN.isEnabled = true
                                }
                            } else {
                                Toast.makeText(this, "Error en la autenticación", Toast.LENGTH_SHORT).show()
                                Acceder_BTN.isEnabled = true
                            }
                        }, 2500)
                    }
                }
            } else {
                dialog.dismiss()
                Toast.makeText(this, "Correo o contraseña inválidos", Toast.LENGTH_SHORT).show()
            }
        }

        // Evento para crear un nuevo usuario (registro)
        Registrarse_BTN.setOnClickListener {
            val email = Correo_ED.text.toString()
            val password = Contraseña_ED.text.toString()

            if (validateEmail(email) && validatePassword(password)) {
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        user?.sendEmailVerification()?.addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "Registro exitoso, ingresa estos mismos datos para iniciar sesión.", Toast.LENGTH_SHORT).show()
                                Correo_ED.text.clear()
                                Contraseña_ED.text.clear()
                            } else {
                                Toast.makeText(this, "Ups, hubo un problema.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Error en el registro. Revisa los datos.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Evento para restablecer contraseña
        ForgotPassword_TV.setOnClickListener {
            val email = Correo_ED.text.toString()
            if (validateEmail(email)) {
                ForgotPassword_TV.isEnabled = true
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Se ha enviado un enlace para restablecer la contraseña a tu correo.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Error al enviar el correo de restablecimiento.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Guarda la sesión en preferencias
    private fun saveSession(email: String, provider: ProviderType) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider.name)
        prefs.apply()
    }

    // Valida que el correo tenga formato correcto
    private fun validateEmail(email: String): Boolean {
        return if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            true
        } else {
            Toast.makeText(this, "Por favor, ingresa un correo electrónico válido.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // Valida que la contraseña tenga una estructura segura
    private fun validatePassword(password: String): Boolean {
        val simbolos = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
        return if (password.matches(simbolos)) {
            true
        } else {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial.", Toast.LENGTH_LONG).show()
            false
        }
    }

    // Muestra la pantalla principal del usuario
    private fun showHome(email: String, provider: ProviderType) {
        enableEdgeToEdge()
        val user = auth.currentUser
        val fotoUrl = user?.photoUrl?.toString()

        Handler(Looper.getMainLooper()).postDelayed({
            val homeIntent = Intent(this, HomeActivity::class.java).apply {
                putExtra("email", email)
                putExtra("provider", provider.name)
                putExtra("foto_perfil_url", fotoUrl)
            }
            startActivity(homeIntent)
            overridePendingTransition(0, 0)
        }, 1000)
    }

    // Muestra la pantalla principal del vigilante
    private fun showHomevigi(inputCorreo: String, provider: ProviderType) {
        enableEdgeToEdge()
        val user_vigi = auth.currentUser
        val fotoUrl = user_vigi?.photoUrl?.toString()

        Handler(Looper.getMainLooper()).postDelayed({
            val homeIntent = Intent(this, Home_vigilante::class.java).apply {
                putExtra("inputCorreo", inputCorreo)
                putExtra("provider", provider.name)
                putExtra("foto_perfil_url", fotoUrl)
            }
            Acceder_BTN.isEnabled = true
            startActivity(homeIntent)
            overridePendingTransition(0, 0)
        }, 1000)
    }

    // Recibe el resultado del login con Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val authResult = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(authResult.idToken, null)

                // Muestra un diálogo mientras se autentica
                val progressBar = ProgressBar(this).apply {
                    isIndeterminate = true
                    indeterminateTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(this@AuthActivity, R.color.Botones)
                    )
                    setPadding(0, 30, 0, 30)
                }

                val dialog = MaterialAlertDialogBuilder(this)
                    .setTitle("🔐 Verificando cuenta")
                    .setMessage("Espéranos un momento...")
                    .setView(progressBar)
                    .setCancelable(false)
                    .create()

                dialog.show()

                // Intenta iniciar sesión con las credenciales de Google
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                    dialog.dismiss()

                    if (it.isSuccessful) {
                        saveSession(authResult.email ?: "", ProviderType.GOOGLE)
                        showHome(authResult.email ?: "", ProviderType.GOOGLE)
                    } else {
                        Toast.makeText(this, "Error de autenticación con Google", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: ApiException) {
                Toast.makeText(this, "Error de Google Sign-In: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}



