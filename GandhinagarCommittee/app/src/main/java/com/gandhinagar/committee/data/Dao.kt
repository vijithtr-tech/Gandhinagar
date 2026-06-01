package com.gandhinagar.committee.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY name")
    fun observeAll(): Flow<List<Member>>

    @Query("SELECT * FROM members ORDER BY name")
    suspend fun getAll(): List<Member>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getById(id: Long): Member?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: Member): Long

    @Delete
    suspend fun delete(member: Member)
}

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans ORDER BY startDate DESC")
    fun observeAll(): Flow<List<Loan>>

    @Query("SELECT * FROM loans ORDER BY startDate DESC")
    suspend fun getAll(): List<Loan>

    @Query("SELECT * FROM loans WHERE memberId = :memberId")
    suspend fun getForMember(memberId: Long): List<Loan>

    @Query("SELECT COALESCE(SUM(principal),0) FROM loans WHERE memberId = :memberId AND closeDate IS NULL")
    suspend fun openPrincipalForMember(memberId: Long): Double

    @Query("SELECT COALESCE(SUM(principal),0) FROM loans WHERE closeDate IS NULL")
    fun observeTotalOpenPrincipal(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(loan: Loan): Long

    @Delete
    suspend fun delete(loan: Loan)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun observe(): Flow<AppSettings?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun get(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettings)
}
