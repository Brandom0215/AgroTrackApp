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
import pa.ac.utp.agrotrackapp.data.inventario.SqliteTransaccionRepository
import pa.ac.utp.agrotrackapp.domain.repository.InventarioRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import java.util.Locale

class ContabilidadFragment : Fragment(R.layout.fragment_contabilidad) {

    private lateinit var inventarioRepository: InventarioRepository
    private lateinit var transaccionRepository: SqliteTransaccionRepository
    private lateinit var adapter: ContabilidadAdapter
    
    private lateinit var tvTotalCosto: TextView
    private lateinit var tvTotalPrecio: TextView
    private lateinit var tvTotalGanancia: TextView
    private lateinit var tvMargenTotal: TextView
    private lateinit var tvGananciaRealizada: TextView
    private lateinit var tvGananciaEstimada: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inventarioRepository = SqliteInventarioRepository(requireContext())
        transaccionRepository = SqliteTransaccionRepository(requireContext())

        // Configurar Drawer Button
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // Bind Summary Views
        tvTotalCosto = view.findViewById(R.id.tvTotalCosto)
        tvTotalPrecio = view.findViewById(R.id.tvTotalPrecio)
        tvTotalGanancia = view.findViewById(R.id.tvTotalGanancia)
        tvMargenTotal = view.findViewById(R.id.tvMargenTotal)
        tvGananciaRealizada = view.findViewById(R.id.tvGananciaRealizada)
        tvGananciaEstimada = view.findViewById(R.id.tvGananciaEstimada)

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

        // 1. Calcular valores del inventario actual en stock
        var costoStock = 0.0
        var precioStock = 0.0

        for (item in items) {
            costoStock += (item.costo * item.stock)
            precioStock += (item.precio * item.stock)
        }

        val gananciaEstimadaStock = precioStock - costoStock

        // 2. Calcular ganancias realizadas desde las transacciones de ventas reales
        val transacciones = transaccionRepository.getTransacciones()
        var ventasRealizadas = 0.0
        var costoVendido = 0.0

        for (trans in transacciones) {
            if (trans.tipo.equals("venta", ignoreCase = true)) {
                ventasRealizadas += (trans.precioUnitario * trans.cantidad)
                costoVendido += (trans.costoUnitario * trans.cantidad)
            }
        }

        val gananciaRealizada = ventasRealizadas - costoVendido

        // 3. Totales integrados del negocio (inventario restante + ventas realizadas)
        val gananciaTotal = gananciaEstimadaStock + gananciaRealizada
        val costoAcumuladoTotal = costoStock + costoVendido
        val margen = if (costoAcumuladoTotal > 0) (gananciaTotal / costoAcumuladoTotal) * 100 else 0.0

        // 4. Mostrar en la UI
        tvTotalCosto.text = "$${String.format(Locale.US, "%.2f", costoStock)}"
        tvTotalPrecio.text = "$${String.format(Locale.US, "%.2f", precioStock)}"
        tvTotalGanancia.text = "$${String.format(Locale.US, "%.2f", gananciaTotal)}"
        tvMargenTotal.text = "${String.format(Locale.US, "%.1f", margen)}%"
        tvGananciaRealizada.text = "$${String.format(Locale.US, "%.2f", ventasRealizadas)}"
        tvGananciaEstimada.text = "$${String.format(Locale.US, "%.2f", gananciaEstimadaStock)}"
        
        if (gananciaTotal < 0) {
            tvTotalGanancia.setTextColor(requireContext().getColor(R.color.status_red))
        } else {
            tvTotalGanancia.setTextColor(requireContext().getColor(R.color.status_green))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }
}
