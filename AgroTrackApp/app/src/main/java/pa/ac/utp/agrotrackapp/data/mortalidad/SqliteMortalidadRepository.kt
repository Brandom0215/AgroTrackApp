package pa.ac.utp.agrotrackapp.data.mortalidad

import android.content.ContentValues
import android.content.Context
import pa.ac.utp.agrotrackapp.data.alertas.AlertManager
import pa.ac.utp.agrotrackapp.data.database.DatabaseHelper
import pa.ac.utp.agrotrackapp.domain.model.MortalidadRecord
import pa.ac.utp.agrotrackapp.domain.repository.MortalidadRepository

class SqliteMortalidadRepository(private val context: Context) : MortalidadRepository {

    private val dbHelper = DatabaseHelper(context)

    override fun getMortalidadRecords(): List<MortalidadRecord> {
        val list = mutableListOf<MortalidadRecord>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_MORTALIDAD_RECORDS,
            null, null, null, null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(
                    MortalidadRecord(
                        numeroAnimal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MORT_NUMERO)),
                        causa = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MORT_CAUSA)),
                        fechaMuerte = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MORT_FECHA)),
                        detalles = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MORT_DETALLES))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    override fun saveMortalidadRecord(record: MortalidadRecord): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_MORT_NUMERO, record.numeroAnimal.trim())
                put(DatabaseHelper.COL_MORT_CAUSA, record.causa)
                put(DatabaseHelper.COL_MORT_FECHA, record.fechaMuerte)
                put(DatabaseHelper.COL_MORT_DETALLES, record.detalles)
            }

            db.insertWithOnConflict(
                DatabaseHelper.TABLE_MORTALIDAD_RECORDS,
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

    override fun deleteMortalidadRecord(numeroAnimal: String): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            db.delete(
                DatabaseHelper.TABLE_MORTALIDAD_RECORDS,
                "${DatabaseHelper.COL_MORT_NUMERO} = ?",
                arrayOf(numeroAnimal.trim())
            )

            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
