package me.xidentified.devotions.commandexecutors;

import me.xidentified.devotions.Devotions;
import me.xidentified.devotions.managers.FavorManager;
import me.xidentified.devotions.util.MessageUtils;
import me.xidentified.devotions.util.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FavorCommand implements CommandExecutor, TabCompleter {

    private final Devotions plugin;

    public FavorCommand(Devotions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // Display player's devotion if they don't provide an argument
        Player player = (Player) sender;
        if (args.length == 0) {
            FavorManager favorManager = plugin.getDevotionManager().getPlayerDevotion(player.getUniqueId());
            if (favorManager == null) {
                plugin.sendMessage(player, "<red>You don't have any devotion set.");
            } else {
                Component favorText = MessageUtils.getFavorText(favorManager.getFavor());
                plugin.sendMessage(player, Messages.FAVOR_CURRENT.formatted(
                    Placeholder.component("favor", favorText)
                ));
            }
            return true;
        }

        if (args.length != 3) {
            plugin.sendMessage(player,Messages.FAVOR_CMD_USAGE);
            return true;
        }

        // Check for permission
        if (!player.hasPermission("devotions.admin")) {
            plugin.sendMessage(player, Messages.GENERAL_CMD_NO_PERM);
            return true;
        }

        String action = args[0].toLowerCase();
        Player targetPlayer = Bukkit.getPlayer(args[1]);
        if (targetPlayer == null) {
            plugin.sendMessage(player, Messages.GENERAL_PLAYER_NOT_FOUND.formatted(
                Placeholder.unparsed("player", args[1])
            ));
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            plugin.sendMessage(player,"<red>Invalid amount. Please enter a number.");
            return true;
        }

        FavorManager favorManager = plugin.getDevotionManager().getPlayerDevotion(targetPlayer.getUniqueId());
        if (favorManager == null) {
            plugin.sendMessage(player,"<red>" + targetPlayer.getName() + " doesn't worship any deity.");
            return true;
        }

        switch (action) {
            case "set" -> favorManager.setFavor(amount);
            case "give" -> favorManager.increaseFavor(amount);
            case "take" -> favorManager.decreaseFavor(amount);
            default -> {
                plugin.sendMessage(player,"<red>Invalid action. Use set, give, or take.");
                return true;
            }
        }

        Component updatedFavorText = MessageUtils.getFavorText(favorManager.getFavor());
        player.sendMessage(Component.text("§a" + targetPlayer.getName() + "'s favor has been set to ").append(updatedFavorText));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("set");
            completions.add("give");
            completions.add("take");
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        }

        return completions;
    }
}