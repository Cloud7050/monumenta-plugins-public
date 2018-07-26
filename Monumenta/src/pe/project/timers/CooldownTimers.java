package pe.project.timers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;

import pe.project.Plugin;
import pe.project.classes.Spells;
import pe.project.utils.MessagingUtils;

public class CooldownTimers {
	public HashMap<UUID, HashMap<Spells, Integer>> mTimers = null;
	private Plugin mPlugin = null;

	public CooldownTimers(Plugin plugin) {
		mPlugin = plugin;
		mTimers = new HashMap<UUID, HashMap<Spells, Integer>>();
	}

	public void RegisterCooldown(Player player, Spells spell, Integer cooldownTime) {
		HashMap<Spells, Integer> cd = new HashMap<Spells, Integer>();
		cd.put(spell, cooldownTime);
		mTimers.put(player.getUniqueId(), cd);
	}

	public boolean isAbilityOnCooldown(UUID playerID, Spells spell) {
		//	First check if the player has any cooldowns in the HashMap.
		HashMap<Spells, Integer> player = mTimers.get(playerID);
		if (player != null) {
			//	Next check if the ability is in our HashMap, if not we're not on cooldown.
			Integer ability = player.get(spell);
			if (ability == null) {
				return false;
			}
		}
		//	No player, means no cooldown.
		else {
			return false;
		}

		return true;
	}

	public boolean AddCooldown(UUID playerID, Spells spell, Integer cooldownTime) {
		//	First let's investigate whether this player already has existing cooldowns.
		HashMap<Spells, Integer> player = mTimers.get(playerID);
		//	Is there a player already storing cooldowns?
		if (player != null) {
			//	Next check to see if this abilityID already exist in this HashMap, if not than we're
			//	not on cooldown and we should put it on cooldown.
			Integer ability = player.get(spell);
			if (ability == null) {
				player.put(spell, cooldownTime);
				return true;
			}
		}
		//	Else add a new player entry with it's info.
		else {
			HashMap<Spells, Integer> cooldownHash = new HashMap<Spells, Integer>();

			cooldownHash.put(spell, cooldownTime);
			mTimers.put(playerID, cooldownHash);

			return true;
		}

		return false;
	}

	public void removeCooldown(UUID playerID, Spells spell) {
		HashMap<Spells, Integer> cooldownHash = mTimers.get(playerID);
		if (cooldownHash != null) {
			cooldownHash.remove(spell);
		}
	}

	public void UpdateCooldowns(Integer ticks) {
		//	Our set of player cooldowns is broken down into a Hashmap of Hashmaps.
		//	Because of this, we first loop through each player (UUID), than we loop
		//	through their different ability ID's.
		Iterator<Entry<UUID, HashMap<Spells, Integer>>> playerIter = mTimers.entrySet().iterator();
		while (playerIter.hasNext()) {
			Entry<UUID, HashMap<Spells, Integer>> player = playerIter.next();

		    Iterator<Entry<Spells, Integer>> abilityIter = player.getValue().entrySet().iterator();
		    while(abilityIter.hasNext()) {
		    	Entry<Spells, Integer> cooldown = abilityIter.next();

		    	Player _player = mPlugin.getPlayer(player.getKey());
		    	if (_player != null && _player.isOnline()) {
			    	//	Update the cooldown time, if it's not over, set the value, else remove it.
			    	int time = cooldown.getValue() - ticks;
			    	if (time <= 0) {
			    		Spells spell = cooldown.getKey();

			    		if (!spell.isFake()) {
			    			MessagingUtils.sendActionBarMessage(mPlugin, _player, spell.getName() + " is now off cooldown!");
			    		} else {
			    			mPlugin.getClass(_player).FakeAbilityOffCooldown(_player, spell);
			    		}

			    		abilityIter.remove();
			    	} else {
			    		cooldown.setValue(time);
			    	}
		    	}
		    }

		    //	If this player no longer has any more cooldowns for them, remove the player.
		    if (player.getValue().isEmpty()) {
		    	playerIter.remove();
		    }
		}
	}

	public void removeAllCooldowns(UUID playerID) {
		mTimers.remove(playerID);
	}
}