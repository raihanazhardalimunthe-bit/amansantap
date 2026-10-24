package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val label: String) {
    MASYARAKAT("Masyarakat"),
    PETUGAS("Petugas Kesehatan"),
    ADMIN("Admin Sistem"),
    PEDAGANG("Pedagang Makanan")
}

enum class ReportStatus(val label: String, val badgeColor: Long) {
    BARU("Baru", 0xFF2563EB),
    PERLU_DITINJAU("Perlu Ditinjau", 0xFFD97706),
    SEDANG_DITINDAKLANJUTI("Sedang Ditindaklanjuti", 0xFF7C3AED),
    SELESAI("Selesai Ditinjau", 0xFF16A34A)
}

enum class AlertStatus(val label: String) {
    PERLU_DITINJAU("Perlu Ditinjau"),
    SEDANG_DITINDAKLANJUTI("Sedang Ditindaklanjuti"),
    SELESAI("Selesai Ditinjau")
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val role: String,
    val institution: String
)

@Entity(tableName = "food_locations")
data class FoodLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val category: String, // Kantin Kampus, Restoran, Warung Makan, Kaki Lima, Katering
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

@Entity(tableName = "food_reports")
data class FoodReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportCode: String,
    val foodName: String,
    val foodCategory: String,
    val locationName: String,
    val locationAddress: String,
    val consumptionDate: String,
    val consumptionTime: String,
    val symptomOnsetDate: String,
    val symptomOnsetTime: String,
    val symptoms: String, // Separated with comma
    val severity: String, // Ringan, Sedang, Berat
    val medicalAttention: Boolean,
    val additionalNotes: String,
    val isAnonymous: Boolean,
    val reporterName: String?,
    val reporterPhone: String?,
    val consentAgreed: Boolean,
    val status: String = ReportStatus.BARU.name,
    val isSimulationData: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertCode: String,
    val locationName: String,
    val reportCount: Int,
    val dominantSymptoms: String,
    val reportedFoods: String,
    val reason: String,
    val timeWindowRange: String,
    val status: String = AlertStatus.PERLU_DITINJAU.name,
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null,
    val reviewerNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "report_reviews")
data class ReportReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val reviewerName: String,
    val previousStatus: String,
    val newStatus: String,
    val notes: String,
    val reviewTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "educational_contents")
data class EducationalContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Kebersihan Makanan, Penyimpanan Makanan, Tips Konsumen, Edukasi Pedagang, Pertolongan Kesehatan
    val summary: String,
    val content: String,
    val isEmergencyTriage: Boolean = false,
    val readTimeMinutes: Int = 3
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val details: String,
    val actorName: String,
    val actorRole: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "detection_rule_config")
data class DetectionRuleConfigEntity(
    @PrimaryKey val id: Long = 1,
    val minReportThreshold: Int = 3,
    val timeWindowHours: Int = 24,
    val enableLocationMatching: Boolean = true,
    val enableSymptomMatching: Boolean = true
)

@Entity(tableName = "merchant_checklists")
data class MerchantChecklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val description: String,
    val isCompleted: Boolean = false
)
