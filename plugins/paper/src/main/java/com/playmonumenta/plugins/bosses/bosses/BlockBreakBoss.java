package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.playmonumenta.plugins.bosses.spells.Spell;
import com.playmonumenta.plugins.bosses.spells.SpellBlockBreak;

public class BlockBreakBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_blockbreak";
	public static final int detectionRange = 40;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new BlockBreakBoss(plugin, boss);
	}

	public BlockBreakBoss(Plugin plugin, LivingEntity boss) {
		List<Spell> passiveSpells = Arrays.asList(new SpellBlockBreak(boss));

		super.constructBoss(plugin, identityTag, boss, null, passiveSpells, detectionRange, null);
	}
}
