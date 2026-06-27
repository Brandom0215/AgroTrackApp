package pa.ac.utp.agrotrackapp.ui.finca

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.repository.AuthRepository
import pa.ac.utp.agrotrackapp.data.auth.SharedPrefsAuthRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import pa.ac.utp.agrotrackapp.ui.animales.CrearAnimalActivity

class FincaFragment : Fragment(R.layout.fragment_finca) {

    private lateinit var authRepository: AuthRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializamos el repositorio de datos de sesión
        authRepository = SharedPrefsAuthRepository(requireContext())
        
        // Cargamos los datos reales del usuario logueado
        val tvFincaNombre = view.findViewById<TextView>(R.id.tvFincaNombreHome)
        val tvUsuarioActivo = view.findViewById<TextView>(R.id.tvUsuarioActivoHome)
        
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            tvFincaNombre?.text = "Finca: ${currentUser.nombreFinca}"
            tvUsuarioActivo?.text = "Productor: ${currentUser.nombre} ${currentUser.apellido}"
        }
        
        // Configurar Drawer Button
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // Configurar tarjeta de producción para navegar a la pestaña de producción
        view.findViewById<MaterialCardView>(R.id.cardProduccion)?.setOnClickListener {
            (requireActivity() as MainActivity).navigateToTab(R.id.nav_produccion)
        }

        // Configurar botón flotante para registrar un nuevo animal
        view.findViewById<View>(R.id.fabAddAnimalHome)?.setOnClickListener {
            val intent = Intent(requireContext(), CrearAnimalActivity::class.java)
            startActivity(intent)
        }
    }
}
