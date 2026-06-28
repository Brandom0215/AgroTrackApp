package pa.ac.utp.agrotrackapp.domain.model

data class CarneRecord(
    val numeroAnimal: String,          // Número de arete (Identificador)
    val raza: String,
    val fechaPesajeActual: String,
    val pesoActual: Double,
    val fechaPesajeAnterior: String,
    val pesoAnterior: Double,
    val pesoEntrada: Double,
    val gananciaTotal: Double,         // pesoActual - pesoAnterior
    val diasTranscurridos: Int,        // fechaPesajeActual - fechaPesajeAnterior
    val gdp: Double,                   // Ganancia Diaria de Peso (kg/día)
    val estadoSalud: String,
    val activo: Boolean = true         // true si está en engorde, false si fue "Sacado/Enviado"
)
