# Menopause Tracker App - Comprehensive Project Report

**Date:** November 18, 2025  
**Platform:** Android  
**Language:** Kotlin  
**Architecture:** MVVM (Model-View-ViewModel)

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Technical Architecture](#technical-architecture)
3. [Features & Functionality](#features--functionality)
4. [Technology Stack](#technology-stack)
5. [Project Structure](#project-structure)
6. [Data Models](#data-models)
7. [Core Components](#core-components)
8. [User Interface](#user-interface)
9. [AI Integration](#ai-integration)
10. [Database Architecture](#database-architecture)
11. [Network Layer](#network-layer)
12. [Known Issues](#known-issues)
13. [Team](#team)
14. [Setup & Installation](#setup--installation)

---

## Project Overview

The **Menopause Tracker App** is a comprehensive Android application designed to help women track, manage, and understand menopause symptoms. The app provides AI-powered personalized recommendations, educational articles from trusted sources, and symptom tracking capabilities.

### Purpose
- Track daily menopause symptoms with severity levels
- Provide AI-generated health recommendations based on symptoms
- Offer curated educational content about menopause
- Enable women to better understand and manage their menopause journey

### Target Audience
- Women experiencing perimenopause, menopause, or postmenopause
- Healthcare professionals looking for patient education resources
- Women seeking information about menopause symptoms and management

---

## Technical Architecture

### MVVM Pattern
The app follows the **Model-View-ViewModel (MVVM)** architecture pattern:

```
┌─────────────┐
│    View     │ (Fragments)
│  (UI Layer) │
└──────┬──────┘
       │ Observes LiveData
       ▼
┌─────────────┐
│ ViewModel   │ (Business Logic)
└──────┬──────┘
       │ Calls
       ▼
┌─────────────┐
│ Repository  │ (Data Layer)
└──────┬──────┘
       │
       ├─────────┐
       ▼         ▼
   ┌──────┐  ┌──────┐
   │ Room │  │ API  │
   │  DB  │  │ Call │
   └──────┘  └──────┘
```

### Key Benefits:
- **Separation of Concerns**: UI, business logic, and data are separated
- **Testability**: ViewModels can be unit tested without UI
- **Lifecycle Awareness**: ViewModels survive configuration changes
- **Reactive UI**: LiveData updates UI automatically when data changes

---

## Features & Functionality

### 1. Home Screen (Symptom Tracking)
**Location:** `HomeFragment.kt` & `HomeViewModel.kt`

**Features:**
- **Time-based Greeting**: Personalized greetings (Good Morning, Afternoon, Evening)
- **Days Counter**: Tracks days since starting menopause tracking
- **Symptom Logging**: 
  - Common symptoms: Hot Flashes, Night Sweats, Mood Changes, Sleep Issues, Fatigue
  - Severity rating (1-5 scale)
  - Custom symptom input via autocomplete
  - Multiple symptom selection
- **AI Recommendations**: Get personalized advice based on logged symptoms
- **Symptom History**: View and edit previously logged symptoms
- **Internet Detection**: Shows status when offline

**Technical Implementation:**
```kotlin
// Symptom logging with severity
fun logSymptom(
    description: String,
    severity: Int,
    hotFlashes: Boolean,
    nightSweats: Boolean,
    moodChanges: Boolean,
    sleepIssues: Boolean,
    fatigue: Boolean
)
```

### 2. AI Health Assistant
**Location:** `AIPromptFragment.kt`, `AIAssistantViewModel.kt`, `AIAssistantRepository.kt`

**Features:**
- **Custom Prompts**: Users can ask specific questions (max 100 characters)
- **AI-Generated Advice**: Powered by Google Gemini 2.0 Flash model
- **Suggestion History**: Saves all past AI suggestions locally
- **Context-Aware**: Considers recent symptoms for better recommendations
- **Fallback System**: Provides offline suggestions when API fails
- **Delete Suggestions**: Remove unwanted advice from history

**AI Model Configuration:**
```kotlin
Model: Gemini 2.0 Flash
API Timeout: 10 seconds
Safety Settings: BLOCK_MEDIUM_AND_ABOVE for harassment, hate speech
Temperature: 0.7 (balanced creativity)
Top-k: 40, Top-p: 0.95
```

**Fallback Topics:**
- Hot Flashes, Night Sweats, Sleep Issues, Mood Changes, Fatigue
- General menopause information
- Diet, Exercise, Sleep recommendations

### 3. Educational Articles
**Location:** `ArticlesFragment.kt`, `ArticlesViewModel.kt`, `RssArticleService.kt`

**Features:**
- **RSS Feed Integration**: Fetches articles from trusted menopause sources
- **Article Categories**: Women's Health, Menopause, Perimenopause
- **Save/Bookmark**: Save articles for offline reading
- **Full Content Display**: Shows complete article with images
- **Pull-to-Refresh**: Update articles manually
- **Saved Articles Tab**: Quick access to bookmarked articles
- **Reading Time**: Estimated reading time for each article
- **Image Fallback**: Uses Unsplash images when article images are missing

**RSS Sources (Current):**
```kotlin
1. Feisty Menopause Blog
2. Menopause Natural Solutions
3. My Menopause Centre
4. The Menopause Charity
5. Balance Menopause
6. Midi Health Blog
```

**Article Processing:**
- Filters relevant menopause content
- Removes duplicates and poor-quality articles
- Calculates reading time based on word count
- Caches articles for offline access
- HTML content cleaning and formatting

### 4. Symptom History
**Location:** `SymptomHistoryFragment.kt`, `SymptomHistoryViewModel.kt`

**Features:**
- **Timeline View**: Chronological list of all logged symptoms
- **Date Display**: Shows when each symptom was logged
- **Severity Indicator**: Visual representation of symptom severity
- **Edit/Delete**: Modify or remove symptom entries
- **Symptom Details**: View all recorded symptoms and notes

### 5. Settings & Data Management
**Location:** `SettingsFragment.kt`, `SettingsViewModel.kt`

**Features:**
- **App Version Info**: Display current app version
- **Delete All Data**: Clear all app data (symptoms, articles, AI suggestions)
- **Data Reset**: Restart app after deletion
- **App Information**: Team credits and disclaimers
- **Privacy**: All data stored locally on device

---

## Technology Stack

### Core Technologies
| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 1.9+ | Primary programming language |
| Android SDK | 24-34 | Platform support |
| Gradle | 8.0+ | Build system |

### Architecture Components
| Library | Version | Purpose |
|---------|---------|---------|
| Navigation Component | 2.7.7 | Fragment navigation and Safe Args |
| ViewModel | 2.7.0 | UI state management |
| LiveData | 2.7.0 | Observable data holder |
| Room Database | 2.6.1 | Local SQLite database |
| View Binding | - | Type-safe view access |

### Networking & APIs
| Library | Version | Purpose |
|---------|---------|---------|
| Retrofit | 2.9.0 | REST API client |
| Gson Converter | 2.9.0 | JSON serialization |
| OkHttp | 4.12.0 | HTTP client |
| RSS Parser | 6.0.3 | RSS feed parsing |
| Gemini AI SDK | 0.1.2 | Google Generative AI integration |

### UI & Graphics
| Library | Version | Purpose |
|---------|---------|---------|
| Material Design 3 | 1.11.0 | UI components |
| Glide | 4.16.0 | Image loading and caching |
| SwipeRefreshLayout | 1.1.0 | Pull-to-refresh functionality |

### Asynchronous Programming
| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin Coroutines | 1.7.3 | Asynchronous operations |
| Coroutines Android | 1.7.3 | Android-specific coroutine support |

### Testing (Configured)
| Library | Version | Purpose |
|---------|---------|---------|
| JUnit | 4.13.2 | Unit testing |
| Espresso | 3.5.1 | UI testing |

---

## Project Structure

```
app/
├── src/main/
│   ├── java/com/menopausetracker/app/
│   │   ├── MainActivity.kt                 # Main activity with bottom navigation
│   │   ├── data/
│   │   │   ├── api/
│   │   │   │   ├── RssArticleService.kt   # RSS feed fetching
│   │   │   │   ├── ArticleService.kt      # Article API interface
│   │   │   │   ├── GeminiAIService.kt     # Gemini AI integration
│   │   │   │   └── RecommendationApi.kt   # Recommendation API
│   │   │   ├── db/
│   │   │   │   ├── MenopauseDatabase.kt   # Main Room database
│   │   │   │   ├── ArticleDatabase.kt     # Article-specific database
│   │   │   │   ├── SymptomDao.kt          # Symptom data access
│   │   │   │   ├── ArticleDao.kt          # Article data access
│   │   │   │   ├── TrackingDao.kt         # Tracking data access
│   │   │   │   └── DateConverter.kt       # Date type converter
│   │   │   ├── model/
│   │   │   │   ├── Symptom.kt             # Symptom entity
│   │   │   │   ├── Article.kt             # Article entity
│   │   │   │   ├── Suggestion.kt          # AI suggestion model
│   │   │   │   └── Tracking.kt            # Tracking entity
│   │   │   └── repository/
│   │   │       ├── SymptomRepository.kt   # Symptom data operations
│   │   │       ├── ArticleRepository.kt   # Article data operations
│   │   │       ├── AIAssistantRepository.kt # AI operations
│   │   │       ├── TrackingRepository.kt  # Tracking operations
│   │   │       └── RecommendationRepository.kt
│   │   ├── ui/
│   │   │   ├── home/
│   │   │   │   ├── HomeFragment.kt        # Main symptom tracking UI
│   │   │   │   └── HomeViewModel.kt
│   │   │   ├── articles/
│   │   │   │   ├── ArticlesFragment.kt    # Article list UI
│   │   │   │   ├── ArticleDetailFragment.kt # Article reader
│   │   │   │   ├── ArticlesViewModel.kt
│   │   │   │   ├── ArticlesAdapter.kt     # RecyclerView adapter
│   │   │   │   └── ArticleAdapter.kt
│   │   │   ├── ai/
│   │   │   │   ├── AIPromptFragment.kt    # AI assistant UI
│   │   │   │   ├── AIAssistantViewModel.kt
│   │   │   │   ├── SuggestionAdapter.kt   # Suggestion list adapter
│   │   │   │   └── SuggestionDetailFragment.kt
│   │   │   ├── symptoms/
│   │   │   │   ├── SymptomHistoryFragment.kt # Symptom list UI
│   │   │   │   ├── EditSymptomFragment.kt # Edit symptom UI
│   │   │   │   ├── SymptomHistoryViewModel.kt
│   │   │   │   ├── EditSymptomViewModel.kt
│   │   │   │   └── SymptomAdapter.kt
│   │   │   └── settings/
│   │   │       ├── SettingsFragment.kt    # Settings UI
│   │   │       └── SettingsViewModel.kt
│   │   └── util/
│   │       ├── GreetingManager.kt         # Time-based greetings
│   │       └── FontSizeManager.kt         # Text size management
│   ├── res/
│   │   ├── layout/                        # XML layouts
│   │   ├── navigation/
│   │   │   └── nav_graph.xml             # Navigation graph
│   │   ├── drawable/                      # Icons and images
│   │   ├── values/                        # Strings, colors, themes
│   │   └── mipmap/                        # App icons
│   └── AndroidManifest.xml
└── build.gradle
```

---

## Data Models

### 1. Symptom Entity
```kotlin
@Entity(tableName = "symptoms")
data class Symptom(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date = Date(),
    val description: String = "",
    val severity: Int = 0,              // 1-5 scale
    val hotFlashes: Boolean = false,
    val nightSweats: Boolean = false,
    val moodChanges: Boolean = false,
    val sleepIssues: Boolean = false,
    val fatigue: Boolean = false,
    val otherSymptoms: String = ""
)
```

### 2. Article Entity
```kotlin
@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val imageUrl: String?,
    val sourceUrl: String,
    val sourceName: String,
    val publishDate: Date,
    val isSaved: Boolean = false,
    val category: String = "General",
    val readTimeMinutes: Int = 0,
    val lastFetchDate: Date = Date()
)
```

### 3. Suggestion Model
```kotlin
data class Suggestion(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val content: String,
    val prompt: String = "",            // User's original question
    val timestamp: Long = System.currentTimeMillis()
)
```

### 4. Tracking Entity
```kotlin
@Entity(tableName = "tracking")
data class Tracking(
    @PrimaryKey
    val id: Int = 1,                    // Single row
    val startDate: Date = Date(),
    val daysCount: Int = 0,
    val isActive: Boolean = true
)
```

---

## Core Components

### Navigation System
The app uses **Android Navigation Component** with a bottom navigation bar:

**Navigation Graph:** `nav_graph.xml`
- Home → Symptom History, AI Assistant
- Articles → Article Detail
- Settings
- Safe Args for type-safe argument passing

**Bottom Navigation:**
- Home (Symptom Tracking)
- Articles (Educational Content)
- Settings (App Configuration)

### Database Architecture

**Two Room Databases:**

1. **MenopauseDatabase**
   - Symptoms table
   - Tracking table
   - Date type converter

2. **ArticleDatabase**
   - Articles table
   - Separate database for article caching
   - Full-text content storage

**DAOs (Data Access Objects):**
- `SymptomDao`: CRUD operations for symptoms
- `ArticleDao`: CRUD operations for articles
- `TrackingDao`: Tracking data management

### Repository Pattern
Repositories abstract data sources from ViewModels:

```kotlin
Repository
    ├── Local Database (Room)
    └── Remote API (Retrofit/RSS)
```

**Benefits:**
- Single source of truth
- Caching strategy implementation
- Network/Database abstraction
- Error handling centralization

---

## User Interface

### Design Language
- **Material Design 3** components
- **Modern Android UI** patterns
- **Dark/Light theme** support

### Key UI Components

1. **Material Chips** - Quick symptom selection
2. **Sliders** - Severity rating
3. **RecyclerView** - Lists (symptoms, articles, suggestions)
4. **SwipeRefreshLayout** - Pull-to-refresh
5. **Bottom Navigation** - Main navigation
6. **Floating Action Buttons** - Primary actions
7. **Material Cards** - Content containers
8. **TextInputLayout** - Form inputs

### Layouts

| Layout File | Purpose |
|-------------|---------|
| `activity_main.xml` | Main container with bottom nav |
| `fragment_home.xml` | Symptom tracking screen |
| `fragment_articles.xml` | Article list with tabs |
| `fragment_article_detail.xml` | Article reader |
| `fragment_ai_prompt.xml` | AI assistant interface |
| `fragment_symptom_history.xml` | Symptom timeline |
| `fragment_settings.xml` | Settings screen |
| `item_symptom.xml` | Symptom list item |
| `item_article.xml` | Article list item |
| `item_suggestion.xml` | AI suggestion item |

---

## AI Integration

### Google Gemini AI (Gemini 2.0 Flash)

**API Configuration:**
```kotlin
API Key: AIzaSyD7UzsETcBH411hr13elPTa5aH0KF5yrkI
Model: gemini-2.0-flash
Timeout: 10 seconds
Max Tokens: 500
Temperature: 0.7
```

**Safety Settings:**
- Harassment: BLOCK_MEDIUM_AND_ABOVE
- Hate Speech: BLOCK_MEDIUM_AND_ABOVE
- Sexually Explicit: BLOCK_MEDIUM_AND_ABOVE
- Dangerous Content: BLOCK_MEDIUM_AND_ABOVE

**Prompt Engineering:**
The app creates context-aware prompts:
```kotlin
val systemPrompt = """
You are a compassionate health assistant specializing in menopause.
Provide supportive, evidence-based advice.
Keep responses concise (200-300 words).
Always recommend consulting healthcare professionals.
"""

val userContext = """
Recent symptoms: [symptom history]
User question: [user prompt]
"""
```

**Fallback System:**
When API fails or times out:
1. Check for symptom-specific fallback advice
2. Provide general menopause guidance
3. Display offline message

**Suggestion Storage:**
- Stored in SharedPreferences as JSON
- Persists across app sessions
- Can be deleted individually
- No server-side storage (privacy-focused)

---

## Database Architecture

### Room Database Configuration

**MenopauseDatabase:**
```kotlin
@Database(
    entities = [Symptom::class, Tracking::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class MenopauseDatabase : RoomDatabase()
```

**ArticleDatabase:**
```kotlin
@Database(
    entities = [Article::class],
    version = 1,
    exportSchema = false
)
abstract class ArticleDatabase : RoomDatabase()
```

### Type Converters
Converts complex types for Room storage:
```kotlin
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
```

### Database Operations

**Asynchronous Execution:**
All database operations run on IO dispatcher:
```kotlin
viewModelScope.launch(Dispatchers.IO) {
    val symptoms = symptomDao.getAllSymptoms()
    withContext(Dispatchers.Main) {
        _symptoms.value = symptoms
    }
}
```

---

## Network Layer

### RSS Feed Integration

**Library:** RSS Parser 6.0.3

**RSS Feeds (Active):**
1. **Feisty Menopause** - `feistymenopause.com/blog.rss`
2. **Menopause Natural Solutions** - `menopausenaturalsolutions.com/blog.rss`
3. **My Menopause Centre** - `mymenopausecentre.com/feed/`
4. **The Menopause Charity** - `themenopausecharity.org/feed/`
5. **Balance Menopause** - `balance-menopause.com/menopause-library/feed/`
6. **Midi Health** - `joinmidi.com/blog?format=rss`

**Feed Processing:**
```kotlin
suspend fun fetchArticles(): List<Article> {
    val allArticles = mutableListOf<Article>()
    
    for (feedUrl in RSS_FEEDS) {
        val channel = rssParser.getRssChannel(feedUrl)
        val articles = channel.items.mapNotNull { item ->
            // Extract content, images, metadata
            // Filter relevant articles
            // Clean HTML
            Article(...)
        }
        allArticles.addAll(articles)
    }
    
    return allArticles
}
```

**Article Filtering:**
- Checks for menopause-related keywords
- Removes duplicate articles
- Filters out poor-quality content
- Validates content length
- Ensures image availability

**Image Handling:**
1. Extract from RSS item media
2. Parse from HTML content (Open Graph)
3. Use source-specific patterns
4. Fallback to Unsplash images

**Caching Strategy:**
- Articles cached in Room database
- Refresh on pull-to-refresh
- Display cached content when offline
- 24-hour cache validity (configurable)

### Retrofit Configuration

**Not actively used for articles** (using RSS Parser instead), but configured for potential API endpoints:

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .client(okHttpClient)
    .build()
```

---

## Known Issues

Based on user feedback, the following issues have been identified:

### 1. Article Formatting Issues ❌
**Problem:** Some articles display as headings only without full content
**Cause:** RSS feeds provide only title/summary without full article body
**Status:** Partially fixed - some feeds still problematic
**Solution:** Need to replace low-quality RSS feeds with better sources

### 2. Missing Article Images 🖼️
**Problem:** Most articles lack images in the main article section
**Status:** Fallback system implemented but not always working
**Proposed Solution:** 
- Fetch images from article meta tags
- Use random category-appropriate images
- Implement Unsplash API integration

### 3. Bad RSS Feed Sources 📰
**Problem:** Source "Intimina" provides low-quality article previews
**Example:** "Perimenopause Changes To Face, Hair and Skin" article
**Status:** Needs removal/replacement
**Action Required:** Remove bad feeds, keep good ones

### 4. AI Chat Deletion Bug 🤖
**Problem:** When deleting an old AI chat inside the assistant, it doesn't get removed immediately
**Behavior:** Chat only disappears after closing and reopening the screen
**Cause:** RecyclerView not updating in real-time after deletion
**Fix Needed:** Update adapter and notify change immediately upon deletion

### 5. Common Symptoms Stay Selected 🔘
**Problem:** After logging symptoms using common symptom chips, they remain selected
**Expected:** Chips should reset/deselect after successful logging
**Fix Needed:** Clear chip selection state after symptom is logged

### 6. Toast Message on Back Navigation 🔙
**Problem:** "Your symptom has been logged" toast appears when navigating back to home from any page
**Cause:** Fragment lifecycle issue - message shown on every resume
**Fix Needed:** Only show toast immediately after logging, not on navigation back

---

## Team

This application was built by:
- **[Abhinash Roy](https://github.com/Abhinashroy)** - Lead Developer
- **[Kumar Harsh](https://github.com/kumarharsh24)** - Developer
- **[Neelesh Kumar](https://github.com/neelesh11204)** - Developer

**Repository:** https://github.com/Abhinashroy/Menopause-Tracker-app

---

## Setup & Installation

### Prerequisites
- Android Studio (Arctic Fox or later)
- JDK 17
- Android SDK 24-34
- Gradle 8.0+

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Abhinashroy/Menopause-Tracker-app.git
   cd Menopause-Tracker-app
   ```

2. **Configure API Key**
   Navigate to:
   ```
   app/src/main/java/com/menopausetracker/app/data/repository/AIAssistantRepository.kt
   ```
   Replace `API_KEY` with your Google Gemini API key:
   ```kotlin
   private const val API_KEY = "YOUR_API_KEY_HERE"
   ```

3. **Sync Gradle**
   ```bash
   ./gradlew build
   ```

4. **Run the App**
   - Connect an Android device or start an emulator
   - Click Run in Android Studio or:
   ```bash
   ./gradlew installDebug
   ```

### Download APK
You can download the latest version from:
[Google Drive](https://drive.google.com/file/d/1v6GFpp3YTTXE_ga9fSs2rVJ3koFZsany/view?usp=sharing)

### Minimum Requirements
- **Android Version:** 7.0 (Nougat) or higher
- **RAM:** 2GB minimum
- **Storage:** 50MB
- **Internet:** Required for AI features and article updates

---

## App Permissions

### Required Permissions
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

**Why these permissions:**
- **INTERNET**: For fetching articles and AI recommendations
- **ACCESS_NETWORK_STATE**: To check connectivity and show offline status

**Privacy Note:** 
- No personal data is sent to external servers
- All symptom data stored locally
- AI prompts are sent to Google Gemini API but not stored server-side

---

## Future Enhancements

### Potential Features
1. **Data Export** - Export symptoms as CSV/PDF
2. **Calendar View** - Visual symptom calendar
3. **Medication Tracker** - Track HRT and supplements
4. **Community Forum** - Connect with other users
5. **Doctor Notes** - Prepare reports for medical appointments
6. **Reminders** - Symptom logging reminders
7. **Charts & Analytics** - Symptom trends over time
8. **Multi-language** - Support for additional languages
9. **Widget** - Home screen symptom logging widget
10. **Wearable Integration** - Sync with fitness trackers

### Technical Improvements
1. **Unit Tests** - Comprehensive test coverage
2. **CI/CD** - Automated builds and testing
3. **Performance** - Optimize database queries
4. **Offline Mode** - Better offline functionality
5. **Accessibility** - Improved screen reader support

---

## Disclaimer

**Medical Disclaimer:**
This application provides AI-generated recommendations and educational content for informational purposes only. It should **NOT** be considered as medical advice, diagnosis, or treatment. Always consult with qualified healthcare professionals for medical decisions and before making any changes to your health regimen.

The developers and contributors are not liable for any health decisions made based on information provided by this application.

---

## License

This application is free to use. Feel free to fork, modify, and distribute according to your needs.

---

## Contact & Support

For issues, questions, or contributions:
- Open an issue on GitHub
- Contact the team members via their GitHub profiles
- Submit pull requests for improvements

---

**Report Generated:** November 18, 2025  
**App Version:** 1.0 (Build 1)  
**Project Status:** Active Development

