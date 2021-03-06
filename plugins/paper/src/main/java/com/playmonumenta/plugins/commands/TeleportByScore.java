package com.playmonumenta.plugins.commands;

import java.util.LinkedHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.playmonumenta.plugins.utils.ScoreboardUtils;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.EntitySelectorArgument.EntitySelector;
import dev.jorel.commandapi.arguments.FloatArgument;
import dev.jorel.commandapi.arguments.ObjectiveArgument;

public class TeleportByScore extends GenericCommand {
	private static final String COMMAND = "teleportbyscore";

	public static void register() {
		CommandPermission perms = CommandPermission.fromString("monumenta.command.teleportbyscore");

		LinkedHashMap<String, Argument> arguments = new LinkedHashMap<>();
		arguments.put("entity", new EntitySelectorArgument(EntitySelector.ONE_ENTITY));
		arguments.put("x objective", new ObjectiveArgument());
		arguments.put("y objective", new ObjectiveArgument());
		arguments.put("z objective", new ObjectiveArgument());
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				teleport(sender, (Entity)args[0], (String)args[1], (String)args[2], (String)args[3], null, null, 1.0f);
			})
			.register();

		arguments.put("yaw objective", new ObjectiveArgument());
		arguments.put("pitch objective", new ObjectiveArgument());
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				teleport(sender, (Entity)args[0], (String)args[1], (String)args[2], (String)args[3], (String)args[4], (String)args[5], 1.0f);
			})
			.register();

		arguments.put("scale", new FloatArgument(1));
		new CommandAPICommand(COMMAND)
			.withPermission(perms)
			.withArguments(arguments)
			.executes((sender, args) -> {
				teleport(sender, (Entity)args[0], (String)args[1], (String)args[2], (String)args[3], (String)args[4], (String)args[5], (Float)args[6]);
			})
			.register();
	}

	@Nullable
	private static Integer getValue(@Nonnull Entity entity, @Nullable String obj) {
		if (obj == null || obj.equals("~")) {
			return null;
		}

		return ScoreboardUtils.getScoreboardValue(entity.getName(), obj).orElse(null);
	}

	private static void teleport(@Nonnull CommandSender sender, @Nonnull Entity entity, @Nonnull String objX, @Nonnull String objY, @Nonnull String objZ, @Nullable String objYaw, @Nullable String objPitch, float scale) {
		Integer x = getValue(entity, objX);
		Integer y = getValue(entity, objY);
		Integer z = getValue(entity, objZ);
		Integer yawNullable = getValue(entity, objYaw);
		Integer pitchNullable = getValue(entity, objPitch);

		if (x == null) {
			error(sender, "Could not get value " + objX);
			return;
		} else if (y == null) {
			error(sender, "Could not get value " + objY);
			return;
		} else if (z == null) {
			error(sender, "Could not get value " + objZ);
			return;
		}

		float yaw;
		float pitch;
		if (yawNullable == null) {
			yaw = entity.getLocation().getYaw();
		} else {
			yaw = (float)yawNullable / scale;
		}
		if (pitchNullable == null) {
			pitch = entity.getLocation().getPitch();
		} else {
			pitch = (float)pitchNullable / scale;
		}

		Location loc = entity.getLocation();
		float offset = (scale == 1 ? 0.5f : 0.0f);
		loc.setX((float)x / scale + offset);
		loc.setY((float)y / scale + 0.1);
		loc.setZ((float)z / scale + offset);
		loc.setPitch((float)pitch);
		loc.setYaw((float)yaw);
		if (entity instanceof Player) {
			((Player)entity).setSwimming(false);
		}
		if (entity instanceof Mob) {
			((Mob)entity).setVelocity(new Vector(0, 0.1, 0));
		}
		entity.teleport(loc);
	}
}

