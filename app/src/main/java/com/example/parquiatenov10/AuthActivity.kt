package com.example.parquiatenov10

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import android.util.Patterns
import android.view.ViewGroup
import android.view.animation.AnimationUtils

enum class ProviderType {
    GOOGLE,
    EMAIL
}

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100
    private val inputCorreo = "vigilante@uniminuto.edu.co" // Entrada simulada para correo
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

        if (alto>=3001){
            val params = logo.layoutParams
            params.width = 1160 // Ancho en píxeles
            params.height = 1160 // Alto en píxeles
            logo.layoutParams = params
        }

        if (alto in 1301..2500){
            if (alto>=2400) {
                val params = logo.layoutParams
                params.width = 761 // Ancho en píxeles
                params.height = 761 // Alto en píxeles
                logo.layoutParams = params
            }
            if (alto in 1841..2399){
                val params = logo.layoutParams
                params.width = 870 // Ancho en píxeles
                params.height = 870 // Alto en píxeles
                logo.layoutParams = params
            }
            if (alto<=1840){
                val params = logo.layoutParams
                params.width = 761 // Ancho en píxeles
                params.height = 761 // Alto en píxeles
                logo.layoutParams = params
            }
        }

        if (alto in 1081..1300){
            val params = logo.layoutParams
            params.width = 500 // Ancho en píxeles
            params.height = 500 // Alto en píxeles
            logo.layoutParams = params
        }
        if (alto<=1080){
            val params = logo.layoutParams
            params.width = 300 // Ancho en píxeles
            params.height = 300 // Alto en píxeles
            logo.layoutParams = params
        }
        overridePendingTransition( 0,0)
        auth = FirebaseAuth.getInstance()
        setup()
        session()
    }

    private fun tamañoPantalla(porcentaje: Float): Int {
        val alto = resources.displayMetrics.heightPixels
        return (alto * porcentaje).toInt()
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val inputCorreo = prefs.getString("inputCorreo", null)
        val provider = prefs.getString("provider", null)

        if (email != null && provider != null) {
            showHome(email, ProviderType.valueOf(provider))
        }
        if (inputCorreo != null && provider != null) {
            showHome_vigi(inputCorreo, ProviderType.valueOf(provider))
        }
    }

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

    private fun setup() {
        val tamañoGoogle = tamañoPantalla(0.05f)
        title = "Autenticación"

        Google_BTN = findViewById(R.id.Google_BTN)
        Acceder_BTN = findViewById(R.id.Acceder_BTN)
        Registrarse_BTN = findViewById(R.id.Registrarse_BTN)
        Correo_ED = findViewById(R.id.Correo_ED)
        Contraseña_ED = findViewById(R.id.Contraseña_ED)
        ForgotPassword_TV = findViewById(R.id.OlvidasteContrasena_TV)

        tamaños(Google_BTN,tamañoGoogle)

        Google_BTN.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        val ocultar: ImageView = findViewById(R.id.ocultar)
        ocultar.setOnClickListener {
            eye = !eye
            if (eye) {
                Contraseña_ED.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                ocultar.setImageResource(R.drawable.eyeopen)
            } else {
                Contraseña_ED.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                ocultar.setImageResource(R.drawable.eyeclose)
            }
            Contraseña_ED.setSelection(Contraseña_ED.text.length)
        }

        Acceder_BTN.setOnClickListener {
            val email = Correo_ED.text.toString()
            val password = Contraseña_ED.text.toString()

            if (validateEmail(email) && validatePassword(password)) {
                Acceder_BTN.isEnabled = false
                if (email == inputCorreo && password == contraseña) {
                    saveSession(inputCorreo, ProviderType.EMAIL)
                    showHome_vigi(inputCorreo, ProviderType.EMAIL)
                    return@setOnClickListener
                }

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null && user.isEmailVerified) {
                            saveSession(email, ProviderType.EMAIL)
                            showHome(user.email ?: "", ProviderType.EMAIL)
                        } else {
                            Toast.makeText(this, "Por favor, verifica tu correo electrónico.", Toast.LENGTH_SHORT).show()
                            Acceder_BTN.isEnabled = true
                        }
                    } else {
                        Toast.makeText(this, "Error en la autenticación. Revisa los datos.", Toast.LENGTH_SHORT).show()
                        Acceder_BTN.isEnabled = true
                    }
                }
            }
        }
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

    private fun saveSession(email: String, provider: ProviderType) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider.name)
        prefs.apply()
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            true
        } else {
            Toast.makeText(this, "Por favor, ingresa un correo electrónico válido.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun validatePassword(password: String): Boolean {
        val simbolos = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
        return if (password.matches(simbolos)) {
            true
        } else {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun showHome(email: String, provider: ProviderType) {
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_home)
        val user = auth.currentUser
        val fotoUrl = user?.photoUrl?.toString()
        val transicionC = findViewById<ImageView>(R.id.carga)
        val fadeOutC = AnimationUtils.loadAnimation(this, R.anim.fade_out_c)
        transicionC.startAnimation(fadeOutC)

        Handler(Looper.getMainLooper()).postDelayed({
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
            putExtra("foto_perfil_url", fotoUrl)
        }
        startActivity(homeIntent)
            overridePendingTransition( 0,0)
        },5000)
    }
    private fun showHome_vigi(inputCorreo: String, provider: ProviderType) {
        setContentView(R.layout.activity_splash_home)
        val user_vigi = auth.currentUser
        val fotoUrl = user_vigi?.photoUrl?.toString()
        val transicionC = findViewById<ImageView>(R.id.carga)
        val fadeOutC = AnimationUtils.loadAnimation(this, R.anim.fade_out_c)
        transicionC.startAnimation(fadeOutC)

        Handler(Looper.getMainLooper()).postDelayed({
        val homeIntent = Intent(this, Home_vigilante::class.java).apply {
            putExtra("inputCorreo", inputCorreo)
            putExtra("provider", provider.name)
            putExtra("foto_perfil_url", fotoUrl)
        }
        Acceder_BTN.isEnabled = true
        startActivity(homeIntent)
            overridePendingTransition( 0,0)
        },5000)
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
                        Toast.makeText(this, "Error de autenticación con Google", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error de Google Sign-In: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
