package org.bukkit.entity;

import java.util.List;

import org.spigotmc.event.entity.Goal;

public interface RPGEntity extends Mob {
	public int getLevel();

	public int getDefense();

	public void addGoal(Goal goal);

	public void removeGoal(Goal goal);

	public void setGoals(List<Goal> goals);

	public List<Goal> getGoals();

	public default void tick() {
		getGoals().forEach((goal) -> goal.tick(this));
	}

	public void tickSecond();

}
