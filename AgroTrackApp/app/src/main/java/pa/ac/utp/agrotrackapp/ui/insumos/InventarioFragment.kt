package pa.ac.utp.agrotrackapp.ui.insumos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.inventario.SqliteInventarioRepository
import pa.ac.utp.agrotrackapp.domain.model.InventarioItem
import pa.ac.utp.agrotrackapp.domain.repository.InventarioRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

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

        // 3. Inicializar el RecyclerView con diseño de cuadrícula (2 columnas)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewInventario)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // 4. Configurar el adaptador de catálogo
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

        // 5. Inicializar el RecyclerView de Historial (diseño lineal vertical)
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
}
