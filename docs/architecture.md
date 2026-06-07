# PicExchange — Unified Architecture & Implementation Plan

**A native Android PECS communication app for Wings Melaka (special-needs / autism centre, Melaka, Malaysia)**
Final Year Project deliverable · Kotlin + Jetpack Compose · 100% offline / on-device

---

## 1. Overview & Scope

PicExchange is a single-Activity, fully offline Android app implementing the Picture Exchange Communication System (PECS). A non-verbal or minimally-verbal learner browses categories of picture cards and sentence-starter cards, assembles them in order onto a horizontal **sentence strip**, and presses **Speak** to have the strip read aloud by the device's on-device text-to-speech engine. A teacher/parent can additionally author content (add/edit/delete cards and categories).

### Four core features (kept deliberately basic)
1. **Add custom images & sentence starters** — pick from gallery (permissionless photo picker) or camera, give a text label.
2. **Organize cards into categories** — create/select; mix of pre-built (seeded) + custom.
3. **Drag-and-drop AND tap** to add picture/starter cards onto a horizontal sentence strip, in order; reorder within the strip; remove items.
4. **Text-to-speech** — a Speak button reads the strip aloud in order using Android's built-in offline TTS engine, highlighting each card as it is spoken.

Supporting actions: edit/delete cards, create/delete categories, clear the strip (with undo).

### Two actors (UI role model — NOT authentication)
- **Teacher / Parent** — FULL access including add/edit/delete of cards and categories.
- **Learner / Autistic child** — RESTRICTED: browse, build a sentence, reorder/remove strip items, Speak, Clear. No editing/adding/deleting and no access to Settings.

### OUT OF SCOPE (do not design or build; flag any creep)
- ❌ Login / authentication / user accounts (the parental PIN is a **local child-lock**, not auth — no usernames, no recovery server)
- ❌ Cloud sync / backup / Google Drive / Firebase (`android:allowBackup="false"`)
- ❌ Multi-language management UI (TTS only selects among **already-installed offline device voices**)
- ❌ Cross-platform / iOS / Kotlin Multiplatform (Android-only; Room/DataStore KMP capability deliberately unused)
- ❌ Online payments / billing
- ❌ Analytics / usage tracking / progress dashboards
- ❌ Local zip export/import — documented as **future work only**, not built for the FYP
- ❌ Onboarding flows, card marketplace, themes store, favorites/history screens
- ❌ Library/category reorder UI (the `position` column exists only for deterministic ordering — see §4)

---

## 2. Tech Stack & Versions

All versions verified against official release notes as of **June 2026**. Pin in `gradle/libs.versions.toml`. Use a single source of truth; pin Compose artifacts via the BOM (name only the BOM version).

> ### ⚙️ AS-BUILT versions (M0, build-verified 2026-06-07)
> The table below is the *aspirational* 2026 stack. The project was actually scaffolded against the **toolchain proven on the dev machine (Android Studio 252, JDK 21, Android SDK 36)** to guarantee it opens and builds locally without a Studio upgrade. The bleeding-edge stack hit two hard walls: **Hilt 2.58+ require AGP 9.0+**, and **Hilt 2.57.2's metadata reader maxes out at Kotlin 2.2 metadata** — so AGP 8.12 forces a coherent late-2025 generation. What is actually pinned in `gradle/libs.versions.toml`:
>
> | Component | As-built | vs. aspirational |
> |---|---|---|
> | Gradle (wrapper) | **8.14.3** | 9.x |
> | Android Gradle Plugin | **8.12.0** | 9.2.x |
> | Kotlin / Compose compiler | **2.2.21** | 2.3.21 |
> | KSP | **2.2.21-2.0.5** | 2.3.9 |
> | Hilt | **2.57.2** | 2.59.2 |
> | Compose BOM | **2025.12.01** | 2026.05.01 |
> | activity-compose / lifecycle | **1.11.0 / 2.9.4** | 1.13.0 / 2.10.0 |
> | compileSdk / minSdk / targetSdk | **36 / 26 / 36** | (same) |
> | Java target / JDK | **17 / 21** | (same) |
>
> **To move to the 2026/AGP-9 stack later:** update Android Studio, then bump the whole version block in one edit (AGP→9.x, Gradle→9.x, Kotlin→2.3.x, KSP→2.3.x, Hilt→2.59.2, Compose BOM→2026.x). The app code does not change. Verified: `./gradlew testDebugUnitTest assembleDebug` → BUILD SUCCESSFUL, 3/3 tests green, debug APK produced.

