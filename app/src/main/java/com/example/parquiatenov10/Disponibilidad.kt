package com.example.parquiatenov10

import android.os.Bundle
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

    class Disponibilidad : AppCompatActivity() {
        var cuantos = 10
        var total = 36
        var restantes = total-cuantos
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_disponibilidad)
            for (i in 1..total) {
                val radioButtonId = resources.getIdentifier("select$i", "id", packageName)
                val radioButton = findViewById<RadioButton>(radioButtonId)
                radioButton.isEnabled = false
            }

            for (i in 1..cuantos) {
                val radioButtonId = resources.getIdentifier("select$i", "id", packageName)
                val radioButton = findViewById<RadioButton>(radioButtonId)
                radioButton.isChecked = true
            }
            val textView = findViewById<TextView>(R.id.textView)
            textView.text = "ojo quedan "+restantes.toString()+" espacios"
        }
    }