package com.example.parquiatenov10

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


class Splash_Auth : AppCompatActivity() {

    private lateinit var imagenSplash: ImageView
    private lateinit var bienvenidaText: TextView
    private lateinit var constraintBienvenida: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_auth)

        imagenSplash = findViewById(R.id.image_splash)
        bienvenidaText = findViewById(R.id.Bienvenida_APP)
        constraintBienvenida = findViewById(R.id.main)

        val alto = resources.displayMetrics.heightPixels

        if (alto >= 3001) {
            val constraintSet = ConstraintSet()

            responsividad(imagenSplash, 1160, 1160)
            responsividad(bienvenidaText, 1000, 1000)
            bienvenidaText.textSize = 20f

            constraintSet.clone(constraintBienvenida)
            constraintSet.connect(
                bienvenidaText.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                300
            ) // 300px desde arriba
            constraintSet.applyTo(constraintBienvenida)

            imagenSplash.animate()
                .translationX(0f)
                .translationY(-1050f)
                .setDuration(3000)
                .start()
        }

        if (alto in 1301..2500) {
            if (alto >= 2400) {
                val constraintSet = ConstraintSet()

                responsividad(imagenSplash, 761, 761)
                responsividad(bienvenidaText, 600, 600)
                bienvenidaText.textSize = 20f

                constraintSet.clone(constraintBienvenida)
                constraintSet.connect(
                    bienvenidaText.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    400
                ) // 300px desde arriba
                constraintSet.applyTo(constraintBienvenida)
                imagenSplash.animate()
                    .translationX(0f)
                    .translationY(-710f)
                    .setDuration(3000)
                    .start()
            }
            if (alto in 2209..2399) {
                val constraintSet = ConstraintSet()

                responsividad(imagenSplash, 870, 870)
                responsividad(bienvenidaText, 600, 600)
                bienvenidaText.textSize = 20f

                constraintSet.clone(constraintBienvenida)
                constraintSet.connect(
                    bienvenidaText.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    400
                ) // 300px desde arriba
                constraintSet.applyTo(constraintBienvenida)
                imagenSplash.animate()
                    .translationX(0f)
                    .translationY(-735f)
                    .setDuration(3000)
                    .start()
            }
            if (alto in 1841..2208) {
                val constraintSet = ConstraintSet()

                responsividad(imagenSplash, 870, 870)
                responsividad(bienvenidaText, 600, 600)
                bienvenidaText.textSize = 20f

                constraintSet.clone(constraintBienvenida)
                constraintSet.connect(
                    bienvenidaText.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    400
                ) // 300px desde arriba
                constraintSet.applyTo(constraintBienvenida)
                imagenSplash.animate()
                    .translationX(0f)
                    .translationY(-655f)
                    .setDuration(3000)
                    .start()
            }
            if (alto <= 1840) {
                val constraintSet = ConstraintSet()

                responsividad(imagenSplash, 761, 761)
                responsividad(bienvenidaText, 600, 600)
                bienvenidaText.textSize = 20f

                constraintSet.clone(constraintBienvenida)
                constraintSet.connect(
                    bienvenidaText.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP,
                    400
                ) // 300px desde arriba
                constraintSet.applyTo(constraintBienvenida)
                imagenSplash.animate()
                    .translationX(0f)
                    .translationY(-543f)
                    .setDuration(3000)
                    .start()
            }
        }

        if (alto in 1081..1300) {
            val constraintSet = ConstraintSet()

            responsividad(imagenSplash, 500, 500)
            responsividad(bienvenidaText, 440, 440)
            bienvenidaText.textSize = 15f

            constraintSet.clone(constraintBienvenida)
            constraintSet.connect(
                bienvenidaText.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                300
            ) // 300px desde arriba
            constraintSet.applyTo(constraintBienvenida)

            imagenSplash.animate()
                .translationX(0f)
                .translationY(-380f)
                .setDuration(3000)
                .start()
        }
        if (alto <= 1080) {
            val constraintSet = ConstraintSet()

            responsividad(imagenSplash, 300, 300)
            responsividad(bienvenidaText, 300, 300)
            bienvenidaText.textSize = 10f

            constraintSet.clone(constraintBienvenida)
            constraintSet.connect(
                bienvenidaText.id,
                ConstraintSet.TOP,
                ConstraintSet.PARENT_ID,
                ConstraintSet.TOP,
                200
            ) // 300px desde arriba
            constraintSet.applyTo(constraintBienvenida)

            imagenSplash.animate()
                .translationX(0f)
                .translationY(-400f)
                .setDuration(3000)
                .start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)

            overridePendingTransition(0, 0)
            finish()
        }, 5000)

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