| Component | Artifact / Plugin | Version | Notes |
|---|---|---|---|
| Android Gradle Plugin | `com.android.application` | **9.2.0** | Requires Gradle 9.1+; supports compileSdk 36 |
| Kotlin | `org.jetbrains.kotlin.android` | **2.3.x** (pin current patch) | Pin one patch at project creation |
| Compose Compiler | `org.jetbrains.kotlin.plugin.compose` | = Kotlin version | Replaces deprecated `composeOptions` |
| KSP | `com.google.devtools.ksp` | **2.3.x** (decoupled; e.g. `2.3.9`) | Single-version scheme; must align to Kotlin patch. **Do NOT use the old `2.2.x-2.0.z` format or fabricated `2.3.10-2.0.0`** |
| Compose BOM | `androidx.compose:compose-bom` | **2026.05.00** | Maps Compose UI/foundation → **1.11.1**, material3 → 1.4.0. (2026.06.00 also available) |
| Material3 | `androidx.compose.material3:material3` | (via BOM) 1.4.0 | Do not ship material3 1.5.0 alpha |
| Navigation Compose | `androidx.navigation:navigation-compose` | **2.9.x** (current patch, ~2.9.8) | Type-safe `@Serializable` routes |
| kotlinx-serialization-json | `org.jetbrains.kotlinx:kotlinx-serialization-json` | **1.7.x** | Nav routes + seed JSON |
| Activity Compose | `androidx.activity:activity-compose` | **1.13.0** | `PickVisualMedia`, `TakePicture` |
| Lifecycle | `androidx.lifecycle:lifecycle-runtime-compose`, `lifecycle-viewmodel-compose` | **2.10.0** | `collectAsStateWithLifecycle`, `viewModel()` |
| Room | `androidx.room:room-runtime / room-ktx / room-compiler / room-testing` | **2.8.4** | Stable; KSP backend. **Not** Room 3.0 (alpha) |
| DataStore | `androidx.datastore:datastore-preferences` | **1.2.1** | Role flag + hashed PIN + prefs |
| Hilt | `com.google.dagger:hilt-android / hilt-compiler` | **2.59.2** | 2.59.1 had an AGP-9 build break |
| Hilt + Compose | `androidx.hilt:hilt-lifecycle-viewmodel-compose` | **1.3.0** | `hiltViewModel()` **moved here** from `hilt-navigation-compose` (deprecated there). Add `hilt-navigation-compose` only if nav-back-stack-scoped APIs are needed |
| Coil 3 | `io.coil-kt.coil3:coil-compose` | **3.4.0** | `AsyncImage`; offline (no `coil-network`) |
| Reorderable | `sh.calvin.reorderable:reorderable` | **3.1.0** | LazyRow reorder for the strip (MIT). Requires `Modifier.animateItem` (satisfied by BOM) |
| Robolectric | `org.robolectric:robolectric` | **4.16.x** | JVM Compose tests; **requires JDK 21** for SDK 36 |
| Coroutines test | `org.jetbrains.kotlinx:kotlinx-coroutines-test` | **1.8.x** | `runTest` |
| Truth | `com.google.truth:truth` | **explicit version** (~1.4.x) | Not covered by Compose BOM — must pin |
| Turbine (optional) | `app.cash.turbine:turbine` | **1.2.1** | Flow emission assertions |

**SDK levels:** `compileSdk = 36`, `targetSdk = 36`, `minSdk = 26` (Android 8.0). Java/Kotlin toolchain **JDK 21**. minSdk 26 reaches ~95% of devices and is the clean modern Compose floor; everything actually built works at API 21, so dropping to 24 is possible if an older centre tablet demands it.

```toml
# gradle/libs.versions.toml (excerpt — pin exact KSP/Kotlin patches at creation)
[versions]
agp = "9.2.0"
kotlin = "2.3.21"            # pin current 2.3.x patch
ksp = "2.3.9"               # decoupled single-version scheme aligned to Kotlin
composeBom = "2026.05.00"
activityCompose = "1.13.0"
lifecycle = "2.10.0"
navigation = "2.9.8"
room = "2.8.4"
hilt = "2.59.2"
hiltCompose = "1.3.0"
datastore = "1.2.1"
coil = "3.4.0"
reorderable = "3.1.0"
serialization = "1.7.3"
robolectric = "4.16.1"
coroutinesTest = "1.8.1"
truth = "1.4.4"
```

---

## 3. High-Level Architecture

**Pattern:** Single-module **MVVM + Jetpack Compose** with strict **unidirectional data flow (UDF)**. Each screen has one immutable `UiState` exposed as `StateFlow<XxxUiState>`, collected via `collectAsStateWithLifecycle()`. All user actions are ViewModel methods (events up); state flows down. No business logic in composables.

**DI:** Hilt. `@HiltAndroidApp` on the Application, `@AndroidEntryPoint` on `MainActivity`, `@HiltViewModel` on ViewModels, `hiltViewModel()` (from `hilt-lifecycle-viewmodel-compose`) in Compose. Room DB/DAOs, repositories, DataStore, and `TtsManager` provided via `@Module @InstallIn(SingletonComponent::class)`.

**Layers**
```
UI (Compose screens + ViewModels, UDF)         ← reads StateFlow, sends events
  ↓ events / ↑ state
Domain (light: shared models + ImageRef resolver; optional use-cases only if real cross-repo logic)
  ↓
Data (repositories → Room DAOs [Flow reads, suspend writes]; DataStore; TtsManager)
  ↓
Android platform (Room/SQLite, DataStore, TextToSpeech, FileProvider, PhotoPicker)
```

Room is the **single source of truth** for cards/categories. The **sentence strip is in-memory ViewModel state** (not persisted to Room), with a `SavedStateHandle` draft for rotation/process-death survival. Fresh-start-per-app-launch is the intended behavior.

### Package structure (package-by-feature)
```
com.wings.picexchange/
  PicExchangeApp.kt              // @HiltAndroidApp
  MainActivity.kt                // @AndroidEntryPoint → setContent { AppShell() }
  di/
    DatabaseModule.kt  RepositoryModule.kt  TtsModule.kt  DataStoreModule.kt
  data/
    local/ entity/{CardEntity,CategoryEntity}.kt  dao/{CardDao,CategoryDao}.kt  PicExchangeDatabase.kt  Converters.kt
    repository/{CardRepository,CategoryRepository,AppModeRepository}.kt
    datastore/AppPreferences.kt
    image/ImageRef.kt            // canonical typed image-ref resolver (compile-time drawable map)
    tts/{Speaker.kt, AndroidTtsSpeaker.kt, TtsManager.kt}
  domain/
    model/{Card,Category,CardType,StripItem,AppMode}.kt   // canonical StripItem lives HERE
  ui/
    AppShell.kt                  // top-level Scaffold: persistent strip bottomBar + NavHost
    theme/  common/ (BigCardTile, ConfirmDialog, PinDialog, EmptyState, LoadingSkeleton)
    navigation/PicExchangeNavHost.kt
    mode/AppModeViewModel.kt     // exposes AppMode app-wide
    home/  library/  strip/  cardeditor/  categoryeditor/  settings/  rolegate/
  drag/DragState.kt              // transient gesture state (CompositionLocal), provided at root
```

