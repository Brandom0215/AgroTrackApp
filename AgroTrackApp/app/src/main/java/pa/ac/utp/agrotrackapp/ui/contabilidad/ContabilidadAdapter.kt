package pa.ac.utp.agrotrackapp.ui.contabilidad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.InventarioItem
import java.util.Locale

class ContabilidadAdapter(private var items: List<InventarioItem>) :
    RecyclerView.Adapter<ContabilidadAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvStockInfo: TextView = view.findViewById(R.id.tvStockInfo)
        val tvCostoUnit: TextView = view.findViewById(R.id.tvCostoUnit)
        val tvPrecioUnit: TextView = view.findViewById(R.id.tvPrecioUnit)
        val tvGanancia: TextView = view.findViewById(R.id.tvGanancia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contabilidad_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvNombre.text = item.nombre
        holder.tvStockInfo.text = "Stock: ${formatValue(item.stock)} ${item.unidad}"
        holder.tvCostoUnit.text = "C: $${String.format(Locale.US, "%.2f", item.costo)}"
        holder.tvPrecioUnit.text = "P: $${String.format(Locale.US, "%.2f", item.precio)}"
        
        val ganancia = item.precio - item.costo
        holder.tvGanancia.text = "${if (ganancia >= 0) "+" else ""}$${String.format(Locale.US, "%.2f", ganancia)}"
        
        if (ganancia < 0) {
            holder.tvGanancia.setTextColor(holder.itemView.context.getColor(R.color.status_red))
        } else {
            holder.tvGanancia.setTextColor(holder.itemView.context.getColor(R.color.status_green))
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<InventarioItem>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    private fun formatValue(value: Double): String {
        return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
    }
}
