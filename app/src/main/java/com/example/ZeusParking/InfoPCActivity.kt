package com.example.parquiatenov10

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class InfoPCActivity : AppCompatActivity() {
    private lateinit var database: FirebaseFirestore
    private lateinit var tvMarca: TextView
    private lateinit var tvModelo: TextView
    private lateinit var tvSerial: TextView
    private lateinit var tvColor: TextView
    private lateinit var tvCaracteristicas: TextView
    private lateinit var tvTipo: TextView
    private lateinit var btnConfirmarLleva: Button
    private lateinit var btnConfirmarNoLleva: Button
    private lateinit var correoUsuario: String
    private lateinit var vehiculo: String
    private lateinit var idVehi: String
    private lateinit var serialPC: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_pc)

        // Responsividad
        Responsividad.inicializar(this)

        // Inicializar Firebase
        database = FirebaseFirestore.getInstance()

        // Obtener datos del intent
        correoUsuario = intent.getStringExtra("correo") ?: ""
        vehiculo = intent.getStringExtra("vehiculo") ?:""
        idVehi = intent.getStringExtra("idVehi") ?: ""
        serialPC = intent.getStringExtra("serialPC") ?: ""

        // Inicializar vistas
        tvMarca = findViewById(R.id.tvMarca)
        tvModelo = findViewById(R.id.tvModelo)
        tvSerial = findViewById(R.id.tvSerial)
        tvColor = findViewById(R.id.tvColor)
        tvCaracteristicas = findViewById(R.id.tvCaracteristicas)
        tvTipo = findViewById(R.id.tvTipo)
        btnConfirmarLleva = findViewById(R.id.btnConfirmarLleva)
        btnConfirmarNoLleva = findViewById(R.id.btnConfirmarNoLleva)

        // Cargar información del PC
        cargarInfoPC()

        // Configurar botones
        btnConfirmarLleva.setOnClickListener {
            registrarEntradaPC(true)
        }

        btnConfirmarNoLleva.setOnClickListener {
            registrarEntradaPC(false)
        }
    }

    private fun cargarInfoPC() {
        database.collection("Portatiles")
            .document(serialPC)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    tvMarca.text = document.getString("marca")
                    tvModelo.text = document.getString("modelo")
                    tvSerial.text = document.getString("serial")
                    tvColor.text = document.getString("color")
                    tvCaracteristicas.text = document.getString("caracteristicas")
                    tvTipo.text = document.getString("tipo")
                } else {
                    Toast.makeText(this, "No se encontró información del PC", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al cargar información: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
    }

    private fun registrarEntradaPC(llevaPC: Boolean) {
        if (!llevaPC) {
            database.collection("Entrada_Portatiles")
                .whereEqualTo("correoUsuario", correoUsuario)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        if (documents.isEmpty) {
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "El usuario no podra salir, este debe salir con su PC",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        } else {
            val entradaData = hashMapOf(
                "serialPC" to serialPC,
                "correoUsuario" to correoUsuario,
                "fechaHora" to System.currentTimeMillis(),
                "llevaPC" to true
            )
            database.collection("Entrada_Portatiles")
                .whereEqualTo("correoUsuario", correoUsuario)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents != null) {
                        if (!documents.isEmpty) {
                            for (document in documents) {
                                database.collection("Entrada_Portatiles").document(document.id).delete()
                                Toast.makeText(this, "PC verificado", Toast.LENGTH_SHORT).show()
                                procesarSalida(correoUsuario,vehiculo,idVehi)
                            }
                        } else {
                            database.collection("Entrada_Portatiles")
                                .add(entradaData)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Registro de PC confirmado",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }.addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Error al registrar: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                }
        }

    }
    private fun procesarSalida(
        correo: String,
        vehiculo: String,
        idVehiculo: String,
    ) {
        // Paso 1: Eliminar registros de Entrada
        database.collection("Entrada")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { entradaDocuments ->
                for (document in entradaDocuments) {
                    if (document.exists()) {
                        database.collection("Entrada").document(document.id).delete()
                        Log.d("Salida", "Marcando entrada para eliminar: ${document.id}")
                    }
                }
                finish()
                commitBatchOperations( correo, vehiculo, idVehiculo)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al procesar salida", Toast.LENGTH_SHORT).show()
                Log.e("Salida", "Error al obtener entradas", e)
            }
    }

    private fun commitBatchOperations(
        correo: String,
        vehiculo: String,
        idVehiculo: String
    ) {
        Log.d("Salida", "Todos los registros eliminados exitosamente")
        val intent = Intent(this, DatosUsuarioSalida::class.java).apply {
            putExtra("correo", correo)
            putExtra("tipo", vehiculo)
            putExtra("id", idVehiculo)
        }
        startActivity(intent)
    }
}
