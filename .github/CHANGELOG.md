## 1.2.3 - Customizable Messages & PlaceholderAPI Expansion

### ✨ New Features

#### 🎨 Customizable Message Prefix
- **NEW**: All message prefixes can now be fully customized
- Use `<prefix>` placeholder in `lang.yml` to reference the main prefix from `config.yml`
- Change `main.prefix` in config once and all messages update automatically
- Updated all default messages in `lang.yml` to use the new `<prefix>` placeholder

#### 📊 Expanded PlaceholderAPI Support
- **NEW**: 40+ new placeholders available for use in scoreboards, holograms, etc.
- PlaceholderAPI identifier changed from `lagfixer` to `syncboost`

##### Performance Metrics
- `%syncboost_tps%` - TPS (formatted to 2 decimals)
- `%syncboost_tps_raw%` - TPS (raw value)
- `%syncboost_tps_color%` - TPS with automatic color coding (&a/&e/&c)
- `%syncboost_mspt%` - Milliseconds per tick
- `%syncboost_mspt_color%` - MSPT with color coding
- `%syncboost_cpu%` - CPU process usage
- `%syncboost_cpusystem%` - CPU system usage

##### Entity Counts
- `%syncboost_entities%` - Total entities
- `%syncboost_mobs%` - Total mobs/creatures
- `%syncboost_items%` - Ground items
- `%syncboost_projectiles%` - Projectiles
- `%syncboost_vehicles%` - Vehicles

##### Memory Statistics
- `%syncboost_memory_used%` - RAM used (e.g., "2048MB")
- `%syncboost_memory_max%` - Max RAM
- `%syncboost_memory_free%` - Free RAM
- `%syncboost_memory_percent%` - RAM usage percentage
- `%syncboost_memory_bar%` - Visual progress bar for RAM
- `%syncboost_memory_used_gb%` / `%syncboost_memory_max_gb%` - RAM in GB

##### Server Statistics
- `%syncboost_players%` - Online player count
- `%syncboost_players_max%` - Max players
- `%syncboost_worlds%` - Number of worlds
- `%syncboost_chunks%` - Total loaded chunks
- `%syncboost_uptime%` - Server uptime (HH:MM:SS format)
- `%syncboost_uptime_hours%` / `%syncboost_uptime_minutes%`

##### Clearlag Timer
- `%syncboost_clearlag_timer%` - Time until next clear (e.g., "45s")
- `%syncboost_clearlag_seconds%` - Raw seconds remaining
- `%syncboost_clearlag_formatted%` - Formatted time (MM:SS)
- `%syncboost_clearlag_interval%` - Configured interval
- `%syncboost_clearlag_enabled%` - Module status (true/false)
- `%syncboost_clearlag_progress%` - Progress percentage
- `%syncboost_clearlag_bar%` - Visual progress bar

### 📝 Technical Changes

- Modified `Language.java` to support `<prefix>` tag resolver
- Refactored `PlaceholderAPIHook.java` with comprehensive placeholder support
- Updated `lang.yml` with `<prefix>` placeholder documentation and usage

---

## 1.2.2 - Resource Health, Chunk Analyzer & Memory Diagnostics Update
### ✨ New Features

#### 🔍 Overselling Detection System
- **NEW**: Automatic detection when your hosting provider may be overselling resources
- Warns on plugin startup if resource issues detected
- Full analysis after running `/syncboost benchmark`
- Visual indicator in Main Menu Performance Hub card
- Detection includes:
  - CPU performance below expected thresholds
  - High CPU performance variance (indicates contention)
  - Memory bandwidth issues
  - RAM overselling signs

#### 📊 Chunk Analyzer Module
- **NEW**: Comprehensive chunk analysis system to identify performance bottlenecks
- Detects entity-heavy chunks, tile entity concentrations, and redstone activity
- Configurable scoring weights and severity thresholds
- Available via `/syncboost chunks` command or through GUI
- Features include:
  - Real-time chunk scanning across all worlds
  - Lag score calculation based on entities, tile entities, and redstone
  - Severity levels: Normal, Warning, Danger, Critical
  - Top 10 problematic chunks display
  - Click-to-teleport functionality in GUI
  - Detailed breakdown per chunk (mobs, items, hoppers, furnaces, etc.)

#### 🖥️ Chunk Analyzer GUI Menu
- Beautiful animated menu showing analysis results
- Color-coded chunk severity indicators
- Summary statistics at a glance
- One-click teleportation to problematic chunks
- Refresh button for real-time updates

#### 🧠 Memory Leak Detector
- **NEW**: Comprehensive memory diagnostics and leak detection system
- Available via `/syncboost memory` command or through GUI
- Features include:
  - Real-time heap memory monitoring
  - Garbage collection metrics and overhead analysis
  - Memory trend analysis with leak detection
  - Thread count monitoring
  - Plugin listener count analysis
  - Health score calculation
- Interactive GUI menu with:
  - Memory health card with overall status
  - Heap usage visualization with progress bars
  - GC overhead statistics
  - Memory trend analysis
  - Thread and warning cards
  - Force GC button with freed memory feedback

### 🐛 Bug Fixes

- **Fixed**: `ArrayIndexOutOfBoundsException` crash on menu open
  - Main Menu now correctly uses 27-slot (3-row) inventory
  - Removed invalid card placements that caused crashes
  
- **Fixed**: Module status not updating in Modules Menu
  - Modules now show real-time ENABLED/DISABLED status
  - Status updates automatically when modules are toggled

### 🎨 UI Improvements

- Revamped Main Menu with modern card-based design
- Rainbow animated borders on menu
- Health-based side indicators (green/yellow/red based on server health)
- Glassmorphism effect decorations
- Enhanced Performance Hub with:
  - TPS health indicator with color coding
  - Memory usage progress bar
  - CPU usage display
  - **NEW**: Resource health status indicator

### 🔧 New Configuration

- Added `modules/ChunkAnalyzer.yml` with:
  - Customizable score weights for entities, creatures, tile entities, hoppers, redstone
  - Adjustable warning/danger/critical thresholds
  - Auto-scan feature with configurable interval
  - Alert notifications for danger-level chunks

### 📝 Technical Changes

- Added `OversellDetector.java` utility class
- Improved `ModulesMenu.java` update() method for real-time status
- Fixed slot calculations in `MainMenu.java` for 3-row inventory
- Cleaned up unused card definitions
- Added `ChunkAnalyzerModule.java` - Core analysis engine
- Added `ChunkAnalyzerCommand.java` - `/sb chunks` command handler
- Added `ChunkAnalyzerMenu.java` - Interactive GUI menu