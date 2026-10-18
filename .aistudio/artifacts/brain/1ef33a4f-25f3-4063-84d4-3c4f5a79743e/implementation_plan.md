# ZIA AI — Native Android Multilingual Hybrid Voice Assistant

**ZIA AI** (*"Your Voice. Your World. Your ZIA."*) is a privacy-first, native Android personal voice and chat assistant designed for smooth execution on Android 10+ devices (such as Redmi 9 with 6 GB RAM). It combines a deterministic offline command engine with optional Gemini cloud reasoning, an interactive anime-style girl character avatar with lightweight reactive state rings, multilingual speech and voice synthesis (English, Hinglish, Banglish, and বাংলা), confirmation-gated phone actions, and transparent local-only preference memory.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Confirmed Choices from Phase 1**
> - **Delivery Scope**: Build the complete, full-featured ZIA AI application across all core screens and capabilities in one verified release, compiled directly via our cloud Android build environment into an installable APK (no PC or local Acode build toolchain hurdles).
> - **Hybrid Cloud AI Provider**: Built-in **Gemini API (`gemini-3.5-flash`)** configured via `BuildConfig.GEMINI_API_KEY` alongside a user-configurable custom API key / endpoint override and an explicit cloud-data consent toggle.
> - **Avatar Visual Identity**: Custom anime-style female AI assistant character portrait paired with lightweight, battery-friendly Jetpack Compose Canvas state rings and waveforms for `Idle`, `Listening`, `Thinking`, `Speaking`, and `Unavailable` states.

> [!NOTE]
> **Android 10 & Mobile Hardware Feasibility Assessment (Redmi 9 / 6 GB RAM)**
> - **Push-to-Talk vs. Always-On Hotword**: Standard Android `SpeechRecognizer` is not a continuous low-power hardware DSP hotword engine. To prevent severe battery drain, OEM background process kills on MIUI/Android 10, and unintended microphone recording, ZIA AI uses **Push-to-Talk** on the Home and Chat screens with a visible listening indicator and instant stop button, plus an interactive **Wake-Word ("Hey ZIA") Foreground Session Mode** and hardware/battery feasibility diagnostics screen.
> - **Least-Privilege Phone Controls**: Actions such as placing calls, sending SMS, setting alarms, opening apps, capturing photos, or toggling Wi-Fi/Bluetooth use documented Android system intents, panels, and explicit user **Confirmation Preview Cards** rather than silent background execution.

---

## 1. Overview & Core Concept

- **What It Does**: ZIA AI acts as a bilingual/multilingual daily companion and device controller. Users can speak or type in **English, Hinglish, Banglish, or বাংলা (Bengali)** to launch installed apps, adjust media/ring/alarm volume streams, prepare calls or SMS messages with confirmation previews, create local reminders and system alarms, inspect user-selected files via the Storage Access Framework, control active media playback, get coding explanations/snippets, and converse with ZIA in 4 distinct personalities (`Friendly`, `Professional`, `Concise`, `Playful`).
- **Target Audience / Persona**: Mobile-first users in South Asia and beyond running mid-range Android 10+ smartphones who want a responsive, culturally fluent (Hinglish/Banglish/Bengali/English) assistant that respects device resources and personal privacy.
- **Key Value**: Works reliably offline for device commands, reminders, volume/settings controls, and local assistant help, while seamlessly upgrading to cloud AI (`gemini-3.5-flash`) for complex reasoning and coding tasks only when online and explicitly permitted by the user.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Home & Anime Avatar Hub**:
   - Displays a top status bar showing active connectivity (`Online · Hybrid AI`, `Offline · Local Engine Only`, or `Cloud Opt-Out Active`), selected language pill (`EN`, `Hinglish`, `Banglish`, `বাংলা`), and active personality badge.
   - Centerpiece **Anime Character Avatar Stage** displaying ZIA's custom anime portrait framed by lightweight, state-driven radial Canvas rings (`Idle` breathing aura, `Listening` cyan acoustic pulse, `Thinking` orbital violet arc, `Speaking` harmonic equalizer bars, `Unavailable` amber muted ring) and a clear disclaimer badge (*"AI Assistant Graphic · Push-to-Talk"*).
   - Bottom push-to-talk microphone dock with explicit `Stop Listening` / `Mute TTS` controls, quick text input bar, and recent conversation card preview with a **Confirmation Preview Modal** whenever ZIA proposes a sensitive phone action.
