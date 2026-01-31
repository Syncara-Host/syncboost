package xyz.lychee.lagfixer.menu;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.managers.AnimationManager;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.utils.ItemBuilder;
import xyz.lychee.lagfixer.utils.MemoryLeakDetector;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class MemoryMenu extends AbstractMenu {

    private int animationTick = 0;
    private MemoryLeakDetector.MemoryAnalysisResult cachedAnalysis;
    
    // Main cards
    private final ItemBuilder healthCard = ItemBuilder.createSkull(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTNhZDhlZTczODI0MmI1YzVkMmEzNDkzMTFlN2I1ZWE5MjI0MzQ4NWI1YzRkNTNmMjc2OGQ4MjMyNjhjIn19fQ=="
    ).setName("§9§l❤ Memory Health");
    
    private final ItemBuilder heapCard = ItemBuilder.createSkull(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFiYzJiY2ZiMmJkMzc1OWU2YjFlODZmYzdiZmQ1OTQ3NWMwMjZlZDFkZGRmMTNhNTllMzQ3YjVlMTIifX19"
    ).setName("§9§l📊 Heap Memory");
    
    private final ItemBuilder gcCard = ItemBuilder.createSkull(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19"
    ).setName("§9§l🗑 Garbage Collection");
    
    private final ItemBuilder trendCard = ItemBuilder.createSkull(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkMWFiYTczZjYzOWY0YmM0MmJkNDgxOTZjNzE1MTk3YmUyNzEyYzNiOTYyYzk3ZWJmOWU5ZWQ4ZWZhMDI1In19fQ=="
    ).setName("§9§l📈 Memory Trend");
    
    private final ItemBuilder threadsCard = ItemBuilder.createSkull(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNThmZTI1MWE0MGU0MTY3ZDM1Y2Q3NWM4YjkxNGI0ZDk0YThjOTQ4ZTRkODFiYTBiM2NhMzUzMjc5ZjkzNTkifX19"
    ).setName("§9§l🧵 Threads");
    
    private final ItemBuilder warningsCard = ItemBuilder.createSkull(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmIwZjZlOGFmNDZhYzZmYWY4ODkxNDE5MWFiNjZmMjYxZDY3MjZhNzk5OWM2MzdjZjJlNDE1OWZlMWZjNDc3In19fQ=="
    ).setName("§c§l⚠ Warnings");
    
    // Action buttons
    private final ItemBuilder refreshButton = ItemBuilder.createSkull(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjRiMjI0MDQxNjJjNTBmYWQyNmFlMTBkNDdhMTBmMzFmOGViZmE5NDk5YjhkMWI0MzgzYjhjMjA5NjhkMzA2NyJ9fX0="
    ).setName("§a§l🔄 Refresh Analysis");
    
    private final ItemBuilder gcButton = ItemBuilder.createSkull(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZjlhYTE0ZTg4NDc3M2VhYzEzNGE0ZWU4OTcyMDYzZjQ2NmRlNjc4MzYzY2Y3YjFhMjFhODViNyJ9fX0="
    ).setName("§e§l🗑 Force GC");

    public MemoryMenu(LagFixer plugin, int size, String title) {
        super(plugin, size, title, 2, true);
        createLayout();
    }

    private void createLayout() {
        createAnimatedBorder();
        fillGlassmorphism();
        
        // Action buttons at bottom
        this.getInv().setItem(48, refreshButton.setLore(
                "",
                " §8» §7Click to refresh analysis",
                ""
        ).build());
        
        this.getInv().setItem(50, gcButton.setLore(
                "",
                " §8» §7Force run garbage collection",
                " §8» §7May free up unused memory",
                ""
        ).build());
        
        // Back button
        ItemStack backButton = new ItemBuilder(new ItemStack(Material.ARROW))
                .setName("§c§l← Back")
                .setLore("", " §7Return to main menu", "")
                .build();
        this.getInv().setItem(45, backButton);
    }

    private void createAnimatedBorder() {
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        Material borderMaterial = Material.PURPLE_STAINED_GLASS_PANE;

        for (int slot : borderSlots) {
            ItemStack pane = new ItemStack(borderMaterial);
            new ItemBuilder(pane).setName(" ");
            this.getInv().setItem(slot, pane);
        }
    }

    private void fillGlassmorphism() {
        Material lightGlass = Material.MAGENTA_STAINED_GLASS_PANE;
        int[] decorSlots = {10, 16, 19, 25, 28, 34, 37, 43};
        
        for (int slot : decorSlots) {
            ItemStack pane = new ItemStack(lightGlass);
            new ItemBuilder(pane).setName(" ");
            this.getInv().setItem(slot, pane);
        }
    }

    public void updateAnalysis() {
        cachedAnalysis = MemoryLeakDetector.analyze();
    }

    @Override
    public void update() {
        animationTick++;
        
        if (cachedAnalysis == null) {
            updateAnalysis();
        }
        
        updateAnimatedBorder(animationTick);
        
        // ===== HEALTH CARD =====
        String statusColor = cachedAnalysis.getOverallStatus().getColor();
        String statusLabel = cachedAnalysis.getOverallStatus().getLabel();
        int score = cachedAnalysis.getHealthScore();
        
        healthCard.setLore(
                "",
                " §8┌─ §fOverall Status",
                " §8│ " + statusColor + "§l" + statusLabel.toUpperCase(),
                " §8│",
                " §8├─ §fHealth Score",
                " §8│  " + getScoreBar(score) + " §f" + score + "%",
                " §8│",
                " §8└─ §7" + cachedAnalysis.getWarnings().size() + " warning(s)",
                ""
        );
        this.getInv().setItem(11, healthCard.build());
        
        // ===== HEAP CARD =====
        String heapColor = cachedAnalysis.getHeapUsagePercent() >= 85 ? "§c" : 
                          cachedAnalysis.getHeapUsagePercent() >= 70 ? "§e" : "§a";
        
        heapCard.setLore(
                "",
                " §8┌─ §fHeap Memory",
                " §8│ " + heapColor + "§l" + cachedAnalysis.getHeapUsedMB() + " MB §8/ §7" + cachedAnalysis.getHeapMaxMB() + " MB",
                " §8│",
                " §8├─ §fUsage",
                " §8│  " + createProgressBar(cachedAnalysis.getHeapUsagePercent(), 100, 12),
                " §8│  " + heapColor + String.format("%.1f", cachedAnalysis.getHeapUsagePercent()) + "%",
                " §8│",
                " §8└─ §fNon-Heap: §7" + cachedAnalysis.getNonHeapUsedMB() + " MB",
                ""
        );
        this.getInv().setItem(13, heapCard.build());
        
        // ===== GC CARD =====
        String gcColor = cachedAnalysis.getGcOverheadPercent() > 10 ? "§c" : 
                        cachedAnalysis.getGcOverheadPercent() > 5 ? "§e" : "§a";
        
        gcCard.setLore(
                "",
                " §8┌─ §fGarbage Collection",
                " §8│ §7Collections: §f" + cachedAnalysis.getTotalGcCount(),
                " §8│ §7Total Time: §f" + cachedAnalysis.getTotalGcTimeMs() + " ms",
                " §8│",
                " §8├─ §fGC Overhead",
                " §8│  " + gcColor + "§l" + String.format("%.2f", cachedAnalysis.getGcOverheadPercent()) + "%",
                " §8│",
                " §8└─ §7" + (cachedAnalysis.getGcOverheadPercent() < 5 ? "§aHealthy" : "§cHigh pressure"),
                ""
        );
        this.getInv().setItem(15, gcCard.build());
        
        // ===== TREND CARD =====
        String trendColor = cachedAnalysis.isLeakSuspected() ? "§c" : 
                           cachedAnalysis.getMemoryGrowthPercent() > 5 ? "§e" : "§a";
        String trendIcon = cachedAnalysis.getMemoryGrowthMB() > 0 ? "↑" : 
                          cachedAnalysis.getMemoryGrowthMB() < 0 ? "↓" : "→";
        
        trendCard.setLore(
                "",
                " §8┌─ §fMemory Trend",
                " §8│ " + trendColor + trendIcon + " §7" + cachedAnalysis.getTrendAnalysis(),
                " §8│",
                " §8├─ §fGrowth",
                " §8│  " + trendColor + (cachedAnalysis.getMemoryGrowthMB() > 0 ? "+" : "") + 
                        cachedAnalysis.getMemoryGrowthMB() + " MB",
                " §8│  " + trendColor + String.format("%.1f", cachedAnalysis.getMemoryGrowthPercent()) + "%",
                " §8│",
                " §8└─ " + (cachedAnalysis.isLeakSuspected() ? "§c⚠ Leak suspected!" : "§a✓ No leaks detected"),
                ""
        );
        this.getInv().setItem(29, trendCard.build());
        
        // ===== THREADS CARD =====
        String threadColor = cachedAnalysis.getThreadCount() > 200 ? "§c" : 
                            cachedAnalysis.getThreadCount() > 100 ? "§e" : "§a";
        
        threadsCard.setLore(
                "",
                " §8┌─ §fThread Count",
                " §8│ §7Active: " + threadColor + "§l" + cachedAnalysis.getThreadCount(),
                " §8│ §7Peak: §f" + cachedAnalysis.getPeakThreadCount(),
                " §8│",
                " §8└─ §7" + (cachedAnalysis.getThreadCount() > 200 ? "§cHigh thread count!" : "§aNormal"),
                ""
        );
        this.getInv().setItem(31, threadsCard.build());
        
        // ===== WARNINGS CARD =====
        List<String> warningLore = new ArrayList<>();
        warningLore.add("");
        warningLore.add(" §8┌─ §fActive Warnings: §c" + cachedAnalysis.getWarnings().size());
        warningLore.add(" §8│");
        
        if (cachedAnalysis.getWarnings().isEmpty()) {
            warningLore.add(" §8└─ §a✓ No warnings!");
        } else {
            int count = 0;
            for (MemoryLeakDetector.MemoryWarning warning : cachedAnalysis.getWarnings()) {
                if (count >= 5) {
                    warningLore.add(" §8│  §7...and " + (cachedAnalysis.getWarnings().size() - 5) + " more");
                    break;
                }
                warningLore.add(" §8│ " + warning.getSeverity().getColor() + "⚠ " + warning.getTitle());
                count++;
            }
            warningLore.add(" §8└───────────");
        }
        warningLore.add("");
        
        warningsCard.setLore(warningLore.toArray(new String[0]));
        this.getInv().setItem(33, warningsCard.build());
    }

    private void updateAnimatedBorder(int tick) {
        Material[] rainbow = AnimationManager.getRainbowBorderMaterials(tick);
        
        int[] topBorder = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        int[] bottomBorder = {46, 47, 49, 51, 52, 53};
        
        for (int i = 0; i < topBorder.length; i++) {
            ItemStack pane = new ItemStack(rainbow[i % rainbow.length]);
            new ItemBuilder(pane).setName(" ");
            this.getInv().setItem(topBorder[i], pane);
        }
        
        for (int i = 0; i < bottomBorder.length; i++) {
            ItemStack pane = new ItemStack(rainbow[i % rainbow.length]);
            new ItemBuilder(pane).setName(" ");
            this.getInv().setItem(bottomBorder[i], pane);
        }
    }

    private String getScoreBar(int score) {
        StringBuilder bar = new StringBuilder("§8[");
        int filled = score / 10;
        
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                if (score >= 80) bar.append("§a█");
                else if (score >= 60) bar.append("§e█");
                else if (score >= 40) bar.append("§c█");
                else bar.append("§4█");
            } else {
                bar.append("§7░");
            }
        }
        
        bar.append("§8]");
        return bar.toString();
    }

    private String createProgressBar(double value, double max, int length) {
        int filled = (int) Math.min(length, (value / max) * length);
        StringBuilder bar = new StringBuilder("§8[");
        
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                if (value >= 85) bar.append("§c█");
                else if (value >= 70) bar.append("§e█");
                else bar.append("§a█");
            } else {
                bar.append("§7░");
            }
        }
        
        bar.append("§8]");
        return bar.toString();
    }

    @Override
    public void handleClick(InventoryClickEvent e, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return;
        
        HumanEntity human = e.getWhoClicked();
        int slot = e.getSlot();
        
        // Back button
        if (slot == 45) {
            human.openInventory(MenuCommand.getInstance().getMainMenu().getInv());
            return;
        }
        
        // Refresh button
        if (slot == 48) {
            if (human instanceof Player player) {
                MessageUtils.sendMessage(true, player, "§e⏳ Refreshing memory analysis...");
                updateAnalysis();
                MessageUtils.sendMessage(true, player, "§a✓ Analysis complete!");
            }
            return;
        }
        
        // GC button
        if (slot == 50) {
            if (human instanceof Player player) {
                MessageUtils.sendMessage(true, player, "§e⏳ Running Garbage Collection...");
                MemoryLeakDetector.GCResult result = MemoryLeakDetector.forceGC();
                MessageUtils.sendMessage(true, player, 
                        "§a✓ Freed §f" + result.getFreedMemoryMB() + " MB §7(" + 
                        String.format("%.1f", result.getFreedPercent()) + "%)");
                updateAnalysis();
            }
        }
    }

    @Override
    public AbstractMenu previousMenu() {
        return MenuCommand.getInstance().getMainMenu();
    }
}
