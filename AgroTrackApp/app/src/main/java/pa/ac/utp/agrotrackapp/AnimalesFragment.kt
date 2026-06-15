package pa.ac.utp.agrotrackapp

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class AnimalesFragment : Fragment(R.layout.fragment_animales) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        view.findViewById<View>(R.id.fabAddAnimal)?.setOnClickListener {
            val intent = android.content.Intent(requireContext(), CrearAnimalActivity::class.java)
            startActivity(intent)
        }
    }
}
