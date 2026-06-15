package pa.ac.utp.agrotrackapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        setupDrawer()
        setupBottomNavigation()
        setupBackPress()

        if (savedInstanceState == null) {
            bottomNavigation.selectedItemId = R.id.nav_finca
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
        bottomNavigation.selectedItemId = itemId
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
