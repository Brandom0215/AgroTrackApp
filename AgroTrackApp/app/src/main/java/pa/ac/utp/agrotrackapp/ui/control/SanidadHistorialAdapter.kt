package pa.ac.utp.agrotrackapp.ui.control

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.RegistroSanitario

/**
 * Adapter para la vista de Historial en Control Sanitario.
 * Cada item representa un GRUPO de dosis (mismo grupoId) con su timeline.
 */
class SanidadHistorialAdapter :
    ListAdapter<List<RegistroSanitario>, SanidadHistorialAdapter.GrupoViewHolder>(GrupoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrupoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_grupo_sanitario, parent, false)
        return GrupoViewHolder(view)
    }

    override fun onBindViewHolder(holder: GrupoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GrupoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIdentificador: TextView = itemView.findViewById(R.id.tvHIdentificador)
        private val chipCategoria: Chip = itemView.findViewById(R.id.chipHCategoria)
        private val tvDetalle: TextView = itemView.findViewById(R.id.tvHDetalle)
        private val tvDosis: TextView = itemView.findViewById(R.id.tvHDosis)
        private val tvVet: TextView = itemView.findViewById(R.id.tvHVet)
        private val llDosisContainer: LinearLayout = itemView.findViewById(R.id.llDosisContainer)

        fun bind(grupo: List<RegistroSanitario>) {
            if (grupo.isEmpty()) return
            val ref = grupo.first() // use first dose for header info

            // Header
            tvIdentificador.text = if (ref.alcance == "masivo") "Lote: ${ref.identificador}" else ref.identificador

            val labelPrefix = when (ref.categoria) {
                "Vacunación"  -> "Vacuna"
                "Tratamiento" -> "Tratamiento"
                "Mastitis"    -> "Mastitis"
                else          -> ref.categoria
            }
            tvDetalle.text = if (ref.producto.isNotEmpty()) {
                "$labelPrefix: ${ref.detalle} — ${ref.producto}"
            } else {
                "$labelPrefix: ${ref.detalle}"
            }
            tvDosis.text = "💉 Dosis: ${ref.dosis.ifEmpty { "—" }}"
            tvVet.text   = "🩺 Vet: ${ref.veterinario.ifEmpty { "—" }}"

            chipCategoria.text = ref.categoria
            when (ref.categoria) {
                "Vacunación"  -> { chipCategoria.setChipBackgroundColorResource(R.color.md_theme_light_primaryContainer); chipCategoria.setTextColor(Color.parseColor("#3182CE")) }
                "Tratamiento" -> { chipCategoria.setChipBackgroundColorResource(R.color.md_theme_light_secondaryContainer); chipCategoria.setTextColor(Color.parseColor("#38A169")) }
                "Mastitis"    -> { chipCategoria.setChipBackgroundColorResource(R.color.md_theme_light_errorContainer); chipCategoria.setTextColor(Color.parseColor("#E53E3E")) }
            }

            // Build dose timeline
            llDosisContainer.removeAllViews()
            val context = itemView.context
            val dosesOrdered = grupo.sortedBy {
                // applied doses by fecha, pending by proximaDosis
                if (it.estado == "aplicado") it.fecha else it.proximaDosis
            }

            dosesOrdered.forEachIndexed { index, dose ->
                val row = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.bottomMargin = if (index < dosesOrdered.size - 1) 8.dpToPx(context) else 0
                    layoutParams = lp
                }

                // Dot indicator
                val dot = View(context).apply {
                    val size = 10.dpToPx(context)
                    layoutParams = LinearLayout.LayoutParams(size, size).also { it.marginEnd = 10.dpToPx(context) }
                    background = ContextCompat.getDrawable(context,
                        if (dose.estado == "aplicado") android.R.drawable.presence_online
                        else android.R.drawable.presence_away
                    )
                }
                row.addView(dot)

                // Dose number
                val numTv = TextView(context).apply {
                    text = "Dosis ${index + 1}:"
                    textSize = 12f
                    setTextColor(Color.parseColor("#718096"))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.marginEnd = 6.dpToPx(context) }
                }
                row.addView(numTv)

                // Date
                val dateTv = TextView(context).apply {
                    text = if (dose.estado == "aplicado") dose.fecha else dose.proximaDosis
                    textSize = 12f
                    setTextColor(Color.parseColor("#2D3748"))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                row.addView(dateTv)

                // Status chip
                val statusTv = TextView(context).apply {
                    text = if (dose.estado == "aplicado") "✔ Aplicada" else "○ Pendiente"
                    textSize = 11f
                    setTextColor(if (dose.estado == "aplicado") Color.parseColor("#38A169") else Color.parseColor("#E2903A"))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                row.addView(statusTv)

                llDosisContainer.addView(row)
            }
        }

        private fun Int.dpToPx(context: android.content.Context): Int =
            (this * context.resources.displayMetrics.density).toInt()
    }

    class GrupoDiffCallback : DiffUtil.ItemCallback<List<RegistroSanitario>>() {
        override fun areItemsTheSame(oldItem: List<RegistroSanitario>, newItem: List<RegistroSanitario>): Boolean {
            val oldKey = oldItem.firstOrNull()?.grupoId?.ifEmpty { oldItem.firstOrNull()?.id } ?: ""
            val newKey = newItem.firstOrNull()?.grupoId?.ifEmpty { newItem.firstOrNull()?.id } ?: ""
            return oldKey == newKey
        }
        override fun areContentsTheSame(oldItem: List<RegistroSanitario>, newItem: List<RegistroSanitario>): Boolean {
            return oldItem == newItem
        }
    }
}
