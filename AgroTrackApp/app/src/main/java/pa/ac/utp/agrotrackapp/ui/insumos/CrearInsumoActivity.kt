package pa.ac.utp.agrotrackapp.ui.insumos

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import pa.ac.utp.agrotrackapp.R

class CrearInsumoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_insumo)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val etUnidad = findViewById<AutoCompleteTextView>(R.id.etUnidad)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Sacos", "Litros", "Kg", "Gramos", "Cajas"))
        etUnidad.setAdapter(adapter)

        val nombre = intent.getStringExtra("insumo_nombre")
        if (nombre != null) {
            // Edit mode
            findViewById<TextView>(R.id.tvTitle).text = "Editar Insumo"
            findViewById<TextInputEditText>(R.id.etNombreInsumo).setText(nombre)
            
            val cantidad = intent.getDoubleExtra("insumo_cantidad", 0.0)
            val cantStr = if (cantidad % 1.0 == 0.0) cantidad.toInt().toString() else cantidad.toString()
            findViewById<TextInputEditText>(R.id.etCantidad).setText(cantStr)
            
            val unidad = intent.getStringExtra("insumo_unidad")
            etUnidad.setText(unidad, false)
        }

        findViewById<View>(R.id.btnGuardar).setOnClickListener {
            Toast.makeText(this, "Insumo guardado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
