package com.example.parquiatenov10

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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

class EntradaQrParqueadero : BaseNavigationActivity() {
    private lateinit var camaraEjecutarEntrada: ExecutorService
    private var database = FirebaseFirestore.getInstance()
    private lateinit var tiposSpinnerEntrada: Spinner
    private lateinit var marcoNumEntrada: EditText
    private var escaneoRealizado: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada_qr_parqueadero)

        //Responsividad
        Responsividad.inicializar(this)

        //Navegacion
        setupNavigation()

        tiposSpinnerEntrada = findViewById(R.id.Tipos_SpinnerVigiEntrada)
        marcoNumEntrada = findViewById(R.id.Marco_NUMVigiEntrada)
        camaraEjecutarEntrada = Executors.newSingleThreadExecutor()
        empezarCamara()
        val adapter = ArrayAdapter.createFromResource(this, R.array.items, R.layout.estilo_spinner)
        adapter.setDropDownViewResource(R.layout.estilo_spinner)
        tiposSpinnerEntrada.adapter = adapter

        tiposSpinnerEntrada.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    1, 2 -> {
                        marcoNumEntrada.hint = "Número de Marco"
                        marcoNumEntrada.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNumEntrada.filters = arrayOf(InputFilter.LengthFilter(20))
                    }

                    3, 4, 5 -> {
                        marcoNumEntrada.hint = when (position) {
                            3 -> "Placa (Ej. ABC-123)"
                            4 -> "Placa (Ej. ABC-123)"
                            5 -> "Número de Furgón"
                            else -> "Número de Marco"
                        }
                        marcoNumEntrada.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        marcoNumEntrada.filters = arrayOf(InputFilter.LengthFilter(7))
                    }

                    else -> {
                        marcoNumEntrada.hint = ""
                        marcoNumEntrada.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNumEntrada.filters = arrayOf(InputFilter.LengthFilter(20))
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    //Navegacion del Sistema
    override fun getCurrentNavigationItem(): Int = R.id.entrada

    private fun empezarCamara() {
        val camaraProvedorFuturo = ProcessCameraProvider.getInstance(this)

        camaraProvedorFuturo.addListener({
            val camaraProvedor: ProcessCameraProvider = camaraProvedorFuturo.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.ScannerEntrada).surfaceProvider)
                }

            val imagenAnalizada = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            imagenAnalizada.setAnalyzer(camaraEjecutarEntrada, { imageProxy ->
                processImage(imageProxy)
            })

            val camaraSelectora = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                camaraProvedor.unbindAll()
                camaraProvedor.bindToLifecycle(this, camaraSelectora, preview, imagenAnalizada)
            } catch (exc: Exception) {
                Log.e("QRScanner", "Error al iniciar la cámara", exc)
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
                    val tiposSpinner = tiposSpinnerEntrada.selectedItem.toString()
                    val id = marcoNumEntrada.text.toString()
                    val qrText = barcode.displayValue
                    if (tiposSpinner == "Tipo de Vehiculo" || id.isEmpty()){
                        Toast.makeText(this, "Porfavor llene todos los campos", Toast.LENGTH_SHORT).show()
                    }else{
                        qrText?.let {
                            escaneoRealizado = true
                            verificarEntrada(qrText, tiposSpinner, id)
                            Log.d("QRScanner", "Código QR detectado: $qrText")
                        }
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

    private fun verificarEntrada(correo: String, vehiculo: String, idVehiculo: String) {
        database.collection("Entrada")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // El usuario no ha ingresado, proceder a verificar salida
                    verificarSalida(correo, vehiculo, idVehiculo)
                } else {
                    Toast.makeText(this, "El usuario ya entro", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar entrada: ", e)
            }
    }

    private fun verificarSalida(correo: String, vehiculo: String, idVehiculo: String) {
        database.collection("Salida")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Si existe salida, eliminarla
                    for (document in documents) {
                        database.collection("Salida").document(document.id).delete()
                    }
                    val intent = Intent(this, DatosUsuarioEntrada::class.java).apply {
                        putExtra("correo", correo)
                        putExtra("tipo", vehiculo)
                        putExtra("id", idVehiculo)
                    }
                    Log.d("IntentData", "Correo: $correo, Tipo: $vehiculo, ID: $idVehiculo")
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar salida: ", e)
            }
    }

    override fun onResume() {
        super.onResume()
        escaneoRealizado = false // Permitir nuevo escaneo al volver a esta actividad
    }

    override fun onDestroy() {
        super.onDestroy()
        camaraEjecutarEntrada.shutdown()
    }
}
