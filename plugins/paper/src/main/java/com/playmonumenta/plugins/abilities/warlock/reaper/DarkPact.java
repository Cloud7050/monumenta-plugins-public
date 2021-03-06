package com.playmonumenta.plugins.abilities.warlock.reaper;

import java.util.EnumSet;
import java.util.NavigableSet;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.effects.Aesthetics;
import com.playmonumenta.plugins.effects.Effect;
import com.playmonumenta.plugins.effects.PercentDamageDealt;
import com.playmonumenta.plugins.effects.PercentDamageReceived;
import com.playmonumenta.plugins.effects.PercentHeal;
import com.playmonumenta.plugins.utils.AbsorptionUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;

public class DarkPact extends Ability {

	public static final String PERCENT_HEAL_EFFECT_NAME = "DarkPactPercentHealEffect";
	private static final int PERCENT_HEAL = -1;
	private static final String PERCENT_DAMAGE_RESIST_EFFECT_NAME = "DarkPactPercentDamageResistEffect";
	private static final double PERCENT_DAMAGE_RESIST = -0.2;
	private static final String AESTHETICS_EFFECT_NAME = "DarkPactAestheticsEffect";
	private static final String PERCENT_DAMAGE_DEALT_EFFECT_NAME = "DarkPactPercentDamageDealtEffect";
	private static final int DURATION = 20 * 10;
	private static final int DURATION_INCREASE_ON_KILL = 20 * 1;
	private static final double PERCENT_DAMAGE_DEALT_1 = 0.4;
	private static final double PERCENT_DAMAGE_DEALT_2 = 0.8;
	private static final EnumSet<DamageCause> ALLOWED_DAMAGE_CAUSES = EnumSet.of(DamageCause.ENTITY_ATTACK);

	private static final int ABSORPTION_ON_KILL = 1;
	private static final int MAX_ABSORPTION = 6;
	private static final int COOLDOWN = 20 * 20;
	private int mLeftClicks = 0;
	private final double mPercentDamageDealt;

	public DarkPact(Plugin plugin, Player player) {
		super(plugin, player, "Dark Pact");
		mInfo.mScoreboardId = "DarkPact";
		mInfo.mShorthandName = "DaP";
		mInfo.mDescriptions.add("Left clicking twice with a scythe causes you to gain 20% damage reduction and deal +40% melee damage for 10 seconds. Each kill during this time increases the duration by 1 second and gives 1 absorption health (capped at 6) for the duration of the melee bonus. However, the player cannot heal for 10 seconds, and Soul Rend cannot be triggered during the anti-heal period. Cooldown: 20s.");
		mInfo.mDescriptions.add("You deal +80% melee damage instead.");
		mInfo.mCooldown = COOLDOWN;
		mInfo.mLinkedSpell = Spells.DARK_PACT;
		mInfo.mTrigger = AbilityTrigger.LEFT_CLICK;
		mInfo.mIgnoreCooldown = true;
		mPercentDamageDealt = getAbilityScore() == 1 ? PERCENT_DAMAGE_DEALT_1 : PERCENT_DAMAGE_DEALT_2;
	}

	@Override
	public void cast(Action action) {
		if (mPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), mInfo.mLinkedSpell) || !InventoryUtils.isScytheItem(mPlayer.getInventory().getItemInMainHand())) {
			return;
		}
		mLeftClicks++;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (mLeftClicks > 0) {
					mLeftClicks--;
				}
				this.cancel();
			}
		}.runTaskLater(mPlugin, 5);
		if (mLeftClicks < 2) {
			return;
		}

		World world = mPlayer.getWorld();
		world.spawnParticle(Particle.SPELL_WITCH, mPlayer.getLocation(), 50, 0.2, 0.1, 0.2, 1);
		world.playSound(mPlayer.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.5f, 1.25f);
		world.playSound(mPlayer.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundCategory.PLAYERS, 1, 0.5f);

		mPlugin.mEffectManager.addEffect(mPlayer, PERCENT_DAMAGE_DEALT_EFFECT_NAME, new PercentDamageDealt(DURATION, mPercentDamageDealt, ALLOWED_DAMAGE_CAUSES));
		mPlugin.mEffectManager.addEffect(mPlayer, PERCENT_HEAL_EFFECT_NAME, new PercentHeal(DURATION, PERCENT_HEAL));
		mPlugin.mEffectManager.addEffect(mPlayer, PERCENT_DAMAGE_RESIST_EFFECT_NAME, new PercentDamageReceived(DURATION, PERCENT_DAMAGE_RESIST));
		mPlugin.mEffectManager.addEffect(mPlayer, AESTHETICS_EFFECT_NAME, new Aesthetics(DURATION,
				(entity, fourHertz, twoHertz, oneHertz) -> {
					world.spawnParticle(Particle.SPELL_WITCH, entity.getLocation(), 3, 0.2, 0.2, 0.2, 0.2);
				},
				(entity) -> {
					world.playSound(entity.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.3f, 0.75f);
				}));

		putOnCooldown();
	}

	@Override
	public void entityDeathEvent(EntityDeathEvent event, boolean shouldGenDrops) {
		NavigableSet<Effect> aestheticsEffects = mPlugin.mEffectManager.getEffects(mPlayer, AESTHETICS_EFFECT_NAME);
		if (aestheticsEffects != null) {
			AbsorptionUtils.addAbsorption(mPlayer, ABSORPTION_ON_KILL, MAX_ABSORPTION, aestheticsEffects.last().getDuration());
			for (Effect effect : aestheticsEffects) {
				effect.setDuration(effect.getDuration() + DURATION_INCREASE_ON_KILL);
			}
		}
		NavigableSet<Effect> percentDamageEffects = mPlugin.mEffectManager.getEffects(mPlayer, PERCENT_DAMAGE_DEALT_EFFECT_NAME);
		if (percentDamageEffects != null) {
			for (Effect effect : percentDamageEffects) {
				effect.setDuration(effect.getDuration() + DURATION_INCREASE_ON_KILL);
			}
		}
	}
}
