package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.AmanSantapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SurveillanceFilterState(
    val searchQuery: String = "",
    val statusFilter: String = "Semua",
    val locationFilter: String = "Semua",
    val categoryFilter: String = "Semua"
)

class AmanSantapViewModel(private val repository: AmanSantapRepository) : ViewModel() {

    // Current Active Role & User
    private val _currentRole = MutableStateFlow(UserRole.MASYARAKAT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentOfficerName = MutableStateFlow("dr. Siti Rahmawati, M.Epid")
    val currentOfficerName: StateFlow<String> = _currentOfficerName.asStateFlow()

    // Data streams
    val reports: StateFlow<List<FoodReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<AlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val locations: StateFlow<List<FoodLocationEntity>> = repository.allLocations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val educations: StateFlow<List<EducationalContentEntity>> = repository.allEducations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activityLogs: StateFlow<List<ActivityLogEntity>> = repository.allActivityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val detectionConfig: StateFlow<DetectionRuleConfigEntity?> = repository.detectionConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val checklist: StateFlow<List<MerchantChecklistEntity>> = repository.allChecklist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Citizen report tracking
    private val _trackedReport = MutableStateFlow<FoodReportEntity?>(null)
    val trackedReport: StateFlow<FoodReportEntity?> = _trackedReport.asStateFlow()

    private val _trackingError = MutableStateFlow<String?>(null)
    val trackingError: StateFlow<String?> = _trackingError.asStateFlow()

    // Surveillance Filters
    private val _filters = MutableStateFlow(SurveillanceFilterState())
    val filters: StateFlow<SurveillanceFilterState> = _filters.asStateFlow()

    // SnackBar message channel
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun submitReport(
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
        consentAgreed: Boolean,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val code = repository.submitReport(
                    foodName = foodName,
                    foodCategory = foodCategory,
                    locationName = locationName,
                    locationAddress = locationAddress,
                    consumptionDate = consumptionDate,
                    consumptionTime = consumptionTime,
                    symptomOnsetDate = symptomOnsetDate,
                    symptomOnsetTime = symptomOnsetTime,
                    symptoms = symptoms,
                    severity = severity,
                    medicalAttention = medicalAttention,
                    additionalNotes = additionalNotes,
                    isAnonymous = isAnonymous,
                    reporterName = reporterName,
                    reporterPhone = reporterPhone,
                    consentAgreed = consentAgreed
                )
                _userMessage.value = "Laporan berhasil dikirim dengan kode: $code"
                onSuccess(code)
            } catch (e: Exception) {
                _userMessage.value = "Gagal mengirim laporan: ${e.message}"
            }
        }
    }

    fun searchReportByCode(code: String) {
        viewModelScope.launch {
            _trackingError.value = null
            val result = repository.getReportByCode(code)
            if (result != null) {
                _trackedReport.value = result
            } else {
                _trackedReport.value = null
                _trackingError.value = "Nomor laporan '$code' tidak ditemukan. Pastikan format benar (contoh: AS-20261024-001)."
            }
        }
    }

    fun clearTrackedReport() {
        _trackedReport.value = null
        _trackingError.value = null
    }

    fun updateFilters(newFilters: SurveillanceFilterState) {
        _filters.value = newFilters
    }

    fun reviewReport(reportId: Long, newStatus: ReportStatus, notes: String) {
        viewModelScope.launch {
            repository.reviewReport(
                reportId = reportId,
                reviewerName = _currentOfficerName.value,
                newStatus = newStatus,
                notes = notes
            )
            _userMessage.value = "Status laporan berhasil diperbarui menjadi ${newStatus.label}."
        }
    }

    fun updateAlertStatus(alertId: Long, newStatus: AlertStatus, notes: String) {
        viewModelScope.launch {
            repository.updateAlertStatus(
                alertId = alertId,
                reviewerName = _currentOfficerName.value,
                newStatus = newStatus,
                notes = notes
            )
            _userMessage.value = "Peringatan berhasil diperbarui ke ${newStatus.label}."
        }
    }

    fun updateDetectionRules(minThreshold: Int, windowHours: Int) {
        viewModelScope.launch {
            val current = detectionConfig.value ?: DetectionRuleConfigEntity()
            val updated = current.copy(
                minReportThreshold = minThreshold,
                timeWindowHours = windowHours
            )
            repository.updateDetectionConfig(updated, "Admin Dinkes")
            _userMessage.value = "Parameter deteksi pola berhasil diperbarui (Ambang: $minThreshold lap, Jendela: $windowHours jam)."
        }
    }

    fun triggerPatternAnalysis() {
        viewModelScope.launch {
            val newAlerts = repository.runPatternDetection()
            if (newAlerts > 0) {
                _userMessage.value = "Analisis pola selesai: Ditemukan $newAlerts klaster dugaan baru!"
            } else {
                _userMessage.value = "Analisis pola selesai: Tidak ada klaster baru yang melebihi ambang batas."
            }
        }
    }

    fun toggleChecklistItem(item: MerchantChecklistEntity) {
        viewModelScope.launch {
            repository.toggleChecklistItem(item)
        }
    }

    fun resetMerchantChecklist() {
        viewModelScope.launch {
            repository.resetMerchantChecklist()
            _userMessage.value = "Checklist kebersihan harian berhasil direset."
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetToSimulationData()
            _userMessage.value = "Data simulasi dan edukasi berhasil disetel ulang."
        }
    }
}

class AmanSantapViewModelFactory(
    private val repository: AmanSantapRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AmanSantapViewModel::class.java)) {
            return AmanSantapViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
