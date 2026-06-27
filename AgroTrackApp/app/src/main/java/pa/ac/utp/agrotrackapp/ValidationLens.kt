package pa.ac.utp.agrotrackapp

/**
 * Lente de Validación (ValidationLens)
 *
 * En desarrollo funcional y móvil, un "Lens" es un concepto que se enfoca en un campo específico
 * de un objeto para inspeccionarlo, limpiarlo o modificarlo sin alterar el resto de la estructura.
 *
 * Este validador actúa como un lente de seguridad que revisa los 8 campos de registro y los
 * campos de login para evitar el ingreso de datos corruptos o extremadamente largos que rompan la UI.
 */
object ValidationLens {

    // Nombres de los campos para asociar con los errores
    const val FIELD_NOMBRE = "nombre"
    const val FIELD_APELLIDO = "apellido"
    const val FIELD_USUARIO = "usuario"
    const val FIELD_CONTRASENA = "contrasena"
    const val FIELD_CONFIRM_CONTRASENA = "confirm_contrasena"
    const val FIELD_FINCA_NOMBRE = "finca_nombre"
    const val FIELD_FINCA_CONTRASENA = "finca_contrasena"
    const val FIELD_LUGAR = "lugar"

    /**
     * Resultado de una validación a través del lente.
     */
    data class Result(
        val isValid: Boolean,
        val errors: Map<String, String> = emptyMap()
    )

    /**
     * Lente que valida si la contraseña cumple con los requisitos:
     * - Combinación de números y letras (caracteres).
     * - Mínimo 8 caracteres de longitud.
     */
    fun validatePasswordFormat(password: String): Result {
        val errors = mutableMapOf<String, String>()

        if (password.length < 8) {
            errors[FIELD_CONTRASENA] = "La contraseña debe tener al menos 8 caracteres."
        } else {
            val hasLetters = password.any { it.isLetter() }
            val hasDigits = password.any { it.isDigit() }
            
            if (!hasLetters || !hasDigits) {
                errors[FIELD_CONTRASENA] = "La contraseña debe combinar letras y números."
            }
        }

        return Result(isValid = errors.isEmpty(), errors = errors)
    }

    /**
     * Lente que valida las reglas completas de registro (8 campos).
     * Cada campo tiene límites de longitud (lengths) para evitar dañar la presentación de datos.
     */
    fun validateRegistrationForm(
        nombre: String,
        apellido: String,
        usuario: String,
        contrasena: String,
        confirmarContrasena: String,
        nombreFinca: String,
        contrasenaFinca: String,
        lugar: String
    ): Result {
        val errors = mutableMapOf<String, String>()

        // 1. Validación de Nombre (Max 30 caracteres)
        val cleanNombre = nombre.trim()
        if (cleanNombre.isEmpty()) {
            errors[FIELD_NOMBRE] = "El nombre es obligatorio."
        } else if (cleanNombre.length > 30) {
            errors[FIELD_NOMBRE] = "El nombre no puede exceder 30 caracteres."
        }

        // 2. Validación de Apellido (Max 30 caracteres)
        val cleanApellido = apellido.trim()
        if (cleanApellido.isEmpty()) {
            errors[FIELD_APELLIDO] = "El apellido es obligatorio."
        } else if (cleanApellido.length > 30) {
            errors[FIELD_APELLIDO] = "El apellido no puede exceder 30 caracteres."
        }

        // 3. Validación de Usuario/Email (Max 35 caracteres)
        val cleanUsuario = usuario.trim()
        if (cleanUsuario.isEmpty()) {
            errors[FIELD_USUARIO] = "El usuario es obligatorio."
        } else if (cleanUsuario.length < 4) {
            errors[FIELD_USUARIO] = "El usuario debe tener al menos 4 caracteres."
        } else if (cleanUsuario.length > 35) {
            errors[FIELD_USUARIO] = "El usuario no puede exceder 35 caracteres."
        }

        // 4. Validación de Contraseña (Llamada al lente de formato de contraseña)
        val passValidation = validatePasswordFormat(contrasena)
        if (!passValidation.isValid) {
            errors[FIELD_CONTRASENA] = passValidation.errors[FIELD_CONTRASENA] ?: "Contraseña inválida"
        }

        // 5. Validación de Confirmar Contraseña (Coincidencia con Contraseña)
        if (confirmarContrasena.isEmpty()) {
            errors[FIELD_CONFIRM_CONTRASENA] = "Confirma tu contraseña."
        } else if (contrasena != confirmarContrasena) {
            errors[FIELD_CONFIRM_CONTRASENA] = "Las contraseñas no coinciden."
        }

        // 6. Validación de Nombre de la Finca (Max 50 caracteres)
        val cleanFincaNombre = nombreFinca.trim()
        if (cleanFincaNombre.isEmpty()) {
            errors[FIELD_FINCA_NOMBRE] = "El nombre de la finca es obligatorio."
        } else if (cleanFincaNombre.length > 50) {
            errors[FIELD_FINCA_NOMBRE] = "El nombre no puede exceder 50 caracteres."
        }

        // 7. Validación de Contraseña de la Finca (Max 30 caracteres)
        if (contrasenaFinca.isEmpty()) {
            errors[FIELD_FINCA_CONTRASENA] = "La contraseña de la finca es obligatoria."
        } else if (contrasenaFinca.length < 4) {
            errors[FIELD_FINCA_CONTRASENA] = "Debe tener al menos 4 caracteres."
        } else if (contrasenaFinca.length > 30) {
            errors[FIELD_FINCA_CONTRASENA] = "La contraseña no puede exceder 30 caracteres."
        }

        // 8. Validación de Lugar / Ubicación (Max 80 caracteres)
        val cleanLugar = lugar.trim()
        if (cleanLugar.isEmpty()) {
            errors[FIELD_LUGAR] = "La ubicación o lugar es obligatorio."
        } else if (cleanLugar.length > 80) {
            errors[FIELD_LUGAR] = "La ubicación no puede exceder 80 caracteres."
        }

        return Result(isValid = errors.isEmpty(), errors = errors)
    }
}
