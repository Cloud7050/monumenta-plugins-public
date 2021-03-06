package com.playmonumenta.plugins.integrations;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;

public class CoreProtectIntegration {
	private static CoreProtectAPI API = null;

	public CoreProtectIntegration(Logger logger) {
		logger.info("Enabling CoreProtect integration");

		Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("CoreProtect");

		// Check that CoreProtect is loaded
		if (plugin == null || !(plugin instanceof CoreProtect)) {
			return;
		}

		// Check that the API is enabled
		CoreProtectAPI coreProtect = ((CoreProtect) plugin).getAPI();
		if (coreProtect.isEnabled() == false) {
			return;
		}

		// Check that a compatible version of the API is loaded
		if (coreProtect.APIVersion() < 6) {
			return;
		}

		API = coreProtect;
	}

	public static void logContainerTransaction(Player player, Location location) {
		if (API != null) {
			API.logContainerTransaction(player.getName(), location);
		}
	}

	public static void logPlacement(Player player, Location location, Material type, BlockData blockData) {
		if (API != null) {
			API.logPlacement(player.getName(), location, type, blockData);
		}
	}
}
