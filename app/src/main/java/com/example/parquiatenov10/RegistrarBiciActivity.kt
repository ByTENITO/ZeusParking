package com.example.parquiatenov10

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.text.InputType
import android.text.InputFilter

class RegistrarBiciActivity : AppCompatActivity() {
    private lateinit var nombreEd: EditText
    private lateinit var apellidosEd: EditText
    private lateinit var colorEd: EditText
    private lateinit var cedulaNum: EditText
    private lateinit var marcoNum: EditText
    private lateinit var agregarFoto1Btn: Button
    private lateinit var agregarFoto2Btn: Button
    private lateinit var editarBtn: Button
    private lateinit var eliminarBtn: Button
    private lateinit var tiposSpinner: Spinner
    private lateinit var VolverButton:Button

    private var fotoUri1: Uri? = null
    private var fotoUri2: Uri? = null
    private var biciId: String? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_bici)
        VolverButton = findViewById(R.id.Volver_BTN)
        VolverButton.setOnClickListener {
            finish()
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de los elementos
        nombreEd = findViewById(R.id.Nombre_ED)
        apellidosEd = findViewById(R.id.Apellidos_ED)
        colorEd = findViewById(R.id.Color_ED)
        cedulaNum = findViewById(R.id.Cedula_NUM)
        marcoNum = findViewById(R.id.Marco_NUM)
        agregarFoto1Btn = findViewById(R.id.AgregarFoto1_BTN)
        agregarFoto2Btn = findViewById(R.id.AgregarFoto2_BTN)
        editarBtn = findViewById(R.id.Editar_BTN)
        eliminarBtn = findViewById(R.id.Eliminar_BTN)
        tiposSpinner = findViewById(R.id.Tipos_Spinner)
        VolverButton = findViewById(R.id.Volver_BTN)

        // Configuración del Spinner
        val tiposVehiculos = arrayOf("Selecciona el Tipo de Vehículo", "Bicicleta", "Motocicleta", "Vehículo Particular", "Furgón")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposVehiculos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tiposSpinner.adapter = adapter

        tiposSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    1 -> {
                        marcoNum.hint = "Número de Marco"
                        marcoNum.inputType = InputType.TYPE_CLASS_NUMBER // Solo números
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(20)) // Permitir hasta 20 caracteres
                    }
                    2, 3, 4 -> {
                        marcoNum.hint = when (position) {
                            2 -> "Placa (Ej. ABC-123)"
                            3 -> "Placa (Ej. ABC-123)"
                            4 -> "Número de Furgón"
                            else -> "Número de Marco"
                        }
                        marcoNum.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD // Texto y números
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(7)) // Limitar a 7 caracteres
                    }
                    else -> {
                        marcoNum.hint = "Número de Marco"
                        marcoNum.inputType = InputType.TYPE_CLASS_NUMBER // Solo números
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(20))
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                marcoNum.hint = "Número de Marco"
                marcoNum.inputType = InputType.TYPE_CLASS_NUMBER
                marcoNum.filters = arrayOf(InputFilter.LengthFilter(20))
            }
        }

        // Evento para agregar la primera foto
        agregarFoto1Btn.setOnClickListener {
            seleccionarImagen(1)
        }

        // Evento para agregar la segunda foto
        agregarFoto2Btn.setOnClickListener {
            seleccionarImagen(2)
        }

        // Guardar los datos en Firestore
        findViewById<Button>(R.id.Guardar_BTN).setOnClickListener {
            guardarDatosEnFirestore()
        }

        // Editar los datos
        editarBtn.setOnClickListener {
            editarDatosEnFirestore()
        }

        // Eliminar los datos
        eliminarBtn.setOnClickListener {
            eliminarDatosEnFirestore()
        }

        // Cargar los datos si se está editando
        cargarDatosSiExistente()
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
                    Toast.makeText(this, "Primera Foto Guardada Con Éxito", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    fotoUri2 = data.data
                    Toast.makeText(this, "Segunda Foto Guardada Con Éxito", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun guardarDatosEnFirestore() {
        val nombre = nombreEd.text.toString()
        val apellidos = apellidosEd.text.toString()
        val color = colorEd.text.toString()
        val cedula = cedulaNum.text.toString()
        val marco = marcoNum.text.toString()
        val tipoVehiculo = tiposSpinner.selectedItem.toString()

        // Validaciones
        if (tipoVehiculo == "Selecciona el Tipo de Vehículo") {
            Toast.makeText(this, "Por favor, selecciona un tipo de vehículo válido", Toast.LENGTH_SHORT).show()
            return
        }

        if (nombre.isEmpty() || apellidos.isEmpty() || color.isEmpty() || cedula.isEmpty() || marco.isEmpty()) {
            Toast.makeText(this, "Llene Todos los Campos Por Favor", Toast.LENGTH_SHORT).show()
            return
        }

        // Validación de cédula (solo números, hasta 10 dígitos)
        if (!cedula.matches(Regex("^[0-9]{1,10}$"))) {
            Toast.makeText(this, "La cédula debe contener solo números y hasta 10 dígitos", Toast.LENGTH_SHORT).show()
            return
        }

        // Validación de número de marco
        if (tipoVehiculo in listOf("Motocicleta", "Vehículo Particular", "Furgón")) {
            // Alfanumérico, hasta 6 caracteres
            if (!marco.matches(Regex("^[A-Za-z0-9]{1,11}$"))) {
                Toast.makeText(this, "El número de marco debe ser alfanumérico y hasta 11 caracteres", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            // Alfanumérico, hasta 20 caracteres
            if (!marco.matches(Regex("^[A-Za-z0-9]{1,20}$"))) {
                Toast.makeText(this, "El número de marco debe ser alfanumérico y hasta 20 caracteres", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (fotoUri1 == null || fotoUri2 == null) {
            Toast.makeText(this, "Debe subir las dos fotos", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val biciCollection = db.collection("Bici Usuarios")
        val query = biciCollection.whereEqualTo("usuarioId", userId).limit(1)

        query.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val biciData: MutableMap<String, Any> = hashMapOf(
                        "nombre" to nombre,
                        "apellidos" to apellidos,
                        "color" to color,
                        "cedula" to cedula,
                        "numero" to marco,
                        "tipo" to tipoVehiculo,
                        "usuarioId" to userId
                    )

                    biciCollection.add(biciData)
                        .addOnSuccessListener { documentReference ->
                            // Aquí subimos las fotos
                            subirFoto(fotoUri1, userId, "foto_perfil", documentReference.id)
                            subirFoto(fotoUri2, userId, "foto_bici", documentReference.id)
                            Toast.makeText(this, "Datos guardados exitosamente", Toast.LENGTH_SHORT).show()
                            limpiarCampos()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Ya existe un registro de bicicleta para este usuario", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar los datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirFoto(fotoUri: Uri?, userId: String, tipo: String, biciId: String) {
        if (fotoUri == null) return

        val storageRef = storage.reference
        val userBiciRef = storageRef.child("$userId/$biciId/$tipo.jpg")

        userBiciRef.putFile(fotoUri)
            .addOnSuccessListener {
                Toast.makeText(this, "Foto $tipo subida exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir la foto $tipo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarDatosSiExistente() {
        // Aquí iría el código para cargar datos existentes si es necesario
    }

    private fun editarDatosEnFirestore() {
        // Aquí iría el código para editar datos existentes si es necesario
    }

    private fun eliminarDatosEnFirestore() {
        // Aquí iría el código para eliminar datos existentes si es necesario
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
    }
}


