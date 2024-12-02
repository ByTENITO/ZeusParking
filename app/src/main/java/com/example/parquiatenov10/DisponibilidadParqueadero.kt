package com.example.parquiatenov10

import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class DisponibilidadParqueadero : AppCompatActivity() {
    private lateinit var VolverHome:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        var Cuantos = 11
        var Total = 20
        var Restante = Total - Cuantos
        super.onCreate(savedInstanceState)
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
        VolverHome.setOnClickListener {
            finish()
        }
    }
}