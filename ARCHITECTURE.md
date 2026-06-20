# Android Autonomous AI Assistant — Architecture & Execution Plan

> **Author**: Principal Software Architect
> **Date**: June 2026
> **Status**: Approved for implementation

---

## Table of Contents

1. [Vision](#1-vision)
2. [Core Capabilities](#2-core-capabilities)
3. [System Architecture](#3-system-architecture)
4. [Android Architecture](#4-android-architecture)
5. [Agent Architecture](#5-agent-architecture)
6. [Memory Architecture](#6-memory-architecture)
7. [Tool System](#7-tool-system)
8. [Android Integrations](#8-android-integrations)
9. [Security Architecture](#9-security-architecture)
10. [Development Roadmap](#10-development-roadmap)
11. [MVP Definition](#11-mvp-definition)
12. [Future Vision](#12-future-vision)
13. [Key Architectural Decisions](#13-key-architectural-decisions)
14. [Hidden Complexities](#14-hidden-complexities)

---

## 1. Vision

### What the Product Is

An **Android AI Operating Companion** — a persistent, context-aware, memory-driven AI agent that lives as a foreground service on the device. It is not a chat app, not a macro recorder, not a voice assistant. It is an intelligent operating layer that understands the user, the device state, and active applications, and can reason, plan, and act on the user's behalf.

### What Problems It Solves

| Problem | Solution |
|---------|----------|
| Android has no persistent intelligent agent | Always-on foreground service with AI agent loop |
| Task-switching between apps is manual | Cross-app automation with planning and execution |
| No persistent user memory on device | Multi-tier memory architecture (episodic + semantic) |
| No unified interface for device control | Single natural language entry point for all device actions |
| Automation tools (Tasker, Macrodroid) require programming | Natural language → automated workflows |

### Primary User Journeys

**Journey 1: Conversational Assistant**
User types "what's on my calendar tomorrow?" → Agent retrieves from Calendar + memory → responds naturally.

**Journey 2: Device Control**
User says "open WhatsApp and message Mom that I'm on my way" → Agent opens app, navigates UI, types, sends. Human confirms before sending.

**Journey 3: Memory & Context**
User says "remind me about the project idea I had yesterday" → Agent retrieves from semantic memory + shows context.

**Journey 4: Multi-Step Task**
User says "every morning at 8 AM, check weather, read top 3 news headlines, and show my schedule for the day" → Agent creates workflow, schedules it, executes daily.

**Journey 5: Autonomous Assistance**
Agent notices user always opens Spotify after connecting Bluetooth headphones → Proactively asks "I notice a pattern — should I auto-play your focus playlist when headphones connect?"

---

## 2. Core Capabilities

### Tier 1 — MVP (Weeks 1-8)

```
┌────────────────────────────────────────────────────┐
│  Conversation │ Memory │ Context │ Basic Tools     │
│  ┌──────────┐ ┌──────┐ ┌───────┐ ┌──────────────┐ │
│  │Natural   │ │Short │ │Device │ │Read          │ │
│  │Language  │ │-term │ │State  │ │Notifications │ │
│  │Input     │ │Mem   │ │       │ │              │ │
│  ├──────────┤ ├──────┤ ├───────┤ │Open Apps     │ │
│  │Streaming │ │Conv  │ │Time   │ │              │ │
│  │Responses │ │Hist  │ │Battery│ │Get Device    │ │
│  │          │ │      │ │Network│ │Info          │ │
│  └──────────┘ └──────┘ └───────┘ └──────────────┘ │
└────────────────────────────────────────────────────┘
```

### Tier 2 — Functional Beta (Weeks 9-22)

```
┌─────────────────────────────────────────────────────────┐
│  Planner │ Memory │ RAG │ App Control                 │
│  ┌──────┐ ┌──────┐ ┌───┐ ┌──────────────────────────┐│
│  │Multi-│ │Episo-│ │Vec│ │UI Tree Reading           ││
│  │Step  │ │dic   │ │tor│ │                          ││
│  │Plan  │ │Mem   │ │DB │ │Click/Tap/Swipe Gestures  ││
│  ├──────┤ ├──────┤ ├───┤ │                          ││
│  │Task  │ │Summa-│ │Se-│ │Notification Actions      ││
│  │Deco  │ │rizat │ │man│ │                          ││
│  │mpose │ │ion   │ │tic│ │App Switch + Launch       ││
│  └──────┘ └──────┘ └───┘ └──────────────────────────┘│
└─────────────────────────────────────────────────────────┘
```

### Tier 3 — Advanced Autonomous (Weeks 23-48+)

```
┌─────────────────────────────────────────────────────────────┐
│  Reasoning │ Workflows │ Proactive │ Multi-Provider        │
│  ┌────────┐ ┌─────────┐ ┌─────────┐ ┌──────────────────┐  │
│  │Complex │ │Saved    │ │Pattern  │ │OpenAI / Claude   │  │
│  │Reason  │ │Workflows│ │Detection│ │                  │  │
│  │ing     │ │         │ │         │ │OpenRouter        │  │
│  ├────────┤ ├─────────┤ ├─────────┤ │                  │  │
│  │Self-   │ │Scheduled│ │Behavior │ │Local Models      │  │
│  │Critique│ │Tasks    │ │al Adapt │ │(LiteRT-LM)       │  │
│  └────────┘ └─────────┘ └─────────┘ └──────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. System Architecture

### Big Picture

```
                    ┌─────────────────────────────────────┐
                    │         User (Input / Output)        │
                    │  Text │ Voice │ Notifications │ UI  │
                    └───────────────────┬─────────────────┘
                                        │
┌───────────────────────────────────────────────────────────┐
│                     Android OS Layer                      │
│  ┌─────────────┐ ┌──────────────┐ ┌──────────────────┐  │
│  │Accessibility│ │Notification  │ │MediaProjection   │  │
│  │Service      │ │Listener      │ │(Screen Capture)  │  │
│  └──────┬──────┘ └──────┬───────┘ └────────┬─────────┘  │
│         │               │                   │            │
└─────────┼───────────────┼───────────────────┼────────────┘
          │               │                   │
┌─────────▼───────────────▼───────────────────▼────────────┐
│              Agent Core (Foreground Service)              │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │              Agent Engine                         │   │
│  │  ┌──────────┐ ┌──────────┐ ┌───────┐ ┌───────┐ │   │
│  │  │Orchestra │ │Context   │ │Planner│ │Safety │ │   │
│  │  │tor       │ │Assembler │ │       │ │Gate   │ │   │
│  │  └──────────┘ └──────────┘ └───────┘ └───────┘ │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  ┌──────────────────┐  ┌──────────────────────────────┐ │
│  │  Memory Manager   │  │  Tool System                 │ │
│  │  ┌────┐ ┌──────┐ │  │  ┌──────┐ ┌──────┐ ┌─────┐ │ │
│  │  │STM │ │ LTM  │ │  │  │Regist│ │Execu │ │Perm │ │ │
│  │  ├────┤ ├──────┤ │  │  │ry    │ │tor   │ │Check│ │ │
│  │  │Epi-│ │Seman │ │  │  └──────┘ └──────┘ └─────┘ │ │
│  │  │sode│ │tic   │ │  └──────────────────────────────┘ │
│  │  └────┘ └──────┘ │                                   │
│  └──────────────────┘                                   │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                   LLM Provider Layer                     │
│  ┌──────────────┐ ┌──────────────┐ ┌────────────────┐  │
│  │  Gemini      │ │  OpenAI      │ │  Local         │  │
│  │  Provider    │ │  Provider    │ │  Provider      │  │
│  │  (Phase 1)   │ │  (Phase 3)   │ │  (Phase 4)     │  │
│  └──────┬───────┘ └──────┬───────┘ └───────┬────────┘  │
│         │                │                  │           │
│  ┌──────▼────────────────▼──────────────────▼────────┐  │
│  │           LLM Provider Interface                  │  │
│  │  sendMessage() │ streamMessage() │ functionCall() │  │
│  └───────────────────────────────────────────────────┘  │
└──────────────────────────┬──────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────┐
│                    Data Layer                            │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐ │
│  │  Room DB     │ │  DataStore   │ │  Vector Store    │ │
│  │  (SQLite)    │ │  Preferences │ │  (sqlite-vec)    │ │
│  └──────────────┘ └──────────────┘ └──────────────────┘ │
└───────────────────────────────────────────────────────────┘
```

### Data Flow — Standard Interaction

```
User → [Input] → Orchestrator
  └→ ContextAssembler
       ├→ Conversation History (STM buffer)
       ├→ Semantic Memory (vector search)
       ├→ Device Context (battery, network, time, current app)
       └→ Screen State (if applicable)
  └→ Build Prompt → LLM Provider
  └→ Response Stream → User
  └→ Memory Manager → Update STM → Extract facts → Embed → Store in LTM
```

### Data Flow — Tool Execution

```
LLM returns FunctionCall
  └→ Safety Gate
       ├→ Checks permission level
       ├→ Checks if user approval required
       └→ Checks rate limits
  └→ Tool Registry → resolve tool by name
  └→ Tool Executor → execute with timeout
       ├→ Pre-execution hook (audit log, toast)
       ├→ Execute on Android (via AccessibilityService, intents, etc.)
       └→ Post-execution hook (audit result, verify outcome)
  └→ Observation → back to agent loop
```

---

## 4. Android Architecture

### Module Breakdown

```
android-assistant/
│
├── app/                          # Thin app entry point
│   ├── AssistantApplication.kt   # Application class, DI init
│   ├── MainActivity.kt           # Launcher activity
│   └── di/                       # App-level DI modules
│
├── core/                         # Shared foundation modules
│   ├── model/                    # Pure domain entities (no Android deps)
│   ├── common/                   # Extensions, constants, utilities
│   ├── network/                  # OkHttp, API client abstractions
│   └── testing/                  # Test helpers, mocks
│
├── data/                         # Data layer implementations
│   ├── local/                    # Room DB, DataStore, DAOs
│   ├── remote/                   # API implementations
│   ├── vector/                   # Vector store (sqlite-vec)
│   └── repository/               # Repository implementations
│
├── domain/                       # Use cases (business logic)
│   ├── conversation/             # Send message, stream response
│   ├── memory/                   # Save memory, query memory
│   ├── tool/                     # Execute tool, check permissions
│   └── auth/                     # API key management
│
├── agent/                        # Agent engine (core intellectual property)
│   ├── engine/                   # Agent loop, orchestrator
│   ├── llm/                      # LLM provider interface + Gemini impl
│   ├── context/                  # Context assembly, prompt building
│   ├── memory/                   # Memory manager
│   └── planning/                 # Planner, task decomposition
│
├── tool/                         # Tool system
│   ├── registry/                 # Tool registry, definition DSL
│   ├── system/                   # Device info, settings, clipboard
│   ├── app/                      # Open app, UI interaction
│   ├── automation/               # Workflow execution
│   └── shell/                    # Termux integration (Phase 3+)
│
├── android/                      # Android integration services
│   ├── accessibility/            # AccessibilityService
│   ├── notification/             # NotificationListenerService
│   ├── media/                    # MediaProjection, screenshot
│   ├── foreground/               # ForegroundService
│   └── overlay/                  # OverlayService (chat bubble)
│
├── ui/                           # User interface
│   ├── chat/                     # Chat screen + composables
│   ├── settings/                 # Settings screens
│   ├── permissions/              # Permission management UI
│   ├── memory/                   # Memory browser
│   └── components/               # Shared UI components (theme, etc.)
│
└── build-logic/                  # Convention plugins (not buildSrc)
    └── src/main/kotlin/
        ├── AndroidAppConventionPlugin.kt
        ├── AndroidLibConventionPlugin.kt
        ├── AndroidFeatureConventionPlugin.kt
        └── AndroidHiltConventionPlugin.kt
```

### Clean Architecture Layers

```
┌────────────────────────────────────────────────┐
│                   UI Layer                      │
│  ViewModels │ Composable Screens │ State       │
│  Observes State from Domain                     │
├────────────────────────────────────────────────┤
│                 Domain Layer                    │
│  UseCases │ Repository Interfaces │ Entities    │
│  Pure Kotlin — No Android dependencies          │
├────────────────────────────────────────────────┤
│                 Data Layer                      │
│  Repository Impls │ DataSources │ DTOs │ DB     │
│  Implements domain interfaces                  │
├────────────────────────────────────────────────┤
│                 Android Layer                   │
│  Services │ BroadcastReceivers │ Platform API   │
│  Bridges between Android OS and Domain          │
└────────────────────────────────────────────────┘
```

### MVVM Structure

Each feature follows:
```
feature/chat/
├── ChatViewModel.kt        # State management, use-case orchestration
├── ChatScreen.kt           # Composable UI
├── ChatUiState.kt          # Sealed class for UI states
├── ChatUiEvent.kt          # User interaction events
└── ChatNavigation.kt       # Navigation routes
```

### Dependency Injection

**Choice: Koin** over Hilt

Rationale:
- Hilt adds compilation overhead and tight coupling to Dagger's annotation processor
- Koin is simpler, faster to iterate with, and works well with AI-generated code
- Kotlin-first, no annotation processing (no KSP/KAPT dependency)
- Easier to understand for someone new to DI

```kotlin
// core/di/CoreModule.kt
val coreModule = module {
    single { OkHttpClient.Builder().build() }
    single { Json { ignoreUnknownKeys = true } }
}

// data/di/DataModule.kt
val dataModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().conversationDao() }
    single { MemoryRepository(get(), get()) }
}

// agent/di/AgentModule.kt
val agentModule = module {
    single<LLMProvider> { GeminiProvider(get(), get()) }
    single { AgentOrchestrator(get(), get(), get(), get()) }
    single { MemoryManager(get(), get()) }
}
```

### Storage Strategy

```
┌─────────────────────────────────────────────────────┐
│              Storage Strategy                        │
│                                                     │
│  Preferences: DataStore (not SharedPreferences)     │
│  ├─ API keys (encrypted with EncryptedSharedPrefs)  │
│  ├─ User preferences                                │
│  └─ Agent configuration                             │
│                                                     │
│  Structured Data: Room DB (SQLite)                  │
│  ├─ Conversations (id, session_id, role, content,   │
│  │                  timestamp, tokens_used)          │
│  ├─ Episodic Memory (id, summary, entities,         │
│  │                  timestamp, importance)           │
│  ├─ Tool Execution Log (id, tool, args, result,     │
│  │                     timestamp, status)            │
│  └─ Workflows (id, name, steps_json, schedule,      │
│                 enabled, created_at)                 │
│                                                     │
│  Vector Data: sqlite-vec extension (loaded via NDK) │
│  ├─ Semantic Memory (id, content, embedding BLOB,   │
│  │                  metadata JSON, timestamp)        │
│  └─ Embedding: 768-dim float32 vectors              │
│                                                     │
│  Files: Android File System                         │
│  ├─ Screenshots (temp, auto-cleaned)                │
│  ├─ Task checkpoints (JSON)                         │
│  └─ Export/Backup archives                          │
│                                                     │
│  Encryption: Android Keystore + Tink                │
│  ├─ API keys encrypted at rest                      │
│  ├─ Conversation content TBD (privacy toggle)       │
│  └─ Backup encryption with user-provided passphrase │
└─────────────────────────────────────────────────────┘
```

---

## 5. Agent Architecture

### Agent Loop (Modified ReAct)

```
┌────────────────────────────────────────────────────────────┐
│                  AGENT LOOP (ITERATIVE)                     │
│                                                            │
│  1. RECEIVE INPUT                                          │
│     ├─ User message (text/voice)                            │
│     ├─ System event (notification, alarm, connectivity)     │
│     └─ Scheduled task (workflow trigger)                    │
│                                                            │
│  2. ASSEMBLE CONTEXT                                       │
│     ├─ System Prompt (agent identity, rules, constraints)   │
│     ├─ Conversation History (last N messages, sliding       │
│     │  window, token-budgeted)                              │
│     ├─ Retrieved Memories (top-K semantically similar)      │
│     ├─ Device Context (battery, network, time, locale,      │
│     │  running apps, active notifications, ringer mode)     │
│     ├─ Screen State (if accessibility enabled: current      │
│     │  app, UI tree summary, focused element)               │
│     └─ Active Task State (if continuing a previous plan:    │
│        remaining steps, partial results, errors)            │
│                                                            │
│  3. LLM REASONING                                           │
│     ├─ Send assembled context to LLM                         │
│     ├─ LLM returns: thought + action OR final response      │
│     └─ If "response" → goto step 7                          │
│                                                            │
│  4. SAFETY CHECK                                            │
│     ├─ Is tool in blocked list? → reject                    │
│     ├─ Are args safe? (no shell injection, valid params)    │
│     ├─ Does tool require user approval?                     │
│     │   ├─ Always-ask tools (send SMS, make call, delete)   │
│     │   ├─ Context-dependent (new contact, large action)    │
│     │   └─ Never-ask (read clipboard, check battery)        │
│     ├─ Rate limit check (same tool called N times?)         │
│     └─ User approval dialog (if needed) → wait for decision │
│                                                            │
│  5. EXECUTE TOOL                                            │
│     ├─ Create execution span (for observability)            │
│     ├─ Log pre-execution to audit trail                     │
│     ├─ Show status to user ("Assistant is opening Chrome")  │
│     ├─ Apply timeout (configurable per tool category)       │
│     ├─ Execute via Tool Executor                            │
│     └─ Capture result (success/error, data, duration)       │
│                                                            │
│  6. OBSERVE & UPDATE                                        │
│     ├─ Format tool result as observation                    │
│     ├─ Append to conversation buffer                        │
│     ├─ Check iteration limit (default: 10)                  │
│     │   ├─ Under limit → goto step 3 (next LLM cycle)      │
│     │   └─ Over limit → summarize, tell user "needs more   │
│     │      steps"                                           │
│     └─ Save checkpoint (if multi-step plan)                 │
│                                                            │
│  7. DELIVER RESPONSE                                        │
│     ├─ Stream response text to UI                           │
│     ├─ If voice mode: send to TTS                           │
│     ├─ Update conversation in short-term memory             │
│     └─ Extract important facts for long-term storage        │
│                                                            │
│  8. MEMORY CONSOLIDATION (post-response, async)             │
│     ├─ Extract entities, facts, preferences from exchange   │
│     ├─ Score importance (1-10)                              │
│     ├─ Generate embedding via embedding model               │
│     ├─ Store in semantic memory (if importance >= 6)        │
│     ├─ Archive episodic summary                             │
│     └─ Trigger consolidation (summarize old episodes)       │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### System Prompt Architecture

The system prompt is assembled dynamically from components:

```
[IDENTITY]
You are an Android AI assistant that controls this device.
You observe, reason, and act on behalf of the user.

[CAPABILITIES]
You have access to these tools: {dynamic tool list}
You can read the screen, interact with apps, manage notifications.

[CONSTRAINTS]
- Never execute destructive actions without explicit approval
- Always explain what you're about to do before doing it
- Respect user privacy — don't read sensitive content unless asked
- If a task requires multiple steps, explain the plan first
- Ask clarifying questions when intent is ambiguous

[RULES]
- max_iterations: 10
- max_tokens_per_response: 4096
- safety_level: {user's configured level}

[DEVICE CONTEXT]
Current time: {time}
Battery: {level}%
Network: {type}
Active app: {package name}
Screen: {on/off}
Notifications: {count}

[SCREEN STATE]
App: {current app}
UI Elements: {simplified tree}
Focused Element: {if any}
Last User Action: {if tracked}

[RECENT MEMORIES]
{top 5 most relevant semantic memories}

[CONVERSATION HISTORY]
{last N exchanges, token-budgeted}
```

### Planner Module (Phase 2+)

```kotlin
// Domain concept — not code
class Plan(
    val goal: String,
    val steps: List<Step>,
    val status: PlanStatus,
    val createdAt: Long,
    val checkpoints: List<Checkpoint>
)

class Step(
    val id: String,
    val description: String,
    val tool: String?,
    val args: Map<String, Any>?,
    val expectedOutcome: String?,
    val isCritical: Boolean
)

// Planner interface
interface Planner {
    suspend fun decomposeTask(task: String, context: AgentContext): Plan
    suspend fun adaptPlan(plan: Plan, failedStep: Step, error: String): Plan
    suspend fun validatePlan(plan: Plan): ValidationResult
}
```

**Strategy**: LLM generates plans. No hardcoded planner. The Planner module wraps the LLM with structured prompting and plan validation.

---

## 6. Memory Architecture

### Three-Tier Architecture

```
┌─────────────────────────────────────────────────────────────┐
│              SHORT-TERM MEMORY (In-Memory)                   │
│                                                              │
│  Lifetime: Session (until process death or explicit clear)   │
│  Storage:  LRU Cache with max 50 messages                   │
│  Content:  Current conversation, active task state           │
│  Eviction: Oldest messages dropped when buffer full          │
│  Purpose:  Immediate context for agent loop                  │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │ Session messages: [{role, content, tokens, time}]  │     │
│  │ Active task: {plan_id, current_step, state}        │     │
│  │ Pending approvals: [{tool, args, expires_at}]      │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│              EPISODIC MEMORY (Room DB)                       │
│                                                              │
│  Lifetime: Persistent until user deletes                     │
│  Storage:  Room DB table with indices                        │
│  Content:  Conversation summaries, tool executions, events   │
│  Retrieval: Time-based query, importance filter              │
│  Purpose:  Recall past interactions, audit trail             │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │ Table: episodes                                     │     │
│  │ ├─ id: String (PK)                                 │     │
│  │ ├─ session_id: String                               │     │
│  │ ├─ summary: String (LLM-generated)                  │     │
│  │ ├─ timestamp: Long                                  │     │
│  │ ├─ importance: Int (1-10)                           │     │
│  │ ├─ token_count: Int                                 │     │
│  │ ├─ tool_calls: JSON (list of tools used)            │     │
│  │ └─ entities: JSON (people, places, things mentioned)│     │
│  │                                                     │     │
│  │ Table: tool_executions                              │     │
│  │ ├─ id: String (PK)                                  │     │
│  │ ├─ session_id: String                               │     │
│  │ ├─ tool_name: String                                │     │
│  │ ├─ args: JSON                                       │     │
│  │ ├─ result: JSON                                     │     │
│  │ ├─ status: Enum (SUCCESS/ERROR/BLOCKED)            │     │
│  │ ├─ timestamp: Long                                  │     │
│  │ └─ duration_ms: Int                                 │     │
│  └────────────────────────────────────────────────────┘     │
│                                                              │
├─────────────────────────────────────────────────────────────┤
│              SEMANTIC MEMORY (Vector Store)                  │
│                                                              │
│  Lifetime: Persistent until user deletes or edits            │
│  Storage:  sqlite-vec extension (SQLite vector search)       │
│  Content:  Facts about user, preferences, learned patterns   │
│  Retrieval: Cosine similarity on 768-dim embeddings          │
│  Purpose:  Knowledge about user for personalized assistance  │
│                                                              │
│  ┌────────────────────────────────────────────────────┐     │
│  │ Table: semantic_memories                            │     │
│  │ ├─ id: String (PK)                                  │     │
│  │ ├─ content: String (natural language fact)          │     │
│  │ ├─ embedding: BLOB (768-dim float32 array)          │     │
│  │ ├─ metadata: JSON                                   │     │
│  │ │   ├─ category: "preference" / "fact" / "pattern" │     │
│  │ │   ├─ source: "explicit" / "inferred"              │     │
│  │ │   ├─ confidence: Float (0-1)                      │     │
│  │ │   └─ related_entities: [String]                   │     │
│  │ ├─ importance: Int (1-10)                           │     │
│  │ ├─ access_count: Int                                │     │
│  │ ├─ last_accessed: Long                              │     │
│  │ └─ created_at: Long                                 │     │
│  │                                                     │     │
│  │ virtual table: vec_memories USING vec0(             │     │
│  │   embedding float[768] distance_metric=cosine       │     │
│  │ )                                                   │     │
│  └────────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

### Retrieval Strategy

```
Query: User says "what was that restaurant Jane recommended?"

1. Embed query → get 768-dim vector
2. Vector search → top 10 semantic memories (cosine similarity > 0.75)
3. Time-based → recent 5 episodic summaries (last 24 hours)
4. Entity extraction → extract "Jane", "restaurant" → filter episodes mentioning these
5. Merge & deduplicate → remove duplicates by content hash
6. Score → rank by (similarity * 0.5 + recency * 0.3 + importance * 0.2)
7. Budget → truncate to fit context window budget (e.g., 2000 tokens for memories)
8. Format → natural language: "From your conversation 3 days ago: Jane recommended..."
```

### Consolidation Strategy (Runs Periodically)

```
1. Check short-term buffer → if > 50 messages, summarize oldest 10
2. Extract facts from summary using LLM
3. Score importance (1-10)
4. Embed and store if importance >= 6
5. Check for contradictions (same entity, conflicting facts)
6. Decay old memories (decrement access_count every 30 days)
7. Archive episodes older than 90 days (compress, store as single entry)
8. Don't run during active conversation
```

---

## 7. Tool System

### Architecture

```
┌────────────────────────────────────────────────────────────┐
│                    TOOL SYSTEM                              │
│                                                             │
│  ┌────────────────────────────────────────────────────┐   │
│  │              Tool Registry                          │   │
│  │                                                     │   │
│  │  register(tool: ToolDefinition) → ToolId            │   │
│  │  resolve(name: String) → ToolDefinition?            │   │
│  │  listByCategory(category: ToolCategory) → List      │   │
│  │  listAvailable(permissions: Set) → List             │   │
│  │  generateFunctionSchema() → List<FunctionDeclaration>│   │
│  └──────────────┬─────────────────────────────────────┘   │
│                  │                                         │
│  ┌──────────────▼─────────────────────────────────────┐   │
│  │           Tool Definition DSL                       │   │
│  │                                                     │   │
│  │  ToolDefinition(                                    │   │
│  │    name = "open_app",                               │   │
│  │    description = "Opens an app by package name",    │   │
│  │    category = ToolCategory.APP_CONTROL,             │   │
│  │    permissionLevel = PermissionLevel.NORMAL,        │   │
│  │    parameters = listOf(                             │   │
│  │      Param("package_name", ParamType.STRING, req)   │   │
│  │    ),                                               │   │
│  │    executor = ::executeOpenApp                      │   │
│  │  )                                                  │   │
│  └──────────────┬─────────────────────────────────────┘   │
│                  │                                         │
│  ┌──────────────▼─────────────────────────────────────┐   │
│  │           Safety Layer                              │   │
│  │                                                     │   │
│  │  ┌────────────┐ ┌──────────────┐ ┌────────────┐   │   │
│  │  │Permission  │ │Arg Validator │ │Rate        │   │   │
│  │  │Checker     │ │              │ │Limiter     │   │   │
│  │  └────────────┘ └──────────────┘ └────────────┘   │   │
│  └──────────────┬─────────────────────────────────────┘   │
│                  │                                         │
│  ┌──────────────▼─────────────────────────────────────┐   │
│  │           Tool Executor                             │   │
│  │                                                     │   │
│  │  1. Start execution span (logging, timing)          │   │
│  │  2. Pre-execution hook (toast, audit log)           │   │
│  │  3. Execute with timeout (per-category timeout)     │   │
│  │  4. Post-execution hook (verify, audit result)      │   │
│  │  5. Return ToolResult                               │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Tool Categories

| Category | Examples | Permission Level | Timeout |
|----------|----------|-----------------|---------|
| `INFORMATION` | get_battery, get_network, get_time, list_apps | NONE | 5s |
| `NOTIFICATION` | read_notifications, dismiss_notification, click_notification | NORMAL | 10s |
| `APP_CONTROL` | open_app, close_app, switch_app | NORMAL | 15s |
| `UI_INTERACTION` | click_element, type_text, swipe, scroll, go_back | NORMAL | 15s |
| `CLIPBOARD` | read_clipboard, write_clipboard | NORMAL | 5s |
| `CONTACTS` | read_contact, list_contacts | SENSITIVE | 10s |
| `MESSAGING` | send_sms, compose_email | CRITICAL | 15s |
| `SYSTEM` | set_volume, toggle_wifi, toggle_bluetooth, set_brightness | NORMAL | 10s |
| `SETTINGS` | open_setting, change_setting | SENSITIVE | 10s |
| `STORAGE` | read_file, save_file, list_directory | SENSITIVE | 15s |
| `AUTOMATION` | run_workflow, create_workflow, schedule_task | CRITICAL | 30s |
| `ADMIN` | install_app, uninstall_app, grant_permission | BLOCKED | — |

### Permission Levels

```kotlin
enum class PermissionLevel {
    NONE,       // No restrictions
    NORMAL,     // Logged, rate-limited
    SENSITIVE,  // Requires user approval per-use
    CRITICAL,   // Requires biometric confirmation
    BLOCKED     // Never allowed (safety)
}
```

### Permission Decision Matrix

| User's Safety Mode | NONE | NORMAL | SENSITIVE | CRITICAL |
|---|---|---|---|---|
| **Trusting** | Auto | Auto | Toast + Auto | Dialog |
| **Balanced** | Auto | Auto | Dialog | Biometric |
| **Cautious** | Auto | Dialog | Dialog | Biometric |
| **Lockdown** | Auto | Dialog | Biometric | Block |

### Error Handling

```
Error Types:
├── ToolNotFound       → Return error, propose alternatives
├── PermissionDenied   → Return user-friendly message, offer to guide user to grant
├── RateLimited        → Retry with exponential backoff
├── Timeout            → Return partial result if available, warn user
├── ExecutionFailed    → Return error details, retry with modified approach
├── SafetyBlocked      → Return "This action is blocked for safety"
└── TOCTOU             → Retry with fresh accessibility tree read

Retry Strategy:
├── Non-destructive: retry 2x with exponential backoff (1s, 3s)
├── Destructive: NEVER auto-retry, ask user
└── Screen-dependent: re-read accessibility tree, retry once
```

---

## 8. Android Integrations

### Strategy Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                Android Integration Services                   │
│                                                               │
│  ┌────────────────────────────────────────────────────┐      │
│  │  1. ACCESSIBILITY SERVICE                           │      │
│  │                                                     │      │
│  │  Purpose: Read screen content, perform UI actions   │      │
│  │  Lifecycle: System-bound, always-on when enabled    │      │
│  │  Setup: User enables in Settings > Accessibility     │      │
│  │                                                     │      │
│  │  Key Methods:                                       │      │
│  │  ├─ getRootInActiveWindow() → UI tree               │      │
│  │  ├─ findAccessibilityNodeInfosByText()              │      │
│  │  ├─ performAction(ACTION_CLICK)                     │      │
│  │  ├─ performGlobalAction(GLOBAL_ACTION_BACK)         │      │
│  │  ├─ dispatchGesture() for swipes/taps               │      │
│  │  └─ onAccessibilityEvent() for event streams        │      │
│  │                                                     │      │
│  │  Events We Listen For:                              │      │
│  │  ├─ TYPE_WINDOW_STATE_CHANGED (app switch)          │      │
│  │  ├─ TYPE_VIEW_CLICKED (interaction tracking)        │      │
│  │  └─ TYPE_NOTIFICATION_STATE_CHANGED                 │      │
│  └────────────────────────────────────────────────────┘      │
│                                                               │
│  ┌────────────────────────────────────────────────────┐      │
│  │  2. NOTIFICATION LISTENER SERVICE                   │      │
│  │                                                     │      │
│  │  Purpose: Read incoming notifications               │      │
│  │  Lifecycle: System-bound, always-on when enabled    │      │
│  │  Setup: User grants notification access in Settings │      │
│  │                                                     │      │
│  │  Integration:                                       │      │
│  │  ├─ onNotificationPosted() → parse → context        │      │
│  │  ├─ onNotificationRemoved() → cleanup               │      │
│  │  ├─ ActiveNotifications → getActiveNotifications()  │      │
│  │  └─ Data sent to ContextManager for device state    │      │
│  └────────────────────────────────────────────────────┘      │
│                                                               │
│  ┌────────────────────────────────────────────────────┐      │
│  │  3. MEDIA PROJECTION (Screen Capture)               │      │
│  │                                                     │      │
│  │  Purpose: Capture screenshots for vision analysis   │      │
│  │  Lifecycle: User must consent each session          │      │
│  │  Setup: createScreenCaptureIntent() → Activity      │      │
│  │                                                     │      │
│  │  Key Considerations:                                │      │
│  │  ├─ Android 14+: requires FGS type mediaProjection  │      │
│  │  ├─ Consent dialog shown on each start              │      │
│  │  ├─ VirtualDisplay → ImageReader → Bitmap           │      │
│  │  └─ Used only when UI tree is insufficient          │      │
│  └────────────────────────────────────────────────────┘      │
│                                                               │
│  ┌────────────────────────────────────────────────────┐      │
│  │  4. FOREGROUND SERVICE                              │      │
│  │                                                     │      │
│  │  Purpose: Keep agent process alive                  │      │
│  │  Lifecycle: Started on app launch, persistent       │      │
│  │                                                     │      │
│  │  Implementation:                                    │      │
│  │  ├─ type: specialUse (or dataSync for short ops)    │      │
│  │  ├─ Persistent notification (cannot be swiped away) │      │
│  │  ├─ Houses the AgentEngine and MemoryManager        │      │
│  │  └─ onDestroy() → save checkpoint                    │      │
│  └────────────────────────────────────────────────────┘      │
│                                                               │
│  ┌────────────────────────────────────────────────────┐      │
│  │  5. OVERLAY SERVICE (Chat Bubble)                   │      │
│  │                                                     │      │
│  │  Purpose: Quick access to assistant from any app    │      │
│  │  Lifecycle: Bound to FGS, shows on-demand           │      │
│  │  Permission: SYSTEM_ALERT_WINDOW                    │      │
│  │  Implementation: Chat head bubble (like Messenger)  │      │
│  └────────────────────────────────────────────────────┘      │
└──────────────────────────────────────────────────────────────┘
```

### Service Communication Architecture

```
AccessibilityService ───────────┐
                                │  Binder / Callback
NotificationListenerService ────┼──→ AgentEngine (in FGS)
                                │
MediaProjectionService ────────┘
                                │
OverlayService ←──→ User taps → AgentEngine
                                │
UI (Chat Activity) ←──→ ViewModel → UseCase → AgentEngine
```

**Key Design Decision**: AgentEngine lives in the Foreground Service process, NOT in the Activity. The chat UI is just one of many possible frontends.

---

## 9. Security Architecture

### Permission Model

```
┌──────────────────────────────────────────────────────────────┐
│                    PERMISSION HIERARCHY                       │
│                                                               │
│  Android Permissions (OS-granted)                             │
│  ├─ INTERNET                                                  │
│  ├─ FOREGROUND_SERVICE                                        │
│  ├─ POST_NOTIFICATIONS                                        │
│  ├─ BIND_ACCESSIBILITY_SERVICE (user in Settings)             │
│  ├─ BIND_NOTIFICATION_LISTENER_SERVICE (user in Settings)     │
│  └─ SYSTEM_ALERT_WINDOW (user grant)                          │
│                                                               │
│  App-Level Permissions (our system)                           │
│  ├─ Tool categories mapped to levels                          │
│  ├─ Per-tool granular control                                 │
│  └─ User safety mode (3 levels)                               │
│                                                               │
│  Runtime Approvals                                            │
│  ├─ Per-action dialog (tool name, args, explanation)          │
│  ├─ "Remember my choice" checkbox                             │
│  ├─ Biometric confirmation for CRITICAL actions               │
│  └─ Timeout: approval expires after 5 minutes                 │
└──────────────────────────────────────────────────────────────┘
```

### User Approval System

```kotlin
// Concept — approval flow
class ApprovalRequest(
    val id: String,
    val toolName: String,
    val description: String,
    val args: Map<String, Any>,
    val riskLevel: PermissionLevel,
    val contextExplanation: String,
    val expiresAt: Long,
    val screenshotBefore: String? // Optional screenshot for context
)

sealed class ApprovalResult {
    data class Approved(val persist: Boolean = false) : ApprovalResult()
    data class ApprovedWithModification(val modifiedArgs: Map<String, Any>) : ApprovalResult()
    data class Denied(val reason: String?) : ApprovalResult()
}
```

### Data Protection

```
┌───────────────────────────────────────────────────────────────┐
│                  DATA PROTECTION LAYERS                        │
│                                                                │
│  At Rest:                                                     │
│  ├─ API Keys → EncryptedSharedPreferences (AES-256 GCM)      │
│  ├─ Conversations → Room DB (optionally encrypted with       │
│  │  SQLCipher if user enables privacy mode)                   │
│  ├─ Semantic Memory → sqlite-vec with same DB encryption     │
│  └─ Screenshots → stored in app-private directory,           │
│     auto-deleted after 5 minutes                              │
│                                                                │
│  In Transit:                                                  │
│  ├─ All API calls over HTTPS (TLS 1.3)                       │
│  ├─ Certificate pinning for Gemini API endpoint              │
│  └─ No telemetry / analytics without explicit consent         │
│                                                                │
│  In Memory:                                                   │
│  ├─ Sensitive args cleared after tool execution              │
│  ├─ Password/pin content filtered before LLM submission      │
│  └─ No persistent logging of sensitive parameters            │
└───────────────────────────────────────────────────────────────┘
```

### Privacy Controls

```
User-Facing Privacy Features:
├─ Privacy Mode toggle → disables screen reading, notification reading
├─ Memory Browser → view all stored facts, delete individual entries
├─ "Forget this conversation" → delete session from memory
├─ Incognito Session → no memory storage, no logging
├─ Data Export → export all memories as JSON
├─ Data Deletion → wipe all stored data
└─ Audit Log → view every tool execution with timestamp and result
```

---

## 10. Development Roadmap

### Phase 0: Foundation (Weeks 1-3)

**Goal**: Set up the complete development infrastructure and project skeleton.

```
┌────────────────────────────────────────────────┐
│              PHASE 0: FOUNDATION                │
├────────────────────────────────────────────────┤
│ Features:                                      │
│ ├─ Gradle multi-module project structure        │
│ ├─ Version catalog (libs.versions.toml)         │
│ ├─ Build convention plugins (build-logic)       │
│ ├─ GitHub Actions CI (build debug APK on push)  │
│ ├─ Koin dependency injection setup              │
│ ├─ Room database with initial schema            │
│ ├─ Gemini API client (basic chat completion)    │
│ ├─ Core domain models (Message, Session, etc.)  │
│ ├─ DataStore for preferences                    │
│ └─ Basic Foreground Service skeleton            │
│                                                │
│ Dependencies: None                              │
│ Risks: Low — standard Android setup             │
│                                                │
│ Deliverables:                                  │
│ ├─ Buildable APK (does nothing useful yet)      │
│ ├─ CI pipeline passing                          │
│ └─ Database schema design doc                   │
└────────────────────────────────────────────────┘
```

### Phase 1: MVP Assistant (Weeks 4-8)

**Goal**: Working conversational assistant with memory and basic tools.

```
┌────────────────────────────────────────────────┐
│              PHASE 1: MVP ASSISTANT             │
├────────────────────────────────────────────────┤
│ Features:                                      │
│ ├─ Agent Engine with ReAct loop                │
│ ├─ Gemini provider (text + function calling)   │
│ ├─ Short-term memory (in-memory conversation   │
│ │  buffer with sliding window)                 │
│ ├─ Tool Registry + basic tool set:             │
│ │  ├─ get_device_info (battery, network, etc.)  │
│ │  ├─ read_notifications                        │
│ │  ├─ open_app (by package name)                │
│ │  ├─ get_current_time                          │
│ │  └─ set_alarm / timer                         │
│ ├─ Chat UI (Jetpack Compose)                   │
│ │  ├─ Message list with streaming              │
│ │  ├─ Input field                              │
│ │  └─ Typing indicator                         │
│ ├─ Settings screen                             │
│ │  ├─ API key configuration                    │
│ │  ├─ Safety mode selector                     │
│ │  └─ Permission status                        │
│ ├─ Safety Layer (basic)                        │
│ │  ├─ Permission level checks                  │
│ │  └─ User approval dialog for SENSITIVE+      │
│ ├─ Foreground Service with persistent notif    │
│ └─ Overlay bubble (minimal — just open chat)   │
│                                                │
│ Dependencies: Phase 0                          │
│ Risks:                                         │
│ ├─ Gemini function calling schema mismatch     │
│ ├─ Agent loop infinite recursion               │
│ ├─ Foreground service killed by OEM            │
│                                                │
│ Deliverables:                                  │
│ ├─ Working APK with chat interface             │
│ ├─ Agent can answer questions + perform tools  │
│ └─ User can configure settings                 │
└────────────────────────────────────────────────┘
```

### Phase 2: Memory & Context (Weeks 9-14)

**Goal**: Persistent memory across sessions with semantic retrieval.

```
┌────────────────────────────────────────────────┐
│            PHASE 2: MEMORY & CONTEXT            │
├────────────────────────────────────────────────┤
│ Features:                                      │
│ ├─ Episodic Memory (Room DB)                   │
│ │  ├─ Session summarization after N messages   │
│ │  ├─ Time-based retrieval                     │
│ │  └─ Importance scoring                       │
│ ├─ Semantic Memory (sqlite-vec)                │
│ │  ├─ Fact extraction from conversations       │
│ │  ├─ Embedding generation (Gemini embedding   │
│ │  │  or all-MiniLM via ONNX)                  │
│ │  ├─ Cosine similarity search                 │
│ │  └─ Metadata filtering                       │
│ ├─ Enhanced Context Assembler                  │
│ │  ├─ Silent memory retrieval in background    │
│ │  ├─ Context window budget management         │
│ │  └─ Priority-based token allocation          │
│ ├─ Memory Browser UI                           │
│ │  ├─ View stored facts                        │
│ │  ├─ Edit / delete individual memories        │
│ │  └─ Search memories                          │
│ ├─ Memory consolidation (periodic)             │
│ │  ├─ Fact extraction from short-term          │
│ │  ├─ Contradiction detection                  │
│ │  └─ Decay / archival strategy               │
│ └─ Device Context Provider                     │
│    ├─ Battery, network, time                   │
│    ├─ Running apps                             │
│    └─ Active notifications                     │
│                                                │
│ Dependencies: Phase 1                          │
│ Risks:                                         │
│ ├─ Embedding quality too low for good recall   │
│ ├─ Vector search performance on large dataset  │
│ ├─ Memory growth unbounded                     │
│                                                │
│ Deliverables:                                  │
│ ├─ Assistant remembers past conversations      │
│ ├─ "Remember that..." queries work             │
│ └─ User can browse / manage memories           │
└────────────────────────────────────────────────┘
```

### Phase 3: Android Control (Weeks 15-22)

**Goal**: Full device interaction through accessibility.

```
┌────────────────────────────────────────────────┐
│           PHASE 3: ANDROID CONTROL             │
├────────────────────────────────────────────────┤
│ Features:                                      │
│ ├─ AccessibilityService integration            │
│ │  ├─ UI tree reader (structured JSON output)  │
│ │  ├─ Element locator (by text, id, bounds)    │
│ │  ├─ Gesture dispatcher (tap, swipe, long     │
│ │  │  press, scroll)                           │
│ │  └─ Window change detection                  │
│ ├─ Tool expansion:                             │
│ │  ├─ click_element (by text/content-desc)     │
│ │  ├─ type_text (into focused element)         │
│ │  ├─ swipe / scroll (direction, distance)     │
│ │  ├─ go_back / go_home / recent_apps          │
│ │  └─ wait_for_element (poll with timeout)     │
│ ├─ Screen reading pipeline                     │
│ │  ├─ Accessibility tree → simplified JSON     │
│ │  ├─ Screenshot capture (MediaProjection)     │
│ │  └─ Vision analysis (optional, Phase 3+)    │
│ ├─ Notification actions                        │
│ │  ├─ Read notification content                │
│ │  ├─ Click notification action                │
│ │  └─ Dismiss notification                     │
│ ├─ App awareness                               │
│ │  ├─ Detect foreground app                    │
│ │  ├─ Track app switch events                  │
│ │  └─ Build app usage timeline                 │
│ └─ Enhanced Safety Layer                       │
│    ├─ TOCTOU detection (re-read UI before      │
│    │  acting)                                  │
│    ├─ App-specific rules (never automate       │
│    │  banking apps)                            │
│    └─ Screenshot before/after for audit        │
│                                                │
│ Dependencies: Phase 2                          │
│ Risks:                                         │
│ ├─ Accessibility tree incomplete on some apps  │
│ ├─ TOCTOU race conditions                      │
│ ├─ OEM customized UI breaks node finding       │
│ ├─ MediaProjection consent dialog UX friction  │
│                                                │
│ Deliverables:                                  │
│ ├─ Assistant can control device UI             │
│ ├─ Can open apps, tap buttons, fill text       │
│ └─ Can read and act on notifications           │
└────────────────────────────────────────────────┘
```

### Phase 4: Automation (Weeks 23-32)

**Goal**: Multi-step planning, workflows, and scheduled tasks.

```
┌────────────────────────────────────────────────┐
│            PHASE 4: AUTOMATION                  │
├────────────────────────────────────────────────┤
│ Features:                                      │
│ ├─ Multi-step planner                          │
│ │  ├─ Task decomposition (LLM + structured)    │
│ │  ├─ Step dependency graph                    │
│ │  ├─ Checkpoint / resume on failure           │
│ │  └─ Plan visualization in UI                 │
│ ├─ Workflow system                             │
│ │  ├─ Save plans as reusable workflows         │
│ │  ├─ Parameterized workflows                  │
│ │  ├─ Conditional steps (if/else branches)     │
│ │  └─ Workflow editor (basic UI)              │
│ ├─ Scheduled tasks (WorkManager)               │
│ │  ├─ One-time delay                           │
│ │  ├─ Recurring (daily, weekly, custom cron)   │
│ │  ├─ Condition-based (WiFi, battery, etc.)    │
│ │  └─ Task listing and management UI           │
│ ├─ Cross-app workflows                         │
│ │  ├─ Read email → create task → send message  │
│ │  ├─ Check calendar → set alarm → navigate    │
│ │  └─ State passing between steps              │
│ └─ Failure recovery                            │
│    ├─ Automatic retry with backoff             │
│    ├─ Step-level fallback (try alternative)    │
│    ├─ Partial result preservation              │
│    └─ User notification on persistent failure  │
│                                                │
│ Dependencies: Phase 3                          │
│ Risks:                                         │
│ ├─ Plans fail mid-execution                    │
│ ├─ Workflows become stale (app UI changes)     │
│ ├─ Scheduled tasks blocked by battery optim.   │
│                                                │
│ Deliverables:                                  │
│ ├─ Multi-step task execution                   │
│ ├─ Saved workflows with scheduling             │
│ └─ Recovery from partial failures              │
└────────────────────────────────────────────────┘
```

### Phase 5: Advanced Agent (Weeks 33-48+)

**Goal**: Multi-provider, proactive, adaptive assistant.

```
┌────────────────────────────────────────────────┐
│           PHASE 5: ADVANCED AGENT               │
├────────────────────────────────────────────────┤
│ Features:                                      │
│ ├─ Multi-provider LLM support                  │
│ │  ├─ OpenAI provider                          │
│ │  ├─ OpenRouter provider                      │
│ │  ├─ Provider auto-failover                   │
│ │  └─ Provider selection by task type          │
│ ├─ Local model support (optional)              │
│ │  ├─ LiteRT-LM integration                    │
│ │  ├─ Privacy mode → route to local model      │
│ │  └─ Hybrid routing (local simple/cloud complex│
│ ├─ Proactive assistance                        │
│ │  ├─ Pattern detection (user habits)          │
│ │  ├─ Contextual suggestions                   │
│ │  ├─ Timing (don't interrupt during meetings) │
│ │  └─ User feedback learning                   │
│ ├─ Behavioral adaptation                       │
│ │  ├─ Learns communication style               │
│ │  ├─ Adapts to user's schedule                │
│ │  ├─ Customizes tool usage patterns           │
│ │  └─ Personality adjustment                   │
│ ├─ Knowledge graph (optional)                  │
│ │  ├─ Entity extraction and linking            │
│ │  ├─ Relationship inference                   │
│ │  └─ Graph-based reasoning                    │
│ ├─ Sub-agent architecture (optional)           │
│ │  ├─ Specialist sub-agents                    │
│ │  ├─ Orchestrator delegates to sub-agents     │
│ │  └─ Shared memory across sub-agents          │
│ └─ MCP protocol support (optional)             │
│    ├─ MCP client for external tools            │
│    ├─ MCP server exposure                      │
│    └─ Third-party skill marketplace            │
│                                                │
│ Dependencies: Phase 4                          │
│ Risks:                                         │
│ ├─ Local model quality insufficient            │
│ ├─ Proactive suggestions annoy users           │
│ ├─ Behavioral adaptation requires significant  │
│ │  data collection                             │
│ ├─ Sub-agent coordination complexity           │
│                                                │
│ Deliverables:                                  │
│ ├─ Works with any LLM provider                 │
│ ├─ Proactively helps the user                  │
│ ├─ Learns and adapts over time                 │
│ └─ Extensible through MCP or skills            │
└────────────────────────────────────────────────┘
```

---

## 11. MVP Definition

### What's In

The MVP is Phase 1 (Weeks 4-8). It must demonstrate real value while being achievable by a solo developer in 2 months.

```
MVP SCOPE:
├── Core: Working conversational assistant
├── LLM: Gemini (function calling + streaming)
├── Agent: ReAct loop with 10-iteration limit
├── Memory: Session-only (short-term buffer)
├── Tools:
│   ├── get_device_info (battery, network, OS version)
│   ├── read_notifications (last N)
│   ├── open_app (by package name)
│   ├── get_current_time / date
│   └── set_timer / alarm
├── UI: Chat screen with streaming responses
├── Settings: API key, safety mode, permission status
├── Safety: Tool permission levels + approval dialogs
└── Background: Foreground service + persistent notification
```

### What's Out (Consciously Deferred)

```
DEFERRED FROM MVP:
├── Long-term memory (Phase 2)
├── Screen reading / accessibility control (Phase 3)
├── Multi-step planning (Phase 4)
├── Workflows and scheduling (Phase 4)
├── Multi-provider LLM support (Phase 5)
├── Proactive assistance (Phase 5)
├── Vision analysis (Phase 5)
├── Voice input / TTS
├── MCP protocol
└── Sub-agents
```

### MVP Success Criteria

```
1. User can send a message and get a natural language response
2. User can ask about device state (battery, time, network)
3. User can say "open Twitter" and the app opens
4. User can say "read my notifications" and get a summary
5. User can set a timer ("set a 5-minute timer called 'pasta'")
6. Agent explains what it's doing before executing tools
7. Agent asks for confirmation before sensitive actions
8. Agent stays alive in background (foreground service)
9. Streaming responses feel fast (<2s to first token)
10. App survives process death (reconnects, shows history)
```

---

## 12. Future Vision

### The Path from MVP to Autonomous Assistant

```
MVP (Month 2)
│
│  "What's my battery level?"
│  "Open Chrome"
│  "Read my notifications"
│
├── Adds Memory (Phase 2)
│
│  "Remember that I prefer dark mode"
│  "What did I ask you yesterday?"
│  Proactively: "You usually check weather at 8 AM. Want me to show it?"
│
├── Adds Android Control (Phase 3)
│
│  "Open WhatsApp, search for Jane, and type 'Dinner at 7?'"
│  "Turn on Bluetooth and connect to my car"  
│  "Scroll down and click the third result"
│
├── Adds Automation (Phase 4)
│
│  "Every morning at 7:30, read my schedule, check weather, and start music"
│  "When I connect headphones, open Spotify"
│  "If battery < 20%, enable battery saver"
│
├── Adds Advanced Agent (Phase 5)
│
│  Proactive: "You have a meeting in 5 min. I've opened Maps to the restaurant."
│  Adaptive: Learns your work hours, adjusts availability and notification style
│  Multi-model: Uses local model for privacy, cloud model for complex reasoning
│  Extensible: Install community skills via MCP
└──
```

### End State Vision (12-18 months)

The assistant should feel like a native part of Android — not an app you open, but an intelligence layer that's always present:

- **Summon**: Shake phone, long-press button, or say "Hey Assistant"
- **Presence**: Chat bubble that follows you across apps
- **Context**: Knows what app you're in, what you're looking at, what time it is
- **Memory**: Remembers everything important, forgets what you don't want stored
- **Proactivity**: Suggests useful actions without being annoying
- **Actions**: Can do anything you can do on your phone — navigate, type, tap, configure
- **Safety**: Never does something destructive without your explicit, informed consent
- **Extensibility**: Install community-created skills like app store
- **Privacy**: All data on-device, encrypted. Your choice whether to use cloud LLMs.
- **Recovery**: Survives crashes, reboots, and process death

---

## 13. Key Architectural Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| DI Framework | Koin | Simpler than Hilt for AI-generated code. No annotation processing. |
| Database | Room + sqlite-vec | Room is Android standard. sqlite-vec adds vector search without another dependency. |
| LLM Abstraction | Custom interface | Provider independence. Easy to add OpenAI, OpenRouter, local later. |
| Agent Pattern | Modified ReAct | Proven, simple to implement. Can evolve into Plan-and-Execute later. |
| Agent Process | Foreground Service | Only reliable way to stay alive on modern Android. Persistent notification required. |
| UI | Jetpack Compose | Modern, declarative. Works well with AI-generated code. |
| Build System | Gradle multi-module + convention plugins | Industry standard for Android. Separates concerns cleanly. |
| CI/CD | GitHub Actions | Free for public repos. Covers build, test, signing, and release. |
| API Security | User-provided key | No server-side key management. User controls their own API usage. |
| Memory Priority | Importance-based scoring | Prevents memory growth from consuming context window. |
| Tool Permission | 5-level system | NONE → NORMAL → SENSITIVE → CRITICAL → BLOCKED. Grows with capability. |
| Safety Default | Balanced mode | Sensible default that protects user without excessive friction. |

---

## 14. Hidden Complexities

These are the non-obvious challenges that will consume unexpected time:

1. **Android 15+ Foreground Service Limits** — 6-hour timeout for `dataSync` type. Need `specialUse` or `mediaPlayback` to work around. May need to restart service periodically.

2. **Accessibility Tree Fragmentation** — Every app renders differently. OEM skins use custom components. Some apps (banking, streaming) actively obfuscate their accessibility trees. The MVP should NOT promise universal app control.

3. **TOCTOU Race Conditions** — The UI tree is a snapshot. Between reading and acting, the UI can change. Every automated action needs verification (try, check, retry).

4. **LLM Hallucination in Tool Calls** — Gemini may call non-existent tools, pass invalid parameters, or imagine results. Every tool call must be validated against the actual registry.

5. **Context Window Budgeting** — The system prompt + conversation history + memories + device state + screen state must fit in the context window. Need a token budget allocator with priority tiers.

6. **Memory Growth is Unbounded** — Without consolidation and decay, the vector store grows forever. Storage is cheap but retrieval latency increases. Need background maintenance jobs.

7. **OEM-Specific Background Kills** — Xiaomi, Huawei, OPPO, OnePlus aggressively kill background services. No universal workaround. The architecture must assume it will be killed and design for resumability.

8. **Gemini API Rate Limits** — Free tier has strict limits. Need usage tracking, graceful degradation, and user visibility into quota consumption.

9. **The "Black Box" Problem** — Users need to understand why the assistant did something. Every action needs a clear, human-readable explanation stored in the audit log.

10. **Testing Without Emulator** — Can't run Android emulator on a phone. Must rely on GitHub Actions (Firebase Test Lab) and physical device testing. Debug cycles are slower.

---

> **Next Step**: Begin Phase 0 — setting up the Gradle multi-module project structure, version catalog, convention plugins, GitHub Actions CI, and the core data layer.
