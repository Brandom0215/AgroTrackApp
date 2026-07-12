package pa.ac.utp.agrotrackapp.ui.insumos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SqliteAnimalRepository
import pa.ac.utp.agrotrackapp.data.inventario.SqliteInventarioRepository
import pa.ac.utp.agrotrackapp.data.inventario.SqliteTransaccionRepository
import pa.ac.utp.agrotrackapp.data.sanidad.SqliteSanidadRepository
import pa.ac.utp.agrotrackapp.domain.model.InventarioItem
import pa.ac.utp.agrotrackapp.domain.model.RegistroSanitario
import pa.ac.utp.agrotrackapp.domain.model.Transaccion
import pa.ac.utp.agrotrackapp.domain.repository.InventarioRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InventarioFragment : Fragment(R.layout.fragment_inventario) {

    private lateinit var inventarioRepository: InventarioRepository
    private lateinit var adapter: InventarioAdapter
    private lateinit var historialAdapter: InventarioHistorialAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inventarioRepository = SqliteInventarioRepository(requireContext())

        // 1. Configurar botón de menú lateral (Drawer)
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // 2. Configurar botón para agregar nuevo producto
        view.findViewById<View>(R.id.btnAdd).setOnClickListener {
            val intent = Intent(requireContext(), CrearInventarioActivity::class.java)
            startActivity(intent)
        }

        // 3. Configurar botones transaccionales rápidos de la barra externa
        view.findViewById<MaterialCardView>(R.id.cardAccionVender)?.setOnClickListener {
            mostrarDialogoVentaGeneral()
        }

        view.findViewById<MaterialCardView>(R.id.cardAccionConsumo)?.setOnClickListener {
            mostrarDialogoConsumoGeneral()
        }

        // 4. Inicializar el RecyclerView con diseño de cuadrícula (2 columnas)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewInventario)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // 5. Configurar el adaptador de catálogo
        adapter = InventarioAdapter(
            itemsList = emptyList(),
            onItemClick = { item ->
                // Abrir en modo edición
                val intent = Intent(requireContext(), CrearInventarioActivity::class.java)
                intent.putExtra("EXTRA_ITEM_ID", item.id)
                startActivity(intent)
            },
            onDeleteClick = { item ->
                // Confirmar eliminación
                mostrarDialogoConfirmacion(item.id, item.nombre, item.fotoPath)
            }
        )
        recyclerView.adapter = adapter

        // 6. Inicializar el RecyclerView de Historial (diseño lineal vertical)
        val recyclerViewHistorial = view.findViewById<RecyclerView>(R.id.recyclerViewHistorial)
        recyclerViewHistorial.layoutManager = LinearLayoutManager(requireContext())
        historialAdapter = InventarioHistorialAdapter(emptyList())
        recyclerViewHistorial.adapter = historialAdapter
        
        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        val items = inventarioRepository.getItems()
        
        val tvEmpty = view?.findViewById<TextView>(R.id.tvEmptyInventario)
        val rvInv = view?.findViewById<View>(R.id.recyclerViewInventario)
        val rvHistorial = view?.findViewById<View>(R.id.recyclerViewHistorial)

        if (items.isEmpty()) {
            tvEmpty?.visibility = View.VISIBLE
            rvInv?.visibility = View.GONE
            rvHistorial?.visibility = View.GONE
        } else {
            tvEmpty?.visibility = View.GONE
            rvInv?.visibility = View.VISIBLE
            rvHistorial?.visibility = View.VISIBLE
        }

        // Catálogo ordenado por nombre (por defecto)
        adapter.updateData(items)

        // Historial ordenado por fecha de registro descendente (más recientes primero)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val sortedByDateDesc = items.sortedWith(Comparator { a, b ->
            try {
                val dateA = format.parse(a.fechaRegistro)
                val dateB = format.parse(b.fechaRegistro)
                if (dateA != null && dateB != null) {
                    dateB.compareTo(dateA) // Descendente (más reciente primero)
                } else {
                    b.fechaRegistro.compareTo(a.fechaRegistro)
                }
            } catch (e: Exception) {
                b.fechaRegistro.compareTo(a.fechaRegistro) // Ordenación por string si falla
            }
        })
        historialAdapter.updateData(sortedByDateDesc)
    }

    private fun mostrarDialogoConfirmacion(id: String, nombre: String, fotoPath: String?) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Eliminar producto")
        builder.setMessage("¿Está seguro de que desea eliminar '$nombre' del inventario?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            // Eliminar de base de datos
            val result = inventarioRepository.deleteItem(id)
            if (result.isSuccess) {
                // Eliminar foto local si existe
                if (!fotoPath.isNullOrEmpty()) {
                    val file = File(fotoPath)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                cargarDatos()
            } else {
                Toast.makeText(requireContext(), "Error al eliminar producto", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    /**
     * Abre el diálogo de venta rápida de productos desde la pantalla de inicio del inventario.
     * Permite seleccionar el producto a vender mediante un dropdown y muestra un resumen con su stock.
     */
    private fun mostrarDialogoVentaGeneral() {
        val items = inventarioRepository.getItems()
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Primero registre un producto en el inventario", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_vender_main, null)
        
        val tilSelectProductoVenta = dialogView.findViewById<TextInputLayout>(R.id.tilSelectProductoVenta)
        val etSelectProductoVenta = dialogView.findViewById<AutoCompleteTextView>(R.id.etSelectProductoVenta)
        
        val cardResumenProductoVenta = dialogView.findViewById<MaterialCardView>(R.id.cardResumenProductoVenta)
        val tvStockVentaResumen = dialogView.findViewById<TextView>(R.id.tvStockVentaResumen)
        val tvPrecioVentaResumen = dialogView.findViewById<TextView>(R.id.tvPrecioVentaResumen)
        
        val tilCantidadVenta = dialogView.findViewById<TextInputLayout>(R.id.tilCantidadVenta)
        val etCantidadVenta = dialogView.findViewById<TextInputEditText>(R.id.etCantidadVenta)

        // Configurar dropdown de selección de productos
        val nombresProductos = items.map { it.nombre }
        val selectAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombresProductos)
        etSelectProductoVenta.setAdapter(selectAdapter)

        var itemSeleccionado: InventarioItem? = null

        etSelectProductoVenta.setOnItemClickListener { _, _, position, _ ->
            itemSeleccionado = items[position]
            itemSeleccionado?.let {
                cardResumenProductoVenta.visibility = View.VISIBLE
                val stockStr = if (it.stock % 1.0 == 0.0) it.stock.toInt().toString() else it.stock.toString()
                tvStockVentaResumen.text = "Stock disponible: $stockStr ${it.unidad}"
                tvPrecioVentaResumen.text = "Precio de venta unitario: $${String.format(Locale.US, "%.2f", it.precio)} (Costo: $${String.format(Locale.US, "%.2f", it.costo)})"
            }
        }

        builder.setView(dialogView)
        builder.setPositiveButton("Vender", null)
        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val productText = etSelectProductoVenta.text.toString().trim()
            if (productText.isEmpty() || itemSeleccionado == null) {
                tilSelectProductoVenta.error = "Debe seleccionar un producto"
                return@setOnClickListener
            }
            tilSelectProductoVenta.error = null

            val cantStr = etCantidadVenta.text.toString().trim()
            if (cantStr.isEmpty()) {
                tilCantidadVenta.error = "Ingrese la cantidad a vender"
                return@setOnClickListener
            }

            val cantidadVal = cantStr.toDoubleOrNull()
            if (cantidadVal == null || cantidadVal <= 0) {
                tilCantidadVenta.error = "Ingrese una cantidad válida mayor a 0"
                return@setOnClickListener
            }

            val item = itemSeleccionado!!
            if (cantidadVal > item.stock) {
                tilCantidadVenta.error = "Stock insuficiente (disponible: ${item.stock} ${item.unidad})"
                return@setOnClickListener
            }
            tilCantidadVenta.error = null

            // Restar stock y persistir
            val nuevoStock = item.stock - cantidadVal
            val productoActualizado = item.copy(stock = nuevoStock)

            val result = inventarioRepository.updateItem(productoActualizado)
            if (result.isSuccess) {
                // Registrar Transacción Histórica en SQLite
                val transaccionRepository = SqliteTransaccionRepository(requireContext())
                val trans = Transaccion(
                    id = UUID.randomUUID().toString(),
                    tipo = "venta",
                    productoId = item.id,
                    productoNombre = item.nombre,
                    cantidad = cantidadVal,
                    precioUnitario = item.precio,
                    costoUnitario = item.costo,
                    fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                    detalles = "Venta de ${cantidadVal} ${item.unidad}"
                )
                transaccionRepository.saveTransaccion(trans)

                val totalRecaudado = cantidadVal * item.precio
                Toast.makeText(
                    requireContext(), 
                    "Venta exitosa: ${cantidadVal} ${item.unidad} de ${item.nombre} vendidos por $${String.format(Locale.US, "%.2f", totalRecaudado)}", 
                    Toast.LENGTH_LONG
                ).show()

                dialog.dismiss()
                cargarDatos()
            } else {
                Toast.makeText(requireContext(), "Error al registrar la venta: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Abre el diálogo de consumo animal rápido desde la pantalla de inicio del inventario.
     * Permite seleccionar el producto, alternar alcance, validar arete contra SQLite y guardar el historial médico.
     */
    private fun mostrarDialogoConsumoGeneral() {
        val items = inventarioRepository.getItems()
        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Primero registre un producto en el inventario", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_consumir_main, null)
        
        val tilSelectProductoConsumo = dialogView.findViewById<TextInputLayout>(R.id.tilSelectProductoConsumo)
        val etSelectProductoConsumo = dialogView.findViewById<AutoCompleteTextView>(R.id.etSelectProductoConsumo)
        
        val cardResumenProductoConsumo = dialogView.findViewById<MaterialCardView>(R.id.cardResumenProductoConsumo)
        val tvStockConsumoResumen = dialogView.findViewById<TextView>(R.id.tvStockConsumoResumen)
        val tvTipoConsumoResumen = dialogView.findViewById<TextView>(R.id.tvTipoConsumoResumen)
        
        val tilCantidadConsumo = dialogView.findViewById<TextInputLayout>(R.id.tilCantidadConsumo)
        val etCantidadConsumo = dialogView.findViewById<TextInputEditText>(R.id.etCantidadConsumo)
        
        val rgAlcanceConsumo = dialogView.findViewById<RadioGroup>(R.id.rgAlcanceConsumo)
        val rbIndividual = dialogView.findViewById<RadioButton>(R.id.rbIndividual)
        
        val tilIdentificadorConsumo = dialogView.findViewById<TextInputLayout>(R.id.tilIdentificadorConsumo)
        val etIdentificadorConsumo = dialogView.findViewById<AutoCompleteTextView>(R.id.etIdentificadorConsumo)

        // Configurar dropdown de selección de productos
        val nombresProductos = items.map { it.nombre }
        val selectAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, nombresProductos)
        etSelectProductoConsumo.setAdapter(selectAdapter)

        var itemSeleccionado: InventarioItem? = null

        etSelectProductoConsumo.setOnItemClickListener { _, _, position, _ ->
            itemSeleccionado = items[position]
            itemSeleccionado?.let {
                cardResumenProductoConsumo.visibility = View.VISIBLE
                val stockStr = if (it.stock % 1.0 == 0.0) it.stock.toInt().toString() else it.stock.toString()
                tvStockConsumoResumen.text = "Stock disponible: $stockStr ${it.unidad}"
                tvTipoConsumoResumen.text = "Tipo de producto: ${it.tipo}"
            }
        }

        // Cargar datos para el autocompletado del identificador
        val animalRepository = SqliteAnimalRepository(requireContext())
        val todosLosAnimales = animalRepository.getAnimals()
        val aretes = todosLosAnimales.map { it.numeroAnimal }
        val mangas = todosLosAnimales.map { it.manga }.filter { it.isNotEmpty() }.distinct()

        val idAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ArrayList(aretes))
        etIdentificadorConsumo.setAdapter(idAdapter)
        etIdentificadorConsumo.threshold = 1 // Mostrar sugerencias desde el primer carácter

        // Mostrar todas las opciones al hacer clic
        etIdentificadorConsumo.setOnTouchListener { _, _ ->
            etIdentificadorConsumo.showDropDown()
            false
        }

        rgAlcanceConsumo.setOnCheckedChangeListener { _, checkedId ->
            etIdentificadorConsumo.setText("") // Limpiar al cambiar modo
            if (checkedId == R.id.rbIndividual) {
                tilIdentificadorConsumo.hint = "Número de Arete *"
                val newAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, aretes)
                etIdentificadorConsumo.setAdapter(newAdapter)
            } else {
                tilIdentificadorConsumo.hint = "Manga o Lote *"
                val newAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mangas)
                etIdentificadorConsumo.setAdapter(newAdapter)
            }
        }

        builder.setView(dialogView)
        builder.setPositiveButton("Consumir", null)
        builder.setNegativeButton("Cancelar", null)

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val productText = etSelectProductoConsumo.text.toString().trim()
            if (productText.isEmpty() || itemSeleccionado == null) {
                tilSelectProductoConsumo.error = "Debe seleccionar un producto"
                return@setOnClickListener
            }
            tilSelectProductoConsumo.error = null

            val cantStr = etCantidadConsumo.text.toString().trim()
            val identificador = etIdentificadorConsumo.text.toString().trim()

            // 1. Validar cantidad
            if (cantStr.isEmpty()) {
                tilCantidadConsumo.error = "Ingrese la cantidad a consumir"
                return@setOnClickListener
            }
            val cantidadVal = cantStr.toDoubleOrNull()
            if (cantidadVal == null || cantidadVal <= 0) {
                tilCantidadConsumo.error = "Ingrese una cantidad válida mayor a 0"
                return@setOnClickListener
            }
            val item = itemSeleccionado!!
            if (cantidadVal > item.stock) {
                tilCantidadConsumo.error = "Stock insuficiente (disponible: ${item.stock} ${item.unidad})"
                return@setOnClickListener
            }
            tilCantidadConsumo.error = null

            // 2. Validar identificador
            if (identificador.isEmpty()) {
                tilIdentificadorConsumo.error = "Este campo es requerido"
                return@setOnClickListener
            }
            tilIdentificadorConsumo.error = null

            val isIndividual = rbIndividual.isChecked

            // 3. Validar existencia del animal en caso de alcance individual
            if (isIndividual) {
                val animal = animalRepository.getAnimal(identificador)
                if (animal == null) {
                    tilIdentificadorConsumo.error = "El arete de animal '$identificador' no existe en el sistema"
                    return@setOnClickListener
                }
            }

            // 4. Actualizar stock en base de datos
            val nuevoStock = item.stock - cantidadVal
            val productoActualizado = item.copy(stock = nuevoStock)

            val result = inventarioRepository.updateItem(productoActualizado)
            if (result.isSuccess) {
                // Registrar Transacción de Consumo
                val transaccionRepository = SqliteTransaccionRepository(requireContext())
                val trans = Transaccion(
                    id = UUID.randomUUID().toString(),
                    tipo = "consumo",
                    productoId = item.id,
                    productoNombre = item.nombre,
                    cantidad = cantidadVal,
                    precioUnitario = 0.0,
                    costoUnitario = item.costo,
                    fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
                    detalles = "Consumo animal (${if (isIndividual) "Individual: $identificador" else "Lote: $identificador"})"
                )
                transaccionRepository.saveTransaccion(trans)

                var message = "Consumo registrado: ${cantidadVal} ${item.unidad} de ${item.nombre}"

                // 5. Integración con Control Sanitario (Sólo si es de tipo "Medicina")
                if (item.tipo.equals("Medicina", ignoreCase = true)) {
                    val sanidadRepository = SqliteSanidadRepository(requireContext())
                    val formatFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

                    val registroSanitario = RegistroSanitario(
                        id = UUID.randomUUID().toString(),
                        identificador = identificador,
                        alcance = if (isIndividual) "individual" else "masivo",
                        categoria = "Tratamiento",
                        detalle = "Consumo automático: ${item.nombre}",
                        producto = item.nombre,
                        dosis = "${cantidadVal} ${item.unidad}",
                        fecha = formatFecha,
                        proximaDosis = "",
                        veterinario = "Administrador",
                        notas = "Registro automático generado por consumo en el inventario.",
                        estado = "aplicado",
                        grupoId = ""
                    )

                    sanidadRepository.saveRegistro(registroSanitario)
                    message += " y registrado en el historial médico de Control Sanitario."
                }

                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                dialog.dismiss()
                cargarDatos()
            } else {
                Toast.makeText(requireContext(), "Error al guardar el consumo: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
