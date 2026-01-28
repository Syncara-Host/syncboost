package xyz.lychee.lagfixer.commands;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ConfigManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.utils.MessageUtils;
import xyz.lychee.lagfixer.utils.TimingUtil;

public class ReloadCommand extends CommandManager.Subcommand {
    private volatile boolean reload = false;

    public ReloadCommand(CommandManager commandManager) {
        super(commandManager, "reload", "reload all plugin configuration");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        if (this.reload) {
            return MessageUtils.sendMessage(true, sender, "&bReload is running, wait for results in console!");
        }

        this.reload = true;
        Thread thread = new Thread(() -> {
            TimingUtil t = TimingUtil.startNew();

            LagFixer plugin = this.getCommandManager().getPlugin();
            plugin.sendHeader();

            plugin.reloadConfig();
            try {
                ConfigManager.getInstance().load();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            ModuleManager.getInstance().getModules().forEach((clazz, m) -> {
                boolean enabled = m.getConfig().getBoolean(m.getName() + ".enabled");

                try {
                    if (enabled) {
                        if (!m.isLoaded()) {
                            m.load();
                            m.setLoaded(true);
                        }
                        m.loadAllConfig();
                        plugin.getLogger().info("&rConfiguration for &b" + m.getName() + " &rsuccessfully reloaded!");
                    } else if (m.isLoaded()) {
                        m.disable();
                        m.setLoaded(false);
                        plugin.getLogger().info("&rSuccessfully disabled module &b" + m.getName() + "&r!");
                    }
                    m.getMenu().updateAll();
                } catch (Exception ex) {
                    plugin.printError(ex);
                    plugin.getLogger().info("&c&lERROR §8» &7Error reloading configuration for &b" + m.getName() + "&r!");
                }
            });

            MessageUtils.sendMessage(true, sender, "&9&lSyncBoost &8» &7Reloaded modules configurations in &b" + t.stop().getExecutingTime() + "&7ms." +
                    "\n " +
                    "\n &7Working methods to apply all changes:" +
                    "\n  §8» &7Server restart (&brecommended&7)" +
                    "\n  §8» &7All plugins reload, command: &b/reload confirm" +
                    "\n  §8» &7Plugman reload, command: &b/plugman reload SyncBoost");
            this.reload = false;
        });
        thread.setName("LagFixer Reload");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
        return true;
    }
}