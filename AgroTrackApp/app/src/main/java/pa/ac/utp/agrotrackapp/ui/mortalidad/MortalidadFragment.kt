package pa.ac.utp.agrotrackapp.ui.mortalidad

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.ui.main.MainActivity

class MortalidadFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mortalidad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        // Forzar el ícono de menú en la barra superior
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        // Tint the icon white so it's visible on the green background
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        val rvHistorial = view.findViewById<RecyclerView>(R.id.rvHistorial)
        val listaPrueba = mutableListOf("136", "245", "089", "450", "112")
        rvHistorial.layoutManager = LinearLayoutManager(context)
        rvHistorial.adapter = MortalidadAdapter(listaPrueba)
    }
}
