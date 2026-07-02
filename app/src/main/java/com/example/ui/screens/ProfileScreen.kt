package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.EcoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: EcoViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val monthlyEmissions by viewModel.monthlyEmissions.collectAsState()
    val aiTips by viewModel.aiTips.collectAsState()
    val isTipsLoading by viewModel.isTipsLoading.collectAsState()
    val tipsError by viewModel.tipsError.collectAsState()

    val focusManager = LocalFocusManager.current

    val user = currentUser ?: return

    var targetGoal by remember(user.monthlyGoal) {
        mutableStateOf(user.monthlyGoal.toString())
    }

    val scrollState = rememberScrollState()

    // Automatically trigger AI tips once when the screen loads if empty
    LaunchedEffect(key1 = user.email) {
        if (aiTips == null && !isTipsLoading) {
            viewModel.loadAITips()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SETTINGS & PROFILE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A994E),
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = "Your Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF111F0E)
            )
        }

        // Profile Header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Eco Initials Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(Color(0xFFE2EBD6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(2).uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF386641)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = user.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111F0E)
                )
                Text(
                    text = user.email,
                    fontSize = 13.sp,
                    color = Color(0xFF43493E).copy(alpha = 0.7f)
                )
            }
        }

        // Carbon Goal Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Sustainability Target",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111F0E)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = targetGoal,
                        onValueChange = { targetGoal = it },
                        label = { Text("Monthly Goal (kg CO2)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val goal = targetGoal.toDoubleOrNull() ?: 300.0
                                if (goal > 0) {
                                    viewModel.updateGoal(goal)
                                }
                                focusManager.clearFocus()
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    Button(
                        onClick = {
                            val goal = targetGoal.toDoubleOrNull() ?: 300.0
                            if (goal > 0) {
                                viewModel.updateGoal(goal)
                            }
                            focusManager.clearFocus()
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF386641),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }

        // Reports Card - PDF Export button!
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Export Carbon Impact Report",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111F0E)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Generate and share a complete, formatted PDF report containing this month's stats, goal comparison, and logged activity table.",
                    fontSize = 12.sp,
                    color = Color(0xFF43493E).copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.exportPdf() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A994E),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PictureAsPdf,
                            contentDescription = null
                        )
                        Text("Export Monthly Report PDF", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Gemini AI Smart Recommendations section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF1F5E9)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E4D9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AI Tips",
                            tint = Color(0xFF386641),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI Smart Recommendations",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111F0E)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.loadAITips() },
                        enabled = !isTipsLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF386641)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                when {
                    isTipsLoading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF386641))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Gemini is analyzing your carbon footprint...",
                                fontSize = 12.sp,
                                color = Color(0xFF386641)
                            )
                        }
                    }
                    tipsError != null -> {
                        Text(
                            text = "Error: $tipsError. Loaded Rules-based local recommendations.",
                            fontSize = 12.sp,
                            color = Color(0xFFBC4749),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        aiTips?.let {
                            Text(
                                text = it,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = Color(0xFF111F0E)
                            )
                        }
                    }
                    aiTips != null -> {
                        // Presenting markdown styled text neatly
                        Text(
                            text = aiTips ?: "",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF111F0E)
                        )
                    }
                    else -> {
                        Text(
                            text = "No tips generated yet. Tap Refresh to load your customized recommendations.",
                            fontSize = 13.sp,
                            color = Color(0xFF43493E).copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}
