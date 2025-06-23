package com.menopausetracker.app.ui.settings

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.menopausetracker.app.MainActivity
import com.menopausetracker.app.R
import com.menopausetracker.app.data.db.ArticleDatabase
import com.menopausetracker.app.data.repository.ArticleRepository
import com.menopausetracker.app.databinding.FragmentSettingsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup app version info
        setupVersionInfo()

        // Setup DELETE ALL DATA button
        setupDeleteDataButton()
    }

    private fun setupVersionInfo() {
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionName = packageInfo.versionName
            val versionCode = packageInfo.versionCode
            binding.textVersion.text = "Version: $versionName ($versionCode)"
        } catch (e: Exception) {
            binding.textVersion.text = "Version: Unknown"
        }
    }

    private fun setupDeleteDataButton() {
        binding.buttonDeleteData.setOnClickListener {
            showDeleteConfirmationDialog()
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete All Data")
            .setMessage("Are you sure you want to delete all app data? This includes all symptoms, articles, settings, and cached data. This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                // User confirmed, proceed with deletion
                deleteAllData()
            }
            .setNegativeButton("Cancel", null)
            .setIcon(R.drawable.ic_delete)
            .show()
    }

    private fun deleteAllData() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Explicitly clear saved articles
                    clearSavedArticles()

                    // Explicitly clear AI health assistant chats
                    clearAIHealthAssistantChats()

                    // Clear database data using a more generic approach
                    clearAllDatabases()

                    // Clear all preferences
                    clearSharedPreferences()

                    // Clear app cache
                    clearAppCache()

                    // Small delay to ensure all operations complete
                    Thread.sleep(500)
                }

                // Show success message
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, "All data has been cleared successfully", Snackbar.LENGTH_LONG).show()

                // Restart the application after a short delay
                Handler(requireContext().mainLooper).postDelayed({
                    restartApp()
                }, 1500)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(binding.root, "Error clearing data: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun clearAllDatabases() = withContext(Dispatchers.IO) {
        try {
            // First close any open Room databases
            val databases = arrayOf(
                "app_database.db",
                "symptoms_database.db",
                "user_database.db",
                "tracking_database.db",
                "recommendations_database.db",
                "ai_assistant_chats.db"
            )

            // For each database, try to clear its tables before deleting
            for (dbName in databases) {
                val dbFile = requireContext().getDatabasePath(dbName)
                if (dbFile.exists()) {
                    try {
                        val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                            dbFile.absolutePath,
                            null,
                            android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                        )

                        // Get all tables and clear them
                        val cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%' AND name NOT LIKE 'room_%'", null)
                        cursor.use { c ->
                            while (c.moveToNext()) {
                                val tableName = c.getString(0)
                                try {
                                    db.execSQL("DELETE FROM $tableName")
                                } catch (e: Exception) {
                                    // Some tables might have constraints, so we catch exceptions
                                    e.printStackTrace()
                                }
                            }
                        }

                        // Run vacuum to free space
                        db.execSQL("VACUUM")
                        db.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // Now delete the files
                    dbFile.delete()
                    File(dbFile.absolutePath + "-shm").delete()
                    File(dbFile.absolutePath + "-wal").delete()
                    File(dbFile.absolutePath + "-journal").delete()
                }
            }

            // Get the database directory and delete all remaining database files
            val dbDir = requireContext().getDatabasePath("app_database.db").parentFile
            dbDir?.listFiles()?.forEach { file ->
                if (file.name.endsWith(".db") || file.name.endsWith("-shm") ||
                    file.name.endsWith("-wal") || file.name.endsWith("-journal")) {
                    file.delete()
                }
            }

            // Force close any database connections that might still be open
            Runtime.getRuntime().gc()

            // Re-create essential database files with empty structure if needed
            // This prevents crashes on app restart
            createEmptyDatabasesIfNeeded()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createEmptyDatabasesIfNeeded() {
        try {
            // Create minimal database structure to prevent crashes
            val appDb = ArticleDatabase.getInstance(requireContext())
            appDb.openHelper.writableDatabase.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun clearSharedPreferences() {
        // Get the shared_prefs directory
        val sharedPrefsDir = File(requireContext().applicationInfo.dataDir, "shared_prefs")

        // Delete all shared preferences files
        sharedPrefsDir.listFiles()?.forEach { file ->
            file.delete()
        }

        // Also clear through the API for currently loaded preferences
        val sharedPrefNames = arrayOf(
            "app_preferences",
            "user_preferences",
            "symptom_tracking_prefs"
        )

        for (prefName in sharedPrefNames) {
            try {
                val sharedPrefs = requireActivity().getSharedPreferences(prefName, Context.MODE_PRIVATE)
                sharedPrefs.edit().clear().apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun clearAppCache() {
        // Clear internal cache
        try {
            requireContext().cacheDir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Clear external cache if available
        try {
            requireContext().externalCacheDir?.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Clear specific cache directories
        val cacheDirs = listOf(
            File(requireContext().filesDir, "image_cache"),
            File(requireContext().filesDir, "article_cache"),
            File(requireContext().filesDir, "temp_files")
        )

        for (dir in cacheDirs) {
            try {
                if (dir.exists()) {
                    dir.deleteRecursively()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun clearSavedArticles() = withContext(Dispatchers.IO) {
        try {
            // Get direct access to the database and clear saved articles more aggressively
            val database = ArticleDatabase.getInstance(requireContext())

            // First try using the DAO method
            database.articleDao().clearSavedArticles()

            // Also execute direct SQL for more thorough cleaning
            database.openHelper.writableDatabase.execSQL("UPDATE articles SET isSaved = 0")

            // Force clear any in-memory cached data
            database.clearAllTables()

            // Close and reopen database to ensure changes are flushed
            database.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun clearAIHealthAssistantChats() = withContext(Dispatchers.IO) {
        try {
            // Clear AI assistant chat database if it exists
            val chatDatabase = requireContext().getDatabasePath("ai_assistant_chats.db")
            if (chatDatabase.exists()) {
                // Force close any open connections to the database
                try {
                    // Try to open and clear the database before deleting
                    val sqliteDB = android.database.sqlite.SQLiteDatabase.openDatabase(
                        chatDatabase.absolutePath,
                        null,
                        android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                    )
                    // Execute SQL to delete all chat messages
                    sqliteDB.execSQL("DELETE FROM chat_messages")
                    sqliteDB.execSQL("DELETE FROM chat_conversations")
                    sqliteDB.execSQL("VACUUM") // Reclaim space
                    sqliteDB.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Now delete the actual files
                chatDatabase.delete()
            }

            // Also delete related -shm and -wal files
            File(chatDatabase.absolutePath + "-shm").delete()
            File(chatDatabase.absolutePath + "-wal").delete()

            // Check for chat-specific shared preferences and clear them
            val chatPrefs = requireActivity().getSharedPreferences("ai_chat_preferences", Context.MODE_PRIVATE)
            chatPrefs.edit().clear().apply()

            // Also try to delete other possible AI chat preferences
            arrayOf(
                "ai_assistant_prefs",
                "health_assistant_preferences",
                "chat_history_prefs"
            ).forEach { prefName ->
                try {
                    requireActivity().getSharedPreferences(prefName, Context.MODE_PRIVATE)
                        .edit().clear().apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Check for chat history files in app's files directory
            val chatDirs = listOf(
                File(requireContext().filesDir, "ai_chats"),
                File(requireContext().filesDir, "health_assistant"),
                File(requireContext().filesDir, "chat_history"),
                File(requireContext().filesDir, "assistant_data")
            )

            for (dir in chatDirs) {
                if (dir.exists()) {
                    dir.deleteRecursively()
                }
            }

            // Create empty directories to replace deleted ones
            for (dir in chatDirs) {
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun restartApp() {
        try {
            // Force Android to kill and restart the entire app process

            // Get package manager and create restart intent with special flags
            val packageManager = requireActivity().packageManager
            val packageName = requireContext().packageName
            val intent = packageManager.getLaunchIntentForPackage(packageName)

            // These flags ensure a complete restart
            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or
                           Intent.FLAG_ACTIVITY_CLEAR_TASK or
                           Intent.FLAG_ACTIVITY_NEW_TASK)

            // Additional Intent component setting to force Android to treat this as a "cold start"
            val componentName = intent?.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)

            // Set up AlarmManager to restart app after a delay
            val restartIntent = mainIntent ?: Intent(requireContext(), MainActivity::class.java)

            // Create a pending intent with FLAG_IMMUTABLE, more appropriate for newer Android versions
            val pendingIntent = PendingIntent.getActivity(
                requireContext().applicationContext,
                9876, // Unique request code
                restartIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            // Use AlarmManager to restart the app after a short delay
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setExact(AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent)

            // Try to force the Android runtime to kill the process immediately
            Handler(Looper.getMainLooper()).postDelayed({
                // Kill the current process completely
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(0)
            }, 200)
        } catch (e: Exception) {
            e.printStackTrace()

            // Fallback method if the above doesn't work
            try {
                val pm = requireContext().packageManager
                val intent = pm.getLaunchIntentForPackage(requireContext().packageName)
                intent?.let {
                    val componentName = intent.component
                    val mainIntent = Intent.makeRestartActivityTask(componentName)
                    requireContext().startActivity(mainIntent)
                    System.exit(0)
                }
            } catch (e2: Exception) {
                e2.printStackTrace()

                // Last resort fallback
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                requireActivity().finishAffinity()
                Runtime.getRuntime().exit(0)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
