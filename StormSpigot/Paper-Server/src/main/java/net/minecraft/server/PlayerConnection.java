package net.minecraft.server;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryView;
import org.bukkit.util.NumberConversions;
import com.destroystokyo.paper.event.player.IllegalPacketEvent; // Paper
import com.destroystokyo.paper.event.player.PlayerJumpEvent; // Paper
import co.aikar.timings.MinecraftTimings; // Paper
// CraftBukkit end

public class PlayerConnection implements PacketListenerPlayIn {

	private static final Logger LOGGER = LogManager.getLogger();
	public final NetworkManager networkManager;
	private final MinecraftServer minecraftServer;
	Runnable playerJoinReady; // Paper
	public EntityPlayer player;
	private int e;
	private long lastKeepAlive = SystemUtils.getMonotonicMillis();

	private void setLastPing(long lastPing) {
		this.lastKeepAlive = lastPing;
	};

	private long getLastPing() {
		return this.lastKeepAlive;
	}; // Paper - OBFHELPER

	private boolean awaitingKeepAlive;

	private void setPendingPing(boolean isPending) {
		this.awaitingKeepAlive = isPending;
	};

	private boolean isPendingPing() {
		return this.awaitingKeepAlive;
	}; // Paper - OBFHELPER

	private long h;

	private void setKeepAliveID(long keepAliveID) {
		this.h = keepAliveID;
	};

	private long getKeepAliveID() {
		return this.h;
	}; // Paper - OBFHELPER
	// CraftBukkit start - multithreaded fields

	private volatile int chatThrottle;
	private static final AtomicIntegerFieldUpdater chatSpamField = AtomicIntegerFieldUpdater
			.newUpdater(PlayerConnection.class, "chatThrottle");
	private final java.util.concurrent.atomic.AtomicInteger tabSpamLimiter = new java.util.concurrent.atomic.AtomicInteger(); // Paper
																																// -
																																// configurable
																																// tab
																																// spam
																																// limits
	// CraftBukkit end
	private int j;
	private final Int2ShortMap k = new Int2ShortOpenHashMap();
	private double l;
	private double m;
	private double n;
	private double o;
	private double p;
	private double q;
	private Entity r;
	private double s;
	private double t;
	private double u;
	private double v;
	private double w;
	private double x;
	private Vec3D teleportPos;
	private int teleportAwait;
	private int A;
	private boolean B;
	private int C;
	private boolean D;
	private int E;
	private int receivedMovePackets;
	private int processedMovePackets;
	private static final int MAX_SIGN_LINE_LENGTH = Integer.getInteger("Paper.maxSignLength", 80);
	private static final long KEEPALIVE_LIMIT = Long.getLong("paper.playerconnection.keepalive", 30) * 1000; // Paper -
																												// provide
																												// property
																												// to
																												// set
																												// keepalive
																												// limit

	public PlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
		this.minecraftServer = minecraftserver;
		this.networkManager = networkmanager;
		networkmanager.setPacketListener(this);
		this.player = entityplayer;
		entityplayer.playerConnection = this;

