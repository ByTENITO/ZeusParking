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
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.ZeusParking.BaseNavigationActivity
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class SalidaQrParqueadero : BaseNavigationActivity() {
    private lateinit var camaraEjecutarSalida: ExecutorService
    private lateinit var tiposSpinnerSalida: Spinner
    private lateinit var Salir: Button
    private lateinit var marcoNumSalida: EditText
    private var escaneoRealizado: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salida_qr_parqueadero)

        //Navegacion
        setupNavigation()

        tiposSpinnerSalida = findViewById(R.id.Tipos_SpinnerVigiSalida)
        marcoNumSalida = findViewById(R.id.Marco_NUMVigiSalida)
        Salir = findViewById(R.id.exitScannerSalida)
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
                    1 -> {
                        marcoNumSalida.hint = "Número de Marco"
                        marcoNumSalida.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNumSalida.filters = arrayOf(InputFilter.LengthFilter(20))
                    }

                    2, 3, 4 -> {
                        marcoNumSalida.hint = when (position) {
                            2 -> "Placa (Ej. ABC-123)"
                            3 -> "Placa (Ej. ABC-123)"
                            4 -> "Numero de Furgon"
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
        Salir.setOnClickListener {
            finish()
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
                    qrText?.let {
                        Log.d("QRScanner", "Código QR detectado: $qrText")
                        escaneoRealizado = true
                        val intent = Intent(this, DatosUsuarioSalida::class.java).apply {
                            putExtra("correo", qrText)
                            putExtra("tipo", tiposSpinner)
                            putExtra("id", id)
                        }
                        Log.d("IntentData", "Correo:$qrText, Tipo $tiposSpinner, ID: $id")
                        startActivity(intent)
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

    override fun onResume() {
        super.onResume()
        escaneoRealizado = false // Permitir nuevo escaneo al volver a esta actividad
    }

    override fun onDestroy() {
        super.onDestroy()
        camaraEjecutarSalida.shutdown()
    }
}