package com.playmonumenta.plugins.bosses.spells.varcosamist;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.bosses.spells.Spell;

public class SpellPurgeGlowing extends Spell {

	private int mCooldown = 0;
	private LivingEntity mBoss;
	private int mTimer;

	public SpellPurgeGlowing(LivingEntity boss, int timer) {
		mBoss = boss;
		mTimer = timer;
	}

	@Override
	public void run() {
		mCooldown -= 5;
		if (mCooldown <= 0) {
			mCooldown = mTimer;
			mBoss.removePotionEffect(PotionEffectType.GLOWING);
			mBoss.addScoreboardTag("HiddenMob");
		}
	}

	@Override
	public int duration() {
		// TODO Auto-generated method stub
		return 0;
	}
}
