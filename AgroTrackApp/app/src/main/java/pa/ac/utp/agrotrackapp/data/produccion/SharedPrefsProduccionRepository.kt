package pa.ac.utp.agrotrackapp.data.produccion

import android.content.Context
import android.content.SharedPreferences
import pa.ac.utp.agrotrackapp.data.alertas.AlertManager
import pa.ac.utp.agrotrackapp.domain.model.CarneRecord
import pa.ac.utp.agrotrackapp.domain.model.LecheRecord
import pa.ac.utp.agrotrackapp.domain.repository.ProduccionRepository

class SharedPrefsProduccionRepository(private val context: Context) : ProduccionRepository {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("GanaDEXProduccionPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CARNE_ARETES = "carne_aretes"
        private const val PREFIX_CARNE = "carne_"
        
        private const val KEY_LECHE_ARETES = "leche_aretes"
        private const val PREFIX_LECHE = "leche_"

        // Carne field suffixes
        private const val SUFFIX_RAZA = "_raza"
        private const val SUFFIX_FECHA_ACTUAL = "_fecha_actual"
        private const val SUFFIX_PESO_ACTUAL = "_peso_actual"
        private const val SUFFIX_FECHA_ANTERIOR = "_fecha_anterior"
        private const val SUFFIX_PESO_ANTERIOR = "_peso_anterior"
        private const val SUFFIX_PESO_ENTRADA = "_peso_entrada"
        private const val SUFFIX_GANANCIA_TOTAL = "_ganancia_total"
        private const val SUFFIX_DIAS_TRANSCURRIDOS = "_dias_transcurridos"
        private const val SUFFIX_GDP = "_gdp"
        private const val SUFFIX_SALUD = "_salud"
        private const val SUFFIX_ACTIVO = "_activo"

        // Leche field suffixes
        private const val SUFFIX_FECHA_REGISTRO = "_fecha_registro"
        private const val SUFFIX_TURNO = "_turno"
        private const val SUFFIX_LITROS = "_litros"
        private const val SUFFIX_FECHA_PARTO = "_fecha_parto"
        private const val SUFFIX_LACTANCIAS = "_lactancias"
        private const val SUFFIX_DEL = "_del"
        private const val SUFFIX_PROMEDIO_DIARIO = "_promedio_diario"
    }

    // --- Carne Helpers ---
    private fun getCarneAretesSet(): Set<String> {
        return sharedPreferences.getStringSet(KEY_CARNE_ARETES, emptySet()) ?: emptySet()
    }

    private fun saveCarneAretesSet(set: Set<String>) {
        sharedPreferences.edit().putStringSet(KEY_CARNE_ARETES, set).apply()
    }

    override fun getCarneRecords(): List<CarneRecord> {
        val aretes = getCarneAretesSet()
        val list = mutableListOf<CarneRecord>()
        for (arete in aretes) {
            val record = getCarneRecord(arete)
            if (record != null) {
                list.add(record)
            }
        }
        return list
    }

