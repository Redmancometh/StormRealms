package org.bukkit.craftbukkit.entity;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.RPGEntity;
import org.spigotmc.event.entity.Goal;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.EntityCustomMonster;

public class CraftRPGEntity extends CraftMonster implements RPGEntity {

	@Getter
	@Setter
	private List<Goal> goals = new ArrayList();

	public CraftRPGEntity(CraftServer server, EntityCustomMonster entity) {
		super(server, entity);
	}

	@Override
	public EntityCustomMonster getHandle() {
		return (EntityCustomMonster) entity;
	}

	@Override
	public String toString() {
		return "CraftRPGEntity";
	}

	@Override
	public int getEntityId() {
		return getHandle().getId();
	}

	@Override
	public EntityType getType() {
		return EntityType.RPG_ENTITY;
	}

	@Override
	public int getLevel() {
		return getHandle().getData().getLevel();
	}

	@Override
	public int getDefense() {
		return getHandle().getData().getDefense();
	}

	@Override
	public void addGoal(Goal goal) {
		this.goals.add(goal);
	}

	@Override
	public void removeGoal(Goal goal) {
		this.goals.add(goal);
	}
	
	@Override
	public void tickSecond() {
		System.out.println("TICK SECOND");
	}
	
	@Override
	public void tick() {
		RPGEntity.super.tick();
	}
	
}
