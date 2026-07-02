package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.GeminiClient
import com.example.api.Part
import com.example.data.*
import com.example.data.repository.EcoRepository
import com.example.utils.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EcoViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = EcoRepository(db.userDao(), db.carbonActivityDao(), db.leaderboardDao())

    // Currently logged-in user state
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // Activities for currently logged-in user
    val activities: StateFlow<List<CarbonActivity>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getActivities(user.email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Leaderboard entries
    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.getLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // MonthlyStats Flow
    private val _monthlyEmissions = MutableStateFlow(0.0)
    val monthlyEmissions: StateFlow<Double> = _monthlyEmissions.asStateFlow()

    private val _userRank = MutableStateFlow(1)
    val userRank: StateFlow<Int> = _userRank.asStateFlow()

    // AI Tips State
    private val _aiTips = MutableStateFlow<String?>(null)
    val aiTips: StateFlow<String?> = _aiTips.asStateFlow()

    private val _isTipsLoading = MutableStateFlow(false)
    val isTipsLoading: StateFlow<Boolean> = _isTipsLoading.asStateFlow()

    private val _tipsError = MutableStateFlow<String?>(null)
    val tipsError: StateFlow<String?> = _tipsError.asStateFlow()

    init {
        viewModelScope.launch {
            // Automatically initialize the leaderboard
            repository.initializeLeaderboardIfNeeded(null)
            
            // Watch activities and update stats
            activities.collect { list ->
                val email = _currentUser.value?.email ?: return@collect
                updateEmissionsAndRank(email)
            }
        }
    }

    private suspend fun updateEmissionsAndRank(email: String) {
        val total = repository.getMonthlyTotalEmissions(email)
        _monthlyEmissions.value = total
        
        // Find user rank
        val currentLeaderboard = leaderboard.value
        val userEntry = currentLeaderboard.find { it.email == email }
        if (userEntry != null) {
            _userRank.value = userEntry.rank
        }
    }

    fun login(email: String, nameIfNew: String = "", isRegistering: Boolean = false) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                if (isRegistering) {
                    val existing = repository.getUserByEmail(email)
                    if (existing != null) {
                        _authError.value = "User already exists with this email"
                    } else {
                        val newUser = User(
                            email = email,
                            name = nameIfNew.ifBlank { email.substringBefore("@") },
                            passwordHash = "local_auth_simple",
                            monthlyGoal = 300.0,
                            avatarSeed = "nature_${(1..5).random()}"
                        )
                        repository.registerUser(newUser)
                        _currentUser.value = newUser
                        repository.initializeLeaderboardIfNeeded(email)
                    }
                } else {
                    val user = repository.getUserByEmail(email)
                    if (user != null) {
                        _currentUser.value = user
                        repository.initializeLeaderboardIfNeeded(email)
                    } else {
                        _authError.value = "User not found. Please register first."
                    }
                }
            } catch (e: Exception) {
                _authError.value = "Authentication error: ${e.message}"
            } finally {
                _isAuthLoading.value = false
                _currentUser.value?.let { updateEmissionsAndRank(it.email) }
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _aiTips.value = null
    }

    fun logActivity(category: String, subType: String, value: Double, notes: String = "") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val emissions = calculateEmissions(category, subType, value)
            val activity = CarbonActivity(
                userEmail = user.email,
                category = category,
                subType = subType,
                value = value,
                co2Emissions = emissions,
                date = System.currentTimeMillis(),
                notes = notes
            )
            repository.insertActivity(activity)
            updateEmissionsAndRank(user.email)
        }
    }

    fun deleteActivity(id: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteActivity(id, user.email)
            updateEmissionsAndRank(user.email)
        }
    }

    fun updateGoal(goal: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.updateMonthlyGoal(user.email, goal)
            _currentUser.value = user.copy(monthlyGoal = goal)
            updateEmissionsAndRank(user.email)
        }
    }

    fun exportPdf() {
        val user = _currentUser.value ?: return
        val currentActivities = activities.value
        val emissions = _monthlyEmissions.value
        PdfExporter.generateAndShareReport(
            getApplication(),
            user,
            currentActivities,
            emissions,
            user.monthlyGoal
        )
    }

    fun loadAITips() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            _isTipsLoading.value = true
            _tipsError.value = null
            _aiTips.value = null
            
            val recentList = activities.value.take(10)
            val prompt = buildPromptForTips(user, recentList, _monthlyEmissions.value)
            
            try {
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    _aiTips.value = getLocalTipsFallback(recentList)
                    return@launch
                }
                
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(temperature = 0.7f, maxOutputTokens = 600),
                    systemInstruction = Content(parts = listOf(Part(text = "You are a professional, motivating climate scientist and sustainability assistant. Speak concisely, clearly, and provide highly practical recommendations.")))
                )
                
                val response = withContext(Dispatchers.IO) {
                    GeminiClient.service.generateContent(apiKey, request)
                }
                
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (responseText != null) {
                    _aiTips.value = responseText
                } else {
                    _aiTips.value = getLocalTipsFallback(recentList)
                }
            } catch (e: Exception) {
                _tipsError.value = "Failed to fetch tips: ${e.message}"
                _aiTips.value = getLocalTipsFallback(recentList)
            } finally {
                _isTipsLoading.value = false
            }
        }
    }

    private fun buildPromptForTips(user: User, activities: List<CarbonActivity>, emissions: Double): String {
        val recentLogsText = if (activities.isEmpty()) {
            "No carbon activity logged yet."
        } else {
            activities.joinToString("\n") { 
                "- ${it.category} (${it.subType}): ${it.value} units logged, emissions: ${String.format("%.1f", it.co2Emissions)} kg CO2"
            }
        }
        
        return """
            Hi Gemini, please act as the personal carbon footprints analyzer of ${user.name}.
            Here are their stats:
            - Monthly carbon limit budget: ${user.monthlyGoal} kg CO2
            - Total CO2 emissions logged this month so far: ${String.format("%.1f", emissions)} kg CO2
            
            Recent logged activities:
            $recentLogsText
            
            Based on these realistic records, generate 3 highly tailored, actionable, and specific tips in clear markdown points. Make sure to suggest how many kg of CO2 they can save by following each tip. Keep the response encouraging, visually engaging, and concise!
        """.trimIndent()
    }

    private fun getLocalTipsFallback(activities: List<CarbonActivity>): String {
        val hasTransport = activities.any { it.category == "Transportation" }
        val hasMeat = activities.any { it.subType.contains("Beef") || it.subType.contains("Meat") }
        val hasEnergy = activities.any { it.category == "Energy" }

        val builder = StringBuilder()
        builder.append("### 🌱 Personalized Sustainability Suggestions\n\n")
        
        if (hasTransport) {
            builder.append("* **Active Transit Swap:** Swapping just 3 car trips a week with public transit, cycling, or walking can reduce your weekly emissions by **12% to 15%** (about 12 kg CO2).\n\n")
        } else {
            builder.append("* **Consolidated Errands:** Planning and combining car errands into a single trip saves fuel, reducing emissions by **8 kg CO2** per week.\n\n")
        }

        if (hasMeat) {
            builder.append("* **Meatless Mondays:** Eliminating beef or lamb for just one day a week and choosing plant-based meals saves up to **5.2 kg CO2** per serving. Plants produce up to 90% fewer emissions!\n\n")
        } else {
            builder.append("* **Local & Seasonal:** Sourcing foods grown locally reduces transportation emissions (food miles) by up to **3 kg CO2** per shopping trip.\n\n")
        }

        if (hasEnergy) {
            builder.append("* **Thermostat Adjustment:** Lowering your heating or raising your cooling by just 1-2 degrees Celsius reduces energy footprint by **5% to 10%** (saving roughly 18 kg CO2 per month).\n\n")
        } else {
            builder.append("* **Unplug Standby Devices:** Unplugging chargers and electronics when not in use removes 'phantom loads', saving **4 kg CO2** and cutting utility bills.\n\n")
        }

        builder.append("* **Embrace Recycling:** Double-check your general waste! Separating plastic, paper, and aluminium correctly saves **1.2 kg CO2** for every kg recycled by diverting waste from landfills.")
        
        return builder.toString()
    }

    fun calculateEmissions(category: String, subType: String, value: Double): Double {
        val factor = when (category) {
            "Transportation" -> when (subType) {
                "Car (Petrol)" -> 0.18
                "Car (Electric)" -> 0.05
                "Bus/Train" -> 0.04
                "Flight" -> 0.15
                "Bike/Walk" -> 0.0
                else -> 0.1
            }
            "Energy" -> when (subType) {
                "Electricity" -> 0.4
                "Natural Gas" -> 0.2
                else -> 0.3
            }
            "Food" -> when (subType) {
                "Beef/Lamb" -> 6.0
                "Poultry/Pork" -> 2.0
                "Fish" -> 1.5
                "Vegetarian" -> 0.8
                "Vegan" -> 0.4
                else -> 1.0
            }
            "Waste" -> when (subType) {
                "General Landfill" -> 0.5
                "Recycled Waste" -> -0.1
                else -> 0.2
            }
            else -> 0.1
        }
        return value * factor
    }
}
