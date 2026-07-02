package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.ElectricCar
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CarbonActivity
import com.example.viewmodel.EcoViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: EcoViewModel,
    onNavigateToLog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val monthlyEmissions by viewModel.monthlyEmissions.collectAsState()
    val userRank by viewModel.userRank.collectAsState()

    val user = currentUser ?: return

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcoming Header (Geometric Balance style)
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CARBON TRACKER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "EcoImpact",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // Avatar representation: JD rounded-full border-2 bg shadow
                val userInitials = user.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD8E2D1))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { viewModel.logout() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (userInitials.isNotEmpty()) userInitials else "JD",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111F0E)
                        )
                    }
                }
            }
        }

        // Current Impact Hero Card (E2EBD6, rounded 32.dp, large display text)
        item {
            val remaining = maxOf(0.0, user.monthlyGoal - monthlyEmissions)
            val budgetRatio = if (user.monthlyGoal > 0) (monthlyEmissions / user.monthlyGoal).toFloat() else 0f
            val cappedRatio = budgetRatio.coerceIn(0f, 1f)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "Current Impact",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF43493E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = String.format("%.1f", monthlyEmissions),
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF111F0E)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "kg CO₂",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF111F0E).copy(alpha = 0.6f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        // Badge: On Track / Over Budget
                        val isOverBudget = monthlyEmissions > user.monthlyGoal
                        val badgeBg = if (isOverBudget) Color(0xFFBC4749) else Color(0xFF386641)
                        val badgeText = if (isOverBudget) "Over Limit" else "On Track"
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(badgeBg)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(cappedRatio)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(Color(0xFF6A994E))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Goal: ${String.format("%.1f", user.monthlyGoal)} kg",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43493E)
                        )
                        Text(
                            text = "Remaining: ${String.format("%.1f", remaining)} kg",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43493E)
                        )
                    }
                }
            }
        }

        // Stats Grid Cards (Rank and Savings in geometric style)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Rank
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "RANK",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A994E),
                            letterSpacing = 1.5.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "#$userRank",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "/ 150",
                                fontSize = 12.sp,
                                color = Color(0xFF43493E)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Top 5% this month",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF43493E)
                        )
                    }
                }

                // Card 2: Savings
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SAVINGS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6A994E),
                            letterSpacing = 1.5.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            val savings = maxOf(0.0, user.monthlyGoal - monthlyEmissions)
                            Text(
                                text = String.format("%.1f", savings),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "kg",
                                fontSize = 12.sp,
                                color = Color(0xFF43493E)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // Overlapping buddy dots from Geometric Balance design
                        Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                            listOf(Color(0xFFBC4749), Color(0xFFA7C957), Color(0xFF386641)).forEach { dotColor ->
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(dotColor, CircleShape)
                                        .border(1.5.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Sustainability Tip (Deep Spruce Container #2D3328, rounded 32.dp, animated indicator, neon accent)
        item {
            val aiTips by viewModel.aiTips.collectAsState()
            val isTipsLoading by viewModel.isTipsLoading.collectAsState()

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2D3328)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI Sustainability Tip",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        // Pulse circle container
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color(0xFFA7C957), CircleShape)
                            )
                        }
                    }

                    if (isTipsLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(color = Color(0xFFA7C957))
                        }
                    } else {
                        val activeTip = aiTips ?: "Switching to a meat-free lunch three days a week could reduce your footprint by 12.5% by next Friday."
                        Text(
                            text = activeTip,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = Color(0xFFE0E4D9),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = { viewModel.loadAITips() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFA7C957),
                            contentColor = Color(0xFF1A1C18)
                        )
                    ) {
                        Text(
                            text = if (aiTips == null) "Generate Gemini Suggestions" else "Accept Challenge",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Action Quick Entry
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLog() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Log",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Log New Activity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Add transit, energy, food, or waste",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Go",
                        tint = Color.White
                    )
                }
            }
        }

        // Visual Charts Section
        item {
            Text(
                text = "Emissions Visualization",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        // Two custom chart cards
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Category Footprint Share",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Render Donut Chart
                    DonutChartSection(activities = activities)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Carbon Log (Recent)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Render Weekly Bar Chart
                    BarChartSection(activities = activities)
                }
            }
        }

        // Recent Activities Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Logs",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (activities.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Park,
                            contentDescription = "No Activity",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No activities logged yet",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(activities.take(4)) { activity ->
                ActivityRowItem(activity = activity)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DonutChartSection(activities: List<CarbonActivity>) {
    // Calculate shares
    val totals = activities.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.co2Emissions } }

    val transportShare = totals["Transportation"]?.toFloat() ?: 0f
    val energyShare = totals["Energy"]?.toFloat() ?: 0f
    val foodShare = totals["Food"]?.toFloat() ?: 0f
    val wasteShare = totals["Waste"]?.toFloat() ?: 0f

    val total = transportShare + energyShare + foodShare + wasteShare

    val transportColor = Color(0xFF6A994E) // Leaf Green
    val energyColor = Color(0xFF386641)    // Deep Spruce Green
    val foodColor = Color(0xFFBC4749)      // Terracotta Red
    val wasteColor = Color(0xFFA7C957)     // Soft Lime Yellow-Green

    if (total == 0f) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Log carbon emissions to view details",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                val strokeWidth = 24f
                val size = this.size
                val centerOffset = Offset(size.width / 2, size.height / 2)
                val radius = (size.width - strokeWidth) / 2

                var startAngle = 0f
                val categories = listOf(
                    transportShare to transportColor,
                    energyShare to energyColor,
                    foodShare to foodColor,
                    wasteShare to wasteColor
                )

                categories.forEach { (share, color) ->
                    if (share > 0f) {
                        val sweepAngle = (share / total) * 360f
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth),
                            size = Size(radius * 2, radius * 2),
                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius)
                        )
                        startAngle += sweepAngle
                    }
                }
            }

            // Legend
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LegendRow(color = transportColor, label = "Transport", amount = transportShare)
                LegendRow(color = energyColor, label = "Energy", amount = energyShare)
                LegendRow(color = foodColor, label = "Food", amount = foodShare)
                LegendRow(color = wasteColor, label = "Waste", amount = wasteShare)
            }
        }
    }
}

