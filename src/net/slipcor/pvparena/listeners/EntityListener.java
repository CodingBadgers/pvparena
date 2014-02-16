package net.slipcor.pvparena.listeners;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaPlayer.Status;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PABlockLocation;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.loadables.ArenaModuleManager;
import net.slipcor.pvparena.loadables.ArenaRegion;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionFlag;
import net.slipcor.pvparena.loadables.ArenaRegion.RegionProtection;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.SpawnManager;
import net.slipcor.pvparena.managers.StatisticsManager;
import net.slipcor.pvparena.runnables.DamageResetRunnable;

import org.bukkit.Bukkit;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * <pre>
 * Entity Listener class
 * </pre>
 * 
 * @author slipcor
 * 
 * @version v0.10.2
 */

public class EntityListener implements Listener {
	private final static Debug DEBUG = new Debug(21);
	private final static Map<PotionEffectType, Boolean> TEAMEFFECT = new HashMap<PotionEffectType, Boolean>();

	static {
		TEAMEFFECT.put(PotionEffectType.BLINDNESS, false);
		TEAMEFFECT.put(PotionEffectType.CONFUSION, false);
		TEAMEFFECT.put(PotionEffectType.DAMAGE_RESISTANCE, true);
		TEAMEFFECT.put(PotionEffectType.FAST_DIGGING, true);
		TEAMEFFECT.put(PotionEffectType.FIRE_RESISTANCE, true);
		TEAMEFFECT.put(PotionEffectType.HARM, false);
		TEAMEFFECT.put(PotionEffectType.HEAL, true);
		TEAMEFFECT.put(PotionEffectType.HUNGER, false);
		TEAMEFFECT.put(PotionEffectType.INCREASE_DAMAGE, true);
		TEAMEFFECT.put(PotionEffectType.JUMP, true);
		TEAMEFFECT.put(PotionEffectType.POISON, false);
		TEAMEFFECT.put(PotionEffectType.REGENERATION, true);
		TEAMEFFECT.put(PotionEffectType.SLOW, false);
		TEAMEFFECT.put(PotionEffectType.SLOW_DIGGING, false);
		TEAMEFFECT.put(PotionEffectType.SPEED, true);
		TEAMEFFECT.put(PotionEffectType.WATER_BREATHING, true);
		TEAMEFFECT.put(PotionEffectType.WEAKNESS, false);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCreatureSpawn(final CreatureSpawnEvent event) {
		DEBUG.i("onCreatureSpawn: " + event.getSpawnReason().name());
		final Set<SpawnReason> naturals = new HashSet<SpawnReason>();
		naturals.add(SpawnReason.CHUNK_GEN);
		naturals.add(SpawnReason.DEFAULT);
		naturals.add(SpawnReason.NATURAL);
		naturals.add(SpawnReason.SLIME_SPLIT);
		naturals.add(SpawnReason.VILLAGE_INVASION);

		if (!naturals.contains(event.getSpawnReason())) {
			// custom generation, this is not our business!
			DEBUG.i(">natural");
			return;
		}

		final Arena arena = ArenaManager
				.getArenaByProtectedRegionLocation(
						new PABlockLocation(event.getLocation()),
						RegionProtection.MOBS);
		if (arena == null) {
			DEBUG.i("null arena");
			return; // no arena => out
		}
		arena.getDebugger().i("cancel CreatureSpawnEvent!");
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(final EntityExplodeEvent event) {
		DEBUG.i("explosion");

		Arena arena = ArenaManager.getArenaByProtectedRegionLocation(
				new PABlockLocation(event.getLocation()), RegionProtection.TNT);
		if (arena == null) {

			arena = ArenaManager.getArenaByProtectedRegionLocation(
					new PABlockLocation(event.getLocation()), RegionProtection.TNTBREAK);
			if (arena == null) {
				
				arena = ArenaManager.getArenaByRegionLocation(new PABlockLocation(event.getLocation()));
				if (arena != null) {
					ArenaModuleManager.onEntityExplode(arena, event);
				}
				
				return; // no arena => out
			}
		}
		arena.getDebugger().i("explosion inside an arena");
		if (!(arena.getArenaConfig().getBoolean(CFG.PROTECT_ENABLED))
				|| (!(event.getEntity() instanceof TNTPrimed) 
						&& !(event.getEntity() instanceof Creeper))) {
			ArenaModuleManager.onEntityExplode(arena, event);
			return;
		}
		
		if (BlockListener.isProtected(event.getLocation(), event,
				RegionProtection.TNTBREAK)) {
			event.blockList().clear();
			return;
		}

		event.setCancelled(true); // ELSE => cancel event
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
		final Entity entity = event.getEntity();

		if ((entity == null) || (!(entity instanceof Player))) {
			return; // no player
		}
		final Arena arena = ArenaPlayer.parsePlayer(((Player) entity).getName())
				.getArena();
		if (arena == null) {
			return;
		}
		final Player player = (Player) entity;
		arena.getDebugger().i("onEntityRegainHealth => fighing player", player);
		if (!arena.isFightInProgress()) {
			return;
		}

		final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());
		final ArenaTeam team = aPlayer.getArenaTeam();

		if (team == null) {
			return;
		}

		ArenaModuleManager.onEntityRegainHealth(arena, event);

	}

	/**
	 * parsing of damage: Entity vs Entity
	 * 
	 * @param event
	 *            the triggering event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
		Entity eDamager = event.getDamager();
		final Entity eDamagee = event.getEntity();

		DEBUG.i("onEntityDamageByEntity: cause: " + event.getCause().name()
				+ " : " + event.getDamager().toString() + " => "
				+ event.getEntity().toString());

		if (eDamager instanceof Projectile) {
			DEBUG.i("parsing projectile");
			eDamager = (LivingEntity)((Projectile) eDamager).getShooter();
			DEBUG.i("=> " + eDamager);
		}

		if(eDamager instanceof Player && ArenaPlayer.parsePlayer(((Player) eDamager).getName()).getStatus() == Status.LOST) {
			event.setCancelled(true);
			return;
		}
		
		if (event.getEntity() instanceof Wolf) {
			final Wolf wolf = (Wolf) event.getEntity();
			if (wolf.getOwner() != null) {
				try {
					eDamager = (Entity) wolf.getOwner();
				} catch (Exception e) {
					// wolf belongs to dead player or whatnot
				}
			}
		}

		if (eDamager instanceof Player && eDamagee instanceof Player
				&& PVPArena.instance.getConfig().getBoolean("onlyPVPinArena")) {
				event.setCancelled(true);
				// cancel events for regular no PVP servers
		}

		if (!(eDamagee instanceof Player)) {
			return;
		}

		final Arena arena = ArenaPlayer.parsePlayer(((Player) eDamagee).getName())
				.getArena();
		if (arena == null) {
			// defender no arena player => out
			return;
		}

		arena.getDebugger().i("onEntityDamageByEntity: fighting player");

		if ((eDamager == null) || (!(eDamager instanceof Player))) {
			// attacker no player => out!
			return;
		}

		arena.getDebugger().i("both entities are players",(Player) eDamager);
		final Player attacker = (Player) eDamager;
		final Player defender = (Player) eDamagee;

		if (attacker.equals(defender)) {
			// player attacking himself. ignore!
			return;
		}

		boolean defTeam = false;
		boolean attTeam = false;
		final ArenaPlayer apDefender = ArenaPlayer.parsePlayer(defender.getName());
		final ArenaPlayer apAttacker = ArenaPlayer.parsePlayer(attacker.getName());

		for (ArenaTeam team : arena.getTeams()) {
			defTeam = defTeam ? true : team.getTeamMembers().contains(
					apDefender);
			attTeam = attTeam ? true : team.getTeamMembers().contains(
					apAttacker);
		}

		if (!defTeam || !attTeam || arena.realEndRunner != null) {
			event.setCancelled(true);
			return;
		}

		arena.getDebugger().i("both players part of the arena",attacker);
		arena.getDebugger().i("both players part of the arena",defender);

		if (PVPArena.instance.getConfig().getBoolean("onlyPVPinArena")) {
			event.setCancelled(false); // uncancel events for regular no PVP
			// servers
		}

		if ((!arena.getArenaConfig().getBoolean(CFG.PERMS_TEAMKILL))
				&& (apAttacker.getArenaTeam())
						.equals(apDefender.getArenaTeam())) {
			// no team fights!
			arena.getDebugger().i("team hit, cancel!",attacker);
			arena.getDebugger().i("team hit, cancel!",defender);
			if (!(event.getDamager() instanceof Snowball)) {
				event.setCancelled(true);
			}
			return;
		}
		
		if (!arena.isFightInProgress() || (arena.pvpRunner != null)) {
			arena.getDebugger().i("fight not started, cancel!",attacker);
			arena.getDebugger().i("fight not started, cancel!",defender);
			event.setCancelled(true);
			return;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(PVPArena.instance,
				new DamageResetRunnable(arena, attacker, defender), 1L);

		if (arena.getArenaConfig().getInt(CFG.PROTECT_SPAWN) > 0
				&& SpawnManager.isNearSpawn(arena, defender, arena
					.getArenaConfig().getInt(CFG.PROTECT_SPAWN))) {
			// spawn protection!
			arena.getDebugger().i("spawn protection! damage cancelled!",attacker);
			arena.getDebugger().i("spawn protection! damage cancelled!",defender);
			event.setCancelled(true);
			return;
		}

		// here it comes, process the damage!

		arena.getDebugger().i("processing damage!",attacker);
		arena.getDebugger().i("processing damage!",defender);

		ArenaModuleManager.onEntityDamageByEntity(arena, attacker, defender,
				event);

		StatisticsManager.damage(arena, attacker, defender, event.getDamage());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamage(final EntityDamageEvent event) {
		final Entity entity = event.getEntity();

		DEBUG.i("onEntityDamage: cause: " + event.getCause().name() + " : "
				+ event.getEntity().toString());

		if (!(entity instanceof Player)) {
			return;
		}
		
		if(ArenaPlayer.parsePlayer(((Player) entity).getName()).getStatus() == Status.LOST) {
			event.setCancelled(true);
			return;
		}

		final Arena arena = ArenaPlayer.parsePlayer(((Player) entity).getName())
				.getArena();
		if (arena == null) {
			// defender no arena player => out
			return;
		}

		final Player defender = (Player) entity;

		final ArenaPlayer apDefender = ArenaPlayer.parsePlayer(defender.getName());

		if (arena.realEndRunner != null
				|| (!apDefender.getStatus().equals(Status.NULL) && !apDefender
						.getStatus().equals(Status.FIGHT))) {
			event.setCancelled(true);
			return;
		}

		for (ArenaRegion ars : arena.getRegions()) {
			if (ars.getFlags().contains(RegionFlag.NODAMAGE)
					&& ars.getShape().contains(new PABlockLocation(defender.getLocation()))) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionSplash(final PotionSplashEvent event) {

		DEBUG.i("onPotionSplash");
		boolean affectTeam = true;

		final Collection<PotionEffect> pot = event.getPotion().getEffects();
		for (PotionEffect eff : pot) {
			DEBUG.i(">" + eff.getType().getName());
			if (TEAMEFFECT.containsKey(eff.getType())) {
				affectTeam = TEAMEFFECT.get(eff.getType());
				DEBUG.i(">" + affectTeam);
				break;
			}
		}

		ArenaPlayer shooter = null;

		try {
			shooter = ArenaPlayer.parsePlayer(((Player) event.getEntity()
					.getShooter()).getName());
		} catch (Exception e) {
			return;
		}

		DEBUG.i("legit player: " + shooter, shooter.getName());

		if (shooter == null || shooter.getArena() == null
				|| !shooter.getStatus().equals(Status.FIGHT)) {
			DEBUG.i("something is null!", shooter.getName());
			return;
		}
		
		if (shooter.getArena().getArenaConfig().getBoolean(CFG.PERMS_TEAMKILL)) {
			return; // if teamkill allowed, don't check, just ignore
		}

		final Collection<LivingEntity> entities = event.getAffectedEntities();
		for (LivingEntity e : entities) {
			if (!(e instanceof Player)) {
				continue;
			}
			final ArenaPlayer damagee = ArenaPlayer.parsePlayer(((Player) e).getName());
			
			if (damagee.getArena() == null || shooter.getArena() == null ||
					(damagee.getArena() != shooter.getArena()) ||
					damagee.getArenaTeam() == null || shooter.getArenaTeam() == null) {
				/**
				 * some people obviously allow non arena players to mess with potions around arena players
				 * 
				 * this check should cover any of the entities not being in the same arena, or not arena at all
				 */
				continue;
			}
			
			final boolean sameTeam = damagee.getArenaTeam().equals(shooter.getArenaTeam());

			
			
			if (sameTeam != affectTeam) {
				// different team and only team should be affected
				// same team and the other team should be affected
				// ==> cancel!
				event.setIntensity(e, 0);
				break;
			}
		}
	}
}