    override fun saveCarneRecord(record: CarneRecord): Result<Unit> {
        return try {
            val arete = record.numeroAnimal.trim()
            val aretes = getCarneAretesSet().toMutableSet()
            aretes.add(arete)
            saveCarneAretesSet(aretes)

            persistCarneData(arete, record)
            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCarneRecord(numeroAnimal: String): CarneRecord? {
        val arete = numeroAnimal.trim()
        val hasRecord = sharedPreferences.contains("$PREFIX_CARNE$arete$SUFFIX_ACTIVO")
        if (!hasRecord) return null

        return CarneRecord(
            numeroAnimal = arete,
            raza = sharedPreferences.getString("$PREFIX_CARNE$arete$SUFFIX_RAZA", "") ?: "",
            fechaPesajeActual = sharedPreferences.getString("$PREFIX_CARNE$arete$SUFFIX_FECHA_ACTUAL", "") ?: "",
            pesoActual = sharedPreferences.getFloat("$PREFIX_CARNE$arete$SUFFIX_PESO_ACTUAL", 0f).toDouble(),
            fechaPesajeAnterior = sharedPreferences.getString("$PREFIX_CARNE$arete$SUFFIX_FECHA_ANTERIOR", "") ?: "",
            pesoAnterior = sharedPreferences.getFloat("$PREFIX_CARNE$arete$SUFFIX_PESO_ANTERIOR", 0f).toDouble(),
            pesoEntrada = sharedPreferences.getFloat("$PREFIX_CARNE$arete$SUFFIX_PESO_ENTRADA", 0f).toDouble(),
            gananciaTotal = sharedPreferences.getFloat("$PREFIX_CARNE$arete$SUFFIX_GANANCIA_TOTAL", 0f).toDouble(),
            diasTranscurridos = sharedPreferences.getInt("$PREFIX_CARNE$arete$SUFFIX_DIAS_TRANSCURRIDOS", 0),
            gdp = sharedPreferences.getFloat("$PREFIX_CARNE$arete$SUFFIX_GDP", 0f).toDouble(),
            estadoSalud = sharedPreferences.getString("$PREFIX_CARNE$arete$SUFFIX_SALUD", "") ?: "",
            activo = sharedPreferences.getBoolean("$PREFIX_CARNE$arete$SUFFIX_ACTIVO", true)
        )
    }

    override fun updateCarneRecord(record: CarneRecord): Result<Unit> {
        return saveCarneRecord(record)
    }

    override fun deleteCarneRecord(numeroAnimal: String): Result<Unit> {
        return try {
            val arete = numeroAnimal.trim()
            val aretes = getCarneAretesSet().toMutableSet()
            if (aretes.contains(arete)) {
                aretes.remove(arete)
                saveCarneAretesSet(aretes)
            }
            removeCarneData(arete)
            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun persistCarneData(arete: String, record: CarneRecord) {
        sharedPreferences.edit().apply {
            putString("$PREFIX_CARNE$arete$SUFFIX_RAZA", record.raza)
            putString("$PREFIX_CARNE$arete$SUFFIX_FECHA_ACTUAL", record.fechaPesajeActual)
            putFloat("$PREFIX_CARNE$arete$SUFFIX_PESO_ACTUAL", record.pesoActual.toFloat())
            putString("$PREFIX_CARNE$arete$SUFFIX_FECHA_ANTERIOR", record.fechaPesajeAnterior)
            putFloat("$PREFIX_CARNE$arete$SUFFIX_PESO_ANTERIOR", record.pesoAnterior.toFloat())
            putFloat("$PREFIX_CARNE$arete$SUFFIX_PESO_ENTRADA", record.pesoEntrada.toFloat())
            putFloat("$PREFIX_CARNE$arete$SUFFIX_GANANCIA_TOTAL", record.gananciaTotal.toFloat())
            putInt("$PREFIX_CARNE$arete$SUFFIX_DIAS_TRANSCURRIDOS", record.diasTranscurridos)
            putFloat("$PREFIX_CARNE$arete$SUFFIX_GDP", record.gdp.toFloat())
            putString("$PREFIX_CARNE$arete$SUFFIX_SALUD", record.estadoSalud)
            putBoolean("$PREFIX_CARNE$arete$SUFFIX_ACTIVO", record.activo)
            apply()
        }
    }

    private fun removeCarneData(arete: String) {
        sharedPreferences.edit().apply {
            remove("$PREFIX_CARNE$arete$SUFFIX_RAZA")
            remove("$PREFIX_CARNE$arete$SUFFIX_FECHA_ACTUAL")
            remove("$PREFIX_CARNE$arete$SUFFIX_PESO_ACTUAL")
            remove("$PREFIX_CARNE$arete$SUFFIX_FECHA_ANTERIOR")
            remove("$PREFIX_CARNE$arete$SUFFIX_PESO_ANTERIOR")
            remove("$PREFIX_CARNE$arete$SUFFIX_PESO_ENTRADA")
            remove("$PREFIX_CARNE$arete$SUFFIX_GANANCIA_TOTAL")
            remove("$PREFIX_CARNE$arete$SUFFIX_DIAS_TRANSCURRIDOS")
            remove("$PREFIX_CARNE$arete$SUFFIX_GDP")
            remove("$PREFIX_CARNE$arete$SUFFIX_SALUD")
            remove("$PREFIX_CARNE$arete$SUFFIX_ACTIVO")
            apply()
        }
    }


    // --- Leche Helpers ---
    private fun getLecheAretesSet(): Set<String> {
        return sharedPreferences.getStringSet(KEY_LECHE_ARETES, emptySet()) ?: emptySet()
    }

    private fun saveLecheAretesSet(set: Set<String>) {
        sharedPreferences.edit().putStringSet(KEY_LECHE_ARETES, set).apply()
    }

    override fun getLecheRecords(): List<LecheRecord> {
        val aretes = getLecheAretesSet()
        val list = mutableListOf<LecheRecord>()
        for (arete in aretes) {
            val record = getLecheRecord(arete)
            if (record != null) {
                list.add(record)
            }
        }
        return list
    }

    override fun saveLecheRecord(record: LecheRecord): Result<Unit> {
        return try {
            val arete = record.numeroAnimal.trim()
            val aretes = getLecheAretesSet().toMutableSet()
            aretes.add(arete)
            saveLecheAretesSet(aretes)

            persistLecheData(arete, record)
            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getLecheRecord(numeroAnimal: String): LecheRecord? {
        val arete = numeroAnimal.trim()
        val hasRecord = sharedPreferences.contains("$PREFIX_LECHE$arete$SUFFIX_ACTIVO")
        if (!hasRecord) return null

        return LecheRecord(
            numeroAnimal = arete,
            fechaRegistro = sharedPreferences.getString("$PREFIX_LECHE$arete$SUFFIX_FECHA_REGISTRO", "") ?: "",
            turno = sharedPreferences.getString("$PREFIX_LECHE$arete$SUFFIX_TURNO", "") ?: "",
            litros = sharedPreferences.getFloat("$PREFIX_LECHE$arete$SUFFIX_LITROS", 0f).toDouble(),
            fechaUltimoParto = sharedPreferences.getString("$PREFIX_LECHE$arete$SUFFIX_FECHA_PARTO", "") ?: "",
            lactancias = sharedPreferences.getInt("$PREFIX_LECHE$arete$SUFFIX_LACTANCIAS", 0),
            del = sharedPreferences.getInt("$PREFIX_LECHE$arete$SUFFIX_DEL", 0),
            promedioDiario = sharedPreferences.getFloat("$PREFIX_LECHE$arete$SUFFIX_PROMEDIO_DIARIO", 0f).toDouble(),
            activo = sharedPreferences.getBoolean("$PREFIX_LECHE$arete$SUFFIX_ACTIVO", true)
        )
    }

    override fun updateLecheRecord(record: LecheRecord): Result<Unit> {
        return saveLecheRecord(record)
    }

    override fun deleteLecheRecord(numeroAnimal: String): Result<Unit> {
        return try {
            val arete = numeroAnimal.trim()
            val aretes = getLecheAretesSet().toMutableSet()
            if (aretes.contains(arete)) {
                aretes.remove(arete)
                saveLecheAretesSet(aretes)
            }
            removeLecheData(arete)
            AlertManager(context).checkAlerts()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun persistLecheData(arete: String, record: LecheRecord) {
        sharedPreferences.edit().apply {
            putString("$PREFIX_LECHE$arete$SUFFIX_FECHA_REGISTRO", record.fechaRegistro)
            putString("$PREFIX_LECHE$arete$SUFFIX_TURNO", record.turno)
            putFloat("$PREFIX_LECHE$arete$SUFFIX_LITROS", record.litros.toFloat())
            putString("$PREFIX_LECHE$arete$SUFFIX_FECHA_PARTO", record.fechaUltimoParto)
            putInt("$PREFIX_LECHE$arete$SUFFIX_LACTANCIAS", record.lactancias)
            putInt("$PREFIX_LECHE$arete$SUFFIX_DEL", record.del)
            putFloat("$PREFIX_LECHE$arete$SUFFIX_PROMEDIO_DIARIO", record.promedioDiario.toFloat())
            putBoolean("$PREFIX_LECHE$arete$SUFFIX_ACTIVO", record.activo)
            apply()
        }
    }

    private fun removeLecheData(arete: String) {
        sharedPreferences.edit().apply {
            remove("$PREFIX_LECHE$arete$SUFFIX_FECHA_REGISTRO")
            remove("$PREFIX_LECHE$arete$SUFFIX_TURNO")
            remove("$PREFIX_LECHE$arete$SUFFIX_LITROS")
            remove("$PREFIX_LECHE$arete$SUFFIX_FECHA_PARTO")
            remove("$PREFIX_LECHE$arete$SUFFIX_LACTANCIAS")
            remove("$PREFIX_LECHE$arete$SUFFIX_DEL")
            remove("$PREFIX_LECHE$arete$SUFFIX_PROMEDIO_DIARIO")
            remove("$PREFIX_LECHE$arete$SUFFIX_ACTIVO")
            apply()
        }
    }
}
