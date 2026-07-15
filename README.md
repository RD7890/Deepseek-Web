<div align="center">

<img src="https://cdn.deepseek.com/favicon.png" width="96" style="border-radius:20px" />

# Deepseek-Web

**A sleek Android WebView wrapper for [chat.deepseek.com](https://chat.deepseek.com)**  
Smart asset caching · Glassmorphism UI · Zero-bloat · Always-fresh AI responses

<br/>

[![Build](https://img.shields.io/github/actions/workflow/status/RD7890/Deepseek-Web/build.yml?branch=main&style=for-the-badge&logo=github-actions&logoColor=white&label=CI%2FCD&color=1a56db&labelColor=0f2c6b)](https://github.com/RD7890/Deepseek-Web/actions)
[![Release](https://img.shields.io/github/v/release/RD7890/Deepseek-Web?include_prereleases&style=for-the-badge&logo=github&color=1a56db&labelColor=0f2c6b)](https://github.com/RD7890/Deepseek-Web/releases/latest)
[![APK](https://img.shields.io/github/downloads/RD7890/Deepseek-Web/total?style=for-the-badge&logo=android&logoColor=white&color=1a56db&labelColor=0f2c6b&label=Downloads)](https://github.com/RD7890/Deepseek-Web/releases)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen?style=for-the-badge&logo=android&labelColor=0f2c6b&color=1a56db)](https://developer.android.com/about/versions/7.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white&labelColor=0f2c6b)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-BOM_2024.08-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white&labelColor=0f2c6b)](https://developer.android.com/jetpack/compose)

</div>

---

## 📲 Download

> **Latest release:** [`v1.0.1`](https://github.com/RD7890/Deepseek-Web/releases/latest)

| Platform | Link |
|----------|------|
| 🤖 **Android APK** | [Download from GitHub Releases](https://github.com/RD7890/Deepseek-Web/releases/latest) |
| 🔁 **Auto-updates** | Every push to `main` triggers a new signed release automatically |

**Install steps:**  
1. Download the `.apk` from Releases  
2. Enable _"Install from unknown sources"_ in your Android settings  
3. Install and open — no account, no setup needed  

---

## ✨ Features

| | Feature | Detail |
|---|---|---|
| 🧠 | **Smart Asset Cache** | CSS, images, icons & fonts cached locally via Room DB + in-memory index |
| ⚡ | **Zero-latency lookup** | `ConcurrentHashMap` index pre-loaded at startup — no disk I/O on hot path |
| 🚫 | **JS never cached** | JavaScript always fetched live so AI responses are never stale |
| 🗂️ | **Cache Manager UI** | View all cached assets by size, delete individually, or clear all at once |
| 🪟 | **Glassmorphism Nav** | Floating pill-shaped bottom bar with frosted glass — takes minimal screen space |
| 🎨 | **Blue Settings Theme** | Settings screen has a rich DeepSeek-blue UI; main chat stays clean & full-screen |
| ↩️ | **Back navigation** | Hardware/gesture back navigates WebView history before exiting |
| 🌐 | **External link handler** | Non-DeepSeek URLs open in the system browser automatically |
| 🌙 | **Dark / Light theme** | Follows system theme; dark mode uses deep navy surfaces |
| 📶 | **Offline-first static** | On slow connections, cached assets load instantly; only AI traffic hits the network |

---

## 🏗️ Architecture

```
com.rohan.deepseek
├── cache/
│   ├── CacheEntry.kt       — Room @Entity (url, fileName, mimeType, sizeBytes, cachedAt)
│   ├── CacheDao.kt         — Flow queries + suspend CRUD
│   ├── CacheDatabase.kt    — Singleton Room DB
│   └── CacheManager.kt     — Intercept layer: mem index → disk → network
├── viewmodel/
│   └── AppViewModel.kt     — StateFlows for cache list, size, count; refresh trigger
├── ui/
│   ├── navigation/
│   │   └── AppNavigation.kt — Floating glass nav + NavHost
│   ├── screens/
│   │   ├── HomeScreen.kt   — Full-screen WebView with progress bar
│   │   └── SettingsScreen.kt — Cache manager with blue theme
│   └── theme/
│       ├── Color.kt        — DeepSeek blue palette
│       ├── Theme.kt        — Material3 dark/light schemes
│       └── Type.kt         — Typography
└── MainActivity.kt
```

### Cache Intercept Flow

```
WebViewClient.shouldInterceptRequest(url)
        │
        ▼
  isCacheable(url)?  ── no ──► return null (browser handles it)
        │ yes
        ▼
  memIndex[url] hit? ── yes ──► return file.inputStream()  ← zero-latency
        │ no
        ▼
  HTTP fetch (CHROME_UA)
        │
        ▼
  write to cacheDir/deepseek_assets/<hash>.bin
  upsert Room + memIndex
        │
        ▼
  return bytes.inputStream()
```

**What gets cached:** `png · jpg · jpeg · gif · webp · svg · ico · woff · woff2 · ttf · otf · css`  
**What never gets cached:** `js` (always fresh — ensures live AI responses)

---

## 🔄 CI / CD Pipeline

Every push to `main` or `release/**`:

```
push to main
    │
    ├─ Restore committed keystore (signing/signing-key.jks)
    ├─ Auto-bump versionCode = run_number
    ├─ Auto-bump versionName = 1.0.<run_number>
    ├─ ./gradlew :app:assembleRelease
    ├─ Upload APK artifact (90-day retention)
    ├─ Create GitHub Release with signed APK
    └─ Telegram notification (if secrets configured)
```

[![View latest workflow run](https://img.shields.io/badge/View_Build_Logs-GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)](https://github.com/RD7890/Deepseek-Web/actions)

---

## 🛠️ Build Locally

**Requirements:** JDK 17+, Android SDK (API 24+)

```bash
git clone https://github.com/RD7890/Deepseek-Web.git
cd Deepseek-Web
./gradlew :app:assembleRelease
# APK → app/build/outputs/apk/release/
```

The committed keystore at `signing/signing-key.jks` is used automatically.  
Alias: `deepseek` · Password: `deepseek123`

---

## 📦 Tech Stack

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | 2.0.0 |
| UI | Jetpack Compose BOM | 2024.08.00 |
| Navigation | Navigation Compose | 2.7.7 |
| Persistence | Room + KSP | 2.6.1 |
| Build | Android Gradle Plugin | 8.5.2 |
| Min SDK | Android 7.0 | API 24 |
| Target SDK | Android 14 | API 34 |

---

## 📄 License

```
MIT License — free to use, modify, and distribute.
```

---

<div align="center">

Made with ❤️ · Powered by [DeepSeek](https://deepseek.com)

<img src="https://cdn.deepseek.com/favicon.png" width="32" />

</div>
