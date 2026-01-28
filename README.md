<div align="center">
  <img src="brand/logo-512.png" width="180" alt="SyncBoost Logo">
  <h1>SyncBoost</h1>
  <p>
    <b>Optimize Your Server, Stop Lag, Boost Performance, In The Most Simple Way.</b>
  </p>
  <p>
    <a href="https://modrinth.com/plugin/syncboost"><img src="https://img.shields.io/badge/Download-Modrinth-00AF5C?style=for-the-badge&logo=modrinth" alt="Modrinth"></a>
    <a href="https://github.com/SyncaraHost/SyncBoost"><img src="https://img.shields.io/badge/Source-GitHub-181717?style=for-the-badge&logo=github" alt="GitHub"></a>
    <a href="https://syncara.host/discord"><img src="https://img.shields.io/badge/Support-Discord-5865F2?style=for-the-badge&logo=discord" alt="Discord"></a>
  </p>
</div>

---

## ⚡ The Ultimate Performance Solution
**SyncBoost** is not just another "lag clear" plugin; it is a comprehensive, professional-grade performance engine. Re-engineered for maximum efficiency, SyncBoost targets the root causes of server degradation—identifying CPU bottlenecks, memory leaks, and excessive entity overhead—allowing your server to sustain high player counts without compromising gameplay quality.

### Why SyncBoost?
- **✨ Premium Experience:** A stunning, modern GUI that brings visual elegance to server management.
- **🔧 Intelligent Automation:** Dynamic algorithms that sense lag spikes and proactively adjust server load.
- **🗑️ Surgical Optimization:** Reduce AI overhead, optimize redstone, and limit entities with pinpoint accuracy.
- **📊 Deep Monitoring:** Real-time MSPT charts via interactive maps and detailed hardware benchmarks.
- **⚡ Asynchronous Core:** Built to be ultra-lightweight, ensuring the optimization process never burdens your server.

> [!IMPORTANT]
> SyncBoost is optimized for **modern Minecraft infrastructure (1.20.5 - 1.21.x)** and requires **Java 21** to leverage the latest JVM performance enhancements.

---

## 📋 Requirements

| Requirement | Version |
| :--- | :--- |
| **Java** | 21 or newer (**Required**) |
| **Minecraft** | 1.20.5 - 1.21.4+ |
| **Server Software** | Spigot, Paper, Purpur, Pufferfish, or compatible forks |

---

## 🚀 Key Modules
SyncBoost is modular by design. Enable or disable features as needed via the in-game GUI or `modules/` config files.

| Module | Impact | Description |
| :--- | :--- | :--- |
| **MobAiReducer** | 🔴 Very High | Optimizes creature pathfinding and AI behavior. Essential for farm-heavy survival servers. |
| **EntityLimiter** | 🔴 High | Enforces hard limits on entity counts per chunk to prevent mass breeding lag. |
| **LagShield** | 🔴 High | Active monitoring that triggers safety measures (e.g., pausing spawners) when TPS drops critically. |
| **ExplosionOptimizer** | 🔴 High | Efficiently handles TNT and crystal explosions to prevent physics ticks from stalling the main thread. |
| **ItemsCleaner** | 🟡 Medium | Periodically removes ground items. Features an "Abyss" system for item recovery via `/abyss`. |
| **RedstoneLimiter** | 🟡 Medium | Detects and suppresses rapid-pulsing redstone clocks that cause TPS drops. |
| **VehicleMotionReducer** | 🟡 Medium | Optimizes boat and minecart collisions, movement logic, and removes mineshaft clutter. |
| **AbilityLimiter** | 🟡 Medium | Limits rapid Trident/Elytra usage to prevent excessive chunk loading. |
| **ConsoleFilter** | 🔵 Visual | Regex-based filtering to keep your server console clean and readable. |

---

## 🛠 Commands
All commands use the `/syncboost` (or alias `/sb`) prefix.

