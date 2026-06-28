package pa.ac.utp.agrotrackapp.ui.animales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pa.ac.utp.agrotrackapp.R
import pa.ac.utp.agrotrackapp.domain.model.Animal

/**
 * Adaptador para gestionar y desplegar la lista de animales en un RecyclerView.
 * * @property animals Lista de datos origen (Modelos de tipo Animal).
 * @property onViewClick Lambda que se ejecuta al presionar el botón de ver detalles.
 * @property onEditClick Lambda que se ejecuta al presionar el botón de editar.
 */
class AnimalAdapter(
    private var animals: List<Animal>,
    private val onViewClick: (Animal) -> Unit, // Callback para navegación/lectura
    private val onEditClick: (Animal) -> Unit   // Callback para edición
) : RecyclerView.Adapter<AnimalAdapter.ViewHolder>() {

    /**
     * Contenedor de vistas (ViewHolder). Se encarga de hacer el 'findViewById'
     * una sola vez por cada elemento visual de la lista, optimizando el rendimiento.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvArete: TextView = view.findViewById(R.id.tvRowArete)
        val tvRaza: TextView = view.findViewById(R.id.tvRowRaza)
        val tvProposito: TextView = view.findViewById(R.id.tvRowProposito)
        val btnView: ImageButton = view.findViewById(R.id.btnViewAnimal)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEditAnimal)
    }

    /**
     * Crea los nuevos contenedores de vistas (ViewHolder) cuando el RecyclerView lo requiere.
     * Infla el diseño XML específico de la fila (`item_animal_row`).
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_animal_row, parent, false)
        return ViewHolder(view)
    }

    /**
     * Vincula los datos de un animal específico en una posición determinada con los
     * componentes visuales del ViewHolder. Se ejecuta dinámicamente al hacer scroll.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = animals[position]

        // Asignación de datos del modelo a las vistas de la fila
        holder.tvArete.text = animal.numeroAnimal
        holder.tvRaza.text = animal.raza
        holder.tvProposito.text = animal.proposito

        // ASIGNACIÓN DE EVENTOS: Delegamos la acción mediante lambdas hacia la View externa (Activity/Fragment)
        holder.btnView.setOnClickListener { onViewClick(animal) }
        holder.btnEdit.setOnClickListener { onEditClick(animal) }
    }

    /**
     * Retorna la cantidad total de elementos en la lista.
     * El RecyclerView utiliza esto para saber cuántas filas debe renderizar.
     */
    override fun getItemCount(): Int = animals.size

    /**
     * Método público para actualizar dinámicamente la lista de animales (por ejemplo, al filtrar o recargar).
     * Reemplaza la lista actual y notifica al RecyclerView que debe redibujarse completamente.
     */
    fun updateList(newAnimals: List<Animal>) {
        animals = newAnimals
        notifyDataSetChanged() // Fuerza la actualización completa de la UI
    }
}