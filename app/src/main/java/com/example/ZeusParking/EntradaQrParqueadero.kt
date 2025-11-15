package com.example.parquiatenov10

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
import android.os.Handler
import android.os.Looper

class EntradaQrParqueadero : BaseNavigationActivity() {
    private lateinit var camaraEjecutarEntrada: ExecutorService
    private var database = FirebaseFirestore.getInstance()
    private var escaneoRealizado: Boolean = false
    private val handler = Handler(Looper.getMainLooper())

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
                    val serialPC = documents.documents[0].getString("serial") ?: ""
                    val intent = Intent(this, InfoPCActivity::class.java).apply {
                        putExtra("correo", correo)
                        putExtra("serialPC", serialPC)
                    }
                    startActivity(intent)
                }
                DatosEntrada(vehiculoId, correo, tipo, numero)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar PC: ", e)
                DatosEntrada(vehiculoId, correo, tipo, numero)
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