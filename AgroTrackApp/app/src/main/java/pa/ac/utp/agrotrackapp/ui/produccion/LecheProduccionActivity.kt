package pa.ac.utp.agrotrackapp.ui.produccion

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SharedPrefsAnimalRepository
import pa.ac.utp.agrotrackapp.data.produccion.SharedPrefsProduccionRepository
import pa.ac.utp.agrotrackapp.domain.model.LecheRecord
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository
import pa.ac.utp.agrotrackapp.domain.repository.ProduccionRepository
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Actividad principal para la gestión y registro de la producción de leche en la aplicación AgroTrack.
 * Permite visualizar KPIs, buscar registros, agregar pesajes de forma individual o masiva,
 * y retirar animales del estado de producción activa.
 */
class LecheProduccionActivity : AppCompatActivity() {

    // Repositorios para la persistencia de datos (SharedPreferences)
    private lateinit var animalRepository: AnimalRepository
    private lateinit var produccionRepository: ProduccionRepository
    private lateinit var adapter: LecheAdapter

    // Elementos de la interfaz de usuario (Componentes de KPIs)
    private lateinit var tvActiveMilkingCount: TextView
    private lateinit var tvTotalTankLiters: TextView
    private lateinit var tvMilkingEfficiency: TextView

    // Componentes de búsqueda y listado
    private lateinit var etSearchLeche: TextInputEditText
    private lateinit var rvLeche: RecyclerView

