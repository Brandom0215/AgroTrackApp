package pa.ac.utp.agrotrackapp.data.alertas

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SqliteAnimalRepository
import pa.ac.utp.agrotrackapp.data.inventario.SqliteInventarioRepository
import pa.ac.utp.agrotrackapp.data.mortalidad.SqliteMortalidadRepository
import pa.ac.utp.agrotrackapp.data.produccion.SqliteProduccionRepository
import pa.ac.utp.agrotrackapp.domain.model.Alerta
import pa.ac.utp.agrotrackapp.domain.model.PrioridadAlerta
import pa.ac.utp.agrotrackapp.domain.model.TipoAlerta
import pa.ac.utp.agrotrackapp.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AlertManager(private val context: Context) {

    private val alertaRepo = SqliteAlertaRepository(context)
    private val inventarioRepo = SqliteInventarioRepository(context)
    private val animalRepo = SqliteAnimalRepository(context)
    private val mortalidadRepo = SqliteMortalidadRepository(context)
    private val produccionRepo = SqliteProduccionRepository(context)
    private val prefs = context.getSharedPreferences("AlertManagerState", Context.MODE_PRIVATE)

    companion object {
        const val CHANNEL_ID = "agrotrack_alerts_channel"
    }

    fun checkAlerts() {
        val authPrefs = context.getSharedPreferences("GanaDEXAuthPrefs", Context.MODE_PRIVATE)
        val alertsEnabled = authPrefs.getBoolean("alerts_enabled", true)
        if (!alertsEnabled) {
            return
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val todayDate = Date()
        val todayStr = sdf.format(todayDate)

        // 1. Stock Minimum
        val items = inventarioRepo.getItems()
        for (item in items) {
            val limite = item.limiteNotificacion
            if (limite != null) {
                val refId = "stock_${item.id}"
                val isMet = item.stock <= limite
                
                processCondition(
                    refId = refId,
                    isMet = isMet,
                    createAlerta = {
                        val isAgotado = item.stock <= 0
                        Alerta(
                            id = UUID.randomUUID().toString(),
                            titulo = if (isAgotado) "Insumo Agotado: ${item.nombre}" else "Stock Bajo: ${item.nombre}",
                            descripcion = if (isAgotado) "Ya no queda de este insumo." else "Quedan solo ${item.stock} ${item.unidad}.",
                            tipo = TipoAlerta.STOCK_MINIMO,
                            fecha = todayStr,
                            prioridad = if (isAgotado) PrioridadAlerta.ALTA else if (item.stock <= limite / 2) PrioridadAlerta.ALTA else PrioridadAlerta.MEDIA,
                            destinationId = R.id.drawer_gestion_insumos,
                            referenceId = refId
                        )
                    }
                )
            }
        }

        // 2. Mortalidad Alta
        val totalVivos = animalRepo.getAnimals().size
        val muertes = mortalidadRepo.getMortalidadRecords().size
        val refMortalidadId = "mortalidad_alta"
        var mortalidadAlta = false
        var tasaStr = "0.0"

        if (totalVivos + muertes > 0) {
            val tasa = (muertes.toDouble() / (totalVivos + muertes)) * 100
            mortalidadAlta = tasa > 5.0
            tasaStr = String.format(Locale.getDefault(), "%.1f", tasa)
        }

        processCondition(
            refId = refMortalidadId,
            isMet = mortalidadAlta,
            createAlerta = {
                Alerta(
                    id = UUID.randomUUID().toString(),
                    titulo = "Tasa de Mortalidad Elevada",
                    descripcion = "La tasa es de $tasaStr%.",
                    tipo = TipoAlerta.MORTALIDAD_ALTA,
                    fecha = todayStr,
                    prioridad = PrioridadAlerta.ALTA,
                    destinationId = R.id.drawer_mortalidad,
                    referenceId = refMortalidadId
                )
            }
        )

        // 3. Pesaje Pendiente
        val carneRecords = produccionRepo.getCarneRecords()
        for (record in carneRecords) {
            val refId = "pesaje_${record.numeroAnimal}"
            var pesajePendiente = false
            var daysDiff = 0L

            try {
                val lastDate = sdf.parse(record.fechaPesajeActual)
                if (lastDate != null) {
                    val diff = todayDate.time - lastDate.time
                    daysDiff = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                    pesajePendiente = daysDiff > 30
                }
            } catch (e: Exception) {}

            processCondition(
                refId = refId,
                isMet = pesajePendiente,
                createAlerta = {
                    Alerta(
                        id = UUID.randomUUID().toString(),
                        titulo = "Pesaje Pendiente: ${record.numeroAnimal}",
                        descripcion = "Lleva $daysDiff días sin ser pesado.",
                        tipo = TipoAlerta.PESAJE_PENDIENTE,
                        fecha = todayStr,
                        prioridad = if (daysDiff > 45) PrioridadAlerta.ALTA else PrioridadAlerta.BAJA,
                        destinationId = R.id.drawer_pesaje,
                        referenceId = refId
                    )
                }
            )
        }
    }

    private fun processCondition(refId: String, isMet: Boolean, createAlerta: () -> Alerta) {
        val lastMet = prefs.getBoolean("cond_$refId", false)

        if (isMet && !lastMet) {
            // Generar nueva alerta
            val alerta = createAlerta()
            alertaRepo.saveAlerta(alerta)
            sendSystemNotification(alerta)
            prefs.edit().putBoolean("cond_$refId", true).apply()
        } else if (isMet && lastMet) {
            // Condición sigue presente, actualizamos valores si cambiaron
            val activeAlerts = alertaRepo.getAlertas(includeDismissed = false).filter { it.referenceId == refId }
            val newAlertaData = createAlerta()
            
            if (activeAlerts.isNotEmpty()) {
                activeAlerts.forEach { existing ->
                    if (existing.titulo != newAlertaData.titulo || 
                        existing.descripcion != newAlertaData.descripcion || 
                        existing.prioridad != newAlertaData.prioridad ||
                        existing.destinationId != newAlertaData.destinationId) {
                        
                        val updatedAlerta = existing.copy(
                            titulo = newAlertaData.titulo,
                            descripcion = newAlertaData.descripcion,
                            prioridad = newAlertaData.prioridad,
                            destinationId = newAlertaData.destinationId
                        )
                        alertaRepo.saveAlerta(updatedAlerta)
                        sendSystemNotification(updatedAlerta)
                    }
                }
            } else {
                val historyAlerts = alertaRepo.getAlertas(includeDismissed = true).filter { it.referenceId == refId }
                val latestHistoryAlert = historyAlerts.firstOrNull()
                
                val changed = latestHistoryAlert == null || 
                    latestHistoryAlert.titulo != newAlertaData.titulo || 
                    latestHistoryAlert.descripcion != newAlertaData.descripcion || 
                    latestHistoryAlert.prioridad != newAlertaData.prioridad
                
                if (changed) {
                    alertaRepo.saveAlerta(newAlertaData)
                    sendSystemNotification(newAlertaData)
                }
            }
        } else if (!isMet && lastMet) {
            // Condición resuelta, eliminar posibles alertas activas
            val allAlerts = alertaRepo.getAlertas(includeDismissed = false)
            allAlerts.filter { it.referenceId == refId }.forEach {
                alertaRepo.dismissAlerta(it.id)
            }
            prefs.edit().putBoolean("cond_$refId", false).apply()
        }
    }

    private fun sendSystemNotification(alerta: Alerta) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificaciones de AgroTrack"
            val descriptionText = "Alertas de stock, mortalidad y tareas pendientes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("OPEN_ALERTAS", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            alerta.id.hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alertas) // Asegúrate de que este drawable sea válido
            .setContentTitle(alerta.titulo)
            .setContentText(alerta.descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(alerta.id.hashCode(), notification)
    }
}
