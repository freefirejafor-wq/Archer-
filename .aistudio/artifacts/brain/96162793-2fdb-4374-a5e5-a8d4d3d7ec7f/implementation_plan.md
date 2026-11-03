# ARCHER AI — Emotion-Aware Voice & Mobile Action Assistant

ARCHER AI হলো একটি আধুনিক, আবেগ ও পরিস্থিতি বোঝা ভয়েস AI অ্যাসিস্ট্যান্ট (Emotion-Aware Voice & Task Assistant), যা ব্যবহারকারীর কণ্ঠস্বর ও কথার ধরন বুঝে বাংলা ও ইংরেজি উভয় ভাষায় কথা বলে, প্রতিদিনের রুটিন ও জরুরি মিটিংয়ের আগাম অ্যালার্ট দেয় এবং ভয়েস কমান্ডের মাধ্যমে মোবাইলের বিভিন্ন কাজ (যেমন—গ্যালারি থেকে টিকটকে ভিডিও আপলোড/শেয়ার, ক্যামেরা ওপেন, অ্যালার্ম সেট, কল বা মেসেজ পাঠানো এবং যেকোনো অ্যাপ চালু করা) সম্পন্ন করে।

### User Review & Critical Decisions

> [!IMPORTANT]
> আপনার দেওয়া উত্তরগুলোর ওপর ভিত্তি করে অ্যাপটির মূল সিদ্ধান্তগুলো নিচে চূড়ান্ত করা হয়েছে। প্ল্যানটি রিভিউ করে **Proceed** বাটনে ক্লিক করলে আমরা অ্যাপটি তৈরি শুরু করব।

- **Confirmed Decision 1 (অতিরিক্ত ফিচারসমূহ)**: স্মার্ট মেমোরি ভল্ট (Smart Memory Vault), ডেইলি ব্রিফিং ও আজকের খবরের হেডলাইনস (Daily Briefing & Headlines), ইমোশন ও মুড ট্র্যাকার + সোল পার্সোনালিটি মোড (Soul & Mood Engine), ফোকাস টাইমার ও স্ট্রিক ট্র্যাকার (Focus & Streak Tracker), এবং ভয়েস নোট ও মিটিং সামারি (Voice Notes & Meeting Summary) — এই সবগুলো ফিচারই অ্যাপে যুক্ত থাকবে।
- **Confirmed Decision 2 (মোবাইল অ্যাকশন ও ফুল ফোন কন্ট্রোল)**: ভয়েস বা চ্যাট কমান্ডের মাধ্যমে ক্যামেরা চালু করা (ছবি/ভিডিও তোলা), গ্যালারি থেকে ভিডিও বেছে সরাসরি টিকটক বা সোশ্যাল মিডিয়ায় আপলোড/শেয়ার করা, অ্যালার্ম ও টাইমার সেট করা, ক্যালেন্ডার মিটিং তৈরি করা, ফোন কল ও মেসেজ পাঠানো এবং ফোনের যেকোনো ইনস্টল করা অ্যাপ ওপেন করার ডিপ অ্যাকশন ইঞ্জিন যুক্ত থাকবে।
- **Confirmed Decision 3 (ডিজাইন ও ভাষা)**: আপনার পাঠানো স্ক্রিনশটের (ARCHER AI) মতো নিওন ডার্ক HUD ডিজাইন, অ্যানিমেটেড থ্রিডি পার্টিকেল অরব (3D Particle Voice Orb), সার্কিট কানেকশন নোড (`MEMORY`, `CHAT`, `SOUL`, `SETTINGS`/`ACTIONS`) এবং বাংলা ও ইংরেজি উভয় ভাষায় রিয়েল-টাইম ভয়েস ও ট্রান্সক্রিপ্ট সাপোর্ট থাকবে।

---

### 1. Overview & Core Concept

