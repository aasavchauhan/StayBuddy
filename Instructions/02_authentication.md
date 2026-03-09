# StayBuddy — Phase 2: Authentication Module

## Screens in This Phase

| Screen | Features |
|--------|----------|
| **Splash** | Logo animation, session check, routing |
| **Onboarding** | 3-slide pager, skip/get started |
| **Login** | Email/password, Google, Phone OTP |
| **Register** | Full form with role selection |

---

## Data Model: `User.kt`

```kotlin
data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "", // "student" or "owner"
    val gender: String = "",
    val city: String = "",
    val college: String = "",
    val profileImage: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
```

**Firestore path**: `users/{userId}`

---

## Auth Flow

```
App Launch
    ↓
Splash Screen (2 sec)
    ↓
[Check DataStore: first launch?]
    ├── Yes → Onboarding (3 slides) → Login
    └── No → [Check Firebase Auth: user logged in?]
              ├── Yes → Home Screen
              └── No → Login Screen
```

---

## Splash Screen Implementation

1. Display app logo with scale + fade animation (1.5s)
2. Check `isFirstLaunch` from DataStore Preferences
3. Check `FirebaseAuth.currentUser`
4. Navigate accordingly with a slight delay for animation

---

## Onboarding Implementation

- Use `HorizontalPager` from Compose Foundation
- 3 pages with illustration, title, and subtitle
- Dot indicator below pager
- "Skip" text button → Login
- "Get Started" button on last page → Login
- Save `onboarding_completed = true` to DataStore

---

## Login Screen

- **Email + Password** → `signInWithEmailAndPassword()`
- **Google Sign-In** → One Tap → `signInWithCredential(GoogleAuthProvider)`
- **Phone OTP** → Navigate to Phone Auth screen

### Validation Rules
- Email: valid format
- Password: minimum 6 characters

---

## Register Screen

- **Fields**: Name, Email, Phone, Password, Role, Gender, City, College
- **Role Selection**: Segmented button (Student / PG Owner)
- **On Submit**:
  1. `createUserWithEmailAndPassword(email, password)`
  2. Create `User` document in Firestore `users` collection
  3. Navigate to Home

### Validation Rules
- Name: non-empty, min 2 chars
- Email: valid format
- Phone: 10-digit Indian number
- Password: min 6 chars, at least 1 number
- Role: must select one

---

## Firebase Auth Service

Key methods to implement:

| Method | Description |
|--------|-------------|
| `signUpWithEmail(email, password)` | Create new account |
| `signInWithEmail(email, password)` | Login with credentials |
| `signInWithGoogle(idToken)` | Google credential auth |
| `sendOtp(phoneNumber)` | Send SMS verification |
| `verifyOtp(verificationId, code)` | Verify SMS code |
| `getCurrentUser()` | Get current Firebase user |
| `signOut()` | Logout |

---

## Next Step
Proceed to **Phase 3: Home & Listing Screens** → `03_student_flow.md`
