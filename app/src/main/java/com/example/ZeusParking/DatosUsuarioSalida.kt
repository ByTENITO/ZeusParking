package com.example.parquiatenov10


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
class DatosUsuarioSalida : AppCompatActivity() {
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
        setContentView(R.layout.activity_datos_usuario_salida)

        //Responsividad
        Responsividad.inicializar(this)

        // Inicializar vistas
        botonSalida = findViewById(R.id.SalidaExit)
        correoTXT = findViewById(R.id.correoFirebaseSalida)
        colorTXT = findViewById(R.id.colorFirebaseSalida)
        nombreTXT = findViewById(R.id.nombreFirebaseSalida)
        apellidoTXT = findViewById(R.id.apellidoFirebaseSalida)
        numeroTXT = findViewById(R.id.idVehiculoFirebaseSalida)
        tipoTXT = findViewById(R.id.tipoVehiculoFirebaseSalida)
        fotoUsuario = findViewById(R.id.fotoUsuarioSalida)
        fotoVehi = findViewById(R.id.fotoBiciSalida)

        val correo = intent.getStringExtra("correo") ?: "No disponible"
        val vehiculo = intent.getStringExtra("tipo") ?: "No disponible"
        val idVehiculo = intent.getStringExtra("id") ?: "No disponible"

        Log.d("IntentReceived", "Correo: $correo, Tipo: $vehiculo, ID: $idVehiculo")

        if (correo.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No se recibió el correo", Toast.LENGTH_SHORT).show()
            return
        }

        database.collection("Reservas")
            .whereEqualTo("tipo",vehiculo)
            .whereEqualTo("numero",idVehiculo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty){
                    for (document in documents) {
                        database.collection("Reservas").document(document.id).delete()
                    }
                }
            }

        database.collection("Bici_Usuarios")
            .whereEqualTo("correo",correo)
            .whereEqualTo("tipo",vehiculo)
            .addSnapshotListener {documents, e->
                if (documents != null) {
                    for (document in documents){
                        val userId = document.getString("id")
                        val cedula = document.getString("cedula")
                        buscarImagenUser(userId,cedula)
                        buscarImagenVehi(userId,document.id,idVehiculo)
                        Log.d("FireStorage","id -> ${document.id} ")
                        Log.d("FireStorage","usuario -> $cedula")
                    }
                }
            }

        registrarIngreso(correo, vehiculo, idVehiculo)

        botonSalida.setOnClickListener {
            finish()
        }
    }

    private fun buscarImagenUser (userId: String?, cedula: String? ){
        val storageImagenUser = FirebaseStorage.getInstance().reference
        val imageStorage = storageImagenUser.child("$userId/$cedula.png")
        Log.d("FireStorage","$userId/$cedula.png")

        imageStorage.downloadUrl.addOnSuccessListener { uri ->
            Log.d("FireStorage","URL obtenida: $uri")
            Picasso.get().load(uri).into(fotoUsuario)
        }.addOnFailureListener { e ->
            Log.e("FireStorage","Error en URL:",e)
        }
    }

    private fun buscarImagenVehi (userId: String?, id: String?,idVehi: String?){
        val storageImagenUser = FirebaseStorage.getInstance().reference
        val imageStorage = storageImagenUser.child("$userId/$id/$idVehi.png")

        imageStorage.downloadUrl.addOnSuccessListener { uri ->
            Log.d("FireStorage","URL obtenida: $uri")
            Picasso.get().load(uri).into(fotoVehi)
        }.addOnFailureListener { e ->
            Log.e("FireStorage","Error en URL:",e)
        }
    }

    private fun registrarIngreso(correo: String, vehiculo: String, idVehiculo: String) {
        database.collection("Salida")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { salidaDocuments ->
                val batch = database.batch()
                for (document in salidaDocuments) {
                    batch.delete(document.reference)
                }

                batch.commit().addOnSuccessListener {
                    registrarNuevaSalida(correo, vehiculo, idVehiculo)
                }
            }
    }

    private fun registrarNuevaSalida(correo: String, vehiculo: String, idVehiculo: String) {
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

                    actualizarInterfaz(biciData)

                    database.collection("Salida")
                        .add(biciData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Salida registrada correctamente", Toast.LENGTH_SHORT).show()
                            actualizarDisponibilidad(biciData.tipo)
                        }
                }
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
            "Patineta Electrica" -> "IuDC5XlTyhxhqU4It8SD"
            "Motocicleta" -> "ntHgnXs4Qbz074siOrvz"
            else -> return
        }
        val FijosId = when (tipoVehiculo) {
            "Furgon" -> "NLRmedawc0M0nrpDt9Ci"
            "Vehiculo Particular" -> "edYUNbYSmPtvu1H6dI93"
            "Bicicleta" -> "sPcLdzFgRF2eAY5BWvFC"
            "Patineta Electrica" -> "sPcLdzFgRF2eAY5BWvFC"
            "Motocicleta" -> "AQjYvV224T01lrSEeQQY"
            else -> return
        }
        if (tipoVehiculo == "Patineta Electrica"){
            Consulta(documentId,"Bicicleta",FijosId)
        }else{
            Consulta(documentId, tipoVehiculo, FijosId)
        }
    }

    private fun Consulta(documentId: String, tipoVehiculo: String, FijosId: String){
        database.collection("Disponibilidad")
            .document(documentId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val espacios = document.getLong(tipoVehiculo) ?: 0
                    database.collection("EspaciosFijos").document(FijosId).get()
                        .addOnSuccessListener { document ->
                            val espaciosFijos = document.getLong(tipoVehiculo) ?: 0
                            if (espacios!=espaciosFijos) {
                                database.collection("Disponibilidad").document(documentId)
                                    .update(tipoVehiculo, FieldValue.increment(1))
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Campo '$tipoVehiculo' aumentado")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Error al actualizar el campo: ", e)
                                    }
                            } else {
                                Toast.makeText(
                                    this,
                                    "No puede salir porque se superaron los espacios disponibles para $tipoVehiculo",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
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