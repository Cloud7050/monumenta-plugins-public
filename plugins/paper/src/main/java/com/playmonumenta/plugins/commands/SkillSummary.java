package com.playmonumenta.plugins.commands;

import java.util.LinkedHashMap;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.BooleanArgument;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class SkillSummary extends GenericCommand {
	private static final String COMMAND = "skillsummary";

	public static void register(Plugin plugin) {
		CommandPermission perms = CommandPermission.fromString("monumenta.command.skillsummary");

		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				tell(plugin, sender, false);
			})
			.register();

		arguments.put("shorthand", new BooleanArgument());
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				tell(plugin, sender, (Boolean) args[0]);
			})
			.register();
	}

	private static void tell(Plugin plugin, CommandSender sender, boolean useShorthand) {
		Player player;
		if (sender instanceof ProxiedCommandSender) {
			if (((ProxiedCommandSender) sender).getCallee() instanceof Player) {
				player = (Player) ((ProxiedCommandSender) sender).getCallee();
			} else {
				error(sender, "Command must be run as a player.");
				return;
			}
		} else if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			error(sender, "Command must be run as a player.");
			return;
		}
		if (player == null) {
			error(sender, "Command must be run as a player.");
			return;
		}

		ComponentBuilder componentBuilder = new ComponentBuilder(player.getDisplayName()).color(ChatColor.AQUA)
		                                        .append("'s Skills:").color(ChatColor.GREEN);
		ComponentBuilder abilityHover;
		if (useShorthand) {
			for (Ability ability : plugin.mAbilityManager.getPlayerAbilities(player).getAbilities()) {
				if (ability != null) {
					abilityHover = ability.getLevelHover(useShorthand);
					if (abilityHover != null) {
						componentBuilder.append(" ");
						componentBuilder.append(abilityHover.create());
					}
				}
			}
			player.sendMessage(componentBuilder.create());
		} else {
			player.sendMessage(componentBuilder.create());
			for (Ability ability : plugin.mAbilityManager.getPlayerAbilities(player).getAbilities()) {
				if (ability != null) {
					abilityHover = ability.getLevelHover(useShorthand);
					if (abilityHover != null) {
						player.sendMessage(abilityHover.create());
					}
				}
			}
		}
	}
}

