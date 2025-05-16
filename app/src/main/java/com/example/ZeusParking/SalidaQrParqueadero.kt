package com.example.parquiatenov10

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SalidaQrParqueadero : BaseNavigationActivity() {
    private lateinit var camaraEjecutarSalida: ExecutorService
    private var database = FirebaseFirestore.getInstance()
    private lateinit var tiposSpinnerSalida: Spinner
    private lateinit var marcoNumSalida: EditText
    private var escaneoRealizado: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salida_qr_parqueadero)

        //Responsividad
        Responsividad.inicializar(this)

        //Navegacion
        setupNavigation()

        tiposSpinnerSalida = findViewById(R.id.Tipos_SpinnerVigiSalida)
        marcoNumSalida = findViewById(R.id.Marco_NUMVigiSalida)
        camaraEjecutarSalida = Executors.newSingleThreadExecutor()
        empezarCamara()
        val adapter = ArrayAdapter.createFromResource(this, R.array.items, R.layout.estilo_spinner)
        adapter.setDropDownViewResource(R.layout.estilo_spinner)
        tiposSpinnerSalida.adapter = adapter

        tiposSpinnerSalida.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    1, 2 -> {
                        marcoNumSalida.hint = "Número de Marco"
                        marcoNumSalida.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNumSalida.filters = arrayOf(InputFilter.LengthFilter(20))
                    }

                    3, 4, 5 -> {
                        marcoNumSalida.hint = when (position) {
                            3 -> "Placa (Ej. ABC-123)"
                            4 -> "Placa (Ej. ABC-123)"
                            5 -> "Numero de Furgon"
                            else -> "Numero de Marco"
                        }
                        marcoNumSalida.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        marcoNumSalida.filters = arrayOf(InputFilter.LengthFilter(7))
                    }

                    else -> {
                        marcoNumSalida.hint = ""
                        marcoNumSalida.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        marcoNumSalida.filters = arrayOf(InputFilter.LengthFilter(20))
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
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
                    val tiposSpinner = tiposSpinnerSalida.selectedItem.toString()
                    val id = marcoNumSalida.text.toString()
                    val qrText = barcode.displayValue
                    if (tiposSpinner == "Tipo de Vehiculo" || id.isEmpty()){
                        Toast.makeText(this, "Porfavor llene todos los campos", Toast.LENGTH_SHORT).show()
                    }else {
                        qrText?.let {
                            escaneoRealizado = true
                            verificarSalida(qrText, tiposSpinner, id)
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

    private fun verificarSalida(correo: String, vehiculo: String, idVehiculo: String) {
        // Verificar si hay una entrada activa primero
        database.collection("Entrada")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { entradaDocuments ->
                if (entradaDocuments.isEmpty) {
                    Toast.makeText(this, "No se encontró registro de entrada activa", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Si hay entrada activa, verificar portátil
                verificarEntrada(correo, vehiculo, idVehiculo)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al verificar entrada", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error al verificar entrada: ", e)
            }
    }

    private fun verificarEntrada(correo: String, vehiculo: String, idVehiculo: String) {
        // Verificar primero si hay un portátil registrado en la entrada
        database.collection("Entrada_Portatiles")
            .whereEqualTo("correoUsuario", correo)
            .orderBy("fechaHora", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { portatilDocuments ->
                try {
                    if (!portatilDocuments.isEmpty) {
                        val llevaPC = portatilDocuments.documents[0].getBoolean("llevaPC") ?: false
                        val serialPC = portatilDocuments.documents[0].getString("serialPC")

                        if (llevaPC && !serialPC.isNullOrEmpty()) {
                            // Mostrar confirmación de devolución de PC
                            val intent = Intent(this, InfoPCActivity::class.java).apply {
                                putExtra("correo", correo)
                                putExtra("serialPC", serialPC)
                                putExtra("modoDevolucion", true)
                            }
                            startActivity(intent)
                        }
                    }
                    // Continuar con el proceso de salida normal
                    procesarSalida(correo, vehiculo, idVehiculo, portatilDocuments)
                } catch (e: Exception) {
                    Log.e("Firestore", "Error procesando documentos de portátil: ", e)
                    procesarSalida(correo, vehiculo, idVehiculo, null)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar portátil: ", e)
                // Continuar con el proceso sin verificación de portátil
                procesarSalida(correo, vehiculo, idVehiculo, null)
            }
    }

    private fun procesarSalida(correo: String, vehiculo: String, idVehiculo: String, portatilDocuments: QuerySnapshot?) {
        // Crear un batch para todas las operaciones de eliminación
        val batch = database.batch()

        // Paso 1: Eliminar registros de Entrada
        database.collection("Entrada")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { entradaDocuments ->
                for (document in entradaDocuments) {
                    batch.delete(document.reference)
                    Log.d("Salida", "Marcando entrada para eliminar: ${document.id}")
                }

                // Paso 2: Eliminar registros de portátiles si existen
                if (portatilDocuments != null && !portatilDocuments.isEmpty) {
                    for (document in portatilDocuments) {
                        batch.delete(document.reference)
                        Log.d("Salida", "Marcando portátil para eliminar: ${document.id}")
                    }
                } else {
                    // Si no se pasaron documentos, buscarlos directamente
                    database.collection("Entrada_Portatiles")
                        .whereEqualTo("correoUsuario", correo)
                        .get()
                        .addOnSuccessListener { portatiles ->
                            for (document in portatiles) {
                                batch.delete(document.reference)
                                Log.d("Salida", "Marcando portátil (búsqueda directa) para eliminar: ${document.id}")
                            }
                            commitBatchOperations(batch, correo, vehiculo, idVehiculo)
                        }
                        .addOnFailureListener { e ->
                            Log.e("Salida", "Error buscando portátiles", e)
                            commitBatchOperations(batch, correo, vehiculo, idVehiculo)
                        }
                    return@addOnSuccessListener
                }

                // Paso 3: Ejecutar todas las eliminaciones
                commitBatchOperations(batch, correo, vehiculo, idVehiculo)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al procesar salida", Toast.LENGTH_SHORT).show()
                Log.e("Salida", "Error al obtener entradas", e)
            }
    }

    private fun commitBatchOperations(batch: WriteBatch, correo: String, vehiculo: String, idVehiculo: String) {
        batch.commit()
            .addOnSuccessListener {
                Log.d("Salida", "Todos los registros eliminados exitosamente")
                val intent = Intent(this, DatosUsuarioSalida::class.java).apply {
                    putExtra("correo", correo)
                    putExtra("tipo", vehiculo)
                    putExtra("id", idVehiculo)
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar los cambios", Toast.LENGTH_SHORT).show()
                Log.e("Salida", "Error en batch commit", e)
            }
    }

    override fun onResume() {
        super.onResume()
        escaneoRealizado = false // Permitir nuevo escaneo al volver a esta actividad
    }

    override fun onDestroy() {
        super.onDestroy()
        camaraEjecutarSalida.shutdown()
    }
}