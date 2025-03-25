package com.example.parquiatenov10

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Disponibilidad : AppCompatActivity() {
    private lateinit var VolverButton:Button

    private lateinit var texto: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        var Cuantos = 11
        var Total = 20
        var Restante = Total - Cuantos
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startAnimationsWithDelay()
        setContentView(R.layout.activity_disponibilidad)
        for (i in 1..Total) {
            val radioButtonId = resources.getIdentifier("select$i", "id", packageName)
            val radioButton = findViewById<RadioButton>(radioButtonId)
            radioButton.isEnabled = false
        }
        for (i in 1..Cuantos) {
            val radioButtonId = resources.getIdentifier("select$i", "id", packageName)
            val radioButton = findViewById<RadioButton>(radioButtonId)
            radioButton.isChecked = true
        }
        val textView = findViewById<TextView>(R.id.textView)
        textView.text = "ojo quedan " + Restante + " espacios"
        VolverButton = findViewById(R.id.Volver_BTN)
        texto = findViewById(R.id.textView)
        VolverButton.setOnClickListener {
            finish()
        }
    }
    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            listOf(
                VolverButton,
                texto
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 0) // Ajusta el tiempo de retraso si es necesario
    }
}