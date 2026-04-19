# SYshop Backend and Database Documentation

## 1. Backend Overview

This app does **not** use a custom backend server written in Node.js, PHP, Java Spring, or another server framework.

Instead, the backend is mainly provided by **Firebase services**:

- **Firebase Authentication** for user login and account creation
- **Cloud Firestore** for app data storage
- **Firebase Analytics** for analytics
- **Firebase Storage** dependency exists in the project, but the current app code does not actively use Storage operations

The Android app itself contains most of the business logic. That means:

- Activities decide when to load and save data
- Repository classes talk directly to Firebase
- local SQLite is used as a cache
- static manager classes keep temporary in-memory session state

So the architecture is:

```text
Android UI
-> local memory managers
-> local SQLite cache
-> Firebase Auth / Firestore
```

## 2. Main Backend Components

### Firebase Auth

Firebase Auth is responsible for:

- creating accounts
- logging users in
- checking whether a user is logged in
- sending password reset emails
- providing the current user ID (`uid`) used for Firestore paths

Relevant app files:

- `app/src/main/java/com/example/SYshop/activities/LoginActivity.java`
- `app/src/main/java/com/example/SYshop/activities/RegisterActivity.java`
- `app/src/main/java/com/example/SYshop/utils/AuthManager.java`

### Cloud Firestore

Firestore is the real backend database used for:

- product catalog
- user profile
- favorites
- cart
- orders
- order items

Relevant app files:

- `app/src/main/java/com/example/SYshop/database/ProductRepository.java`
- `app/src/main/java/com/example/SYshop/database/FavoriteSyncRepository.java`
- `app/src/main/java/com/example/SYshop/database/CartSyncRepository.java`
- `app/src/main/java/com/example/SYshop/database/OrderSyncRepository.java`

### Local SQLite

SQLite is used only as a **local cache**, not as the main source of truth.

It currently caches:

- product data
- favorite data

Relevant app files:

- `app/src/main/java/com/example/SYshop/database/LocalDbHelper.java`
- `app/src/main/java/com/example/SYshop/database/ProductCacheRepository.java`
- `app/src/main/java/com/example/SYshop/database/FavoriteCacheRepository.java`

### In-memory managers

The app also uses static memory state:

- `CartManager`
- `FavoriteManager`

These are fast session-level stores inside the app process. They help update badges and lists immediately, but they are not durable like Firestore or SQLite.

## 3. Database Layers

The app has 3 data layers working together:

### Layer 1: In-memory session data

Classes:

- `CartManager`
- `FavoriteManager`
- internal memory cache in `ProductRepository`

Purpose:

- instant UI updates
- fast access while app is open
- temporary session state

Limit:

- data is lost when the app process is killed unless it was synced elsewhere

### Layer 2: Local SQLite cache

Classes:

- `LocalDbHelper`
- `ProductCacheRepository`
- `FavoriteCacheRepository`

Purpose:

- keep previously loaded products available
- allow favorites to appear quickly even before cloud refresh
- provide offline-like fallback for some screens

Limit:

- currently only products and favorites are cached locally
- cart and orders are not stored in SQLite

### Layer 3: Firestore cloud database

Classes:

- `ProductRepository`
- `FavoriteSyncRepository`
- `CartSyncRepository`
- `OrderSyncRepository`

Purpose:

- persistent shared user data
- per-user cart/favorites/orders
- product catalog storage

This is the real backend database for the app.

## 4. Firestore Database Structure

### 4.1 Root-level collections

The app currently uses:

- `products`
- `users`

### Firestore structure map

```text
products/{productId}

users/{uid}
users/{uid}/favorites/{productId}
users/{uid}/cart/{productId}
users/{uid}/orders/{orderId}
users/{uid}/orders/{orderId}/items/{productId}
```

### 4.2 Products collection

Collection:

`products/{productId}`

This collection holds the product catalog displayed on the home page, search screen, and product details screen.

### Expected fields

The code supports these product fields:

- `id`
- `category`
- `tag`
- `name`
- `price`
- `description`
- `image_res`
- `image`
- `images`
- `imageUrl`
- `hasOffer`
- `discountPercent`
- `oldPrice`
- `rating`
- `review_count`
- `reviewCount`

### Why multiple field names are supported

`Product.fromMap(...)` accepts more than one naming style because Firestore data may not always be perfectly consistent.

Examples:

- `review_count` or `reviewCount`
- `imageUrl` or `image_url`
- `image_res` or `image`

That makes the app more tolerant to mixed product document formats.

### 4.3 Users profile document

Document:

`users/{uid}`

This document stores the main profile information for the logged-in user.

### Fields used by the app

- `fullName`
- `email`
- `phone`
- `address`
- `avatarUrl`
- `avatarPreset`

### How it is used

- created during registration
- loaded in `ProfileActivity`
- updated in `EditProfileActivity`
- checked during checkout to confirm phone/address exist

### 4.4 Favorites subcollection

Collection:

`users/{uid}/favorites/{productId}`

Each favorite document stores a **snapshot** of the product, not just a boolean flag.

