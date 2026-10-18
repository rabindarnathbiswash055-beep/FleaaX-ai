# ZIA AI — Ultimate Feature Expansion Plan

**ZIA AI** (*"Your Voice. Your World. Your ZIA."*) is upgrading from a core hybrid voice assistant into a comprehensive, modular, privacy-first personal workspace for Android 10+ (Redmi 9 · 6 GB RAM). This expansion preserves 100% of the existing working voice, chat, offline command, and anime avatar functionality while adding an integrated **Workspace Hub** (Daily Planner & Focus Timer, Coding Lab & Snippet Library, Gaming Companion, Knowledge Vault & Smart Search) and a dedicated **Privacy Control Center**.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Choices from Phase 1**
> - **Delivery Strategy**: Deliver all expansion modules (Phases A through J) in one cohesive, verified build on top of the existing working ZIA AI codebase without breaking or removing any existing features.
> - **Multimodal Gaming Analysis & Cited Smart Search**: Enable Android's zero-permission Photo Picker (`PickVisualMedia`) for user-selected gameplay screenshot analysis (gated by an explicit cloud upload confirmation) and Gemini-powered web search with source citations alongside instant offline local vault search.
> - **Navigation Architecture**: Maintain 5 ergonomic bottom navigation tabs (`Home`, `Chat`, `Workspace`, `Controls`, `Privacy & Settings`), where the new **`Workspace` Hub Tab** hosts clean pill-switched sub-modules for **Planner & Focus**, **Coding Lab**, **Gaming Companion**, **Knowledge Vault & Search**, and **Memory**.

> [!NOTE]
> **Transparency, Safety & Redmi 9 Hardware Guardrails**
> - **No Fake Emotional States or Hidden Background Actions**: ZIA's personalities (`Friendly`, `Professional`, `Funny`, `Study Buddy`, `Gaming Assistant`, `Concise`) and anime avatar themes are clearly presented as user-configurable UI/UX styles—never as factual internal emotions.
> - **OS Permission Honesty**: In the **Privacy Control Center**, disabling an in-app toggle immediately stops ZIA from using that feature, and clearly explains that revoking an actual Android OS permission requires opening the system App Permissions screen (via a one-tap shortcut).
> - **Fair-Play Gaming Companion**: Provides legitimate sensitivity profiles, match checklists, YouTube creator tools, and manual user-picked screenshot analysis—never promising automatic background gameplay recognition or anti-cheat bypasses.

---

## 1. Overview & Core Concept

- **What It Does**: Expands ZIA AI into an all-in-one multilingual (English, Hinglish, Banglish, বাংলা) companion that combines offline device control and voice assistance with a **Daily Study Planner & Pomodoro Focus Timer**, a **Coding Assistant & Local Snippet Library**, a **Gaming Companion & YouTube Creator Studio**, a **Personal Knowledge Vault with SAF Document Summarization**, **Offline + Online Smart Search**, and a **Privacy Control Center**.
- **Target Audience / Persona**: Students, mobile developers, gamers, and creators using mid-range Android 10 smartphones (such as Redmi 9) who need a single lightweight, battery-conscious native APK for productivity, coding, study sessions, and device automation.
- **Key Value**: Every new productivity module works offline first using local SQLite persistence, while seamlessly offering opt-in cloud AI acceleration (code explanation, document summarization, screenshot analysis, and cited search) only when online and explicitly authorized.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Home & Customizable Anime Avatar Stage**:
   - Displays live connection status, response latency (`ms`), and app RAM footprint (`MB`), plus quick language chips (`Auto`, `EN`, `Hinglish`, `Banglish`, `বাংলা`).
   - Users can customize ZIA's **Anime Avatar Theme** (`Sapphire Cyber`, `Violet Neural`, `Emerald Matrix`, `Sunset Rose`), **Custom Greeting**, **Response Length** (`Short`, `Balanced`, `Detailed`), and **Personality** (`Friendly`, `Professional`, `Funny`, `Study Buddy`, `Gaming Assistant`, `Concise`).
