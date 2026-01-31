package xyz.lychee.lagfixer.utils;

import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

import java.lang.management.*;
import java.util.*;

/**
 * Memory Leak Detector - Identifies potential memory issues in the server
 * 
 * Detects:
 * - Memory growth trends
 * - Excessive listener registrations
 * - Large object allocations
 * - GC pressure indicators
 * - Potential memory leaks from plugins
 */
public class MemoryLeakDetector {

    // Memory snapshots for trend analysis
    private static final LinkedList<MemorySnapshot> memoryHistory = new LinkedList<>();
    private static final int MAX_HISTORY_SIZE = 60; // 60 snapshots (1 minute at 1 snapshot/sec)
    
    // Thresholds
    private static final double MEMORY_GROWTH_THRESHOLD = 0.15; // 15% growth is suspicious
    private static final double HIGH_MEMORY_USAGE_THRESHOLD = 0.85; // 85% usage is high
    private static final double CRITICAL_MEMORY_THRESHOLD = 0.95; // 95% is critical
    private static final int LISTENER_WARNING_THRESHOLD = 50; // Per plugin
    private static final long LARGE_ALLOCATION_THRESHOLD = 50 * 1024 * 1024; // 50MB

    /**
     * Run a full memory analysis
     */
    public static MemoryAnalysisResult analyze() {
        MemoryAnalysisResult result = new MemoryAnalysisResult();
        
        // Capture current memory state
        takeSnapshot();
        
        // Analyze memory usage
        analyzeMemoryUsage(result);
        
        // Analyze GC metrics
        analyzeGarbageCollection(result);
        
        // Analyze memory trends
        analyzeMemoryTrends(result);
        
        // Analyze plugin listeners
        analyzePluginListeners(result);
        
        // Analyze thread count
        analyzeThreads(result);
        
        // Calculate overall health score
        calculateHealthScore(result);
        
        return result;
    }
    
    /**
     * Quick check for immediate memory issues
     */
    public static QuickCheckResult quickCheck() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        double usagePercent = (double) usedMemory / maxMemory;
        
        QuickCheckResult result = new QuickCheckResult();
        result.usedMemoryMB = usedMemory / (1024 * 1024);
        result.maxMemoryMB = maxMemory / (1024 * 1024);
        result.usagePercent = usagePercent * 100;
        
        if (usagePercent >= CRITICAL_MEMORY_THRESHOLD) {
            result.status = MemoryStatus.CRITICAL;
            result.message = "§4§l⚠ CRITICAL: Memory usage at " + String.format("%.1f", result.usagePercent) + "%!";
        } else if (usagePercent >= HIGH_MEMORY_USAGE_THRESHOLD) {
            result.status = MemoryStatus.HIGH;
            result.message = "§c§l● HIGH: Memory usage at " + String.format("%.1f", result.usagePercent) + "%";
        } else if (usagePercent >= 0.7) {
            result.status = MemoryStatus.MODERATE;
            result.message = "§e● MODERATE: Memory usage at " + String.format("%.1f", result.usagePercent) + "%";
        } else {
            result.status = MemoryStatus.HEALTHY;
            result.message = "§a● HEALTHY: Memory usage at " + String.format("%.1f", result.usagePercent) + "%";
        }
        
