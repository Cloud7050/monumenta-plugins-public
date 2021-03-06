package com.playmonumenta.plugins.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.potion.PotionManager;
import com.playmonumenta.plugins.potion.PotionManager.PotionID;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.PotionEffectArgument;

public class Effect {
	private static final String COMMAND = "effect";

	@SuppressWarnings("unchecked")
	public static void register(Plugin plugin) {
		CommandPermission perms = CommandPermission.fromString("minecraft.command.effect");

		/* Unregister the default /effect command */
		CommandAPI.unregister("effect");

		/* Add effects (/effect give) */
		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();

		arguments.put("give", new LiteralArgument("give"));
		arguments.put("entity", new EntitySelectorArgument(EntitySelector.MANY_ENTITIES));
		arguments.put("effect", new PotionEffectArgument());
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				giveEffect(plugin, sender, (Collection<Entity>)args[0],
						   (PotionEffectType)args[1], 30, 0, false);
			})
			.register();

		arguments.put("seconds", new IntegerArgument(1));
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				giveEffect(plugin, sender, (Collection<Entity>)args[0],
						   (PotionEffectType)args[1], (Integer)args[2],
						   0, false);
			})
			.register();

		arguments.put("amplifier", new IntegerArgument(0));
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				giveEffect(plugin, sender, (Collection<Entity>)args[0],
						   (PotionEffectType)args[1], (Integer)args[2],
						   (Integer)args[3], false);
			})
			.register();

		arguments.put("hideParticles", new BooleanArgument());
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				giveEffect(plugin, sender, (Collection<Entity>)args[0],
						   (PotionEffectType)args[1], (Integer)args[2],
						   (Integer)args[3], (Boolean)args[4]);
			})
			.register();

		/* Clear effects (/effect clear) */
		arguments.clear();
		arguments.put("clear", new LiteralArgument("clear"));
		arguments.put("entity", new EntitySelectorArgument(EntitySelector.MANY_ENTITIES));
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				clearEffect(plugin, sender, (Collection<Entity>)args[0], null);
			})
			.register();

		arguments.put("effect", new PotionEffectArgument());
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				clearEffect(plugin, sender, (Collection<Entity>)args[0], (PotionEffectType)args[1]);
			})
			.register();
	}

	private static void giveEffect(Plugin plugin, CommandSender sender, Collection<Entity> entities,
	                               PotionEffectType type, int seconds, int amplifier, boolean hideParticles) {
		PotionManager manager = plugin.mPotionManager;

		for (Entity e : entities) {
			if (e instanceof Player && manager != null) {
				// This is a player - use the potion manager
				Player player = (Player)e;

				/* Apply potion via potion manager */
				manager.addPotion(player, PotionID.APPLIED_POTION,
				                  new PotionEffect(type, seconds * 20, amplifier, true, true));
			} else if (e instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity)e;
				// Not a player - apply the effect directly
				entity.addPotionEffect(new PotionEffect(type, seconds * 20, amplifier, false, !hideParticles));
			}
		}
		sender.sendMessage("Applied " + type.toString() + ":" + Integer.toString(amplifier + 1) +
		                   " to entities for " + Integer.toString(seconds) + "s");
	}

	private static void clearEffect(Plugin plugin, CommandSender sender, Collection<Entity> entities,
	                                PotionEffectType type) {
		PotionManager manager = plugin.mPotionManager;

		World world = null;
		for (Entity e : entities) {
			world = e.getWorld();
			if (e instanceof Player && manager != null) {
				// This is a player - use the potion manager
				Player player = (Player)e;

				if (type == null) {
					// Clear all effects
					manager.clearAllPotions(player);
				} else {
					// Clear one effect
					manager.clearPotionEffectType(player, type);
				}
			} else if (e instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity)e;
				// Not a player - clear the effect directly
				if (type == null) {
					// Clear all effects
					// Copy the list to prevent ConcurrentModificationException's
					for (PotionEffect effect : new ArrayList<PotionEffect>(entity.getActivePotionEffects())) {
						entity.removePotionEffect(effect.getType());
					}
				} else {
					// Clear one effect
					entity.removePotionEffect(type);
				}
			}
		}

		if (world != null && world.getGameRuleValue(GameRule.SEND_COMMAND_FEEDBACK)) {
			if (type == null) {
				sender.sendMessage("Cleared all effects from entities");
			} else {
				sender.sendMessage("Cleared " + type.toString() + " effect from entities");
			}
		}
	}
}
