package pa.ac.utp.agrotrackapp.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "AgroTrack.db"
        const val DATABASE_VERSION = 2

        // Table Names
        const val TABLE_USUARIOS = "usuarios"
        const val TABLE_ANIMALES = "animales"
        const val TABLE_CARNE_RECORDS = "carne_records"
        const val TABLE_LECHE_RECORDS = "leche_records"
        const val TABLE_MORTALIDAD_RECORDS = "mortalidad_records"
        const val TABLE_INVENTARIO_ITEMS = "inventario_items"
        const val TABLE_ALERTAS = "alertas"

        // Usuarios Columns
        const val COL_USER_USUARIO = "usuario"
        const val COL_USER_NOMBRE = "nombre"
        const val COL_USER_APELLIDO = "apellido"
        const val COL_USER_CONTRASENA = "contrasena"
        const val COL_USER_NOMBRE_FINCA = "nombre_finca"
        const val COL_USER_CONTRASENA_FINCA = "contrasena_finca"
        const val COL_USER_LUGAR = "lugar"
        const val COL_USER_ROL = "rol"
        const val COL_USER_PROFILE_IMAGE = "profile_image_path"

        // Animales Columns
        const val COL_ANIMAL_NUMERO = "numero_animal"
        const val COL_ANIMAL_SEXO = "sexo"
        const val COL_ANIMAL_TRAZABILIDAD = "trazabilidad"
        const val COL_ANIMAL_CHIP = "numero_chip"
        const val COL_ANIMAL_FECHA_NACIMIENTO = "fecha_nacimiento"
        const val COL_ANIMAL_RAZA = "raza"
        const val COL_ANIMAL_PROPOSITO = "proposito"
        const val COL_ANIMAL_MANGA = "manga"
        const val COL_ANIMAL_PESO = "peso"
        const val COL_ANIMAL_PADRE = "padre"
        const val COL_ANIMAL_MADRE = "madre"
        const val COL_ANIMAL_NOTAS = "notas"
        const val COL_ANIMAL_IMAGEN = "imagen_path"

        // Carne Columns
        const val COL_CARNE_NUMERO = "numero_animal"
        const val COL_CARNE_RAZA = "raza"
        const val COL_CARNE_FECHA_ACTUAL = "fecha_pesaje_actual"
        const val COL_CARNE_PESO_ACTUAL = "peso_actual"
        const val COL_CARNE_FECHA_ANTERIOR = "fecha_pesaje_anterior"
        const val COL_CARNE_PESO_ANTERIOR = "peso_anterior"
        const val COL_CARNE_PESO_ENTRADA = "peso_entrada"
        const val COL_CARNE_GANANCIA = "ganancia_total"
        const val COL_CARNE_DIAS = "dias_transcurridos"
        const val COL_CARNE_GDP = "gdp"
        const val COL_CARNE_SALUD = "estado_salud"
        const val COL_CARNE_ACTIVO = "activo"

        // Leche Columns
        const val COL_LECHE_NUMERO = "numero_animal"
        const val COL_LECHE_FECHA = "fecha_registro"
        const val COL_LECHE_TURNO = "turno"
        const val COL_LECHE_LITROS = "litros"
        const val COL_LECHE_FECHA_PARTO = "fecha_ultimo_parto"
        const val COL_LECHE_LACTANCIAS = "lactancias"
        const val COL_LECHE_DEL = "del"
        const val COL_LECHE_PROMEDIO = "promedio_diario"
        const val COL_LECHE_ACTIVO = "activo"

        // Mortalidad Columns
        const val COL_MORT_NUMERO = "numero_animal"
        const val COL_MORT_CAUSA = "causa"
        const val COL_MORT_FECHA = "fecha_muerte"
        const val COL_MORT_DETALLES = "detalles"

        // Inventario Columns
        const val COL_INV_ID = "id"
        const val COL_INV_NOMBRE = "nombre"
        const val COL_INV_TIPO = "tipo"
        const val COL_INV_TIPO_OTRO = "tipo_otro"
        const val COL_INV_FOTO = "foto_path"
        const val COL_INV_STOCK = "stock"
        const val COL_INV_LIMITE = "limite_notificacion"
        const val COL_INV_UNIDAD = "unidad"
        const val COL_INV_COSTO = "costo"
        const val COL_INV_PRECIO = "precio"
        const val COL_INV_FECHA = "fecha_registro"

        // Alertas Columns
        const val COL_ALERTA_ID = "id"
        const val COL_ALERTA_TITULO = "titulo"
        const val COL_ALERTA_DESC = "descripcion"
        const val COL_ALERTA_TIPO = "tipo"
        const val COL_ALERTA_FECHA = "fecha"
        const val COL_ALERTA_PRIO = "prioridad"
        const val COL_ALERTA_DISMISSED = "is_dismissed"
        const val COL_ALERTA_DEST = "destination_id"
        const val COL_ALERTA_REF = "reference_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USUARIOS (
                $COL_USER_USUARIO TEXT PRIMARY KEY,
                $COL_USER_NOMBRE TEXT,
                $COL_USER_APELLIDO TEXT,
                $COL_USER_CONTRASENA TEXT,
                $COL_USER_NOMBRE_FINCA TEXT,
                $COL_USER_CONTRASENA_FINCA TEXT,
                $COL_USER_LUGAR TEXT,
                $COL_USER_ROL TEXT,
                $COL_USER_PROFILE_IMAGE TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_ANIMALES (
                $COL_ANIMAL_NUMERO TEXT PRIMARY KEY,
                $COL_ANIMAL_SEXO TEXT,
                $COL_ANIMAL_TRAZABILIDAD TEXT,
                $COL_ANIMAL_CHIP TEXT,
                $COL_ANIMAL_FECHA_NACIMIENTO TEXT,
                $COL_ANIMAL_RAZA TEXT,
                $COL_ANIMAL_PROPOSITO TEXT,
                $COL_ANIMAL_MANGA TEXT,
                $COL_ANIMAL_PESO TEXT,
                $COL_ANIMAL_PADRE TEXT,
                $COL_ANIMAL_MADRE TEXT,
                $COL_ANIMAL_NOTAS TEXT,
                $COL_ANIMAL_IMAGEN TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_CARNE_RECORDS (
                $COL_CARNE_NUMERO TEXT PRIMARY KEY,
                $COL_CARNE_RAZA TEXT,
                $COL_CARNE_FECHA_ACTUAL TEXT,
                $COL_CARNE_PESO_ACTUAL REAL,
                $COL_CARNE_FECHA_ANTERIOR TEXT,
                $COL_CARNE_PESO_ANTERIOR REAL,
                $COL_CARNE_PESO_ENTRADA REAL,
                $COL_CARNE_GANANCIA REAL,
                $COL_CARNE_DIAS INTEGER,
                $COL_CARNE_GDP REAL,
                $COL_CARNE_SALUD TEXT,
                $COL_CARNE_ACTIVO INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_LECHE_RECORDS (
                $COL_LECHE_NUMERO TEXT PRIMARY KEY,
                $COL_LECHE_FECHA TEXT,
                $COL_LECHE_TURNO TEXT,
                $COL_LECHE_LITROS REAL,
                $COL_LECHE_FECHA_PARTO TEXT,
                $COL_LECHE_LACTANCIAS INTEGER,
                $COL_LECHE_DEL INTEGER,
                $COL_LECHE_PROMEDIO REAL,
                $COL_LECHE_ACTIVO INTEGER
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_MORTALIDAD_RECORDS (
                $COL_MORT_NUMERO TEXT PRIMARY KEY,
                $COL_MORT_CAUSA TEXT,
                $COL_MORT_FECHA TEXT,
                $COL_MORT_DETALLES TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_INVENTARIO_ITEMS (
                $COL_INV_ID TEXT PRIMARY KEY,
                $COL_INV_NOMBRE TEXT,
                $COL_INV_TIPO TEXT,
                $COL_INV_TIPO_OTRO TEXT,
                $COL_INV_FOTO TEXT,
                $COL_INV_STOCK REAL,
                $COL_INV_LIMITE REAL,
                $COL_INV_UNIDAD TEXT,
                $COL_INV_COSTO REAL,
                $COL_INV_PRECIO REAL,
                $COL_INV_FECHA TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_ALERTAS (
                $COL_ALERTA_ID TEXT PRIMARY KEY,
                $COL_ALERTA_TITULO TEXT,
                $COL_ALERTA_DESC TEXT,
                $COL_ALERTA_TIPO TEXT,
                $COL_ALERTA_FECHA TEXT,
                $COL_ALERTA_PRIO TEXT,
                $COL_ALERTA_DISMISSED INTEGER,
                $COL_ALERTA_DEST INTEGER,
                $COL_ALERTA_REF TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ANIMALES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CARNE_RECORDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LECHE_RECORDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MORTALIDAD_RECORDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INVENTARIO_ITEMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ALERTAS")
        onCreate(db)
    }

    fun clearAllTables() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_USUARIOS, null, null)
            db.delete(TABLE_ANIMALES, null, null)
            db.delete(TABLE_CARNE_RECORDS, null, null)
            db.delete(TABLE_LECHE_RECORDS, null, null)
            db.delete(TABLE_MORTALIDAD_RECORDS, null, null)
            db.delete(TABLE_INVENTARIO_ITEMS, null, null)
            db.delete(TABLE_ALERTAS, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
