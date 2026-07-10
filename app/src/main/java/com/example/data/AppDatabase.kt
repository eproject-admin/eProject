package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        ContractEntity::class,
        OpnameEntity::class,
        RoadNameEntity::class,
        CustomContractItemEntity::class,
        CustomUnitEntity::class,
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "eproject_database"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDefaultData(database.appDao())
                    }
                }
            }
        }

        suspend fun seedDefaultData(dao: AppDao) {
            // Seed default Super Admin
            val defaultSuperAdmin = AccountEntity(
                email = "eproject.admin@gmail.com",
                passwordHash = "eProject@2026",
                role = "super_admin",
                isApproved = true,
                userNumber = "ADM-000001",
                expiryDateString = "Unlimited",
                packageName = "BPJN Kalimantan Utara Admin"
            )
            dao.insertAccount(defaultSuperAdmin)

            // Seed default units
            val defaultUnits = listOf("m3", "ton", "m", "m2", "Bulan", "Ls", "buah")
            for (unit in defaultUnits) {
                dao.insertCustomUnit(CustomUnitEntity(name = unit))
            }

            // Seed Bina Marga items
            val defaultItems = listOf(
                // Divisi 1
                CustomContractItemEntity(divisionCode = "Divisi 1 : Umum dan Penerapan SMKK", codeAndName = "1.2 - Mobilisasi dan Demobilisasi", defaultUnit = "Ls"),
                CustomContractItemEntity(divisionCode = "Divisi 1 : Umum dan Penerapan SMKK", codeAndName = "1.2.(1) - Sewa Lahan", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 1 : Umum dan Penerapan SMKK", codeAndName = "1.2.(3) - Penyediaan dan Pemeliharaan Basecamp", defaultUnit = "Bulan"),
                CustomContractItemEntity(divisionCode = "Divisi 1 : Umum dan Penerapan SMKK", codeAndName = "1.20.(1) - Pengeboran, termasuk SPT dan Laporan", defaultUnit = "m"),

                // Divisi 2
                CustomContractItemEntity(divisionCode = "Divisi 2 : Drainase", codeAndName = "2.1.(1) - Galian untuk Selokan Drainase dan Saluran Air", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 2 : Drainase", codeAndName = "2.3.(14a) - Gorong-gorong Kotak Beton Bertulang, ukuran dalam 100 cm x 100 cm", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 2 : Drainase", codeAndName = "2.3.(17) - Gorong-gorong Kotak Beton Bertulang, ukuran dalam 150 cm x 150 cm", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 2 : Drainase", codeAndName = "2.3.(20) - Gorong-gorong Kotak Beton Bertulang, ukuran dalam 200 cm x 200 cm", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 2 : Drainase", codeAndName = "2.3.(9) - Gorong-gorong Pipa Baja Bergelombang", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 2 : Drainase", codeAndName = "2.2.(1) - Pasangan Batu dengan Mortar", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 2 : Drainase", codeAndName = "2.3.(22a) - Saluran U Pracetak Tipe DS 2", defaultUnit = "buah"),
                CustomContractItemEntity(divisionCode = "Divisi 2 : Drainase", codeAndName = "2.3.(22b) - Saluran U Pracetak Tipe DS 2A (dengan tutup)", defaultUnit = "buah"),
                CustomContractItemEntity(divisionCode = "Divisi 2 : Drainase", codeAndName = "2.3.(23a) - Saluran U Pracetak Tipe DS 3", defaultUnit = "buah"),
                CustomContractItemEntity(divisionCode = "Divisi 2 : Drainase", codeAndName = "2.3.(23b) - Saluran U Pracetak Tipe DS 3A (dengan tutup)", defaultUnit = "buah"),

                // Divisi 3
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.1.(1) - Galian Biasa", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.1.(2) - Galian Batu Lunak", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.1.(3) - Galian Batu", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.1.(10a) - Galian Perkerasan Beraspal dengan Cold Milling Machine", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.1.(10b) - Galian Perkerasan Beraspal tanpa Cold Milling Machine", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.1.(11) - Galian Perkerasan Berbutir", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.1.(12) - Galian Perkerasan Beton", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.1.(4) - Galian Struktur dengan Kedalaman 0 - 2 M", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.1.(5) - Galian Struktur dengan Kedalaman 2 - 4 M", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.1.(6) - Galian Struktur dengan Kedalaman 4 - 6 M", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.5.(2b) - Geotekstil Separator Kelas 1", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.5.(2c) - Geotekstil Separator Kelas 2", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.4.(1) - Pembersihan dan Pengupasan Lahan", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.4.(2) - Pemotongan Pohon Pilihan diameter >15 - 30 cm", defaultUnit = "buah"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.4.(3) - Pemotongan Pohon Pilihan diameter >30 - 50 cm", defaultUnit = "buah"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.2.(4) - Penimbunan Kembali Bahan Berbutir (Granular Backfill)", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.3.(1a) - Penyiapan Badan Jalan pada Galian", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.3.(2a) - Penyiapan Badan Jalan untuk Rekonstruksi Perkerasan Lama", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.2.(1d) - Timbunan Biasa Dari Hasil Galian", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.2.(1a) - Timbunan Biasa Dari Sumber Galian", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.2.(2c1) - Timbunan Pilihan Halus Dari Hasil Galian", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.2.(2a1) - Timbunan Pilihan Halus Dari Sumber Galian", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.2.(2c2) - Timbunan Pilihan Kasar Dari Hasil Galian", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 3 : Pekerjaan Tanah dan Geosintetik", codeAndName = "3.2.(2a2) - Timbunan Pilihan Kasar Dari Sumber Galian", defaultUnit = "m3"),

                // Divisi 4
                CustomContractItemEntity(divisionCode = "Divisi 4 : Pekerjaan Preventif", codeAndName = "4.4.(3) - Penghamparan Lapis Penutup Bubur Aspal Emulsi, Tipe 2, CSS-1h / SS-1h", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 4 : Pekerjaan Preventif", codeAndName = "4.4.(5) - Penghamparan Lapis Penutup Bubur Aspal Emulsi, Tipe 3, CSS-1h / SS-1h", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 4 : Pekerjaan Preventif", codeAndName = "4.7.(1) - Lapis Tipis Beton Aspal - A (LTBA-A) (Tumbukan 75 x 2)", defaultUnit = "ton"),

                // Divisi 5
                CustomContractItemEntity(divisionCode = "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen", codeAndName = "5.1.(1a) - Lapis Fondasi Agregat Kelas A", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen", codeAndName = "5.1.(2a) - Lapis Fondasi Agregat Kelas B", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen", codeAndName = "5.1.(3a) - Lapis Fondasi Agregat Kelas S", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen", codeAndName = "5.5.(1) - Lapis Fondasi Agregat Semen (Cement Treated Base, CTB)", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen", codeAndName = "5.3.(5) - Lapis Fondasi Bawah Beton Kurus", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen", codeAndName = "5.3.(1c) - Perkerasan Beton Semen Fast Track hingga 24 Jam", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen", codeAndName = "5.3.(1b) - Perkerasan Beton Semen Fast Track hingga 8 Jam", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen", codeAndName = "5.3.(1a1) - Perkerasan Beton Semen, fs = 4,5 MPa", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen", codeAndName = "5.4.(1a) - Stabilisasi Tanah Dasar dengan Semen", defaultUnit = "ton"),

                // Divisi 6
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.1.(2a) - Lapis Perekat - Aspal Cair/Emulsi", defaultUnit = "liter"),
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.1.(1) - Lapis Resap Pengikat - Aspal Cair/Emulsi", defaultUnit = "liter"),
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.3.(4a1) - Laston Lapis Aus (AC-WC) (Tumbukan 75x2)", defaultUnit = "ton"),
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.3.(4b1) - Laston Lapis Aus Modifikasi (AC-WC Mod)", defaultUnit = "ton"),
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.3.(6a1) - Laston Lapis Fondasi (AC-Base)", defaultUnit = "ton"),
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.3.(8) - Bahan Anti Pengelupasan", defaultUnit = "kg"),
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.3.(5a1) - Laston Lapis Antara (AC-BC)", defaultUnit = "ton"),
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.3.(5b1) - Laston Lapis Antara Modifikasi (AC-BC Mod)", defaultUnit = "ton"),
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.5.(1a) - Laston Lapis Aus Asbuton Butir (AC-WC Asbuton Butir)", defaultUnit = "ton"),
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.5.(2a) - Laston Lapis Antara Asbuton Butir (AC-BC Asbuton Butir)", defaultUnit = "ton"),
                CustomContractItemEntity(divisionCode = "Divisi 6 : Perkerasan Aspal", codeAndName = "6.6.(1) - CPHMA Kemasan", defaultUnit = "ton"),

                // Divisi 7
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.1.(2a2) - Beton struktur fc' 45 Mpa", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.1.(4a7) - Beton struktur fc' 35 MPa", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.1.(5a2) - Beton struktur fc' 30 MPa untuk Dinding Sayap Gorong-gorong Kotak", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.1.(5a4) - Beton struktur, fc'30 MPa", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.1.(5c1) - Beton struktur memadat sendiri, fc'30 MPa untuk Isian Tiang Pancang", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.1.(6a) - Beton struktur fc' 25 MPa", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.1.(7a) - Beton struktur fc' 20 MPa", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.1.(8) - Beton, fc'15 MPa", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.1.(10) - Beton, fc' 10 MPa", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.3.(1) - Baja Tulangan Polos BjTP 280", defaultUnit = "kg"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.3.(3a) - Baja Tulangan Sirip dengan Proteksi BjTS 420", defaultUnit = "kg"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.6.(11).400.400 - Tiang Pancang Beton Bertulang Pracetak Ukuran 400 mm x 400 mm, Penyediaan", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.6.(12).400.400 - Tiang Pancang Beton Bertulang Pracetak Ukuran 400 mm x 400 mm, Pemancangan", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.6.(18b).600 - Tiang Bor Beton, diameter 600 mm", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.6.(1a) - Fondasi Cerucuk, Penyediaan dan Pemancangan", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.7.(3) - Dinding Sumuran Silinder Terpasang, Diameter 350 cm", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.9.(1) - Pasangan Batu", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.10.(3a) - Bronjong dengan kawat yang dilapisi galvanis", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.11.(1a) - Sambungan Siar Muai Tipe Asphaltic Plug, Lebar 15 cm", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.13.(1) - Sandaran (Railing)", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.15.(1) - Pembongkaran Pasangan Batu", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 7 : Struktur", codeAndName = "7.16.(3b) - Pipa Drainase PVC Diameter 200 mm", defaultUnit = "m"),

                // Divisi 8
                CustomContractItemEntity(divisionCode = "Divisi 8 : Rehabilitasi Jembatan", codeAndName = "8.1.(1) - Cairan Perekat (Epoksi Resin)", defaultUnit = "kg"),
                CustomContractItemEntity(divisionCode = "Divisi 8 : Rehabilitasi Jembatan", codeAndName = "8.1.(2) - Bahan Penutup (Sealant)", defaultUnit = "kg"),
                CustomContractItemEntity(divisionCode = "Divisi 8 : Rehabilitasi Jembatan", codeAndName = "8.1.(3) - Tabung Penyuntik, penyediaan dan pemasangan", defaultUnit = "buah"),
                CustomContractItemEntity(divisionCode = "Divisi 8 : Rehabilitasi Jembatan", codeAndName = "8.2.(1) - Penambalan (Patching)", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 8 : Rehabilitasi Jembatan", codeAndName = "8.7.(1b) - Pengecatan struktur baja pada daerah kering tebal 240 mikron", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 8 : Rehabilitasi Jembatan", codeAndName = "8.11.(1) - Penggantian dan Perbaikan Sambungan Siar Muai Tipe Asphaltic Plug", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 8 : Rehabilitasi Jembatan", codeAndName = "8.11.(3) - Penggantian Karet Pengisi Sambungan Siar Muai Tipe Strip Seal", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 8 : Rehabilitasi Jembatan", codeAndName = "8.12.(2).500.500.100 - Penggantian Landasan Elastomer Karet Alam Berlapis Baja Ukuran 500 mm x 500 mm x 100 mm", defaultUnit = "buah"),

                // Divisi 9
                CustomContractItemEntity(divisionCode = "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "9.1.(1) - Mandor", defaultUnit = "Jam"),
                CustomContractItemEntity(divisionCode = "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "9.1.(2) - Pekerja Biasa", defaultUnit = "Jam"),
                CustomContractItemEntity(divisionCode = "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "9.1.(4a) - Dump Truck, kapasitas nominal 4 ton", defaultUnit = "Jam"),
                CustomContractItemEntity(divisionCode = "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "9.1.(8) - Motor Grader Min 100 PK", defaultUnit = "Jam"),
                CustomContractItemEntity(divisionCode = "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "9.1.(11) - Alat Penggali (Excavator) 80 - 140 PK", defaultUnit = "Jam"),
                CustomContractItemEntity(divisionCode = "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "9.1.(15) - Pemadat Bervibrasi 1.5 - 3.0 PK", defaultUnit = "Jam"),
                CustomContractItemEntity(divisionCode = "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "9.2.(1e) - Marka Jalan Termoplastik Glow in The Dark (Penerapan Umum)", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "9.2.(11a) - Patok Lalu Lintas Beton", defaultUnit = "buah"),
                CustomContractItemEntity(divisionCode = "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "9.2.(13a) - Patok Kilometer (Beton)", defaultUnit = "buah"),
                CustomContractItemEntity(divisionCode = "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "9.2.(13c) - Patok Hektometer", defaultUnit = "buah"),

                // Divisi 10
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(1) - Galian pada Saluran Air atau Lereng untuk Pemeliharaan", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(3) - Perbaikan Pasangan Batu dengan Mortar", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(4) - Perbaikan Lapis Fondasi Agregat Kelas A", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(6a) - Perbaikan Lapis Fondasi Agregat Kelas S", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.2.(7a) - Pembersihan Endapan pada Daerah Aliran Sungai dengan Menggunakan Alat Berat", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.2.(7b) - Pembersihan Endapan/Sampah pada Daerah Aliran Sungai dengan Cara Manual", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.2.(9a) - Perbaikan Parapet Jembatan", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.2.(11) - Pembersihan Pipa Cucuran/Pipa Penyalur", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(11a) - Perbaikan AC-WC", defaultUnit = "ton"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.2.(12a) - Pengecatan Pipa dan Tiang Sandaran", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.2.(12b) - Pengecatan Dinding Beton Sandaran", defaultUnit = "m2"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(21) - Perbaikan Pasangan Batu", defaultUnit = "m3"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(22) - Pengecatan Kereb pada Trotoar atau Median", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(24a) - Pengecatan Patok", defaultUnit = "buah"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(24b) - Pembersihan Patok", defaultUnit = "buah"), // Wait, user lists "10.1.(24b) - Pembersihan Patok"
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(25b) - Pembersihan Rambu", defaultUnit = "buah"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(26) - Pembersihan Drainase dan Saluran Samping", defaultUnit = "m"),
                CustomContractItemEntity(divisionCode = "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain", codeAndName = "10.1.(27) - Pengendalian Tanaman", defaultUnit = "m2")
            )

            for (item in defaultItems) {
                dao.insertCustomItem(item)
            }
        }
    }
}
