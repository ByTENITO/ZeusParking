package com.example.parquiatenov10

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
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
    private lateinit var fotoUsuario: ImageView
    private lateinit var fotoVehi: ImageView

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

        //Responsividad
        Responsividad.inicializar(this)

        // Inicializar vistas
        botonSalida = findViewById(R.id.Salida)
        correoTXT = findViewById(R.id.correoFirebase)
        colorTXT = findViewById(R.id.colorFirebase)
        nombreTXT = findViewById(R.id.nombreFirebase)
        apellidoTXT = findViewById(R.id.apellidoFirebase)
        numeroTXT = findViewById(R.id.idVehiculoFirebase)
        tipoTXT = findViewById(R.id.tipoVehiculoFirebase)
        fotoUsuario = findViewById(R.id.fotoUsuario)
        fotoVehi = findViewById(R.id.fotoBici)

        val correo = intent.getStringExtra("correo") ?: "No disponible"
        val vehiculo = intent.getStringExtra("tipo") ?: "No disponible"
        val idVehiculo = intent.getStringExtra("id") ?: "No disponible"

        Log.d("IntentReceived", "Correo: $correo, Tipo: $vehiculo, ID: $idVehiculo")

        if (correo.isEmpty()) {
            Toast.makeText(this, "Error: No se recibió el correo", Toast.LENGTH_SHORT).show()
            return
        }

        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", correo)
            .whereEqualTo("tipo", vehiculo)
            .addSnapshotListener { documents, e ->
                if (documents != null) {
                    for (document in documents) {
                        val userId = document.getString("id")
                        val cedula = document.getString("cedula")
                        buscarImagenUser(userId, cedula)
                        buscarImagenVehi(userId, document.id, idVehiculo)
                        Log.d("FireStorage", "id -> ${document.id} ")
                        Log.d("FireStorage", "usuario -> $cedula")
                    }
                }
            }
        database.collection("Reservas")
            .whereEqualTo("tipo", vehiculo)
            .whereEqualTo("numero", idVehiculo)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    registrarIngreso(correo, vehiculo, idVehiculo)
                } else {
                    registroEntrada(correo, vehiculo, idVehiculo)
                }
            }

        botonSalida.setOnClickListener {
            finish()
        }
    }

    private fun buscarImagenUser(userId: String?, cedula: String?) {
        val storageImagenUser = FirebaseStorage.getInstance().reference
        val imageStorage = storageImagenUser.child("$userId/$cedula.png")
        Log.d("FireStorage", "$userId/$cedula.png")

        imageStorage.downloadUrl.addOnSuccessListener { uri ->
            Log.d("FireStorage", "URL obtenida: $uri")
            Picasso.get().load(uri).into(fotoUsuario)
        }.addOnFailureListener { e ->
            Log.e("FireStorage", "Error en URL:", e)
        }
    }

    private fun buscarImagenVehi(userId: String?, id: String?, idVehi: String?) {
        val storageImagenUser = FirebaseStorage.getInstance().reference
        val imageStorage = storageImagenUser.child("$userId/$id/$idVehi.png")

        imageStorage.downloadUrl.addOnSuccessListener { uri ->
            Log.d("FireStorage", "URL obtenida: $uri")
            Picasso.get().load(uri).into(fotoVehi)
        }.addOnFailureListener { e ->
            Log.e("FireStorage", "Error en URL:", e)
        }
    }

    private fun registrarIngreso(correo: String, vehiculo: String, idVehiculo: String) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", correo)
            .whereEqualTo("tipo", vehiculo)
            .whereEqualTo("numero", idVehiculo)
            .get()
            .addOnSuccessListener { documents ->
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

                    actualizarDisponibilidad(biciData.tipo, biciData)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al registrar salio: ", e)
            }
    }

    private fun actualizarDisponibilidad(tipoVehiculo: String, biciData: BiciData) {
        val documentId = when (tipoVehiculo) {
            "Furgon" -> "0ctYNlFXwtVw9ylURFXi"
            "Vehiculo Particular" -> "UF0tfabGHGitcj7En6Wy"
            "Bicicleta" -> "IuDC5XlTyhxhqU4It8SD"
            "Patineta Electrica" -> "IuDC5XlTyhxhqU4It8SD"
            "Motocicleta" -> "ntHgnXs4Qbz074siOrvz"
            else -> return
        }
        if (tipoVehiculo == "Patineta Electrica") {
            Consulta(documentId, "Bicicleta", biciData)
        } else {
            Consulta(documentId, tipoVehiculo, biciData)
        }
    }

    private fun Consulta(documentId: String, tipoVehiculo: String, biciData: BiciData) {
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
                                actualizarInterfaz(biciData)
                                database.collection("Entrada")
                                    .add(biciData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Datos cargados", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "No se cargaron los datos", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error al actualizar el campo: ", e)
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "No hay espacios disponibles para $tipoVehiculo",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Log.d("Firestore", "No se encontró el documento para $tipoVehiculo")
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener el documento de disponibilidad: ", e)
            }
    }

    private fun registroEntrada(correo: String, vehiculo: String, idVehiculo: String) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", correo)
            .whereEqualTo("tipo", vehiculo)
            .whereEqualTo("numero", idVehiculo)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("Firestore", "No se encontraron documentos con correo: $correo")
                    Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT)
                        .show()
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

                        // Registrar el entrada
                        database.collection("Entrada")
                            .add(biciData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Datos cargados", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "No se cargaron los datos", Toast.LENGTH_SHORT)
                                    .show()
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
}
