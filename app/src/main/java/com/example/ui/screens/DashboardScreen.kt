package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.AppViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Build
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: AppViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUserState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = currentTab == "beranda",
                    onClick = { viewModel.currentTab.value = "beranda" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "kontrak",
                    onClick = { viewModel.currentTab.value = "kontrak" },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Kontrak") },
                    label = { Text("Kontrak", fontSize = 11.sp) }
                )
                if (currentUser?.role == "user") {
                    NavigationBarItem(
                        selected = currentTab == "input",
                        onClick = { viewModel.currentTab.value = "input" },
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = "Input") },
                        label = { Text("Input", fontSize = 11.sp) }
                    )
                }
                NavigationBarItem(
                    selected = currentTab == "opname",
                    onClick = { viewModel.currentTab.value = "opname" },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Opname") },
                    label = { Text("Opname", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = currentTab == "profil",
                    onClick = { viewModel.currentTab.value = "profil" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil", fontSize = 11.sp) }
                )
            }
        },
        containerColor = Color(0xFFF6F9FD)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                "beranda" -> BerandaTab(viewModel)
                "kontrak" -> KontrakTab(viewModel)
                "input" -> InputTab(viewModel)
                "opname" -> OpnameTab(viewModel)
                "profil" -> ProfilTab(viewModel)
            }
        }
    }
}

