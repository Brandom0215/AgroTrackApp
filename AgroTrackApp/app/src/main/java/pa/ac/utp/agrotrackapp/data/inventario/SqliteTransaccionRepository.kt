package pa.ac.utp.agrotrackapp.data.inventario

import android.content.ContentValues
import android.content.Context
import pa.ac.utp.agrotrackapp.data.database.DatabaseHelper
import pa.ac.utp.agrotrackapp.domain.model.Transaccion

class SqliteTransaccionRepository(context: Context) {
    
    private val dbHelper = DatabaseHelper(context)

    fun saveTransaccion(trans: Transaccion): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_TRANS_ID, trans.id)
                put(DatabaseHelper.COL_TRANS_TIPO, trans.tipo)
                put(DatabaseHelper.COL_TRANS_PROD_ID, trans.productoId)
                put(DatabaseHelper.COL_TRANS_PROD_NOMBRE, trans.productoNombre)
                put(DatabaseHelper.COL_TRANS_CANTIDAD, trans.cantidad)
                put(DatabaseHelper.COL_TRANS_PRECIO_UNIT, trans.precioUnitario)
                put(DatabaseHelper.COL_TRANS_COSTO_UNIT, trans.costoUnitario)
                put(DatabaseHelper.COL_TRANS_FECHA, trans.fecha)
                put(DatabaseHelper.COL_TRANS_DETALLES, trans.detalles)
            }
            db.insertWithOnConflict(
                DatabaseHelper.TABLE_TRANSACCIONES,
                null,
                values,
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTransacciones(): List<Transaccion> {
        val list = mutableListOf<Transaccion>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_TRANSACCIONES,
            null, null, null, null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Transaccion(
                        id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_ID)),
                        tipo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TIPO)),
                        productoId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_PROD_ID)),
                        productoNombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_PROD_NOMBRE)),
                        cantidad = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_CANTIDAD)),
                        precioUnitario = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_PRECIO_UNIT)),
                        costoUnitario = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_COSTO_UNIT)),
                        fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_FECHA)),
                        detalles = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DETALLES))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}
