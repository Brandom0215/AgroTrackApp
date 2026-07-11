package pa.ac.utp.agrotrackapp.ui.insumos

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.inventario.SqliteInventarioRepository
import pa.ac.utp.agrotrackapp.domain.model.InventarioItem
import pa.ac.utp.agrotrackapp.domain.repository.InventarioRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CrearInventarioActivity : AppCompatActivity() {

    private lateinit var inventarioRepository: InventarioRepository
    private var isEditMode = false
    private var itemId: String? = null
    private var fotoPathLocal: String? = null

    // Views
    private lateinit var tvTitle: TextView
    private lateinit var ivProductoPreview: ImageView
    private lateinit var btnCamera: MaterialCardView
    
    private lateinit var tilNombre: TextInputLayout
    private lateinit var etNombre: TextInputEditText
    
    private lateinit var tilTipo: TextInputLayout
    private lateinit var etTipo: AutoCompleteTextView
    
    private lateinit var tilTipoOtro: TextInputLayout
    private lateinit var etTipoOtro: TextInputEditText
    
    private lateinit var tilStock: TextInputLayout
    private lateinit var etStock: TextInputEditText

    private lateinit var tilCosto: TextInputLayout
    private lateinit var etCosto: TextInputEditText

    private lateinit var tilPrecio: TextInputLayout
    private lateinit var etPrecio: TextInputEditText
    
    private lateinit var tilLimite: TextInputLayout
    private lateinit var etLimite: TextInputEditText
    
    private lateinit var tilUnidad: TextInputLayout
    private lateinit var etUnidad: AutoCompleteTextView
    
    private lateinit var btnGuardar: MaterialButton
    private lateinit var btnEliminar: MaterialButton

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 2001
        private const val REQUEST_IMAGE_PICK = 2002
        private val TIPOS = arrayOf("Alimento", "Medicina", "Herramienta", "Otro")
        private val UNIDADES = arrayOf("Sacos", "Litros", "Kg", "Gramos", "Cajas", "Unidades")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_inventario)

        inventarioRepository = SqliteInventarioRepository(this)

        // Bind views
        tvTitle = findViewById(R.id.tvTitle)
        ivProductoPreview = findViewById(R.id.ivProductoPreview)
        btnCamera = findViewById(R.id.btnCamera)
        
        tilNombre = findViewById(R.id.tilNombre)
        etNombre = findViewById(R.id.etNombre)
        
        tilTipo = findViewById(R.id.tilTipo)
        etTipo = findViewById(R.id.etTipo)
        
        tilTipoOtro = findViewById(R.id.tilTipoOtro)
        etTipoOtro = findViewById(R.id.etTipoOtro)
        
        tilStock = findViewById(R.id.tilStock)
        etStock = findViewById(R.id.etStock)

        tilCosto = findViewById(R.id.tilCosto)
        etCosto = findViewById(R.id.etCosto)

        tilPrecio = findViewById(R.id.tilPrecio)
        etPrecio = findViewById(R.id.etPrecio)
        
        tilLimite = findViewById(R.id.tilLimite)
        etLimite = findViewById(R.id.etLimite)
        
        tilUnidad = findViewById(R.id.tilUnidad)
        etUnidad = findViewById(R.id.etUnidad)
        
        btnGuardar = findViewById(R.id.btnGuardar)
        btnEliminar = findViewById(R.id.btnEliminar)

        // Setup back button
        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup dropdown adapters
        val tipoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, TIPOS)
        etTipo.setAdapter(tipoAdapter)

        val unidadAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, UNIDADES)
        etUnidad.setAdapter(unidadAdapter)

        // Listen for type change to toggle dynamic fields
        etTipo.setOnItemClickListener { _, _, position, _ ->
            val selectedTipo = TIPOS[position]
            updateDynamicFieldsVisibility(selectedTipo)
        }

        // Setup photo selector
        btnCamera.setOnClickListener {
            mostrarOpcionesImagen()
        }

        // Detect Edit Mode
        itemId = intent.getStringExtra("EXTRA_ITEM_ID")
        isEditMode = itemId != null

        if (isEditMode) {
            cargarDatos()
        }

        btnGuardar.setOnClickListener {
            guardarProducto()
        }

        btnEliminar.setOnClickListener {
            eliminarProducto()
        }
    }

    private fun updateDynamicFieldsVisibility(tipo: String) {
        if (tipo == "Otro") {
            tilTipoOtro.visibility = View.VISIBLE
        } else {
            tilTipoOtro.visibility = View.GONE
            etTipoOtro.setText("")
        }

        if (tipo == "Alimento" || tipo == "Medicina") {
            tilLimite.visibility = View.VISIBLE
        } else {
            tilLimite.visibility = View.GONE
            etLimite.setText("")
        }
    }

    private fun cargarDatos() {
        val item = inventarioRepository.getItem(itemId!!) ?: return
        
        tvTitle.text = "Editar Producto"
        btnEliminar.visibility = View.VISIBLE
        
        etNombre.setText(item.nombre)
        etTipo.setText(item.tipo, false)
        etUnidad.setText(item.unidad, false)
        
        val stockStr = if (item.stock % 1.0 == 0.0) item.stock.toInt().toString() else item.stock.toString()
        etStock.setText(stockStr)

        val costStr = if (item.costo % 1.0 == 0.0) item.costo.toInt().toString() else item.costo.toString()
        etCosto.setText(costStr)

        val priceStr = if (item.precio % 1.0 == 0.0) item.precio.toInt().toString() else item.precio.toString()
        etPrecio.setText(priceStr)

        updateDynamicFieldsVisibility(item.tipo)

        if (item.tipo == "Otro") {
            etTipoOtro.setText(item.tipoOtro ?: "")
        }

        if (item.tipo == "Alimento" || item.tipo == "Medicina") {
            item.limiteNotificacion?.let {
                val limStr = if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
                etLimite.setText(limStr)
            }
        }

        if (!item.fotoPath.isNullOrEmpty()) {
            val imgFile = File(item.fotoPath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                ivProductoPreview.setImageBitmap(bitmap)
                fotoPathLocal = item.fotoPath
            }
        }
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf<CharSequence>("Tomar Foto (Cámara)", "Seleccionar de Galería", "Cancelar")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Añadir foto del producto")
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
            val tempId = itemId ?: UUID.randomUUID().toString()
            val destFile = File(filesDir, "product_$tempId.png")
            try {
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    if (bitmap != null) {
                        destFile.outputStream().use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        fotoPathLocal = destFile.absolutePath
                        ivProductoPreview.setImageBitmap(bitmap)
                    } else {
                        Toast.makeText(this, "No se recibió imagen de la cámara", Toast.LENGTH_SHORT).show()
                    }
                } else if (requestCode == REQUEST_IMAGE_PICK) {
                    val selectedImageUri = data?.data
                    if (selectedImageUri != null) {
                        contentResolver.openInputStream(selectedImageUri)?.use { inputStream ->
                            destFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        fotoPathLocal = destFile.absolutePath
                        ivProductoPreview.setImageURI(selectedImageUri)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error al guardar foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun guardarProducto() {
        val nombre = etNombre.text.toString().trim()
        val tipo = etTipo.text.toString().trim()
        val tipoOtro = etTipoOtro.text.toString().trim()
        val stockStr = etStock.text.toString().trim()
        val costoStr = etCosto.text.toString().trim()
        val precioStr = etPrecio.text.toString().trim()
        val limiteStr = etLimite.text.toString().trim()
        val unidad = etUnidad.text.toString().trim()

        // Validaciones
        if (nombre.isEmpty()) {
            tilNombre.error = "Ingrese el nombre del producto"
            return
        } else {
            tilNombre.error = null
        }

        if (tipo.isEmpty()) {
            tilTipo.error = "Seleccione un tipo"
            return
        } else {
            tilTipo.error = null
        }

        if (tipo == "Otro" && tipoOtro.isEmpty()) {
            tilTipoOtro.error = "Especifique el tipo personalizado"
            return
        } else {
            tilTipoOtro.error = null
        }

        if (stockStr.isEmpty()) {
            tilStock.error = "Ingrese el stock disponible"
            return
        }
        val stock = stockStr.toDoubleOrNull()
        if (stock == null || stock < 0) {
            tilStock.error = "Ingrese una cantidad válida de stock (>= 0)"
            return
        } else {
            tilStock.error = null
        }

        if (costoStr.isEmpty()) {
            tilCosto.error = "Ingrese el costo del producto"
            return
        }
        val costo = costoStr.toDoubleOrNull()
        if (costo == null || costo < 0) {
            tilCosto.error = "Ingrese un costo unitario válido (>= 0)"
            return
        } else {
            tilCosto.error = null
        }

        if (precioStr.isEmpty()) {
            tilPrecio.error = "Ingrese el precio de venta"
            return
        }
        val precio = precioStr.toDoubleOrNull()
        if (precio == null || precio < 0) {
            tilPrecio.error = "Ingrese un precio unitario válido (>= 0)"
            return
        } else {
            tilPrecio.error = null
        }

        var limite: Double? = null
        if (tipo == "Alimento" || tipo == "Medicina") {
            if (limiteStr.isNotEmpty()) {
                limite = limiteStr.toDoubleOrNull()
                if (limite == null || limite < 0) {
                    tilLimite.error = "Ingrese una cantidad de alerta válida"
                    return
                } else {
                    tilLimite.error = null
                }
            }
        }

        if (unidad.isEmpty()) {
            tilUnidad.error = "Seleccione la unidad de medida"
            return
        } else {
            tilUnidad.error = null
        }

        var fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        if (isEditMode) {
            val originalItem = inventarioRepository.getItem(itemId!!)
            if (originalItem != null) {
                fecha = originalItem.fechaRegistro
            }
        }

        val targetId = itemId ?: UUID.randomUUID().toString()
        val item = InventarioItem(
            id = targetId,
            nombre = nombre,
            tipo = tipo,
            tipoOtro = if (tipo == "Otro") tipoOtro else null,
            fotoPath = fotoPathLocal,
            stock = stock,
            limiteNotificacion = limite,
            unidad = unidad,
            costo = costo,
            precio = precio,
            fechaRegistro = fecha
        )

        val result = if (isEditMode) {
            inventarioRepository.updateItem(item)
        } else {
            inventarioRepository.saveItem(item)
        }

        if (result.isSuccess) {
            Toast.makeText(this, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error al guardar el producto: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun eliminarProducto() {
        if (!isEditMode || itemId == null) return

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar producto")
        builder.setMessage("¿Está seguro de que desea eliminar este producto del inventario?")
        builder.setPositiveButton("Sí") { dialog, _ ->
            val result = inventarioRepository.deleteItem(itemId!!)
            if (result.isSuccess) {
                // Eliminar foto local si existe
                if (!fotoPathLocal.isNullOrEmpty()) {
                    val file = File(fotoPathLocal!!)
                    if (file.exists()) {
                        file.delete()
                    }
                }
                Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al eliminar producto", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}
