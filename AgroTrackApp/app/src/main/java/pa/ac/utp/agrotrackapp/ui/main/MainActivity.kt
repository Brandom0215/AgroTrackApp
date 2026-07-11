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
import pa.ac.utp.agrotrackapp.ui.insumos.InventarioFragment
import pa.ac.utp.agrotrackapp.ui.contabilidad.ContabilidadFragment
import pa.ac.utp.agrotrackapp.ui.auth.PerfilUsuarioActivity

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
            val openAlertas = intent.getBooleanExtra("OPEN_ALERTAS", false)
            if (openAlertas) {
                bottomNavigation.selectedItemId = R.id.nav_alertas
            } else {
                bottomNavigation.selectedItemId = R.id.nav_finca
            }
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarHeaderUsuario()
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
            val ivHeaderProfile = headerView.findViewById<android.widget.ImageView>(R.id.ivHeaderProfile)
            
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                tvFincaNombre.text = currentUser.nombreFinca
                tvUserName.text = "${currentUser.nombre} ${currentUser.apellido} - ${currentUser.rol}"
                
                if (ivHeaderProfile != null) {
                    if (!currentUser.profileImagePath.isNullOrEmpty()) {
                        val file = java.io.File(currentUser.profileImagePath)
                        if (file.exists()) {
                            val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                            ivHeaderProfile.setImageBitmap(bitmap)
                            ivHeaderProfile.imageTintList = null
                            ivHeaderProfile.setPadding(0, 0, 0, 0)
                        } else {
                            ivHeaderProfile.setImageResource(R.drawable.vaca)
                            val paddingPx = (12 * resources.displayMetrics.density).toInt()
                            ivHeaderProfile.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                            ivHeaderProfile.imageTintList = android.content.res.ColorStateList.valueOf(
                                androidx.core.content.ContextCompat.getColor(this, R.color.md_theme_light_primary)
                            )
                        }
                    } else {
                        ivHeaderProfile.setImageResource(R.drawable.vaca)
                        val paddingPx = (12 * resources.displayMetrics.density).toInt()
                        ivHeaderProfile.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                        ivHeaderProfile.imageTintList = android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(this, R.color.md_theme_light_primary)
                        )
                    }
                }
            }

            // Click en la cabecera abre el perfil
            headerView.setOnClickListener {
                startActivity(Intent(this, PerfilUsuarioActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
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
        val bottomIds = listOf(R.id.nav_finca, R.id.nav_animales, R.id.nav_produccion, R.id.nav_alertas)
        
        if (itemId in bottomIds) {
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
        } else {
            // Manejar navegación a fragmentos del Drawer que no están en el Bottom Nav
            val fragment: Fragment? = when (itemId) {
                R.id.drawer_control_sanitario -> ControlSanitarioFragment()
                R.id.drawer_pesaje -> PesajeFragment()
                R.id.drawer_mortalidad -> MortalidadFragment()
                R.id.drawer_gestion_insumos -> InventarioFragment()
                R.id.drawer_contabilidad -> ContabilidadFragment()
                else -> null
            }
            
            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, it)
                    .commit()
                bottomNavigation.menu.setGroupCheckable(0, false, true)
            }
        }
    }

    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun setupDrawer() {
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        
        // Desactivar el tinte para todos los ítems para preservar el color de los PNGs
        navigationView.itemIconTintList = null

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
                R.id.drawer_gestion_insumos   -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, InventarioFragment()).commit()
                    bottomNavigation.menu.setGroupCheckable(0, false, true)
                }
                R.id.drawer_contabilidad      -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ContabilidadFragment()).commit()
                    bottomNavigation.menu.setGroupCheckable(0, false, true)
                }
                R.id.drawer_perfil            -> {
                    startActivity(Intent(this, PerfilUsuarioActivity::class.java))
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
