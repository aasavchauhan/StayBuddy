# StayBuddy — Phase 3: Student Flow Screens

## Screens in This Phase

| Screen | Purpose |
|--------|---------|
| **Home** | Landing page with recommendations & search |
| **Search** | Filter + list/map toggle |
| **Map View** | osmdroid map with PG markers |
| **Listing Detail** | Full PG info with actions |
| **Favorites** | Saved PG bookmarks |
| **Roommate List/Post** | Browse & post roommate requests |

---

## Home Screen

### Layout Structure
```
┌──────────────────────────────┐
│  📍 Bangalore ▼   [🔔]      │  ← Location selector + notifications
├──────────────────────────────┤
│  🔍 Search PGs, hostels...   │  ← Search bar → navigates to Search
├──────────────────────────────┤
│  ▸ Recommended PGs           │  ← Horizontal LazyRow
│  [Card][Card][Card]→         │
├──────────────────────────────┤
│  ▸ Nearby PGs                │  ← Vertical LazyColumn
│  ┌──────────────────────┐    │
│  │ 🖼️ Title  ₹5000/mo  │    │
│  │ 📍 2.3 km  ⭐ 4.2    │    │
│  └──────────────────────┘    │
├──────────────────────────────┤
│  ▸ Find Roommates            │  ← Horizontal cards
│  [Card][Card]→               │
├──────────────────────────────┤
│ 🏠  🔍  ➕  💬  👤          │  ← Bottom Navigation
└──────────────────────────────┘
```

### Data Loading
- Fetch PG listings from Firestore, ordered by `created_at` desc
- "Nearby" uses device location + Firestore geoqueries
- Pull-to-refresh

---

## PG Listing Card Component

Reusable card for listing display across Home, Search, Favorites:

```kotlin
@Composable
fun PgListingCard(
    listing: PgListing,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit
)
```

Shows: Image (Coil), Title, Price (₹/month), Distance, Rating stars, Favorite heart icon

---

## Search Screen

### Filter Sheet (Bottom Sheet)
| Filter | UI Component | Firestore Query |
|--------|-------------|----------------|
| Price Range | Range slider (₹500–₹30000) | `price >= min, price <= max` |
| Room Type | Chips: Single, Double, Triple, Dorm | `room_type == selected` |
| Gender | Chips: Male, Female, Any | `gender_allowed == selected` |
| Amenities | Toggle chips: WiFi, AC, Food, etc. | `amenities arrayContains` |
| Distance | Slider (1km–10km) | Location-based filter |

### View Toggle
- **List View**: LazyColumn with `PgListingCard`
- **Map View**: Navigate to MapViewScreen

---

## Map View Screen

> **USES osmdroid (OpenStreetMap), NOT Google Maps**

- Create an `OsmMapView` Compose wrapper using `AndroidView` wrapping `MapView`
- Add markers for each PG listing using lat/lng
- On marker click → show `ModalBottomSheet` with:
  - Listing image
  - Price
  - Title
  - "View Details" button → navigate to ListingDetail
- Current location button (FAB)
- Request location permission via Accompanist

---

## Listing Detail Screen

### Sections (ScrollColumn)
1. **Image Carousel** — `HorizontalPager` with dots, swipeable
2. **Price & Badges** — ₹ price, room type chip, gender chip
3. **Distance** — "2.3 km from [College Name]"
4. **Amenity Grid** — Icon + label for each amenity (WiFi ✓, AC ✓, etc.)
5. **Description** — Multi-line text
6. **House Rules** — Bulleted list
7. **Location Map** — Embedded osmdroid map with marker
8. **Owner Info** — Avatar, name, "since [date]"
9. **Action Bar** — Chat | Call | Save (sticky bottom)

---

## Favorites Screen

- Query Firestore `favorites` collection where `user_id == currentUserId`
- Join with `pg_listings` to get full listing data
- Display as grid or list
- Swipe-to-delete gesture
- Empty state with illustration: "No saved listings yet"

---

## Roommate Screens

### Browse Roommates (`RoommateListScreen`)
- Query `roommate_posts` collection
- Filter by city, price, preferences
- Cards show: location, rent, beds available, preferences

### Post Roommate (`AddRoommatePostScreen`)
Fields: Location, Available beds, Monthly rent, Bills split, Preferences (chips)

---

## Next Step
Proceed to **Phase 4: Owner Flow** → `04_owner_flow.md`