- **What It Does**:
  - **ইমোশন ও পরিস্থিতি বিশ্লেষণ (Emotion & Context-Aware AI)**: ব্যবহারকারীর কথার ভাবার্থ ও পরিস্থিতি (আনন্দিত, চিন্তিত, ব্যস্ত বা ক্লান্ত) বিশ্লেষণ করে সহানুভূতিশীল, উৎসাহব্যঞ্জক বা প্রফেশনাল টোনে কথা বলে।
  - **লাইভ ভয়েস কথোপকথন ও ট্রান্সক্রিপ্ট (Bilingual Live Voice & Transcript)**: বাংলা ও ইংরেজিতে সরাসরি কথা বলা যায় (`SpeechRecognizer` ও `TextToSpeech` + Gemini AI এর মাধ্যমে) এবং স্ক্রিনের উপরে লাইভ ট্রান্সক্রিপ্ট বক্সে কথোপকথন দেখা যায়।
  - **স্মার্ট মোবাইল অ্যাকশন কমান্ডার (Mobile Action Dispatcher)**: ভয়েস কমান্ড দিলে AI স্বয়ংক্রিয়ভাবে ইন্টেন্ট (Intent) শনাক্ত করে ক্যামেরা চালু করা, গ্যালারি থেকে ভিডিও সিলেক্ট করে TikTok বা অন্য প্ল্যাটফর্মে শেয়ার/আপলোড করা, অ্যালার্ম/টাইমার সেট করা, ফোন কল, এসএমএস/হোয়াটসঅ্যাপ মেসেজ এবং অ্যাপ লঞ্চের কাজ করে দেয়।
  - **রুটিন, টাস্ক ও মিটিং প্রোঅ্যাক্টিভ অ্যালার্ট (Proactive Routine & Meeting Alerts)**: প্রতিদিনের রুটিন, বাকি থাকা টাস্ক, স্ট্রিক কাউন্টার এবং জরুরি মিটিং শুরু হওয়ার ১৫–৩০ মিনিট আগেই ভয়েস ও নোটিফিকেশন অ্যালার্ট দেয়।
- **Target Audience / Persona**: যারা প্রতিদিনের ব্যস্ত রুটিন, মিটিং ও মোবাইলের কাজগুলো ভয়েস কমান্ড এবং একজন বুদ্ধিমান ও বন্ধুভাবাপন্ন ডিজিটাল সহকারীর মাধ্যমে দ্রুত পরিচালনা করতে চান।
- **Key Value**: সাধারণ চ্যাটবটের বদলে এটি একজন জীবন্ত ডিজিটাল কম্প্যানিয়নের মতো কাজ করে—যা আপনার মুড বোঝে, আপনার ব্যক্তিগত তথ্য মনে রাখে (`Memory Vault`), এবং মুখের কথায় ফোনের ভেতরের কাজগুলো সহজে করে দেয়।

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **সাইন-ইন ও পার্সোনালাইজেশন (Sign-In & Onboarding)**: Google Sign-In এর মাধ্যমে নিরাপদে লগইন করার পর সরাসরি মেইন **ARCHER AI HUD Dashboard**-এ প্রবেশ।
  2. **মেইন ড্যাশবোর্ড ও ভয়েস ইন্টারঅ্যাকশন (HUD & 3D Particle Orb)**:
     - উপরে স্ট্যাটাস বার (`ARCHER AI`, ইমোশন/মুড ব্যাজ, এবং কুইক অ্যাকশন বাটন)।
     - নিচে নিওন বর্ডারযুক্ত **TRANSCRIPT** প্যানেল—যেখানে বাম পাশে `ARCHER`-এর বাংলা/ইংরেজি উত্তর এবং ডান পাশে `YOU` (ব্যবহারকারীর কথা) রিয়েল-টাইমে ভেসে ওঠে।
     - মাঝখানে বাম পাশে সার্কিট-লাইনের মাধ্যমে যুক্ত ৪টি ইন্টারঅ্যাক্টিভ নোড (`MEMORY`, `CHAT`, `SOUL`, `SETTINGS`) এবং ডান পাশে ঘূর্ণায়মান **3D Particle Voice Orb** (যা কথা বলা বা শোনার সময় অডিও ফ্রিকোয়েন্সি ও মুড অনুযায়ী রঙ ও গতি পরিবর্তন করে), সাথে `LISTEN / STOP` ও স্পিকার টগল বাটন।
     - নিচের অংশে বাম পাশে **TODAY HEADLINES & BRIEFING** (আজকের আবহাওয়া, ব্রিফিং ও খবরের হেডলাইন) এবং ডান পাশে **TODAY TASKS & STREAK** কার্ড (প্রগ্রেস বার, ফায়ার স্ট্রিক আইকন ও চেকবক্সসহ টাস্ক ও জরুরি মিটিং লিস্ট)।
  3. **মোবাইল অ্যাকশন ফ্লো (Voice-to-Phone Execution)**: ব্যবহারকারী যখন বলেন *"টিকটকে ভিডিও আপলোড করো"* বা *"ক্যামেরা ওপেন করো"* বা *"সকাল ৭টায় অ্যালার্ম দাও"*, তখন AI সাথে সাথে কথা বলে নিশ্চিত করে এবং সংশ্লিষ্ট Android সিস্টেম অ্যাকশন বা মিডিয়া পিকার/শেয়ার শিট চালু করে।
  4. **মেমোরি, চ্যাট, সোল ও মিটিং হাব (Node Sub-Screens / Modals)**:
     - **MEMORY**: ব্যবহারকারীর জরুরি তথ্য, নোট, ও ভয়েস মেমো সংরক্ষণ এবং সার্চ করা।
     - **CHAT**: পূর্ণাঙ্গ মাল্টি-টার্ন চ্যাট হিস্ট্রি, ভয়েস নোট সামারি এবং কুইক ফোন অ্যাকশন শর্টকাট বার (TikTok Upload, Camera, Gallery, Call, Alarm, App Launcher)।
     - **SOUL**: ইমোশন/মুড ট্র্যাকার চার্ট, AI-এর পার্সোনালিটি মোড (Empathetic Friend, Productivity Coach, Calm Mentor, Cyber Tactical) এবং ফোকাস পোমোডোরো টাইমার।
     - **SETTINGS & ALERTS**: ভাষা নির্বাচন (বাংলা / English / Auto), ভয়েস পিচ/স্পিড এবং মিটিংয়ের আগাম অ্যালার্ট টাইমিং (১০/১৫/৩০ মিনিট আগে) কনফিগারেশন।

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: **Cyber-Organic Neon HUD** (স্ক্রিনশটের ARCHER AI-এর অনুপ্রেরণায় ডার্ক ম্যাট ব্যাকগ্রাউন্ড, গ্লোয়িং নিওন বর্ডার, সার্কিট ট্রেস কানেক্টর এবং থ্রিডি পার্টিকেল অরব)।
  - *Color Palette & Mood*:
    - `--background`: Deep Obsidian Carbon (`#060D0A` থেকে `#0B1612` রেডিয়াল গ্র্যাডিয়েন্ট)
    - `--surface`: Translucent Cyber Glass (`#11221C` with subtle neon border glow)
    - `--primary`: Electric Matrix Green (`#00FF7F` / `#10B981`) — মেইন বর্ডার, ট্রান্সক্রিপ্ট ফ্রেম ও অ্যাক্টিভ ইন্ডিকেটর
    - `--secondary`: Quantum Cyan (`#00E5FF`) — MEMORY নোড ও পার্টিকেল অরব কোর
    - `--accent-warm`: Solar Amber / Orange (`#FF8A00`) — CHAT নোড ও স্ট্রিক ফায়ার ব্যাজ
    - `--accent-danger`: Crimson Neon (`#FF2E54`) — STOP বাটন ও জরুরি মিটিং অ্যালার্ট
  - *Typography & Hierarchy*:
    - Display & HUD Headers: **Orbitron** / **Rajdhani** (স্ক্রিনশটের মতো ট্র্যাকড আপারকেস HUD হেডার: `ARCHER AI`, `TRANSCRIPT`, `TODAY HEADLINES`, `TODAY TASKS`)
    - Body & Bengali/English Text: **Plus Jakarta Sans** / সিস্টেম বাংলা ফন্টের সাথে অপ্টিমাইজড লাইন-হাইট যাতে বাংলা ও ইংরেজি উভয় টেক্সটই ঝকঝকে ও স্পষ্টভাবে পড়া যায়।

