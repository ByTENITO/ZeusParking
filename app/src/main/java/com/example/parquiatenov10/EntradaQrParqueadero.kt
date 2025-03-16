package com.example.parquiatenov10

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EntradaQrParqueadero : AppCompatActivity() {
    private lateinit var camaraEjecutar: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entrada_qr_parqueadero)

        camaraEjecutar = Executors.newSingleThreadExecutor()
        empezarCamara()
    }
    private fun empezarCamara() {
        val camaraProvedorFuturo = ProcessCameraProvider.getInstance(this)

        camaraProvedorFuturo.addListener({
            val camaraProvedor: ProcessCameraProvider = camaraProvedorFuturo.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(findViewById<PreviewView>(R.id.Scanner).surfaceProvider)
            }

            val imagenAnalizada = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            imagenAnalizada.setAnalyzer(camaraEjecutar, { imageProxy ->
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
        val mediaImage = imageProxy.image ?: return

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val qrText = barcode.displayValue
                    qrText?.let {
                        Toast.makeText(this, "Código QR: $it", Toast.LENGTH_SHORT).show()
                        Log.d("QRScanner", "Código escaneado: $it")
                        imageProxy.close()
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

    override fun onDestroy() {
        super.onDestroy()
        camaraEjecutar.shutdown()
    }

}
