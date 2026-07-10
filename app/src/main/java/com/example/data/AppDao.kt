package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Accounts ---
    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): AccountEntity?

    @Query("SELECT * FROM accounts ORDER BY registerTimestamp DESC")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE email = :email")
    suspend fun deleteAccountByEmail(email: String)

    @Query("UPDATE accounts SET packageName = :packageName WHERE email = :email")
    suspend fun updatePackageName(email: String, packageName: String)

    @Query("UPDATE accounts SET ppnPercentage = :ppnPercentage WHERE email = :email")
    suspend fun updatePPN(email: String, ppnPercentage: Double)


    // --- Contracts ---
    @Query("SELECT * FROM contracts WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getContractsByUser(email: String): Flow<List<ContractEntity>>

    @Query("SELECT * FROM contracts ORDER BY timestamp DESC")
    fun getAllContracts(): Flow<List<ContractEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: ContractEntity)

    @Update
    suspend fun updateContract(contract: ContractEntity)

    @Query("DELETE FROM contracts WHERE id = :id")
    suspend fun deleteContractById(id: Int)


    // --- Opnames ---
    @Query("SELECT * FROM opnames WHERE userEmail = :email ORDER BY timestamp DESC")
    fun getOpnamesByUser(email: String): Flow<List<OpnameEntity>>

    @Query("SELECT * FROM opnames ORDER BY timestamp DESC")
    fun getAllOpnames(): Flow<List<OpnameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpname(opname: OpnameEntity)

    @Update
    suspend fun updateOpname(opname: OpnameEntity)

    @Query("DELETE FROM opnames WHERE id = :id")
    suspend fun deleteOpnameById(id: Int)


    // --- Custom Road Names ---
    @Query("SELECT * FROM custom_road_names WHERE userEmail = :email ORDER BY id DESC")
    fun getRoadNamesByUser(email: String): Flow<List<RoadNameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadName(road: RoadNameEntity)

    @Query("DELETE FROM custom_road_names WHERE id = :id")
    suspend fun deleteRoadNameById(id: Int)


    // --- Custom Contract Items ---
    @Query("SELECT * FROM custom_contract_items ORDER BY id ASC")
    fun getCustomItemsFlow(): Flow<List<CustomContractItemEntity>>

    @Query("SELECT * FROM custom_contract_items ORDER BY id ASC")
    suspend fun getCustomItems(): List<CustomContractItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomItem(item: CustomContractItemEntity)

    @Query("DELETE FROM custom_contract_items WHERE id = :id")
    suspend fun deleteCustomItemById(id: Int)


    // --- Custom Units ---
    @Query("SELECT * FROM custom_units ORDER BY name ASC")
    fun getCustomUnitsFlow(): Flow<List<CustomUnitEntity>>

    @Query("SELECT * FROM custom_units ORDER BY name ASC")
    suspend fun getCustomUnits(): List<CustomUnitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomUnit(unit: CustomUnitEntity)

    @Query("DELETE FROM custom_units WHERE id = :id")
    suspend fun deleteCustomUnitById(id: Int)
}
