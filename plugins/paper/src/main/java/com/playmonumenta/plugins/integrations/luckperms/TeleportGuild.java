package com.playmonumenta.plugins.integrations.luckperms;

import java.util.LinkedHashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import me.lucko.luckperms.api.Group;
import me.lucko.luckperms.api.LuckPermsApi;

public class TeleportGuild {
	private static final String COMMAND = "teleportguild";

	@SuppressWarnings("unchecked")
	public static void register(LuckPermsApi lp) {
		// teleportguild <guildname> <player>
		CommandPermission perms = CommandPermission.fromString("monumenta.command.teleportguild");

		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put("player", new EntitySelectorArgument(EntitySelector.MANY_PLAYERS));
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				for (Player player : (List<Player>)args[0]) {
					run(lp, player, null);
				}
			})
			.register();

		arguments.put("guild name", new TextArgument());
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				for (Player player : (List<Player>)args[0]) {
					run(lp, player, (String)args[1]);
				}
			})
			.register();
	}

	private static void run(LuckPermsApi lp, Player player, String guildName) throws WrapperCommandSyntaxException {

		Group group = null;

		if (guildName == null) {
			// Look up the player's guild
			group = LuckPermsIntegration.getGuild(lp, player);
			if (group == null) {
				String err = ChatColor.RED + "You are not in a guild!";
				player.sendMessage(err);
				CommandAPI.fail(err);
			}
		}

		if (group == null) {
			// Still null, need to look up from name
			// The only way to get here is if guildName != null

			// Guild name sanitization for command usage
			//TODO: Better lookup of guild name?
			String cleanGuildName = LuckPermsIntegration.getCleanGuildName(guildName);

			group = lp.getGroup(cleanGuildName);
			if (group == null) {
				CommandAPI.fail("The luckperms group '" + cleanGuildName + "' does not exist");
			}
		}

		Location loc = LuckPermsIntegration.getGuildTp(lp, player.getWorld(), group);

		if (loc == null) {
			player.sendMessage(ChatColor.RED + "The teleport for your guild is not set up");
			player.sendMessage(ChatColor.RED + "Please ask a moderator to fix this");
		} else {
			player.teleport(loc);
		}
	}
}
