package pa.ac.utp.agrotrackapp.ui.insumos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.InventarioItem

class InventarioHistorialAdapter(
    private var itemsList: List<InventarioItem>
) : RecyclerView.Adapter<InventarioHistorialAdapter.HistorialViewHolder>() {

    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivHistorialIcon: ImageView = itemView.findViewById(R.id.ivHistorialIcon)
        val tvHistorialNombre: TextView = itemView.findViewById(R.id.tvHistorialNombre)
        val tvHistorialTipo: TextView = itemView.findViewById(R.id.tvHistorialTipo)
        val tvHistorialFecha: TextView = itemView.findViewById(R.id.tvHistorialFecha)
        val tvHistorialCantidad: TextView = itemView.findViewById(R.id.tvHistorialCantidad)
        val tvHistorialCosto: TextView = itemView.findViewById(R.id.tvHistorialCosto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historial_inventario, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val item = itemsList[position]

        holder.tvHistorialNombre.text = item.nombre
        
        val displayTipo = if (item.tipo == "Otro") {
            item.tipoOtro ?: "Otro"
        } else {
            item.tipo
        }
        holder.tvHistorialTipo.text = displayTipo
        holder.tvHistorialFecha.text = "Reg: ${item.fechaRegistro}"

        val stockStr = if (item.stock % 1.0 == 0.0) item.stock.toInt().toString() else item.stock.toString()
        holder.tvHistorialCantidad.text = "$stockStr ${item.unidad}"
        
        holder.tvHistorialCosto.text = String.format("Costo: $%.2f", item.costo)

        // Set type icon
        val iconRes = when (item.tipo) {
            "Alimento" -> R.drawable.ic_finca
            "Medicina" -> R.drawable.ic_alertas
            "Herramienta" -> R.drawable.ic_produccion
            else -> R.drawable.ic_finca
        }
        holder.ivHistorialIcon.setImageResource(iconRes)
    }

    override fun getItemCount() = itemsList.size

    fun updateData(newItems: List<InventarioItem>) {
        itemsList = newItems
        notifyDataSetChanged()
    }
}
