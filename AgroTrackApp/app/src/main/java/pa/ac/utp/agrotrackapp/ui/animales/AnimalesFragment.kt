package pa.ac.utp.agrotrackapp.ui.animales

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SqliteAnimalRepository
import pa.ac.utp.agrotrackapp.domain.model.Animal
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import androidx.activity.result.contract.ActivityResultContracts
import pa.ac.utp.agrotrackapp.data.animal.SqliteLoteRepository

class AnimalesFragment : Fragment(R.layout.fragment_animales) {

    private lateinit var animalRepository: AnimalRepository
    private lateinit var loteRepository: SqliteLoteRepository
    private lateinit var adapter: AnimalAdapter
    private var allAnimals = listOf<Animal>()

    private lateinit var etSearchArete: TextInputEditText
    
    private lateinit var llFilterMachos: LinearLayout
    private lateinit var llFilterHembras: LinearLayout
    private lateinit var llFilterLotes: LinearLayout
    private lateinit var tvCountMachos: TextView
    private lateinit var tvCountHembras: TextView
    private lateinit var tvCountLotes: TextView

    private var currentFilter = "Todos"
    private var selectedLoteName = ""

    private val selectLoteLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val loteName = result.data?.getStringExtra("EXTRA_LOTE_NOMBRE")
            if (!loteName.isNullOrEmpty()) {
                currentFilter = "Lotes"
                selectedLoteName = loteName
                llFilterLotes.isSelected = true
                llFilterMachos.isSelected = false
                llFilterHembras.isSelected = false
                filtrarAnimales()
            }
        } else {
            // Si cancela la selección, revertir el filtro
            if (currentFilter != "Lotes") {
                llFilterLotes.isSelected = false
            }
        }
    }

    private lateinit var btnToggleSearch: ImageButton
    private lateinit var searchContainer: LinearLayout
    private lateinit var rvAnimalesTabla: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var llRecentActivityContainer: LinearLayout
    private lateinit var tvEmptyRecentActivity: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos repositorio
        animalRepository = SqliteAnimalRepository(requireContext())
        loteRepository = SqliteLoteRepository(requireContext())

        // Enlazar filtros y tabla
        etSearchArete = view.findViewById(R.id.etSearchArete)
        
        llFilterMachos = view.findViewById(R.id.llFilterMachos)
        llFilterHembras = view.findViewById(R.id.llFilterHembras)
        llFilterLotes = view.findViewById(R.id.llFilterLotes)
        tvCountMachos = view.findViewById(R.id.tvCountMachos)
        tvCountHembras = view.findViewById(R.id.tvCountHembras)
        tvCountLotes = view.findViewById(R.id.tvCountLotes)

        btnToggleSearch = view.findViewById(R.id.btnToggleSearch)
        searchContainer = view.findViewById(R.id.searchContainer)
        rvAnimalesTabla = view.findViewById(R.id.rvAnimalesTabla)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        llRecentActivityContainer = view.findViewById(R.id.llRecentActivityContainer)
        tvEmptyRecentActivity = view.findViewById(R.id.tvEmptyRecentActivity)

        // Setup Drawer Menu
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // Setup Floating Action Button to Add Animal
        view.findViewById<View>(R.id.fabAddAnimal)?.setOnClickListener {
            val intent = Intent(requireContext(), CrearAnimalActivity::class.java)
            startActivity(intent)
        }

        // Setup Search Toggle
        btnToggleSearch.setOnClickListener {
            if (searchContainer.visibility == View.VISIBLE) {
                searchContainer.visibility = View.GONE
                etSearchArete.text?.clear()
            } else {
                searchContainer.visibility = View.VISIBLE
                etSearchArete.requestFocus()
            }
        }

        // Setup RecyclerView Adapter
        adapter = AnimalAdapter(
            animals = emptyList(),
            onViewClick = { animal -> mostrarDetalleAnimal(animal) },
            onEditClick = { animal -> editarAnimal(animal) }
        )
        rvAnimalesTabla.adapter = adapter

        // Setup Listeners for Filters
        etSearchArete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarAnimales()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val filterClickListener = View.OnClickListener { v ->
            // Unselect all
            llFilterMachos.isSelected = false
            llFilterHembras.isSelected = false
            llFilterLotes.isSelected = false

            when (v.id) {
                R.id.llFilterMachos -> {
                    if (currentFilter == "Macho") {
                        currentFilter = "Todos"
                    } else {
                        currentFilter = "Macho"
                        llFilterMachos.isSelected = true
                    }
                }
                R.id.llFilterHembras -> {
                    if (currentFilter == "Hembra") {
                        currentFilter = "Todos"
                    } else {
                        currentFilter = "Hembra"
                        llFilterHembras.isSelected = true
                    }
                }
                R.id.llFilterLotes -> {
                    if (currentFilter == "Lotes") {
                        currentFilter = "Todos"
                        selectedLoteName = ""
                        filtrarAnimales()
                    } else {
                        val intent = Intent(requireContext(), LotesActivity::class.java)
                        selectLoteLauncher.launch(intent)
                        return@OnClickListener // No filtrar todavía
                    }
                }
            }
            if (v.id != R.id.llFilterLotes) {
                filtrarAnimales()
            }
        }

        llFilterMachos.setOnClickListener(filterClickListener)
        llFilterHembras.setOnClickListener(filterClickListener)
        llFilterLotes.setOnClickListener(filterClickListener)
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos actualizados cada vez que la pantalla vuelve a estar activa
        cargarDatosYActualizarUI()
    }

    private fun cargarDatosYActualizarUI() {
        allAnimals = animalRepository.getAnimals()

        // Actualizar contadores
        val totalMachos = allAnimals.count { it.sexo == "Macho" }
        val totalHembras = allAnimals.count { it.sexo == "Hembra" }
        val totalLotes = loteRepository.getLotes().size

        tvCountMachos.text = totalMachos.toString()
        tvCountHembras.text = totalHembras.toString()
        tvCountLotes.text = totalLotes.toString()



        // Actualizar la lista de la tabla con los filtros actuales
        filtrarAnimales()
        // Cargar actividad reciente dinámicamente
        cargarActividadReciente()
    }

    private fun filtrarAnimales() {
        val query = etSearchArete.text.toString().trim().lowercase()
        val sexFilter = currentFilter

        val filteredList = allAnimals.filter { animal ->
            val matchesArete = animal.numeroAnimal.lowercase().contains(query)
            val matchesSex = when (sexFilter) {
                "Macho" -> animal.sexo == "Macho"
                "Hembra" -> animal.sexo == "Hembra"
                "Lotes" -> animal.lote == selectedLoteName
                else -> true
            }
            matchesArete && matchesSex
        }

        adapter.updateList(filteredList)

        // Mostrar u ocultar estado vacío
        if (filteredList.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            rvAnimalesTabla.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            rvAnimalesTabla.visibility = View.VISIBLE
        }
    }

    private fun mostrarDetalleAnimal(animal: Animal) {
        val dialog = BottomSheetDialog(requireContext())
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_animal_detalle, null)

        // Vincular componentes
        val ivDetalleFoto = dialogView.findViewById<ImageView>(R.id.ivDetalleFoto)
        val tvArete = dialogView.findViewById<TextView>(R.id.tvDetalleArete)
        val tvSexo = dialogView.findViewById<TextView>(R.id.tvDetalleSexo)
        val tvRaza = dialogView.findViewById<TextView>(R.id.tvDetalleRaza)
        val tvProposito = dialogView.findViewById<TextView>(R.id.tvDetalleProposito)
        val tvFechaNacimiento = dialogView.findViewById<TextView>(R.id.tvDetalleFechaNacimiento)
        val tvManga = dialogView.findViewById<TextView>(R.id.tvDetalleManga)
        val tvTrazabilidad = dialogView.findViewById<TextView>(R.id.tvDetalleTrazabilidad)
        val tvChip = dialogView.findViewById<TextView>(R.id.tvDetalleChip)
        
        val tvPeso = dialogView.findViewById<TextView>(R.id.tvDetallePeso)
        
        val tvPadre = dialogView.findViewById<TextView>(R.id.tvDetallePadre)
        val tvMadre = dialogView.findViewById<TextView>(R.id.tvDetalleMadre)
        val tvNotas = dialogView.findViewById<TextView>(R.id.tvDetalleNotas)
        
        val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrarDetalle)

        // Asignar datos del animal
        if (animal.imagenPath.isNotEmpty()) {
            val file = java.io.File(animal.imagenPath)
            if (file.exists()) {
                ivDetalleFoto.setImageURI(android.net.Uri.fromFile(file))
            } else {
                ivDetalleFoto.setImageResource(R.drawable.vaca)
            }
        } else {
            ivDetalleFoto.setImageResource(R.drawable.vaca)
        }

        tvArete.text = "Arete #${animal.numeroAnimal}"
        tvSexo.text = animal.sexo
        
        // Estilo visual del sexo
        if (animal.sexo == "Macho") {
            tvSexo.setBackgroundColor(Color.parseColor("#03A9F4")) // Celeste
        } else {
            tvSexo.setBackgroundColor(Color.parseColor("#9C27B0")) // Morado
        }
        
        tvRaza.text = animal.raza
        tvProposito.text = animal.proposito
        tvFechaNacimiento.text = animal.fechaNacimiento
        tvManga.text = animal.manga.ifEmpty { "N/A" }
        tvTrazabilidad.text = animal.trazabilidad.ifEmpty { "N/A" }
        tvChip.text = animal.numeroChip.ifEmpty { "N/A" }
        
        tvPeso.text = if (animal.peso.isEmpty()) "-" else "${animal.peso} kg"
        
        tvPadre.text = animal.padre.ifEmpty { "Desconocido" }
        tvMadre.text = animal.madre.ifEmpty { "Desconocido" }
        tvNotas.text = animal.notas.ifEmpty { "Ninguna observación registrada." }

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(dialogView)
        dialog.show()
    }

    private fun editarAnimal(animal: Animal) {
        val intent = Intent(requireContext(), CrearAnimalActivity::class.java)
        intent.putExtra("EXTRA_NUMERO_ANIMAL", animal.numeroAnimal)
        startActivity(intent)
    }

    private fun cargarActividadReciente() {
        llRecentActivityContainer.removeAllViews()
        val lastAnimals = allAnimals.takeLast(3).reversed()
        if (lastAnimals.isEmpty()) {
            tvEmptyRecentActivity.visibility = View.VISIBLE
            llRecentActivityContainer.addView(tvEmptyRecentActivity)
        } else {
            tvEmptyRecentActivity.visibility = View.GONE
            val inflater = LayoutInflater.from(requireContext())
            for (animal in lastAnimals) {
                val itemLayout = RelativeLayout(requireContext()).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 20)
                    }
                }

                val cardView = com.google.android.material.card.MaterialCardView(requireContext()).apply {
                    id = View.generateViewId()
                    layoutParams = RelativeLayout.LayoutParams(120, 120).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_START)
                    }
                    radius = 22f
                    cardElevation = 0f
                    strokeWidth = 0
                }

                val imageView = ImageView(requireContext()).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    if (animal.imagenPath.isNotEmpty() && java.io.File(animal.imagenPath).exists()) {
                        val optBitmap = pa.ac.utp.agrotrackapp.utils.ImageResizer.decodeSampledBitmapFromFile(animal.imagenPath, 100, 100)
                        if (optBitmap != null) {
                            setImageBitmap(optBitmap)
                        } else {
                            setImageResource(R.drawable.vaca)
                        }
                    } else {
                        setImageResource(R.drawable.vaca)
                    }
                }
                cardView.addView(imageView)
                itemLayout.addView(cardView)

                val infoLayout = LinearLayout(requireContext()).apply {
                    layoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.END_OF, cardView.id)
                        marginStart = 30
                    }
                    orientation = LinearLayout.VERTICAL
                }

                val titleView = TextView(requireContext()).apply {
                    text = "Bovino Registrado"
                    setTextAppearance(android.R.style.TextAppearance_Medium)
                    textSize = 14f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }

                val subtitleView = TextView(requireContext()).apply {
                    text = "Arete: ${animal.numeroAnimal} • ${animal.raza} • ${animal.peso} kg"
                    textSize = 12f
                    setTextColor(Color.GRAY)
                }

                infoLayout.addView(titleView)
                infoLayout.addView(subtitleView)
                itemLayout.addView(infoLayout)

                val dateView = TextView(requireContext()).apply {
                    layoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_END)
                    }
                    text = animal.fechaNacimiento
                    textSize = 11f
                    setTextColor(Color.GRAY)
                }
                itemLayout.addView(dateView)

                llRecentActivityContainer.addView(itemLayout)
            }
        }
    }
}
