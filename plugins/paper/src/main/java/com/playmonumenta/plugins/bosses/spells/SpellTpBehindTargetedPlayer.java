package com.playmonumenta.plugins.bosses.spells;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.ZoneUtils;
import com.playmonumenta.plugins.utils.ZoneUtils.ZoneProperty;

public class SpellTpBehindTargetedPlayer extends Spell {
	private static final int MAX_RANGE = 20;
	private static final int TP_DELAY = 50;
	private static final int TP_STUN = 10;					// Time after teleporting that the mob cannot perform actions
	private static final int TP_STUN_CREEPER = 30;			// Increased time for creepers
	private static final int DISTANCE_TO_PLAYER = 2 * 4;	// How many half-blocks behind the player maximum
	private static final int VERTICAL_DISTANCE_TO_PLAYER = 3;

	private final Plugin mPlugin;
	protected final Entity mLauncher;
	private final int mDuration;

	public SpellTpBehindTargetedPlayer(Plugin plugin, Entity launcher, int duration) {
		mPlugin = plugin;
		mLauncher = launcher;
		mDuration = duration;
	}

	@Override
	public void run() {
		if (mLauncher instanceof Mob) {
			LivingEntity target = ((Mob) mLauncher).getTarget();
			if (target instanceof Player && target.getLocation().distance(mLauncher.getLocation()) < MAX_RANGE
					&& !ZoneUtils.hasZoneProperty(target, ZoneProperty.RESIST_5)) {
				Player player = (Player) target;
				launch(player, MAX_RANGE);
				animation(player);
			}
		}
	}

	@Override
	public int duration() {
		return mDuration;
	}

	protected void launch(Player target, double maxRange) {
		BukkitRunnable runnable = new BukkitRunnable() {
			@Override
			public void run() {
				Location loc = target.getLocation();
				// We need a second check when the teleport is actually about to occur
				if (loc.distance(mLauncher.getLocation()) > maxRange || ZoneUtils.hasZoneProperty(target, ZoneProperty.RESIST_5)) {
					return;
				}

				World world = target.getWorld();
				loc.setY(loc.getY() + 0.1f);
				Vector shift = loc.getDirection();
				shift.setY(0).normalize().multiply(-0.5);

				// Check from farthest horizontally to closest, lowest vertically to highest
				for (int horizontalShift = DISTANCE_TO_PLAYER; horizontalShift > 0; horizontalShift--) {
					for (int verticalShift = 0; verticalShift <= VERTICAL_DISTANCE_TO_PLAYER; verticalShift++) {
						Location locTest = loc.clone().add(shift.clone().multiply(horizontalShift));
						locTest.setY(locTest.getY() + verticalShift);
						if (canTeleport(locTest)) {
							loc.add(0, mLauncher.getHeight() / 2, 0);
							world.spawnParticle(Particle.SPELL_WITCH, loc, 30, 0.25, 0.45, 0.25, 1);
							world.spawnParticle(Particle.SMOKE_LARGE, loc, 12, 0, 0.45, 0, 0.125);

							mLauncher.teleport(locTest);

							locTest.add(0, mLauncher.getHeight() / 2, 0);
							world.spawnParticle(Particle.SPELL_WITCH, locTest, 30, 0.25, 0.45, 0.25, 1);
							world.spawnParticle(Particle.SMOKE_LARGE, locTest, 12, 0, 0.45, 0, 0.125);
							world.playSound(locTest, Sound.ENTITY_ENDERMAN_TELEPORT, 3f, 0.7f);

							// The mPlugin here is of the incorrect type for some reason
							if (mLauncher instanceof Creeper) {
								EntityUtils.applyCooling(com.playmonumenta.plugins.Plugin.getInstance(), TP_STUN_CREEPER, (LivingEntity) mLauncher);
							} else if (mLauncher instanceof LivingEntity) {
								EntityUtils.applyCooling(com.playmonumenta.plugins.Plugin.getInstance(), TP_STUN, (LivingEntity) mLauncher);
							}

							// Janky way to break out of nested loop
							horizontalShift = 0;
							break;
						}
					}
				}
			}
		};

		runnable.runTaskLater(mPlugin, TP_DELAY);
		mActiveRunnables.add(runnable);
	}

	protected void animation(Player target) {
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITCH_AMBIENT, 1.4f, 0.5f);

		BukkitRunnable runnable = new BukkitRunnable() {
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
		};
		runnable.runTaskTimer(mPlugin, 0, 1);
		mActiveRunnables.add(runnable);
	}

	private boolean canTeleport(Location loc) {
		World world = loc.getWorld();

		// Move a bounding box to the target location
		BoundingBox box = mLauncher.getBoundingBox();
		box.shift(loc.clone().subtract(mLauncher.getLocation()));

		// Check the 8 corners of the bounding box and the location itself for blocks
		if (isObstructed(loc, box)
				|| isObstructed(new Location(world, box.getMinX(), box.getMinY(), box.getMinZ()), box)
				|| isObstructed(new Location(world, box.getMinX(), box.getMinY(), box.getMaxZ()), box)
				|| isObstructed(new Location(world, box.getMinX(), box.getMaxY(), box.getMinZ()), box)
				|| isObstructed(new Location(world, box.getMinX(), box.getMaxY(), box.getMaxZ()), box)
				|| isObstructed(new Location(world, box.getMaxX(), box.getMinY(), box.getMinZ()), box)
				|| isObstructed(new Location(world, box.getMaxX(), box.getMinY(), box.getMaxZ()), box)
				|| isObstructed(new Location(world, box.getMaxX(), box.getMaxY(), box.getMinZ()), box)
				|| isObstructed(new Location(world, box.getMaxX(), box.getMaxY(), box.getMaxZ()), box)) {
			return false;
		}

		return true;
	}

	private boolean isObstructed(Location loc, BoundingBox box) {
		Block block = loc.getBlock();
		return block.getBoundingBox().overlaps(box) && !block.isLiquid();
	}

}
