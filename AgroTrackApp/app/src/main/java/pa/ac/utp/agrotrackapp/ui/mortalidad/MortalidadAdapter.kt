package pa.ac.utp.agrotrackapp.ui.mortalidad

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R

/**
 * Adaptador de Recycler View para mostrar el historial de mortalidad.
 * Conversión de Java a Kotlin con corrección de nomenclatura (PascalCase).
 */
class MortalidadAdapter(
    private val listaIds: List<String>
) : RecyclerView.Adapter<MortalidadAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView = itemView.findViewById(R.id.tvTrazabilidad)
        val tvCausa: TextView = itemView.findViewById(R.id.tvCausa)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_mortalidad, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Asignamos la información (ejemplo ficticio)
        holder.tvId.text = "ID: " + listaIds[position]
        holder.tvCausa.text = "Causa: Pendiente de diagnóstico"
    }

    override fun getItemCount() = listaIds.size
}
