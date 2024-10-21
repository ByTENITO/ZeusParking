package com.example.parquiatenov10.ui.theme

import android.os.Bundle
import android.widget.RadioButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.parquiatenov10.R

class Disponibilidad : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var cuantos = 11
        var total = 20
        var restante = total-cuantos
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_disponibilidad)
        for (i in 1..cuantos) {
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
        textView.text = "ojo quedan "+restante+" espacios"
    }
}