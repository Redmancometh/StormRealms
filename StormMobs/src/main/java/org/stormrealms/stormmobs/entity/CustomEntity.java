package org.stormrealms.stormmobs.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.stormrealms.stormcore.StormCore;
import org.stormrealms.stormmobs.tasks.ShootingRunnable;
import org.stormrealms.stormmobs.util.MobUtil;

import io.netty.util.internal.ThreadLocalRandom;
import javafx.util.Pair;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.Entity;
import net.minecraft.server.ChatMessage;
import net.minecraft.server.IChatBaseComponent;
import net.minecraft.server.PacketPlayOutTitle;
import net.minecraft.server.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.PlayerConnection;

/**
 * 
 * TODO: Port all this shit to Java 8. Returning pairs instead of just having
 * callbacks is retarded.
 * 
 * 
 * 
 * @author Redmancometh
 *
 * @param <T>
 * 
 *            Make sure you set a method called setNameSupplier or a static
 *            Supplier<String> field with @Setter on it
 * 
 */
public interface CustomEntity<T extends Entity, U extends CustomEntity> {

	public default String getName() {
		return nameSupplier().get();
	}

	public abstract Supplier<String> nameSupplier();

	public abstract Function<World, U> spawnSupplier();

	public default T spawn(Location loc) {
		T e = MobUtil.<T>spawnEntity(spawnSupplier().apply(loc.getWorld()), loc);
		onSpawn();
		return (T) e;
	}

	public void onSpawn();

	public T getEntity();

