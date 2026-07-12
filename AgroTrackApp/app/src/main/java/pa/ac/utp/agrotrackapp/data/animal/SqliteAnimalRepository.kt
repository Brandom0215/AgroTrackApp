package pa.ac.utp.agrotrackapp.data.animal

import android.content.ContentValues
import android.content.Context
import pa.ac.utp.agrotrackapp.data.alertas.AlertManager
import pa.ac.utp.agrotrackapp.data.database.DatabaseHelper
import pa.ac.utp.agrotrackapp.domain.model.Animal
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository

class SqliteAnimalRepository(private val context: Context) : AnimalRepository {

    private val dbHelper = DatabaseHelper(context)

    override fun getAnimals(): List<Animal> {
        val list = mutableListOf<Animal>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ANIMALES,
            null, null, null, null, null,
            "${DatabaseHelper.COL_ANIMAL_NUMERO} ASC"
        )

        if (cursor.moveToFirst()) {
            do {
                list.add(cursorToAnimal(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    override fun saveAnimal(animal: Animal): Result<Unit> {
        return try {
            val arete = animal.numeroAnimal.trim()
            if (arete.isEmpty()) {
                return Result.failure(Exception("El número de arete no puede estar vacío"))
            }

            if (animalExists(arete)) {
                return Result.failure(Exception("El animal con arete $arete ya está registrado"))
            }

            val db = dbHelper.writableDatabase
            val values = animalToValues(animal)
            db.insertOrThrow(DatabaseHelper.TABLE_ANIMALES, null, values)
            
            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAnimal(numeroAnimal: String): Animal? {
        val arete = numeroAnimal.trim()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ANIMALES,
            null,
            "${DatabaseHelper.COL_ANIMAL_NUMERO} = ?",
            arrayOf(arete),
            null, null, null
        )

        var animal: Animal? = null
        if (cursor.moveToFirst()) {
            animal = cursorToAnimal(cursor)
        }
        cursor.close()
        return animal
    }

    override fun updateAnimal(oldArete: String, animal: Animal): Result<Unit> {
        return try {
            val db = dbHelper.writableDatabase
            val areteActual = oldArete.trim()
            val nuevoArete = animal.numeroAnimal.trim()

            if (!animalExists(areteActual)) {
                return Result.failure(Exception("El animal con arete $areteActual no existe"))
            }

            if (areteActual != nuevoArete && animalExists(nuevoArete)) {
                return Result.failure(Exception("Ya existe un animal con el nuevo arete $nuevoArete"))
            }

            db.beginTransaction()
            try {
                // Actualizar animal
                val values = animalToValues(animal)
                db.update(
                    DatabaseHelper.TABLE_ANIMALES,
                    values,
                    "${DatabaseHelper.COL_ANIMAL_NUMERO} = ?",
                    arrayOf(areteActual)
                )

                // Actualizar referencias si el arete cambió
                if (areteActual != nuevoArete) {
                    val updateValues = ContentValues().apply { put(DatabaseHelper.COL_CARNE_NUMERO, nuevoArete) }
                    db.update(DatabaseHelper.TABLE_CARNE_RECORDS, updateValues, "${DatabaseHelper.COL_CARNE_NUMERO} = ?", arrayOf(areteActual))
                    
                    val updateLeche = ContentValues().apply { put(DatabaseHelper.COL_LECHE_NUMERO, nuevoArete) }
                    db.update(DatabaseHelper.TABLE_LECHE_RECORDS, updateLeche, "${DatabaseHelper.COL_LECHE_NUMERO} = ?", arrayOf(areteActual))
                    
                    val updateMort = ContentValues().apply { put(DatabaseHelper.COL_MORT_NUMERO, nuevoArete) }
                    db.update(DatabaseHelper.TABLE_MORTALIDAD_RECORDS, updateMort, "${DatabaseHelper.COL_MORT_NUMERO} = ?", arrayOf(areteActual))
                    
                    val updateSan = ContentValues().apply { put(DatabaseHelper.COL_SAN_IDENTIFICADOR, nuevoArete) }
                    db.update(DatabaseHelper.TABLE_SANITARIA, updateSan, "${DatabaseHelper.COL_SAN_IDENTIFICADOR} = ?", arrayOf(areteActual))
                }

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }

            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun deleteAnimal(numeroAnimal: String): Result<Unit> {
        return try {
            val arete = numeroAnimal.trim()
            if (!animalExists(arete)) {
                return Result.failure(Exception("El animal con arete $arete no existe"))
            }

            val db = dbHelper.writableDatabase
            db.delete(
                DatabaseHelper.TABLE_ANIMALES,
                "${DatabaseHelper.COL_ANIMAL_NUMERO} = ?",
                arrayOf(arete)
            )

            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun animalExists(arete: String): Boolean {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_ANIMALES,
            arrayOf(DatabaseHelper.COL_ANIMAL_NUMERO),
            "${DatabaseHelper.COL_ANIMAL_NUMERO} = ?",
            arrayOf(arete),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    private fun cursorToAnimal(cursor: android.database.Cursor): Animal {
        return Animal(
            numeroAnimal = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_NUMERO)),
            sexo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_SEXO)),
            trazabilidad = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_TRAZABILIDAD)),
            numeroChip = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_CHIP)),
            fechaNacimiento = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_FECHA_NACIMIENTO)),
            raza = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_RAZA)),
            proposito = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_PROPOSITO)),
            manga = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_MANGA)),
            peso = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_PESO)),
            padre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_PADRE)),
            madre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_MADRE)),
            notas = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_NOTAS)),
            imagenPath = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_IMAGEN)),
            lote = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ANIMAL_LOTE)) ?: ""
        )
    }

    private fun animalToValues(animal: Animal): ContentValues {
        return ContentValues().apply {
            put(DatabaseHelper.COL_ANIMAL_NUMERO, animal.numeroAnimal)
            put(DatabaseHelper.COL_ANIMAL_SEXO, animal.sexo)
            put(DatabaseHelper.COL_ANIMAL_TRAZABILIDAD, animal.trazabilidad)
            put(DatabaseHelper.COL_ANIMAL_CHIP, animal.numeroChip)
            put(DatabaseHelper.COL_ANIMAL_FECHA_NACIMIENTO, animal.fechaNacimiento)
            put(DatabaseHelper.COL_ANIMAL_RAZA, animal.raza)
            put(DatabaseHelper.COL_ANIMAL_PROPOSITO, animal.proposito)
            put(DatabaseHelper.COL_ANIMAL_MANGA, animal.manga)
            put(DatabaseHelper.COL_ANIMAL_PESO, animal.peso)
            put(DatabaseHelper.COL_ANIMAL_PADRE, animal.padre)
            put(DatabaseHelper.COL_ANIMAL_MADRE, animal.madre)
            put(DatabaseHelper.COL_ANIMAL_NOTAS, animal.notas)
            put(DatabaseHelper.COL_ANIMAL_IMAGEN, animal.imagenPath)
            put(DatabaseHelper.COL_ANIMAL_LOTE, animal.lote)
        }
    }
}