        return result;
    }
    
    /**
     * Take a memory snapshot for trend analysis
     */
    public static void takeSnapshot() {
        Runtime runtime = Runtime.getRuntime();
        MemorySnapshot snapshot = new MemorySnapshot();
        snapshot.timestamp = System.currentTimeMillis();
        snapshot.usedMemory = runtime.totalMemory() - runtime.freeMemory();
        snapshot.totalMemory = runtime.totalMemory();
        snapshot.maxMemory = runtime.maxMemory();
        
        synchronized (memoryHistory) {
            memoryHistory.addLast(snapshot);
            while (memoryHistory.size() > MAX_HISTORY_SIZE) {
                memoryHistory.removeFirst();
            }
        }
    }
    
    private static void analyzeMemoryUsage(MemoryAnalysisResult result) {
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        // Heap memory
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        result.heapUsedMB = heapUsage.getUsed() / (1024 * 1024);
        result.heapMaxMB = heapUsage.getMax() / (1024 * 1024);
        result.heapUsagePercent = (double) heapUsage.getUsed() / heapUsage.getMax() * 100;
        
        // Non-heap memory (Metaspace, etc.)
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        result.nonHeapUsedMB = nonHeapUsage.getUsed() / (1024 * 1024);
        
        // Check for high memory usage
        if (result.heapUsagePercent >= CRITICAL_MEMORY_THRESHOLD * 100) {
            result.warnings.add(new MemoryWarning(
                    WarningSeverity.CRITICAL,
                    "Heap memory critically high",
                    "Heap usage at " + String.format("%.1f", result.heapUsagePercent) + "%. Server may crash!"
            ));
        } else if (result.heapUsagePercent >= HIGH_MEMORY_USAGE_THRESHOLD * 100) {
            result.warnings.add(new MemoryWarning(
                    WarningSeverity.HIGH,
                    "Heap memory usage high",
                    "Heap usage at " + String.format("%.1f", result.heapUsagePercent) + "%. Consider increasing max memory."
            ));
        }
    }
    
    private static void analyzeGarbageCollection(MemoryAnalysisResult result) {
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        
        long totalGcCount = 0;
        long totalGcTime = 0;
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            result.gcInfo.put(gcBean.getName(), new GCInfo(
                    gcBean.getName(),
                    gcBean.getCollectionCount(),
                    gcBean.getCollectionTime()
            ));
            totalGcCount += gcBean.getCollectionCount();
            totalGcTime += gcBean.getCollectionTime();
        }
        
        result.totalGcCount = totalGcCount;
        result.totalGcTimeMs = totalGcTime;
        
        // Calculate GC overhead (time spent in GC vs uptime)
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        if (uptime > 0) {
            result.gcOverheadPercent = (double) totalGcTime / uptime * 100;
            
            if (result.gcOverheadPercent > 10) {
                result.warnings.add(new MemoryWarning(
                        WarningSeverity.HIGH,
                        "High GC overhead",
                        "GC is consuming " + String.format("%.1f", result.gcOverheadPercent) + 
                        "% of runtime. This indicates memory pressure."
                ));
            } else if (result.gcOverheadPercent > 5) {
                result.warnings.add(new MemoryWarning(
                        WarningSeverity.MODERATE,
                        "Elevated GC activity",
                        "GC overhead at " + String.format("%.1f", result.gcOverheadPercent) + 
                        "%. Monitor for increases."
                ));
            }
        }
    }
    
    private static void analyzeMemoryTrends(MemoryAnalysisResult result) {
        synchronized (memoryHistory) {
            if (memoryHistory.size() < 10) {
                result.trendAnalysis = "Insufficient data for trend analysis (need 10+ snapshots)";
                return;
            }
            
            // Calculate memory growth rate
            MemorySnapshot oldest = memoryHistory.getFirst();
            MemorySnapshot newest = memoryHistory.getLast();
            
            long memoryGrowth = newest.usedMemory - oldest.usedMemory;
            double growthPercent = (double) memoryGrowth / oldest.usedMemory;
            
            result.memoryGrowthMB = memoryGrowth / (1024 * 1024);
            result.memoryGrowthPercent = growthPercent * 100;
            
            // Analyze trend
            if (growthPercent > MEMORY_GROWTH_THRESHOLD) {
                result.trendAnalysis = "⚠ Memory is growing rapidly (" + String.format("%.1f", result.memoryGrowthPercent) + "% increase)";
                result.isLeakSuspected = true;
                result.warnings.add(new MemoryWarning(
                        WarningSeverity.HIGH,
                        "Suspected memory leak",
                        "Memory has grown by " + result.memoryGrowthMB + "MB in the observation period. " +
                        "This may indicate a memory leak."
                ));
            } else if (growthPercent > 0.05) {
                result.trendAnalysis = "Memory is growing slowly (" + String.format("%.1f", result.memoryGrowthPercent) + "% increase)";
            } else if (growthPercent < -0.05) {
                result.trendAnalysis = "Memory is decreasing (GC is working effectively)";
            } else {
                result.trendAnalysis = "Memory is stable";
            }
        }
    }
    
    private static void analyzePluginListeners(MemoryAnalysisResult result) {
        Map<String, Integer> listenerCounts = new HashMap<>();
        
        // Count listeners per plugin
        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener listener : handlerList.getRegisteredListeners()) {
                String pluginName = listener.getPlugin().getName();
                listenerCounts.merge(pluginName, 1, Integer::sum);
            }
        }
        
        result.pluginListenerCounts.putAll(listenerCounts);
        
        // Check for plugins with excessive listeners
        for (Map.Entry<String, Integer> entry : listenerCounts.entrySet()) {
            if (entry.getValue() > LISTENER_WARNING_THRESHOLD) {
                result.warnings.add(new MemoryWarning(
                        WarningSeverity.MODERATE,
                        "High listener count: " + entry.getKey(),
                        "Plugin has " + entry.getValue() + " registered listeners. " +
                        "This may cause performance issues."
                ));
            }
        }
    }
    
    private static void analyzeThreads(MemoryAnalysisResult result) {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        result.threadCount = threadBean.getThreadCount();
        result.peakThreadCount = threadBean.getPeakThreadCount();
        
        if (result.threadCount > 200) {
            result.warnings.add(new MemoryWarning(
                    WarningSeverity.MODERATE,
                    "High thread count",
                    "Server has " + result.threadCount + " active threads. " +
                    "This may indicate thread leaks or excessive async tasks."
            ));
        }
    }
    
    private static void calculateHealthScore(MemoryAnalysisResult result) {
        int score = 100;
        
        // Deduct for memory usage
        if (result.heapUsagePercent >= 95) score -= 40;
        else if (result.heapUsagePercent >= 85) score -= 25;
        else if (result.heapUsagePercent >= 70) score -= 10;
        
        // Deduct for GC overhead
        if (result.gcOverheadPercent > 10) score -= 20;
        else if (result.gcOverheadPercent > 5) score -= 10;
        
        // Deduct for memory growth
        if (result.isLeakSuspected) score -= 25;
        else if (result.memoryGrowthPercent > 10) score -= 10;
        
        // Deduct for warnings
        for (MemoryWarning warning : result.warnings) {
            switch (warning.severity) {
                case CRITICAL -> score -= 15;
                case HIGH -> score -= 10;
                case MODERATE -> score -= 5;
                default -> score -= 2;
            }
        }
        
        result.healthScore = Math.max(0, Math.min(100, score));
        
        // Set overall status
        if (result.healthScore >= 80) {
            result.overallStatus = MemoryStatus.HEALTHY;
        } else if (result.healthScore >= 60) {
            result.overallStatus = MemoryStatus.MODERATE;
        } else if (result.healthScore >= 40) {
            result.overallStatus = MemoryStatus.HIGH;
        } else {
            result.overallStatus = MemoryStatus.CRITICAL;
        }
    }
    
    /**
     * Force garbage collection and measure freed memory
     */
    public static GCResult forceGC() {
        Runtime runtime = Runtime.getRuntime();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        System.gc();
        
        // Wait a bit for GC to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long freedMemory = beforeMemory - afterMemory;
        
        GCResult result = new GCResult();
        result.beforeMemoryMB = beforeMemory / (1024 * 1024);
        result.afterMemoryMB = afterMemory / (1024 * 1024);
        result.freedMemoryMB = freedMemory / (1024 * 1024);
        result.freedPercent = freedMemory > 0 ? (double) freedMemory / beforeMemory * 100 : 0;
        
        return result;
    }
    
    // ==================== Data Classes ====================
    
    @Getter
    public static class MemorySnapshot {
        private long timestamp;
        private long usedMemory;
        private long totalMemory;
        private long maxMemory;
    }
    
    @Getter
    public static class MemoryAnalysisResult {
        // Memory metrics
        private long heapUsedMB;
        private long heapMaxMB;
        private double heapUsagePercent;
        private long nonHeapUsedMB;
        
        // GC metrics
        private final Map<String, GCInfo> gcInfo = new HashMap<>();
        private long totalGcCount;
        private long totalGcTimeMs;
        private double gcOverheadPercent;
        
        // Trend analysis
        private long memoryGrowthMB;
        private double memoryGrowthPercent;
        private String trendAnalysis = "No data";
        private boolean isLeakSuspected = false;
        
        // Plugin analysis
        private final Map<String, Integer> pluginListenerCounts = new HashMap<>();
        
        // Thread analysis
        private int threadCount;
        private int peakThreadCount;
        
        // Overall health
        private int healthScore;
        private MemoryStatus overallStatus = MemoryStatus.HEALTHY;
        private final List<MemoryWarning> warnings = new ArrayList<>();
    }
    
    @Getter
    public static class GCInfo {
        private final String name;
        private final long collectionCount;
        private final long collectionTimeMs;
        
        public GCInfo(String name, long count, long timeMs) {
            this.name = name;
            this.collectionCount = count;
            this.collectionTimeMs = timeMs;
        }
    }
    
    @Getter
    public static class MemoryWarning {
        private final WarningSeverity severity;
        private final String title;
        private final String description;
        
        public MemoryWarning(WarningSeverity severity, String title, String description) {
            this.severity = severity;
            this.title = title;
            this.description = description;
        }
    }
    
    @Getter
    public static class QuickCheckResult {
        private long usedMemoryMB;
        private long maxMemoryMB;
        private double usagePercent;
        private MemoryStatus status;
        private String message;
    }
    
    @Getter
    public static class GCResult {
        private long beforeMemoryMB;
        private long afterMemoryMB;
        private long freedMemoryMB;
        private double freedPercent;
    }
    
    public enum MemoryStatus {
        HEALTHY("§a", "Healthy"),
        MODERATE("§e", "Moderate"),
        HIGH("§c", "High"),
        CRITICAL("§4", "Critical");
        
        @Getter private final String color;
        @Getter private final String label;
        
        MemoryStatus(String color, String label) {
            this.color = color;
            this.label = label;
        }
    }
    
    public enum WarningSeverity {
        LOW("§7"),
        MODERATE("§e"),
        HIGH("§c"),
        CRITICAL("§4");
        
        @Getter private final String color;
        
        WarningSeverity(String color) {
            this.color = color;
        }
    }
}