// ==========================================
// 1. BERANDA TAB
// ==========================================
@Composable
fun BerandaTab(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUserState.collectAsState()
    val contractList by viewModel.contractList.collectAsState()
    val opnameList by viewModel.opnameList.collectAsState()
    val isOnline = viewModel.repository.isNetworkAvailable()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Aplikasi Jalan dan Jembatan",
                        fontSize = 12.sp,
                        color = Color(0xFF0D5DA3),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Selamat Datang di eProject!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
                // Refresh icon
                IconButton(onClick = { viewModel.repository.triggerSync() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color(0xFF0D5DA3)
                    )
                }
            }
        }

        // Auto Sync Status Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                border = BorderStroke(1.dp, Color(0xFF90CAF9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = Color(0xFF0D5DA3)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOnline) "Sync Otomatis Aktif" else "Mode Offline Aktif",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D5DA3)
                        )
                        Text(
                            text = if (isOnline) "Terakhir upload: Baru saja" else "Perubahan disimpan lokal",
                            fontSize = 11.sp,
                            color = Color(0xFF546E7A)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(1.dp, Color(0xFF81C784))
                    ) {
                        Text(
                            text = "OFFLINE READY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Physical Progress and Items Inputted stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Progress
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("PROGRES FISIK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        // Calculate average progression based on values
                        var totalContractValue = 0.0
                        var totalOpnameValue = 0.0
                        for (c in contractList) {
                            totalContractValue += c.volume * c.unitPrice
                            val opnamesForItem = opnameList.filter { it.itemName.startsWith(c.itemCodeAndName.take(10)) }
                            val totalItemOpnameVol = opnamesForItem.sumOf { it.calculatedVolume }
                            totalOpnameValue += totalItemOpnameVol * c.unitPrice
                        }
                        val progressPercentage = if (totalContractValue > 0.0) (totalOpnameValue / totalContractValue) * 100.0 else 0.0
                        Text(
                            text = "${viewModel.formatDecimal(progressPercentage)} %",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D5DA3)
                        )
                    }
                }

                // Item Count
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ITEM TERINPUT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.height(4.dp))
                        val distinctItems = opnameList.map { it.itemName }.distinct().size
                        Text(
                            text = "$distinctItems Items",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D5DA3)
                        )
                    }
                }
            }
        }

        // Work Package Information (Informasi Paket Pekerjaan)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Informasi Paket Pekerjaan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nama Paket:", fontSize = 13.sp, color = Color(0xFF64748B))
                        Text(
                            text = currentUser?.packageName ?: "-",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D5DA3),
                            modifier = Modifier.widthIn(max = 200.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        // Rekapitulasi per Item
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rekapitulasi per item",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = "${contractList.size} Item Pekerjaan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // List of items and their progress
        if (contractList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada rincian item kontrak yang diinput.",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Sort items by division and item numerically
            val sortedContracts = viewModel.getSortedContracts(contractList)
            items(sortedContracts) { contract ->
                // Calculate accumulated volume
                val opnamesForItem = opnameList.filter { it.itemName.startsWith(contract.itemCodeAndName.take(10)) }
                val currentVol = opnamesForItem.sumOf { it.calculatedVolume }
                val percentage = if (contract.volume > 0.0) (currentVol / contract.volume) * 100.0 else 0.0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Division Pill (wrap text securely so it's not cut off!)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE8EAF6),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = contract.division.substringBefore(":").trim(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F51B5),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                // Code and Item Name (wrap text cleanly!)
                                Text(
                                    text = contract.itemCodeAndName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    softWrap = true
                                )
                                Text(
                                    text = contract.division,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    softWrap = true,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Volumes
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${viewModel.formatDecimal(currentVol)} ${contract.unit}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0D5DA3)
                                )
                                Text(
                                    text = "/ ${viewModel.formatDecimal(contract.volume)} ${contract.unit}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Progres Kontrak", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = "${viewModel.formatDecimal(percentage)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D5DA3)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Progress Indicator Bar
                        LinearProgressIndicator(
                            progress = { (percentage / 100.0).coerceIn(0.0, 1.0).toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF0D5DA3),
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. KONTRAK TAB
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KontrakTab(viewModel: AppViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUserState.collectAsState()
    val contractList by viewModel.contractList.collectAsState()
    val customItems by viewModel.customItems.collectAsState()
    val customUnits by viewModel.customUnits.collectAsState()

    // Form inputs
    val division by viewModel.contractDivision.collectAsState()
    val itemCodeAndName by viewModel.contractItem.collectAsState()
    val unit by viewModel.contractUnit.collectAsState()
    val volume by viewModel.contractVolume.collectAsState()
    val unitPrice by viewModel.contractUnitPrice.collectAsState()

    // Dialog state for adding custom items/units
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddUnitDialog by remember { mutableStateOf(false) }

    var searchEmailInput by remember { mutableStateOf("") }
    val searchedUserAccount by viewModel.searchedUserAccount.collectAsState()

    // Division options
    val divisions = listOf(
        "Divisi 1 : Umum dan Penerapan SMKK",
        "Divisi 2 : Drainase",
        "Divisi 3 : Pekerjaan Tanah dan Geosintetik",
        "Divisi 4 : Pekerjaan Preventif",
        "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen",
        "Divisi 6 : Perkerasan Aspal",
        "Divisi 7 : Struktur",
        "Divisi 8 : Rehabilitasi Jembatan",
        "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain",
        "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search bar for Admin to search other users' accounts
        if (currentUser?.role == "admin" || currentUser?.role == "super_admin") {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Cari Akun Pengguna (Lihat Kontrak)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchEmailInput,
                                onValueChange = { searchEmailInput = it },
                                label = { Text("Email Pengguna") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    viewModel.searchUserByEmail(searchEmailInput)
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5DA3))
                            ) {
                                Text("Cari")
                            }
                        }

                        if (searchedUserAccount != null) {
                            Text(
                                text = "Paket: ${searchedUserAccount?.packageName ?: ""}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else if (searchEmailInput.isNotBlank()) {
                            Text(
                                text = "Akun tidak ditemukan atau belum dimasukkan.",
                                fontSize = 12.sp,
                                color = Color.Red,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // PPN Configuration Card
        if (currentUser?.role == "user" || currentUser?.role == "super_admin") {
            item {
                var ppnInput by remember(currentUser) { mutableStateOf(currentUser?.ppnPercentage?.toString() ?: "11.0") }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pengaturan Pajak (PPN %)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        OutlinedTextField(
                            value = ppnInput,
                            onValueChange = { 
                                ppnInput = it
                                val parsed = it.toDoubleOrNull()
                                if (parsed != null) {
                                    viewModel.updateProfilePPN(parsed)
                                }
                            },
                            label = { Text("PPN (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // If regular user or super admin, show input contract card
        if (currentUser?.role == "user" || currentUser?.role == "super_admin") {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (viewModel.editingContractId != null) "Edit Data Kontrak" else "Input Data Kontrak",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Division Dropdown
                        var divExpanded by remember { mutableStateOf(false) }
                        Text("Divisi Spesifikasi", fontSize = 12.sp, color = Color(0xFF64748B))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedCard(
                                onClick = { divExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(division, fontSize = 14.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = divExpanded,
                                onDismissRequest = { divExpanded = false }
                            ) {
                                divisions.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d) },
                                        onClick = {
                                            viewModel.contractDivision.value = d
                                            divExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Item Dropdown with Search, Add and Delete Buttons
                        var itemExpanded by remember { mutableStateOf(false) }
                        var itemSearchQuery by remember { mutableStateOf("") }
                        val itemsInDiv = customItems.filter { it.divisionCode == division }
                        Text("Item Pekerjaan", fontSize = 12.sp, color = Color(0xFF64748B))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedCard(
                                    onClick = { itemExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = itemCodeAndName.ifBlank { "Pilih Item Pekerjaan..." },
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(
                                    expanded = itemExpanded,
                                    onDismissRequest = {
                                        itemExpanded = false
                                        itemSearchQuery = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    OutlinedTextField(
                                        value = itemSearchQuery,
                                        onValueChange = { itemSearchQuery = it },
                                        label = { Text("Cari Item...") },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    
                                    val filteredItems = itemsInDiv.filter {
                                        it.codeAndName.lowercase().contains(itemSearchQuery.lowercase())
                                    }
                                    
                                    if (filteredItems.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Item tidak ditemukan") },
                                            onClick = {},
                                            enabled = false
                                        )
                                    } else {
                                        filteredItems.forEach { item ->
                                            DropdownMenuItem(
                                                text = { Text(item.codeAndName) },
                                                onClick = {
                                                    viewModel.contractItem.value = item.codeAndName
                                                    viewModel.contractUnit.value = item.defaultUnit
                                                    itemExpanded = false
                                                    itemSearchQuery = ""
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedIconButton(
                                onClick = { showAddItemDialog = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah Item")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedIconButton(
                                onClick = {
                                    val matchingItem = customItems.find { it.codeAndName.lowercase() == itemCodeAndName.lowercase() }
                                    if (matchingItem != null) {
                                        viewModel.deleteCustomItem(matchingItem.id)
                                        val remainingItems = customItems.filter { it.divisionCode == division && it.id != matchingItem.id }
                                        viewModel.contractItem.value = remainingItems.firstOrNull()?.codeAndName ?: ""
                                        viewModel.contractUnit.value = remainingItems.firstOrNull()?.defaultUnit ?: "m3"
                                    } else {
                                        Toast.makeText(context, "Item tidak ditemukan untuk dihapus.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Item", tint = Color.Red)
                            }
                        }

                        // Satuan Dropdown with Add and Delete Buttons
                        var unitExpanded by remember { mutableStateOf(false) }
                        Text("Satuan Volume", fontSize = 12.sp, color = Color(0xFF64748B))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedCard(
                                    onClick = { unitExpanded = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(unit, fontSize = 14.sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(
                                    expanded = unitExpanded,
                                    onDismissRequest = { unitExpanded = false }
                                ) {
                                    customUnits.forEach { u ->
                                        DropdownMenuItem(
                                            text = { Text(u.name) },
                                            onClick = {
                                                viewModel.contractUnit.value = u.name
                                                unitExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedIconButton(
                                onClick = { showAddUnitDialog = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Tambah Satuan")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedIconButton(
                                onClick = {
                                    val matchingUnit = customUnits.find { it.name.lowercase() == unit.lowercase() }
                                    if (matchingUnit != null) {
                                        viewModel.deleteCustomUnit(matchingUnit.id)
                                        val remainingUnits = customUnits.filter { it.id != matchingUnit.id }
                                        viewModel.contractUnit.value = remainingUnits.firstOrNull()?.name ?: "m3"
                                    } else {
                                        Toast.makeText(context, "Satuan tidak ditemukan untuk dihapus.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Satuan", tint = Color.Red)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Volume Input
                        Text("Volume", fontSize = 12.sp, color = Color(0xFF64748B))
                        OutlinedTextField(
                            value = volume,
                            onValueChange = { viewModel.contractVolume.value = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Harga Satuan Input
                        OutlinedTextField(
                            value = unitPrice,
                            onValueChange = { viewModel.contractUnitPrice.value = it },
                            label = { Text("Harga Satuan (Rupiah)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (itemCodeAndName.isBlank()) {
                                    Toast.makeText(context, "Pilih Item Pekerjaan terlebih dahulu!", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveContract()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5DA3)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (viewModel.editingContractId != null) "Perbarui Item Kontrak" else "Simpan ke Kontrak")
                        }
                    }
                }
            }
        }

        // Table Rincian Item Kontrak
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rincian Item Kontrak",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )

                        if (currentUser?.role != "admin") {
                            TextButton(
                                onClick = { viewModel.insertAllDefaultItemsToContract() }
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Masukkan Semua Item", fontSize = 12.sp)
                            }
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    if (contractList.isEmpty()) {
                        Text(
                            text = "Belum ada rincian item kontrak.",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Horizontally scrollable table
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Column {
                                // Headers
                                Row(
                                    modifier = Modifier
                                        .background(Color(0xFFF1F5F9))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Divisi", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                                    Text("Item Pekerjaan", fontWeight = FontWeight.Bold, modifier = Modifier.width(220.dp))
                                    Text("Satuan", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                                    Text("Volume", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                    Text("Harga Satuan", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                                    Text("Jumlah Harga", fontWeight = FontWeight.Bold, modifier = Modifier.width(140.dp), textAlign = TextAlign.End)
                                    Text("Persen (%)", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                    if (currentUser?.role != "admin") {
                                        Text("Aksi", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp), textAlign = TextAlign.Center)
                                    }
                                }

                                // Rows
                                val totalValue = contractList.sumOf { it.volume * it.unitPrice }
                                viewModel.getSortedContracts(contractList).forEach { c ->
                                    val itemTotal = c.volume * c.unitPrice
                                    val pct = if (totalValue > 0.0) (itemTotal / totalValue) * 100 else 0.0
                                    val pctStr = String.format(Locale.US, "%.2f%%", pct)
                                    Row(
                                        modifier = Modifier
                                            .border(1.dp, Color(0xFFE2E8F0))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(c.division.substringBefore(":").trim(), modifier = Modifier.width(100.dp))
                                        Text(c.itemCodeAndName, modifier = Modifier.width(220.dp), softWrap = true)
                                        Text(c.unit, modifier = Modifier.width(60.dp))
                                        Text(viewModel.formatDecimalWithSeparator(c.volume), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                        Text(viewModel.formatDecimalWithSeparator(c.unitPrice), modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                                        Text(viewModel.formatDecimalWithSeparator(itemTotal), modifier = Modifier.width(140.dp), textAlign = TextAlign.End)
                                        Text(pctStr, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                        if (currentUser?.role != "admin") {
                                            Row(
                                                modifier = Modifier.width(100.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(onClick = { viewModel.setEditingContract(c) }) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF0D5DA3))
                                                }
                                                IconButton(onClick = { viewModel.deleteContract(c.id) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Total Contract Price Card
        item {
            val ppnRate = if (currentUser?.role == "admin" || currentUser?.role == "super_admin") {
                searchedUserAccount?.ppnPercentage ?: currentUser?.ppnPercentage ?: 11.0
            } else {
                currentUser?.ppnPercentage ?: 11.0
            }
            val totalValue = contractList.sumOf { it.volume * it.unitPrice }
            val dpp = totalValue / (1.0 + ppnRate / 100.0)
            val ppnAmount = totalValue - dpp

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = BorderStroke(1.dp, Color(0xFF81C784))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TOTAL NILAI KONTRAK (SUDAH PPN)", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text(viewModel.formatCurrency(totalValue), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                        Icon(
                            imageVector = Icons.Default.Work,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Divider(color = Color(0xFFC8E6C9), thickness = 1.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nilai Fisik (Sebelum PPN):", fontSize = 12.sp, color = Color(0xFF1B5E20))
                        Text(viewModel.formatCurrency(dpp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("PPN (${viewModel.formatDecimal(ppnRate)}%):", fontSize = 12.sp, color = Color(0xFF1B5E20))
                        Text(viewModel.formatCurrency(ppnAmount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // Add Custom Item Dialog
    if (showAddItemDialog) {
        var newDivision by remember { mutableStateOf(divisions.first()) }
        var newCodeAndName by remember { mutableStateOf("") }
        var newUnit by remember { mutableStateOf("m3") }

        Dialog(onDismissRequest = { showAddItemDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tambah Item Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    // Division drop
                    var showDivs by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { showDivs = true }) {
                            Text(newDivision)
                        }
                        DropdownMenu(expanded = showDivs, onDismissRequest = { showDivs = false }) {
                            divisions.forEach { d ->
                                DropdownMenuItem(text = { Text(d) }, onClick = { newDivision = d; showDivs = false })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newCodeAndName,
                        onValueChange = { newCodeAndName = it },
                        label = { Text("Nomor & Nama Item") }
                    )

                    OutlinedTextField(
                        value = newUnit,
                        onValueChange = { newUnit = it },
                        label = { Text("Satuan") }
                    )

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showAddItemDialog = false }) { Text("Batal") }
                        Button(onClick = {
                            if (newCodeAndName.isNotBlank()) {
                                viewModel.addCustomItem(newDivision, newCodeAndName, newUnit)
                                showAddItemDialog = false
                            }
                        }) { Text("Tambah") }
                    }
                }
            }
        }
    }

    // Add Custom Unit Dialog
    if (showAddUnitDialog) {
        var newUnitName by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddUnitDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Tambah Satuan Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = newUnitName,
                        onValueChange = { newUnitName = it },
                        label = { Text("Nama Satuan") }
                    )

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showAddUnitDialog = false }) { Text("Batal") }
                        Button(onClick = {
                            if (newUnitName.isNotBlank()) {
                                viewModel.addCustomUnit(newUnitName)
                                showAddUnitDialog = false
                            }
                        }) { Text("Tambah") }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. INPUT TAB
// ==========================================
@Composable
fun InputTab(viewModel: AppViewModel) {
    val context = LocalContext.current
    val customItems by viewModel.customItems.collectAsState()
    val customUnits by viewModel.customUnits.collectAsState()
    val roadNames by viewModel.roadNames.collectAsState()

    // Form inputs
    val division by viewModel.opnameDivision.collectAsState()
    val item by viewModel.opnameItem.collectAsState()
    val location by viewModel.opnameLocation.collectAsState()
    val side by viewModel.opnameSide.collectAsState()
    val selectedRoad by viewModel.opnameRoadName.collectAsState()
    val date by viewModel.opnameDate.collectAsState()
    val unit by viewModel.opnameUnit.collectAsState()

    val length by viewModel.opnameLength.collectAsState()
    val width by viewModel.opnameWidth.collectAsState()
    val height by viewModel.opnameHeight.collectAsState()
    val thickness by viewModel.opnameThickness.collectAsState()
    val area by viewModel.opnameArea.collectAsState()
    val density by viewModel.opnameDensity.collectAsState()
    val quantity by viewModel.opnameQuantity.collectAsState()
    val photoUri by viewModel.opnamePhotoUri.collectAsState()

    var showRoadDialog by remember { mutableStateOf(false) }

    val divisions = listOf(
        "Divisi 1 : Umum dan Penerapan SMKK",
        "Divisi 2 : Drainase",
        "Divisi 3 : Pekerjaan Tanah dan Geosintetik",
        "Divisi 4 : Pekerjaan Preventif",
        "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen",
        "Divisi 6 : Perkerasan Aspal",
        "Divisi 7 : Struktur",
        "Divisi 8 : Rehabilitasi Jembatan",
        "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain",
        "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Input Data Opname",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Pastikan semua dimensi terisi sesuai pengukuran lapangan.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        // Form fields
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Location Work field
                    OutlinedTextField(
                        value = location,
                        onValueChange = { viewModel.opnameLocation.value = it },
                        label = { Text("Lokasi Pekerjaan (e.g. Sta 0+200)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Side selection
                    Text("Sisi", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        listOf("Kanan", "Kiri", "CL").forEach { s ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { viewModel.opnameSide.value = s }
                            ) {
                                RadioButton(
                                    selected = side == s,
                                    onClick = { viewModel.opnameSide.value = s }
                                )
                                Text(s)
                            }
                        }
                    }

                    // Road selection
                    var roadExpanded by remember { mutableStateOf(false) }
                    Text("Ruas Jalan", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedCard(
                                onClick = { roadExpanded = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedRoad.ifBlank { "Pilih Ruas Jalan..." }, fontSize = 14.sp)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = roadExpanded,
                                onDismissRequest = { roadExpanded = false }
                            ) {
                                roadNames.forEach { r ->
                                    DropdownMenuItem(
                                        text = { Text(r.name) },
                                        onClick = {
                                            viewModel.opnameRoadName.value = r.name
                                            roadExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedIconButton(
                            onClick = { showRoadDialog = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah Ruas Jalan")
                        }
                    }

                    // Tanggal field
                    val calendarInstance = Calendar.getInstance()
                    val year = calendarInstance.get(Calendar.YEAR)
                    val month = calendarInstance.get(Calendar.MONTH)
                    val day = calendarInstance.get(Calendar.DAY_OF_MONTH)

                    val datePickerDialog = remember {
                        android.app.DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                                val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
                                viewModel.opnameDate.value = formattedDate
                            },
                            year,
                            month,
                            day
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = {},
                            label = { Text("Tanggal Pengukuran (YYYY-MM-DD)") },
                            shape = RoundedCornerShape(12.dp),
                            readOnly = true,
                            trailingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { datePickerDialog.show() }
                        )
                    }

                    // Division
                    var divExpanded by remember { mutableStateOf(false) }
                    Text("Divisi Spesifikasi", fontSize = 12.sp, color = Color(0xFF64748B))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { divExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(division, fontSize = 14.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = divExpanded,
                            onDismissRequest = { divExpanded = false }
                        ) {
                            divisions.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = {
                                        viewModel.opnameDivision.value = d
                                        divExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Item dropdown with Search
                    var itemExpanded by remember { mutableStateOf(false) }
                    var itemSearchQuery by remember { mutableStateOf("") }
                    val itemsInDiv = customItems.filter { it.divisionCode == division }
                    Text("Item Pekerjaan", fontSize = 12.sp, color = Color(0xFF64748B))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { itemExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.ifBlank { "Pilih Item Pekerjaan..." },
                                    fontSize = 14.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = itemExpanded,
                            onDismissRequest = {
                                itemExpanded = false
                                itemSearchQuery = ""
                            },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            OutlinedTextField(
                                value = itemSearchQuery,
                                onValueChange = { itemSearchQuery = it },
                                label = { Text("Cari Item...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            
                            val filteredItems = itemsInDiv.filter {
                                it.codeAndName.lowercase().contains(itemSearchQuery.lowercase())
                            }
                            
                            if (filteredItems.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Item tidak ditemukan") },
                                    onClick = {},
                                    enabled = false
                                )
                            } else {
                                filteredItems.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.codeAndName) },
                                        onClick = {
                                            viewModel.opnameItem.value = item.codeAndName
                                            viewModel.opnameUnit.value = item.defaultUnit
                                            itemExpanded = false
                                            itemSearchQuery = ""
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Satuan Dropdown
                    var unitExpanded by remember { mutableStateOf(false) }
                    Text("Satuan", fontSize = 12.sp, color = Color(0xFF64748B))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { unitExpanded = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(unit, fontSize = 14.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            customUnits.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u.name) },
                                    onClick = {
                                        viewModel.opnameUnit.value = u.name
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dimensions input
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Dimensi Pengukuran (Masukkan angka desimal)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = length,
                            onValueChange = { viewModel.opnameLength.value = it },
                            label = { Text("Panjang (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = width,
                            onValueChange = { viewModel.opnameWidth.value = it },
                            label = { Text("Lebar (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = height,
                            onValueChange = { viewModel.opnameHeight.value = it },
                            label = { Text("Tinggi (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = thickness,
                            onValueChange = { viewModel.opnameThickness.value = it },
                            label = { Text("Tebal (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Luasan Mode Selector (Rumus vs Manual / Rumus Kustom)
                    val areaMode by viewModel.opnameAreaMode.collectAsState()
                    
                    Text("Metode Pengisian Luasan", fontSize = 12.sp, color = Color(0xFF64748B))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.opnameAreaMode.value = "rumus" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (areaMode == "rumus") 2.dp else 1.dp,
                                color = if (areaMode == "rumus") Color(0xFF0D5DA3) else Color(0xFFCBD5E1)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (areaMode == "rumus") Color(0xFFE2F0FD) else Color.Transparent
                            )
                        ) {
                            Text(
                                "Rumus (P x L)",
                                color = if (areaMode == "rumus") Color(0xFF0D5DA3) else Color(0xFF64748B),
                                fontWeight = if (areaMode == "rumus") FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.opnameAreaMode.value = "manual" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (areaMode == "manual") 2.dp else 1.dp,
                                color = if (areaMode == "manual") Color(0xFF0D5DA3) else Color(0xFFCBD5E1)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (areaMode == "manual") Color(0xFFE2F0FD) else Color.Transparent
                            )
                        ) {
                            Text(
                                "Manual / Rumus Kustom",
                                color = if (areaMode == "manual") Color(0xFF0D5DA3) else Color(0xFF64748B),
                                fontWeight = if (areaMode == "manual") FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (areaMode == "rumus") {
                        val autoArea = (length.toDoubleOrNull() ?: 0.0) * (width.toDoubleOrNull() ?: 0.0)
                        OutlinedTextField(
                            value = viewModel.formatDecimal(autoArea),
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Luasan (m2) - Auto hitung (P x L)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val evaluated = viewModel.evaluateExpression(area)
                        val helperText = if (evaluated != null && area != viewModel.formatDecimal(evaluated)) {
                            "Hasil: ${viewModel.formatDecimal(evaluated)} m2"
                        } else {
                            "Masukkan angka desimal atau rumus (contoh: 5*4+2)"
                        }
                        
                        OutlinedTextField(
                            value = area,
                            onValueChange = { viewModel.opnameArea.value = it },
                            label = { Text("Luasan (m2) - Manual atau Rumus") },
                            supportingText = { Text(helperText) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Jumlah input without plus/minus buttons
                        OutlinedTextField(
                            value = quantity,
                            onValueChange = { viewModel.opnameQuantity.value = it },
                            label = { Text("Jumlah") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.3f)
                        )

                        OutlinedTextField(
                            value = density,
                            onValueChange = { viewModel.opnameDensity.value = it },
                            label = { Text("Berat Jenis (BJ)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Live calculation preview
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Color(0xFF0D5DA3))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pratinjau Hasil Perhitungan Volume", fontWeight = FontWeight.Bold, color = Color(0xFF0D5DA3), fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rumus / Langkah: ${viewModel.getOpnameCalculationFormulaString()}",
                        fontSize = 11.sp,
                        color = Color(0xFF546E7A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Volume Total = ${viewModel.formatDecimal(viewModel.calculateOpnameVolumePreview())} $unit",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D5DA3)
                    )
                }
            }
        }

        // Foto select
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Foto Dokumentasi Pekerjaan", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                    Spacer(modifier = Modifier.height(12.dp))

                    if (photoUri.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Foto Dokumentasi Terpilih", color = Color(0xFF64748B))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF94A3B8))
                                Text("Belum ada foto yang dipilih", color = Color(0xFF94A3B8), fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.opnamePhotoUri.value = "simulated_camera_uri"
                                Toast.makeText(context, "Foto berhasil diambil!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ambil Foto", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                viewModel.opnamePhotoUri.value = "simulated_file_uri"
                                Toast.makeText(context, "Berkas dipilih!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569))
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pilih Berkas", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = {
                    if (location.isBlank() || selectedRoad.isBlank() || item.isBlank()) {
                        Toast.makeText(context, "Harap isi semua kolom wajib!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveOpname()
                        Toast.makeText(context, "Data Opname Offline berhasil disimpan!", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5DA3)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan Data Pekerjaan (Offline)")
            }
        }
    }

    // --- Road Dialog ---
    if (showRoadDialog) {
        var newRoadName by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showRoadDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Kelola Ruas Jalan", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(
                        value = newRoadName,
                        onValueChange = { newRoadName = it },
                        label = { Text("Nama Ruas Jalan Baru") }
                    )

                    Button(
                        onClick = {
                            if (newRoadName.isNotBlank()) {
                                viewModel.addRoadName(newRoadName)
                                newRoadName = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tambah Ruas Jalan")
                    }

                    Divider()

                    Text("Daftar Ruas Jalan:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Box(modifier = Modifier.height(150.dp)) {
                        LazyColumn {
                            items(roadNames) { r ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(r.name)
                                    IconButton(onClick = { viewModel.deleteRoadName(r.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showRoadDialog = false }) { Text("Tutup") }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. OPNAME TAB (REKAP TABLE)
// ==========================================
@Composable
fun OpnameTab(viewModel: AppViewModel) {
    val context = LocalContext.current
    val opnameList by viewModel.opnameList.collectAsState()
    val customItems by viewModel.customItems.collectAsState()
    val roadNames by viewModel.roadNames.collectAsState()

    // Filters
    var startDateFilter by remember { mutableStateOf("") }
    var endDateFilter by remember { mutableStateOf("") }
    var itemFilter by remember { mutableStateOf("") }
    var roadFilter by remember { mutableStateOf("") }
    var divisionFilter by remember { mutableStateOf("") }

    // Dropdown expanded states
    var itemExp by remember { mutableStateOf(false) }
    var itemFilterSearchQuery by remember { mutableStateOf("") }
    var roadExp by remember { mutableStateOf(false) }
    var divisionExp by remember { mutableStateOf(false) }

    val divisions = listOf(
        "Divisi 1 : Umum dan Penerapan SMKK",
        "Divisi 2 : Drainase",
        "Divisi 3 : Pekerjaan Tanah dan Geosintetik",
        "Divisi 4 : Pekerjaan Preventif",
        "Divisi 5 : Perkerasan Berbutir dan Perkerasan Beton Semen",
        "Divisi 6 : Perkerasan Aspal",
        "Divisi 7 : Struktur",
        "Divisi 8 : Rehabilitasi Jembatan",
        "Divisi 9 : Pekerjaan Harian dan Pekerjaan Lain-lain",
        "Divisi 10 : Pekerjaan Harian dan Pekerjaan Lain-lain"
    )

    val filteredList = opnameList.filter { op ->
        val matchesStart = startDateFilter.isBlank() || op.dateString >= startDateFilter
        val matchesEnd = endDateFilter.isBlank() || op.dateString <= endDateFilter
        val matchesItem = itemFilter.isBlank() || op.itemName.lowercase().contains(itemFilter.lowercase())
        val matchesRoad = roadFilter.isBlank() || op.roadName.lowercase().contains(roadFilter.lowercase())
        val matchesDivision = divisionFilter.isBlank() || op.division.lowercase().contains(divisionFilter.lowercase())
        matchesStart && matchesEnd && matchesItem && matchesRoad && matchesDivision
    }.sortedWith(compareByDescending<OpnameEntity> { it.dateString }.thenByDescending { it.timestamp })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Data Opname",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Berikut rekapitulasi data opname yang telah dimasukkan.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }

        // Filters Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Filter Data", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    val calStart = Calendar.getInstance()
                    val datePickerDialogStart = remember {
                        android.app.DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                                val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
                                startDateFilter = formattedDate
                            },
                            calStart.get(Calendar.YEAR),
                            calStart.get(Calendar.MONTH),
                            calStart.get(Calendar.DAY_OF_MONTH)
                        )
                    }

                    val calEnd = Calendar.getInstance()
                    val datePickerDialogEnd = remember {
                        android.app.DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                                val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDayOfMonth)
                                endDateFilter = formattedDate
                            },
                            calEnd.get(Calendar.YEAR),
                            calEnd.get(Calendar.MONTH),
                            calEnd.get(Calendar.DAY_OF_MONTH)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = startDateFilter,
                                onValueChange = {},
                                label = { Text("Dari Tanggal") },
                                readOnly = true,
                                trailingIcon = {
                                    if (startDateFilter.isNotBlank()) {
                                        IconButton(onClick = { startDateFilter = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    } else {
                                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { datePickerDialogStart.show() }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = endDateFilter,
                                onValueChange = {},
                                label = { Text("s/d Tanggal") },
                                readOnly = true,
                                trailingIcon = {
                                    if (endDateFilter.isNotBlank()) {
                                        IconButton(onClick = { endDateFilter = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear")
                                        }
                                    } else {
                                        Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { datePickerDialogEnd.show() }
                            )
                        }
                    }

                    // Division Filter Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = divisionFilter,
                            onValueChange = { divisionFilter = it },
                            label = { Text("Filter Berdasarkan Divisi") },
                            trailingIcon = {
                                IconButton(onClick = { divisionExp = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = divisionExp, onDismissRequest = { divisionExp = false }) {
                            DropdownMenuItem(text = { Text("Tampilkan Semua") }, onClick = { divisionFilter = ""; divisionExp = false })
                            divisions.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = { divisionFilter = d; divisionExp = false }
                                )
                            }
                        }
                    }

                    // Item Filter Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = itemFilter,
                            onValueChange = { itemFilter = it },
                            label = { Text("Filter Berdasarkan Item") },
                            trailingIcon = {
                                IconButton(onClick = { itemExp = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = itemExp,
                            onDismissRequest = {
                                itemExp = false
                                itemFilterSearchQuery = ""
                            },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            OutlinedTextField(
                                value = itemFilterSearchQuery,
                                onValueChange = { itemFilterSearchQuery = it },
                                label = { Text("Cari Item...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            DropdownMenuItem(
                                text = { Text("Tampilkan Semua") },
                                onClick = {
                                    itemFilter = ""
                                    itemExp = false
                                    itemFilterSearchQuery = ""
                                }
                            )
                            val filteredItems = customItems.filter {
                                it.codeAndName.lowercase().contains(itemFilterSearchQuery.lowercase())
                            }
                            if (filteredItems.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Item tidak ditemukan") },
                                    onClick = {},
                                    enabled = false
                                )
                            } else {
                                filteredItems.forEach { i ->
                                    DropdownMenuItem(
                                        text = { Text(i.codeAndName) },
                                        onClick = {
                                            itemFilter = i.codeAndName
                                            itemExp = false
                                            itemFilterSearchQuery = ""
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Road Filter Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = roadFilter,
                            onValueChange = { roadFilter = it },
                            label = { Text("Filter Berdasarkan Ruas Jalan") },
                            trailingIcon = {
                                IconButton(onClick = { roadExp = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = roadExp, onDismissRequest = { roadExp = false }) {
                            DropdownMenuItem(text = { Text("Tampilkan Semua") }, onClick = { roadFilter = ""; roadExp = false })
                            roadNames.forEach { r ->
                                DropdownMenuItem(
                                    text = { Text(r.name) },
                                    onClick = { roadFilter = r.name; roadExp = false }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Horizontally Scrollable Table
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Geser tabel ke samping kanan untuk detil →", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text("${filteredList.size} Baris Data", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D5DA3))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada data opname yang cocok dengan filter.", color = Color(0xFF64748B), textAlign = TextAlign.Center)
                        }
                    } else {
                        Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            Column {
                                // Table Header Row
                                Row(
                                    modifier = Modifier
                                        .background(Color(0xFFF1F5F9))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("No", fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                                    Text("Tanggal", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                                    Text("Lokasi", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                                    Text("Sisi", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                                    Text("Ruas Jalan", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp))
                                    Text("Divisi", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp))
                                    Text("Item Pekerjaan", fontWeight = FontWeight.Bold, modifier = Modifier.width(220.dp))
                                    Text("Satuan", fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                                    Text("Panjang", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                    Text("Lebar", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                    Text("Tinggi", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                    Text("Tebal", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                    Text("Luasan", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
                                    Text("Volume Total", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                                    Text("Foto", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp), textAlign = TextAlign.Center)
                                    Text("Aksi", fontWeight = FontWeight.Bold, modifier = Modifier.width(120.dp), textAlign = TextAlign.Center)
                                }

                                // Table Content Rows
                                filteredList.forEachIndexed { index, op ->
                                    Row(
                                        modifier = Modifier
                                            .border(1.dp, Color(0xFFE2E8F0))
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${index + 1}", modifier = Modifier.width(40.dp))
                                        Text(op.dateString, modifier = Modifier.width(100.dp))
                                        Text(op.location, modifier = Modifier.width(100.dp))
                                        Text(op.side, modifier = Modifier.width(60.dp))
                                        Text(op.roadName, modifier = Modifier.width(120.dp))
                                        Text(op.division.substringBefore(":").trim(), modifier = Modifier.width(100.dp))
                                        Text(op.itemName, modifier = Modifier.width(220.dp), softWrap = true)
                                        Text(op.unit, modifier = Modifier.width(60.dp))
                                        Text(viewModel.formatDecimal(op.length), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                        Text(viewModel.formatDecimal(op.width), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                        Text(viewModel.formatDecimal(op.height), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                        Text(viewModel.formatDecimal(op.thickness), modifier = Modifier.width(80.dp), textAlign = TextAlign.End)
                                        Text(viewModel.formatDecimal(op.area), modifier = Modifier.width(100.dp), textAlign = TextAlign.End)
                                        Text(viewModel.formatDecimal(op.calculatedVolume), modifier = Modifier.width(120.dp), textAlign = TextAlign.End)
                                        Box(
                                            modifier = Modifier.width(100.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (op.photoUri.isNotBlank()) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = "Ada", tint = Color(0xFF2E7D32))
                                            } else {
                                                Text("-", color = Color.Gray)
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.width(120.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            IconButton(onClick = {
                                                viewModel.setEditingOpname(op)
                                                viewModel.currentTab.value = "input"
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF0D5DA3))
                                            }
                                            IconButton(onClick = { viewModel.deleteOpname(op.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Export Button
        item {
            Button(
                onClick = {
                    Toast.makeText(context, "Exporting to Excel (CSV)...", Toast.LENGTH_SHORT).show()
                    try {
                        val csvBuilder = StringBuilder()
                        csvBuilder.append("No,Tanggal,Lokasi,Sisi,Ruas Jalan,Divisi,Item Pekerjaan,Satuan,Panjang,Lebar,Tinggi,Tebal,Luasan,Volume Total\n")
                        filteredList.forEachIndexed { idx, op ->
                            csvBuilder.append("${idx + 1},${op.dateString},${op.location},${op.side},${op.roadName},${op.division},${op.itemName},${op.unit},${op.length},${op.width},${op.height},${op.thickness},${op.area},${op.calculatedVolume}\n")
                        }

                        val fileName = "Rekap_Opname_eProject_${System.currentTimeMillis()}.csv"
                        var isSavedPublicly = false

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val resolver = context.contentResolver
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                            }
                            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                            if (uri != null) {
                                resolver.openOutputStream(uri)?.use { outputStream ->
                                    outputStream.write(csvBuilder.toString().toByteArray(Charsets.UTF_8))
                                }
                                isSavedPublicly = true
                            }
                        } else {
                            val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            if (!targetDir.exists()) {
                                targetDir.mkdirs()
                            }
                            val file = File(targetDir, fileName)
                            file.writeText(csvBuilder.toString(), Charsets.UTF_8)
                            isSavedPublicly = true
                        }

                        if (isSavedPublicly) {
                            Toast.makeText(context, "Export berhasil disimpan di folder Download: $fileName", Toast.LENGTH_LONG).show()
                        } else {
                            val path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            val file = File(path, fileName)
                            file.writeText(csvBuilder.toString(), Charsets.UTF_8)
                            Toast.makeText(context, "Export berhasil disimpan di folder aplikasi: ${file.name}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export gagal: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Print / Export ke Excel")
            }
        }
    }
}

// ==========================================
// 5. PROFIL TAB
// ==========================================
@Composable
fun ProfilTab(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUserState.collectAsState()
    val accountsList by viewModel.allAccounts.collectAsState()
    var isEditingName by remember { mutableStateOf(false) }
    var tempPackageName by remember { mutableStateOf(currentUser?.packageName ?: "") }

    val currentEmail = currentUser?.email ?: ""
    val avatarData = remember(currentUser, currentEmail) { viewModel.getProfileAvatar(currentEmail) }
    var showAvatarDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf("") }
    var showIntegrationGuide by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = viewModel.copyUriToInternalStorage(uri)
            if (savedPath != null) {
                viewModel.updateProfileAvatar(currentEmail, "file://$savedPath")
                Toast.makeText(context, "Foto profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showAvatarDialog) {
        Dialog(onDismissRequest = { showAvatarDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ubah Foto Profil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = "Pilih preset avatar atau unggah dari galeri:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Preset Avatars Grid/Row
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val presets = listOf("👷‍♂️", "👷‍♀️", "🛣️", "🌉", "💼", "📊")
                        presets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFFF1F5F9), CircleShape)
                                    .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                                    .clickable {
                                        viewModel.updateProfileAvatar(currentEmail, "emoji:$preset")
                                        showAvatarDialog = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = preset, fontSize = 24.sp)
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Pick from Gallery Button
                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showAvatarDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5DA3)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pilih dari Galeri", color = Color.White)
                    }

                    // Custom URL Input Row
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = { Text("Atau masukkan URL gambar") },
                        placeholder = { Text("https://example.com/foto.jpg") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )

                    if (customUrlInput.isNotBlank()) {
                        Button(
                            onClick = {
                                viewModel.updateProfileAvatar(currentEmail, customUrlInput.trim())
                                customUrlInput = ""
                                showAvatarDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Text("Gunakan URL Gambar", color = Color.White)
                        }
                    }

                    if (avatarData.isNotBlank()) {
                        TextButton(
                            onClick = {
                                viewModel.updateProfileAvatar(currentEmail, "")
                                showAvatarDialog = false
                            }
                        ) {
                            Text("Hapus Foto & Gunakan Inisial", color = Color.Red)
                        }
                    }

                    TextButton(onClick = { showAvatarDialog = false }) {
                        Text("Batal")
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "Profil Pengguna",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clickable { showAvatarDialog = true }
                            .background(Color(0xFFE2E8F0), CircleShape)
                            .border(2.dp, Color(0xFF0D5DA3), CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarData.startsWith("file://") || avatarData.startsWith("http://") || avatarData.startsWith("https://")) {
                            AsyncImage(
                                model = avatarData,
                                contentDescription = "Foto Profil",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else if (avatarData.startsWith("emoji:")) {
                            val emoji = avatarData.removePrefix("emoji:")
                            Text(
                                text = emoji,
                                fontSize = 42.sp,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                text = (currentUser?.email?.take(1) ?: "E").uppercase(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D5DA3)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(onClick = { showAvatarDialog = true }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color(0xFF0D5DA3),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ubah Foto Profil", fontSize = 12.sp, color = Color(0xFF0D5DA3))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentUser?.email ?: "eproject@gmail.com",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    Text(
                        text = "Hak Akses: ${
                            when (currentUser?.role) {
                                "super_admin" -> "Super Admin"
                                "admin" -> "Admin"
                                else -> "Pengguna"
                            }
                        }",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (currentUser?.role == "user") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nomor Akun: ${currentUser?.userNumber}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D5DA3)
                        )
                        Text(
                            text = "Masa Berlaku: Hingga ${currentUser?.expiryDateString}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    // Package Name Editing
                    Text(
                        text = "Nama Paket Pekerjaan:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    if (isEditingName) {
                        OutlinedTextField(
                            value = tempPackageName,
                            onValueChange = { tempPackageName = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { isEditingName = false }) { Text("Batal") }
                            Button(onClick = {
                                viewModel.updateProfilePackageName(tempPackageName)
                                isEditingName = false
                            }) { Text("Simpan") }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentUser?.packageName ?: "-",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D5DA3),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                tempPackageName = currentUser?.packageName ?: ""
                                isEditingName = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Nama Paket", tint = Color(0xFF0D5DA3))
                            }
                        }
                    }
                }
            }
        }

        // --- Super Admin Account List & Approvals ---
        if (currentUser?.role == "super_admin") {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Manajemen Akun Pengguna",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        accountsList.forEach { acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acc.email,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Role: ${acc.role.uppercase()} | Berlaku: ${acc.expiryDateString}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                if (!acc.isApproved) {
                                    Button(
                                        onClick = { viewModel.approveAccount(acc.email) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text("Setujui", fontSize = 11.sp)
                                    }
                                } else {
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            "Aktif",
                                            color = Color(0xFF2E7D32),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                // Delete option (super admins can delete any accounts except themselves)
                                if (acc.email.lowercase() != "eproject.admin@gmail.com") {
                                    IconButton(onClick = { viewModel.deleteAccount(acc.email) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus Akun", tint = Color.Red)
                                    }
                                }
                            }
                            Divider()
                        }
                    }
                }
            }
        }

        // --- Integration Guide Card ---
        if (currentUser?.role == "super_admin") {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    border = BorderStroke(1.dp, Color(0xFF90CAF9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showIntegrationGuide = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = Color(0xFF0D5DA3),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Panduan Integrasi Cloud",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D5DA3)
                            )
                            Text(
                                text = "Langkah integrasi eProject dengan GitHub, Firebase, dan Vercel.",
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF0D5DA3),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Logout button
        item {
            Button(
                onClick = { viewModel.logout() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Keluar Aplikasi (Logout)", color = Color.White)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "eProject by Dendy Sofian ©2024",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showIntegrationGuide) {
        Dialog(onDismissRequest = { showIntegrationGuide = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Panduan Integrasi Cloud",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D5DA3)
                        )
                        IconButton(onClick = { showIntegrationGuide = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "eProject by Dendy Sofian ditenagai oleh integrasi cloud modern yang aman, andal, dan modular. Ikuti panduan lengkap berikut untuk menghubungkan codebase Anda dengan GitHub, Firebase, dan Vercel.",
                            fontSize = 13.sp,
                            color = Color(0xFF334155)
                        )

                        // 1. GITHUB
                        Text("1. Integrasi GitHub (Source Control & CI/CD)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0D5DA3))
                        Text(
                            text = "• Hubungkan Codebase ke GitHub:\n" +
                                   "  Inisialisasi git pada direktori proyek, buat repository baru di GitHub, lalu push kode menggunakan:\n" +
                                   "  `git remote add origin <url_github_anda>`\n" +
                                   "  `git push -u origin main`\n\n" +
                                   "• Konfigurasi CI/CD Otomatis (GitHub Actions):\n" +
                                   "  Buat folder `.github/workflows/` dan tambahkan file `android.yml` untuk melakukan kompilasi otomatis (build APK) setiap kali Anda melakukan push ke branch `main`.\n\n" +
                                   "• Sinkronisasi Repo:\n" +
                                   "  Gunakan repositori GitHub ini untuk berkolaborasi dengan tim pengembang serta mengamankan source code secara cloud.",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )

                        // 2. FIREBASE
                        Text("2. Firebase Console (Auth, Database, & Storage)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0D5DA3))
                        Text(
                            text = "Aplikasi ini dapat langsung diintegrasikan dengan Google Firebase Console untuk menangani autentikasi, penyimpanan data terpusat, dan foto dokumentasi.\n\n" +
                                   "• Registrasi Proyek:\n" +
                                   "  Buka Firebase Console, klik 'Add Project', beri nama proyek (e.g. 'eProject Kaltara'). Daftarkan aplikasi Android dengan package name 'com.example' (atau sesuai applicationId Anda).\n\n" +
                                   "• Unduh google-services.json:\n" +
                                   "  Setelah registrasi, unduh file `google-services.json` dan taruh di folder `/app` proyek Anda.\n\n" +
                                   "• Autentikasi Pengguna (Sign In / Sign Up):\n" +
                                   "  Di Firebase sidebar, pilih 'Build' -> 'Authentication' -> aktifkan provider 'Email/Password' agar pendaftaran dan login akun berjalan secara aman.\n\n" +
                                   "• Penyimpanan Data Terpusat (Firestore / Realtime Database):\n" +
                                   "  Pilih 'Build' -> 'Firestore Database', klik 'Create Database', pilih 'Start in test mode' (atau konfigurasikan security rules). Firestore akan menyimpan data akun, rincian kontrak, dan laporan opname secara realtime.\n\n" +
                                   "• Foto Dokumentasi Lapangan (Firebase Storage):\n" +
                                   "  Pilih 'Build' -> 'Storage', aktifkan Firebase Storage. Foto opname yang diambil di lapangan akan di-upload ke bucket Storage dan link URL fotonya disimpan pada data opname Firestore.",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )

                        // 3. VERCEL
                        Text("3. Integrasi Vercel (Web Dashboard & Admin View)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0D5DA3))
                        Text(
                            text = "Untuk memantau progress opname secara real-time dari kantor, Anda dapat mendeploy web dashboard (React/Next.js/HTML kustom) menggunakan Vercel.\n\n" +
                                   "• Deployment Instan via GitHub:\n" +
                                   "  Masuk ke vercel.com menggunakan akun GitHub Anda. Klik 'Add New' -> 'Project' -> pilih repository eProject yang sudah Anda push ke GitHub.\n\n" +
                                   "• Konfigurasi Environment Variables:\n" +
                                   "  Jika web dashboard memerlukan API key Firebase, tambahkan di bagian 'Environment Variables' pada dashboard proyek Vercel agar tersimpan aman.\n\n" +
                                   "• CI/CD Deployment Otomatis:\n" +
                                   "  Setiap kali Anda push update frontend ke GitHub, Vercel akan otomatis mem-build dan merilis versi terbaru dashboard Anda dalam hitungan detik.",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )

                        // 4. ALUR AUTO SYNC
                        Text("4. Alur Sinkronisasi Otomatis", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0D5DA3))
                        Text(
                            text = "• Saat Offline:\n" +
                                   "  Semua data kontrak, akun, dan opname (termasuk foto) disimpan dengan aman di database lokal Room.\n\n" +
                                   "• Saat Online:\n" +
                                   "  Aplikasi mendeteksi koneksi internet secara cerdas dan langsung melakukan trigger sinkronisasi otomatis ke Google Sheets / Firebase API, memastikan data lapangan selalu ter-update di cloud secara real-time.",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = { showIntegrationGuide = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5DA3)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Saya Mengerti", color = Color.White)
                    }
                }
            }
        }
    }
}
