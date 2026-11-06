
package com.example.umcimbisa

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_providers")
data class ProviderEntity(
    @PrimaryKey
    val providerId: String,
    val businessName: String,
    val category: String,
    val location: String,
    val price: Double,
    val rating: Double,
    val availability: String
)
