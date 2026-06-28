package pa.ac.utp.agrotrackapp.domain.model

data class MortalidadRecord(
    val numeroAnimal: String,          // Número de arete (Identificador)
    val causa: String,                 // Causa de muerte (Diarrea, Digestivo, etc.)
    val fechaMuerte: String,
    val detalles: String
)
