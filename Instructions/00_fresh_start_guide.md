# StayBuddy — Fresh Start Guide

## Overview

This guide helps you set up a brand-new StayBuddy project from scratch using Android Studio, with all the correct dependency versions and a clear workflow for an AI agent to follow.

---

## Step 1: Create the Project in Android Studio

1. Open Android Studio
2. **File → New → New Project**
3. Select **Empty Activity** (Compose-based)
4. Configure:
   - **Name**: `StayBuddy`
   - **Package name**: `com.example.staybuddy`
   - **Save location**: Choose your new directory
   - **Minimum SDK**: API 26 (Android 8.0)
   - **Build configuration language**: Kotlin DSL (build.gradle.kts)
5. Click **Finish** and wait for the project to sync

> **IMPORTANT**: Let Android Studio create the project first. This ensures the Gradle wrapper, SDK references, and JDK are all correctly configured for YOUR specific Android Studio installation.

---

## Step 2: Verify the Project Builds

Before doing anything else:

1. Click **Build → Rebuild Project** in Android Studio
2. Make sure it says **BUILD SUCCESSFUL**
3. If it fails, fix any Android Studio / SDK issues first

---

## Step 3: Copy These Files Into the New Project

Copy the **entire `Instructions/` folder** into your new project root:

```
NewStayBuddy/
├── Instructions/
│   ├── 00_fresh_start_guide.md    ← This file
│   ├── 01_project_setup.md        ← Phase 1 spec
│   ├── 02_authentication.md       ← Phase 2 spec
│   ├── 03_student_flow.md         ← Phase 3 spec
│   ├── 04_owner_flow.md           ← Phase 4 spec
│   ├── 05_chat_and_remaining.md   ← Phase 5+ spec
│   └── SHA1_fingerprint_guide.md  ← SHA-1 setup
├── app/
│   └── google-services.json       ← Copy from old project
├── build.gradle.kts
├── settings.gradle.kts
└── ...
```

Also copy `google-services.json` into the `app/` directory.

---

## Step 4: Dependency Version Matrix (Tested & Working)

> [!CAUTION]
> **DO NOT blindly upgrade versions.** The biggest issue in the previous attempt was version incompatibilities between AGP, Gradle, Kotlin, Hilt, and your JDK. Use the versions that Android Studio generates, then only add the libraries below.

### What Android Studio Will Auto-Generate (DON'T CHANGE THESE)

These are set by your Android Studio version and should NOT be manually changed:
- **Android Gradle Plugin (AGP)** — whatever Android Studio puts in `build.gradle.kts`
- **Gradle version** — whatever is in `gradle/wrapper/gradle-wrapper.properties`
- **Kotlin version** — whatever Android Studio selects

### Libraries to ADD (in `app/build.gradle.kts` dependencies block)

| Library | Version | Notes |
|---------|---------|-------|
| Compose BOM | `2024.12.01` | Platform BOM for Compose |
| Firebase BOM | `33.7.0` | Platform BOM for Firebase |
| Hilt | Match AGP compatibility | See note below |
| KSP | Must match Kotlin version | e.g., Kotlin 2.1.10 → KSP 2.1.10-1.0.31 |
| Navigation Compose | `2.8.5` | |
| Activity Compose | `1.9.3` | |
| Lifecycle | `2.8.7` | |
| Coil Compose | `2.7.0` | Image loading |
| osmdroid | `6.1.18` | Free OpenStreetMap (no API key) |
| Play Services Location | `21.3.0` | GPS location |
| Play Services Auth | `21.3.0` | Google Sign-In |
| DataStore | `1.1.1` | Preferences |
| Accompanist Permissions | `0.36.0` | Runtime permissions |
| Coroutines Android | `1.9.0` | |

### Hilt Version Compatibility

| AGP Version | Compatible Hilt | Compatible Gradle |
|-------------|-----------------|-------------------|
| 8.7.x | 2.51.1 | 8.9+ |
| 8.8.x | 2.55 | 8.10.2+ |
| 9.0.x | 2.59+ | 9.1+ (also remove `kotlin-android` plugin) |

> [!WARNING]
> If using AGP 9.0+, you must **remove** the `org.jetbrains.kotlin.android` plugin from both root and app `build.gradle.kts` because AGP 9.0 has built-in Kotlin support.

---

## Step 5: Plugins to Add (in root `build.gradle.kts`)

After Android Studio generates the project, add these plugins with `apply false`:

