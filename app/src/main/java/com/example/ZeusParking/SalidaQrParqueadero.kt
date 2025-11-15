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

class SalidaQrParqueadero : BaseNavigationActivity() {
    private lateinit var camaraEjecutarSalida: ExecutorService
    private var database = FirebaseFirestore.getInstance()
    private var escaneoRealizado: Boolean = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salida_qr_parqueadero)

        val checker = object : Runnable {
            override fun run() {
                if (hayConexionInternet(this@SalidaQrParqueadero)) {
                    Log.d("conexion", "¡Hay conexión a Internet!")
                } else {
                    finish()
                    Log.d("conexion", "No hay conexión")
                }
                handler.postDelayed(this, 5000)
            }
        }

        handler.post(checker)

        //Responsividad
        Responsividad.inicializar(this)

        //Navegacion
        setupNavigation()

        camaraEjecutarSalida = Executors.newSingleThreadExecutor()
        empezarCamara()
    }

    fun hayConexionInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val redActiva = connectivityManager.activeNetwork ?: return false
        val capacidades = connectivityManager.getNetworkCapabilities(redActiva) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    //Navegacion del Sistema
    override fun getCurrentNavigationItem(): Int = R.id.salida

    private fun empezarCamara() {
        val camaraProvedorFuturo = ProcessCameraProvider.getInstance(this)
        camaraProvedorFuturo.addListener({
            val cameraProvedor: ProcessCameraProvider = camaraProvedorFuturo.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.ScannerSalida).surfaceProvider)
                }
            val imaenAnalizada = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            imaenAnalizada.setAnalyzer(camaraEjecutarSalida, { imageProxy ->
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
                        // Verificar si es un QR antiguo (correo)
                        if (vehiculoId.contains("@") && vehiculoId.contains(".")) {
                            Toast.makeText(this, "⚠️ QR antiguo detectado. Por favor pide al usuario que genere un nuevo QR", Toast.LENGTH_LONG).show()
                            escaneoRealizado = false
                            return@let
                        }

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

                    if (correo.isEmpty() || tipo.isEmpty() || numero.isEmpty()) {
                        Toast.makeText(this, "❌ Datos del vehículo incompletos", Toast.LENGTH_SHORT).show()
                        escaneoRealizado = false
                        return@addOnSuccessListener
                    }

                    // Verificar si tiene entrada activa
                    verificarEntradaActiva(vehiculoId, correo, tipo, numero)
                } else {
                    Toast.makeText(this, "❌ Vehículo no encontrado", Toast.LENGTH_LONG).show()
                    escaneoRealizado = false
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error al buscar vehículo", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error al buscar vehículo: ", e)
                escaneoRealizado = false
            }
    }

    private fun verificarEntradaActiva(vehiculoId: String, correo: String, tipo: String, numero: String) {
        database.collection("Entrada")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(this, "❌ No hay entrada registrada para este vehículo", Toast.LENGTH_LONG).show()
                    escaneoRealizado = false
                } else {
                    // Verificar que la entrada activa corresponda al vehículo escaneado
                    val entradaActiva = documents.documents[0]
                    val vehiculoEntrada = entradaActiva.getString("placa") ?: ""

                    if (vehiculoEntrada == numero) {
                        verificarSalidaPrevia(vehiculoId, correo, tipo, numero)
                    } else {
                        Toast.makeText(this, "❌ La entrada activa no corresponde a este vehículo", Toast.LENGTH_LONG).show()
                        escaneoRealizado = false
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error al verificar entrada", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error al verificar entrada: ", e)
                escaneoRealizado = false
            }
    }

    private fun verificarSalidaPrevia(vehiculoId: String, correo: String, tipo: String, numero: String) {
        database.collection("Salida")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { salidaDocuments ->
                if (!salidaDocuments.isEmpty) {
                    Toast.makeText(this, "⚠️ Ya existe una salida registrada para este usuario", Toast.LENGTH_LONG).show()
                    escaneoRealizado = false
                } else {
                    verificarPortatil(vehiculoId, correo, tipo, numero)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar salida: ", e)
                verificarPortatil(vehiculoId, correo, tipo, numero)
            }
    }

    private fun verificarPortatil(vehiculoId: String, correo: String, tipo: String, numero: String) {
        database.collection("Entrada_Portatiles")
            .whereEqualTo("correoUsuario", correo)
            .get()
            .addOnSuccessListener { portatilDocuments ->
                try {
                    if (!portatilDocuments.isEmpty) {
                        val llevaPC = portatilDocuments.documents[0].getBoolean("llevaPC") ?: false
                        val serialPC = portatilDocuments.documents[0].getString("serialPC")
                        if (llevaPC && !serialPC.isNullOrEmpty()) {
                            val intent = Intent(this, InfoPCActivity::class.java).apply {
                                putExtra("correo", correo)
                                putExtra("vehiculo", tipo)
                                putExtra("idVehi", numero)
                                putExtra("serialPC", serialPC)
                                putExtra("modoDevolucion", true)
                                putExtra("vehiculoId", vehiculoId)
                            }
                            startActivity(intent)
                        } else {
                            procesarSalida(vehiculoId, correo, tipo, numero)
                        }
                    } else {
                        procesarSalida(vehiculoId, correo, tipo, numero)
                    }
                } catch (e: Exception) {
                    Log.e("Firestore", "Error procesando portátil: ", e)
                    procesarSalida(vehiculoId, correo, tipo, numero)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar portátil: ", e)
                procesarSalida(vehiculoId, correo, tipo, numero)
            }
    }

    private fun procesarSalida(vehiculoId: String, correo: String, tipo: String, numero: String) {
        database.collection("Entrada")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { entradaDocuments ->
                if (entradaDocuments.isEmpty) {
                    Toast.makeText(this, "❌ No se encontró entrada para procesar salida", Toast.LENGTH_SHORT).show()
                    escaneoRealizado = false
                    return@addOnSuccessListener
                }

                // Eliminar todas las entradas del usuario (debería ser solo una)
                for (document in entradaDocuments) {
                    database.collection("Entrada").document(document.id).delete()
                }

                val intent = Intent(this, DatosUsuarioSalida::class.java).apply {
                    putExtra("vehiculoId", vehiculoId)
                    putExtra("correo", correo)
                    putExtra("tipo", tipo)
                    putExtra("id", numero)
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "❌ Error al procesar salida", Toast.LENGTH_SHORT).show()
                Log.e("Salida", "Error al obtener entradas", e)
                escaneoRealizado = false
            }
    }

    override fun onResume() {
        super.onResume()
        escaneoRealizado = false
    }

    override fun onDestroy() {
        super.onDestroy()
        camaraEjecutarSalida.shutdown()
    }
}