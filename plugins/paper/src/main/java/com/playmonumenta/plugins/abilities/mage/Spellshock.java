package com.playmonumenta.plugins.abilities.mage;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NavigableSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.effects.Effect;
import com.playmonumenta.plugins.effects.PercentSpeed;
import com.playmonumenta.plugins.effects.SpellShockStatic;
import com.playmonumenta.plugins.enchantments.BaseAbilityEnchantment;
import com.playmonumenta.plugins.enchantments.EnchantmentManager.ItemSlot;
import com.playmonumenta.plugins.events.CustomDamageEvent;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.scriptedquests.utils.MetadataUtils;

public class Spellshock extends Ability {
	public static class SpellshockRadiusEnchantment extends BaseAbilityEnchantment {
		public SpellshockRadiusEnchantment() {
			super("Spellshock Range", EnumSet.of(ItemSlot.MAINHAND, ItemSlot.OFFHAND, ItemSlot.ARMOR));
		}
	}

	public static class SpellshockDamageEnchantment extends BaseAbilityEnchantment {
		public SpellshockDamageEnchantment() {
			super("Spellshock Damage", EnumSet.of(ItemSlot.OFFHAND));
		}
	}

	private static final String DAMAGED_THIS_TICK_METAKEY = "SpellShockDamagedThisTick";

	private static final String SPELL_SHOCK_STATIC_EFFECT_NAME = "SpellShockStaticEffect";
	private static final String PERCENT_SPEED_EFFECT_NAME = "SpellShockPercentSpeedEffect";

	private static final int DAMAGE_1 = 3;
	private static final int DAMAGE_2 = 5;
	private static final int RADIUS = 3;
	private static final double PERCENT_SPEED_EFFECT_2 = 0.15;
	private static final double PERCENT_SPEED_EFFECT_2_DISPLAY = Math.round(PERCENT_SPEED_EFFECT_2 * 100);
	private static final int DURATION_SECONDS = 6;
	private static final int DURATION = DURATION_SECONDS * 20;

	public Spellshock(Plugin plugin, Player player) {
		super(plugin, player, "Spellshock");
		mInfo.mLinkedSpell = Spells.SPELLSHOCK;
		mInfo.mScoreboardId = "SpellShock";
		mInfo.mShorthandName = "SS";
		mInfo.mDescriptions.add(
			String.format(
				"Attacking an enemy with a wand or skill applies %ss of \"static\". Attacking an enemy that already has static with a skill triggers that static, causing a spellshock centered on that enemy, dealing %s damage to it and all enemies within %s blocks of it. This skill itself cannot apply but can trigger static, causing a chain reaction amongst other enemies with static, though each enemy can only be damaged by this once per chain reaction.",
				DURATION_SECONDS,
				DAMAGE_1,
				RADIUS
			)
		);
		mInfo.mDescriptions.add(
			String.format(
				"Damage is increased from %s to %s. Triggering static now applies %ss of +%s%% speed on you.",
				DAMAGE_1,
				DAMAGE_2,
				DURATION_SECONDS,
				PERCENT_SPEED_EFFECT_2_DISPLAY
			)
		);
	}

