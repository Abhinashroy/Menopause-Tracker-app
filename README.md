# Menopause Tracker App for Android

An Android application designed to help women track and manage menopause symptoms, providing personalized recommendations and educational content.

## Features

- **Home Screen**
  - Track daily symptoms and feelings
  - Get AI-powered recommendations based on symptoms
  - View symptom history
  - Requires internet connection for AI recommendations

- **Articles**
  - Read educational content about menopause
  - Save favorite articles for offline reading
  - View saved articles in a dedicated section

- **Settings**
  - Manage app data
  - View app information and disclaimers
  - Delete all stored data

## Team

This application was built by:
- [Abhinash Roy](https://github.com/Abhinashroy)
- [Kumar Harsh](https://github.com/kumarharsh24)
- [Neelesh Kumar](https://github.com/neelesh11204)

## Download

You can download the latest version of the Menopause Tracker App from our [Google Drive](https://drive.google.com/file/d/1v6GFpp3YTTXE_ga9fSs2rVJ3koFZsany/view?usp=sharing).

## Build Instructions

1. Clone the repository
   ```
   https://github.com/Abhinashroy/Menopause-Tracker-app.git
   ```

2. Open the project in Android Studio

3. Update the API key
   - Navigate to `app/src/main/java/com/menopausetracker/app/data/repository/AIAssistantRepository.kt`
   - Replace `API_KEY` with your Google Gemini API key

4. Sync Gradle files
   ```
   ./gradlew build
   ```

5. Run the application on an emulator or physical device
   ```
   ./gradlew installDebug
   ```

## Technical Details

- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Language: Kotlin
- Architecture: MVVM
- Libraries:
  - Material Design 3
  - Room Database
  - Retrofit
  - Navigation Component
  - ViewBinding
  - Coroutines
  - ViewModel
  - Google Generative AI (Gemini)

## License

This application is free to use. Feel free to fork, modify and distribute according to your needs.

## Disclaimer

This application provides AI-generated recommendations and should not be considered as medical advice. Always consult with healthcare professionals for medical decisions.
