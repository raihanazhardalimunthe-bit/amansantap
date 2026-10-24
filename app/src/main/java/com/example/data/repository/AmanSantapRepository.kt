package com.example.data.repository

import com.example.data.database.AmanSantapDatabase
import com.example.data.database.DatabaseSeeder
import com.example.data.model.ActivityLogEntity
import com.example.data.model.AlertEntity
import com.example.data.model.AlertStatus
import com.example.data.model.DetectionRuleConfigEntity
import com.example.data.model.EducationalContentEntity
import com.example.data.model.FoodLocationEntity
import com.example.data.model.FoodReportEntity
import com.example.data.model.MerchantChecklistEntity
import com.example.data.model.ReportReviewEntity
import com.example.data.model.ReportStatus
import com.example.data.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AmanSantapRepository(private val database: AmanSantapDatabase) {

    // Flows
    val allReports: Flow<List<FoodReportEntity>> = database.reportDao().getAllReports()
    val allAlerts: Flow<List<AlertEntity>> = database.alertDao().getAllAlerts()
    val allLocations: Flow<List<FoodLocationEntity>> = database.locationDao().getAllLocations()
    val allEducations: Flow<List<EducationalContentEntity>> = database.educationDao().getAllContents()
    val allActivityLogs: Flow<List<ActivityLogEntity>> = database.activityDao().getAllLogs()
    val detectionConfig: Flow<DetectionRuleConfigEntity?> = database.ruleConfigDao().getConfigFlow()
    val allChecklist: Flow<List<MerchantChecklistEntity>> = database.checklistDao().getAllChecklist()
    val allUsers: Flow<List<UserEntity>> = database.userDao().getAllUsers()

    // Submit Report with Unique Code
    suspend fun submitReport(
        foodName: String,
        foodCategory: String,
        locationName: String,
        locationAddress: String,
        consumptionDate: String,
        consumptionTime: String,
        symptomOnsetDate: String,
        symptomOnsetTime: String,
        symptoms: List<String>,
        severity: String,
        medicalAttention: Boolean,
        additionalNotes: String,
        isAnonymous: Boolean,
        reporterName: String?,
        reporterPhone: String?,
        consentAgreed: Boolean
    ): String = withContext(Dispatchers.IO) {
        val dateCode = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val randomSuffix = (1000..9999).random()
        val reportCode = "AS-$dateCode-$randomSuffix"

        val entity = FoodReportEntity(
            reportCode = reportCode,
            foodName = foodName.trim(),
            foodCategory = foodCategory,
            locationName = locationName.trim(),
            locationAddress = locationAddress.trim(),
            consumptionDate = consumptionDate,
            consumptionTime = consumptionTime,
            symptomOnsetDate = symptomOnsetDate,
            symptomOnsetTime = symptomOnsetTime,
            symptoms = symptoms.joinToString(", "),
            severity = severity,
            medicalAttention = medicalAttention,
            additionalNotes = additionalNotes.trim(),
            isAnonymous = isAnonymous,
            reporterName = if (isAnonymous) null else reporterName?.trim(),
            reporterPhone = if (isAnonymous) null else reporterPhone?.trim(),
            consentAgreed = consentAgreed,
            status = ReportStatus.BARU.name,
            isSimulationData = false,
            createdAt = System.currentTimeMillis()
        )

        val newId = database.reportDao().insertReport(entity)

        // Log Activity
        database.activityDao().insertLog(
            ActivityLogEntity(
                action = "Laporan Baru Dikirim",
                details = "Laporan $reportCode dicatat untuk lokasi '${locationName.trim()}'.",
                actorName = if (isAnonymous) "Masyarakat (Anonim)" else (reporterName ?: "Pelapor"),
                actorRole = "Masyarakat"
            )
        )

        // Run pattern detection
        runPatternDetectionInternal()

        reportCode
    }

    // Pattern Detection Algorithm
    suspend fun runPatternDetection(): Int = withContext(Dispatchers.IO) {
        runPatternDetectionInternal()
    }

    private suspend fun runPatternDetectionInternal(): Int {
        val reports = database.reportDao().getAllReports().firstOrNull() ?: return 0
        val config = database.ruleConfigDao().getConfig() ?: DetectionRuleConfigEntity()
        val existingAlerts = database.alertDao().getAllAlerts().firstOrNull() ?: emptyList()

        var newAlertsCreated = 0

        // Group by location (case-insensitive)
        val groupedByLocation = reports.groupBy { it.locationName.trim().lowercase(Locale.getDefault()) }

        for ((locKey, locReports) in groupedByLocation) {
            val originalLocName = locReports.first().locationName

            // 1. Minimum report count criterion
            if (locReports.size >= config.minReportThreshold) {
                // Check time window between earliest and latest reports
                val timeDifferenceHours = (locReports.maxOf { it.createdAt } - locReports.minOf { it.createdAt }) / (1000 * 3600)
                
                // If within config time window
                if (timeDifferenceHours <= config.timeWindowHours || locReports.size >= config.minReportThreshold) {
                    // Extract dominant symptoms
                    val symptomFrequency = mutableMapOf<String, Int>()
                    locReports.forEach { rep ->
                        rep.symptoms.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { sym ->
                            symptomFrequency[sym] = (symptomFrequency[sym] ?: 0) + 1
                        }
                    }
                    val dominantSymptoms = symptomFrequency.entries
                        .sortedByDescending { it.value }
                        .take(3)
                        .joinToString(", ") { "${it.key} (${it.value} lap.)" }

                    val reportedFoods = locReports.map { it.foodName }.distinct().take(3).joinToString(", ")

                    // Check if open alert exists for this location
                    val existingAlert = existingAlerts.find { 
                        it.locationName.trim().equals(originalLocName.trim(), ignoreCase = true) &&
                        it.status != AlertStatus.SELESAI.name
                    }

                    val reasonText = "Terdeteksi ${locReports.size} laporan dari lokasi '$originalLocName' dalam rentang ${config.timeWindowHours} jam dengan kemiripan gejala ($dominantSymptoms). Indikasi dugaan kejadian pangan — perlu ditinjau petugas."

                    if (existingAlert != null) {
                        // Update existing alert with latest counts
                        val updatedAlert = existingAlert.copy(
                            reportCount = locReports.size,
                            dominantSymptoms = dominantSymptoms,
                            reportedFoods = reportedFoods,
                            reason = reasonText
                        )
                        database.alertDao().updateAlert(updatedAlert)
                    } else {
                        // Create new alert
                        val alertCode = "ALT-" + SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) + "-" + (100..999).random()
                        val newAlert = AlertEntity(
                            alertCode = alertCode,
                            locationName = originalLocName,
                            reportCount = locReports.size,
                            dominantSymptoms = dominantSymptoms,
                            reportedFoods = reportedFoods,
                            reason = reasonText,
                            timeWindowRange = "Rentang < ${config.timeWindowHours} Jam",
                            status = AlertStatus.PERLU_DITINJAU.name,
                            createdAt = System.currentTimeMillis()
                        )
                        database.alertDao().insertAlert(newAlert)
                        newAlertsCreated++

                        // Update status of BARU reports to PERLU_DITINJAU
                        locReports.filter { it.status == ReportStatus.BARU.name }.forEach { rep ->
                            database.reportDao().updateStatus(rep.id, ReportStatus.PERLU_DITINJAU.name)
                        }

                        // Log Activity
                        database.activityDao().insertLog(
                            ActivityLogEntity(
                                action = "Peringatan Dini Terpicu",
                                details = "Sistem menghasilkan $alertCode untuk lokasi '$originalLocName' (${locReports.size} laporan terhubung).",
                                actorName = "Sistem Deteksi Otomatis",
                                actorRole = "Algoritma"
                            )
                        )
                    }
                }
            }
        }
        return newAlertsCreated
    }

    // Health Officer Review Workflow
    suspend fun reviewReport(
        reportId: Long,
        reviewerName: String,
        newStatus: ReportStatus,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val report = database.reportDao().getReportById(reportId) ?: return@withContext
        val oldStatus = report.status

        database.reportDao().updateStatus(reportId, newStatus.name)

        database.reviewDao().insertReview(
            ReportReviewEntity(
                reportId = reportId,
                reviewerName = reviewerName,
                previousStatus = oldStatus,
                newStatus = newStatus.name,
                notes = notes,
                reviewTimestamp = System.currentTimeMillis()
            )
        )

        database.activityDao().insertLog(
            ActivityLogEntity(
                action = "Peninjauan Laporan",
                details = "Laporan ${report.reportCode} diubah dari $oldStatus ke ${newStatus.label} oleh $reviewerName.",
                actorName = reviewerName,
                actorRole = "Petugas Kesehatan"
            )
        )
    }

    // Update Alert Status
    suspend fun updateAlertStatus(
        alertId: Long,
        reviewerName: String,
        newStatus: AlertStatus,
        notes: String
    ) = withContext(Dispatchers.IO) {
        val alert = database.alertDao().getAlertById(alertId) ?: return@withContext
        val updated = alert.copy(
            status = newStatus.name,
            reviewedBy = reviewerName,
            reviewedAt = System.currentTimeMillis(),
            reviewerNotes = notes
        )
        database.alertDao().updateAlert(updated)

        database.activityDao().insertLog(
            ActivityLogEntity(
                action = "Tindak Lanjut Peringatan",
                details = "Peringatan ${alert.alertCode} (${alert.locationName}) diperbarui menjadi ${newStatus.label}.",
                actorName = reviewerName,
                actorRole = "Petugas Kesehatan"
            )
        )
    }

    // Admin Config
    suspend fun updateDetectionConfig(config: DetectionRuleConfigEntity, adminName: String) = withContext(Dispatchers.IO) {
        database.ruleConfigDao().setConfig(config)
        database.activityDao().insertLog(
            ActivityLogEntity(
                action = "Pembaruan Kriteria Deteksi",
                details = "Ambang batas laporan: ${config.minReportThreshold}, Jendela waktu: ${config.timeWindowHours} jam.",
                actorName = adminName,
                actorRole = "Admin"
            )
        )
        // Re-evaluate with new rules
        runPatternDetectionInternal()
    }

    // Merchant Checklist Toggle
    suspend fun toggleChecklistItem(item: MerchantChecklistEntity) = withContext(Dispatchers.IO) {
        database.checklistDao().updateItem(item.copy(isCompleted = !item.isCompleted))
    }

    suspend fun resetMerchantChecklist() = withContext(Dispatchers.IO) {
        database.checklistDao().resetAll()
    }

    // Reset Simulation Data
    suspend fun resetToSimulationData() = withContext(Dispatchers.IO) {
        database.reportDao().deleteSimulationReports()
        database.alertDao().clearAllAlerts()
        DatabaseSeeder.seedInitialData(database)
        database.activityDao().insertLog(
            ActivityLogEntity(
                action = "Reset Data Simulasi",
                details = "Database disetel ulang ke kondisi awal prototipe surveilans.",
                actorName = "Admin Sistem",
                actorRole = "Admin"
            )
        )
    }

    suspend fun getReportByCode(code: String): FoodReportEntity? = withContext(Dispatchers.IO) {
        database.reportDao().getReportByCode(code.trim())
    }
}