	/**
	 * 
	 * @param title
	 * @param subTitle
	 */
	public default void sendTitle(String title, String subTitle) {
		IChatBaseComponent titleComp = new ChatMessage(ChatColor.translateAlternateColorCodes('&', title));
		IChatBaseComponent sTitlecomp = new ChatMessage(ChatColor.translateAlternateColorCodes('&', subTitle));
		PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, titleComp, 5, 40, 5);
		PacketPlayOutTitle subTitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, sTitlecomp, 5, 40, 5);
		getNearbyPlayers().forEach((p) -> { sendTitlePackets(p, titlePacket, subTitlePacket); });
	}

	public default Location firstTowards(Location to) {
		CraftEntity e = (CraftEntity) getEntity().getBukkitEntity();
		BlockIterator it = new BlockIterator(e.getWorld(), e.getLocation().toVector(), getVectorTo(to), 0, 2);
		return it.next().getLocation();
	}

	/**
	 * 
	 * @param title
	 * @param subTitle
	 * @param p
	 */
	public default void sendTitle(String title, String subTitle, Player p) {
		IChatBaseComponent titleComp = new ChatMessage(ChatColor.translateAlternateColorCodes('&', title));
		IChatBaseComponent sTitlecomp = new ChatMessage(ChatColor.translateAlternateColorCodes('&', subTitle));
		PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(EnumTitleAction.TITLE, titleComp, 5, 40, 5);
		PacketPlayOutTitle subTitlePacket = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, sTitlecomp, 5, 40, 5);
		sendTitlePackets(p, titlePacket, subTitlePacket);
	}

	/**
	 * 
	 * @param p
	 * @param titlePacket
	 * @param subTitlePacket
	 */
	public default void sendTitlePackets(Player p, PacketPlayOutTitle titlePacket, PacketPlayOutTitle subTitlePacket) {
		PlayerConnection c = ((CraftPlayer) p).getHandle().playerConnection;
		Bukkit.getScheduler().scheduleAsyncDelayedTask(StormCore.getInstance(), () -> c.sendPacket(titlePacket));
		Bukkit.getScheduler().scheduleAsyncDelayedTask(StormCore.getInstance(), () -> c.sendPacket(subTitlePacket));
	}

	/**
	 * 
	 * @return
	 */
	public default List<Player> getNearbyPlayers() {
		List<Player> players = new ArrayList();
		getEntity().getBukkitEntity().getNearbyEntities(40, 15, 40).forEach((e) -> {
			if (e instanceof Player) {
				players.add((Player) e);
			}
		});
		return players;
	}

	public default void lookAtNearest() {
		Pair<Boolean, Player> targetData = getNearestTarget();
		if (!targetData.getKey())
			return;
		Location from = this.getEntity().getBukkitEntity().getLocation();
		Player at = targetData.getValue();
		if (from == null || at == null)
			return;
		Location loc = getEntity().getBukkitEntity().getLocation();
		double xDiff = at.getLocation().getX() - loc.getX();
		double zDiff = at.getLocation().getZ() - loc.getZ();
		double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
		if (distanceXZ > 20 || !((LivingEntity) this.getEntity()).hasLineOfSight(at))
			return;
		/*
		 * ((EntityInsentient) this.getEntity()).getControllerLook().a(((CraftPlayer)
		 * at).getHandle(), 10.0F, ((EntityInsentient) this.getEntity()).bQ());
		 */
	}

	/**
	 * 
	 * @return A pair serving as an optional. If the boolean is true a target was
	 *         found, but if it's false no target was found and the value will be
	 *         null
	 */
	public default Pair<Boolean, Player> getNearestTarget() {
		Pair<Double, Player> closestPlayer = null;
		for (org.bukkit.entity.Entity e : getEntity().getBukkitEntity().getNearbyEntities(30, 15, 30)) {
			if (e instanceof Player) {
				double dist = e.getLocation().distanceSquared(getEntity().getBukkitEntity().getLocation());
				if (closestPlayer == null || closestPlayer.getKey() > dist) {
					closestPlayer = new Pair(dist, e);
				}
			}
		}
		if (closestPlayer != null) {
			return new Pair(true, closestPlayer.getValue());
		}
		return new Pair(false, null);
	}

	public default void fireAtNearest(Class<? extends Projectile> projectileClass, double speedMultiplier, int shots) {
		Pair<Boolean, Player> targetOption = getNearestTarget();
		if (targetOption.getKey()) {
			if (this instanceof LivingEntity)
				lookAt(targetOption.getValue());
			sendTitle(ChatColor.DARK_RED + "Targeted", ChatColor.GOLD + "The boss is targeting you!",
					targetOption.getValue());
			broadcastArmSwing();
			for (int x = 0; x < shots; x++) {
				Projectile proj = ((LivingEntity) (getEntity())).launchProjectile(projectileClass);
				proj.setMetadata("bossprojectile", new FixedMetadataValue(StormCore.getInstance(), true));
				proj.setVelocity(proj.getVelocity().multiply(2));
			}
		}
	}

	/**
	 * Get nearby players for nonliving entities. This is 100% necessary.
	 * 
	 * @return
	 */
	public default List<Player> playersNearNL(Location loc, int radius) {
		return Bukkit.getOnlinePlayers().stream().filter((p -> p.getWorld() == loc.getWorld()))
				.filter((ent) -> (ent.getLocation().distance(loc) < radius)).collect(Collectors.toList());
	}

	public default Player randomNearby(int radius) {
		List<Player> es = getEntity().getBukkitEntity().getNearbyEntities(radius, radius, radius).stream()
				.filter(entity -> entity instanceof Player).map(entity -> (Player) entity).collect(Collectors.toList());
		if (es.size() < 1)
			return null;
		Player p = es.get(ThreadLocalRandom.current().nextInt(es.size()));
		return p;
	}

	public default void fireAtRandom(Class<? extends Projectile> projectileClass, double speedMultiplier, int shots,
			int delay, boolean notify) {
		List<Player> es = getEntity().getBukkitEntity().getNearbyEntities(30, 30, 30).stream()
				.filter(entity -> entity instanceof Player).map(entity -> (Player) entity).collect(Collectors.toList());
		if (es.size() < 1)
			return;
		Player p = es.get(ThreadLocalRandom.current().nextInt(es.size()));
		if (this instanceof LivingEntity)
			lookAt(p);
		if (notify) {
			sendTitle(ChatColor.DARK_RED + "Targeted", ChatColor.GOLD + "The boss is targeting you!", p);
		}
		ShootingRunnable shoot = new ShootingRunnable(shots, speedMultiplier, (CustomEntity) getEntity(),
				projectileClass);
		shoot.runTaskTimer(StormCore.getInstance(), 0, delay);

	}

	public default void fireAtNearest(Class<? extends Projectile> projectileClass, double speedMultiplier, int shots,
			int delay, boolean notify) {
		Pair<Boolean, Player> targetOption = getNearestTarget();
		if (targetOption.getKey()) {
			if (this instanceof LivingEntity)
				lookAt(targetOption.getValue());
			if (notify) {
				sendTitle(ChatColor.DARK_RED + "Targeted", ChatColor.GOLD + "The boss is targeting you!",
						targetOption.getValue());
			}
			ShootingRunnable shoot = new ShootingRunnable(shots, speedMultiplier, (CustomEntity) getEntity(),
					projectileClass);
			shoot.runTaskTimer(StormCore.getInstance(), 0, delay);
		}
	}

	public default void fireAt(Player target, Class<? extends Projectile> projectileClass, double speedMultiplier,
			int shots, boolean notify) {
		if (this instanceof LivingEntity)
			lookAt(target);
		if (notify) {
			sendTitle(ChatColor.DARK_RED + "Targeted", ChatColor.GOLD + "The boss is targeting you!", target);
		}
		for (int x = 0; x < shots; x++) {
			Projectile proj = ((LivingEntity) (getEntity())).launchProjectile(projectileClass);
			proj.setVelocity(proj.getVelocity().multiply(2));
		}
	}

	public default void fireAt(Player target, Class<? extends Projectile> projectileClass, double speedMultiplier,
			int shots, int delay, boolean notify) {
		if (this instanceof LivingEntity)
			lookAt(target);
		if (notify)
			sendTitle(ChatColor.DARK_RED + "Targeted", ChatColor.GOLD + "The boss is targeting you!", target);
		ShootingRunnable shoot = new ShootingRunnable(shots, speedMultiplier, (CustomEntity) getEntity(),
				projectileClass);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(StormCore.getInstance(), shoot, 0, delay);
	}

	public default void broadcastArmSwing() {
		getEntity().world.broadcastEntityEffect(getEntity(), (byte) 4);
	}

	/**
	 * Find sounds here:
	 * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/CraftSound.java?at=cee6a7bab52ebb33c17a76b2b81891111e2f9d93&raw
	 * 
	 * @param sound
	 */
	public default void playSound(String sound) {
		// getEntity().world.playSound(getEntity().getBukkitEntity(), sound, 1F, 1F);
	}

	public default void lookAt(org.bukkit.entity.Entity target) {
		Entity at = ((CraftEntity) target).getHandle();
		Location loc = getEntity().getBukkitEntity().getLocation();
		double xDiff = at.getBukkitEntity().getLocation().getX() - loc.getX();
		double zDiff = at.getBukkitEntity().getLocation().getZ() - loc.getZ();
		double distanceXZ = Math.sqrt(Math.pow(xDiff, 2) + Math.pow(zDiff, 2));
		/*
		 * TODO: I don't know how to do this yet in 1.15.2 - Redman
		 * 
		 * if (distanceXZ > 20 || !((LivingEntity)
		 * this.getEntity().getBukkitEntity()).hasLineOfSight(at)) return;
		 * ((EntityInsentient) this.getEntity()).getControllerLook().a(at, 10.0F,
		 * ((EntityInsentient) this.getEntity()).bQ());
		 */
	}

	public default Vector getVectorTo(Location to, double speed) {
		return getVectorTo(to).multiply(speed);
	}

	public default Vector getVectorTo(Location to) {
		Location loc = this.getEntity().getBukkitEntity().getLocation();
		double x = loc.getX() - to.getX();
		double y = loc.getY() - to.getY();
		double z = loc.getZ() - to.getZ();
		return new Vector(x, y, z).normalize();
	}

	public default void moveToward(Location to, double speed) {
		CraftEntity entity = (CraftEntity) getEntity().getBukkitEntity();
		Location loc = entity.getLocation();
		double x = loc.getX() - to.getX();
		double y = loc.getY() - to.getY();
		double z = loc.getZ() - to.getZ();
		Vector velocity = new Vector(x, y, z).normalize().multiply(-speed);
		entity.setVelocity(velocity);
	}

	public default void messageNearby(String message) {
		String coloredMessage = ChatColor.translateAlternateColorCodes('&',
				CustomEntity.this.getName() + " " + message);
		getNearbyPlayers().forEach((p) -> p.sendMessage(coloredMessage));
	}

	public abstract void tick();

}
