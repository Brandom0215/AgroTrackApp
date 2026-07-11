package pa.ac.utp.agrotrackapp.domain.model

data class Alerta(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val tipo: TipoAlerta,
    val fecha: String,
    val prioridad: PrioridadAlerta = PrioridadAlerta.MEDIA,
    val isDismissed: Boolean = false,
    val destinationId: Int? = null, // Resource ID of the menu item to navigate to
    val referenceId: String? = null // Origin reference id, e.g. "stock_1"
)

enum class TipoAlerta {
    STOCK_MINIMO,
    MORTALIDAD_ALTA,
    PESAJE_PENDIENTE,
    RECORDATORIO
}

enum class PrioridadAlerta {
    ALTA,
    MEDIA,
    BAJA
}