| Command | Description |
| :--- | :--- |
| `/syncboost` | Open the main configuration GUI. |
| `/syncboost help` | Display the full list of available commands. |
| `/syncboost monitor` | View real-time server statistics (TPS, MSPT, RAM, CPU). |
| `/syncboost map` | Get a map item that displays a live performance graph. |
| `/syncboost benchmark` | Run a CPU/RAM stress test to measure server hardware performance. |
| `/syncboost free` | Force run the Java Garbage Collector (GC). |
| `/syncboost clear <type>` | Manually clear entities: `items`, `creatures`, or `projectiles`. |
| `/syncboost ping [player]` | Check average player latency or a specific player's ping. |
| `/syncboost reload` | Reload all configuration files and modules. |

**Default Permission:** `syncboost.admin`

---

## 🔗 Integrations
SyncBoost works seamlessly with popular plugins:

- [PlaceholderAPI](https://www.spigotmc.org/resources/6245/) - Placeholders for TPS, MSPT, CPU usage, and cleaner countdown.
- [Spark](https://spark.lucko.me/) - Performance profiling compatibility.
- Stacker Plugins: [WildStacker](https://bg-software.com/wildstacker/), [RoseStacker](https://www.spigotmc.org/resources/82729/), [UltimateStacker](https://songoda.com/product/16)

### PlaceholderAPI Placeholders
| Placeholder | Description |
| :--- | :--- |
| `%syncboost_tps%` | Current Ticks Per Second |
| `%syncboost_mspt%` | Current Milliseconds Per Tick |
| `%syncboost_cpuprocess%` | Process CPU Usage |
| `%syncboost_cpusystem%` | System CPU Usage |
| `%syncboost_worldcleaner%` | Countdown to next world clean |

---

## ⚙️ Configuration
Configuration is split into:
- `config.yml` - Main plugin settings (prefix, permissions, updater).
- `lang.yml` - All user-facing messages.
- `modules/*.yml` - Individual module settings.

You can edit configurations directly in-game using the GUI:
```
/syncboost
```

---

## 📦 Installation
1. **Download** the latest `SyncBoost.jar` from [Modrinth](https://modrinth.com/plugin/syncboost).
2. **Stop** your Minecraft server.
3. **Place** the jar into your `plugins/` folder.
4. **Start** the server.
5. **Configure** via `/syncboost` or edit files in `plugins/SyncBoost/`.

---

## 💰 Sponsored: Syncara Host
**Cari host stabil di wilayah Indonesia? [Syncara Host](https://syncara.host) jawabannya!~**

Mulai dari **Rp 50.000**, kalian dapat server premium yang:
- ✅ **No Oversell** (Shared performa? Gak jaman).
- ✅ **Aikar Flags Optimized** (Settingan Java tuning terbaik).
- ✅ **Lokasi Indonesia** (Ping rendah, koneksi stabil).

Visit: **[https://syncara.host](https://syncara.host)**

---

## ⭐ Credits

SyncBoost is built upon the strong foundation of **[LagFixer](https://modrinth.com/plugin/lagfixer)**.
We explicitly acknowledge and thank **[Jenya705](https://github.com/Jenya705)** for their original work and contributions to the optimization community.

> SyncBoost is a forked and modernized continuation, designed to support the latest Minecraft infrastructure while respecting the legacy of the original project.

---

## ❓ FAQ

<details>
<summary><b>Why does SyncBoost require Java 21?</b></summary>
<p>Java 21 offers significant performance improvements and is the current LTS (Long-Term Support) version. Modern Minecraft servers (1.20.5+) already expect Java 21.</p>
</details>

<details>
<summary><b>Will older Minecraft versions be supported?</b></summary>
<p>The source code for 1.16.5 - 1.20.4 exists but is currently disabled. Future builds may re-enable legacy support.</p>
</details>

<details>
<summary><b>Is SyncBoost compatible with Folia?</b></summary>
<p>Currently, SyncBoost is designed for single-threaded server software (Spigot, Paper, Purpur). Folia support is not available yet.</p>
</details>

---

<div align="center">
  <p>Made with <3 by <b>[Syncara Host](https://syncara.host)</b></p>
  <p>© 2026 Syncara Host. All rights reserved.</p>
</div>
