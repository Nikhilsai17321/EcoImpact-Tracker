# 🌱 EcoImpact Tracker

**EcoImpact Tracker** is a complete, production-ready, modern Android Carbon Footprint Tracker. Built entirely with Jetpack Compose, modern Kotlin, and Android Jetpack libraries, it provides users with a comprehensive, beautiful interface to track emissions, visualize ecological shares, generate professional PDF reports, and receive personalized AI-powered reduction recommendations from Google Gemini.

---

## ✨ Features

- 👤 **Multi-User Local Authentication:** Clean, beautiful secure login and registration cards with custom nature-inspired details.
- 📊 **Responsive grid Dashboard:** Real-time stats cards including Total CO2, Monthly Goals, Remaining Budgets, and global ranks.
- 📈 **Stunning Custom Visualizations:** High-fidelity, smooth, canvas-drawn category share donut charts and daily emission bar charts.
- 🍃 **Intuitive Impact Logger:** Dynamic inputs matching specific categories (Transportation, Food, Energy, Waste) with real-time live carbon calculations before saving.
- 📜 **Interactive Filterable History:** Full table showing past activities with dynamic type searching, category filter chips, and secure deletion capability.
- 🏆 **Eco Champion Leaderboard:** Competitor system showing local eco champions, ranked with custom medals and interactive progress bars indicating target ratios.
- 🤖 **Gemini AI Sustainability Tips:** High-fidelity personalized eco-suggestions based on real logged user habits. Includes robust local smart rule fallbacks.
- 📄 **Monthly PDF Report Generator:** Custom vector rendering of A4 reports with active styling, goals, comparison states, and shareable file intents.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework:** Jetpack Compose (Kotlin) with full Material 3 Dynamic Custom Theming.
- **Architecture:** MVVM (Model-View-ViewModel) + Repository Pattern.
- **Data Persistence:** SQLite-backed Room Database with KSP annotation processing.
- **Networking:** Retrofit + Moshi for high-performance direct REST calls to Google Gemini API.
- **Reporting:** Standard Android `PdfDocument` with `FileProvider` secure content sharing.

---

## 🚀 Setup & API Configuration

### 1. Configure the Gemini API Key
To enable the AI tips section, configure your Google Gemini API key:
1. Open the **Secrets panel in the AI Studio UI**.
2. Add a new secret called `GEMINI_API_KEY` with your actual key.
3. The app automatically injects this secret at runtime via `.env` / `BuildConfig`.

### 2. Standard Build & Run
- Click the **Run** or **Compile Applet** button in Google AI Studio to launch the interactive Streaming Android Emulator.
- Access the fully integrated and operational app instantly in your browser.

---

*Thank you for tracking your footprint and protecting our environment!* 🌍