@Composable
fun LegendRow(color: Color, label: String, amount: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ${String.format("%.1f", amount)} kg",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BarChartSection(activities: List<CarbonActivity>) {
    // Group last 7 activities or aggregate emissions by past 5 logs
    val recentLogs = activities.sortedByDescending { it.date }.take(5).reversed()

    if (recentLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Log activities to view history",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    } else {
        val maxEmission = recentLogs.maxOfOrNull { it.co2Emissions }?.toFloat() ?: 1f
        val primaryColor = MaterialTheme.colorScheme.primary

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                recentLogs.forEach { activity ->
                    val ratio = maxOf(0.05f, (activity.co2Emissions.toFloat() / maxEmission))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(48.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", activity.co2Emissions),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .fillMaxHeight(ratio)
                                .background(
                                    when (activity.category) {
                                        "Transportation" -> Color(0xFF6A994E)
                                        "Energy" -> Color(0xFF386641)
                                        "Food" -> Color(0xFFBC4749)
                                        else -> Color(0xFFA7C957)
                                    },
                                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                recentLogs.forEach { activity ->
                    Text(
                        text = activity.category.take(5),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.width(48.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityRowItem(activity: CarbonActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            when (activity.category) {
                                "Transportation" -> Color(0xFFE2EBD6)
                                "Energy" -> Color(0xFFD8E2D1)
                                "Food" -> Color(0xFFF5E6E6)
                                else -> Color(0xFFF1F5E9)
                            },
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (activity.category) {
                            "Transportation" -> Icons.Rounded.ElectricCar
                            "Energy" -> Icons.Rounded.Home
                            "Food" -> Icons.Rounded.Fastfood
                            "Waste" -> Icons.Rounded.WaterDrop
                            else -> Icons.Rounded.DirectionsRun
                        },
                        contentDescription = null,
                        tint = when (activity.category) {
                            "Transportation" -> Color(0xFF6A994E)
                            "Energy" -> Color(0xFF386641)
                            "Food" -> Color(0xFFBC4749)
                            else -> Color(0xFFA7C957)
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = activity.subType,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(activity.date)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (activity.co2Emissions >= 0) "+" else ""}${String.format("%.1f", activity.co2Emissions)} kg",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (activity.co2Emissions > 15.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${activity.value} units",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
