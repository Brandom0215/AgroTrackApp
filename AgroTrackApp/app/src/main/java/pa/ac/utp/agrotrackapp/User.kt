package pa.ac.utp.agrotrackapp

/**
 * Clase de datos que representa un Usuario de AgroTrack.
 * Contiene la información requerida tanto para la cuenta personal como para la finca.
 *
 * Si en el futuro migras a una base de datos local (como Room), puedes convertir fácilmente
 * esta clase en una Entidad de Room añadiendo la anotación @Entity y definiendo una clave primaria (como @PrimaryKey).
 */
data class User(
    val nombre: String,
    val apellido: String,
    val usuario: String,           // Nombre de usuario o correo electrónico
    val contrasena: String,        // Contraseña combinada de letras y números (mínimo 8 caracteres)
    val nombreFinca: String,       // Nombre de la finca
    val contrasenaFinca: String,   // Contraseña o código de acceso de la finca
    val lugar: String              // Ubicación / Lugar de la finca
)
