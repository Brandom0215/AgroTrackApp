package pa.ac.utp.agrotrackapp.domain.repository

import pa.ac.utp.agrotrackapp.domain.model.Lote

interface LoteRepository {
    fun getLotes(): List<Lote>
    fun getLote(id: Int): Lote?
    fun saveLote(lote: Lote): Result<Unit>
    fun updateLote(lote: Lote): Result<Unit>
    fun deleteLote(id: Int): Result<Unit>
}
