package pa.ac.utp.agrotrackapp.data.auth

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import pa.ac.utp.agrotrackapp.data.database.DatabaseHelper
import pa.ac.utp.agrotrackapp.domain.model.User
import pa.ac.utp.agrotrackapp.domain.repository.AuthRepository

class SqliteAuthRepository(context: Context) : AuthRepository {

    private val dbHelper = DatabaseHelper(context)
    private val sessionPrefs: SharedPreferences =
        context.getSharedPreferences("GanaDEXAuthPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACTIVE_USER = "active_user_username"
        private const val KEY_LAST_USERNAME = "last_logged_in_username"
    }

    override fun registerUser(user: User): Result<Unit> {
        return try {
            val username = user.usuario.trim().lowercase()
            
            if (userExists(username)) {
                return Result.failure(Exception("El nombre de usuario o correo ya está registrado"))
            }

            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_USER_USUARIO, username)
                put(DatabaseHelper.COL_USER_NOMBRE, user.nombre)
                put(DatabaseHelper.COL_USER_APELLIDO, user.apellido)
                put(DatabaseHelper.COL_USER_CONTRASENA, user.contrasena)
                put(DatabaseHelper.COL_USER_NOMBRE_FINCA, user.nombreFinca)
                put(DatabaseHelper.COL_USER_CONTRASENA_FINCA, user.contrasenaFinca)
                put(DatabaseHelper.COL_USER_LUGAR, user.lugar)
                put(DatabaseHelper.COL_USER_ROL, user.rol)
                put(DatabaseHelper.COL_USER_PROFILE_IMAGE, user.profileImagePath)
            }

            db.insertOrThrow(DatabaseHelper.TABLE_USUARIOS, null, values)
            
            // Guardamos como último usuario registrado/logueado
            sessionPrefs.edit().putString(KEY_LAST_USERNAME, username).apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun loginUser(usuario: String, contrasena: String): Result<User> {
        return try {
            val username = usuario.trim().lowercase()
            val user = getUserFromDb(username) ?: return Result.failure(Exception("El usuario ingresado no existe"))

            if (user.contrasena != contrasena) {
                return Result.failure(Exception("La contraseña ingresada es incorrecta"))
            }

            // Marcamos a este usuario como el usuario activo de la sesión y último usuario
            sessionPrefs.edit().apply {
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
        val activeUsername = sessionPrefs.getString(KEY_ACTIVE_USER, null) ?: return null
        return getUserFromDb(activeUsername)
    }

    override fun logout() {
        sessionPrefs.edit().remove(KEY_ACTIVE_USER).apply()
    }

    override fun updateUser(user: User): Result<Unit> {
        return try {
            val username = user.usuario.trim().lowercase()
            if (!userExists(username)) {
                return Result.failure(Exception("El usuario no existe"))
            }

            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_USER_NOMBRE, user.nombre)
                put(DatabaseHelper.COL_USER_APELLIDO, user.apellido)
                put(DatabaseHelper.COL_USER_CONTRASENA, user.contrasena)
                put(DatabaseHelper.COL_USER_NOMBRE_FINCA, user.nombreFinca)
                put(DatabaseHelper.COL_USER_CONTRASENA_FINCA, user.contrasenaFinca)
                put(DatabaseHelper.COL_USER_LUGAR, user.lugar)
                put(DatabaseHelper.COL_USER_ROL, user.rol)
                put(DatabaseHelper.COL_USER_PROFILE_IMAGE, user.profileImagePath)
            }

            db.update(
                DatabaseHelper.TABLE_USUARIOS,
                values,
                "${DatabaseHelper.COL_USER_USUARIO} = ?",
                arrayOf(username)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLastUsername(): String? {
        return sessionPrefs.getString(KEY_LAST_USERNAME, null)
    }

    override fun loginWithUsername(username: String): Result<User> {
        return try {
            val userKey = username.trim().lowercase()
            val user = getUserFromDb(userKey) ?: return Result.failure(Exception("El usuario no tiene una cuenta configurada"))

            sessionPrefs.edit().apply {
                putString(KEY_ACTIVE_USER, userKey)
                putString(KEY_LAST_USERNAME, userKey)
                apply()
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun userExists(username: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            arrayOf(DatabaseHelper.COL_USER_USUARIO),
            "${DatabaseHelper.COL_USER_USUARIO} = ?",
            arrayOf(username),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    private fun getUserFromDb(username: String): User? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_USUARIOS,
            null,
            "${DatabaseHelper.COL_USER_USUARIO} = ?",
            arrayOf(username),
            null, null, null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NOMBRE)),
                apellido = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_APELLIDO)),
                usuario = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USUARIO)),
                contrasena = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_CONTRASENA)),
                nombreFinca = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NOMBRE_FINCA)),
                contrasenaFinca = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_CONTRASENA_FINCA)),
                lugar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_LUGAR)),
                rol = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ROL)),
                profileImagePath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PROFILE_IMAGE))
            )
        }
        cursor.close()
        return user
    }
}
