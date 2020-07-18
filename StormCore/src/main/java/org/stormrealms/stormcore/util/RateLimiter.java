package org.stormrealms.stormcore.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.stormrealms.stormcore.StormCore;

/**
 * 
 * @author Redmancometh
 *
 */
public class RateLimiter {
	private long lockoutTime;
	private TimeUnit units;
	Consumer<Player> onRemove;
	private List<UUID> lockoutList = new ArrayList();
	// This is being used so we don't have to tune our shit to ticks.
	private ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

	public RateLimiter(TimeUnit units, long lockoutTime) {
		this.units = units;
		this.lockoutTime = lockoutTime;
	}

	public RateLimiter(TimeUnit units, long lockoutTime, Consumer<Player> onRemove) {
		this.units = units;
		this.lockoutTime = lockoutTime;
		this.onRemove = onRemove;
	}

	/**
	 * 
	 * @param p
	 * @param succeed
	 * @param fail
	 */
	public void tryAction(Player p, Runnable succeed, Runnable fail) {
		if (lockoutList.contains(p.getUniqueId())) {
			fail.run();
			return;
		}
		lockoutList.add(p.getUniqueId());
		exec.schedule(() -> {
			// Ugly but we want to stick it back on the main thread.
			Bukkit.getScheduler().runTask(StormCore.getInstance(), () -> {
				this.lockoutList.remove(p.getUniqueId());
				if (onRemove != null)
					onRemove.accept(p);
			});
		}, lockoutTime, units);
		succeed.run();
	}

}
