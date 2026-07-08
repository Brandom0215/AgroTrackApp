package pa.ac.utp.agrotrackapp.data.auth

import android.content.Context
import android.content.SharedPreferences
import pa.ac.utp.agrotrackapp.domain.model.User
import pa.ac.utp.agrotrackapp.domain.repository.AuthRepository

class SharedPrefsAuthRepository(context: Context) : AuthRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("GanaDEXAuthPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PREFIX_USER = "user_"
        private const val KEY_ACTIVE_USER = "active_user_username"
        private const val KEY_LAST_USERNAME = "last_logged_in_username"
        
        private const val FIELD_NOMBRE = "_nombre"
        private const val FIELD_APELLIDO = "_apellido"
        private const val FIELD_PASSWORD = "_password"
        private const val FIELD_FINCA_NOMBRE = "_finca_nombre"
        private const val FIELD_FINCA_PASSWORD = "_finca_password"
        private const val FIELD_LUGAR = "_lugar"
        private const val FIELD_ROL = "_rol"
        private const val FIELD_PROFILE_IMAGE = "_profile_image"
    }

    override fun registerUser(user: User): Result<Unit> {
        return try {
            val username = user.usuario.trim().lowercase()
            
            if (sharedPreferences.contains("$KEY_PREFIX_USER$username$FIELD_PASSWORD")) {
                return Result.failure(Exception("El nombre de usuario o correo ya está registrado"))
            }

            persistUserData(username, user)
            // Guardamos como último usuario registrado/logueado
            sharedPreferences.edit().putString(KEY_LAST_USERNAME, username).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

            val user = loadUser(username, savedPassword)

            // Marcamos a este usuario como el usuario activo de la sesión y último usuario
            sharedPreferences.edit().apply {
                putString(KEY_ACTIVE_USER, username)
                putString(KEY_LAST_USERNAME, username)
                apply()
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        val activeUsername = sharedPreferences.getString(KEY_ACTIVE_USER, null) ?: return null
        
        return try {
            val password = sharedPreferences.getString("$KEY_PREFIX_USER$activeUsername$FIELD_PASSWORD", null) ?: return null
            loadUser(activeUsername, password)
        } catch (e: Exception) {
            null
        }
    }

    override fun logout() {
        sharedPreferences.edit().remove(KEY_ACTIVE_USER).apply()
    }

    override fun updateUser(user: User): Result<Unit> {
        return try {
            val username = user.usuario.trim().lowercase()
            if (!sharedPreferences.contains("$KEY_PREFIX_USER$username$FIELD_PASSWORD")) {
                return Result.failure(Exception("El usuario no existe"))
            }

            persistUserData(username, user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLastUsername(): String? {
        return sharedPreferences.getString(KEY_LAST_USERNAME, null)
    }

    override fun loginWithUsername(username: String): Result<User> {
        return try {
            val userKey = username.trim().lowercase()
            val savedPassword = sharedPreferences.getString("$KEY_PREFIX_USER$userKey$FIELD_PASSWORD", null)
                ?: return Result.failure(Exception("El usuario no tiene una cuenta configurada"))

            val user = loadUser(userKey, savedPassword)

            sharedPreferences.edit().apply {
                putString(KEY_ACTIVE_USER, userKey)
                putString(KEY_LAST_USERNAME, userKey)
                apply()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun persistUserData(username: String, user: User) {
        sharedPreferences.edit().apply {
            putString("$KEY_PREFIX_USER$username$FIELD_NOMBRE", user.nombre)
            putString("$KEY_PREFIX_USER$username$FIELD_APELLIDO", user.apellido)
            putString("$KEY_PREFIX_USER$username$FIELD_PASSWORD", user.contrasena)
            putString("$KEY_PREFIX_USER$username$FIELD_FINCA_NOMBRE", user.nombreFinca)
            putString("$KEY_PREFIX_USER$username$FIELD_FINCA_PASSWORD", user.contrasenaFinca)
            putString("$KEY_PREFIX_USER$username$FIELD_LUGAR", user.lugar)
            putString("$KEY_PREFIX_USER$username$FIELD_ROL", user.rol)
            putString("$KEY_PREFIX_USER$username$FIELD_PROFILE_IMAGE", user.profileImagePath)
            apply()
        }
    }

    private fun loadUser(username: String, passwordState: String): User {
        return User(
            nombre = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_NOMBRE", "") ?: "",
            apellido = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_APELLIDO", "") ?: "",
            usuario = username,
            contrasena = passwordState,
            nombreFinca = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_FINCA_NOMBRE", "") ?: "",
            contrasenaFinca = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_FINCA_PASSWORD", "") ?: "",
            lugar = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_LUGAR", "") ?: "",
            rol = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_ROL", "Productor") ?: "Productor",
            profileImagePath = sharedPreferences.getString("$KEY_PREFIX_USER$username$FIELD_PROFILE_IMAGE", null)
        )
    }
}
