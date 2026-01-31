package xyz.lychee.lagfixer.utils;

import lombok.Data;
import xyz.lychee.lagfixer.commands.BenchmarkCommand;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects when hosting providers are overselling resources
 * by comparing actual performance against expected values.
 */
public class OversellDetector {
    
    // Detection thresholds
    private static final double CPU_OVERSELL_THRESHOLD = 0.50; // 50% of expected performance
    private static final double RAM_BANDWIDTH_MIN_DDR4 = 10000.0; // 10 GB/s minimum for DDR4
    private static final double RAM_BANDWIDTH_MIN_DDR3 = 5000.0; // 5 GB/s minimum for DDR3
    private static final double CPU_VARIANCE_THRESHOLD = 0.30; // 30% variance indicates instability
    
    // Cache results for 5 minutes
    private static OversellResult cachedResult = null;
    private static long lastCheck = 0;
    private static final long CACHE_DURATION = 300_000; // 5 minutes in ms
    
    /**
     * Main detection method that checks all resources
     */
    public static OversellResult detectOverselling(BenchmarkCommand.Benchmark benchmark) {
        // Check cache first
        long now = System.currentTimeMillis();
        if (cachedResult != null && (now - lastCheck) < CACHE_DURATION) {
            return cachedResult;
        }
        
        OversellResult result = new OversellResult();
        
        if (benchmark == null) {
            result.setDataAvailable(false);
            cachedResult = result;
            lastCheck = now;
            return result;
        }
        
        result.setDataAvailable(true);
        
        // Check CPU overselling
        checkCPUOverselling(benchmark, result);
        
        // Check RAM overselling
        checkRAMOverselling(benchmark, result);
        
        // Calculate overall severity
        calculateSeverity(result);
        
        cachedResult = result;
        lastCheck = now;
        
        return result;
    }
    
    /**
     * Quick check without benchmark data (uses heuristics)
     */
    public static OversellResult quickCheck() {
        AbstractMonitor monitor = SupportManager.getInstance().getMonitor();
        OversellResult result = new OversellResult();
        result.setDataAvailable(false);
        
        // Check for extremely low RAM (possible memory ballooning)
        long totalRam = monitor.getRamMax();
        long usedRam = monitor.getRamUsed();
        double usagePercent = (double) usedRam / totalRam * 100;
        
        if (totalRam < 1024 && usagePercent > 90) {
            result.addIssue("Low memory available (" + totalRam + "MB) with high usage (" + 
                String.format("%.1f%%", usagePercent) + ")");
            result.setSeverity(Severity.MEDIUM);
        }
        
        // Check TPS instability (could indicate CPU overselling)
        double tps = monitor.getTps();
        if (tps < 18.0) {
            result.addIssue("Low TPS detected (" + String.format("%.1f", tps) + ") - possible CPU overselling");
            result.setSeverity(Severity.MEDIUM);
        }
        
        return result;
    }
    
    private static void checkCPUOverselling(BenchmarkCommand.Benchmark benchmark, OversellResult result) {
        double avgScore = benchmark.getTotalScore();
        double[] scores = benchmark.getScores();
        
        // Expected minimum score for modern VPS (baseline: ~2.0 Gop/s)
        double expectedMinScore = 2.0;
        
        // Check if performance is below threshold
        if (avgScore < expectedMinScore * CPU_OVERSELL_THRESHOLD) {
            result.addIssue(String.format(
                "CPU Performance significantly below expected (%.2f Gop/s vs expected ~%.2f Gop/s)",
                avgScore, expectedMinScore
            ));
            result.setCpuOversold(true);
        }
        
        // Check for high variance (indicates CPU stealing/contention)
        if (scores.length > 1) {
            double variance = calculateVariance(scores);
            double coefficient = Math.sqrt(variance) / avgScore;
            
            if (coefficient > CPU_VARIANCE_THRESHOLD) {
                result.addIssue(String.format(
                    "High CPU performance variance detected (%.1f%% coefficient) - indicates resource contention",
                    coefficient * 100
                ));
                result.setCpuOversold(true);
            }
        }
        
        // Check best vs worst time ratio
        double bestScore = benchmark.getBestScore();
        double worstScore = benchmark.getWorstScore();
        double ratio = worstScore / bestScore;
        
        if (ratio > 2.0) {
            result.addIssue(String.format(
                "CPU performance inconsistency detected (worst/best ratio: %.2fx)",
                ratio
            ));
            result.setCpuOversold(true);
        }
    }
    