```kotlin
plugins {
    // These will already exist from Android Studio:
    id("com.android.application") version "X.X.X" apply false
    id("org.jetbrains.kotlin.android") version "X.X.X" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "X.X.X" apply false

    // ADD these:
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.dagger.hilt.android") version "2.55" apply false  // Match AGP!
    id("com.google.devtools.ksp") version "X.X.X-1.0.31" apply false  // Match Kotlin!
}
```

In `app/build.gradle.kts` plugins block, add:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    // ADD these:
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}
```

---

## Key Architecture Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| UI | Jetpack Compose + Material 3 | Modern, declarative |
| Architecture | MVVM + Clean Architecture | Separation of concerns |
| DI | Hilt | Official Android DI |
| Backend | Firebase (Auth, Firestore, Storage, FCM) | No custom server needed |
| Maps | osmdroid (OpenStreetMap) | Free, no API key required |
| Image Loading | Coil | Lightweight, Compose-native |
| Navigation | Navigation Compose | Type-safe routing |
| State | StateFlow + ViewModel | Lifecycle-aware |

---

## Firestore Database Schema

```
users/{userId}
  - name, email, phone, role, gender, city, college, profileImage, createdAt

pg_listings/{listingId}
  - ownerId, title, description, city, area, latitude, longitude
  - price, deposit, roomType, genderAllowed, amenities[], images[]
  - availableBeds, isActive, rating, createdAt

roommate_posts/{postId}
  - userId, city, location, priceShare, availableBeds, preferences{}, createdAt

chats/{chatId}
  - participants[], listingId, lastMessage, lastMessageTime
  chats/{chatId}/messages/{messageId}
    - senderId, text, timestamp, seen

favorites/{favId}
  - userId, listingId, createdAt
```

---

## App Folder Structure

```
app/src/main/java/com/example/staybuddy/
├── StayBuddyApp.kt                    # @HiltAndroidApp Application class
├── MainActivity.kt                     # Single Activity, setContent with NavGraph
├── data/
│   ├── model/                          # User, PgListing, RoommatePost, ChatRoom, Message
│   └── repository/                     # AuthRepository, ListingRepository, ChatRepository, etc.
├── di/
│   └── AppModule.kt                    # Hilt @Module providing Firebase instances
├── ui/
│   ├── theme/                          # Color, Type, Shape, Theme (Material 3)
│   ├── components/                     # PgListingCard, OsmMapView, etc.
│   ├── screens/
│   │   ├── splash/                     # SplashScreen + VM
│   │   ├── onboarding/                 # OnboardingScreen + VM
│   │   ├── auth/                       # Login, Register + VMs
│   │   ├── home/                       # HomeScreen + VM
│   │   ├── search/                     # SearchScreen + VM
│   │   ├── map/                        # MapViewScreen + VM
│   │   ├── listing/                    # ListingDetail + VM
│   │   ├── favorites/                  # FavoritesScreen + VM
│   │   ├── roommate/                   # RoommateList, AddRoommate + VMs
│   │   ├── owner/                      # AddListing, OwnerDashboard + VMs
│   │   ├── chat/                       # ChatList, Chat + VMs
│   │   └── profile/                    # ProfileScreen + VM
│   └── navigation/
│       ├── NavGraph.kt                 # All routes defined here
│       ├── Screen.kt                   # Sealed class for type-safe routes
│       └── BottomNavItem.kt            # Bottom nav configuration
└── utils/
    ├── Constants.kt
    └── ValidationUtils.kt
```

---

## Build Order (Phase by Phase)

1. **Phase 1**: Project scaffolding, Gradle deps, theming, data models, DI, navigation skeleton
2. **Phase 2**: Splash → Onboarding → Login → Register → Home → Profile
3. **Phase 3**: Search, MapView, ListingDetail, Favorites, Roommate screens
4. **Phase 4**: AddListing, OwnerDashboard
5. **Phase 5**: ChatList, ChatScreen
6. **Phase 6**: Animations, loading skeletons, polish
7. **Phase 7**: Firebase security rules
8. **Phase 8**: Testing & performance

> [!IMPORTANT]
> **Build after EVERY phase.** Run `Build → Rebuild Project` in Android Studio after finishing each phase. Fix compilation errors before moving to the next phase.

---

## Debug SHA-1 Fingerprint

Previously retrieved: `E7:4D:03:9C:60:07:CD:54:33:22:A3:41:C5:E2:D0:ED:05:A7:DF:63`

To get it again:
```bash
keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android
```

Register this in Firebase Console → Project Settings → Your Apps → Add/Update Fingerprint.

---

## Firebase Project

- **Project ID**: `stay-buddy-9d294`
- **Package**: `com.example.staybuddy`
- **Services**: Auth, Firestore, Storage, FCM, Analytics
