package pa.ac.utp.agrotrackapp.ui.animales

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SqliteAnimalRepository
import pa.ac.utp.agrotrackapp.data.produccion.SqliteProduccionRepository
import pa.ac.utp.agrotrackapp.domain.model.Animal
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository
import pa.ac.utp.agrotrackapp.services.BluetoothRfidService
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla que gestiona tanto el registro de un nuevo animal como la edición de uno existente.
 * Sigue el patrón Repository para la persistencia de datos y soporta captura de fotos con validación de entradas.
 * Incorpora conexión Bluetooth REAL con bastón RFID inalámbrico y modos de registro Individual o Global (Lote).
 */
class CrearAnimalActivity : AppCompatActivity(), BluetoothRfidService.BluetoothRfidCallback {

    // Repositorio y banderas de estado para el flujo de edición y foto
    private lateinit var animalRepository: AnimalRepository
    private var isEditMode = false
    private var editArete: String? = null
    private var sexoSeleccionado = "Macho" // Valor por defecto
    private var imagenPathLocal: String? = null // Ruta de la foto capturada o elegida

    // Elementos de la Interfaz de Usuario (UI)
    private lateinit var tvTitleActivity: TextView
    private lateinit var cardMacho: MaterialCardView
    private lateinit var cardHembra: MaterialCardView
    private lateinit var ivAnimalPreview: ImageView
    
    // Contenedores TextInputLayout para mostrar errores visuales correctos
    private lateinit var tilNumeroAnimal: TextInputLayout
    private lateinit var tilTrazabilidad: TextInputLayout
    private lateinit var tilNumeroChip: TextInputLayout
    private lateinit var tilFechaNacimiento: TextInputLayout
    private lateinit var tilNotas: TextInputLayout
    
    private lateinit var etNumeroAnimal: TextInputEditText
    private lateinit var etTrazabilidad: TextInputEditText
    private lateinit var etNumeroChip: TextInputEditText
    private lateinit var etFechaNacimiento: TextInputEditText
    
    private lateinit var etRaza: AutoCompleteTextView
    private lateinit var etProposito: AutoCompleteTextView
    private lateinit var etManga: AutoCompleteTextView
    
    private lateinit var etPeso: TextInputEditText
    
    private lateinit var etPadre: AutoCompleteTextView
    private lateinit var etMadre: AutoCompleteTextView
    private lateinit var etNotas: TextInputEditText
    private lateinit var btnCrear: MaterialButton

    // Elementos para el escaneo Bluetooth y modos de registro
    private var registroModo = "Individual" // "Individual" o "Global"
    private val scannedTagsList = mutableListOf<String>()
    
    private lateinit var tvModoRegistroHeader: TextView
    private lateinit var layoutModoRegistroSelector: LinearLayout
    private lateinit var cardModoIndividual: MaterialCardView
    private lateinit var cardModoGlobal: MaterialCardView
    private lateinit var tvLabelIndividual: TextView
    private lateinit var tvLabelGlobal: TextView
    
    private lateinit var layoutGlobalTags: LinearLayout
    private lateinit var tvConexionBaston: TextView
    private lateinit var btnConectarBaston: MaterialButton
    private lateinit var tvContadorTags: TextView
    private lateinit var tvListaTags: TextView
    private lateinit var btnLimpiarTags: MaterialButton
    private lateinit var btnBluetooth: ImageButton
    