- **Interactive Feedback & Motion**:
  - পার্টিকেল অরবটি `Canvas`-এ ত্রিমাত্রিক গোলক (3D spherical projection + orbital rings) হিসেবে রেন্ডার হবে, যা `IDLE`, `LISTENING`, `THINKING`, এবং `SPEAKING` অবস্থায় ভিন্ন ভিন্ন তরঙ্গ ও রঙে স্পন্দিত হবে।
  - বাম পাশের ৪টি নোড (`MEMORY`, `CHAT`, `SOUL`, `SETTINGS`) থেকে অরব পর্যন্ত উজ্জ্বল সার্কিট লাইনের ভেতর দিয়ে অ্যানিমেটেড এনার্জি পালস প্রবাহিত হবে।

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Structured AI Intent Parsing for Real Mobile Actions**
  - *Chosen Approach*: **Firebase AI Logic (`gemini-3.8-flash`)** এর সাথে স্ট্রাকচার্ড JSON স্কিমা ব্যবহার করা হবে, যাতে ব্যবহারকারীর যেকোনো বাংলা বা ইংরেজি কথা থেকে একইসাথে (১) আবেগপূর্ণ ভয়েস রিপ্লাই, (২) শনাক্তকৃত ইমোশন/মুড, এবং (৩) এক্সিকিউটেবল মোবাইল অ্যাকশন কমান্ড (`OPEN_CAMERA`, `UPLOAD_TIKTOK_VIDEO`, `OPEN_GALLERY`, `SET_ALARM`, `SET_TIMER`, `MAKE_CALL`, `SEND_MESSAGE`, `OPEN_APP`, `ADD_TASK`, `ADD_MEETING`, `SAVE_MEMORY`) পাওয়া যায়।
  - *Why*: এতে ব্যবহারকারী যেভাবেই কথা বলুন না কেন ("টিকটকে একটা ভিডিও আপলোড করতে চাই" বা "আমাকে কাল সকাল ১০টার মিটিং মনে করিয়ে দিও"), অ্যাসিস্ট্যান্ট কথা বলার পাশাপাশি সাথে সাথে ফোনের সেই কাজটি ট্রিগার করতে পারবে।
