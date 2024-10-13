package com.example.parquiatenov10

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

class RegistrarBiciActivity : AppCompatActivity() {
    private lateinit var nombreEd: EditText
    private lateinit var apellidosEd: EditText
    private lateinit var colorEd: EditText
    private lateinit var cedulaNum: EditText
    private lateinit var marcoNum: EditText
    private lateinit var agregarFoto1Btn: Button
    private lateinit var agregarFoto2Btn: Button
    private lateinit var editarBtn: Button
    private lateinit var eliminarBtn: Button

    private var fotoUri1: Uri? = null
    private var fotoUri2: Uri? = null
    private var biciId: String? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_bici)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de los elementos
        nombreEd = findViewById(R.id.Nombre_ED)
        apellidosEd = findViewById(R.id.Apellidos_ED)
        colorEd = findViewById(R.id.Color_ED)
        cedulaNum = findViewById(R.id.Cedula_NUM)
        marcoNum = findViewById(R.id.Marco_NUM)
        agregarFoto1Btn = findViewById(R.id.AgregarFoto1_BTN)
        agregarFoto2Btn = findViewById(R.id.AgregarFoto2_BTN)
        editarBtn = findViewById(R.id.Editar_BTN)
        eliminarBtn = findViewById(R.id.Eliminar_BTN)

        // Evento para agregar la primera foto
        agregarFoto1Btn.setOnClickListener {
            seleccionarImagen(1)
        }

        // Evento para agregar la segunda foto
        agregarFoto2Btn.setOnClickListener {
            seleccionarImagen(2)
        }

        // Guardar los datos en Firestore
        findViewById<Button>(R.id.Guardar_BTN).setOnClickListener {
            guardarDatosEnFirestore()
        }

        // Editar los datos
        editarBtn.setOnClickListener {
            editarDatosEnFirestore()
        }

        // Eliminar los datos
        eliminarBtn.setOnClickListener {
            eliminarDatosEnFirestore()
        }

        // Cargar los datos si se está editando
        cargarDatosSiExistente()
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
                    Toast.makeText(this, "Primera Foto Guardada Con Exito", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    fotoUri2 = data.data
                    Toast.makeText(this, "Segunda Foto Guardada Con Exito", Toast.LENGTH_SHORT).show()
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

        if (nombre.isEmpty() || apellidos.isEmpty() || color.isEmpty() || cedula.isEmpty() || marco.isEmpty()) {
            Toast.makeText(this, "Llene Todos los Campos Porfavor", Toast.LENGTH_SHORT).show()
            return
        }

        if (fotoUri1 == null || fotoUri2 == null) {
            Toast.makeText(this, "Debe subir las dos fotos", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val biciCollection = db.collection("Bici Usuarios")
        val query = biciCollection.whereEqualTo("usuarioId", userId).limit(1)

        query.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    val biciData: MutableMap<String, Any> = hashMapOf(
                        "nombre" to nombre,
                        "apellidos" to apellidos,
                        "color" to color,
                        "cedula" to cedula,
                        "marco" to marco,
                        "usuarioId" to userId
                    )

                    biciCollection.add(biciData)
                        .addOnSuccessListener { documentReference ->
                            biciId = documentReference.id
                            Toast.makeText(this, "Datos Guardados Correctamente", Toast.LENGTH_SHORT).show()
                            subirImagenAFirebase(fotoUri1!!, "Foto1")
                            subirImagenAFirebase(fotoUri2!!, "Foto2")
                            limpiarCampos() // Limpiar los campos después de guardar
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "No se Pudieron Guardar los Datos: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Ya has registrado una bicicleta anteriormente", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al verificar registro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Función para limpiar los campos
    private fun limpiarCampos() {
        nombreEd.text.clear()
        apellidosEd.text.clear()
        colorEd.text.clear()
        cedulaNum.text.clear()
        marcoNum.text.clear()
        fotoUri1 = null
        fotoUri2 = null
    }

    private fun editarDatosEnFirestore() {
        val nombre = nombreEd.text.toString()
        val apellidos = apellidosEd.text.toString()
        val color = colorEd.text.toString()
        val cedula = cedulaNum.text.toString()
        val marco = marcoNum.text.toString()

        if (biciId == null) {
            Toast.makeText(this, "No hay registro para editar", Toast.LENGTH_SHORT).show()
            return
        }

        if (nombre.isEmpty() || apellidos.isEmpty() || color.isEmpty() || cedula.isEmpty() || marco.isEmpty()) {
            Toast.makeText(this, "Llene Todos los Campos Porfavor", Toast.LENGTH_SHORT).show()
            return
        }

        val biciData: MutableMap<String, Any> = hashMapOf(
            "nombre" to nombre,
            "apellidos" to apellidos,
            "color" to color,
            "cedula" to cedula,
            "marco" to marco
        )

        db.collection("Bici Usuarios").document(biciId!!).update(biciData)
            .addOnSuccessListener {
                Toast.makeText(this, "Datos Editados Correctamente", Toast.LENGTH_SHORT).show()
                if (fotoUri1 != null) {
                    subirImagenAFirebase(fotoUri1!!, "Foto1")
                }
                if (fotoUri2 != null) {
                    subirImagenAFirebase(fotoUri2!!, "Foto2")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se Pudieron Editar los Datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarDatosEnFirestore() {
        if (biciId == null) {
            Toast.makeText(this, "No hay registro para eliminar", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("Bici Usuarios").document(biciId!!).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Datos Eliminados Correctamente", Toast.LENGTH_SHORT).show()
                eliminarImagenesDeFirebase()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se Pudieron Eliminar los Datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarImagenesDeFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val storageRef = storage.reference.child("Bicicletas/${cedulaNum.text.toString()}_Foto1.jpg")
        storageRef.delete().addOnSuccessListener {
            Toast.makeText(this, "Foto1 Eliminada Correctamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al Eliminar Foto1: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        val storageRef2 = storage.reference.child("Bicicletas/${cedulaNum.text.toString()}_Foto2.jpg")
        storageRef2.delete().addOnSuccessListener {
            Toast.makeText(this, "Foto2 Eliminada Correctamente", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al Eliminar Foto2: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosSiExistente() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("Bici Usuarios")
            .whereEqualTo("usuarioId", userId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.first()
                    biciId = document.id
                    val biciData = document.data

                    nombreEd.setText(biciData["nombre"] as? String)
                    apellidosEd.setText(biciData["apellidos"] as? String)
                    colorEd.setText(biciData["color"] as? String)
                    cedulaNum.setText(biciData["cedula"] as? String)
                    marcoNum.setText(biciData["marco"] as? String)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar datos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirImagenAFirebase(uri: Uri, fotoName: String) {
        val ref: StorageReference = storage.reference.child("Bicicletas/${cedulaNum.text.toString()}_$fotoName.jpg")
        ref.putFile(uri)
            .addOnSuccessListener {
                Toast.makeText(this, "$fotoName Se Subieron Perfectamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al Subir $fotoName: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}



