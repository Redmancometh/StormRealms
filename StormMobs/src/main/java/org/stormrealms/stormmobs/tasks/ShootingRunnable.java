package org.stormrealms.stormmobs.tasks;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.stormrealms.stormmobs.entity.CustomEntity;

public class ShootingRunnable extends BukkitRunnable {
	private int shots;
	private int shotCounter;
	private CustomEntity shooter;
	private Class<? extends Projectile> projectileClass;
	private double projectileSpeed;

	public ShootingRunnable(int shots, double projectileSpeed, CustomEntity shooter,
			Class<? extends Projectile> projectileClass) {
		this.projectileClass = projectileClass;
		this.shooter = shooter;
		this.shots = shots;
		this.shotCounter = 0;
		this.projectileSpeed = projectileSpeed;
	}

	@Override
	public void run() {
		if (shotCounter >= shots) {
			this.cancel();
			return;
		}
		shooter.broadcastArmSwing();
		Projectile proj = ((CraftLivingEntity) shooter).launchProjectile(projectileClass);
		proj.setVelocity(proj.getVelocity().multiply(projectileSpeed));
		shotCounter++;
	}
}