- **Decision 2: Cloud Persistence + Real-Time Sync with Firebase Firestore**
  - *Chosen Approach*: ব্যবহারকারীর সব টাস্ক, রুটিন, মিটিং রিমাইন্ডার, মেমোরি ভল্ট নোট, মুড লগ এবং স্ট্রিক ডেটা **Firebase Firestore** (Google Sign-In সহ) এ সংরক্ষিত থাকবে।
  - *Why*: ফোন পরিবর্তন বা অ্যাপ পুনরায় ইনস্টল করলেও আপনার মেমোরি ভল্ট, রুটিন ও টাস্ক কখনোই হারাবে না।
- **Decision 3: Safe & Compliant Android System Action Execution**
  - *Chosen Approach*: গ্যালারি থেকে ভিডিও বেছে TikTok বা অন্য অ্যাপে আপলোডের জন্য Android-এর অফিসিয়াল Photo Picker (`PickVisualMedia`) এবং Package-targeted Share Intent (`com.zhiliaoapp.musically` / `com.ss.android.ugc.trill` বা সিস্টেম শেয়ার শিট) ব্যবহার করা হবে; ক্যামেরা, অ্যালার্ম (`AlarmClock`), ক্যালেন্ডার ও কলের জন্য স্ট্যান্ডার্ড Android Intents ও রানটাইম পারমিশন হ্যান্ডলার ব্যবহার করা হবে।
  - *Why*: এটি Google Play পলিসি সম্পূর্ণ মেনে চলে এবং রুট অ্যাক্সেস ছাড়াই নিরাপদে ফোনের গ্যালারি, ক্যামেরা, টিকটক শেয়ারিং, অ্যালার্ম ও অ্যাপ কন্ট্রোল নিশ্চিত করে।

---

### 4. Technical Architecture & Data Strategy

- **Architecture & Component Diagram**:

```
┌────────────────────────────────────────────────────────────────────────────┐
│                        ARCHER AI — MainActivity (HUD)                      │
│  ┌──────────────────────────────────────────────────────────────────────┐  │
│  │ Top Bar: Brand Header + Emotion/Mood Pill + Proactive Alert Banner   │  │
│  ├──────────────────────────────────────────────────────────────────────┤  │
│  │ Neon Transcript Frame: ARCHER (AI Response)  <--->  YOU (Live Input) │  │
│  ├───────────────────────────────┬──────────────────────────────────────┤  │
│  │ Circuit Nodes (Left Column)   │ 3D Particle Voice Orb (Right Canvas) │  │
│  │  (o) MEMORY ──┐               │    * Spherical 3D Particles + Rings  │  │
│  │  (o) CHAT   ──┼══>[Energy]═══>│    * State: IDLE/LISTEN/THINK/SPEAK  │  │
│  │  (o) SOUL   ──┤               │    * Controls: [LISTEN/STOP] [MUTE]  │  │
│  │  (o) CONFIG ──┘               │    * Quick Device Action Bar         │  │
│  ├───────────────────────────────┼──────────────────────────────────────┤  │
│  │ TODAY HEADLINES & BRIEFING    │ TODAY TASKS, ROUTINES & STREAK       │  │
│  │  * AI Daily Briefing & News   │  * Progress Bar + Streak Counter     │  │
│  │  * Weather & Meeting Summary  │  * Interactive Task & Meeting List   │  │
│  └───────────────────────────────┴──────────────────────────────────────┘  │
└──────────────────┬───────────────────────────────────────┬─────────────────┘
                   │                                       │
                   ▼                                       ▼
┌─────────────────────────────────────────┐ ┌────────────────────────────────┐
│      ArcherViewModel (State Holder)     │ │  MobileActionExecutor (Intent) │
│  * Voice Engine (STT SpeechRecognizer   │ │  * TikTok / Video Share Picker │
│    + TTS TextToSpeech Bilingual)        │ │  * Camera Photo/Video Capture  │
│  * Proactive Meeting Alert Scheduler    │ │  * Gallery Media Viewer        │
│  * Focus Timer & Mood/Soul State        │ │  * Alarm, Timer, Calendar,     │
│  * AI Command & Emotion Orchestrator    │ │    Phone Call, SMS, App Launch │
└──────────┬──────────────────────┬───────┘ └────────────────────────────────┘
           │                      │
           ▼                      ▼
┌─────────────────────────┐ ┌────────────────────────────────────────────────┐
│   Firebase AI Logic     │ │     ArcherRepository (Firebase Firestore)      │
│  * Model: gemini-3.8    │ │  * /users/{uid}/tasks     (Tasks & Routines)   │
│  * Emotion Detection    │ │  * /users/{uid}/meetings  (Meetings & Alerts)  │
│  * Structured JSON      │ │  * /users/{uid}/memories  (Smart Memory Vault) │
│    Action & Reply       │ │  * /users/{uid}/moods     (Emotion & Soul Log) │
│  * Daily News/Briefing  │ │  * /users/{uid}/notes     (Voice Notes/Summary)│
└─────────────────────────┘ └────────────────────────────────────────────────┘
```

