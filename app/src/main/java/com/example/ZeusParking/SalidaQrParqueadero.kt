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
import kotlin.math.log

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
                            verificarUsuario(qrText, tiposSpinner, id)
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

    private fun verificarUsuario(correo: String, vehiculo: String, idVehiculo: String) {
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", correo)
            .whereEqualTo("tipo", vehiculo)
            .whereEqualTo("numero", idVehiculo)
            .addSnapshotListener { documents,e ->
                if (documents != null) {
                    if (documents.isEmpty) {
                        escaneoRealizado = false
                        Log.d("Firestore", "No se encontraron documentos con correo: $correo")
                        Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_SHORT).show()
                    }else {
                        verificarVehiculo(correo,vehiculo,idVehiculo)
                    }
                }
            }
    }

    private fun verificarVehiculo(correo: String, vehiculo: String, idVehiculo: String){
        database.collection("Bici_Usuarios")
            .whereEqualTo("correo", correo)
            .whereEqualTo("tipo", vehiculo)
            .whereEqualTo("numero", idVehiculo)
            .addSnapshotListener { documents , e ->
                if (documents != null){
                    if (documents.isEmpty) {
                        escaneoRealizado = false
                        Log.d("Firestore", "No se encontraron documentos con correo: $correo")
                        Toast.makeText(this, "No se encontraron datos del vehiculo", Toast.LENGTH_SHORT).show()
                    }else {
                        verificarSalida(correo,vehiculo,idVehiculo)
                    }
                }
            }
    }

    private fun verificarSalida(correo: String, vehiculo: String, idVehiculo: String) {
        // Verificar si hay una entrada activa primero
        database.collection("Salida")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { entradaDocuments ->
                //verifica que no exista una salida
                if (entradaDocuments.isEmpty) {
                    verificarEntrada(correo, vehiculo, idVehiculo)
                } else {
                    escaneoRealizado = false
                    Toast.makeText(this, "El usuario ya salio", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error al verificar salida", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error al verificar salida: ", e)
            }
    }

    private fun verificarEntrada(correo: String, vehiculo: String, idVehiculo: String) {
        // Verificar primero si hay un portátil registrado en la entrada
        database.collection("Entrada_Portatiles")
            .whereEqualTo("correoUsuario", correo)
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
                                putExtra("vehiculo", vehiculo)
                                putExtra("idVehi",idVehiculo)
                                putExtra("serialPC", serialPC)
                                putExtra("modoDevolucion", true)
                            }
                            startActivity(intent)
                        }
                    }else{
                        procesarSalida(correo, vehiculo, idVehiculo)
                    }
                } catch (e: Exception) {
                    Log.e("Firestore", "Error procesando documentos de portátil: ", e)
                    procesarSalida(correo, vehiculo, idVehiculo)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar portátil: ", e)
                // Continuar con el proceso sin verificación de portátil
                procesarSalida(correo, vehiculo, idVehiculo)
            }
    }

    private fun procesarSalida(
        correo: String,
        vehiculo: String,
        idVehiculo: String,
    ) {
        // Paso 1: Eliminar registros de Entrada
        database.collection("Entrada")
            .whereEqualTo("correo", correo)
            .get()
            .addOnSuccessListener { entradaDocuments ->
                for (document in entradaDocuments) {
                    if (document.exists()) {
                        database.collection("Entrada").document(document.id).delete()
                        Log.d("Salida", "Marcando entrada para eliminar: ${document.id}")
                    }
                }
                commitBatchOperations( correo, vehiculo, idVehiculo)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al procesar salida", Toast.LENGTH_SHORT).show()
                Log.e("Salida", "Error al obtener entradas", e)
            }
    }

    private fun commitBatchOperations(
        correo: String,
        vehiculo: String,
        idVehiculo: String
    ) {
        Log.d("Salida", "Todos los registros eliminados exitosamente")
        val intent = Intent(this, DatosUsuarioSalida::class.java).apply {
            putExtra("correo", correo)
            putExtra("tipo", vehiculo)
            putExtra("id", idVehiculo)
        }
        startActivity(intent)
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