package pa.ac.utp.agrotrackapp.ui.mortalidad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.MortalidadRecord

/**
 * Adaptador de Recycler View para mostrar el historial de mortalidad.
 */
class MortalidadAdapter : RecyclerView.Adapter<MortalidadAdapter.ViewHolder>() {

    private var list: List<MortalidadRecord> = emptyList()

    fun submitList(newList: List<MortalidadRecord>) {
        list = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvArete: TextView = itemView.findViewById(R.id.tvRowArete)
        val tvCausa: TextView = itemView.findViewById(R.id.tvRowCausa)
        val tvDetalles: TextView = itemView.findViewById(R.id.tvRowDetalles)
        val tvFecha: TextView = itemView.findViewById(R.id.tvRowFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_mortalidad, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = list[position]
        holder.tvArete.text = "Arete: ${record.numeroAnimal}"
        holder.tvCausa.text = record.causa.uppercase()
        holder.tvDetalles.text = record.detalles
        holder.tvFecha.text = "Fecha: ${record.fechaMuerte}"
    }

    override fun getItemCount() = list.size
}
