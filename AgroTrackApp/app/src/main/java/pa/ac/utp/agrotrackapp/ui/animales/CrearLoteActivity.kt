package pa.ac.utp.agrotrackapp.ui.animales

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SqliteLoteRepository
import pa.ac.utp.agrotrackapp.domain.model.Lote

class CrearLoteActivity : AppCompatActivity() {

    private lateinit var etNombreLote: TextInputEditText
    private lateinit var loteRepository: SqliteLoteRepository

    private var editLoteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_lote)

        loteRepository = SqliteLoteRepository(this)
        etNombreLote = findViewById(R.id.etNombreLote)

        val toolbar: Toolbar = findViewById(R.id.toolbarCrearLote)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white)

        if (intent.hasExtra("EXTRA_LOTE_ID")) {
            editLoteId = intent.getIntExtra("EXTRA_LOTE_ID", -1)
            val nombre = intent.getStringExtra("EXTRA_LOTE_NOMBRE") ?: ""
            supportActionBar?.title = "Editar Lote"
            etNombreLote.setText(nombre)
            findViewById<MaterialButton>(R.id.btnCrear).text = "Guardar Cambios"
        } else {
            supportActionBar?.title = "Crear Lote"
        }

        findViewById<MaterialButton>(R.id.btnCrear).setOnClickListener {
            guardarLote()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun guardarLote() {
        val nombre = etNombreLote.text.toString().trim()
        if (nombre.isEmpty()) {
            etNombreLote.error = "Campo requerido"
            return
        }

        val lote = Lote(id = if (editLoteId != -1) editLoteId else 0, nombre = nombre)

        val result = if (editLoteId != -1) {
            loteRepository.updateLote(lote)
        } else {
            loteRepository.saveLote(lote)
        }

        if (result.isSuccess) {
            Toast.makeText(this, "Lote guardado", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
