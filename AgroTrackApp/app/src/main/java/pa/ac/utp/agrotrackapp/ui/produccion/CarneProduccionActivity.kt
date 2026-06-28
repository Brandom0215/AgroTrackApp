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
import pa.ac.utp.agrotrackapp.data.animal.SharedPrefsAnimalRepository
import pa.ac.utp.agrotrackapp.data.produccion.SharedPrefsProduccionRepository
import pa.ac.utp.agrotrackapp.domain.model.CarneRecord
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository
import pa.ac.utp.agrotrackapp.domain.repository.ProduccionRepository
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class CarneProduccionActivity : AppCompatActivity() {

    private lateinit var animalRepository: AnimalRepository
    private lateinit var produccionRepository: ProduccionRepository
    private lateinit var adapter: CarneAdapter
    
    private lateinit var tvCountCarne: TextView
    private lateinit var etSearchCarne: TextInputEditText
    private lateinit var rvCarne: RecyclerView

    private var originalList: List<CarneRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carne_produccion)

        // Inicializar repositorios
        animalRepository = SharedPrefsAnimalRepository(this)
        produccionRepository = SharedPrefsProduccionRepository(this)

        // Bind Views
        tvCountCarne = findViewById(R.id.tvCountCarne)
        etSearchCarne = findViewById(R.id.etSearchCarne)
        rvCarne = findViewById(R.id.rvCarne)

        // Setup Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        adapter = CarneAdapter()
        rvCarne.layoutManager = LinearLayoutManager(this)
        rvCarne.adapter = adapter

        // Setup FAB click
        findViewById<ExtendedFloatingActionButton>(R.id.fabAddPesaje).setOnClickListener {
            mostrarDialogPesaje()
        }

        // Setup Search filtering
        etSearchCarne.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cargarDatos()
    }

    private fun cargarDatos() {
        originalList = produccionRepository.getCarneRecords().filter { it.activo }
        adapter.submitList(originalList)
        actualizarKpi()
    }

    private fun actualizarKpi() {
        val size = originalList.size
        tvCountCarne.text = if (size == 1) "1 Animal" else "$size Animales"
    }

    private fun filtrarList(query: String) {
        val filtered = originalList.filter {
            it.numeroAnimal.contains(query, ignoreCase = true) ||
            it.raza.contains(query, ignoreCase = true) ||
            it.estadoSalud.contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }

    private fun mostrarDialogPesaje() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_registro_carne, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        // Bind dialog views
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

        // Cargar lista de aretes disponibles del ganado general para Autocompletar
        val hatoAnimals = animalRepository.getAnimals()
        val aretes = hatoAnimals.map { it.numeroAnimal }
        val autocompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, aretes)
        etRegArete.setAdapter(autocompleteAdapter)

        // Si se selecciona un Arete, jalar datos anteriores en automático
        etRegArete.setOnItemClickListener { _, _, _, _ ->
            val selectedArete = etRegArete.text.toString().trim()
            val previousRecord = produccionRepository.getCarneRecord(selectedArete)
            if (previousRecord != null) {
                etRegPesoAnterior.setText(previousRecord.pesoActual.toString())
                etRegFechaAnterior.setText(previousRecord.fechaPesajeActual)
                etRegPesoEntrada.setText(previousRecord.pesoEntrada.toString())
            } else {
                // Si no tiene registro previo, buscar el peso inicial registrado en el animal
                val animal = hatoAnimals.find { it.numeroAnimal == selectedArete }
                if (animal != null && animal.peso.isNotEmpty()) {
                    etRegPesoAnterior.setText(animal.peso)
                    etRegFechaAnterior.setText(animal.fechaNacimiento)
                    etRegPesoEntrada.setText(animal.peso)
                }
            }
        }

        // Setup Date Pickers
        setupDatePickerField(etRegFechaActual)
        setupDatePickerField(etRegFechaAnterior)

        // Dropdown de salud
        val saludAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Sano", "En Tratamiento", "Cuarentena"))
        etRegSalud.setAdapter(saludAdapter)
        etRegSalud.setText("Sano", false)

        dialogView.findViewById<MaterialButton>(R.id.btnCancelPesaje).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnSavePesaje).setOnClickListener {
            val arete = etRegArete.text.toString().trim()
            val fechaAct = etRegFechaActual.text.toString().trim()
            val pesoActStr = etRegPesoActual.text.toString().trim()
            val pesoAntStr = etRegPesoAnterior.text.toString().trim()
            val pesoEntStr = etRegPesoEntrada.text.toString().trim()
            val fechaAnt = etRegFechaAnterior.text.toString().trim()
            val salud = etRegSalud.text.toString().trim()

            // Reset errors
            tilRegArete.error = null
            tilRegFechaActual.error = null
            tilRegPesoActual.error = null

            // 1. Validaciones de Arete
            if (arete.isEmpty()) {
                tilRegArete.error = "Arete requerido"
                return@setOnClickListener
            }
            val matchingAnimal = hatoAnimals.find { it.numeroAnimal == arete }
            if (matchingAnimal == null) {
                tilRegArete.error = "Este animal no está registrado en la finca"
                return@setOnClickListener
            }

            // 2. Otras validaciones
            if (fechaAct.isEmpty()) {
                tilRegFechaActual.error = "Fecha requerida"
                return@setOnClickListener
            }
            val pesoAct = pesoActStr.toDoubleOrNull()
            if (pesoAct == null || pesoAct <= 0) {
                tilRegPesoActual.error = "Ingrese un peso válido"
                return@setOnClickListener
            }

            val pesoAnt = pesoAntStr.toDoubleOrNull() ?: pesoAct
            val pesoEnt = pesoEntStr.toDoubleOrNull() ?: pesoAnt

            // 3. Cálculos automáticos
            val gt = pesoAct - pesoAnt
            val dias = if (fechaAnt.isNotEmpty()) {
                calculateDaysBetween(fechaAnt, fechaAct)
            } else {
                1
            }
            val diasSeguros = if (dias <= 0) 1 else dias
            val gdp = gt / diasSeguros

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

            produccionRepository.saveCarneRecord(record)
            cargarDatos()
            dialog.dismiss()
            Toast.makeText(this, "Pesaje registrado exitosamente", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

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

    private fun sacarDeProduccion(record: CarneRecord) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sacar y Enviar de Producción")
            .setMessage("¿Está seguro de que desea sacar el animal ${record.numeroAnimal} del ciclo de engorde?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ ->
                val updated = record.copy(activo = false)
                produccionRepository.updateCarneRecord(updated)
                cargarDatos()
                Toast.makeText(this, "Animal retirado de la producción de carne", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // --- Recycler Adapter ---
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
            holder.tvArete.text = record.numeroAnimal
            holder.tvRaza.text = record.raza
            holder.tvPesoActual.text = "${record.pesoActual} kg"
            holder.tvPesoAnterior.text = "Ant: ${record.pesoAnterior} kg"
            
            val formattedGdp = String.format(Locale.getDefault(), "%.2f", record.gdp)
            holder.tvGDP.text = if (record.gdp >= 0) "+$formattedGdp kg" else "$formattedGdp kg"
            holder.tvGDP.setTextColor(if (record.gdp >= 0) Color.parseColor("#1A5C38") else Color.RED)
            
            holder.tvSalud.text = record.estadoSalud
            
            holder.btnAction.setOnClickListener {
                sacarDeProduccion(record)
            }
        }

        override fun getItemCount(): Int = list.size

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
