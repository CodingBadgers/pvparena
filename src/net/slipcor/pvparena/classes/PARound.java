package net.slipcor.pvparena.classes;

import java.util.Set;

import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.loadables.ArenaGoal;

/**
 * <pre>PVP Arena Round class</pre>
 * 
 * A class to organize multiple ways of playing for an arena
 * 
 * @author slipcor
 * 
 * @version v0.9.1
 */

public class PARound {
	private final Set<ArenaGoal> goals;
	
	public PARound(final Set<ArenaGoal> result) {
		goals = result;
	}
	
	public Set<ArenaGoal> getGoals() {
		return goals;
	}

	public boolean toggle(final Arena arena, final ArenaGoal goal) {
		final ArenaGoal nugoal = (ArenaGoal) goal.clone();
		nugoal.setArena(arena);
		
		boolean contains = false;
		ArenaGoal removeGoal = nugoal;
		
		for (ArenaGoal g : goals) {
			if (g.getName().equals(goal.getName())) {
				contains = true;
				removeGoal = g;
				break;
			}
		}
		
		if (contains) {
			goals.remove(removeGoal);
			arena.updateRounds();
			return false;
		} else {
			goals.add(nugoal);
			arena.updateRounds();
			return true;
		}
	}
}
