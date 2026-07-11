package pa.ac.utp.agrotrackapp.data.alertas

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class AlertWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val alertManager = AlertManager(context)
        alertManager.checkAlerts()
        return Result.success()
    }
}
