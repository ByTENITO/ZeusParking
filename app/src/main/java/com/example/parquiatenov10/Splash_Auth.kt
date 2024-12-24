package com.example.parquiatenov10

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class Splash_Auth : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_auth)

        val transicion = findViewById<ImageView>(R.id.image_splash)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        transicion.startAnimation(fadeOut)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)

            overridePendingTransition( 0,0)
            finish()
        }, 4000)

    }
}