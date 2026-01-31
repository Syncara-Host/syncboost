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