package pa.ac.utp.agrotrackapp

import adapterMortalidad
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
// Borra "import android.R" si aparece, para que use el R de tu app

class pagMortalidad : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pag_mortalidad)

        // Configuración de los Insets (Para que no se vea debajo de la barra de estado)
        val mainView = findViewById<RecyclerView>(R.id.rvHistorial).parent as? android.view.View
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // 1. Referencia al RecyclerView
        val rvHistorial = findViewById<RecyclerView>(R.id.rvHistorial)

        // 2. Datos de prueba (Usando sintaxis moderna de Kotlin)
        val listaPrueba = mutableListOf("136", "245", "089", "450", "112")

        // 3. Configurar el RecyclerView
        // Es vital asignarle un LayoutManager para que sepa cómo posicionar los items
        rvHistorial.layoutManager = LinearLayoutManager(this)

        // 4. Conectar el adaptador
        val adapter = adapterMortalidad(listaPrueba)
        rvHistorial.adapter = adapter
    }
}