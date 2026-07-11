package pa.ac.utp.agrotrackapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.auth.SqliteAuthRepository
import pa.ac.utp.agrotrackapp.domain.repository.AuthRepository
import pa.ac.utp.agrotrackapp.services.BiometricService
import pa.ac.utp.agrotrackapp.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var biometricService: BiometricService

    private lateinit var tilUsuario: TextInputLayout
    private lateinit var tilContrasena: TextInputLayout
    private lateinit var etUsuario: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var btnIngresar: MaterialButton
    private lateinit var tvRegistrarse: TextView
    private lateinit var tvRecuperarContrasena: TextView
    private lateinit var btnBiometria: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        // Inicializamos componentes de la vista
        tilUsuario = findViewById(R.id.tilUsuario)
        tilContrasena = findViewById(R.id.tilContrasena)
        etUsuario = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)
        btnIngresar = findViewById(R.id.btnIngresar)
        tvRegistrarse = findViewById(R.id.tvRegistrarse)
        tvRecuperarContrasena = findViewById(R.id.tvRecuperarContrasena)
        btnBiometria = findViewById(R.id.btnBiometria)

        authRepository = SqliteAuthRepository(this)
        biometricService = BiometricService(this)

        verificarConsentimientoPrivacidad()

        // Configuración de botones
        btnIngresar.setOnClickListener {
            realizarLogin()
        }

        tvRegistrarse.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvRecuperarContrasena.setOnClickListener {
            mostrarDialogoRecuperacion()
        }

        // Configurar detección biométrica
        if (biometricService.isBiometricAvailable()) {
            btnBiometria.visibility = android.view.View.VISIBLE
            btnBiometria.setOnClickListener {
                realizarLoginBiometrico()
            }
        } else {
            btnBiometria.visibility = android.view.View.GONE
        }
    }

    private fun realizarLogin() {
        tilUsuario.error = null
        tilContrasena.error = null

        val usuario = etUsuario.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        var hasError = false

        if (usuario.isEmpty()) {
            tilUsuario.error = "Ingresa tu usuario o correo electrónico"
            hasError = true
        }

        if (contrasena.isEmpty()) {
            tilContrasena.error = "Ingresa tu contraseña"
            hasError = true
        }

        if (hasError) return

        val resultado = authRepository.loginUser(usuario, contrasena)

        resultado.fold(
            onSuccess = { user ->
                Toast.makeText(this, "¡Bienvenido de vuelta, ${user.nombre}!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            },
            onFailure = { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun realizarLoginBiometrico() {
        val lastUsername = authRepository.getLastUsername()
        if (lastUsername.isNullOrEmpty()) {
            Toast.makeText(this, "Inicie sesión con contraseña al menos una vez antes de usar huella dactilar.", Toast.LENGTH_LONG).show()
            return
        }

        val prefs = getSharedPreferences("GanaDEXAuthPrefs", MODE_PRIVATE)
        val biometricEnabled = prefs.getBoolean("biometric_enabled", true)
        if (!biometricEnabled) {
            Toast.makeText(this, "La autenticación biométrica está desactivada en la configuración.", Toast.LENGTH_LONG).show()
            return
        }

        biometricService.showBiometricPrompt(
            activity = this,
            title = "Inicio de Sesión",
            subtitle = "Acceso biométrico para: $lastUsername",
            description = "Coloque su huella en el lector para ingresar",
            onSuccess = {
                val resultado = authRepository.loginWithUsername(lastUsername)
                resultado.fold(
                    onSuccess = { user ->
                        Toast.makeText(this, "¡Autenticación biométrica exitosa! Bienvenido, ${user.nombre}.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onFailure = { error ->
                        Toast.makeText(this, "Error de inicio biométrico: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onError = { errorString ->
                Toast.makeText(this, "Biometría cancelada o fallida: $errorString", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun mostrarDialogoRecuperacion() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Recuperar Contraseña")
        builder.setMessage("Ingrese su usuario o correo electrónico registrado:")

        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.leftMargin = (24 * resources.displayMetrics.density).toInt()
        params.rightMargin = (24 * resources.displayMetrics.density).toInt()
        input.layoutParams = params
        container.addView(input)
        builder.setView(container)

        builder.setPositiveButton("Recuperar") { dialog, _ ->
            val username = input.text.toString().trim().lowercase()
            if (username.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese un usuario", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            
            val prefs = getSharedPreferences("GanaDEXAuthPrefs", MODE_PRIVATE)
            val savedPassword = prefs.getString("user_${username}_password", null)
            if (savedPassword != null) {
                AlertDialog.Builder(this)
                    .setTitle("Contraseña Encontrada")
                    .setMessage("Su contraseña registrada para el usuario '$username' es:\n\n$savedPassword")
                    .setPositiveButton("Aceptar") { innerDialog, _ -> innerDialog.dismiss() }
                    .show()
            } else {
                Toast.makeText(this, "El usuario no existe en el sistema", Toast.LENGTH_LONG).show()
            }
            dialog.dismiss()
        }
        
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }

    private fun verificarConsentimientoPrivacidad() {
        val prefs = getSharedPreferences("GanaDEXAuthPrefs", MODE_PRIVATE)
        val consentido = prefs.getBoolean("privacy_policy_accepted", false)
        if (!consentido) {
            mostrarDialogoPrivacidad(obligatorio = true)
        }
    }

    private fun mostrarDialogoPrivacidad(obligatorio: Boolean) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_aviso_privacidad, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(!obligatorio)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        val btnAccept = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnAcceptConsent)
        val btnDecline = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnDeclineConsent)

        if (obligatorio) {
            btnDecline.text = "Salir"
        } else {
            btnDecline.text = "Cerrar"
        }

        btnAccept.setOnClickListener {
            val prefs = getSharedPreferences("GanaDEXAuthPrefs", MODE_PRIVATE)
            prefs.edit().putBoolean("privacy_policy_accepted", true).apply()
            dialog.dismiss()
        }

        btnDecline.setOnClickListener {
            if (obligatorio) {
                finishAffinity()
            } else {
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}
