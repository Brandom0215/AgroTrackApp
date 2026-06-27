package pa.ac.utp.agrotrackapp

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Pantalla de Inicio de Sesión (Login)
 *
 * Captura las credenciales del usuario, las valida utilizando el repositorio desacoplado,
 * y permite redirigir al registro en caso de no tener una cuenta activa.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    private lateinit var tilUsuario: TextInputLayout
    private lateinit var tilContrasena: TextInputLayout
    private lateinit var etUsuario: TextInputEditText
    private lateinit var etContrasena: TextInputEditText
    private lateinit var btnIngresar: MaterialButton
    private lateinit var tvRegistrarse: TextView

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

        // Inicializamos el repositorio de datos (desacoplado)
        // SI MIGRARAS A BASE DE DATOS (Room/SQLite), solo necesitas inicializar aquí tu clase de base de datos
        authRepository = SharedPrefsAuthRepository(this)

        // Configuración de botones
        btnIngresar.setOnClickListener {
            realizarLogin()
        }

        tvRegistrarse.setOnClickListener {
            // Navega a la pantalla de creación de cuenta (Registro)
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Realiza la validación local de ingreso y consulta al repositorio.
     */
    private fun realizarLogin() {
        // Limpiamos errores anteriores
        tilUsuario.error = null
        tilContrasena.error = null

        val usuario = etUsuario.text.toString().trim()
        val contrasena = etContrasena.text.toString().trim()

        var hasError = false

        // Validación inicial para evitar romper datos o enviar campos vacíos
        if (usuario.isEmpty()) {
            tilUsuario.error = "Ingresa tu usuario o correo electrónico"
            hasError = true
        }

        if (contrasena.isEmpty()) {
            tilContrasena.error = "Ingresa tu contraseña"
            hasError = true
        }

        if (hasError) return

        // Consultamos al repositorio de autenticación
        val resultado = authRepository.loginUser(usuario, contrasena)

        resultado.fold(
            onSuccess = { user ->
                // Login exitoso: Mensaje de bienvenida
                Toast.makeText(this, "¡Bienvenido de vuelta, ${user.nombre}!", Toast.LENGTH_SHORT).show()
                
                // Redirige al Dashboard principal de la aplicación
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Cierra el LoginActivity para que no regrese atrás al Dashboard
            },
            onFailure = { error ->
                // Error de autenticación: Muestra el mensaje devuelto por el repositorio
                Toast.makeText(this, error.message ?: "Credenciales inválidas", Toast.LENGTH_LONG).show()
            }
        )
    }
}
