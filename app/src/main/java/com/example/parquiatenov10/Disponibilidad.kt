package com.example.parquiatenov10

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Disponibilidad : AppCompatActivity() {
    private lateinit var VolverButton:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        var Cuantos = 11
        var Total = 20
        var Restante = Total - Cuantos
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
        VolverButton.setOnClickListener {
            finish()
        }
    }
}