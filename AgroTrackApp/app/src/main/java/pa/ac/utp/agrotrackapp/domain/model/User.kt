package pa.ac.utp.agrotrackapp.domain.model

/**
 * Clase de datos que representa un Usuario de GanaDEX.
 * Contiene la información requerida tanto para la cuenta personal como para la finca.
 */
data class User(
    val nombre: String,
    val apellido: String,
    val usuario: String,           // Nombre de usuario o correo electrónico
    val contrasena: String,        // Contraseña
    val nombreFinca: String,       // Nombre de la finca
    val contrasenaFinca: String,   // Contraseña o código de acceso de la finca
    val lugar: String,             // Ubicación / Lugar de la finca
    val rol: String = "Productor",  // Rol activo (ej. Administrador, Productor, Veterinario)
    val profileImagePath: String? = null // Ruta de la imagen de perfil local
)
