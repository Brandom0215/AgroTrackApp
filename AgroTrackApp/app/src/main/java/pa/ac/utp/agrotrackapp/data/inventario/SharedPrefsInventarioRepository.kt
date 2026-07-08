package pa.ac.utp.agrotrackapp.data.inventario

import android.content.Context
import android.content.SharedPreferences
import pa.ac.utp.agrotrackapp.domain.model.InventarioItem
import pa.ac.utp.agrotrackapp.domain.repository.InventarioRepository

class SharedPrefsInventarioRepository(context: Context) : InventarioRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("GanaDEXInventarioPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ITEMS_LIST = "items_list"
        private const val PREFIX_ITEM = "item_"

        private const val SUFFIX_NOMBRE = "_nombre"
        private const val SUFFIX_TIPO = "_tipo"
        private const val SUFFIX_TIPO_OTRO = "_tipo_otro"
        private const val SUFFIX_FOTO = "_foto"
        private const val SUFFIX_STOCK = "_stock"
        private const val SUFFIX_LIMITE = "_limite"
        private const val SUFFIX_UNIDAD = "_unidad"
        private const val SUFFIX_COSTO = "_costo"
        private const val SUFFIX_FECHA = "_fecha"
    }

    init {
        // Precargar datos iniciales si la persistencia está vacía
        val ids = getIdsSet()
        if (ids.isEmpty()) {
            val initialItems = listOf(
                InventarioItem("1", "LEVADURA", "Otro", "Levadura para ganado", null, 180.0, null, "Litros", 45.0, "01/07/2026 09:30"),
                InventarioItem("2", "SAL MINERAL", "Alimento", null, null, 60.0, 10.0, "Sacos", 25.0, "02/07/2026 10:15"),
                InventarioItem("3", "AFRECHO DE CERVEZA", "Alimento", null, null, 320.0, 50.0, "Sacos", 80.0, "03/07/2026 11:00"),
                InventarioItem("4", "GALLINAZA", "Alimento", null, null, 150.0, 20.0, "Sacos", 15.0, "04/07/2026 14:20"),
                InventarioItem("5", "MAÍZ MOLIDO", "Alimento", null, null, 200.0, 30.0, "Sacos", 35.0, "05/07/2026 16:45")
            )
            for (item in initialItems) {
                saveItem(item)
            }
        }
    }

    private fun getIdsSet(): Set<String> {
        return prefs.getStringSet(KEY_ITEMS_LIST, emptySet()) ?: emptySet()
    }

    private fun saveIdsSet(set: Set<String>) {
        prefs.edit().putStringSet(KEY_ITEMS_LIST, set).apply()
    }

    override fun getItems(): List<InventarioItem> {
        val ids = getIdsSet()
        val list = mutableListOf<InventarioItem>()
        for (id in ids) {
            getItem(id)?.let { list.add(it) }
        }
        return list.sortedBy { it.nombre.lowercase() }
    }

    override fun getItem(id: String): InventarioItem? {
        val nombre = prefs.getString("$PREFIX_ITEM$id$SUFFIX_NOMBRE", null) ?: return null
        val tipo = prefs.getString("$PREFIX_ITEM$id$SUFFIX_TIPO", "") ?: ""
        val tipoOtro = prefs.getString("$PREFIX_ITEM$id$SUFFIX_TIPO_OTRO", null)
        val fotoPath = prefs.getString("$PREFIX_ITEM$id$SUFFIX_FOTO", null)
        val stock = prefs.getFloat("$PREFIX_ITEM$id$SUFFIX_STOCK", 0f).toDouble()

        val limiteRaw = prefs.getFloat("$PREFIX_ITEM$id$SUFFIX_LIMITE", -1f)
        val limiteNotificacion = if (limiteRaw >= 0f) limiteRaw.toDouble() else null

        val unidad = prefs.getString("$PREFIX_ITEM$id$SUFFIX_UNIDAD", "Unidades") ?: "Unidades"
        val costo = prefs.getFloat("$PREFIX_ITEM$id$SUFFIX_COSTO", 0f).toDouble()
        val fecha = prefs.getString("$PREFIX_ITEM$id$SUFFIX_FECHA", "") ?: ""

        return InventarioItem(
            id = id,
            nombre = nombre,
            tipo = tipo,
            tipoOtro = tipoOtro,
            fotoPath = fotoPath,
            stock = stock,
            limiteNotificacion = limiteNotificacion,
            unidad = unidad,
            costo = costo,
            fechaRegistro = fecha
        )
    }

    override fun saveItem(item: InventarioItem): Result<Unit> {
        return try {
            val ids = getIdsSet().toMutableSet()
            ids.add(item.id)
            saveIdsSet(ids)
            persistItemData(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun updateItem(item: InventarioItem): Result<Unit> {
        return try {
            val ids = getIdsSet()
            if (!ids.contains(item.id)) {
                return Result.failure(Exception("Item no encontrado"))
            }
            persistItemData(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun deleteItem(id: String): Result<Unit> {
        return try {
            val ids = getIdsSet().toMutableSet()
            if (!ids.contains(id)) {
                return Result.failure(Exception("Item no encontrado"))
            }
            ids.remove(id)
            saveIdsSet(ids)
            removeItemData(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun persistItemData(item: InventarioItem) {
        prefs.edit().apply {
            putString("$PREFIX_ITEM${item.id}$SUFFIX_NOMBRE", item.nombre)
            putString("$PREFIX_ITEM${item.id}$SUFFIX_TIPO", item.tipo)
            putString("$PREFIX_ITEM${item.id}$SUFFIX_TIPO_OTRO", item.tipoOtro)
            putString("$PREFIX_ITEM${item.id}$SUFFIX_FOTO", item.fotoPath)
            putFloat("$PREFIX_ITEM${item.id}$SUFFIX_STOCK", item.stock.toFloat())
            putFloat("$PREFIX_ITEM${item.id}$SUFFIX_LIMITE", item.limiteNotificacion?.toFloat() ?: -1f)
            putString("$PREFIX_ITEM${item.id}$SUFFIX_UNIDAD", item.unidad)
            putFloat("$PREFIX_ITEM${item.id}$SUFFIX_COSTO", item.costo.toFloat())
            putString("$PREFIX_ITEM${item.id}$SUFFIX_FECHA", item.fechaRegistro)
            apply()
        }
    }

    private fun removeItemData(id: String) {
        prefs.edit().apply {
            remove("$PREFIX_ITEM${id}$SUFFIX_NOMBRE")
            remove("$PREFIX_ITEM${id}$SUFFIX_TIPO")
            remove("$PREFIX_ITEM${id}$SUFFIX_TIPO_OTRO")
            remove("$PREFIX_ITEM${id}$SUFFIX_FOTO")
            remove("$PREFIX_ITEM${id}$SUFFIX_STOCK")
            remove("$PREFIX_ITEM${id}$SUFFIX_LIMITE")
            remove("$PREFIX_ITEM${id}$SUFFIX_UNIDAD")
            remove("$PREFIX_ITEM${id}$SUFFIX_COSTO")
            remove("$PREFIX_ITEM${id}$SUFFIX_FECHA")
            apply()
        }
    }
}
