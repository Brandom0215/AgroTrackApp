package pa.ac.utp.agrotrackapp.domain.model

data class Transaccion(
    val id: String,
    val tipo: String,              // "venta", "consumo"
    val productoId: String,
    val productoNombre: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val costoUnitario: Double,
    val fecha: String,             // "dd/MM/yyyy HH:mm"
    val detalles: String? = null
)
