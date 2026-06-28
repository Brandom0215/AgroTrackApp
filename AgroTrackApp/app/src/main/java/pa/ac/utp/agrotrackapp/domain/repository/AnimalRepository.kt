package pa.ac.utp.agrotrackapp.domain.repository

import pa.ac.utp.agrotrackapp.domain.model.Animal

interface AnimalRepository {
    /**
     * Obtiene la lista completa de todos los animales registrados.
     */
    fun getAnimals(): List<Animal>

    /**
     * Guarda un nuevo animal. Retorna éxito o fallo.
     */
    fun saveAnimal(animal: Animal): Result<Unit>

    /**
     * Obtiene los detalles de un animal por su número de arete.
     */
    fun getAnimal(numeroAnimal: String): Animal?

    /**
     * Actualiza los datos de un animal existente.
     */
    fun updateAnimal(animal: Animal): Result<Unit>

    /**
     * Elimina un animal por su número de arete.
     */
    fun deleteAnimal(numeroAnimal: String): Result<Unit>
}