- **Data Model & State**:
  - `TaskItem`: টাস্কের নাম, ক্যাটাগরি (Routine / Urgent / Work), নির্ধারিত সময়, সম্পন্ন হওয়ার স্ট্যাটাস (`isCompleted`), এবং স্ট্রিক পয়েন্ট।
  - `MeetingAlert`: মিটিংয়ের শিরোনাম, শুরুর সময়, জরুরি মাত্রা (`isUrgent`), আগেভাগে অ্যালার্ট দেওয়ার সময় (`remindBeforeMinutes`), এবং মিটিং সামারি।
  - `MemoryEntry`: ব্যবহারকারীর ব্যক্তিগত তথ্য বা নোট, ট্যাগ (`Personal`, `Preference`, `Important`, `VoiceNote`), এবং তৈরির সময়।
  - `MoodLog`: শনাক্তকৃত আবেগ (`Happy`, `Focused`, `Stressed`, `Tired`, `Calm`), AI-এর সোল মোড এবং সংক্ষিপ্ত নোট।
  - `AssistantResponseSchema`: Gemini AI থেকে প্রাপ্ত স্ট্রাকচার্ড রেসপন্স (`spokenReply`, `detectedEmotion`, `actionType`, `actionTarget`, `actionPayload`, `taskTitle`, `memoryFact`)।

- **Interactive Component & State Mapping**:
  - **Voice Orb & Stop/Listen Button**: ট্যাপ করলে মাইক্রোফোন পারমিশন চেক করে বাংলা/ইংরেজি ভয়েস ইনপুট নেওয়া শুরু করে (`LISTENING`), কথা শেষ হলে Gemini AI-তে পাঠায় (`THINKING`), এবং উত্তর পাওয়ার সাথে সাথে `TextToSpeech` দিয়ে কথা বলে (`SPEAKING`) ও কোনো মোবাইল অ্যাকশন থাকলে তা রান করে।
  - **Circuit Nodes (`MEMORY`, `CHAT`, `SOUL`, `SETTINGS`)**: যেকোনো নোডে ট্যাপ করলে নিওন গ্লোসহ একটি ফুল-ফিচার্ড বটম শিট বা সাব-স্ক্রিন ওপেন হয় (এবং ব্যাক বাটনে আবার মেইন HUD-এ ফিরে আসে)।
  - **Mobile Action Triggers (Voice + Quick Bar)**: ব্যবহারকারী ভয়েস দিয়ে বললে অথবা কুইক অ্যাকশন কার্ডে (`Upload to TikTok`, `Open Camera`, `Open Gallery`, `Set Alarm`, `Call / Message`, `Launch App`) ট্যাপ করলে সাথে সাথে সংশ্লিষ্ট সিস্টেম পিকার বা ইন্টেন্ট চালু হয়।
  - **Proactive Alert Engine**: অ্যাপ চালু থাকা অবস্থায় এবং ব্যাকগ্রাউন্ড রিমাইন্ডার চেকের মাধ্যমে আসন্ন মিটিং বা জরুরি রুটিন কাজের সময় ঘনিয়ে এলে স্ক্রিনের উপরে নিওন অ্যালার্ট ব্যানার ও ভয়েস রিমাইন্ডার দেয়।
