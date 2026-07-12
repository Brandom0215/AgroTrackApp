package pa.ac.utp.agrotrackapp.domain.repository

import pa.ac.utp.agrotrackapp.domain.model.RegistroSanitario

interface SanidadRepository {
    fun getRegistros(): List<RegistroSanitario>           // estado=aplicado
    fun getProximos(): List<RegistroSanitario>            // estado=programado
    fun getAllRegistros(): List<RegistroSanitario>         // todos
    fun saveRegistro(registro: RegistroSanitario)
    fun deleteRegistro(id: String)
    fun updateEstado(id: String, estado: String)
    fun marcarAplicado(id: String, fechaAplicacion: String) // actualiza estado + fecha en el mismo registro
}
