package pa.ac.utp.agrotrackapp.domain.model

data class Alerta(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val tipo: TipoAlerta,
    val fecha: String,
    val prioridad: PrioridadAlerta = PrioridadAlerta.MEDIA,
    val isDismissed: Boolean = false,
    val destinationId: Int? = null,
    val referenceId: String? = null,
    val fechaProgramada: String? = null // Fecha de próxima dosis (solo para SANIDAD_PENDIENTE)
)

enum class TipoAlerta {
    STOCK_MINIMO,
    MORTALIDAD_ALTA,
    PESAJE_PENDIENTE,
    RECORDATORIO,
    SANIDAD_PENDIENTE
}

enum class PrioridadAlerta {
    ALTA,
    MEDIA,
    BAJA
}