2. **Full Chat & Coding Helper Flow**:
   - Threaded bilingual/multilingual conversation history with source badges (`Local Engine`, `On-Device Assistant`, `Gemini Cloud`), syntax-highlighted/copyable code snippet cards, voice replay buttons per message, and one-tap confirmation cards for pending phone actions.
3. **Quick Actions & Phone Controls Center**:
   - Categorized interactive grid for **App Launcher** (search and launch installed apps or test package intents with graceful fallback), **Volume & System Panels** (live sliders for Media, Ring, Notification, and Alarm volume + one-tap Wi-Fi/Bluetooth/Sound settings panels), **Calls & SMS Composer** (prefilled dialer/SMS with mandatory confirmation preview), **Alarms & Local Reminders**, **Media Playback Controller** (Play/Pause/Next/Previous + active media session info), **Camera & SAF File Inspector** (inspect user-picked text/code file metadata and content safely), and **Coding Helper Templates**.
4. **Memory & Privacy Vault**:
   - View, add, edit, or delete explicit assistant preferences (e.g., preferred name, favorite language, coding language, response style) stored locally on-device. Includes a master **"Save Assistant Memory"** opt-out switch, **"Allow Cloud AI Queries"** consent gate, and one-tap **"Wipe All Local Memory & Chat History"**.
5. **Offline Mode & Diagnostics Hub**:
   - Live capability matrix comparing what works 100% offline vs. what requires internet, plus a dedicated **Wake-Word ("Hey ZIA") & On-Device LLM Hardware Evaluator** that inspects actual device RAM/storage (`ActivityManager.MemoryInfo`, `StatFs`) on the user's phone and explains Android 10 battery/microphone rules in friendly Hinglish + English.
6. **Settings & Notification Reader Setup**:
   - Configure language mode (`Auto-Detect / Mixed`, `English`, `Hinglish`, `Banglish`, `বাংলা`), TTS speech rate & pitch, personality preset, Cloud AI configuration (Built-in Gemini vs. Custom API Key override), Notification Reader toggle with system permission status, and a built-in **Hinglish Build, Privacy & APK Guide**.

### Visual Identity & Theme
- **Aesthetic Direction**: **Cyber-Nocturne Anime Console** — a deep, eye-friendly obsidian dark interface with restrained electric blue and violet accents, crisp glassmorphic cards, and clear status telemetry.
- **Color Palette & Mood**:
  - `--background`: `#090B14` (Deep Cosmic Obsidian)
  - `--surface`: `#121726` (Elevated Midnight Slate)
  - `--surface-variant`: `#1A2136` (Translucent Card Surface)
  - `--primary`: `#4F8CFF` (ZIA Electric Sapphire Blue)
  - `--secondary`: `#8B5CF6` (Neural Violet Accent)
  - `--tertiary`: `#22D3EE` (Acoustic Cyan for Listening/Voice states)
  - `--warning` / `--error`: `#F59E0B` (Amber Offline/Permission Alert) / `#EF4444` (Crimson Stop/Delete)
- **Typography & Hierarchy**:
  - Locally bundled Google Fonts (`Space Grotesk` for futuristic headers and status telemetry; `Plus Jakarta Sans` for crisp multilingual Latin/Bengali body legibility; `JetBrains Mono` for the Coding Helper and diagnostics).
