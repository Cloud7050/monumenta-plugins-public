package com.playmonumenta.plugins.abilities.scout;

import org.bukkit.World;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.utils.MessagingUtils;

public class Sharpshooter extends Ability {
	private static final int SHARPSHOOTER_DECAY_TIMER = 20 * 4;
	private static final int MAX_STACKS_1 = 5;
	private static final int MAX_STACKS_2 = 15;
	private static final int MAX_EFFECTIVE_STACKS = 10;
	private static final double PERCENT_DAMAGE_PER_STACK = 0.07;

	private final int mMaxStacks;

	public Sharpshooter(Plugin plugin, World world, Player player) {
		super(plugin, world, player, "Sharpshooter");
		mInfo.mScoreboardId = "Sharpshooter";
		mInfo.mShorthandName = "Ss";
		mInfo.mDescriptions.add("Each enemy hit with a critical arrow gives you a stack of Sharpshooter, up to 5. Stacks decay after 4 seconds of not gaining a stack. Each stack makes your arrows deal +7% damage.");
		mInfo.mDescriptions.add("Max stacks increased to 15, but the damage bonus is capped at 10 stacks (+70% damage)");
		mInfo.mIgnoreTriggerCap = true;

		mMaxStacks = getAbilityScore() == 1 ? MAX_STACKS_1 : MAX_STACKS_2;
	}

	private int mStacks = 0;
	private int mTicksToStackDecay = 0;

	@Override
	public boolean livingEntityShotByPlayerEvent(Projectile proj, LivingEntity damagee, EntityDamageByEntityEvent event) {
		if (proj instanceof Arrow || proj instanceof SpectralArrow) {
			AbstractArrow arrow = (AbstractArrow) proj;

			// Critical arrow and mob is actually going to take damage
			if (arrow.isCritical() && (damagee.getNoDamageTicks() <= 10 || damagee.getLastDamage() < event.getDamage())) {
				mTicksToStackDecay = SHARPSHOOTER_DECAY_TIMER;

				if (mStacks < mMaxStacks) {
					mStacks++;
					MessagingUtils.sendActionBarMessage(mPlugin, mPlayer, "Sharpshooter Stacks: " + mStacks);
				}
			}

			event.setDamage(event.getDamage() * (1 + Math.min(MAX_EFFECTIVE_STACKS, mStacks) * PERCENT_DAMAGE_PER_STACK));
		}

		return true;
	}

	@Override
	public void periodicTrigger(boolean fourHertz, boolean twoHertz, boolean oneSecond, int ticks) {
		if (mStacks > 0) {
			mTicksToStackDecay -= 5;

			if (mTicksToStackDecay <= 0) {
				mTicksToStackDecay = SHARPSHOOTER_DECAY_TIMER;
				mStacks--;
				MessagingUtils.sendActionBarMessage(mPlugin, mPlayer, "Sharpshooter Stacks: " + mStacks);
			}
		}
	}

	public static void addStacks(Plugin plugin, Player player, int stacks) {
		Sharpshooter ss = AbilityManager.getManager().getPlayerAbility(player, Sharpshooter.class);
		if (ss != null) {
			ss.mStacks = Math.min(ss.mMaxStacks, ss.mStacks + stacks);
			MessagingUtils.sendActionBarMessage(plugin, player, "Sharpshooter Stacks: " + ss.mStacks);
		}
	}

	public static double getDamageMultiplier(Player player) {
		Sharpshooter ss = AbilityManager.getManager().getPlayerAbility(player, Sharpshooter.class);
		return ss == null ? 1 : (1 + Math.min(MAX_EFFECTIVE_STACKS, ss.mStacks) * PERCENT_DAMAGE_PER_STACK);
	}

}
