package com.example.parquiatenov10

    import android.content.Intent
    import android.content.res.ColorStateList
    import android.os.Bundle
    import android.text.Editable
    import android.text.TextWatcher
    import android.widget.TextView
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import com.google.android.material.dialog.MaterialAlertDialogBuilder
    import com.google.android.material.snackbar.Snackbar
    import com.google.android.material.textfield.TextInputEditText
    import com.google.android.material.textfield.TextInputLayout
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
    import com.google.firebase.auth.FirebaseAuthUserCollisionException
    import com.google.firebase.auth.ktx.auth
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.ktx.firestore
    import com.google.firebase.ktx.Firebase
    import android.net.ConnectivityManager
    import android.net.NetworkCapabilities
    import android.content.Context
    import android.util.Log
    import android.widget.Toast

class RegistroActivity : AppCompatActivity() {

        private lateinit var auth: FirebaseAuth
        private lateinit var database: FirebaseFirestore

        private lateinit var nombreInputLayout: TextInputLayout
        private lateinit var apellidoInputLayout: TextInputLayout
        private lateinit var correoInputLayout: TextInputLayout
        private lateinit var contrasenaInputLayout: TextInputLayout
        private lateinit var confirmarContrasenaInputLayout: TextInputLayout

        private lateinit var nombreEditText: TextInputEditText
        private lateinit var apellidoEditText: TextInputEditText
        private lateinit var correoEditText: TextInputEditText
        private lateinit var contrasenaEditText: TextInputEditText
        private lateinit var confirmarContrasenaEditText: TextInputEditText

