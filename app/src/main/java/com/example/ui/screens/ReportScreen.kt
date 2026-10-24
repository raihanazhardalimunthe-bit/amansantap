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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AmanSantapViewModel
import com.example.ui.components.PublicHealthDisclaimerCard
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MintContainer
import com.example.ui.theme.MintLight
import com.example.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportScreen(
    viewModel: AmanSantapViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current

    // Today default dates
    val todayDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Food fields
    var foodName by remember { mutableStateOf("") }
    var foodCategory by remember { mutableStateOf("Makanan Berat") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val foodCategories = listOf(
        "Makanan Berat",
        "Olahan Daging/Ayam",
        "Olahan Laut/Ikan",
        "Jajanan/Kudapan",
        "Minuman / Es",
        "Sayur / Lalapan / Salad",
        "Katering / Prasmanan",
        "Lainnya"
    )

    var locationName by remember { mutableStateOf("") }
    var locationAddress by remember { mutableStateOf("") }
    var consumptionDate by remember { mutableStateOf(todayDate) }
    var consumptionTime by remember { mutableStateOf("12:30") }

    // Symptoms fields
    val availableSymptoms = listOf(
        "Mual",
        "Muntah",
        "Diare",
        "Sakit perut",
        "Demam",
        "Pusing",
        "Keluhan lainnya"
    )
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var otherSymptomText by remember { mutableStateOf("") }

    var symptomOnsetDate by remember { mutableStateOf(todayDate) }
    var symptomOnsetTime by remember { mutableStateOf("16:00") }
    var severity by remember { mutableStateOf("Sedang") }
    val severityOptions = listOf("Ringan", "Sedang", "Berat")

    var medicalAttention by remember { mutableStateOf(false) }
    var additionalNotes by remember { mutableStateOf("") }

    // Privacy & Reporter
    var isAnonymous by remember { mutableStateOf(true) }
    var reporterName by remember { mutableStateOf("") }
    var reporterPhone by remember { mutableStateOf("") }
    var consentAgreed by remember { mutableStateOf(false) }

    // Validation & Success Modal
    var validationError by remember { mutableStateOf<String?>(null) }
    var submittedReportCode by remember { mutableStateOf<String?>(null) }

    fun resetForm() {
        foodName = ""
        foodCategory = "Makanan Berat"
        locationName = ""
        locationAddress = ""
        consumptionDate = todayDate
        consumptionTime = "12:30"
        selectedSymptoms.clear()
        otherSymptomText = ""
        symptomOnsetDate = todayDate
        symptomOnsetTime = "16:00"
        severity = "Sedang"
        medicalAttention = false
        additionalNotes = ""
        isAnonymous = true
        reporterName = ""
        reporterPhone = ""
        consentAgreed = false
        validationError = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Formulir Laporan Keluhan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Pelaporan Dugaan Gangguan Keamanan Pangan",
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            PublicHealthDisclaimerCard()

            // 1. INFORMASI MAKANAN
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "A. Informasi Makanan & Pembelian",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenDark
                        )
                    }

                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("Nama Makanan atau Minuman *") },
                        placeholder = { Text("Contoh: Nasi Goreng Spesial, Ayam Bakar") },
                        modifier = Modifier.fillMaxWidth().testTag("food_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = foodCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jenis Makanan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("food_category_dropdown"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            foodCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        foodCategory = category
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = locationName,
                        onValueChange = { locationName = it },
                        label = { Text("Nama Tempat Pembelian *") },
                        placeholder = { Text("Contoh: Kantin Gedung C, Warung Bu Joko") },
                        modifier = Modifier.fillMaxWidth().testTag("location_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = locationAddress,
                        onValueChange = { locationAddress = it },
                        label = { Text("Alamat atau Lokasi Pembelian *") },
                        placeholder = { Text("Contoh: Jl. Kampus Terpadu No. 10") },
                        modifier = Modifier.fillMaxWidth().testTag("location_address_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = consumptionDate,
                            onValueChange = { consumptionDate = it },
                            label = { Text("Tanggal Konsumsi") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f).testTag("consumption_date_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = consumptionTime,
                            onValueChange = { consumptionTime = it },
                            label = { Text("Waktu Konsumsi") },
                            placeholder = { Text("HH:mm") },
                            modifier = Modifier.weight(1f).testTag("consumption_time_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // 2. INFORMASI KELUHAN
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "B. Informasi Keluhan yang Dialami",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenDark
                        )
                    }

                    Text(
                        text = "Pilih gejala yang Anda rasakan: *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableSymptoms.forEach { symptom ->
                            val isSelected = selectedSymptoms.contains(symptom)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedSymptoms.remove(symptom)
                                    } else {
                                        selectedSymptoms.add(symptom)
                                    }
                                },
                                label = { Text(symptom) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MintContainer,
                                    selectedLabelColor = ForestGreenDark
                                ),
                                modifier = Modifier.testTag("symptom_chip_${symptom.replace(" ", "_").lowercase()}")
                            )
                        }
                    }

                    if (selectedSymptoms.contains("Keluhan lainnya")) {
                        OutlinedTextField(
                            value = otherSymptomText,
                            onValueChange = { otherSymptomText = it },
                            label = { Text("Sebutkan keluhan lainnya") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = symptomOnsetDate,
                            onValueChange = { symptomOnsetDate = it },
                            label = { Text("Tgl Mulai Keluhan") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f).testTag("onset_date_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = symptomOnsetTime,
                            onValueChange = { symptomOnsetTime = it },
                            label = { Text("Jam Mulai Keluhan") },
                            placeholder = { Text("HH:mm") },
                            modifier = Modifier.weight(1f).testTag("onset_time_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Tingkat Keluhan
                    Text(
                        text = "Tingkat Keluhan:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        severityOptions.forEach { opt ->
                            val isChosen = severity == opt
                            OutlinedButton(
                                onClick = { severity = opt },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isChosen) ForestGreenPrimary else Color.Transparent,
                                    contentColor = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(opt)
                            }
                        }
                    }

                    // Penanganan medis toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sudah mendapat penanganan medis?",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Pemeriksaan di Klinik/Puskesmas/Rumah Sakit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = medicalAttention,
                            onCheckedChange = { medicalAttention = it },
                            modifier = Modifier.testTag("medical_attention_switch")
                        )
                    }

                    OutlinedTextField(
                        value = additionalNotes,
                        onValueChange = { additionalNotes = it },
                        label = { Text("Keterangan Tambahan (Opsional)") },
                        placeholder = { Text("Contoh: Teman yang ikut makan juga merasakan mual.") },
                        modifier = Modifier.fillMaxWidth().testTag("additional_notes_input"),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 3. PRIVASI & PERSETUJUAN
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ForestGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "C. Privasi & Persetujuan Pelapor",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenDark
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MintLight)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Laporkan Secara Anonim",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenDark
                            )
                            Text(
                                text = "Identitas Anda tidak akan disimpan maupun dibagikan ke publik.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ForestGreenDark.copy(alpha = 0.8f)
                            )
                        }
                        Switch(
                            checked = isAnonymous,
                            onCheckedChange = { isAnonymous = it },
                            modifier = Modifier.testTag("anonymous_switch")
                        )
                    }

                    if (!isAnonymous) {
                        OutlinedTextField(
                            value = reporterName,
                            onValueChange = { reporterName = it },
                            label = { Text("Nama Pelapor") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = reporterPhone,
                            onValueChange = { reporterPhone = it },
                            label = { Text("Nomor Kontak / WhatsApp") },
                            placeholder = { Text("Untuk konfirmasi tim surveilans bila diperlukan") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Checkbox Persetujuan
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { consentAgreed = !consentAgreed }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = consentAgreed,
                            onCheckedChange = { consentAgreed = it },
                            colors = CheckboxDefaults.colors(checkedColor = ForestGreenPrimary),
                            modifier = Modifier.testTag("consent_checkbox")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Saya menyetujui penggunaan data laporan ini semata-mata untuk kepentingan pemantauan keamanan pangan masyarakat dan surveilans kesehatan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Validation Error Box
            if (validationError != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = validationError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Action Buttons (Kirim, Reset, Kembali)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { resetForm() },
                    modifier = Modifier.weight(1f).testTag("reset_form_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }

                Button(
                    onClick = {
                        // Validation
                        if (foodName.isBlank()) {
                            validationError = "Mohon isi nama makanan atau minuman."
                            return@Button
                        }
                        if (locationName.isBlank()) {
                            validationError = "Mohon isi nama tempat pembelian makanan."
                            return@Button
                        }
                        if (locationAddress.isBlank()) {
                            validationError = "Mohon isi alamat atau lokasi pembelian."
                            return@Button
                        }
                        if (selectedSymptoms.isEmpty()) {
                            validationError = "Pilih minimal satu gejala atau keluhan yang Anda rasakan."
                            return@Button
                        }
                        if (!consentAgreed) {
                            validationError = "Mohon centang persetujuan penggunaan data surveilans."
                            return@Button
                        }

                        val symptomsToSave = selectedSymptoms.toMutableList()
                        if (symptomsToSave.contains("Keluhan lainnya") && otherSymptomText.isNotBlank()) {
                            symptomsToSave.remove("Keluhan lainnya")
                            symptomsToSave.add("Lainnya: ${otherSymptomText.trim()}")
                        }

                        validationError = null
                        viewModel.submitReport(
                            foodName = foodName,
                            foodCategory = foodCategory,
                            locationName = locationName,
                            locationAddress = locationAddress,
                            consumptionDate = consumptionDate,
                            consumptionTime = consumptionTime,
                            symptomOnsetDate = symptomOnsetDate,
                            symptomOnsetTime = symptomOnsetTime,
                            symptoms = symptomsToSave,
                            severity = severity,
                            medicalAttention = medicalAttention,
                            additionalNotes = additionalNotes,
                            isAnonymous = isAnonymous,
                            reporterName = reporterName,
                            reporterPhone = reporterPhone,
                            consentAgreed = consentAgreed,
                            onSuccess = { code ->
                                submittedReportCode = code
                            }
                        )
                    },
                    modifier = Modifier.weight(2f).testTag("kirim_laporan_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kirim Laporan", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Submission Success Dialog
        if (submittedReportCode != null) {
            AlertDialog(
                onDismissRequest = {
                    submittedReportCode = null
                    onNavigateBack()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ForestGreenPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        text = "Laporan Berhasil Dikirim",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Laporan berhasil dikirim. Terima kasih telah membantu pemantauan keamanan pangan.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = MintLight,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Nomor Tiket Laporan Anda:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ForestGreenDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = submittedReportCode!!,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ForestGreenPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(submittedReportCode!!))
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Salin Kode",
                                            tint = ForestGreenDark,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Simpan kode ini untuk melacak status peninjauan di Beranda.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ForestGreenDark.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            submittedReportCode = null
                            onNavigateBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                    ) {
                        Text("Selesai")
                    }
                }
            )
        }
    }
}
