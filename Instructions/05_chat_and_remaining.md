# StayBuddy — Phase 5+: Chat, Profile, Navigation & Polish

---

## Chat System

### Architecture
- **Collection**: `chats/{chatId}` with subcollection `messages/{messageId}`
- Real-time updates via Firestore `snapshotFlow()`
- Chat created when student clicks "Chat" on a listing

### Chat List Screen
- Query chats where `participants` array contains current user
- Show: other user's name/avatar, last message preview, timestamp, unread count
- Tap → open Chat Screen

### Chat Conversation Screen
- Message bubbles: sent (right, primary color) / received (left, surface color)
- Group by date (Today, Yesterday, etc.)
- "Seen" indicator (double-check icon)
- Text input bar with send button
- Auto-scroll on new messages

### Firestore Document Structure
```
chats/{chatId}:
  participants: ["uid1", "uid2"]
  listingId: "abc123"
  lastMessage: "Is the room still available?"
  lastMessageTime: Timestamp

chats/{chatId}/messages/{messageId}:
  senderId: "uid1"
  text: "Is the room still available?"
  timestamp: Timestamp
  seen: false
```

---

## Profile Screen

- Circular profile image (tap to change)
- Name, Email, Role badge
- Menu items:
  | Item | Student | Owner |
  |------|---------|-------|
  | My Listings | ✗ | ✓ → Dashboard |
  | Saved Listings | ✓ → Favorites | ✗ |
  | Chats | ✓ | ✓ |
  | Settings | ✓ | ✓ |
  | Logout | ✓ | ✓ |

---

## Bottom Navigation

5 tabs with Material 3 `NavigationBar`:

| Tab | Icon | Route | Role |
|-----|------|-------|------|
| Home | 🏠 | `/home` | Both |
| Search | 🔍 | `/search` | Student |
| Add | ➕ | `/add-listing` | Owner: PG form / Student: Roommate form |
| Chats | 💬 | `/chats` | Both |
| Profile | 👤 | `/profile` | Both |

---

## Animations & Polish

1. **Page Transitions**: Compose Navigation `enterTransition` / `exitTransition` with slide + fade
2. **Loading Skeletons**: Shimmer composable for cards while data loads
3. **Micro-animations**: Heart icon pulse on favorite, card scale on press
4. **Pull-to-refresh**: On Home and Search screens
5. **Empty states**: Illustrated placeholders for no results, no favorites, no chats

---

## Firebase Cloud Messaging

### Notification Types
- New chat message
- New PG near saved location
- Roommate match alert

### Implementation
1. Extend `FirebaseMessagingService`
2. Store FCM token in `users/{userId}/fcmToken`
3. Create notification channels (Android 8+):
   - `messages` — Chat notifications
   - `listings` — New listing alerts
   - `roommates` — Roommate match alerts

---

## Security Rules Summary

| Collection | Read | Write |
|-----------|------|-------|
| `users` | Authenticated | Own doc only |
| `pg_listings` | Authenticated | Owner of listing |
| `roommate_posts` | Authenticated | Creator only |
| `chats` | Participant only | Participant only |
| `favorites` | Own only | Own only |

---

## Performance Checklist

- [ ] Coil image caching with disk + memory cache
- [ ] Firestore pagination (10 docs/page, cursor-based)
- [ ] DataStore for user preferences
- [ ] Minimize Firestore reads with `.limit()` and selective field queries
- [ ] ProGuard/R8 minification for release builds
- [ ] Baseline Profiles for Compose rendering performance
