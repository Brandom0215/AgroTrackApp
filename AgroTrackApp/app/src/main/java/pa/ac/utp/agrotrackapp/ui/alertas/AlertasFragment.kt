package pa.ac.utp.agrotrackapp.ui.alertas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.alertas.AlertManager
import pa.ac.utp.agrotrackapp.data.alertas.SqliteAlertaRepository
import pa.ac.utp.agrotrackapp.domain.repository.AlertaRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity

class AlertasFragment : Fragment(R.layout.fragment_alertas) {

    private lateinit var alertaRepository: AlertaRepository
    private lateinit var adapter: AlertasAdapter
    private lateinit var rvAlertas: RecyclerView
    private lateinit var tvEmpty: TextView
    private var isHistoryMode = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "No recibirás alertas en el panel de notificaciones", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        checkNotificationPermission()
        
        alertaRepository = SqliteAlertaRepository(requireContext())

        view.findViewById<ImageButton>(R.id.btnMenu)?.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        view.findViewById<ImageButton>(R.id.btnHistorial)?.setOnClickListener {
            isHistoryMode = !isHistoryMode
            adapter.isHistoryMode = isHistoryMode
            view.findViewById<TextView>(R.id.tvEmptyAlertas).text = if (isHistoryMode) "No hay historial de alertas" else "No hay alertas activas"
            cargarAlertas()
            Toast.makeText(requireContext(), if (isHistoryMode) "Viendo Historial" else "Viendo Activas", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.llVolverAlertas)?.setOnClickListener {
            isHistoryMode = false
            adapter.isHistoryMode = isHistoryMode
            view.findViewById<TextView>(R.id.tvEmptyAlertas).text = "No hay alertas activas"
            cargarAlertas()
        }

        rvAlertas = view.findViewById(R.id.rvAlertas)
        tvEmpty = view.findViewById(R.id.tvEmptyAlertas)

        adapter = AlertasAdapter { alerta ->
            alerta.destinationId?.let { tabId ->
                (requireActivity() as MainActivity).navigateToDestination(tabId)
            }
        }
        rvAlertas.layoutManager = LinearLayoutManager(requireContext())
        rvAlertas.adapter = adapter

        setupSwipeToDismiss()
        
        AlertManager(requireContext()).checkAlerts()
        
        cargarAlertas()
        
        setupAlertWorker()
    }

    private fun setupSwipeToDismiss() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                if (isHistoryMode) return 0
                return super.getMovementFlags(recyclerView, viewHolder)
            }

            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val alerta = adapter.currentList[position]

                if (!isHistoryMode) {
                    alertaRepository.dismissAlerta(alerta.id)
                    Snackbar.make(rvAlertas, "Alerta eliminada", Snackbar.LENGTH_LONG)
                        .setAction("DESHACER") {
                            alertaRepository.restoreAlerta(alerta.id)
                            AlertasWidgetProvider.updateAllWidgets(requireContext())
                            cargarAlertas()
                        }.show()
                    AlertasWidgetProvider.updateAllWidgets(requireContext())
                } else {
                    Toast.makeText(requireContext(), "El historial no se puede deslizar", Toast.LENGTH_SHORT).show()
                }
                cargarAlertas()
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvAlertas)
    }

    private fun setupAlertWorker() {
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<pa.ac.utp.agrotrackapp.data.alertas.AlertWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        ).build()
        androidx.work.WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "AlertCheckWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun cargarAlertas() {
        val llVolverAlertas = view?.findViewById<View>(R.id.llVolverAlertas)
        llVolverAlertas?.visibility = if (isHistoryMode) View.VISIBLE else View.GONE
        
        if (isHistoryMode) {
            val history = alertaRepository.getAlertas(true).filter { it.isDismissed }
            adapter.submitList(history) {
                adapter.notifyDataSetChanged()
            }
            tvEmpty.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
            rvAlertas.visibility = if (history.isEmpty()) View.GONE else View.VISIBLE
            return
        }

        val alertasActivas = alertaRepository.getAlertas(includeDismissed = false)

        if (alertasActivas.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvAlertas.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvAlertas.visibility = View.VISIBLE
            adapter.submitList(alertasActivas.sortedBy { it.prioridad.ordinal }) {
                adapter.notifyDataSetChanged()
            }
        }
    }
}
