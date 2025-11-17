package com.example.parquiatenov10

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.ZeusParking.BaseNavigationActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class EntradaQrParqueadero : BaseNavigationActivity() {
    private lateinit var camaraEjecutarEntrada: ExecutorService
    private var database = FirebaseFirestore.getInstance()
    private var escaneoRealizado: Boolean = false
    private val handler = Handler(Looper.getMainLooper())
    private var PcsAgregado = false
    private lateinit var layoutEmptyState: com.google.android.material.card.MaterialCardView
    private lateinit var tvEmptyMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada_qr_parqueadero)

        val checker = object : Runnable {
            override fun run() {
                if (hayConexionInternet(this@EntradaQrParqueadero)) {
                    Log.d("conexion", "¡Hay conexión a Internet!")
                } else {
                    finish()
                    Log.d("conexion", "No hay conexión")
                }

                handler.postDelayed(this, 5000) // repetir cada 5 segundos
            }
        }

        handler.post(checker)

        //Responsividad
        Responsividad.inicializar(this)

        //Navegacion
        setupNavigation()

        camaraEjecutarEntrada = Executors.newSingleThreadExecutor()
        empezarCamara()
    }

    fun hayConexionInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val redActiva = connectivityManager.activeNetwork ?: return false
        val capacidades = connectivityManager.getNetworkCapabilities(redActiva) ?: return false

        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    //Navegacion del Sistema
    override fun getCurrentNavigationItem(): Int = R.id.entrada

    private fun empezarCamara() {
        val camaraProvedorFuturo = ProcessCameraProvider.getInstance(this)
        camaraProvedorFuturo.addListener({
            val cameraProvedor: ProcessCameraProvider = camaraProvedorFuturo.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.ScannerEntrada).surfaceProvider)
                }
            val imaenAnalizada = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            imaenAnalizada.setAnalyzer(camaraEjecutarEntrada, { imageProxy ->
                processImage(imageProxy)
            })
            val camaraSelectora = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            try {
                cameraProvedor.unbindAll()
                cameraProvedor.bindToLifecycle(this, camaraSelectora, preview, imaenAnalizada)
            } catch (exc: Exception) {
                Log.e("QRScanner", "Error al iniciar la camara", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: ImageProxy) {
        if (escaneoRealizado) {
            imageProxy.close()
            return
        }
        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val vehiculoId = barcode.displayValue
                    vehiculoId?.let {
                        escaneoRealizado = true
                        verificarVehiculo(vehiculoId)
                        Log.d("QRScanner", "ID del vehículo detectado: $vehiculoId")
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("QRScanner", "Error al leer QR", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun verificarVehiculo(vehiculoId: String) {
        database.collection("Bici_Usuarios")
            .document(vehiculoId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val correo = document.getString("correo") ?: ""
                    val tipo = document.getString("tipo") ?: ""
                    val numero = document.getString("numero") ?: ""

                    if (correo.isNotEmpty() && tipo.isNotEmpty() && numero.isNotEmpty()) {
                        verificarEntrada(vehiculoId, correo, tipo, numero)
                    } else {
                        Toast.makeText(this, "Datos del vehículo incompletos", Toast.LENGTH_SHORT).show()
                        escaneoRealizado = false
                    }
                } else {
                    Toast.makeText(this, "Vehículo no encontrado", Toast.LENGTH_SHORT).show()
                    escaneoRealizado = false
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al buscar vehículo", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error al buscar vehículo: ", e)
                escaneoRealizado = false
            }
    }

    private fun verificarEntrada(vehiculoId: String, correo: String, tipo: String, numero: String) {
        database.collection("Entrada")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Verificar si el usuario tiene PC registrado
                    verificarSalida(vehiculoId, correo, tipo, numero)
                } else {
                    Toast.makeText(this, "El usuario ya entro", Toast.LENGTH_SHORT).show()
                    escaneoRealizado = false
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar entrada: ", e)
                escaneoRealizado = false
            }
    }

    private fun verificarSalida(vehiculoId: String, correo: String, tipo: String, numero: String) {
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
                verificarPCUsuario(vehiculoId, correo, tipo, numero)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar salida: ", e)
                verificarPCUsuario(vehiculoId, correo, tipo, numero)
            }
    }

    private fun verificarPCUsuario(vehiculoId: String, correo: String, tipo: String, numero: String) {
        database.collection("Portatiles")
            .whereEqualTo("correoUsuario", correo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Tiene PC registrado, mostrar confirmación
                    if (documents.size() > 1){
                        listaPortatiles(documents, correo)
                    }else{
                        val serialPC = documents.documents[0].getString("serial") ?: ""
                        enviarAInfoPcs(correo,serialPC)
                    }
                }
                DatosEntrada(vehiculoId, correo, tipo, numero)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar PC: ", e)
                DatosEntrada(vehiculoId, correo, tipo, numero)
            }
    }

    private fun enviarAInfoPcs(correo: String,serialPC: String){
        val intent = Intent(this, InfoPCActivity::class.java).apply {
            putExtra("correo", correo)
            putExtra("serialPC", serialPC)
        }
        startActivity(intent)
    }
    private fun listaPortatiles (documents: com.google.firebase.firestore.QuerySnapshot, correo: String){
        val Pcs = mutableListOf<Triple<String, String, String>>()

        for(document in documents){
            val marca = document.getString("marca") ?: ""
            val modelo = document.getString("modelo") ?:""
            val serial = document.getString("serial") ?:""
            Pcs.add(Triple(marca, modelo, serial))
        }

        val dialog = AlertDialog.Builder(this).create()
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20,20,20,20)
            background = createRoundedDrawable(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Blanco), 25f)
        }

        val titulo = TextView(this).apply {
            text = "Seleccione un PC"
            textSize = 20f
            setTextColor(ContextCompat.getColor(this@EntradaQrParqueadero,R.color.Principal))
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 10, 0, 20)
        }

        val mensaje = TextView(this).apply {
            text = "Selecciona el PC que va ingresar"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Negro))
            gravity = Gravity.CENTER
            setPadding(0,0,0,10)
        }

        val scrollView = ScrollView(this)
        val listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10,10,10,10)
        }

        Pcs.forEachIndexed { index, Pc ->
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(20,15,20,15)
                background = createRoundedDrawable(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Texto_pastel), 12f)
                setOnClickListener {
                    enviarAInfoPcs(correo, Pc.third)
                    dialog.dismiss()
                    showSweetToast("Pc seleccionado: ${Pc.first} - ${Pc.second} - ${Pc.third}", true)
                }
                setOnTouchListener { v, event ->
                    when(event.action){
                        MotionEvent.ACTION_DOWN -> v.alpha = 0.7f
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.alpha = 1.0f
                    }
                    false
                }
            }

            val infoLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val marcaView = TextView(this).apply{
                text = Pc.first
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@EntradaQrParqueadero,R.color.Negro))
                setTypeface(typeface, Typeface.BOLD)
            }

            val serialView = TextView(this).apply {
                text = Pc.third
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Negro))
            }

            infoLayout.addView(marcaView)
            infoLayout.addView(serialView)

            itemLayout.addView(infoLayout)

            listLayout.addView(itemLayout)

            if(index < Pcs.size - 1){
                val separador = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,1
                    ).apply {
                        setMargins(10,5,10,5)
                    }
                    setBackgroundColor(ContextCompat.getColor(this@EntradaQrParqueadero,R.color.Tercero))
                }
                listLayout.addView(separador)
            }
        }
        scrollView.addView(listLayout)

        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0,10,0,0)
        }

        val cancelButton = Button(this).apply {
            text = "Cancelar"
            setTextColor(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Blanco))
            setBackgroundColor(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Secundario))
            setPadding(30,15,30,15)
            setOnClickListener {
                dialog.dismiss()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                setMargins(5,0,5,0)
            }
            background = createRoundedDrawable(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Secundario),20f)
        }
        val defaultButton = Button(this).apply {
            text = "Usar el primer Pc"
            setTextColor(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Blanco))
            setBackgroundColor(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Verde_bien))
            setPadding(30,15,30,15)
            setOnClickListener {
                if (Pcs.isNotEmpty()){
                    val primerPc = Pcs[0]
                    enviarAInfoPcs(correo,primerPc.third)
                    dialog.dismiss()
                    showSweetToast("Selecciono el primer Pc: ${primerPc.first} - ${primerPc.second} - ${primerPc.third}", true)
                }
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(5,0,5,0)
            }
            background = createRoundedDrawable(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Verde_bien) ,20f)
        }
        buttonLayout.addView(cancelButton)
        buttonLayout.addView(defaultButton)

        mainLayout.addView(titulo)
        mainLayout.addView(mensaje)
        mainLayout.addView(scrollView)
        mainLayout.addView(buttonLayout)

        dialog.setView(mainLayout)
        dialog.show()
    }

    private fun showSweetToast(message: String, isSuccess: Boolean) {
        runOnUiThread {
            val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
            val toastView = TextView(this).apply {
                text = message
                setTextColor(ContextCompat.getColor(this@EntradaQrParqueadero, R.color.Blanco))
                gravity = Gravity.CENTER
                setPadding(40, 20, 40, 20)
                val backgroundColor = if (isSuccess) R.color.Verde_bien else R.color.Secundario
                background = createRoundedDrawable(ContextCompat.getColor(this@EntradaQrParqueadero, backgroundColor), 25f)
            }
            toast.view = toastView
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    private fun createRoundedDrawable(color: Int, cornerRadius: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            this.cornerRadius = cornerRadius
            setColor(color)
        }
    }

    private fun DatosEntrada(vehiculoId: String, correo: String, tipo: String, numero: String) {
        val entradaIntent = Intent(this, DatosUsuarioEntrada::class.java).apply {
            putExtra("vehiculoId", vehiculoId)
            putExtra("correo", correo)
            putExtra("tipo", tipo)
            putExtra("id", numero)
        }
        startActivity(entradaIntent)
    }

    override fun onResume() {
        super.onResume()
        escaneoRealizado = false
    }

    override fun onDestroy() {
        super.onDestroy()
        camaraEjecutarEntrada.shutdown()
    }
}
