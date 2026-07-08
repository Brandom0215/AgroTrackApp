package pa.ac.utp.agrotrackapp.domain.repository

import pa.ac.utp.agrotrackapp.domain.model.InventarioItem

interface InventarioRepository {
    fun getItems(): List<InventarioItem>
    fun getItem(id: String): InventarioItem?
    fun saveItem(item: InventarioItem): Result<Unit>
    fun updateItem(item: InventarioItem): Result<Unit>
    fun deleteItem(id: String): Result<Unit>
}