- **Interactive Feedback & Motion**:
  - Lightweight Compose `animateFloatAsState` and `infiniteRepeatable` single-layer Canvas strokes capped at low CPU/GPU overhead so Redmi 9 (Helio G80 / Mali-G52) stays cool and responsive at 60fps.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Hybrid 3-Tier Routing (`LocalCommandEngine` -> `LocalAssistantEngine` -> `RemoteAIProvider`)**
  - *Chosen Approach*: Every user utterance or text message is first evaluated by `LocalCommandEngine` (deterministic regex + keyword intent parser supporting English, Hinglish, Banglish, and Bengali phrases like *"alarm lagao"*, *"volume kam karo"*, *"open YouTube"*, *"kemon acho"*). If it is not a device command, it checks whether Cloud AI is online and permitted: if yes, it queries `RemoteAIProvider` (`gemini-3.5-flash` with personality, language, and opt-in local memory context); if offline or cloud-disabled, `LocalAssistantEngine` provides helpful offline bilingual responses, coding reference templates, and math/utility answers.
  - *Why*: Guarantees sub-50ms execution for phone controls without wasting mobile data or API quota, while keeping the app genuinely useful even with zero internet connectivity.
- **Decision 2: Strict Confirmation Gate for Consequential Phone Actions**
  - *Chosen Approach*: Whenever a voice or text command triggers a phone call, SMS composition, alarm creation, system setting modification, or data deletion, `ActionExecutor` creates a `PendingAction` object first. The UI presents a **Confirmation Preview Card** showing the exact target, parameters, and permission required before the user taps **Confirm & Execute**.
  - *Why*: Prevents accidental dialer/message actions from misheard speech recognition and satisfies strict Android security best practices.
- **Decision 3: Privacy-First Local Storage (Room + DataStore)**
  - *Chosen Approach*: Assistant memory facts, chat messages, and local reminders are stored strictly on-device in a Room SQLite database and Jetpack DataStore preferences. Passwords, OTPs, tokens, or sensitive patterns are automatically filtered out by a `PrivacyGuard` sanitizer before saving to memory or sending to cloud AI.
  - *Why*: Directly fulfills the requirement to keep memory local by default, give users full view/edit/delete control, and never upload private notification or personal data.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        ZIA AI — PRESENTATION LAYER                         │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌───────────────────┐  │
│  │ Home & Anime │ │  Multilingual│ │Quick Actions │ │ Memory, Offline & │  │
│  │ Avatar Stage │ │Chat & Coding │ │& Phone Panel │ │  Settings Screens │  │
│  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └─────────┬─────────┘  │
│         └────────────────┴────────┬───────┴───────────────────┘            │
│                                   ▼                                        │
│                    ┌─────────────────────────────┐                         │
│                    │   ZiaAssistantViewModel     │                         │
│                    │  (StateFlow<ZiaUiState>)    │                         │
│                    └──────────────┬──────────────┘                         │
└───────────────────────────────────┼────────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────────┐
│                     HYBRID ASSISTANT ORCHESTRATOR                          │
│                                                                            │
│   User Input (Voice / Text) ──► [ PrivacyGuard Sanitizer ]                 │
│                                           │                                │
│         ┌─────────────────────────────────┼───────────────────────┐        │
│         ▼ (1. Deterministic Match)        ▼ (2. Online & Consent) ▼ (3.Off)│
│  ┌────────────────────┐        ┌─────────────────────┐  ┌────────────────┐ │
│  │ LocalCommandEngine │        │  RemoteAIProvider   │  │ LocalAssistant │ │
│  │ (EN/HI/BN Intents) │        │ (Gemini 3.5 Flash)  │  │ Engine (Rules, │ │
│  └─────────┬──────────┘        └──────────┬──────────┘  │ Code & Math)   │ │
│            ▼                              └──────┬──────┴────────┬───────┘ │
│  ┌────────────────────┐                          ▼               │         │
│  │  ConfirmationGate  │──► [ ActionExecutor ] ◄──┴───────────────┘         │
│  └────────────────────┘                                                    │
└─────────┬──────────────────────────┬───────────────────────────┬───────────┘
          ▼                          ▼                           ▼
