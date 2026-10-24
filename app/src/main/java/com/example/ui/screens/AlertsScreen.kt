package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.model.AlertEntity
import com.example.data.model.AlertStatus
import com.example.ui.AmanSantapViewModel
import com.example.ui.components.PublicHealthDisclaimerCard
import com.example.ui.components.SimulationDataBanner
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintLight
import com.example.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AmanSantapViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    var selectedAlertForAction by remember { mutableStateOf<AlertEntity?>(null) }
    var actionNotes by remember { mutableStateOf("") }
    var chosenStatus by remember { mutableStateOf(AlertStatus.SEDANG_DITINDAKLANJUTI) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Peringatan Dini Internal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Surveilans Dugaan Klaster Keracunan Pangan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MintLight
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ForestGreenPrimary)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SimulationDataBanner()
            }

            item {
                PublicHealthDisclaimerCard()
            }

            item {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = WarningOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Notifikasi ini khusus untuk peninjauan petugas surveilans. Peringatan dini bukan merupakan vonis penyebab keracunan, melainkan indikasi untuk memandu tindak lanjut peninjauan epidemiologi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF92400E),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (alerts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ForestGreenPrimary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Tidak Ada Peringatan Aktif",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Belum terdeteksi klaster laporan yang memenuhi ambang batas mencurigakan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(alerts) { alert ->
                    AlertCardItem(
                        alert = alert,
                        onActionClick = {
                            selectedAlertForAction = alert
                            actionNotes = alert.reviewerNotes ?: ""
                            chosenStatus = when (alert.status) {
                                AlertStatus.PERLU_DITINJAU.name -> AlertStatus.SEDANG_DITINDAKLANJUTI
                                else -> AlertStatus.SELESAI
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Action Modal
        if (selectedAlertForAction != null) {
            val alert = selectedAlertForAction!!
            AlertDialog(
                onDismissRequest = { selectedAlertForAction = null },
                title = {
                    Text(
                        text = "Tindak Lanjut Peringatan: ${alert.alertCode}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Lokasi: ${alert.locationName} (${alert.reportCount} laporan)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Status Peninjauan Saat Ini:",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                AlertStatus.PERLU_DITINJAU,
                                AlertStatus.SEDANG_DITINDAKLANJUTI,
                                AlertStatus.SELESAI
                            ).forEach { st ->
                                FilterChip(
                                    selected = chosenStatus == st,
                                    onClick = { chosenStatus = st },
                                    label = { Text(st.label, fontSize = 11.sp) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = actionNotes,
                            onValueChange = { actionNotes = it },
                            label = { Text("Catatan Tindak Lanjut Petugas") },
                            placeholder = { Text("Contoh: Mengirimkan tim investigasi lapangan dan wawancara pelapor.") },
                            modifier = Modifier.fillMaxWidth().testTag("alert_notes_input"),
                            minLines = 2,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateAlertStatus(
                                alertId = alert.id,
                                newStatus = chosenStatus,
                                notes = actionNotes.ifBlank { "Diperbarui menjadi ${chosenStatus.label}" }
                            )
                            selectedAlertForAction = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                        modifier = Modifier.testTag("simpan_tindak_lanjut_alert_button")
                    ) {
                        Text("Simpan Tindak Lanjut")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { selectedAlertForAction = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
private fun AlertCardItem(
    alert: AlertEntity,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.status == AlertStatus.SELESAI.name)
                MaterialTheme.colorScheme.surface
            else
                Color(0xFFFFFBEB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (alert.status == AlertStatus.SELESAI.name)
                                    ForestGreenPrimary
                                else
                                    WarningOrange
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (alert.status == AlertStatus.SELESAI.name)
                                Icons.Default.AssignmentTurnedIn
                            else
                                Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.alertCode,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = ForestGreenDark
                    )
                }

                Surface(
                    color = when (alert.status) {
                        AlertStatus.PERLU_DITINJAU.name -> Color(0xFFFEF3C7)
                        AlertStatus.SEDANG_DITINDAKLANJUTI.name -> Color(0xFFEDE9FE)
                        else -> Color(0xFFD1FAE5)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = alert.status.replace("_", " "),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (alert.status) {
                            AlertStatus.PERLU_DITINJAU.name -> Color(0xFFB45309)
                            AlertStatus.SEDANG_DITINDAKLANJUTI.name -> Color(0xFF6D28D9)
                            else -> Color(0xFF047857)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reason notification text
            Text(
                text = alert.reason,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Details breakdown
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = ForestGreenPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Lokasi: ${alert.locationName} (${alert.reportCount} Laporan)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(16.dp), tint = ForestGreenPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Makanan Dilaporkan: ${alert.reportedFoods}", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Healing, contentDescription = null, modifier = Modifier.size(16.dp), tint = WarningOrange)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Gejala Dominan: ${alert.dominantSymptoms}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (alert.reviewerNotes != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Catatan Tindak Lanjut (${alert.reviewedBy ?: "Petugas"}):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenDark
                )
                Text(
                    text = alert.reviewerNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("tindak_lanjuti_alert_button_${alert.id}")
                ) {
                    Text("Tindak Lanjuti Peringatan", fontSize = 12.sp)
                }
            }
        }
    }
}
