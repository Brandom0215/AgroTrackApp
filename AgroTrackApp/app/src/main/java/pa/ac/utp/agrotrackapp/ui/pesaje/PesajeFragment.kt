package pa.ac.utp.agrotrackapp.ui.pesaje

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SqliteAnimalRepository
import pa.ac.utp.agrotrackapp.data.produccion.SqliteProduccionRepository
import pa.ac.utp.agrotrackapp.domain.model.Animal
import pa.ac.utp.agrotrackapp.domain.model.CarneRecord
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository
import pa.ac.utp.agrotrackapp.domain.repository.ProduccionRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PesajeFragment : Fragment() {

    private lateinit var animalRepository: AnimalRepository
    private lateinit var produccionRepository: ProduccionRepository

    private lateinit var etFecha: TextInputEditText
    private lateinit var etHora: TextInputEditText
    private lateinit var etBuscar: TextInputEditText
    private lateinit var etPeso: TextInputEditText
    private lateinit var rvPesajeAnimales: RecyclerView
    private lateinit var btnCancelar: MaterialButton
    private lateinit var btnRegistrar: MaterialButton

    private lateinit var adapter: PesajeAnimalAdapter
    private var allAnimalsList: List<Animal> = emptyList()
    private var filteredAnimalsList: List<Animal> = emptyList()
    private var selectedAnimal: Animal? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pesaje, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar repositorios
        animalRepository = SqliteAnimalRepository(requireContext())
        produccionRepository = SqliteProduccionRepository(requireContext())

        // Configurar Barra Superior
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // Vincular componentes del diseño
        etFecha = view.findViewById(R.id.etFecha)
        etHora = view.findViewById(R.id.etHora)
        etBuscar = view.findViewById(R.id.etBuscar)
        etPeso = view.findViewById(R.id.etPeso)

        // Filtro para máximo 4 dígitos y 2 decimales (ej. 9999.99)
        etPeso.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(4, 2))

        rvPesajeAnimales = view.findViewById(R.id.rvPesajeAnimales)
        btnCancelar = view.findViewById(R.id.btnCancelar)
        btnRegistrar = view.findViewById(R.id.btnRegistrar)

        // Precargar Fecha y Hora actuales
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        etFecha.setText(currentDate)
        etHora.setText(currentTime)

        // Configurar diálogos interactivos de Fecha y Hora
        setupDatePickerField(etFecha)
        setupTimePickerField(etHora)

        // Cargar listado de animales
        cargarAnimales()

        // Configurar RecyclerView de selección
        adapter = PesajeAnimalAdapter()
        rvPesajeAnimales.layoutManager = LinearLayoutManager(requireContext())
        rvPesajeAnimales.adapter = adapter
        adapter.submitList(filteredAnimalsList)

        // Buscador de animales por Arete
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarAnimales(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Botones de acción
        btnCancelar.setOnClickListener {
            limpiarFormulario()
        }

        btnRegistrar.setOnClickListener {
            registrarPesaje()
        }
    }

    private fun cargarAnimales() {
        allAnimalsList = animalRepository.getAnimals()
        filteredAnimalsList = allAnimalsList
    }

    override fun onResume() {
        super.onResume()
        cargarAnimales()
        if (::adapter.isInitialized) {
            adapter.submitList(filteredAnimalsList)
        }
    }

    private fun filtrarAnimales(query: String) {
        filteredAnimalsList = if (query.isEmpty()) {
            allAnimalsList
        } else {
            allAnimalsList.filter { it.numeroAnimal.contains(query, ignoreCase = true) }
        }
        adapter.submitList(filteredAnimalsList)
    }

    private fun setupDatePickerField(editText: TextInputEditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val dateString = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                editText.setText(dateString)
            }, year, month, day)

            // La fecha no se puede elegir una fecha futura
            datePicker.datePicker.maxDate = System.currentTimeMillis()
            datePicker.show()
        }
    }

    private fun setupTimePickerField(editText: TextInputEditText) {
        editText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                val amPm = if (selectedHour < 12) "AM" else "PM"
                val displayHour = when {
                    selectedHour == 0 -> 12
                    selectedHour > 12 -> selectedHour - 12
                    else -> selectedHour
                }
                val timeString = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, selectedMinute, amPm)
                editText.setText(timeString)
            }, hour, minute, false).show()
        }
    }

    private fun limpiarFormulario() {
        selectedAnimal = null
        adapter.notifyDataSetChanged()
        etBuscar.setText("")
        etPeso.setText("0.00")
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        etFecha.setText(currentDate)
        etHora.setText(currentTime)
    }

    private fun registrarPesaje() {
        val animal = selectedAnimal
        if (animal == null) {
            Toast.makeText(requireContext(), "Por favor, seleccione un animal de la lista", Toast.LENGTH_SHORT).show()
            return
        }

        val pesoStr = etPeso.text.toString().trim()
        val pesoVal = pesoStr.toDoubleOrNull()
        if (pesoVal == null || pesoVal <= 0) {
            Toast.makeText(requireContext(), "Ingrese un peso válido mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        if (pesoStr.replace(".", "").length > 6) {
            Toast.makeText(requireContext(), "El peso ingresado no es válido", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaText = etFecha.text.toString().trim()
        if (fechaText.isEmpty()) {
            Toast.makeText(requireContext(), "La fecha de registro es requerida", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar que la fecha no sea futura
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val selectedDate = sdf.parse(fechaText)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time

            if (selectedDate != null && selectedDate.after(today)) {
                Toast.makeText(requireContext(), "No se puede registrar un pesaje con fecha futura", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Formato de fecha inválido", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Sincronizar en Hato General: actualizar el peso en el registro del animal
        val animalActualizado = animal.copy(peso = pesoVal.toString())
        animalRepository.updateAnimal(animalActualizado.numeroAnimal, animalActualizado)

        // 2. Sincronizar en Producción de Carne: si el animal está en ceba, actualizar su registro de carne
        val recordCarnePrevio = produccionRepository.getCarneRecord(animal.numeroAnimal)
        if (recordCarnePrevio != null) {
            val pesoAnt = recordCarnePrevio.pesoActual
            val fechaAnt = recordCarnePrevio.fechaPesajeActual

            // Calcular ganancia
            val gt = pesoVal - pesoAnt
            val dias = calculateDaysBetween(fechaAnt, fechaText)
            val diasSeguros = if (dias <= 0) 1 else dias
            val gdp = gt / diasSeguros

            val recordCarneActualizado = recordCarnePrevio.copy(
                fechaPesajeActual = fechaText,
                pesoActual = pesoVal,
                fechaPesajeAnterior = fechaAnt,
                pesoAnterior = pesoAnt,
                gananciaTotal = recordCarnePrevio.gananciaTotal + gt, // Acumulado de ganancia total
                diasTranscurridos = recordCarnePrevio.diasTranscurridos + diasSeguros, // Acumulado de días
                gdp = gdp
            )
            produccionRepository.saveCarneRecord(recordCarneActualizado)
        }

        Toast.makeText(requireContext(), "Pesaje registrado y sincronizado exitosamente", Toast.LENGTH_SHORT).show()

        // Recargar listados y limpiar
        cargarAnimales()
        limpiarFormulario()
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

    // Adaptador Interno del Listado de Selección de Animales
    private inner class PesajeAnimalAdapter : RecyclerView.Adapter<PesajeAnimalAdapter.ViewHolder>() {

        private var list: List<Animal> = emptyList()

        fun submitList(newList: List<Animal>) {
            list = newList
            notifyDataSetChanged()
        }

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val rlAnimalItem: RelativeLayout = v.findViewById(R.id.rlAnimalItem)
            val tvArete: TextView = v.findViewById(R.id.tvRowArete)
            val tvDetails: TextView = v.findViewById(R.id.tvRowDetails)
            val ivSelect: ImageView = v.findViewById(R.id.ivSelect)
            val imgAnimal: ImageView = v.findViewById(R.id.imgAnimal)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_pesaje_animal_row, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val animal = list[position]
            holder.tvArete.text = "Arete: ${animal.numeroAnimal}"
            holder.tvDetails.text = "${animal.sexo} · Raza: ${animal.raza} · Peso: ${animal.peso} kg"

            val isSelected = selectedAnimal?.numeroAnimal == animal.numeroAnimal
            val primaryColor = Color.parseColor("#2E7D32")

            if (isSelected) {
                holder.rlAnimalItem.setBackgroundColor(primaryColor)
                holder.tvArete.setTextColor(Color.WHITE)
                holder.tvDetails.setTextColor(Color.WHITE)
                holder.imgAnimal.imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
                holder.ivSelect.setImageResource(R.drawable.ic_check_white)
                holder.ivSelect.imageTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
                holder.ivSelect.visibility = View.VISIBLE
            } else {
                holder.rlAnimalItem.setBackgroundColor(Color.TRANSPARENT)
                holder.tvArete.setTextColor(Color.BLACK)
                holder.tvDetails.setTextColor(Color.parseColor("#757575"))
                holder.imgAnimal.imageTintList = android.content.res.ColorStateList.valueOf(primaryColor)
                holder.ivSelect.visibility = View.INVISIBLE
            }

            holder.rlAnimalItem.setOnClickListener {
                selectedAnimal = if (isSelected) null else animal
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int = list.size
    }

    /**
     * Filtro para limitar la cantidad de dígitos y decimales en un EditText.
     */
    inner class DecimalDigitsInputFilter(digitsBeforeZero: Int, digitsAfterZero: Int) : InputFilter {
        private val mPattern = java.util.regex.Pattern.compile("[0-9]{0,$digitsBeforeZero}+((\\.[0-9]{0,$digitsAfterZero})?)||(\\.)?")
        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            val matcher = mPattern.matcher(dest.subSequence(0, dstart).toString() + source.subSequence(start, end).toString() + dest.subSequence(dend, dest.length).toString())
            return if (!matcher.matches()) "" else null
        }
    }
}
