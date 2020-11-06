package com.playmonumenta.plugins.effects;

import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public abstract class Effect implements Comparable<Effect> {

	private int mDuration;

	public Effect(int duration) {
		mDuration = duration;
	}

	public EffectPriority getPriority() {
		return EffectPriority.NORMAL;
	}

	public int getDuration() {
		return mDuration;
	}

	public void setDuration(int duration) {
		mDuration = duration;
	}

	public double getMagnitude() {
		return 0;
	}

	@Override
	public int compareTo(Effect otherEffect) {
		if (getMagnitude() > otherEffect.getMagnitude()) {
			return 1;
		} else if (getMagnitude() < otherEffect.getMagnitude()) {
			return -1;
		} else {
			return 0;
		}
	}

	public boolean entityRegainHealthEvent(EntityRegainHealthEvent event) {
		return true;
	}

	public boolean entityDealDamageEvent(EntityDamageByEntityEvent event) {
		return true;
	}

	public boolean entityReceiveDamageEvent(EntityDamageEvent event) {
		return true;
	}

	public void entityTickEffect(Entity entity, boolean fourHertz, boolean twoHertz, boolean oneHertz) { }

	public void entityGainEffect(Entity entity) { }

	public void entityLoseEffect(Entity entity) { }

	public boolean tick(int ticks) {
		mDuration -= ticks;
		return mDuration <= 0;
	}

	/* Must implement this method to print info about what the effect does for debug */
	public abstract String toString();
}
