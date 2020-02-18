package com.playmonumenta.plugins.enchantments;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.enchantments.EnchantmentManager.ItemSlot;
import com.playmonumenta.plugins.events.EvasionEvent;
import com.playmonumenta.plugins.utils.BossUtils.BossAbilityDamageEvent;
import com.playmonumenta.plugins.utils.InventoryUtils;

public interface BaseEnchantment {
	/*
	 * Required - the name of the property
	 */
	public String getProperty();

	/*
	 * Returns whether the property can have negative values (attributes)
	 */
	public default boolean negativeLevelsAllowed() {
		return false;
	}

	/*
	 * To use enchant levels or not
	 * If true, uses enchant levels (I, II, III, etc.)
	 * If false, always return level 1 for the item when the enchant is found
	 */
	public default boolean useEnchantLevels() {
		return true;
	}

	/*
	 * Describes which slots this property is valid in
	 */
	public default EnumSet<ItemSlot> validSlots() {
		return EnumSet.noneOf(ItemSlot.class);
	}

	/*
	 * Computes what level the given item is for this particular ItemProperty.
	 * If the item does not have this property, it should return 0
	 */
	public default int getLevelFromItem(ItemStack item) {
		return InventoryUtils.getCustomEnchantLevel(item, getProperty(), useEnchantLevels());
	}

	/*
	 * Computes what level the given item is for this particular ItemProperty.
	 * If the item does not have this property, it should return 0
	 * This variant is useful for soulbound items
	 */
	public default int getLevelFromItem(ItemStack item, Player player) {
		// By default ignore the player
		return getLevelFromItem(item);
	}

	/*
	 * Computes what level the given item is for this particular ItemProperty.
	 * If the item does not have this property, it should return 0
	 * This variant is useful for regeneration
	 */
	public default int getLevelFromItem(ItemStack item, Player player, ItemSlot slot) {
		// By default ignore the slot
		return getLevelFromItem(item, player);
	}

	/*
	 * applyProperty will be called every time the player changes their inventory
	 * and the player matches this ItemProperty
	 */
	public default void applyProperty(Plugin plugin, Player player, int level) { }

	/*
	 * removeProperty will be called every time the player changes their inventory
	 * and they previously had this property active, even if it is still active
	 * (in which case applyProperty will be called again immediately afterwards)
	 *
	 * TODO: Modify this so it is only called when the item effect should actually
	 * be removed
	 */
	public default void removeProperty(Plugin plugin, Player player) { }

	/*
	 * Runs whenever the player changes their inventory and equipment is updated
	 */
	public default void onEquipmentUpdate(Plugin plugin, Player player) { }


	/* This method will be called four times per second */
	public default void tick(Plugin plugin, World world, Player player, int level) { }

	/*
	 * The onAttack() method will be called whenever the player damages something while
	 * they have any levels of this property (This only applies for MELEE attacks)
	 */
	public default void onAttack(Plugin plugin, Player player, int level, LivingEntity target, EntityDamageByEntityEvent event) { }

	/*
	 * The onKill() method will be called whenever the player kills something
	 * while they have any levels of this property
	 */
	public default void onKill(Plugin plugin, Player player, int level, Entity target, EntityDeathEvent event) { }

	/*
	 * The onDamage() method will be called whenever the player damages something, no
	 * matter the cause of damage.
	 */
	public default void onDamage(Plugin plugin, Player player, int level, LivingEntity target, EntityDamageByEntityEvent event) { }

	public default void onHurt(Plugin plugin, Player player, int level, EntityDamageEvent event) { }

	public default void onBossDamage(Plugin plugin, Player player, int level, BossAbilityDamageEvent event) { }

	public default void onHurtByEntity(Plugin plugin, Player player, int level, EntityDamageByEntityEvent event) { }

	public default void onEvade(Plugin plugin, Player player, int level, EvasionEvent event) { }

	public default void onDeath(Plugin plugin, Player player, PlayerDeathEvent event, int level) { }

	/*
	 * The onShootAttack() method will be called whenever the player damages something with a projectile while
	 * they have any levels of this property
	 */
	public default void onLaunchProjectile(Plugin plugin, Player player, int level, Projectile target, ProjectileLaunchEvent event) { }

	public default void onExpChange(Plugin plugin, Player player, PlayerExpChangeEvent event, int level) { }

	public default void onBlockBreak(Plugin plugin, Player player, BlockBreakEvent event, ItemStack item, int level) { }

	public default void onPlayerInteract(Plugin plugin, Player player, PlayerInteractEvent event, int level) { }

	public default void onConsume(Plugin plugin, Player player, PlayerItemConsumeEvent event, int level) { }

	/*
	 * Triggers when an item entity spawns in the world (possibly a player dropped item)
	 *
	 * IMPORTANT - To use this, you must also override hasOnSpawn() to return true
	 */
	public default boolean hasOnSpawn() {
		return false;
	}

	public default void onSpawn(Plugin plugin, Item item, int level) { }

	/*
	 * TODO: Add an onRightClick() method so you can make items that cast spells
	 */
}