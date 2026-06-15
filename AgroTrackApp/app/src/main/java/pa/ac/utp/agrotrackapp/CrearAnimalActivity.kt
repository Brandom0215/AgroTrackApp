package pa.ac.utp.agrotrackapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class CrearAnimalActivity : AppCompatActivity() {

    private lateinit var cardMacho: MaterialCardView
    private lateinit var cardHembra: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_animal)

        // Setup Back Button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Setup Sex Toggles
        cardMacho = findViewById(R.id.cardMacho)
        cardHembra = findViewById(R.id.cardHembra)

        cardMacho.setOnClickListener { selectSex(true) }
        cardHembra.setOnClickListener { selectSex(false) }

        // Setup Dropdowns with dummy data
        setupDropdowns()

        // Setup Create Button
        findViewById<MaterialButton>(R.id.btnCrear).setOnClickListener {
            Toast.makeText(this, "Animal guardado exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun selectSex(isMacho: Boolean) {
        if (isMacho) {
            // Activar Macho
            cardMacho.setCardBackgroundColor(android.graphics.Color.parseColor("#4FC3F7"))
            cardMacho.strokeWidth = 0
            
            // Desactivar Hembra
            val surfaceColor = getColor(com.google.android.material.R.color.m3_sys_color_light_surface)
            cardHembra.setCardBackgroundColor(surfaceColor)
            cardHembra.strokeWidth = 2
            cardHembra.strokeColor = android.graphics.Color.parseColor("#E0E0E0")
        } else {
            // Activar Hembra
            val surfaceColor = getColor(com.google.android.material.R.color.m3_sys_color_light_surface)
            cardMacho.setCardBackgroundColor(surfaceColor)
            cardMacho.strokeWidth = 2
            cardMacho.strokeColor = android.graphics.Color.parseColor("#E0E0E0")
            
            // Desactivar Macho
            cardHembra.setCardBackgroundColor(android.graphics.Color.parseColor("#E1BEE7"))
            cardHembra.strokeWidth = 0
        }
    }

    private fun setupDropdowns() {
        val razaAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Brahman", "Gyr", "Holstein", "Angus", "Cebú", "Pardo Suizo"))
        findViewById<AutoCompleteTextView>(R.id.etRaza).setAdapter(razaAdapter)

        val propositoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Doble propósito", "Carne", "Leche", "Cría"))
        findViewById<AutoCompleteTextView>(R.id.etProposito).setAdapter(propositoAdapter)
        
        val mangaAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Lote 1", "Lote 2", "Corral Principal", "Cuarentena"))
        findViewById<AutoCompleteTextView>(R.id.etManga).setAdapter(mangaAdapter)

        val padreMadreAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, arrayOf("Desconocido", "Toro 001", "Vaca 102", "Inseminación Artificial"))
        findViewById<AutoCompleteTextView>(R.id.etPadre).setAdapter(padreMadreAdapter)
        findViewById<AutoCompleteTextView>(R.id.etMadre).setAdapter(padreMadreAdapter)
    }
}
