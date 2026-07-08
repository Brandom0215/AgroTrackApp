package pa.ac.utp.agrotrackapp.domain.repository

import pa.ac.utp.agrotrackapp.domain.model.User

interface AuthRepository {
    fun registerUser(user: User): Result<Unit>
    fun loginUser(usuario: String, contrasena: String): Result<User>
    fun getCurrentUser(): User?
    fun logout()
    fun updateUser(user: User): Result<Unit>
    fun getLastUsername(): String?
    fun loginWithUsername(username: String): Result<User>
}
