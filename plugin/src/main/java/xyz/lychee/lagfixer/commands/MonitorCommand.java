package xyz.lychee.lagfixer.commands;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractMonitor;
import xyz.lychee.lagfixer.utils.MessageUtils;

public class MonitorCommand extends CommandManager.Subcommand {
    public MonitorCommand(CommandManager commandManager) {
        super(commandManager, "monitor", "check server load statistics", "tps", "mspt");
    }

    @Override
    public void load() {}

    @Override
    public void unload() {}

    @Override
    public boolean execute(@NotNull org.bukkit.command.CommandSender sender, @NotNull String[] args) {
        AbstractMonitor monitor = SupportManager.getInstance().getMonitor();
        return MessageUtils.sendMessage(true, sender,
                "&9&lSyncBoost &fMonitor:" +
                        "\n §8» &7TPS: &b" + monitor.getTps() +
                        "\n §8» &7MSPT: &b" + monitor.getMspt() +
                        "\n §8» &7Memory: &b" + monitor.getRamUsed() + "§8/§b" + monitor.getRamTotal() + "§8/§b" + monitor.getRamMax() + " MB" +
                        "\n §8» &7CPU Process: &b" + monitor.getCpuProcess() + "%" +
                        "\n §8» &7CPU System: &b" + monitor.getCpuSystem() + "%");
    }
}