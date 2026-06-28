package pa.ac.utp.agrotrackapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.repository.AuthRepository
import pa.ac.utp.agrotrackapp.data.auth.SharedPrefsAuthRepository
import pa.ac.utp.agrotrackapp.ui.auth.LoginActivity
import pa.ac.utp.agrotrackapp.ui.finca.FincaFragment
import pa.ac.utp.agrotrackapp.ui.animales.AnimalesFragment
import pa.ac.utp.agrotrackapp.ui.produccion.ProduccionFragment
import pa.ac.utp.agrotrackapp.ui.alertas.AlertasFragment
import pa.ac.utp.agrotrackapp.ui.control.ControlSanitarioFragment
import pa.ac.utp.agrotrackapp.ui.pesaje.PesajeFragment
import pa.ac.utp.agrotrackapp.ui.mortalidad.MortalidadFragment
import pa.ac.utp.agrotrackapp.ui.insumos.InsumosActivity
import pa.ac.utp.agrotrackapp.ui.contabilidad.ContabilidadFragment

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        // Inicializamos el repositorio de datos (desacoplado)
        authRepository = SharedPrefsAuthRepository(this)

        drawerLayout = findViewById(R.id.drawerLayout)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        setupDrawer()
        setupBottomNavigation()
        setupBackPress()

        // Cargamos los datos reales del usuario logueado en la cabecera del menú lateral
        actualizarHeaderUsuario()

        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.nav_finca
        }
    }

    /**
     * Obtiene el usuario activo y actualiza los campos de texto del Drawer Navigation.
     */
    private fun actualizarHeaderUsuario() {
        try {
            val navigationView = findViewById<NavigationView>(R.id.navigationView)
            val headerView = navigationView.getHeaderView(0)
            
            val tvFincaNombre = headerView.findViewById<TextView>(R.id.tvHeaderFincaNombre)
            val tvUserName = headerView.findViewById<TextView>(R.id.tvHeaderUserName)
            
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                tvFincaNombre.text = currentUser.nombreFinca
                tvUserName.text = "${currentUser.nombre} ${currentUser.apellido} - ${currentUser.lugar}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_finca -> FincaFragment()
                R.id.nav_animales -> AnimalesFragment()
                R.id.nav_produccion -> ProduccionFragment()
                R.id.nav_alertas -> AlertasFragment()
                else -> FincaFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
            true
        }
    }

    fun navigateToTab(itemId: Int) {
        bottomNavigation.menu.setGroupCheckable(0, true, true)
        if (bottomNavigation.selectedItemId == itemId) {
            val fragment: Fragment = when (itemId) {
                R.id.nav_finca -> FincaFragment()
                R.id.nav_animales -> AnimalesFragment()
                R.id.nav_produccion -> ProduccionFragment()
                R.id.nav_alertas -> AlertasFragment()
                else -> FincaFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        } else {
            bottomNavigation.selectedItemId = itemId
        }
    }

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun setupDrawer() {
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.drawer_inicio            -> navigateToTab(R.id.nav_finca)
                R.id.drawer_gestion_ganado    -> navigateToTab(R.id.nav_animales)
                R.id.drawer_control_sanitario -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ControlSanitarioFragment()).commit()
                    bottomNavigation.menu.setGroupCheckable(0, false, true)
                }
                R.id.drawer_produccion        -> navigateToTab(R.id.nav_produccion)
                R.id.drawer_pesaje            -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, PesajeFragment()).commit()
                    bottomNavigation.menu.setGroupCheckable(0, false, true)
                }
                R.id.drawer_mortalidad        -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MortalidadFragment()).commit()
                    bottomNavigation.menu.setGroupCheckable(0, false, true)
                }
                R.id.drawer_gestion_insumos   -> { startActivity(Intent(this, InsumosActivity::class.java)) }
                R.id.drawer_contabilidad      -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ContabilidadFragment()).commit()
                    bottomNavigation.menu.setGroupCheckable(0, false, true)
                }
                R.id.drawer_cerrar_sesion     -> {
                    // Cerrar sesión en el repositorio y volver al login
                    authRepository.logout()
                    Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else if (bottomNavigation.selectedItemId != R.id.nav_finca) {
                bottomNavigation.selectedItemId = R.id.nav_finca
            } else {
                finish()
            }
        }
    }
}
