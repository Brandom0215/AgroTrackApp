package pa.ac.utp.agrotrackapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InsumosAdapter(
    private val insumosList: List<Insumo>,
    private val onItemClick: (Insumo) -> Unit
) : RecyclerView.Adapter<InsumosAdapter.InsumoViewHolder>() {

    class InsumoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorBar: View = itemView.findViewById(R.id.colorBar)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvCantidad: TextView = itemView.findViewById(R.id.tvCantidad)
        val tvUnidad: TextView = itemView.findViewById(R.id.tvUnidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsumoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_insumo_card, parent, false)
        return InsumoViewHolder(view)
    }

    override fun onBindViewHolder(holder: InsumoViewHolder, position: Int) {
        val insumo = insumosList[position]
        holder.tvNombre.text = insumo.nombre
        // Format number without decimals if it's a whole number
        val cantStr = if (insumo.cantidad % 1.0 == 0.0) insumo.cantidad.toInt().toString() else insumo.cantidad.toString()
        holder.tvCantidad.text = cantStr
        holder.tvUnidad.text = insumo.unidad
        try {
            holder.colorBar.setBackgroundColor(Color.parseColor(insumo.colorHex))
        } catch (e: Exception) {
            holder.colorBar.setBackgroundColor(Color.parseColor("#4CAF50")) // Fallback green
        }

        holder.itemView.setOnClickListener {
            onItemClick(insumo)
        }
    }

    override fun getItemCount() = insumosList.size
}
