package pa.ac.utp.agrotrackapp.data.alertas

import android.content.ContentValues
import android.content.Context
import pa.ac.utp.agrotrackapp.data.database.DatabaseHelper
import pa.ac.utp.agrotrackapp.domain.model.Alerta
import pa.ac.utp.agrotrackapp.domain.model.PrioridadAlerta
import pa.ac.utp.agrotrackapp.domain.model.TipoAlerta
import pa.ac.utp.agrotrackapp.domain.repository.AlertaRepository
import java.text.SimpleDateFormat
import java.util.Locale

class SqliteAlertaRepository(context: Context) : AlertaRepository {

    private val dbHelper = DatabaseHelper(context)

    override fun getAlertas(includeDismissed: Boolean): List<Alerta> {
        val list = mutableListOf<Alerta>()
        val db = dbHelper.readableDatabase

        val selection = if (includeDismissed) null else "${DatabaseHelper.COL_ALERTA_DISMISSED} = 0"
        val cursor = db.query(
            DatabaseHelper.TABLE_ALERTAS,
            null,
            selection,
            null,
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToAlerta(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fallbackSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return list.sortedByDescending { alerta ->
            try {
                sdf.parse(alerta.fecha)?.time ?: 0L
            } catch (e: Exception) {
                try {
                    fallbackSdf.parse(alerta.fecha)?.time ?: 0L
                } catch (e2: Exception) {
                    0L
                }
            }
        }
    }

    override fun saveAlerta(alerta: Alerta) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ALERTA_ID, alerta.id)
            put(DatabaseHelper.COL_ALERTA_TITULO, alerta.titulo)
            put(DatabaseHelper.COL_ALERTA_DESC, alerta.descripcion)
            put(DatabaseHelper.COL_ALERTA_TIPO, alerta.tipo.name)
            put(DatabaseHelper.COL_ALERTA_FECHA, alerta.fecha)
            put(DatabaseHelper.COL_ALERTA_PRIO, alerta.prioridad.name)
            put(DatabaseHelper.COL_ALERTA_DISMISSED, if (alerta.isDismissed) 1 else 0)
            put(DatabaseHelper.COL_ALERTA_DEST, alerta.destinationId ?: -1)
            put(DatabaseHelper.COL_ALERTA_REF, alerta.referenceId)
            put(DatabaseHelper.COL_ALERTA_FECHA_PROG, alerta.fechaProgramada)
        }

        db.insertWithOnConflict(
            DatabaseHelper.TABLE_ALERTAS,
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    override fun dismissAlerta(id: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ALERTA_DISMISSED, 1)
        }
        db.update(
            DatabaseHelper.TABLE_ALERTAS,
            values,
            "${DatabaseHelper.COL_ALERTA_ID} = ?",
            arrayOf(id)
        )
    }

    override fun restoreAlerta(id: String) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_ALERTA_DISMISSED, 0)
        }
        db.update(
            DatabaseHelper.TABLE_ALERTAS,
            values,
            "${DatabaseHelper.COL_ALERTA_ID} = ?",
            arrayOf(id)
        )
    }

    private fun cursorToAlerta(cursor: android.database.Cursor): Alerta {
        val destVal = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALERTA_DEST))
        val destinationId = if (destVal == -1) null else destVal
        return Alerta(
            id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALERTA_ID)),
            titulo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALERTA_TITULO)),
            descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALERTA_DESC)),
            tipo = TipoAlerta.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALERTA_TIPO))),
            fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALERTA_FECHA)),
            prioridad = PrioridadAlerta.valueOf(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALERTA_PRIO))),
            isDismissed = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALERTA_DISMISSED)) == 1,
            destinationId = destinationId,
            referenceId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALERTA_REF)),
            fechaProgramada = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALERTA_FECHA_PROG))
        )
    }
}
