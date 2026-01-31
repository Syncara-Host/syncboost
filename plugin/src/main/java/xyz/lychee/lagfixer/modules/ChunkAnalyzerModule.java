package xyz.lychee.lagfixer.modules;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractModule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Getter
public class ChunkAnalyzerModule extends AbstractModule implements Listener {

    // Score weights
    private double entityWeight = 1.0;
    private double creatureWeight = 1.5;
    private double tileEntityWeight = 2.0;
    private double hopperWeight = 3.0;
    private double redstoneWeight = 2.5;

    // Thresholds
    private int warningThreshold = 50;
    private int dangerThreshold = 100;
    private int criticalThreshold = 200;
    
    // Top chunks limit
    private int topChunksLimit = 10;
    
    // Auto-scan settings
    private boolean autoScanEnabled = false;
    private int autoScanInterval = 300;
    private boolean alertOnDanger = true;

    // Analysis results cache
    private final ConcurrentHashMap<String, ChunkAnalysisResult> analysisCache = new ConcurrentHashMap<>();
    private long lastAnalysisTime = 0;

    public ChunkAnalyzerModule(LagFixer plugin, ModuleManager manager) {
        super(plugin, manager, Impact.HIGH, "ChunkAnalyzer",
                new String[]{"Analyzes chunks for performance issues", "Identifies entity-heavy and tile entity-heavy chunks"},
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjdjYTI2ODVjYTM3NTMzZjkwZjFiMzU3YmUwMzQ5MzkyYzMzZTU5Nzk2YzI0OWY1MzAwNmI0ZjdkOTBlIn19fQ==");
    }

    @Override
    public void load() throws Exception {
        Bukkit.getPluginManager().registerEvents(this, this.getPlugin());
        
        // Schedule auto-scan if enabled
        if (autoScanEnabled) {
            SupportManager.getInstance().getFork().runTimer(
                    true, // async
                    this::runAnalysis,
                    autoScanInterval * 1000L, // initial delay in ms
                    autoScanInterval * 1000L, // repeat delay in ms
                    TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * Run full chunk analysis across all allowed worlds
     */
    public List<ChunkAnalysisResult> runAnalysis() {
        analysisCache.clear();
        lastAnalysisTime = System.currentTimeMillis();
        
        List<ChunkAnalysisResult> results = new ArrayList<>();
        
        for (World world : this.getAllowedWorlds()) {
            Chunk[] loadedChunks = world.getLoadedChunks();
            
            for (Chunk chunk : loadedChunks) {
                ChunkAnalysisResult result = analyzeChunk(chunk);
                String key = getChunkKey(world.getName(), chunk.getX(), chunk.getZ());
                analysisCache.put(key, result);
                results.add(result);
            }
        }
        
        // Sort by lag score descending
        results.sort((a, b) -> Double.compare(b.getLagScore(), a.getLagScore()));
        
        // Alert if danger chunks found
        if (alertOnDanger) {
            long dangerCount = results.stream()
                    .filter(r -> r.getLagScore() >= dangerThreshold)
                    .count();
            if (dangerCount > 0) {
                this.getPlugin().getLogger().warning("⚠ Found " + dangerCount + " problematic chunks! Run /sb chunks for details.");
            }
        }
        
        return results;
    }

    /**
     * Analyze a single chunk
     */
    public ChunkAnalysisResult analyzeChunk(Chunk chunk) {
        ChunkAnalysisResult result = new ChunkAnalysisResult();
        result.worldName = chunk.getWorld().getName();
        result.chunkX = chunk.getX();
        result.chunkZ = chunk.getZ();
        
        // Count entities
        Entity[] entities = chunk.getEntities();
        result.entityCount = entities.length;
        
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !(entity instanceof Player) && !(entity instanceof ArmorStand)) {
                result.creatureCount++;
            } else if (entity instanceof Item) {
                result.itemCount++;
            } else if (entity instanceof Projectile) {
                result.projectileCount++;
            } else if (entity instanceof Vehicle) {
                result.vehicleCount++;
            }
        }
        
        // Count tile entities
        BlockState[] tileEntities = chunk.getTileEntities();
        result.tileEntityCount = tileEntities.length;
        
        for (BlockState state : tileEntities) {
            if (state instanceof Hopper) {
                result.hopperCount++;
            } else if (state.getType().name().contains("FURNACE")) {
                result.furnaceCount++;
            } else if (state.getType().name().contains("CHEST")) {
                result.chestCount++;
            }
        }
        
        // Estimate redstone activity (based on redstone-related blocks)
        result.redstoneScore = countRedstoneBlocks(chunk);
        
        // Calculate lag score
        result.lagScore = calculateLagScore(result);
        
        // Set severity level
        if (result.lagScore >= criticalThreshold) {
            result.severity = Severity.CRITICAL;
        } else if (result.lagScore >= dangerThreshold) {
            result.severity = Severity.DANGER;
        } else if (result.lagScore >= warningThreshold) {
            result.severity = Severity.WARNING;
        } else {
            result.severity = Severity.NORMAL;
        }
        
        return result;
    }
    
    private int countRedstoneBlocks(Chunk chunk) {
        int count = 0;
        BlockState[] tileEntities = chunk.getTileEntities();
        
        for (BlockState state : tileEntities) {
            String typeName = state.getType().name();
            if (typeName.contains("PISTON") || 
                typeName.contains("OBSERVER") || 
                typeName.contains("COMPARATOR") ||
                typeName.contains("REPEATER") ||
                typeName.contains("DISPENSER") ||
                typeName.contains("DROPPER")) {
                count++;
            }
        }
        
        return count;
    }
    
    private double calculateLagScore(ChunkAnalysisResult result) {
        double score = 0;
        
        score += result.entityCount * entityWeight;
        score += result.creatureCount * creatureWeight;
        score += result.tileEntityCount * tileEntityWeight;
        score += result.hopperCount * hopperWeight;
        score += result.redstoneScore * redstoneWeight;
        
        return score;
    }

    /**
     * Get top N problematic chunks
     */
    public List<ChunkAnalysisResult> getTopProblematicChunks(int limit) {
        if (analysisCache.isEmpty()) {
            runAnalysis();
        }
        
        return analysisCache.values().stream()
                .sorted((a, b) -> Double.compare(b.getLagScore(), a.getLagScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get top problematic chunks with default limit
     */
    public List<ChunkAnalysisResult> getTopProblematicChunks() {
        return getTopProblematicChunks(topChunksLimit);
    }
    
    /**
     * Get analysis results for a specific world
     */
    public List<ChunkAnalysisResult> getWorldAnalysis(String worldName) {
        return analysisCache.values().stream()
                .filter(r -> r.worldName.equals(worldName))
                .sorted((a, b) -> Double.compare(b.getLagScore(), a.getLagScore()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get summary statistics
     */
    public AnalysisSummary getSummary() {
        AnalysisSummary summary = new AnalysisSummary();
        
        summary.totalChunks = analysisCache.size();
        summary.lastAnalysisTime = lastAnalysisTime;
        
        for (ChunkAnalysisResult result : analysisCache.values()) {
            summary.totalEntities += result.entityCount;
            summary.totalTileEntities += result.tileEntityCount;
            summary.totalHoppers += result.hopperCount;
            
            switch (result.severity) {
                case CRITICAL -> summary.criticalChunks++;
                case DANGER -> summary.dangerChunks++;
                case WARNING -> summary.warningChunks++;
                default -> summary.normalChunks++;
            }
        }
        
        return summary;
    }
    
    private String getChunkKey(String world, int x, int z) {
        return world + ":" + x + ":" + z;
    }

    @Override
    public boolean loadConfig() throws Exception {
        // Score weights
        this.entityWeight = this.getSection().getDouble("entity_weight", 1.0);
        this.creatureWeight = this.getSection().getDouble("creature_weight", 1.5);
        this.tileEntityWeight = this.getSection().getDouble("tile_entity_weight", 2.0);
        this.hopperWeight = this.getSection().getDouble("hopper_weight", 3.0);
        this.redstoneWeight = this.getSection().getDouble("redstone_weight", 2.5);
        
        // Thresholds
        this.warningThreshold = this.getSection().getInt("warning_threshold", 50);
        this.dangerThreshold = this.getSection().getInt("danger_threshold", 100);
        this.criticalThreshold = this.getSection().getInt("critical_threshold", 200);
        
        // Top chunks
        this.topChunksLimit = this.getSection().getInt("top_chunks_limit", 10);
        
        // Auto-scan
        this.autoScanEnabled = this.getSection().getBoolean("auto_scan.enabled", false);
        this.autoScanInterval = this.getSection().getInt("auto_scan.interval", 300);
        this.alertOnDanger = this.getSection().getBoolean("auto_scan.alert_on_danger", true);
        
        return true;
    }

    @Override
    public void disable() throws Exception {
        HandlerList.unregisterAll(this);
        analysisCache.clear();
    }

    // ==================== Data Classes ====================
    
    @Getter
    public static class ChunkAnalysisResult {
        private String worldName;
        private int chunkX;
        private int chunkZ;
        
        // Entity counts
        private int entityCount;
        private int creatureCount;
        private int itemCount;
        private int projectileCount;
        private int vehicleCount;
        
        // Tile entity counts
        private int tileEntityCount;
        private int hopperCount;
        private int furnaceCount;
        private int chestCount;
        
        // Redstone
        private int redstoneScore;
        
        // Score & severity
        private double lagScore;
        private Severity severity = Severity.NORMAL;
        
        public String getCoordinates() {
            return worldName + " @ [" + (chunkX * 16) + ", " + (chunkZ * 16) + "]";
        }
        
        public String getChunkCoords() {
            return "[" + chunkX + ", " + chunkZ + "]";
        }
    }
    
    @Getter 
    public static class AnalysisSummary {
        private int totalChunks;
        private int totalEntities;
        private int totalTileEntities;
        private int totalHoppers;
        
        private int normalChunks;
        private int warningChunks;
        private int dangerChunks;
        private int criticalChunks;
        
        private long lastAnalysisTime;
    }
    
    public enum Severity {
        NORMAL("§a", "Normal"),
        WARNING("§e", "Warning"),
        DANGER("§c", "Danger"),
        CRITICAL("§4", "Critical");
        
        @Getter
        private final String color;
        @Getter
        private final String label;
        
        Severity(String color, String label) {
            this.color = color;
            this.label = label;
        }
    }
}
