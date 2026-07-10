package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val email: String = "",
    val passwordHash: String = "",
    val role: String = "user",
    val isApproved: Boolean = false,
    val userNumber: String = "",
    val expiryDateString: String = "",
    val packageName: String = "",
    val registerTimestamp: Long = System.currentTimeMillis(),
    val ppnPercentage: Double = 11.0
)

@Entity(tableName = "contracts")
data class ContractEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
    val division: String = "",
    val itemCodeAndName: String = "",
    val unit: String = "",
    val volume: Double = 0.0,
    val unitPrice: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "opnames")
data class OpnameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String = "",
    val location: String = "",
    val side: String = "", // "Kanan", "Kiri", "CL"
    val roadName: String = "",
    val dateString: String = "", // yyyy-MM-dd
    val unit: String = "",
    val length: Double = 0.0,
    val width: Double = 0.0,
    val height: Double = 0.0,
    val thickness: Double = 0.0,
    val area: Double = 0.0,
    val density: Double = 0.0, // BJ for "ton"
    val photoUri: String = "", // local file or base64 or placeholder
    val division: String = "",
    val itemName: String = "",
    val calculatedVolume: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "custom_road_names")
data class RoadNameEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val userEmail: String
)

@Entity(tableName = "custom_contract_items")
data class CustomContractItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val divisionCode: String,
    val codeAndName: String,
    val defaultUnit: String
)

@Entity(tableName = "custom_units")
data class CustomUnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)
