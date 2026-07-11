package pa.ac.utp.agrotrackapp.data.inventario

import android.content.ContentValues
import android.content.Context
import pa.ac.utp.agrotrackapp.data.alertas.AlertManager
import pa.ac.utp.agrotrackapp.data.database.DatabaseHelper
import pa.ac.utp.agrotrackapp.domain.model.InventarioItem
import pa.ac.utp.agrotrackapp.domain.repository.InventarioRepository

class SqliteInventarioRepository(private val context: Context) : InventarioRepository {

    private val dbHelper = DatabaseHelper(context)

    // La base de datos inicia vacía sin datos precargados.

    override fun getItems(): List<InventarioItem> {
        val list = mutableListOf<InventarioItem>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_INVENTARIO_ITEMS,
            null, null, null, null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()

        return list.sortedBy { it.nombre.lowercase() }
    }

    override fun getItem(id: String): InventarioItem? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_INVENTARIO_ITEMS,
            null,
            "${DatabaseHelper.COL_INV_ID} = ?",
            arrayOf(id),
            null, null, null
        )

        var item: InventarioItem? = null
        if (cursor.moveToFirst()) {
            item = cursorToItem(cursor)
        }
        cursor.close()
        return item
    }

    override fun saveItem(item: InventarioItem): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            val values = itemToValues(item)
            db.insertWithOnConflict(
                DatabaseHelper.TABLE_INVENTARIO_ITEMS,
                null,
                values,
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
            )

            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun updateItem(item: InventarioItem): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            val values = itemToValues(item)
            db.update(
                DatabaseHelper.TABLE_INVENTARIO_ITEMS,
                values,
                "${DatabaseHelper.COL_INV_ID} = ?",
                arrayOf(item.id)
            )

            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun deleteItem(id: String): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            db.delete(
                DatabaseHelper.TABLE_INVENTARIO_ITEMS,
                "${DatabaseHelper.COL_INV_ID} = ?",
                arrayOf(id)
            )

            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cursorToItem(cursor: android.database.Cursor): InventarioItem {
        val limitVal = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_LIMITE))
        val limiteNotificacion = if (cursor.isNull(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_LIMITE)) || limitVal < 0) null else limitVal

        return InventarioItem(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_ID)),
            nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_NOMBRE)),
            tipo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_TIPO)),
            tipoOtro = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_TIPO_OTRO)),
            fotoPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_FOTO)),
            stock = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_STOCK)),
            limiteNotificacion = limiteNotificacion,
            unidad = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_UNIDAD)),
            costo = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_COSTO)),
            precio = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_PRECIO)),
            fechaRegistro = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INV_FECHA))
        )
    }

    private fun itemToValues(item: InventarioItem): ContentValues {
        return ContentValues().apply {
            put(DatabaseHelper.COL_INV_ID, item.id)
            put(DatabaseHelper.COL_INV_NOMBRE, item.nombre)
            put(DatabaseHelper.COL_INV_TIPO, item.tipo)
            put(DatabaseHelper.COL_INV_TIPO_OTRO, item.tipoOtro)
            put(DatabaseHelper.COL_INV_FOTO, item.fotoPath)
            put(DatabaseHelper.COL_INV_STOCK, item.stock)
            put(DatabaseHelper.COL_INV_LIMITE, item.limiteNotificacion ?: -1.0)
            put(DatabaseHelper.COL_INV_UNIDAD, item.unidad)
            put(DatabaseHelper.COL_INV_COSTO, item.costo)
            put(DatabaseHelper.COL_INV_PRECIO, item.precio)
            put(DatabaseHelper.COL_INV_FECHA, item.fechaRegistro)
        }
    }
}
