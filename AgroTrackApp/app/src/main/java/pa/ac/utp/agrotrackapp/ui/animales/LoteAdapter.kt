package pa.ac.utp.agrotrackapp.ui.animales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.Lote

class LoteAdapter(
    private var lotes: List<Lote>,
    private val onItemClick: (Lote) -> Unit,
    private val onEditClick: (Lote) -> Unit,
    private val onDeleteClick: (Lote) -> Unit
) : RecyclerView.Adapter<LoteAdapter.LoteViewHolder>() {

    var isEditMode = false

    class LoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreLote: TextView = view.findViewById(R.id.tvNombreLote)
        val tvCantidadAnimales: TextView = view.findViewById(R.id.tvCantidadAnimales)
        val llActionButtons: View = view.findViewById(R.id.llActionButtons)
        val btnEditLote: View = view.findViewById(R.id.btnEditLote)
        val btnDeleteLote: View = view.findViewById(R.id.btnDeleteLote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lote, parent, false)
        return LoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoteViewHolder, position: Int) {
        val lote = lotes[position]
        holder.tvNombreLote.text = lote.nombre
        holder.tvCantidadAnimales.text = lote.cantidadAnimales.toString()

        holder.llActionButtons.visibility = if (isEditMode) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            if (!isEditMode) {
                onItemClick(lote)
            }
        }

        holder.btnEditLote.setOnClickListener {
            onEditClick(lote)
        }

        holder.btnDeleteLote.setOnClickListener {
            onDeleteClick(lote)
        }
    }

    override fun getItemCount() = lotes.size

    fun updateList(newList: List<Lote>) {
        lotes = newList
        notifyDataSetChanged()
    }

    fun getLoteAt(position: Int): Lote {
        return lotes[position]
    }
}
