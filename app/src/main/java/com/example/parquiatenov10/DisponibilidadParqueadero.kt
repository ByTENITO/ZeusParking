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

class DisponibilidadParqueadero : AppCompatActivity() {
    private lateinit var VolverHome:Button
    private lateinit var disponibilidad:Button
    private lateinit var texto:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        var Cuantos = 11
        var Total = 20
        var Restante = Total - Cuantos
        super.onCreate(savedInstanceState)
        startAnimationsWithDelay()
        enableEdgeToEdge()
        setContentView(R.layout.activity_disponibilidad_parqueadero)
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
        val textView = findViewById<TextView>(R.id.textView_vigi)
        textView.text = "parcerito quedan " + Restante + " espacios"
        VolverHome = findViewById(R.id.Volver_Home)
        disponibilidad = findViewById(R.id.DisponibilidadParque)
        texto = findViewById(R.id.textView_vigi)
        VolverHome.setOnClickListener {
            finish()
        }
    }
    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            listOf(
                VolverHome,
                disponibilidad,
                texto
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 1) // Ajusta el tiempo de retraso si es necesario
    }
}