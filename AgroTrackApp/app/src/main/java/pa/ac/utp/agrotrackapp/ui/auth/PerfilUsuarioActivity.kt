package pa.ac.utp.agrotrackapp.ui.auth

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.auth.SqliteAuthRepository
import pa.ac.utp.agrotrackapp.domain.model.User
import pa.ac.utp.agrotrackapp.domain.repository.AuthRepository
import pa.ac.utp.agrotrackapp.services.BiometricService
import java.io.File

class PerfilUsuarioActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private var currentUser: User? = null
    private var imagePathLocal: String? = null
    private var isEditMode = false

    // Containers and toggle buttons
    private lateinit var viewModeContainer: LinearLayout
    private lateinit var editModeContainer: LinearLayout
    private lateinit var cardCameraOverlay: View
    private lateinit var btnEditarPerfil: ImageButton
    private lateinit var tvProfileTitle: TextView

    // View Mode TextViews
    private lateinit var tvNombreCompletoVal: TextView
    private lateinit var tvUsuarioVal: TextView
    private lateinit var tvRolVal: TextView
    private lateinit var tvFincaNombreVal: TextView
    private lateinit var tvLugarVal: TextView

    // Edit Mode Views
    private lateinit var ivPerfilPreview: ImageView
    private lateinit var btnCambiarFoto: MaterialCardView
    
    private lateinit var tilNombre: TextInputLayout
    private lateinit var etNombre: TextInputEditText
    
    private lateinit var tilApellido: TextInputLayout
    private lateinit var etApellido: TextInputEditText
    
    private lateinit var tilRol: TextInputLayout
    private lateinit var etRol: AutoCompleteTextView
    
    private lateinit var tilUsuario: TextInputLayout
    private lateinit var etUsuario: TextInputEditText
    
    private lateinit var tilFincaNombre: TextInputLayout
    private lateinit var etFincaNombre: TextInputEditText
    
    private lateinit var tilLugar: TextInputLayout
    private lateinit var etLugar: TextInputEditText
    
    private lateinit var btnGuardarPerfil: MaterialButton
    private lateinit var btnDestruirDatos: MaterialButton
    private lateinit var switchBiometria: com.google.android.material.switchmaterial.SwitchMaterial
    private lateinit var switchAlertas: com.google.android.material.switchmaterial.SwitchMaterial

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 3001
        private const val REQUEST_IMAGE_PICK = 3002
        private val ROLES = arrayOf("Productor", "Administrador", "Veterinario", "Encargado")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfil_usuario)

        authRepository = SqliteAuthRepository(this)
        currentUser = authRepository.getCurrentUser()

        // Bind containers and toolbar
        viewModeContainer = findViewById(R.id.viewModeContainer)
        editModeContainer = findViewById(R.id.editModeContainer)
        cardCameraOverlay = findViewById(R.id.cardCameraOverlay)
        btnEditarPerfil = findViewById(R.id.btnEditarPerfil)
        tvProfileTitle = findViewById(R.id.tvProfileTitle)

        // Bind view mode TextViews
        tvNombreCompletoVal = findViewById(R.id.tvNombreCompletoVal)
        tvUsuarioVal = findViewById(R.id.tvUsuarioVal)
        tvRolVal = findViewById(R.id.tvRolVal)
        tvFincaNombreVal = findViewById(R.id.tvFincaNombreVal)
        tvLugarVal = findViewById(R.id.tvLugarVal)

        // Bind edit mode inputs
        ivPerfilPreview = findViewById(R.id.ivPerfilPreview)
        btnCambiarFoto = findViewById(R.id.btnCambiarFoto)
        
        tilNombre = findViewById(R.id.tilNombre)
        etNombre = findViewById(R.id.etNombre)
        
        tilApellido = findViewById(R.id.tilApellido)
        etApellido = findViewById(R.id.etApellido)
        
        tilRol = findViewById(R.id.tilRol)
        etRol = findViewById(R.id.etRol)
        
        tilUsuario = findViewById(R.id.tilUsuario)
        etUsuario = findViewById(R.id.etUsuario)
        
        tilFincaNombre = findViewById(R.id.tilFincaNombre)
        etFincaNombre = findViewById(R.id.etFincaNombre)
        
        tilLugar = findViewById(R.id.tilLugar)
        etLugar = findViewById(R.id.etLugar)
        
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil)
        btnDestruirDatos = findViewById(R.id.btnDestruirDatos)

        // Setup back button
        findViewById<View>(R.id.btnBack).setOnClickListener {
            if (isEditMode) {
                setEditModeEnabled(false)
            } else {
                finish()
            }
        }

        // Setup Roles Adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ROLES)
        etRol.setAdapter(adapter)

        // Toggle edit mode click listener
        btnEditarPerfil.setOnClickListener {
            setEditModeEnabled(true)
        }

        // Photo chooser click listener
        btnCambiarFoto.setOnClickListener {
            if (isEditMode) {
                mostrarOpcionesImagen()
            }
        }

        btnGuardarPerfil.setOnClickListener {
            guardarPerfilConConfirmacion()
        }

        btnDestruirDatos.setOnClickListener {
            destruirDatosPersonalesYFinca()
        }

        findViewById<View>(R.id.btnVerAvisoPrivacidad)?.setOnClickListener {
            mostrarAvisoPrivacidadTransparencia()
        }

        // Bind and initialize privacy switches (Ley N° 81 - Oposición)
        switchBiometria = findViewById(R.id.switchBiometria)
        switchAlertas = findViewById(R.id.switchAlertas)

        val authPrefs = pa.ac.utp.agrotrackapp.data.auth.AuthPrefsHelper.getAuthPrefs(this)
        switchBiometria.isChecked = authPrefs.getBoolean("biometric_enabled", false)
        switchAlertas.isChecked = authPrefs.getBoolean("alerts_enabled", true)

        switchBiometria.setOnCheckedChangeListener { _, isChecked ->
            authPrefs.edit().putBoolean("biometric_enabled", isChecked).apply()
            if (isChecked) {
                Toast.makeText(this, "Acceso biométrico activado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Acceso biométrico desactivado", Toast.LENGTH_SHORT).show()
            }
        }

        switchAlertas.setOnCheckedChangeListener { _, isChecked ->
            authPrefs.edit().putBoolean("alerts_enabled", isChecked).apply()
            if (isChecked) {
                Toast.makeText(this, "Recordatorios y alertas activados", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Recordatorios y alertas desactivados", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize state (Read-Only mode by default)
        setEditModeEnabled(false)
        cargarFotoPerfil()
    }

    private fun cargarFotoPerfil() {
        currentUser?.let { user ->
            if (!user.profileImagePath.isNullOrEmpty()) {
                val file = File(user.profileImagePath)
                if (file.exists()) {
                    val bitmap = pa.ac.utp.agrotrackapp.utils.ImageResizer.decodeSampledBitmapFromFile(file.absolutePath, 300, 300)
                    if (bitmap != null) {
                        ivPerfilPreview.setImageBitmap(bitmap)
                        ivPerfilPreview.imageTintList = null
                        imagePathLocal = user.profileImagePath
                    } else {
                        ivPerfilPreview.setImageResource(R.drawable.vaca)
                    }
                } else {
                    ivPerfilPreview.setImageResource(R.drawable.vaca)
                }
            } else {
                ivPerfilPreview.setImageResource(R.drawable.vaca)
            }
        }
    }

    private fun setEditModeEnabled(enabled: Boolean) {
        isEditMode = enabled
        if (enabled) {
            viewModeContainer.visibility = View.GONE
            editModeContainer.visibility = View.VISIBLE
            cardCameraOverlay.visibility = View.VISIBLE
            btnEditarPerfil.visibility = View.GONE
            tvProfileTitle.text = "Editar Perfil"
            
            btnCambiarFoto.isClickable = true
            btnCambiarFoto.isFocusable = true
        } else {
            viewModeContainer.visibility = View.VISIBLE
            editModeContainer.visibility = View.GONE
            cardCameraOverlay.visibility = View.GONE
            btnEditarPerfil.visibility = View.VISIBLE
            tvProfileTitle.text = "Mi Perfil"
            
            btnCambiarFoto.isClickable = false
            btnCambiarFoto.isFocusable = false

            // Populate text labels with latest values
            currentUser?.let { user ->
                tvNombreCompletoVal.text = "${user.nombre} ${user.apellido}"
                tvUsuarioVal.text = user.usuario
                tvRolVal.text = user.rol
                tvFincaNombreVal.text = user.nombreFinca
                tvLugarVal.text = user.lugar

                // Fill inputs as well
                etNombre.setText(user.nombre)
                etApellido.setText(user.apellido)
                etRol.setText(user.rol, false)
                etUsuario.setText(user.usuario)
                etFincaNombre.setText(user.nombreFinca)
                etLugar.setText(user.lugar)
            }
        }
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf<CharSequence>("Tomar Foto (Cámara)", "Seleccionar de Galería", "Cancelar")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Foto de perfil")
        builder.setItems(opciones) { dialog, item ->
            when {
                opciones[item] == "Tomar Foto (Cámara)" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    try {
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                    } catch (e: Exception) {
                        Toast.makeText(this, "No se pudo abrir la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                opciones[item] == "Seleccionar de Galería" -> {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    intent.type = "image/*"
                    startActivityForResult(intent, REQUEST_IMAGE_PICK)
                }
                else -> dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && currentUser != null) {
            val destFile = File(filesDir, "profile_${currentUser!!.usuario}.png")
            try {
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    if (bitmap != null) {
                        val saved = pa.ac.utp.agrotrackapp.utils.ImageResizer.compressAndSaveBitmap(bitmap, destFile)
                        if (saved) {
                            imagePathLocal = destFile.absolutePath
                            val optBitmap = pa.ac.utp.agrotrackapp.utils.ImageResizer.decodeSampledBitmapFromFile(destFile.absolutePath, 300, 300)
                            ivPerfilPreview.setImageBitmap(optBitmap ?: bitmap)
                            ivPerfilPreview.imageTintList = null
                        } else {
                            Toast.makeText(this, "Error al optimizar foto de la cámara", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (requestCode == REQUEST_IMAGE_PICK) {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        val saved = pa.ac.utp.agrotrackapp.utils.ImageResizer.compressAndSaveImage(this, selectedImageUri, destFile)
                        if (saved) {
                            imagePathLocal = destFile.absolutePath
                            val optBitmap = pa.ac.utp.agrotrackapp.utils.ImageResizer.decodeSampledBitmapFromFile(destFile.absolutePath, 300, 300)
                            ivPerfilPreview.setImageBitmap(optBitmap)
                            ivPerfilPreview.imageTintList = null
                        } else {
                            Toast.makeText(this, "Error al optimizar foto de la galería", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al guardar foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarPerfilConConfirmacion() {
        if (currentUser == null) return

        val nombre = etNombre.text.toString().trim()
        val apellido = etApellido.text.toString().trim()
        val rol = etRol.text.toString().trim()
        val fincaNombre = etFincaNombre.text.toString().trim()
        val lugar = etLugar.text.toString().trim()

        // Validations
        if (nombre.isEmpty()) {
            tilNombre.error = "Ingrese su nombre"
            return
        } else tilNombre.error = null

        if (apellido.isEmpty()) {
            tilApellido.error = "Ingrese su apellido"
            return
        } else tilApellido.error = null

        if (rol.isEmpty()) {
            tilRol.error = "Seleccione un rol"
            return
        } else tilRol.error = null

        if (fincaNombre.isEmpty()) {
            tilFincaNombre.error = "Ingrese el nombre de la finca"
            return
        } else tilFincaNombre.error = null

        if (lugar.isEmpty()) {
            tilLugar.error = "Ingrese la ubicación"
            return
        } else tilLugar.error = null

        // Mostrar Dialog de Confirmación
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar Cambios")
        builder.setMessage("¿Está seguro de que desea guardar los cambios en su perfil?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            val updatedUser = currentUser!!.copy(
                nombre = nombre,
                apellido = apellido,
                rol = rol,
                nombreFinca = fincaNombre,
                lugar = lugar,
                profileImagePath = imagePathLocal
            )

            val result = authRepository.updateUser(updatedUser)
            if (result.isSuccess) {
                currentUser = authRepository.getCurrentUser() // Recargar datos locales
                Toast.makeText(this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show()
                setEditModeEnabled(false) // Regresar a modo visualización
            } else {
                Toast.makeText(this, "Error al actualizar perfil: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun destruirDatosPersonalesYFinca() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("⚠️ DESTRUCCIÓN DE DATOS")
        builder.setMessage("Esta acción es irreversible y eliminará de forma permanente:\n\n" +
                "• Su cuenta de usuario y contraseña\n" +
                "• Todos los datos registrados de la finca\n" +
                "• Todo el inventario e historial contable\n" +
                "• Todas las fotos de perfil y productos\n\n" +
                "¿Está completamente seguro de que desea proceder?")
        builder.setPositiveButton("ELIMINAR TODO") { dialog, _ ->
            // 1. Borrar SharedPreferences de Autenticación
            val authPrefs = pa.ac.utp.agrotrackapp.data.auth.AuthPrefsHelper.getAuthPrefs(this)
            authPrefs.edit().clear().apply()

            // 2. Borrar SharedPreferences de Inventario
            val inventarioPrefs = getSharedPreferences("GanaDEXInventarioPrefs", MODE_PRIVATE)
            inventarioPrefs.edit().clear().apply()

            // 3. Borrar todos los archivos locales del directorio de la app (fotos, etc.)
            val dir = filesDir
            dir.listFiles()?.forEach { file ->
                try {
                    file.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Toast.makeText(this, "Datos destruidos y sesión cerrada.", Toast.LENGTH_LONG).show()

            // 4. Redirigir al Login y limpiar pila de actividades
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        // Colorear el botón positivo de rojo para alertar peligro
        val alert = builder.create()
        alert.show()
        alert.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(android.graphics.Color.RED)
    }

    private fun mostrarAvisoPrivacidadTransparencia() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_aviso_privacidad, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        val btnAccept = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAcceptConsent)
        val btnDecline = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDeclineConsent)
        val tvText = dialogView.findViewById<TextView>(R.id.tvDialogText)

        tvText.text = "De conformidad con la Ley N° 81 de 2019 de la República de Panamá:\n\n" +
                "1. Responsable del Tratamiento: Los datos son recopilados por la aplicación AgroTrackApp de forma local.\n" +
                "2. Finalidad del Tratamiento: Gestionar la producción, ganado, inventario y alertas del usuario para fines de monitoreo personal en su finca.\n" +
                "3. Destinatarios: Los datos no se transfieren a terceros ni se almacenan en servidores externos sin su previa autorización.\n" +
                "4. Derechos ARCO:\n" +
                "• Acceso: Puede consultar sus historiales en las pantallas respectivas.\n" +
                "• Rectificación: Puede corregir sus datos mediante este formulario de perfil.\n" +
                "• Cancelación: Al pulsar 'Destrucción de Datos', se borrará toda su información sin dejar rastro.\n" +
                "• Oposición: Puede activar o desactivar el módulo de alertas e inicio biométrico desde los controles en esta pantalla.\n\n" +
                "Contacto: Para consultas o ejercicio de sus derechos, contacte al responsable de la aplicación en: soporte@agrotrack.pa"

        btnAccept.text = "Entendido"
        btnAccept.setOnClickListener {
            dialog.dismiss()
        }

        btnDecline.visibility = View.GONE

        dialog.show()
    }
    override fun onBackPressed() {
        if (isEditMode) {
            setEditModeEnabled(false)
        } else {
            super.onBackPressed()
        }
    }
}
