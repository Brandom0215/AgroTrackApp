package pa.ac.utp.agrotrackapp.data.produccion

import android.content.ContentValues
import android.content.Context
import pa.ac.utp.agrotrackapp.data.alertas.AlertManager
import pa.ac.utp.agrotrackapp.data.database.DatabaseHelper
import pa.ac.utp.agrotrackapp.domain.model.CarneRecord
import pa.ac.utp.agrotrackapp.domain.model.LecheRecord
import pa.ac.utp.agrotrackapp.domain.repository.ProduccionRepository

class SqliteProduccionRepository(private val context: Context) : ProduccionRepository {

    private val dbHelper = DatabaseHelper(context)

    // --- Carne Records ---

    override fun getCarneRecords(): List<CarneRecord> {
        val list = mutableListOf<CarneRecord>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_CARNE_RECORDS,
            null, null, null, null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToCarneRecord(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    override fun saveCarneRecord(record: CarneRecord): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            val values = carneRecordToValues(record)
            db.insertWithOnConflict(
                DatabaseHelper.TABLE_CARNE_RECORDS,
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

    override fun getCarneRecord(numeroAnimal: String): CarneRecord? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_CARNE_RECORDS,
            null,
            "${DatabaseHelper.COL_CARNE_NUMERO} = ?",
            arrayOf(numeroAnimal.trim()),
            null, null, null
        )

        var record: CarneRecord? = null
        if (cursor.moveToFirst()) {
            record = cursorToCarneRecord(cursor)
        }
        cursor.close()
        return record
    }

    override fun updateCarneRecord(record: CarneRecord): Result<Unit> {
        return saveCarneRecord(record)
    }

    override fun deleteCarneRecord(numeroAnimal: String): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            db.delete(
                DatabaseHelper.TABLE_CARNE_RECORDS,
                "${DatabaseHelper.COL_CARNE_NUMERO} = ?",
                arrayOf(numeroAnimal.trim())
            )

            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Leche Records ---

    override fun getLecheRecords(): List<LecheRecord> {
        val list = mutableListOf<LecheRecord>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_LECHE_RECORDS,
            null, null, null, null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToLecheRecord(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    override fun saveLecheRecord(record: LecheRecord): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            val values = lecheRecordToValues(record)
            db.insertWithOnConflict(
                DatabaseHelper.TABLE_LECHE_RECORDS,
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

    override fun getLecheRecord(numeroAnimal: String): LecheRecord? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_LECHE_RECORDS,
            null,
            "${DatabaseHelper.COL_LECHE_NUMERO} = ?",
            arrayOf(numeroAnimal.trim()),
            null, null, null
        )

        var record: LecheRecord? = null
        if (cursor.moveToFirst()) {
            record = cursorToLecheRecord(cursor)
        }
        cursor.close()
        return record
    }

    override fun updateLecheRecord(record: LecheRecord): Result<Unit> {
        return saveLecheRecord(record)
    }

    override fun deleteLecheRecord(numeroAnimal: String): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            db.delete(
                DatabaseHelper.TABLE_LECHE_RECORDS,
                "${DatabaseHelper.COL_LECHE_NUMERO} = ?",
                arrayOf(numeroAnimal.trim())
            )

            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Helpers ---

    private fun cursorToCarneRecord(cursor: android.database.Cursor): CarneRecord {
        return CarneRecord(
            numeroAnimal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_NUMERO)),
            raza = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_RAZA)),
            fechaPesajeActual = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_FECHA_ACTUAL)),
            pesoActual = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_PESO_ACTUAL)),
            fechaPesajeAnterior = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_FECHA_ANTERIOR)),
            pesoAnterior = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_PESO_ANTERIOR)),
            pesoEntrada = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_PESO_ENTRADA)),
            gananciaTotal = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_GANANCIA)),
            diasTranscurridos = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_DIAS)),
            gdp = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_GDP)),
            estadoSalud = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_SALUD)),
            activo = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CARNE_ACTIVO)) == 1
        )
    }

    private fun carneRecordToValues(record: CarneRecord): ContentValues {
        return ContentValues().apply {
            put(DatabaseHelper.COL_CARNE_NUMERO, record.numeroAnimal.trim())
            put(DatabaseHelper.COL_CARNE_RAZA, record.raza)
            put(DatabaseHelper.COL_CARNE_FECHA_ACTUAL, record.fechaPesajeActual)
            put(DatabaseHelper.COL_CARNE_PESO_ACTUAL, record.pesoActual)
            put(DatabaseHelper.COL_CARNE_FECHA_ANTERIOR, record.fechaPesajeAnterior)
            put(DatabaseHelper.COL_CARNE_PESO_ANTERIOR, record.pesoAnterior)
            put(DatabaseHelper.COL_CARNE_PESO_ENTRADA, record.pesoEntrada)
            put(DatabaseHelper.COL_CARNE_GANANCIA, record.gananciaTotal)
            put(DatabaseHelper.COL_CARNE_DIAS, record.diasTranscurridos)
            put(DatabaseHelper.COL_CARNE_GDP, record.gdp)
            put(DatabaseHelper.COL_CARNE_SALUD, record.estadoSalud)
            put(DatabaseHelper.COL_CARNE_ACTIVO, if (record.activo) 1 else 0)
        }
    }

    private fun cursorToLecheRecord(cursor: android.database.Cursor): LecheRecord {
        return LecheRecord(
            numeroAnimal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LECHE_NUMERO)),
            fechaRegistro = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LECHE_FECHA)),
            turno = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LECHE_TURNO)),
            litros = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LECHE_LITROS)),
            fechaUltimoParto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LECHE_FECHA_PARTO)),
            lactancias = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LECHE_LACTANCIAS)),
            del = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LECHE_DEL)),
            promedioDiario = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LECHE_PROMEDIO)),
            activo = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LECHE_ACTIVO)) == 1
        )
    }

    private fun lecheRecordToValues(record: LecheRecord): ContentValues {
        return ContentValues().apply {
            put(DatabaseHelper.COL_LECHE_NUMERO, record.numeroAnimal.trim())
            put(DatabaseHelper.COL_LECHE_FECHA, record.fechaRegistro)
            put(DatabaseHelper.COL_LECHE_TURNO, record.turno)
            put(DatabaseHelper.COL_LECHE_LITROS, record.litros)
            put(DatabaseHelper.COL_LECHE_FECHA_PARTO, record.fechaUltimoParto)
            put(DatabaseHelper.COL_LECHE_LACTANCIAS, record.lactancias)
            put(DatabaseHelper.COL_LECHE_DEL, record.del)
            put(DatabaseHelper.COL_LECHE_PROMEDIO, record.promedioDiario)
            put(DatabaseHelper.COL_LECHE_ACTIVO, if (record.activo) 1 else 0)
        }
    }
}
