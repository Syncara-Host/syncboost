package xyz.lychee.lagfixer.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.menu.MemoryMenu;
import xyz.lychee.lagfixer.utils.MemoryLeakDetector;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MemoryCommand extends CommandManager.Subcommand {

    private MemoryMenu menu;

    public MemoryCommand(CommandManager commandManager) {
        super(commandManager, "memory", "Analyze memory usage and detect potential leaks", "mem", "leak");
    }

    @Override
    public void load() {
        this.menu = new MemoryMenu(this.getCommandManager().getPlugin(), 54, "§9§l🧠 Memory Analyzer");
    }

    @Override
    public void unload() {
        this.menu = null;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        String subCommand = args.length > 0 ? args[0].toLowerCase() : "";
        
        switch (subCommand) {
            case "gc", "free" -> {
                return runGC(sender);
            }
            case "gui" -> {
                if (sender instanceof Player player) {
                    openGui(player);
                } else {
                    MessageUtils.sendMessage(true, sender, "§cGUI is only available for players!");
                }
                return true;
            }
            case "listeners" -> {
                return showListenerAnalysis(sender);
            }
            default -> {
                // Default: show full analysis for console, GUI for players
                if (sender instanceof Player player) {
                    openGui(player);
                } else {
                    return showFullAnalysis(sender);
                }
                return true;
            }
        }
    }
    
    private void openGui(Player player) {
        MessageUtils.sendMessage(true, player, "§e⏳ Analyzing memory...");
        menu.updateAnalysis();
        player.openInventory(menu.getInv());
    }
    
    private boolean runGC(CommandSender sender) {
        MessageUtils.sendMessage(true, sender, "§e⏳ Running Garbage Collection...");
        
        MemoryLeakDetector.GCResult result = MemoryLeakDetector.forceGC();
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n§9§l━━━━━━━━━━ GARBAGE COLLECTION ━━━━━━━━━━\n\n");
        sb.append(" §8│ §7Before: §f").append(result.getBeforeMemoryMB()).append(" MB\n");
        sb.append(" §8│ §7After: §f").append(result.getAfterMemoryMB()).append(" MB\n");
        sb.append(" §8│ §7Freed: §a").append(result.getFreedMemoryMB()).append(" MB");
        
        if (result.getFreedPercent() > 0) {
            sb.append(" §7(").append(String.format("%.1f", result.getFreedPercent())).append("%)");
        }
        
        sb.append("\n\n§9§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        return MessageUtils.sendMessage(false, sender, sb.toString());
    }
    
    private boolean showListenerAnalysis(CommandSender sender) {
        MemoryLeakDetector.MemoryAnalysisResult analysis = MemoryLeakDetector.analyze();
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n§9§l━━━━━━━━━━ PLUGIN LISTENERS ━━━━━━━━━━\n\n");
        
        Map<String, Integer> listeners = analysis.getPluginListenerCounts();
        if (listeners.isEmpty()) {
            sb.append(" §7No registered listeners found.\n");
        } else {
            listeners.entrySet().stream()
                    .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                    .limit(15)
                    .forEach(entry -> {
                        String color = entry.getValue() > 50 ? "§c" : entry.getValue() > 20 ? "§e" : "§a";
                        sb.append(" §8│ ").append(color).append(entry.getKey())
                                .append(" §8» §f").append(entry.getValue()).append(" listeners\n");
                    });
        }
        
        sb.append("\n§9§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        
        return MessageUtils.sendMessage(false, sender, sb.toString());
    }
    
    private boolean showFullAnalysis(CommandSender sender) {
        MessageUtils.sendMessage(true, sender, "§e⏳ Analyzing memory usage...");
        
        MemoryLeakDetector.MemoryAnalysisResult analysis = MemoryLeakDetector.analyze();
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n§9§l━━━━━━━━━━━━━ MEMORY ANALYSIS ━━━━━━━━━━━━━\n\n");
        
        // Overall Health
        String statusColor = analysis.getOverallStatus().getColor();
        String statusLabel = analysis.getOverallStatus().getLabel();
        sb.append(" §8┌─ §fOverall Health: ").append(statusColor).append("§l")
                .append(statusLabel.toUpperCase()).append(" §7(Score: ").append(analysis.getHealthScore()).append("/100)\n");
        sb.append(" §8│\n");
        
        // Heap Memory
        String heapColor = analysis.getHeapUsagePercent() >= 85 ? "§c" : 
                          analysis.getHeapUsagePercent() >= 70 ? "§e" : "§a";
        sb.append(" §8├─ §fHeap Memory\n");
        sb.append(" §8│  §7Used: ").append(heapColor).append(analysis.getHeapUsedMB())
                .append(" MB §7/ §f").append(analysis.getHeapMaxMB()).append(" MB ")
                .append(heapColor).append("(").append(String.format("%.1f", analysis.getHeapUsagePercent())).append("%)\n");
        sb.append(" §8│  §7Non-Heap: §f").append(analysis.getNonHeapUsedMB()).append(" MB\n");
        sb.append(" §8│\n");
        
        // GC Info
        sb.append(" §8├─ §fGarbage Collection\n");
        sb.append(" §8│  §7Total Collections: §f").append(analysis.getTotalGcCount()).append("\n");
        sb.append(" §8│  §7Total GC Time: §f").append(analysis.getTotalGcTimeMs()).append(" ms\n");
        String gcColor = analysis.getGcOverheadPercent() > 10 ? "§c" : 
                        analysis.getGcOverheadPercent() > 5 ? "§e" : "§a";
        sb.append(" §8│  §7GC Overhead: ").append(gcColor)
                .append(String.format("%.2f", analysis.getGcOverheadPercent())).append("%\n");
        sb.append(" §8│\n");
        
        // Trend Analysis
        sb.append(" §8├─ §fMemory Trend\n");
        sb.append(" §8│  §7").append(analysis.getTrendAnalysis()).append("\n");
        if (analysis.getMemoryGrowthMB() != 0) {
            String growthColor = analysis.getMemoryGrowthMB() > 0 ? "§c+" : "§a";
            sb.append(" §8│  §7Growth: ").append(growthColor).append(analysis.getMemoryGrowthMB()).append(" MB\n");
        }
        sb.append(" §8│\n");
        
        // Thread Info
        sb.append(" §8├─ §fThreads\n");
        String threadColor = analysis.getThreadCount() > 200 ? "§c" : "§a";
        sb.append(" §8│  §7Active: ").append(threadColor).append(analysis.getThreadCount())
                .append(" §7(Peak: §f").append(analysis.getPeakThreadCount()).append("§7)\n");
        sb.append(" §8│\n");
        
        // Warnings
        if (!analysis.getWarnings().isEmpty()) {
            sb.append(" §8└─ §fWarnings (").append(analysis.getWarnings().size()).append(")\n");
            for (MemoryLeakDetector.MemoryWarning warning : analysis.getWarnings()) {
                sb.append("    ").append(warning.getSeverity().getColor()).append("⚠ ")
                        .append(warning.getTitle()).append("\n");
                sb.append("    §7  ").append(warning.getDescription()).append("\n");
            }
        } else {
            sb.append(" §8└─ §a✓ No warnings\n");
        }
        
        sb.append("\n§9§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("§7Tip: Use §f/sb memory gc §7to force garbage collection\n");
        sb.append("§7Tip: Use §f/sb memory listeners §7to view plugin listeners\n");
        
        return MessageUtils.sendMessage(false, sender, sb.toString());
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = Arrays.asList("gc", "gui", "listeners");
            if (!args[0].isEmpty()) {
                return completions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                        .toList();
            }
            return completions;
        }
        return Collections.emptyList();
    }
    
    public MemoryMenu getMenu() {
        return menu;
    }
}
