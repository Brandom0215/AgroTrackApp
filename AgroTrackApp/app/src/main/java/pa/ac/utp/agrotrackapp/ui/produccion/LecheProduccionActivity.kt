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

class LecheProduccionActivity : AppCompatActivity() {

    private lateinit var animalRepository: AnimalRepository
    private lateinit var produccionRepository: ProduccionRepository
    private lateinit var adapter: LecheAdapter

    private lateinit var tvActiveMilkingCount: TextView
    private lateinit var tvTotalTankLiters: TextView
    private lateinit var tvMilkingEfficiency: TextView
    
    private lateinit var etSearchLeche: TextInputEditText
    private lateinit var rvLeche: RecyclerView

    private var originalList: List<LecheRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leche_produccion)

        // Inicializar repositorios
        animalRepository = SharedPrefsAnimalRepository(this)
        produccionRepository = SharedPrefsProduccionRepository(this)

        // Bind Views
        tvActiveMilkingCount = findViewById(R.id.tvActiveMilkingCount)
        tvTotalTankLiters = findViewById(R.id.tvTotalTankLiters)
        tvMilkingEfficiency = findViewById(R.id.tvMilkingEfficiency)
        etSearchLeche = findViewById(R.id.etSearchLeche)
        rvLeche = findViewById(R.id.rvLeche)

        // Setup Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup RecyclerView
        adapter = LecheAdapter()
        rvLeche.layoutManager = LinearLayoutManager(this)
        rvLeche.adapter = adapter

        // Setup FAB: Ordeño Individual
        findViewById<ExtendedFloatingActionButton>(R.id.fabAddLecheIndividual).setOnClickListener {
            mostrarDialogRegistroIndividual()
        }

        // Setup FAB: Carga Masiva
        findViewById<ExtendedFloatingActionButton>(R.id.fabAddLecheMasivo).setOnClickListener {
            mostrarDialogRegistroMasivo()
        }

        // Setup Search filtering
        etSearchLeche.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarList(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        cargarDatos()
    }

    private fun cargarDatos() {
        originalList = produccionRepository.getLecheRecords().filter { it.activo }
        adapter.submitList(originalList)
        actualizarKpis()
    }

    private fun actualizarKpis() {
        val totalVacas = originalList.size
        val totalLitros = originalList.sumOf { it.litros }
        val eficiencia = if (totalVacas > 0) totalLitros / totalVacas else 0.0

        tvActiveMilkingCount.text = if (totalVacas == 1) "1 Vaca" else "$totalVacas Vacas"
        tvTotalTankLiters.text = String.format(Locale.getDefault(), "%.1f L", totalLitros)
        tvMilkingEfficiency.text = String.format(Locale.getDefault(), "%.1f L/Vaca", eficiencia)
    }

    private fun filtrarList(query: String) {
        val filtered = originalList.filter {
            it.numeroAnimal.contains(query, ignoreCase = true)
        }
        adapter.submitList(filtered)
    }

    private fun mostrarDialogRegistroIndividual() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_registro_leche, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        // Bind dialog views
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

        // Cargar lista de hembras disponibles del ganado general
        val hatoAnimals = animalRepository.getAnimals().filter { it.sexo.equals("Hembra", ignoreCase = true) }
        val aretes = hatoAnimals.map { it.numeroAnimal }
        val autocompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, aretes)
        etRegArete.setAdapter(autocompleteAdapter)

        // Si se selecciona un Arete, jalar datos anteriores en automático
        etRegArete.setOnItemClickListener { _, _, _, _ ->
            val selectedArete = etRegArete.text.toString().trim()
            val previousRecord = produccionRepository.getLecheRecord(selectedArete)
            if (previousRecord != null) {
                etRegFechaParto.setText(previousRecord.fechaUltimoParto)
                etRegLactancias.setText(previousRecord.lactancias.toString())
            }
        }

        // Setup Date Pickers
        setupDatePickerField(etRegFecha)
        setupDatePickerField(etRegFechaParto)

        // Dropdown Turno
        val turnoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("AM", "PM", "Semanal"))
        etRegTurno.setAdapter(turnoAdapter)
        etRegTurno.setText("AM", false)

        dialogView.findViewById<MaterialButton>(R.id.btnCancelLeche).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnSaveLeche).setOnClickListener {
            val arete = etRegArete.text.toString().trim()
            val fecha = etRegFecha.text.toString().trim()
            val turno = etRegTurno.text.toString().trim()
            val litrosStr = etRegLitros.text.toString().trim()
            val fechaParto = etRegFechaParto.text.toString().trim()
            val lactanciasStr = etRegLactancias.text.toString().trim()

            // Reset errors
            tilRegArete.error = null
            tilRegFecha.error = null
            tilRegLitros.error = null
            tilRegFechaParto.error = null
            tilRegLactancias.error = null

            // Validaciones
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

            // Cálculos automáticos
            val del = calculateDaysBetween(fechaParto, fecha)
            val promedio = if (turno == "Semanal") litros / 7.0 else litros

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

            produccionRepository.saveLecheRecord(record)
            cargarDatos()
            dialog.dismiss()
            Toast.makeText(this, "Ordeño registrado exitosamente", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }

    private fun mostrarDialogRegistroMasivo() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_registro_leche_masivo, null)
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .create()

        val rgFrecuencia = dialogView.findViewById<RadioGroup>(R.id.rgFrecuencia)
        val btnCopiarAyer = dialogView.findViewById<MaterialButton>(R.id.btnCopiarAyer)
        val llMasivoContainer = dialogView.findViewById<LinearLayout>(R.id.llMasivoContainer)

        // Obtener todas las vacas registradas (hembras) en la finca
        val vacas = animalRepository.getAnimals().filter { it.sexo.equals("Hembra", ignoreCase = true) }

        if (vacas.isEmpty()) {
            Toast.makeText(this, "No hay vacas hembras registradas en el hato", Toast.LENGTH_LONG).show()
            dialog.dismiss()
            return
        }

        // Crear dinámicamente las filas de carga rápida
        val inputMap = mutableMapOf<String, EditText>()
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

            val tvArete = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                text = vaca.numeroAnimal
                textSize = 14f
                setTextColor(Color.parseColor("#333333"))
            }

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

            inputMap[vaca.numeroAnimal] = etLitros
        }

        // Evento Copiar de Ayer
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

        dialogView.findViewById<MaterialButton>(R.id.btnSaveMasivo).setOnClickListener {
            val isDiario = rgFrecuencia.checkedRadioButtonId == R.id.rbDiario
            val turnoText = if (isDiario) "AM" else "Semanal"
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaHoyStr = simpleDateFormat.format(Date())

            var insertCount = 0
            for ((arete, editText) in inputMap) {
                val litrosStr = editText.text.toString().trim()
                val litros = litrosStr.toDoubleOrNull()
                if (litros != null && litros > 0) {
                    val matchingVaca = vacas.first { it.numeroAnimal == arete }
                    val previousRecord = produccionRepository.getLecheRecord(arete)

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

    private fun sacarDeProduccion(record: LecheRecord) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Sacar y Enviar de Producción")
            .setMessage("¿Está seguro de que desea retirar la vaca ${record.numeroAnimal} de producción?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ ->
                val updated = record.copy(activo = false)
                produccionRepository.updateLecheRecord(updated)
                cargarDatos()
                Toast.makeText(this, "Vaca retirada de producción de leche", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // --- Recycler Adapter ---
    inner class LecheAdapter : RecyclerView.Adapter<LecheAdapter.ViewHolder>() {

        private var list: List<LecheRecord> = emptyList()

        fun submitList(newList: List<LecheRecord>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_leche_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val record = list[position]
            holder.tvArete.text = record.numeroAnimal
            holder.tvParto.text = "Parto: ${record.fechaUltimoParto}"
            holder.tvLactancias.text = record.lactancias.toString()
            holder.tvLitros.text = "${record.litros} L"
            holder.tvTurno.text = "Turno: ${record.turno}"
            holder.tvDEL.text = "${record.del} d"

            holder.btnAction.setOnClickListener {
                sacarDeProduccion(record)
            }
        }

        override fun getItemCount(): Int = list.size

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
