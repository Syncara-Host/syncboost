package xyz.lychee.lagfixer.commands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.modules.EntityLimiterModule;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class SmartCapCommand extends CommandManager.Subcommand {
    public SmartCapCommand(CommandManager commandManager) {
        super(commandManager, "smartcap", "toggle Smart Mob Cap feature");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        EntityLimiterModule module = ModuleManager.getInstance().get(EntityLimiterModule.class);
        if (module == null || !module.isLoaded()) {
            MessageUtils.sendMessage(true, sender, "&c&lERROR §8» &7EntityLimiter module is disabled!");
            return true;
        }

        try {
            boolean currentState = module.getSection().getBoolean("smart_cap.enabled");
            boolean newState = !currentState;
            
            // Update config
            module.getSection().set("smart_cap.enabled", newState);
            File configFile = new File(module.getPlugin().getDataFolder(), "modules/" + module.getName() + ".yml");
            module.getConfig().save(configFile);
            
            // Reload module to apply changes
            module.disable();
            module.loadAllConfig();
            module.load();
            
            String status = newState ? "&a&lENABLED" : "&c&lDISABLED";
            MessageUtils.sendMessage(true, sender, "&9&lSmart Mob Cap §8» " + status);
            
            if (newState) {
                double tps = SupportManager.getInstance().getMonitor().getTps();
                MessageUtils.sendMessage(true, sender, "&7Current TPS: &b" + String.format("%.1f", tps));
            }
            
        } catch (Exception ex) {
            MessageUtils.sendMessage(true, sender, "&c&lERROR §8» &7Failed to toggle Smart Cap!");
            module.getPlugin().printError(ex);
        }
        
        return true;
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
