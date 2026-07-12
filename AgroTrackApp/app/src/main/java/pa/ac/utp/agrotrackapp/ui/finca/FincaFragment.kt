package pa.ac.utp.agrotrackapp.ui.finca

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import android.graphics.Color
import com.google.android.material.card.MaterialCardView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.repository.AuthRepository
import pa.ac.utp.agrotrackapp.data.auth.SqliteAuthRepository
import pa.ac.utp.agrotrackapp.data.animal.SqliteAnimalRepository
import pa.ac.utp.agrotrackapp.data.produccion.SqliteProduccionRepository
import pa.ac.utp.agrotrackapp.data.mortalidad.SqliteMortalidadRepository
import pa.ac.utp.agrotrackapp.data.inventario.SqliteInventarioRepository
import pa.ac.utp.agrotrackapp.data.alertas.SqliteAlertaRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import pa.ac.utp.agrotrackapp.ui.animales.CrearAnimalActivity

class FincaFragment : Fragment(R.layout.fragment_finca) {

    private lateinit var authRepository: AuthRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializamos el repositorio de datos de sesión
        authRepository = SqliteAuthRepository(requireContext())
        
        // Cargamos los datos reales del usuario logueado
        val tvFincaNombre = view.findViewById<TextView>(R.id.tvFincaNombreHome)
        val tvUsuarioActivo = view.findViewById<TextView>(R.id.tvUsuarioActivoHome)
        
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            tvFincaNombre?.text = "Finca: ${currentUser.nombreFinca}"
            tvUsuarioActivo?.text = "Productor: ${currentUser.nombre} ${currentUser.apellido}"
        }
        
        // Cargar Estadísticas Dinámicas desde SQLite
        cargarEstadisticasDinamicas(view)
        
        // Configurar Drawer Button
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // Configurar tarjeta de producción para navegar a la pestaña de producción
        view.findViewById<MaterialCardView>(R.id.cardProduccion)?.setOnClickListener {
            (requireActivity() as MainActivity).navigateToTab(R.id.nav_produccion)
        }


    }

    private fun cargarEstadisticasDinamicas(view: View) {
        val context = requireContext()
        val animalRepo = SqliteAnimalRepository(context)
        val prodRepo = SqliteProduccionRepository(context)
        val mortRepo = SqliteMortalidadRepository(context)
        val invRepo = SqliteInventarioRepository(context)
        val alertRepo = SqliteAlertaRepository(context)

        // 1. Resumen de Hato
        val animals = animalRepo.getAnimals()
        val totalAnimals = animals.size
        val machos = animals.count { it.sexo.equals("Macho", ignoreCase = true) }
        val hembras = animals.count { it.sexo.equals("Hembra", ignoreCase = true) }

        view.findViewById<TextView>(R.id.tvActiveAnimalsCount)?.text = totalAnimals.toString()
        view.findViewById<TextView>(R.id.tvTorosCount)?.text = " Machos: $machos"
        view.findViewById<TextView>(R.id.tvVacasCount)?.text = " Hembras: $hembras"
        view.findViewById<TextView>(R.id.tvTernerosCount)?.text = " Total Registros: $totalAnimals"

        val pieChart = view.findViewById<PieChart>(R.id.donutChart)
        if (pieChart != null) {
            pieChart.setNoDataText("Sin datos")
            pieChart.setNoDataTextColor(Color.parseColor("#999999"))
            
            val entries = ArrayList<PieEntry>()
            if (machos > 0) entries.add(PieEntry(machos.toFloat(), "Machos"))
            if (hembras > 0) entries.add(PieEntry(hembras.toFloat(), "Hembras"))
            
            if (entries.isNotEmpty()) {
                val dataSet = PieDataSet(entries, "")
                dataSet.colors = listOf(Color.parseColor("#4CAF50"), Color.parseColor("#81C784"))
                dataSet.valueTextColor = Color.WHITE
                dataSet.valueTextSize = 12f
                
                pieChart.data = PieData(dataSet)
                pieChart.description.isEnabled = false
                pieChart.legend.isEnabled = false
                pieChart.isDrawHoleEnabled = true
                pieChart.setHoleColor(Color.TRANSPARENT)
                pieChart.animateY(1000)
                pieChart.invalidate()
            } else {
                pieChart.clear()
            }
        }

        // 2. Alertas y Cuarentena
        val activeAlerts = alertRepo.getAlertas().filter { !it.isDismissed }
        val alertCount = activeAlerts.size
        val tvAlerts = view.findViewById<TextView>(R.id.tvAlertsCountHome)
        if (alertCount > 0) {
            tvAlerts?.text = "$alertCount alertas pendientes hoy"
        } else {
            tvAlerts?.text = "Sin alertas pendientes hoy"
        }

        // 3. Rendimiento y Producción
        val lecheRecords = prodRepo.getLecheRecords().filter { it.activo }
        val totalLeche = lecheRecords.sumOf { it.litros }
        view.findViewById<TextView>(R.id.tvProduccionHome)?.text = String.format(java.util.Locale.US, "%.1f Ltrs", totalLeche)

        val carneRecords = prodRepo.getCarneRecords().filter { it.activo }
        val avgGdp = if (carneRecords.isNotEmpty()) carneRecords.map { it.gdp }.average() else 0.0
        view.findViewById<TextView>(R.id.tvGdpHome)?.text = String.format(java.util.Locale.US, " %.2f kg/día", avgGdp)

        // 4. Mortalidad
        val mortRecords = mortRepo.getMortalidadRecords()
        val mortCount = mortRecords.size
        val mortRate = if (totalAnimals > 0) (mortCount.toFloat() / (totalAnimals + mortCount) * 100) else 0.0
        view.findViewById<TextView>(R.id.tvMortalidadHome)?.text = String.format(java.util.Locale.US, "Tasa de mortalidad: %.1f%% 📉", mortRate)

        // 5. Inventario Crítico
        val invItems = invRepo.getItems()
        val criticalItems = invItems.filter { it.limiteNotificacion != null && it.stock <= it.limiteNotificacion }
        
        val tvCritical1 = view.findViewById<TextView>(R.id.tvCriticalInv1)
        val tvCritical2 = view.findViewById<TextView>(R.id.tvCriticalInv2)
        val cardCritical1 = view.findViewById<View>(R.id.cardCriticalInv1)
        val cardCritical2 = view.findViewById<View>(R.id.cardCriticalInv2)

        if (criticalItems.isNotEmpty()) {
            val item1 = criticalItems[0]
            tvCritical1?.text = "${item1.nombre}: Solo quedan ${item1.stock} ${item1.unidad}"
            cardCritical1?.visibility = View.VISIBLE

            if (criticalItems.size > 1) {
                val item2 = criticalItems[1]
                tvCritical2?.text = "${item2.nombre}: Stock bajo (${item2.stock} ${item2.unidad})"
                cardCritical2?.visibility = View.VISIBLE
            } else {
                cardCritical2?.visibility = View.GONE
            }
        } else {
            tvCritical1?.text = "Inventario en niveles óptimos"
            cardCritical2?.visibility = View.GONE
        }

        // 6. Resumen Financiero (Cálculo dinámico simple basado en costos de inventario y leche producida)
        val totalCostoInventario = invItems.sumOf { it.stock * it.costo }
        val ingresosEstimados = totalLeche * 0.60
        val gastosEstimados = totalCostoInventario
        val balance = ingresosEstimados - gastosEstimados

        view.findViewById<TextView>(R.id.tvIngresosHome)?.text = String.format(java.util.Locale.US, "$%.2f", ingresosEstimados)
        view.findViewById<TextView>(R.id.tvGastosHome)?.text = String.format(java.util.Locale.US, "$%.2f", gastosEstimados)
        view.findViewById<TextView>(R.id.tvBalanceHome)?.text = String.format(java.util.Locale.US, "Balance neto: $%.2f", balance)

        val gastadoPercent = if (ingresosEstimados > 0) ((gastosEstimados / ingresosEstimados) * 100).toInt().coerceIn(0, 100) else 0
        view.findViewById<TextView>(R.id.tvGastadoPercentHome)?.text = "$gastadoPercent% Gastado"
        view.findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.progressBalanceHome)?.progress = gastadoPercent
    }
}
