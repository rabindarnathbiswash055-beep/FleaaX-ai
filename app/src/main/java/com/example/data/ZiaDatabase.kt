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

@Entity(tableName = "planner_tasks")
data class PlannerTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subjectOrTag: String = "Study",
    val dayOffset: Int = 0, // 0 = Today, 1 = Tomorrow, .. 6
    val timeSlot: String = "06:00 PM",
    val recurrence: String = TaskRecurrence.ONCE.name,
    val durationMinutes: Int = 25,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "knowledge_notes")
data class KnowledgeNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String = NoteCategory.NOTE.name,
    val languageOrTag: String = "General",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gaming_profiles")
data class GamingProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gameTitle: String,
    val sensitivityNotes: String,
    val matchChecklist: String,
    val ytIdeaNotes: String,
    val updatedAt: Long = System.currentTimeMillis()
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

    // Planner Tasks
    @Query("SELECT * FROM planner_tasks ORDER BY dayOffset ASC, isCompleted ASC, createdAt DESC")
    fun getAllPlannerTasks(): Flow<List<PlannerTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannerTask(task: PlannerTaskEntity)

    @Query("UPDATE planner_tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setPlannerTaskCompleted(id: Int, completed: Boolean)

    @Query("DELETE FROM planner_tasks WHERE id = :id")
    suspend fun deletePlannerTaskById(id: Int)

    @Query("DELETE FROM planner_tasks")
    suspend fun clearAllPlannerTasks()

    // Knowledge Vault & Code Snippets Library
    @Query("SELECT * FROM knowledge_notes ORDER BY updatedAt DESC")
    fun getAllKnowledgeNotes(): Flow<List<KnowledgeNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgeNote(note: KnowledgeNoteEntity)

    @Query("DELETE FROM knowledge_notes WHERE id = :id")
    suspend fun deleteKnowledgeNoteById(id: Int)

    @Query("DELETE FROM knowledge_notes")
    suspend fun clearAllKnowledgeNotes()

    // Gaming Profiles
    @Query("SELECT * FROM gaming_profiles ORDER BY updatedAt DESC")
    fun getAllGamingProfiles(): Flow<List<GamingProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGamingProfile(profile: GamingProfileEntity)

    @Query("DELETE FROM gaming_profiles WHERE id = :id")
    suspend fun deleteGamingProfileById(id: Int)

    @Query("DELETE FROM gaming_profiles")
    suspend fun clearAllGamingProfiles()
}