2. **Multilingual Chat with One-Tap "Save to Vault / Code Library"**:
   - Every assistant reply now includes a **Save to Knowledge Vault** action, and every generated code block (Kotlin, JavaScript, HTML, CSS, Python) includes both **Copy** and **Save to Code Library** actions.
3. **Workspace Hub Tab (5 Integrated Sub-Modules)**:
   - **Daily Planner & Focus Timer**: Interactive 7-day calendar strip, study task & schedule creator with recurrence tags (`Once`, `Daily`, `Weekdays`), completion tracking, system alarm handoff, and an interactive **Study Focus Timer** (`25m Study`, `5m Break`, `45m Deep Work`) with integrated media playback controls.
   - **Coding Lab & Snippet Library**: Dedicated prompt actions for *Explain Concept*, *Debug Error Message*, and *Generate Snippet* (`Kotlin`, `JavaScript`, `HTML/CSS`, `Python`), a searchable local **Code-Notes Library**, and optional **SAF Code File Editor** to load, edit, and copy user-selected files safely.
   - **Gaming Companion**: Manage favorite games, custom sensitivity/HUD profiles (Camera, ADS, Gyroscope notes), and match checklists; generate **YouTube Gaming Video Ideas, Titles, Descriptions & Thumbnail Concepts**; and run **Manual Gameplay Screenshot Analysis** by picking a screenshot via Android Photo Picker with an explicit cloud-upload confirmation gate.
   - **Knowledge Vault & Smart Search**: Unified search bar that queries local Memory, Vault Notes, Code Snippets, and Study Tasks 100% offline first, with an optional **Run Cited Online Search** button that clearly separates **Retrieved Facts & Sources** from **AI-Generated Synthesis**. Also supports importing & summarizing user-selected SAF documents with explicit cloud consent.
   - **Smart Personal Memory**: Search, add, edit, delete, and **Export Local Memory (JSON/Text)**, protected by `PrivacyGuard` and a master enable/disable switch.
4. **Device Action Center (`Controls` Tab)**:
   - Streamlined access to installed app launching, Media/Ring/Alarm/Notification volume sliders, Wi-Fi/Bluetooth/Sound/Display system panels, confirmation-gated Call & SMS draft composers, Camera intent, and opt-in Notification Reader.
5. **Privacy Control Center & Settings (`Privacy & Settings` Tab)**:
   - Unified dashboard displaying real-time OS permission badges (`Microphone: Granted/Denied`, `Notification Access: Granted/Not Granted`), master toggles for **Cloud AI**, **Personal Context & Document Upload Consent**, **Local Memory**, **Voice Input**, and **Voice Replies**, one-tap **Wipe All Saved Data**, runtime memory/latency diagnostics, and transparent Android 10 permission explanations.

### Visual Identity & Theme
- **Aesthetic Direction**: **Cyber-Nocturne Modular Console** — high-contrast obsidian surfaces (`#090B14`, `#121726`, `#1A2136`) with user-selectable avatar accent palettes (`#4F8CFF` Sapphire, `#8B5CF6` Violet, `#10B981` Emerald, `#F43F5E` Sunset Rose).
- **Typography & Hierarchy**: Bundled `Space Grotesk` for headers and telemetry badges, `Plus Jakarta Sans` for multilingual English/Hinglish/Banglish/Bengali readability, and `JetBrains Mono` for the Coding Lab, sensitivity profiles, and latency metrics.
- **Interactive Feedback & Motion**: Lightweight single-pass Canvas drawing and smooth pill transitions that consume zero background CPU when idle.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Provider-Independent Hybrid AI Interface with Multimodal & Search Support**
  - *Chosen Approach*: Extend `RemoteAIProvider` with structured methods for text reasoning, multimodal image analysis (`analyzeImageWithPrompt`), and grounded search/summarization, all gated by explicit user consent and backed by `LocalAssistantEngine` offline fallbacks.
  - *Why*: Allows swapping or upgrading cloud/local models without touching UI or ViewModel layers, and guarantees the app never crashes or hangs when offline.