	@Override
	public void playerDealtCustomDamageEvent(CustomDamageEvent event) {
		LivingEntity mob = event.getDamaged();

		// Check if the mob has static, and trigger it if possible; otherwise, apply/refresh it
		NavigableSet<Effect> effectGroupOriginal = mPlugin.mEffectManager.getEffects(mob, SPELL_SHOCK_STATIC_EFFECT_NAME);
		if (effectGroupOriginal != null) {
			SpellShockStatic effectOriginal = (SpellShockStatic) effectGroupOriginal.last();
			// We don't directly remove the effect, because we don't want mobs with static to immediately be re-applied with it, so set flags instead
			if (event.triggersSpellshock() && !effectOriginal.isTriggered()) {
				effectOriginal.trigger();

				if (getAbilityScore() > 1) {
					mPlugin.mEffectManager.addEffect(mPlayer, PERCENT_SPEED_EFFECT_NAME, new PercentSpeed(DURATION, PERCENT_SPEED_EFFECT_2, PERCENT_SPEED_EFFECT_NAME));
				}

				Location mobCenterLocation = mob.getLocation().add(0, mob.getHeight() / 2, 0);
				World world = mPlayer.getWorld();
				world.spawnParticle(Particle.SPELL_WITCH, mobCenterLocation, 60, 1, 1, 1, 0.001);
				world.spawnParticle(Particle.CRIT_MAGIC, mobCenterLocation, 45, 1, 1, 1, 0.25);
				world.playSound(mobCenterLocation, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 0.75f, 2.5f);
				world.playSound(mobCenterLocation, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 0.75f, 2.0f);
				world.playSound(mobCenterLocation, Sound.ENTITY_PLAYER_HURT_ON_FIRE, 0.75f, 1.5f);

				// Grab all realistically possible nearby mobs for simplicity, use Set for fast removal
				Set<LivingEntity> nearbyMobs = new HashSet<LivingEntity>(EntityUtils.getNearbyMobs(mobCenterLocation, 32, mPlayer));
				// Adding and removing elements in the middle of the list means LinkedList ListIterator should be more efficient
				List<LivingEntity> triggeredMobs = new LinkedList<LivingEntity>();
				triggeredMobs.add(mob);

				int radius = (int) SpellshockRadiusEnchantment.getRadius(mPlayer, RADIUS, SpellshockRadiusEnchantment.class);
				int damage = getAbilityScore() == 1 ? DAMAGE_1 : DAMAGE_2;
				damage += SpellshockDamageEnchantment.getExtraDamage(mPlayer, SpellshockDamageEnchantment.class);

				/*
				 * Loop through triggeredMobs, and check distances to each in nearbyMobs. If in range,
				 * deal damage. If the mob can be triggered, trigger it, adding it before the iteration
				 * cursor for triggeredMobs. Remove the mob from nearbyMobs. When we finish iterating
				 * through nearbyMobs, remove the triggered mob from triggeredMobs.
				 *
				 * Once we reach the end of the iteration for triggeredMobs, create another iterator
				 * to catch any newly triggered mobs, and repeat until there are no more newly triggered
				 * mobs.
				 */
				while (!triggeredMobs.isEmpty()) {
					ListIterator<LivingEntity> triggeredMobsIter = triggeredMobs.listIterator();
					while (triggeredMobsIter.hasNext()) {
						LivingEntity triggeredMob = triggeredMobsIter.next();

						Iterator<LivingEntity> nearbyMobsIter = nearbyMobs.iterator();
						while (nearbyMobsIter.hasNext()) {
							LivingEntity nearbyMob = nearbyMobsIter.next();
							if (nearbyMob.getLocation().distanceSquared(triggeredMob.getLocation()) < (radius * radius)) {
								// Only damage a mob once per tick
								if (MetadataUtils.checkOnceThisTick(mPlugin, nearbyMob, DAMAGED_THIS_TICK_METAKEY)) {
									Vector velocity = nearbyMob.getVelocity();
									/*
									 * We should probably call the CustomDamageEvent, but historically that's caused problems with
									 * "passives" like SpellShock triggering all sorts of things it shouldn't be triggering. I
									 * can't currently think of a reason why it should still be not calling the event (since
									 * Overload has been reworked and there's a SpellShock specific parameter (which should also
									 * probably be restructured and removed)), but I don't have the time currently to verify that
									 * nothing explodes
									 */
									EntityUtils.damageEntity(mPlugin, nearbyMob, damage, mPlayer, MagicType.ARCANE, false, mInfo.mLinkedSpell);
									nearbyMob.setVelocity(velocity);
								}

								NavigableSet<Effect> effectGroup = mPlugin.mEffectManager.getEffects(mob, SPELL_SHOCK_STATIC_EFFECT_NAME);
								if (effectGroup != null) {
									SpellShockStatic effect = (SpellShockStatic) effectGroup.last();
									if (!effect.isTriggered()) {
										effect.trigger();
										triggeredMobsIter.add(nearbyMob);
									}
								}

								nearbyMobsIter.remove();
							}
						}

						triggeredMobsIter.remove();
					}
				}
			}
		} else if (event.appliesSpellshock()) {
			NavigableSet<Effect> effectGroup = mPlugin.mEffectManager.getEffects(mob, SPELL_SHOCK_STATIC_EFFECT_NAME);
			if (effectGroup != null) {
				SpellShockStatic effect = (SpellShockStatic) effectGroup.last();
				if (!effect.isTriggered()) {
					effect.setDuration(DURATION);
				}
			} else {
				mPlugin.mEffectManager.addEffect(mob, SPELL_SHOCK_STATIC_EFFECT_NAME, new SpellShockStatic(DURATION));
			}
		}
	}

	@Override
	public boolean livingEntityDamagedByPlayerEvent(EntityDamageByEntityEvent event) {
		mPlugin.mEffectManager.addEffect(event.getEntity(), SPELL_SHOCK_STATIC_EFFECT_NAME, new SpellShockStatic(DURATION));
		return true;
	}

	@Override
	public boolean runCheck() {
		return InventoryUtils.isWandItem(mPlayer.getInventory().getItemInMainHand());
	}

}
