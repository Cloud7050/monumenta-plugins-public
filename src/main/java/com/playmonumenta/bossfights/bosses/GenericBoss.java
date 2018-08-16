package com.playmonumenta.bossfights.bosses;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.LivingEntity;

import com.playmonumenta.bossfights.BossBarManager;
import com.playmonumenta.bossfights.Plugin;

public class GenericBoss extends BossAbilityGroup
{
	public static final String identityTag = "boss_generic";
	public static final int detectionRange = 40;

	LivingEntity mBoss;

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception
	{
		return new GenericBoss(plugin, boss);
	}

	public GenericBoss(Plugin plugin, LivingEntity boss)
	{
		mBoss = boss;

		BossBarManager bossBar = new BossBarManager(boss, detectionRange, BarColor.WHITE, BarStyle.SOLID, null);

		super.constructBoss(plugin, identityTag, mBoss, null, null, detectionRange, bossBar);
	}
}
