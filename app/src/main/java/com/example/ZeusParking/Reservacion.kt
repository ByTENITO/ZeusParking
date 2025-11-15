package com.example.parquiatenov10

import android.R.attr.description
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import androidx.core.app.NotificationCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

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
        val fecha: String,
        val horaReserva: String,
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

        Responsividad.inicializar(this)

        val userId = intent.getStringExtra("ID") ?: "No disponible"
        val vehiculo = intent.getStringExtra("Tipo") ?: "No disponible"
        val numero = intent.getStringExtra("numero") ?: "No disponible"
        val horaReserva = intent.getStringExtra("horaReserva") ?: LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))

        Log.d("Datos","Datos recibidos - Tipo: $vehiculo, Número: $numero, ID: $userId, Hora: $horaReserva")

        generateAndDisplayQrCode()
        registrarIngreso(userId, vehiculo, numero, horaReserva)

        salir.setOnClickListener { finish() }
        crearCanalNotificacion(this)
    }

    private fun generateAndDisplayQrCode() {
        val correo = FirebaseAuth.getInstance().currentUser?.email.toString()

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

    private fun registrarIngreso(idUser: String, vehiculo: String, idVehiculo: String, horaReserva: String) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("id", idUser)
            .whereEqualTo("tipo", vehiculo)
            .whereEqualTo("numero", idVehiculo)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val reserData = ReserData(
                        nombre = document.getString("nombre") ?: "No disponible",
                        apellidos = document.getString("apellidos") ?: "",
                        color = document.getString("color") ?: "No disponible",
                        cedula = document.getString("cedula") ?: "No disponible",
                        numero = document.getString("numero") ?: "No disponible",
                        tipo = document.getString("tipo") ?: "No disponible",
                        fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        horaReserva = horaReserva,
                        id = idUser
                    )
                    Log.d("Firestore", "Datos de reserva a guardar: $reserData")
                    actualizarDisponibilidad(reserData.tipo, reserData)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al registrar reserva: ", e)
                Toast.makeText(this, "Error al registrar reserva", Toast.LENGTH_SHORT).show()
            }
    }

    private fun actualizarDisponibilidad(tipoVehiculo: String, reserData: ReserData) {
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
            "Motocicleta" -> "AQjYvV224T01lrSEeQQY"
            else -> return
        }

        if (tipoVehiculo == "Patineta Electrica") {
            Consulta(documentId, FijosId, "Bicicleta", reserData)
        } else {
            Consulta(documentId, FijosId, tipoVehiculo, reserData)
        }
    }

    private fun Consulta(documentId: String, FijosId: String, tipoVehiculo: String, reserData: ReserData) {
        database.collection("EspaciosFijos").document(FijosId)
            .get()
            .addOnSuccessListener { documentFijo ->
                val espacioFijo = documentFijo.getLong(tipoVehiculo) ?: 0
            database.collection("Disponibilidad").document(documentId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val espacios = document.getLong(tipoVehiculo) ?: 0
                        val limite = (espacioFijo * 0.80).toInt()
                        if (espacios > 0 && espacios > limite) {
                            disminuirDisponibilidad(documentId, tipoVehiculo, reserData)
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
                }.addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener el documento de disponibilidad: ", e)
            }
        }
    }

    private fun disminuirDisponibilidad (documentId: String, tipoVehiculo: String, reserData: ReserData){
        database.collection("Disponibilidad").document(documentId)
            .update(tipoVehiculo, FieldValue.increment(-1))
            .addOnSuccessListener {
                Log.d("Firestore", "Campo '$tipoVehiculo' decrementado")
                actualizarInterfaz(reserData)
                registrarReserva(reserData)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al actualizar el campo: ", e)
            }
    }

    private fun registrarReserva(reserData: ReserData){
        database.collection("Reservas").add(reserData).addOnSuccessListener {
            Toast.makeText(this, "Datos cargados", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "No se cargaron los datos", Toast.LENGTH_SHORT).show()
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
        horaTxT.text = "${reserData.horaReserva}"
    }

    private fun crearCanalNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Canal ZeusParking"
            val descripcion = "Notificaciones de reservas"
            val importancia = NotificationManager.IMPORTANCE_DEFAULT
            val canal = NotificationChannel("ZeusParking", nombre, importancia).apply {
                description = descripcion
            }
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    private fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, "ZeusParking")
            .setSmallIcon(R.drawable.icon_zeusparking)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(Color.BLUE)
            .setAutoCancel(true)

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, builder.build())
    }
}