		// CraftBukkit start - add fields and methods
		this.server = minecraftserver.server;
	}

	private final org.bukkit.craftbukkit.CraftServer server;
	public boolean processedDisconnect;
	private int lastTick = MinecraftServer.currentTick;
	private int allowedPlayerTicks = 1;
	private int lastDropTick = MinecraftServer.currentTick;
	private int lastBookTick = MinecraftServer.currentTick;
	private int dropCount = 0;
	private static final int SURVIVAL_PLACE_DISTANCE_SQUARED = 6 * 6;
	private static final int CREATIVE_PLACE_DISTANCE_SQUARED = 7 * 7;

	// Get position of last block hit for BlockDamageLevel.STOPPED
	private double lastPosX = Double.MAX_VALUE;
	private double lastPosY = Double.MAX_VALUE;
	private double lastPosZ = Double.MAX_VALUE;
	private float lastPitch = Float.MAX_VALUE;
	private float lastYaw = Float.MAX_VALUE;
	private boolean justTeleported = false;
	private boolean hasMoved; // Spigot

	public CraftPlayer getPlayer() {
		return (this.player == null) ? null : (CraftPlayer) this.player.getBukkitEntity();
	}
	// CraftBukkit end

	public void tick() {
		// Paper start - login async
		Runnable playerJoinReady = this.playerJoinReady;
		if (playerJoinReady != null) {
			this.playerJoinReady = null;
			playerJoinReady.run();
		}
		// Don't tick if not valid (dead), otherwise we load chunks below
		if (this.player.valid) {
			// Paper end
			this.syncPosition();
			this.player.lastX = this.player.locX();
			this.player.lastY = this.player.locY();
			this.player.lastZ = this.player.locZ();
			this.player.playerTick();
			this.player.setLocation(this.l, this.m, this.n, this.player.yaw, this.player.pitch);
			++this.e;
			this.processedMovePackets = this.receivedMovePackets;
			if (this.B && !this.player.isSleeping()) { // Paper - #3176 Allow sleeping players to float
				if (++this.C > 80) {
					PlayerConnection.LOGGER.warn("{} was kicked for floating too long!",
							this.player.getDisplayName().getString());
					this.disconnect(com.destroystokyo.paper.PaperConfig.flyingKickPlayerMessage); // Paper - use
																									// configurable kick
																									// message
					return;
				}
			} else {
				this.B = false;
				this.C = 0;
			}

			this.r = this.player.getRootVehicle();
			if (this.r != this.player && this.r.getRidingPassenger() == this.player) {
				this.s = this.r.locX();
				this.t = this.r.locY();
				this.u = this.r.locZ();
				this.v = this.r.locX();
				this.w = this.r.locY();
				this.x = this.r.locZ();
				if (this.D && this.player.getRootVehicle().getRidingPassenger() == this.player) {
					if (++this.E > 80) {
						PlayerConnection.LOGGER.warn("{} was kicked for floating a vehicle too long!",
								this.player.getDisplayName().getString());
						this.disconnect(com.destroystokyo.paper.PaperConfig.flyingKickVehicleMessage); // Paper - use
																										// configurable
																										// kick message
						return;
					}
				} else {
					this.D = false;
					this.E = 0;
				}
			} else {
				this.r = null;
				this.D = false;
				this.E = 0;
			}
		} // Paper - end if (valid)

		this.minecraftServer.getMethodProfiler().enter("keepAlive");
		// Paper Start - give clients a longer time to respond to pings as per pre
		// 1.12.2 timings
		// This should effectively place the keepalive handling back to "as it was"
		// before 1.12.2
		long currentTime = SystemUtils.getMonotonicMillis();
		long elapsedTime = currentTime - this.getLastPing();

		if (this.isPendingPing()) {
			if (!this.processedDisconnect && elapsedTime >= KEEPALIVE_LIMIT) { // check keepalive limit, don't fire if
																				// already disconnected
				PlayerConnection.LOGGER.warn("{} was kicked due to keepalive timeout!", this.player.getName()); // more
																												// info
				this.disconnect(new ChatMessage("disconnect.timeout", new Object[0]));
			}
		} else {
			if (elapsedTime >= 15000L) { // 15 seconds
				this.setPendingPing(true);
				this.setLastPing(currentTime);
				this.setKeepAliveID(currentTime);
				this.sendPacket(new PacketPlayOutKeepAlive(this.getKeepAliveID()));

			}
		}
		// Paper end

		this.minecraftServer.getMethodProfiler().exit();
		// CraftBukkit start
		for (int spam; (spam = this.chatThrottle) > 0 && !chatSpamField.compareAndSet(this, spam, spam - 1);)
			;
		if (tabSpamLimiter.get() > 0)
			tabSpamLimiter.getAndDecrement(); // Paper - split to seperate variable
		/*
		 * Use thread-safe field access instead if (this.chatThrottle > 0) {
		 * --this.chatThrottle; }
		 */
		// CraftBukkit end

		if (this.j > 0) {
			--this.j;
		}

		if (this.player.F() > 0L && this.minecraftServer.getIdleTimeout() > 0 && SystemUtils.getMonotonicMillis()
				- this.player.F() > (long) (this.minecraftServer.getIdleTimeout() * 1000 * 60)) {
			this.player.resetIdleTimer(); // CraftBukkit - SPIGOT-854
			this.disconnect(new ChatMessage("multiplayer.disconnect.idling", new Object[0]));
		}

	}

	public void syncPosition() {
		this.l = this.player.locX();
		this.m = this.player.locY();
		this.n = this.player.locZ();
		this.o = this.player.locX();
		this.p = this.player.locY();
		this.q = this.player.locZ();
	}

	@Override
	public NetworkManager a() {
		return this.networkManager;
	}

	private boolean isExemptPlayer() {
		return this.minecraftServer.a(this.player.getProfile());
	}

	// CraftBukkit start
	@Deprecated
	public void disconnect(IChatBaseComponent ichatbasecomponent) {
		disconnect(CraftChatMessage.fromComponent(ichatbasecomponent));
	}
	// CraftBukkit end

	public void disconnect(String s) {
		// CraftBukkit start - fire PlayerKickEvent
		if (this.processedDisconnect) {
			return;
		}
		String leaveMessage = EnumChatFormat.YELLOW + this.player.getName() + " left the game.";

		PlayerKickEvent event = new PlayerKickEvent(this.server.getPlayer(this.player), s, leaveMessage);

		if (this.server.getServer().isRunning()) {
			this.server.getPluginManager().callEvent(event);
		}

		if (event.isCancelled()) {
			// Do not kick the player
			return;
		}
		// Send the possibly modified leave message
		s = event.getReason();
		// CraftBukkit end
		final ChatComponentText ichatbasecomponent = new ChatComponentText(s);

		this.networkManager.sendPacket(new PacketPlayOutKickDisconnect(ichatbasecomponent), (future) -> {
			this.networkManager.close(ichatbasecomponent);
		});
		this.a(ichatbasecomponent); // CraftBukkit - fire quit instantly
		this.networkManager.stopReading();
		MinecraftServer minecraftserver = this.minecraftServer;
		NetworkManager networkmanager = this.networkManager;

		this.networkManager.getClass();
		// CraftBukkit - Don't wait
		minecraftserver.scheduleOnMain(networkmanager::handleDisconnection); // Paper
	}

	@Override
	public void a(PacketPlayInSteerVehicle packetplayinsteervehicle) {
		PlayerConnectionUtils.ensureMainThread(packetplayinsteervehicle, this, this.player.getWorldServer());
		this.player.a(packetplayinsteervehicle.b(), packetplayinsteervehicle.c(), packetplayinsteervehicle.d(),
				packetplayinsteervehicle.e());
	}

	private static boolean b(PacketPlayInFlying packetplayinflying) {
		return Doubles.isFinite(packetplayinflying.a(0.0D)) && Doubles.isFinite(packetplayinflying.b(0.0D))
				&& Doubles.isFinite(packetplayinflying.c(0.0D)) && Floats.isFinite(packetplayinflying.b(0.0F))
				&& Floats.isFinite(packetplayinflying.a(0.0F))
						? Math.abs(packetplayinflying.a(0.0D)) > 3.0E7D || Math.abs(packetplayinflying.b(0.0D)) > 3.0E7D
								|| Math.abs(packetplayinflying.c(0.0D)) > 3.0E7D
						: true;
	}

	private static boolean b(PacketPlayInVehicleMove packetplayinvehiclemove) {
		return !Doubles.isFinite(packetplayinvehiclemove.getX()) || !Doubles.isFinite(packetplayinvehiclemove.getY())
				|| !Doubles.isFinite(packetplayinvehiclemove.getZ())
				|| !Floats.isFinite(packetplayinvehiclemove.getPitch())
				|| !Floats.isFinite(packetplayinvehiclemove.getYaw());
	}

	@Override
	public void a(PacketPlayInVehicleMove packetplayinvehiclemove) {
		PlayerConnectionUtils.ensureMainThread(packetplayinvehiclemove, this, this.player.getWorldServer());
		if (b(packetplayinvehiclemove)) {
			this.disconnect(new ChatMessage("multiplayer.disconnect.invalid_vehicle_movement", new Object[0]));
		} else {
			Entity entity = this.player.getRootVehicle();

			if (entity != this.player && entity.getRidingPassenger() == this.player && entity == this.r) {
				WorldServer worldserver = this.player.getWorldServer();
				double d0 = entity.locX();
				double d1 = entity.locY();
				double d2 = entity.locZ();
				double d3 = packetplayinvehiclemove.getX();
				double d4 = packetplayinvehiclemove.getY();
				double d5 = packetplayinvehiclemove.getZ();
				float f = packetplayinvehiclemove.getYaw();
				float f1 = packetplayinvehiclemove.getPitch();
				double d6 = d3 - this.s;
				double d7 = d4 - this.t;
				double d8 = d5 - this.u;
				double d9 = entity.getMot().g();
				double d10 = d6 * d6 + d7 * d7 + d8 * d8;

				// CraftBukkit start - handle custom speeds and skipped ticks
				this.allowedPlayerTicks += (System.currentTimeMillis() / 50) - this.lastTick;
				this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
				this.lastTick = (int) (System.currentTimeMillis() / 50);

				++this.receivedMovePackets;
				int i = this.receivedMovePackets - this.processedMovePackets;
				if (i > Math.max(this.allowedPlayerTicks, 5)) {
					PlayerConnection.LOGGER.debug(this.player.getName() + " is sending move packets too frequently ("
							+ i + " packets since last tick)");
					i = 1;
				}

				if (d10 > 0) {
					allowedPlayerTicks -= 1;
				} else {
					allowedPlayerTicks = 20;
				}
				double speed;
				if (player.abilities.isFlying) {
					speed = player.abilities.flySpeed * 20f;
				} else {
					speed = player.abilities.walkSpeed * 10f;
				}
				speed *= 2f; // TODO: Get the speed of the vehicle instead of the player

				// Paper start - Prevent moving into unloaded chunks
				if (player.world.paperConfig.preventMovingIntoUnloadedChunks && worldserver.getChunkIfLoadedImmediately(
						(int) Math.floor(packetplayinvehiclemove.getX()) >> 4,
						(int) Math.floor(packetplayinvehiclemove.getZ()) >> 4) == null) {
					this.networkManager.sendPacket(new PacketPlayOutVehicleMove(entity));
					return;
				}
				// Paper end

				if (d10 - d9 > Math.max(100.0D,
						Math.pow((double) (org.spigotmc.SpigotConfig.movedTooQuicklyMultiplier * (float) i * speed), 2))
						&& !this.isExemptPlayer()) {
					// CraftBukkit end
					PlayerConnection.LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}",
							entity.getDisplayName().getString(), this.player.getDisplayName().getString(), d6, d7, d8);
					this.networkManager.sendPacket(new PacketPlayOutVehicleMove(entity));
					return;
				}

				boolean flag = worldserver.getCubes(entity, entity.getBoundingBox().shrink(0.0625D));

				d6 = d3 - this.v;
				d7 = d4 - this.w - 1.0E-6D;
				d8 = d5 - this.x;
				entity.move(EnumMoveType.PLAYER, new Vec3D(d6, d7, d8));
				double d11 = d7;

				d6 = d3 - entity.locX();
				d7 = d4 - entity.locY();
				if (d7 > -0.5D || d7 < 0.5D) {
					d7 = 0.0D;
				}

				d8 = d5 - entity.locZ();
				d10 = d6 * d6 + d7 * d7 + d8 * d8;
				boolean flag1 = false;

				if (d10 > org.spigotmc.SpigotConfig.movedWronglyThreshold) { // Spigot
					flag1 = true;
					PlayerConnection.LOGGER
							.warn(entity.getName() + " (vehicle of " + this.player.getName() + ") moved wrongly!"); // Paper
																													// -
																													// More
																													// informative
				}
				Location curPos = this.getPlayer().getLocation(); // Spigot

				entity.setLocation(d3, d4, d5, f, f1);
				player.setLocation(d3, d4, d5, this.player.yaw, this.player.pitch); // CraftBukkit
				boolean flag2 = worldserver.getCubes(entity, entity.getBoundingBox().shrink(0.0625D));

				if (flag && (flag1 || !flag2)) {
					entity.setLocation(d0, d1, d2, f, f1);
					player.setLocation(d0, d1, d2, this.player.yaw, this.player.pitch); // CraftBukkit
					this.networkManager.sendPacket(new PacketPlayOutVehicleMove(entity));
					return;
				}

				// CraftBukkit start - fire PlayerMoveEvent
				Player player = this.getPlayer();
				// Spigot Start
				if (!hasMoved) {
					lastPosX = curPos.getX();
					lastPosY = curPos.getY();
					lastPosZ = curPos.getZ();
					lastYaw = curPos.getYaw();
					lastPitch = curPos.getPitch();
					hasMoved = true;
				}
				// Spigot End
				Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw, lastPitch); // Get
																													// the
																													// Players
																													// previous
																													// Event
																													// location.
				Location to = player.getLocation().clone(); // Start off the To location as the Players current
															// location.

				// If the packet contains movement information then we update the To location
				// with the correct XYZ.
				to.setX(packetplayinvehiclemove.getX());
				to.setY(packetplayinvehiclemove.getY());
				to.setZ(packetplayinvehiclemove.getZ());

				// If the packet contains look information then we update the To location with
				// the correct Yaw & Pitch.
				to.setYaw(packetplayinvehiclemove.getYaw());
				to.setPitch(packetplayinvehiclemove.getPitch());

				// Prevent 40 event-calls for less than a single pixel of movement >.>
				double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2)
						+ Math.pow(this.lastPosZ - to.getZ(), 2);
				float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

				if ((delta > 1f / 256 || deltaAngle > 10f) && !this.player.isFrozen()) {
					this.lastPosX = to.getX();
					this.lastPosY = to.getY();
					this.lastPosZ = to.getZ();
					this.lastYaw = to.getYaw();
					this.lastPitch = to.getPitch();

					// Skip the first time we do this
					if (true) { // Spigot - don't skip any move events
						Location oldTo = to.clone();
						PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
						this.server.getPluginManager().callEvent(event);

						// If the event is cancelled we move the player back to their old location.
						if (event.isCancelled()) {
							teleport(from);
							return;
						}

						// If a Plugin has changed the To destination then we teleport the Player
						// there to avoid any 'Moved wrongly' or 'Moved too quickly' errors.
						// We only do this if the Event was not cancelled.
						if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
							this.player.getBukkitEntity().teleport(event.getTo(),
									PlayerTeleportEvent.TeleportCause.PLUGIN);
							return;
						}

						// Check to see if the Players Location has some how changed during the call of
						// the event.
						// This can happen due to a plugin teleporting the player instead of using
						// .setTo()
						if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
							this.justTeleported = false;
							return;
						}
					}
				}
				// CraftBukkit end

				this.player.getWorldServer().getChunkProvider().movePlayer(this.player);
				this.player.checkMovement(this.player.locX() - d0, this.player.locY() - d1, this.player.locZ() - d2);
				this.D = d11 >= -0.03125D && !this.minecraftServer.getAllowFlight()
						&& !worldserver.b(entity.getBoundingBox().g(0.0625D).b(0.0D, -0.55D, 0.0D));
				this.v = entity.locX();
				this.w = entity.locY();
				this.x = entity.locZ();
			}

		}
	}

	@Override
	public void a(PacketPlayInTeleportAccept packetplayinteleportaccept) {
		PlayerConnectionUtils.ensureMainThread(packetplayinteleportaccept, this, this.player.getWorldServer());
		if (packetplayinteleportaccept.b() == this.teleportAwait && this.teleportPos != null) { // CraftBukkit
			this.player.setLocation(this.teleportPos.x, this.teleportPos.y, this.teleportPos.z, this.player.yaw,
					this.player.pitch);
			this.o = this.teleportPos.x;
			this.p = this.teleportPos.y;
			this.q = this.teleportPos.z;
			this.syncPosition(); // Paper
			if (this.player.H()) {
				this.player.I();
			}

			this.teleportPos = null;
			this.player.getWorldServer().getChunkProvider().movePlayer(this.player); // CraftBukkit
		}

	}

	@Override
	public void a(PacketPlayInRecipeDisplayed packetplayinrecipedisplayed) {
		PlayerConnectionUtils.ensureMainThread(packetplayinrecipedisplayed, this, this.player.getWorldServer());
		if (packetplayinrecipedisplayed.b() == PacketPlayInRecipeDisplayed.Status.SHOWN) {
			Optional<? extends IRecipe<?>> optional = this.minecraftServer.getCraftingManager()
					.a(packetplayinrecipedisplayed.c()); // CraftBukkit - decompile error
			RecipeBookServer recipebookserver = this.player.B();

			optional.ifPresent(recipebookserver::e);
		} else if (packetplayinrecipedisplayed.b() == PacketPlayInRecipeDisplayed.Status.SETTINGS) {
			this.player.B().a(packetplayinrecipedisplayed.d());
			this.player.B().b(packetplayinrecipedisplayed.e());
			this.player.B().c(packetplayinrecipedisplayed.f());
			this.player.B().d(packetplayinrecipedisplayed.g());
			this.player.B().e(packetplayinrecipedisplayed.h());
			this.player.B().f(packetplayinrecipedisplayed.i());
			this.player.B().g(packetplayinrecipedisplayed.j());
			this.player.B().h(packetplayinrecipedisplayed.k());
		}

	}

	@Override
	public void a(PacketPlayInAdvancements packetplayinadvancements) {
		PlayerConnectionUtils.ensureMainThread(packetplayinadvancements, this, this.player.getWorldServer());
		if (packetplayinadvancements.c() == PacketPlayInAdvancements.Status.OPENED_TAB) {
			MinecraftKey minecraftkey = packetplayinadvancements.d();
			Advancement advancement = this.minecraftServer.getAdvancementData().a(minecraftkey);

			if (advancement != null) {
				this.player.getAdvancementData().a(advancement);
			}
		}

	}

	@Override
	public void a(PacketPlayInTabComplete packetplayintabcomplete) {
		// PlayerConnectionUtils.ensureMainThread(packetplayintabcomplete, this,
		// this.player.getWorldServer()); // Paper - run this async
		// CraftBukkit start
		if (tabSpamLimiter.addAndGet(
				com.destroystokyo.paper.PaperConfig.tabSpamIncrement) > com.destroystokyo.paper.PaperConfig.tabSpamLimit
				&& !this.minecraftServer.getPlayerList().isOp(this.player.getProfile())) { // Paper start - split and
																							// make configurable
			minecraftServer.scheduleOnMain(() -> this.disconnect(new ChatMessage("disconnect.spam", new Object[0]))); // Paper
			return;
		}
		// Paper start
		String str = packetplayintabcomplete.c();
		int index = -1;
		if (str.length() > 64 && ((index = str.indexOf(' ')) == -1 || index >= 64)) {
			minecraftServer.scheduleOnMain(() -> this.disconnect(new ChatMessage("disconnect.spam", new Object[0]))); // Paper
			return;
		}
		// Paper end
		// CraftBukkit end
		StringReader stringreader = new StringReader(packetplayintabcomplete.c());

		if (stringreader.canRead() && stringreader.peek() == '/') {
			stringreader.skip();
		}

		// Paper start - async tab completion
		com.destroystokyo.paper.event.server.AsyncTabCompleteEvent event;
		java.util.List<String> completions = new java.util.ArrayList<>();
		String buffer = packetplayintabcomplete.c();
		event = new com.destroystokyo.paper.event.server.AsyncTabCompleteEvent(this.getPlayer(), completions, buffer,
				true, null);
		event.callEvent();
		completions = event.isCancelled() ? com.google.common.collect.ImmutableList.of() : event.getCompletions();
		// If the event isn't handled, we can assume that we have no completions, and so
		// we'll ask the server
		if (!event.isHandled()) {
			if (!event.isCancelled()) {
				// Paper end - async tab completion
				this.minecraftServer.scheduleOnMain(() -> { // Paper - This needs to be on main
					ParseResults<CommandListenerWrapper> parseresults = this.minecraftServer.getCommandDispatcher().a()
							.parse(stringreader, this.player.getCommandListener());

					this.minecraftServer.getCommandDispatcher().a().getCompletionSuggestions(parseresults)
							.thenAccept((suggestions) -> {
								// Paper start
								com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent suggestEvent = new com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent(
										this.getPlayer(), suggestions, buffer);
								suggestEvent.setCancelled(suggestions.isEmpty());
								if (!suggestEvent.callEvent())
									return;
								this.networkManager.sendPacket(new PacketPlayOutTabComplete(packetplayintabcomplete.b(),
										(Suggestions) suggestEvent.getSuggestions())); // CraftBukkit - decompile error
																						// // Paper
								// Paper end
							});
				}); // Paper - This needs to be on main
			}
			// Paper start - async tab completion
		} else if (!completions.isEmpty()) {
			com.mojang.brigadier.suggestion.SuggestionsBuilder builder = new com.mojang.brigadier.suggestion.SuggestionsBuilder(
					packetplayintabcomplete.c(), stringreader.getTotalLength());

			builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);
			completions.forEach(builder::suggest);
			Suggestions suggestions = builder.buildFuture().join();
			com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent suggestEvent = new com.destroystokyo.paper.event.brigadier.AsyncPlayerSendSuggestionsEvent(
					this.getPlayer(), suggestions, buffer);
			suggestEvent.setCancelled(suggestions.isEmpty());
			if (!suggestEvent.callEvent())
				return;
			this.networkManager.sendPacket(
					new PacketPlayOutTabComplete(packetplayintabcomplete.b(), suggestEvent.getSuggestions()));
		}
		// Paper end - async tab completion

	}

	@Override
	public void a(PacketPlayInSetCommandBlock packetplayinsetcommandblock) {
		PlayerConnectionUtils.ensureMainThread(packetplayinsetcommandblock, this, this.player.getWorldServer());
		if (!this.minecraftServer.getEnableCommandBlock()) {
			this.player.sendMessage(new ChatMessage("advMode.notEnabled", new Object[0]));
		} else if (!this.player.isCreativeAndOp() && !this.player.isCreative()
				&& !this.player.getBukkitEntity().hasPermission("minecraft.commandblock")) { // Paper - command block
																								// permission
			this.player.sendMessage(new ChatMessage("advMode.notAllowed", new Object[0]));
		} else {
			CommandBlockListenerAbstract commandblocklistenerabstract = null;
			TileEntityCommand tileentitycommand = null;
			BlockPosition blockposition = packetplayinsetcommandblock.b();
			TileEntity tileentity = this.player.world.getTileEntity(blockposition);

			if (tileentity instanceof TileEntityCommand) {
				tileentitycommand = (TileEntityCommand) tileentity;
				commandblocklistenerabstract = tileentitycommand.getCommandBlock();
			}

			String s = packetplayinsetcommandblock.c();
			boolean flag = packetplayinsetcommandblock.d();

			if (commandblocklistenerabstract != null) {
				TileEntityCommand.Type tileentitycommand_type = tileentitycommand.m();
				EnumDirection enumdirection = (EnumDirection) this.player.world.getType(blockposition)
						.get(BlockCommand.a);
				IBlockData iblockdata;

				switch (packetplayinsetcommandblock.g()) {
				case SEQUENCE:
					iblockdata = Blocks.CHAIN_COMMAND_BLOCK.getBlockData();
					this.player.world.setTypeAndData(blockposition,
							(IBlockData) ((IBlockData) iblockdata.set(BlockCommand.a, enumdirection))
									.set(BlockCommand.b, packetplayinsetcommandblock.e()),
							2);
					break;
				case AUTO:
					iblockdata = Blocks.REPEATING_COMMAND_BLOCK.getBlockData();
					this.player.world.setTypeAndData(blockposition,
							(IBlockData) ((IBlockData) iblockdata.set(BlockCommand.a, enumdirection))
									.set(BlockCommand.b, packetplayinsetcommandblock.e()),
							2);
					break;
				case REDSTONE:
				default:
					iblockdata = Blocks.COMMAND_BLOCK.getBlockData();
					this.player.world.setTypeAndData(blockposition,
							(IBlockData) ((IBlockData) iblockdata.set(BlockCommand.a, enumdirection))
									.set(BlockCommand.b, packetplayinsetcommandblock.e()),
							2);
				}

				tileentity.r();
				this.player.world.setTileEntity(blockposition, tileentity);
				commandblocklistenerabstract.setCommand(s);
				commandblocklistenerabstract.a(flag);
				if (!flag) {
					commandblocklistenerabstract.c((IChatBaseComponent) null);
				}

				tileentitycommand.b(packetplayinsetcommandblock.f());
				if (tileentitycommand_type != packetplayinsetcommandblock.g()) {
					tileentitycommand.h();
				}

				commandblocklistenerabstract.e();
				if (!UtilColor.b(s)) {
					this.player.sendMessage(new ChatMessage("advMode.setCommand.success", new Object[] { s }));
				}
			}

		}
	}

	@Override
	public void a(PacketPlayInSetCommandMinecart packetplayinsetcommandminecart) {
		PlayerConnectionUtils.ensureMainThread(packetplayinsetcommandminecart, this, this.player.getWorldServer());
		if (!this.minecraftServer.getEnableCommandBlock()) {
			this.player.sendMessage(new ChatMessage("advMode.notEnabled", new Object[0]));
		} else if (!this.player.isCreativeAndOp() && !this.player.isCreative()
				&& !this.player.getBukkitEntity().hasPermission("minecraft.commandblock")) { // Paper - command block
																								// permission
			this.player.sendMessage(new ChatMessage("advMode.notAllowed", new Object[0]));
		} else {
			CommandBlockListenerAbstract commandblocklistenerabstract = packetplayinsetcommandminecart
					.a(this.player.world);

			if (commandblocklistenerabstract != null) {
				commandblocklistenerabstract.setCommand(packetplayinsetcommandminecart.b());
				commandblocklistenerabstract.a(packetplayinsetcommandminecart.c());
				if (!packetplayinsetcommandminecart.c()) {
					commandblocklistenerabstract.c((IChatBaseComponent) null);
				}

				commandblocklistenerabstract.e();
				this.player.sendMessage(new ChatMessage("advMode.setCommand.success",
						new Object[] { packetplayinsetcommandminecart.b() }));
			}

		}
	}

	@Override
	public void a(PacketPlayInPickItem packetplayinpickitem) {
		PlayerConnectionUtils.ensureMainThread(packetplayinpickitem, this, this.player.getWorldServer());
		// Paper start - validate pick item position
		if (!(packetplayinpickitem.b() >= 0 && packetplayinpickitem.b() < this.player.inventory.items.size())) {
			PlayerConnection.LOGGER.warn("{} tried to set an invalid carried item",
					this.player.getDisplayName().getString());
			this.disconnect("Invalid hotbar selection (Hacking?)");
			return;
		}
		this.player.inventory.c(packetplayinpickitem.b()); // Paper - Diff above if changed
		// Paper end
		this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-2, this.player.inventory.itemInHandIndex,
				this.player.inventory.getItem(this.player.inventory.itemInHandIndex)));
		this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-2, packetplayinpickitem.b(),
				this.player.inventory.getItem(packetplayinpickitem.b())));
		this.player.playerConnection.sendPacket(new PacketPlayOutHeldItemSlot(this.player.inventory.itemInHandIndex));
	}

	@Override
	public void a(PacketPlayInItemName packetplayinitemname) {
		PlayerConnectionUtils.ensureMainThread(packetplayinitemname, this, this.player.getWorldServer());
		if (this.player.activeContainer instanceof ContainerAnvil) {
			ContainerAnvil containeranvil = (ContainerAnvil) this.player.activeContainer;
			String s = SharedConstants.a(packetplayinitemname.b());

			if (s.length() <= 35) {
				containeranvil.a(s);
			}
		}

	}

	@Override
	public void a(PacketPlayInBeacon packetplayinbeacon) {
		PlayerConnectionUtils.ensureMainThread(packetplayinbeacon, this, this.player.getWorldServer());
		if (this.player.activeContainer instanceof ContainerBeacon) {
			((ContainerBeacon) this.player.activeContainer).c(packetplayinbeacon.b(), packetplayinbeacon.c());
		}

	}

	@Override
	public void a(PacketPlayInStruct packetplayinstruct) {
		PlayerConnectionUtils.ensureMainThread(packetplayinstruct, this, this.player.getWorldServer());
		if (this.player.isCreativeAndOp()) {
			BlockPosition blockposition = packetplayinstruct.b();
			IBlockData iblockdata = this.player.world.getType(blockposition);
			TileEntity tileentity = this.player.world.getTileEntity(blockposition);

			if (tileentity instanceof TileEntityStructure) {
				TileEntityStructure tileentitystructure = (TileEntityStructure) tileentity;

				tileentitystructure.setUsageMode(packetplayinstruct.d());
				tileentitystructure.setStructureName(packetplayinstruct.e());
				tileentitystructure.b(packetplayinstruct.f());
				tileentitystructure.c(packetplayinstruct.g());
				tileentitystructure.b(packetplayinstruct.h());
				tileentitystructure.b(packetplayinstruct.i());
				tileentitystructure.b(packetplayinstruct.j());
				tileentitystructure.a(packetplayinstruct.k());
				tileentitystructure.e(packetplayinstruct.l());
				tileentitystructure.f(packetplayinstruct.m());
				tileentitystructure.a(packetplayinstruct.n());
				tileentitystructure.a(packetplayinstruct.o());
				if (tileentitystructure.g()) {
					String s = tileentitystructure.getStructureName();

					if (packetplayinstruct.c() == TileEntityStructure.UpdateType.SAVE_AREA) {
						if (tileentitystructure.D()) {
							this.player.a((IChatBaseComponent) (new ChatMessage("structure_block.save_success",
									new Object[] { s })), false);
						} else {
							this.player.a((IChatBaseComponent) (new ChatMessage("structure_block.save_failure",
									new Object[] { s })), false);
						}
					} else if (packetplayinstruct.c() == TileEntityStructure.UpdateType.LOAD_AREA) {
						if (!tileentitystructure.G()) {
							this.player.a((IChatBaseComponent) (new ChatMessage("structure_block.load_not_found",
									new Object[] { s })), false);
						} else if (tileentitystructure.E()) {
							this.player.a((IChatBaseComponent) (new ChatMessage("structure_block.load_success",
									new Object[] { s })), false);
						} else {
							this.player.a((IChatBaseComponent) (new ChatMessage("structure_block.load_prepare",
									new Object[] { s })), false);
						}
					} else if (packetplayinstruct.c() == TileEntityStructure.UpdateType.SCAN_AREA) {
						if (tileentitystructure.C()) {
							this.player.a((IChatBaseComponent) (new ChatMessage("structure_block.size_success",
									new Object[] { s })), false);
						} else {
							this.player.a((IChatBaseComponent) (new ChatMessage("structure_block.size_failure",
									new Object[0])), false);
						}
					}
				} else {
					this.player.a((IChatBaseComponent) (new ChatMessage("structure_block.invalid_structure_name",
							new Object[] { packetplayinstruct.e() })), false);
				}

				tileentitystructure.update();
				this.player.world.notify(blockposition, iblockdata, iblockdata, 3);
			}

		}
	}

	@Override
	public void a(PacketPlayInSetJigsaw packetplayinsetjigsaw) {
		PlayerConnectionUtils.ensureMainThread(packetplayinsetjigsaw, this, this.player.getWorldServer());
		if (this.player.isCreativeAndOp()) {
			BlockPosition blockposition = packetplayinsetjigsaw.b();
			IBlockData iblockdata = this.player.world.getType(blockposition);
			TileEntity tileentity = this.player.world.getTileEntity(blockposition);

			if (tileentity instanceof TileEntityJigsaw) {
				TileEntityJigsaw tileentityjigsaw = (TileEntityJigsaw) tileentity;

				tileentityjigsaw.a(packetplayinsetjigsaw.d());
				tileentityjigsaw.b(packetplayinsetjigsaw.c());
				tileentityjigsaw.a(packetplayinsetjigsaw.e());
				tileentityjigsaw.update();
				this.player.world.notify(blockposition, iblockdata, iblockdata, 3);
			}

		}
	}

	@Override
	public void a(PacketPlayInTrSel packetplayintrsel) {
		PlayerConnectionUtils.ensureMainThread(packetplayintrsel, this, this.player.getWorldServer());
		int i = packetplayintrsel.b();
		Container container = this.player.activeContainer;

		if (container instanceof ContainerMerchant) {
			ContainerMerchant containermerchant = (ContainerMerchant) container;
			CraftEventFactory.callTradeSelectEvent(this.player, i, containermerchant); // CraftBukkit

			containermerchant.d(i);
			containermerchant.g(i);
		}

	}

	@Override
	public void a(PacketPlayInBEdit packetplayinbedit) {
		// Paper start
		ItemStack testStack = packetplayinbedit.b();
		if (!server.isPrimaryThread() && !testStack.isEmpty() && testStack.getTag() != null) {
			NBTTagList pageList = testStack.getTag().getList("pages", 8);
			long byteTotal = 0;
			int maxBookPageSize = com.destroystokyo.paper.PaperConfig.maxBookPageSize;
			double multiplier = Math.max(0.3D,
					Math.min(1D, com.destroystokyo.paper.PaperConfig.maxBookTotalSizeMultiplier));
			long byteAllowed = maxBookPageSize;
			for (int i = 0; i < pageList.size(); ++i) {
				String testString = pageList.getString(i);
				int byteLength = testString.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
				byteTotal += byteLength;
				int length = testString.length();
				int multibytes = 0;
				if (byteLength != length) {
					for (char c : testString.toCharArray()) {
						if (c > 127) {
							multibytes++;
						}
					}
				}
				byteAllowed += (maxBookPageSize * Math.min(1, Math.max(0.1D, (double) length / 255D))) * multiplier;

				if (multibytes > 1) {
					// penalize MB
					byteAllowed -= multibytes;
				}
			}

			if (byteTotal > byteAllowed) {
				PlayerConnection.LOGGER.warn(this.player.getName() + " tried to send too large of a book. Book Size: "
						+ byteTotal + " - Allowed:  " + byteAllowed + " - Pages: " + pageList.size());
				minecraftServer.scheduleOnMain(() -> this.disconnect("Book too large!"));
				return;
			}
		}
		// Paper end
		PlayerConnectionUtils.ensureMainThread(packetplayinbedit, this, this.player.getWorldServer());
		// CraftBukkit start
		if (this.lastBookTick + 20 > MinecraftServer.currentTick) {
			this.disconnect("Book edited too quickly!");
			return;
		}
		this.lastBookTick = MinecraftServer.currentTick;
		EnumItemSlot enumitemslot = packetplayinbedit.d() == EnumHand.MAIN_HAND ? EnumItemSlot.MAINHAND
				: EnumItemSlot.OFFHAND;
		// CraftBukkit end
		ItemStack itemstack = packetplayinbedit.b();

		if (!itemstack.isEmpty()) {
			if (ItemBookAndQuill.b(itemstack.getTag())) {
				ItemStack itemstack1 = this.player.b(packetplayinbedit.d());

				if (itemstack.getItem() == Items.WRITABLE_BOOK && itemstack1.getItem() == Items.WRITABLE_BOOK) {
					if (packetplayinbedit.c()) {
						ItemStack itemstack2 = new ItemStack(Items.WRITTEN_BOOK);
						NBTTagCompound nbttagcompound = itemstack1.getTag();

						if (nbttagcompound != null) {
							itemstack2.setTag(nbttagcompound.clone());
						}

						itemstack2.a("author", (NBTBase) NBTTagString.a(this.player.getDisplayName().getString()));
						itemstack2.a("title", (NBTBase) NBTTagString.a(itemstack.getTag().getString("title")));
						NBTTagList nbttaglist = itemstack.getTag().getList("pages", 8);

						for (int i = 0; i < nbttaglist.size(); ++i) {
							String s = nbttaglist.getString(i);
							ChatComponentText chatcomponenttext = new ChatComponentText(s);

							s = IChatBaseComponent.ChatSerializer.a((IChatBaseComponent) chatcomponenttext);
							nbttaglist.set(i, (NBTBase) NBTTagString.a(s));
						}

						itemstack2.a("pages", (NBTBase) nbttaglist);
						this.player.a(packetplayinbedit.d(),
								CraftEventFactory.handleEditBookEvent(player, enumitemslot, itemstack1, itemstack2)); // CraftBukkit
					} else {
						// Paper start - dont mutate players current item, set it from the event
						ItemStack newBook = itemstack1.cloneItemStack();
						newBook.getOrCreateTagAndSet("pages", (NBTBase) itemstack.getTag().getList("pages", 8));
						this.player.setSlot(enumitemslot,
								CraftEventFactory.handleEditBookEvent(player, enumitemslot, itemstack1, newBook));
						// Paper end
					}
				}

			}
		}
	}

	@Override
	public void a(PacketPlayInEntityNBTQuery packetplayinentitynbtquery) {
		PlayerConnectionUtils.ensureMainThread(packetplayinentitynbtquery, this, this.player.getWorldServer());
		if (this.player.k(2)) {
			Entity entity = this.player.getWorldServer().getEntity(packetplayinentitynbtquery.c());

			if (entity != null) {
				NBTTagCompound nbttagcompound = entity.save(new NBTTagCompound());

				this.player.playerConnection
						.sendPacket(new PacketPlayOutNBTQuery(packetplayinentitynbtquery.b(), nbttagcompound));
			}

		}
	}

	@Override
	public void a(PacketPlayInTileNBTQuery packetplayintilenbtquery) {
		PlayerConnectionUtils.ensureMainThread(packetplayintilenbtquery, this, this.player.getWorldServer());
		if (this.player.k(2)) {
			TileEntity tileentity = this.player.getWorldServer().getTileEntity(packetplayintilenbtquery.c());
			NBTTagCompound nbttagcompound = tileentity != null ? tileentity.save(new NBTTagCompound()) : null;

			this.player.playerConnection
					.sendPacket(new PacketPlayOutNBTQuery(packetplayintilenbtquery.b(), nbttagcompound));
		}
	}

	@Override
	public void a(PacketPlayInFlying packetplayinflying) {
		PlayerConnectionUtils.ensureMainThread(packetplayinflying, this, this.player.getWorldServer());
		if (b(packetplayinflying)) {
			this.disconnect(new ChatMessage("multiplayer.disconnect.invalid_player_movement", new Object[0]));
		} else {
			WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

			if (!this.player.viewingCredits && !this.player.isFrozen()) { // CraftBukkit
				if (this.e == 0) {
					this.syncPosition();
				}

				if (this.teleportPos != null) {
					if (this.e - this.A > 20) {
						this.A = this.e;
						this.a(this.teleportPos.x, this.teleportPos.y, this.teleportPos.z, this.player.yaw,
								this.player.pitch);
					}
					this.allowedPlayerTicks = 20; // CraftBukkit
				} else {
					this.A = this.e;
					if (this.player.isPassenger()) {
						this.player.setLocation(this.player.locX(), this.player.locY(), this.player.locZ(),
								packetplayinflying.a(this.player.yaw), packetplayinflying.b(this.player.pitch));
						this.player.getWorldServer().getChunkProvider().movePlayer(this.player);
						this.allowedPlayerTicks = 20; // CraftBukkit
					} else {
						// CraftBukkit - Make sure the move is valid but then reset it for plugins to
						// modify
						double prevX = player.locX();
						double prevY = player.locY();
						double prevZ = player.locZ();
						float prevYaw = player.yaw;
						float prevPitch = player.pitch;
						// CraftBukkit end
						double d0 = this.player.locX();
						double d1 = this.player.locY();
						double d2 = this.player.locZ();
						double d3 = this.player.locY();
						double d4 = packetplayinflying.a(this.player.locX());
						double toX = d4; // Paper - OBFHELPER
						double d5 = packetplayinflying.b(this.player.locY());
						double d6 = packetplayinflying.c(this.player.locZ());
						double toZ = d6; // Paper - OBFHELPER
						float f = packetplayinflying.a(this.player.yaw);
						float f1 = packetplayinflying.b(this.player.pitch);
						double d7 = d4 - this.l;
						double d8 = d5 - this.m;
						double d9 = d6 - this.n;
						double d10 = this.player.getMot().g();
						double d11 = d7 * d7 + d8 * d8 + d9 * d9;

						if (this.player.isSleeping()) {
							if (d11 > 1.0D) {
								this.a(this.player.locX(), this.player.locY(), this.player.locZ(),
										packetplayinflying.a(this.player.yaw), packetplayinflying.b(this.player.pitch));
							}

						} else {
							++this.receivedMovePackets;
							int i = this.receivedMovePackets - this.processedMovePackets;

							// CraftBukkit start - handle custom speeds and skipped ticks
							this.allowedPlayerTicks += (System.currentTimeMillis() / 50) - this.lastTick;
							this.allowedPlayerTicks = Math.max(this.allowedPlayerTicks, 1);
							this.lastTick = (int) (System.currentTimeMillis() / 50);

							if (i > Math.max(this.allowedPlayerTicks, 5)) {
								PlayerConnection.LOGGER.debug(
										"{} is sending move packets too frequently ({} packets since last tick)",
										this.player.getDisplayName().getString(), i);
								i = 1;
							}

							if (packetplayinflying.hasLook || d11 > 0) {
								allowedPlayerTicks -= 1;
							} else {
								allowedPlayerTicks = 20;
							}
							double speed;
							if (player.abilities.isFlying) {
								speed = player.abilities.flySpeed * 20f;
							} else {
								speed = player.abilities.walkSpeed * 10f;
							}
							// Paper start - Prevent moving into unloaded chunks
							if (player.world.paperConfig.preventMovingIntoUnloadedChunks
									&& (this.player.locX() != toX || this.player.locZ() != toZ)
									&& worldserver.getChunkIfLoadedImmediately((int) Math.floor(toX) >> 4,
											(int) Math.floor(toZ) >> 4) == null) { // Paper - use getIfLoadedImmediately
								this.internalTeleport(this.player.locX(), this.player.locY(), this.player.locZ(),
										this.player.yaw, this.player.pitch, Collections.emptySet());
								return;
							}
							// Paper end

							if (!this.player.H() && (!this.player.getWorldServer().getGameRules()
									.getBoolean(GameRules.DISABLE_ELYTRA_MOVEMENT_CHECK) || !this.player.isGliding())) {
								float f2 = this.player.isGliding() ? 300.0F : 100.0F;

								if (d11 - d10 > Math.max(f2,
										Math.pow((double) (org.spigotmc.SpigotConfig.movedTooQuicklyMultiplier
												* (float) i * speed), 2))
										&& !this.isExemptPlayer()) {
									// CraftBukkit end
									PlayerConnection.LOGGER.warn("{} moved too quickly! {},{},{}",
											this.player.getDisplayName().getString(), d7, d8, d9);
									this.a(this.player.locX(), this.player.locY(), this.player.locZ(), this.player.yaw,
											this.player.pitch);
									return;
								}
							}

							boolean flag = this.a((IWorldReader) worldserver);

							d7 = d4 - this.o;
							d8 = d5 - this.p;
							d9 = d6 - this.q;
							if (d8 > 0.0D) {
								this.player.fallDistance = 0.0F;
							}

							if (this.player.onGround && !packetplayinflying.b() && d8 > 0.0D) {
								// Paper start - Add player jump event
								Player player = this.getPlayer();
								Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw,
										lastPitch); // Get the Players previous Event location.
								Location to = player.getLocation().clone(); // Start off the To location as the Players
																			// current location.

								// If the packet contains movement information then we update the To location
								// with the correct XYZ.
								if (packetplayinflying.hasPos) {
									to.setX(packetplayinflying.x);
									to.setY(packetplayinflying.y);
									to.setZ(packetplayinflying.z);
								}

								// If the packet contains look information then we update the To location with
								// the correct Yaw & Pitch.
								if (packetplayinflying.hasLook) {
									to.setYaw(packetplayinflying.yaw);
									to.setPitch(packetplayinflying.pitch);
								}

								PlayerJumpEvent event = new PlayerJumpEvent(player, from, to);

								if (event.callEvent()) {
									this.player.jump();
								} else {
									from = event.getFrom();
									this.internalTeleport(from.getX(), from.getY(), from.getZ(), from.getYaw(),
											from.getPitch(), Collections.emptySet());
									return;
								}
								// Paper end
							}

							this.player.move(EnumMoveType.PLAYER, new Vec3D(d7, d8, d9));
							// Paper start - prevent position desync
							if (this.teleportPos != null) {
								return; // ... thanks Mojang for letting move calls teleport across dimensions.
							}
							// Paper end - prevent position desync
							this.player.onGround = packetplayinflying.b();
							double d12 = d8;

							d7 = d4 - this.player.locX();
							d8 = d5 - this.player.locY();
							if (d8 > -0.5D || d8 < 0.5D) {
								d8 = 0.0D;
							}

							d9 = d6 - this.player.locZ();
							d11 = d7 * d7 + d8 * d8 + d9 * d9;
							boolean flag1 = false;

							if (!this.player.H() && d11 > org.spigotmc.SpigotConfig.movedWronglyThreshold
									&& !this.player.isSleeping() && !this.player.playerInteractManager.isCreative()
									&& this.player.playerInteractManager.getGameMode() != EnumGamemode.SPECTATOR) { // Spigot
								flag1 = true;
								PlayerConnection.LOGGER.warn("{} moved wrongly!",
										this.player.getDisplayName().getString());
							}

							this.player.setLocation(d4, d5, d6, f, f1);
							this.player.checkMovement(this.player.locX() - d0, this.player.locY() - d1,
									this.player.locZ() - d2);
							if (!this.player.noclip && !this.player.isSleeping()) {
								boolean flag2 = this.a((IWorldReader) worldserver);

								if (flag && (flag1 || !flag2)) {
									this.a(d0, d1, d2, f, f1);
									return;
								}
							}

							// CraftBukkit start - fire PlayerMoveEvent
							// Rest to old location first
							this.player.setLocation(prevX, prevY, prevZ, prevYaw, prevPitch);

							Player player = this.getPlayer();
							Location from = new Location(player.getWorld(), lastPosX, lastPosY, lastPosZ, lastYaw,
									lastPitch); // Get the Players previous Event location.
							Location to = player.getLocation().clone(); // Start off the To location as the Players
																		// current location.

							// If the packet contains movement information then we update the To location
							// with the correct XYZ.
							if (packetplayinflying.hasPos) {
								to.setX(packetplayinflying.x);
								to.setY(packetplayinflying.y);
								to.setZ(packetplayinflying.z);
							}

							// If the packet contains look information then we update the To location with
							// the correct Yaw & Pitch.
							if (packetplayinflying.hasLook) {
								to.setYaw(packetplayinflying.yaw);
								to.setPitch(packetplayinflying.pitch);
							}

							// Prevent 40 event-calls for less than a single pixel of movement >.>
							double delta = Math.pow(this.lastPosX - to.getX(), 2)
									+ Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
							float deltaAngle = Math.abs(this.lastYaw - to.getYaw())
									+ Math.abs(this.lastPitch - to.getPitch());

							if ((delta > 1f / 256 || deltaAngle > 10f) && !this.player.isFrozen()) {
								this.lastPosX = to.getX();
								this.lastPosY = to.getY();
								this.lastPosZ = to.getZ();
								this.lastYaw = to.getYaw();
								this.lastPitch = to.getPitch();

								// Skip the first time we do this
								if (from.getX() != Double.MAX_VALUE) {
									Location oldTo = to.clone();
									PlayerMoveEvent event = new PlayerMoveEvent(player, from, to);
									this.server.getPluginManager().callEvent(event);

									// If the event is cancelled we move the player back to their old location.
									if (event.isCancelled()) {
										teleport(from);
										return;
									}

									// If a Plugin has changed the To destination then we teleport the Player
									// there to avoid any 'Moved wrongly' or 'Moved too quickly' errors.
									// We only do this if the Event was not cancelled.
									if (!oldTo.equals(event.getTo()) && !event.isCancelled()) {
										this.player.getBukkitEntity().teleport(event.getTo(),
												PlayerTeleportEvent.TeleportCause.PLUGIN);
										return;
									}

									// Check to see if the Players Location has some how changed during the call of
									// the event.
									// This can happen due to a plugin teleporting the player instead of using
									// .setTo()
									if (!from.equals(this.getPlayer().getLocation()) && this.justTeleported) {
										this.justTeleported = false;
										return;
									}
								}
							}
							this.player.setLocation(d4, d5, d6, f, f1); // Copied from above

							// MC-135989, SPIGOT-5564: isRiptiding
							this.B = d12 >= -0.03125D
									&& this.player.playerInteractManager.getGameMode() != EnumGamemode.SPECTATOR
									&& !this.minecraftServer.getAllowFlight() && !this.player.abilities.canFly
									&& !this.player.hasEffect(MobEffects.LEVITATION) && !this.player.isGliding()
									&& !worldserver.b(this.player.getBoundingBox().g(0.0625D).b(0.0D, -0.55D, 0.0D))
									&& !this.player.isRiptiding();
							// CraftBukkit end
							this.player.onGround = packetplayinflying.b();
							this.player.getWorldServer().getChunkProvider().movePlayer(this.player);
							this.player.a(this.player.locY() - d3, packetplayinflying.b());
							this.o = this.player.locX();
							this.p = this.player.locY();
							this.q = this.player.locZ();
						}
					}
				}
			}
		}
	}

	private boolean a(IWorldReader iworldreader) {
		return iworldreader.getCubes(this.player, this.player.getBoundingBox().shrink(9.999999747378752E-6D));
	}

	public void a(double d0, double d1, double d2, float f, float f1) {
		this.a(d0, d1, d2, f, f1, Collections.<PacketPlayOutPosition.EnumPlayerTeleportFlags>emptySet());
	}

	// CraftBukkit start - Delegate to teleport(Location)
	public final void teleport(double d0, double d1, double d2, float f, float f1,
			PlayerTeleportEvent.TeleportCause cause) {
		this.a(d0, d1, d2, f, f1, cause);
	} // Paper - OBFHELPER

	public void a(double d0, double d1, double d2, float f, float f1, PlayerTeleportEvent.TeleportCause cause) {
		this.a(d0, d1, d2, f, f1, Collections.<PacketPlayOutPosition.EnumPlayerTeleportFlags>emptySet(), cause);
	}

	public void a(double d0, double d1, double d2, float f, float f1,
			Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> set) {
		this.a(d0, d1, d2, f, f1, set, PlayerTeleportEvent.TeleportCause.UNKNOWN);
	}

	public void a(double d0, double d1, double d2, float f, float f1,
			Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> set, PlayerTeleportEvent.TeleportCause cause) {
		Player player = this.getPlayer();
		Location from = player.getLocation();

		double x = d0;
		double y = d1;
		double z = d2;
		float yaw = f;
		float pitch = f1;

		Location to = new Location(this.getPlayer().getWorld(), x, y, z, yaw, pitch);
		// SPIGOT-5171: Triggered on join
		if (from.equals(to)) {
			this.internalTeleport(d0, d1, d2, f, f1, set);
			return;
		}

		PlayerTeleportEvent event = new PlayerTeleportEvent(player, from.clone(), to.clone(), cause);
		this.server.getPluginManager().callEvent(event);

		if (event.isCancelled() || !to.equals(event.getTo())) {
			set.clear(); // Can't relative teleport
			to = event.isCancelled() ? event.getFrom() : event.getTo();
			d0 = to.getX();
			d1 = to.getY();
			d2 = to.getZ();
			f = to.getYaw();
			f1 = to.getPitch();
		}

		this.internalTeleport(d0, d1, d2, f, f1, set);
	}

	public void teleport(Location dest) {
		internalTeleport(dest.getX(), dest.getY(), dest.getZ(), dest.getYaw(), dest.getPitch(),
				Collections.<PacketPlayOutPosition.EnumPlayerTeleportFlags>emptySet());
	}

	private void internalTeleport(double d0, double d1, double d2, float f, float f1,
			Set<PacketPlayOutPosition.EnumPlayerTeleportFlags> set) {
		if (player.dead) {
			LOGGER.info("Attempt to teleport dead player {} restricted", player.getName());
			return;
		}
		// CraftBukkit start
		if (Float.isNaN(f)) {
			f = 0;
		}
		if (Float.isNaN(f1)) {
			f1 = 0;
		}

		this.justTeleported = true;
		// CraftBukkit end
		double d3 = set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.X) ? this.player.locX() : 0.0D;
		double d4 = set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Y) ? this.player.locY() : 0.0D;
		double d5 = set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Z) ? this.player.locZ() : 0.0D;
		float f2 = set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.Y_ROT) ? this.player.yaw : 0.0F;
		float f3 = set.contains(PacketPlayOutPosition.EnumPlayerTeleportFlags.X_ROT) ? this.player.pitch : 0.0F;

		this.teleportPos = new Vec3D(d0, d1, d2);
		if (++this.teleportAwait == Integer.MAX_VALUE) {
			this.teleportAwait = 0;
		}

		// CraftBukkit start - update last location
		this.lastPosX = this.teleportPos.x;
		this.lastPosY = this.teleportPos.y;
		this.lastPosZ = this.teleportPos.z;
		this.lastYaw = f;
		this.lastPitch = f1;
		// CraftBukkit end

		this.A = this.e;
		this.player.setLocation(d0, d1, d2, f, f1);
		this.syncPosition(); // Paper
		this.player.forceCheckHighPriority(); // Paper
		this.player.playerConnection.sendPacket(
				new PacketPlayOutPosition(d0 - d3, d1 - d4, d2 - d5, f - f2, f1 - f3, set, this.teleportAwait));
	}

	@Override
	public void a(PacketPlayInBlockDig packetplayinblockdig) {
		PlayerConnectionUtils.ensureMainThread(packetplayinblockdig, this, this.player.getWorldServer());
		if (this.player.isFrozen())
			return; // CraftBukkit
		BlockPosition blockposition = packetplayinblockdig.b();

		this.player.resetIdleTimer();
		PacketPlayInBlockDig.EnumPlayerDigType packetplayinblockdig_enumplayerdigtype = packetplayinblockdig.d();

		switch (packetplayinblockdig_enumplayerdigtype) {
		case SWAP_HELD_ITEMS:
			if (!this.player.isSpectator()) {
				ItemStack itemstack = this.player.b(EnumHand.OFF_HAND);

				// CraftBukkit start - inspiration taken from DispenserRegistry (See
				// SpigotCraft#394)
				CraftItemStack mainHand = CraftItemStack.asCraftMirror(itemstack);
				CraftItemStack offHand = CraftItemStack.asCraftMirror(this.player.b(EnumHand.MAIN_HAND));
				PlayerSwapHandItemsEvent swapItemsEvent = new PlayerSwapHandItemsEvent(getPlayer(), mainHand.clone(),
						offHand.clone());
				this.server.getPluginManager().callEvent(swapItemsEvent);
				if (swapItemsEvent.isCancelled()) {
					return;
				}
				if (swapItemsEvent.getOffHandItem().equals(offHand)) {
					this.player.a(EnumHand.OFF_HAND, this.player.b(EnumHand.MAIN_HAND));
				} else {
					this.player.a(EnumHand.OFF_HAND, CraftItemStack.asNMSCopy(swapItemsEvent.getOffHandItem()));
				}
				if (swapItemsEvent.getMainHandItem().equals(mainHand)) {
					this.player.a(EnumHand.MAIN_HAND, itemstack);
				} else {
					this.player.a(EnumHand.MAIN_HAND, CraftItemStack.asNMSCopy(swapItemsEvent.getMainHandItem()));
				}
				// CraftBukkit end
			}

			return;
		case DROP_ITEM:
			if (!this.player.isSpectator()) {
				// limit how quickly items can be dropped
				// If the ticks aren't the same then the count starts from 0 and we update the
				// lastDropTick.
				if (this.lastDropTick != MinecraftServer.currentTick) {
					this.dropCount = 0;
					this.lastDropTick = MinecraftServer.currentTick;
				} else {
					// Else we increment the drop count and check the amount.
					this.dropCount++;
					if (this.dropCount >= 20) {
						LOGGER.warn(this.player.getName() + " dropped their items too quickly!");
						this.disconnect("You dropped your items too quickly (Hacking?)");
						return;
					}
				}
				// CraftBukkit end
				this.player.n(false);
			}

			return;
		case DROP_ALL_ITEMS:
			if (!this.player.isSpectator()) {
				this.player.n(true);
			}

			return;
		case RELEASE_USE_ITEM:
			this.player.clearActiveItem();
			return;
		case START_DESTROY_BLOCK:
		case ABORT_DESTROY_BLOCK:
		case STOP_DESTROY_BLOCK:
			// Paper start - Don't allow digging in unloaded chunks
			if (this.player.world.getChunkIfLoadedImmediately(blockposition.getX() >> 4,
					blockposition.getZ() >> 4) == null) {
				return;
			}
			// Paper end - Don't allow digging in unloaded chunks
			this.player.playerInteractManager.a(blockposition, packetplayinblockdig_enumplayerdigtype,
					packetplayinblockdig.c(), this.minecraftServer.getMaxBuildHeight());
			return;
		default:
			throw new IllegalArgumentException("Invalid player action");
		}
	}

	// Spigot start - limit place/interactions
	private int limitedPackets;
	private long lastLimitedPacket = -1;
	private static final int THRESHOLD = com.destroystokyo.paper.PaperConfig.packetInSpamThreshold; // Paper -
																									// Configurable
																									// threshold

	private boolean checkLimit(long timestamp) {
		if (lastLimitedPacket != -1 && timestamp - lastLimitedPacket < THRESHOLD && limitedPackets++ >= 8) { // Paper -
																												// Use
																												// threshold,
																												// raise
																												// packet
																												// limit
																												// to 8
			return false;
		}

		if (lastLimitedPacket == -1 || timestamp - lastLimitedPacket >= THRESHOLD) { // Paper
			lastLimitedPacket = timestamp;
			limitedPackets = 0;
			return true;
		}

		return true;
	}
	// Spigot end

	@Override
	public void a(PacketPlayInUseItem packetplayinuseitem) {
		PlayerConnectionUtils.ensureMainThread(packetplayinuseitem, this, this.player.getWorldServer());
		if (this.player.isFrozen())
			return; // CraftBukkit
		if (!checkLimit(packetplayinuseitem.timestamp))
			return; // Spigot - check limit
		WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
		EnumHand enumhand = packetplayinuseitem.b();
		ItemStack itemstack = this.player.b(enumhand);
		MovingObjectPositionBlock movingobjectpositionblock = packetplayinuseitem.c();
		BlockPosition blockposition = movingobjectpositionblock.getBlockPosition();
		EnumDirection enumdirection = movingobjectpositionblock.getDirection();
		// Paper start - move check up
		Location eyeLoc = this.getPlayer().getEyeLocation();
		double reachDistance = NumberConversions.square(eyeLoc.getX() - blockposition.getX())
				+ NumberConversions.square(eyeLoc.getY() - blockposition.getY())
				+ NumberConversions.square(eyeLoc.getZ() - blockposition.getZ());
		if (reachDistance > (this.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE
				? CREATIVE_PLACE_DISTANCE_SQUARED
				: SURVIVAL_PLACE_DISTANCE_SQUARED)) {
			return;
		}
		// Paper end - move check up
		IBlockData clickedBlock = worldserver.getType(blockposition); // Spigot

		this.player.resetIdleTimer();
		if (blockposition.getY() >= this.minecraftServer.getMaxBuildHeight() - 1
				&& ((enumdirection == EnumDirection.UP && !(clickedBlock.getBlock() instanceof BlockStepAbstract
						&& clickedBlock.get(BlockStepAbstract.a) == BlockPropertySlabType.BOTTOM))
						|| blockposition.getY() >= this.minecraftServer.getMaxBuildHeight())) { // Spigot
			IChatBaseComponent ichatbasecomponent = (new ChatMessage("build.tooHigh",
					new Object[] { this.minecraftServer.getMaxBuildHeight() })).a(EnumChatFormat.RED);

			this.player.playerConnection
					.sendPacket(new PacketPlayOutChat(ichatbasecomponent, ChatMessageType.GAME_INFO));
		} else if (this.teleportPos == null
				&& this.player.g((double) blockposition.getX() + 0.5D, (double) blockposition.getY() + 0.5D,
						(double) blockposition.getZ() + 0.5D) < 64.0D
				&& worldserver.a((EntityHuman) this.player, blockposition)) {
			// CraftBukkit start - Check if we can actually do something over this large a
			// distance
			// Paper - move up
			this.player.clearActiveItem(); // SPIGOT-4706
			// CraftBukkit end
			EnumInteractionResult enuminteractionresult = this.player.playerInteractManager.a(this.player, worldserver,
					itemstack, enumhand, movingobjectpositionblock);

			if (enuminteractionresult.b()) {
				this.player.a(enumhand, true);
			}
		}

		this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(worldserver, blockposition));
		this.player.playerConnection
				.sendPacket(new PacketPlayOutBlockChange(worldserver, blockposition.shift(enumdirection)));
	}

	@Override
	public void a(PacketPlayInBlockPlace packetplayinblockplace) {
		PlayerConnectionUtils.ensureMainThread(packetplayinblockplace, this, this.player.getWorldServer());
		if (this.player.isFrozen())
			return; // CraftBukkit
		if (!checkLimit(packetplayinblockplace.timestamp))
			return; // Spigot - check limit
		WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
		EnumHand enumhand = packetplayinblockplace.b();
		ItemStack itemstack = this.player.b(enumhand);

		this.player.resetIdleTimer();
		if (!itemstack.isEmpty()) {
			// CraftBukkit start
			// Raytrace to look for 'rogue armswings'
			float f1 = this.player.pitch;
			float f2 = this.player.yaw;
			double d0 = this.player.locX();
			double d1 = this.player.locY() + (double) this.player.getHeadHeight();
			double d2 = this.player.locZ();
			Vec3D vec3d = new Vec3D(d0, d1, d2);

			float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
			float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
			float f5 = -MathHelper.cos(-f1 * 0.017453292F);
			float f6 = MathHelper.sin(-f1 * 0.017453292F);
			float f7 = f4 * f5;
			float f8 = f3 * f5;
			double d3 = player.playerInteractManager.getGameMode() == EnumGamemode.CREATIVE ? 5.0D : 4.5D;
			Vec3D vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
			MovingObjectPosition movingobjectposition = this.player.world.rayTrace(new RayTrace(vec3d, vec3d1,
					RayTrace.BlockCollisionOption.OUTLINE, RayTrace.FluidCollisionOption.NONE, player));

			boolean cancelled;
			if (movingobjectposition == null
					|| movingobjectposition.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
				org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory
						.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_AIR, itemstack, enumhand);
				cancelled = event.useItemInHand() == Event.Result.DENY;
			} else {
				if (player.playerInteractManager.firedInteract) {
					player.playerInteractManager.firedInteract = false;
					cancelled = player.playerInteractManager.interactResult;
				} else {
					MovingObjectPositionBlock movingobjectpositionblock = (MovingObjectPositionBlock) movingobjectposition;
					org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent(
							player, Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPosition(),
							movingobjectpositionblock.getDirection(), itemstack, true, enumhand);
					cancelled = event.useItemInHand() == Event.Result.DENY;
				}
			}

			if (cancelled) {
				this.player.getBukkitEntity().updateInventory(); // SPIGOT-2524
			} else {
				// Paper start
				itemstack = this.player.getItemInHand(enumhand);
				if (itemstack.isEmpty())
					return;
				// Paper end
				this.player.playerInteractManager.a(this.player, worldserver, itemstack, enumhand);
			}
			// CraftBukkit end
		}
	}

	@Override
	public void a(PacketPlayInSpectate packetplayinspectate) {
		PlayerConnectionUtils.ensureMainThread(packetplayinspectate, this, this.player.getWorldServer());
		if (this.player.isSpectator()) {
			Iterator iterator = this.minecraftServer.getWorlds().iterator();

			while (iterator.hasNext()) {
				WorldServer worldserver = (WorldServer) iterator.next();
				Entity entity = packetplayinspectate.a(worldserver);

				if (entity != null) {
					this.player.a(worldserver, entity.locX(), entity.locY(), entity.locZ(), entity.yaw, entity.pitch,
							org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.SPECTATE); // CraftBukkit
					return;
				}
			}
		}

	}

	@Override
	// CraftBukkit start
	public void a(PacketPlayInResourcePackStatus packetplayinresourcepackstatus) {
		PlayerConnectionUtils.ensureMainThread(packetplayinresourcepackstatus, this, this.player.getWorldServer());
		// Paper start
		PlayerResourcePackStatusEvent.Status packStatus = PlayerResourcePackStatusEvent.Status
				.values()[packetplayinresourcepackstatus.status.ordinal()];
		player.getBukkitEntity().setResourcePackStatus(packStatus);
		this.server.getPluginManager().callEvent(new PlayerResourcePackStatusEvent(getPlayer(), packStatus));
		// Paper end
	}
	// CraftBukkit end

	@Override
	public void a(PacketPlayInBoatMove packetplayinboatmove) {
		PlayerConnectionUtils.ensureMainThread(packetplayinboatmove, this, this.player.getWorldServer());
		Entity entity = this.player.getVehicle();

		if (entity instanceof EntityBoat) {
			((EntityBoat) entity).a(packetplayinboatmove.b(), packetplayinboatmove.c());
		}

	}

	@Override
	public void a(IChatBaseComponent ichatbasecomponent) {
		// CraftBukkit start - Rarely it would send a disconnect line twice
		if (this.processedDisconnect) {
			return;
		} else {
			this.processedDisconnect = true;
		}
		// CraftBukkit end
		PlayerConnection.LOGGER.info("{} lost connection: {}", this.player.getDisplayName().getString(),
				ichatbasecomponent.getString());
		// CraftBukkit start - Replace vanilla quit message handling with our own.
		/*
		 * this.minecraftServer.invalidatePingSample();
		 * this.minecraftServer.getPlayerList().sendMessage((new
		 * ChatMessage("multiplayer.player.left", new
		 * Object[]{this.player.getScoreboardDisplayName()})).a(EnumChatFormat.YELLOW));
		 */

		this.player.n();
		String quitMessage = this.minecraftServer.getPlayerList().disconnect(this.player);
		if ((quitMessage != null) && (quitMessage.length() > 0)) {
			this.minecraftServer.getPlayerList().sendMessage(CraftChatMessage.fromString(quitMessage));
		}
		// CraftBukkit end
		if (this.isExemptPlayer()) {
			PlayerConnection.LOGGER.info("Stopping singleplayer server as player logged out");
			this.minecraftServer.safeShutdown(false);
		}

	}

	public void sendPacket(Packet<?> packet) {
		this.a(packet, (GenericFutureListener) null);
	}

	public void a(Packet<?> packet,
			@Nullable GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) {
		if (packet instanceof PacketPlayOutChat) {
			PacketPlayOutChat packetplayoutchat = (PacketPlayOutChat) packet;
			EnumChatVisibility enumchatvisibility = this.player.getChatFlags();

			if (enumchatvisibility == EnumChatVisibility.HIDDEN && packetplayoutchat.d() != ChatMessageType.GAME_INFO) {
				return;
			}

			if (enumchatvisibility == EnumChatVisibility.SYSTEM && !packetplayoutchat.c()) {
				return;
			}
		}

		// CraftBukkit start
		if (packet == null || this.processedDisconnect) { // Spigot
			return;
		} else if (packet instanceof PacketPlayOutSpawnPosition) {
			PacketPlayOutSpawnPosition packet6 = (PacketPlayOutSpawnPosition) packet;
			this.player.compassTarget = new Location(this.getPlayer().getWorld(), packet6.position.getX(),
					packet6.position.getY(), packet6.position.getZ());
		}
		// CraftBukkit end

		try {
			this.networkManager.sendPacket(packet, genericfuturelistener);
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.a(throwable, "Sending packet");
			CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Packet being sent");

			crashreportsystemdetails.a("Packet class", () -> {
				return packet.getClass().getCanonicalName();
			});
			throw new ReportedException(crashreport);
		}
	}

	@Override
	public void a(PacketPlayInHeldItemSlot packetplayinhelditemslot) {
		PlayerConnectionUtils.ensureMainThread(packetplayinhelditemslot, this, this.player.getWorldServer());
		if (this.player.isFrozen())
			return; // CraftBukkit
		if (packetplayinhelditemslot.b() >= 0 && packetplayinhelditemslot.b() < PlayerInventory.getHotbarSize()) {
			PlayerItemHeldEvent event = new PlayerItemHeldEvent(this.getPlayer(), this.player.inventory.itemInHandIndex,
					packetplayinhelditemslot.b());
			this.server.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				this.sendPacket(new PacketPlayOutHeldItemSlot(this.player.inventory.itemInHandIndex));
				this.player.resetIdleTimer();
				return;
			}
			// CraftBukkit end
			this.player.inventory.itemInHandIndex = packetplayinhelditemslot.b();
			this.player.resetIdleTimer();
		} else {
			PlayerConnection.LOGGER.warn("{} tried to set an invalid carried item",
					this.player.getDisplayName().getString());
			this.disconnect("Invalid hotbar selection (Hacking?)"); // CraftBukkit
		}
	}

	@Override
	public void a(PacketPlayInChat packetplayinchat) {
		// CraftBukkit start - async chat
		// SPIGOT-3638
		if (this.minecraftServer.isStopped()) {
			return;
		}

		boolean isSync = packetplayinchat.b().startsWith("/");
		if (packetplayinchat.b().startsWith("/")) {
			PlayerConnectionUtils.ensureMainThread(packetplayinchat, this, this.player.getWorldServer());
		}
		// CraftBukkit end
		if (this.player.dead || this.player.getChatFlags() == EnumChatVisibility.HIDDEN) { // CraftBukkit - dead men
																							// tell no tales
			this.sendPacket(
					new PacketPlayOutChat((new ChatMessage("chat.cannotSend", new Object[0])).a(EnumChatFormat.RED)));
		} else {
			this.player.resetIdleTimer();
			String s = packetplayinchat.b();

			s = StringUtils.normalizeSpace(s);

			for (int i = 0; i < s.length(); ++i) {
				if (!SharedConstants.isAllowedChatCharacter(s.charAt(i))) {
					// CraftBukkit start - threadsafety
					if (!isSync) {
						Waitable waitable = new Waitable() {
							@Override
							protected Object evaluate() {
								PlayerConnection.this.disconnect(
										new ChatMessage("multiplayer.disconnect.illegal_characters", new Object[0]));
								return null;
							}
						};

						this.minecraftServer.processQueue.add(waitable);

						try {
							waitable.get();
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						} catch (ExecutionException e) {
							throw new RuntimeException(e);
						}
					} else {
						this.disconnect(new ChatMessage("multiplayer.disconnect.illegal_characters", new Object[0]));
					}
					// CraftBukkit end
					return;
				}
			}

			// CraftBukkit start
			if (isSync) {
				try {
					this.minecraftServer.server.playerCommandState = true;
					this.handleCommand(s);
				} finally {
					this.minecraftServer.server.playerCommandState = false;
				}
			} else if (s.isEmpty()) {
				LOGGER.warn(this.player.getName() + " tried to send an empty message");
			} else if (getPlayer().isConversing()) {
				final String conversationInput = s;
				this.minecraftServer.processQueue.add(new Runnable() {
					@Override
					public void run() {
						getPlayer().acceptConversationInput(conversationInput);
					}
				});
			} else if (this.player.getChatFlags() == EnumChatVisibility.SYSTEM) { // Re-add "Command Only" flag check
				ChatMessage chatmessage = new ChatMessage("chat.cannotSend", new Object[0]);

				chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
				this.sendPacket(new PacketPlayOutChat(chatmessage));
			} else if (true) {
				this.chat(s, true);
				// CraftBukkit end - the below is for reference. :)
			} else {
				ChatMessage chatmessage = new ChatMessage("chat.type.text",
						new Object[] { this.player.getScoreboardDisplayName(), s });

				this.minecraftServer.getPlayerList().sendMessage(chatmessage, false);
			}

			// Spigot start - spam exclusions
			boolean counted = true;
			for (String exclude : org.spigotmc.SpigotConfig.spamExclusions) {
				if (exclude != null && s.startsWith(exclude)) {
					counted = false;
					break;
				}
			}
			// Spigot end
			// CraftBukkit start - replaced with thread safe throttle
			// this.chatThrottle += 20;
			if (counted && chatSpamField.addAndGet(this, 20) > 200
					&& !this.minecraftServer.getPlayerList().isOp(this.player.getProfile())) { // Spigot
				if (!isSync) {
					Waitable waitable = new Waitable() {
						@Override
						protected Object evaluate() {
							PlayerConnection.this.disconnect(new ChatMessage("disconnect.spam", new Object[0]));
							return null;
						}
					};

					this.minecraftServer.processQueue.add(waitable);

					try {
						waitable.get();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} catch (ExecutionException e) {
						throw new RuntimeException(e);
					}
				} else {
					this.disconnect(new ChatMessage("disconnect.spam", new Object[0]));
				}
				// CraftBukkit end
			}

		}
	}

	// CraftBukkit start - add method
	public void chat(String s, boolean async) {
		if (s.isEmpty() || this.player.getChatFlags() == EnumChatVisibility.HIDDEN) {
			return;
		}

		if (!async && s.startsWith("/")) {
			// Paper Start
			if (!org.spigotmc.AsyncCatcher.shuttingDown && !org.bukkit.Bukkit.isPrimaryThread()) {
				final String fCommandLine = s;
				MinecraftServer.LOGGER.log(org.apache.logging.log4j.Level.ERROR,
						"Command Dispatched Async: " + fCommandLine);
				MinecraftServer.LOGGER.log(org.apache.logging.log4j.Level.ERROR,
						"Please notify author of plugin causing this execution to fix this bug! see: http://bit.ly/1oSiM6C",
						new Throwable());
				Waitable wait = new Waitable() {
					@Override
					protected Object evaluate() {
						chat(fCommandLine, false);
						return null;
					}
				};
				minecraftServer.processQueue.add(wait);
				try {
					wait.get();
					return;
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass
														// it on!
				} catch (Exception e) {
					throw new RuntimeException("Exception processing chat command", e.getCause());
				}
			}
			// Paper End
			this.handleCommand(s);
		} else if (this.player.getChatFlags() == EnumChatVisibility.SYSTEM) {
			// Do nothing, this is coming from a plugin
		} else {
			Player player = this.getPlayer();
			AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(async, player, s, new LazyPlayerSet(minecraftServer));
			this.server.getPluginManager().callEvent(event);

			if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
				// Evil plugins still listening to deprecated event
				final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(),
						event.getRecipients());
				queueEvent.setCancelled(event.isCancelled());
				Waitable waitable = new Waitable() {
					@Override
					protected Object evaluate() {
						org.bukkit.Bukkit.getPluginManager().callEvent(queueEvent);

						if (queueEvent.isCancelled()) {
							return null;
						}

						String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(),
								queueEvent.getMessage());
						PlayerConnection.this.minecraftServer.console.sendMessage(message);
						if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
							for (Object player : PlayerConnection.this.minecraftServer.getPlayerList().players) {
								((EntityPlayer) player).sendMessage(CraftChatMessage.fromString(message));
							}
						} else {
							for (Player player : queueEvent.getRecipients()) {
								player.sendMessage(message);
							}
						}
						return null;
					}
				};
				if (async) {
					minecraftServer.processQueue.add(waitable);
				} else {
					waitable.run();
				}
				try {
					waitable.get();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass
														// it on!
				} catch (ExecutionException e) {
					throw new RuntimeException("Exception processing chat event", e.getCause());
				}
			} else {
				if (event.isCancelled()) {
					return;
				}

				// Paper Start - (Meh) Support for vanilla world scoreboard name coloring
				String displayName = event.getPlayer().getDisplayName();
				if (this.player.getWorld().paperConfig.useVanillaScoreboardColoring) {
					IChatBaseComponent nameFromTeam = ScoreboardTeam.a(this.player.getScoreboardTeam(),
							((CraftPlayer) player).getHandle().getDisplayName());
					// Explicitly add a RESET here, vanilla uses components for this now...
					displayName = CraftChatMessage.fromComponent(nameFromTeam) + org.bukkit.ChatColor.RESET;
				}

				s = String.format(event.getFormat(), displayName, event.getMessage());
				// Paper end
				minecraftServer.console.sendMessage(s);
				if (((LazyPlayerSet) event.getRecipients()).isLazy()) {
					for (Object recipient : minecraftServer.getPlayerList().players) {
						((EntityPlayer) recipient).sendMessage(CraftChatMessage.fromString(s));
					}
				} else {
					for (Player recipient : event.getRecipients()) {
						recipient.sendMessage(s);
					}
				}
			}
		}
	}
	// CraftBukkit end

	private void handleCommand(String s) {
		MinecraftTimings.playerCommandTimer.startTiming(); // Paper
		// CraftBukkit start - whole method
		if (org.spigotmc.SpigotConfig.logCommands) // Spigot
			this.LOGGER.info(this.player.getName() + " issued server command: " + s);

		CraftPlayer player = this.getPlayer();

		PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, s,
				new LazyPlayerSet(minecraftServer));
		this.server.getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			MinecraftTimings.playerCommandTimer.stopTiming(); // Paper
			return;
		}

		try {
			if (this.server.dispatchCommand(event.getPlayer(), event.getMessage().substring(1))) {
				return;
			}
		} catch (org.bukkit.command.CommandException ex) {
			player.sendMessage(
					org.bukkit.ChatColor.RED + "An internal error occurred while attempting to perform this command");
			java.util.logging.Logger.getLogger(PlayerConnection.class.getName()).log(java.util.logging.Level.SEVERE,
					null, ex);
			return;
		} finally {
			MinecraftTimings.playerCommandTimer.stopTiming(); // Paper
		}
		// this.minecraftServer.getCommandDispatcher().a(this.player.getCommandListener(),
		// s);
		// CraftBukkit end
	}

	@Override
	public void a(PacketPlayInArmAnimation packetplayinarmanimation) {
		PlayerConnectionUtils.ensureMainThread(packetplayinarmanimation, this, this.player.getWorldServer());
		if (this.player.isFrozen())
			return; // CraftBukkit
		this.player.resetIdleTimer();
		// CraftBukkit start - Raytrace to look for 'rogue armswings'
		float f1 = this.player.pitch;
		float f2 = this.player.yaw;
		double d0 = this.player.locX();
		double d1 = this.player.locY() + (double) this.player.getHeadHeight();
		double d2 = this.player.locZ();
		Vec3D vec3d = new Vec3D(d0, d1, d2);

		float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = player.playerInteractManager.getGameMode() == EnumGamemode.CREATIVE ? 5.0D : 4.5D;
		Vec3D vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
		MovingObjectPosition movingobjectposition = this.player.world.rayTrace(new RayTrace(vec3d, vec3d1,
				RayTrace.BlockCollisionOption.OUTLINE, RayTrace.FluidCollisionOption.NONE, player));

		if (movingobjectposition == null
				|| movingobjectposition.getType() != MovingObjectPosition.EnumMovingObjectType.BLOCK) {
			CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR,
					this.player.inventory.getItemInHand(), EnumHand.MAIN_HAND);
		}

		// Arm swing animation
		PlayerAnimationEvent event = new PlayerAnimationEvent(this.getPlayer());
		this.server.getPluginManager().callEvent(event);

		if (event.isCancelled())
			return;
		// CraftBukkit end
		this.player.a(packetplayinarmanimation.b());
	}

	@Override
	public void a(PacketPlayInEntityAction packetplayinentityaction) {
		PlayerConnectionUtils.ensureMainThread(packetplayinentityaction, this, this.player.getWorldServer());
		// CraftBukkit start
		if (this.player.dead)
			return;
		switch (packetplayinentityaction.c()) {
		case PRESS_SHIFT_KEY:
		case RELEASE_SHIFT_KEY:
			PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(this.getPlayer(),
					packetplayinentityaction.c() == PacketPlayInEntityAction.EnumPlayerAction.PRESS_SHIFT_KEY);
			this.server.getPluginManager().callEvent(event);

			if (event.isCancelled()) {
				return;
			}
			break;
		case START_SPRINTING:
		case STOP_SPRINTING:
			PlayerToggleSprintEvent e2 = new PlayerToggleSprintEvent(this.getPlayer(),
					packetplayinentityaction.c() == PacketPlayInEntityAction.EnumPlayerAction.START_SPRINTING);
			this.server.getPluginManager().callEvent(e2);

			if (e2.isCancelled()) {
				return;
			}
			break;
		}
		// CraftBukkit end
		this.player.resetIdleTimer();
		IJumpable ijumpable;

		switch (packetplayinentityaction.c()) {
		case PRESS_SHIFT_KEY:
			this.player.setSneaking(true);

			// Paper start - Hang on!
			if (this.player.world.paperConfig.parrotsHangOnBetter) {
				this.player.releaseShoulderEntities();
			}
			// Paper end

			break;
		case RELEASE_SHIFT_KEY:
			this.player.setSneaking(false);
			break;
		case START_SPRINTING:
			this.player.setSprinting(true);
			break;
		case STOP_SPRINTING:
			this.player.setSprinting(false);
			break;
		case STOP_SLEEPING:
			if (this.player.isSleeping()) {
				this.player.wakeup(false, true);
				this.teleportPos = this.player.getPositionVector();
			}
			break;
		case START_RIDING_JUMP:
			if (this.player.getVehicle() instanceof IJumpable) {
				ijumpable = (IJumpable) this.player.getVehicle();
				int i = packetplayinentityaction.d();

				if (ijumpable.G_() && i > 0) {
					ijumpable.b(i);
				}
			}
			break;
		case STOP_RIDING_JUMP:
			if (this.player.getVehicle() instanceof IJumpable) {
				ijumpable = (IJumpable) this.player.getVehicle();
				ijumpable.c();
			}
			break;
		case OPEN_INVENTORY:
			if (this.player.getVehicle() instanceof EntityHorseAbstract) {
				((EntityHorseAbstract) this.player.getVehicle()).e((EntityHuman) this.player);
			}
			break;
		case START_FALL_FLYING:
			if (!this.player.ei()) {
				this.player.stopGliding();
			}
			break;
		default:
			throw new IllegalArgumentException("Invalid client command!");
		}

	}

	@Override
	public void a(PacketPlayInUseEntity packetplayinuseentity) {
		PlayerConnectionUtils.ensureMainThread(packetplayinuseentity, this, this.player.getWorldServer());
		if (this.player.isFrozen())
			return; // CraftBukkit
		WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
		Entity entity = packetplayinuseentity.a((World) worldserver);
		// Spigot Start
		if (entity == player && !player.isSpectator()) {
			disconnect("Cannot interact with self!");
			return;
		}
		// Spigot End

		this.player.resetIdleTimer();
		if (entity != null) {
			boolean flag = this.player.hasLineOfSight(entity);
			double d0 = 36.0D;

			if (!flag) {
				d0 = 9.0D;
			}

			if (this.player.h(entity) < d0) {
				EnumHand enumhand;

				ItemStack itemInHand = this.player
						.b(packetplayinuseentity.c() == null ? EnumHand.MAIN_HAND : packetplayinuseentity.c()); // CraftBukkit

				if (packetplayinuseentity.b() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT
						|| packetplayinuseentity.b() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
					// CraftBukkit start
					boolean triggerLeashUpdate = itemInHand != null && itemInHand.getItem() == Items.LEAD
							&& entity instanceof EntityInsentient;
					Item origItem = this.player.inventory.getItemInHand() == null ? null
							: this.player.inventory.getItemInHand().getItem();
					PlayerInteractEntityEvent event;
					if (packetplayinuseentity.b() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT) {
						event = new PlayerInteractEntityEvent((Player) this.getPlayer(), entity.getBukkitEntity(),
								(packetplayinuseentity.c() == EnumHand.OFF_HAND) ? EquipmentSlot.OFF_HAND
										: EquipmentSlot.HAND);
					} else {
						Vec3D target = packetplayinuseentity.d();
						event = new PlayerInteractAtEntityEvent((Player) this.getPlayer(), entity.getBukkitEntity(),
								new org.bukkit.util.Vector(target.x, target.y, target.z),
								(packetplayinuseentity.c() == EnumHand.OFF_HAND) ? EquipmentSlot.OFF_HAND
										: EquipmentSlot.HAND);
					}
					this.server.getPluginManager().callEvent(event);

					// Fish bucket - SPIGOT-4048
					if ((entity instanceof EntityFish && origItem != null && origItem.getItem() == Items.WATER_BUCKET)
							&& (event.isCancelled() || this.player.inventory.getItemInHand() == null
									|| this.player.inventory.getItemInHand().getItem() != origItem)) {
						this.sendPacket(new PacketPlayOutSpawnEntityLiving((EntityFish) entity));
						this.player.updateInventory(this.player.activeContainer);
					}

					if (triggerLeashUpdate && (event.isCancelled() || this.player.inventory.getItemInHand() == null
							|| this.player.inventory.getItemInHand().getItem() != origItem)) {
						// Refresh the current leash state
						this.sendPacket(
								new PacketPlayOutAttachEntity(entity, ((EntityInsentient) entity).getLeashHolder()));
					}

					if (event.isCancelled() || this.player.inventory.getItemInHand() == null
							|| this.player.inventory.getItemInHand().getItem() != origItem) {
						// Refresh the current entity metadata
						// Paper start - update entity for all players
						PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(entity.getId(),
								entity.datawatcher, true);
						if (entity.tracker != null) {
							entity.tracker.broadcast(packet);
						} else {
							this.sendPacket(packet);
						}
						// Paper end
					}

					if (event.isCancelled()) {
						this.player.updateInventory(this.player.activeContainer); // Paper - Refresh player inventory
						return;
					}
					// CraftBukkit end
				}

				if (packetplayinuseentity.b() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT) {
					enumhand = packetplayinuseentity.c();
					this.player.a(entity, enumhand);
					// CraftBukkit start
					if (!itemInHand.isEmpty() && itemInHand.getCount() <= -1) {
						this.player.updateInventory(this.player.activeContainer);
					}
					// CraftBukkit end
				} else if (packetplayinuseentity.b() == PacketPlayInUseEntity.EnumEntityUseAction.INTERACT_AT) {
					enumhand = packetplayinuseentity.c();
					EnumInteractionResult enuminteractionresult = entity.a((EntityHuman) this.player,
							packetplayinuseentity.d(), enumhand);

					if (enuminteractionresult.b()) {
						this.player.a(enumhand, true);
					}
					// CraftBukkit start
					if (!itemInHand.isEmpty() && itemInHand.getCount() <= -1) {
						this.player.updateInventory(this.player.activeContainer);
					}
					// CraftBukkit end
				} else if (packetplayinuseentity.b() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK) {
					if (entity instanceof EntityItem || entity instanceof EntityExperienceOrb
							|| entity instanceof EntityArrow || (entity == this.player && !player.isSpectator())) { // CraftBukkit
						this.disconnect(
								new ChatMessage("multiplayer.disconnect.invalid_entity_attacked", new Object[0]));
						this.minecraftServer.warning("Player " + this.player.getDisplayName().getString()
								+ " tried to attack an invalid entity");
						return;
					}

					this.player.attack(entity);

					// CraftBukkit start
					if (!itemInHand.isEmpty() && itemInHand.getCount() <= -1) {
						this.player.updateInventory(this.player.activeContainer);
					}
					// CraftBukkit end
				}
			}
		}
		// Paper start - fire event
		else {
			this.server.getPluginManager()
					.callEvent(new com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent(this.getPlayer(),
							packetplayinuseentity.getEntityId(),
							packetplayinuseentity.b() == PacketPlayInUseEntity.EnumEntityUseAction.ATTACK,
							packetplayinuseentity.c() == EnumHand.MAIN_HAND ? EquipmentSlot.HAND
									: EquipmentSlot.OFF_HAND));
		}
		// Paper end

	}

	@Override
	public void a(PacketPlayInClientCommand packetplayinclientcommand) {
		PlayerConnectionUtils.ensureMainThread(packetplayinclientcommand, this, this.player.getWorldServer());
		this.player.resetIdleTimer();
		PacketPlayInClientCommand.EnumClientCommand packetplayinclientcommand_enumclientcommand = packetplayinclientcommand
				.b();

		switch (packetplayinclientcommand_enumclientcommand) {
		case PERFORM_RESPAWN:
			if (this.player.viewingCredits) {
				this.player.viewingCredits = false;
				this.player = this.minecraftServer.getPlayerList().moveToWorld(this.player, DimensionManager.OVERWORLD,
						true);
				CriterionTriggers.v.a(this.player, DimensionManager.THE_END, DimensionManager.OVERWORLD);
			} else {
				if (this.player.getHealth() > 0.0F) {
					return;
				}

				this.player = this.minecraftServer.getPlayerList().moveToWorld(this.player, DimensionManager.OVERWORLD,
						false);
				if (this.minecraftServer.isHardcore()) {
					this.player.a(EnumGamemode.SPECTATOR);
					((GameRules.GameRuleBoolean) this.player.getWorldServer().getGameRules()
							.get(GameRules.SPECTATORS_GENERATE_CHUNKS)).a(false, this.minecraftServer);
				}
			}
			break;
		case REQUEST_STATS:
			this.player.getStatisticManager().a(this.player);
		}

	}

	@Override
	public void a(PacketPlayInCloseWindow packetplayinclosewindow) {
		PlayerConnectionUtils.ensureMainThread(packetplayinclosewindow, this, this.player.getWorldServer());

		if (this.player.isFrozen())
			return; // CraftBukkit
		CraftEventFactory.handleInventoryCloseEvent(this.player,
				org.bukkit.event.inventory.InventoryCloseEvent.Reason.PLAYER); // CraftBukkit // Paper

		this.player.m();
	}

	@Override
	public void a(PacketPlayInWindowClick packetplayinwindowclick) {
		PlayerConnectionUtils.ensureMainThread(packetplayinwindowclick, this, this.player.getWorldServer());
		if (this.player.isFrozen())
			return; // CraftBukkit
		this.player.resetIdleTimer();
		if (this.player.activeContainer.windowId == packetplayinwindowclick.b()
				&& this.player.activeContainer.c(this.player) && this.player.activeContainer.canUse(this.player)) { // CraftBukkit
			boolean cancelled = this.player.isSpectator(); // CraftBukkit - see below if
			if (false/* this.player.isSpectator() */) { // CraftBukkit
				NonNullList<ItemStack> nonnulllist = NonNullList.a();

				for (int i = 0; i < this.player.activeContainer.slots.size(); ++i) {
					nonnulllist.add(((Slot) this.player.activeContainer.slots.get(i)).getItem());
				}

				this.player.a(this.player.activeContainer, nonnulllist);
			} else {
				// CraftBukkit start - Call InventoryClickEvent
				if (packetplayinwindowclick.c() < -1 && packetplayinwindowclick.c() != -999) {
					return;
				}

				InventoryView inventory = this.player.activeContainer.getBukkitView();
				SlotType type = inventory.getSlotType(packetplayinwindowclick.c());

				InventoryClickEvent event;
				ClickType click = ClickType.UNKNOWN;
				InventoryAction action = InventoryAction.UNKNOWN;

				ItemStack itemstack = ItemStack.a;

				switch (packetplayinwindowclick.g()) {
				case PICKUP:
					if (packetplayinwindowclick.d() == 0) {
						click = ClickType.LEFT;
					} else if (packetplayinwindowclick.d() == 1) {
						click = ClickType.RIGHT;
					}
					if (packetplayinwindowclick.d() == 0 || packetplayinwindowclick.d() == 1) {
						action = InventoryAction.NOTHING; // Don't want to repeat ourselves
						if (packetplayinwindowclick.c() == -999) {
							if (!player.inventory.getCarried().isEmpty()) {
								action = packetplayinwindowclick.d() == 0 ? InventoryAction.DROP_ALL_CURSOR
										: InventoryAction.DROP_ONE_CURSOR;
							}
						} else if (packetplayinwindowclick.c() < 0) {
							action = InventoryAction.NOTHING;
						} else {
							Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.c());
							if (slot != null) {
								ItemStack clickedItem = slot.getItem();
								ItemStack cursor = player.inventory.getCarried();
								if (clickedItem.isEmpty()) {
									if (!cursor.isEmpty()) {
										action = packetplayinwindowclick.d() == 0 ? InventoryAction.PLACE_ALL
												: InventoryAction.PLACE_ONE;
									}
								} else if (slot.isAllowed(player)) {
									if (cursor.isEmpty()) {
										action = packetplayinwindowclick.d() == 0 ? InventoryAction.PICKUP_ALL
												: InventoryAction.PICKUP_HALF;
									} else if (slot.isAllowed(cursor)) {
										if (clickedItem.doMaterialsMatch(cursor)
												&& ItemStack.equals(clickedItem, cursor)) {
											int toPlace = packetplayinwindowclick.d() == 0 ? cursor.getCount() : 1;
											toPlace = Math.min(toPlace,
													clickedItem.getMaxStackSize() - clickedItem.getCount());
											toPlace = Math.min(toPlace,
													slot.inventory.getMaxStackSize() - clickedItem.getCount());
											if (toPlace == 1) {
												action = InventoryAction.PLACE_ONE;
											} else if (toPlace == cursor.getCount()) {
												action = InventoryAction.PLACE_ALL;
											} else if (toPlace < 0) {
												action = toPlace != -1 ? InventoryAction.PICKUP_SOME
														: InventoryAction.PICKUP_ONE; // this happens with oversized
																						// stacks
											} else if (toPlace != 0) {
												action = InventoryAction.PLACE_SOME;
											}
										} else if (cursor.getCount() <= slot.getMaxStackSize()) {
											action = InventoryAction.SWAP_WITH_CURSOR;
										}
									} else if (cursor.getItem() == clickedItem.getItem()
											&& ItemStack.equals(cursor, clickedItem)) {
										if (clickedItem.getCount() >= 0) {
											if (clickedItem.getCount() + cursor.getCount() <= cursor
													.getMaxStackSize()) {
												// As of 1.5, this is result slots only
												action = InventoryAction.PICKUP_ALL;
											}
										}
									}
								}
							}
						}
					}
					break;
				// TODO check on updates
				case QUICK_MOVE:
					if (packetplayinwindowclick.d() == 0) {
						click = ClickType.SHIFT_LEFT;
					} else if (packetplayinwindowclick.d() == 1) {
						click = ClickType.SHIFT_RIGHT;
					}
					if (packetplayinwindowclick.d() == 0 || packetplayinwindowclick.d() == 1) {
						if (packetplayinwindowclick.c() < 0) {
							action = InventoryAction.NOTHING;
						} else {
							Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.c());
							if (slot != null && slot.isAllowed(this.player) && slot.hasItem()) {
								action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
							} else {
								action = InventoryAction.NOTHING;
							}
						}
					}
					break;
				case SWAP:
					if (packetplayinwindowclick.d() >= 0 && packetplayinwindowclick.d() < 9) {
						click = ClickType.NUMBER_KEY;
						Slot clickedSlot = this.player.activeContainer.getSlot(packetplayinwindowclick.c());
						if (clickedSlot.isAllowed(player)) {
							ItemStack hotbar = this.player.inventory.getItem(packetplayinwindowclick.d());
							boolean canCleanSwap = hotbar.isEmpty()
									|| (clickedSlot.inventory == player.inventory && clickedSlot.isAllowed(hotbar)); // the
																														// slot
																														// will
																														// accept
																														// the
																														// hotbar
																														// item
							if (clickedSlot.hasItem()) {
								if (canCleanSwap) {
									action = InventoryAction.HOTBAR_SWAP;
								} else {
									action = InventoryAction.HOTBAR_MOVE_AND_READD;
								}
							} else if (!clickedSlot.hasItem() && !hotbar.isEmpty() && clickedSlot.isAllowed(hotbar)) {
								action = InventoryAction.HOTBAR_SWAP;
							} else {
								action = InventoryAction.NOTHING;
							}
						} else {
							action = InventoryAction.NOTHING;
						}
					}
					break;
				case CLONE:
					if (packetplayinwindowclick.d() == 2) {
						click = ClickType.MIDDLE;
						if (packetplayinwindowclick.c() < 0) {
							action = InventoryAction.NOTHING;
						} else {
							Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.c());
							if (slot != null && slot.hasItem() && player.abilities.canInstantlyBuild
									&& player.inventory.getCarried().isEmpty()) {
								action = InventoryAction.CLONE_STACK;
							} else {
								action = InventoryAction.NOTHING;
							}
						}
					} else {
						click = ClickType.UNKNOWN;
						action = InventoryAction.UNKNOWN;
					}
					break;
				case THROW:
					if (packetplayinwindowclick.c() >= 0) {
						if (packetplayinwindowclick.d() == 0) {
							click = ClickType.DROP;
							Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.c());
							if (slot != null && slot.hasItem() && slot.isAllowed(player) && !slot.getItem().isEmpty()
									&& slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
								action = InventoryAction.DROP_ONE_SLOT;
							} else {
								action = InventoryAction.NOTHING;
							}
						} else if (packetplayinwindowclick.d() == 1) {
							click = ClickType.CONTROL_DROP;
							Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.c());
							if (slot != null && slot.hasItem() && slot.isAllowed(player) && !slot.getItem().isEmpty()
									&& slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
								action = InventoryAction.DROP_ALL_SLOT;
							} else {
								action = InventoryAction.NOTHING;
							}
						}
					} else {
						// Sane default (because this happens when they are holding nothing. Don't ask
						// why.)
						click = ClickType.LEFT;
						if (packetplayinwindowclick.d() == 1) {
							click = ClickType.RIGHT;
						}
						action = InventoryAction.NOTHING;
					}
					break;
				case QUICK_CRAFT:
					itemstack = this.player.activeContainer.a(packetplayinwindowclick.c(), packetplayinwindowclick.d(),
							packetplayinwindowclick.g(), this.player);
					break;
				case PICKUP_ALL:
					click = ClickType.DOUBLE_CLICK;
					action = InventoryAction.NOTHING;
					if (packetplayinwindowclick.c() >= 0 && !this.player.inventory.getCarried().isEmpty()) {
						ItemStack cursor = this.player.inventory.getCarried();
						action = InventoryAction.NOTHING;
						// Quick check for if we have any of the item
						if (inventory.getTopInventory().contains(CraftMagicNumbers.getMaterial(cursor.getItem()))
								|| inventory.getBottomInventory()
										.contains(CraftMagicNumbers.getMaterial(cursor.getItem()))) {
							action = InventoryAction.COLLECT_TO_CURSOR;
						}
					}
					break;
				default:
					break;
				}

				if (packetplayinwindowclick.g() != InventoryClickType.QUICK_CRAFT) {
					if (click == ClickType.NUMBER_KEY) {
						event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.c(), click, action,
								packetplayinwindowclick.d());
					} else {
						event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.c(), click, action);
					}

					org.bukkit.inventory.Inventory top = inventory.getTopInventory();
					if (packetplayinwindowclick.c() == 0 && top instanceof CraftingInventory) {
						org.bukkit.inventory.Recipe recipe = ((CraftingInventory) top).getRecipe();
						if (recipe != null) {
							if (click == ClickType.NUMBER_KEY) {
								event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.c(), click,
										action, packetplayinwindowclick.d());
							} else {
								event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.c(), click,
										action);
							}
						}
					}

					event.setCancelled(cancelled);
					Container oldContainer = this.player.activeContainer; // SPIGOT-1224
					server.getPluginManager().callEvent(event);
					if (this.player.activeContainer != oldContainer) {
						return;
					}

					switch (event.getResult()) {
					case ALLOW:
					case DEFAULT:
						itemstack = this.player.activeContainer.a(packetplayinwindowclick.c(),
								packetplayinwindowclick.d(), packetplayinwindowclick.g(), this.player);
						break;
					case DENY:
						/*
						 * Needs enum constructor in InventoryAction if (action.modifiesOtherSlots()) {
						 * 
						 * } else { if (action.modifiesCursor()) {
						 * this.player.playerConnection.sendPacket(new Packet103SetSlot(-1, -1,
						 * this.player.inventory.getCarried())); } if (action.modifiesClicked()) {
						 * this.player.playerConnection.sendPacket(new
						 * Packet103SetSlot(this.player.activeContainer.windowId,
						 * packet102windowclick.slot,
						 * this.player.activeContainer.getSlot(packet102windowclick.slot).getItem())); }
						 * }
						 */
						switch (action) {
						// Modified other slots
						case PICKUP_ALL:
						case MOVE_TO_OTHER_INVENTORY:
						case HOTBAR_MOVE_AND_READD:
						case HOTBAR_SWAP:
						case COLLECT_TO_CURSOR:
						case UNKNOWN:
							this.player.updateInventory(this.player.activeContainer);
							break;
						// Modified cursor and clicked
						case PICKUP_SOME:
						case PICKUP_HALF:
						case PICKUP_ONE:
						case PLACE_ALL:
						case PLACE_SOME:
						case PLACE_ONE:
						case SWAP_WITH_CURSOR:
							this.player.playerConnection
									.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.player.inventory.getCarried()));
							this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(
									this.player.activeContainer.windowId, packetplayinwindowclick.c(),
									this.player.activeContainer.getSlot(packetplayinwindowclick.c()).getItem()));
							break;
						// Modified clicked only
						case DROP_ALL_SLOT:
						case DROP_ONE_SLOT:
							this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(
									this.player.activeContainer.windowId, packetplayinwindowclick.c(),
									this.player.activeContainer.getSlot(packetplayinwindowclick.c()).getItem()));
							break;
						// Modified cursor only
						case DROP_ALL_CURSOR:
						case DROP_ONE_CURSOR:
						case CLONE_STACK:
							this.player.playerConnection
									.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.player.inventory.getCarried()));
							break;
						// Nothing
						case NOTHING:
							break;
						}
						return;
					}

					if (event instanceof CraftItemEvent) {
						// Need to update the inventory on crafting to
						// correctly support custom recipes
						player.updateInventory(player.activeContainer);
					}
				}
				// CraftBukkit end
				if (ItemStack.matches(packetplayinwindowclick.f(), itemstack)) {
					this.player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.b(),
							packetplayinwindowclick.e(), true));
					this.player.e = true;
					this.player.activeContainer.c();
					this.player.broadcastCarriedItem();
					this.player.e = false;
				} else {
					this.k.put(this.player.activeContainer.windowId, packetplayinwindowclick.e());
					this.player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.b(),
							packetplayinwindowclick.e(), false));
					this.player.activeContainer.a(this.player, false);
					NonNullList<ItemStack> nonnulllist1 = NonNullList.a();

					for (int j = 0; j < this.player.activeContainer.slots.size(); ++j) {
						ItemStack itemstack1 = ((Slot) this.player.activeContainer.slots.get(j)).getItem();

						nonnulllist1.add(itemstack1.isEmpty() ? ItemStack.a : itemstack1);
					}

					this.player.a(this.player.activeContainer, nonnulllist1);
				}
			}
		}

	}

	@Override
	public void a(PacketPlayInAutoRecipe packetplayinautorecipe) {
		PlayerConnectionUtils.ensureMainThread(packetplayinautorecipe, this, this.player.getWorldServer());
		this.player.resetIdleTimer();
		if (!this.player.isSpectator() && this.player.activeContainer.windowId == packetplayinautorecipe.b()
				&& this.player.activeContainer.c(this.player)
				&& this.player.activeContainer instanceof ContainerRecipeBook) {
			// Paper start - fire event for clicking recipes in the recipe book
			com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent event = new com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent(
					player.getBukkitEntity(),
					org.bukkit.craftbukkit.util.CraftNamespacedKey.fromMinecraft(packetplayinautorecipe.c()),
					packetplayinautorecipe.d());
			if (event.callEvent()) {
				this.minecraftServer.getCraftingManager()
						.a(org.bukkit.craftbukkit.util.CraftNamespacedKey.toMinecraft(event.getRecipe()))
						.ifPresent((irecipe) -> {
							((ContainerRecipeBook) this.player.activeContainer).a(event.isMakeAll(), irecipe,
									this.player);
						});
			}
			// Paper end
		}
	}

	@Override
	public void a(PacketPlayInEnchantItem packetplayinenchantitem) {
		PlayerConnectionUtils.ensureMainThread(packetplayinenchantitem, this, this.player.getWorldServer());
		if (this.player.isFrozen())
			return; // CraftBukkit
		this.player.resetIdleTimer();
		if (this.player.activeContainer.windowId == packetplayinenchantitem.b()
				&& this.player.activeContainer.c(this.player) && !this.player.isSpectator()) {
			this.player.activeContainer.a((EntityHuman) this.player, packetplayinenchantitem.c());
			this.player.activeContainer.c();
		}

	}

	@Override
	public void a(PacketPlayInSetCreativeSlot packetplayinsetcreativeslot) {
		PlayerConnectionUtils.ensureMainThread(packetplayinsetcreativeslot, this, this.player.getWorldServer());
		if (this.player.playerInteractManager.isCreative()) {
			boolean flag = packetplayinsetcreativeslot.b() < 0;
			ItemStack itemstack = packetplayinsetcreativeslot.getItemStack();
			NBTTagCompound nbttagcompound = itemstack.b("BlockEntityTag");

			if (!itemstack.isEmpty() && nbttagcompound != null && nbttagcompound.hasKey("x")
					&& nbttagcompound.hasKey("y") && nbttagcompound.hasKey("z")
					&& this.player.getBukkitEntity().hasPermission("minecraft.nbt.copy")) { // Spigot
				BlockPosition blockposition = new BlockPosition(nbttagcompound.getInt("x"), nbttagcompound.getInt("y"),
						nbttagcompound.getInt("z"));
				TileEntity tileentity = this.player.world.getTileEntity(blockposition);

				if (tileentity != null) {
					NBTTagCompound nbttagcompound1 = tileentity.save(new NBTTagCompound());

					nbttagcompound1.remove("x");
					nbttagcompound1.remove("y");
					nbttagcompound1.remove("z");
					itemstack.a("BlockEntityTag", (NBTBase) nbttagcompound1);
				}
			}

			boolean flag1 = packetplayinsetcreativeslot.b() >= 1 && packetplayinsetcreativeslot.b() <= 45;
			boolean flag2 = itemstack.isEmpty()
					|| itemstack.getDamage() >= 0 && itemstack.getCount() <= 64 && !itemstack.isEmpty();
			if (flag || (flag1 && !ItemStack.matches(
					this.player.defaultContainer.getSlot(packetplayinsetcreativeslot.b()).getItem(),
					packetplayinsetcreativeslot.getItemStack()))) { // Insist on valid slot
				// CraftBukkit start - Call click event
				InventoryView inventory = this.player.defaultContainer.getBukkitView();
				org.bukkit.inventory.ItemStack item = CraftItemStack
						.asBukkitCopy(packetplayinsetcreativeslot.getItemStack());

				SlotType type = SlotType.QUICKBAR;
				if (flag) {
					type = SlotType.OUTSIDE;
				} else if (packetplayinsetcreativeslot.b() < 36) {
					if (packetplayinsetcreativeslot.b() >= 5 && packetplayinsetcreativeslot.b() < 9) {
						type = SlotType.ARMOR;
					} else {
						type = SlotType.CONTAINER;
					}
				}
				InventoryCreativeEvent event = new InventoryCreativeEvent(inventory, type,
						flag ? -999 : packetplayinsetcreativeslot.b(), item);
				server.getPluginManager().callEvent(event);

				itemstack = CraftItemStack.asNMSCopy(event.getCursor());

				switch (event.getResult()) {
				case ALLOW:
					// Plugin cleared the id / stacksize checks
					flag2 = true;
					break;
				case DEFAULT:
					break;
				case DENY:
					// Reset the slot
					if (packetplayinsetcreativeslot.b() >= 0) {
						this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(
								this.player.defaultContainer.windowId, packetplayinsetcreativeslot.b(),
								this.player.defaultContainer.getSlot(packetplayinsetcreativeslot.b()).getItem()));
						this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, ItemStack.a));
					}
					return;
				}
			}
			// CraftBukkit end

			if (flag1 && flag2) {
				if (itemstack.isEmpty()) {
					this.player.defaultContainer.setItem(packetplayinsetcreativeslot.b(), ItemStack.a);
				} else {
					this.player.defaultContainer.setItem(packetplayinsetcreativeslot.b(), itemstack);
				}

				this.player.defaultContainer.a(this.player, true);
				this.player.defaultContainer.c();
			} else if (flag && flag2 && this.j < 200) {
				this.j += 20;
				this.player.drop(itemstack, true);
			}
		}

	}

	@Override
	public void a(PacketPlayInTransaction packetplayintransaction) {
		PlayerConnectionUtils.ensureMainThread(packetplayintransaction, this, this.player.getWorldServer());
		if (this.player.isFrozen())
			return; // CraftBukkit
		int i = this.player.activeContainer.windowId;

		if (i == packetplayintransaction.b()
				&& this.k.getOrDefault(i, (short) (packetplayintransaction.c() + 1)) == packetplayintransaction.c()
				&& !this.player.activeContainer.c(this.player) && !this.player.isSpectator()) {
			this.player.activeContainer.a(this.player, true);
		}

	}

	@Override
	public void a(PacketPlayInUpdateSign packetplayinupdatesign) {
		PlayerConnectionUtils.ensureMainThread(packetplayinupdatesign, this, this.player.getWorldServer());
		if (this.player.isFrozen())
			return; // CraftBukkit
		this.player.resetIdleTimer();
		WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
		BlockPosition blockposition = packetplayinupdatesign.b();

		if (worldserver.isLoaded(blockposition)) {
			IBlockData iblockdata = worldserver.getType(blockposition);
			TileEntity tileentity = worldserver.getTileEntity(blockposition);

			if (!(tileentity instanceof TileEntitySign)) {
				return;
			}

			TileEntitySign tileentitysign = (TileEntitySign) tileentity;

			if (!tileentitysign.d() || tileentitysign.signEditor == null
					|| !tileentitysign.signEditor.equals(this.player.getUniqueID())) {
				this.minecraftServer.warning("Player " + this.player.getDisplayName().getString()
						+ " just tried to change non-editable sign");
				this.sendPacket(tileentity.getUpdatePacket()); // CraftBukkit
				return;
			}

			String[] astring = packetplayinupdatesign.c();

			// CraftBukkit start
			Player player = this.server.getPlayer(this.player);
			int x = packetplayinupdatesign.b().getX();
			int y = packetplayinupdatesign.b().getY();
			int z = packetplayinupdatesign.b().getZ();
			String[] lines = new String[4];

			for (int i = 0; i < astring.length; ++i) {
				// Paper start - cap line length - modified clients can send longer data than
				// normal
				if (MAX_SIGN_LINE_LENGTH > 0 && astring[i].length() > MAX_SIGN_LINE_LENGTH) {
					// This handles multibyte characters as 1
					int offset = astring[i].codePoints().limit(MAX_SIGN_LINE_LENGTH).map(Character::charCount).sum();
					if (offset < astring[i].length()) {
						astring[i] = astring[i].substring(0, offset);
					}
				}
				// Paper end
				lines[i] = SharedConstants.a(astring[i]); // Paper - Replaced with anvil color stripping method to stop
															// exploits that allow colored signs to be created.
			}
			SignChangeEvent event = new SignChangeEvent(
					(org.bukkit.craftbukkit.block.CraftBlock) player.getWorld().getBlockAt(x, y, z),
					this.server.getPlayer(this.player), lines);
			this.server.getPluginManager().callEvent(event);

			if (!event.isCancelled()) {
				System.arraycopy(org.bukkit.craftbukkit.block.CraftSign.sanitizeLines(event.getLines()), 0,
						tileentitysign.lines, 0, 4);
				tileentitysign.isEditable = false;
			}
			// CraftBukkit end

			tileentitysign.update();
			worldserver.notify(blockposition, iblockdata, iblockdata, 3);
		}

	}

	@Override
	public void a(PacketPlayInKeepAlive packetplayinkeepalive) {
		// PlayerConnectionUtils.ensureMainThread(packetplayinkeepalive, this,
		// this.player.getWorldServer()); // CraftBukkit // Paper - This shouldn't be on
		// the main thread
		if (this.awaitingKeepAlive && packetplayinkeepalive.b() == this.h) {
			int i = (int) (SystemUtils.getMonotonicMillis() - this.lastKeepAlive);

			this.player.ping = (this.player.ping * 3 + i) / 4;
			this.awaitingKeepAlive = false;
		} else if (!this.isExemptPlayer()) {
			// Paper start - This needs to be handled on the main thread for plugins
			minecraftServer.scheduleOnMain(() -> {
				this.disconnect(new ChatMessage("disconnect.timeout"));
			});
			// Paper end
		}

	}

	@Override
	public void a(PacketPlayInAbilities packetplayinabilities) {
		PlayerConnectionUtils.ensureMainThread(packetplayinabilities, this, this.player.getWorldServer());
		// CraftBukkit start
		if (this.player.abilities.canFly && this.player.abilities.isFlying != packetplayinabilities.isFlying()) {
			PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(this.server.getPlayer(this.player),
					packetplayinabilities.isFlying());
			this.server.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				this.player.abilities.isFlying = packetplayinabilities.isFlying(); // Actually set the player's flying
																					// status
			} else {
				this.player.updateAbilities(); // Tell the player their ability was reverted
			}
		}
		// CraftBukkit end
	}

	@Override
	public void a(PacketPlayInSettings packetplayinsettings) {
		PlayerConnectionUtils.ensureMainThread(packetplayinsettings, this, this.player.getWorldServer());
		this.player.a(packetplayinsettings);
	}

	// CraftBukkit start
	private static final MinecraftKey CUSTOM_REGISTER = new MinecraftKey("register");
	private static final MinecraftKey CUSTOM_UNREGISTER = new MinecraftKey("unregister");

	@Override
	public void a(PacketPlayInCustomPayload packetplayincustompayload) {
		PlayerConnectionUtils.ensureMainThread(packetplayincustompayload, this, this.player.getWorldServer());
		if (packetplayincustompayload.tag.equals(CUSTOM_REGISTER)) {
			try {
				String channels = packetplayincustompayload.data.toString(com.google.common.base.Charsets.UTF_8);
				for (String channel : channels.split("\0")) {
					getPlayer().addChannel(channel);
				}
			} catch (Exception ex) {
				PlayerConnection.LOGGER.error("Couldn\'t register custom payload", ex);
				this.disconnect("Invalid payload REGISTER!");
			}
		} else if (packetplayincustompayload.tag.equals(CUSTOM_UNREGISTER)) {
			try {
				String channels = packetplayincustompayload.data.toString(com.google.common.base.Charsets.UTF_8);
				for (String channel : channels.split("\0")) {
					getPlayer().removeChannel(channel);
				}
			} catch (Exception ex) {
				PlayerConnection.LOGGER.error("Couldn\'t unregister custom payload", ex);
				this.disconnect("Invalid payload UNREGISTER!");
			}
		} else {
			try {
				byte[] data = new byte[packetplayincustompayload.data.readableBytes()];
				packetplayincustompayload.data.readBytes(data);
				server.getMessenger().dispatchIncomingMessage(player.getBukkitEntity(),
						packetplayincustompayload.tag.toString(), data);
			} catch (Exception ex) {
				PlayerConnection.LOGGER.error("Couldn\'t dispatch custom payload", ex);
				this.disconnect("Invalid custom payload!");
			}
		}

	}

	public final boolean isDisconnected() {
		return (!this.player.joining && !this.networkManager.isConnected()) || this.processedDisconnect; // Paper
	}
	// CraftBukkit end

	@Override
	public void a(PacketPlayInDifficultyChange packetplayindifficultychange) {
		PlayerConnectionUtils.ensureMainThread(packetplayindifficultychange, this, this.player.getWorldServer());
		if (this.player.k(2) || this.isExemptPlayer()) {
			this.minecraftServer.a(packetplayindifficultychange.b(), false);
		}
	}

	@Override
	public void a(PacketPlayInDifficultyLock packetplayindifficultylock) {
		PlayerConnectionUtils.ensureMainThread(packetplayindifficultylock, this, this.player.getWorldServer());
		if (this.player.k(2) || this.isExemptPlayer()) {
			this.minecraftServer.d(packetplayindifficultylock.b());
		}
	}
}