- **Decision 2: Non-Destructive Room Database Expansion**
  - *Chosen Approach*: Preserve `MemoryFactEntity`, `ChatMessageEntity`, and `ReminderEntity`, and add `KnowledgeNoteEntity` (for Vault notes, saved AI answers, and code snippets), `PlannerTaskEntity` (for study sessions, schedules, and recurring tasks), and `GamingProfileEntity` (for game sensitivity profiles, checklists, and creator ideas).
  - *Why*: Keeps all user data structured, searchable offline in under 10ms, and 100% local by default.
- **Decision 3: Explicit Cloud Upload Confirmation for Documents & Screenshots**
  - *Chosen Approach*: Before sending any user-selected SAF document text or Photo Picker gameplay screenshot to the cloud AI provider, ZIA checks a dedicated consent gate and displays a clear notice explaining what is being sent. Offline local summarization/inspection remains available without cloud upload.
  - *Why*: Strictly honors the privacy-first architecture and prevents unintentional data uploads.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────────────┐
│                        ZIA AI — 5-TAB ADAPTIVE UI LAYER                        │
│  ┌──────────┐  ┌──────────┐  ┌───────────────────┐  ┌──────────┐  ┌─────────┐  │
│  │   Home   │  │   Chat   │  │   Workspace Hub   │  │ Controls │  │Privacy &│  │
│  │ & Avatar │  │ & Coding │  │ • Planner & Focus │  │  Center  │  │Settings │  │
│  │  Stage   │  │  Stream  │  │ • Coding Lab      │  │          │  │ Center  │  │
│  │          │  │          │  │ • Gaming Companion│  │          │  │         │  │
│  │          │  │          │  │ • Vault & Search  │  │          │  │         │  │
│  │          │  │          │  │ • Smart Memory    │  │          │  │         │  │
│  └────┬─────┘  └────┬─────┘  └─────────┬─────────┘  └────┬─────┘  └────┬────┘  │
│       └─────────────┴──────────────────┼─────────────────┴─────────────┘       │
│                                        ▼                                       │
│                        ┌───────────────────────────────┐                       │
│                        │     ZiaAssistantViewModel     │                       │
│                        │ (StateFlow + Latency/RAM Mon) │                       │
│                        └───────────────┬───────────────┘                       │
└────────────────────────────────────────┼───────────────────────────────────────┘
                                         ▼
