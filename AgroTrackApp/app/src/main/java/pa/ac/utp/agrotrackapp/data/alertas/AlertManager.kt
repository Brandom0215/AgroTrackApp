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
import pa.ac.utp.agrotrackapp.data.sanidad.SqliteSanidadRepository
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
    private val sanidadRepo = SqliteSanidadRepository(context)
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

        // 4. Sanidad Pendiente
        val sdfDateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStrOnlyDate = sdfDateOnly.format(todayDate)
        val todayDateMidnight = sdfDateOnly.parse(todayStrOnlyDate) ?: todayDate
        
        val proximosSanidad = sanidadRepo.getProximos()
        for (registro in proximosSanidad) {
            val refId = "sanidad_${registro.id}"
            var sanidadPendiente = false
            var isUrgente = false
            var diasRestantes = 0L

            try {
                val nextDate = sdfDateOnly.parse(registro.proximaDosis)
                if (nextDate != null) {
                    val diff = nextDate.time - todayDateMidnight.time
                    diasRestantes = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                    sanidadPendiente = true
                    isUrgente = diasRestantes <= 0
                }
            } catch (e: Exception) {}

            processCondition(
                refId = refId,
                isMet = sanidadPendiente,
                createAlerta = {
                    val isAtrasada = diasRestantes < 0
                    val isHoy      = diasRestantes == 0L
                    val diasAtraso = Math.abs(diasRestantes)

                    val titulo = when {
                        isAtrasada -> "⚠️ ${registro.categoria} ATRASADA: ${registro.identificador}"
                        isHoy      -> "📅 ${registro.categoria} para HOY: ${registro.identificador}"
                        else       -> "${registro.categoria} Pendiente: ${registro.identificador}"
                    }

                    val desc = when {
                        isAtrasada && diasAtraso == 1L ->
                            "Debía aplicarse ${registro.detalle} AYER (${registro.proximaDosis}). Por favor aplícala cuanto antes."
                        isAtrasada ->
                            "Debía aplicarse ${registro.detalle} hace $diasAtraso días (${registro.proximaDosis}). Está atrasada."
                        isHoy ->
                            "La aplicación de ${registro.detalle} está programada para HOY (${registro.proximaDosis})."
                        diasRestantes == 1L ->
                            "La aplicación de ${registro.detalle} es MAÑANA (${registro.proximaDosis})."
                        diasRestantes <= 5 ->
                            "Faltan $diasRestantes días para aplicar ${registro.detalle} (${registro.proximaDosis})."
                        diasRestantes <= 14 ->
                            "En $diasRestantes días: aplicación de ${registro.detalle} (${registro.proximaDosis})."
                        else ->
                            "Aplicación de ${registro.detalle} programada para ${registro.proximaDosis}."
                    }

                    val prioridad = when {
                        isAtrasada         -> PrioridadAlerta.ALTA
                        isHoy              -> PrioridadAlerta.ALTA
                        diasRestantes <= 5 -> PrioridadAlerta.MEDIA
                        else               -> PrioridadAlerta.BAJA
                    }

                    Alerta(
                        id = UUID.randomUUID().toString(),
                        titulo = titulo,
                        descripcion = desc,
                        tipo = TipoAlerta.SANIDAD_PENDIENTE,
                        fecha = todayStr,
                        prioridad = prioridad,
                        destinationId = R.id.drawer_control_sanitario,
                        referenceId = refId,
                        fechaProgramada = registro.proximaDosis
                    )
                }
            )
        }

        // Cleanup: dismiss alerts for sanidad records that are no longer pending (were applied or deleted)
        val activeSanidadAlerts = alertaRepo.getAlertas(includeDismissed = false).filter { it.tipo == TipoAlerta.SANIDAD_PENDIENTE }
        val proximosIds = proximosSanidad.map { "sanidad_${it.id}" }
        for (alerta in activeSanidadAlerts) {
            val ref = alerta.referenceId
            if (ref != null && !proximosIds.contains(ref)) {
                processCondition(
                    refId = ref,
                    isMet = false,
                    createAlerta = { alerta }
                )
            }
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
