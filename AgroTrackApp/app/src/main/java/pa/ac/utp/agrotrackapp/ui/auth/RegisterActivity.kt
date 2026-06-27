package pa.ac.utp.agrotrackapp.ui.auth

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.User
import pa.ac.utp.agrotrackapp.domain.repository.AuthRepository
import pa.ac.utp.agrotrackapp.data.auth.SharedPrefsAuthRepository
import pa.ac.utp.agrotrackapp.utils.ValidationLens

/**
 * Pantalla de Registro de Usuario (Crear Cuenta)
 *
 * Administra el formulario de 8 campos obligatorios, aplica validaciones en tiempo real
 * y guarda la información registrada a través del repositorio desacoplado.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    // Referencias a los contenedores TextInputLayout para mostrar errores
    private lateinit var tilNombre: TextInputLayout
    private lateinit var tilApellido: TextInputLayout
    private lateinit var tilUsuario: TextInputLayout
    private lateinit var tilContrasena: TextInputLayout
    private lateinit var tilConfirmContrasena: TextInputLayout
    private lateinit var tilFincaNombre: TextInputLayout
    private lateinit var tilFincaContrasena: TextInputLayout
    private lateinit var tilLugar: TextInputLayout

    // Referencias a los campos de texto
    private lateinit var etNombre: TextInputEditText
    private lateinit var etApellido: TextInputEditText
    private lateinit var etUsuario: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var etConfirmContrasena: TextInputEditText
    private lateinit var etFincaNombre: TextInputEditText
    private lateinit var etFincaContrasena: TextInputEditText
    private lateinit var etLugar: TextInputEditText

    private lateinit var btnRegistrar: MaterialButton
    private lateinit var tvVolverLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        // Inicializamos el repositorio de datos (desacoplado)
        authRepository = SharedPrefsAuthRepository(this)

        // Inicializamos las vistas
        inicializarVistas()

        btnRegistrar.setOnClickListener {
            ejecutarRegistro()
        }

        tvVolverLogin.setOnClickListener {
            // Regresa a la pantalla de Login
            finish()
        }
    }

    private fun inicializarVistas() {
        tilNombre = findViewById(R.id.tilNombre)
        tilApellido = findViewById(R.id.tilApellido)
        tilUsuario = findViewById(R.id.tilUsuario)
        tilContrasena = findViewById(R.id.tilContrasena)
        tilConfirmContrasena = findViewById(R.id.tilConfirmContrasena)
        tilFincaNombre = findViewById(R.id.tilFincaNombre)
        tilFincaContrasena = findViewById(R.id.tilFincaContrasena)
        tilLugar = findViewById(R.id.tilLugar)

        etNombre = findViewById(R.id.etNombre)
        etApellido = findViewById(R.id.etApellido)
        etUsuario = findViewById(R.id.etUsuario)
        etContrasena = findViewById(R.id.etContrasena)
        etConfirmContrasena = findViewById(R.id.etConfirmContrasena)
        etFincaNombre = findViewById(R.id.etFincaNombre)
        etFincaContrasena = findViewById(R.id.etFincaContrasena)
        etLugar = findViewById(R.id.etLugar)

        btnRegistrar = findViewById(R.id.btnRegistrar)
        tvVolverLogin = findViewById(R.id.tvVolverLogin)
    }

    private fun limpiarErrores() {
        tilNombre.error = null
        tilApellido.error = null
        tilUsuario.error = null
        tilContrasena.error = null
        tilConfirmContrasena.error = null
        tilFincaNombre.error = null
        tilFincaContrasena.error = null
        tilLugar.error = null
    }

    /**
     * Ejecuta el flujo de validación de 8 campos mediante el ValidationLens y guarda al usuario.
     */
    private fun ejecutarRegistro() {
        limpiarErrores()

        val nombre = etNombre.text.toString()
        val apellido = etApellido.text.toString()
        val usuario = etUsuario.text.toString()
        val contrasena = etContrasena.text.toString()
        val confirmContrasena = etConfirmContrasena.text.toString()
        val fincaNombre = etFincaNombre.text.toString()
        val fincaContrasena = etFincaContrasena.text.toString()
        val lugar = etLugar.text.toString()

        // Ejecutamos la validación usando los Lentes de Validación
        val validationResult = ValidationLens.validateRegistrationForm(
            nombre = nombre,
            apellido = apellido,
            usuario = usuario,
            contrasena = contrasena,
            confirmarContrasena = confirmContrasena,
            nombreFinca = fincaNombre,
            contrasenaFinca = fincaContrasena,
            lugar = lugar
        )

        if (!validationResult.isValid) {
            // Mapeamos los errores a los TextInputLayout específicos
            validationResult.errors.forEach { (field, errorMessage) ->
                when (field) {
                    ValidationLens.FIELD_NOMBRE -> tilNombre.error = errorMessage
                    ValidationLens.FIELD_APELLIDO -> tilApellido.error = errorMessage
                    ValidationLens.FIELD_USUARIO -> tilUsuario.error = errorMessage
                    ValidationLens.FIELD_CONTRASENA -> tilContrasena.error = errorMessage
                    ValidationLens.FIELD_CONFIRM_CONTRASENA -> tilConfirmContrasena.error = errorMessage
                    ValidationLens.FIELD_FINCA_NOMBRE -> tilFincaNombre.error = errorMessage
                    ValidationLens.FIELD_FINCA_CONTRASENA -> tilFincaContrasena.error = errorMessage
                    ValidationLens.FIELD_LUGAR -> tilLugar.error = errorMessage
                }
            }
            Toast.makeText(this, "Por favor corrige los campos con errores", Toast.LENGTH_SHORT).show()
            return
        }

        // Si la validación es exitosa, instanciamos el objeto User
        val newUser = User(
            nombre = nombre.trim(),
            apellido = apellido.trim(),
            usuario = usuario.trim().lowercase(),
            contrasena = contrasena.trim(),
            nombreFinca = fincaNombre.trim(),
            contrasenaFinca = fincaContrasena.trim(),
            lugar = lugar.trim()
        )

        // Intentamos registrar al usuario en el repositorio
        val result = authRepository.registerUser(newUser)

        result.fold(
            onSuccess = {
                Toast.makeText(this, "¡Cuenta creada exitosamente! Inicia sesión para entrar.", Toast.LENGTH_LONG).show()
                finish() // Regresa a la pantalla de Login
            },
            onFailure = { error ->
                // Ocurrió un error (por ejemplo, usuario ya registrado)
                tilUsuario.error = error.message
                Toast.makeText(this, error.message ?: "Error al registrar la cuenta", Toast.LENGTH_LONG).show()
            }
        )
    }
}
