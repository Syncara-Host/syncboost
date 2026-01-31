package xyz.lychee.lagfixer.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.managers.CommandManager;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.menu.ChunkAnalyzerMenu;
import xyz.lychee.lagfixer.modules.ChunkAnalyzerModule;
import xyz.lychee.lagfixer.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChunkAnalyzerCommand extends CommandManager.Subcommand {
    
    private ChunkAnalyzerMenu menu;

    public ChunkAnalyzerCommand(CommandManager commandManager) {
        super(commandManager, "chunks", "Analyze chunks for performance issues", "chunk", "analyze");
    }

    @Override
    public void load() {
        this.menu = new ChunkAnalyzerMenu(this.getCommandManager().getPlugin(), 54, "§9§l⚡ Chunk Analyzer");
    }

    @Override
    public void unload() {
        this.menu = null;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        ChunkAnalyzerModule module = ModuleManager.getInstance().get(ChunkAnalyzerModule.class);
        
        if (module == null || !module.isLoaded()) {
            return MessageUtils.sendMessage(true, sender, "§c§l✗ §cChunk Analyzer module is not enabled!");
        }
        
        // Check for world argument
        String worldName = args.length > 0 ? args[0] : null;
        
        if (worldName != null && !worldName.equalsIgnoreCase("gui")) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                return MessageUtils.sendMessage(true, sender, "§c§l✗ §cWorld '" + worldName + "' not found!");
            }
            
            // Run analysis for specific world
            MessageUtils.sendMessage(true, sender, "§e⏳ Analyzing chunks in §f" + worldName + "§e...");
            
            module.runAnalysis();
            List<ChunkAnalyzerModule.ChunkAnalysisResult> results = module.getWorldAnalysis(worldName);
            
            displayResults(sender, results, worldName);
            return true;
        }
        
        // Open GUI if player, otherwise show text results
        if (sender instanceof Player player) {
            if (worldName != null && worldName.equalsIgnoreCase("gui")) {
                // Force GUI
                openGui(player, module);
            } else {
                // Default: open GUI for players
                openGui(player, module);
            }
        } else {
            // Console: show text results
            MessageUtils.sendMessage(true, sender, "§e⏳ Analyzing all loaded chunks...");
            
            module.runAnalysis();
            List<ChunkAnalyzerModule.ChunkAnalysisResult> results = module.getTopProblematicChunks();
            
            displayResults(sender, results, "All Worlds");
        }
        
        return true;
    }
    
    private void openGui(Player player, ChunkAnalyzerModule module) {
        MessageUtils.sendMessage(true, player, "§e⏳ Analyzing chunks...");
        
        // Run analysis
        module.runAnalysis();
        
        // Update and show menu
        menu.updateResults();
        player.openInventory(menu.getInv());
    }
    
    private void displayResults(CommandSender sender, List<ChunkAnalyzerModule.ChunkAnalysisResult> results, String scope) {
        if (results.isEmpty()) {
            MessageUtils.sendMessage(true, sender, "§a✓ No problematic chunks found in " + scope + "!");
            return;
        }
        
        ChunkAnalyzerModule module = ModuleManager.getInstance().get(ChunkAnalyzerModule.class);
        ChunkAnalyzerModule.AnalysisSummary summary = module.getSummary();
        
        StringBuilder sb = new StringBuilder();
        sb.append("\n§9§l━━━━━━━━━━━━━━ CHUNK ANALYSIS ━━━━━━━━━━━━━━\n\n");
        
        // Summary
        sb.append("§8┌─ §fSummary for §b").append(scope).append("\n");
        sb.append("§8│ §7Total Chunks: §f").append(summary.getTotalChunks()).append("\n");
        sb.append("§8│ §7Total Entities: §f").append(summary.getTotalEntities()).append("\n");
        sb.append("§8│ §7Total Tile Entities: §f").append(summary.getTotalTileEntities()).append("\n");
        sb.append("§8│\n");
        sb.append("§8│ §a● Normal: §f").append(summary.getNormalChunks());
        sb.append(" §8| §e● Warning: §f").append(summary.getWarningChunks());
        sb.append(" §8| §c● Danger: §f").append(summary.getDangerChunks());
        sb.append(" §8| §4● Critical: §f").append(summary.getCriticalChunks()).append("\n");
        sb.append("§8└───────────────────────────────────────────\n\n");
        
        // Top problematic chunks
        sb.append("§c§lTop Problematic Chunks:\n\n");
        
        int rank = 1;
        for (ChunkAnalyzerModule.ChunkAnalysisResult result : results) {
            if (rank > 10) break;
            
            String severityColor = result.getSeverity().getColor();
            String severityLabel = result.getSeverity().getLabel();
            
            sb.append("§f").append(rank).append(". ")
                    .append(severityColor).append("■ ")
                    .append("§f").append(result.getWorldName())
                    .append(" §7").append(result.getChunkCoords())
                    .append(" §8(Block: ").append(result.getChunkX() * 16).append(", ").append(result.getChunkZ() * 16).append(")\n");
            
            sb.append("   §8│ §7Score: ").append(severityColor).append(String.format("%.1f", result.getLagScore()))
                    .append(" §8(").append(severityLabel).append(")\n");
            
            sb.append("   §8│ §7Entities: §f").append(result.getEntityCount())
                    .append(" §8(§eMobs: ").append(result.getCreatureCount())
                    .append("§8, §6Items: ").append(result.getItemCount()).append("§8)\n");
            
            sb.append("   §8└ §7Tile Entities: §f").append(result.getTileEntityCount())
                    .append(" §8(§cHoppers: ").append(result.getHopperCount()).append("§8)\n\n");
            
            rank++;
        }
        
        sb.append("§9§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("§7Tip: Use §f/sb chunks gui §7for interactive menu\n");
        
        MessageUtils.sendMessage(false, sender, sb.toString());
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("gui");
            completions.addAll(Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .collect(Collectors.toList()));
            
            if (!args[0].isEmpty()) {
                completions.removeIf(s -> !s.toLowerCase().startsWith(args[0].toLowerCase()));
            }
            
            Collections.sort(completions);
            return completions;
        }
        return Collections.emptyList();
    }
    
    public ChunkAnalyzerMenu getMenu() {
        return menu;
    }
}
