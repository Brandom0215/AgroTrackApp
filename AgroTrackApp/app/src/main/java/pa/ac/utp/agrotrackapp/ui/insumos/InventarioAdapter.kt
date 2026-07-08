package pa.ac.utp.agrotrackapp.ui.insumos

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.InventarioItem
import java.io.File

class InventarioAdapter(
    private var itemsList: List<InventarioItem>,
    private val onItemClick: (InventarioItem) -> Unit,
    private val onDeleteClick: (InventarioItem) -> Unit
) : RecyclerView.Adapter<InventarioAdapter.InventarioViewHolder>() {

    class InventarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProducto: ImageView = itemView.findViewById(R.id.ivProducto)
        val tvChipTipo: TextView = itemView.findViewById(R.id.tvChipTipo)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombre)
        val tvStock: TextView = itemView.findViewById(R.id.tvStock)
        val tvUnidad: TextView = itemView.findViewById(R.id.tvUnidad)
        val tvAlertaStock: TextView = itemView.findViewById(R.id.tvAlertaStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventario_card, parent, false)
        return InventarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventarioViewHolder, position: Int) {
        val item = itemsList[position]
        
        holder.tvNombre.text = item.nombre
        
        val stockStr = if (item.stock % 1.0 == 0.0) item.stock.toInt().toString() else item.stock.toString()
        holder.tvStock.text = stockStr
        holder.tvUnidad.text = item.unidad

        // Tipo chip configuration
        val displayTipo = if (item.tipo == "Otro") {
            item.tipoOtro?.uppercase() ?: "OTRO"
        } else {
            item.tipo.uppercase()
        }
        holder.tvChipTipo.text = displayTipo

        // Load image
        if (!item.fotoPath.isNullOrEmpty()) {
            val imgFile = File(item.fotoPath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                holder.ivProducto.setImageBitmap(bitmap)
            } else {
                holder.ivProducto.setImageResource(R.drawable.icono_ganadex)
            }
        } else {
            holder.ivProducto.setImageResource(R.drawable.icono_ganadex)
        }

        // Show/hide stock warning if it's Alimento or Medicina and is under threshold
        val isAlimentoOrMedicina = item.tipo == "Alimento" || item.tipo == "Medicina"
        val limite = item.limiteNotificacion
        if (isAlimentoOrMedicina && limite != null && item.stock <= limite) {
            holder.tvAlertaStock.visibility = View.VISIBLE
            val limitStr = if (limite % 1.0 == 0.0) limite.toInt().toString() else limite.toString()
            holder.tvAlertaStock.text = "⚠️ STOCK CRÍTICO (MIN $limitStr)"
        } else {
            holder.tvAlertaStock.visibility = View.GONE
        }

        // Action listeners
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.btnEditar.setOnClickListener {
            onItemClick(item)
        }

        holder.btnEliminar.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount() = itemsList.size

    fun updateData(newItems: List<InventarioItem>) {
        itemsList = newItems
        notifyDataSetChanged()
    }
}
