package pa.ac.utp.agrotrackapp.ui.control

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.ui.main.MainActivity

class ControlSanitarioFragment : Fragment(R.layout.fragment_control_sanitario) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar Drawer Button
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
    }
}
