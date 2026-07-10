package com.example.ui

import android.app.Application
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application, val repository: AppRepository) : AndroidViewModel(application) {

    // --- Auth States ---
    val currentUserState = MutableStateFlow<AccountEntity?>(null)
    val loginError = MutableStateFlow<String?>(null)
    val signupSuccess = MutableStateFlow<String?>(null)

    // --- Admin Views ---
    // The admin/super_admin can view another user's contract/opname by searching/entering their email.
    val searchedUserEmail = MutableStateFlow("")
    val searchedUserAccount = MutableStateFlow<AccountEntity?>(null)

    // --- Navigation ---
    val currentRoute = MutableStateFlow("login") // login, register, main

    // --- Bottom Nav Tab ---
    val currentTab = MutableStateFlow("beranda") // beranda, kontrak, input, opname, profil

    // --- Master Data (Bina Marga & Units) ---
    val customItems = repository.getCustomItemsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customUnits = repository.getCustomUnitsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Contracts ---
    // For normal user, show their own contracts. For admin, show searched user's contracts if email matches.
    val contractList = combine(repository.allContracts, currentUserState, searchedUserEmail) { all, current, search ->
        if (current == null) emptyList()
        else if (current.role == "super_admin") {
            all // super admin can see everything
        } else if (current.role == "admin") {
            // Admin only sees searched user's contract
            if (search.isNotBlank()) all.filter { it.userEmail.lowercase() == search.lowercase() }
            else emptyList()
        } else {
            // Regular user sees their own
            all.filter { it.userEmail.lowercase() == current.email.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Opnames ---
    val opnameList = combine(repository.allOpnames, currentUserState, searchedUserEmail) { all, current, search ->
        if (current == null) emptyList()
        else if (current.role == "super_admin") {
            all
        } else if (current.role == "admin") {
            if (search.isNotBlank()) all.filter { it.userEmail.lowercase() == search.lowercase() }
            else emptyList()
        } else {
            all.filter { it.userEmail.lowercase() == current.email.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Road Names ---
    val roadNames = currentUserState.flatMapLatest { user ->
        if (user != null) repository.getRoadNamesByUser(user.email)
        else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Accounts List (for Super Admin) ---
    val allAccounts = repository.allAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Input Form States (Contract) ---
    val contractDivision = MutableStateFlow("Divisi 1 : Umum dan Penerapan SMKK")
    val contractItem = MutableStateFlow("")
    val contractUnit = MutableStateFlow("m3")
    val contractVolume = MutableStateFlow("")
    val contractUnitPrice = MutableStateFlow("")

    // --- Input Form States (Opname) ---
    val opnameLocation = MutableStateFlow("")
    val opnameSide = MutableStateFlow("Kanan") // Kanan, Kiri, CL
    val opnameRoadName = MutableStateFlow("")
    val opnameDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val opnameUnit = MutableStateFlow("m3")
    val opnameLength = MutableStateFlow("")
    val opnameWidth = MutableStateFlow("")
    val opnameHeight = MutableStateFlow("")
    val opnameThickness = MutableStateFlow("")
    val opnameArea = MutableStateFlow("")
    val opnameAreaMode = MutableStateFlow("rumus") // "rumus" or "manual"
    val opnameDensity = MutableStateFlow("1.0")
    val opnameQuantity = MutableStateFlow("1.0") // Jumlah
    val opnamePhotoUri = MutableStateFlow("")
    val opnameDivision = MutableStateFlow("Divisi 1 : Umum dan Penerapan SMKK")
    val opnameItem = MutableStateFlow("")

    // Editing State trackers
    var editingContractId: Int? = null
    var editingOpnameId: Int? = null

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
        }
        // Fetch custom items and select first item as default for form
        viewModelScope.launch {
            customItems.collectLatest { list ->
                if (list.isNotEmpty()) {
                    if (contractItem.value.isBlank()) {
                        contractItem.value = list.first().codeAndName
                    }
                    if (opnameItem.value.isBlank()) {
                        opnameItem.value = list.first().codeAndName
                    }
                }
            }
        }
    }

    // --- Actions ---

    fun login(email: String, sandi: String) {
        viewModelScope.launch {
            loginError.value = null
            // Check default super admin
            if (email.lowercase() == "eproject.admin@gmail.com" && sandi == "eProject@2026") {
                val superAdmin = repository.getAccountByEmail("eproject.admin@gmail.com")
                currentUserState.value = superAdmin
                currentRoute.value = "main"
                return@launch
            }

            val account = repository.getAccountByEmail(email)
            if (account == null) {
                loginError.value = "Akun tidak ditemukan. Silakan mendaftar terlebih dahulu."
            } else if (account.passwordHash != sandi) {
                loginError.value = "Kata sandi salah."
            } else if (!account.isApproved) {
                loginError.value = "Pendaftaran akun Anda sedang menunggu persetujuan dari eproject.admin@gmail.com."
            } else {
                currentUserState.value = account
                currentRoute.value = "main"
            }
        }
    }

    fun signUp(email: String, sandi: String, role: String, packageName: String) {
        viewModelScope.launch {
            loginError.value = null
            signupSuccess.value = null

            if (email.isBlank() || sandi.isBlank() || packageName.isBlank()) {
                loginError.value = "Harap isi semua kolom pendaftaran."
                return@launch
            }

            val existing = repository.getAccountByEmail(email)
            if (existing != null) {
                loginError.value = "Email sudah terdaftar."
                return@launch
            }

            // Generate user number if user
            val userNum = if (role == "user") {
                "USR-${(100000..999999).random()}"
            } else {
                ""
            }

            // Expiry date is 1 year from now
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, 1)
            val expiryFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val expiryStr = if (role == "user") expiryFormat.format(calendar.time) else "Unlimited"

            val newAccount = AccountEntity(
                email = email,
                passwordHash = sandi,
                role = role,
                isApproved = false, // Must be approved by super admin
                userNumber = userNum,
                expiryDateString = expiryStr,
                packageName = packageName
            )

            repository.insertAccount(newAccount)
            signupSuccess.value = "Pendaftaran berhasil! Akun Anda menunggu persetujuan oleh eproject.admin@gmail.com."
        }
    }

    fun approveAccount(email: String) {
        viewModelScope.launch {
            val acc = repository.getAccountByEmail(email)
            if (acc != null) {
                val updated = acc.copy(isApproved = true)
                repository.insertAccount(updated)
            }
        }
    }

    fun deleteAccount(email: String) {
        viewModelScope.launch {
            repository.deleteAccountByEmail(email)
        }
    }

    fun searchUserByEmail(email: String) {
        viewModelScope.launch {
            searchedUserEmail.value = email
            val acc = repository.getAccountByEmail(email)
            searchedUserAccount.value = acc
        }
    }

    fun logout() {
        currentUserState.value = null
        searchedUserEmail.value = ""
        searchedUserAccount.value = null
        currentRoute.value = "login"
    }

    // --- Contract Management ---

    fun saveContract() {
        val user = currentUserState.value ?: return
        val email = user.email

        val vol = contractVolume.value.toDoubleOrNull() ?: 0.0
        val price = contractUnitPrice.value.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            if (editingContractId != null) {
                val updated = ContractEntity(
                    id = editingContractId!!,
                    userEmail = email,
                    division = contractDivision.value,
                    itemCodeAndName = contractItem.value,
                    unit = contractUnit.value,
                    volume = vol,
                    unitPrice = price
                )
                repository.updateContract(updated)
                editingContractId = null
            } else {
                val newContract = ContractEntity(
                    userEmail = email,
                    division = contractDivision.value,
                    itemCodeAndName = contractItem.value,
                    unit = contractUnit.value,
                    volume = vol,
                    unitPrice = price
                )
                repository.insertContract(newContract)
            }
            // Reset fields
            contractVolume.value = ""
            contractUnitPrice.value = ""
        }
    }

    fun setEditingContract(contract: ContractEntity) {
        editingContractId = contract.id
        contractDivision.value = contract.division
        contractItem.value = contract.itemCodeAndName
        contractUnit.value = contract.unit
        contractVolume.value = contract.volume.toString()
        contractUnitPrice.value = contract.unitPrice.toString()
    }

    fun deleteContract(id: Int) {
        viewModelScope.launch {
            repository.deleteContractById(id)
        }
    }

    fun insertAllDefaultItemsToContract() {
        val user = currentUserState.value ?: return
        val email = user.email
        viewModelScope.launch {
            val existingItemNames = contractList.value.map { it.itemCodeAndName }.toSet()
            val itemsToInsert = customItems.value.filter { it.codeAndName !in existingItemNames }
            itemsToInsert.forEach { item ->
                val newContract = ContractEntity(
                    userEmail = email,
                    division = item.divisionCode,
                    itemCodeAndName = item.codeAndName,
                    unit = item.defaultUnit,
                    volume = 0.0,
                    unitPrice = 0.0
                )
                repository.insertContract(newContract)
            }
        }
    }

    fun increaseContractVolume(contract: ContractEntity) {
        viewModelScope.launch {
            val updated = contract.copy(volume = contract.volume + 1.0)
            repository.updateContract(updated)
        }
    }

    fun decreaseContractVolume(contract: ContractEntity) {
        viewModelScope.launch {
            val newVolume = (contract.volume - 1.0).coerceAtLeast(0.0)
            val updated = contract.copy(volume = newVolume)
            repository.updateContract(updated)
        }
    }

    // --- Opname Management ---

    fun calculateOpnameVolumePreview(): Double {
        val len = opnameLength.value.toDoubleOrNull() ?: 0.0
        val wid = opnameWidth.value.toDoubleOrNull() ?: 0.0
        val hgt = opnameHeight.value.toDoubleOrNull() ?: 0.0
        val thick = opnameThickness.value.toDoubleOrNull() ?: 0.0
        val qnty = opnameQuantity.value.toDoubleOrNull() ?: 1.0
        val dens = opnameDensity.value.toDoubleOrNull() ?: 1.0
        
        val areaVal = getParsedAreaValue()

        return when (opnameUnit.value.lowercase()) {
            "m3" -> {
                if (hgt > 0.0) {
                    if (opnameAreaMode.value == "manual") areaVal * hgt * qnty
                    else len * wid * hgt * qnty
                } else {
                    if (opnameAreaMode.value == "manual") areaVal * (thick / 100.0) * qnty
                    else len * wid * (thick / 100.0) * qnty
                }
            }
            "ton" -> {
                val thickInM = thick / 100.0
                if (opnameAreaMode.value == "manual") areaVal * thickInM * dens * qnty
                else len * wid * thickInM * dens * qnty
            }
            "m2" -> {
                areaVal * qnty
            }
            "m" -> {
                len * qnty
            }
            else -> qnty
        }
    }

    fun getOpnameCalculationFormulaString(): String {
        val len = opnameLength.value.toDoubleOrNull() ?: 0.0
        val wid = opnameWidth.value.toDoubleOrNull() ?: 0.0
        val hgt = opnameHeight.value.toDoubleOrNull() ?: 0.0
        val thick = opnameThickness.value.toDoubleOrNull() ?: 0.0
        val qnty = opnameQuantity.value.toDoubleOrNull() ?: 1.0
        val dens = opnameDensity.value.toDoubleOrNull() ?: 1.0

        val areaVal = getParsedAreaValue()
        val areaStr = if (opnameAreaMode.value == "manual") {
            val expr = opnameArea.value
            val evaluated = evaluateExpression(expr)
            if (evaluated != null && expr != formatDecimal(evaluated)) {
                "$expr (= ${formatDecimal(evaluated)}) m2"
            } else {
                "${formatDecimal(areaVal)} m2"
            }
        } else {
            "$len m x $wid m"
        }

        return when (opnameUnit.value.lowercase()) {
            "m3" -> {
                if (hgt > 0.0) {
                    if (opnameAreaMode.value == "manual") "$areaStr x $hgt m x $qnty"
                    else "$len m x $wid m x $hgt m x $qnty"
                } else {
                    if (opnameAreaMode.value == "manual") "$areaStr x ${thick / 100.0} m x $qnty"
                    else "$len m x $wid m x ${thick / 100.0} m x $qnty"
                }
            }
            "ton" -> {
                if (opnameAreaMode.value == "manual") "$areaStr x ${thick / 100.0} m x $dens (BJ) x $qnty"
                else "$len m x $wid m x ${thick / 100.0} m x $dens (BJ) x $qnty"
            }
            "m2" -> {
                if (opnameAreaMode.value == "manual") "$areaStr x $qnty"
                else "$len m x $wid m x $qnty"
            }
            "m" -> {
                "$len m x $qnty"
            }
            else -> "$qnty"
        }
    }

    fun saveOpname() {
        val user = currentUserState.value ?: return
        val email = user.email

        val len = opnameLength.value.toDoubleOrNull() ?: 0.0
        val wid = opnameWidth.value.toDoubleOrNull() ?: 0.0
        val hgt = opnameHeight.value.toDoubleOrNull() ?: 0.0
        val thick = opnameThickness.value.toDoubleOrNull() ?: 0.0
        val areaVal = getParsedAreaValue()
        val dens = opnameDensity.value.toDoubleOrNull() ?: 1.0
        val qnty = opnameQuantity.value.toDoubleOrNull() ?: 1.0
        val calculatedVol = calculateOpnameVolumePreview()

        viewModelScope.launch {
            if (editingOpnameId != null) {
                val updated = OpnameEntity(
                    id = editingOpnameId!!,
                    userEmail = email,
                    location = opnameLocation.value,
                    side = opnameSide.value,
                    roadName = opnameRoadName.value,
                    dateString = opnameDate.value,
                    unit = opnameUnit.value,
                    length = len,
                    width = wid,
                    height = hgt,
                    thickness = thick,
                    area = areaVal,
                    density = dens,
                    photoUri = opnamePhotoUri.value,
                    division = opnameDivision.value,
                    itemName = opnameItem.value,
                    calculatedVolume = calculatedVol
                )
                repository.updateOpname(updated)
                editingOpnameId = null
            } else {
                val newOpname = OpnameEntity(
                    userEmail = email,
                    location = opnameLocation.value,
                    side = opnameSide.value,
                    roadName = opnameRoadName.value,
                    dateString = opnameDate.value,
                    unit = opnameUnit.value,
                    length = len,
                    width = wid,
                    height = hgt,
                    thickness = thick,
                    area = areaVal,
                    density = dens,
                    photoUri = opnamePhotoUri.value,
                    division = opnameDivision.value,
                    itemName = opnameItem.value,
                    calculatedVolume = calculatedVol
                )
                repository.insertOpname(newOpname)
            }

            // Clear values
            opnameLocation.value = ""
            opnameLength.value = ""
            opnameWidth.value = ""
            opnameHeight.value = ""
            opnameThickness.value = ""
            opnameArea.value = ""
            opnameAreaMode.value = "rumus"
            opnameDensity.value = "1.0"
            opnameQuantity.value = "1.0"
            opnamePhotoUri.value = ""
        }
    }

    fun setEditingOpname(opname: OpnameEntity) {
        editingOpnameId = opname.id
        opnameLocation.value = opname.location
        opnameSide.value = opname.side
        opnameRoadName.value = opname.roadName
        opnameDate.value = opname.dateString
        opnameUnit.value = opname.unit
        opnameLength.value = opname.length.toString()
        opnameWidth.value = opname.width.toString()
        opnameHeight.value = opname.height.toString()
        opnameThickness.value = opname.thickness.toString()
        opnameArea.value = opname.area.toString()
        
        if (opname.area > 0.0 && Math.abs(opname.area - (opname.length * opname.width)) > 0.001) {
            opnameAreaMode.value = "manual"
        } else {
            opnameAreaMode.value = "rumus"
        }
        
        opnameDensity.value = opname.density.toString()
        opnamePhotoUri.value = opname.photoUri
        opnameDivision.value = opname.division
        opnameItem.value = opname.itemName
    }

    fun deleteOpname(id: Int) {
        viewModelScope.launch {
            repository.deleteOpnameById(id)
        }
    }

    // --- Road Name Management ---

    fun addRoadName(name: String) {
        val user = currentUserState.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertRoadName(RoadNameEntity(name = name, userEmail = user.email))
            opnameRoadName.value = name
        }
    }

    fun deleteRoadName(id: Int) {
        viewModelScope.launch {
            repository.deleteRoadNameById(id)
        }
    }

    // --- Custom Contract Items ---

    fun addCustomItem(division: String, codeAndName: String, unit: String) {
        viewModelScope.launch {
            repository.insertCustomItem(CustomContractItemEntity(divisionCode = division, codeAndName = codeAndName, defaultUnit = unit))
        }
    }

    fun deleteCustomItem(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomItemById(id)
        }
    }

    // --- Custom Units ---

    fun addCustomUnit(name: String) {
        viewModelScope.launch {
            repository.insertCustomUnit(CustomUnitEntity(name = name))
        }
    }

    fun deleteCustomUnit(id: Int) {
        viewModelScope.launch {
            repository.deleteCustomUnitById(id)
        }
    }

    // --- Profile Management ---

    fun getProfileAvatar(email: String): String {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("eproject_prefs", android.content.Context.MODE_PRIVATE)
        return sharedPrefs.getString("avatar_$email", "") ?: ""
    }

    fun updateProfileAvatar(email: String, avatarData: String) {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("eproject_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("avatar_$email", avatarData).apply()
        val current = currentUserState.value
        if (current != null && current.email == email) {
            currentUserState.value = current.copy(registerTimestamp = current.registerTimestamp + 1) // slightly vary the copy to guarantee state change trigger
        }
    }

    fun copyUriToInternalStorage(uri: Uri): String? {
        return try {
            val context = getApplication<Application>()
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(context.filesDir, "avatar_$timeStamp.jpg")
            file.outputStream().use { outputStream ->
                inputStream.use { it.copyTo(outputStream) }
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e("AppViewModel", "Error copying uri: ", e)
            null
        }
    }

    fun updateProfilePackageName(newName: String) {
        val user = currentUserState.value ?: return
        viewModelScope.launch {
            repository.updatePackageName(user.email, newName)
            currentUserState.value = user.copy(packageName = newName)
        }
    }

    fun updateProfilePPN(ppn: Double) {
        val user = currentUserState.value ?: return
        viewModelScope.launch {
            repository.updatePPN(user.email, ppn)
            currentUserState.value = user.copy(ppnPercentage = ppn)
        }
    }

    // --- Helper formatting ---
    fun formatCurrency(amount: Double): String {
        return "Rp " + String.format(Locale("in", "ID"), "%,.2f", amount)
    }

    fun formatDecimal(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    fun formatDecimalWithSeparator(value: Double): String {
        val symbols = java.text.DecimalFormatSymbols(Locale("in", "ID"))
        val df = java.text.DecimalFormat("#,##0.00", symbols)
        return df.format(value)
    }

    fun getSortedContracts(list: List<ContractEntity>): List<ContractEntity> {
        return list.sortedWith { a, b ->
            val divA = Regex("""Divisi\s+(\d+)""").find(a.division)?.groupValues?.get(1)?.toIntOrNull() ?: 999
            val divB = Regex("""Divisi\s+(\d+)""").find(b.division)?.groupValues?.get(1)?.toIntOrNull() ?: 999
            if (divA != divB) {
                divA.compareTo(divB)
            } else {
                val codePartA = a.itemCodeAndName.split(" ").firstOrNull() ?: ""
                val codePartB = b.itemCodeAndName.split(" ").firstOrNull() ?: ""
                val numsA = Regex("""\d+""").findAll(codePartA).map { it.value.toIntOrNull() ?: 0 }.toList()
                val numsB = Regex("""\d+""").findAll(codePartB).map { it.value.toIntOrNull() ?: 0 }.toList()
                
                var result = 0
                val minSize = minOf(numsA.size, numsB.size)
                for (i in 0 until minSize) {
                    val cmp = numsA[i].compareTo(numsB[i])
                    if (cmp != 0) {
                        result = cmp
                        break
                    }
                }
                if (result == 0) {
                    numsA.size.compareTo(numsB.size)
                } else {
                    result
                }
            }
        }
    }

    fun getParsedAreaValue(): Double {
        if (opnameAreaMode.value == "rumus") {
            val len = opnameLength.value.toDoubleOrNull() ?: 0.0
            val wid = opnameWidth.value.toDoubleOrNull() ?: 0.0
            return len * wid
        } else {
            val expr = opnameArea.value
            val evaluated = evaluateExpression(expr)
            if (evaluated != null) {
                return evaluated
            }
            return expr.toDoubleOrNull() ?: 0.0
        }
    }

    fun evaluateExpression(expr: String): Double? {
        try {
            val cleanExpr = expr.replace(",", ".").replace(" ", "")
            if (cleanExpr.isBlank()) return null
            
            return object : Any() {
                var pos = -1
                var ch = 0

                fun nextChar() {
                    ch = if (++pos < cleanExpr.length) cleanExpr[pos].code else -1
                }

                fun eat(charToEat: Int): Boolean {
                    while (ch == ' '.code) nextChar()
                    if (ch == charToEat) {
                        nextChar()
                        return true
                    }
                    return false
                }

                fun parse(): Double {
                    nextChar()
                    val x = parseExpression()
                    if (pos < cleanExpr.length) throw RuntimeException("Unexpected: " + ch.toChar())
                    return x
                }

                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        if (eat('+'.code)) x += parseTerm()
                        else if (eat('-'.code)) x -= parseTerm()
                        else return x
                    }
                }

                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        if (eat('*'.code)) x *= parseFactor()
                        else if (eat('/'.code)) x /= parseFactor()
                        else return x
                    }
                }

                fun parseFactor(): Double {
                    if (eat('+'.code)) return parseFactor()
                    if (eat('-'.code)) return -parseFactor()

                    var x: Double
                    val startPos = pos
                    if (eat('('.code)) {
                        x = parseExpression()
                        eat(')'.code)
                    } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
                        while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                        x = cleanExpr.substring(startPos, pos).toDouble()
                    } else {
                        throw RuntimeException("Unexpected: " + ch.toChar())
                    }
                    return x
                }
            }.parse()
        } catch (e: Exception) {
            return null
        }
    }
}
