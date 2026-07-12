package pa.ac.utp.agrotrackapp.ui.mortalidad

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SqliteAnimalRepository
import pa.ac.utp.agrotrackapp.data.mortalidad.SqliteMortalidadRepository
import pa.ac.utp.agrotrackapp.domain.model.MortalidadRecord
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository
import pa.ac.utp.agrotrackapp.domain.repository.MortalidadRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class MortalidadFragment : Fragment() {

    private lateinit var animalRepository: AnimalRepository
    private lateinit var mortalidadRepository: MortalidadRepository
    private lateinit var adapter: MortalidadAdapter

    private lateinit var tvTasaMortalidad: TextView
    private lateinit var tvCausaPrincipal: TextView
    private lateinit var barChartMortalidad: BarChart
    private lateinit var rvHistorial: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mortalidad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos repositorios
        animalRepository = SqliteAnimalRepository(requireContext())
        mortalidadRepository = SqliteMortalidadRepository(requireContext())

        // Vincular vistas de la barra superior
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // Vincular KPIs e Historial
        tvTasaMortalidad = view.findViewById(R.id.tvTasaMortalidad)
        tvCausaPrincipal = view.findViewById(R.id.tvCausaPrincipal)
        barChartMortalidad = view.findViewById(R.id.barChartMortalidad)
        rvHistorial = view.findViewById(R.id.rvHistorial)

        setupBarChart()

        // Configurar RecyclerView
        adapter = MortalidadAdapter()
        rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        rvHistorial.adapter = adapter

        // Configurar FAB
        view.findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            mostrarDialogRegistroMortalidad()
        }

        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        val records = mortalidadRepository.getMortalidadRecords()
        adapter.submitList(records.reversed()) // Mostrar primero los más recientes

        // Calcular Tasa de Mortalidad: (Muertes / (Vivos + Muertes)) * 100
        val totalVivos = animalRepository.getAnimals().size
        val totalMuertes = records.size
        val denominador = totalVivos + totalMuertes
        val tasa = if (denominador > 0) {
            (totalMuertes.toDouble() / denominador) * 100
        } else {
            0.0
        }
        tvTasaMortalidad.text = String.format(Locale.getDefault(), "%.1f%%", tasa)

        // Calcular Causa Principal
        if (records.isEmpty()) {
            tvCausaPrincipal.text = "Ninguna"
        } else {
            val causaMode = records.groupBy { it.causa }
                .maxByOrNull { it.value.size }?.key ?: "Ninguna"
            tvCausaPrincipal.text = causaMode
        }

        actualizarGrafica(records)
    }

    private fun setupBarChart() {
        barChartMortalidad.description.isEnabled = false
        barChartMortalidad.setNoDataText("Cargando datos...")
        barChartMortalidad.setDrawGridBackground(false)
        barChartMortalidad.setDrawBarShadow(false)
        barChartMortalidad.setDrawValueAboveBar(true)
        barChartMortalidad.setPinchZoom(false)
        barChartMortalidad.setScaleEnabled(false)

        val xAxis = barChartMortalidad.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.setDrawLabels(true)

        barChartMortalidad.axisLeft.setDrawGridLines(true)
        barChartMortalidad.axisLeft.axisMinimum = 0f
        barChartMortalidad.axisRight.isEnabled = false
        barChartMortalidad.legend.isEnabled = false
    }

    private fun actualizarGrafica(records: List<MortalidadRecord>) {
        if (records.isEmpty()) {
            barChartMortalidad.clear()
            barChartMortalidad.setNoDataText("No hay muertes registradas")
            barChartMortalidad.invalidate()
            return
        }

        // Obtener los últimos 6 meses (incluyendo el actual)
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val keyFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        val monthLabels = mutableListOf<String>()
        val monthKeys = mutableListOf<String>()

        for (i in 5 downTo 0) {
            val tempCal = calendar.clone() as Calendar
            tempCal.add(Calendar.MONTH, -i)
            monthLabels.add(monthFormat.format(tempCal.time))
            monthKeys.add(keyFormat.format(tempCal.time))
        }

        // Contar muertes por mes
        val countsByMonth = records.groupBy { record ->
            try {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(record.fechaMuerte)
                if (date != null) {
                    SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(date)
                } else ""
            } catch (e: Exception) { "" }
        }.mapValues { it.value.size }

        val entries = mutableListOf<BarEntry>()
        monthKeys.forEachIndexed { index, key ->
            val count = countsByMonth[key] ?: 0
            entries.add(BarEntry(index.toFloat(), count.toFloat()))
        }

        val dataSet = BarDataSet(entries, "Muertes")
        dataSet.color = Color.parseColor("#D32F2F") // Rojo
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.BLACK
        // Formatear valores como enteros
        dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String = value.toInt().toString()
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f

        barChartMortalidad.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < monthLabels.size) monthLabels[index] else ""
            }
        }
        barChartMortalidad.data = barData
        barChartMortalidad.animateY(1000)
        barChartMortalidad.invalidate()
    }

    private fun mostrarDialogRegistroMortalidad() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_registro_mortalidad, null)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        // Bind dialog views
        val tilMortalidadArete = dialogView.findViewById<TextInputLayout>(R.id.tilMortalidadArete)
        val etMortalidadArete = dialogView.findViewById<AutoCompleteTextView>(R.id.etMortalidadArete)
        val tilMortalidadCausa = dialogView.findViewById<TextInputLayout>(R.id.tilMortalidadCausa)
        val etMortalidadCausa = dialogView.findViewById<AutoCompleteTextView>(R.id.etMortalidadCausa)
        val tilMortalidadOtraCausa = dialogView.findViewById<TextInputLayout>(R.id.tilMortalidadOtraCausa)
        val etMortalidadOtraCausa = dialogView.findViewById<TextInputEditText>(R.id.etMortalidadOtraCausa)
        val tilMortalidadFecha = dialogView.findViewById<TextInputLayout>(R.id.tilMortalidadFecha)
        val etMortalidadFecha = dialogView.findViewById<TextInputEditText>(R.id.etMortalidadFecha)
        val tilMortalidadDetalles = dialogView.findViewById<TextInputLayout>(R.id.tilMortalidadDetalles)
        val etMortalidadDetalles = dialogView.findViewById<TextInputEditText>(R.id.etMortalidadDetalles)

        // Autocomplete de Aretes de animales vivos registrados
        val activeAnimals = animalRepository.getAnimals()
        val aretes = activeAnimals.map { it.numeroAnimal }
        val areteAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, aretes)
        etMortalidadArete.setAdapter(areteAdapter)

        // Dropdown de Causas preestablecidas
        val causasAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, arrayOf("Diarrea", "Digestivo", "Otra causa"))
        etMortalidadCausa.setAdapter(causasAdapter)
        etMortalidadCausa.setText("Diarrea", false) // Valor por defecto

        // Escucha cambios en la causa para mostrar/ocultar el campo de texto manual
        etMortalidadCausa.setOnItemClickListener { _, _, position, _ ->
            val seleccion = causasAdapter.getItem(position)
            if (seleccion == "Otra causa") {
                tilMortalidadOtraCausa.visibility = View.VISIBLE
            } else {
                tilMortalidadOtraCausa.visibility = View.GONE
                etMortalidadOtraCausa.setText("")
            }
        }

        // DatePicker para la fecha de deceso
        etMortalidadFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val dateString = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                etMortalidadFecha.setText(dateString)
            }, year, month, day)
            
            // La fecha no se puede elegir una fecha futura
            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnCancelMortalidad).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnSaveMortalidad).setOnClickListener {
            val arete = etMortalidadArete.text.toString().trim()
            val causaSel = etMortalidadCausa.text.toString().trim()
            val causaManual = etMortalidadOtraCausa.text.toString().trim()
            val fecha = etMortalidadFecha.text.toString().trim()
            val detalles = etMortalidadDetalles.text.toString().trim()

            // Resetear errores
            tilMortalidadArete.error = null
            tilMortalidadCausa.error = null
            tilMortalidadOtraCausa.error = null
            tilMortalidadFecha.error = null
            tilMortalidadDetalles.error = null

            // 1. Validar Arete
            if (arete.isEmpty()) {
                tilMortalidadArete.error = "El número de arete es requerido"
                return@setOnClickListener
            }
            val animalExiste = activeAnimals.any { it.numeroAnimal == arete }
            if (!animalExiste) {
                tilMortalidadArete.error = "Este animal no está registrado como activo en la finca"
                return@setOnClickListener
            }

            // 2. Validar Causa
            val causaFinal = if (causaSel == "Otra causa") {
                if (causaManual.isEmpty()) {
                    tilMortalidadOtraCausa.error = "Escriba la causa de muerte"
                    return@setOnClickListener
                }
                causaManual
            } else {
                causaSel
            }

            // 3. Validar Fecha
            if (fecha.isEmpty()) {
                tilMortalidadFecha.error = "La fecha de muerte es requerida"
                return@setOnClickListener
            }
            
            // Validar que la fecha no sea futura
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDate = sdf.parse(fecha)
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }.time
                
                if (selectedDate != null && selectedDate.after(today)) {
                    tilMortalidadFecha.error = "No se permiten fechas futuras"
                    return@setOnClickListener
                }
            } catch (e: Exception) {
                tilMortalidadFecha.error = "Formato de fecha inválido"
                return@setOnClickListener
            }

            // 4. Validar Detalles (Largo máx. 100 caracteres)
            if (detalles.isEmpty()) {
                tilMortalidadDetalles.error = "Ingrese detalles u observaciones de la muerte"
                return@setOnClickListener
            }
            if (detalles.length > 100) {
                tilMortalidadDetalles.error = "Los detalles deben tener menos de 100 caracteres"
                return@setOnClickListener
            }

            // Crear y Guardar el registro de mortalidad
            val record = MortalidadRecord(
                numeroAnimal = arete,
                causa = causaFinal,
                fechaMuerte = fecha,
                detalles = detalles
            )

            mortalidadRepository.saveMortalidadRecord(record)

            // Dar de baja (Eliminar) el animal del hato de animales vivos
            animalRepository.deleteAnimal(arete)

            // Recargar datos en la UI y notificar
            cargarDatos()
            dialog.dismiss()
            Toast.makeText(requireContext(), "Mortalidad registrada y animal dado de baja del hato", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }
}
