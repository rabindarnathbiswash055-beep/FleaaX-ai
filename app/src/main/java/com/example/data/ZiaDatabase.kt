package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.security.PrivacyGuard
import com.example.security.SanitizationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Entity(tableName = "memory_facts")
data class MemoryFactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val key: String,
    val value: String,
    val category: String = "Preference",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val isUser: Boolean,
    val text: String,
    val languageCode: String = "AUTO",
    val engineSource: String = EngineSource.LOCAL_ASSISTANT.name,
    val codeSnippet: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val scheduledTimeText: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ZiaDao {
    // Memory Facts
    @Query("SELECT * FROM memory_facts ORDER BY updatedAt DESC")
    fun getAllMemoryFacts(): Flow<List<MemoryFactEntity>>

    @Query("SELECT * FROM memory_facts WHERE LOWER(key) = LOWER(:key) LIMIT 1")
    suspend fun getMemoryFactByKey(key: String): MemoryFactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoryFact(fact: MemoryFactEntity)

    @Query("DELETE FROM memory_facts WHERE id = :id")
    suspend fun deleteMemoryFactById(id: Int)

    @Query("DELETE FROM memory_facts")
    suspend fun clearAllMemoryFacts()

    // Chat Messages
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllChatMessages()

    // Reminders
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = :completed WHERE id = :id")
    suspend fun setReminderCompleted(id: Int, completed: Boolean)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)

    @Query("DELETE FROM reminders")
    suspend fun clearAllReminders()
}

@Database(
    entities = [MemoryFactEntity::class, ChatMessageEntity::class, ReminderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ZiaDatabase : RoomDatabase() {
    abstract fun ziaDao(): ZiaDao

    companion object {
        @Volatile
        private var INSTANCE: ZiaDatabase? = null

        fun getInstance(context: Context): ZiaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZiaDatabase::class.java,
                    "zia_local_vault.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ZiaRepository(private val dao: ZiaDao) {

    val memoryFacts: Flow<List<MemoryFactEntity>> = dao.getAllMemoryFacts()
    val chatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()
    val reminders: Flow<List<ReminderEntity>> = dao.getAllReminders()

    suspend fun ensureStarterDataSeeded() {
        val currentMessages = dao.getAllChatMessages().first()
        if (currentMessages.isEmpty()) {
            dao.insertChatMessage(
                ChatMessageEntity(
                    isUser = false,
                    text = "Namaste & Hello! Main ZIA AI hoon — aapki personal multilingual voice assistant (English, Hinglish, Banglish, aur বাংলা).\n\n" +
                        "• Offline Commands: \"Volume 60% karo\", \"7:30 ka alarm lagao\", \"Open YouTube\", \"Wi-Fi settings kholo\", \"Remind me to practice Kotlin\".\n" +
                        "• Coding & Reasoning: Online hone par Gemini Cloud AI se complex questions aur code explanations puch sakte hain.\n" +
                        "• Privacy First: Aapki memory phone par local rehti hai aur har sensitive phone action se pehle Confirmation Preview dikhta hai.",
                    languageCode = AssistantLanguage.AUTO_MIXED.code,
                    engineSource = EngineSource.LOCAL_ASSISTANT.name
                )
            )
        }

        val currentMemory = dao.getAllMemoryFacts().first()
        if (currentMemory.isEmpty()) {
            dao.insertMemoryFact(
                MemoryFactEntity(
                    key = "Preferred Languages",
                    value = "Hinglish, Banglish, English & বাংলা",
                    category = "Language"
                )
            )
            dao.insertMemoryFact(
                MemoryFactEntity(
                    key = "Coding Focus",
                    value = "Kotlin & Jetpack Compose Android Development",
                    category = "Skill"
                )
            )
            dao.insertMemoryFact(
                MemoryFactEntity(
                    key = "Target Device Profile",
                    value = "Redmi 9 (Android 10 · 6 GB RAM) Optimized",
                    category = "System"
                )
            )
        }

        val currentReminders = dao.getAllReminders().first()
        if (currentReminders.isEmpty()) {
            dao.insertReminder(
                ReminderEntity(
                    title = "Test ZIA push-to-talk in Hinglish & Banglish",
                    scheduledTimeText = "Today · 6:00 PM"
                )
            )
        }
    }

    suspend fun saveMemoryFactSafely(
        key: String,
        value: String,
        category: String = "Preference",
        memoryEnabled: Boolean = true
    ): SanitizationResult {
        if (!memoryEnabled) {
            return SanitizationResult(
                isSafe = false,
                sanitizedText = "",
                blockedReasonHinglish = "Local Memory Opt-Out ON hai. Memory save karne ke liye Memory screen ya Settings mein 'Save Local Preferences' enable karein."
            )
        }
        val check = PrivacyGuard.validateMemoryPreference(key, value)
        if (!check.isSafe) return check

        val existing = dao.getMemoryFactByKey(key.trim())
        if (existing != null) {
            dao.insertMemoryFact(
                existing.copy(
                    value = check.sanitizedText,
                    category = category,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            dao.insertMemoryFact(
                MemoryFactEntity(
                    key = key.trim().take(60),
                    value = check.sanitizedText,
                    category = category
                )
            )
        }
        return check
    }

    suspend fun updateMemoryFact(fact: MemoryFactEntity): SanitizationResult {
        val check = PrivacyGuard.validateMemoryPreference(fact.key, fact.value)
        if (!check.isSafe) return check
        dao.insertMemoryFact(
            fact.copy(
                key = fact.key.trim().take(60),
                value = check.sanitizedText,
                updatedAt = System.currentTimeMillis()
            )
        )
        return check
    }

    suspend fun deleteMemoryFact(id: Int) = dao.deleteMemoryFactById(id)

    suspend fun clearAllMemoryFacts() = dao.clearAllMemoryFacts()

    suspend fun addChatMessage(
        isUser: Boolean,
        text: String,
        languageCode: String,
        engineSource: EngineSource,
        codeSnippet: String? = null
    ) {
        dao.insertChatMessage(
            ChatMessageEntity(
                isUser = isUser,
                text = text,
                languageCode = languageCode,
                engineSource = engineSource.name,
                codeSnippet = codeSnippet
            )
        )
    }

    suspend fun clearChatHistory() = dao.clearAllChatMessages()

    suspend fun addReminder(title: String, timeText: String) {
        dao.insertReminder(
            ReminderEntity(
                title = title.trim().take(120),
                scheduledTimeText = timeText.trim().take(40)
            )
        )
    }

    suspend fun toggleReminder(id: Int, completed: Boolean) = dao.setReminderCompleted(id, completed)

    suspend fun deleteReminder(id: Int) = dao.deleteReminderById(id)

    suspend fun wipeAllLocalData() {
        dao.clearAllMemoryFacts()
        dao.clearAllChatMessages()
        dao.clearAllReminders()
    }
}
