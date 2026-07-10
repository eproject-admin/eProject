package com.example.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AppRepository(private val context: Context, private val dao: AppDao) {

    private val firestore = FirebaseFirestore.getInstance()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val syncScope = CoroutineScope(Dispatchers.IO)

    val allAccounts: Flow<List<AccountEntity>> = dao.getAllAccounts()
    val allContracts: Flow<List<ContractEntity>> = dao.getAllContracts()
    val allOpnames: Flow<List<OpnameEntity>> = dao.getAllOpnames()

    // --- Accounts ---
    suspend fun getAccountByEmail(email: String): AccountEntity? {
        // Special case: Default Super Admin
        if (email.lowercase() == "eproject.admin@gmail.com") {
            val dbAdmin = dao.getAccountByEmail(email)
            if (dbAdmin == null) {
                val superAdmin = AccountEntity(
                    email = "eproject.admin@gmail.com",
                    passwordHash = "eProject@2026",
                    role = "super_admin",
                    isApproved = true,
                    userNumber = "ADM-000001",
                    expiryDateString = "Unlimited",
                    packageName = "BPJN Kalimantan Utara Admin",
                )
                dao.insertAccount(superAdmin)
                return superAdmin
            }
            return dbAdmin
        }
        return dao.getAccountByEmail(email)
    }

    suspend fun insertAccount(account: AccountEntity) {
        dao.insertAccount(account)
        triggerSync()
    }

    suspend fun deleteAccountByEmail(email: String) {
        dao.deleteAccountByEmail(email)
        triggerSync()
    }

    suspend fun updatePackageName(email: String, packageName: String) {
        dao.updatePackageName(email, packageName)
        triggerSync()
    }

    suspend fun updatePPN(email: String, ppnPercentage: Double) {
        dao.updatePPN(email, ppnPercentage)
        triggerSync()
    }

    // --- Contracts ---
    fun getContractsByUser(email: String): Flow<List<ContractEntity>> = dao.getContractsByUser(email)

    suspend fun insertContract(contract: ContractEntity) {
        dao.insertContract(contract)
        triggerSync()
    }

    suspend fun updateContract(contract: ContractEntity) {
        dao.updateContract(contract)
        triggerSync()
    }

    suspend fun deleteContractById(id: Int) {
        dao.deleteContractById(id)
        triggerSync()
    }

    // --- Opnames ---
    fun getOpnamesByUser(email: String): Flow<List<OpnameEntity>> = dao.getOpnamesByUser(email)

    suspend fun insertOpname(opname: OpnameEntity) {
        dao.insertOpname(opname)
        triggerSync()
    }

    suspend fun updateOpname(opname: OpnameEntity) {
        dao.updateOpname(opname)
        triggerSync()
    }

    suspend fun deleteOpnameById(id: Int) {
        dao.deleteOpnameById(id)
        triggerSync()
    }

    // --- Road Names ---
    fun getRoadNamesByUser(email: String): Flow<List<RoadNameEntity>> = dao.getRoadNamesByUser(email)

    suspend fun insertRoadName(road: RoadNameEntity) {
        dao.insertRoadName(road)
    }

    suspend fun deleteRoadNameById(id: Int) {
        dao.deleteRoadNameById(id)
    }

    // --- Custom Contract Items ---
    fun getCustomItemsFlow(): Flow<List<CustomContractItemEntity>> = dao.getCustomItemsFlow()

    suspend fun insertCustomItem(item: CustomContractItemEntity) {
        dao.insertCustomItem(item)
    }

    suspend fun deleteCustomItemById(id: Int) {
        dao.deleteCustomItemById(id)
    }

    // --- Custom Units ---
    fun getCustomUnitsFlow(): Flow<List<CustomUnitEntity>> = dao.getCustomUnitsFlow()

    suspend fun insertCustomUnit(unit: CustomUnitEntity) {
        dao.insertCustomUnit(unit)
    }

    suspend fun deleteCustomUnitById(id: Int) {
        dao.deleteCustomUnitById(id)
    }

    suspend fun seedIfEmpty() {
        val items = dao.getCustomItems()
        val units = dao.getCustomUnits()
        if (items.isEmpty() || units.isEmpty()) {
            AppDatabase.seedDefaultData(dao)
        }
    }

    // --- Sync Methods ---
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun triggerSync() {
        if (!isNetworkAvailable()) {
            Log.d("AppRepository", "Sync skipped: No Internet Connection")
            return
        }

        syncToFirestore()
        
        syncScope.launch {
            try {
                // Fetch all data for sync
                val accountsList = dao.getAllAccounts().first()
                val contractsList = dao.getAllContracts().first()
                val opnamesList = dao.getAllOpnames().first()

                val payload = JSONObject().apply {
                    put("action", "syncData")
                    
                    val accountsArray = JSONArray()
                    accountsList.forEach { acc ->
                        accountsArray.put(
                            JSONObject().apply {
                                put("email", acc.email)
                                put("role", acc.role)
                                put("isApproved", acc.isApproved)
                                put("userNumber", acc.userNumber)
                                put("expiryDateString", acc.expiryDateString)
                                put("packageName", acc.packageName)
                            },
                        )
                    }
                    put("accounts", accountsArray)

                    val contractsArray = JSONArray()
                    contractsList.forEach { con ->
                        contractsArray.put(
                            JSONObject().apply {
                                put("id", con.id)
                                put("userEmail", con.userEmail)
                                put("division", con.division)
                                put("itemCodeAndName", con.itemCodeAndName)
                                put("unit", con.unit)
                                put("volume", con.volume)
                                put("unitPrice", con.unitPrice)
                            },
                        )
                    }
                    put("contracts", contractsArray)

                    val opnamesArray = JSONArray()
                    opnamesList.forEach { op ->
                        opnamesArray.put(
                            JSONObject().apply {
                                put("id", op.id)
                                put("userEmail", op.userEmail)
                                put("location", op.location)
                                put("side", op.side)
                                put("roadName", op.roadName)
                                put("dateString", op.dateString)
                                put("unit", op.unit)
                                put("length", op.length)
                                put("width", op.width)
                                put("height", op.height)
                                put("thickness", op.thickness)
                                put("area", op.area)
                                put("density", op.density)
                                put("division", op.division)
                                put("itemName", op.itemName)
                                put("calculatedVolume", op.calculatedVolume)
                            },
                        )
                    }
                    put("opnames", opnamesArray)
                }

                val url = "https://script.google.com/macros/s/AKfycbz7H74eVMyUD3DzedctdlWdaCZgmCbrysjiiJkNXRTPwxNDvmBfhRQKOYM2YnKWb39-Xw/exec"
                val body = payload.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.d("AppRepository", "Auto Sync to Apps Script succeeded!")
                        // Optionally update DB to mark isSynced = true
                    } else {
                        Log.e("AppRepository", "Auto Sync failed: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AppRepository", "Auto Sync error: ${e.message}")
            }
        }
    }

    fun syncToFirestore() {
        if (!isNetworkAvailable()) return

        syncScope.launch {
            try {
                val accounts = dao.getAllAccounts().first()
                val contracts = dao.getAllContracts().first()
                val opnames = dao.getAllOpnames().first()

                accounts.forEach { acc ->
                    firestore.collection("accounts").document(acc.email).set(acc)
                        .addOnSuccessListener { Log.d("Firestore", "Account synced: ${acc.email}") }
                        .addOnFailureListener { e -> Log.e("Firestore", "Account sync failed", e) }
                }

                contracts.forEach { con ->
                    firestore.collection("contracts").document(con.id.toString()).set(con)
                        .addOnSuccessListener { Log.d("Firestore", "Contract synced: ${con.id}") }
                        .addOnFailureListener { e -> Log.e("Firestore", "Contract sync failed", e) }
                }

                opnames.forEach { op ->
                    firestore.collection("opnames").document(op.id.toString()).set(op)
                        .addOnSuccessListener { 
                            Log.d("Firestore", "Opname synced: ${op.id}")
                            // Mark as synced locally if needed
                            syncScope.launch {
                                dao.updateOpname(op.copy(isSynced = true))
                            }
                        }
                        .addOnFailureListener { e -> Log.e("Firestore", "Opname sync failed", e) }
                }
            } catch (e: Exception) {
                Log.e("AppRepository", "Firestore Sync Error: ${e.message}")
            }
        }
    }

    // --- Extra Firestore Helpers ---
    fun fetchAccountsFromFirestore(onResult: (List<AccountEntity>) -> Unit) {
        firestore.collection("accounts").get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(AccountEntity::class.java)
                onResult(list)
            }
            .addOnFailureListener { 
                onResult(emptyList())
            }
    }

    fun saveContractToFirestore(contract: ContractEntity) {
        firestore.collection("contracts").document(contract.id.toString()).set(contract)
            .addOnSuccessListener { Log.d("Firestore", "Contract saved: ${contract.id}") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error saving contract", e) }
    }

    fun saveOpnameToFirestore(opname: OpnameEntity) {
        firestore.collection("opnames").document(opname.id.toString()).set(opname)
            .addOnSuccessListener { Log.d("Firestore", "Opname saved: ${opname.id}") }
            .addOnFailureListener { e -> Log.e("Firestore", "Error saving opname", e) }
    }
}
