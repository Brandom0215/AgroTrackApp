package pa.ac.utp.agrotrackapp.data.mortalidad

import android.content.Context
import android.content.SharedPreferences
import pa.ac.utp.agrotrackapp.data.alertas.AlertManager
import pa.ac.utp.agrotrackapp.domain.model.MortalidadRecord
import pa.ac.utp.agrotrackapp.domain.repository.MortalidadRepository

class SharedPrefsMortalidadRepository(private val context: Context) : MortalidadRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("GanaDEXMortalidadPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MORTALIDAD_LIST = "mortalidad_list"
        private const val PREFIX_RECORD = "mortalidad_"
        private const val SUFFIX_CAUSA = "_causa"
        private const val SUFFIX_FECHA = "_fecha"
        private const val SUFFIX_DETALLES = "_detalles"
    }

    private fun getMortalidadSet(): Set<String> {
        return sharedPreferences.getStringSet(KEY_MORTALIDAD_LIST, emptySet()) ?: emptySet()
    }

    private fun saveMortalidadSet(set: Set<String>) {
        sharedPreferences.edit().putStringSet(KEY_MORTALIDAD_LIST, set).apply()
    }

    override fun getMortalidadRecords(): List<MortalidadRecord> {
        val aretes = getMortalidadSet()
        val list = mutableListOf<MortalidadRecord>()
        for (arete in aretes) {
            val record = getMortalidadRecord(arete)
            if (record != null) {
                list.add(record)
            }
        }
        return list
    }

    private fun getMortalidadRecord(arete: String): MortalidadRecord? {
        val causa = sharedPreferences.getString("$PREFIX_RECORD$arete$SUFFIX_CAUSA", null) ?: return null
        val fecha = sharedPreferences.getString("$PREFIX_RECORD$arete$SUFFIX_FECHA", "") ?: ""
        val detalles = sharedPreferences.getString("$PREFIX_RECORD$arete$SUFFIX_DETALLES", "") ?: ""

        return MortalidadRecord(
            numeroAnimal = arete,
            causa = causa,
            fechaMuerte = fecha,
            detalles = detalles
        )
    }

    override fun saveMortalidadRecord(record: MortalidadRecord): Result<Unit> {
        return try {
            val arete = record.numeroAnimal.trim()
            val set = getMortalidadSet().toMutableSet()
            set.add(arete)
            saveMortalidadSet(set)

            sharedPreferences.edit().apply {
                putString("$PREFIX_RECORD$arete$SUFFIX_CAUSA", record.causa)
                putString("$PREFIX_RECORD$arete$SUFFIX_FECHA", record.fechaMuerte)
                putString("$PREFIX_RECORD$arete$SUFFIX_DETALLES", record.detalles)
                apply()
            }
            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun deleteMortalidadRecord(numeroAnimal: String): Result<Unit> {
        return try {
            val arete = numeroAnimal.trim()
            val set = getMortalidadSet().toMutableSet()
            if (set.contains(arete)) {
                set.remove(arete)
                saveMortalidadSet(set)
            }
            sharedPreferences.edit().apply {
                remove("$PREFIX_RECORD$arete$SUFFIX_CAUSA")
                remove("$PREFIX_RECORD$arete$SUFFIX_FECHA")
                remove("$PREFIX_RECORD$arete$SUFFIX_DETALLES")
                apply()
            }
            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
