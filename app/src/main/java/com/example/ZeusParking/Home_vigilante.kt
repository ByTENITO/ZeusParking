package com.example.parquiatenov10

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.NotificationCompat
import com.example.ZeusParking.BaseNavigationActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.File

class Home_vigilante : BaseNavigationActivity() {

    private var database = FirebaseFirestore.getInstance()
    private lateinit var perfilImageView: ImageView
    private lateinit var notifiFurgon: TextView
    private lateinit var notifiVehiculoParticular: TextView
    private lateinit var notifiBicicleta: TextView
    private lateinit var notifiMotocicleta: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_vigilante)
        enableEdgeToEdge()

        setupNavigation()
        initViews()
        setupUserProfile()
        listenToAllAvailabilities()
    }

    private fun initViews() {
        perfilImageView = findViewById(R.id.profile_image)
        notifiFurgon = findViewById(R.id.notiFurgon)
        notifiVehiculoParticular = findViewById(R.id.notiVehiculo)
        notifiBicicleta = findViewById(R.id.notiBicicleta)
        notifiMotocicleta = findViewById(R.id.notiMoto)
    }

    private fun setupUserProfile() {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        val welcomeText = findViewById<TextView>(R.id.welcome_text)
        val emailText = findViewById<TextView>(R.id.email_text)

        // Get user data from Firestore
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    val nombre = document.getString("nombre") ?: "Vigilante"
                    val cedula = document.getString("cedula") ?: ""

                    welcomeText.text = "Bienvenido, $nombre"
                    emailText.text = email

                    // Load profile image from Firebase Storage
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null && cedula.isNotEmpty()) {
                        buscarImagenUser(userId, cedula)
                    }
                } else {
                    welcomeText.text = "Bienvenido, Vigilante"
                    emailText.text = email
                }
            }
            .addOnFailureListener {
                welcomeText.text = "Bienvenido, Vigilante"
                emailText.text = email
            }
    }

    private fun buscarImagenUser(userId: String?, cedula: String?) {
        val storageImagenUser = FirebaseStorage.getInstance().reference.child("$userId/$cedula.png")
        storageImagenUser.metadata.addOnSuccessListener { metadata ->
            val actualizacionRemota = metadata.updatedTimeMillis
            val carpeta = File(getExternalFilesDir(null), "70T05_U$3R")

            if (!carpeta.exists() || carpeta.lastModified() < actualizacionRemota) {
                carpeta.mkdirs()
                val file = File(carpeta, "$cedula.png")
                storageImagenUser.getFile(file).addOnSuccessListener {
                    val bitMap = BitmapFactory.decodeFile(file.absolutePath)
                    perfilImageView.setImageBitmap(bitMap)
                }.addOnFailureListener {
                    Log.e("FireStorage", "Error al descargar la imagen:", it)
                }
            } else {
                val archivo = File(getExternalFilesDir(null), "70T05_U$3R/$cedula.png")
                if (archivo.exists()) {
                    val bitMap = BitmapFactory.decodeFile(archivo.absolutePath)
                    perfilImageView.setImageBitmap(bitMap)
                }
            }
        }
    }

    private fun listenToAllAvailabilities() {
        // Furgon
        escucharDisponibilidad(
            "0ctYNlFXwtVw9ylURFXi",
            "Furgon",
            notifiFurgon,
            findViewById(R.id.progressFurgon),
            5
        )

        // Vehiculo Particular
        escucharDisponibilidad(
            "UF0tfabGHGitcj7En6Wy",
            "Vehiculo Particular",
            notifiVehiculoParticular,
            findViewById(R.id.progressVehiculo),
            10
        )

        // Bicicleta
        escucharDisponibilidad(
            "IuDC5XlTyhxhqU4It8SD",
            "Bicicleta",
            notifiBicicleta,
            findViewById(R.id.progressBicicleta),
            10
        )

        // Motocicleta
        escucharDisponibilidad(
            "ntHgnXs4Qbz074siOrvz",
            "Motocicleta",
            notifiMotocicleta,
            findViewById(R.id.progressMoto),
            10
        )
    }

    private fun escucharDisponibilidad(
        documentId: String,
        campo: String,
        textoView: TextView,
        progressBar: com.google.android.material.progressindicator.LinearProgressIndicator,
        maxCapacity: Int
    ) {
        database.collection("Disponibilidad")
            .document(documentId)
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.d("Firestore", "Error al escuchar: $documentId", e)
                    return@addSnapshotListener
                }

                val espacios = document?.getLong(campo)?.toInt() ?: 0
                val percentage = (espacios.toFloat() / maxCapacity.toFloat() * 100).toInt()

                textoView.text = "$espacios/$maxCapacity espacios para $campo"
                progressBar.progress = percentage

                if (espacios <= 2) {
                    mostrarNotificacion(
                        this,
                        "Alerta de Disponibilidad",
                        "Quedan pocos espacios para $campo ($espacios)"
                    )
                }
            }
    }

    private fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(context, "ZeusParking")
            .setSmallIcon(R.drawable.icon_zeusparking)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(Color.BLUE)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun getCurrentNavigationItem(): Int = R.id.home_vigi
}