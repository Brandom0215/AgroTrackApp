package pa.ac.utp.agrotrackapp.ui.produccion

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SqliteAnimalRepository
import pa.ac.utp.agrotrackapp.data.produccion.SqliteProduccionRepository
import pa.ac.utp.agrotrackapp.domain.model.CarneRecord
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository
import pa.ac.utp.agrotrackapp.domain.repository.ProduccionRepository
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Actividad para la gestión y control de la producción de carne (ganado de engorde) en AgroTrack.
 * Permite registrar pesajes periódicos, calcular la Ganancia Diaria de Peso (GDP), filtrar el
 * hato por salud/raza/arete y remover animales del ciclo de ceba.
 */
class CarneProduccionActivity : AppCompatActivity() {

    // Repositorios de datos basados en SharedPreferences
    private lateinit var animalRepository: AnimalRepository
    private lateinit var produccionRepository: ProduccionRepository
    private lateinit var adapter: CarneAdapter

    // Componentes visuales principales de la UI
    private lateinit var tvCountCarne: TextView
    private lateinit var etSearchCarne: TextInputEditText
    private lateinit var rvCarne: RecyclerView

    // Cache local de la lista para agilizar búsquedas sin volver a consultar el almacenamiento
    private var originalList: List<CarneRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carne_produccion)

        // Inicializar capas de datos
        animalRepository = SqliteAnimalRepository(this)
        produccionRepository = SqliteProduccionRepository(this)

        // Vincular componentes de la interfaz
        tvCountCarne = findViewById(R.id.tvCountCarne)
        etSearchCarne = findViewById(R.id.etSearchCarne)
        rvCarne = findViewById(R.id.rvCarne)

        // Botón de retorno a la pantalla anterior
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Configuración inicial del listado adaptativo (RecyclerView)
        adapter = CarneAdapter()
        rvCarne.layoutManager = LinearLayoutManager(this)
        rvCarne.adapter = adapter

        // Botón Flotante (FAB) para registrar un nuevo pesaje individual
        findViewById<ExtendedFloatingActionButton>(R.id.fabAddPesaje).setOnClickListener {
            mostrarDialogPesaje()
        }

        // Buscador reactivo multi-parámetro (Filtra conforme el usuario escribe)
        etSearchCarne.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Carga inicial de datos al abrir la pantalla
        cargarDatos()
    }

    /**
     * Recupera del repositorio los animales que continúan activos dentro del lote de carne.
     */
    private fun cargarDatos() {
        originalList = produccionRepository.getCarneRecords().filter { it.activo }
        adapter.submitList(originalList)
        actualizarKpi()
    }

    /**
     * Actualiza el contador de cabezas de ganado en engorde actual.
     */
    private fun actualizarKpi() {
        val size = originalList.size
        tvCountCarne.text = if (size == 1) "1 Animal" else "$size Animales"
    }

    /**
     * Filtra la lista local usando criterios de: Número de arete, raza o estado de salud.
     */
    private fun filtrarList(query: String) {
        val filtered = originalList.filter {
            it.numeroAnimal.contains(query, ignoreCase = true) ||
                    it.raza.contains(query, ignoreCase = true) ||
                    it.estadoSalud.contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }

    /**
     * Construye y muestra el formulario modal para el registro de pesaje de un bovino.
     */
    private fun mostrarDialogPesaje() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_registro_carne, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        // Vincular campos del formulario del diálogo
        val tilRegArete = dialogView.findViewById<TextInputLayout>(R.id.tilRegArete)
        val etRegArete = dialogView.findViewById<AutoCompleteTextView>(R.id.etRegArete)
        val tilRegFechaActual = dialogView.findViewById<TextInputLayout>(R.id.tilRegFechaActual)
        val etRegFechaActual = dialogView.findViewById<TextInputEditText>(R.id.etRegFechaActual)
        val tilRegPesoActual = dialogView.findViewById<TextInputLayout>(R.id.tilRegPesoActual)
        val etRegPesoActual = dialogView.findViewById<TextInputEditText>(R.id.etRegPesoActual)
        val etRegPesoAnterior = dialogView.findViewById<TextInputEditText>(R.id.etRegPesoAnterior)
        val etRegPesoEntrada = dialogView.findViewById<TextInputEditText>(R.id.etRegPesoEntrada)
        val etRegFechaAnterior = dialogView.findViewById<TextInputEditText>(R.id.etRegFechaAnterior)
        val etRegSalud = dialogView.findViewById<AutoCompleteTextView>(R.id.etRegSalud)

        // Autocompletado: Obtener todos los aretes de la finca para agilizar la entrada
        val hatoAnimals = animalRepository.getAnimals()
        val aretes = hatoAnimals.map { it.numeroAnimal }
        val autocompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, aretes)
        etRegArete.setAdapter(autocompleteAdapter)

        // Evento gatillo al seleccionar un animal: Busca datos históricos o iniciales automáticamente
        etRegArete.setOnItemClickListener { _, _, _, _ ->
            val selectedArete = etRegArete.text.toString().trim()
            val previousRecord = produccionRepository.getCarneRecord(selectedArete)

            if (previousRecord != null) {
                // Caso A: El animal ya cuenta con pesajes previos registrados en este módulo
                etRegPesoAnterior.setText(previousRecord.pesoActual.toString())
                etRegFechaAnterior.setText(previousRecord.fechaPesajeActual)
                etRegPesoEntrada.setText(previousRecord.pesoEntrada.toString())
            } else {
                // Caso B: Primer pesaje en ceba; se extraen los datos de nacimiento del registro general
                val animal = hatoAnimals.find { it.numeroAnimal == selectedArete }
                if (animal != null && animal.peso.isNotEmpty()) {
                    etRegPesoAnterior.setText(animal.peso)
                    etRegFechaAnterior.setText(animal.fechaNacimiento)
                    etRegPesoEntrada.setText(animal.peso)
                }
            }
        }

        // Configurar los selectores de fechas visuales
        setupDatePickerField(etRegFechaActual)
        setupDatePickerField(etRegFechaAnterior)

        // Menú desplegable para la categorización clínica rápida del bovino
        val saludAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Sano", "En Tratamiento", "Cuarentena"))
        etRegSalud.setAdapter(saludAdapter)
        etRegSalud.setText("Sano", false)

        dialogView.findViewById<MaterialButton>(R.id.btnCancelPesaje).setOnClickListener {
            dialog.dismiss()
        }

        // Lógica de procesamiento y guardado del pesaje
        dialogView.findViewById<MaterialButton>(R.id.btnSavePesaje).setOnClickListener {
            val arete = etRegArete.text.toString().trim()
            val fechaAct = etRegFechaActual.text.toString().trim()
            val pesoActStr = etRegPesoActual.text.toString().trim()
            val pesoAntStr = etRegPesoAnterior.text.toString().trim()
            val pesoEntStr = etRegPesoEntrada.text.toString().trim()
            val fechaAnt = etRegFechaAnterior.text.toString().trim()
            val salud = etRegSalud.text.toString().trim()

            // Limpieza preventiva de alertas visuales de error
            tilRegArete.error = null
            tilRegFechaActual.error = null
            tilRegPesoActual.error = null

            // --- Validaciones de Consistencia ---
            if (arete.isEmpty()) {
                tilRegArete.error = "Arete requerido"
                return@setOnClickListener
            }
            val matchingAnimal = hatoAnimals.find { it.numeroAnimal == arete }
            if (matchingAnimal == null) {
                tilRegArete.error = "Este animal no está registrado en la finca"
                return@setOnClickListener
            }
            if (fechaAct.isEmpty()) {
                tilRegFechaActual.error = "Fecha requerida"
                return@setOnClickListener
            }
            val pesoAct = pesoActStr.toDoubleOrNull()
            if (pesoAct == null || pesoAct <= 0) {
                tilRegPesoActual.error = "Ingrese un peso válido"
                return@setOnClickListener
            }

            // Fallbacks seguros en caso de valores vacíos
            val pesoAnt = pesoAntStr.toDoubleOrNull() ?: pesoAct
            val pesoEnt = pesoEntStr.toDoubleOrNull() ?: pesoAnt

            // --- Fórmulas de Negocio de Engorde ---
            // 1. Ganancia de peso total desde el último control
            val gt = pesoAct - pesoAnt

            // 2. Días transcurridos entre ambos controles
            val dias = if (fechaAnt.isNotEmpty()) {
                calculateDaysBetween(fechaAnt, fechaAct)
            } else {
                1
            }
            // Control de división por cero o fechas mal configuradas
            val diasSeguros = if (dias <= 0) 1 else dias

            // 3. GDP: Ganancia Diaria de Peso (kg ganados / días transcurridos)
            val gdp = gt / diasSeguros

            // Creación de la entidad con los resultados zootécnicos calculados
            val record = CarneRecord(
                numeroAnimal = arete,
                raza = matchingAnimal.raza,
                fechaPesajeActual = fechaAct,
                pesoActual = pesoAct,
                fechaPesajeAnterior = if (fechaAnt.isNotEmpty()) fechaAnt else fechaAct,
                pesoAnterior = pesoAnt,
                pesoEntrada = pesoEnt,
                gananciaTotal = gt,
                diasTranscurridos = diasSeguros,
                gdp = gdp,
                estadoSalud = salud,
                activo = true
            )

            // Almacenar, refrescar y cerrar modal
            produccionRepository.saveCarneRecord(record)
            cargarDatos()
            dialog.dismiss()
            Toast.makeText(this, "Pesaje registrado exitosamente", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    /**
     * Helper que enlaza un DatePickerDialog nativo a un cuadro de texto.
     */
    private fun setupDatePickerField(editText: TextInputEditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                val dateString = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                editText.setText(dateString)
            }, year, month, day).show()
        }
    }

    /**
     * Calcula los días transcurridos entre dos cadenas de fechas.
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
            0
        }
    }

    /**
     * Ejecuta una baja lógica del animal del lote de ceba actual a través de confirmación.
     */
    private fun sacarDeProduccion(record: CarneRecord) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sacar y Enviar de Producción")
            .setMessage("¿Está seguro de que desea sacar el animal ${record.numeroAnimal} del ciclo de engorde?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ ->
                val updated = record.copy(activo = false) // Baja lógica
                produccionRepository.updateCarneRecord(updated)
                cargarDatos()
                Toast.makeText(this, "Animal retirado de la producción de carne", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // --- Recycler Adapter ---
    /**
     * Controlador encargado de mapear la colección de [CarneRecord] en renglones visuales.
     */
    inner class CarneAdapter : RecyclerView.Adapter<CarneAdapter.ViewHolder>() {

        private var list: List<CarneRecord> = emptyList()

        fun submitList(newList: List<CarneRecord>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_carne_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = list[position]

            // Asignación básica de propiedades
            holder.tvArete.text = record.numeroAnimal
            holder.tvRaza.text = record.raza
            holder.tvPesoActual.text = "${record.pesoActual} kg"
            holder.tvPesoAnterior.text = "Ant: ${record.pesoAnterior} kg"

            // Renderizado condicional del GDP: Verde si gana peso, Rojo si pierde peso
            val formattedGdp = String.format(Locale.getDefault(), "%.2f", record.gdp)
            holder.tvGDP.text = if (record.gdp >= 0) "+$formattedGdp kg/día" else "$formattedGdp kg/día"
            holder.tvGDP.setTextColor(if (record.gdp >= 0) Color.parseColor("#2E7D32") else Color.RED)

            holder.tvSalud.text = record.estadoSalud

            // Acción para sacar de producción desde la propia fila
            holder.btnAction.setOnClickListener {
                sacarDeProduccion(record)
            }
        }

        override fun getItemCount(): Int = list.size

        /**
         * Mapeo de vistas internas de cada celda del listado.
         */
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvArete: TextView = v.findViewById(R.id.tvRowArete)
            val tvRaza: TextView = v.findViewById(R.id.tvRowRaza)
            val tvPesoActual: TextView = v.findViewById(R.id.tvRowPesoActual)
            val tvPesoAnterior: TextView = v.findViewById(R.id.tvRowPesoAnterior)
            val tvGDP: TextView = v.findViewById(R.id.tvRowGDP)
            val tvSalud: TextView = v.findViewById(R.id.tvRowSalud)
            val btnAction: MaterialButton = v.findViewById(R.id.btnRowAction)
        }
    }
}