package xyz.lychee.lagfixer.menu;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.commands.MenuCommand;
import xyz.lychee.lagfixer.managers.AnimationManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMenu;
import xyz.lychee.lagfixer.objects.AbstractModule;
import xyz.lychee.lagfixer.objects.AbstractMonitor;
import xyz.lychee.lagfixer.utils.GUIUtils;
import xyz.lychee.lagfixer.utils.ItemBuilder;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.OversellDetector;

public class MainMenu extends AbstractMenu {
    
    private int animationTick = 0;
    private double previousTPS = 20.0;
    
    // Card icons
    private final ItemBuilder performanceCard = this.skull(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNjNzg5ZjIzMDc5NGY5MGUzM2M0ZjlhZDAwNjk0YmMyYTJmZjVlOGI5YjM3NWRjMzUzMjQwMWIyODFmM2U1OCJ9fX0=",
        "§9§l⚡ Performance Hub"
    );
    
    private final ItemBuilder modulesCard = this.skull(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWMyZmYyNDRkZmM5ZGQzYTJjZWY2MzExMmU3NTAyZGM2MzY3YjBkMDIxMzI5NTAzNDdiMmI0NzlhNzIzNjZkZCJ9fX0=",
        "§9§l📦 Modules"
    );
    
    private final ItemBuilder worldCard = this.skull(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTI4OWQ1YjE3ODYyNmVhMjNkMGIwYzNkMmRmNWMwODVlODM3NTA1NmJmNjg1YjVlZDViYjQ3N2ZlODQ3MmQ5NCJ9fX0=",
        "§9§l🌍 World Info"
    );
    
    private final ItemBuilder quickActionsCard = this.skull(
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ5ZjE4YzlkODVmOTJmNzJmODY0ZDY3YzEzNjdlOWE0NWRjMTBmMzcxNTQ5YzQ2YTRkNGRkOWU0ZjEzZmY0In19fQ==",
        "§9§l⚡ Quick Actions"
    );

    public MainMenu(LagFixer plugin, int size, String title) {
        super(plugin, size, title, 1, true);
        this.createLayout();
    }
    
    private void createLayout() {
        // Animated glassmorphism border
        this.createAnimatedBorder();
        
        // Main cards layout (2x2 grid centered)
        // Performance Card - Top Left
        this.getInv().setItem(11, performanceCard.build());
        
        // Modules Card - Top Right
        this.getInv().setItem(15, modulesCard.build());
        
        // Note: World Info and Quick Actions removed due to 27-slot inventory limitation
        // These features will be accessible through sub-menus in future update
        
        // Decorative glassmorphism panels
        this.fillGlassmorphism();
    }
    
    private void createAnimatedBorder() {
        // 3-row inventory: slots 0-26
        // Top row: 0-8, Bottom row: 18-26, Sides: 9,17 (middle row)
        int[] borderSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        Material borderMaterial = Material.CYAN_STAINED_GLASS_PANE;
        
        for (int slot : borderSlots) {
            ItemStack pane = new ItemStack(borderMaterial);
            new ItemBuilder(pane).setName(" ");
            this.getInv().setItem(slot, pane);
        }
    }
    
    private void fillGlassmorphism() {
        // Simplified for 3-row inventory
        // Only light decoration around center cards
        Material lightGlass = Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        
        // Decorative touches (minimal for 3-row)
        // Left and right of center cards only
        this.setGlassPane(10, lightGlass);
        this.setGlassPane(16, lightGlass);
    }
    
    private void setGlassPane(int slot, Material material) {
        ItemStack pane = new ItemStack(material);
        new ItemBuilder(pane).setName(" ");
        this.getInv().setItem(slot, pane);
    }

    private ItemBuilder skull(String textureHash, String name) {
        return ItemBuilder.createSkull(textureHash).setName(name).setLore(" §8» §7Loading...");
    }

