# StayBuddy — Phase 4: Owner Flow Screens

## Screens in This Phase

| Screen | Purpose |
|--------|---------|
| **Add Listing** | Multi-step form to create PG listing |
| **Owner Dashboard** | Stats & listing management |

---

## Add Listing Screen (Multi-Step Wizard)

### Step 1: Basic Info
- Title (text field)
- Description (multi-line text field)
- City (dropdown / text field)
- Area (text field)

### Step 2: Pricing & Type
- Rent price (₹ number input)
- Deposit amount (₹ number input)
- Room type (Single / Double / Triple / Dorm)
- Gender allowed (Male / Female / Any)

### Step 3: Amenities
- Checkbox grid with icons:
  - WiFi, AC, Laundry, Food, Parking, TV, Geyser, Power Backup, Security, CCTV

### Step 4: Images
- Pick from gallery (use `ActivityResultContracts.GetMultipleContents`)
- Preview grid with remove option
- Minimum 3 images required
- Upload to Firebase Storage → get download URLs

### Step 5: Location
- Embedded osmdroid map with draggable marker
- Auto-detect current location option
- Extract lat/lng from marker position
- Show address text below map

### Submit
1. Validate all steps
2. Upload images to `Firebase Storage: pg_images/{listingId}/`
3. Create document in `pg_listings` collection
4. Navigate to Dashboard with success message

---

## Owner Dashboard

### Stats Section (Top)
```
┌─────────┐ ┌─────────┐ ┌─────────┐
│  Total  │ │ Active  │ │Messages │
│   12    │ │    8    │ │   5     │
└─────────┘ └─────────┘ └─────────┘
```

### Listing Management
- LazyColumn of owner's listings
- Each card shows: image, title, price, status badge (Active/Inactive)
- Swipe actions or overflow menu:
  - **Edit** → Pre-filled Add Listing form
  - **Toggle Active/Inactive** → Update `is_active` field
  - **Delete** → Confirmation dialog → Delete from Firestore + Storage

### FAB
- "Add New PG" → navigate to Add Listing Screen

---

## Data Model: `PgListing.kt`

```kotlin
data class PgListing(
    val listingId: String = "",
    val ownerId: String = "",
    val title: String = "",
    val description: String = "",
    val city: String = "",
    val area: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val price: Int = 0,
    val deposit: Int = 0,
    val roomType: String = "",
    val genderAllowed: String = "",
    val amenities: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val availableBeds: Int = 0,
    val isActive: Boolean = true,
    val rating: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)
```

> **IMPORTANT**: Use these exact field names everywhere:
> - `listingId` (NOT `id`)
> - `area` (NOT `address`)
> - `roomType` (NOT `type`)
> - `genderAllowed` (NOT `genderPreference`)

---

## Next Step
Proceed to **Phase 5: Chat System** → `05_chat_and_remaining.md`
