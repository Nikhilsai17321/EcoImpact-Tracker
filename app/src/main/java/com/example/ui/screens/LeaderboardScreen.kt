package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LeaderboardEntry
import com.example.viewmodel.EcoViewModel

@Composable
fun LeaderboardScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val maxReduction = remember(leaderboard) {
        leaderboard.maxOfOrNull { it.monthlyReduction } ?: 1.0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Leaderboard Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE2EBD6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFF386641),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "GLOBAL LEADERBOARD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A994E),
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Eco Champions",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF111F0E)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid list of ranked eco champions
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(leaderboard) { entry ->
                val isMe = entry.email == currentUser?.email
                LeaderboardCard(
                    entry = entry,
                    maxReduction = maxReduction,
                    isMe = isMe
                )
            }
        }
    }
}

@Composable
fun LeaderboardCard(
    entry: LeaderboardEntry,
    maxReduction: Double,
    isMe: Boolean
) {
    val progress = (entry.monthlyReduction / maxReduction).toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMe) Color(0xFFF1F5E9) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isMe) 2.dp else 1.dp,
            color = if (isMe) Color(0xFF386641) else Color(0xFFE0E4D9)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMe) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        when (entry.rank) {
                            1 -> Color(0xFFFFD700) // Gold
                            2 -> Color(0xFFC0C0C0) // Silver
                            3 -> Color(0xFFCD7F32) // Bronze
                            else -> Color(0xFFE2EBD6)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${entry.rank}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (entry.rank <= 3) Color.Black else Color(0xFF386641)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Avatar placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF386641).copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = entry.name.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF386641)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name & Reduction details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isMe) "${entry.name} (You)" else entry.name,
                        fontWeight = if (isMe) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF111F0E)
                    )
                    Text(
                        text = "${String.format("%.1f", entry.monthlyReduction)} kg",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF386641)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                
                // Linear Progress bar showing how close they are to first place
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF386641),
                    trackColor = Color(0xFFE2EBD6)
                )
            }
        }
    }
}