    private static void checkRAMOverselling(BenchmarkCommand.Benchmark benchmark, OversellResult result) {
        double readSpeed = benchmark.getReadSpeed();
        double writeSpeed = benchmark.getWriteSpeed();
        double randomSpeed = benchmark.getRandomSpeed();
        
        // Check sequential read/write speeds
        if (readSpeed < RAM_BANDWIDTH_MIN_DDR3) {
            result.addIssue(String.format(
                "Memory read bandwidth very low (%.2f MB/s vs expected >%.0f MB/s)",
                readSpeed, RAM_BANDWIDTH_MIN_DDR3
            ));
            result.setRamOversold(true);
        }
        
        if (writeSpeed < RAM_BANDWIDTH_MIN_DDR3) {
            result.addIssue(String.format(
                "Memory write bandwidth very low (%.2f MB/s vs expected >%.0f MB/s)",
                writeSpeed, RAM_BANDWIDTH_MIN_DDR3
            ));
            result.setRamOversold(true);
        }
        
        // Check random access (should be at least 30% of sequential for real RAM)
        double randomRatio = randomSpeed / readSpeed;
        if (randomRatio < 0.20) {
            result.addIssue(String.format(
                "Memory random access extremely slow (%.1f%% of sequential) - possible swap/disk usage",
                randomRatio * 100
            ));
            result.setRamOversold(true);
        }
    }
    
    private static void calculateSeverity(OversellResult result) {
        int issueCount = result.getIssues().size();
        boolean cpuAndRam = result.isCpuOversold() && result.isRamOversold();
        
        if (!result.isCpuOversold() && !result.isRamOversold()) {
            result.setSeverity(Severity.NONE);
        } else if (cpuAndRam || issueCount >= 4) {
            result.setSeverity(Severity.CRITICAL);
        } else if (issueCount >= 2) {
            result.setSeverity(Severity.HIGH);
        } else {
            result.setSeverity(Severity.MEDIUM);
        }
    }
    
    private static double calculateVariance(double[] values) {
        double mean = 0;
        for (double value : values) {
            mean += value;
        }
        mean /= values.length;
        
        double variance = 0;
        for (double value : values) {
            variance += Math.pow(value - mean, 2);
        }
        return variance / values.length;
    }
    
    public enum Severity {
        NONE("§a", "None"),
        LOW("§e", "Low"),
        MEDIUM("§6", "Medium"),
        HIGH("§c", "High"),
        CRITICAL("§4§l", "Critical");
        
        private final String color;
        private final String displayName;
        
        Severity(String color, String displayName) {
            this.color = color;
            this.displayName = displayName;
        }
        
        public String getColor() {
            return color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    @Data
    public static class OversellResult {
        private boolean dataAvailable = true;
        private boolean cpuOversold = false;
        private boolean ramOversold = false;
        private Severity severity = Severity.NONE;
        private List<String> issues = new ArrayList<>();
        
        public void addIssue(String issue) {
            issues.add(issue);
        }
        
        public boolean isOverselling() {
            return cpuOversold || ramOversold;
        }
        
        public String getWarningMessage() {
            if (!isOverselling()) {
                return "§a✓ No overselling detected - your resources appear healthy!";
            }
            
            StringBuilder msg = new StringBuilder();
            msg.append("\n§8§m                                                    §r\n");
            msg.append("§c§l⚠ RESOURCE OVERSELLING DETECTED\n");
            msg.append("§7Severity: ").append(severity.getColor()).append(severity.getDisplayName()).append("\n");
            msg.append("§8§m                                                    §r\n\n");
            
            if (cpuOversold) {
                msg.append("§c§lCPU Issues Detected:\n");
            }
            if (ramOversold) {
                msg.append("§c§lRAM Issues Detected:\n");
            }
            
            for (String issue : issues) {
                msg.append("  §8» §7").append(issue).append("\n");
            }
            
            msg.append("\n§e§lRecommendations:\n");
            msg.append("  §8» §7Contact your hosting provider for clarification\n");
            msg.append("  §8» §7Check resource limits in your hosting panel\n");
            msg.append("  §8» §7Consider migrating to a more reliable provider\n");
            msg.append("\n§bFor more info: §9§n/syncboost benchmark§r\n");
            msg.append("§8§m                                                    §r");
            
            return msg.toString();
        }
        
        public String getShortStatus() {
            if (!dataAvailable) {
                return "§7? Unknown (Run /syncboost benchmark)";
            }
            if (!isOverselling()) {
                return "§a✓ Healthy";
            }
            return severity.getColor() + "⚠ " + severity.getDisplayName() + " Risk";
        }
    }
}
