package xyz.lychee.lagfixer.managers;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SingleLineChart;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.objects.AbstractManager;
import xyz.lychee.lagfixer.objects.AbstractModule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Metrics manager for SyncBoost using official bStats library.
 * Plugin ID: 29119
 * Dashboard: https://bstats.org/plugin/bukkit/SyncBoost/29119
 */
public class MetricsManager extends AbstractManager {

    private static final int PLUGIN_ID = 29119;
    private Metrics metrics;

    public MetricsManager(LagFixer plugin) {
        super(plugin);
    }

    @Override
    public void load() throws Exception {
        // Initialize bStats metrics
        this.metrics = new Metrics(this.getPlugin(), PLUGIN_ID);

        // Custom charts for server statistics
        this.metrics.addCustomChart(new SingleLineChart("entities", () -> SupportManager.getInstance().getEntities()));

        this.metrics
                .addCustomChart(new SingleLineChart("creatures", () -> SupportManager.getInstance().getCreatures()));

        this.metrics.addCustomChart(new SingleLineChart("items", () -> SupportManager.getInstance().getItems()));

        this.metrics.addCustomChart(
                new SingleLineChart("projectiles", () -> SupportManager.getInstance().getProjectiles()));

        this.metrics.addCustomChart(new SingleLineChart("vehicles", () -> SupportManager.getInstance().getVehicles()));

        // Chart showing which modules are enabled
        this.metrics.addCustomChart(new AdvancedPie("modules", () -> {
            Map<String, Integer> values = new HashMap<>();
            Set<AbstractModule> modules = new HashSet<>(ModuleManager.getInstance().getModules().values());
            for (AbstractModule module : modules) {
                if (module.isLoaded()) {
                    values.put(module.getName(), 1);
                }
            }
            return values;
        }));

        this.getPlugin().getLogger().info(" §8• §rEnabled bStats metrics (ID: " + PLUGIN_ID + ")");
    }

    @Override
    public void disable() throws Exception {
        // bStats handles shutdown automatically
        this.metrics = null;
    }

    @Override
    public boolean isEnabled() {
        return this.getPlugin().getConfig().getBoolean("main.bStats");
    }
}
