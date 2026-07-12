package pa.ac.utp.agrotrackapp.data.sanidad

import android.content.ContentValues
import android.content.Context
import pa.ac.utp.agrotrackapp.data.database.DatabaseHelper
import pa.ac.utp.agrotrackapp.domain.model.RegistroSanitario
import pa.ac.utp.agrotrackapp.domain.repository.SanidadRepository

class SqliteSanidadRepository(context: Context) : SanidadRepository {
    private val dbHelper = DatabaseHelper(context)

    override fun getRegistros(): List<RegistroSanitario> {
        return queryByEstado("aplicado", "${DatabaseHelper.COL_SAN_FECHA} DESC")
    }

    override fun getProximos(): List<RegistroSanitario> {
        return queryByEstado("programado", "${DatabaseHelper.COL_SAN_PROXIMA_DOSIS} ASC")
    }

    override fun getAllRegistros(): List<RegistroSanitario> {
        val list = mutableListOf<RegistroSanitario>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_SANITARIA, null, null, null, null, null,
            "${DatabaseHelper.COL_SAN_PROXIMA_DOSIS} ASC, ${DatabaseHelper.COL_SAN_FECHA} ASC"
        )
        if (cursor.moveToFirst()) {
            do { list.add(cursorToRegistro(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    private fun queryByEstado(estado: String, orderBy: String): List<RegistroSanitario> {
        val list = mutableListOf<RegistroSanitario>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_SANITARIA,
            null,
            "${DatabaseHelper.COL_SAN_ESTADO} = ?",
            arrayOf(estado),
            null, null,
            orderBy
        )
        if (cursor.moveToFirst()) {
            do { list.add(cursorToRegistro(cursor)) } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    override fun saveRegistro(registro: RegistroSanitario) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_SAN_ID, registro.id)
            put(DatabaseHelper.COL_SAN_IDENTIFICADOR, registro.identificador)
            put(DatabaseHelper.COL_SAN_ALCANCE, registro.alcance)
            put(DatabaseHelper.COL_SAN_CATEGORIA, registro.categoria)
            put(DatabaseHelper.COL_SAN_DETALLE, registro.detalle)
            put(DatabaseHelper.COL_SAN_PRODUCTO, registro.producto)
            put(DatabaseHelper.COL_SAN_DOSIS, registro.dosis)
            put(DatabaseHelper.COL_SAN_FECHA, registro.fecha)
            put(DatabaseHelper.COL_SAN_PROXIMA_DOSIS, registro.proximaDosis)
            put(DatabaseHelper.COL_SAN_VETERINARIO, registro.veterinario)
            put(DatabaseHelper.COL_SAN_NOTAS, registro.notas)
            put(DatabaseHelper.COL_SAN_ESTADO, registro.estado)
            put(DatabaseHelper.COL_SAN_GRUPO_ID, registro.grupoId)
        }
        db.insertWithOnConflict(DatabaseHelper.TABLE_SANITARIA, null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
    }

    override fun deleteRegistro(id: String) {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_SANITARIA, "${DatabaseHelper.COL_SAN_ID} = ?", arrayOf(id))
    }

    override fun updateEstado(id: String, estado: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_SAN_ESTADO, estado)
        }
        db.update(DatabaseHelper.TABLE_SANITARIA, values, "${DatabaseHelper.COL_SAN_ID} = ?", arrayOf(id))
    }

    override fun marcarAplicado(id: String, fechaAplicacion: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_SAN_ESTADO, "aplicado")
            put(DatabaseHelper.COL_SAN_FECHA, fechaAplicacion)
            put(DatabaseHelper.COL_SAN_PROXIMA_DOSIS, "") // ya no es próxima
        }
        db.update(DatabaseHelper.TABLE_SANITARIA, values, "${DatabaseHelper.COL_SAN_ID} = ?", arrayOf(id))
    }

    private fun cursorToRegistro(cursor: android.database.Cursor): RegistroSanitario {
        val grupoIdIdx = cursor.getColumnIndex(DatabaseHelper.COL_SAN_GRUPO_ID)
        return RegistroSanitario(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_ID)),
            identificador = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_IDENTIFICADOR)),
            alcance = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_ALCANCE)),
            categoria = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_CATEGORIA)),
            detalle = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_DETALLE)),
            producto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_PRODUCTO)),
            dosis = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_DOSIS)),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_FECHA)),
            proximaDosis = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_PROXIMA_DOSIS)),
            veterinario = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_VETERINARIO)),
            notas = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_NOTAS)),
            estado = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SAN_ESTADO)),
            grupoId = if (grupoIdIdx >= 0) cursor.getString(grupoIdIdx) ?: "" else ""
        )
    }
}
