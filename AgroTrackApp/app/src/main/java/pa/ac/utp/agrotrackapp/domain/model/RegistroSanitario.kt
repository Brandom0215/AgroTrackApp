package pa.ac.utp.agrotrackapp.domain.model

data class RegistroSanitario(
    val id: String,
    val identificador: String,
    val alcance: String,        // "individual" o "masivo"
    val categoria: String,      // "Vacunación", "Tratamiento", "Mastitis"
    val detalle: String,        // Nombre de vacuna, cuartos, o nombre de enfermedad
    val producto: String,
    val dosis: String,
    val fecha: String,
    val proximaDosis: String,
    val veterinario: String,
    val notas: String,
    val estado: String,         // "aplicado" o "programado"
    val grupoId: String = ""    // UUID compartido por todas las dosis del mismo plan
)
