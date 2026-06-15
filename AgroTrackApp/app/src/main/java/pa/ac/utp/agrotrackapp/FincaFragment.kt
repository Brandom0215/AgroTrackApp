package pa.ac.utp.agrotrackapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView

class FincaFragment : Fragment(R.layout.fragment_finca) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Configurar Drawer Button
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // Configurar tarjeta de producción
        view.findViewById<MaterialCardView>(R.id.cardProduccion)?.setOnClickListener {
            (requireActivity() as MainActivity).navigateToTab(R.id.nav_produccion)
        }
    }
}
