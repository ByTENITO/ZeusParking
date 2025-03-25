package com.example.parquiatenov10

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DatosUsuarioSalida : AppCompatActivity() {

    private var database = FirebaseFirestore.getInstance()
    private lateinit var botonSalida: Button
    private lateinit var correoTXT: TextView
    private lateinit var colorTXT: TextView
    private lateinit var nombreTXT: TextView
    private lateinit var apellidoTXT: TextView
    private lateinit var numeroTXT: TextView
    private lateinit var tipoTXT: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_usuario_salida)
        botonSalida = findViewById(R.id.SalidaExit)
        correoTXT = findViewById(R.id.correoFirebaseSalida)
        colorTXT = findViewById(R.id.colorFirebaseSalida)
        nombreTXT = findViewById(R.id.nombreFirebaseSalida)
        apellidoTXT = findViewById(R.id.apellidoFirebaseSalida)
        numeroTXT = findViewById(R.id.idVehiculoFirebaseSalida)
        tipoTXT = findViewById(R.id.tipoVehiculoFirebaseSalida)
        val correo = intent.getStringExtra("correo") ?: "No disponible"
        val vehiculo = intent.getStringExtra("tipo") ?: "No disponible"
        val idVehiculo = intent.getStringExtra("id") ?: "No disponible"
        Log.d("IntentReceived", "Correo: $correo, Tipo: $vehiculo, ID: $idVehiculo")
        Log.d("FireStore", "Base de datos inicializada: ${database != null}")
        if (correo.isNullOrEmpty()) {
            Toast.makeText(this, "Error: No se recibio el correo", Toast.LENGTH_SHORT).show()
        } else {
            database.collection("Bici_Usuarios")
                .whereEqualTo("correo", correo)
                .whereEqualTo("tipo", vehiculo)
                .whereEqualTo("numero", idVehiculo)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Log.d("FireStore", "No se encontraron documentos con correo: $correo")
                    } else {
                        for (document in documents) {
                            val correo = document.getString("correo")
                            val color = document.getString("color")
                            val nombre = document.getString("nombre")
                            val apellido = document.getString("apellidos")
                            val numero = document.getString("numero")
                            val tipoVehiculo = document.getString("tipo")
                            correoTXT.text = correo
                            colorTXT.text = color
                            nombreTXT.text = nombre
                            apellidoTXT.text = apellido
                            numeroTXT.text = numero
                            tipoTXT.text = tipoVehiculo
                            Toast.makeText(
                                this,
                                "FireStore,${document.id}=>${document.data}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        if (tipoTXT.text == "Furgon") {
                            database.collection("Disponibilidad")
                                .document("0ctYNlFXwtVw9ylURFXi")
                                .get()
                                .addOnSuccessListener { document ->
                                    if (!document.exists()) {
                                        Log.d("FireStore", "No se encontraron documentos:$vehiculo")
                                    } else {
                                        val espacios = document.getLong(vehiculo)?:5L
                                        if (espacios.toInt() == 5) {
                                            Toast.makeText(
                                                this,
                                                "No es posible realizar ingresos, porque se superaron los espacios posibles en la base de datos",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            val Ref = database.collection("Disponibilidad")
                                                .document("0ctYNlFXwtVw9ylURFXi")
                                            Ref.update("Furgon", FieldValue.increment(1))
                                                .addOnSuccessListener {
                                                    Log.d("FireStore", "Campo 'Furgon' incremento")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                        "FireStore",
                                                        "Error al actualizar el campo",
                                                        e
                                                    )
                                                }
                                        }
                                    }
                                }
                        } else if (tipoTXT.text == "Vehiculo Particular") {
                            database.collection("Disponibilidad")
                                .document("UF0tfabGHGitcj7En6Wy")
                                .get()
                                .addOnSuccessListener { document ->
                                    if (!document.exists()) {
                                        Log.d("FireStore", "No se encontraron documentos:$vehiculo")
                                    } else {
                                        val espacios = document.getLong(vehiculo)?:15L
                                        if (espacios.toInt() == 15) {
                                            Toast.makeText(
                                                this,
                                                "No es posible realizar ingresos, porque se superaron los espacios posibles en la base de datos",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            val Ref = database.collection("Disponibilidad")
                                                .document("UF0tfabGHGitcj7En6Wy")
                                            Ref.update("Vehiculo Particular", FieldValue.increment(1))
                                                .addOnSuccessListener {
                                                    Log.d(
                                                        "FireStore",
                                                        "Campo 'Vehiculo Particular' incremento"
                                                    )
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                        "FireStore",
                                                        "Error al actualizar el campo",
                                                        e
                                                    )
                                                }
                                        }
                                    }
                                }
                        } else if (tipoTXT.text == "Bicicleta") {
                            database.collection("Disponibilidad")
                                .document("XZLnv4sWJJ4M4h9KK1dc")
                                .get()
                                .addOnSuccessListener { document ->
                                    if (!document.exists()) {
                                        Log.d("FireStore", "No se encontraron documentos:$vehiculo")
                                    } else {
                                        val espacios = document.getLong(vehiculo)?:50L
                                        if (espacios.toInt() == 50) {
                                            Toast.makeText(
                                                this,
                                                "No es posible realizar ingresos, porque se superaron los espacios posibles en la base de datos",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            val Ref = database.collection("Disponibilidad")
                                                .document("XZLnv4sWJJ4M4h9KK1dc")
                                            Ref.update("Bicicleta", FieldValue.increment(1))
                                                .addOnSuccessListener {
                                                    Log.d("FireStore", "Campo 'Bicicleta' incremento")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                        "FireStore",
                                                        "Error al actualizar el campo",
                                                        e
                                                    )
                                                }
                                        }
                                    }
                                }
                        } else if (tipoTXT.text == "Motocicleta") {
                            database.collection("Disponibilidad")
                                .document("ntHgnXs4Qbz074siOrvz")
                                .get()
                                .addOnSuccessListener { document ->
                                    if (!document.exists()) {
                                        Log.d("FireStore", "No se encontraron documentos:$vehiculo")
                                    } else {
                                        val espacios = document.getLong(vehiculo)?:0L
                                        if (espacios.toInt() == 10) {
                                            Toast.makeText(
                                                this,
                                                "No es posible realizar ingresos, porque se superaron los espacios posibles en la base de datos",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            val Ref = database.collection("Disponibilidad")
                                                .document("ntHgnXs4Qbz074siOrvz")
                                            Ref.update("Motocicleta", FieldValue.increment(1))
                                                .addOnSuccessListener {
                                                    Log.d("FireStore", "Campo 'Motocicleta' incremento")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(
                                                        "FireStore",
                                                        "Error al actualizar el campo",
                                                        e
                                                    )
                                                }
                                        }
                                    }
                                }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("FireStore", "Error obteniendo documentos", exception)
                }
        }
        botonSalida.setOnClickListener {
            finish()
        }
    }
}