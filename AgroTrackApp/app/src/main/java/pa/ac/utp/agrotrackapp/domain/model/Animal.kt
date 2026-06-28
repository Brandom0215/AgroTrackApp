package pa.ac.utp.agrotrackapp.domain.model

data class Animal(
    val numeroAnimal: String,          // Número de arete (Identificador Único)
    val sexo: String,                  // "Macho" o "Hembra"
    val trazabilidad: String = "",
    val numeroChip: String = "",
    val fechaNacimiento: String,
    val raza: String,
    val proposito: String,
    val manga: String = "",
    val peso: String = "",             // Peso único (kg)
    val padre: String = "",
    val madre: String = "",
    val notas: String = "",            // Largo < 30, sin caracteres especiales excepto ':'
    val imagenPath: String = ""        // Ruta local de la foto del animal
)
