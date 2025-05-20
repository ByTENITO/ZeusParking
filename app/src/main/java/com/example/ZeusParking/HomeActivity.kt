package com.example.parquiatenov10

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import com.example.ZeusParking.BaseNavigationActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : BaseNavigationActivity() {
    private var database = FirebaseFirestore.getInstance()
    private lateinit var perfilImageView: com.google.android.material.imageview.ShapeableImageView
    private lateinit var welcomeText: TextView
    private lateinit var userEmail: TextView
    private lateinit var vehiculosContainer: LinearLayout
    private lateinit var portatilesContainer: LinearLayout
    private lateinit var disponibilidadContainer: LinearLayout
    private lateinit var reservaText: TextView
    private lateinit var reservaCard: CardView
    private lateinit var qrImageView: ImageView
    private lateinit var btnVerQR: Button
    private lateinit var btnEliminarReserva: Button
    private lateinit var btnModificarReserva: Button
    private var reservaId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        enableEdgeToEdge()

        Responsividad.inicializar(this)
        setupNavigation()

        // Inicializar vistas
        perfilImageView = findViewById(R.id.FotoPerfil_ImageView)
        welcomeText = findViewById(R.id.welcome_text)
        userEmail = findViewById(R.id.user_email)
        vehiculosContainer = findViewById(R.id.vehiculos_container)
        portatilesContainer = findViewById(R.id.portatiles_container)
        disponibilidadContainer = findViewById(R.id.disponibilidad_container)
        reservaText = findViewById(R.id.reserva_text)
        reservaCard = findViewById(R.id.reserva_card)
        qrImageView = findViewById(R.id.qr_image)
        btnVerQR = findViewById(R.id.btn_ver_qr)
        btnEliminarReserva = findViewById(R.id.btn_eliminar_reserva)
        btnModificarReserva = findViewById(R.id.btn_modificar_reserva)

        // Configurar botones
        findViewById<Button>(R.id.btnRegistroVehi).setOnClickListener {
            startActivity(Intent(this, RegistrarBiciActivity::class.java))
        }

        findViewById<Button>(R.id.btnRegistroPC).setOnClickListener {
            startActivity(Intent(this, RegistroPC::class.java))
        }

        // Cargar datos del usuario
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            welcomeText.text = "Bienvenido, ${it.displayName ?: "Usuario"}"
            userEmail.text = it.email ?: "correo@ejemplo.com"

            if (it.photoUrl != null) {
                Picasso.get().load(it.photoUrl).into(perfilImageView)
            }

            // Configurar escuchas en tiempo real
            configurarEscuchasEnTiempoReal(it.email, it.uid)
        }

        crearCanalNotificacion(this)
    }

    private fun configurarEscuchasEnTiempoReal(email: String?, userId: String) {
        // Escuchar cambios en vehículos
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", email)
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    Log.w("HomeActivity", "Error escuchando vehículos", e)
                    return@addSnapshotListener
                }
                documents?.let {
                    cargarVehiculosUsuario(email)
                }
            }

        // Escuchar cambios en portátiles
        database.collection("Portatiles")
            .whereEqualTo("usuarioId", userId)
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    Log.w("HomeActivity", "Error escuchando portátiles", e)
                    return@addSnapshotListener
                }
                documents?.let {
                    actualizarVistaPortatiles(it)
                }
            }

        // Escuchar cambios en reservas
        database.collection("Reservas")
            .whereEqualTo("id", userId)
            .addSnapshotListener { documents, e ->
                if (e != null) {
                    Log.w("HomeActivity", "Error escuchando reservas", e)
                    return@addSnapshotListener
                }
                if (documents?.isEmpty == true) {
                    mostrarSinReservas()
                } else {
                    documents?.let {
                        cargarReservasUsuario(userId)
                    }
                }
            }
    }

    private fun cargarVehiculosUsuario(email: String?) {
        if (email.isNullOrEmpty()) return

        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", email)
            .get()
            .addOnSuccessListener { documents ->
                vehiculosContainer.removeAllViews()
                disponibilidadContainer.removeAllViews()

                if (documents.isEmpty) {
                    val emptyView = TextView(this).apply {
                        text = "No tienes vehículos registrados"
                        setTextColor(Color.WHITE)
                    }
                    vehiculosContainer.addView(emptyView)
                    return@addOnSuccessListener
                }

                val tiposVehiculos = mutableSetOf<String>()
                val vehiculosPorTipo = mutableMapOf<String, MutableList<DocumentSnapshot>>()

                for (document in documents) {
                    val tipo = document.getString("tipo") ?: "Vehículo"
                    tiposVehiculos.add(tipo)

                    if (!vehiculosPorTipo.containsKey(tipo)) {
                        vehiculosPorTipo[tipo] = mutableListOf()
                    }
                    vehiculosPorTipo[tipo]?.add(document)
                }

                for ((tipo, docs) in vehiculosPorTipo) {
                    for (document in docs) {
                        val numero = document.getString("numero") ?: "N/A"
                        val color = document.getString("color") ?: "N/A"

                        val vehiculoView = LayoutInflater.from(this)
                            .inflate(R.layout.item_vehiculo, vehiculosContainer, false)

                        vehiculoView.findViewById<TextView>(R.id.tipo_vehiculo).text = tipo
                        vehiculoView.findViewById<TextView>(R.id.numero_vehiculo).text = "Número: $numero"
                        vehiculoView.findViewById<TextView>(R.id.color_vehiculo).text = "Color: $color"

                        vehiculosContainer.addView(vehiculoView)
                    }
                }

                configurarDisponibilidad(tiposVehiculos)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar vehículos: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun configurarDisponibilidad(tiposVehiculos: Set<String>) {
        if (tiposVehiculos.contains("Furgon")) {
            agregarTarjetaDisponibilidad(
                "Furgon",
                "0ctYNlFXwtVw9ylURFXi",
                "#E8F5E9",
                listOf(
                    (0..0) to "No quedan espacios para Furgon",
                    (1..2) to "Quedan pocos espacios en el parqueadero de Furgon, quedan: {espacios}",
                    (3..4) to "Quedan {espacios} espacios para Furgon"
                )
            )
        }

        if (tiposVehiculos.contains("Vehiculo Particular")) {
            agregarTarjetaDisponibilidad(
                "Vehiculo Particular",
                "UF0tfabGHGitcj7En6Wy",
                "#E3F2FD",
                listOf(
                    (0..0) to "No quedan espacios para Vehiculo Particular",
                    (1..5) to "Quedan pocos espacios en el parqueadero de Vehiculo Particular, quedan: {espacios}",
                    (6..10) to "Quedan {espacios} espacios para Vehiculo Particular"
                )
            )
        }

        // Modificado para incluir Patineta Electrica
        if (tiposVehiculos.contains("Bicicleta") || tiposVehiculos.contains("Patineta Electrica")) {
            agregarTarjetaDisponibilidad(
                "Bicicleta",
                "IuDC5XlTyhxhqU4It8SD",
                "#FFF3E0",
                listOf(
                    (0..0) to "No quedan espacios para Bicicleta/Patineta",
                    (1..5) to "Quedan pocos espacios en el parqueadero de Bicicleta/Patineta, quedan: {espacios}",
                    (6..10) to "Quedan {espacios} espacios para Bicicleta/Patineta"
                )
            )
        }

        if (tiposVehiculos.contains("Motocicleta")) {
            agregarTarjetaDisponibilidad(
                "Motocicleta",
                "ntHgnXs4Qbz074siOrvz",
                "#F3E5F5",
                listOf(
                    (0..0) to "No quedan espacios para Motocicleta",
                    (1..5) to "Quedan pocos espacios en el parqueadero de Motocicleta, quedan: {espacios}",
                    (6..10) to "Quedan {espacios} espacios para Motocicleta"
                )
            )
        }
    }

    private fun agregarTarjetaDisponibilidad(
        tipo: String,
        docId: String,
        color: String,
        umbrales: List<Pair<IntRange, String>>
    ) {
        val cardView = LayoutInflater.from(this).inflate(
            R.layout.item_disponibilidad,
            disponibilidadContainer,
            false
        ) as CardView

        val textView = cardView.findViewById<TextView>(R.id.notificacion_item)
        cardView.setCardBackgroundColor(Color.parseColor(color))

        escucharDisponibilidad(docId, tipo, textView, umbrales)

        disponibilidadContainer.addView(cardView)
    }

    private fun actualizarVistaPortatiles(documents: QuerySnapshot) {
        portatilesContainer.removeAllViews()

        if (documents.isEmpty) {
            val emptyView = TextView(this).apply {
                text = "No tienes portátiles registrados"
                setTextColor(Color.WHITE)
            }
            portatilesContainer.addView(emptyView)
            return
        }

        for (document in documents) {
            val marca = document.getString("marca") ?: "N/A"
            val modelo = document.getString("modelo") ?: "N/A"
            val serial = document.getString("serial") ?: "N/A"

            val portatilView = LayoutInflater.from(this)
                .inflate(R.layout.item_portatil, portatilesContainer, false)

            portatilView.findViewById<TextView>(R.id.marca_portatil).text = "Marca: $marca"
            portatilView.findViewById<TextView>(R.id.modelo_portatil).text = "Modelo: $modelo"
            portatilView.findViewById<TextView>(R.id.serial_portatil).text = "Serial: $serial"

            portatilesContainer.addView(portatilView)
        }
    }

    private fun cargarReservasUsuario(userId: String?) {
        if (userId.isNullOrEmpty()) return

        database.collection("Reservas")
            .whereEqualTo("id", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    mostrarSinReservas()
                } else {
                    for (document in documents) {
                        reservaId = document.id
                        val fecha = document.getString("fecha") ?: ""
                        val tipoVehiculo = document.getString("tipo") ?: ""
                        val numeroVehiculo = document.getString("numero") ?: ""
                        val horaReserva = document.getString("horaReserva") ?: ""

                        reservaText.text =
                            "Reserva activa para $tipoVehiculo ($numeroVehiculo) el $fecha a las $horaReserva"
                        mostrarBotonesReserva()

                        btnVerQR.setOnClickListener {
                            qrImageView.visibility =
                                if (qrImageView.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        }

                        btnEliminarReserva.setOnClickListener {
                            mostrarDialogoConfirmacionEliminar(tipoVehiculo)
                        }

                        btnModificarReserva.setOnClickListener {
                            mostrarDialogoModificarReserva(userId)
                        }

                        val correo = FirebaseAuth.getInstance().currentUser?.email.toString()

                        try {
                            val qrBitmap = generateQRCode(correo, 500)
                            qrImageView.setImageBitmap(qrBitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                reservaText.text = "Error al cargar reservas"
                Log.e("HomeActivity", "Error cargando reservas", e)
            }
    }

    private fun mostrarSinReservas() {
        reservaText.text = "No tienes reservas activas"
        btnVerQR.visibility = View.GONE
        btnEliminarReserva.visibility = View.GONE
        btnModificarReserva.visibility = View.GONE
        qrImageView.visibility = View.GONE
    }

    private fun mostrarBotonesReserva() {
        btnVerQR.visibility = View.VISIBLE
        btnEliminarReserva.visibility = View.VISIBLE
        btnModificarReserva.visibility = View.VISIBLE
    }

    private fun mostrarDialogoConfirmacionEliminar(tipoVehiculo: String) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar reserva")
            .setMessage("¿Estás seguro de que deseas cancelar esta reserva?")
            .setPositiveButton("Sí") { dialog, which ->
                eliminarReserva(tipoVehiculo)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun eliminarReserva(tipoVehiculo: String) {
        if (reservaId.isEmpty()) return

        database.collection("Reservas").document(reservaId).delete()
        Toast.makeText(this, "Reserva cancelada correctamente", Toast.LENGTH_SHORT).show()
        mostrarSinReservas()
        actualizarDisponibilidad(tipoVehiculo)
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

    private fun mostrarDialogoModificarReserva(userId: String) {
        database.collection("Reservas")
            .document(reservaId)
            .get()
            .addOnSuccessListener { reservaDoc ->
                val tipoActual = reservaDoc.getString("tipoVehiculo") ?: ""

                database.collection("Bici_Usuarios")
                    .whereEqualTo("id", userId)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty()) { // Corregido: agregado paréntesis
                            Toast.makeText(
                                this,
                                "No tienes vehículos registrados",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@addOnSuccessListener
                        }

                        // Filtrar solo vehículos de tipos diferentes al actual
                        val otrosVehiculos = documents.filter {
                            it.getString("tipo") != tipoActual
                        }

                        if (otrosVehiculos.isEmpty()) { // Corregido: agregado paréntesis
                            Toast.makeText(
                                this,
                                "No tienes otros vehículos para modificar la reserva",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@addOnSuccessListener
                        }

                        val tiposVehiculos =
                            otrosVehiculos.mapNotNull { it.getString("tipo") }.distinct()

                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Selecciona un nuevo tipo de vehículo")

                        builder.setSingleChoiceItems(
                            tiposVehiculos.toTypedArray(),
                            -1
                        ) { dialog, which ->
                            val nuevoTipo = tiposVehiculos[which]
                            val vehiculosFiltrados =
                                otrosVehiculos.filter { it.getString("tipo") == nuevoTipo }

                            if (vehiculosFiltrados.size == 1) {
                                val nuevoNumero = vehiculosFiltrados[0].getString("numero") ?: ""
                                modificarReserva(nuevoTipo, nuevoNumero)
                                dialog.dismiss()
                            } else {
                                mostrarDialogoSeleccionVehiculo(vehiculosFiltrados, nuevoTipo)
                                dialog.dismiss()
                            }
                        }

                        builder.setNegativeButton("Cancelar", null)
                        builder.show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al cargar vehículos: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("HomeActivity", "Error cargando vehículos", e)
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al obtener reserva actual: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("HomeActivity", "Error obteniendo reserva", e)
            }
    }

    private fun mostrarDialogoSeleccionVehiculo(vehiculos: List<DocumentSnapshot>, tipo: String) {
        val numerosVehiculos = vehiculos.map { it.getString("numero") ?: "" }

        AlertDialog.Builder(this)
            .setTitle("Selecciona un vehículo ($tipo)")
            .setItems(numerosVehiculos.toTypedArray()) { dialog, which ->
                val numero = numerosVehiculos[which]
                modificarReserva(tipo, numero)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun modificarReserva(nuevoTipo: String, nuevoNumero: String) {

        // Primero verificar disponibilidad del nuevo tipo
        verificarDisponibilidad(nuevoTipo) { disponible ->
            if (disponible) {
                // Obtener el tipo actual para incrementar su disponibilidad
                database.collection("Reservas")
                    .document(reservaId)
                    .get()
                    .addOnSuccessListener { document ->
                        val tipoActual = document.getString("tipo") ?: ""

                        // Actualizar la reserva
                        database.collection("Reservas")
                            .document(reservaId)
                            .update(
                                "tipo",
                                nuevoTipo,
                                "numero",
                                nuevoNumero
                            )
                            .addOnSuccessListener {
                                // Actualizar disponibilidades
                                actualizarDisponibilidad(tipoActual, nuevoTipo)
                                Toast.makeText(
                                    this,
                                    "Reserva modificada correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                cargarReservasUsuario(FirebaseAuth.getInstance().currentUser?.uid)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error al modificar reserva: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("HomeActivity", "Error modificando reserva", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al obtener reserva: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("HomeActivity", "Error obteniendo reserva", e)
                    }
            } else {
                Toast.makeText(
                    this,
                    "No hay espacios disponibles para $nuevoTipo",
                    Toast.LENGTH_SHORT
                ).show()
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
                Log.e("HomeActivity", "Error verificando disponibilidad", e)
                callback(false)
            }
    }

    private fun actualizarDisponibilidad(tipoAnterior: String, tipoNuevo: String) {
        // Incrementar disponibilidad del tipo anterior
        val docIdAnterior = when (tipoAnterior) {
            "Furgon" -> "0ctYNlFXwtVw9ylURFXi"
            "Vehiculo Particular" -> "UF0tfabGHGitcj7En6Wy"
            "Bicicleta" -> "IuDC5XlTyhxhqU4It8SD"
            "Patineta Electrica" -> "IuDC5XlTyhxhqU4It8SD"
            "Motocicleta" -> "ntHgnXs4Qbz074siOrvz"
            else -> return
        }

        val campoAnterior = if (tipoAnterior == "Patineta Electrica") "Bicicleta" else tipoAnterior

        // Decrementar disponibilidad del nuevo tipo
        val docIdNuevo = when (tipoNuevo) {
            "Furgon" -> "0ctYNlFXwtVw9ylURFXi"
            "Vehiculo Particular" -> "UF0tfabGHGitcj7En6Wy"
            "Bicicleta" -> "IuDC5XlTyhxhqU4It8SD"
            "Patineta Electrica" -> "IuDC5XlTyhxhqU4It8SD"
            "Motocicleta" -> "ntHgnXs4Qbz074siOrvz"
            else -> return
        }

        val campoNuevo = if (tipoNuevo == "Patineta Electrica") "Bicicleta" else tipoNuevo

        // Ejecutar ambas operaciones
        val batch = database.batch()

        val refAnterior = database.collection("Disponibilidad").document(docIdAnterior)
        batch.update(refAnterior, campoAnterior, FieldValue.increment(1))

        val refNuevo = database.collection("Disponibilidad").document(docIdNuevo)
        batch.update(refNuevo, campoNuevo, FieldValue.increment(-1))

        batch.commit()
            .addOnSuccessListener {
                Log.d("HomeActivity", "Disponibilidades actualizadas correctamente")
            }
            .addOnFailureListener { e ->
                Log.e("HomeActivity", "Error actualizando disponibilidades", e)
                Toast.makeText(this, "Error actualizando disponibilidades", Toast.LENGTH_SHORT)
                    .show()
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

    private fun escucharDisponibilidad(
        documentId: String,
        campo: String,
        textoView: TextView,
        umbrales: List<Pair<IntRange, String>>
    ) {
        database.collection("Disponibilidad")
            .document(documentId)
            .addSnapshotListener { document, e ->
                if (e != null) {
                    Log.d("Firestore", "Error al escuchar: $documentId", e)
                    return@addSnapshotListener
                }

                val espacios = document?.getLong(campo)?.toInt() ?: 0
                Log.d("Firestore", "Actualizado $campo -> $espacios")

                // Actualización para mostrar correctamente Bicicleta/Patineta
                val displayText = when (campo.lowercase()) {
                    "bicicleta" -> {
                        val emoji = "🚲/🛴"
                        "$emoji Quedan pocos $espacios para Bicicleta/Patineta"
                    }

                    "furgon" -> "🚐 Quedan pocos $espacios para Furgon"
                    "motocicleta" -> "🏍️ Quedan pocos $espacios para Motocicleta"
                    "vehiculo particular" -> "🚗 Quedan pocos $espacios para Vehículo Particular"
                    else -> "Quedan $espacios espacios"
                }

                textoView.text = displayText

                for ((rango, mensaje) in umbrales) {
                    if (espacios in rango) {
                        val notificationTitle = when (campo.lowercase()) {
                            "bicicleta" -> "ZeusParking - Bicicleta/Patineta"
                            else -> "ZeusParking - $campo"
                        }
                        mostrarNotificacion(
                            this,
                            notificationTitle,
                            mensaje.replace("{espacios}", espacios.toString())
                        )
                        break
                    }
                }
            }
    }

    private fun crearCanalNotificacion(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nombre = "Canal ZeusParking"
            val descripcion = "Descripción del canal ZeusParking"
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

    override fun getCurrentNavigationItem(): Int = R.id.home
}