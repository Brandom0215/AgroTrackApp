package pa.ac.utp.agrotrackapp.ui.alertas

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.ui.main.MainActivity

class AlertasFragment : Fragment(R.layout.fragment_alertas) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
    }
}
