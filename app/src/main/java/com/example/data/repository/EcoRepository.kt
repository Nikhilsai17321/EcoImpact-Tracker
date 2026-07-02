package com.example.data.repository

import com.example.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.Calendar

class EcoRepository(
    private val userDao: UserDao,
    private val carbonDao: CarbonActivityDao,
    private val leaderboardDao: LeaderboardDao
) {
    fun getActivities(email: String): Flow<List<CarbonActivity>> =
        carbonDao.getActivitiesByEmail(email)

    suspend fun insertActivity(activity: CarbonActivity) {
        carbonDao.insertActivity(activity)
        // Refresh leaderboard after inserting activity
        refreshLeaderboardForUser(activity.userEmail)
    }

    suspend fun deleteActivity(id: Int, userEmail: String) {
        carbonDao.deleteActivityById(id)
        // Refresh leaderboard after deleting activity
        refreshLeaderboardForUser(userEmail)
    }

    suspend fun getMonthlyTotalEmissions(email: String): Double {
        val startOfMonth = getStartOfMonthTimestamp()
        return carbonDao.getMonthlyTotalEmissions(email, startOfMonth) ?: 0.0
    }

    suspend fun getUserByEmail(email: String): User? =
        userDao.getUserByEmail(email)

    suspend fun registerUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun updateMonthlyGoal(email: String, goal: Double) {
        userDao.updateMonthlyGoal(email, goal)
    }

    fun getLeaderboard(): Flow<List<LeaderboardEntry>> =
        leaderboardDao.getLeaderboard()

    suspend fun initializeLeaderboardIfNeeded(userEmail: String?) {
        val existing = leaderboardDao.getLeaderboard().first()
        if (existing.isEmpty()) {
            val baseCompetitors = listOf(
                LeaderboardEntry("alice@eco.org", "Alice Green", "nature_1", 185.2, 1),
                LeaderboardEntry("bob@eco.org", "Bob Oak", "nature_2", 145.0, 2),
                LeaderboardEntry("charlie@eco.org", "Charlie Forest", "nature_3", 120.5, 3),
                LeaderboardEntry("diana@eco.org", "Diana Leaf", "nature_4", 95.8, 4),
                LeaderboardEntry("ethan@eco.org", "Ethan Eco", "nature_5", 65.2, 5)
            )
            leaderboardDao.insertLeaderboard(baseCompetitors)
        }
        if (userEmail != null) {
            refreshLeaderboardForUser(userEmail)
        }
    }

    private suspend fun refreshLeaderboardForUser(userEmail: String) {
        val user = userDao.getUserByEmail(userEmail) ?: return
        val monthlyEmissions = getMonthlyTotalEmissions(userEmail)
        
        // Assume standard baseline is 400.0 kg/month. If they emit less, they reduced the rest.
        val baseline = 400.0
        val monthlyReduction = maxOf(0.0, baseline - monthlyEmissions)

        // Fetch other competitors
        val defaultCompetitors = listOf(
            LeaderboardEntry("alice@eco.org", "Alice Green", "nature_1", 185.2, 1),
            LeaderboardEntry("bob@eco.org", "Bob Oak", "nature_2", 145.0, 2),
            LeaderboardEntry("charlie@eco.org", "Charlie Forest", "nature_3", 120.5, 3),
            LeaderboardEntry("diana@eco.org", "Diana Leaf", "nature_4", 95.8, 4),
            LeaderboardEntry("ethan@eco.org", "Ethan Eco", "nature_5", 65.2, 5)
        )

        val allEntries = (defaultCompetitors.filter { it.email != userEmail } + 
                LeaderboardEntry(userEmail, user.name, user.avatarSeed, monthlyReduction, 0))
                .sortedByDescending { it.monthlyReduction }

        // Recalculate ranks
        val rankedEntries = allEntries.mapIndexed { index, entry ->
            entry.copy(rank = index + 1)
        }

        leaderboardDao.insertLeaderboard(rankedEntries)
    }

    fun getStartOfMonthTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
