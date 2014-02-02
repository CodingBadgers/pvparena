package net.slipcor.pvparena.loadables;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.arena.ArenaTeam;
import net.slipcor.pvparena.classes.PACheck;
import net.slipcor.pvparena.core.Config.CFG;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.ncloader.NCBLoadable;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * <pre>
 * Arena Goal class
 * </pre>
 * 
 * The framework for adding goals to an arena
 * 
 * @author slipcor
 */

public class ArenaGoal extends NCBLoadable {
	protected Debug debug = new Debug(30);
	protected Arena arena;
	protected Map<String, Integer> lifeMap = null;

	/**
	 * create an arena type instance
	 * 
	 * @param sName
	 *            the arena type name
	 */
	public ArenaGoal(final String sName) {
		super(sName);
	}

	/**
	 * does the arena type allow joining in battle?
	 */
	public boolean allowsJoinInBattle() {
		return false;
	}

	/**
	 * check if the goal should commit a command
	 * 
	 * @param res
	 *            the PACheck instance
	 * @param string
	 *            the command argument
	 * @return the PACheck instance
	 */
	public PACheck checkCommand(final PACheck res, final String string) {
		return res;
	}

	/**
	 * check if the goal should commit the end
	 * 
	 * @param res
	 *            the PACheck instance
	 * @return the PACheck instance
	 */
	public PACheck checkEnd(final PACheck res) {
		return res;
	}

	/**
	 * check if all necessary spawns are set
	 * 
	 * @param list
	 *            the list of all set spawns
	 * @return null if ready, error message otherwise
	 */
	public String checkForMissingSpawns(final Set<String> list) {
		return null;
	}

	/**
	 * check if necessary FFA spawns are set
	 * 
	 * @return null if ready, error message otherwise
	 */
	protected String checkForMissingSpawn(final Set<String> list) {
		int count = 0;
		for (String s : list) {
			if (s.startsWith("spawn")) {
				count++;
			}
		}
		return count > 3 ? null : "need more spawns! (" + count + "/4)";
	}

	/**
	 * check if necessary team spawns are set
	 * 
	 * @return null if ready, error message otherwise
	 */
	protected String checkForMissingTeamSpawn(final Set<String> list) {
		for (ArenaTeam team : arena.getTeams()) {
			final String sTeam = team.getName();
			if (!list.contains(team + "spawn")) {
				boolean found = false;
				for (String s : list) {
					if (s.startsWith(sTeam + "spawn")) {
						found = true;
						break;
					}
				}
				if (!found) {
					return team.getName() + "spawn not set";
				}
			}
		}
		return null;
	}

	/**
	 * check if necessary custom team spawns are set
	 * 
	 * @return null if ready, error message otherwise
	 */
	protected String checkForMissingTeamCustom(final Set<String> list,
			String custom) {
		for (ArenaTeam team : arena.getTeams()) {
			final String sTeam = team.getName();
			if (!list.contains(sTeam + custom)) {
				boolean found = false;
				for (String s : list) {
					if (s.startsWith(sTeam + custom)) {
						found = true;
						break;
					}
				}
				if (!found) {
					return sTeam + custom + "not set";
				}
			}
		}
		return null;
	}

	/**
	 * hook into an interacting player
	 * 
	 * @param res
	 *            the PACheck instance
	 * @param player
	 *            the interacting player
	 * @param clickedBlock
	 *            the block being clicked
	 * @return the PACheck instance
	 */
	public PACheck checkInteract(final PACheck res, final Player player,
			final Block clickedBlock) {
		return res;
	}

	/**
	 * check if the goal should commit a player join
	 * 
	 * @param sender
	 *            the joining player
	 * @param res
	 *            the PACheck instance
	 * @param args
	 *            command arguments
	 * @return the PACheck instance
	 */
	public PACheck checkJoin(final CommandSender sender, final PACheck res,
			final String[] args) {
		return res;
	}

	/**
	 * check if the goal should commit a player death
	 * 
	 * @param player
	 *            the dying player
	 * @return the PACheck instance
	 */
	public PACheck checkPlayerDeath(final PACheck res, final Player player) {
		return res;
	}

	/**
	 * check if the goal should set a block
	 * 
	 * @param res
	 *            the PACheck instance
	 * @param player
	 *            the setting player
	 * @param block
	 *            the block being set
	 * @return the PACheck instance
	 */
	public PACheck checkSetBlock(final PACheck res, final Player player,
			final Block block) {
		return res;
	}

	/**
	 * check if the goal should start the game
	 * 
	 * @param res
	 *            the PACheck instance
	 * @return the PACheck instance
	 */
	public PACheck checkStart(final PACheck res) {
		return res;
	}

	/**
	 * commit a command
	 * 
	 * @param sender
	 *            the committing player
	 * @param args
	 *            the command arguments
	 */
	public void commitCommand(final CommandSender sender, final String[] args) {
		throw new IllegalStateException(this.getName());
	}

	/**
	 * commit the arene end
	 * 
	 * @param force
	 *            true, if we need to force
	 */
	public void commitEnd(final boolean force) {
		throw new IllegalStateException(this.getName());
	}

	/**
	 * commit player interaction
	 * 
	 * @param player
	 *            the interacting player
	 * @param clickedBlock
	 *            the block being interacted with
	 */
	public void commitInteract(final Player player, final Block clickedBlock) {
		throw new IllegalStateException(this.getName());
	}

