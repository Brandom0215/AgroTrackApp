package pa.ac.utp.agrotrackapp.ui.control

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.alertas.AlertManager
import pa.ac.utp.agrotrackapp.data.animal.SqliteAnimalRepository
import pa.ac.utp.agrotrackapp.data.sanidad.SqliteSanidadRepository
import pa.ac.utp.agrotrackapp.domain.model.RegistroSanitario
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository
import pa.ac.utp.agrotrackapp.domain.repository.SanidadRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class ControlSanitarioFragment : Fragment(R.layout.fragment_control_sanitario) {

    private lateinit var sanidadRepository: SanidadRepository
    private lateinit var animalRepository: AnimalRepository
    private lateinit var proximasAdapter: SanidadAdapter
    private lateinit var historialAdapter: SanidadHistorialAdapter
    private lateinit var rvSanidad: RecyclerView
    private lateinit var tvEmptySanidad: TextView
    private lateinit var tvKpiVacunas: TextView
    private lateinit var tvKpiPendientes: TextView
    private lateinit var toggleGroup: MaterialButtonToggleGroup

    private var isProximasMode = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        sanidadRepository = SqliteSanidadRepository(requireContext())
        animalRepository = SqliteAnimalRepository(requireContext())

        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        rvSanidad = view.findViewById(R.id.rvSanidad)
        tvEmptySanidad = view.findViewById(R.id.tvEmptySanidad)
        tvKpiVacunas = view.findViewById(R.id.tvKpiVacunas)
        tvKpiPendientes = view.findViewById(R.id.tvKpiPendientes)
        toggleGroup = view.findViewById(R.id.toggleGroup)

        proximasAdapter = SanidadAdapter { registro ->
            marcarComoAplicado(registro)
        }
        historialAdapter = SanidadHistorialAdapter()

        rvSanidad.layoutManager = LinearLayoutManager(requireContext())

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isProximasMode = checkedId == R.id.btnProximas
                cargarRegistros()
            }
        }

        view.findViewById<ExtendedFloatingActionButton>(R.id.fabAddSanidad).setOnClickListener {
            mostrarDialogNuevoRegistro()
        }

        cargarRegistros()
        actualizarKPIs()
    }

    private fun cargarRegistros() {
        if (isProximasMode) {
            proximasAdapter.isProximasMode = true
            rvSanidad.adapter = proximasAdapter
            val proximas = sanidadRepository.getProximos()
            proximasAdapter.submitList(proximas)
            tvEmptySanidad.text = "No hay aplicaciones próximas"
            tvEmptySanidad.visibility = if (proximas.isEmpty()) View.VISIBLE else View.GONE
            rvSanidad.visibility = if (proximas.isEmpty()) View.GONE else View.VISIBLE
        } else {
            rvSanidad.adapter = historialAdapter
            // Group ALL records by grupoId (most recent groups first)
            val all = sanidadRepository.getAllRegistros()
            val groups = agruparPorGrupoId(all)
            historialAdapter.submitList(groups)
            tvEmptySanidad.text = "No hay historial de registros"
            tvEmptySanidad.visibility = if (groups.isEmpty()) View.VISIBLE else View.GONE
            rvSanidad.visibility = if (groups.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    /** Groups records by grupoId, each legacy record (empty grupoId) becomes its own group.
     *  Groups are sorted by the MOST RECENT dose date in each group (newest group at top). */
    private fun agruparPorGrupoId(all: List<RegistroSanitario>): List<List<RegistroSanitario>> {
        val map = linkedMapOf<String, MutableList<RegistroSanitario>>()
        all.forEach { r ->
            val key = r.grupoId.ifEmpty { r.id }
            map.getOrPut(key) { mutableListOf() }.add(r)
        }
        return map.values
            // Sort each group's doses chronologically (earliest dose first)
            .map { group ->
                group.sortedBy { r ->
                    val d = if (r.estado == "aplicado") r.fecha else r.proximaDosis
                    d.ifEmpty { "0000-00-00" }
                }
            }
            // Sort groups: the group with the LATEST dose date comes first
            .sortedByDescending { group ->
                group.maxOfOrNull { r ->
                    val d = if (r.estado == "aplicado") r.fecha else r.proximaDosis
                    d.ifEmpty { "0000-00-00" }
                } ?: "0000-00-00"
            }
    }

    private fun actualizarKPIs() {
        val historial = sanidadRepository.getRegistros()
        val proximas = sanidadRepository.getProximos()

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentMonth = sdf.format(Date()).substring(0, 7)
        
        val vacunasMes = historial.count { it.categoria == "Vacunación" && it.fecha.startsWith(currentMonth) }
        
        tvKpiVacunas.text = vacunasMes.toString()
        tvKpiPendientes.text = proximas.size.toString()
    }

    private fun marcarComoAplicado(registro: RegistroSanitario) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaActual = sdf.format(Date())
        // Update the existing record in-place: set estado=aplicado, fecha=hoy
        sanidadRepository.marcarAplicado(registro.id, fechaActual)
        AlertManager(requireContext()).checkAlerts()
        Toast.makeText(requireContext(), "Registro marcado como aplicado", Toast.LENGTH_SHORT).show()
        cargarRegistros()
        actualizarKPIs()
    }

    private fun mostrarDialogNuevoRegistro() {
        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_nuevo_registro_sanitario, null)
        dialog.setContentView(view)

        val etIdentificador = view.findViewById<AutoCompleteTextView>(R.id.etIdentificador)
        val rgAlcance       = view.findViewById<RadioGroup>(R.id.rgAlcance)
        val spCategoria     = view.findViewById<AutoCompleteTextView>(R.id.spCategoria)

        val llVacunacion    = view.findViewById<LinearLayout>(R.id.llVacunacion)
        val etVacuna        = view.findViewById<TextInputEditText>(R.id.etVacuna)
        val llTratamiento   = view.findViewById<LinearLayout>(R.id.llTratamiento)
        val etTipoTrat      = view.findViewById<TextInputEditText>(R.id.etTipoTratamiento)
        val etProposito     = view.findViewById<TextInputEditText>(R.id.etPropositoTratamiento)
        val llMastitis      = view.findViewById<LinearLayout>(R.id.llMastitis)
        val cbAD            = view.findViewById<CheckBox>(R.id.cbAD)
        val cbPD            = view.findViewById<CheckBox>(R.id.cbPD)
        val cbAI            = view.findViewById<CheckBox>(R.id.cbAI)
        val cbPI            = view.findViewById<CheckBox>(R.id.cbPI)
        val tilProducto     = view.findViewById<TextInputLayout>(R.id.tilProductoComun)
        val etProducto      = view.findViewById<TextInputEditText>(R.id.etProductoComun)

        val etDosis         = view.findViewById<TextInputEditText>(R.id.etDosis)
        val etVeterinario   = view.findViewById<TextInputEditText>(R.id.etVeterinario)

        val spRepeticion    = view.findViewById<AutoCompleteTextView>(R.id.spRepeticion)
        val tilFechaUnica   = view.findViewById<TextInputLayout>(R.id.tilFechaUnica)
        val etFechaUnica    = view.findViewById<TextInputEditText>(R.id.etFechaUnica)
        val llFechas        = view.findViewById<LinearLayout>(R.id.llFechasContainer)

        val btnGuardar      = view.findViewById<MaterialButton>(R.id.btnGuardar)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()

        // --- Identificador autocomplete ---
        val animalesVivos = animalRepository.getAnimals()
        val aretes = animalesVivos.map { it.numeroAnimal }
        val mangas = animalesVivos.map { it.manga }.filter { it.isNotEmpty() }.distinct()
        var currentList = aretes
        etIdentificador.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, aretes))

        rgAlcance.setOnCheckedChangeListener { _, id ->
            currentList = if (id == R.id.rbMasivo) mangas else aretes
            etIdentificador.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currentList))
            etIdentificador.setText("")
        }

        // --- Categoría dinámica ---
        val categorias = resources.getStringArray(R.array.sanitaria_categorias)
        spCategoria.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categorias))
        spCategoria.setOnItemClickListener { _, _, pos, _ ->
            val cat = categorias[pos]
            llVacunacion.visibility  = if (cat == "Vacunación")  View.VISIBLE else View.GONE
            llTratamiento.visibility = if (cat == "Tratamiento") View.VISIBLE else View.GONE
            llMastitis.visibility    = if (cat == "Mastitis")    View.VISIBLE else View.GONE
            tilProducto.visibility   = if (cat != "Vacunación")  View.VISIBLE else View.GONE
        }

        // --- Repetición y fechas ---
        val repeticiones = resources.getStringArray(R.array.sanitaria_repeticiones)
        spRepeticion.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, repeticiones))

        // Set today as default for "Única"
        etFechaUnica.setText(sdf.format(today.time))
        etFechaUnica.setOnClickListener {
            DatePickerDialog(requireContext(), { _, y, m, d ->
                etFechaUnica.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d))
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show()
        }

        fun buildDateCheckboxes(dates: List<Pair<String, String>>) {
            // dates = list of (label, isoDate)
            llFechas.removeAllViews()
            val labelView = TextView(requireContext()).apply {
                text = "Selecciona las fechas:"
                setTextColor(0xFF4A5568.toInt())
                textSize = 14f
                setPadding(0, 0, 0, 8)
            }
            llFechas.addView(labelView)
            for ((label, iso) in dates) {
                val cb = CheckBox(requireContext()).apply {
                    text = label
                    tag = iso
                    isChecked = true
                }
                llFechas.addView(cb)
            }
        }

        spRepeticion.setOnItemClickListener { _, _, pos, _ ->
            val rep = repeticiones[pos]
            when (rep) {
                "Única" -> {
                    tilFechaUnica.visibility = View.VISIBLE
                    llFechas.visibility      = View.GONE
                }
                "Diaria" -> {
                    tilFechaUnica.visibility = View.GONE
                    llFechas.visibility      = View.VISIBLE
                    val days = (0..6).map { offset ->
                        val cal = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, offset) }
                        val iso = sdf.format(cal.time)
                        val label = when (offset) {
                            0 -> "Hoy ($iso)"
                            1 -> "Mañana ($iso)"
                            else -> "En $offset días ($iso)"
                        }
                        Pair(label, iso)
                    }
                    buildDateCheckboxes(days)
                }
                "Semanal" -> {
                    tilFechaUnica.visibility = View.GONE
                    llFechas.visibility      = View.VISIBLE
                    val weeks = (0..3).map { offset ->
                        val cal = Calendar.getInstance().also { it.add(Calendar.WEEK_OF_YEAR, offset) }
                        val iso = sdf.format(cal.time)
                        val label = when (offset) {
                            0 -> "Esta semana ($iso)"
                            1 -> "En 1 semana ($iso)"
                            else -> "En $offset semanas ($iso)"
                        }
                        Pair(label, iso)
                    }
                    buildDateCheckboxes(weeks)
                }
            }
        }

        // Show "Única" by default
        spRepeticion.setText(repeticiones[0], false)
        tilFechaUnica.visibility = View.VISIBLE
        llFechas.visibility      = View.GONE

        btnGuardar.setOnClickListener {
            val identificador = etIdentificador.text.toString().trim()
            val alcance  = if (rgAlcance.checkedRadioButtonId == R.id.rbMasivo) "masivo" else "individual"
            val categoria = spCategoria.text.toString()
            val dosis    = etDosis.text.toString().trim()
            val vet      = etVeterinario.text.toString().trim()
            val rep      = spRepeticion.text.toString()

            if (identificador.isEmpty() || !currentList.contains(identificador)) {
                Toast.makeText(requireContext(), "Seleccione un Identificador válido de la lista", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (categoria.isEmpty()) {
                Toast.makeText(requireContext(), "Seleccione una Categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var detalle  = ""
            var producto = ""
            when (categoria) {
                "Vacunación" -> {
                    detalle = etVacuna.text.toString().trim()
                    if (detalle.isEmpty()) { Toast.makeText(requireContext(), "Especifique la Vacuna", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                }
                "Tratamiento" -> {
                    val tipo = etTipoTrat.text.toString().trim()
                    val prop = etProposito.text.toString().trim()
                    producto = etProducto.text.toString().trim()
                    if (tipo.isEmpty() || prop.isEmpty() || producto.isEmpty()) { Toast.makeText(requireContext(), "Complete todos los campos del tratamiento", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    detalle = "$tipo - $prop"
                }
                "Mastitis" -> {
                    val cuartos = listOf(cbAD, cbPD, cbAI, cbPI).filter { it.isChecked }.map { it.text.toString() }
                    producto = etProducto.text.toString().trim()
                    if (cuartos.isEmpty()) { Toast.makeText(requireContext(), "Seleccione al menos un cuarto afectado", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    if (producto.isEmpty()) { Toast.makeText(requireContext(), "Ingrese el medicamento", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    detalle = "Cuartos: ${cuartos.joinToString(", ")}"
                }
            }

            // Dosis: required and must be numeric
            if (dosis.isEmpty()) {
                Toast.makeText(requireContext(), "Ingrese la dosis", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (dosis.toDoubleOrNull() == null) {
                Toast.makeText(requireContext(), "La dosis debe ser un número válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Veterinario: required, only letters / spaces / accented chars
            if (vet.isEmpty()) {
                Toast.makeText(requireContext(), "Ingrese el nombre del veterinario", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val vetRegex = Regex("^[a-zA-ZáéíóúÁÉÍÓÚüÜñÑ ]+$")
            if (!vetRegex.matches(vet)) {
                Toast.makeText(requireContext(), "El nombre del veterinario solo puede contener letras, espacios y tildes", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }


            // All doses in this plan share the same grupoId
            val grupoId = UUID.randomUUID().toString()

            fun saveProgramado(iso: String) = sanidadRepository.saveRegistro(
                RegistroSanitario(
                    id = UUID.randomUUID().toString(),
                    identificador = identificador,
                    alcance = alcance,
                    categoria = categoria,
                    detalle = detalle,
                    producto = producto,
                    dosis = dosis,
                    fecha = "",
                    proximaDosis = iso,
                    veterinario = vet,
                    notas = "",
                    estado = "programado",
                    grupoId = grupoId
                )
            )

            when (rep) {
                "Única" -> {
                    val iso = etFechaUnica.text.toString().trim()
                    if (iso.isEmpty()) { Toast.makeText(requireContext(), "Seleccione una fecha", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    saveProgramado(iso)
                }
                "Diaria", "Semanal" -> {
                    val selected = (0 until llFechas.childCount)
                        .mapNotNull { llFechas.getChildAt(it) as? CheckBox }
                        .filter { it.isChecked }
                        .map { it.tag as String }
                    if (selected.isEmpty()) { Toast.makeText(requireContext(), "Seleccione al menos una fecha", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
                    selected.forEach { saveProgramado(it) }
                }
            }

            AlertManager(requireContext()).checkAlerts()
            Toast.makeText(requireContext(), "Registro(s) guardado(s) ✓", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            cargarRegistros()
            actualizarKPIs()
        }


        dialog.show()
    }
}
