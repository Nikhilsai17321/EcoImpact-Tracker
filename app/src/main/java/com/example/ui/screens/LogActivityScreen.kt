package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.EcoViewModel

data class CategoryData(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val subTypes: List<String>,
    val unit: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogActivityScreen(
    viewModel: EcoViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        CategoryData(
            name = "Transportation",
            icon = Icons.Rounded.ElectricCar,
            color = Color(0xFF6A994E),
            subTypes = listOf("Car (Petrol)", "Car (Electric)", "Bus/Train", "Flight", "Bike/Walk"),
            unit = "km"
        ),
        CategoryData(
            name = "Energy",
            icon = Icons.Rounded.Home,
            color = Color(0xFF386641),
            subTypes = listOf("Electricity", "Natural Gas"),
            unit = "kWh"
        ),
        CategoryData(
            name = "Food",
            icon = Icons.Rounded.Fastfood,
            color = Color(0xFFBC4749),
            subTypes = listOf("Beef/Lamb", "Poultry/Pork", "Fish", "Vegetarian", "Vegan"),
            unit = "portions"
        ),
        CategoryData(
            name = "Waste",
            icon = Icons.Rounded.WaterDrop,
            color = Color(0xFFA7C957),
            subTypes = listOf("General Landfill", "Recycled Waste"),
            unit = "kg"
        )
    )

    var selectedCategory by remember { mutableStateOf<CategoryData?>(null) }
    var selectedSubType by remember { mutableStateOf("") }
    var rawValue by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    val showDialog = selectedCategory != null

    // Calculate dynamic live emissions estimate
    val liveEmissions = remember(selectedCategory, selectedSubType, rawValue) {
        val value = rawValue.toDoubleOrNull() ?: 0.0
        val cat = selectedCategory?.name ?: ""
        viewModel.calculateEmissions(cat, selectedSubType, value)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "ECOLOGICAL IMPACT",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6A994E),
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = "Log Activity",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF111F0E),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Choose an eco-action category to record your impact.",
            fontSize = 13.sp,
            color = Color(0xFF43493E),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable {
                            selectedCategory = category
                            selectedSubType = category.subTypes.first()
                            rawValue = ""
                            notes = ""
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(category.color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = category.name,
                                tint = category.color,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = category.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Record ${category.unit}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Action Dialog Form
        if (showDialog && selectedCategory != null) {
            val category = selectedCategory!!
            AlertDialog(
                onDismissRequest = { 
                    selectedCategory = null 
                    focusManager.clearFocus()
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val value = rawValue.toDoubleOrNull() ?: 0.0
                            if (value > 0 || (category.name == "Waste" && selectedSubType == "Recycled Waste" && value > 0)) {
                                viewModel.logActivity(category.name, selectedSubType, value, notes)
                                selectedCategory = null
                                focusManager.clearFocus()
                                onSuccess()
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF386641),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Log Activity")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        selectedCategory = null 
                        focusManager.clearFocus()
                    }) {
                        Text("Cancel", color = Color(0xFFBC4749))
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(category.color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = category.icon,
                                contentDescription = null,
                                tint = category.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Log ${category.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Subtype selector
                        Text(
                            text = "Select Type:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        // Scrollable or wrapping sub-type list
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            category.subTypes.take(3).forEach { type ->
                                val selected = selectedSubType == type
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedSubType = type },
                                    label = { Text(type, fontSize = 11.sp) }
                                )
                            }
                        }
                        if (category.subTypes.size > 3) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                category.subTypes.drop(3).forEach { type ->
                                    val selected = selectedSubType == type
                                    FilterChip(
                                        selected = selected,
                                        onClick = { selectedSubType = type },
                                        label = { Text(type, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        // Value Input
                        OutlinedTextField(
                            value = rawValue,
                            onValueChange = { rawValue = it },
                            label = { Text("Value (${category.unit})") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Notes Input
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        // Interactive Carbon Footprint calculation preview!
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (liveEmissions >= 0) {
                                    if (liveEmissions > 15.0) Color(0xFFFBEBEB) else Color(0xFFE2EBD6)
                                } else {
                                    Color(0xFFE2EBD6) // negative offset credit
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimated Footprint:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF111F0E)
                                )
                                Text(
                                    text = "${if (liveEmissions >= 0) "" else "-"}${String.format("%.2f", kotlin.math.abs(liveEmissions))} kg CO2",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (liveEmissions > 15.0) Color(0xFFBC4749) else Color(0xFF386641)
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}
