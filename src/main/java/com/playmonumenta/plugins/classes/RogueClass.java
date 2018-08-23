package com.playmonumenta.plugins.classes;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.managers.potion.PotionManager.PotionID;
import com.playmonumenta.plugins.point.Raycast;
import com.playmonumenta.plugins.point.RaycastData;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.LocationUtils;
import com.playmonumenta.plugins.utils.MessagingUtils;
import com.playmonumenta.plugins.utils.MetadataUtils;
import com.playmonumenta.plugins.utils.MovementUtils;
import com.playmonumenta.plugins.utils.ParticleUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import com.playmonumenta.plugins.utils.ScoreboardUtils;
import com.playmonumenta.plugins.utils.particlelib.ParticleEffect;
import com.playmonumenta.plugins.utils.particlelib.ParticleEffect.OrdinaryColor;

/*
    ByMyBlade
    AdvancingShadows
    Dodging
    EscapeDeath
    ViciousCombos
    SmokeScreen
*/

public class RogueClass extends BaseClass {
	private static final double PASSIVE_DAMAGE_ELITE_MODIFIER = 2.0;
	private static final double PASSIVE_DAMAGE_BOSS_MODIFIER = 1.5;

	private static final int BY_MY_BLADE_HASTE_1_LVL = 1;
	private static final int BY_MY_BLADE_HASTE_2_LVL = 3;
	private static final int BY_MY_BLADE_HASTE_DURATION = 4 * 20;
	private static final double BY_MY_BLADE_DAMAGE_1 = 12;
	private static final double BY_MY_BLADE_DAMAGE_2 = 24;
	private static final int BY_MY_BLADE_COOLDOWN = 10 * 20;

	private static final int ADVANCING_SHADOWS_RANGE_1 = 11;
	private static final int ADVANCING_SHADOWS_RANGE_2 = 16;
	private static final float ADVANCING_SHADOWS_AOE_KNOCKBACKS_SPEED = 0.5f;
	private static final float ADVANCING_SHADOWS_AOE_KNOCKBACKS_RANGE = 4;
	private static final double ADVANCING_SHADOWS_OFFSET = 2.7;
	private static final int ADVANCING_SHADOWS_STRENGTH_DURATION = 5 * 20;
	private static final int ADVANCING_SHADOWS_STRENGTH_EFFECT_LEVEL = 1;
	private static final int ADVANCING_SHADOWS_COOLDOWN = 20 * 20;

	private static final int DODGING_SPEED_EFFECT_DURATION = 15 * 20;
	private static final int DODGING_SPEED_EFFECT_LEVEL = 0;
	private static final int DODGING_COOLDOWN_1 = 12 * 20;
	private static final int DODGING_COOLDOWN_2 = 10 * 20;

	private static final double ESCAPE_DEATH_HEALTH_TRIGGER = 10;
	private static final int ESCAPE_DEATH_DURATION = 5 * 20;
	private static final int ESCAPE_DEATH_DURATION_OTHER = 8 * 20;
	private static final int ESCAPE_DEATH_ABSORBTION_EFFECT_LVL = 1;
	private static final int ESCAPE_DEATH_SPEED_EFFECT_LVL = 1;
	private static final int ESCAPE_DEATH_JUMP_EFFECT_LVL = 2;
	private static final int ESCAPE_DEATH_RANGE = 5;
	private static final int ESCAPE_DEATH_DURATION_SLOWNESS = 5 * 20;
	private static final int ESCAPE_DEATH_SLOWNESS_EFFECT_LVL = 4;
	private static final int ESCAPE_DEATH_WEAKNES_EFFECT_LEVEL = 2;
	private static final int ESCAPE_DEATH_COOLDOWN = 90 * 20;

	private static final int VICIOUS_COMBOS_RANGE = 5;
	private static final int VICIOUS_COMBOS_DAMAGE = 24;
	private static final int VICIOUS_COMBOS_EFFECT_DURATION = 15 * 20;
	private static final int VICIOUS_COMBOS_EFFECT_LEVEL = 0;
	private static final int VICIOUS_COMBOS_COOL_1 = 1 * 20;
	private static final int VICIOUS_COMBOS_COOL_2 = 2 * 20;

