package pa.ac.utp.agrotrackapp.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "AgroTrack.db"
        const val DATABASE_VERSION = 7

        // Table Names
        const val TABLE_USUARIOS = "usuarios"
        const val TABLE_ANIMALES = "animales"
        const val TABLE_LOTES = "lotes"
        const val TABLE_CARNE_RECORDS = "carne_records"
        const val TABLE_LECHE_RECORDS = "leche_records"
        const val TABLE_MORTALIDAD_RECORDS = "mortalidad_records"
        const val TABLE_INVENTARIO_ITEMS = "inventario_items"
        const val TABLE_ALERTAS = "alertas"
        const val TABLE_SANITARIA = "registros_sanitarios"
        const val TABLE_TRANSACCIONES = "transacciones"

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
        const val COL_ANIMAL_LOTE = "lote_nombre"

        // Lotes Columns
        const val COL_LOTE_ID = "id"
        const val COL_LOTE_NOMBRE = "nombre"

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
        const val COL_ALERTA_FECHA_PROG = "fecha_programada"

        // Sanitaria Columns
        const val COL_SAN_ID = "id"
        const val COL_SAN_IDENTIFICADOR = "identificador"
        const val COL_SAN_ALCANCE = "alcance"
        const val COL_SAN_CATEGORIA = "categoria"
        const val COL_SAN_DETALLE = "detalle"
        const val COL_SAN_PRODUCTO = "producto"
        const val COL_SAN_DOSIS = "dosis"
        const val COL_SAN_FECHA = "fecha"
        const val COL_SAN_PROXIMA_DOSIS = "proxima_dosis"
        const val COL_SAN_VETERINARIO = "veterinario"
        const val COL_SAN_NOTAS = "notas"
        const val COL_SAN_ESTADO = "estado"
        const val COL_SAN_GRUPO_ID = "grupo_id"

        // Transacciones Columns
        const val COL_TRANS_ID = "id"
        const val COL_TRANS_TIPO = "tipo"
        const val COL_TRANS_PROD_ID = "producto_id"
        const val COL_TRANS_PROD_NOMBRE = "producto_nombre"
        const val COL_TRANS_CANTIDAD = "cantidad"
        const val COL_TRANS_PRECIO_UNIT = "precio_unitario"
        const val COL_TRANS_COSTO_UNIT = "costo_unitario"
        const val COL_TRANS_FECHA = "fecha"
        const val COL_TRANS_DETALLES = "detalles"
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
                $COL_ANIMAL_IMAGEN TEXT,
                $COL_ANIMAL_LOTE TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_LOTES (
                $COL_LOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LOTE_NOMBRE TEXT UNIQUE NOT NULL
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
                $COL_ALERTA_REF TEXT,
                $COL_ALERTA_FECHA_PROG TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_SANITARIA (
                $COL_SAN_ID TEXT PRIMARY KEY,
                $COL_SAN_IDENTIFICADOR TEXT,
                $COL_SAN_ALCANCE TEXT,
                $COL_SAN_CATEGORIA TEXT,
                $COL_SAN_DETALLE TEXT,
                $COL_SAN_PRODUCTO TEXT,
                $COL_SAN_DOSIS TEXT,
                $COL_SAN_FECHA TEXT,
                $COL_SAN_PROXIMA_DOSIS TEXT,
                $COL_SAN_VETERINARIO TEXT,
                $COL_SAN_NOTAS TEXT,
                $COL_SAN_ESTADO TEXT,
                $COL_SAN_GRUPO_ID TEXT
            )
        """)

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_TRANSACCIONES (
                $COL_TRANS_ID TEXT PRIMARY KEY,
                $COL_TRANS_TIPO TEXT,
                $COL_TRANS_PROD_ID TEXT,
                $COL_TRANS_PROD_NOMBRE TEXT,
                $COL_TRANS_CANTIDAD REAL,
                $COL_TRANS_PRECIO_UNIT REAL,
                $COL_TRANS_COSTO_UNIT REAL,
                $COL_TRANS_FECHA TEXT,
                $COL_TRANS_DETALLES TEXT
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 3) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_SANITARIA (
                    $COL_SAN_ID TEXT PRIMARY KEY,
                    $COL_SAN_IDENTIFICADOR TEXT,
                    $COL_SAN_ALCANCE TEXT,
                    $COL_SAN_CATEGORIA TEXT,
                    $COL_SAN_DETALLE TEXT,
                    $COL_SAN_PRODUCTO TEXT,
                    $COL_SAN_DOSIS TEXT,
                    $COL_SAN_FECHA TEXT,
                    $COL_SAN_PROXIMA_DOSIS TEXT,
                    $COL_SAN_VETERINARIO TEXT,
                    $COL_SAN_NOTAS TEXT,
                    $COL_SAN_ESTADO TEXT
                )
            """)
        }
        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE $TABLE_ALERTAS ADD COLUMN $COL_ALERTA_FECHA_PROG TEXT")
            } catch (e: Exception) { /* column may already exist */ }
        }
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE $TABLE_SANITARIA ADD COLUMN $COL_SAN_GRUPO_ID TEXT DEFAULT ''")
            } catch (e: Exception) { /* column may already exist */ }
        }
        if (oldVersion < 6) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_TRANSACCIONES (
                    $COL_TRANS_ID TEXT PRIMARY KEY,
                    $COL_TRANS_TIPO TEXT,
                    $COL_TRANS_PROD_ID TEXT,
                    $COL_TRANS_PROD_NOMBRE TEXT,
                    $COL_TRANS_CANTIDAD REAL,
                    $COL_TRANS_PRECIO_UNIT REAL,
                    $COL_TRANS_COSTO_UNIT REAL,
                    $COL_TRANS_FECHA TEXT,
                    $COL_TRANS_DETALLES TEXT
                )
            """)
        }
        if (oldVersion < 7) {
            try {
                db.execSQL("ALTER TABLE $TABLE_ANIMALES ADD COLUMN $COL_ANIMAL_LOTE TEXT DEFAULT ''")
            } catch (e: Exception) { /* column may already exist */ }
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_LOTES (
                    $COL_LOTE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_LOTE_NOMBRE TEXT UNIQUE NOT NULL
                )
            """)
        }
    }

    fun clearAllTables() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_USUARIOS, null, null)
            db.delete(TABLE_ANIMALES, null, null)
            db.delete(TABLE_LOTES, null, null)
            db.delete(TABLE_CARNE_RECORDS, null, null)
            db.delete(TABLE_LECHE_RECORDS, null, null)
            db.delete(TABLE_MORTALIDAD_RECORDS, null, null)
            db.delete(TABLE_INVENTARIO_ITEMS, null, null)
            db.delete(TABLE_ALERTAS, null, null)
            db.delete(TABLE_SANITARIA, null, null)
            db.delete(TABLE_TRANSACCIONES, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        preloadSeedDataIfEmpty(db)
    }

    private fun preloadSeedDataIfEmpty(db: SQLiteDatabase) {
        db.beginTransaction()
        try {
            // 1. Seed Users
            val userCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_USUARIOS", null)
            var userCount = 0
            if (userCursor.moveToFirst()) {
                userCount = userCursor.getInt(0)
            }
            userCursor.close()
            
            if (userCount == 0) {
                db.execSQL("""
                    INSERT INTO $TABLE_USUARIOS (
                        $COL_USER_USUARIO, $COL_USER_NOMBRE, $COL_USER_APELLIDO, 
                        $COL_USER_CONTRASENA, $COL_USER_NOMBRE_FINCA, $COL_USER_CONTRASENA_FINCA, 
                        $COL_USER_LUGAR, $COL_USER_ROL, $COL_USER_PROFILE_IMAGE
                    ) VALUES (
                        'admin', 'Juan', 'Pérez', 'password123', 'Finca La Esmeralda', 'finca123', 'Chiriquí, Panamá', 'Administrador', ''
                    )
                """)
            }

            // 2. Seed Animals
            val animalCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_ANIMALES", null)
            var animalCount = 0
            if (animalCursor.moveToFirst()) {
                animalCount = animalCursor.getInt(0)
            }
            animalCursor.close()

            if (animalCount == 0) {
                db.execSQL("""
                    INSERT INTO $TABLE_ANIMALES (
                        $COL_ANIMAL_NUMERO, $COL_ANIMAL_SEXO, $COL_ANIMAL_TRAZABILIDAD, 
                        $COL_ANIMAL_CHIP, $COL_ANIMAL_FECHA_NACIMIENTO, $COL_ANIMAL_RAZA, 
                        $COL_ANIMAL_PROPOSITO, $COL_ANIMAL_MANGA, $COL_ANIMAL_PESO, 
                        $COL_ANIMAL_PADRE, $COL_ANIMAL_MADRE, $COL_ANIMAL_NOTAS, $COL_ANIMAL_IMAGEN, $COL_ANIMAL_LOTE
                    ) VALUES 
                    ('1001', 'Macho', '740000000000001', 'CHIP1001', '15/05/2024', 'Brahman', 'Carne', 'Ceba', '420', 'Padre 1', 'Madre 1', 'Macho Brahman para ceba', '', 'Ceba'),
                    ('1002', 'Hembra', '740000000000002', 'CHIP1002', '10/01/2024', 'Holando', 'Leche', 'Ordeño', '380', 'Padre 2', 'Madre 2', 'Vaca lechera alta producción', '', 'Ordeño'),
                    ('1003', 'Hembra', '740000000000003', 'CHIP1003', '20/02/2024', 'Jersey', 'Leche', 'Secado', '350', 'Padre 3', 'Madre 3', 'Novilla lechera primeriza', '', 'Secado'),
                    ('1004', 'Hembra', '740000000000004', 'CHIP1004', '05/03/2024', 'Gyr', 'Doble propósito', 'Ceba', '390', 'Padre 4', 'Madre 4', 'Doble propósito adaptada', '', 'Ceba')
                """)
            }

            // 2.5 Seed Lotes
            val loteCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_LOTES", null)
            var loteCount = 0
            if (loteCursor.moveToFirst()) {
                loteCount = loteCursor.getInt(0)
            }
            loteCursor.close()

            if (loteCount == 0) {
                db.execSQL("""
                    INSERT INTO $TABLE_LOTES ($COL_LOTE_NOMBRE) VALUES 
                    ('Secado'),
                    ('Ordeño'),
                    ('Ceba')
                """)
            }

            // 3. Seed Leche Records
            val lecheCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_LECHE_RECORDS", null)
            var lecheCount = 0
            if (lecheCursor.moveToFirst()) {
                lecheCount = lecheCursor.getInt(0)
            }
            lecheCursor.close()

            if (lecheCount == 0) {
                db.execSQL("""
                    INSERT INTO $TABLE_LECHE_RECORDS (
                        $COL_LECHE_NUMERO, $COL_LECHE_FECHA, $COL_LECHE_TURNO, $COL_LECHE_LITROS, 
                        $COL_LECHE_FECHA_PARTO, $COL_LECHE_LACTANCIAS, $COL_LECHE_DEL, $COL_LECHE_PROMEDIO, $COL_LECHE_ACTIVO
                    ) VALUES 
                    ('1002', '12/07/2026', 'Mañana', 18.5, '10/01/2024', 1, 915, 18.5, 1),
                    ('1003', '12/07/2026', 'Mañana', 15.0, '20/02/2024', 1, 874, 15.0, 1)
                """)
            }

            // 4. Seed Carne Records
            val carneCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CARNE_RECORDS", null)
            var carneCount = 0
            if (carneCursor.moveToFirst()) {
                carneCount = carneCursor.getInt(0)
            }
            carneCursor.close()

            if (carneCount == 0) {
                db.execSQL("""
                    INSERT INTO $TABLE_CARNE_RECORDS (
                        $COL_CARNE_NUMERO, $COL_CARNE_RAZA, $COL_CARNE_FECHA_ACTUAL, $COL_CARNE_PESO_ACTUAL, 
                        $COL_CARNE_FECHA_ANTERIOR, $COL_CARNE_PESO_ANTERIOR, $COL_CARNE_PESO_ENTRADA, 
                        $COL_CARNE_GANANCIA, $COL_CARNE_DIAS, $COL_CARNE_GDP, $COL_CARNE_SALUD, $COL_CARNE_ACTIVO
                    ) VALUES (
                        '1001', 'Brahman', '12/07/2026', 420.0, '12/06/2026', 390.0, 200.0, 220.0, 30, 1.0, 'Bueno', 1
                    )
                """)
            }

            // 5. Seed Inventario Items (Medicinas, Alimentos)
            val invCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_INVENTARIO_ITEMS", null)
            var invCount = 0
            if (invCursor.moveToFirst()) {
                invCount = invCursor.getInt(0)
            }
            invCursor.close()

            if (invCount == 0) {
                db.execSQL("""
                    INSERT INTO $TABLE_INVENTARIO_ITEMS (
                        $COL_INV_ID, $COL_INV_NOMBRE, $COL_INV_TIPO, $COL_INV_TIPO_OTRO, 
                        $COL_INV_FOTO, $COL_INV_STOCK, $COL_INV_LIMITE, $COL_INV_UNIDAD, 
                        $COL_INV_COSTO, $COL_INV_PRECIO, $COL_INV_FECHA
                    ) VALUES 
                    ('inv_001', 'Ivermectina 1%', 'Medicamento', '', '', 15.0, 5.0, 'Frasco', 25.0, 30.0, '12/07/2026 10:00'),
                    ('inv_002', 'Penicilina G', 'Medicamento', '', '', 3.0, 5.0, 'Frasco', 15.0, 20.0, '12/07/2026 10:05'),
                    ('inv_003', 'Concentrado Ordeño', 'Alimento', '', '', 50.0, 10.0, 'Saco', 18.0, 22.0, '12/07/2026 10:10')
                """)
            }

            // 6. Seed Registros Sanitarios (Control Sanitario)
            val sanCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_SANITARIA", null)
            var sanCount = 0
            if (sanCursor.moveToFirst()) {
                sanCount = sanCursor.getInt(0)
            }
            sanCursor.close()

            if (sanCount == 0) {
                db.execSQL("""
                    INSERT INTO $TABLE_SANITARIA (
                        $COL_SAN_ID, $COL_SAN_IDENTIFICADOR, $COL_SAN_ALCANCE, $COL_SAN_CATEGORIA, 
                        $COL_SAN_DETALLE, $COL_SAN_PRODUCTO, $COL_SAN_DOSIS, $COL_SAN_FECHA, 
                        $COL_SAN_PROXIMA_DOSIS, $COL_SAN_VETERINARIO, $COL_SAN_NOTAS, $COL_SAN_ESTADO, $COL_SAN_GRUPO_ID
                    ) VALUES (
                        'san_001', '1002', 'Individual', 'Desparasitante', 'Desparasitación de rutina', 'Ivermectina 1%', '10 ml', '10/07/2026', '10/10/2026', 'Dr. Carlos Gómez', 'Ninguna', 'Completado', ''
                    )
                """)
            }

            // 7. Seed Transacciones (Contabilidad)
            val transCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_TRANSACCIONES", null)
            var transCount = 0
            if (transCursor.moveToFirst()) {
                transCount = transCursor.getInt(0)
            }
            transCursor.close()

            if (transCount == 0) {
                db.execSQL("""
                    INSERT INTO $TABLE_TRANSACCIONES (
                        $COL_TRANS_ID, $COL_TRANS_TIPO, $COL_TRANS_PROD_ID, $COL_TRANS_PROD_NOMBRE, 
                        $COL_TRANS_CANTIDAD, $COL_TRANS_PRECIO_UNIT, $COL_TRANS_COSTO_UNIT, $COL_TRANS_FECHA, $COL_TRANS_DETALLES
                    ) VALUES 
                    ('trans_001', 'Egreso', 'inv_001', 'Ivermectina 1%', 5.0, 0.0, 25.0, '12/07/2026', 'Compra de medicamentos para inventario'),
                    ('trans_002', 'Ingreso', 'leche_1002', 'Venta de Leche - 1002', 18.5, 0.60, 0.0, '12/07/2026', 'Venta de producción diaria')
                """)
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }
}
