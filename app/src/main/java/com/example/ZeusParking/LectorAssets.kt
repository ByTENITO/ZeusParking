package com.example.parquiatenov10

import java.io.IOException



object LectorAssets {
    fun readFileFromAssets(context: AuthActivity, fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            "Error al cargar el archivo: ${e.message}"
        }
    }
}