    // Lista en memoria para almacenar los registros activos y facilitar el filtrado
    private var originalList: List<LecheRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leche_produccion)

        // Inicializar repositorios encargados del acceso a datos
        animalRepository = SharedPrefsAnimalRepository(this)
        produccionRepository = SharedPrefsProduccionRepository(this)

        // Vincular vistas del layout XML con los objetos Kotlin
        tvActiveMilkingCount = findViewById(R.id.tvActiveMilkingCount)
        tvTotalTankLiters = findViewById(R.id.tvTotalTankLiters)
        tvMilkingEfficiency = findViewById(R.id.tvMilkingEfficiency)
        etSearchLeche = findViewById(R.id.etSearchLeche)
        rvLeche = findViewById(R.id.rvLeche)

        // Configurar botón de regreso (Finaliza la actividad actual)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Configuración del RecyclerView para listar el histórico de ordeño
        adapter = LecheAdapter()
        rvLeche.layoutManager = LinearLayoutManager(this)
        rvLeche.adapter = adapter

        // Configurar el FAB para abrir el formulario de Registro Individual
        findViewById<ExtendedFloatingActionButton>(R.id.fabAddLecheIndividual).setOnClickListener {
            mostrarDialogRegistroIndividual()
        }

        // Configurar el FAB para abrir el formulario de Carga Masiva (Por lotes)
        findViewById<ExtendedFloatingActionButton>(R.id.fabAddLecheMasivo).setOnClickListener {
            mostrarDialogRegistroMasivo()
        }

        // Implementar filtro de búsqueda en tiempo real mediante el número de arete
        etSearchLeche.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Carga inicial de la base de datos o almacenamiento local
        cargarDatos()
    }

    /**
     * Recupera los registros de producción vigentes y actualiza la UI.
     */
    private fun cargarDatos() {
        // Filtrar solo los registros que se encuentren activos en producción
        originalList = produccionRepository.getLecheRecords().filter { it.activo }
        adapter.submitList(originalList)
        actualizarKpis()
    }

    /**
     * Calcula y renderiza los indicadores clave de rendimiento (KPIs) en la parte superior.
     */
    private fun actualizarKpis() {
        val totalVacas = originalList.size
        val totalLitros = originalList.sumOf { it.litros }
        // Evitar división por cero si no hay vacas registradas
        val eficiencia = if (totalVacas > 0) totalLitros / totalVacas else 0.0

        // Asignación de textos formateados a la UI
        tvActiveMilkingCount.text = if (totalVacas == 1) "1 Vaca" else "$totalVacas Vacas"
        tvTotalTankLiters.text = String.format(Locale.getDefault(), "%.1f L", totalLitros)
        tvMilkingEfficiency.text = String.format(Locale.getDefault(), "%.1f L/Vaca", eficiencia)
    }

    /**
     * Filtra la lista del RecyclerView según el texto ingresado en la barra de búsqueda.
     */
    private fun filtrarList(query: String) {
        val filtered = originalList.filter {
            it.numeroAnimal.contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }

    /**
     * Despliega un diálogo emergente (Modal) para registrar la producción de una sola vaca.
     */
    private fun mostrarDialogRegistroIndividual() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_registro_leche, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        // Vincular componentes internos del diálogo
        val tilRegArete = dialogView.findViewById<TextInputLayout>(R.id.tilRegArete)
        val etRegArete = dialogView.findViewById<AutoCompleteTextView>(R.id.etRegArete)
        val tilRegFecha = dialogView.findViewById<TextInputLayout>(R.id.tilRegFecha)
        val etRegFecha = dialogView.findViewById<TextInputEditText>(R.id.etRegFecha)
        val etRegTurno = dialogView.findViewById<AutoCompleteTextView>(R.id.etRegTurno)
        val tilRegLitros = dialogView.findViewById<TextInputLayout>(R.id.tilRegLitros)
        val etRegLitros = dialogView.findViewById<TextInputEditText>(R.id.etRegLitros)
        val tilRegFechaParto = dialogView.findViewById<TextInputLayout>(R.id.tilRegFechaParto)
        val etRegFechaParto = dialogView.findViewById<TextInputEditText>(R.id.etRegFechaParto)
        val tilRegLactancias = dialogView.findViewById<TextInputLayout>(R.id.tilRegLactancias)
        val etRegLactancias = dialogView.findViewById<TextInputEditText>(R.id.etRegLactancias)

        // Cargar y filtrar el hato general para mostrar únicamente las hembras en el autocompletado
        val hatoAnimals = animalRepository.getAnimals().filter { it.sexo.equals("Hembra", ignoreCase = true) }
        val aretes = hatoAnimals.map { it.numeroAnimal }
        val autocompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, aretes)
        etRegArete.setAdapter(autocompleteAdapter)

        // Lógica de autocompletado: Al elegir un arete, precarga la fecha de parto y lactancias anteriores
        etRegArete.setOnItemClickListener { _, _, _, _ ->
            val selectedArete = etRegArete.text.toString().trim()
            val previousRecord = produccionRepository.getLecheRecord(selectedArete)
            if (previousRecord != null) {
                etRegFechaParto.setText(previousRecord.fechaUltimoParto)
                etRegLactancias.setText(previousRecord.lactancias.toString())
            }
        }

        // Configuración de los selectores de fecha de tipo calendario
        setupDatePickerField(etRegFecha)
        setupDatePickerField(etRegFechaParto)

        // Configuración del menú desplegable para los turnos de ordeño
        val turnoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("AM", "PM", "Semanal"))
        etRegTurno.setAdapter(turnoAdapter)
        etRegTurno.setText("AM", false) // Valor por defecto

        // Acción del botón Cancelar
        dialogView.findViewById<MaterialButton>(R.id.btnCancelLeche).setOnClickListener {
            dialog.dismiss()
        }

        // Acción del botón Guardar (Contiene validaciones de negocio)
        dialogView.findViewById<MaterialButton>(R.id.btnSaveLeche).setOnClickListener {
            val arete = etRegArete.text.toString().trim()
            val fecha = etRegFecha.text.toString().trim()
            val turno = etRegTurno.text.toString().trim()
            val litrosStr = etRegLitros.text.toString().trim()
            val fechaParto = etRegFechaParto.text.toString().trim()
            val lactanciasStr = etRegLactancias.text.toString().trim()

            // Limpieza de estados de error visuales previos
            tilRegArete.error = null
            tilRegFecha.error = null
            tilRegLitros.error = null
            tilRegFechaParto.error = null
            tilRegLactancias.error = null

            // --- Bloque de Validaciones Estrictas ---
            if (arete.isEmpty()) {
                tilRegArete.error = "Arete requerido"
                return@setOnClickListener
            }
            val matchingAnimal = hatoAnimals.find { it.numeroAnimal == arete }
            if (matchingAnimal == null) {
                tilRegArete.error = "Este animal no está registrado en la finca como hembra"
                return@setOnClickListener
            }
            if (fecha.isEmpty()) {
                tilRegFecha.error = "Fecha requerida"
                return@setOnClickListener
            }
            val litros = litrosStr.toDoubleOrNull()
            if (litros == null || litros <= 0) {
                tilRegLitros.error = "Ingrese litros válidos"
                return@setOnClickListener
            }
            if (fechaParto.isEmpty()) {
                tilRegFechaParto.error = "Fecha de parto requerida"
                return@setOnClickListener
            }
            val lactancias = lactanciasStr.toIntOrNull()
            if (lactancias == null || lactancias < 0) {
                tilRegLactancias.error = "Ingrese un número válido"
                return@setOnClickListener
            }

            // --- Cálculos Automáticos Técnicos ---
            // DEL = Días En Lactancia (Días transcurridos desde el parto hasta el pesaje actual)
            val del = calculateDaysBetween(fechaParto, fecha)
            // Si el registro fue semanal, prorratea el promedio diario entre 7 días
            val promedio = if (turno == "Semanal") litros / 7.0 else litros

            // Construcción del modelo de datos
            val record = LecheRecord(
                numeroAnimal = arete,
                fechaRegistro = fecha,
                turno = turno,
                litros = litros,
                fechaUltimoParto = fechaParto,
                lactancias = lactancias,
                del = del,
                promedioDiario = promedio,
                activo = true
            )

            // Persistencia, actualización de UI y cierre de diálogo
            produccionRepository.saveLecheRecord(record)
            cargarDatos()
            dialog.dismiss()
            Toast.makeText(this, "Ordeño registrado exitosamente", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    /**
     * Despliega un diálogo avanzado para la captura masiva de datos en lote.
     * Genera la interfaz de manera dinámica basándose en todas las vacas disponibles.
     */
    private fun mostrarDialogRegistroMasivo() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_registro_leche_masivo, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        val rgFrecuencia = dialogView.findViewById<RadioGroup>(R.id.rgFrecuencia)
        val btnCopiarAyer = dialogView.findViewById<MaterialButton>(R.id.btnCopiarAyer)
        val llMasivoContainer = dialogView.findViewById<LinearLayout>(R.id.llMasivoContainer)

        // Obtener inventario de hembras
        val vacas = animalRepository.getAnimals().filter { it.sexo.equals("Hembra", ignoreCase = true) }

        if (vacas.isEmpty()) {
            Toast.makeText(this, "No hay vacas hembras registradas en el hato", Toast.LENGTH_LONG).show()
            dialog.dismiss()
            return
        }

        // Mapa en memoria para asociar el arete del animal con su campo EditText dinámico
        val inputMap = mutableMapOf<String, EditText>()

        // Construcción dinámica de renglones en la interfaz por cada vaca
        for (vaca in vacas) {
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
                gravity = Gravity.CENTER_VERTICAL
            }

            // Etiqueta identificadora del animal
            val tvArete = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = vaca.numeroAnimal
                textSize = 14f
                setTextColor(Color.parseColor("#333333"))
            }

            // Campo de entrada numérico decimal para los litros producidos
            val etLitros = EditText(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f)
                hint = "0.0 L"
                textSize = 14f
                gravity = Gravity.END
                inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            row.addView(tvArete)
            row.addView(etLitros)
            llMasivoContainer.addView(row)

            // Mapear el componente para recuperar su valor posteriormente
            inputMap[vaca.numeroAnimal] = etLitros
        }

        // Funcionalidad de conveniencia: Copia los últimos litros registrados para agilizar el llenado
        btnCopiarAyer.setOnClickListener {
            var count = 0
            for ((arete, editText) in inputMap) {
                val previousRecord = produccionRepository.getLecheRecord(arete)
                if (previousRecord != null) {
                    editText.setText(previousRecord.litros.toString())
                    count++
                }
            }
            if (count > 0) {
                Toast.makeText(this, "Se copiaron $count registros de producción de ayer", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se encontraron registros previos en producción", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<MaterialButton>(R.id.btnCancelMasivo).setOnClickListener {
            dialog.dismiss()
        }

        // Procesamiento masivo de los registros ingresados
        dialogView.findViewById<MaterialButton>(R.id.btnSaveMasivo).setOnClickListener {
            val isDiario = rgFrecuencia.checkedRadioButtonId == R.id.rbDiario
            val turnoText = if (isDiario) "AM" else "Semanal"
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaHoyStr = simpleDateFormat.format(Date())

            var insertCount = 0
            // Iterar sobre el mapa de componentes dinámicos
            for ((arete, editText) in inputMap) {
                val litrosStr = editText.text.toString().trim()
                val litros = litrosStr.toDoubleOrNull()

                // Solo procesa y guarda aquellas filas donde se digitó un valor mayor a cero
                if (litros != null && litros > 0) {
                    val matchingVaca = vacas.first { it.numeroAnimal == arete }
                    val previousRecord = produccionRepository.getLecheRecord(arete)

                    // Si no tiene registros previos de parto, hereda la fecha de nacimiento como fallback
                    val fechaParto = previousRecord?.fechaUltimoParto ?: matchingVaca.fechaNacimiento
                    val lactancias = previousRecord?.lactancias ?: 1
                    val del = calculateDaysBetween(fechaParto, fechaHoyStr)
                    val promedio = if (isDiario) litros else litros / 7.0

                    val record = LecheRecord(
                        numeroAnimal = arete,
                        fechaRegistro = fechaHoyStr,
                        turno = turnoText,
                        litros = litros,
                        fechaUltimoParto = fechaParto,
                        lactancias = lactancias,
                        del = del,
                        promedioDiario = promedio,
                        activo = true
                    )

                    produccionRepository.saveLecheRecord(record)
                    insertCount++
                }
            }

            if (insertCount > 0) {
                cargarDatos()
                dialog.dismiss()
                Toast.makeText(this, "Se registraron $insertCount vacas en lote", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ingrese producción al menos para una vaca", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    /**
     * Helper para asociar un DatePickerDialog nativo a un TextInputEditText.
     */
    private fun setupDatePickerField(editText: TextInputEditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                // Formatear mes (+1) porque Calendar.MONTH va de 0 a 11
                val dateString = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                editText.setText(dateString)
            }, year, month, day).show()
        }
    }

    /**
     * Calcula la diferencia en días absolutos entre dos fechas con formato "dd/MM/yyyy".
     * Utilizado para calcular la métrica DEL (Días en Lactancia).
     */
    private fun calculateDaysBetween(startDateStr: String, endDateStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val start = sdf.parse(startDateStr)
            val end = sdf.parse(endDateStr)
            if (start != null && end != null) {
                val diffInMillis = end.time - start.time
                val days = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS).toInt()
                if (days < 0) 0 else days
            } else {
                0
            }
        } catch (e: Exception) {
            0 // Retorna 0 ante cualquier error de parsing de fechas
        }
    }

    /**
     * Desactiva un registro de producción mediante un diálogo de confirmación.
     * Hace un borrado lógico (activo = false) para retirar al animal del hato de ordeño actual.
     */
    private fun sacarDeProduccion(record: LecheRecord) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sacar y Enviar de Producción")
            .setMessage("¿Está seguro de que desea retirar la vaca ${record.numeroAnimal} de producción?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ ->
                val updated = record.copy(activo = false) // Inactivación lógica
                produccionRepository.updateLecheRecord(updated)
                cargarDatos() // Refrescar interfaz
                Toast.makeText(this, "Vaca retirada de producción de leche", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // --- Recycler Adapter (Clase Interna) ---
    /**
     * Adaptador para gestionar el inflado y vinculación de datos en el listado RecyclerView.
     */
    inner class LecheAdapter : RecyclerView.Adapter<LecheAdapter.ViewHolder>() {

        private var list: List<LecheRecord> = emptyList()

        /**
         * Actualiza la lista interna de datos y notifica los cambios al adaptador.
         */
        fun submitList(newList: List<LecheRecord>) {
            list = newList
            notifyDataSetChanged() // Notificación global de refresco
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leche_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = list[position]
            // Enlace de datos de producción zootécnica a las vistas del renglón
            holder.tvArete.text = record.numeroAnimal
            holder.tvParto.text = "Parto: ${record.fechaUltimoParto}"
            holder.tvLactancias.text = record.lactancias.toString()
            holder.tvLitros.text = "${record.litros} L"
            holder.tvTurno.text = "Turno: ${record.turno}"
            holder.tvDEL.text = "${record.del} d" // Días En Lactancia

            // Configurar el botón de acción del renglón (Sacar de producción)
            holder.btnAction.setOnClickListener {
                sacarDeProduccion(record)
            }
        }

        override fun getItemCount(): Int = list.size

        /**
         * Caché de vistas para optimizar el rendimiento del scroll.
         */
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvArete: TextView = v.findViewById(R.id.tvRowLecheArete)
            val tvParto: TextView = v.findViewById(R.id.tvRowLecheParto)
            val tvLactancias: TextView = v.findViewById(R.id.tvRowLecheLactancias)
            val tvLitros: TextView = v.findViewById(R.id.tvRowLecheLitros)
            val tvTurno: TextView = v.findViewById(R.id.tvRowLecheTurno)
            val tvDEL: TextView = v.findViewById(R.id.tvRowLecheDEL)
            val btnAction: MaterialButton = v.findViewById(R.id.btnRowLecheAction)
        }
    }
}