package pa.ac.utp.agrotrackapp.ui.alertas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.Alerta
import pa.ac.utp.agrotrackapp.domain.model.PrioridadAlerta
import pa.ac.utp.agrotrackapp.domain.model.TipoAlerta

class AlertasAdapter(
    private val onIrClick: (Alerta) -> Unit
) : ListAdapter<Alerta, AlertasAdapter.AlertaViewHolder>(AlertaDiffCallback()) {

    var isHistoryMode: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alerta, parent, false)
        return AlertaViewHolder(view, onIrClick)
    }

    override fun onBindViewHolder(holder: AlertaViewHolder, position: Int) {
        holder.bind(getItem(position), isHistoryMode)
    }

    class AlertaViewHolder(
        itemView: View,
        private val onIrClick: (Alerta) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val viewPrioridad: View = itemView.findViewById(R.id.viewPrioridad)
        private val tvTitulo: TextView = itemView.findViewById(R.id.tvAlertaTitulo)
        private val tvDescripcion: TextView = itemView.findViewById(R.id.tvAlertaDescripcion)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvAlertaFecha)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivAlertaIcon)
        private val btnIr: View = itemView.findViewById(R.id.btnIr)

        fun bind(alerta: Alerta, isHistoryMode: Boolean) {
            tvTitulo.text = alerta.titulo
            tvDescripcion.text = alerta.descripcion
            tvFecha.text = alerta.fecha

            btnIr.visibility = if (alerta.destinationId != null) View.VISIBLE else View.GONE
            btnIr.setOnClickListener { onIrClick(alerta) }

            val context = itemView.context
            val colorPrioridad = when (alerta.prioridad) {
                PrioridadAlerta.ALTA -> ContextCompat.getColor(context, android.R.color.holo_red_dark)
                PrioridadAlerta.MEDIA -> ContextCompat.getColor(context, android.R.color.holo_orange_dark)
                PrioridadAlerta.BAJA -> ContextCompat.getColor(context, android.R.color.holo_blue_dark)
            }
            viewPrioridad.setBackgroundColor(colorPrioridad)

            val iconRes = when (alerta.tipo) {
                TipoAlerta.STOCK_MINIMO -> R.drawable.inventario
                TipoAlerta.MORTALIDAD_ALTA -> R.drawable.mortalidad
                TipoAlerta.PESAJE_PENDIENTE -> R.drawable.pesaje
                TipoAlerta.RECORDATORIO -> R.drawable.ic_alertas
            }
            ivIcon.setImageResource(iconRes)

            if (isHistoryMode) {
                itemView.alpha = 0.5f
                btnIr.isEnabled = false
                btnIr.isClickable = false
            } else {
                itemView.alpha = 1.0f
                btnIr.isEnabled = true
                btnIr.isClickable = true
            }
        }
    }

    class AlertaDiffCallback : DiffUtil.ItemCallback<Alerta>() {
        override fun areItemsTheSame(oldItem: Alerta, newItem: Alerta): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Alerta, newItem: Alerta): Boolean = oldItem == newItem
    }
}
