package pa.ac.utp.agrotrackapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProduccionFragment : Fragment(R.layout.fragment_produccion) {

    private lateinit var adapterHistorial: ProduccionAdapter
    private lateinit var adapterTop: ProduccionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        val rvHistorial = view.findViewById<RecyclerView>(R.id.rvHistorial)
        val rvTopProduccion = view.findViewById<RecyclerView>(R.id.rvTopProduccion)
        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAdd)

        adapterHistorial = ProduccionAdapter()
        rvHistorial.layoutManager = LinearLayoutManager(requireContext())
        rvHistorial.adapter = adapterHistorial

        adapterTop = ProduccionAdapter()
        rvTopProduccion.layoutManager = LinearLayoutManager(requireContext())
        rvTopProduccion.adapter = adapterTop

        fabAdd.setOnClickListener {
            Toast.makeText(requireContext(), "Abriendo registro diario...", Toast.LENGTH_SHORT).show()
        }

        loadMockData(view)
    }

    private fun loadMockData(view: View) {
        val tvKpiHoy = view.findViewById<TextView>(R.id.tvKpiHoy)
        val tvKpiSemana = view.findViewById<TextView>(R.id.tvKpiSemana)
        val tvKpiMes = view.findViewById<TextView>(R.id.tvKpiMes)
        val tvKpiPromedio = view.findViewById<TextView>(R.id.tvKpiPromedio)

        val itemsHistorial = listOf(
            ItemProduccion("001", 12.5, 10.0, 4.0, 22.5),
            ItemProduccion("002", 14.2, 12.8, 5.0, 27.0),
            ItemProduccion("003", 11.0, 9.5, 3.5, 20.5),
            ItemProduccion("004", 15.5, 14.0, 6.0, 29.5)
        )
        adapterHistorial.submitList(itemsHistorial)

        val itemsTop = listOf(
            ItemProduccion("105", 18.0, 16.5, 7.0, 34.5),
            ItemProduccion("202", 17.5, 15.0, 6.5, 32.5),
            ItemProduccion("098", 16.8, 14.2, 6.0, 31.0)
        )
        adapterTop.submitList(itemsTop)

        tvKpiHoy?.text = "145.0 L"
        tvKpiSemana?.text = "1,020.5 L"
        tvKpiMes?.text = "4,200.0 L"
        tvKpiPromedio?.text = "14.5 L"
    }

    data class ItemProduccion(
        val id: String,
        val am: Double,
        val pm: Double,
        val concentrado: Double,
        val total: Double
    )

    inner class ProduccionAdapter : ListAdapter<ItemProduccion, ProduccionAdapter.ViewHolder>(DiffCallback()) {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvId: TextView = v.findViewById(R.id.tvId)
            val tvTotal: TextView = v.findViewById(R.id.tvTotal)
            val tvDetalle: TextView = v.findViewById(R.id.tvDetalle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_produccion_row, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)
            val numericId = if (item.id.startsWith("V", ignoreCase = true)) item.id.substring(1) else item.id
            holder.tvId.text = "ID: $numericId"
            holder.tvTotal.text = "${item.total} L"
            holder.tvDetalle.text = "AM: ${item.am} | PM: ${item.pm} | Conc: ${item.concentrado}kg"
            holder.tvId.setTextColor(Color.parseColor("#1A5C38"))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemProduccion>() {
        override fun areItemsTheSame(oldItem: ItemProduccion, newItem: ItemProduccion) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ItemProduccion, newItem: ItemProduccion) = oldItem == newItem
    }
}
