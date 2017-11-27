package pe.project.network.packet;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import pe.project.Constants;
import pe.project.Plugin;
import pe.project.utils.PacketUtils;

public class ForwardErrorPacket implements Packet {

	// TODO - Ugh, this is so annoying
	// Want to just be able to call TransferPlayerDataPacket.getPacketChannel() without an object
	// But making that static just causes other problems
	public static String getStaticPacketChannel() {
		return "Monumenta.Bungee.Error.Forward";
	}

	@Override
	public String getPacketChannel() {
		return "Monumenta.Bungee.Error.Forward";
	}

	@Override
	public String getPacketData() throws Exception {
		throw new Exception("This packet is generated by bungeecord!");
	}

	public static void handlePacket(Plugin plugin, String data) throws Exception {
		String[] rcvStrings = PacketUtils.decodeStrings(data);
		if (rcvStrings == null || rcvStrings.length == 0) {
			throw new Exception("Received string data is null or invalid length");
		}

		String failedChannel = rcvStrings[0];
		if (failedChannel.equals(TransferPlayerDataPacket.getStaticPacketChannel())) {
			// Failed to transfer the player to the requested server
			// Notify player and unfreeze their inventory
			String server = rcvStrings[1];
			UUID playerUUID = UUID.fromString(rcvStrings[3]);

			Player player = plugin.getPlayer(playerUUID);
			if (player != null) {
				player.sendMessage(ChatColor.RED + "Bungee reports server '" + server + "' is not available!");

				// Remove the metadata that prevents player from interacting with things (if present)
				player.removeMetadata(Constants.PLAYER_ITEMS_LOCKED_METAKEY, plugin);
			}
		}
	}
}
