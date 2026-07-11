package pa.ac.utp.agrotrackapp.domain.repository

import pa.ac.utp.agrotrackapp.domain.model.Alerta

interface AlertaRepository {
    fun getAlertas(includeDismissed: Boolean = false): List<Alerta>
    fun saveAlerta(alerta: Alerta)
    fun dismissAlerta(id: String)
    fun restoreAlerta(id: String)
}
