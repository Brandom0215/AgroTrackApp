package pa.ac.utp.agrotrackapp.data.alertas

import android.content.Context
import android.content.SharedPreferences
import pa.ac.utp.agrotrackapp.domain.model.Alerta
import pa.ac.utp.agrotrackapp.domain.model.PrioridadAlerta
import pa.ac.utp.agrotrackapp.domain.model.TipoAlerta
import pa.ac.utp.agrotrackapp.domain.repository.AlertaRepository

class SharedPrefsAlertaRepository(context: Context) : AlertaRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("AgroTrackAlertasPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ALERTAS_IDS = "alertas_ids"
        private const val PREFIX = "alerta_"
        private const val SUFFIX_TITULO = "_titulo"
        private const val SUFFIX_DESC = "_desc"
        private const val SUFFIX_TIPO = "_tipo"
        private const val SUFFIX_FECHA = "_fecha"
        private const val SUFFIX_PRIO = "_prio"
        private const val SUFFIX_DISMISSED = "_dismissed"
        private const val SUFFIX_DEST = "_dest"
        private const val SUFFIX_REF = "_ref"
    }

    private fun getIds(): Set<String> = prefs.getStringSet(KEY_ALERTAS_IDS, emptySet()) ?: emptySet()

    override fun getAlertas(includeDismissed: Boolean): List<Alerta> {
        val ids = getIds()
        val list = mutableListOf<Alerta>()
        for (id in ids) {
            val isDismissed = prefs.getBoolean("$PREFIX$id$SUFFIX_DISMISSED", false)
            if (includeDismissed || !isDismissed) {
                list.add(getAlerta(id))
            }
        }
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val fallbackSdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return list.sortedByDescending { alerta ->
            try {
                sdf.parse(alerta.fecha)?.time ?: 0L
            } catch (e: Exception) {
                try {
                    fallbackSdf.parse(alerta.fecha)?.time ?: 0L
                } catch (e2: Exception) {
                    0L
                }
            }
        }
    }

    private fun getAlerta(id: String): Alerta {
        return Alerta(
            id = id,
            titulo = prefs.getString("$PREFIX$id$SUFFIX_TITULO", "") ?: "",
            descripcion = prefs.getString("$PREFIX$id$SUFFIX_DESC", "") ?: "",
            tipo = TipoAlerta.valueOf(prefs.getString("$PREFIX$id$SUFFIX_TIPO", TipoAlerta.RECORDATORIO.name) ?: TipoAlerta.RECORDATORIO.name),
            fecha = prefs.getString("$PREFIX$id$SUFFIX_FECHA", "") ?: "",
            prioridad = PrioridadAlerta.valueOf(prefs.getString("$PREFIX$id$SUFFIX_PRIO", PrioridadAlerta.MEDIA.name) ?: PrioridadAlerta.MEDIA.name),
            isDismissed = prefs.getBoolean("$PREFIX$id$SUFFIX_DISMISSED", false),
            destinationId = prefs.getInt("$PREFIX$id$SUFFIX_DEST", -1).let { if (it == -1) null else it },
            referenceId = prefs.getString("$PREFIX$id$SUFFIX_REF", null)
        )
    }

    override fun saveAlerta(alerta: Alerta) {
        val ids = getIds().toMutableSet()
        ids.add(alerta.id)
        prefs.edit().apply {
            putStringSet(KEY_ALERTAS_IDS, ids)
            putString("$PREFIX${alerta.id}$SUFFIX_TITULO", alerta.titulo)
            putString("$PREFIX${alerta.id}$SUFFIX_DESC", alerta.descripcion)
            putString("$PREFIX${alerta.id}$SUFFIX_TIPO", alerta.tipo.name)
            putString("$PREFIX${alerta.id}$SUFFIX_FECHA", alerta.fecha)
            putString("$PREFIX${alerta.id}$SUFFIX_PRIO", alerta.prioridad.name)
            putBoolean("$PREFIX${alerta.id}$SUFFIX_DISMISSED", alerta.isDismissed)
            putInt("$PREFIX${alerta.id}$SUFFIX_DEST", alerta.destinationId ?: -1)
            putString("$PREFIX${alerta.id}$SUFFIX_REF", alerta.referenceId)
            apply()
        }
    }

    override fun dismissAlerta(id: String) {
        prefs.edit().putBoolean("$PREFIX$id$SUFFIX_DISMISSED", true).apply()
    }

    override fun restoreAlerta(id: String) {
        prefs.edit().putBoolean("$PREFIX$id$SUFFIX_DISMISSED", false).apply()
    }
}