	private static final int SMOKESCREEN_RANGE = 7;
	private static final int SMOKESCREEN_WEAKNESS_EFFECT_LEVEL_1 = 0;
	private static final int SMOKESCREEN_WEAKNESS_EFFECT_LEVEL_2 = 1;
	private static final int SMOKESCREEN_DURATION = 8 * 20;
	private static final int SMOKESCREEN_SLOWNESS_EFFECT_LEVEL_1 = 1;
	private static final int SMOKESCREEN_SLOWNESS_EFFECT_LEVEL_2 = 2;
	private static final int SMOKESCREEN_COOLDOWN = 20 * 20;

	private static final int DAGGER_THROW_COOLDOWN = 12 * 20;
	private static final int DAGGER_THROW_RANGE = 8;
	private static final int DAGGER_THROW_1_DAMAGE = 6;
	private static final int DAGGER_THROW_2_DAMAGE = 12;
	private static final int DAGGER_THROW_DURATION = 10 * 20;
	private static final int DAGGER_THROW_1_VULN = 3;
	private static final int DAGGER_THROW_2_VULN = 7;

	private static final String ROGUE_DODGING_NONCE_METAKEY = "MonumentaRogueDodgingNonce";

	public RogueClass(Plugin plugin, Random random) {
		super(plugin, random);
	}

