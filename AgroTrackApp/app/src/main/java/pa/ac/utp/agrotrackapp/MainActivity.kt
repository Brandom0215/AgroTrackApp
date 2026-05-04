package pa.ac.utp.agrotrackapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.ImageButton
import androidx.activity.addCallback

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupDrawer()
        setupBackPress()
    }

    // Drawer
    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout)

        // Botón hamburguesa que abre el drawer
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Items del drawer
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.drawer_inicio            -> { /* Pantalla de inicio (menú principal) */ }
                R.id.drawer_gestion_ganado    -> { /* startActivity(Intent(this, GestionGanadoActivity::class.java)) */ }
                R.id.drawer_control_sanitario -> { /* startActivity(Intent(this, ControlSanitarioActivity::class.java)) */ }
                R.id.drawer_produccion        -> { /* startActivity(Intent(this, ProduccionActivity::class.java)) */ }
                R.id.drawer_pesaje            -> { /* startActivity(Intent(this, PesajeActivity::class.java)) */ }
                R.id.drawer_mortalidad        -> { /* startActivity(Intent(this, MortalidadActivity::class.java)) */ }
                R.id.drawer_gestion_insumos   -> { /* startActivity(Intent(this, GestionInsumosActivity::class.java)) */ }
                R.id.drawer_contabilidad      -> { /* startActivity(Intent(this, ContabilidadActivity::class.java)) */ }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    // Para que el boton atras, cierre el drawer si esta abierto
    private fun setupBackPress() {
        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                finish()
            }
        }
    }
}