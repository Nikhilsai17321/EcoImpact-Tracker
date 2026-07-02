package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("UPDATE users SET monthlyGoal = :goal WHERE email = :email")
    suspend fun updateMonthlyGoal(email: String, goal: Double)
}

@Dao
interface CarbonActivityDao {
    @Query("SELECT * FROM carbon_activities WHERE userEmail = :email ORDER BY date DESC")
    fun getActivitiesByEmail(email: String): Flow<List<CarbonActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: CarbonActivity)

    @Query("DELETE FROM carbon_activities WHERE id = :id")
    suspend fun deleteActivityById(id: Int)

    @Query("SELECT SUM(co2Emissions) FROM carbon_activities WHERE userEmail = :email AND date >= :startOfMonth")
    suspend fun getMonthlyTotalEmissions(email: String, startOfMonth: Long): Double?
}

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leaderboard ORDER BY monthlyReduction DESC")
    fun getLeaderboard(): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboard(entries: List<LeaderboardEntry>)
}
