package pa.ac.utp.agrotrackapp

import android.content.Context
import android.content.SharedPreferences

/**
 * Implementación de [AuthRepository] utilizando SharedPreferences como persistencia local.
 * Esta clase es perfecta para desarrollo inicial ya que no requiere configurar una base de datos.
 *
 * PARA REEMPLAZAR CON BASE DE DATOS REAL (Room, SQLite, API):
 * - Sustituye el uso de `sharedPreferences` en esta clase por llamadas a tu base de datos (por ejemplo, `userDao.insert(user)`).
 * - Si utilizas Room, recuerda ejecutar las operaciones de base de datos en un hilo secundario (con Coroutines o Threads).
 */
class SharedPrefsAuthRepository(context: Context) : AuthRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("AgroTrackAuthPrefs", Context.MODE_PRIVATE)

    // Prefijos de claves para evitar conflictos en SharedPreferences
    companion object {
        private const val KEY_PREFIX_USER = "user_"
        private const val KEY_ACTIVE_USER = "active_user_username"
        
        // Campos individuales
        private const val FIELD_NOMBRE = "_nombre"
        private const val FIELD_APELLIDO = "_apellido"
        private const val FIELD_PASSWORD = "_password"
        private const val FIELD_FINCA_NOMBRE = "_finca_nombre"
        private const val FIELD_FINCA_PASSWORD = "_finca_password"
        private const val FIELD_LUGAR = "_lugar"
    }

    /**
     * Registra un nuevo usuario guardando cada campo individualmente en SharedPreferences.
     * En una Base de Datos real, aquí harías:
     * `db.userDao().insert(UserEntity(nombre, apellido, ...))`
     */
    override fun registerUser(user: User): Result<Unit> {
        return try {
            val username = user.usuario.trim().lowercase()
            
            // Verificamos si el usuario ya existe para evitar duplicados
            if (sharedPreferences.contains("$KEY_PREFIX_USER$username$FIELD_PASSWORD")) {
                return Result.failure(Exception("El nombre de usuario o correo ya está registrado"))
            }

            // Guardamos los datos del usuario en SharedPreferences
            sharedPreferences.edit().apply {
                putString("$KEY_PREFIX_USER$username$FIELD_NOMBRE", user.nombre)
                putString("$KEY_PREFIX_USER$username$FIELD_APELLIDO", user.apellido)
                putString("$KEY_PREFIX_USER$username$FIELD_PASSWORD", user.contrasena)
                putString("$KEY_PREFIX_USER$username$FIELD_FINCA_NOMBRE", user.nombreFinca)
                putString("$KEY_PREFIX_USER$username$FIELD_FINCA_PASSWORD", user.contrasenaFinca)
                putString("$KEY_PREFIX_USER$username$FIELD_LUGAR", user.lugar)
                apply() // Guarda en segundo plano de manera asíncrona
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Valida si el usuario existe y si la contraseña coincide.
     * En una Base de Datos real, aquí harías:
     * `val user = db.userDao().getUserByCredentials(usuario, contrasena)`
     */
    override fun loginUser(usuario: String, contrasena: String): Result<User> {
        return try {
            val username = usuario.trim().lowercase()
            val savedPassword = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_PASSWORD", null)

            if (savedPassword == null) {
                return Result.failure(Exception("El usuario ingresado no existe"))
            }

            if (savedPassword != contrasena) {
                return Result.failure(Exception("La contraseña ingresada es incorrecta"))
            }

            // Recuperamos el objeto User completo desde SharedPreferences
            val user = User(
                nombre = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_NOMBRE", "") ?: "",
                apellido = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_APELLIDO", "") ?: "",
                usuario = username,
                contrasena = savedPassword,
                nombreFinca = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_FINCA_NOMBRE", "") ?: "",
                contrasenaFinca = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_FINCA_PASSWORD", "") ?: "",
                lugar = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_LUGAR", "") ?: ""
            )

            // Marcamos a este usuario como el usuario activo de la sesión
            sharedPreferences.edit().putString(KEY_ACTIVE_USER, username).apply()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el usuario activo actual.
     * En una Base de Datos real, aquí leerías de la tabla de sesión activa o una clave local rápida.
     */
    override fun getCurrentUser(): User? {
        val activeUsername = sharedPreferences.getString(KEY_ACTIVE_USER, null) ?: return null
        
        return try {
            val password = sharedPreferences.getString("$KEY_PREFIX_USER$activeUsername$FIELD_PASSWORD", null) ?: return null
            
            User(
                nombre = sharedPreferences.getString("$KEY_PREFIX_USER$activeUsername$FIELD_NOMBRE", "") ?: "",
                apellido = sharedPreferences.getString("$KEY_PREFIX_USER$activeUsername$FIELD_APELLIDO", "") ?: "",
                usuario = activeUsername,
                contrasena = password,
                nombreFinca = sharedPreferences.getString("$KEY_PREFIX_USER$activeUsername$FIELD_FINCA_NOMBRE", "") ?: "",
                contrasenaFinca = sharedPreferences.getString("$KEY_PREFIX_USER$activeUsername$FIELD_FINCA_PASSWORD", "") ?: "",
                lugar = sharedPreferences.getString("$KEY_PREFIX_USER$activeUsername$FIELD_LUGAR", "") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Cierra la sesión activa actual borrando el registro de usuario activo de SharedPreferences.
     */
    override fun logout() {
        sharedPreferences.edit().remove(KEY_ACTIVE_USER).apply()
    }
}