        private lateinit var registrarButton: com.google.android.material.button.MaterialButton
        private lateinit var volverLoginTextView: TextView

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_registro)

            // Responsividad
            Responsividad.inicializar(this)

            // Inicializar Firebase
            auth = Firebase.auth
            database = Firebase.firestore

            // Inicializar vistas
            bindViews()
            setupTextWatchers()
            setupButtonListeners()
            setupUI()
        }

        private fun bindViews() {
            nombreInputLayout = findViewById(R.id.nombre_input_layout)
            apellidoInputLayout = findViewById(R.id.apellido_input_layout)
            correoInputLayout = findViewById(R.id.correo_input_layout)
            contrasenaInputLayout = findViewById(R.id.contrasena_input_layout)
            confirmarContrasenaInputLayout = findViewById(R.id.confirmar_contrasena_input_layout)

            nombreEditText = findViewById(R.id.nombre_edittext)
            apellidoEditText = findViewById(R.id.apellido_edittext)
            correoEditText = findViewById(R.id.correo_edittext)
            contrasenaEditText = findViewById(R.id.contrasena_edittext)
            confirmarContrasenaEditText = findViewById(R.id.confirmar_contrasena_edittext)

            registrarButton = findViewById(R.id.registrar_button)
            volverLoginTextView = findViewById(R.id.volver_login_textview)
        }

        private fun setupUI() {
            // Configurar estilo de los TextInputLayout
            listOf(
                nombreInputLayout,
                apellidoInputLayout,
                correoInputLayout,
                contrasenaInputLayout,
                confirmarContrasenaInputLayout
            ).forEach { inputLayout ->
                inputLayout.boxStrokeColor = ContextCompat.getColor(this, R.color.Principal)
                inputLayout.hintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.Principal)
                )
                inputLayout.defaultHintTextColor = ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.Principal)
                )
            }

            // Configurar botón de registro
            registrarButton.apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.Principal))
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                cornerRadius = resources.getDimensionPixelSize(R.dimen.button_corner_radius)
                strokeWidth = 0
            }

            // Configurar texto de volver a login
            volverLoginTextView.setTextColor(ContextCompat.getColor(this, R.color.Principal))
        }

        private fun setupTextWatchers() {
            // Limpiar errores cuando el usuario empieza a escribir
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    clearErrors()
                }
            }

            listOf(
                nombreEditText,
                apellidoEditText,
                correoEditText,
                contrasenaEditText,
                confirmarContrasenaEditText
            ).forEach { editText ->
                editText.addTextChangedListener(textWatcher)
            }
        }

        private fun setupButtonListeners() {
            registrarButton.setOnClickListener {
                if (hayConexionInternet(this)) {
                    Log.d("conexion", "¡Hay conexión a Internet!")
                    registrarUsuario()
                } else {
                    Toast.makeText(this, "¡Se ha perdido la conexion!", Toast.LENGTH_SHORT).show()
                    finish()
                    Log.d("conexion", "No hay conexión")
                }
            }

            volverLoginTextView.setOnClickListener {
                volverALogin()
            }
        }
        fun hayConexionInternet(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val redActiva = connectivityManager.activeNetwork ?: return false
            val capacidades = connectivityManager.getNetworkCapabilities(redActiva) ?: return false

            return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        private fun registrarUsuario() {
            val nombre = nombreEditText.text.toString().trim()
            val apellido = apellidoEditText.text.toString().trim()
            val correo = correoEditText.text.toString().trim()
            val contrasena = contrasenaEditText.text.toString()
            val confirmarContrasena = confirmarContrasenaEditText.text.toString()

            if (!validarDatos(nombre, apellido, correo, contrasena, confirmarContrasena)) {
                return
            }

            showProgressDialog("Creando cuenta...")

            auth.fetchSignInMethodsForEmail(correo).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList()
                    if (signInMethods.isNotEmpty()) {
                        hideProgressDialog()
                        showErrorSnackbar("Ya existe una cuenta con este correo electrónico")
                        return@addOnCompleteListener
                    }

                    createUserWithEmail(nombre, apellido, correo, contrasena)
                } else {
                    hideProgressDialog()
                    showErrorSnackbar("Error al verificar el correo: ${task.exception?.message}")
                }
            }
        }

        private fun createUserWithEmail(
            nombre: String,
            apellido: String,
            correo: String,
            contrasena: String
        ) {
            auth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        saveUserDataToFirestore(nombre, apellido, correo)
                    } else {
                        hideProgressDialog()
                        handleRegistrationError(authTask.exception)
                    }
                }
        }

        private fun saveUserDataToFirestore(nombre: String, apellido: String, correo: String) {
            val usuario = hashMapOf(
                "nombre" to nombre,
                "apellido" to apellido,
                "correo" to correo,
                "fechaRegistro" to System.currentTimeMillis()
            )

            auth.currentUser?.uid?.let { userId ->
                database.collection("usuarios")
                    .document(userId)
                    .set(usuario)
                    .addOnSuccessListener {
                        sendVerificationEmail()
                    }
                    .addOnFailureListener { e ->
                        hideProgressDialog()
                        showErrorSnackbar("Error al guardar datos: ${e.message}")
                    }
            } ?: run {
                hideProgressDialog()
                showErrorSnackbar("Error: Usuario no creado correctamente")
            }
        }

        private fun sendVerificationEmail() {
            auth.currentUser?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        showSuccessDialog()
                    } else {
                        showErrorSnackbar("Error al enviar correo de verificación: ${task.exception?.message}")
                    }
                }
        }

        private fun validarDatos(
            nombre: String,
            apellido: String,
            correo: String,
            contrasena: String,
            confirmarContrasena: String
        ): Boolean {
            var isValid = true

            if (nombre.isEmpty()) {
                nombreInputLayout.error = "Ingrese su nombre"
                isValid = false
            }

            if (apellido.isEmpty()) {
                apellidoInputLayout.error = "Ingrese su apellido"
                isValid = false
            }

            if (correo.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                correoInputLayout.error = "Ingrese un correo válido"
                isValid = false
            }

            // Validación de contraseña IDÉNTICA a AuthActivity
            val passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$".toRegex()
            if (!contrasena.matches(passwordRegex)) {
                contrasenaInputLayout.error = "La contraseña debe tener:\n- Mínimo 8 caracteres\n- Al menos una mayúscula\n- Al menos una minúscula\n- Al menos un número\n- Al menos un símbolo (@\$!%*?&)"
                isValid = false
            }

            if (contrasena != confirmarContrasena) {
                confirmarContrasenaInputLayout.error = "Las contraseñas no coinciden"
                isValid = false
            }

            return isValid
        }

        private fun handleRegistrationError(exception: Exception?) {
            when (exception) {
                is FirebaseAuthUserCollisionException -> {
                    showErrorSnackbar("Ya existe una cuenta con este correo electrónico")
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    if (exception.message?.contains("email") == true) {
                        correoInputLayout.error = "Correo electrónico inválido"
                    } else {
                        contrasenaInputLayout.error = "Contraseña inválida: debe tener al menos 8 caracteres"
                    }
                }
                else -> {
                    showErrorSnackbar("Error en el registro: ${exception?.message ?: "Error desconocido"}")
                }
            }
        }

        private fun clearErrors() {
            listOf(
                nombreInputLayout,
                apellidoInputLayout,
                correoInputLayout,
                contrasenaInputLayout,
                confirmarContrasenaInputLayout
            ).forEach { it.error = null }
        }

        private fun showProgressDialog(message: String) {
            // Implementar diálogo de progreso similar al de AuthActivity
        }

        private fun hideProgressDialog() {
            // Ocultar diálogo de progreso
        }

        private fun showSuccessDialog() {
            MaterialAlertDialogBuilder(this)
                .setTitle("✅ Registro exitoso")
                .setMessage("Se ha enviado un correo de verificación a tu dirección de email. Por favor verifica tu correo antes de iniciar sesión.")
                .setPositiveButton("Aceptar") { _, _ ->
                    volverALogin()
                }
                .setCancelable(false)
                .show()
        }

        private fun showErrorSnackbar(message: String) {
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(this, R.color.Principal))
                .setTextColor(ContextCompat.getColor(this, android.R.color.black))
                .show()
        }

        private fun volverALogin() {
            val intent = Intent(this, AuthActivity ::class.java)
            startActivity(intent)
            finish()
        }
    }
