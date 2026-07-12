package pa.ac.utp.agrotrackapp.ui.control

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.RegistroSanitario

class SanidadAdapter(
    private val onCompletarClick: (RegistroSanitario) -> Unit
) : ListAdapter<RegistroSanitario, SanidadAdapter.SanidadViewHolder>(SanidadDiffCallback()) {

    var isProximasMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SanidadViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_registro_sanitario, parent, false)
        return SanidadViewHolder(view)
    }

    override fun onBindViewHolder(holder: SanidadViewHolder, position: Int) {
        holder.bind(getItem(position), isProximasMode, onCompletarClick)
    }

    class SanidadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivIconoCategoria: ImageView = itemView.findViewById(R.id.ivIconoCategoria)
        private val tvIdentificador: TextView = itemView.findViewById(R.id.tvIdentificador)
        private val chipCategoria: Chip = itemView.findViewById(R.id.chipCategoria)
        private val tvDetalle: TextView = itemView.findViewById(R.id.tvDetalle)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        private val btnMarcarAplicado: MaterialButton = itemView.findViewById(R.id.btnMarcarAplicado)
        private val btnVerDetalle: ImageButton = itemView.findViewById(R.id.btnVerDetalle)

        fun bind(registro: RegistroSanitario, isProximasMode: Boolean, onCompletarClick: (RegistroSanitario) -> Unit) {
            tvIdentificador.text = if (registro.alcance == "masivo") "Lote: ${registro.identificador}" else registro.identificador
            chipCategoria.text = registro.categoria

            // Build labelled detalle based on category
            val labelPrefix = when (registro.categoria) {
                "Vacunación"  -> "Vacuna"
                "Tratamiento" -> "Tratamiento"
                "Mastitis"    -> "Mastitis"
                else          -> registro.categoria
            }
            tvDetalle.text = if (registro.producto.isNotEmpty()) {
                "$labelPrefix: ${registro.detalle} — ${registro.producto}"
            } else {
                "$labelPrefix: ${registro.detalle}"
            }

            if (isProximasMode) {
                tvFecha.text = "📅 Programado para: ${registro.proximaDosis}"
                btnMarcarAplicado.visibility = View.VISIBLE
                btnVerDetalle.visibility = View.GONE
                btnMarcarAplicado.setOnClickListener { onCompletarClick(registro) }
            } else {
                tvFecha.text = "✔ Aplicado: ${registro.fecha}"
                btnMarcarAplicado.visibility = View.GONE
                btnVerDetalle.visibility = View.VISIBLE
                btnVerDetalle.setOnClickListener { mostrarDetalle(registro) }
            }

            when (registro.categoria) {
                "Vacunación" -> {
                    chipCategoria.setChipBackgroundColorResource(R.color.md_theme_light_primaryContainer)
                    chipCategoria.setTextColor(Color.parseColor("#3182CE"))
                }
                "Tratamiento" -> {
                    chipCategoria.setChipBackgroundColorResource(R.color.md_theme_light_secondaryContainer)
                    chipCategoria.setTextColor(Color.parseColor("#38A169"))
                }
                "Mastitis" -> {
                    chipCategoria.setChipBackgroundColorResource(R.color.md_theme_light_errorContainer)
                    chipCategoria.setTextColor(Color.parseColor("#E53E3E"))
                }
                else -> {
                    chipCategoria.setChipBackgroundColorResource(R.color.md_theme_light_surfaceVariant)
                }
            }
        }

        private fun mostrarDetalle(registro: RegistroSanitario) {
            val context = itemView.context
            val dialog = BottomSheetDialog(context)
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_detalle_registro_sanitario, null)
            dialog.setContentView(view)

            view.findViewById<TextView>(R.id.tvDetalleIdentificador).text =
                if (registro.alcance == "masivo") "Lote: ${registro.identificador}" else registro.identificador

            view.findViewById<TextView>(R.id.tvDetalleAlcance).text =
                if (registro.alcance == "masivo") "Aplicación masiva" else "Animal individual"

            val chipCat = view.findViewById<Chip>(R.id.chipDetalleCategoria)
            chipCat.text = registro.categoria
            when (registro.categoria) {
                "Vacunación"  -> { chipCat.setChipBackgroundColorResource(R.color.md_theme_light_primaryContainer); chipCat.setTextColor(Color.parseColor("#3182CE")) }
                "Tratamiento" -> { chipCat.setChipBackgroundColorResource(R.color.md_theme_light_secondaryContainer); chipCat.setTextColor(Color.parseColor("#38A169")) }
                "Mastitis"    -> { chipCat.setChipBackgroundColorResource(R.color.md_theme_light_errorContainer); chipCat.setTextColor(Color.parseColor("#E53E3E")) }
            }

            view.findViewById<TextView>(R.id.tvDetalleCategoria).text = registro.categoria
            view.findViewById<TextView>(R.id.tvDetalleDetalle).text = registro.detalle.ifEmpty { "—" }
            view.findViewById<TextView>(R.id.tvDetalleDosis).text = registro.dosis.ifEmpty { "—" }
            view.findViewById<TextView>(R.id.tvDetalleFecha).text = registro.fecha.ifEmpty { registro.proximaDosis }
            view.findViewById<TextView>(R.id.tvDetalleVet).text = registro.veterinario.ifEmpty { "—" }

            // Producto row
            val rowProducto = view.findViewById<LinearLayout>(R.id.rowProducto)
            if (registro.producto.isNotEmpty()) {
                rowProducto.visibility = View.VISIBLE
                view.findViewById<TextView>(R.id.tvDetalleProducto).text = registro.producto
            } else {
                rowProducto.visibility = View.GONE
            }

            // Estado chip
            val chipEstado = view.findViewById<Chip>(R.id.chipDetalleEstado)
            chipEstado.text = if (registro.estado == "aplicado") "Aplicado ✔" else "Programado 📅"
            if (registro.estado == "aplicado") {
                chipEstado.setChipBackgroundColorResource(R.color.md_theme_light_secondaryContainer)
                chipEstado.setTextColor(Color.parseColor("#38A169"))
            } else {
                chipEstado.setChipBackgroundColorResource(R.color.md_theme_light_primaryContainer)
                chipEstado.setTextColor(Color.parseColor("#3182CE"))
            }

            view.findViewById<MaterialButton>(R.id.btnCerrarDetalle).setOnClickListener { dialog.dismiss() }

            dialog.show()
        }
    }
}

class SanidadDiffCallback : DiffUtil.ItemCallback<RegistroSanitario>() {
    override fun areItemsTheSame(oldItem: RegistroSanitario, newItem: RegistroSanitario): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: RegistroSanitario, newItem: RegistroSanitario): Boolean {
        return oldItem == newItem
    }
}
