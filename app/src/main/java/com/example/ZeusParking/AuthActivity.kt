@file:Suppress("DEPRECATION")

package com.example.parquiatenov10

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

enum class ProviderType {
    GOOGLE,
    EMAIL
}

class AuthActivity : AppCompatActivity() {

    private var database = FirebaseFirestore.getInstance()

    private val GOOGLE_SIGN_IN = 100
    private lateinit var Google_BTN: Button
    private lateinit var Acceder_BTN: Button
    private lateinit var Registrarse_BTN: Button
    private lateinit var Correo_ED: EditText
    private lateinit var Contrasena_ED: EditText
    private lateinit var ForgotPassword_TV: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_auth)

        //Inicio de firebase
        FirebaseApp.initializeApp(this)

        //ID de botones
        Google_BTN = findViewById(R.id.Google_BTN)
        Acceder_BTN = findViewById(R.id.Acceder_BTN)
        Registrarse_BTN = findViewById(R.id.Registrarse_BTN)
        Correo_ED = findViewById(R.id.Correo_ED)
        Contrasena_ED = findViewById(R.id.Contraseña_ED)
        ForgotPassword_TV = findViewById(R.id.OlvidasteContrasena_TV)
        val text5 = findViewById<TextView>(R.id.textView5)
        val text6 = findViewById<TextView>(R.id.textView6)

        val logo = findViewById<ImageView>(R.id.imageView8)
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val alto = resources.displayMetrics.heightPixels

        Google_BTN.startAnimation(fadeIn)
        Acceder_BTN.startAnimation(fadeIn)
        Registrarse_BTN.startAnimation(fadeIn)
        Correo_ED.startAnimation(fadeIn)
        Contrasena_ED.startAnimation(fadeIn)
        ForgotPassword_TV.startAnimation(fadeIn)
        text5.startAnimation(fadeIn)
        text6.startAnimation(fadeIn)

        bundle.putString("message", "Integración de Firebase completa")
        analytics.logEvent("InitScreen", bundle)

        // Solicitar permisos al abrir la app
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED)
        ) {
            val permisos = mutableListOf<String>()
            permisos.add(Manifest.permission.CAMERA)
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permisos.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            ActivityCompat.requestPermissions(this, permisos.toTypedArray(), 123)
        }

        when {
            alto >= 3001 -> responsividadImagenes(logo, 1160, 1160)
            alto in 2400..2500 -> responsividadImagenes(logo, 761, 761)
            alto in 1841..2399 -> responsividadImagenes(logo, 870, 870)
            alto in 1301..1840 -> responsividadImagenes(logo, 761, 761)
            alto in 1081..1300 -> responsividadImagenes(logo, 500, 500)
            alto <= 1080 -> responsividadImagenes(logo, 300, 300)
        }

        overridePendingTransition(0, 0)
        auth = FirebaseAuth.getInstance()
        setup()
        session()
    }

    private fun responsividadImagenes(Imagen: ImageView, Alto: Int, Ancho: Int) {
        val params = Imagen.layoutParams
        params.width = Ancho // Ancho en píxeles
        params.height = Alto // Alto en píxeles
        Imagen.layoutParams = params
    }

    private fun tamanoPantalla(porcentaje: Float): Int {
        val alto = resources.displayMetrics.heightPixels
        return (alto * porcentaje).toInt()
    }

    private fun session() {
        val prefs = getSharedPreferences("Sesion", MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val inputCorreo = prefs.getString("inputCorreo", null)
        val provider = prefs.getString("provider", null)

        if (inputCorreo != null && provider != null) {
            showHomevigi(inputCorreo, ProviderType.valueOf(provider))
        }

        if (email != null && provider != null) {
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun tamanos(Google_BTN: Button, startHeight: Int) {
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
        val tamanoGoogle = tamanoPantalla(0.05f)
        title = "Autenticación"

        tamanos(Google_BTN, tamanoGoogle)

        Google_BTN.setOnClickListener {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        Acceder_BTN.setOnClickListener {
            val email = Correo_ED.text.toString().trim()
            val password = Contrasena_ED.text.toString()

            if (!validateEmailWithFeedback(email)) {
                return@setOnClickListener
            }

            if (!validatePasswordWithFeedback(password)) {
                return@setOnClickListener
            }

            val dialog = createAuthProgressDialog(
                "🔐 Verificando credenciales",
                "Validando tu información..."
            )
            dialog.show()
            Acceder_BTN.isEnabled = false

            database.collection("Usuarios_Admin")
                .whereEqualTo("correo", email)
                .whereEqualTo("contraseña", password)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        for (document in documents) {
                            val correo = document.getString("correo").toString()
                            val rol = document.getString("rol").toString()
                            when (rol) {
                                "vigilante" -> {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        dialog.dismiss()
                                        saveSession(correo, ProviderType.EMAIL)
                                        showHomevigi(correo, ProviderType.EMAIL)
                                        Acceder_BTN.isEnabled = true
                                    }, 1500)
                                }
                            }
                        }
                    } else {
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        handleSuccessfulLogin(task, email, dialog)
                                    }, 1500)
                                } else {
                                    dialog.dismiss()
                                    handleLoginErrorWithDetailedMessages(task.exception)
                                    Acceder_BTN.isEnabled = true
                                }
                            }
                    }
                }
            }

        Registrarse_BTN.setOnClickListener {
            val email = Correo_ED.text.toString().trim()
            val password = Contrasena_ED.text.toString()

            if (!validateEmailWithFeedback(email)) {
                return@setOnClickListener
            }

            if (!validatePasswordWithFeedback(password)) {
                return@setOnClickListener
            }

            auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList()
                    if (signInMethods.isNotEmpty()) {
                        showErrorSnackbar("Ya existe una cuenta con este correo electrónico")
                        return@addOnCompleteListener
                    }

                    // Continue with registration if email is not in use
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val user = auth.currentUser
                            user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                                if (verificationTask.isSuccessful) {
                                    showSuccessSnackbar("Registro exitoso. Se ha enviado un correo de verificación. Por favor verifica tu correo antes de iniciar sesión.")
                                    Correo_ED.text.clear()
                                    Contrasena_ED.text.clear()
                                } else {
                                    showErrorSnackbar("Error al enviar el correo de verificación: ${verificationTask.exception?.message}")
                                }
                            }
                        } else {
                            handleRegistrationError(it.exception)
                        }
                    }
                } else {
                    showErrorSnackbar("Error al verificar el correo: ${task.exception?.message}")
                }
            }
        }

        ForgotPassword_TV.setOnClickListener {
            val email = Correo_ED.text.toString()
            if (validateEmail(email)) {
                ForgotPassword_TV.isEnabled = true
                auth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Se ha enviado un enlace para restablecer la contraseña a tu correo.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Error al enviar el correo de restablecimiento.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun validateEmailWithFeedback(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                showErrorSnackbar("Por favor ingresa un correo electrónico")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showErrorSnackbar("Por favor ingresa un correo electrónico válido")
                false
            }
            else -> true
        }
    }

    private fun handleLoginErrorWithDetailedMessages(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidUserException -> {
                showErrorSnackbar("No existe una cuenta con este correo electrónico")
            }
            is FirebaseAuthInvalidCredentialsException -> {
                if (exception.message?.contains("password") == true) {
                    showErrorSnackbar("Contraseña incorrecta")
                } else {
                    showErrorSnackbar("Credenciales inválidas")
                }
            }
            else -> {
                showErrorSnackbar("Error de autenticación: ${exception?.message ?: "Error desconocido"}")
            }
        }
    }

    private fun handleRegistrationError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthUserCollisionException -> {
                showErrorSnackbar("Ya existe una cuenta con este correo electrónico")
            }
            is FirebaseAuthInvalidCredentialsException -> {
                if (exception.message?.contains("email") == true) {
                    showErrorSnackbar("Correo electrónico inválido")
                } else {
                    showErrorSnackbar("Contraseña inválida: debe tener al menos 8 caracteres con mayúsculas, minúsculas, números y símbolos")
                }
            }
            else -> {
                showErrorSnackbar("Error en el registro: ${exception?.message ?: "Error desconocido"}")
            }
        }
    }

    private fun validatePasswordWithFeedback(password: String): Boolean {
        return when {
            password.isEmpty() -> {
                showErrorSnackbar("Por favor ingresa una contraseña")
                false
            }
            password.length < 8 -> {
                showErrorSnackbar("La contraseña debe tener al menos 8 caracteres")
                false
            }
            password.length > 64 -> {
                showErrorSnackbar("La contraseña no puede exceder 64 caracteres")
                false
            }
            !password.any { it.isUpperCase() } -> {
                showErrorSnackbar("Debe contener al menos una mayúscula (A-Z)")
                false
            }
            !password.any { it.isLowerCase() } -> {
                showErrorSnackbar("Debe contener al menos una minúscula (a-z)")
                false
            }
            !password.any { it.isDigit() } -> {
                showErrorSnackbar("Debe contener al menos un número (0-9)")
                false
            }
            !password.any { it in "!@#$%^&*()-_=+[]{}|;:'\",.<>?/" } -> {
                showErrorSnackbar("Debe contener al menos un símbolo especial")
                false
            }
            password.contains(Regex("(.)\\1{2,}")) -> {
                showErrorSnackbar("No se permiten caracteres repetidos 3+ veces")
                false
            }
            password == Correo_ED.text.toString() -> {
                showErrorSnackbar("La contraseña no puede ser igual al correo")
                false
            }
            else -> true
        }
    }


    private fun handleSuccessfulLogin(
        task: com.google.android.gms.tasks.Task<AuthResult>,
        email: String,
        dialog: AlertDialog
    ) {
        dialog.dismiss()
        val user = auth.currentUser

        when {
            user == null -> {
                showErrorSnackbar("Error: Usuario no encontrado")
            }

            user.isEmailVerified -> {
                saveSession(email, ProviderType.EMAIL)
                showHome(user.email ?: email, ProviderType.EMAIL)
            }

            else -> {
                showVerificationNeededDialog(email)
            }
        }
        Acceder_BTN.isEnabled = true
    }

    private fun handleLoginError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidUserException ->
                showErrorSnackbar("Usuario no registrado")

            is FirebaseAuthInvalidCredentialsException ->
                showErrorSnackbar("Credenciales incorrectas")

            else ->
                showErrorSnackbar("Error: ${exception?.message ?: "Error desconocido"}")
        }
    }

    private fun saveSession(email: String, provider: ProviderType) {
        val prefs =
            getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider.name)
        prefs.apply()
    }

    private fun validateEmail(email: String): Boolean {
        return if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            true
        } else {
            false
        }
    }

    private fun validatePassword(password: String): Boolean {
        val simbolos =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
        return if (password.matches(simbolos)) {
            true
        } else {
            false
        }
    }

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
            Acceder_BTN.isEnabled = true
            startActivity(homeIntent)
            overridePendingTransition(0, 0)
        }, 1000)
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                val dialog =
                    createAuthProgressDialog("🔐 Iniciando sesión", "Conectando con Google...")
                dialog.show()

                Handler(Looper.getMainLooper()).postDelayed({
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                        dialog.dismiss()

                        if (authTask.isSuccessful) {
                            val email = account.email ?: ""
                            saveSession(email, ProviderType.GOOGLE)
                            showHome(email, ProviderType.GOOGLE)
                        } else {
                            showErrorSnackbar(
                                when (authTask.exception) {
                                    is FirebaseAuthUserCollisionException ->
                                        "Esta cuenta ya está registrada con email/password"

                                    else ->
                                        "Error de autenticación: ${authTask.exception?.message}"
                                }
                            )
                        }
                    }
                }, 500)
            } catch (e: ApiException) {
                when (e.statusCode) {
                    GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> {}
                    else -> showErrorSnackbar("Error de Google: ${e.message}")
                }
            }
        }
    }

    private fun showVerificationNeededDialog(email: String) {
        MaterialAlertDialogBuilder(this).apply {
            setTitle("📧 Verificación requerida")
            setMessage("Hemos enviado un correo de verificación a $email. Por favor verifica tu correo antes de iniciar sesión.")
            setPositiveButton("Reenviar verificación") { _, _ ->
                auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        showSuccessSnackbar("Correo de verificación reenviado")
                    } else {
                        showErrorSnackbar("Error al reenviar el correo: ${task.exception?.message}")
                    }
                }
            }
            setNegativeButton("Entendido", null)
        }.show()
    }

    private fun showSuccessSnackbar(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).apply {
            setBackgroundTint(ContextCompat.getColor(this@AuthActivity, R.color.success_green))
            setTextColor(ContextCompat.getColor(this@AuthActivity, android.R.color.white))
            show()
        }
    }

    private fun createAuthProgressDialog(title: String, message: String): AlertDialog {
        return MaterialAlertDialogBuilder(this).apply {
            setTitle(title)
            setMessage(message)
            setView(createProgressView())
            setCancelable(false)
        }.create()
    }

    private fun createProgressView(): View {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dpToPx(32), dpToPx(32), dpToPx(32), dpToPx(32))

            addView(CircularProgressIndicator(this@AuthActivity).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(48), dpToPx(48))
                isIndeterminate = true
                indeterminateTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(this@AuthActivity, R.color.Botones)
                )
            })

            addView(TextView(this@AuthActivity).apply {
                text = "Por favor espera..."
                //setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body2)
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                setPadding(0, dpToPx(16), 0, 0)
                gravity = Gravity.CENTER
            })
        }
    }

    private fun showErrorSnackbar(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).apply {
            setBackgroundTint(ContextCompat.getColor(this@AuthActivity, R.color.Botones))
            setTextColor(Color.BLACK)
            show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}