package pa.ac.utp.agrotrackapp.ui.animales

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.data.animal.SqliteLoteRepository
import pa.ac.utp.agrotrackapp.domain.model.Lote

class LotesActivity : AppCompatActivity() {

    private lateinit var rvLotes: RecyclerView
    private lateinit var loteAdapter: LoteAdapter
    private lateinit var loteRepository: SqliteLoteRepository
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lotes)

        val toolbar: Toolbar = findViewById(R.id.toolbarLotes)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close_white)

        loteRepository = SqliteLoteRepository(this)

        rvLotes = findViewById(R.id.rvLotes)
        rvLotes.layoutManager = LinearLayoutManager(this)

        loteAdapter = LoteAdapter(
            lotes = emptyList(),
            onItemClick = { lote ->
                if (!isEditMode) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("EXTRA_LOTE_NOMBRE", lote.nombre)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
            },
            onEditClick = { lote ->
                abrirCrearLoteActivity(lote)
            },
            onDeleteClick = { lote ->
                androidx.appcompat.app.AlertDialog.Builder(this@LotesActivity)
                    .setTitle("Eliminar Lote")
                    .setMessage("¿Estás seguro que deseas eliminar este lote?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        val result = loteRepository.deleteLote(lote.id)
                        if (result.isSuccess) {
                            Toast.makeText(this@LotesActivity, "Lote eliminado", Toast.LENGTH_SHORT).show()
                            cargarLotes()
                        } else {
                            Toast.makeText(this@LotesActivity, "Error al eliminar: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )
        rvLotes.adapter = loteAdapter

        findViewById<FloatingActionButton>(R.id.fabAddLote).setOnClickListener {
            abrirCrearLoteActivity(null)
        }
    }

    override fun onResume() {
        super.onResume()
        cargarLotes()
    }

    private fun cargarLotes() {
        val lotes = loteRepository.getLotes()
        loteAdapter.updateList(lotes)
    }

    private fun abrirCrearLoteActivity(lote: Lote?) {
        val intent = Intent(this, CrearLoteActivity::class.java)
        if (lote != null) {
            intent.putExtra("EXTRA_LOTE_ID", lote.id)
            intent.putExtra("EXTRA_LOTE_NOMBRE", lote.nombre)
        }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_lotes, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_edit_mode -> {
                isEditMode = !isEditMode
                loteAdapter.isEditMode = isEditMode
                loteAdapter.notifyDataSetChanged()
                
                if (isEditMode) {
                    Toast.makeText(this, "Modo edición activado.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Modo selección activado.", Toast.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