---

## 4. Data Model

### 4.1 Canonical identity & shared types (resolved cross-cutting contracts)

These were inconsistent across source dimensions and are now **fixed once** here:

- **All card/category IDs are `Long`** (Room autogenerated PK). Navigation routes pass `Long`. The drag layer uses `Long` too. The **only** `String` key in the app is the strip item's per-instance `UUID` (`instanceId`), used solely as a stable LazyRow key.
- **One image-reference format everywhere:** a typed string — `"drawable:<resName>"` for bundled images, `"file:<relativeName>"` for custom images copied into `filesDir/card_images/`. Both `Card.image` and `Category.iconRef` use this. Built-in and custom cards render through **one** Coil path.
- **`drawable:` is resolved via a compile-time `Map<String, Int>`** of seed name → `R.drawable.*` (NOT `resources.getIdentifier`, which R8/AGP-9 resource shrinking strips → `Resources$NotFoundException` in release).

```kotlin
// domain/model/ImageRef + data/image/ImageRef.kt
sealed interface ImageRef {
    data class Drawable(@DrawableRes val resId: Int) : ImageRef
    data class LocalFile(val name: String) : ImageRef
}

// Compile-time, R8-safe map of seed drawable names → resource IDs.
private val SEED_DRAWABLES: Map<String, Int> = mapOf(
    "ic_drink" to R.drawable.ic_drink,
    "ic_cat_food" to R.drawable.ic_cat_food,
    // ... every seeded drawable listed explicitly
)

fun String.toImageRef(): ImageRef = when {
    startsWith("drawable:") -> ImageRef.Drawable(
        SEED_DRAWABLES[removePrefix("drawable:")]
            ?: error("Unknown seed drawable: $this")
    )
    startsWith("file:") -> ImageRef.LocalFile(removePrefix("file:"))
    else -> error("Bad image ref: $this")
}

fun ImageRef.toCoilModel(ctx: Context): Any = when (this) {
    is ImageRef.Drawable -> resId                         // Int → Coil
    is ImageRef.LocalFile -> File(ctx.filesDir, "card_images/$name")
}
// UI everywhere: AsyncImage(model = card.image.toImageRef().toCoilModel(ctx), ...)
```

### 4.2 Entities

```kotlin
enum class CardType { PICTURE, SENTENCE_STARTER }

@Entity(
    tableName = "categories",
    indices = [Index("position")]   // name uniqueness enforced in repository over VISIBLE rows, not a DB UNIQUE index
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconRef: String,        // "drawable:..." or "file:..."
    val position: Int,          // deterministic display order (set = max+1 on add; no reorder UI in v1)
    val isBuiltIn: Boolean = false  // built-ins are hidden, never hard-deleted
)

@Entity(
    tableName = "cards",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE,     // deleting a category removes its cards (verified safety property)
        onUpdate = ForeignKey.CASCADE
    )],
    indices = [Index("categoryId"), Index(value = ["categoryId", "position"])]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,          // displayed + spoken (drives TTS)
    val type: CardType,         // stored as name via TypeConverter
    val image: String,          // "drawable:..." or "file:..."
    val categoryId: Long,
    val position: Int,
    val isBuiltIn: Boolean = false
)

class Converters {
    @TypeConverter fun typeToString(t: CardType) = t.name
    @TypeConverter fun stringToType(s: String) = CardType.valueOf(s)  // name, not ordinal
}
```

> **Category name uniqueness:** enforced in the repository over **currently-visible** rows (friendly error message) rather than a DB `UNIQUE` index, so a hidden built-in "Food" does not block creating a user "Food". (Avoids the soft-hide collision the data verifier flagged.)

### 4.3 Canonical `StripItem` (the single most-shared state)

One Parcelable type used by **every** dimension (drag, strip render, TTS, SavedStateHandle). It **copies `label` + `imageRef` at add-time**, so deleting the source card mid-sentence is harmless.

```kotlin
@Parcelize
data class StripItem(
    val instanceId: String = UUID.randomUUID().toString(), // LazyRow key (duplicates allowed)
    val cardId: Long,            // source card (Long, consistent with Room/nav)
    val label: String,           // snapshot — TTS reads this
    val imageRef: String,        // snapshot — "drawable:"/"file:", same resolver as cards
    val isStarter: Boolean
) : Parcelable
```

### 4.4 DAOs