	@Override
	public void PlayerInteractEvent(Player player, Action action, ItemStack itemInHand,
	                                Material blockClicked) {
		if (action.equals(Action.RIGHT_CLICK_AIR) || (action.equals(Action.RIGHT_CLICK_BLOCK))) {
			ItemStack mainHand = player.getInventory().getItemInMainHand();
			ItemStack offHand = player.getInventory().getItemInOffHand();

			if (InventoryUtils.isSwordItem(mainHand) && InventoryUtils.isSwordItem(offHand)) {
				int daggerThrow = ScoreboardUtils.getScoreboardValue(player, "DaggerThrow");
				int advancingShadows = ScoreboardUtils.getScoreboardValue(player, "AdvancingShadows");
				boolean dagger = false;

				if (daggerThrow > 0) {
					if (player.isSneaking()) {
						if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), Spells.DAGGER_THROW)) {

							World world = player.getWorld();

							dagger = true;
							Location loc = player.getEyeLocation();
							Vector dir = loc.getDirection();

							double damage = (daggerThrow == 1) ? DAGGER_THROW_1_DAMAGE : DAGGER_THROW_2_DAMAGE;
							int vulnLevel = (daggerThrow == 1) ? DAGGER_THROW_1_VULN : DAGGER_THROW_2_VULN;

							for (int a = -1; a < 2; a++) {
								double angle = a * 0.463; //25o. Set to 0.524 for 30o or 0.349 for 20o
								Vector newDir = new Vector(Math.cos(angle) * dir.getX() + Math.sin(angle) * dir.getZ(), dir.getY(), Math.cos(angle) * dir.getZ() - Math.sin(angle) * dir.getX());
								newDir.normalize();

								boolean hit = false;

								for (int i = 1; i <= DAGGER_THROW_RANGE; i++) {
									Location mLoc = (loc.clone()).add((newDir.clone()).multiply(i));
									Location pLoc = mLoc.clone();

									for (int t = 0; t < 10; t++) {
										pLoc.add((newDir.clone()).multiply(0.1));
										ParticleEffect.REDSTONE.display(new OrdinaryColor(64, 64, 64), pLoc, 10);
									}

									for (Entity e : mLoc.getWorld().getNearbyEntities(mLoc, 1, 1, 1)) {
										if (EntityUtils.isHostileMob(e) && !hit) {
											LivingEntity le = (LivingEntity) e;

											EntityUtils.damageEntity(mPlugin, le, damage, player);
											le.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, DAGGER_THROW_DURATION, vulnLevel, true, false));

											hit = true;
										}
									}

									if (mLoc.getBlock().getType().isSolid() || hit) {
										mLoc.subtract((newDir.clone()).multiply(0.5));
										ParticleUtils.playParticlesInWorld(world, Particle.SWEEP_ATTACK, mLoc, 3, 0.3, 0.3, 0.3, 0.1);

										if (hit) {
											world.playSound(loc, "block.anvil.place", 0.4f, 2.5f);
										}

										break;
									}
								}
							}

							mPlugin.mTimers.AddCooldown(player.getUniqueId(), Spells.DAGGER_THROW, DAGGER_THROW_COOLDOWN);
							world.playSound(loc, "entity.player.attack.sweep", 0.9f, 1.5f);
							world.playSound(loc, "entity.player.attack.sweep", 0.9f, 1.25f);
							world.playSound(loc, "entity.player.attack.sweep", 0.9f, 1.0f);
						}
					}
				}

				if (advancingShadows > 0 && !dagger && !player.isSneaking()) {
					if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), Spells.ADVANCING_SHADOWS)) {
						int range  = (advancingShadows == 1) ? ADVANCING_SHADOWS_RANGE_1 : ADVANCING_SHADOWS_RANGE_2;

						//Basically makes sure if the target is in LoS and if there is a path.
						Location eyeLoc = player.getEyeLocation();
						Raycast ray = new Raycast(eyeLoc, eyeLoc.getDirection(), range);
						ray.throughBlocks = false;
						ray.throughNonOccluding = false;
						RaycastData data = ray.shootRaycast();

						List<LivingEntity> rayEntities = data.getEntities();
						if (rayEntities != null && !rayEntities.isEmpty()) {
							LivingEntity entity = rayEntities.get(0);

							if (entity != null && !entity.isDead() && EntityUtils.isHostileMob(entity)) {
								Vector dir = LocationUtils.getDirectionTo(entity.getLocation(), player.getLocation());
								Location loc = player.getLocation();
								while (loc.distance(entity.getLocation()) > ADVANCING_SHADOWS_OFFSET) {
									loc.add(dir);
									if (loc.distance(entity.getLocation()) < ADVANCING_SHADOWS_OFFSET) {
										double multiplier = ADVANCING_SHADOWS_OFFSET - loc.distance(entity.getLocation());
										loc.subtract(dir.clone().multiply(multiplier));
										break;
									}
								}
								loc.add(0, 1, 0);

								//Just in case the player's teleportation loc is in a block.
								while (loc.getBlock().getType().isSolid()) {
									loc.subtract(dir.clone().multiply(1.15));
								}
								ParticleEffect.SPELL_WITCH.display(0, 0.5f, 0, 1, 50, player.getLocation().add(0, 1.1, 0), 40);
								ParticleEffect.SMOKE_LARGE.display(0, 0.5f, 0, 0.05f, 12, player.getLocation().add(0, 1.1, 0), 40)
								;
								player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1.0f, 1.5f);

								player.teleport(loc);

								mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF, new PotionEffect(PotionEffectType.INCREASE_DAMAGE, ADVANCING_SHADOWS_STRENGTH_DURATION, ADVANCING_SHADOWS_STRENGTH_EFFECT_LEVEL, true, false));

								if (advancingShadows > 1) {
									List<Entity> entities = entity.getNearbyEntities(ADVANCING_SHADOWS_AOE_KNOCKBACKS_RANGE, ADVANCING_SHADOWS_AOE_KNOCKBACKS_RANGE, ADVANCING_SHADOWS_AOE_KNOCKBACKS_RANGE);
									for (Entity mob : entities) {
										if (mob != player && mob != entity && EntityUtils.isHostileMob(mob)) {
											MovementUtils.KnockAway(entity, (LivingEntity)mob, ADVANCING_SHADOWS_AOE_KNOCKBACKS_SPEED);
										}
									}
								}

								ParticleEffect.SPELL_WITCH.display(0, 0.5f, 0, 1, 50, player.getLocation().add(0, 1.1, 0), 40);
								ParticleEffect.SMOKE_LARGE.display(0, 0.5f, 0, 0.05f, 12, player.getLocation().add(0, 1.1, 0), 40);
								player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1.0f, 1.5f);
								mPlugin.mTimers.AddCooldown(player.getUniqueId(), Spells.ADVANCING_SHADOWS, ADVANCING_SHADOWS_COOLDOWN);
							}
						}
					}
				}
			}
		} else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			if (player.isSneaking()) {
				int smokeScreen = ScoreboardUtils.getScoreboardValue(player, "SmokeScreen");
				if (smokeScreen > 0) {
					if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), Spells.SMOKESCREEN)) {
						ItemStack mainHand = player.getInventory().getItemInMainHand();
						if (mainHand != null && mainHand.getType() != Material.BOW && InventoryUtils.isSwordItem(mainHand)) {
							List<Entity> entities = player.getNearbyEntities(SMOKESCREEN_RANGE, SMOKESCREEN_RANGE,
							                                                 SMOKESCREEN_RANGE);
							for (Entity entity : entities) {
								if (EntityUtils.isHostileMob(entity)) {
									LivingEntity mob = (LivingEntity)entity;

									int weaknessLevel = smokeScreen == 1 ? SMOKESCREEN_WEAKNESS_EFFECT_LEVEL_1 :
									                    SMOKESCREEN_WEAKNESS_EFFECT_LEVEL_2;
									int slownessLevel = smokeScreen == 1 ? SMOKESCREEN_SLOWNESS_EFFECT_LEVEL_1 :
					                    SMOKESCREEN_SLOWNESS_EFFECT_LEVEL_2;

									mob.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, SMOKESCREEN_DURATION, weaknessLevel, false, true));
									mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, SMOKESCREEN_DURATION, slownessLevel, false, true));
								}
							}
						}

						Location loc = player.getLocation();
						World world = player.getWorld();
						ParticleEffect.SMOKE_LARGE.display(2.5f, 0.8f, 2.5f, 0.05f, 300, loc.clone().add(0,1,0), 40);
						ParticleEffect.SMOKE_NORMAL.display(2.5f, 0.2f, 2.5f, 0.1f, 600, loc, 40);
						world.playSound(loc, "entity.blaze.shoot", 1.0f, 0.35f);

						mPlugin.mTimers.AddCooldown(player.getUniqueId(), Spells.SMOKESCREEN, SMOKESCREEN_COOLDOWN);
					}
				}
			}
		}
	}

	@Override
	public boolean LivingEntityDamagedByPlayerEvent(Player player, LivingEntity damagee, double damage,
	                                                DamageCause cause) {
		if (PlayerUtils.isCritical(player) && cause != DamageCause.PROJECTILE) {
			ItemStack mainHand = player.getInventory().getItemInMainHand();
			ItemStack offHand = player.getInventory().getItemInOffHand();
			if (InventoryUtils.isSwordItem(mainHand) && InventoryUtils.isSwordItem(offHand)) {
				int byMyBlade = ScoreboardUtils.getScoreboardValue(player, "ByMyBlade");
				if (byMyBlade > 0) {
					if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), Spells.BY_MY_BLADE)) {
						int effectLevel = (byMyBlade == 1) ? BY_MY_BLADE_HASTE_1_LVL : BY_MY_BLADE_HASTE_2_LVL;
						mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF,
						                                 new PotionEffect(PotionEffectType.FAST_DIGGING,
						                                                  BY_MY_BLADE_HASTE_DURATION,
						                                                  effectLevel, false, true));

						mPlugin.mTimers.AddCooldown(player.getUniqueId(), Spells.BY_MY_BLADE, BY_MY_BLADE_COOLDOWN);

						double extraDamage = (byMyBlade == 1) ? BY_MY_BLADE_DAMAGE_1 : BY_MY_BLADE_DAMAGE_2;
						_damageMob(player, damagee, extraDamage);

						World world = player.getWorld();
						Location loc = damagee.getLocation();
						loc.add(0, 1, 0);
						int count = 15;
						world.playSound(loc, Sound.ITEM_SHIELD_BREAK, 2.0f, 0.5f);
						if (byMyBlade > 1) {
							ParticleEffect.SPELL_WITCH.display(0.2f, 0.65f, 0.2f, 1, 45, loc, 40);
							count = 30;
						}
						ParticleUtils.playParticlesInWorld(world, Particle.SPELL_MOB, loc, count, 0.25, 0.5, 0.5, 0.001);
						ParticleUtils.playParticlesInWorld(world, Particle.CRIT, loc, 30, 0.25, 0.5, 0.5, 0.001);
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean PlayerDamagedByProjectileEvent(Player player, Projectile damager) {
		EntityType type = damager.getType();

		// If this projectile was dodged in the ProjectileHitPlayerEvent, cancel it's damage
		if ((type == EntityType.ARROW || type == EntityType.TIPPED_ARROW
		     || type == EntityType.SPECTRAL_ARROW || type == EntityType.SMALL_FIREBALL)
		    && !MetadataUtils.checkOnceThisTick(mPlugin, player, ROGUE_DODGING_NONCE_METAKEY)) {
			return false;
		}

		return true;
	}

	@Override
	public void ProjectileHitPlayerEvent(Player player, Projectile damager) {
		EntityType type = damager.getType();

		//  Dodging
		if (type == EntityType.ARROW || type == EntityType.TIPPED_ARROW
		    || type == EntityType.SPECTRAL_ARROW || type == EntityType.SMALL_FIREBALL) {

			int dodging = ScoreboardUtils.getScoreboardValue(player, "Dodging");
			if (dodging > 0) {
				if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), Spells.DODGING)) {
					World world = player.getWorld();
					if (dodging > 1) {
						mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF,
						                                 new PotionEffect(PotionEffectType.SPEED,
						                                                  DODGING_SPEED_EFFECT_DURATION,
						                                                  DODGING_SPEED_EFFECT_LEVEL,
						                                                  true, false));
						world.playSound(player.getLocation(), "entity.firework.launch", 2.0f, 0.5f);
					}

					world.playSound(player.getLocation(), "block.anvil.land", 0.5f, 1.5f);

					int cooldown = dodging == 1 ? DODGING_COOLDOWN_1 : DODGING_COOLDOWN_2;
					mPlugin.mTimers.AddCooldown(player.getUniqueId(), Spells.DODGING, cooldown);

					// Remove effects from tipped arrows
					// TODO: This is the same code as for removing from shields, should probably be
					// a utility function
					if (type == EntityType.TIPPED_ARROW) {
						TippedArrow arrow = (TippedArrow)damager;
						PotionData data = new PotionData(PotionType.AWKWARD);
						arrow.setBasePotionData(data);

						if (arrow.hasCustomEffects()) {
							Iterator<PotionEffect> effectIter = arrow.getCustomEffects().iterator();
							while (effectIter.hasNext()) {
								PotionEffect effect = effectIter.next();
								arrow.removeCustomEffect(effect.getType());
							}
						}
					}

					// Set metadata indicating this event happened this tick
					MetadataUtils.checkOnceThisTick(mPlugin, player, ROGUE_DODGING_NONCE_METAKEY);
				}
			}
		}
	}

	@Override
	public boolean PlayerDamagedByLivingEntityEvent(Player player, LivingEntity damager,
	                                                double damage) {
		double correctHealth = player.getHealth() - damage;
		if (correctHealth > 0 && correctHealth < ESCAPE_DEATH_HEALTH_TRIGGER) {
			int escapeDeath = ScoreboardUtils.getScoreboardValue(player, "EscapeDeath");
			if (escapeDeath > 0) {
				if (!mPlugin.mTimers.isAbilityOnCooldown(player.getUniqueId(), Spells.ESCAPE_DEATH)) {
					List<Entity> entities = player.getNearbyEntities(ESCAPE_DEATH_RANGE, ESCAPE_DEATH_RANGE,
					                                                 ESCAPE_DEATH_RANGE);
					for (Entity entity : entities) {
						if (EntityUtils.isHostileMob(entity)) {
							LivingEntity mob = (LivingEntity)entity;
							mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ESCAPE_DEATH_DURATION_SLOWNESS,
							                                     ESCAPE_DEATH_SLOWNESS_EFFECT_LVL, true, false));
							mob.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ESCAPE_DEATH_DURATION_SLOWNESS,
							                                     ESCAPE_DEATH_WEAKNES_EFFECT_LEVEL, true, false));
						}
					}

					if (escapeDeath > 1) {
						mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF,
						                                 new PotionEffect(PotionEffectType.ABSORPTION, ESCAPE_DEATH_DURATION,
						                                                  ESCAPE_DEATH_ABSORBTION_EFFECT_LVL, true, false));
						mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF,
						                                 new PotionEffect(PotionEffectType.SPEED, ESCAPE_DEATH_DURATION_OTHER,
						                                                  ESCAPE_DEATH_SPEED_EFFECT_LVL, true, false));
						mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF,
						                                 new PotionEffect(PotionEffectType.JUMP, ESCAPE_DEATH_DURATION_OTHER,
						                                                  ESCAPE_DEATH_JUMP_EFFECT_LVL, true, false));
					}

					World world = player.getWorld();
					Location loc = player.getLocation();
					loc.add(0, 1, 0);

					int val = escapeDeath == 1 ? 1 : ESCAPE_DEATH_RANGE;
					Vector offset = new Vector(val, val, val);
					int particles = escapeDeath == 1 ? 30 : 500;

					ParticleUtils.playParticlesInWorld(world, Particle.SPELL_INSTANT, loc, particles, offset.getX(),
					                                   offset.getY(), offset.getZ(), 0.001);

					if (escapeDeath > 1) {
						ParticleUtils.playParticlesInWorld(world, Particle.CLOUD, loc, particles, offset.getX(),
						                                   offset.getY(), offset.getZ(), 0.001);
					}

					world.playSound(loc, "item.totem.use", 0.5f, 0.5f);

					MessagingUtils.sendActionBarMessage(mPlugin, player, "Escape Death has been activated");

					mPlugin.mTimers.AddCooldown(player.getUniqueId(), Spells.ESCAPE_DEATH, ESCAPE_DEATH_COOLDOWN);
				}
			}
		}
		return true;
	}

	@Override
	public boolean PlayerCombustByEntityEvent(Player player, Entity combuster) {
		// If the player just dodged a projectile this tick, cancel the combust event
		return MetadataUtils.checkOnceThisTick(mPlugin, player, ROGUE_DODGING_NONCE_METAKEY);
	}

	@Override
	public void EntityDeathEvent(Player player, LivingEntity killedEntity, DamageCause cause, boolean shouldGenDrops) {
		int viciousCombos = ScoreboardUtils.getScoreboardValue(player, "ViciousCombos");
		if (viciousCombos > 0) {
			World world = player.getWorld();
			Location loc = killedEntity.getLocation();
			loc = loc.add(0, 0.5, 0);

			if (EntityUtils.isElite(killedEntity)) {
				mPlugin.mTimers.removeAllCooldowns(player.getUniqueId());
				MessagingUtils.sendActionBarMessage(mPlugin, player, "All your cooldowns have been reset");
				mPlugin.mPotionManager.addPotion(player, PotionID.ABILITY_SELF,	new PotionEffect(PotionEffectType.SPEED, VICIOUS_COMBOS_EFFECT_DURATION, VICIOUS_COMBOS_EFFECT_LEVEL, true, false));

				if (viciousCombos > 1) {
					List<Entity> entities = player.getNearbyEntities(VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE);
					for (Entity entity : entities) {
						if (EntityUtils.isHostileMob(entity)) {
							LivingEntity mob = (LivingEntity)entity;
							_damageMob(player, mob, VICIOUS_COMBOS_DAMAGE);
						}
					}
				}

				world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 2, 0.5f);
				ParticleEffect.CRIT.display(VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, 0.25f, 500, loc, 40);
				ParticleEffect.CRIT_MAGIC.display(VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, 0.25f, 500, loc, 40);
				ParticleUtils.playParticlesInWorld(world, Particle.SWEEP_ATTACK, loc, 350, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, 0.001);
				ParticleUtils.playParticlesInWorld(world, Particle.SPELL_MOB, loc, 350, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, 0.001);
			}
			else if (EntityUtils.isHostileMob(killedEntity)) {
				int timeReduction = (viciousCombos == 1) ? VICIOUS_COMBOS_COOL_1 : VICIOUS_COMBOS_COOL_2;
				mPlugin.mTimers.UpdateCooldowns(timeReduction);

				world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 0.5f);
				ParticleEffect.CRIT.display(VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, 0.25f, 50, loc, 40);
				ParticleEffect.CRIT_MAGIC.display(VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, 0.25f, 50, loc, 40);
				ParticleUtils.playParticlesInWorld(world, Particle.SWEEP_ATTACK, loc, 30, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, 0.001);
				ParticleUtils.playParticlesInWorld(world, Particle.SPELL_MOB, loc, 30, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, VICIOUS_COMBOS_RANGE, 0.001);
			}


		}
	}

	@Override
	public void ModifyDamage(Player player, BaseClass owner, EntityDamageByEntityEvent event) {
		//  Make sure only players trigger this.
		if (event.getDamager() instanceof Player) {
			Entity damagee = event.getEntity();

			ItemStack mainHand = player.getInventory().getItemInMainHand();
			ItemStack offHand = player.getInventory().getItemInOffHand();
			if (InventoryUtils.isSwordItem(mainHand) && InventoryUtils.isSwordItem(offHand)) {
			//  This test if the damagee is an instance of a Elite.
				if (damagee instanceof LivingEntity && EntityUtils.isElite(event.getEntity())) {
					event.setDamage(event.getDamage() * PASSIVE_DAMAGE_ELITE_MODIFIER);
				} else if (damagee instanceof LivingEntity && EntityUtils.isBoss(event.getEntity())) {
					event.setDamage(event.getDamage() * PASSIVE_DAMAGE_BOSS_MODIFIER);
				}
			}
		}
	}

	private void _damageMob(Player player, LivingEntity damagee, double damage) {
		double correctDamage = damage;
		if (EntityUtils.isElite(damagee)) {
			correctDamage = damage * PASSIVE_DAMAGE_ELITE_MODIFIER;
		} else if (EntityUtils.isBoss(damagee)) {
			correctDamage = damage * PASSIVE_DAMAGE_BOSS_MODIFIER;
		}
		EntityUtils.damageEntity(mPlugin, damagee, correctDamage, player);
	}
}