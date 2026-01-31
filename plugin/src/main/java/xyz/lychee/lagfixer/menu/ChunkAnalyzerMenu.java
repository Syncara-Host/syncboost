package xyz.lychee.lagfixer.menu;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.managers.AnimationManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.modules.ChunkAnalyzerModule;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.utils.ItemBuilder;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class ChunkAnalyzerMenu extends AbstractMenu {

    private int animationTick = 0;
    private List<ChunkAnalyzerModule.ChunkAnalysisResult> cachedResults = new ArrayList<>();
    
    // Summary card
    private final ItemBuilder summaryCard = ItemBuilder.createSkull(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjdjYTI2ODVjYTM3NTMzZjkwZjFiMzU3YmUwMzQ5MzkyYzMzZTU5Nzk2YzI0OWY1MzAwNmI0ZjdkOTBlIn19fQ=="
    ).setName("§9§l📊 Analysis Summary");
    
    // Refresh button
    private final ItemBuilder refreshButton = ItemBuilder.createSkull(
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjRiMjI0MDQxNjJjNTBmYWQyNmFlMTBkNDdhMTBmMzFmOGViZmE5NDk5YjhkMWI0MzgzYjhjMjA5NjhkMzA2NyJ9fX0="
    ).setName("§a§l🔄 Refresh Analysis");

    public ChunkAnalyzerMenu(LagFixer plugin, int size, String title) {
        super(plugin, size, title, 2, true);
        createLayout();
    }

    private void createLayout() {
        // Animated border
        createAnimatedBorder();
        
        // Summary card at top center
        this.getInv().setItem(4, summaryCard.build());
        
        // Refresh button at bottom
        this.getInv().setItem(49, refreshButton.setLore(
                "",
                " §8» §7Click to run a new analysis",
                " §8» §7Updates chunk data in real-time",
                ""
        ).build());
        
        // Back button
        ItemStack backButton = new ItemBuilder(new ItemStack(Material.ARROW))
                .setName("§c§l← Back")
                .setLore("", " §7Return to main menu", "")
                .build();
        this.getInv().setItem(45, backButton);
        
        // Fill empty slots with glass
        fillGlassmorphism();
    }

    private void createAnimatedBorder() {
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
        Material borderMaterial = Material.CYAN_STAINED_GLASS_PANE;

        for (int slot : borderSlots) {
            ItemStack pane = new ItemStack(borderMaterial);
            new ItemBuilder(pane).setName(" ");
            this.getInv().setItem(slot, pane);
        }
    }

    private void fillGlassmorphism() {
        Material lightGlass = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        int[] decorSlots = {10, 16, 19, 25, 28, 34, 37, 43};
        
        for (int slot : decorSlots) {
            ItemStack pane = new ItemStack(lightGlass);
            new ItemBuilder(pane).setName(" ");
            this.getInv().setItem(slot, pane);
        }
    }

    /**
     * Update results from the analyzer module
     */
    public void updateResults() {
        ChunkAnalyzerModule module = ModuleManager.getInstance().get(ChunkAnalyzerModule.class);
        if (module == null) return;

        cachedResults = module.getTopProblematicChunks(28); // Max slots available
        
        // Clear chunk slots first
        int[] chunkSlots = getChunkSlots();
        for (int slot : chunkSlots) {
            this.getInv().setItem(slot, new ItemStack(Material.AIR));
        }
        
        // Place chunk results
        int index = 0;
        for (ChunkAnalyzerModule.ChunkAnalysisResult result : cachedResults) {
            if (index >= chunkSlots.length) break;
            
            ItemStack chunkItem = createChunkItem(result, index + 1);
            this.getInv().setItem(chunkSlots[index], chunkItem);
            index++;
        }
        
        // If no results, show "all clear" message
        if (cachedResults.isEmpty()) {
            ItemStack allClear = new ItemBuilder(new ItemStack(Material.EMERALD_BLOCK))
                    .setName("§a§l✓ All Clear!")
                    .setLore(
                            "",
                            " §7No problematic chunks detected.",
                            " §7Your server is running optimally!",
                            ""
                    ).build();
            this.getInv().setItem(22, allClear);
        }
    }

    private int[] getChunkSlots() {
        // Slots for chunk items (avoiding borders)
        return new int[]{
                11, 12, 13, 14, 15,
                20, 21, 22, 23, 24,
                29, 30, 31, 32, 33,
                38, 39, 40, 41, 42
        };
    }

    private ItemStack createChunkItem(ChunkAnalyzerModule.ChunkAnalysisResult result, int rank) {
        Material material = getSeverityMaterial(result.getSeverity());
        String severityColor = result.getSeverity().getColor();
        String severityLabel = result.getSeverity().getLabel();
        
        return new ItemBuilder(new ItemStack(material))
                .setName(severityColor + "§l#" + rank + " " + result.getWorldName() + " " + result.getChunkCoords())
                .setLore(
                        "",
                        " §8┌─ §fChunk Info",
                        " §8│ §7World: §b" + result.getWorldName(),
                        " §8│ §7Chunk: §f" + result.getChunkCoords(),
                        " §8│ §7Block Pos: §f(" + (result.getChunkX() * 16) + ", ~, " + (result.getChunkZ() * 16) + ")",
                        " §8│",
                        " §8├─ §fLag Score",
                        " §8│  " + severityColor + "§l" + String.format("%.1f", result.getLagScore()) + " §8(" + severityLabel + ")",
                        " §8│  " + createScoreBar(result.getLagScore(), 200),
                        " §8│",
                        " §8├─ §fEntities §7(" + result.getEntityCount() + " total)",
                        " §8│  §eMobs: §f" + result.getCreatureCount(),
                        " §8│  §6Items: §f" + result.getItemCount(),
                        " §8│  §cProjectiles: §f" + result.getProjectileCount(),
                        " §8│",
                        " §8├─ §fTile Entities §7(" + result.getTileEntityCount() + " total)",
                        " §8│  §cHoppers: §f" + result.getHopperCount(),
                        " §8│  §6Furnaces: §f" + result.getFurnaceCount(),
                        " §8│  §eChests: §f" + result.getChestCount(),
                        " §8│",
                        " §8└─ §fRedstone Score: §d" + result.getRedstoneScore(),
                        "",
                        "§b§nClick to teleport to this chunk!"
                ).build();
    }

    private Material getSeverityMaterial(ChunkAnalyzerModule.Severity severity) {
        return switch (severity) {
            case CRITICAL -> Material.REDSTONE_BLOCK;
            case DANGER -> Material.RED_TERRACOTTA;
            case WARNING -> Material.YELLOW_TERRACOTTA;
            default -> Material.LIME_TERRACOTTA;
        };
    }

    private String createScoreBar(double score, double max) {
        int filled = (int) Math.min(10, (score / max) * 10);
        StringBuilder bar = new StringBuilder(" §8[");
        
        for (int i = 0; i < 10; i++) {
            if (i < filled) {
                if (score >= 200) bar.append("§4█");
                else if (score >= 100) bar.append("§c█");
                else if (score >= 50) bar.append("§e█");
                else bar.append("§a█");
            } else {
                bar.append("§7░");
            }
        }
        
        bar.append("§8]");
        return bar.toString();
    }

    @Override
    public void update() {
        animationTick++;
        
        ChunkAnalyzerModule module = ModuleManager.getInstance().get(ChunkAnalyzerModule.class);
        if (module == null) return;
        
        // Update animated border
        updateAnimatedBorder(animationTick);
        
        // Update summary card
        ChunkAnalyzerModule.AnalysisSummary summary = module.getSummary();
        
        long timeSinceAnalysis = System.currentTimeMillis() - summary.getLastAnalysisTime();
        String timeAgo = timeSinceAnalysis < 60000 ? 
                (timeSinceAnalysis / 1000) + "s ago" : 
                (timeSinceAnalysis / 60000) + "m ago";
        
        summaryCard.setLore(
                "",
                " §8┌─ §fAnalysis Summary",
                " §8│ §7Last Scan: §b" + timeAgo,
                " §8│",
                " §8├─ §fChunks Analyzed: §b" + summary.getTotalChunks(),
                " §8├─ §fTotal Entities: §e" + summary.getTotalEntities(),
                " §8├─ §fTotal Tile Entities: §6" + summary.getTotalTileEntities(),
                " §8├─ §fTotal Hoppers: §c" + summary.getTotalHoppers(),
                " §8│",
                " §8├─ §fSeverity Breakdown:",
                " §8│  §a● Normal: §f" + summary.getNormalChunks(),
                " §8│  §e● Warning: §f" + summary.getWarningChunks(),
                " §8│  §c● Danger: §f" + summary.getDangerChunks(),
                " §8│  §4● Critical: §f" + summary.getCriticalChunks(),
                " §8│",
                " §8└─ §7Click for detailed report",
                ""
        );
        
        this.getInv().setItem(4, summaryCard.build());
    }

    private void updateAnimatedBorder(int tick) {
        Material[] rainbow = AnimationManager.getRainbowBorderMaterials(tick);
        
        int[] topBorder = {0, 1, 2, 3, 5, 6, 7, 8};
        int[] bottomBorder = {46, 47, 48, 50, 51, 52, 53};
        
        // Animate top border
        for (int i = 0; i < topBorder.length; i++) {
            ItemStack pane = new ItemStack(rainbow[i % rainbow.length]);
            new ItemBuilder(pane).setName(" ");
            this.getInv().setItem(topBorder[i], pane);
        }
        
        // Animate bottom border
        for (int i = 0; i < bottomBorder.length; i++) {
            ItemStack pane = new ItemStack(rainbow[i % rainbow.length]);
            new ItemBuilder(pane).setName(" ");
            this.getInv().setItem(bottomBorder[i], pane);
        }
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
        if (slot == 49) {
            if (human instanceof Player player) {
                MessageUtils.sendMessage(true, player, "§e⏳ Refreshing chunk analysis...");
                
                ChunkAnalyzerModule module = ModuleManager.getInstance().get(ChunkAnalyzerModule.class);
                if (module != null) {
                    module.runAnalysis();
                    updateResults();
                    MessageUtils.sendMessage(true, player, "§a✓ Analysis complete!");
                }
            }
            return;
        }
        
        // Chunk items - teleport on click
        int[] chunkSlots = getChunkSlots();
        for (int i = 0; i < chunkSlots.length; i++) {
            if (slot == chunkSlots[i] && i < cachedResults.size()) {
                ChunkAnalyzerModule.ChunkAnalysisResult result = cachedResults.get(i);
                
                if (human instanceof Player player) {
                    World world = Bukkit.getWorld(result.getWorldName());
                    if (world != null) {
                        int blockX = result.getChunkX() * 16 + 8;
                        int blockZ = result.getChunkZ() * 16 + 8;
                        int blockY = world.getHighestBlockYAt(blockX, blockZ) + 1;
                        
                        Location loc = new Location(world, blockX, blockY, blockZ);
                        player.teleport(loc);
                        player.closeInventory();
                        MessageUtils.sendMessage(true, player, 
                                "§a✓ Teleported to chunk " + result.getChunkCoords() + 
                                " in §b" + result.getWorldName() + 
                                " §7(Score: " + result.getSeverity().getColor() + 
                                String.format("%.1f", result.getLagScore()) + "§7)");
                    }
                }
                return;
            }
        }
    }

    @Override
    public AbstractMenu previousMenu() {
        return MenuCommand.getInstance().getMainMenu();
    }
}
