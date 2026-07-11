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

class AnimalesFragment : Fragment(R.layout.fragment_animales) {

    private lateinit var animalRepository: AnimalRepository
    private lateinit var adapter: AnimalAdapter
    private var allAnimals = listOf<Animal>()

    private lateinit var tvHeaderMachos: TextView
    private lateinit var tvHeaderHembras: TextView
    
    private lateinit var tvDonutTotal: TextView
    private lateinit var tvCountHembras: TextView
    private lateinit var tvCountMachos: TextView
    private lateinit var tvCountTerneros: TextView

    private lateinit var etSearchArete: TextInputEditText
    private lateinit var spinnerFilterSexo: Spinner
    private lateinit var rvAnimalesTabla: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var llRecentActivityContainer: LinearLayout
    private lateinit var tvEmptyRecentActivity: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos repositorio
        animalRepository = SqliteAnimalRepository(requireContext())

        // Enlazar vistas de cabecera e inventario
        tvHeaderMachos = view.findViewById(R.id.tvHeaderMachosCount)
        tvHeaderHembras = view.findViewById(R.id.tvHeaderHembrasCount)
        tvDonutTotal = view.findViewById(R.id.tvDonutTotal)
        tvCountHembras = view.findViewById(R.id.tvCountHembras)
        tvCountMachos = view.findViewById(R.id.tvCountMachos)
        tvCountTerneros = view.findViewById(R.id.tvCountTerneros)

        // Enlazar filtros y tabla
        etSearchArete = view.findViewById(R.id.etSearchArete)
        spinnerFilterSexo = view.findViewById(R.id.spinnerFilterSexo)
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

        // Setup Spinner
        val sexOptions = arrayOf("Todos", "Machos", "Hembras")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, sexOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFilterSexo.adapter = spinnerAdapter

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

        spinnerFilterSexo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filtrarAnimales()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos actualizados cada vez que la pantalla vuelve a estar activa
        cargarDatosYActualizarUI()
    }

    private fun cargarDatosYActualizarUI() {
        allAnimals = animalRepository.getAnimals()

        // 1. Calcular inventarios reales
        val totalCount = allAnimals.size
        val machosHeader = allAnimals.count { it.sexo == "Macho" }
        val hembrasHeader = allAnimals.count { it.sexo == "Hembra" }

        // Clasificación para gráfico donut
        // Se considera "Ternero" si el propósito es "Cría"
        val ternerosCount = allAnimals.count { it.proposito == "Cría" }
        val machosCount = allAnimals.count { it.sexo == "Macho" && it.proposito != "Cría" }
        val hembrasCount = allAnimals.count { it.sexo == "Hembra" && it.proposito != "Cría" }

        // Actualizar UI del inventario
        tvHeaderMachos.text = "$machosHeader Machos"
        tvHeaderHembras.text = "$hembrasHeader Hembras"
        tvDonutTotal.text = "Total\n$totalCount"
        tvCountHembras.text = "$hembrasCount"
        tvCountMachos.text = "$machosCount"
        tvCountTerneros.text = "$ternerosCount"

        // Actualizar la lista de la tabla con los filtros actuales
        filtrarAnimales()
        // Cargar actividad reciente dinámicamente
        cargarActividadReciente()
    }

    private fun filtrarAnimales() {
        val query = etSearchArete.text.toString().trim().lowercase()
        val sexFilter = spinnerFilterSexo.selectedItem?.toString() ?: "Todos"

        val filteredList = allAnimals.filter { animal ->
            val matchesArete = animal.numeroAnimal.lowercase().contains(query)
            val matchesSex = when (sexFilter) {
                "Machos" -> animal.sexo == "Macho"
                "Hembras" -> animal.sexo == "Hembra"
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
