package pa.ac.utp.agrotrackapp.ui.produccion

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.produccion.SharedPrefsProduccionRepository
import pa.ac.utp.agrotrackapp.domain.repository.ProduccionRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity

class ProduccionFragment : Fragment(R.layout.fragment_produccion) {

    private lateinit var produccionRepository: ProduccionRepository
    private lateinit var tvKpiCarneCount: TextView
    private lateinit var tvKpiLecheCount: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos repositorio de producción
        produccionRepository = SharedPrefsProduccionRepository(requireContext())

        // Vincular vistas
        tvKpiCarneCount = view.findViewById(R.id.tvKpiCarneCount)
        tvKpiLecheCount = view.findViewById(R.id.tvKpiLecheCount)

        // Botón Menú Lateral
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // Navegación a Módulo Carne
        view.findViewById<MaterialCardView>(R.id.btnModuleCarne).setOnClickListener {
            val intent = Intent(requireContext(), CarneProduccionActivity::class.java)
            startActivity(intent)
        }

        // Navegación a Módulo Leche
        view.findViewById<MaterialCardView>(R.id.btnModuleLeche).setOnClickListener {
            val intent = Intent(requireContext(), LecheProduccionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Cargar y actualizar KPIs dinámicos al volver a la pantalla
        actualizarKpis()
    }

    private fun actualizarKpis() {
        val carneCount = produccionRepository.getCarneRecords().count { it.activo }
        val lecheCount = produccionRepository.getLecheRecords().count { it.activo }

        tvKpiCarneCount.text = if (carneCount == 1) "1 Animal" else "$carneCount Animales"
        tvKpiLecheCount.text = if (lecheCount == 1) "1 Vaca" else "$lecheCount Vacas"
    }
}
