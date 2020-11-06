package com.playmonumenta.plugins.effects;

import java.util.EnumSet;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class FlatDamageDealt extends Effect {

	private final double mAmount;
	private final EnumSet<EntityDamageEvent.DamageCause> mAffectedDamageCauses;

	public FlatDamageDealt(int duration, double amount, EnumSet<EntityDamageEvent.DamageCause> affectedDamageCauses) {
		super(duration);
		mAmount = amount;
		mAffectedDamageCauses = affectedDamageCauses;
	}

	public FlatDamageDealt(int duration, double amount) {
		this(duration, amount, null);
	}

	@Override
	public double getMagnitude() {
		return Math.abs(mAmount);
	}

	@Override
	public boolean entityDealDamageEvent(EntityDamageByEntityEvent event) {
		if (mAffectedDamageCauses == null || mAffectedDamageCauses.contains(event.getCause())) {
			event.setDamage(event.getDamage() + mAmount);
		}

		return true;
	}

	@Override
	public String toString() {
		String causes = "any";
		if (mAffectedDamageCauses != null) {
			causes = "";
			for (EntityDamageEvent.DamageCause cause : mAffectedDamageCauses) {
				if (!causes.isEmpty()) {
					causes += ",";
				}
				causes += cause.name();
			}
		}
		return String.format("FlatDamageDealt duration:%d causes:%s amount:%f", this.getDuration(), causes, mAmount);
	}
}
