package pa.ac.utp.agrotrackapp.ui.contabilidad

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.inventario.SqliteInventarioRepository
import pa.ac.utp.agrotrackapp.domain.repository.InventarioRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import java.util.Locale

class ContabilidadFragment : Fragment(R.layout.fragment_contabilidad) {

    private lateinit var inventarioRepository: InventarioRepository
    private lateinit var adapter: ContabilidadAdapter
    
    private lateinit var tvTotalCosto: TextView
    private lateinit var tvTotalPrecio: TextView
    private lateinit var tvTotalGanancia: TextView
    private lateinit var tvMargenTotal: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inventarioRepository = SqliteInventarioRepository(requireContext())

        // Configurar Drawer Button
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // Bind Summary Views
        tvTotalCosto = view.findViewById(R.id.tvTotalCosto)
        tvTotalPrecio = view.findViewById(R.id.tvTotalPrecio)
        tvTotalGanancia = view.findViewById(R.id.tvTotalGanancia)
        tvMargenTotal = view.findViewById(R.id.tvMargenTotal)

        // Setup RecyclerView
        val rv = view.findViewById<RecyclerView>(R.id.recyclerViewContabilidad)
        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = ContabilidadAdapter(emptyList())
        rv.adapter = adapter

        cargarDatos()
    }

    private fun cargarDatos() {
        val items = inventarioRepository.getItems()
        adapter.updateItems(items)

        var totalCosto = 0.0
        var totalPrecio = 0.0

        for (item in items) {
            // Calculamos en base al stock actual
            totalCosto += (item.costo * item.stock)
            totalPrecio += (item.precio * item.stock)
        }

        val totalGanancia = totalPrecio - totalCosto
        val margen = if (totalCosto > 0) (totalGanancia / totalCosto) * 100 else 0.0

        tvTotalCosto.text = "$${String.format(Locale.US, "%.2f", totalCosto)}"
        tvTotalPrecio.text = "$${String.format(Locale.US, "%.2f", totalPrecio)}"
        tvTotalGanancia.text = "$${String.format(Locale.US, "%.2f", totalGanancia)}"
        tvMargenTotal.text = "${String.format(Locale.US, "%.1f", margen)}%"
        
        if (totalGanancia < 0) {
            tvTotalGanancia.setTextColor(requireContext().getColor(R.color.status_red))
        } else {
            tvTotalGanancia.setTextColor(requireContext().getColor(R.color.status_green))
        }
    }
}
