package com.example.parquiatenov10

import android.graphics.Bitmap
import android.graphics.Color
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class Reservacion : AppCompatActivity() {

    private var database = FirebaseFirestore.getInstance()
    private lateinit var CodigoQR: ImageView
    private lateinit var nombreTxT: TextView
    private lateinit var cedulaTxT: TextView
    private lateinit var tipoTxT: TextView
    private lateinit var idVehiTxT: TextView
    private lateinit var colorTxT: TextView
    private lateinit var horaTxT: TextView
    private lateinit var salir: Button
    private var fechaHoraActual = ZonedDateTime.now()
    private var formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private var fechaHoraFormateada = fechaHoraActual.format(formato)

    data class ReserData(
        val nombre: String,
        val apellidos: String,
        val color: String,
        val cedula: String,
        val numero: String,
        val tipo: String,
        val fechaHora: String,
        val id: String
    ) : java.io.Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserva)
        CodigoQR = findViewById(R.id.CodigoReserva)
        nombreTxT = findViewById(R.id.NombreTxt)
        cedulaTxT = findViewById(R.id.cedulaTxt)
        tipoTxT = findViewById(R.id.TipoTxt)
        idVehiTxT = findViewById(R.id.idVehiTxt)
        colorTxT = findViewById(R.id.ColorTxt)
        horaTxT = findViewById(R.id.Hora)
        salir = findViewById(R.id.SalidaReser)

        //Responsividad
        Responsividad.inicializar(this)


        val userId = intent.getStringExtra("ID") ?: "No disponible"
        val vehiculo = intent.getStringExtra("Tipo") ?: "No disponible"
        Log.d("Datos","Datos recibios, tipo $vehiculo , id:$userId")
        generateAndDisplayQrCode()
        consulta(userId, vehiculo)
        salir.setOnClickListener {
            finish()
        }
    }

    private fun consulta(userId: String, vehiculo: String) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("id", userId)
            .whereEqualTo("tipo", vehiculo)
            .addSnapshotListener { documents, e ->
                if (documents != null) {
                    for (document in documents) {
                        val reserData = ReserData(
                            nombre = document.getString("nombre") ?: "",
                            apellidos = document.getString("apellidos") ?: "",
                            color = document.getString("color") ?: "",
                            cedula = document.getString("cedula") ?: "",
                            numero = document.getString("numero") ?: "",
                            tipo = document.getString("tipo") ?: "",
                            fechaHora = fechaHoraFormateada.toString(),
                            id = document.getString("id")?:""
                        )
                        actualizarInterfaz(reserData)
                        Reserva(reserData.numero,vehiculo,reserData)
                    }
                }
            }
    }

    private fun Reserva(numero: String, tipo: String, reserva: ReserData){
        database.collection("Reservas")
            .whereEqualTo("tipo",tipo)
            .whereEqualTo("numero",numero)
            .get()
            .addOnSuccessListener{ documents ->
                database.collection("Reservas")
                    .add(reserva)
                    .addOnSuccessListener { document ->
                        actualizarDisponibilidad(tipo)
                        Toast.makeText(this, "Reserva realizada, guarda soporte de esta para que sea validada antes de ingresar", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun actualizarInterfaz(reserData: ReserData) {
        nombreTxT.text = (buildString {
            append(reserData.nombre)
            append(" ")
            append(reserData.apellidos)
        })
        colorTxT.text = reserData.color
        cedulaTxT.text = reserData.cedula
        idVehiTxT.text = reserData.numero
        tipoTxT.text = reserData.tipo
        horaTxT.text = reserData.fechaHora
    }

    private fun generateAndDisplayQrCode() {
        val sharedPref = getSharedPreferences("MisDatos", MODE_PRIVATE)
        val correo = sharedPref.getString("nombreUsuario", "Desconocido") ?: "DefaultValue"

        try {
            val qrBitmap = generateQRCode(correo, 700)
            CodigoQR.setImageBitmap(qrBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateQRCode(content: String, size: Int): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.MARGIN to 1,
                EncodeHintType.ERROR_CORRECTION to "L"
            )

            val bitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            null
        }
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
        if (tipoVehiculo == "Patineta Electrica"){
            Consulta(documentId,"Bicicleta")
        }else{
            Consulta(documentId, tipoVehiculo)
        }
    }
    private fun Consulta(documentId: String, tipoVehiculo: String){
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