### Why snapshot storage is used

This lets the app:

- render favorite cards quickly
- keep favorite information even if product list is not loaded yet
- later enrich the data with newer product data if available

### Write behavior

When the user favorites a product:

1. the app updates `FavoriteManager`
2. the app writes the product into SQLite favorites cache
3. the app writes product snapshot to Firestore

When the user removes a favorite:

1. the app updates `FavoriteManager`
2. removes the record from SQLite
3. deletes the Firestore favorite document

### 4.5 Cart subcollection

Collection:

`users/{uid}/cart/{productId}`

Each cart document stores:

- product snapshot
- `quantity`

### Cart document behavior

When adding an item:

- app checks if the document already exists
- if yes, it increments `quantity`
- if not, it creates the document with quantity

### Why cart uses product snapshots too

This helps the cart screen load product information directly from cart documents without needing a second product query for every row.

### 4.6 Orders collection

Collection:

`users/{uid}/orders/{orderId}`

Each order document stores header data:

- `orderId`
- `totalPrice`
- `status`
- `createdAt`
- `itemsCount`

### Meaning of fields

- `orderId`: generated by Firestore document creation
- `totalPrice`: string like `"$120.00"`
- `status`: currently saved as `"Placed"`
- `createdAt`: current timestamp in milliseconds
- `itemsCount`: number of cart rows, not total quantity sum

### 4.7 Order items subcollection

Collection:

`users/{uid}/orders/{orderId}/items/{productId}`

Each item document stores:

- product snapshot
- `productId`
- `quantity`

### Why order items are stored separately

This gives the app a permanent snapshot of what was bought during checkout.

That means:

- order history remains stable
- purchased item details can still be displayed later
- the app does not need live cart data after checkout

## 5. Local SQLite Database

### Database name

`syshop_local.db`

### Helper class

`LocalDbHelper.java`

### Current tables

- `products_cache`
- `favorites_cache`

### Common columns

Both tables contain product snapshot fields:

- `id INTEGER PRIMARY KEY`
- `category TEXT`
- `tag TEXT`
- `name TEXT`
- `price TEXT`
- `description TEXT`
- `image_res INTEGER`
- `image_url TEXT`
- `rating REAL`
- `review_count INTEGER`

### 5.1 `products_cache`

Purpose:

- keep a local copy of fetched products
- allow the home screen, search screen, and product details screen to render cached content before Firestore returns

Usage:

- filled by `ProductCacheRepository.replaceAllProducts(...)`
- read by `getCachedProducts()` and `getCachedProductById(...)`

### How the app uses it

- `MainActivity` loads cached products before remote fetch
- `SearchActivity` loads cached products before remote fetch
- `ProductDetailsActivity` loads cached product details before remote fetch

### 5.2 `favorites_cache`

Purpose:

- quickly restore favorite items
- provide local fallback if favorite cloud load is slow or fails

Usage:

- written by `FavoriteCacheRepository.saveFavorite(...)`
- removed by `removeFavorite(...)`
- fully cleared during cloud favorite reload

### How the app uses it

- `FavoritesActivity` merges cached favorites with in-memory favorites
- `ProductDetailsActivity` checks cache to decide initial heart icon state
- `ProductAdapter` and `PromoAdapter` also check cache for favorite icon state

## 6. Backend Repository Classes

### 6.1 `ProductRepository`

Role:

- reads products from Firestore
- loads product by ID
- maintains in-memory product cache

### Important methods

- `loadProducts(...)`
- `loadProductById(...)`
- `getCachedProduct(...)`
- `getCachedProducts()`

### Load strategy

- query `products` collection
- convert Firestore documents to `Product`
- store them in memory cache

### Special fallback logic

When loading by ID:

1. it first tries `products/{productId}`
2. if that fails or does not exist, it searches with `whereEqualTo("id", productId)`

This is useful when Firestore doc ID and product `id` field are not aligned.

### 6.2 `ProductCacheRepository`

Role:

- manages SQLite product cache

### Main methods

- `replaceAllProducts(...)`
- `getCachedProducts()`
- `getCachedProductById(...)`
- `hasCachedProducts()`

### Behavior

On full refresh, it:

1. clears `products_cache`
2. inserts all current products again

This is a simple cache replacement strategy.

### 6.3 `FavoriteSyncRepository`

Role:

- sync favorites to Firestore
- sync favorites to local SQLite

### Main methods

- `addFavorite(...)`
- `removeFavorite(...)`
- `loadFavoritesFromCloud(...)`

### Behavior

When loading cloud favorites:

1. reads all favorite docs from Firestore
2. clears local SQLite favorite table
3. saves every returned favorite into SQLite
4. returns list to the caller

So SQLite becomes a local mirror of the latest cloud favorites.

### 6.4 `FavoriteCacheRepository`

Role:

- stores and retrieves local favorite snapshots

### Main methods

- `saveFavorite(...)`
- `removeFavorite(...)`
- `clearAllFavorites()`
- `isFavoriteCached(...)`
- `getAllFavorites()`