@Database(
    entities = [
        MemoryFactEntity::class,
        ChatMessageEntity::class,
        ReminderEntity::class,
        PlannerTaskEntity::class,
        KnowledgeNoteEntity::class,
        GamingProfileEntity::class
    ],
    version = 2,
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
    val plannerTasks: Flow<List<PlannerTaskEntity>> = dao.getAllPlannerTasks()
    val knowledgeNotes: Flow<List<KnowledgeNoteEntity>> = dao.getAllKnowledgeNotes()
    val gamingProfiles: Flow<List<GamingProfileEntity>> = dao.getAllGamingProfiles()

    suspend fun ensureStarterDataSeeded() {
        val currentMessages = dao.getAllChatMessages().first()
        if (currentMessages.isEmpty()) {
            dao.insertChatMessage(
                ChatMessageEntity(
                    isUser = false,
                    text = "Namaste & Hello! Main ZIA AI hoon — aapki personal multilingual voice & productivity assistant (English, Hinglish, Banglish, aur বাংলা).\n\n" +
                        "• Offline Commands: \"Volume 60% karo\", \"7:30 ka alarm lagao\", \"Open YouTube\", \"Wi-Fi settings kholo\".\n" +
                        "• Workspace Hub: Daily Study Planner & Focus Timer, Coding Lab & Snippet Library, Gaming Companion, aur Personal Knowledge Vault + Smart Search.\n" +
                        "• Privacy First: Aapki memory phone par local rehti hai aur har sensitive action se pehle Confirmation Preview dikhta hai.",
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
                    category = "Preference"
                )
            )
            dao.insertMemoryFact(
                MemoryFactEntity(
                    key = "Favourite Command",
                    value = "Hey ZIA, 7:30 ka alarm lagao aur Focus Timer start karo",
                    category = "Favourite Command"
                )
            )
            dao.insertMemoryFact(
                MemoryFactEntity(
                    key = "Project Note",
                    value = "ZIA AI Modular Hybrid Voice Assistant for Redmi 9 (Android 10 · 6 GB RAM)",
                    category = "Project Note"
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

        val currentTasks = dao.getAllPlannerTasks().first()
        if (currentTasks.isEmpty()) {
            dao.insertPlannerTask(
                PlannerTaskEntity(
                    title = "Kotlin Coroutines & StateFlow Revision",
                    subjectOrTag = "Android Study",
                    dayOffset = 0,
                    timeSlot = "07:00 PM",
                    recurrence = TaskRecurrence.DAILY.name,
                    durationMinutes = 25
                )
            )
            dao.insertPlannerTask(
                PlannerTaskEntity(
                    title = "Data Structures Practice (Arrays & HashMaps)",
                    subjectOrTag = "CS Fundamentals",
                    dayOffset = 1,
                    timeSlot = "08:30 PM",
                    recurrence = TaskRecurrence.WEEKDAYS.name,
                    durationMinutes = 45
                )
            )
        }

        val currentNotes = dao.getAllKnowledgeNotes().first()
        if (currentNotes.isEmpty()) {
            dao.insertKnowledgeNote(
                KnowledgeNoteEntity(
                    title = "Jetpack Compose State Hoisting Snippet",
                    content = "@Composable\nfun CounterRow(count: Int, onIncrement: () -> Unit) {\n    Row(verticalAlignment = Alignment.CenterVertically) {\n        Text(\"Count: \$count\")\n        Button(onClick = onIncrement) { Text(\"+1\") }\n    }\n}",
                    category = NoteCategory.CODE_SNIPPET.name,
                    languageOrTag = "Kotlin"
                )
            )
            dao.insertKnowledgeNote(
                KnowledgeNoteEntity(
                    title = "Redmi 9 (6 GB RAM) Performance Checklist",
                    content = "1. Keep animations single-layer Canvas.\n2. Use Room Flow queries on Dispatchers.IO.\n3. Avoid background wake-locks; use Push-to-Talk.",
                    category = NoteCategory.PROJECT_IDEA.name,
                    languageOrTag = "Architecture"
                )
            )
        }

        val currentGaming = dao.getAllGamingProfiles().first()
        if (currentGaming.isEmpty()) {
            dao.insertGamingProfile(
                GamingProfileEntity(
                    gameTitle = "BGMI / Mobile Tactical FPS (Redmi 9 Profile)",
                    sensitivityNotes = "Camera: 120% · ADS Red Dot: 55% · 3x Scope: 26% · Gyroscope: 280% (Smooth + Extreme/Ultra Frame Rate)",
                    matchChecklist = "[x] Clear background cache before match\n[x] Disable auto-brightness\n[x] Check ping < 50ms & earphones mic",
                    ytIdeaNotes = "Title: Redmi 9 (6GB RAM) Smooth Gameplay + Zero Lag Sensitivity Guide 2026!\nThumbnail Concept: Split screen FPS counter + sensitivity sliders with neon cyan glow."
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
                blockedReasonHinglish = "Local Memory Opt-Out ON hai. Memory save karne ke liye Memory ya Privacy Center mein 'Save Local Preferences' enable karein."
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

    // Planner Tasks CRUD
    suspend fun addPlannerTask(
        title: String,
        subjectOrTag: String,
        dayOffset: Int,
        timeSlot: String,
        recurrence: TaskRecurrence,
        durationMinutes: Int
    ) {
        dao.insertPlannerTask(
            PlannerTaskEntity(
                title = title.trim().take(120),
                subjectOrTag = subjectOrTag.trim().take(40).ifEmpty { "Study" },
                dayOffset = dayOffset.coerceIn(0, 6),
                timeSlot = timeSlot.trim().take(30).ifEmpty { "06:00 PM" },
                recurrence = recurrence.name,
                durationMinutes = durationMinutes.coerceIn(5, 180)
            )
        )
    }

    suspend fun togglePlannerTask(id: Int, completed: Boolean) =
        dao.setPlannerTaskCompleted(id, completed)

    suspend fun deletePlannerTask(id: Int) = dao.deletePlannerTaskById(id)

    // Knowledge Vault & Code Snippets CRUD
    suspend fun saveKnowledgeNote(
        title: String,
        content: String,
        category: NoteCategory,
        languageOrTag: String
    ): SanitizationResult {
        val check = PrivacyGuard.validateMemoryPreference(title, content.take(180))
        if (!check.isSafe) return check
        dao.insertKnowledgeNote(
            KnowledgeNoteEntity(
                title = title.trim().take(100),
                content = content.trim().take(4000),
                category = category.name,
                languageOrTag = languageOrTag.trim().take(40).ifEmpty { "General" }
            )
        )
        return SanitizationResult(isSafe = true, sanitizedText = content.trim())
    }

    suspend fun updateKnowledgeNote(note: KnowledgeNoteEntity) {
        dao.insertKnowledgeNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteKnowledgeNote(id: Int) = dao.deleteKnowledgeNoteById(id)

    // Gaming Profiles CRUD
    suspend fun saveGamingProfile(
        id: Int = 0,
        gameTitle: String,
        sensitivityNotes: String,
        matchChecklist: String,
        ytIdeaNotes: String
    ) {
        dao.insertGamingProfile(
            GamingProfileEntity(
                id = id,
                gameTitle = gameTitle.trim().take(80),
                sensitivityNotes = sensitivityNotes.trim().take(600),
                matchChecklist = matchChecklist.trim().take(600),
                ytIdeaNotes = ytIdeaNotes.trim().take(800)
            )
        )
    }

    suspend fun deleteGamingProfile(id: Int) = dao.deleteGamingProfileById(id)

    suspend fun wipeAllLocalData() {
        dao.clearAllMemoryFacts()
        dao.clearAllChatMessages()
        dao.clearAllReminders()
        dao.clearAllPlannerTasks()
        dao.clearAllKnowledgeNotes()
        dao.clearAllGamingProfiles()
    }
}
