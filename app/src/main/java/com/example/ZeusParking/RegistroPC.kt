package com.example.parquiatenov10

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*

class RegistroPC : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore
    private lateinit var marcaEditText: EditText
    private lateinit var modeloEditText: EditText
    private lateinit var serialEditText: EditText
    private lateinit var colorEditText: EditText
    private lateinit var caracteristicasEditText: EditText
    private lateinit var registrarButton: Button
    private lateinit var spinnerTipo: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_pc)

        // Responsividad
        Responsividad.inicializar(this)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        // Inicializar vistas
        marcaEditText = findViewById(R.id.marcaEditText)
        modeloEditText = findViewById(R.id.modeloEditText)
        serialEditText = findViewById(R.id.serialEditText)
        colorEditText = findViewById(R.id.colorEditText)
        caracteristicasEditText = findViewById(R.id.caracteristicasEditText)
        registrarButton = findViewById(R.id.registrarButton)
        spinnerTipo = findViewById(R.id.spinnerTipo)

        // Configurar spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.tipos_pc,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTipo.adapter = adapter
        }

        // Configurar filtros de entrada
        serialEditText.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            if (source.isBlank()) return@InputFilter null
            // Solo permite letras, números y guiones
            if (!source.matches(Regex("^[a-zA-Z0-9-]*$"))) {
                ""
            } else {
                null
            }
        })

        // Registrar portátil
        registrarButton.setOnClickListener {
            registrarPortatil()
        }
    }

    private fun registrarPortatil() {
        val marca = marcaEditText.text.toString().trim()
        val modelo = modeloEditText.text.toString().trim()
        val serial = serialEditText.text.toString().trim()
        val color = colorEditText.text.toString().trim()
        val caracteristicas = caracteristicasEditText.text.toString().trim()
        val tipo = spinnerTipo.selectedItem.toString()

        // Validaciones
        when {
            marca.isEmpty() -> {
                marcaEditText.error = "Ingrese la marca del portátil"
                return
            }
            modelo.isEmpty() -> {
                modeloEditText.error = "Ingrese el modelo del portátil"
                return
            }
            serial.isEmpty() -> {
                serialEditText.error = "Ingrese el serial del portátil"
                return
            }
            color.isEmpty() -> {
                colorEditText.error = "Ingrese el color del portátil"
                return
            }
            tipo == "Seleccione el tipo" -> {
                Toast.makeText(this, "Seleccione un tipo de portátil", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val portatilData = hashMapOf(
            "marca" to marca,
            "modelo" to modelo,
            "serial" to serial.uppercase(Locale.getDefault()),
            "color" to color,
            "caracteristicas" to caracteristicas,
            "tipo" to tipo,
            "usuarioId" to user.uid,
            "correoUsuario" to user.email,
            "fechaRegistro" to System.currentTimeMillis()
        )

        // Verificar si el serial ya existe
        database.collection("Portatiles")
            .whereEqualTo("serial", serial.uppercase(Locale.getDefault()))
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Serial no existe, proceder con el registro
                    database.collection("Portatiles")
                        .document(serial.uppercase(Locale.getDefault()))
                        .set(portatilData, SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(this, "Portátil registrado exitosamente", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("RegistroPC", "Error al registrar portátil", e)
                        }
                } else {
                    // Serial ya existe
                    serialEditText.error = "Este serial ya está registrado"
                    Toast.makeText(this, "El serial ingresado ya está registrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al verificar serial: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("RegistroPC", "Error al verificar serial", e)
            }
    }
}