### 6.5 `CartSyncRepository`

Role:

- syncs user cart with Firestore

### Main methods

- `addOrIncreaseCartItem(...)`
- `updateQuantity(...)`
- `removeItem(...)`
- `loadCartFromCloud(...)`

### Backend logic

When adding to cart:

1. open `users/{uid}/cart`
2. read existing document for product
3. calculate new quantity
4. write updated product snapshot + quantity

When decreasing quantity to zero or less:

- the app deletes the cart document

### 6.6 `OrderSyncRepository`

Role:

- creates orders during checkout
- loads orders list
- loads order items

### Main methods

- `checkout(...)`
- `loadOrders(...)`
- `loadOrderItems(...)`

### Checkout backend flow

1. check user is logged in
2. check cart is not empty
3. create new order document reference
4. build order header data
5. create a Firestore batch
6. add order header to batch
7. add each cart item into `items` subcollection
8. read all cart docs
9. add cart deletions to batch
10. commit batch

This gives one logical checkout operation for:

- order creation
- order item creation
- cart clearing

## 7. How Data Moves Through the App

### 7.1 Product loading flow

```text
Screen opens
-> check ProductRepository memory cache
-> check SQLite products_cache
-> request Firestore products
-> update UI
-> refresh SQLite cache
```

Used by:

- `MainActivity`
- `SearchActivity`
- `ProductDetailsActivity`

### 7.2 Favorites flow

```text
User taps favorite
-> Auth check
-> update FavoriteManager memory
-> save/remove SQLite favorite
-> save/remove Firestore favorite
-> refresh UI
```

Used by:

- `ProductAdapter`
- `PromoAdapter`
- `ProductDetailsActivity`
- `FavoritesActivity`

### 7.3 Cart flow

```text
User adds product to cart
-> Auth check
-> update CartManager memory
-> write/update Firestore cart document
-> badge/UI refresh
```

Cart screen load:

```text
CartActivity opens
-> reads CartManager
-> loads Firestore cart
-> rebuilds CartManager from cloud response
-> refreshes UI
```

### 7.4 Checkout flow

```text
Checkout button pressed
-> load profile from users/{uid}
-> verify phone and address exist
-> show confirmation dialog
-> OrderSyncRepository.checkout(...)
-> create order doc
-> create item subdocs
-> delete cart docs
-> clear CartManager
-> open OrdersActivity
```

### 7.5 Orders flow

```text
OrdersActivity opens
-> query users/{uid}/orders ordered by createdAt desc
-> map docs to Order objects
-> display timeline
```

Order details:

```text
OrderDetailsActivity opens
-> query users/{uid}/orders/{orderId}/items
-> map docs to OrderItem
-> enrich image data from product cache/product repository
-> display ordered items
```

### 7.6 Profile flow

```text
RegisterActivity
-> create Firebase Auth user
-> create users/{uid} profile doc

ProfileActivity
-> read users/{uid}

EditProfileActivity
-> update users/{uid}
```

## 8. Important Backend Design Choices

### 8.1 Snapshot-based storage

Cart, favorites, and order items all store copies of product information instead of only storing product IDs.

### Benefits

- easier UI rendering
- fewer extra Firestore reads per list item
- order history stays readable even if live product data changes

### Tradeoff

- duplicated data
- some snapshots may become stale

The app partly solves this by enriching favorites and order items later with fresher product data when available.

### 8.2 Client-driven backend logic

There is no custom backend layer or Cloud Functions in the current project code.

The Android client directly performs:

- Firestore writes
- Firestore reads
- order creation logic
- cart quantity logic
- profile validation before checkout

### Implication

Most business rules currently live on the client side, so secure Firestore rules are very important for production.

### 8.3 Cache-first UX

Several screens show cached data before remote data arrives.

That improves:

- perceived speed
- resilience when network is slow

But it also means:

- UI may first show older data, then refresh

## 9. What the Backend Currently Does Not Include

The current codebase does **not** show these backend features:

- custom REST API
- GraphQL API
- Cloud Functions
- admin panel backend
- payment gateway integration
- shipment/tracking backend integration
- inventory reservation logic
- server-side order validation layer
- local Room database
- SQL server backend

So this app is best described as:

**Android client + Firebase backend + SQLite cache**

## 10. Security and Production Considerations

Based on the current code design, a production deployment should pay attention to:

- Firestore security rules
- restricting users to only their own `users/{uid}` data
- controlling writes to favorites/cart/orders paths
- validating product write permissions
- protecting order integrity

Because the app writes directly from the client, the database rules are the real backend protection layer.

## 11. Backend Summary

### Main backend technologies

- Firebase Auth
- Cloud Firestore
- SQLite local cache

### Main backend responsibilities

- product catalog storage
- user authentication
- profile storage
- favorites sync
- cart sync
- checkout order creation
- order history storage

### Main idea

The backend works by combining:

1. Firebase cloud data for persistence
2. SQLite for local cached data
3. in-memory managers for fast current-session UI state

That combination is the core of how this app's database and backend work.
