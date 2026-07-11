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
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SqliteAnimalRepository
import pa.ac.utp.agrotrackapp.domain.model.Animal
import pa.ac.utp.agrotrackapp.domain.repository.AnimalRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla que gestiona tanto el registro de un nuevo animal como la edición de uno existente.
 * Sigue el patrón Repository para la persistencia de datos y soporta captura de fotos con validación de entradas.
 */
class CrearAnimalActivity : AppCompatActivity() {

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

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1001
        private const val REQUEST_IMAGE_PICK = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_animal)

        // Inicializamos repositorio
        animalRepository = SqliteAnimalRepository(this)

        // Bind Views de TextInputLayout
        tilNumeroAnimal = findViewById(R.id.tilNumeroAnimal)
        tilTrazabilidad = findViewById(R.id.tilTrazabilidad)
        tilNumeroChip = findViewById(R.id.tilNumeroChip)
        tilFechaNacimiento = findViewById(R.id.tilFechaNacimiento)
        tilNotas = findViewById(R.id.tilNotas)

        // Bind Views
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

        // Setup Dropdowns with values
        setupDropdowns()

        // Detectar si estamos en modo edición
        editArete = intent.getStringExtra("EXTRA_NUMERO_ANIMAL")
        isEditMode = editArete != null

        if (isEditMode) {
            cargarDatosParaEdicion()
        }

        // Setup Create/Save Button
        btnCrear.setOnClickListener {
            guardarAnimal()
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
            // Activar Macho (Celeste)
            cardMacho.setCardBackgroundColor(Color.parseColor("#4FC3F7"))
            cardMacho.strokeWidth = 0
            
            // Desactivar Hembra
            val surfaceColor = getColor(com.google.android.material.R.color.m3_sys_color_light_surface)
            cardHembra.setCardBackgroundColor(surfaceColor)
            cardHembra.strokeWidth = 2
            cardHembra.strokeColor = Color.parseColor("#E0E0E0")
        } else {
            sexoSeleccionado = "Hembra"
            // Activar Hembra (Morado claro)
            cardHembra.setCardBackgroundColor(Color.parseColor("#E1BEE7"))
            cardHembra.strokeWidth = 0
            
            // Desactivar Macho
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

        // Cambiar títulos
        tvTitleActivity.text = "Editar Animal"
        btnCrear.text = "Guardar Cambios"
        
        // Bloquear número de arete
        etNumeroAnimal.setText(animal.numeroAnimal)
        etNumeroAnimal.isEnabled = false

        // Seleccionar sexo
        selectSex(animal.sexo == "Macho")

        // Cargar otros campos
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

        // Cargar imagen si existe y el archivo está guardado localmente
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

        // 1. Validaciones para Arete (Mínimo 15 y máximo 30 números, sin letras ni caracteres especiales)
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

        // 2. Validaciones para Trazabilidad (Misma validación: si existe, de 15 a 30 números, sin caracteres especiales)
        if (trazabilidad.isNotEmpty() && !trazabilidad.matches(Regex("^[0-9]{15,30}$"))) {
            tilTrazabilidad.error = "La trazabilidad debe tener entre 15 y 30 números (sin letras ni caracteres especiales)"
            etTrazabilidad.requestFocus()
            return
        }

        // 3. Validaciones para Número de Chip (Misma validación: si existe, de 15 a 30 números, sin caracteres especiales)
        if (chip.isNotEmpty() && !chip.matches(Regex("^[0-9]{15,30}$"))) {
            tilNumeroChip.error = "El número de chip debe tener entre 15 y 30 números (sin letras ni caracteres especiales)"
            etNumeroChip.requestFocus()
            return
        }

        // 4. Validaciones de Información básica
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

        // 5. Validaciones para Notas/Observación (Máximo 100 caracteres y evitar especiales excepto el ':')
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

        // Mover imagen temporal a permanente usando el arete como clave
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

        // Crear objeto Animal
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
                    val prodRepository = pa.ac.utp.agrotrackapp.data.produccion.SqliteProduccionRepository(this)
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
                    val prodRepository = pa.ac.utp.agrotrackapp.data.produccion.SqliteProduccionRepository(this)
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
}