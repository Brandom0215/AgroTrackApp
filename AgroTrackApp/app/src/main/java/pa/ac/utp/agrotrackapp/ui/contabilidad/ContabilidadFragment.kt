package pa.ac.utp.agrotrackapp.ui.contabilidad

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.ui.main.MainActivity

class ContabilidadFragment : Fragment(R.layout.fragment_contabilidad) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar Drawer Button
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
    }
}
