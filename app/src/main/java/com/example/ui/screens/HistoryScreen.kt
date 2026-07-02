package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CarbonActivity
import com.example.viewmodel.EcoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val activities by viewModel.activities.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterCategory by remember { mutableStateOf("All") }
    var activityToDelete by remember { mutableStateOf<CarbonActivity?>(null) }

    val categories = listOf("All", "Transportation", "Energy", "Food", "Waste")

    // Filtered list
    val filteredActivities = remember(activities, searchQuery, selectedFilterCategory) {
        activities.filter { activity ->
            val matchesCategory = selectedFilterCategory == "All" || activity.category == selectedFilterCategory
            val matchesSearch = searchQuery.isBlank() || 
                    activity.subType.contains(searchQuery, ignoreCase = true) || 
                    activity.notes.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "CARBON FOOTPRINT LOGS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6A994E),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = "History & Logs",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111F0E),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search logs (e.g., Petrol, Solar, Beef)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF6A994E)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(3).forEach { cat ->
                val isSelected = selectedFilterCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilterCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE2EBD6),
                        selectedLabelColor = Color(0xFF386641)
                    )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.drop(3).forEach { cat ->
                val isSelected = selectedFilterCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilterCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE2EBD6),
                        selectedLabelColor = Color(0xFF386641)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredActivities.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Empty",
                        tint = Color(0xFF386641).copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No logs match your search filters",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color(0xFF43493E).copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredActivities) { activity ->
                    HistoryCard(
                        activity = activity,
                        onDeleteClick = { activityToDelete = activity }
                    )
                }
            }
        }

        // Deletion Confirmation Dialog
        if (activityToDelete != null) {
            AlertDialog(
                onDismissRequest = { activityToDelete = null },
                confirmButton = {
                    Button(
                        onClick = {
                            activityToDelete?.let { viewModel.deleteActivity(it.id) }
                            activityToDelete = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBC4749), contentColor = Color.White)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { activityToDelete = null }) {
                        Text("Cancel", color = Color(0xFF386641))
                    }
                },
                title = { Text("Confirm Deletion", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to permanently delete this logged carbon footprint entry?") }
            )
        }
    }
}

@Composable
fun HistoryCard(
    activity: CarbonActivity,
    onDeleteClick: () -> Unit
) {
    val dateStr = remember(activity.date) {
        SimpleDateFormat("EEE, MMM dd, yyyy - hh:mm a", Locale.getDefault()).format(Date(activity.date))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            when (activity.category) {
                                "Transportation" -> Color(0xFF6A994E).copy(alpha = 0.15f)
                                "Energy" -> Color(0xFF386641).copy(alpha = 0.15f)
                                "Food" -> Color(0xFFBC4749).copy(alpha = 0.15f)
                                else -> Color(0xFFA7C957).copy(alpha = 0.15f)
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
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity.subType,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Amount: ${activity.value} ${if (activity.category == "Transportation") "km" else if (activity.category == "Energy") "kWh" else if (activity.category == "Food") "portions" else "kg"}",
                        fontSize = 12.sp,
                        color = Color(0xFF43493E)
                    )
                    if (activity.notes.isNotBlank()) {
                        Text(
                            text = "Note: ${activity.notes}",
                            fontSize = 11.sp,
                            color = Color(0xFF6A994E),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = Color(0xFF43493E).copy(alpha = 0.5f)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (activity.co2Emissions >= 0) "+" else ""}${String.format("%.1f", activity.co2Emissions)} kg",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activity.co2Emissions > 15.0) Color(0xFFBC4749) else Color(0xFF386641)
                    )
                    Text(
                        text = "CO2",
                        fontSize = 10.sp,
                        color = Color(0xFF43493E).copy(alpha = 0.5f)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFBC4749).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