┌────────────────────────────────────────────────────────────────────────────────┐
│                    PROVIDER-INDEPENDENT HYBRID ORCHESTRATOR                    │
│                                                                                │
│   Input / File / Image ──► [ PrivacyGuard & Cloud Upload Consent Gate ]        │
│                                              │                                 │
│         ┌────────────────────────────────────┼──────────────────────┐          │
│         ▼ (Offline Commands)                 ▼ (Online + Consent)   ▼ (Offline)│
│  ┌─────────────────────┐          ┌───────────────────────┐  ┌───────────────┐ │
│  │ LocalCommandEngine  │          │   RemoteAIProvider    │  │LocalAssistant │ │
│  │ (EN/HI/BN Intents)  │          │ • Text & Coding       │  │• Code Library │ │
│  └──────────┬──────────┘          │ • Screenshot Vision   │  │• Gaming Tips  │ │
│             ▼                     │ • Grounded Web Search │  │• Vault Search │ │
│  ┌─────────────────────┐          │ • Doc Summarizer      │  │• Study Helper │ │
│  │  ConfirmationGate   │          └───────────┬───────────┘  └───────┬───────┘ │
│  │  & ActionExecutor   │                      └───────────┬──────────┘         │
│  └─────────────────────┘                                  ▼                    │
│                                          [ Local Room SQLite & DataStore ]     │
└────────────────────────────────────────────────────────────────────────────────┘
```

### Data Model & State
- **Expanded `PersonalityStyle`**: `FRIENDLY`, `PROFESSIONAL`, `FUNNY`, `STUDY_BUDDY`, `GAMING_ASSISTANT`, `CONCISE`.
- **`AvatarThemePreset`**: `SAPPHIRE_CYBER` (`#4F8CFF`), `VIOLET_NEURAL` (`#8B5CF6`), `EMERALD_MATRIX` (`#10B981`), `SUNSET_ROSE` (`#F43F5E`).
- **`ResponseLengthPreference`**: `SHORT`, `BALANCED`, `DETAILED`.
- **Room Database Entities (`ZiaDatabase` v2)**:
  - `MemoryFactEntity`: `id`, `key`, `value`, `category`, `updatedAt`.
  - `ChatMessageEntity`: `id`, `isUser`, `text`, `languageCode`, `engineSource`, `codeSnippet`, `timestamp`.
  - `ReminderEntity`: `id`, `title`, `scheduledTimeText`, `isCompleted`, `createdAt`.
  - `PlannerTaskEntity`: `id`, `title`, `subjectOrTag`, `dayOffset` (`0..6` for 7-day calendar strip), `timeSlot`, `recurrence` (`ONCE`, `DAILY`, `WEEKDAYS`), `durationMinutes`, `isCompleted`, `createdAt`.
  - `KnowledgeNoteEntity`: `id`, `title`, `content`, `category` (`NOTE`, `AI_ANSWER`, `CODE_SNIPPET`, `PROJECT_IDEA`, `DOC_SUMMARY`), `languageOrTag`, `updatedAt`.
  - `GamingProfileEntity`: `id`, `gameTitle`, `sensitivityNotes`, `matchChecklist`, `ytIdeaNotes`, `updatedAt`.
- **`PerformanceMetrics`**: `appUsedRamMb`, `deviceAvailableRamGb`, `lastResponseLatencyMs`, `activeEngineLabel`.

### Interactive Component & State Mapping
- **Daily Planner & Study Focus Timer**:
  - Users select a day on the 7-day calendar bar, add study sessions or recurring tasks, toggle completion checkboxes, or launch a live countdown **Focus Session Timer** (`Start`, `Pause`, `Reset`, `25m / 5m / 45m` presets) paired with media session controls.
- **Coding Lab & Safe File Editor**:
  - Users can pick a language (`Kotlin`, `JavaScript`, `HTML`, `CSS`, `Python`), enter a concept or error log, and generate/save snippets into the local `KnowledgeNoteEntity` (`CODE_SNIPPET` filter). When editing a user-picked SAF file, changes are held in an interactive editor buffer with one-tap copy/save to the local vault.
- **Gaming Companion & Screenshot Analyzer**:
  - Users can create/edit game profiles, generate YouTube Gaming titles/descriptions/thumbnail prompts, or pick a screenshot via `ActivityResultContracts.PickVisualMedia`. Selecting a screenshot shows a preview card and an explicit **"Analyze Gameplay Screenshot with Cloud AI"** confirmation button.
- **Knowledge Vault & Cited Smart Search**:
  - Typing a query immediately filters local Memory, Vault Notes, Code Snippets, and Planner Tasks offline. Tapping **"Search Online & Summarize"** queries `RemoteAIProvider` and renders a structured card separating **Retrieved Key Facts & Source Domains** from **ZIA's Generated Summary**.
- **Privacy Control Center**:
  - Displays live Android permission states (`ContextCompat.checkSelfPermission` for `RECORD_AUDIO` and `NotificationVault.isNotificationAccessGranted`), interactive switches for every privacy/voice/cloud control, memory export, and confirmation-gated data wipe.
