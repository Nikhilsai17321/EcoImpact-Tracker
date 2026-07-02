package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String, // Simple local password verification
    val monthlyGoal: Double = 300.0, // Default monthly CO2 limit (in kg)
    val avatarSeed: String = "nature" // Seed for loading avatar or custom initials
)

@Entity(tableName = "carbon_activities")
data class CarbonActivity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val category: String, // Transportation, Energy, Food, Waste
    val subType: String, // e.g. Car (Petrol), Electricity, Beef, Recycled, etc.
    val value: Double, // The numerical raw input (e.g., 20 km, 15 kWh)
    val co2Emissions: Double, // Calculated CO2 in kg
    val date: Long, // timestamp
    val notes: String = ""
)

@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
    @PrimaryKey val email: String,
    val name: String,
    val avatarSeed: String,
    val monthlyReduction: Double, // total CO2 reduced this month (in kg) Compared to a baseline (e.g. baseline of 500 kg)
    val rank: Int
)
