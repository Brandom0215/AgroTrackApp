package pa.ac.utp.agrotrackapp.data.animal

import android.content.ContentValues
import android.content.Context
import pa.ac.utp.agrotrackapp.data.database.DatabaseHelper
import pa.ac.utp.agrotrackapp.domain.model.Lote
import pa.ac.utp.agrotrackapp.domain.repository.LoteRepository

class SqliteLoteRepository(private val context: Context) : LoteRepository {

    private val dbHelper = DatabaseHelper(context)

    override fun getLotes(): List<Lote> {
        val list = mutableListOf<Lote>()
        val db = dbHelper.readableDatabase
        // Use a LEFT JOIN to count animals per lote
        val query = """
            SELECT l.${DatabaseHelper.COL_LOTE_ID}, l.${DatabaseHelper.COL_LOTE_NOMBRE}, COUNT(a.${DatabaseHelper.COL_ANIMAL_NUMERO}) as cantidad
            FROM ${DatabaseHelper.TABLE_LOTES} l
            LEFT JOIN ${DatabaseHelper.TABLE_ANIMALES} a ON l.${DatabaseHelper.COL_LOTE_NOMBRE} = a.${DatabaseHelper.COL_ANIMAL_LOTE}
            GROUP BY l.${DatabaseHelper.COL_LOTE_ID}, l.${DatabaseHelper.COL_LOTE_NOMBRE}
            ORDER BY l.${DatabaseHelper.COL_LOTE_NOMBRE} ASC
        """
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val nombre = cursor.getString(1)
                val cantidad = cursor.getInt(2)
                list.add(Lote(id, nombre, cantidad))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    override fun getLote(id: Int): Lote? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_LOTES,
            null,
            "${DatabaseHelper.COL_LOTE_ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        var lote: Lote? = null
        if (cursor.moveToFirst()) {
            val dbId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOTE_ID))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LOTE_NOMBRE))
            lote = Lote(dbId, nombre)
        }
        cursor.close()
        return lote
    }

    override fun saveLote(lote: Lote): Result<Unit> {
        return try {
            val nombre = lote.nombre.trim()
            if (nombre.isEmpty()) {
                return Result.failure(Exception("El nombre del lote no puede estar vacío"))
            }

            if (loteExists(nombre)) {
                return Result.failure(Exception("El lote '$nombre' ya existe"))
            }

            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_LOTE_NOMBRE, nombre)
            }
            db.insertOrThrow(DatabaseHelper.TABLE_LOTES, null, values)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun updateLote(lote: Lote): Result<Unit> {
        return try {
            val nombre = lote.nombre.trim()
            if (nombre.isEmpty()) {
                return Result.failure(Exception("El nombre del lote no puede estar vacío"))
            }

            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put(DatabaseHelper.COL_LOTE_NOMBRE, nombre)
            }
            db.update(
                DatabaseHelper.TABLE_LOTES,
                values,
                "${DatabaseHelper.COL_LOTE_ID} = ?",
                arrayOf(lote.id.toString())
            )
            // Ideally we should also update the animals that had the old lote name
            // But for simplicity, we assume we update the animals when updating a lote or it cascades.
            // Let's manually update the animals' lote_nombre.
            // First we need the old name... This is complex without it. Let's fetch the old name.
            val oldLote = getLote(lote.id)
            if (oldLote != null && oldLote.nombre != nombre) {
                val valuesAnimal = ContentValues().apply {
                    put(DatabaseHelper.COL_ANIMAL_LOTE, nombre)
                }
                db.update(
                    DatabaseHelper.TABLE_ANIMALES,
                    valuesAnimal,
                    "${DatabaseHelper.COL_ANIMAL_LOTE} = ?",
                    arrayOf(oldLote.nombre)
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun deleteLote(id: Int): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            val lote = getLote(id)
            if (lote != null) {
                // Remove lote from animals
                val valuesAnimal = ContentValues().apply {
                    put(DatabaseHelper.COL_ANIMAL_LOTE, "")
                }
                db.update(
                    DatabaseHelper.TABLE_ANIMALES,
                    valuesAnimal,
                    "${DatabaseHelper.COL_ANIMAL_LOTE} = ?",
                    arrayOf(lote.nombre)
                )
            }

            db.delete(
                DatabaseHelper.TABLE_LOTES,
                "${DatabaseHelper.COL_LOTE_ID} = ?",
                arrayOf(id.toString())
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loteExists(nombre: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_LOTES,
            arrayOf(DatabaseHelper.COL_LOTE_ID),
            "${DatabaseHelper.COL_LOTE_NOMBRE} = ?",
            arrayOf(nombre),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }
}
