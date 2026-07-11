package pa.ac.utp.agrotrackapp.data.animal

import android.content.Context
import android.content.SharedPreferences
import pa.ac.utp.agrotrackapp.data.alertas.AlertManager
import pa.ac.utp.agrotrackapp.domain.model.Animal
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository

/**
 * Implementación de [AnimalRepository] utilizando [SharedPreferences] como almacenamiento persistente local.
 * Diseñado para gestionar los datos de los animales (ganado) mediante el uso de "aretes" como identificador único.
 */
class SharedPrefsAnimalRepository(private val context: Context) : AnimalRepository {

    // Archivo de SharedPreferences específico para los datos de ganado
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("GanaDEXAnimalPrefs", Context.MODE_PRIVATE)

    companion object {
        // Llave para almacenar el Set de todos los aretes registrados
        private const val KEY_ARETES_LIST = "aretes_list"

        // Prefijo identificador para la clave de cada animal
        private const val PREFIX_ANIMAL = "animal_"

        // Sufijos mapeados para simular una estructura de columnas/campos de una base de datos
        private const val SUFFIX_SEXO = "_sexo"
        private const val SUFFIX_TRAZABILIDAD = "_trazabilidad"
        private const val SUFFIX_CHIP = "_chip"
        private const val SUFFIX_FECHA_NACIMIENTO = "_fecha_nacimiento"
        private const val SUFFIX_RAZA = "_raza"
        private const val SUFFIX_PROPOSITO = "_proposito"
        private const val SUFFIX_MANGA = "_manga"
        private const val SUFFIX_PESO = "_peso"
        private const val SUFFIX_PADRE = "_padre"
        private const val SUFFIX_MADRE = "_madre"
        private const val SUFFIX_NOTAS = "_notas"
        private const val SUFFIX_IMAGEN = "_imagen"
    }

    /**
     * Recupera el conjunto (Set) de números de aretes almacenados.
     */
    private fun getAretesSet(): Set<String> {
        return sharedPreferences.getStringSet(KEY_ARETES_LIST, emptySet()) ?: emptySet()
    }

    /**
     * Persiste el conjunto actualizado de números de aretes.
     */
    private fun saveAretesSet(set: Set<String>) {
        sharedPreferences.edit().putStringSet(KEY_ARETES_LIST, set).apply()
    }

    /**
     * Obtiene la lista completa de animales registrados, ordenada por el número de arete.
     */
    override fun getAnimals(): List<Animal> {
        val aretes = getAretesSet()
        val list = mutableListOf<Animal>()

        // Iterar sobre cada arete para reconstruir el objeto Animal
        for (arete in aretes) {
            val animal = getAnimal(arete)
            if (animal != null) {
                list.add(animal)
            }
        }
        // Retorna la lista ordenada por el identificador del animal
        return list.sortedBy { it.numeroAnimal }
    }

    /**
     * Registra un nuevo animal en el almacenamiento.
     * @return [Result.success] si se guarda con éxito o [Result.failure] si hay duplicados o campos vacíos.
     */
    override fun saveAnimal(animal: Animal): Result<Unit> {
        return try {
            val arete = animal.numeroAnimal.trim()

            // Validaciones de negocio iniciales
            if (arete.isEmpty()) {
                return Result.failure(Exception("El número de arete no puede estar vacío"))
            }

            val aretes = getAretesSet().toMutableSet()
            if (aretes.contains(arete)) {
                return Result.failure(Exception("El animal con arete $arete ya está registrado"))
            }

            // Agregar nuevo arete al índice global
            aretes.add(arete)
            saveAretesSet(aretes)

            // Guardar los detalles del animal
            persistAnimalData(arete, animal)
            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca y retorna un [Animal] específico a través de su número de arete.
     * Devuelve null si el animal no existe.
     */
    override fun getAnimal(numeroAnimal: String): Animal? {
        val arete = numeroAnimal.trim()

        // Validar existencia verificando si el campo obligatorio 'sexo' está presente
        val sexo = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_SEXO", null) ?: return null

        val fechaNacimiento = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_FECHA_NACIMIENTO", "") ?: ""
        val raza = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_RAZA", "") ?: ""
        val proposito = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_PROPOSITO", "") ?: ""

        // Reconstrucción del modelo de dominio Animal con valores por defecto en caso de nulos
        return Animal(
            numeroAnimal = arete,
            sexo = sexo,
            trazabilidad = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_TRAZABILIDAD", "") ?: "",
            numeroChip = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_CHIP", "") ?: "",
            fechaNacimiento = fechaNacimiento,
            raza = raza,
            proposito = proposito,
            manga = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_MANGA", "") ?: "",
            peso = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_PESO", "") ?: "",
            padre = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_PADRE", "") ?: "",
            madre = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_MADRE", "") ?: "",
            notas = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_NOTAS", "") ?: "",
            imagenPath = sharedPreferences.getString("$PREFIX_ANIMAL$arete$SUFFIX_IMAGEN", "") ?: ""
        )
    }

    /**
     * Actualiza los datos de un animal ya existente.
     * @return [Result.failure] si el animal no se encuentra en los registros.
     */
    override fun updateAnimal(animal: Animal): Result<Unit> {
        return try {
            val arete = animal.numeroAnimal.trim()
            val aretes = getAretesSet()

            if (!aretes.contains(arete)) {
                return Result.failure(Exception("El animal con arete $arete no existe"))
            }

            // Sobrescribir los datos persistidos
            persistAnimalData(arete, animal)
            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un animal del almacenamiento, borrando tanto su índice como sus datos individuales.
     */
    override fun deleteAnimal(numeroAnimal: String): Result<Unit> {
        return try {
            val arete = numeroAnimal.trim()
            val aretes = getAretesSet().toMutableSet()

            if (!aretes.contains(arete)) {
                return Result.failure(Exception("El animal con arete $arete no existe"))
            }

            // Remover el arete del índice global
            aretes.remove(arete)
            saveAretesSet(aretes)

            // Limpiar todos los campos del SharedPreferences asociados a este arete
            removeAnimalData(arete)
            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Escribe todos los atributos del objeto [Animal] en SharedPreferences.
     */
    private fun persistAnimalData(arete: String, animal: Animal) {
        sharedPreferences.edit().apply {
            putString("$PREFIX_ANIMAL$arete$SUFFIX_SEXO", animal.sexo)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_TRAZABILIDAD", animal.trazabilidad)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_CHIP", animal.numeroChip)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_FECHA_NACIMIENTO", animal.fechaNacimiento)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_RAZA", animal.raza)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_PROPOSITO", animal.proposito)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_MANGA", animal.manga)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_PESO", animal.peso)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_PADRE", animal.padre)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_MADRE", animal.madre)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_NOTAS", animal.notas)
            putString("$PREFIX_ANIMAL$arete$SUFFIX_IMAGEN", animal.imagenPath)
            apply()
        }
    }

    /**
     * Remueve todas las llaves de SharedPreferences vinculadas al arete de un animal específico.
     */
    private fun removeAnimalData(arete: String) {
        sharedPreferences.edit().apply {
            remove("$PREFIX_ANIMAL$arete$SUFFIX_SEXO")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_TRAZABILIDAD")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_CHIP")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_FECHA_NACIMIENTO")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_RAZA")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_PROPOSITO")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_MANGA")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_PESO")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_PADRE")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_MADRE")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_NOTAS")
            remove("$PREFIX_ANIMAL$arete$SUFFIX_IMAGEN")
            apply()
        }
    }
}