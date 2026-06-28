package pa.ac.utp.agrotrackapp.domain.repository

import pa.ac.utp.agrotrackapp.domain.model.MortalidadRecord

interface MortalidadRepository {
    fun getMortalidadRecords(): List<MortalidadRecord>
    fun saveMortalidadRecord(record: MortalidadRecord): Result<Unit>
    fun deleteMortalidadRecord(numeroAnimal: String): Result<Unit>
}
