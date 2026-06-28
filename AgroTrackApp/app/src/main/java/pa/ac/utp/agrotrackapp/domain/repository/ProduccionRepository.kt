package pa.ac.utp.agrotrackapp.domain.repository

import pa.ac.utp.agrotrackapp.domain.model.CarneRecord
import pa.ac.utp.agrotrackapp.domain.model.LecheRecord

interface ProduccionRepository {
    
    // --- Producción de Carne (Engorde) ---
    fun getCarneRecords(): List<CarneRecord>
    fun saveCarneRecord(record: CarneRecord): Result<Unit>
    fun deleteCarneRecord(numeroAnimal: String): Result<Unit>
    fun getCarneRecord(numeroAnimal: String): CarneRecord?
    fun updateCarneRecord(record: CarneRecord): Result<Unit>

    // --- Producción de Leche (Ordeño) ---
    fun getLecheRecords(): List<LecheRecord>
    fun saveLecheRecord(record: LecheRecord): Result<Unit>
    fun deleteLecheRecord(numeroAnimal: String): Result<Unit>
    fun getLecheRecord(numeroAnimal: String): LecheRecord?
    fun updateLecheRecord(record: LecheRecord): Result<Unit>
}
