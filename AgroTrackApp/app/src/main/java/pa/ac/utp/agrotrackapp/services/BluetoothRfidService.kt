package pa.ac.utp.agrotrackapp.services

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Servicio encargado de gestionar la comunicación Bluetooth SPP (Serial Port Profile)
 * con lectores de aretes RFID (bastones).
 */
class BluetoothRfidService(
    private val context: Context,
    private val callback: BluetoothRfidCallback
) {

    interface BluetoothRfidCallback {
        fun onConnected(deviceName: String)
        fun onDisconnected()
        fun onTagRead(tag: String)
        fun onError(message: String)
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null

    // UUID estándar para perfil de puerto serie SPP
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Handler para ejecutar callbacks en el hilo principal
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Comprueba si el dispositivo soporta Bluetooth.
     */
    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null
    }

    /**
     * Comprueba si Bluetooth está habilitado.
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Devuelve la lista de permisos necesarios según la versión de Android.
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    /**
     * Comprueba si se tienen los permisos concedidos.
     */
    fun hasPermissions(): Boolean {
        for (permission in getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /**
     * Obtiene los dispositivos vinculados/pareados que son compatibles.
     */
    fun getPairedDevices(): List<BluetoothDevice> {
        if (!hasPermissions()) return emptyList()
        return bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
    }

    /**
     * Inicia la conexión con el dispositivo Bluetooth seleccionado.
     */
    @Synchronized
    fun connect(device: BluetoothDevice) {
        // Cancelar cualquier hilo intentando conectar
        disconnect()

        // Iniciar hilo para conectar con el dispositivo
        connectThread = ConnectThread(device)
        connectThread?.start()
    }

    /**
     * Cierra todas las conexiones activas.
     */
    @Synchronized
    fun disconnect() {
        connectThread?.cancel()
        connectThread = null

        connectedThread?.cancel()
        connectedThread = null

        mainHandler.post { callback.onDisconnected() }
    }

    /**
     * Hilo encargado de realizar la conexión saliente en segundo plano.
     */
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null

        init {
            try {
                if (hasPermissions()) {
                    socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                }
            } catch (e: IOException) {
                mainHandler.post { callback.onError("Error al crear el socket: ${e.message}") }
            }
        }

        override fun run() {
            // Cancelar el escaneo si está activo para acelerar la conexión
            if (hasPermissions()) {
                try {
                    bluetoothAdapter?.cancelDiscovery()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            }

            try {
                socket?.connect()
            } catch (connectException: IOException) {
                try {
                    socket?.close()
                } catch (closeException: IOException) {
                    closeException.printStackTrace()
                }
                mainHandler.post { callback.onError("No se pudo establecer conexión con el bastón RFID") }
                return
            }

            // Conexión exitosa, iniciar el hilo de comunicación
            synchronized(this@BluetoothRfidService) {
                connectThread = null
            }
            socket?.let { startConnected(it, device) }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Inicializa la comunicación sobre un socket conectado.
     */
    @Synchronized
    private fun startConnected(socket: BluetoothSocket, device: BluetoothDevice) {
        connectedThread = ConnectedThread(socket)
        connectedThread?.start()
        
        val deviceName = if (hasPermissions()) {
            device.name ?: "Dispositivo desconocido"
        } else {
            "Lector RFID"
        }
        mainHandler.post { callback.onConnected(deviceName) }
    }

    /**
     * Hilo encargado de leer los datos de entrada del socket conectado.
     */
    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream? = socket.inputStream
        private var isRunning = true

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int
            val stringBuilder = StringBuilder()

            while (isRunning) {
                try {
                    inputStream?.let { stream ->
                        bytes = stream.read(buffer)
                        val readMessage = String(buffer, 0, bytes)
                        stringBuilder.append(readMessage)

                        // Procesar líneas completas recibidas
                        var index: Int
                        while (stringBuilder.indexOf("\n").also { index = it } >= 0) {
                            val line = stringBuilder.substring(0, index).trim()
                            stringBuilder.delete(0, index + 1)
                            
                            // Limpiar y extraer solo números del tag RFID
                            val cleanTag = line.replace(Regex("[^0-9]"), "")
                            if (cleanTag.isNotEmpty() && cleanTag.length >= 15) {
                                mainHandler.post {
                                    callback.onTagRead(cleanTag)
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    if (isRunning) {
                        mainHandler.post { callback.onError("Se perdió la conexión con el bastón RFID") }
                        disconnect()
                    }
                    break
                }
            }
        }

        fun cancel() {
            isRunning = false
            try {
                socket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
