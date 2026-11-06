package com.example.umcimbisa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProviderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(
        providers: List<ProviderEntity>
    )

    @Query(
        "SELECT * FROM cached_providers WHERE category = :category"
    )
    suspend fun getProvidersByCategory(
        category: String
    ): List<ProviderEntity>

    @Query("SELECT * FROM cached_providers")
    suspend fun getAllProviders(): List<ProviderEntity>

    @Query("DELETE FROM cached_providers")
    suspend fun clearProviders()

    // Remove outdated providers from one category.
    @Query(
        "DELETE FROM cached_providers " +
                "WHERE category = :category " +
                "AND providerId NOT IN (:activeProviderIds)"
    )
    suspend fun deleteOutdatedProviders(
        category: String,
        activeProviderIds: List<String>
    )

    // Remove all cached providers in one category.
    @Query(
        "DELETE FROM cached_providers WHERE category = :category"
    )
    suspend fun clearProvidersByCategory(
        category: String
    )
}
