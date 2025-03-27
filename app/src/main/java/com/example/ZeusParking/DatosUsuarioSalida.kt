package com.example.parquiatenov10

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
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
                                .addSnapshotListener { document, e ->
                                    if (e != null) {
                                        Log.d("FireStore", "No se encontraron documentos:$vehiculo")
                                        return@addSnapshotListener
                                    } else {
                                        if (document != null && document.exists()) {
                                            val espacios = document.getLong(vehiculo)
                                            database.collection("EspaciosFijos")
                                                .document("NLRmedawc0M0nrpDt9Ci")
                                                .get()
                                                .addOnSuccessListener { document ->
                                                    if (!document.exists()) {
                                                        Log.w(
                                                            "FireStore",
                                                            "No se encontraron los espacios fijos 'Teno' volvio a borrar la base de datos: NLRmedawc0M0nrpDt9Ci "
                                                        )
                                                    } else {
                                                        val espaciosFijos =
                                                            document.getLong("Furgon")

                                                        if (espacios?.toInt() ?: 0 >= espaciosFijos?.toInt() ?: 0) {
                                                            Toast.makeText(
                                                                this,
                                                                "No es posible realizar salidas, porque se superaron los espacios posibles en la base de datos",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        } else {
                                                            database.collection("Disponibilidad")
                                                                .document("0ctYNlFXwtVw9ylURFXi")
                                                                .update(
                                                                    "Furgon",
                                                                    FieldValue.increment(1)
                                                                )
                                                                .addOnSuccessListener {
                                                                    Log.d(
                                                                        "FireStore",
                                                                        "Campo 'Furgon' incremento"
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
                                        }
                                    }
                                }
                        }
                        if (tipoTXT.text == "Vehiculo Particular") {
                            database.collection("Disponibilidad")
                                .document("UF0tfabGHGitcj7En6Wy")
                                .addSnapshotListener { document, e ->
                                    if (e != null) {
                                        Log.d("FireStore", "No se encontraron documentos:$vehiculo")
                                        return@addSnapshotListener
                                    } else {
                                        if (document != null && document.exists()) {
                                            val espacios = document.getLong(vehiculo)
                                            database.collection("EspaciosFijos")
                                                .document("edYUNbYSmPtvu1H6dI93")
                                                .get()
                                                .addOnSuccessListener { document ->
                                                    if (!document.exists()) {
                                                        Log.w(
                                                            "FireStore",
                                                            "No se encontraron los espacios fijos 'Teno' volvio a borrar la base de datos: edYUNbYSmPtvu1H6dI93 "
                                                        )
                                                    } else {
                                                        val espaciosFijos =
                                                            document.getLong("Vehiculo Particular")

                                                        if (espacios?.toInt() ?: 0 >= espaciosFijos?.toInt() ?: 0) {
                                                            Toast.makeText(
                                                                this,
                                                                "No es posible realizar salidas, porque se superaron los espacios posibles en la base de datos",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        } else {
                                                            database.collection("Disponibilidad")
                                                                .document("UF0tfabGHGitcj7En6Wy")
                                                                .update(
                                                                    "Vehiculo Particular",
                                                                    FieldValue.increment(1)
                                                                )
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
                                        }
                                    }
                                }
                        }
                        if (tipoTXT.text == "Bicicleta") {
                            database.collection("Disponibilidad")
                                .document("IuDC5XlTyhxhqU4It8SD")
                                .addSnapshotListener { document, e ->
                                    if (e != null) {
                                        Log.d("FireStore", "No se encontraron documentos:$vehiculo")
                                    } else {
                                        if (document != null && document.exists()) {
                                            val espacios = document.getLong(vehiculo)
                                            database.collection("EspaciosFijos")
                                                .document("sPcLdzFgRF2eAY5BWvFC")
                                                .get()
                                                .addOnSuccessListener { document ->
                                                    if (!document.exists()) {
                                                        Log.w(
                                                            "FireStore",
                                                            "No se encontraron los espacios fijos 'Teno' volvio a borrar la base de datos: sPcLdzFgRF2eAY5BWvFC "
                                                        )
                                                    } else {
                                                        val espaciosFijos =
                                                            document.getLong("Bicicleta")

                                                        if (espacios?.toInt() ?: 0 >= espaciosFijos?.toInt() ?: 0) {
                                                            Toast.makeText(
                                                                this,
                                                                "No es posible realizar salidas, porque se superaron los espacios posibles en la base de datos",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        } else {
                                                            database.collection("Disponibilidad")
                                                                .document("IuDC5XlTyhxhqU4It8SD")
                                                                .update(
                                                                    "Bicicleta",
                                                                    FieldValue.increment(1)
                                                                )
                                                                .addOnSuccessListener {
                                                                    Log.d(
                                                                        "FireStore",
                                                                        "Campo 'Bicicleta' incremento"
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
                                        }
                                    }
                                }
                        }
                        if (tipoTXT.text == "Motocicleta") {
                            database.collection("Disponibilidad")
                                .document("ntHgnXs4Qbz074siOrvz")
                                .addSnapshotListener { document, e ->
                                    if (e != null) {
                                        Log.d("FireStore", "No se encontraron documentos:$vehiculo")
                                    } else {
                                        if (document != null && document.exists()) {
                                            val espacios = document.getLong(vehiculo)
                                            database.collection("EspaciosFijos")
                                                .document("AQjYvV224T01lrSEeQQY")
                                                .get()
                                                .addOnSuccessListener { document ->
                                                    if (!document.exists()) {
                                                        Log.w(
                                                            "FireStore",
                                                            "No se encontraron los espacios fijos 'Teno' volvio a borrar la base de datos: AQjYvV224T01lrSEeQQY "
                                                        )
                                                    } else {
                                                        val espaciosFijos =
                                                            document.getLong("Motocicleta")

                                                        if (espacios?.toInt() ?: 0 >= espaciosFijos?.toInt() ?: 0) {
                                                            Toast.makeText(
                                                                this,
                                                                "No es posible realizar ingresos, porque se superaron los espacios posibles en la base de datos",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        } else {
                                                            database.collection("Disponibilidad")
                                                                .document("ntHgnXs4Qbz074siOrvz")
                                                                .update(
                                                                    "Motocicleta",
                                                                    FieldValue.increment(1)
                                                                )
                                                                .addOnSuccessListener {
                                                                    Log.d(
                                                                        "FireStore",
                                                                        "Campo 'Motocicleta' incremento"
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