    @Override
    public void update() {
        animationTick++;
        SupportManager support = SupportManager.getInstance();
        ModuleManager moduleManager = ModuleManager.getInstance();
        AbstractMonitor monitor = support.getMonitor();
        
        // Update animated border
        updateAnimatedBorder(animationTick);
        
        // ===== PERFORMANCE CARD =====
        double tps = monitor.getTps();
        double mspt = monitor.getMspt();
        long ramUsed = monitor.getRamUsed();
        long ramMax = monitor.getRamMax();
        double ramPercentage = (ramUsed / (double) ramMax) * 100.0;
        double cpuProcess = monitor.getCpuProcess();
        
        // Health status
        String healthStatus = tps >= 19.0 ? "§a§l● Excellent" :
                             tps >= 17.0 ? "§e§l◐ Good" :
                             tps >= 15.0 ? "§6§l◑ Fair" :
                             tps >= 10.0 ? "§c§l◌ Poor" : "§4§l✗ Critical";
        
        String trendIndicator = GUIUtils.getTrendIndicator(tps, previousTPS);
        previousTPS = tps;
        
        performanceCard.setLore(
                "",
                " §8┌─ SERVER HEALTH",
                " §8│ " + healthStatus,
                " §8│",
                " §8├─ TPS: " + GUIUtils.getTPSColor(tps) + "§l" + GUIUtils.formatNumber(tps) + " " + GUIUtils.getPerformanceIndicator(tps) + " " + trendIndicator,
                " §8├─ MSPT: " + GUIUtils.getMSPTColor(mspt) + "§l" + GUIUtils.formatNumber(mspt) + "ms",
                " §8│",
                " §8├─ RAM: " + GUIUtils.getMemoryColor(ramPercentage) + "§l" + ramUsed + "§8/§7" + ramMax + " MB",
                " §8│  " + GUIUtils.createProgressBar(ramUsed, ramMax, 12) + " §7" + GUIUtils.formatNumber(ramPercentage) + "%",
                " §8│",
                " §8└─ CPU: §b§l" + GUIUtils.formatNumber(cpuProcess) + "%",
                "",
                " §8[RESOURCE HEALTH]",
                " " + OversellDetector.quickCheck().getShortStatus(),
                "",
                "§b§nClick for detailed hardware info!"
        );
        
        // ===== MODULES CARD =====
        long activeModules = moduleManager.getModules().values().stream()
                .filter(AbstractModule::isLoaded)
                .count();
        long totalModules = moduleManager.getModules().size();
        
        // Calculate overall module performance score
        int moduleScore = (int) ((activeModules / (double) totalModules) * 100);
        String scoreTier = GUIUtils.getTierIndicator(moduleScore / 20);
        
        modulesCard.setLore(
                "",
                " §8┌─ MODULE STATUS",
                " §8│ " + GUIUtils.getStatusSymbol(activeModules > 0) + " §7Active: §b§l" + activeModules + "§8/§7" + totalModules,
                " §8│ " + scoreTier,
                " §8│",
                " §8├─ Overall Performance",
                " §8│  " + GUIUtils.createPercentageBar(moduleScore, 12),
                " §8│  §7Efficiency: §b§l" + moduleScore + "%",
                " §8│",
                " §8└─ " + GUIUtils.getActivityIndicator(activeModules > 0),
                "",
                "§b§nClick to manage modules!"
        );
        
        // ===== WORLD INFO CARD =====
        long entities = support.getEntities();
        long creatures = support.getCreatures();
        long items = support.getItems();
        
        worldCard.setLore(
                "",
                " §8┌─ WORLD STATISTICS",
                " §8│ §7Total Entities: §b§l" + GUIUtils.formatLargeNumber(entities),
                " §8├─ §7Creatures: §e§l" + GUIUtils.formatLargeNumber(creatures),
                " §8├─ §7Items: §6§l" + GUIUtils.formatLargeNumber(items),
                " §8├─ §7Projectiles: §c§l" + GUIUtils.formatLargeNumber(support.getProjectiles()),
                " §8│",
                " §8└─ §7Vehicles: §a§l" + GUIUtils.formatLargeNumber(support.getVehicles()),
                "",
                "§b§nClick for world management!"
        );
        
        // ===== QUICK ACTIONS CARD =====
        quickActionsCard.setLore(
                "",
                " §8┌─ AVAILABLE ACTIONS",
                " §8│",
                " §8│ §a⚡ §7Force Garbage Collection",
                " §8│ §c🗑 §7Clear Entities",
                " §8│ §e🛡 §7Toggle Lag Shield",
                " §8│ §b📊 §7Performance Dashboard",
                " §8│",
                " §8└─ §7Quick optimization tools",
                "",
                "§c§lComing Soon!"
        );
        
        // Update card positions
        this.getInv().setItem(11, performanceCard.build());
        this.getInv().setItem(15, modulesCard.build());
        // World and quick actions cards removed for 27-slot inventory compatibility
    }
    
    private void updateAnimatedBorder(int tick) {
        // Get rainbow materials based on current tick
        Material[] rainbow = AnimationManager.getRainbowBorderMaterials(tick);
        
        // 3-row inventory layout
        int[] topBorder = {0, 1, 2, 3, 4, 5, 6, 7, 8};
        int[] bottomBorder = {18, 19, 20, 21, 22, 23, 24, 25, 26};
        
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
        
        // Animate side borders with health-based colors
        AbstractMonitor monitor = SupportManager.getInstance().getMonitor();
        Material[] healthPalette = AnimationManager.getHealthPulseMaterials(monitor.getTps());
        
        // Left border (slot 9) and right border (slot 17) only for 3-row
        int[] sideBorders = {9, 17};
        
        Material sideMaterial = healthPalette[(tick / 10) % healthPalette.length];
        
        for (int slot : sideBorders) {
            ItemStack pane = new ItemStack(sideMaterial);
            new ItemBuilder(pane).setName(" ");
            this.getInv().setItem(slot, pane);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent e, ItemStack item) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        HumanEntity human = e.getWhoClicked();
        int slot = e.getSlot();

        if (slot == 11) {
            // Performance Card - Open Hardware Menu
            HardwareMenu menu = MenuCommand.getInstance().getHardwareMenu();
            if (menu == null) {
                MessageUtils.sendMessage(true, human, "§c§lHardware menu is not supported. :/");
            } else {
                human.openInventory(menu.getInv());
            }
        } else if (slot == 15) {
            // Modules Card - Open Modules Menu
            human.openInventory(MenuCommand.getInstance().getModulesMenu().getInv());
        }
        // World Info and Quick Actions cards removed for 27-slot compatibility
    }

    @Override
    public AbstractMenu previousMenu() {
        return null;
    }
}
