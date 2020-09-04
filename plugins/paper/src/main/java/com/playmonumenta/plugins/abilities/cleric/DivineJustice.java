package com.playmonumenta.plugins.abilities.cleric;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;

public class DivineJustice extends Ability {

	private static final int CRITICAL_UNDEAD_DAMAGE_1 = 5;
	private static final int CRITICAL_UNDEAD_DAMAGE_2 = 8;
	private static final int ON_UNDEAD_KILL_HEAL = 2;

	private final int mCriticalUndeadDamage;

	public DivineJustice(Plugin plugin, World world, Player player) {
		super(plugin, world, player, "Divine Justice");
		mInfo.mScoreboardId = "DivineJustice";
		mInfo.mShorthandName = "DJ";
		mInfo.mDescriptions.add("Your critical strikes deal +5 damage to undead enemies.");
		mInfo.mDescriptions.add("Your critical strikes deal +8 damage to undead enemies. Additionally, heal 2 health whenever you kill an undead enemy.");
		mCriticalUndeadDamage = getAbilityScore() == 1 ? CRITICAL_UNDEAD_DAMAGE_1 : CRITICAL_UNDEAD_DAMAGE_2;
	}

	@Override
	public boolean livingEntityDamagedByPlayerEvent(EntityDamageByEntityEvent event) {
		if (event.getCause() == DamageCause.ENTITY_ATTACK && PlayerUtils.isCritical(mPlayer)) {
			LivingEntity damagee = (LivingEntity) event.getEntity();
			if (EntityUtils.isUndead(damagee)) {
				Location loc = damagee.getLocation().add(0, damagee.getHeight() / 2, 0);
				double xz = damagee.getWidth() / 2 + 0.1;
				double y = damagee.getHeight() / 3;
				mWorld.spawnParticle(Particle.END_ROD, loc, 5, xz, y, xz, 0.065);
				mWorld.spawnParticle(Particle.FLAME, loc, 6, xz, y, xz, 0.05);
				mWorld.playSound(loc, Sound.BLOCK_ANVIL_LAND, 0.15f, 1.5f);

				event.setDamage(event.getDamage() + mCriticalUndeadDamage);
			}
		}

		return true;
	}

	@Override
	public void entityDeathEvent(EntityDeathEvent event, boolean shouldGenDrops) {
		if (EntityUtils.isUndead(event.getEntity())) {
			PlayerUtils.healPlayer(mPlayer, ON_UNDEAD_KILL_HEAL);
		}
	}

}