	/**
	 * commit a player death
	 * 
	 * @param player
	 *            the dying player
	 * @param doesRespawn
	 *            true if the player will respawn
	 * @param error
	 *            an optional error string
	 * @param event
	 *            the causing death event
	 */
	public void commitPlayerDeath(final Player player,
			final boolean doesRespawn, final String error,
			final PlayerDeathEvent event) {
		throw new IllegalStateException(this.getName());
	}

	/**
	 * commit setting a flag
	 * 
	 * @param player
	 *            the setting player
	 * @param block
	 *            the flag block
	 * @return true if the interact event should be cancelled
	 */
	public boolean commitSetFlag(final Player player, final Block block) {
		throw new IllegalStateException(this.getName());
	}

	/**
	 * commit an arena start
	 */
	public void commitStart() {
		throw new IllegalStateException(this.getName());
	}

	/**
	 * hook into the config parsing
	 * 
	 * @param config
	 *            the arena config
	 */
	public void configParse(final YamlConfiguration config) {
	}

	/**
	 * hook into disconnecting a player
	 * 
	 * @param player
	 *            the player being disconnected
	 */
	public void disconnect(final ArenaPlayer player) {
	}

	/**
	 * display information about the goal
	 * 
	 * @param sender
	 *            the sender to receive more information
	 */
	public void displayInfo(final CommandSender sender) {
	}

	/**
	 * Getter for the goal life map
	 * 
	 * @return the goal life map
	 */
	protected Map<String, Integer> getLifeMap() {
		if (lifeMap == null) {
			lifeMap = new HashMap<String, Integer>();
		}
		return lifeMap;
	}

	/**
	 * Get a player's remaining lives
	 * 
	 * @param res
	 *            the PACheck instance
	 * @param player
	 *            the player to check
	 * @return the PACheck instance for more information, eventually an ERROR
	 *         containing the lives
	 */
	public PACheck getLives(final PACheck res, final ArenaPlayer player) {
		return res;
	}

	/**
	 * does a goal know this spawn?
	 * 
	 * @param string
	 *            the spawn name to check
	 * @return if the goal knows this spawn
	 */
	public boolean hasSpawn(final String string) {
		return false;
	}

	/**
	 * hook into initializing a player being put directly to the battlefield
	 * (contrary to lounge/spectate)
	 * 
	 * @param player
	 *            the player being put
	 */
	public void initate(final Player player) {
	}

	/**
	 * hook into an arena joining the game after it has begin
	 * 
	 * @param player
	 *            the joining player
	 */
	public void lateJoin(final Player player) {
	}

	/**
	 * hook into the initial goal loading
	 */
	public void onThisLoad() {
	}

	/**
	 * hook into a player leaving the arena
	 * 
	 * @param player
	 *            the leaving player
	 */
	public void parseLeave(final Player player) {
	}

	/**
	 * hook into a player dying
	 * 
	 * @param player
	 *            the dying player
	 * @param lastDamageCause
	 *            the last damage cause
	 */
	public void parsePlayerDeath(final Player player,
			final EntityDamageEvent lastDamageCause) {
	}

	/**
	 * hook into an arena start
	 */
	public void parseStart() {
	}

	/**
	 * check if the arena is ready
	 * 
	 * @return null if ready, error message otherwise
	 */
	public String ready() {
		return null;
	}

	/**
	 * hook into a player being refilled
	 * 
	 * @param player
	 *            the player being refilled
	 */
	public void refillInventory(Player player) {
	}

	/**
	 * hook into an arena reset
	 * 
	 * @param force
	 *            is the resetting forced?
	 */
	public void reset(final boolean force) {
	}

	/**
	 * update the arena instance (should only be used on instanciation)
	 * 
	 * @param arena
	 *            the new instance
	 */
	public void setArena(final Arena arena) {
		this.arena = arena;
	}

	/**
	 * hook into setting config defaults
	 * 
	 * @param config
	 *            the arena config
	 */
	public void setDefaults(final YamlConfiguration config) {
	}

	/**
	 * set all player lives
	 * 
	 * @param value
	 *            the value being set
	 */
	public void setPlayerLives(final int value) {
	}

	/**
	 * set a specific player's lives
	 * 
	 * @param player
	 *            the player to update
	 * @param value
	 *            the value being set
	 */
	public void setPlayerLives(final ArenaPlayer player, final int value) {
	}

	/**
	 * hook into the score calculation
	 * 
	 * @param scores
	 *            the scores so far: team name or player name is key
	 * @return the updated map
	 */
	public Map<String, Double> timedEnd(final Map<String, Double> scores) {
		return scores;
	}

	/**
	 * hook into arena player unloading
	 * 
	 * @param player
	 *            the player to unload
	 */
	public void unload(final Player player) {
	}
	
	protected void updateLives(final ArenaTeam team, final int value) {
		if (arena.getArenaConfig().getBoolean(CFG.GOAL_ADDLIVESPERPLAYER)) {
			getLifeMap().put(team.getName(), team.getTeamMembers().size() * value);
		} else {
			getLifeMap().put(team.getName(), value);
		}
	}
	
	protected void updateLives(final Player player, final int value) {
		if (arena.getArenaConfig().getBoolean(CFG.GOAL_ADDLIVESPERPLAYER)) {
			getLifeMap().put(player.getName(), arena.getFighters().size() * value);
		} else {
			getLifeMap().put(player.getName(), value);
		}
	}

	/**
	 * the goal version (should be overridden!)
	 * 
	 * @return
	 */
	public String version() {
		return "outdated";
	}
    
    /**
     * 
     * @param attacker
     * @param defender
     * @param event 
     */
    public void onEntityDamageByEntity(final Player attacker,
			final Player defender, final EntityDamageByEntityEvent event) {
	}
}
