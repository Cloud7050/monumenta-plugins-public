package com.playmonumenta.plugins.bosses.spells;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.ZoneUtils;
import com.playmonumenta.plugins.utils.ZoneUtils.ZoneProperty;

public class SpellTpSwapPlaces extends Spell {
	private static final int MAX_RANGE = 16;
	private static final int TP_DELAY = 50;

	private final Plugin mPlugin;
	private final Entity mLauncher;
	private final int mDuration;

	public SpellTpSwapPlaces(Plugin plugin, Entity launcher, int duration) {
		mPlugin = plugin;
		mLauncher = launcher;
		mDuration = duration;
	}

	@Override
	public void run() {
		List<Player> players = PlayerUtils.playersInRange(mLauncher.getLocation(), MAX_RANGE);
		while (!players.isEmpty()) {
			Player target = players.get(FastUtils.RANDOM.nextInt(players.size()));

			/* Do not teleport to players in safezones */
			if (ZoneUtils.hasZoneProperty(target, ZoneProperty.RESIST_5)) {
				/* This player is in a safe area - don't tp to them */
				players.remove(target);
			} else {
				launch(target);
				animation(target);
				break;
			}
		}
	}

	@Override
	public int duration() {
		return mDuration;
	}

	private void launch(Player target) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (target.getLocation().distance(mLauncher.getLocation()) > MAX_RANGE) {
					return;
				}

				// set targetLoc to target and slightly elevate it
				Location targetLoc = target.getLocation();
				targetLoc.setY(target.getLocation().getY() + 0.1f);
				// set mobLoc to mLauncher and slightly elevate it
				Location mobLoc = mLauncher.getLocation();
				mobLoc.setY(mLauncher.getLocation().getY() + 0.1f);
				World world = mLauncher.getWorld();

				world.spawnParticle(Particle.SPELL_WITCH, mLauncher.getLocation().add(0, mLauncher.getHeight() / 2, 0), 30, 0.25, 0.45, 0.25, 1);
				world.spawnParticle(Particle.SMOKE_LARGE, mLauncher.getLocation().add(0, mLauncher.getHeight() / 2, 0), 12, 0, 0.45, 0, 0.125);
				mLauncher.teleport(targetLoc);
				target.teleport(mobLoc);
				world.spawnParticle(Particle.SPELL_WITCH, targetLoc.clone().add(0, mLauncher.getHeight() / 2, 0), 30, 0.25, 0.45, 0.25, 1);
				world.spawnParticle(Particle.SMOKE_LARGE, targetLoc.clone().add(0, mLauncher.getHeight() / 2, 0), 12, 0, 0.45, 0, 0.125);
				mLauncher.getWorld().playSound(mLauncher.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 3f, 0.7f);
			}
		}.runTaskLater(mPlugin, TP_DELAY);
	}

	private void animation(Player target) {
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 1.4f, 0.5f);

		new BukkitRunnable() {
			int mTicks = 0;

			@Override
			public void run() {
				mTicks++;
				Location particleLoc = mLauncher.getLocation().add(new Location(mLauncher.getWorld(), -0.5f, 0f, 0.5f));
				particleLoc.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 10, 1, 1, 1, 0.03);

				if (mTicks > TP_DELAY) {
					this.cancel();
				}
			}
		}.runTaskTimer(mPlugin, 0, 1);
	}
}
