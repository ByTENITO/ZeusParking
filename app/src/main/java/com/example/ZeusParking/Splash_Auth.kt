package com.example.parquiatenov10

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class Splash_Auth : AppCompatActivity() {

    private lateinit var imagenSplash: ImageView
    private lateinit var constraintBienvenida: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_auth)

        imagenSplash = findViewById(R.id.image_splash)
        constraintBienvenida = findViewById(R.id.main)

        val alto = resources.displayMetrics.heightPixels

        // Reducción de la duración de la animación a 2 segundos
        val duracionAnimacion = 2000L // 2 segundos

        // Se adapta el logo dependiendo del tamaño de la pantalla
        if (alto >= 3001) {
            val constraintSet = ConstraintSet()
            responsividad(imagenSplash, 1160, 1160) // Tamaño grande

            // Centramos la imagen
            constraintSet.clone(constraintBienvenida)
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                0 // Centrado en el eje Y
            )
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                0 // Centrado en el eje X
            )
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0 // Centrado en el eje X
            )
            constraintSet.applyTo(constraintBienvenida)

            // Animación para hacer crecer la imagen sin moverla
            imagenSplash.animate()
                .scaleX(1.2f)  // Aumenta un 20% el tamaño en el eje X
                .scaleY(1.2f)  // Aumenta un 20% el tamaño en el eje Y
                .setDuration(duracionAnimacion)
                .start()
        }

        if (alto in 1301..2500) {
            val constraintSet = ConstraintSet()
            responsividad(imagenSplash, 761, 761)

            // Centramos la imagen
            constraintSet.clone(constraintBienvenida)
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                0 // Centrado en el eje Y
            )
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                0 // Centrado en el eje X
            )
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0 // Centrado en el eje X
            )
            constraintSet.applyTo(constraintBienvenida)

            imagenSplash.animate()
                .scaleX(1.2f) // Aumenta un 20% el tamaño en el eje X
                .scaleY(1.2f) // Aumenta un 20% el tamaño en el eje Y
                .setDuration(duracionAnimacion)
                .start()
        }

        if (alto in 1081..1300) {
            val constraintSet = ConstraintSet()
            responsividad(imagenSplash, 500, 500)

            // Centramos la imagen
            constraintSet.clone(constraintBienvenida)
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                0 // Centrado en el eje Y
            )
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                0 // Centrado en el eje X
            )
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0 // Centrado en el eje X
            )
            constraintSet.applyTo(constraintBienvenida)

            imagenSplash.animate()
                .scaleX(1.1f)  // Aumenta un 10% el tamaño en el eje X
                .scaleY(1.1f)  // Aumenta un 10% el tamaño en el eje Y
                .setDuration(duracionAnimacion)
                .start()
        }

        if (alto <= 1080) {
            val constraintSet = ConstraintSet()
            responsividad(imagenSplash, 300, 300)

            // Centramos la imagen
            constraintSet.clone(constraintBienvenida)
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                0 // Centrado en el eje Y
            )
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START,
                0 // Centrado en el eje X
            )
            constraintSet.connect(
                imagenSplash.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END,
                0 // Centrado en el eje X
            )
            constraintSet.applyTo(constraintBienvenida)

            imagenSplash.animate()
                .scaleX(1.1f)  // Aumenta un 10% el tamaño en el eje X
                .scaleY(1.1f)  // Aumenta un 10% el tamaño en el eje Y
                .setDuration(duracionAnimacion)
                .start()
        }

        // Transición a la siguiente actividad después de 2 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0)
            finish()
        }, 2000) // 2 segundos
    }

    private fun responsividad(view: View, width: Int, heigth: Int) {
        val anchoComponente = ValueAnimator.ofInt(width)
        val altoComponente = ValueAnimator.ofInt(heigth)
        anchoComponente.addUpdateListener { animation ->
            val params = view.layoutParams
            params.width = animation.animatedValue as Int
            view.layoutParams = params
        }

        altoComponente.addUpdateListener { animation ->
            val params = view.layoutParams
            params.height = animation.animatedValue as Int
            view.layoutParams = params
        }
        altoComponente.start()
        anchoComponente.start()
    }
}
