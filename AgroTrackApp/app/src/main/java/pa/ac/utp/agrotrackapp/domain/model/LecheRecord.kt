package pa.ac.utp.agrotrackapp.domain.model

data class LecheRecord(
    val numeroAnimal: String,          // Número de arete (Identificador)
    val fechaRegistro: String,
    val turno: String,                 // "AM", "PM" o "Semanal"
    val litros: Double,
    val fechaUltimoParto: String,
    val lactancias: Int,
    val del: Int,                      // Días en Lactancia (fechaRegistro - fechaUltimoParto)
    val promedioDiario: Double,
    val activo: Boolean = true         // true si está en ordeño, false si fue "Sacada/Enviada"
)
