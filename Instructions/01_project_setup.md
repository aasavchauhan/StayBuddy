# StayBuddy — Phase 1: Project Setup & Configuration

## Prerequisites

| Prerequisite | Status | Action |
|---|---|---|
| Android Studio (Ladybug+) | Required | Install latest stable |
| JDK 17+ | Required | Bundled with Android Studio |
| Firebase CLI | Optional | `npm install -g firebase-tools` then `firebase login` |
| SHA-1 Fingerprint | Required | Register in Firebase Console for Phone Auth & Google Sign-In |

> **NOTE**: We use osmdroid (OpenStreetMap) for maps — NO Google Maps API key needed!

---

## Step 1: Create Android Project

Create a new **Empty Compose Activity** project in Android Studio:

- **Name**: StayBuddy
- **Package**: `com.example.staybuddy`
- **Min SDK**: API 26 (Android 8.0)
- **Build Config Language**: Kotlin DSL

---

## Step 2: Configure `google-services.json`

Copy the existing `google-services.json` into `app/`:

```
StayBuddy/
├── app/
│   ├── google-services.json  ← Place here
│   └── build.gradle.kts
└── build.gradle.kts
```

---

## Step 3: Gradle Dependencies

### `build.gradle.kts` (Project-level)

> **IMPORTANT**: Keep the AGP, Kotlin, and Gradle versions that Android Studio generated.
> Only ADD new plugins below the existing ones:

```kotlin
plugins {
    // Keep whatever Android Studio generated for these — DON'T CHANGE VERSIONS:
    id("com.android.application") version "X.X.X" apply false
    id("org.jetbrains.kotlin.android") version "X.X.X" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "X.X.X" apply false

    // ADD these:
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.dagger.hilt.android") version "2.55" apply false  // Check AGP compat!
    id("com.google.devtools.ksp") version "KOTLIN_VERSION-1.0.31" apply false  // Match Kotlin!
}
```

### `app/build.gradle.kts`

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.staybuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.staybuddy"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Activity & Navigation
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Maps (osmdroid — free OpenStreetMap, no API key needed)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Hilt DI
    implementation("com.google.dagger:hilt-android:2.55")
    ksp("com.google.dagger:hilt-compiler:2.55")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Accompanist
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // Core & AppCompat
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

---

## Step 4: AndroidManifest.xml Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

> No Google Maps API key needed — we use osmdroid (OpenStreetMap).

---

## Step 5: Firebase Console Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/project/stay-buddy-9d294)
2. Enable **Authentication** → Sign-in methods:
   - Email/Password ✅
   - Phone ✅ (add test numbers for dev)
   - Google ✅ (requires SHA-1)
3. Enable **Cloud Firestore** → Start in test mode (we'll add rules later)
4. Enable **Firebase Storage** → Start in test mode
5. Enable **Cloud Messaging** → Enabled by default
6. Register SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android
   ```
   Copy SHA-1 → Firebase Console → Project Settings → Android app → Add fingerprint

---

## Next Step
Once setup is complete, proceed to **Phase 2: Authentication Module** → `02_authentication.md`
