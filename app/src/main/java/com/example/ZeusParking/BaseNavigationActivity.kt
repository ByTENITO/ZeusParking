package com.example.ZeusParking

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.parquiatenov10.HomeActivity
import com.example.parquiatenov10.Localizacion
import com.example.parquiatenov10.QrActivity
import com.example.parquiatenov10.R
import com.example.parquiatenov10.RegistrarBiciActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
abstract class BaseNavigationActivity : AppCompatActivity() {

    protected lateinit var bottomNavigationView: BottomNavigationView
    private var doubleBackToExitPressedOnce = false

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
            else -> null
        }

        targetActivity?.let {
            // Avoid reopening the same activity
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

        if (::bottomNavigationView.isInitialized) {
            bottomNavigationView.selectedItemId = getCurrentNavigationItem()
        }
    }

    override fun onBackPressed() {
        if (this is HomeActivity) {
            handleDoubleBackToExit()
        } else {
            navigateTo(R.id.home)
        }
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