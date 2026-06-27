package pa.ac.utp.agrotrackapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida (Splash Screen)
 *
 * Muestra el logotipo de la aplicación de manera minimalista y determina el flujo de navegación
 * inicial comprobando el estado de la sesión activa en el repositorio de autenticación.
 */
class Splash : AppCompatActivity() {

    // Repositorio de autenticación (fácilmente intercambiable por base de datos o API)
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()

        // Inicializamos el repositorio.
        // SI MIGRARAS A ROOM/DATABASE: aquí instanciarías tu RoomAuthRepository(this).
        authRepository = SharedPrefsAuthRepository(this)

        // Lanzamos una corrutina para controlar el tiempo del Splash (2 segundos)
        lifecycleScope.launch {
            delay(2000)
            
            // Verificamos si existe una sesión activa mediante el repositorio
            val currentUser = authRepository.getCurrentUser()
            
            if (currentUser != null) {
                // Usuario ya autenticado -> Redirige al Dashboard principal
                startActivity(Intent(this@Splash, MainActivity::class.java))
            } else {
                // Sin sesión activa -> Redirige a la pantalla de Inicio de Sesión (Login)
                startActivity(Intent(this@Splash, LoginActivity::class.java))
            }
            
            // Finalizamos el Splash para que el usuario no regrese aquí al presionar atrás
            finish()
        }
    }
}