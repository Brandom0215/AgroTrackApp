package pa.ac.utp.agrotrackapp.domain.repository

import pa.ac.utp.agrotrackapp.domain.model.User

/**
 * Interfaz que define las operaciones de autenticación y gestión de usuarios.
 *
 * Esta abstracción permite que las pantallas de la aplicación (Login, Registro, Dashboard)
 * no dependan de cómo se guardan los datos.
 *
 * PARA REEMPLAZAR CON BASE DE DATOS (ej. Room, SQLite, Firebase o API REST):
 * 1. Crea una nueva clase que implemente esta interfaz (ej. `RoomAuthRepository`).
 * 2. Escribe la lógica para guardar/leer en la base de datos dentro de los métodos.
 * 3. En tus actividades, reemplaza la inicialización de `SharedPrefsAuthRepository` por tu nueva clase.
 */
interface AuthRepository {

    /**
     * Registra un nuevo usuario en el sistema.
     * Retorna [Result.success] si el registro es exitoso, o [Result.failure] con un mensaje de error.
     */
    fun registerUser(user: User): Result<Unit>

    /**
     * Valida las credenciales de un usuario.
     * Retorna el [User] si las credenciales coinciden, o [Result.failure] si son incorrectas.
     */
    fun loginUser(usuario: String, contrasena: String): Result<User>

    /**
     * Obtiene el usuario que tiene la sesión activa actualmente.
     * Retorna null si no hay sesión iniciada.
     */
    fun getCurrentUser(): User?

    /**
     * Cierra la sesión activa actual en la aplicación.
     */
    fun logout()
}