    // Servicio Bluetooth RFID real
    private lateinit var bluetoothRfidService: BluetoothRfidService
    private var isBastonConectado = false
    private var nombreBastonConectado: String? = null
    private var isModoDemo = false // Bandera para simular lecturas en pruebas locales

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1001
        private const val REQUEST_IMAGE_PICK = 1002
        private const val REQUEST_PERMISSIONS_CODE = 4002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_animal)

        // Inicializamos repositorio
        animalRepository = SqliteAnimalRepository(this)

        // Inicializar Servicio Bluetooth RFID Real
        bluetoothRfidService = BluetoothRfidService(this, this)

        // Bind Views de TextInputLayout
        tilNumeroAnimal = findViewById(R.id.tilNumeroAnimal)
        tilTrazabilidad = findViewById(R.id.tilTrazabilidad)
        tilNumeroChip = findViewById(R.id.tilNumeroChip)
        tilFechaNacimiento = findViewById(R.id.tilFechaNacimiento)
        tilNotas = findViewById(R.id.tilNotas)

        // Bind Views comunes
        tvTitleActivity = findViewById(R.id.tvTitleActivity)
        cardMacho = findViewById(R.id.cardMacho)
        cardHembra = findViewById(R.id.cardHembra)
        ivAnimalPreview = findViewById(R.id.ivAnimalPreview)
        
        etNumeroAnimal = findViewById(R.id.etNumeroAnimal)
        etTrazabilidad = findViewById(R.id.etTrazabilidad)
        etNumeroChip = findViewById(R.id.etNumeroChip)
        etFechaNacimiento = findViewById(R.id.etFechaNacimiento)
        
        etRaza = findViewById(R.id.etRaza)
        etProposito = findViewById(R.id.etProposito)
        etManga = findViewById(R.id.etManga)
        
        etPeso = findViewById(R.id.etPeso)
        
        etPadre = findViewById(R.id.etPadre)
        etMadre = findViewById(R.id.etMadre)
        etNotas = findViewById(R.id.etNotas)
        btnCrear = findViewById(R.id.btnCrear)

        // Bind nuevos elementos de Bluetooth y Registro
        tvModoRegistroHeader = findViewById(R.id.tvModoRegistroHeader)
        layoutModoRegistroSelector = findViewById(R.id.layoutModoRegistroSelector)
        cardModoIndividual = findViewById(R.id.cardModoIndividual)
        cardModoGlobal = findViewById(R.id.cardModoGlobal)
        tvLabelIndividual = findViewById(R.id.tvLabelIndividual)
        tvLabelGlobal = findViewById(R.id.tvLabelGlobal)
        
        layoutGlobalTags = findViewById(R.id.layoutGlobalTags)
        tvConexionBaston = findViewById(R.id.tvConexionBaston)
        btnConectarBaston = findViewById(R.id.btnConectarBaston)
        tvContadorTags = findViewById(R.id.tvContadorTags)
        tvListaTags = findViewById(R.id.tvListaTags)
        btnLimpiarTags = findViewById(R.id.btnLimpiarTags)
        btnBluetooth = findViewById(R.id.btnBluetooth)

        // Setup Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup Camera/Gallery picker on Card click
        findViewById<MaterialCardView>(R.id.btnCamera).setOnClickListener {
            mostrarOpcionesImagen()
        }

        // Setup Sex Toggles
        cardMacho.setOnClickListener { selectSex(true) }
        cardHembra.setOnClickListener { selectSex(false) }

        // Setup DatePicker Dialog
        setupDatePicker()

        // Setup Dropdowns con valores
        setupDropdowns()

        // Listeners para modos de registro (Individual / Global)
        cardModoIndividual.setOnClickListener { selectRegistroModo("Individual") }
        cardModoGlobal.setOnClickListener { selectRegistroModo("Global") }

        // Listeners de Bluetooth / Bastón RFID
        btnBluetooth.setOnClickListener { verificarYMostrarDispositivos() }
        btnConectarBaston.setOnClickListener { verificarYMostrarDispositivos() }

        btnLimpiarTags.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Limpiar Lista")
                .setMessage("¿Está seguro de que desea limpiar todos los aretes escaneados?")
                .setPositiveButton("Sí") { _, _ ->
                    scannedTagsList.clear()
                    actualizarVistaTagsGlobales()
                }
                .setNegativeButton("No", null)
                .show()
        }

        // Detectar si estamos en modo edición
        editArete = intent.getStringExtra("EXTRA_NUMERO_ANIMAL")
        isEditMode = editArete != null

        if (isEditMode) {
            // El modo de registro en lote no aplica para edición de un animal existente
            tvModoRegistroHeader.visibility = View.GONE
            layoutModoRegistroSelector.visibility = View.GONE
            cargarDatosParaEdicion()
        } else {
            selectRegistroModo("Individual")
        }

        // Setup Create/Save Button
        btnCrear.setOnClickListener {
            guardarAnimal()
        }

        // Solicitar permisos al iniciar
        if (!bluetoothRfidService.hasPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                bluetoothRfidService.getRequiredPermissions(),
                REQUEST_PERMISSIONS_CODE
            )
        }
    }

    /**
     * Alterna la interfaz y visibilidad de los campos dependiendo del modo seleccionado.
     */
    private fun selectRegistroModo(modo: String) {
        registroModo = modo
        val surfaceColor = getColor(com.google.android.material.R.color.m3_sys_color_light_surface)
        
        if (modo == "Individual") {
            // Activar Individual (Celeste)
            cardModoIndividual.setCardBackgroundColor(Color.parseColor("#4FC3F7"))
            cardModoIndividual.strokeWidth = 0
            tvLabelIndividual.setTextColor(Color.WHITE)
            
            // Desactivar Global
            cardModoGlobal.setCardBackgroundColor(surfaceColor)
            cardModoGlobal.strokeWidth = 2
            cardModoGlobal.strokeColor = Color.parseColor("#E0E0E0")
            tvLabelGlobal.setTextColor(Color.parseColor("#9E9E9E"))
            
            // Visibilidades de campos individuales
            tilNumeroAnimal.visibility = View.VISIBLE
            tilNumeroChip.visibility = View.VISIBLE
            layoutGlobalTags.visibility = View.GONE
            btnCrear.text = "Crear"
        } else {
            // Activar Global (Celeste)
            cardModoGlobal.setCardBackgroundColor(Color.parseColor("#4FC3F7"))
            cardModoGlobal.strokeWidth = 0
            tvLabelGlobal.setTextColor(Color.WHITE)
            
            // Desactivar Individual
            cardModoIndividual.setCardBackgroundColor(surfaceColor)
            cardModoIndividual.strokeWidth = 2
            cardModoIndividual.strokeColor = Color.parseColor("#E0E0E0")
            tvLabelIndividual.setTextColor(Color.parseColor("#9E9E9E"))
            
            // Visibilidades de campos de lote
            tilNumeroAnimal.visibility = View.GONE
            tilNumeroChip.visibility = View.GONE
            layoutGlobalTags.visibility = View.VISIBLE
            actualizarVistaTagsGlobales()
        }
    }

    /**
     * Actualiza el listado visual de tags leídos en modo Global.
     */
    private fun actualizarVistaTagsGlobales() {
        tvContadorTags.text = "Tags Escaneados: ${scannedTagsList.size}"
        if (scannedTagsList.isEmpty()) {
            tvListaTags.text = "Ningún tag escaneado aún. Conecte el bastón y realice escaneos."
            tvListaTags.setTextColor(Color.GRAY)
            btnLimpiarTags.visibility = View.GONE
            btnCrear.text = "Registrar Lote"
        } else {
            val builder = StringBuilder()
            scannedTagsList.forEachIndexed { index, tag ->
                builder.append("${index + 1}. RFID (Trazabilidad): $tag\n")
            }
            tvListaTags.text = builder.toString().trim()
            tvListaTags.setTextColor(Color.parseColor("#37474F"))
            btnLimpiarTags.visibility = View.VISIBLE
            btnCrear.text = "Registrar Lote (${scannedTagsList.size})"
        }
    }

    /**
     * Valida permisos y comprueba si Bluetooth está activo antes de mostrar los dispositivos.
     */
    private fun verificarYMostrarDispositivos() {
        if (!bluetoothRfidService.isBluetoothSupported()) {
            Toast.makeText(this, "El dispositivo no cuenta con soporte Bluetooth", Toast.LENGTH_LONG).show()
            return
        }

        if (!bluetoothRfidService.hasPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                bluetoothRfidService.getRequiredPermissions(),
                REQUEST_PERMISSIONS_CODE
            )
            return
        }

        if (!bluetoothRfidService.isBluetoothEnabled()) {
            Toast.makeText(this, "Por favor active el Bluetooth en los ajustes del dispositivo", Toast.LENGTH_LONG).show()
            return
        }

        mostrarDialogoBluetooth()
    }

    /**
     * Despliega la lista de dispositivos vinculados (pareados) de forma real utilizando un diseño premium.
     */
    private fun mostrarDialogoBluetooth() {
        if (isBastonConectado) {
            val opciones = mutableListOf<CharSequence>()
            if (isModoDemo) {
                opciones.add("Simular Lectura de Arete (Demo)")
            }
            opciones.add("Desconectar Bastón")
            opciones.add("Cancelar")

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Bastón RFID: $nombreBastonConectado")
                .setItems(opciones.toTypedArray()) { dialog, item ->
                    val seleccion = opciones[item].toString()
                    when (seleccion) {
                        "Simular Lectura de Arete (Demo)" -> {
                            simularLecturaTagDemo()
                        }
                        "Desconectar Bastón" -> {
                            if (isModoDemo) {
                                onDisconnected()
                            } else {
                                bluetoothRfidService.disconnect()
                            }
                        }
                        else -> dialog.dismiss()
                    }
                }
                .show()
        } else {
            val pairedDevices = bluetoothRfidService.getPairedDevices()
            
            // Inflar el diseño personalizado para el diálogo
            val dialogView = layoutInflater.inflate(R.layout.dialog_bluetooth_devices, null)
            val layoutDeviceListContainer = dialogView.findViewById<LinearLayout>(R.id.layoutDeviceListContainer)

            val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setNegativeButton("Cerrar", null)
                .create()

            // Función auxiliar para agregar filas de dispositivos de forma dinámica
            fun agregarDispositivoALista(nombre: String, mac: String, onClick: () -> Unit) {
                val rowView = layoutInflater.inflate(R.layout.item_bluetooth_device, layoutDeviceListContainer, false)
                val tvDeviceName = rowView.findViewById<TextView>(R.id.tvDeviceName)
                val tvDeviceAddress = rowView.findViewById<TextView>(R.id.tvDeviceAddress)
                val cardDeviceRow = rowView.findViewById<MaterialCardView>(R.id.cardDeviceRow)
                val ivDeviceIcon = rowView.findViewById<ImageView>(R.id.ivDeviceIcon)

                tvDeviceName.text = nombre
                tvDeviceAddress.text = mac
                
                // Darle una distinción visual especial si es la opción de simulación
                if (nombre.startsWith("[Simulador]")) {
                    ivDeviceIcon.setColorFilter(Color.parseColor("#9C27B0"))
                    ivDeviceIcon.alpha = 0.8f
                } else {
                    ivDeviceIcon.setColorFilter(Color.parseColor("#4FC3F7"))
                }

                cardDeviceRow.setOnClickListener {
                    onClick()
                    dialog.dismiss()
                }

                layoutDeviceListContainer.addView(rowView)
            }

            // 1. Agregar los dispositivos vinculados reales del usuario
            pairedDevices.forEach { device ->
                var name = "Dispositivo Desconocido"
                try {
                    name = device.name ?: "Lector RFID SPP"
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
                val address = device.address
                agregarDispositivoALista(name, address) {
                    isModoDemo = false
                    Toast.makeText(this, "Conectando al lector RFID...", Toast.LENGTH_SHORT).show()
                    bluetoothRfidService.connect(device)
                }
            }

            // 2. Agregar siempre el simulador virtual para pruebas locales sencillas
            agregarDispositivoALista(
                "[Simulador] Bastón RFID Virtual (Demo)",
                "DE:AD:BE:EF:00:FF"
            ) {
                isModoDemo = true
                onConnected("Bastón RFID Tru-Test SRS2 (Simulado)")
            }

            dialog.show()
        }
    }

    /**
     * Simulación local de lecturas de arete (sólo si se elige el dispositivo virtual).
     */
    private fun simularLecturaTagDemo() {
        val countryCode = "982" // Código ISO/Identificación para la finca
        val rest = "${(100000..999999).random()}${(100000..999999).random()}"
        val tagSimulado = "$countryCode$rest"
        onTagRead(tagSimulado)
    }

    // --- Callbacks de BluetoothRfidService.BluetoothRfidCallback ---

    override fun onConnected(deviceName: String) {
        isBastonConectado = true
        nombreBastonConectado = deviceName
        tvConexionBaston.text = "Bastón: Conectado ($deviceName)"
        tvConexionBaston.setTextColor(Color.parseColor("#2E7D32")) // Verde de éxito
        btnConectarBaston.text = "Desconectar"
        Toast.makeText(this, "Lector $deviceName conectado exitosamente", Toast.LENGTH_SHORT).show()
        
        if (registroModo == "Global") {
            actualizarVistaTagsGlobales()
        }
    }

    override fun onDisconnected() {
        isBastonConectado = false
        nombreBastonConectado = null
        isModoDemo = false
        tvConexionBaston.text = "Bastón RFID: Desconectado"
        tvConexionBaston.setTextColor(Color.GRAY)
        btnConectarBaston.text = "Conectar"
        Toast.makeText(this, "Lector RFID desconectado", Toast.LENGTH_SHORT).show()
    }

    override fun onError(message: String) {
        Toast.makeText(this, "Error Bluetooth: $message", Toast.LENGTH_LONG).show()
        onDisconnected()
    }

    /**
     * Recibe la lectura real o simulada del tag RFID desde el lector de bastón Bluetooth.
     * CRÍTICO: Registra la información en el campo de Trazabilidad como lo solicitó el usuario.
     */
    override fun onTagRead(tag: String) {
        if (registroModo == "Individual") {
            // Ingresa el tag escaneado en el campo de Trazabilidad
            etTrazabilidad.setText(tag)
            
            // Para comodidad del usuario, también autocompletamos el Arete para que la clave primaria esté lista
            etNumeroAnimal.setText(tag)
            
            Toast.makeText(this, "RFID escaneado en Trazabilidad y Arete: $tag", Toast.LENGTH_SHORT).show()
        } else {
            // En modo Global, lo acumulamos en la lista de aretes de lote
            if (scannedTagsList.contains(tag)) {
                Toast.makeText(this, "El arete $tag ya está en la lista del lote", Toast.LENGTH_SHORT).show()
            } else {
                scannedTagsList.add(tag)
                Toast.makeText(this, "Arete leído: $tag", Toast.LENGTH_SHORT).show()
                actualizarVistaTagsGlobales()
            }
        }
    }

    // --- Fin Callbacks de Bluetooth ---

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if (bluetoothRfidService.hasPermissions()) {
                Toast.makeText(this, "Permisos de Bluetooth concedidos", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Se requieren permisos para escanear y conectar con el lector RFID", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf<CharSequence>("Tomar Foto (Cámara)", "Seleccionar de Galería", "Cancelar")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Añadir foto del bovino")
        builder.setItems(opciones) { dialog, item ->
            when {
                opciones[item] == "Tomar Foto (Cámara)" -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    try {
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                    } catch (e: Exception) {
                        Toast.makeText(this, "No se pudo abrir la cámara: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                opciones[item] == "Seleccionar de Galería" -> {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    intent.type = "image/*"
                    startActivityForResult(intent, REQUEST_IMAGE_PICK)
                }
                else -> dialog.dismiss()
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            val tempFile = File(filesDir, "temp_animal_image.png")
            try {
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    if (bitmap != null) {
                        val saved = pa.ac.utp.agrotrackapp.utils.ImageResizer.compressAndSaveBitmap(bitmap, tempFile)
                        if (saved) {
                            imagenPathLocal = tempFile.absolutePath
                            ivAnimalPreview.visibility = View.VISIBLE
                            val optBitmap = pa.ac.utp.agrotrackapp.utils.ImageResizer.decodeSampledBitmapFromFile(tempFile.absolutePath, 400, 400)
                            ivAnimalPreview.setImageBitmap(optBitmap ?: bitmap)
                        } else {
                            Toast.makeText(this, "Error al optimizar imagen de la cámara", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "No se recibió imagen de la cámara", Toast.LENGTH_SHORT).show()
                    }
                } else if (requestCode == REQUEST_IMAGE_PICK) {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        val saved = pa.ac.utp.agrotrackapp.utils.ImageResizer.compressAndSaveImage(this, selectedImageUri, tempFile)
                        if (saved) {
                            imagenPathLocal = tempFile.absolutePath
                            ivAnimalPreview.visibility = View.VISIBLE
                            val optBitmap = pa.ac.utp.agrotrackapp.utils.ImageResizer.decodeSampledBitmapFromFile(tempFile.absolutePath, 400, 400)
                            ivAnimalPreview.setImageBitmap(optBitmap)
                        } else {
                            Toast.makeText(this, "Error al optimizar imagen de la galería", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al guardar foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectSex(isMacho: Boolean) {
        if (isMacho) {
            sexoSeleccionado = "Macho"
            cardMacho.setCardBackgroundColor(Color.parseColor("#4FC3F7"))
            cardMacho.strokeWidth = 0
            
            val surfaceColor = getColor(com.google.android.material.R.color.m3_sys_color_light_surface)
            cardHembra.setCardBackgroundColor(surfaceColor)
            cardHembra.strokeWidth = 2
            cardHembra.strokeColor = Color.parseColor("#E0E0E0")
        } else {
            sexoSeleccionado = "Hembra"
            cardHembra.setCardBackgroundColor(Color.parseColor("#E1BEE7"))
            cardHembra.strokeWidth = 0
            
            val surfaceColor = getColor(com.google.android.material.R.color.m3_sys_color_light_surface)
            cardMacho.setCardBackgroundColor(surfaceColor)
            cardMacho.strokeWidth = 2
            cardMacho.strokeColor = Color.parseColor("#E0E0E0")
        }
    }

    private fun setupDatePicker() {
        etFechaNacimiento.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            
            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val dateString = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                    etFechaNacimiento.setText(dateString)
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }

    private fun setupDropdowns() {
        val razaAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Brahman", "Gyr", "Holstein", "Angus", "Cebú", "Pardo Suizo"))
        etRaza.setAdapter(razaAdapter)

        val propositoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Doble propósito", "Carne", "Leche", "Cría"))
        etProposito.setAdapter(propositoAdapter)
        
        val mangaAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Lote 1", "Lote 2", "Corral Principal", "Cuarentena"))
        etManga.setAdapter(mangaAdapter)

        val padreMadreAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Desconocido", "Toro 001", "Vaca 102", "Inseminación Artificial"))
        etPadre.setAdapter(padreMadreAdapter)
        etMadre.setAdapter(padreMadreAdapter)
    }

    private fun cargarDatosParaEdicion() {
        val arete = editArete ?: return
        val animal = animalRepository.getAnimal(arete) ?: return

        tvTitleActivity.text = "Editar Animal"
        btnCrear.text = "Guardar Cambios"
        
        etNumeroAnimal.setText(animal.numeroAnimal)
        etNumeroAnimal.isEnabled = false

        selectSex(animal.sexo == "Macho")

        etTrazabilidad.setText(animal.trazabilidad)
        etNumeroChip.setText(animal.numeroChip)
        etFechaNacimiento.setText(animal.fechaNacimiento)
        
        etRaza.setText(animal.raza, false)
        etProposito.setText(animal.proposito, false)
        etManga.setText(animal.manga, false)
        
        etPeso.setText(animal.peso)
        
        etPadre.setText(animal.padre, false)
        etMadre.setText(animal.madre, false)
        etNotas.setText(animal.notas)

        if (animal.imagenPath.isNotEmpty()) {
            val file = File(animal.imagenPath)
            if (file.exists()) {
                imagenPathLocal = animal.imagenPath
                ivAnimalPreview.visibility = View.VISIBLE
                val bitmap = pa.ac.utp.agrotrackapp.utils.ImageResizer.decodeSampledBitmapFromFile(file.absolutePath, 400, 400)
                if (bitmap != null) {
                    ivAnimalPreview.setImageBitmap(bitmap)
                } else {
                    ivAnimalPreview.setImageURI(Uri.fromFile(file))
                }
            }
        }
    }

    /**
     * Valida la información del formulario y ejecuta el registro individual o global en lote.
     */
    private fun guardarAnimal() {
        val arete = etNumeroAnimal.text.toString().trim()
        val trazabilidad = etTrazabilidad.text.toString().trim()
        val chip = etNumeroChip.text.toString().trim()
        val raza = etRaza.text.toString().trim()
        val proposito = etProposito.text.toString().trim()
        val fechaNac = etFechaNacimiento.text.toString().trim()
        val peso = etPeso.text.toString().trim()
        val notas = etNotas.text.toString().trim()

        // Limpiar errores previos
        tilNumeroAnimal.error = null
        tilTrazabilidad.error = null
        tilNumeroChip.error = null
        tilFechaNacimiento.error = null
        tilNotas.error = null

        // 1. Validaciones en Modo Individual
        if (registroModo == "Individual") {
            if (arete.isEmpty()) {
                tilNumeroAnimal.error = "El número de arete es requerido"
                etNumeroAnimal.requestFocus()
                return
            }
            if (!arete.matches(Regex("^[0-9]{15,30}$"))) {
                tilNumeroAnimal.error = "El arete debe tener entre 15 y 30 números (sin letras ni caracteres especiales)"
                etNumeroAnimal.requestFocus()
                return
            }
            if (chip.isNotEmpty() && !chip.matches(Regex("^[0-9]{15,30}$"))) {
                tilNumeroChip.error = "El número de chip debe tener entre 15 y 30 números (sin letras ni caracteres especiales)"
                etNumeroChip.requestFocus()
                return
            }
        } else {
            // 2. Validaciones en Modo Global
            if (scannedTagsList.isEmpty()) {
                Toast.makeText(this, "Debe escanear al menos un arete usando el bastón RFID", Toast.LENGTH_LONG).show()
                return
            }
        }

        // Validaciones comunes compartidas
        if (trazabilidad.isNotEmpty() && !trazabilidad.matches(Regex("^[0-9]{15,30}$"))) {
            tilTrazabilidad.error = "La trazabilidad debe tener entre 15 y 30 números (sin letras ni caracteres especiales)"
            etTrazabilidad.requestFocus()
            return
        }

        if (raza.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione una raza", Toast.LENGTH_SHORT).show()
            return
        }
        if (proposito.isEmpty()) {
            Toast.makeText(this, "Por favor seleccione un propósito", Toast.LENGTH_SHORT).show()
            return
        }
        if (fechaNac.isEmpty()) {
            tilFechaNacimiento.error = "La fecha de nacimiento es requerida"
            etFechaNacimiento.requestFocus()
            return
        }

        if (notas.length > 100) {
            tilNotas.error = "Las observaciones deben tener como máximo 100 caracteres"
            etNotas.requestFocus()
            return
        }
        if (notas.isNotEmpty() && !notas.matches(Regex("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s:]*$"))) {
            tilNotas.error = "Solo letras, números y el carácter especial ':' son permitidos"
            etNotas.requestFocus()
            return
        }

        if (registroModo == "Individual") {
            // Lógica de guardado individual original
            var finalImagenPath = ""
            if (imagenPathLocal != null) {
                val tempFile = File(imagenPathLocal!!)
                if (tempFile.exists() && tempFile.name == "temp_animal_image.png") {
                    val permanentFile = File(filesDir, "animal_$arete.png")
                    try {
                        if (permanentFile.exists()) {
                            permanentFile.delete()
                        }
                        tempFile.renameTo(permanentFile)
                        finalImagenPath = permanentFile.absolutePath
                    } catch (e: Exception) {
                        e.printStackTrace()
                        finalImagenPath = tempFile.absolutePath
                    }
                } else {
                    finalImagenPath = imagenPathLocal!!
                }
            } else if (isEditMode) {
                val animalPrevio = animalRepository.getAnimal(arete)
                if (animalPrevio != null) {
                    finalImagenPath = animalPrevio.imagenPath
                }
            }

            val animal = Animal(
                numeroAnimal = arete,
                sexo = sexoSeleccionado,
                trazabilidad = trazabilidad,
                numeroChip = chip,
                fechaNacimiento = fechaNac,
                raza = raza,
                proposito = proposito,
                manga = etManga.text.toString().trim(),
                peso = peso,
                padre = etPadre.text.toString().trim(),
                madre = etMadre.text.toString().trim(),
                notas = notas,
                imagenPath = finalImagenPath
            )

            val resultado = if (isEditMode) {
                animalRepository.updateAnimal(animal)
            } else {
                animalRepository.saveAnimal(animal)
            }

            resultado.fold(
                onSuccess = {
                    val mensaje = if (isEditMode) "Datos actualizados correctamente" else "Animal registrado exitosamente"
                    Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                    evaluarYNotificarProduccion(animal)
                },
                onFailure = { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            )
        } else {
            // Lógica de registro global en lote
            var guardados = 0
            var duplicados = 0
            
            for (tag in scannedTagsList) {
                // Verificar duplicados locales en la base de datos
                if (animalRepository.getAnimal(tag) != null) {
                    duplicados++
                    continue
                }

                var finalImagenPath = ""
                if (imagenPathLocal != null) {
                    val tempFile = File(imagenPathLocal!!)
                    if (tempFile.exists()) {
                        val permanentFile = File(filesDir, "animal_$tag.png")
                        try {
                            tempFile.copyTo(permanentFile, overwrite = true)
                            finalImagenPath = permanentFile.absolutePath
                        } catch (e: Exception) {
                            e.printStackTrace()
                            finalImagenPath = tempFile.absolutePath
                        }
                    }
                }

                val animal = Animal(
                    numeroAnimal = tag,
                    sexo = sexoSeleccionado,
                    trazabilidad = tag, // Registra el tag escaneado en Trazabilidad para el lote
                    numeroChip = "", // En lote, el RFID es la trazabilidad y la clave primaria
                    fechaNacimiento = fechaNac,
                    raza = raza,
                    proposito = proposito,
                    manga = etManga.text.toString().trim(),
                    peso = peso,
                    padre = etPadre.text.toString().trim(),
                    madre = etMadre.text.toString().trim(),
                    notas = notas,
                    imagenPath = finalImagenPath
                )

                val result = animalRepository.saveAnimal(animal)
                if (result.isSuccess) {
                    guardados++
                }
            }

            val resultMsg = "Registro global exitoso: se guardaron $guardados bovinos ($duplicados omitidos por existir previamente)"
            Toast.makeText(this, resultMsg, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun evaluarYNotificarProduccion(animal: Animal) {
        val ageInMonths = calculateAgeInMonths(animal.fechaNacimiento)
        val arete = animal.numeroAnimal
        val manga = animal.manga.ifEmpty { "Sin Manga" }
        val proposito = animal.proposito
        val pesoVal = animal.peso.toDoubleOrNull() ?: 0.0

        val esLeche = proposito.equals("Leche", ignoreCase = true) || proposito.equals("Doble propósito", ignoreCase = true)
        val esCarne = proposito.equals("Carne", ignoreCase = true) || proposito.equals("Doble propósito", ignoreCase = true)

        if (esLeche && animal.sexo.equals("Hembra", ignoreCase = true) && (ageInMonths in 14..16 || ageInMonths in 24..25)) {
            val msg = if (ageInMonths in 14..16) {
                "La novilla $arete tiene $ageInMonths meses (edad ideal de preñez). ¿Desea registrarla en el módulo de producción de leche con la Manga '$manga'?"
            } else {
                "La vaca $arete tiene $ageInMonths meses (edad ideal de primer parto y ordeño). ¿Desea registrarla en el módulo de producción de leche con la Manga '$manga'?"
            }
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Criterio de Producción de Leche")
                .setMessage(msg)
                .setPositiveButton("Agregar a Producción") { _, _ ->
                    val prodRepository = SqliteProduccionRepository(this)
                    val record = pa.ac.utp.agrotrackapp.domain.model.LecheRecord(
                        numeroAnimal = arete,
                        fechaRegistro = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                        turno = "AM",
                        litros = 0.0,
                        fechaUltimoParto = animal.fechaNacimiento,
                        lactancias = 1,
                        del = ageInMonths * 30,
                        promedioDiario = 0.0,
                        activo = true
                    )
                    prodRepository.saveLecheRecord(record)
                    Toast.makeText(this, "Registrada en producción de leche", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Omitir") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } else if (esCarne && ageInMonths in 14..15 && pesoVal in 544.0..590.0) {
            val msg = "El animal $arete tiene $ageInMonths meses y pesa $pesoVal kg. Alcanzó el peso ideal de mercado (544-590 kg). ¿Desea agregarlo al módulo de producción de carne con la Manga '$manga'?"
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Criterio de Producción de Carne")
                .setMessage(msg)
                .setPositiveButton("Agregar a Producción") { _, _ ->
                    val prodRepository = SqliteProduccionRepository(this)
                    val record = pa.ac.utp.agrotrackapp.domain.model.CarneRecord(
                        numeroAnimal = arete,
                        raza = animal.raza,
                        fechaPesajeActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
                        pesoActual = pesoVal,
                        fechaPesajeAnterior = animal.fechaNacimiento,
                        pesoAnterior = pesoVal,
                        pesoEntrada = pesoVal,
                        gananciaTotal = 0.0,
                        diasTranscurridos = 1,
                        gdp = 0.0,
                        estadoSalud = "Sano",
                        activo = true
                    )
                    prodRepository.saveCarneRecord(record)
                    Toast.makeText(this, "Registrado en producción de carne", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("Omitir") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        } else {
            finish()
        }
    }

    private fun calculateAgeInMonths(birthDateStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = sdf.parse(birthDateStr) ?: return 0
            val today = Calendar.getInstance()
            val birth = Calendar.getInstance().apply { time = birthDate }
            
            val yearsDiff = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            val monthsDiff = today.get(Calendar.MONTH) - birth.get(Calendar.MONTH)
            
            val totalMonths = yearsDiff * 12 + monthsDiff
            if (totalMonths < 0) 0 else totalMonths
        } catch (e: Exception) {
            0
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Asegurar desconexión del socket al salir de la pantalla
        if (isBastonConectado && !isModoDemo) {
            bluetoothRfidService.disconnect()
        }
    }
}