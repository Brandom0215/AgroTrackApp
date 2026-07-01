package pa.ac.utp.agrotrackapp.ui.insumos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.Insumo
import pa.ac.utp.agrotrackapp.ui.main.MainActivity

/**
 * Fragmento para la Gestión de Insumos (Inventario de Alimentos).
 * Al ser un Fragment dentro de MainActivity, conserva automáticamente la barra inferior.
 */
class InsumosFragment : Fragment(R.layout.fragment_insumos) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar botón de menú lateral (Drawer)
        // Usamos (requireActivity() as MainActivity) para acceder a la función openDrawer()
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // 2. Configurar botón para agregar nuevo insumo
        view.findViewById<View>(R.id.btnAdd).setOnClickListener {
            val intent = Intent(requireContext(), CrearInsumoActivity::class.java)
            startActivity(intent)
        }

        // 3. Inicializar el RecyclerView con diseño de cuadrícula (2 columnas)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewInsumos)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Datos de ejemplo (se mantienen igual que en la Activity original)
        val insumos = listOf(
            Insumo("LEVADURA", 180.0, "litros", "#4CAF50"),
            Insumo("SAL MINERAL", 60.0, "sacos", "#4CAF50"),
            Insumo("AFRECHO DE CERVEZA", 320.0, "sacos", "#FFA000"),
            Insumo("GALLINAZA", 150.0, "sacos", "#4CAF50"),
            Insumo("MAÍZ MOLIDO", 200.0, "sacos", "#FFA000")
        )

        // 4. Configurar el adaptador con el click listener para editar
        recyclerView.adapter = InsumosAdapter(insumos) { insumo ->
            val intent = Intent(requireContext(), CrearInsumoActivity::class.java)
            intent.putExtra("insumo_nombre", insumo.nombre)
            intent.putExtra("insumo_cantidad", insumo.cantidad)
            intent.putExtra("insumo_unidad", insumo.unidad)
            startActivity(intent)
        }
    }
}
