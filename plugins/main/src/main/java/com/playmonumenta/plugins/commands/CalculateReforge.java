package com.playmonumenta.plugins.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.utils.ItemUtils;
import com.playmonumenta.plugins.utils.ItemUtils.ItemRegion;

import io.github.jorelali.commandapi.api.CommandAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class CalculateReforge extends GenericCommand {

	public static ItemStack mCXP = null;
	public static ItemStack mHXP = null;
	public static ItemStack mCCS = null;
	public static ItemStack mHCS = null;

	public static void register() {
		registerPlayerCommand("calculatereforge", "monumenta.command.calculatereforge", (sender, player) -> {
			run(sender, player);
		});
	}

	@SuppressWarnings("deprecation")
	private static void run(CommandSender sender, Player player) throws CommandSyntaxException {
		// #region Loot Table extraction
		// Grab currency items from the loot tables so we can scan for and remove them from players' inventories.
		if (mCXP == null || mHXP == null || mCCS == null || mHCS == null) {
			Random random = new Random();
			LootContext context = new LootContext.Builder(player.getLocation()).build();
			ItemStack[] dummy = new ItemStack[0];
			if (mCXP == null) {
				NamespacedKey key = new NamespacedKey("epic", "r1/items/currency/concentrated_experience");
				LootTable table = Bukkit.getLootTable(key);
				if (table != null) {
					Collection<ItemStack> loot = table.populateLoot(random, context);
					if (!loot.isEmpty()) {
						mCXP = loot.toArray(dummy)[0];
					}
				}
			}
			if (mHXP == null) {
				NamespacedKey key = new NamespacedKey("epic", "r1/items/currency/hyper_experience");
				LootTable table = Bukkit.getLootTable(key);
				if (table != null) {
					Collection<ItemStack> loot = table.populateLoot(random, context);
					if (!loot.isEmpty()) {
						mHXP = loot.toArray(dummy)[0];
					}
				}
			}
			if (mCCS == null) {
				NamespacedKey key = new NamespacedKey("epic", "r2/items/currency/compressed_crystalline_shard");
				LootTable table = Bukkit.getLootTable(key);
				if (table != null) {
					Collection<ItemStack> loot = table.populateLoot(random, context);
					if (!loot.isEmpty()) {
						mCCS = loot.toArray(dummy)[0];
					}
				}
			}
			if (mHCS == null) {
				NamespacedKey key = new NamespacedKey("epic", "r2/items/currency/hyper_crystalline_shard");
				LootTable table = Bukkit.getLootTable(key);
				if (table != null) {
					Collection<ItemStack> loot = table.populateLoot(random, context);
					if (!loot.isEmpty()) {
						mHCS = loot.toArray(dummy)[0];
					}
				}
			}
		}
		//#endregion

		List<ItemStack> shatteredItems = new ArrayList<>();
		for (ItemStack item : player.getInventory()) {
			if (ItemUtils.isItemShattered(item)) {
				shatteredItems.add(item);
			}
		}
		if (!shatteredItems.isEmpty()) {
			Map<ItemRegion, Integer> fullInventoryCost = ItemUtils.getReforgeCosts(shatteredItems);
			TextComponent message1 = null; // First line, Cost of reforging hand item
			TextComponent message2 = null; // Second line, Cost of reforging entire inventory
			TextComponent message3 = new TextComponent("[Reforge Held Item] "); // First clicable area, reforge hand item
			TextComponent message4 = new TextComponent("[Reforge all items]"); // Second clickable area, reforge entire inventory

			// Make clickable areas actually clickable
			message3.setColor(ChatColor.DARK_PURPLE);
			message3.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/reforgehelditem"));
			message4.setColor(ChatColor.DARK_PURPLE);
			message4.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/reforgeinventory"));

			// Check how much it costs to reforge the mainhand item
			ItemStack hand = player.getInventory().getItemInMainHand();
			if (hand != null && ItemUtils.isItemShattered(hand)) {
				ItemRegion handRegion = ItemUtils.getItemRegion(hand);
				String handCurrency = null;
				if (handRegion == ItemRegion.KINGS_VALLEY) {
					handCurrency = "XP";
				} else if (handRegion == ItemRegion.CELSIAN_ISLES) {
					handCurrency = "CS";
				} else if (handRegion == ItemRegion.MONUMENTA) {
					if (player.getWorld().getName() == "Project_Epic-region_1") {
						handCurrency = "XP";
					} else if (player.getWorld().getName() == "Project_Epic-region_2") {
						handCurrency = "CS";
					} else {
						handCurrency = "XP";
					}
				}
				if (handCurrency != null) {
					// If the player is holding a shattered item, let them know how much it costs to reforge
					int handCost = ItemUtils.getReforgeCost(hand);
					message1 = new TextComponent(String.format(
					                                 "To reforge the item in your main hand, it will cost you %s%dH%s%s/%s%dC%s%s.\n",
					                                 ChatColor.YELLOW, handCost / 64, handCurrency, ChatColor.RESET, ChatColor.YELLOW,
					                                 handCost % 64, handCurrency, ChatColor.RESET));
				}
			}
			int cxp = fullInventoryCost.getOrDefault(ItemRegion.KINGS_VALLEY, 0);
			int ccs = fullInventoryCost.getOrDefault(ItemRegion.CELSIAN_ISLES, 0);
			int cmm = fullInventoryCost.getOrDefault(ItemRegion.MONUMENTA, 0);
			if (cmm != 0) {
				// If the player has any "Monumenta :" items, convert the cost to reforge that item to the current region, or CXP by default
				if (player.getWorld().getName() == "Project_Epic-region_1") {
					cxp += cmm;
				} else if (player.getWorld().getName() == "Project_Epic-region_2") {
					ccs += cmm;
				} else {
					cxp += cmm;
				}
			}

			// Tell the player the cost to reforge all items in their inventory
			if (cxp > 0 && ccs > 0) {
				message2 = new TextComponent(String.format(
						"To reforge all items in your inventory, it will cost you %s%dHXP%s/%s%dCXP%s and %s%dHCS%s/%s%dCCS%s.\n",
						ChatColor.YELLOW, cxp / 64, ChatColor.RESET, ChatColor.YELLOW, cxp % 64, ChatColor.RESET,
						ChatColor.YELLOW, ccs / 64, ChatColor.RESET, ChatColor.YELLOW, ccs % 64, ChatColor.RESET));
			} else if (cxp > 0) {
				message2 = new TextComponent(String.format(
						"To reforge all items in your inventory, it will cost you %s%dHXP%s/%s%dCXP%s\n",
						ChatColor.YELLOW, cxp / 64, ChatColor.RESET, ChatColor.YELLOW, cxp % 64, ChatColor.RESET));
			} else if (ccs > 0) {
				message2 = new TextComponent(String.format(
						"To reforge all items in your inventory, it will cost you %s%dHCS%s/%s%dCCS%s\n",
						ChatColor.YELLOW, ccs / 64, ChatColor.RESET, ChatColor.YELLOW, ccs % 64, ChatColor.RESET));
			} else {
				player.sendMessage("You don't have any shattered items");
				CommandAPI.fail("Player must have a Shattered item in their inventory!");
				return;
			}
			if (message1 != null && message2 != null) {
				player.spigot().sendMessage(message1, message2, message3, message4);
			} else if (message1 != null) {
				player.spigot().sendMessage(message1, message3);
			} else if (message2 != null) {
				player.spigot().sendMessage(message2, message4);
			}

			// Add the tag that allows the player to run the command to reforge their items, and remove it 30 seconds later.
			player.setMetadata("PlayerCanReforge", new FixedMetadataValue(Plugin.getInstance(), true));
			new BukkitRunnable() {
				@Override
				public void run() {
					if (player.hasMetadata("PlayerCanReforge")) {
						player.removeMetadata("PlayerCanReforge", Plugin.getInstance());
					}
				}
			}.runTaskLater(Plugin.getInstance(), 30 * 20);
		} else {
			player.sendMessage("You don't have any shattered items");
			CommandAPI.fail("Player must have a shattered item in their inventory");
		}
	}
}