package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FoodReportEntity
import com.example.data.model.ReportStatus
import com.example.ui.AmanSantapViewModel
import com.example.ui.components.DistributionBar
import com.example.ui.components.PublicHealthDisclaimerCard
import com.example.ui.components.SeverityBadge
import com.example.ui.components.SimulationDataBanner
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SurveillanceDashboardScreen(
    viewModel: AmanSantapViewModel,
    onNavigateToAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val officerName by viewModel.currentOfficerName.collectAsStateWithLifecycle()

    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Metrics calculations from real database records
    val totalReports = reports.size
    val todayReports = reports.count { it.consumptionDate == todayDate || SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.createdAt)) == todayDate }
    val needsReviewReports = reports.count { it.status == ReportStatus.PERLU_DITINJAU.name || it.status == ReportStatus.BARU.name }
    val suspiciousPatternsCount = alerts.count { it.status != "SELESAI" }
    val followedUpReports = reports.count { it.status == ReportStatus.SEDANG_DITINDAKLANJUTI.name || it.status == ReportStatus.SELESAI.name }

    // Filter states
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Semua") }
    var selectedLocation by remember { mutableStateOf("Semua") }
    var selectedCategory by remember { mutableStateOf("Semua") }

    val distinctLocations = remember(reports) {
        listOf("Semua") + reports.map { it.locationName }.distinct().sorted()
    }
    val distinctCategories = remember(reports) {
        listOf("Semua") + reports.map { it.foodCategory }.distinct().sorted()
    }

    val symptomFreq = remember(reports) {
        val map = mutableMapOf<String, Int>()
        reports.forEach { rep ->
            rep.symptoms.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                map[it] = (map[it] ?: 0) + 1
            }
        }
        map.entries.sortedByDescending { it.value }
    }

    val locFreq = remember(reports) {
        reports.groupBy { it.locationName }.mapValues { it.value.size }.entries.sortedByDescending { it.value }
    }

    val catFreq = remember(reports) {
        reports.groupBy { it.foodCategory }.mapValues { it.value.size }.entries.sortedByDescending { it.value }
    }

    val filteredReports = remember(reports, searchQuery, selectedStatus, selectedLocation, selectedCategory) {
        reports.filter { report ->
            val matchQuery = searchQuery.isBlank() ||
                    report.foodName.contains(searchQuery, ignoreCase = true) ||
                    report.locationName.contains(searchQuery, ignoreCase = true) ||
                    report.reportCode.contains(searchQuery, ignoreCase = true) ||
                    report.symptoms.contains(searchQuery, ignoreCase = true)

            val matchStatus = selectedStatus == "Semua" || report.status == selectedStatus
            val matchLoc = selectedLocation == "Semua" || report.locationName == selectedLocation
            val matchCat = selectedCategory == "Semua" || report.foodCategory == selectedCategory

            matchQuery && matchStatus && matchLoc && matchCat
        }
    }

    // Detail & Action Dialog state
    var selectedReportForDetail by remember { mutableStateOf<FoodReportEntity?>(null) }
    var reviewStatusChoice by remember { mutableStateOf(ReportStatus.SEDANG_DITINDAKLANJUTI) }
    var reviewNotes by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ForestGreenPrimary,
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Dashboard Surveilans Keamanan Pangan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Petugas: $officerName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Button(
                        onClick = { viewModel.triggerPatternAnalysis() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ForestGreenDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("jalankan_deteksi_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analisis Ulang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        SimulationDataBanner()
        PublicHealthDisclaimerCard()

        // Alert Banner if suspicious patterns exist
        if (suspiciousPatternsCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAlerts() },
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Peringatan",
                        tint = WarningOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$suspiciousPatternsCount Pola Klaster Mencurigakan Terdeteksi",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF92400E)
                        )
                        Text(
                            text = "Terdapat kelompok laporan dengan kesamaan lokasi dan waktu. Ketuk untuk meninjau.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }
        }

        // A. RINGKASAN INDIKATOR
        Text(
            text = "A. Ringkasan Indikator Surveilans",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Total Laporan",
                value = "$totalReports",
                subtitle = "Semua riwayat",
                icon = Icons.Default.PendingActions,
                iconTint = ForestGreenPrimary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Laporan Hari Ini",
                value = "$todayReports",
                subtitle = "Baru 24 jam",
                icon = Icons.Default.Today,
                iconTint = MintSecondary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                title = "Perlu Ditinjau",
                value = "$needsReviewReports",
                subtitle = "Menunggu verifikasi",
                icon = Icons.Default.Warning,
                iconTint = WarningOrange,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Tindak Lanjut",
                value = "$followedUpReports",
                subtitle = "Ditangani/Selesai",
                icon = Icons.Default.AssignmentTurnedIn,
                iconTint = ForestGreenPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        // B. GRAFIK & DISTRIBUSI LAPORAN
        Text(
            text = "B. Distribusi Analisis Pangan & Gejala",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Symptoms Breakdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Healing,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Frekuensi Jenis Keluhan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (symptomFreq.isEmpty()) {
                    Text("Belum ada data keluhan.", style = MaterialTheme.typography.bodySmall)
                } else {
                    for (entry in symptomFreq.take(5)) {
                        DistributionBar(
                            label = entry.key,
                            count = entry.value,
                            total = totalReports.coerceAtLeast(1),
                            color = WarningOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Location Breakdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lokasi Pembelian Terbanyak (Klaster)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                for (entry in locFreq.take(4)) {
                    DistributionBar(
                        label = entry.key,
                        count = entry.value,
                        total = totalReports.coerceAtLeast(1),
                        color = ForestGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Food Category Breakdown
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kategori Makanan yang Dilaporkan",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                for (entry in catFreq.take(4)) {
                    DistributionBar(
                        label = entry.key,
                        count = entry.value,
                        total = totalReports.coerceAtLeast(1),
                        color = MintSecondary
                    )
                }
            }
        }

        // C. PETA & KLASTERISASI LOKASI
        Text(
            text = "C. Pemantauan Klaster Lokasi Pembelian",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daftar Klaster Lokasi Laporan",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sistem mengelompokkan laporan berdasarkan kesamaan titik lokasi tanpa mempublikasikan identitas pelapor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                for (entry in locFreq) {
                    val locName = entry.key
                    val count = entry.value
                    val isCluster = count >= 3
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = if (isCluster) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isCluster) WarningOrange else ForestGreenPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = locName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isCluster) "Klaster aktif (>= 3 laporan terhubung)" else "Laporan terpencar",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isCluster) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                color = if (isCluster) WarningOrange else ForestGreenPrimary,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "$count Laporan",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // D. FILTER & TABEL LAPORAN LENGKAP
        Text(
            text = "D. Tabel Laporan Masyarakat",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = ForestGreenDark
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari tiket, makanan, gejala, atau lokasi...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Hapus")
                            }
                        }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("surveillance_search_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Status Filter Chips
                Text(
                    text = "Filter Status:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statusOptions = listOf("Semua", ReportStatus.BARU.name, ReportStatus.PERLU_DITINJAU.name, ReportStatus.SEDANG_DITINDAKLANJUTI.name, ReportStatus.SELESAI.name)
                    statusOptions.forEach { st ->
                        val isSel = selectedStatus == st
                        val label = when (st) {
                            "Semua" -> "Semua Status"
                            ReportStatus.BARU.name -> "Baru"
                            ReportStatus.PERLU_DITINJAU.name -> "Perlu Ditinjau"
                            ReportStatus.SEDANG_DITINDAKLANJUTI.name -> "Ditindaklanjuti"
                            ReportStatus.SELESAI.name -> "Selesai"
                            else -> st
                        }
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedStatus = st },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Text(
                    text = "Menampilkan ${filteredReports.size} dari $totalReports laporan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Table / List of items
                if (filteredReports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada laporan yang sesuai dengan filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    filteredReports.forEach { report ->
                        ReportItemRow(
                            report = report,
                            onDetailClick = {
                                selectedReportForDetail = report
                                reviewNotes = ""
                                reviewStatusChoice = when (report.status) {
                                    ReportStatus.BARU.name -> ReportStatus.PERLU_DITINJAU
                                    ReportStatus.PERLU_DITINJAU.name -> ReportStatus.SEDANG_DITINDAKLANJUTI
                                    else -> ReportStatus.SELESAI
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // DETAIL & REVIEW DIALOG
    if (selectedReportForDetail != null) {
        val rep = selectedReportForDetail!!
        AlertDialog(
            onDismissRequest = { selectedReportForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Detail Laporan: ${rep.reportCode}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (rep.isSimulationData) "Data Simulasi" else "Laporan Masyarakat",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(status = rep.status)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailSectionItem("Makanan / Minuman", "${rep.foodName} (${rep.foodCategory})")
                    DetailSectionItem("Lokasi Pembelian", "${rep.locationName}\n${rep.locationAddress}")
                    DetailSectionItem("Waktu Konsumsi", "${rep.consumptionDate} pukul ${rep.consumptionTime}")
                    DetailSectionItem("Mulai Keluhan", "${rep.symptomOnsetDate} pukul ${rep.symptomOnsetTime}")
                    DetailSectionItem("Gejala Dilaporkan", rep.symptoms)
                    DetailSectionItem("Tingkat Keluhan", rep.severity)
                    DetailSectionItem("Penanganan Medis", if (rep.medicalAttention) "Ya, telah diperiksa fasilitas medis" else "Belum/Pengobatan mandiri")
                    if (rep.additionalNotes.isNotBlank()) {
                        DetailSectionItem("Keterangan Tambahan", rep.additionalNotes)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Perbarui Status & Tindak Lanjut Petugas:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenDark
                    )

                    // Role status choice
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            ReportStatus.PERLU_DITINJAU,
                            ReportStatus.SEDANG_DITINDAKLANJUTI,
                            ReportStatus.SELESAI
                        ).forEach { st ->
                            FilterChip(
                                selected = reviewStatusChoice == st,
                                onClick = { reviewStatusChoice = st },
                                label = { Text(st.label, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = reviewNotes,
                        onValueChange = { reviewNotes = it },
                        label = { Text("Catatan Peninjauan Petugas") },
                        placeholder = { Text("Contoh: Telah dikonfirmasi ke puskesmas wilayah untuk koordinasi sampel makanan.") },
                        modifier = Modifier.fillMaxWidth().testTag("review_notes_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reviewReport(
                            reportId = rep.id,
                            newStatus = reviewStatusChoice,
                            notes = reviewNotes.ifBlank { "Status diperbarui menjadi ${reviewStatusChoice.label}" }
                        )
                        selectedReportForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    modifier = Modifier.testTag("simpan_peninjauan_button")
                ) {
                    Text("Simpan Status")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedReportForDetail = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

@Composable
private fun ReportItemRow(
    report: FoodReportEntity,
    onDetailClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onDetailClick() }
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.reportCode,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge,
                    color = ForestGreenPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SeverityBadge(severity = report.severity)
                    StatusBadge(status = report.status)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${report.foodName} • ${report.locationName}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Keluhan: ${report.symptoms}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Konsumsi: ${report.consumptionDate} ${report.consumptionTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Ketuk untuk Detail & Tindak Lanjut →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }
        }
    }
}

@Composable
private fun DetailSectionItem(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
