package com.example.parquiatenov10

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.text.InputType
import android.text.InputFilter
import android.view.animation.AnimationUtils
import com.example.ZeusParking.BaseNavigationActivity
import android.text.Editable
import android.text.TextWatcher
import java.util.regex.Pattern

class RegistrarBiciActivity : BaseNavigationActivity() {
    private lateinit var nombreEd: EditText
    private lateinit var apellidosEd: EditText
    private lateinit var colorEd: EditText
    private lateinit var cedulaNum: EditText
    private lateinit var marcoNum: EditText
    private lateinit var agregarFoto1Btn: Button
    private lateinit var agregarFoto2Btn: Button
    private lateinit var tiposSpinner: Spinner
    private lateinit var Guardar: Button
    private lateinit var texto: TextView

    private var fotoUri1: Uri? = null
    private var fotoUri2: Uri? = null
    private var foto1Subida = false
    private var foto2Subida = false

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Patrones de validación
    private val nombreApellidoPattern = Pattern.compile("^[\\p{L} .'-]+$")
    private val placaPattern = Pattern.compile("^[a-z]{3}[0-9]{3}$")
    private val cedulaPattern = Pattern.compile("^[0-9]{1,10}$")

    data class BiciData(
        val nombre: String,
        val apellidos: String,
        val color: String,
        val cedula: String,
        val numero: String,
        var tipo: String,
        val correo: String,
        val id: String
    ) : java.io.Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_bici)
        enableEdgeToEdge()
        startAnimationsWithDelay()

        //Pautas para el registro
        mostrarInstruccionesIniciales()

        //Responsividad
        Responsividad.inicializar(this)

        //Navegacion
        setupNavigation()

        overridePendingTransition(0, 0)

        // Configuración de los elementos de la interfaz
        nombreEd = findViewById(R.id.Nombre_ED)
        apellidosEd = findViewById(R.id.Apellidos_ED)
        colorEd = findViewById(R.id.Color_ED)
        cedulaNum = findViewById(R.id.Cedula_NUM)
        marcoNum = findViewById(R.id.Marco_NUM)
        agregarFoto1Btn = findViewById(R.id.AgregarFoto1_BTN)
        agregarFoto2Btn = findViewById(R.id.AgregarFoto2_BTN)
        tiposSpinner = findViewById(R.id.Tipos_Spinner)
        Guardar = findViewById(R.id.Guardar_BTN)
        texto = findViewById(R.id.textView3)

        // Configurar validaciones en tiempo real
        setupValidaciones()

        // Configuración del Spinner
        val adapter = ArrayAdapter.createFromResource(this, R.array.items, R.layout.estilo_spinner)
        adapter.setDropDownViewResource(R.layout.estilo_spinner)
        tiposSpinner.adapter = adapter

        tiposSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    1, 2 -> {
                        marcoNum.hint = "4 Ultimos Numeros"
                        marcoNum.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(4))
                    }

                    3, 4 -> { // Vehículo particular y moto
                        marcoNum.hint = "Placa (Ej. abc123)"
                        marcoNum.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(6))
                        marcoNum.addTextChangedListener(placaTextWatcher)
                    }

                    5 -> { // Furgón
                        marcoNum.hint = "Número de Furgón"
                        marcoNum.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(7))
                    }

                    else -> {
                        marcoNum.hint = ""
                        marcoNum.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(20))
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Evento para agregar la primera foto
        agregarFoto1Btn.setOnClickListener {
            seleccionarImagen(1)
        }

        // Evento para agregar la segunda foto
        agregarFoto2Btn.setOnClickListener {
            seleccionarImagen(2)
        }

        Guardar.setOnClickListener {
            guardarDatosEnFirestore()
        }
    }

    private fun setupValidaciones() {
        // Validación para nombre
        nombreEd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!nombreApellidoPattern.matcher(s.toString()).matches()) {
                    nombreEd.error = "Nombre no válido"
                } else {
                    nombreEd.error = null
                }
            }
        })

        // Validación para apellidos
        apellidosEd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!nombreApellidoPattern.matcher(s.toString()).matches()) {
                    apellidosEd.error = "Apellidos no válidos"
                } else {
                    apellidosEd.error = null
                }
            }
        })

        // Validación para cédula (máximo 10 números)
        cedulaNum.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!cedulaPattern.matcher(s.toString()).matches()) {
                    cedulaNum.error = "Cédula debe tener máximo 10 números"
                } else {
                    cedulaNum.error = null
                }
            }
        })

        // Validación para color
        colorEd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length < 3) {
                    colorEd.error = "Color muy corto"
                } else {
                    colorEd.error = null
                }
            }
        })
    }

    // TextWatcher para validar placas (3 letras + 3 números)
    private val placaTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            val texto = s.toString().lowercase()
            if (!placaPattern.matcher(texto).matches()) {
                marcoNum.error = "Formato: 3 letras + 3 números (ej: abc123)"
            } else {
                marcoNum.error = null
            }
        }
    }

    //Navegacion del Sistema
    override fun getCurrentNavigationItem(): Int = R.id.registro

    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            listOf(
                nombreEd,
                apellidosEd,
                colorEd,
                cedulaNum,
                marcoNum,
                agregarFoto1Btn,
                agregarFoto2Btn,
                tiposSpinner,
                texto,
                Guardar
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 0)
    }

    private fun seleccionarImagen(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when (requestCode) {
                1 -> {
                    fotoUri1 = data.data
                    foto1Subida = true
                    agregarFoto1Btn.text = "Foto 1 ✔"
                    Toast.makeText(this, "Foto Guardada Con Éxito", Toast.LENGTH_SHORT).show()
                }

                2 -> {
                    fotoUri2 = data.data
                    foto2Subida = true
                    agregarFoto2Btn.text = "Foto 2 ✔"
                    Toast.makeText(this, "Foto Guardada Con Éxito", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun guardarDatosEnFirestore() {
        val nombre = nombreEd.text.toString().trim()
        val apellidos = apellidosEd.text.toString().trim()
        val color = colorEd.text.toString().trim()
        val cedula = cedulaNum.text.toString().trim()
        val marco = marcoNum.text.toString().trim()
        val tipoVehiculo = tiposSpinner.selectedItem.toString()
        val correo = FirebaseAuth.getInstance().currentUser?.email.toString()
        val id = FirebaseAuth.getInstance().currentUser?.uid

        // Validaciones adicionales antes de guardar
        if (nombre.isEmpty() || !nombreApellidoPattern.matcher(nombre).matches()) {
            nombreEd.error = "Nombre no válido"
            return
        }

        if (apellidos.isEmpty() || !nombreApellidoPattern.matcher(apellidos).matches()) {
            apellidosEd.error = "Apellidos no válidos"
            return
        }

        if (color.isEmpty() || color.length < 3) {
            colorEd.error = "Color no válido"
            return
        }

        if (cedula.isEmpty() || !cedulaPattern.matcher(cedula).matches()) {
            cedulaNum.error = "Cédula no válida"
            return
        }

        if (marco.isEmpty()) {
            marcoNum.error = "Número de marco/placa requerido"
            return
        }

        // Validación específica para placas de vehículo particular y moto
        if ((tiposSpinner.selectedItemPosition == 3 || tiposSpinner.selectedItemPosition == 4) &&
            !placaPattern.matcher(marco.lowercase()).matches()) {
            marcoNum.error = "Formato de placa inválido (ej: abc123)"
            return
        }

        if (!foto1Subida || !foto2Subida) {
            Toast.makeText(this, "Debe subir ambas fotos", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Bici_Usuarios")
            .whereEqualTo("numero", marco)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result?.isEmpty == false) {
                        Toast.makeText(this, "Error, el vehículo ya está registrado", Toast.LENGTH_LONG).show()
                        limpiarCampos()
                    } else {
                        val biciData = BiciData(
                            nombre,
                            apellidos,
                            color,
                            cedula,
                            marco,
                            tipoVehiculo,
                            correo,
                            id.toString()
                        )

                        db.collection("Bici_Usuarios")
                            .add(biciData)
                            .addOnSuccessListener { documentReference ->
                                fotoUri1?.let { uri ->
                                    User(uri, cedula)
                                }
                                fotoUri2?.let { uri ->
                                    subirFoto(uri, marco, documentReference.id)
                                }
                                Toast.makeText(
                                    this,
                                    "Datos guardados exitosamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                limpiarCampos()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Error al guardar los datos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Error al verificar el registro: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun subirFoto(fotoUri: Uri?, tipo: String, biciId: String) {
        if (fotoUri == null) return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = storage.reference.child("$userId/$biciId/$tipo.png")

        storageRef.putFile(fotoUri)
            .addOnSuccessListener {
                Toast.makeText(this, "Foto $tipo subida exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir la foto $tipo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun User(fotoUri: Uri?, tipo: String) {
        if (fotoUri == null) return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = storage.reference.child("$userId/$tipo.png")

        storageRef.putFile(fotoUri)
            .addOnSuccessListener {
                Toast.makeText(this, "Foto $tipo subida exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir la foto $tipo", Toast.LENGTH_SHORT).show()
            }
    }

    //Pautas para el registro
    private fun mostrarInstruccionesIniciales() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Instrucciones para el Registro")
        builder.setMessage("""
        1. Complete todos los campos obligatorios
        2. Para vehículos: ingrese placa en formato abc123
        3. Para bicicletas: últimos 4 dígitos del marco
        4. Debe subir 2 fotos claras del vehículo
        5. Verifique que los datos sean correctos antes de guardar
    """.trimIndent())
        builder.setPositiveButton("Entendido", null)
        builder.show()
    }

    private fun limpiarCampos() {
        nombreEd.text.clear()
        apellidosEd.text.clear()
        colorEd.text.clear()
        cedulaNum.text.clear()
        marcoNum.text.clear()
        tiposSpinner.setSelection(0)
        fotoUri1 = null
        fotoUri2 = null
        foto1Subida = false
        foto2Subida = false
        agregarFoto1Btn.text = "USUARIO"
        agregarFoto2Btn.text = "VEHICULO"
    }
}