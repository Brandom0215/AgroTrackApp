package pa.ac.utp.agrotrackapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class InsumosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insumos)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnAdd).setOnClickListener {
            val intent = Intent(this, CrearInsumoActivity::class.java)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewInsumos)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns

        val insumos = listOf(
            Insumo("LEVADURA", 180.0, "litros", "#4CAF50"),
            Insumo("SAL MINERAL", 60.0, "sacos", "#4CAF50"),
            Insumo("AFRECHO DE CERVEZA", 320.0, "sacos", "#FFA000"),
            Insumo("GALLINAZA", 150.0, "sacos", "#4CAF50"),
            Insumo("MAÍZ MOLIDO", 200.0, "sacos", "#FFA000")
        )

        recyclerView.adapter = InsumosAdapter(insumos) { insumo ->
            val intent = Intent(this, CrearInsumoActivity::class.java)
            intent.putExtra("insumo_nombre", insumo.nombre)
            intent.putExtra("insumo_cantidad", insumo.cantidad)
            intent.putExtra("insumo_unidad", insumo.unidad)
            startActivity(intent)
        }
    }
}
