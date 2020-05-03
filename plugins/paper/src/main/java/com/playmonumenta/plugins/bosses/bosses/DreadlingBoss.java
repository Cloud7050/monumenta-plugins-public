package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

import com.playmonumenta.plugins.abilities.delves.twisted.Dreadful;
import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellDreadlingParticle;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.MovementUtils;

public class DreadlingBoss extends BossAbilityGroup {
	public static final String identityTag = Dreadful.DREADFUL_DREADLING_TAG;
	public static final int detectionRange = 50;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new DreadlingBoss(plugin, boss);
	}

	public DreadlingBoss(Plugin plugin, LivingEntity boss) throws Exception {
		List<Spell> passiveSpells = Arrays.asList(
			new SpellDreadlingParticle(boss)
		);

		super.constructBoss(plugin, identityTag, boss, null, passiveSpells, detectionRange, null);
	}

	@Override
	public void bossDamagedEntity(EntityDamageByEntityEvent event) {
		LivingEntity target = (LivingEntity) event.getEntity();
		Location loc = target.getLocation();

		LivingEntity dreadnaught = null;
		double dreadnaughtDistance = Double.POSITIVE_INFINITY;
		for (LivingEntity mob : EntityUtils.getNearbyMobs(loc, 16)) {
			if (mob.getScoreboardTags().contains(Dreadful.DREADFUL_DREADNAUGHT_TAG)) {
				double distance = loc.distance(mob.getLocation());
				if (distance < dreadnaughtDistance) {
					dreadnaughtDistance = distance;
					dreadnaught = mob;
				}
			}
		}

		if (dreadnaught != null) {
			MovementUtils.pullTowards(dreadnaught, target, 0.5f);
		}
	}
}