```kotlin
@Dao interface CategoryDao {
    @Insert suspend fun insert(c: CategoryEntity): Long
    @Insert suspend fun insertAll(cs: List<CategoryEntity>)
    @Update suspend fun update(c: CategoryEntity)
    @Delete suspend fun delete(c: CategoryEntity)
    @Query("SELECT * FROM categories ORDER BY position") fun observeAll(): Flow<List<CategoryEntity>>
    @Query("SELECT * FROM categories WHERE id = :id") suspend fun getById(id: Long): CategoryEntity?
    @Query("SELECT COUNT(*) FROM categories") suspend fun count(): Int
}

@Dao interface CardDao {
    @Insert suspend fun insert(card: CardEntity): Long
    @Insert suspend fun insertAll(cards: List<CardEntity>)
    @Update suspend fun update(card: CardEntity)
    @Delete suspend fun delete(card: CardEntity)
    @Query("SELECT * FROM cards WHERE categoryId = :catId ORDER BY position")
    fun observeByCategory(catId: Long): Flow<List<CardEntity>>
    @Query("SELECT * FROM cards WHERE id = :id") suspend fun getById(id: Long): CardEntity?
}
```
Reads return `Flow` → Compose auto-refreshes. Writes are `suspend` (from `viewModelScope`, on Room's IO dispatcher).

### 4.5 Image import (off main thread, returns `Result`)

```kotlin
suspend fun importImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val dir = File(context.filesDir, "card_images").apply { mkdirs() }
        val name = "${UUID.randomUUID()}.jpg"
        context.contentResolver.openInputStream(uri).use { input ->
            val bmp = decodeDownscaled(input!!, maxDim = 1024)   // inSampleSize / ImageDecoder
            FileOutputStream(File(dir, name)).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
        }
        "file:$name"   // exact string stored in CardEntity.image
    }
}
// On card DELETE, repository also deletes the orphaned file (Room CASCADE removes rows, not files).
```

### 4.6 First-run seeding (robust, no fire-and-forget)

Seed via `RoomDatabase.Callback.onCreate` reading `assets/seed/seed_data.json`, but **do not** use an ad-hoc `CoroutineScope(...).launch`. Either insert with `db.execSQL` directly inside `onCreate`, **or** (recommended) seed lazily on first repository access guarded by `count() == 0` using a managed application scope. Seed JSON references bundled drawables (`"image":"drawable:ic_drink"`), so no files are copied at seed time. Built-in rows get `isBuiltIn = true`. Seeded sentence starters ("I want", "I see", "I feel", "give me", "more") use `CardType.SENTENCE_STARTER`. **Seeded content makes Learner mode non-empty on first boot**, enabling a demo before any Teacher setup.

```kotlin
@Database(entities = [CategoryEntity::class, CardEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class PicExchangeDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun cardDao(): CardDao
}
// Repository: suspend fun seedIfEmpty() { if (categoryDao.count() == 0) { /* parse JSON, insertAll */ } }
```

**Migrations:** `exportSchema = true`, `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`, commit schema JSON, add the schemas dir as an `androidTest` asset for `MigrationTestHelper`. Production uses real `Migration`/`@AutoMigration`. `fallbackToDestructiveMigration(dropAllTables = true)` is **debug-only** (note: the no-arg form is deprecated in Room 2.7+).

---

## 5. Screen Map & Navigation

**Single Activity → `AppShell()` (top-level Scaffold) → `NavHost`.** The persistent **sentence strip** lives in the Scaffold `bottomBar` and is shown only on browsing screens (Home, Card Library); editor/settings screens hide it for full height. Type-safe `@Serializable` routes; arguments are IDs only (`Long`), nullable id encodes Add vs Edit.

| ID | Route | Strip? | Role | Purpose |
|---|---|---|---|---|
| Home | `@Serializable object Home` (start) | ✅ | All | Category grid |
| CardLibrary | `data class CardLibrary(categoryId: Long)` | ✅ | All | Cards in one category |
| CardEditor | `data class CardEditor(cardId: Long? = null, presetCategoryId: Long? = null)` | ❌ | Teacher | Add (null id) / Edit card |
| CategoryEditor | `data class CategoryEditor(categoryId: Long? = null)` | ❌ | Teacher | Create / Edit category |
| Settings | `@Serializable object Settings` | ❌ | Teacher | Role switch, PIN, appearance, speech, TTS-unavailable help |
| RoleGate | `@Serializable object RoleGate` | ❌ | — | PIN entry / first-run PIN setup |

```kotlin
@Composable
fun AppShell() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val showStrip = backStack?.destination?.hasRoute(Home::class) == true ||
                    backStack?.destination?.hasRoute(CardLibrary::class) == true

    // STRIP VM OBTAINED ONCE, ABOVE THE NAVHOST → survives Home↔Library navigation.
    val stripVm: StripViewModel = hiltViewModel()       // Activity ViewModelStoreOwner at AppShell scope
    val modeVm: AppModeViewModel = hiltViewModel()
    val mode by modeVm.appMode.collectAsStateWithLifecycle()   // initial value = LEARNER (synchronous)
    val dragState = remember { DragState() }            // ONE instance, provided at root

    CompositionLocalProvider(LocalAppMode provides mode, LocalDragState provides dragState) {
        Box(Modifier.fillMaxSize()) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = { if (showStrip) SentenceStrip(stripVm) }
            ) { pad ->
                NavHost(nav, startDestination = Home, modifier = Modifier.padding(pad)) {
                    composable<Home> { HomeScreen(stripVm = stripVm, onOpenCategory = { nav.navigate(CardLibrary(it)) }, ...) }
                    composable<CardLibrary> { e -> CardLibraryScreen(stripVm = stripVm, args = e.toRoute(), ...) }
                    composable<CardEditor> { e -> TeacherGuarded { CardEditorScreen(e.toRoute(), nav::popBackStack) } }
                    composable<CategoryEditor> { e -> TeacherGuarded { CategoryEditorScreen(e.toRoute(), nav::popBackStack) } }
                    composable<Settings> { TeacherGuarded { SettingsScreen(onRequestTeacher = { nav.navigate(RoleGate) }) } }
                    composable<RoleGate> { RoleGateScreen(onUnlocked = nav::popBackStack) }
                }
            }
            DragGhostOverlay()   // floats above NavHost + bottomBar, reads LocalDragState
        }
    }
}
```
> **Do NOT** call `hiltViewModel<StripViewModel>()` inside individual `composable<>` destinations — each would get a fresh instance and the in-progress sentence would reset on navigation, defeating the persistent-strip design. Pass the one AppShell instance down.

**Material3 theming:** custom **WCAG-AA-checked high-contrast `ColorScheme`** (light + dark), large type scale, large card density. **Dynamic color is disabled** on the learner surface. No `NavigationBar` for primary nav (the strip occupies the bottom).

---

## 6. Sentence-Strip Interaction (tap + drag)

**Tap-to-add is the primary, mandatory path** (lowest-effort, accessibility-friendly for motor difficulties); **drag is an equal-status enhancement**. Both funnel into the same ViewModel intents. Reorder within the strip is drag-only (via `sh.calvin.reorderable`). Remove is a large X badge. Clear is confirm/undo.

### 6.1 Strip ViewModel (single source of truth; in-memory + SavedStateHandle)

```kotlin
data class StripUiState(
    val items: List<StripItem> = emptyList(),
    val isTeacher: Boolean = false,
    val speakingIndex: Int? = null,
    val ttsReady: Boolean = false,
)

@HiltViewModel
class StripViewModel @Inject constructor(
    private val tts: TtsManager,                 // @Singleton, process-scoped
    private val savedState: SavedStateHandle,
    appModeRepository: AppModeRepository,
) : ViewModel() {
    private val _items = MutableStateFlow(savedState.get<List<StripItem>>(KEY) ?: emptyList())
    private var lastCleared: List<StripItem> = emptyList()

    val uiState: StateFlow<StripUiState> =
        combine(_items, appModeRepository.appMode, tts.state) { items, mode, t ->
            StripUiState(items, mode == AppMode.TEACHER, t.speakingIndex, t.ready)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StripUiState())

    fun add(item: StripItem)            { update { it + item } }          // TAP and drag-drop both call this
    fun insertAt(i: Int, item: StripItem){ update { it.toMutableList().apply { add(i.coerceIn(0, size), item) } } }
    fun move(from: Int, to: Int)        { update { it.toMutableList().apply { add(if (to > from) to - 1 else to, removeAt(from)) } } } // off-by-one corrected
    fun removeAt(i: Int)                { update { it.toMutableList().apply { removeAt(i) } } }
    fun clearWithUndo()                 { lastCleared = _items.value; setItems(emptyList()) }
    fun undoClear()                     { if (lastCleared.isNotEmpty()) { setItems(lastCleared); lastCleared = emptyList() } }
    fun speak()                         { tts.speak(_items.value.map { it.label }) }

    private inline fun update(block: (List<StripItem>) -> List<StripItem>) = setItems(block(_items.value))
    private fun setItems(v: List<StripItem>) { _items.value = v; savedState[KEY] = v }
    companion object { private const val KEY = "strip_items" }
}
```

### 6.2 Drag state (transient, NOT in the persistent VM)

```kotlin
class DragState {
    var dragging by mutableStateOf<StripItem?>(null)
    var ghostOffset by mutableStateOf(Offset.Zero)   // root coordinates
    var stripBounds by mutableStateOf<Rect?>(null)   // captured via boundsInRoot()
    val isOverStrip get() = dragging != null && stripBounds?.contains(ghostOffset) == true
}
// Provided ONCE at AppShell root so library cards (in NavHost) and the strip (in bottomBar) share one instance.
val LocalDragState = staticCompositionLocalOf<DragState> { error("No DragState provided") }
```

### 6.3 Library card: tap + drag in ONE `pointerInput` (avoids the `clickable`+`detectDragGesturesAfterLongPress` conflict)

```kotlin
@Composable
fun LibraryCard(card: CardEntity, onAdd: (StripItem) -> Unit) {
    val drag = LocalDragState.current
    val haptics = LocalHapticFeedback.current
    var originInRoot by remember { mutableStateOf(Offset.Zero) }
    val item = remember(card) { StripItem(cardId = card.id, label = card.label, imageRef = card.image, isStarter = card.type == CardType.SENTENCE_STARTER) }

    Box(Modifier
        .size(96.dp)                                   // large touch target (≥64dp interactive area)
        .onGloballyPositioned { originInRoot = it.positionInRoot() }
        .semantics { role = Role.Button; contentDescription = card.label
                     onClick(label = "Add ${card.label} to sentence") { onAdd(item); true } }
        .pointerInput(card.id) {
            // BOTH gestures in one scope — do NOT also use Modifier.clickable here.
            detectTapGestures(onTap = { onAdd(item) })               // TAP (primary)
        }
        .pointerInput(card.id) {
            detectDragGesturesAfterLongPress(                        // DRAG (enhancement)
                onDragStart = { pos -> haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                drag.dragging = item; drag.ghostOffset = originInRoot + pos },
                onDrag = { change, delta -> change.consume(); drag.ghostOffset += delta },  // consume() required
                onDragEnd = { if (drag.isOverStrip) onAdd(item); drag.dragging = null },
                onDragCancel = { drag.dragging = null }
            )
        }
    ) { CardContent(card) }
}
```

### 6.4 Strip render (drop target + reorder + remove)

```kotlin
@Composable
fun SentenceStrip(vm: StripViewModel) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val drag = LocalDragState.current
    val listState = rememberLazyListState()
    val reorder = rememberReorderableLazyListState(listState) { from, to -> vm.move(from.index, to.index) }

    Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth().height(130.dp)) {
        Row {
            LazyRow(state = listState, modifier = Modifier.weight(1f)
                .onGloballyPositioned { drag.stripBounds = it.boundsInRoot() }
                .border(3.dp, if (drag.isOverStrip) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)) {
                if (state.items.isEmpty()) item { EmptyStripHint() }   // single shared string (see §8)
                itemsIndexed(state.items, key = { _, it -> it.instanceId }) { index, item ->
                    ReorderableItem(reorder, key = item.instanceId) { dragging ->
                        Box(Modifier.animateItem()) {
                            StripChip(item, dragging, Modifier.draggableHandle())
                            IconButton(onClick = { vm.removeAt(index) }, modifier = Modifier.align(Alignment.TopEnd).size(40.dp)) {
                                Icon(Icons.Default.Close, "Remove ${item.label}")
                            }
                        }
                    }
                }
            }
            Column {
                FilledButton(onClick = vm::speak, enabled = state.ttsReady && state.items.isNotEmpty(),
                             modifier = Modifier.heightIn(min = 64.dp)) { Icon(Icons.Default.VolumeUp, null); Text("Speak") }
                OutlinedButton(onClick = { /* Snackbar undo; AlertDialog confirm if items.size >= 4 */ }) { Text("Clear") }
            }
        }
    }
}
```
**Robustness notes:** the finger point is the hit-test reference (pad `stripBounds` slightly so near-misses still drop); `change.consume()` prevents the parent scrollable stealing the gesture; coordinate spaces all root-based (`positionInRoot` / `boundsInRoot` vs `ghostOffset`). **v1 supports append + drag-reorder**; positional drop-between (`insertAt`) is optional.

---

## 7. Text-to-Speech (offline, highlighted)

One process-wide `TtsManager` wrapping `android.speech.tts.TextToSpeech`, provided as Hilt `@Singleton`. **Per-card utterances** (`QUEUE_FLUSH` for the first, `QUEUE_ADD` for the rest) so `UtteranceProgressListener` can drive per-card highlighting. Speak reads `StripItem.label` snapshots in order. The engine is hidden behind a `Speaker` interface for testability.

### 7.1 Lifecycle (resolved conflict)

The engine is a **process-scoped `@Singleton`** and is **NEVER `shutdown()` in a ViewModel `onCleared()`** (that would kill it on the first rotation). Do **not** rely on `Application.onTerminate()` (not called on devices). Instead: `tts.stop()` on app-background via a `ProcessLifecycleOwner` `ON_STOP` observer; let the OS reclaim the engine on process death.

### 7.2 Sketch

```kotlin
interface Speaker {
    val state: StateFlow<SpeakState>            // ready + speakingIndex + unavailable reason
    fun speak(labels: List<String>)
    fun stop()
}
data class SpeakState(val ready: Boolean = false, val speakingIndex: Int? = null, val unavailable: TtsUnavailable? = null)
enum class TtsUnavailable { NO_ENGINE, LANGUAGE_MISSING_DATA, LANGUAGE_NOT_SUPPORTED, INIT_FAILED }

@Singleton
class TtsManager @Inject constructor(@ApplicationContext private val ctx: Context) : Speaker {
    private val _state = MutableStateFlow(SpeakState())
    override val state = _state.asStateFlow()
    private val main = Handler(Looper.getMainLooper())
    private var session = 0
    private var ready = false
    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(ctx) { status ->
            if (status != TextToSpeech.SUCCESS) { _state.value = SpeakState(unavailable = TtsUnavailable.NO_ENGINE); return@TextToSpeech }
            val target = Locale.getDefault()
            val res = if (tts.isLanguageAvailable(target) >= TextToSpeech.LANG_AVAILABLE) tts.setLanguage(target)
                      else tts.setLanguage(Locale.ENGLISH)
            when (res) {
                TextToSpeech.LANG_MISSING_DATA -> _state.value = SpeakState(unavailable = TtsUnavailable.LANGUAGE_MISSING_DATA)
                TextToSpeech.LANG_NOT_SUPPORTED -> _state.value = SpeakState(unavailable = TtsUnavailable.LANGUAGE_NOT_SUPPORTED)
                else -> { tts.setSpeechRate(0.85f); tts.setPitch(1.0f); ensureOfflineVoice(); ready = true; _state.value = SpeakState(ready = true) }
            }
        }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String) = onMain(id) { s, idx -> if (s == session) _state.update { it.copy(speakingIndex = idx) } }
            override fun onDone(id: String)  = onMain(id) { s, idx -> if (s == session && idx == lastIndex) _state.update { it.copy(speakingIndex = null) } }
            override fun onStop(id: String, interrupted: Boolean) { main.post { _state.update { it.copy(speakingIndex = null) } } }
            @Deprecated("") override fun onError(id: String) = onError(id, -1)
            override fun onError(id: String, code: Int) { main.post { _state.update { it.copy(speakingIndex = null) } } }
        })
    }

    private var lastIndex = -1
    override fun speak(labels: List<String>) {
        if (!ready || labels.isEmpty()) return
        session++; lastIndex = labels.lastIndex
        labels.forEachIndexed { i, l -> tts.speak(l, if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD, null, "s${session}_c$i") }
    }
    override fun stop() { session++; tts.stop(); _state.update { it.copy(speakingIndex = null) } }

    private fun ensureOfflineVoice() {
        val v = tts.voice ?: return
        if (!v.isNetworkConnectionRequired) return                 // Voice.isNetworkConnectionRequired() — modern, correct check
        tts.voices?.firstOrNull { it.locale == v.locale && !it.isNetworkConnectionRequired && it.quality >= Voice.QUALITY_NORMAL }
            ?.let { tts.voice = it }
    }
    private inline fun onMain(id: String, crossinline b: (Int, Int) -> Unit) {
        Regex("s(\\d+)_c(\\d+)").find(id)?.let { main.post { b(it.groupValues[1].toInt(), it.groupValues[2].toInt()) } }
    }
}
```

**Offline guarantee:** no `INTERNET` permission. Synthesis is on-device from installed voice data. Network-only "enhanced" voices are filtered out via `Voice.isNetworkConnectionRequired()` (the deprecated `KEY_FEATURE_NETWORK_SYNTHESIS` feature-key is **not** the primary check; the invented `"networkTtsFeature"` constant does not exist).

**Failure UI (now owned — see §5 Settings + a banner):** when `state.unavailable != null`, the Speak button is disabled AND:
- **Teacher mode:** a one-tap banner/dialog "Speech voice not installed — Install voice data" that launches `Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)`. (Correct action string is `"android.speech.tts.engine.INSTALL_TTS_DATA"`; always reference the **named constant**, never a literal.)
- **Learner mode:** a calm non-actionable note.
- Wired into Settings "Test voice". Verify offline `ms-MY`/`en` coverage on the centre's actual tablets (manual checklist item).

**Manifest:** add package-visibility query so the engine + install intent resolve on API 30+:
```xml
<queries><intent><action android:name="android.intent.action.TTS_SERVICE"/></intent></queries>
```

**Deferred:** `onRangeStart` sub-word highlighting and single-concatenated-utterance prosody are future work; per-card highlighting (close/best-effort sync) is sufficient.

---

## 8. Two-Actor Role Model & Gating

**One enum, one holder.** `enum AppMode { LEARNER, TEACHER }` (default **LEARNER**) persisted in DataStore via `AppModeRepository`. The `AppRole` name from one source dimension is dropped. Read for the UI gate via `compositionLocalOf` (`LocalAppMode`) at the NavHost root (use `compositionLocalOf`, **not** `staticCompositionLocalOf` — value changes at runtime). The write path additionally checks the **cached in-memory StateFlow value** (defence-in-depth) and **silently no-ops** in Learner mode (never `require()`/throw).

```kotlin
private val Context.dataStore by preferencesDataStore("app_prefs")
private object Keys { val MODE = stringPreferencesKey("app_mode"); val PIN_HASH = stringPreferencesKey("pin_hash") }

class AppModeRepository @Inject constructor(@ApplicationContext ctx: Context) {
    private val ds = ctx.dataStore
    val appMode: Flow<AppMode> = ds.data.map { p ->
        runCatching { AppMode.valueOf(p[Keys.MODE] ?: "LEARNER") }.getOrDefault(AppMode.LEARNER)
    }   // collected with initial value = LEARNER everywhere (synchronous default during cold-start window)

    suspend fun hasPin(): Boolean = ds.data.first()[Keys.PIN_HASH] != null
    suspend fun setPin(pin: String) { ds.edit { it[Keys.PIN_HASH] = obfuscate(pin) } }   // salted SHA-256 = obfuscation, NOT security
    suspend fun verifyAndEnterTeacher(pin: String): Boolean {
        val ok = ds.data.first()[Keys.PIN_HASH] == obfuscate(pin)
        if (ok) ds.edit { it[Keys.MODE] = AppMode.TEACHER.name }
        return ok
    }
    suspend fun lock() = ds.edit { it[Keys.MODE] = AppMode.LEARNER.name }
}

val LocalAppMode = compositionLocalOf { AppMode.LEARNER }   // runtime-changing → compositionLocalOf
```

**Gating rules**
- In Learner mode, every mutating affordance is **not rendered** (hidden, not disabled): Settings entry, all FABs (Add Category/Card), per-item overflow Edit/Delete, long-press edit menu.
- Editor/Settings destinations are wrapped in `TeacherGuarded { }`: a `LaunchedEffect` that treats "not yet TEACHER" as redirect-to-Home (synchronous LEARNER default closes the cold-start window). These destinations are **not** exposed as deep links and have **no** exported intent filters.
- Strip add/remove/reorder/Clear/Speak remain available to the Learner (they affect only the transient strip, never persisted data).

**First-run bootstrap (resolved chicken-and-egg).** On a fresh install no PIN exists. The discreet Teacher gesture (sustained ~2s long-press on a low-affordance target, e.g. the app-bar title) checks `hasPin()`:
- `hasPin() == false` → go straight to **"Set a 4-digit parental PIN"** (no verification), then enter TEACHER. The seeded sample cards already make Learner mode usable for a demo before this.
- `hasPin() == true` → RoleGate verifies the PIN.
Re-locking is a single visible "Lock" tap.

**PIN honesty:** a salted SHA-256 of a 4-digit PIN is **obfuscation, not security** (10,000-space, DataStore is plaintext on-disk). It exists only to avoid storing the PIN in cleartext and to deter a curious child. **No recovery flow** — forgotten PIN is resolved by clearing app data only. Do not add a recovery gesture (auth creep).

---

## 9. Accessibility & UX Guardrails

- **Touch targets ≥ 64dp** (exceeds the 48dp Material minimum) for motor-control needs; ≥16dp gaps to avoid mis-taps.
- **Minimal taps:** launch → spoken sentence in **3 taps** (open category → tap card → Speak).
- **Tap is the default, robust path;** drag is an enhancement (never required). Card lift uses elevation + scale + haptic on long-press.
- **Destructive actions:** single-item removal + small clears use a **Snackbar with Undo (5s)**; category delete + clear when `items >= 4` use an `AlertDialog` confirm. Category delete dialog offers "Move cards to Uncategorized" vs "Delete cards too".
- **High contrast:** custom WCAG-AA `ColorScheme`; dynamic color disabled on learner surfaces; large type scale; light/dark/high-contrast toggle in Settings.
- **TalkBack:** every Card/Chip has `contentDescription = label`; mutating controls have stable `testTag`s.
- **Empty/loading states (uniform, owned):** model list state as `sealed { Loading; Empty; Content }` in each screen's `UiState` so Home, Library, and the strip all render consistently. **One** empty-strip string: *"Tap a card to start your sentence."* Empty Home/Library use friendly, **non-infantilizing** copy ("No cards yet") + Teacher-only CTA. Loading uses shimmer skeletons.
- **Asset licensing (shipping blocker, resolve before build):** bundle **Material Symbols (Apache-2.0)** as vector drawables for icons, plus a single simple in-house empty-state illustration. No copyright-unclear PECS symbol packs.
- **Camera path completeness (owned by Card Editor):** `RequestPermission()` for `CAMERA` with rationale + denied fallback ("Camera unavailable — choose from gallery"); declared `FileProvider` + `res/xml/file_paths.xml`; image-import failure shows a retry Snackbar and never saves a card with a broken ref (`importImage` returns `Result`). The gallery path uses `PickVisualMedia` (no permission).

---

## 10. Testing Strategy

A pragmatic pyramid: heavy JVM unit tests, a thin band of high-value Compose UI tests (runnable on JVM via **Robolectric 4.16 + JDK 21**, and on-device for the demo), and a manual usability checklist for caregiver/child evaluation.

**JVM unit tests (load-bearing):**
- Strip ordering: add/append, `move` (verify the off-by-one fix), `removeAt`, `clearWithUndo`/`undoClear`, `label` list build.
- TTS sequencing + highlight progression against a **`FakeSpeaker`** test double (records ordered utterances, simulates per-utterance completion). Tests the strip→utterance mapping and the highlight state machine — the real engine is a thin, manually-verified shell.
- Room DAOs against `Room.inMemoryDatabaseBuilder(...)` under Robolectric with `runTest`: insert/query, ordering by `position`, and **cascade delete** (requires the `@ForeignKey(onDelete = CASCADE)` declared in §4 with FK enforcement on — make it an explicit M1 deliverable, else the "no orphaned cards" property is untested).

**Compose UI tests (2 critical scenarios, `createComposeRule()` → JVM-friendly):**
- **Build a sentence + Speak:** inject `FakeSpeaker` and `startRole = LEARNER`, tap two cards, tap Speak, assert ordered utterances. (`Speaker` and initial role are **hoisted parameters / DI seams** at the composition root so tests can substitute them; v2 test APIs default to `StandardTestDispatcher` → call `waitForIdle()`.)
- **Learner safety invariant:** launch in Learner mode, `assertDoesNotExist()` for every tagged mutating control (`add-card`, `edit-card`, `delete-card`, `manage-categories`); paired Teacher test asserts they ARE present. This test drives the **single** `AppMode` source from §8.

**Notes:** keep automated UI thin (no per-screen E2E, no screenshot tooling). `truth` and `androidx.test.ext:truth` must be **explicitly versioned** (not in the Compose BOM). JVM and device runs use separate source sets — keep JVM/Robolectric Compose tests in the unit-test source set for the inner loop.

---

## 11. Milestone Roadmap

Every milestone yields a tagged, installable **debug APK** and a 60-second demo script.

| ID | Deliverable | Demo | FYP phase |
|---|---|---|---|
| **M0** | Scaffold: version catalog, Compose-compiler plugin, KSP, BOM, Hilt, **JDK 21 toolchain** all wired; one passing unit test; placeholder Home | App opens; `./gradlew test` green | Requirements / QuickDesign |
| **M1** | Room `Category`+`Card` (+`@ForeignKey CASCADE`), DAOs, canonical `StripItem`/`ImageRef`, first-run seeding (incl. starters), compile-time drawable map | Fresh install → seeded data visible; DAO + cascade tests pass | QuickDesign → Prototype |
| **M2** | Home category grid → Card Library (read-only), Room-backed, large targets | Navigate launch → category → library on device | Prototype |
| **M3** | Persistent strip in bottomBar; tap-add + drag-add in order; reorder; remove; Clear-with-undo | Build "I want + drink", reorder, clear | Prototype |
| **M4** | Offline TTS Speak via `TtsManager`/`Speaker` + per-card highlight; TTS-unavailable UI | Press Speak with **airplane mode ON** (proves offline); highlight tracks audio | Prototype → Eval |
| **M5** | Teacher CRUD: add card (gallery `PickVisualMedia` + camera `FileProvider`/permission + label, copied to internal storage), edit/delete card, create/delete category; remove throwaway debug add-affordance | Caregiver adds a photo card; it appears for the child | Prototype / Implement |
| **M6** | Single `AppMode` role state; Learner hides ALL mutating controls; first-run PIN setup + RoleGate verify + Lock; `TeacherGuarded` redirects | Learner shows no edit controls; PIN required to administer | Implement |
| **M7** | Polish: accessibility (TalkBack, ≥64dp, high-contrast), uniform empty/loading states, undo/confirm, accidental-tap robustness; fixes from M4/M5 usability sessions; `allowBackup=false` | Run manual usability checklist with a caregiver; before/after on logged issues | Eval → Refine |
