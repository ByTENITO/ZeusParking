package com.example.parquiatenov10

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import android.text.InputType
import android.text.InputFilter
import android.view.animation.AnimationUtils
import com.example.ZeusParking.BaseNavigationActivity

class RegistrarBiciActivity : BaseNavigationActivity() {
    private lateinit var nombreEd: EditText
    private lateinit var apellidosEd: EditText
    private lateinit var colorEd: EditText
    private lateinit var cedulaNum: EditText
    private lateinit var marcoNum: EditText
    private lateinit var agregarFoto1Btn: Button
    private lateinit var agregarFoto2Btn: Button
    private lateinit var tiposSpinner: Spinner
    private lateinit var Guardar: Button
    private lateinit var texto: TextView

    private var fotoUri1: Uri? = null
    private var fotoUri2: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()


    data class BiciData(
        val nombre: String,
        val apellidos: String,
        val color: String,
        val cedula: String,
        val numero: String,
        var tipo: String,
        val correo: String,
        val id: String
    ) : java.io.Serializable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar_bici)
        enableEdgeToEdge()
        startAnimationsWithDelay()

        //Responsividad
        Responsividad.inicializar(this)

        //Navegacion
        setupNavigation()

        overridePendingTransition(0, 0)

        // Configuración de los elementos de la interfaz
        nombreEd = findViewById(R.id.Nombre_ED)
        apellidosEd = findViewById(R.id.Apellidos_ED)
        colorEd = findViewById(R.id.Color_ED)
        cedulaNum = findViewById(R.id.Cedula_NUM)
        marcoNum = findViewById(R.id.Marco_NUM)
        agregarFoto1Btn = findViewById(R.id.AgregarFoto1_BTN)
        agregarFoto2Btn = findViewById(R.id.AgregarFoto2_BTN)
        tiposSpinner = findViewById(R.id.Tipos_Spinner)
        Guardar = findViewById(R.id.Guardar_BTN)
        texto = findViewById(R.id.textView3)


        // Configuración del Spinner
        val adapter = ArrayAdapter.createFromResource(this, R.array.items, R.layout.estilo_spinner)
        adapter.setDropDownViewResource(R.layout.estilo_spinner)
        tiposSpinner.adapter = adapter

        tiposSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    1, 2 -> {
                        marcoNum.hint = "4 Ultimos Numeros"
                        marcoNum.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(20))
                    }

                    3, 4, 5 -> {
                        marcoNum.hint = when (position) {
                            3 -> "Placa (Ej. abc123)"
                            4 -> "Placa (Ej. abc123)"
                            5 -> "Número de Furgón"
                            else -> "Número de Marco"
                        }
                        marcoNum.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(7))
                    }

                    else -> {
                        marcoNum.hint = ""
                        marcoNum.inputType = InputType.TYPE_CLASS_NUMBER
                        marcoNum.filters = arrayOf(InputFilter.LengthFilter(20))
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Evento para agregar la primera foto
        agregarFoto1Btn.setOnClickListener {
            seleccionarImagen(1)
        }


        // Evento para agregar la segunda foto
        agregarFoto2Btn.setOnClickListener {
            seleccionarImagen(2)
        }

        Guardar.setOnClickListener {
            guardarDatosEnFirestore()
        }
    }

    //Navegacion del Sistema
    override fun getCurrentNavigationItem(): Int = R.id.registro

    private fun startAnimationsWithDelay() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Handler(Looper.getMainLooper()).postDelayed({
            listOf(
                nombreEd,
                apellidosEd,
                colorEd,
                cedulaNum,
                marcoNum,
                agregarFoto1Btn,
                agregarFoto2Btn,
                tiposSpinner,
                texto,
                Guardar
            ).forEach { view ->
                view.startAnimation(fadeIn)
            }
        }, 0) // Ajusta el tiempo de retraso si es necesario
    }

    private fun seleccionarImagen(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            when (requestCode) {
                1 -> {
                    fotoUri1 = data.data
                    Toast.makeText(this, "Foto Guardada Con Éxito", Toast.LENGTH_SHORT)
                        .show()
                }

                2 -> {
                    fotoUri2 = data.data
                    Toast.makeText(this, "Foto Guardada Con Éxito", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun guardarDatosEnFirestore() {
        val nombre = nombreEd.text.toString()
        val apellidos = apellidosEd.text.toString()
        val color = colorEd.text.toString()
        val cedula = cedulaNum.text.toString()
        val marco = marcoNum.text.toString()
        val tipoVehiculo = tiposSpinner.selectedItem.toString()
        val correo = FirebaseAuth.getInstance().currentUser?.email.toString()
        val id = FirebaseAuth.getInstance().currentUser?.uid

        if (nombre.isEmpty() || apellidos.isEmpty() || color.isEmpty() || cedula.isEmpty() || marco.isEmpty() || fotoUri1 == null || fotoUri2 == null) {
            Toast.makeText(this, "Llene Todos los Campos Por Favor", Toast.LENGTH_SHORT).show()
            return
        }

        // First check if vehicle already exists
        db.collection("Bici_Usuarios")
            .whereEqualTo("numero", marco)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result?.isEmpty == false) {
                        // Vehicle already exists
                        Toast.makeText(this, "Error, el vehículo ya está registrado", Toast.LENGTH_LONG).show()
                        limpiarCampos()
                    } else {
                        // Vehicle doesn't exist, proceed with registration
                        val biciData = BiciData(
                            nombre,
                            apellidos,
                            color,
                            cedula,
                            marco,
                            tipoVehiculo,
                            correo,
                            id.toString()
                        )

                        db.collection("Bici_Usuarios")
                            .add(biciData)
                            .addOnSuccessListener { documentReference ->
                                // Upload photos after successful registration
                                fotoUri1?.let { uri ->
                                    User(uri, cedula)
                                }
                                fotoUri2?.let { uri ->
                                    subirFoto(uri, marco, documentReference.id)
                                }
                                Toast.makeText(
                                    this,
                                    "Datos guardados exitosamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                                limpiarCampos()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Error al guardar los datos",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Error al verificar el registro: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun subirFoto(fotoUri: Uri?, tipo: String, biciId: String) {
        if (fotoUri == null) return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = storage.reference.child("$userId/$biciId/$tipo.png")

        storageRef.putFile(fotoUri)
            .addOnSuccessListener {
                Toast.makeText(this, "Foto $tipo subida exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir la foto $tipo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun User(fotoUri: Uri?, tipo: String) {
        if (fotoUri == null) return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = storage.reference.child("$userId/$tipo.png")

        storageRef.putFile(fotoUri)
            .addOnSuccessListener {
                Toast.makeText(this, "Foto $tipo subida exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al subir la foto $tipo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        nombreEd.text.clear()
        apellidosEd.text.clear()
        colorEd.text.clear()
        cedulaNum.text.clear()
        marcoNum.text.clear()
        tiposSpinner.setSelection(0)
        fotoUri1 = null
        fotoUri2 = null
    }
}
