package xyz.lychee.lagfixer.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for GUI visual components and formatting
 */
public class GUIUtils {
    private static final DecimalFormat DF = new DecimalFormat("#.##");
    
    // Progress bar characters
    private static final String FILLED = "█";
    private static final String HALF_FILLED = "▓";
    private static final String EMPTY = "░";
    
    // Trend indicators
    private static final String TREND_UP = "↑";
    private static final String TREND_DOWN = "↓";
    private static final String TREND_STABLE = "→";

    /**
     * Get color based on TPS value
     * @param tps Current TPS
     * @return ChatColor for the TPS value
     */
    public static ChatColor getTPSColor(double tps) {
        if (tps >= 19.0) return ChatColor.GREEN;
        if (tps >= 17.0) return ChatColor.YELLOW;
        if (tps >= 15.0) return ChatColor.GOLD;
        if (tps >= 10.0) return ChatColor.RED;
        return ChatColor.DARK_RED;
    }

    /**
     * Get color based on MSPT value
     * @param mspt Current MSPT
     * @return ChatColor for the MSPT value
     */
    public static ChatColor getMSPTColor(double mspt) {
        if (mspt <= 40.0) return ChatColor.GREEN;
        if (mspt <= 45.0) return ChatColor.YELLOW;
        if (mspt <= 50.0) return ChatColor.GOLD;
        if (mspt <= 60.0) return ChatColor.RED;
        return ChatColor.DARK_RED;
    }

    /**
     * Get color based on memory percentage
     * @param percentage Memory usage percentage (0-100)
     * @return ChatColor for the memory usage
     */
    public static ChatColor getMemoryColor(double percentage) {
        if (percentage <= 60.0) return ChatColor.GREEN;
        if (percentage <= 75.0) return ChatColor.YELLOW;
        if (percentage <= 85.0) return ChatColor.GOLD;
        if (percentage <= 95.0) return ChatColor.RED;
        return ChatColor.DARK_RED;
    }

    /**
     * Create a progress bar visualization
     * @param current Current value
     * @param max Maximum value
     * @param bars Number of bars to display
     * @return Formatted progress bar string
     */
    public static String createProgressBar(double current, double max, int bars) {
        if (max <= 0) return EMPTY.repeat(bars);
        
        double percentage = (current / max) * 100.0;
        int filledBars = (int) Math.round((percentage / 100.0) * bars);
        int emptyBars = bars - filledBars;
        
        ChatColor color = getMemoryColor(percentage);
        return color + FILLED.repeat(Math.max(0, filledBars)) + 
               ChatColor.DARK_GRAY + EMPTY.repeat(Math.max(0, emptyBars));
    }

    /**
     * Create a percentage-based progress bar
     * @param percentage Percentage value (0-100)
     * @param length Length of the bar
     * @return Formatted progress bar string
     */
    public static String createPercentageBar(double percentage, int length) {
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;
        
        int filled = (int) Math.round((percentage / 100.0) * length);
        int halfFilled = 0;
        int empty = length - filled - halfFilled;
        
        ChatColor color = getMemoryColor(percentage);
        StringBuilder bar = new StringBuilder();
        
        if (filled > 0) bar.append(color).append(FILLED.repeat(filled));
        if (halfFilled > 0) bar.append(ChatColor.GRAY).append(HALF_FILLED.repeat(halfFilled));
        if (empty > 0) bar.append(ChatColor.DARK_GRAY).append(EMPTY.repeat(empty));
        
        return bar.toString();
    }

    /**
     * Get animated item based on tick count
     * @param tickCount Current tick count
     * @param materials Array of materials to cycle through
     * @return ItemStack for current animation frame
     */
    public static ItemStack getAnimatedItem(int tickCount, Material... materials) {
        if (materials == null || materials.length == 0) {
            return new ItemStack(Material.BARRIER);
        }
        int index = (tickCount / 10) % materials.length;
        return new ItemStack(materials[index]);
    }

    /**
     * Get trend indicator based on current and previous values
     * @param current Current value
     * @param previous Previous value
     * @return Trend symbol with color
     */
    public static String getTrendIndicator(double current, double previous) {
        double diff = current - previous;
        double threshold = 0.1; // Minimum change to show trend
        
        if (Math.abs(diff) < threshold) {
            return ChatColor.GRAY + TREND_STABLE;
        } else if (diff > 0) {
            return ChatColor.GREEN + TREND_UP;
        } else {
            return ChatColor.RED + TREND_DOWN;
        }
    }

    /**
     * Format stat line with consistent spacing
     * @param key Stat name
     * @param value Stat value
     * @param color Value color
     * @return Formatted stat line
     */
    public static String formatStatLine(String key, String value, ChatColor color) {
        return " §8» §7" + key + ": " + color + value;
    }

    /**
     * Format stat line with automatic color based on value type
     * @param key Stat name
     * @param value Stat value
     * @return Formatted stat line
     */
    public static String formatStatLine(String key, String value) {
        return formatStatLine(key, value, ChatColor.AQUA);
    }

    /**
     * Add separator line to lore
     * @return Separator line list
     */
    public static List<String> addSeparatorLine() {
        List<String> lines = new ArrayList<>();
        lines.add("");
        return lines;
    }

    /**
     * Format number with decimal places
     * @param value Number to format
     * @return Formatted string
     */
    public static String formatNumber(double value) {
        return DF.format(value);
    }

    /**
     * Get performance emoji based on TPS
     * @param tps Current TPS
     * @return Performance indicator symbol
     */
    public static String getPerformanceIndicator(double tps) {
        if (tps >= 19.0) return "●"; // Full circle - excellent
        if (tps >= 17.0) return "◐"; // Half circle - good
        if (tps >= 15.0) return "◑"; // Half circle inverse - fair
        if (tps >= 10.0) return "◌"; // Empty circle - poor
        return "✗"; // X - critical
    }

    /**
     * Get status symbol
     * @param enabled Whether feature is enabled
     * @return Status symbol
     */
    public static String getStatusSymbol(boolean enabled) {
        return enabled ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
    }

    /**
     * Create multi-line lore with automatic wrapping
     * @param text Text to wrap
     * @param maxWidth Maximum characters per line
     * @param prefix Prefix for each line
     * @return List of wrapped lines
     */
    public static List<String> wrapText(String text, int maxWidth, String prefix) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder(prefix);
        
        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxWidth) {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(prefix).append(word).append(" ");
            } else {
                currentLine.append(word).append(" ");
            }
        }
        
        if (currentLine.length() > prefix.length()) {
            lines.add(currentLine.toString().trim());
        }
        
        return lines;
    }

    /**
     * Format memory value to human-readable format
     * @param bytes Bytes value
     * @return Formatted string (MB)
     */
    public static String formatMemory(long bytes) {
        double mb = bytes / (1024.0 * 1024.0);
        return formatNumber(mb) + " MB";
    }

    /**
     * Get material based on performance level
     * @param tps Current TPS
     * @return Material for indicator
     */
    public static Material getPerformanceMaterial(double tps) {
        if (tps >= 19.0) return Material.LIME_STAINED_GLASS_PANE;
        if (tps >= 17.0) return Material.YELLOW_STAINED_GLASS_PANE;
        if (tps >= 15.0) return Material.ORANGE_STAINED_GLASS_PANE;
        if (tps >= 10.0) return Material.RED_STAINED_GLASS_PANE;
        return Material.BLACK_STAINED_GLASS_PANE;
    }
}
