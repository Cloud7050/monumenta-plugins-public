package com.playmonumenta.plugins.abilities.warlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityTrigger;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.ItemUtils;
import com.playmonumenta.plugins.utils.MovementUtils;
import com.playmonumenta.plugins.utils.PotionUtils;

public class GraspingClaws extends Ability {

	private static final int RADIUS = 8;
	private static final float PULL_SPEED = 0.175f;
	private static final int AMPLIFIER_1 = 1;
	private static final int AMPLIFIER_2 = 2;
	private static final int DAMAGE_1 = 3;
	private static final int DAMAGE_2 = 8;
	private static final int DURATION = 8 * 20;
	private static final int COOLDOWN_1 = 16 * 20;
	private static final int COOLDOWN_2 = 12 * 20;

	private final int mAmplifier;
	private final int mDamage;
	private Arrow mArrow = null;

	public GraspingClaws(Plugin plugin, Player player) {
		super(plugin, player, "Grasping Claws");
		mInfo.mScoreboardId = "GraspingClaws";
		mInfo.mShorthandName = "GC";
		mInfo.mDescriptions.add("Left-clicking while shifted while holding a bow fires an arrow that pulls nearby enemies towards your arrow once it makes contact with a mob or block. Mobs caught in the arrow's 8 block radius are given Slowness 2 for 8 seconds and take 3 damage. Cooldown: 16s.");
		mInfo.mDescriptions.add("The pulled enemies now take 8 damage, and Slowness is increased to 3.");
		mInfo.mLinkedSpell = Spells.GRASPING_CLAWS;
		mInfo.mCooldown = getAbilityScore() == 1 ? COOLDOWN_1 : COOLDOWN_2;
		mInfo.mTrigger = AbilityTrigger.LEFT_CLICK;
		mInfo.mIgnoreCooldown = true;
		mAmplifier = getAbilityScore() == 1 ? AMPLIFIER_1 : AMPLIFIER_2;
		mDamage = getAbilityScore() == 1 ? DAMAGE_1 : DAMAGE_2;
	}

	@Override
	public void cast(Action action) {
		ItemStack inMainHand = mPlayer.getInventory().getItemInMainHand();
		if (!mPlugin.mTimers.isAbilityOnCooldown(mPlayer.getUniqueId(), Spells.GRASPING_CLAWS) && mPlayer.isSneaking() && InventoryUtils.isBowItem(inMainHand) && !ItemUtils.isItemShattered(inMainHand)) {
			mArrow = mPlayer.launchProjectile(Arrow.class);
			mArrow.setDamage(0);
			mArrow.setVelocity(mPlayer.getLocation().getDirection().multiply(1.5));
			mPlugin.mProjectileEffectTimers.addEntity(mArrow, Particle.SPELL_WITCH);
			putOnCooldown();
		}
	}

	@Override
	public void projectileHitEvent(ProjectileHitEvent event, Projectile proj) {
		if (this.mArrow != null && this.mArrow == proj) {
			this.mArrow = null;
			Location loc = proj.getLocation();
			World world = proj.getWorld();

			world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1.25f, 1.25f);
			world.playSound(loc, Sound.BLOCK_PORTAL_TRIGGER, 1.25f, 1.45f);
			world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_HURT, 1.25f, 0.65f);
			world.spawnParticle(Particle.PORTAL, loc, 125, 2, 2, 2, 0.25);
			world.spawnParticle(Particle.PORTAL, loc, 400, 0, 0, 0, 1.45);
			world.spawnParticle(Particle.DRAGON_BREATH, loc, 85, 0, 0, 0, 0.125);
			world.spawnParticle(Particle.FALLING_DUST, loc, 150, 2, 2, 2, Material.ANVIL.createBlockData());

			for (LivingEntity mob : EntityUtils.getNearbyMobs(loc, RADIUS, mPlayer)) {
				EntityUtils.damageEntity(mPlugin, mob, mDamage, mPlayer, MagicType.DARK_MAGIC, true, mInfo.mLinkedSpell);
				MovementUtils.pullTowards(proj, mob, PULL_SPEED);
				PotionUtils.applyPotion(mPlayer, mob, new PotionEffect(PotionEffectType.SLOW, DURATION, mAmplifier, false, true));
			}

			proj.remove();
		}
	}

}
