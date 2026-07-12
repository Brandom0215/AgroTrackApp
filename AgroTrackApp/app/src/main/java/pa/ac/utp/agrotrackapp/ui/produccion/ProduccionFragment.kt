package pa.ac.utp.agrotrackapp.ui.produccion

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.produccion.SqliteProduccionRepository
import pa.ac.utp.agrotrackapp.domain.repository.ProduccionRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity

class ProduccionFragment : Fragment(R.layout.fragment_produccion) {

    private lateinit var produccionRepository: ProduccionRepository
    private lateinit var tvKpiCarneCount: TextView
    private lateinit var tvKpiLecheCount: TextView
    private lateinit var lineChart: LineChart

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializamos repositorio de producción
        produccionRepository = SqliteProduccionRepository(requireContext())

        // Vincular vistas
        tvKpiCarneCount = view.findViewById(R.id.tvKpiCarneCount)
        tvKpiLecheCount = view.findViewById(R.id.tvKpiLecheCount)
        lineChart = view.findViewById(R.id.ivTrendChart)

        // Botón Menú Lateral
        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        // Navegación a Módulo Carne
        view.findViewById<MaterialCardView>(R.id.btnModuleCarne).setOnClickListener {
            val intent = Intent(requireContext(), CarneProduccionActivity::class.java)
            startActivity(intent)
        }

        // Navegación a Módulo Leche
        view.findViewById<MaterialCardView>(R.id.btnModuleLeche).setOnClickListener {
            val intent = Intent(requireContext(), LecheProduccionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Cargar y actualizar KPIs dinámicos al volver a la pantalla
        actualizarKpis()
    }

    private fun actualizarKpis() {
        val carneCount = produccionRepository.getCarneRecords().count { it.activo }
        val lecheCount = produccionRepository.getLecheRecords().count { it.activo }

        tvKpiCarneCount.text = if (carneCount == 1) "1 Animal" else "$carneCount Animales"
        tvKpiLecheCount.text = if (lecheCount == 1) "1 Vaca" else "$lecheCount Vacas"

        val recordsLeche = produccionRepository.getLecheRecords().filter { it.activo }.sortedBy { it.fechaRegistro }
        val entries = ArrayList<Entry>()
        for ((index, record) in recordsLeche.withIndex()) {
            entries.add(Entry(index.toFloat(), record.litros.toFloat()))
        }
        
        lineChart.setNoDataText("Sin datos")
        lineChart.setNoDataTextColor(Color.parseColor("#999999"))

        if (entries.isNotEmpty()) {
            val dataSet = LineDataSet(entries, "Litros de Leche")
            dataSet.color = Color.parseColor("#4CAF50")
            dataSet.valueTextColor = Color.BLACK
            dataSet.lineWidth = 2f
            dataSet.circleRadius = 4f
            dataSet.setCircleColor(Color.parseColor("#388E3C"))
            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

            lineChart.data = LineData(dataSet)
            lineChart.description.isEnabled = false
            lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            lineChart.xAxis.setDrawGridLines(false)
            lineChart.axisRight.isEnabled = false
            lineChart.animateX(1000)
            lineChart.invalidate()
        } else {
            lineChart.clear()
        }
    }
}
