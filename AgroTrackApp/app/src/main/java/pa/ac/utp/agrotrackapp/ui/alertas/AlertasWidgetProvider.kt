package pa.ac.utp.agrotrackapp.ui.alertas

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.alertas.SqliteAlertaRepository
import pa.ac.utp.agrotrackapp.ui.main.MainActivity

class AlertasWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val repo = SqliteAlertaRepository(context)
            val count = repo.getAlertas(includeDismissed = false).size

            val views = RemoteViews(context.packageName, R.layout.widget_alertas)
            views.setTextViewText(R.id.widget_count, count.toString())
            
            // Set up the intent that starts the AlertasWidgetService, which will
            // provide the views for this collection.
            val intent = Intent(context, AlertasWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list, intent)

            // The empty view is displayed when the collection has no items. 
            // It should be a sibling of the collection view.
            views.setEmptyView(R.id.widget_list, R.id.widget_empty)

            // Pending intent template for list items
            val clickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val clickPendingIntent = PendingIntent.getActivity(
                context, 0, clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, AlertasWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            
            // Notify the ListView to refresh
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
