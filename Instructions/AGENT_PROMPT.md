# StayBuddy — Agent Prompt

Use this as a prompt when starting a fresh conversation with an AI coding agent.

---

## Prompt to Copy-Paste

```
I have a StayBuddy Android app project that needs to be built from scratch.
The project was already created in Android Studio as an Empty Compose Activity
with package name `com.example.staybuddy`.

The `Instructions/` folder in the project root contains the complete specification:

- `00_fresh_start_guide.md` — Project overview, version matrix, folder structure, build order
- `01_project_setup.md` — Phase 1: Dependencies, Gradle config, AndroidManifest, Firebase setup
- `02_authentication.md` — Phase 2: Splash, Onboarding, Login, Register screens
- `03_student_flow.md` — Phase 3: Home, Search, MapView, ListingDetail, Favorites, Roommate screens
- `04_owner_flow.md` — Phase 4: AddListing wizard, OwnerDashboard
- `05_chat_and_remaining.md` — Phase 5+: Chat system, Profile, Navigation, Polish, FCM, Security

Please read ALL instruction files first before starting. Here are the critical rules:

## CRITICAL BUILD RULES

1. **DO NOT CHANGE the AGP, Gradle, or Kotlin versions** that Android Studio generated.
   Only ADD new plugins and dependencies.

2. **KSP version MUST match Kotlin version format**: e.g., Kotlin `2.1.10` → KSP `2.1.10-1.0.31`

3. **Hilt version must be compatible with AGP**: Check the version matrix in `00_fresh_start_guide.md`

4. **Use osmdroid (OpenStreetMap)** instead of Google Maps. No API key needed.
   Do NOT use `com.google.maps.android:maps-compose` or `play-services-maps`.

5. **Build after EVERY phase**. Run `./gradlew compileDebugKotlin` after finishing each phase.
   Fix ALL compilation errors before moving to the next phase.

6. **Use consistent field names** across all files. The canonical data model field names are:
   - `PgListing.listingId` (NOT `id`)
   - `PgListing.area` (NOT `address`)
   - `PgListing.roomType` (NOT `type`)
   - `PgListing.genderAllowed` (NOT `genderPreference`)
   - `ListingRepository.getListings()` (NOT `getAllListings()`)

7. **The `google-services.json` is already in `app/`**. Firebase project ID: `stay-buddy-9d294`

8. **Debug SHA-1**: `E7:4D:03:9C:60:07:CD:54:33:22:A3:41:C5:E2:D0:ED:05:A7:DF:63`

## IMPLEMENTATION ORDER

Start with Phase 1, build and verify, then Phase 2, build and verify, etc.
Do NOT skip ahead. Each phase depends on the previous one.

## ARCHITECTURE

- MVVM + Clean Architecture
- Jetpack Compose + Material 3
- Hilt for DI
- Firebase (Auth, Firestore, Storage, FCM)
- StateFlow for observable states
- osmdroid for maps
- Coil for image loading

Please start by reading the instruction files, then begin Phase 1.
```

---

## Tips for Better Results

1. **Create the project in Android Studio FIRST**, let it sync successfully, THEN open the conversation
2. **Copy `Instructions/` and `google-services.json`** before starting
3. If the agent tries to change AGP/Gradle/Kotlin versions, tell it to stop
4. After each phase, try building in Android Studio to catch issues early
5. If you get version errors, share the EXACT error message with the agent
