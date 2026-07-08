package pa.ac.utp.agrotrackapp.domain.model

data class InventarioItem(
    val id: String,
    val nombre: String,
    val tipo: String, // "Herramienta", "Alimento", "Medicina", "Otro"
    val tipoOtro: String? = null,
    val fotoPath: String? = null,
    val stock: Double,
    val limiteNotificacion: Double? = null,
    val unidad: String,
    val costo: Double,
    val fechaRegistro: String
)
