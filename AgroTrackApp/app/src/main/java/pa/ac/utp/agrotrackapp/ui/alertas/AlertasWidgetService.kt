package pa.ac.utp.agrotrackapp.ui.alertas

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.alertas.SqliteAlertaRepository
import pa.ac.utp.agrotrackapp.domain.model.Alerta
import pa.ac.utp.agrotrackapp.domain.model.PrioridadAlerta

class AlertasWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return AlertasRemoteViewsFactory(this.applicationContext)
    }
}

class AlertasRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var alerts: List<Alerta> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val repo = SqliteAlertaRepository(context)
        alerts = repo.getAlertas(includeDismissed = false).sortedBy { it.prioridad.ordinal }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = alerts.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= alerts.size) return RemoteViews(context.packageName, R.layout.widget_alerta_item)
        
        val alerta = alerts[position]
        val views = RemoteViews(context.packageName, R.layout.widget_alerta_item)
        
        views.setTextViewText(R.id.widget_item_title, alerta.titulo)
        views.setTextViewText(R.id.widget_item_desc, alerta.descripcion)
        
        val priorityColor = when (alerta.prioridad) {
            PrioridadAlerta.ALTA -> Color.parseColor("#BA1A1A")
            PrioridadAlerta.MEDIA -> Color.parseColor("#ED8936")
            PrioridadAlerta.BAJA -> Color.parseColor("#2E7D32")
        }
        views.setInt(R.id.widget_item_priority, "setBackgroundColor", priorityColor)

        // Fill-in intent for clicks
        val fillInIntent = Intent().apply {
            putExtra("OPEN_ALERTAS", true)
        }
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)
        
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
