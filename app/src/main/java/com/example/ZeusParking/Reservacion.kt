package com.example.parquiatenov10

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

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

        Responsividad.inicializar(this)

        val userId = intent.getStringExtra("ID") ?: "No disponible"
        val vehiculo = intent.getStringExtra("Tipo") ?: "No disponible"
        val numero = intent.getStringExtra("numero") ?: "No disponible"
        Log.d("Datos","Datos recibios, tipo $vehiculo ,numero: $numero ,id:$userId")
        generateAndDisplayQrCode()
        consulta(userId, vehiculo, numero)
        salir.setOnClickListener {
            finish()
        }

        crearCanalNotificacion(this)
    }

    private fun consulta(userId: String, vehiculo: String, numero: String) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("id", userId)
            .whereEqualTo("tipo", vehiculo)
            .whereEqualTo("numero",numero)
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
                        verificarYRealizarReserva(numero, vehiculo, reserData)
                    }
                }
            }
    }

    private fun verificarYRealizarReserva(numero: String, tipo: String, reserva: ReserData) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Primero verificar disponibilidad
        verificarDisponibilidad(tipo) { hayEspacio ->
            if (hayEspacio) {
                realizarReserva(userId, numero, tipo, reserva)
            } else {
                Toast.makeText(this, "No hay espacios disponibles para $tipo", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun verificarDisponibilidad(tipoVehiculo: String, callback: (Boolean) -> Unit) {
        val documentId = when (tipoVehiculo) {
            "Furgon" -> "0ctYNlFXwtVw9ylURFXi"
            "Vehiculo Particular" -> "UF0tfabGHGitcj7En6Wy"
            "Bicicleta" -> "IuDC5XlTyhxhqU4It8SD"
            "Patineta Electrica" -> "IuDC5XlTyhxhqU4It8SD"
            "Motocicleta" -> "ntHgnXs4Qbz074siOrvz"
            else -> {
                callback(false)
                return
            }
        }

        val campo = if (tipoVehiculo == "Patineta Electrica") "Bicicleta" else tipoVehiculo

        database.collection("Disponibilidad").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val espacios = document.getLong(campo) ?: 0
                    callback(espacios > 0)
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al obtener el documento de disponibilidad: ", e)
                callback(false)
            }
    }

    private fun realizarReserva(userId: String, numero: String, tipo: String, reserva: ReserData) {
        database.collection("Reservas")
            .whereEqualTo("tipo",tipo)
            .whereEqualTo("numero",numero)
            .whereEqualTo("usuarioId", userId)
            .get()
            .addOnSuccessListener{ documents ->
                if (documents.isEmpty()) {
                    val reservaData = hashMapOf(
                        "usuarioId" to userId,
                        "nombre" to reserva.nombre, // Añadir nombre
                        "apellidos" to reserva.apellidos, // Añadir apellidos
                        "numero" to reserva.numero,
                        "tipoVehiculo" to reserva.tipo,
                        "fecha" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        "hora" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                        "activa" to true
                    )

                    database.collection("Reservas")
                        .add(reservaData)
                        .addOnSuccessListener { document ->
                            actualizarDisponibilidad(tipo)
                            mostrarNotificacion(
                                this,
                                "Reserva exitosa",
                                "Tu reserva para $tipo ha sido registrada"
                            )
                            Toast.makeText(this, "Reserva realizada, guarda soporte de esta para que sea validada antes de ingresar", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Ya tienes una reserva activa", Toast.LENGTH_SHORT).show()
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

        val campo = if (tipoVehiculo == "Patineta Electrica") "Bicicleta" else tipoVehiculo

        database.collection("Disponibilidad").document(documentId)
            .update(campo, FieldValue.increment(-1))
            .addOnSuccessListener {
                Log.d("Firestore", "Campo '$campo' decrementado")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al actualizar el campo: ", e)
            }
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