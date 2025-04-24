package com.example.ZeusParking

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parquiatenov10.AuthActivity
import com.example.parquiatenov10.EntradaQrParqueadero
import com.example.parquiatenov10.HomeActivity
import com.example.parquiatenov10.Home_vigilante
import com.example.parquiatenov10.Localizacion
import com.example.parquiatenov10.QrActivity
import com.example.parquiatenov10.R
import com.example.parquiatenov10.RegistrarBiciActivity
import com.example.parquiatenov10.SalidaQrParqueadero
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseNavigationActivity : AppCompatActivity() {

    protected lateinit var bottomNavigationView: BottomNavigationView
    private var doubleBackToExitPressedOnce = false
    private var backPressCountVigilante = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setupNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = getCurrentNavigationItem()

        bottomNavigationView.setOnItemSelectedListener { item ->
            if (item.itemId != bottomNavigationView.selectedItemId) {
                navigateTo(item.itemId)
            }
            true
        }
    }

    private fun navigateTo(itemId: Int) {
        val targetActivity = when (itemId) {
            R.id.home -> HomeActivity::class.java
            R.id.localizacion -> Localizacion::class.java
            R.id.registro -> RegistrarBiciActivity::class.java
            R.id.qr -> QrActivity::class.java
            R.id.entrada -> EntradaQrParqueadero::class.java
            R.id.salida -> SalidaQrParqueadero::class.java
            R.id.home_vigi -> Home_vigilante::class.java
            else -> null
        }

        targetActivity?.let {
            if (this::class.java != it) {
                val intent = Intent(this, it).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Resetear el contador cuando la actividad vuelve al frente
        backPressCountVigilante = 0

        if (::bottomNavigationView.isInitialized) {
            bottomNavigationView.selectedItemId = getCurrentNavigationItem()
        }
    }

    override fun onBackPressed() {
        when {
            // Comportamiento para Home de usuario normal
            this is HomeActivity -> handleDoubleBackToExit()

            // Comportamiento para Home de vigilante
            this is Home_vigilante -> handleVigilanteBackPress()

            // Comportamiento para otras actividades de vigilante
            isVigilanteActivity() -> {
                navigateTo(R.id.home_vigi)
            }

            // Comportamiento por defecto para otras actividades (usuario normal)
            else -> navigateTo(R.id.home)
        }
    }

    private fun handleVigilanteBackPress() {
        backPressCountVigilante++

        if (backPressCountVigilante == 1) {
            Toast.makeText(this, "Presiona de nuevo para cerrar sesión", Toast.LENGTH_SHORT).show()

            // Resetear el contador después de 2 segundos
            Handler(Looper.getMainLooper()).postDelayed({
                if (backPressCountVigilante < 3) {
                    backPressCountVigilante = 0
                }
            }, 2000)
        } else if (backPressCountVigilante >= 2) {
            // Cerrar sesión y redirigir a Auth
            val intent = Intent(this, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }

    private fun isVigilanteActivity(): Boolean {
        // Lista de actividades de vigilante
        val vigilanteActivities = listOf(
            EntradaQrParqueadero::class.java,
            SalidaQrParqueadero::class.java,
            Home_vigilante::class.java
            // Agrega aquí otras actividades de vigilante si es necesario
        )
        return vigilanteActivities.contains(this::class.java)
    }

    private fun handleDoubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Presiona de nuevo para salir", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    protected abstract fun getCurrentNavigationItem(): Int
}