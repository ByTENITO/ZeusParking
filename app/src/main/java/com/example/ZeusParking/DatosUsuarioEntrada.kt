package com.example.parquiatenov10

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class DatosUsuarioEntrada : AppCompatActivity() {
    private var database = FirebaseFirestore.getInstance()
    private lateinit var correoTXT: TextView
    private lateinit var colorTXT: TextView
    private lateinit var nombreTXT: TextView
    private lateinit var apellidoTXT: TextView
    private lateinit var numeroTXT: TextView
    private lateinit var tipoTXT: TextView
    private lateinit var botonSalida: Button

    private var fechaHoraActual = ZonedDateTime.now()
    private var formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private var fechaHoraFormateada = fechaHoraActual.format(formato)

    data class BiciData(
        val nombre: String,
        val apellidos: String,
        val color: String,
        val cedula: String,
        val placa: String,
        val tipo: String,
        val correo: String,
        val fechaHora: String
    ) : java.io.Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_usuario_entrada)

        // Inicializar vistas
        botonSalida = findViewById(R.id.Salida)
        correoTXT = findViewById(R.id.correoFirebase)
        colorTXT = findViewById(R.id.colorFirebase)
        nombreTXT = findViewById(R.id.nombreFirebase)
        apellidoTXT = findViewById(R.id.apellidoFirebase)
        numeroTXT = findViewById(R.id.idVehiculoFirebase)
        tipoTXT = findViewById(R.id.tipoVehiculoFirebase)

        val correo = intent.getStringExtra("correo") ?: "No disponible"
        val vehiculo = intent.getStringExtra("tipo") ?: "No disponible"
        val idVehiculo = intent.getStringExtra("id") ?: "No disponible"

        Log.d("IntentReceived", "Correo: $correo, Tipo: $vehiculo, ID: $idVehiculo")

        if (correo.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No se recibió el correo", Toast.LENGTH_SHORT).show()
            return
        }

        verificarEntrada(correo, vehiculo, idVehiculo)

        botonSalida.setOnClickListener {
            finish()
        }
    }

    private fun verificarEntrada(correo: String, vehiculo: String, idVehiculo: String) {
        database.collection("Entrada")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // El usuario no ha ingresado, proceder a verificar salida
                    verificarSalida(correo, vehiculo, idVehiculo)
                } else {
                    Toast.makeText(this, "El usuario ya salio", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar entrada: ", e)
            }
    }

    private fun verificarSalida(correo: String, vehiculo: String, idVehiculo: String) {
        database.collection("Salida")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Si existe salida, eliminarla
                    for (document in documents) {
                        database.collection("Salida").document(document.id).delete()
                    }
                }
                // Proceder a registrar el ingreso
                registrarIngreso(correo, vehiculo, idVehiculo)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar salida: ", e)
            }
    }

    private fun registrarIngreso(correo: String, vehiculo: String, idVehiculo: String) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", correo)
            .whereEqualTo("tipo", vehiculo)
            .whereEqualTo("numero", idVehiculo)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("Firestore", "No se encontraron documentos con correo: $correo")
                    Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        val biciData = BiciData(
                            nombre = document.getString("nombre") ?: "",
                            apellidos = document.getString("apellidos") ?: "",
                            color = document.getString("color") ?: "",
                            cedula = document.getString("cedula") ?: "",
                            placa = document.getString("numero") ?: "",
                            tipo = document.getString("tipo") ?: "",
                            correo = document.getString("correo") ?: "",
                            fechaHora = fechaHoraFormateada.toString()
                        )

                        // Actualizar la interfaz
                        actualizarInterfaz(biciData)

                        // Registrar el ingreso
                        database.collection("Entrada")
                            .add(biciData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Datos cargados", Toast.LENGTH_SHORT).show()
                                actualizarDisponibilidad(biciData.tipo)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "No se cargaron los datos", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al registrar salio: ", e)
            }
    }

    private fun actualizarInterfaz(biciData: BiciData) {
        correoTXT.text = biciData.correo
        colorTXT.text = biciData.color
        nombreTXT.text = biciData.nombre
        apellidoTXT.text = biciData.apellidos
        numeroTXT.text = biciData.placa
        tipoTXT.text = biciData.tipo
    }

    private fun actualizarDisponibilidad(tipoVehiculo: String) {
        val documentId = when (tipoVehiculo) {
            "Furgon" -> "0ctYNlFXwtVw9ylURFXi"
            "Vehiculo Particular" -> "UF0tfabGHGitcj7En6Wy"
            "Bicicleta" -> "IuDC5XlTyhxhqU4It8SD"
            "Motocicleta" -> "ntHgnXs4Qbz074siOrvz"
            else -> return
        }

        database.collection("Disponibilidad").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val espacios = document.getLong(tipoVehiculo) ?: 0
                    if (espacios > 0) {
                        database.collection("Disponibilidad").document(documentId)
                            .update(tipoVehiculo, FieldValue.increment(-1))
                            .addOnSuccessListener {
                                Log.d("Firestore", "Campo '$tipoVehiculo' decrementado")
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error al actualizar el campo: ", e)
                            }
                    } else {
                        Toast.makeText(this, "No hay espacios disponibles para $tipoVehiculo", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.d("Firestore", "No se encontró el documento para $tipoVehiculo")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener el documento de disponibilidad: ", e)
            }
    }
}