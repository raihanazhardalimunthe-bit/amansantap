package com.example.data.database

import com.example.data.model.ActivityLogEntity
import com.example.data.model.AlertEntity
import com.example.data.model.AlertStatus
import com.example.data.model.DetectionRuleConfigEntity
import com.example.data.model.EducationalContentEntity
import com.example.data.model.FoodLocationEntity
import com.example.data.model.FoodReportEntity
import com.example.data.model.MerchantChecklistEntity
import com.example.data.model.ReportStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole

object DatabaseSeeder {
    suspend fun seedInitialData(database: AmanSantapDatabase) {
        val now = System.currentTimeMillis()
        val oneHour = 3600 * 1000L
        val oneDay = 24 * oneHour

        // 1. Initial Users
        val users = listOf(
            UserEntity(name = "Masyarakat Umum", email = "warga@amansantap.id", role = UserRole.MASYARAKAT.name, institution = "Warga Mandiri"),
            UserEntity(name = "dr. Siti Rahmawati, M.Epid", email = "siti.surveilans@dinkes.go.id", role = UserRole.PETUGAS.name, institution = "Tim Surveilans Puskesmas Terpadu"),
            UserEntity(name = "Administrator Dinkes", email = "admin@amansantap.id", role = UserRole.ADMIN.name, institution = "Dinas Kesehatan Kota"),
            UserEntity(name = "Pak Bambang (Mitra Kantin)", email = "bambang.kantin@mitra.id", role = UserRole.PEDAGANG.name, institution = "Kantin Gedung A")
        )
        database.userDao().insertUsers(users)

        // 2. Locations
        val locations = listOf(
            FoodLocationEntity(name = "Kantin Gedung A Kampus Terpadu", address = "Jl. Pendidikan No. 45, Area Kampus", category = "Kantin Kampus", latitude = -6.2088, longitude = 106.8456),
            FoodLocationEntity(name = "Warung Makan Berkah Bu Siti", address = "Jl. Melati Raya No. 12", category = "Warung Makan", latitude = -6.2120, longitude = 106.8480),
            FoodLocationEntity(name = "Restoran Rasa Minang Jaya", address = "Jl. Sudirman No. 88", category = "Restoran", latitude = -6.2001, longitude = 106.8390),
            FoodLocationEntity(name = "Katering Sejahtera Utama", address = "Komp. Industri Blok D No. 5", category = "Katering", latitude = -6.1950, longitude = 106.8520),
            FoodLocationEntity(name = "Kedai Es & Jajanan Sore", address = "Depan Gerbang Timur Kampus", category = "Kaki Lima", latitude = -6.2095, longitude = 106.8465)
        )
        database.locationDao().insertLocations(locations)

        // 3. Simulation Reports (Labeled clearly as simulation data)
        val reports = listOf(
            // Cluster 1: Kantin Gedung A (3 reports -> triggers pattern alert!)
            FoodReportEntity(
                reportCode = "AS-20261024-001",
                foodName = "Ayam Suwir Sambal Matah & Nasi Uduk",
                foodCategory = "Olahan Daging/Ayam",
                locationName = "Kantin Gedung A Kampus Terpadu",
                locationAddress = "Jl. Pendidikan No. 45, Area Kampus",
                consumptionDate = "2026-10-23",
                consumptionTime = "12:30",
                symptomOnsetDate = "2026-10-23",
                symptomOnsetTime = "16:45",
                symptoms = "Mual, Sakit Perut, Diare",
                severity = "Sedang",
                medicalAttention = false,
                additionalNotes = "Mengeluh perut melilit 4 jam setelah makan siang bersama teman sekelas.",
                isAnonymous = true,
                reporterName = null,
                reporterPhone = null,
                consentAgreed = true,
                status = ReportStatus.PERLU_DITINJAU.name,
                isSimulationData = true,
                createdAt = now - (6 * oneHour)
            ),
            FoodReportEntity(
                reportCode = "AS-20261024-002",
                foodName = "Nasi Uduk Ayam Suwir Sambal Matah",
                foodCategory = "Olahan Daging/Ayam",
                locationName = "Kantin Gedung A Kampus Terpadu",
                locationAddress = "Jl. Pendidikan No. 45, Area Kampus",
                consumptionDate = "2026-10-23",
                consumptionTime = "13:00",
                symptomOnsetDate = "2026-10-23",
                symptomOnsetTime = "17:15",
                symptoms = "Mual, Muntah, Pusing",
                severity = "Sedang",
                medicalAttention = true,
                additionalNotes = "Muntah 2 kali, sempat minum obat warung dan beristirahat di klinik kampus.",
                isAnonymous = true,
                reporterName = null,
                reporterPhone = null,
                consentAgreed = true,
                status = ReportStatus.PERLU_DITINJAU.name,
                isSimulationData = true,
                createdAt = now - (5 * oneHour)
            ),
            FoodReportEntity(
                reportCode = "AS-20261024-003",
                foodName = "Paket Ayam Sambal Matah",
                foodCategory = "Olahan Daging/Ayam",
                locationName = "Kantin Gedung A Kampus Terpadu",
                locationAddress = "Jl. Pendidikan No. 45, Area Kampus",
                consumptionDate = "2026-10-23",
                consumptionTime = "13:15",
                symptomOnsetDate = "2026-10-23",
                symptomOnsetTime = "18:00",
                symptoms = "Diare, Sakit Perut, Demam",
                severity = "Sedang",
                medicalAttention = false,
                additionalNotes = "Perut keram hebat dan buang air cair lebih dari 4 kali.",
                isAnonymous = true,
                reporterName = null,
                reporterPhone = null,
                consentAgreed = true,
                status = ReportStatus.PERLU_DITINJAU.name,
                isSimulationData = true,
                createdAt = now - (4 * oneHour)
            ),
            // Standalone Report 2: Warung Makan Bu Siti
            FoodReportEntity(
                reportCode = "AS-20261023-010",
                foodName = "Soto Daging Santan & Perkedel",
                foodCategory = "Makanan Berat",
                locationName = "Warung Makan Berkah Bu Siti",
                locationAddress = "Jl. Melati Raya No. 12",
                consumptionDate = "2026-10-22",
                consumptionTime = "18:30",
                symptomOnsetDate = "2026-10-23",
                symptomOnsetTime = "02:00",
                symptoms = "Sakit Perut, Mual",
                severity = "Ringan",
                medicalAttention = false,
                additionalNotes = "Perut kembung dan mual saat dini hari, membaik setelah minum teh hangat.",
                isAnonymous = true,
                reporterName = null,
                reporterPhone = null,
                consentAgreed = true,
                status = ReportStatus.BARU.name,
                isSimulationData = true,
                createdAt = now - (18 * oneHour)
            ),
            // Standalone Report 3: Katering Sejahtera
            FoodReportEntity(
                reportCode = "AS-20261022-005",
                foodName = "Nasi Box Ikan Tongkol Balado",
                foodCategory = "Olahan Laut/Ikan",
                locationName = "Katering Sejahtera Utama",
                locationAddress = "Komp. Industri Blok D No. 5",
                consumptionDate = "2026-10-21",
                consumptionTime = "12:00",
                symptomOnsetDate = "2026-10-21",
                symptomOnsetTime = "15:00",
                symptoms = "Pusing, Mual",
                severity = "Ringan",
                medicalAttention = false,
                additionalNotes = "Laporan individu acara seminar.",
                isAnonymous = true,
                reporterName = null,
                reporterPhone = null,
                consentAgreed = true,
                status = ReportStatus.SELESAI.name,
                isSimulationData = true,
                createdAt = now - (2 * oneDay)
            )
        )
        database.reportDao().insertReports(reports)

        // 4. Initial Alert (Corresponding to Cluster 1)
        val initialAlert = AlertEntity(
            alertCode = "ALT-20261024-001",
            locationName = "Kantin Gedung A Kampus Terpadu",
            reportCount = 3,
            dominantSymptoms = "Mual, Sakit Perut, Diare",
            reportedFoods = "Ayam Suwir Sambal Matah",
            reason = "Ditemukan 3 laporan dengan kesamaan lokasi (Kantin Gedung A Kampus Terpadu), waktu konsumsi berdekatan (12:30 - 13:15), dan kemiripan gejala pencernaan (Mual/Diare).",
            timeWindowRange = "23 Okt 2026 (Rentang 6 jam)",
            status = AlertStatus.PERLU_DITINJAU.name,
            createdAt = now - (3 * oneHour)
        )
        database.alertDao().insertAlert(initialAlert)

        // 5. Educational Content
        val educations = listOf(
            EducationalContentEntity(
                title = "Tanda Bahaya: Kapan Harus Segera Mencari Pertolongan Medis?",
                category = "Pertolongan Kesehatan",
                summary = "Panduan mengenali tanda dehidrasi berat dan kondisi darurat setelah makan.",
                content = """
                    Jika Anda atau keluarga mengalami keluhan setelah makan, waspadai tanda-tanda bahaya berikut yang memerlukan pertolongan medis segera di IGD atau Puskesmas terdekat:
                    
                    1. Muntah terus-menerus dan tidak dapat menahan cairan apa pun.
                    2. Buang air besar cair lebih dari 6 kali dalam 24 jam atau disertai darah/lendir.
                    3. Tanda dehidrasi berat: mulut sangat kering, mata cekung, tidak buang air kecil lebih dari 8 jam, lemas ekstrem, atau pusing berputar saat berdiri.
                    4. Demam tinggi di atas 38.5°C yang tidak turun dengan antipiretik.
                    5. Kram atau nyeri perut hebat yang semakin memburuk.
                    6. Gejala neurologis: pandangan kabur, kelemahan otot wajah/tangan, atau kesulitan bernapas (waspada toksin botulisme).
                    
                    Tindakan Awal Mandiri di Rumah:
                    - Minum larutan oralit secara berkala (1 sachet dilarutkan dalam 200 ml air matang setiap setelah BAB cair).
                    - Jangan menahan diare dengan obat antidiare sembarangan tanpa petunjuk dokter, karena racun dalam usus perlu dikeluarkan.
                """.trimIndent(),
                isEmergencyTriage = true,
                readTimeMinutes = 4
            ),
            EducationalContentEntity(
                title = "5 Kunci Keamanan Pangan Menurut WHO",
                category = "Kebersihan Makanan",
                summary = "Prinsip dasar pencegahan kontaminasi pangan dari hulu ke hilir.",
                content = """
                    Organisasi Kesehatan Dunia (WHO) merumuskan 5 Kunci Keamanan Pangan untuk mencegah penyakit bawaan pangan:
                    
                    1. Jaga Kebersihan:
                    Cuci tangan dengan sabun dan air mengalir selama 20 detik sebelum memegang makanan dan sesudah dari toilet. Bersihkan seluruh permukaan meja dan peralatan masak secara rutin.
                    
                    2. Pisahkan Pangan Mentah dari Pangan Matang:
                    Gunakan talenan dan pisau berbeda untuk daging/ikan mentah dan makanan siap saji. Simpan dalam wadah tertutup agar tetesan cairan mentah tidak mengenai makanan lain.
                    
                    3. Masak dengan Benar:
                    Pastikan daging ayam, telur, dan olahan laut matang sempurna hingga bagian terdalam mencapai suhu minimal 75°C. Rebus sup hingga benar-benar mendidih.
                    
                    4. Jaga Pangan pada Suhu Aman:
                    Jangan biarkan makanan matang pada suhu ruang lebih dari 2 jam. Simpan makanan mudah basi di kulkas (di bawah 5°C) dan hangatkan kembali hingga mengepul (di atas 60°C) sebelum disajikan.
                    
                    5. Gunakan Air dan Bahan Baku yang Aman:
                    Gunakan air matang/layak minum, pilih bahan pangan segar yang tidak berlendir/berbau asam, dan selalu periksa tanggal kedaluwarsa.
                """.trimIndent(),
                readTimeMinutes = 5
            ),
            EducationalContentEntity(
                title = "Mengenal Zona Bahaya Suhu (Danger Zone 5°C - 60°C)",
                category = "Penyimpanan Makanan",
                summary = "Mengapa bakteri berkembang biak paling cepat pada suhu ruang?",
                content = """
                    Bakteri patogen seperti Salmonella, E. coli, Bacillus cereus, dan Staphylococcus aureus berkembang biak secara eksponensial pada rentang suhu antara 5°C hingga 60°C.
                    
                    Aturan 2 Jam / 4 Jam:
                    - Makanan matang yang berada di zona bahaya kurang dari 2 jam aman untuk dimakan atau langsung dimasukkan kulkas.
                    - Antara 2 hingga 4 jam, makanan harus segera dikonsumsi dan tidak boleh disimpan kembali.
                    - Lebih dari 4 jam pada suhu ruang, makanan WAJIB dibuang demi keselamatan kesehatan, meski aromanya tampak belum berubah.
                """.trimIndent(),
                readTimeMinutes = 3
            ),
            EducationalContentEntity(
                title = "Tips Konsumen: Memilih Tempat Makan yang Bersih & Higienis",
                category = "Tips Konsumen",
                summary = "Cara cerdas mengamati kebersihan warung, kantin, dan pedagang kaki lima.",
                content = """
                    Sebagai konsumen cerdas, perhatikan hal berikut saat membeli makanan:
                    
                    - Penjual tidak memegang uang dan makanan langsung dengan tangan yang sama tanpa mencuci tangan atau menggunakan penjepit makanan.
                    - Makanan lauk dipajang di etalase tertutup kaca dan terlindung dari lalat, debu, dan asap kendaraan.
                    - Area cuci piring menggunakan air bersih mengalir, bukan ember berisi air keruh berulang kali.
                    - Minuman menggunakan es batu dari air matang (berongga/kristal higienis).
                    - Tempat sampah memiliki penutup dan berada jauh dari meja saji.
                """.trimIndent(),
                readTimeMinutes = 3
            ),
            EducationalContentEntity(
                title = "Panduan Sanitasi dan Higiene untuk Pedagang Makanan",
                category = "Edukasi Pedagang",
                summary = "Standar operasional kebersihan untuk warung, kantin sekolah, dan katering.",
                content = """
                    Kepercayaan pelanggan adalah aset utama usaha makanan Anda. Terapkan standar berikut:
                    
                    1. Higiene Personal:
                    Gunakan celemek bersih, penutup rambut, dan potong kuku secara rapi. Jika sedang flu, batuk, atau memiliki luka terbuka di tangan, gunakan masker dan sarung tangan medis.
                    
                    2. Air Bersih:
                    Pastikan air untuk memasak dan mencuci piring berasal dari sumber air bersih yang tidak berbau, berwarna, atau berasa.
                    
                    3. Pengendalian Hama:
                    Pastikan celah dapur tertutup kawat kasa untuk mencegah tikus, kecoa, dan lalat masuk ke area penyimpanan bahan makanan.
                    
                    4. Penyimpanan Bertingkat:
                    Di dalam lemari pendingin, letakkan makanan matang di rak paling atas, dan bahan mentah di rak paling bawah untuk mencegah kontaminasi tetesan.
                """.trimIndent(),
                readTimeMinutes = 4
            )
        )
        database.educationDao().insertContents(educations)

        // 6. Merchant Checklist
        val checklistItems = listOf(
            MerchantChecklistEntity(title = "Cuci Tangan dengan Sabun", category = "Higiene Personal", description = "Mencuci tangan sebelum mengolah makanan dan setelah dari toilet.", isCompleted = true),
            MerchantChecklistEntity(title = "Gunakan Penutup Rambut & Celemek", category = "Higiene Personal", description = "Mengenakan pakaian kerja bersih untuk mencegah rambut jatuh ke makanan.", isCompleted = true),
            MerchantChecklistEntity(title = "Pemisahan Pisau & Talenan", category = "Pengolahan & Memasak", description = "Memisahkan talenan khusus daging/ayam mentah dari sayuran dan makanan matang.", isCompleted = true),
            MerchantChecklistEntity(title = "Pemasakan Daging Matang Sempurna", category = "Pengolahan & Memasak", description = "Memastikan tidak ada cairan merah/darah pada daging ayam atau olahan laut.", isCompleted = false),
            MerchantChecklistEntity(title = "Penyimpanan Suhu Dingin (< 5°C)", category = "Penyimpanan Bahan", description = "Bahan mudah basi segera dimasukkan lemari pendingin.", isCompleted = true),
            MerchantChecklistEntity(title = "Etalase Tertutup Rapat", category = "Fasilitas & Air", description = "Lauk terhindar dari lalat, debu, dan droplet pembeli.", isCompleted = false),
            MerchantChecklistEntity(title = "Air Bersih Mengalir untuk Mencuci", category = "Fasilitas & Air", description = "Peralatan makan dicuci dengan sabun dan dibilas air mengalir.", isCompleted = true),
            MerchantChecklistEntity(title = "Tempat Sampah Tertutup & Dikosongkan", category = "Fasilitas & Air", description = "Tempat sampah tidak menumpuk dan berjarak dari area saji.", isCompleted = false)
        )
        database.checklistDao().insertChecklist(checklistItems)

        // 7. Rule Config
        database.ruleConfigDao().setConfig(
            DetectionRuleConfigEntity(
                id = 1,
                minReportThreshold = 3,
                timeWindowHours = 48,
                enableLocationMatching = true,
                enableSymptomMatching = true
            )
        )

        // 8. Activity Log
        database.activityDao().insertLog(
            ActivityLogEntity(
                action = "Inisialisasi Sistem",
                details = "Sistem AmanSantap aktif dengan data simulasi awal untuk pemantauan keamanan pangan.",
                actorName = "Sistem Surveilans",
                actorRole = "System"
            )
        )
    }
}
