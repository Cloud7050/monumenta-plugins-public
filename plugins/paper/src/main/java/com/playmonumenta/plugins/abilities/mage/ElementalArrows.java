package com.playmonumenta.plugins.abilities.mage;

import org.bukkit.Particle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Stray;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.classes.Spells;
import com.playmonumenta.plugins.classes.magic.MagicType;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PotionUtils;

public class ElementalArrows extends Ability {

	private static final int ELEMENTAL_ARROWS_1_DAMAGE = 1;
	private static final int ELEMENTAL_ARROWS_2_DAMAGE = 3;
	private static final int ELEMENTAL_ARROWS_BONUS_DAMAGE = 8;
	private static final double ELEMENTAL_ARROWS_2_RADIUS = 3.0;
	private static final int ELEMENTAL_ARROWS_DURATION_SECONDS = 6;
	private static final int ELEMENTAL_ARROWS_DURATION = ELEMENTAL_ARROWS_DURATION_SECONDS * 20;

	public ElementalArrows(Plugin plugin, Player player) {
		super(plugin, player, "Elemental Arrows");
		mInfo.mLinkedSpell = Spells.ELEMENTAL_ARROWS;
		mInfo.mScoreboardId = "Elemental";
		mInfo.mShorthandName = "EA";
		mInfo.mDescriptions.add(
			String.format(
				"Your arrows deal %s extra damage. They turn into fire arrows, dealing another %s bonus damage to strays. Shooting while sneaking instead turns them into ice arrows, dealing another %3$s bonus damage if the enemy is a blaze. Based on the type of arrow, they also apply a %ss effect on the enemy - fire arrows set the enemy on fire while ice arrows apply slowness II. This skill cannot apply but can trigger Spellshock's \"static\".",
				ELEMENTAL_ARROWS_1_DAMAGE,
				ELEMENTAL_ARROWS_DURATION_SECONDS,
				ELEMENTAL_ARROWS_BONUS_DAMAGE
			) // Amplifier has no constant
		);
		mInfo.mDescriptions.add(
			String.format(
				"Extra damage is increased from %s to %s. This extra damage and the applied effect now also affects all enemies within %s blocks of the enemy.",
				ELEMENTAL_ARROWS_1_DAMAGE,
				ELEMENTAL_ARROWS_2_DAMAGE,
				ELEMENTAL_ARROWS_2_RADIUS
			)
		);
	}

	@Override
	public boolean livingEntityShotByPlayerEvent(Projectile proj, LivingEntity damagee, EntityDamageByEntityEvent event) {
		if (!(proj instanceof Arrow)) {
			return true;
		}
		Arrow arrow = (Arrow) proj;

		int elementalArrows = getAbilityScore();
		int damage = elementalArrows == 1 ? ELEMENTAL_ARROWS_1_DAMAGE : ELEMENTAL_ARROWS_2_DAMAGE;
		if (arrow.hasMetadata("ElementalArrowsFireArrow")) {
			if (elementalArrows > 1) {
				for (LivingEntity mob : EntityUtils.getNearbyMobs(damagee.getLocation(), ELEMENTAL_ARROWS_2_RADIUS, damagee)) {
					EntityUtils.damageEntity(mPlugin, mob, damage, mPlayer, MagicType.FIRE, true, mInfo.mLinkedSpell, false, true);
					EntityUtils.applyFire(mPlugin, ELEMENTAL_ARROWS_DURATION, mob, mPlayer);
				}
			}
			if (damagee instanceof Stray) {
				damage += ELEMENTAL_ARROWS_BONUS_DAMAGE;
			}

			EntityUtils.damageEntity(mPlugin, damagee, damage, mPlayer, MagicType.FIRE, true, mInfo.mLinkedSpell, false, true);
			EntityUtils.applyFire(mPlugin, ELEMENTAL_ARROWS_DURATION, damagee, mPlayer);
		} else if (arrow.hasMetadata("ElementalArrowsIceArrow")) {
			if (elementalArrows > 1) {
				for (LivingEntity mob : EntityUtils.getNearbyMobs(damagee.getLocation(), ELEMENTAL_ARROWS_2_RADIUS, damagee)) {
					EntityUtils.damageEntity(mPlugin, mob, damage, mPlayer, MagicType.ICE, true, mInfo.mLinkedSpell, false, true);
					PotionUtils.applyPotion(mPlayer, mob, new PotionEffect(PotionEffectType.SLOW, ELEMENTAL_ARROWS_DURATION, 1));
				}
			}
			if (damagee instanceof Blaze) {
				damage += ELEMENTAL_ARROWS_BONUS_DAMAGE;
			}

			EntityUtils.damageEntity(mPlugin, damagee, damage, mPlayer, MagicType.ICE, true, mInfo.mLinkedSpell, false, true);
			PotionUtils.applyPotion(mPlayer, damagee, new PotionEffect(PotionEffectType.SLOW, ELEMENTAL_ARROWS_DURATION, 1));
		}

		return true;
	}

	@Override
	public boolean playerShotArrowEvent(AbstractArrow arrow) {
		if (mPlayer.isSneaking()) {
			arrow.setMetadata("ElementalArrowsIceArrow", new FixedMetadataValue(mPlugin, 0));
			arrow.setFireTicks(0);
			mPlugin.mProjectileEffectTimers.addEntity(arrow, Particle.SNOW_SHOVEL);
		} else {
			arrow.setMetadata("ElementalArrowsFireArrow", new FixedMetadataValue(mPlugin, 0));
			arrow.setFireTicks(ELEMENTAL_ARROWS_DURATION);
			mPlugin.mProjectileEffectTimers.addEntity(arrow, Particle.FLAME);
		}

		return true;
	}
}