┌───────────────────┐    ┌───────────────────────┐   ┌───────────────────────┐
│  SPEECH & MEDIA   │    │  ANDROID SYSTEM APIs  │   │   LOCAL PERSISTENCE   │
│ • SpeechInput     │    │ • PackageManager Apps │   │ • Room Database       │
│   (Recognizer)    │    │ • AudioManager Volume │   │   - MemoryFactEntity  │
│ • SpeechOutput    │    │ • AlarmClock / Dialer │   │   - ChatMessageEntity │
│   (Android TTS)   │    │ • Settings Panels     │   │   - ReminderEntity    │
│ • MediaController │    │ • SAF File Picker     │   │ • PreferenceStore     │
│ • ZiaNotification │    │ • Camera Capture      │   │   (DataStore + OptIn) │
│   ListenerService │    │ • ConnectivityManager │   │                       │
└───────────────────┘    └───────────────────────┘   └───────────────────────┘
```

### Data Model & State
- **`AvatarState`**: `IDLE`, `LISTENING`, `THINKING`, `SPEAKING`, `UNAVAILABLE`.
- **`AssistantLanguage`**: `AUTO_MIXED` (English/Hinglish/Banglish), `ENGLISH` (`en-IN`), `HINGLISH` (`hi-IN` / Latin-Hindi hybrid), `BANGLISH` (`bn-IN` / Latin-Bengali hybrid), `BENGALI` (`bn-BD` / `bn-IN`).
- **`PersonalityStyle`**: `FRIENDLY`, `PROFESSIONAL`, `CONCISE`, `PLAYFUL` — dynamically shapes both local responses and Gemini system instructions.
- **Room Entities (`ZiaDatabase`)**:
  - `MemoryFactEntity`: `id`, `key`, `value`, `category`, `updatedAt` (blocks sensitive keys like passwords/OTPs).
  - `ChatMessageEntity`: `id`, `sender` (`USER` / `ZIA`), `text`, `languageTag`, `sourceEngine` (`LOCAL_COMMAND`, `LOCAL_ASSISTANT`, `GEMINI_CLOUD`), `codeSnippet`, `timestamp`.
  - `ReminderEntity`: `id`, `title`, `triggerTimeMillis`, `isCompleted`, `createdAt`.
- **`PendingAction`**: Sealed/structured representation of device actions (`OpenApp`, `DialNumber`, `ComposeSms`, `SetSystemAlarm`, `SetVolume`, `OpenSystemPanel`, `LaunchCamera`, `OpenSafPicker`, `MediaControl`) requiring user confirmation when sensitive.

### Interactive Component & State Mapping
- **Home & Push-to-Talk Dock**:
  - Tapping the Microphone button checks `RECORD_AUDIO` permission via `rememberLauncherForActivityResult`, transitions `AvatarState` to `LISTENING`, starts `SpeechInput` with the user's selected locale (`en-IN`, `hi-IN`, `bn-IN`), streams partial recognition text live on-screen, and automatically routes the final transcript through the Hybrid Orchestrator.
  - If `SpeechRecognizer` is unavailable on the device, ZIA immediately shows a clear Hinglish/English fallback banner and focuses the text input bar.
- **Confirmation Preview Modal / Card**:
  - Displays whenever `PendingAction.requiresConfirmation == true` (e.g., calling a number, sending an SMS, setting an alarm, or clearing data). User can edit parameters or tap **Confirm & Launch** or **Cancel**.
- **Memory Vault & Privacy Controls**:
  - Users can add custom preferences (e.g., *"Call me Rahul"*, *"Preferred language: Hinglish"*), edit existing rows, delete individual items, or toggle off memory storage entirely. `PrivacyGuard` rejects entries matching credit card, password, token, or OTP patterns with an educational warning.
- **Notification Reader Service (`ZiaNotificationListenerService`)**:
  - Declared in `AndroidManifest.xml` with `BIND_NOTIFICATION_LISTENER_SERVICE`. Strictly opt-in: only caches the latest notification summary in memory when the user enables "Read Notifications Aloud" in Settings and grants notification access via `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`. Includes a one-tap **Read Latest Notification** and **Revoke Access** shortcut. Never uploads notification text to any server.
