package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.server.ArraySetSorted;
import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.BiomeBase;
import net.minecraft.server.BiomeDecoratorGroups;
import net.minecraft.server.BlockChorusFlower;
import net.minecraft.server.BlockDiodeAbstract;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Blocks;
import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.ChunkMapDistance;
import net.minecraft.server.ChunkStatus;
import net.minecraft.server.EntityAreaEffectCloud;
import net.minecraft.server.EntityArmorStand;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityBoat;
import net.minecraft.server.EntityCustomMonster;
import net.minecraft.server.EntityEgg;
import net.minecraft.server.EntityEnderSignal;
import net.minecraft.server.EntityEvokerFangs;
import net.minecraft.server.EntityExperienceOrb;
import net.minecraft.server.EntityFallingBlock;
import net.minecraft.server.EntityFireball;
import net.minecraft.server.EntityFireworks;
import net.minecraft.server.EntityHanging;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityInsentient;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityItemFrame;
import net.minecraft.server.EntityLeash;
import net.minecraft.server.EntityLightning;
import net.minecraft.server.EntityMinecartChest;
import net.minecraft.server.EntityMinecartCommandBlock;
import net.minecraft.server.EntityMinecartFurnace;
import net.minecraft.server.EntityMinecartHopper;
import net.minecraft.server.EntityMinecartMobSpawner;
import net.minecraft.server.EntityMinecartRideable;
import net.minecraft.server.EntityMinecartTNT;
import net.minecraft.server.EntityPainting;
import net.minecraft.server.EntityPotion;
import net.minecraft.server.EntitySnowball;
import net.minecraft.server.EntityTNTPrimed;
import net.minecraft.server.EntityTippedArrow;
import net.minecraft.server.EntityTypes;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.EnumDifficulty;
import net.minecraft.server.EnumDirection;
import net.minecraft.server.EnumMobSpawn;
import net.minecraft.server.ExceptionWorldConflict;
import net.minecraft.server.Explosion;
import net.minecraft.server.GameRules;
import net.minecraft.server.GroupDataEntity;
import net.minecraft.server.IBlockData;
import net.minecraft.server.IChunkAccess;
import net.minecraft.server.MCUtil;
import net.minecraft.server.MinecraftKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.MovingObjectPosition;
import net.minecraft.server.PacketPlayOutCustomSoundEffect;
import net.minecraft.server.PacketPlayOutUpdateTime;
import net.minecraft.server.PacketPlayOutWorldEvent;
import net.minecraft.server.PersistentRaid;
import net.minecraft.server.PlayerChunk;
import net.minecraft.server.ProtoChunkExtension;
import net.minecraft.server.RayTrace;
import net.minecraft.server.SoundCategory;
import net.minecraft.server.Ticket;
import net.minecraft.server.TicketType;
import net.minecraft.server.Unit;
import net.minecraft.server.Vec3D;
import net.minecraft.server.WorldGenerator;
import net.minecraft.server.WorldProvider;
import net.minecraft.server.WorldProviderTheEnd;
import net.minecraft.server.WorldServer;
import org.apache.commons.lang.Validate;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Difficulty;
import org.bukkit.Effect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Raid;
import org.bukkit.Sound;
import org.bukkit.StructureType;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.boss.CraftDragonBattle;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftLightningStrike;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftRPGEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.metadata.BlockMetadataStore;
import org.bukkit.craftbukkit.potion.CraftPotionUtil;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftRayTraceResult;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Consumer;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.stormrealms.stormspigot.entities.RPGEntityData;

public class CraftWorld implements World {
	public static final int CUSTOM_DIMENSION_OFFSET = 10;

	private final WorldServer world;
	private WorldBorder worldBorder;
	private Environment environment;
	private final CraftServer server = (CraftServer) Bukkit.getServer();
	private final ChunkGenerator generator;
	private final List<BlockPopulator> populators = new ArrayList<BlockPopulator>();
	private final BlockMetadataStore blockMetadata = new BlockMetadataStore(this);
	private int monsterSpawn = -1;
	private int animalSpawn = -1;
	private int waterAnimalSpawn = -1;
	private int ambientSpawn = -1;

	// Paper start - Provide fast information methods
	public int getEntityCount() {
		int ret = 0;
		for (net.minecraft.server.Entity entity : world.entitiesById.values()) {
			if (entity.isChunkLoaded()) {
				++ret;
			}
		}
		return ret;
	}

	public int getTileEntityCount() {
		return MCUtil.ensureMain(() -> {
			// We don't use the full world tile entity list, so we must iterate chunks
			Long2ObjectLinkedOpenHashMap<PlayerChunk> chunks = world.getChunkProvider().playerChunkMap.visibleChunks;
			int size = 0;
			for (net.minecraft.server.PlayerChunk playerchunk : chunks.values()) {
				net.minecraft.server.Chunk chunk = playerchunk.getChunk();
				if (chunk == null) {
					continue;
				}
				size += chunk.tileEntities.size();
			}
			return size;
		});
	}

	public int getTickableTileEntityCount() {
		return world.tileEntityListTick.size();
	}

	public int getChunkCount() {
		return MCUtil.ensureMain(() -> {
			int ret = 0;

			for (PlayerChunk chunkHolder : world.getChunkProvider().playerChunkMap.visibleChunks.values()) {
				if (chunkHolder.getChunk() != null) {
					++ret;
				}
			}

			return ret;
		});
	}

	public int getPlayerCount() {
		return world.players.size();
	}
	// Paper end

	private static final Random rand = new Random();

	public CraftWorld(WorldServer world, ChunkGenerator gen, Environment env) {
		this.world = world;
		this.generator = gen;

		environment = env;
	}

	@Override
	public Block getBlockAt(int x, int y, int z) {
		return CraftBlock.at(world, new BlockPosition(x, y, z));
	}

	@Override
	public int getHighestBlockYAt(int x, int z) {
		return getHighestBlockYAt(x, z, org.bukkit.HeightMap.MOTION_BLOCKING);
	}

	// Paper start - Implement heightmap api
	@Override
	public int getHighestBlockYAt(final int x, final int z, final com.destroystokyo.paper.HeightmapType heightmap)
			throws UnsupportedOperationException {
		this.getChunkAt(x >> 4, z >> 4); // heightmap will ret 0 on unloaded areas

		switch (heightmap) {
		case LIGHT_BLOCKING:
			throw new UnsupportedOperationException(); // TODO
		// return this.world.getHighestBlockY(HeightMap.Type.LIGHT_BLOCKING, x, z);
		case ANY:
			return this.world.getHighestBlockY(net.minecraft.server.HeightMap.Type.WORLD_SURFACE, x, z);
		case SOLID:
			return this.world.getHighestBlockY(net.minecraft.server.HeightMap.Type.OCEAN_FLOOR, x, z);
		case SOLID_OR_LIQUID:
			return this.world.getHighestBlockY(net.minecraft.server.HeightMap.Type.MOTION_BLOCKING, x, z);
		case SOLID_OR_LIQUID_NO_LEAVES:
			return this.world.getHighestBlockY(net.minecraft.server.HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
		default:
			throw new UnsupportedOperationException();
		}
	}
	// Paper end

	@Override
	public Location getSpawnLocation() {
		BlockPosition spawn = world.getSpawn();
		return new Location(this, spawn.getX(), spawn.getY(), spawn.getZ());
	}

	@Override
	public boolean setSpawnLocation(Location location) {
		Preconditions.checkArgument(location != null, "location");

		return equals(location.getWorld())
				? setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ())
				: false;
	}

	@Override
	public boolean setSpawnLocation(int x, int y, int z) {
		try {
			Location previousLocation = getSpawnLocation();
			world.worldData.setSpawn(new BlockPosition(x, y, z));

			// Notify anyone who's listening.
			SpawnChangeEvent event = new SpawnChangeEvent(this, previousLocation);
			server.getPluginManager().callEvent(event);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Chunk getChunkAt(int x, int z) {
		// Paper start - add ticket to hold chunk for a little while longer if plugin
		// accesses it
		net.minecraft.server.Chunk chunk = world.getChunkProvider().getChunkAtIfLoadedImmediately(x, z);
		if (chunk == null) {
			addTicket(x, z);
			chunk = this.world.getChunkProvider().getChunkAt(x, z, true);
		}
		return chunk.bukkitChunk;
		// Paper end
	}

	// Paper start
	private void addTicket(int x, int z) {
		MCUtil.MAIN_EXECUTOR.execute(() -> world.getChunkProvider().addTicket(TicketType.PLUGIN,
				new ChunkCoordIntPair(x, z), 0, Unit.INSTANCE)); // Paper
	}
	// Paper end

	@Override
	public Chunk getChunkAt(Block block) {
		Preconditions.checkArgument(block != null, "null block");

		return getChunkAt(block.getX() >> 4, block.getZ() >> 4);
	}

	@Override
	public boolean isChunkLoaded(int x, int z) {
		return world.getChunkProvider().getChunkAtIfLoadedImmediately(x, z) != null; // Paper
	}

	@Override
	public boolean isChunkGenerated(int x, int z) {
		// Paper start - Fix this method
		if (!Bukkit.isPrimaryThread()) {
			return CompletableFuture.supplyAsync(() -> {
				return CraftWorld.this.isChunkGenerated(x, z);
			}, world.getChunkProvider().serverThreadQueue).join();
		}
		IChunkAccess chunk = world.getChunkProvider().getChunkAtImmediately(x, z);
		if (chunk == null) {
			chunk = world.getChunkProvider().playerChunkMap.getUnloadingChunk(x, z);
		}
		if (chunk != null) {
			return chunk instanceof ProtoChunkExtension || chunk instanceof net.minecraft.server.Chunk;
		}
		try {
			return world.getChunkProvider().playerChunkMap
					.getChunkStatusOnDisk(new ChunkCoordIntPair(x, z)) == ChunkStatus.FULL;
			// Paper end
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Chunk[] getLoadedChunks() {
		// Paper start
		if (Thread.currentThread() != world.getMinecraftWorld().serverThread) {
			synchronized (world.getChunkProvider().playerChunkMap.visibleChunks) {
				Long2ObjectLinkedOpenHashMap<PlayerChunk> chunks = world
						.getChunkProvider().playerChunkMap.visibleChunks;
				return chunks.values().stream().map(PlayerChunk::getFullChunk).filter(Objects::nonNull)
						.map(net.minecraft.server.Chunk::getBukkitChunk).toArray(Chunk[]::new);
			}
		}
		// Paper end
		Long2ObjectLinkedOpenHashMap<PlayerChunk> chunks = world.getChunkProvider().playerChunkMap.visibleChunks;
		return chunks.values().stream().map(PlayerChunk::getFullChunk).filter(Objects::nonNull)
				.map(net.minecraft.server.Chunk::getBukkitChunk).toArray(Chunk[]::new);
	}

	@Override
	public void loadChunk(int x, int z) {
		loadChunk(x, z, true);
	}

	@Override
	public boolean unloadChunk(Chunk chunk) {
		return unloadChunk(chunk.getX(), chunk.getZ());
	}

	@Override
	public boolean unloadChunk(int x, int z) {
		return unloadChunk(x, z, true);
	}

	@Override
	public boolean unloadChunk(int x, int z, boolean save) {
		return unloadChunk0(x, z, save);
	}

	@Override
	public boolean unloadChunkRequest(int x, int z) {
		org.spigotmc.AsyncCatcher.catchOp("chunk unload"); // Spigot
		if (isChunkLoaded(x, z)) {
			world.getChunkProvider().removeTicket(TicketType.PLUGIN, new ChunkCoordIntPair(x, z), 0, Unit.INSTANCE); // Paper
		}

		return true;
	}

	private boolean unloadChunk0(int x, int z, boolean save) {
		org.spigotmc.AsyncCatcher.catchOp("chunk unload"); // Spigot
		if (!isChunkLoaded(x, z)) {
			return true;
		}
		net.minecraft.server.Chunk chunk = world.getChunkAt(x, z);

		chunk.mustNotSave = !save;
		unloadChunkRequest(x, z);

		world.getChunkProvider().purgeUnload();
		return !isChunkLoaded(x, z);
	}

	@Override
	public boolean regenerateChunk(int x, int z) {
		org.spigotmc.AsyncCatcher.catchOp("chunk regenerate"); // Spigot
		throw new UnsupportedOperationException(
				"Not supported in this Minecraft version! Unless you can fix it, this is not a bug :)");
		/*
		 * if (!unloadChunk0(x, z, false)) { return false; }
		 * 
		 * final long chunkKey = ChunkCoordIntPair.pair(x, z);
		 * world.getChunkProvider().unloadQueue.remove(chunkKey);
		 * 
		 * net.minecraft.server.Chunk chunk = world.getChunkProvider().generateChunk(x,
		 * z); PlayerChunk playerChunk = world.getPlayerChunkMap().getChunk(x, z); if
		 * (playerChunk != null) { playerChunk.chunk = chunk; }
		 * 
		 * if (chunk != null) { refreshChunk(x, z); }
		 * 
		 * return chunk != null;
		 */
	}

	@Override
	public boolean refreshChunk(int x, int z) {
		if (!isChunkLoaded(x, z)) {
			return false;
		}

		int px = x << 4;
		int pz = z << 4;

		// If there are more than 64 updates to a chunk at once, it will update all
		// 'touched' sections within the chunk
		// And will include biome data if all sections have been 'touched'
		// This flags 65 blocks distributed across all the sections of the chunk, so
		// that everything is sent, including biomes
		int height = getMaxHeight() / 16;
		for (int idx = 0; idx < 64; idx++) {
			world.notify(new BlockPosition(px + (idx / height), ((idx % height) * 16), pz), Blocks.AIR.getBlockData(),
					Blocks.STONE.getBlockData(), 3);
		}
		world.notify(new BlockPosition(px + 15, (height * 16) - 1, pz + 15), Blocks.AIR.getBlockData(),
				Blocks.STONE.getBlockData(), 3);

		return true;
	}

	@Override
	public boolean isChunkInUse(int x, int z) {
		return isChunkLoaded(x, z);
	}

	@Override
	public boolean loadChunk(int x, int z, boolean generate) {
		org.spigotmc.AsyncCatcher.catchOp("chunk load"); // Spigot
		// Paper start - Optimize this method
		ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(x, z);
		IChunkAccess immediate = world.getChunkProvider().getChunkAtIfLoadedImmediately(x, z); // Paper
		if (immediate != null)
			return true; // Paper

		if (!generate) {

			// IChunkAccess immediate = world.getChunkProvider().getChunkAtImmediately(x,
			// z); // Paper
			if (immediate == null) {
				immediate = world.getChunkProvider().playerChunkMap.getUnloadingChunk(x, z);
			}
			if (immediate != null) {
				if (!(immediate instanceof ProtoChunkExtension) && !(immediate instanceof net.minecraft.server.Chunk)) {
					return false; // not full status
				}
				world.getChunkProvider().addTicket(TicketType.PLUGIN, chunkPos, 0, Unit.INSTANCE); // Paper
				world.getChunkAt(x, z); // make sure we're at ticket level 32 or lower
				return true;
			}

			ChunkStatus status = world.getChunkProvider().playerChunkMap.getStatusOnDiskNoLoad(x, z); // Paper - async
																										// io - move to
																										// own method

			// Paper start - async io
			if (status == ChunkStatus.EMPTY) {
				// does not exist on disk
				return false;
			}

			if (status == null) { // at this stage we don't know what it is on disk
				IChunkAccess chunk = world.getChunkProvider().getChunkAt(x, z, ChunkStatus.EMPTY, true);
				if (!(chunk instanceof ProtoChunkExtension) && !(chunk instanceof net.minecraft.server.Chunk)) {
					return false;
				}
			} else if (status != ChunkStatus.FULL) {
				return false; // not full status on disk
			}
			// Paper end

			// fall through to load
			// we do this so we do not re-read the chunk data on disk
		}

		world.getChunkProvider().addTicket(TicketType.PLUGIN, chunkPos, 0, Unit.INSTANCE); // Paper
		world.getChunkProvider().getChunkAt(x, z, ChunkStatus.FULL, true);
		return true;
		// Paper end
	}

	@Override
	public boolean isChunkLoaded(Chunk chunk) {
		Preconditions.checkArgument(chunk != null, "null chunk");

		return isChunkLoaded(chunk.getX(), chunk.getZ());
	}

	@Override
	public void loadChunk(Chunk chunk) {
		Preconditions.checkArgument(chunk != null, "null chunk");

		loadChunk(chunk.getX(), chunk.getZ());
		((CraftChunk) getChunkAt(chunk.getX(), chunk.getZ())).getHandle().bukkitChunk = chunk;
	}

	@Override
	public boolean addPluginChunkTicket(int x, int z, Plugin plugin) {
		Preconditions.checkArgument(plugin != null, "null plugin");
		Preconditions.checkArgument(plugin.isEnabled(), "plugin is not enabled");

		ChunkMapDistance chunkDistanceManager = this.world.getChunkProvider().playerChunkMap.chunkDistanceManager;

		if (chunkDistanceManager.addTicketAtLevel(TicketType.PLUGIN_TICKET, new ChunkCoordIntPair(x, z), 31, plugin)) {
			this.getChunkAt(x, z); // ensure loaded
			return true;
		}

		return false;
	}

	@Override
	public boolean removePluginChunkTicket(int x, int z, Plugin plugin) {
		Preconditions.checkNotNull(plugin, "null plugin");

		ChunkMapDistance chunkDistanceManager = this.world.getChunkProvider().playerChunkMap.chunkDistanceManager;
		return chunkDistanceManager.removeTicketAtLevel(TicketType.PLUGIN_TICKET, new ChunkCoordIntPair(x, z), 31,
				plugin); // keep in-line with force loading, remove at level 31
	}

	@Override
	public void removePluginChunkTickets(Plugin plugin) {
		Preconditions.checkNotNull(plugin, "null plugin");

		ChunkMapDistance chunkDistanceManager = this.world.getChunkProvider().playerChunkMap.chunkDistanceManager;
		chunkDistanceManager.removeAllTicketsFor(TicketType.PLUGIN_TICKET, 31, plugin); // keep in-line with force
																						// loading, remove at level 31
	}

	@Override
	public Collection<Plugin> getPluginChunkTickets(int x, int z) {
		ChunkMapDistance chunkDistanceManager = this.world.getChunkProvider().playerChunkMap.chunkDistanceManager;
		ArraySetSorted<Ticket<?>> tickets = chunkDistanceManager.tickets.get(ChunkCoordIntPair.pair(x, z));

		if (tickets == null) {
			return Collections.emptyList();
		}

		ImmutableList.Builder<Plugin> ret = ImmutableList.builder();
		for (Ticket<?> ticket : tickets) {
			if (ticket.getTicketType() == TicketType.PLUGIN_TICKET) {
				ret.add((Plugin) ticket.identifier);
			}
		}

		return ret.build();
	}

	@Override
	public Map<Plugin, Collection<Chunk>> getPluginChunkTickets() {
		Map<Plugin, ImmutableList.Builder<Chunk>> ret = new HashMap<>();
		ChunkMapDistance chunkDistanceManager = this.world.getChunkProvider().playerChunkMap.chunkDistanceManager;

		for (Long2ObjectMap.Entry<ArraySetSorted<Ticket<?>>> chunkTickets : chunkDistanceManager.tickets
				.long2ObjectEntrySet()) {
			long chunkKey = chunkTickets.getLongKey();
			ArraySetSorted<Ticket<?>> tickets = chunkTickets.getValue();

			Chunk chunk = null;
			for (Ticket<?> ticket : tickets) {
				if (ticket.getTicketType() != TicketType.PLUGIN_TICKET) {
					continue;
				}

				if (chunk == null) {
					chunk = this.getChunkAt(ChunkCoordIntPair.getX(chunkKey), ChunkCoordIntPair.getZ(chunkKey));
				}

				ret.computeIfAbsent((Plugin) ticket.identifier, (key) -> ImmutableList.builder()).add(chunk);
			}
		}

		return ret.entrySet().stream()
				.collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry) -> entry.getValue().build()));
	}

	@Override
	public boolean isChunkForceLoaded(int x, int z) {
		return getHandle().getForceLoadedChunks().contains(ChunkCoordIntPair.pair(x, z));
	}

	@Override
	public void setChunkForceLoaded(int x, int z, boolean forced) {
		getHandle().setForceLoaded(x, z, forced);
	}

	@Override
	public Collection<Chunk> getForceLoadedChunks() {
		Set<Chunk> chunks = new HashSet<>();

		for (long coord : getHandle().getForceLoadedChunks()) {
			chunks.add(getChunkAt(ChunkCoordIntPair.getX(coord), ChunkCoordIntPair.getZ(coord)));
		}

		return Collections.unmodifiableCollection(chunks);
	}

	public WorldServer getHandle() {
		return world;
	}

	@Override
	public org.bukkit.entity.Item dropItem(Location loc, ItemStack item) {
		Validate.notNull(item, "Cannot drop a Null item.");
		EntityItem entity = new EntityItem(world, loc.getX(), loc.getY(), loc.getZ(), CraftItemStack.asNMSCopy(item));
		entity.pickupDelay = 10;
		world.addEntity(entity, SpawnReason.CUSTOM);
		return (org.bukkit.entity.Item) entity.getBukkitEntity();
	}

	@Override
	public org.bukkit.entity.Item dropItemNaturally(Location loc, ItemStack item) {
		double xs = (world.random.nextFloat() * 0.5F) + 0.25D;
		double ys = (world.random.nextFloat() * 0.5F) + 0.25D;
		double zs = (world.random.nextFloat() * 0.5F) + 0.25D;
		loc = loc.clone();
		loc.setX(loc.getX() + xs);
		loc.setY(loc.getY() + ys);
		loc.setZ(loc.getZ() + zs);
		return dropItem(loc, item);
	}

	@Override
	public Arrow spawnArrow(Location loc, Vector velocity, float speed, float spread) {
		return spawnArrow(loc, velocity, speed, spread, Arrow.class);
	}

	@Override
	public <T extends AbstractArrow> T spawnArrow(Location loc, Vector velocity, float speed, float spread,
			Class<T> clazz) {
		Validate.notNull(loc, "Can not spawn arrow with a null location");
		Validate.notNull(velocity, "Can not spawn arrow with a null velocity");
		Validate.notNull(clazz, "Can not spawn an arrow with no class");

		EntityArrow arrow;
		if (TippedArrow.class.isAssignableFrom(clazz)) {
			arrow = EntityTypes.ARROW.a(world);
			((EntityTippedArrow) arrow)
					.setType(CraftPotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
		} else if (SpectralArrow.class.isAssignableFrom(clazz)) {
			arrow = EntityTypes.SPECTRAL_ARROW.a(world);
		} else if (Trident.class.isAssignableFrom(clazz)) {
			arrow = EntityTypes.TRIDENT.a(world);
		} else {
			arrow = EntityTypes.ARROW.a(world);
		}

		arrow.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		arrow.shoot(velocity.getX(), velocity.getY(), velocity.getZ(), speed, spread);
		world.addEntity(arrow);
		return (T) arrow.getBukkitEntity();
	}

	@Override
	public Entity spawnEntity(Location loc, EntityType entityType) {
		return spawn(loc, entityType.getEntityClass());
	}

	@Override
	public LightningStrike strikeLightning(Location loc) {
		EntityLightning lightning = new EntityLightning(world, loc.getX(), loc.getY(), loc.getZ(), false);
		world.strikeLightning(lightning);
		return new CraftLightningStrike(server, lightning);
	}

	@Override
	public LightningStrike strikeLightningEffect(Location loc) {
		EntityLightning lightning = new EntityLightning(world, loc.getX(), loc.getY(), loc.getZ(), true);
		world.strikeLightning(lightning);
		return new CraftLightningStrike(server, lightning);
	}

	@Override
	public boolean generateTree(Location loc, TreeType type) {
		BlockPosition pos = new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

		net.minecraft.server.WorldGenerator gen;
		net.minecraft.server.WorldGenFeatureConfiguration conf;
		switch (type) {
		case BIG_TREE:
			gen = WorldGenerator.FANCY_TREE;
			conf = BiomeDecoratorGroups.FANCY_TREE;
			break;
		case BIRCH:
			gen = WorldGenerator.NORMAL_TREE;
			conf = BiomeDecoratorGroups.BIRCH_TREE;
			break;
		case REDWOOD:
			gen = WorldGenerator.NORMAL_TREE;
			conf = BiomeDecoratorGroups.SPRUCE_TREE;
			break;
		case TALL_REDWOOD:
			gen = WorldGenerator.NORMAL_TREE;
			conf = BiomeDecoratorGroups.PINE_TREE;
			break;
		case JUNGLE:
			gen = WorldGenerator.MEGA_JUNGLE_TREE;
			conf = BiomeDecoratorGroups.MEGA_JUNGLE_TREE;
			break;
		case SMALL_JUNGLE:
			gen = WorldGenerator.NORMAL_TREE;
			conf = BiomeDecoratorGroups.JUNGLE_TREE_NOVINE;
			break;
		case COCOA_TREE:
			gen = WorldGenerator.NORMAL_TREE;
			conf = BiomeDecoratorGroups.JUNGLE_TREE;
			break;
		case JUNGLE_BUSH:
			gen = WorldGenerator.JUNGLE_GROUND_BUSH;
			conf = BiomeDecoratorGroups.JUNGLE_BUSH;
			break;
		case RED_MUSHROOM:
			gen = WorldGenerator.HUGE_RED_MUSHROOM;
			conf = BiomeDecoratorGroups.HUGE_RED_MUSHROOM;
			break;
		case BROWN_MUSHROOM:
			gen = WorldGenerator.HUGE_BROWN_MUSHROOM;
			conf = BiomeDecoratorGroups.HUGE_BROWN_MUSHROOM;
			break;
		case SWAMP:
			gen = WorldGenerator.NORMAL_TREE;
			conf = BiomeDecoratorGroups.SWAMP_TREE;
			break;
		case ACACIA:
			gen = WorldGenerator.ACACIA_TREE;
			conf = BiomeDecoratorGroups.ACACIA_TREE;
			break;
		case DARK_OAK:
			gen = WorldGenerator.DARK_OAK_TREE;
			conf = BiomeDecoratorGroups.DARK_OAK_TREE;
			break;
		case MEGA_REDWOOD:
			gen = WorldGenerator.MEGA_SPRUCE_TREE;
			conf = BiomeDecoratorGroups.MEGA_PINE_TREE;
			break;
		case TALL_BIRCH:
			gen = WorldGenerator.NORMAL_TREE;
			conf = BiomeDecoratorGroups.TALL_BIRCH_TREE_BEES_0002;
			break;
		case CHORUS_PLANT:
			((BlockChorusFlower) Blocks.CHORUS_FLOWER).a(world, pos, rand, 8);
			return true;
		case TREE:
		default:
			gen = WorldGenerator.NORMAL_TREE;
			conf = BiomeDecoratorGroups.NORMAL_TREE;
			break;
		}

		return gen.generate(world, world.worldProvider.getChunkGenerator(), rand, pos, conf);
	}

	@Override
	public boolean generateTree(Location loc, TreeType type, BlockChangeDelegate delegate) {
		world.captureTreeGeneration = true;
		world.captureBlockStates = true;
		boolean grownTree = generateTree(loc, type);
		world.captureBlockStates = false;
		world.captureTreeGeneration = false;
		if (grownTree) { // Copy block data to delegate
			for (BlockState blockstate : world.capturedBlockStates.values()) {
				BlockPosition position = ((CraftBlockState) blockstate).getPosition();
				net.minecraft.server.IBlockData oldBlock = world.getType(position);
				int flag = ((CraftBlockState) blockstate).getFlag();
				delegate.setBlockData(blockstate.getX(), blockstate.getY(), blockstate.getZ(),
						blockstate.getBlockData());
				net.minecraft.server.IBlockData newBlock = world.getType(position);
				world.notifyAndUpdatePhysics(position, null, oldBlock, newBlock, newBlock, flag);
			}
			world.capturedBlockStates.clear();
			return true;
		} else {
			world.capturedBlockStates.clear();
			return false;
		}
	}

	@Override
	public String getName() {
		return world.worldData.getName();
	}

	@Override
	public UUID getUID() {
		return world.getDataManager().getUUID();
	}

	@Override
	public String toString() {
		return "CraftWorld{name=" + getName() + '}';
	}

	@Override
	public long getTime() {
		long time = getFullTime() % 24000;
		if (time < 0)
			time += 24000;
		return time;
	}

	@Override
	public void setTime(long time) {
		long margin = (time - getFullTime()) % 24000;
		if (margin < 0)
			margin += 24000;
		setFullTime(getFullTime() + margin);
	}

	@Override
	public long getFullTime() {
		return world.getDayTime();
	}

	@Override
	public void setFullTime(long time) {
		// Notify anyone who's listening
		TimeSkipEvent event = new TimeSkipEvent(this, TimeSkipEvent.SkipReason.CUSTOM, time - world.getDayTime());
		server.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}

		world.setDayTime(world.getDayTime() + event.getSkipAmount());

		// Forces the client to update to the new time immediately
		for (Player p : getPlayers()) {
			CraftPlayer cp = (CraftPlayer) p;
			if (cp.getHandle().playerConnection == null)
				continue;

			cp.getHandle().playerConnection.sendPacket(
					new PacketPlayOutUpdateTime(cp.getHandle().world.getTime(), cp.getHandle().getPlayerTime(),
							cp.getHandle().world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
		}
	}

	// Paper start
	@Override
	public boolean isDayTime() {
		return getHandle().isDay();
	}
	// Paper end

	@Override
	public boolean createExplosion(double x, double y, double z, float power) {
		return createExplosion(x, y, z, power, false, true);
	}

	@Override
	public boolean createExplosion(double x, double y, double z, float power, boolean setFire) {
		return createExplosion(x, y, z, power, setFire, true);
	}

	@Override
	public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks) {
		return createExplosion(x, y, z, power, setFire, breakBlocks, null);
	}

	@Override
	public boolean createExplosion(double x, double y, double z, float power, boolean setFire, boolean breakBlocks,
			Entity source) {
		return !world.createExplosion(source == null ? null : ((CraftEntity) source).getHandle(), x, y, z, power,
				setFire, breakBlocks ? Explosion.Effect.BREAK : Explosion.Effect.NONE).wasCanceled;
	}

	// Paper start
	public boolean createExplosion(Entity source, Location loc, float power, boolean setFire, boolean breakBlocks) {
		return !world.createExplosion(
				source != null ? ((org.bukkit.craftbukkit.entity.CraftEntity) source).getHandle() : null, loc.getX(),
				loc.getY(), loc.getZ(), power, setFire,
				breakBlocks ? Explosion.Effect.BREAK : Explosion.Effect.NONE).wasCanceled;
	}
	// Paper end

	@Override
	public boolean createExplosion(Location loc, float power) {
		return createExplosion(loc, power, false);
	}

	@Override
	public boolean createExplosion(Location loc, float power, boolean setFire) {
		return createExplosion(loc, power, setFire, true);
	}

	@Override
	public boolean createExplosion(Location loc, float power, boolean setFire, boolean breakBlocks) {
		return createExplosion(loc, power, setFire, breakBlocks, null);
	}

	@Override
	public boolean createExplosion(Location loc, float power, boolean setFire, boolean breakBlocks, Entity source) {
		Preconditions.checkArgument(loc != null, "Location is null");
		Preconditions.checkArgument(this.equals(loc.getWorld()), "Location not in world");

		return createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, setFire, breakBlocks, source);
	}

	@Override
	public Environment getEnvironment() {
		return environment;
	}

	@Override
	public Block getBlockAt(Location location) {
		return getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	@Override
	public int getHighestBlockYAt(Location location) {
		return getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
	}

	@Override
	public Chunk getChunkAt(Location location) {
		return getChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);
	}

	@Override
	public ChunkGenerator getGenerator() {
		return generator;
	}

	@Override
	public List<BlockPopulator> getPopulators() {
		return populators;
	}

	@Override
	public Block getHighestBlockAt(int x, int z) {
		return getBlockAt(x, getHighestBlockYAt(x, z), z);
	}

	@Override
	public Block getHighestBlockAt(Location location) {
		return getHighestBlockAt(location.getBlockX(), location.getBlockZ());
	}

	@Override
	public int getHighestBlockYAt(int x, int z, org.bukkit.HeightMap heightMap) {
		// Transient load for this tick
		return world.getChunkAt(x >> 4, z >> 4).a(CraftHeightMap.toNMS(heightMap), x, z);
	}

	@Override
	public int getHighestBlockYAt(Location location, org.bukkit.HeightMap heightMap) {
		return getHighestBlockYAt(location.getBlockX(), location.getBlockZ(), heightMap);
	}

	@Override
	public Block getHighestBlockAt(int x, int z, org.bukkit.HeightMap heightMap) {
		return getBlockAt(x, getHighestBlockYAt(x, z, heightMap), z);
	}

	@Override
	public Block getHighestBlockAt(Location location, org.bukkit.HeightMap heightMap) {
		return getHighestBlockAt(location.getBlockX(), location.getBlockZ(), heightMap);
	}

	@Override
	public Biome getBiome(int x, int z) {
		return getBiome(x, 0, z);
	}

	@Override
	public Biome getBiome(int x, int y, int z) {
		return CraftBlock.biomeBaseToBiome(this.world.getBiome(x >> 2, y >> 2, z >> 2));
	}

	@Override
	public void setBiome(int x, int z, Biome bio) {
		for (int y = 0; y < getMaxHeight(); y++) {
			setBiome(x, y, z, bio);
		}
	}

	@Override
	public void setBiome(int x, int y, int z, Biome bio) {
		BiomeBase bb = CraftBlock.biomeToBiomeBase(bio);
		BlockPosition pos = new BlockPosition(x, 0, z);
		if (this.world.isLoaded(pos)) {
			net.minecraft.server.Chunk chunk = this.world.getChunkAtWorldCoords(pos);

			if (chunk != null) {
				chunk.getBiomeIndex().setBiome(x >> 2, y >> 2, z >> 2, bb);

				chunk.markDirty(); // SPIGOT-2890
			}
		}
	}

	@Override
	public double getTemperature(int x, int z) {
		return getTemperature(x, 0, z);
	}

	@Override
	public double getTemperature(int x, int y, int z) {
		BlockPosition pos = new BlockPosition(x, y, z);
		return this.world.getBiome(x >> 2, y >> 2, z >> 2).getAdjustedTemperature(pos);
	}

	@Override
	public double getHumidity(int x, int z) {
		return getHumidity(x, 0, z);
	}

	@Override
	public double getHumidity(int x, int y, int z) {
		return this.world.getBiome(x >> 2, y >> 2, z >> 2).getHumidity();
	}

	@Override
	public List<Entity> getEntities() {
		List<Entity> list = new ArrayList<Entity>();

		for (Object o : world.entitiesById.values()) {
			if (o instanceof net.minecraft.server.Entity) {
				net.minecraft.server.Entity mcEnt = (net.minecraft.server.Entity) o;
				if (mcEnt.shouldBeRemoved)
					continue; // Paper
				Entity bukkitEntity = mcEnt.getBukkitEntity();

				// Assuming that bukkitEntity isn't null
				if (bukkitEntity != null && bukkitEntity.isValid()) {
					list.add(bukkitEntity);
				}
			}
		}

		return list;
	}

	@Override
	public List<LivingEntity> getLivingEntities() {
		List<LivingEntity> list = new ArrayList<LivingEntity>();

		for (Object o : world.entitiesById.values()) {
			if (o instanceof net.minecraft.server.Entity) {
				net.minecraft.server.Entity mcEnt = (net.minecraft.server.Entity) o;
				if (mcEnt.shouldBeRemoved)
					continue; // Paper
				Entity bukkitEntity = mcEnt.getBukkitEntity();

				// Assuming that bukkitEntity isn't null
				if (bukkitEntity != null && bukkitEntity instanceof LivingEntity && bukkitEntity.isValid()) {
					list.add((LivingEntity) bukkitEntity);
				}
			}
		}

		return list;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Deprecated
	public <T extends Entity> Collection<T> getEntitiesByClass(Class<T>... classes) {
		return (Collection<T>) getEntitiesByClasses(classes);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Entity> Collection<T> getEntitiesByClass(Class<T> clazz) {
		Collection<T> list = new ArrayList<T>();

		for (Object entity : world.entitiesById.values()) {
			if (entity instanceof net.minecraft.server.Entity) {
				if (((net.minecraft.server.Entity) entity).shouldBeRemoved)
					continue; // Paper
				Entity bukkitEntity = ((net.minecraft.server.Entity) entity).getBukkitEntity();

				if (bukkitEntity == null) {
					continue;
				}

				Class<?> bukkitClass = bukkitEntity.getClass();

				if (clazz.isAssignableFrom(bukkitClass) && bukkitEntity.isValid()) {
					list.add((T) bukkitEntity);
				}
			}
		}

		return list;
	}

	@Override
	public Collection<Entity> getEntitiesByClasses(Class<?>... classes) {
		Collection<Entity> list = new ArrayList<Entity>();

		for (Object entity : world.entitiesById.values()) {
			if (entity instanceof net.minecraft.server.Entity) {
				if (((net.minecraft.server.Entity) entity).shouldBeRemoved)
					continue; // Paper
				Entity bukkitEntity = ((net.minecraft.server.Entity) entity).getBukkitEntity();

				if (bukkitEntity == null) {
					continue;
				}

				Class<?> bukkitClass = bukkitEntity.getClass();

				for (Class<?> clazz : classes) {
					if (clazz.isAssignableFrom(bukkitClass)) {
						if (bukkitEntity.isValid()) {
							list.add(bukkitEntity);
						}
						break;
					}
				}
			}
		}

		return list;
	}

	@Override
	public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
		return this.getNearbyEntities(location, x, y, z, null);
	}

	@Override
	public Collection<Entity> getNearbyEntities(Location location, double x, double y, double z,
			Predicate<Entity> filter) {
		Validate.notNull(location, "Location is null!");
		Validate.isTrue(this.equals(location.getWorld()), "Location is from different world!");

		BoundingBox aabb = BoundingBox.of(location, x, y, z);
		return this.getNearbyEntities(aabb, filter);
	}

	@Override
	public Collection<Entity> getNearbyEntities(BoundingBox boundingBox) {
		return this.getNearbyEntities(boundingBox, null);
	}

	@Override
	public Collection<Entity> getNearbyEntities(BoundingBox boundingBox, Predicate<Entity> filter) {
		org.spigotmc.AsyncCatcher.catchOp("getNearbyEntities"); // Spigot
		Validate.notNull(boundingBox, "Bounding box is null!");

		AxisAlignedBB bb = new AxisAlignedBB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
				boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
		List<net.minecraft.server.Entity> entityList = getHandle().getEntities((net.minecraft.server.Entity) null, bb,
				null);
		List<Entity> bukkitEntityList = new ArrayList<org.bukkit.entity.Entity>(entityList.size());

		for (net.minecraft.server.Entity entity : entityList) {
			Entity bukkitEntity = entity.getBukkitEntity();
			if (filter == null || filter.test(bukkitEntity)) {
				bukkitEntityList.add(bukkitEntity);
			}
		}

		return bukkitEntityList;
	}

	@Override
	public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance) {
		return this.rayTraceEntities(start, direction, maxDistance, null);
	}

	@Override
	public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize) {
		return this.rayTraceEntities(start, direction, maxDistance, raySize, null);
	}

	@Override
	public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance,
			Predicate<Entity> filter) {
		return this.rayTraceEntities(start, direction, maxDistance, 0.0D, filter);
	}

	@Override
	public RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance, double raySize,
			Predicate<Entity> filter) {
		Validate.notNull(start, "Start location is null!");
		Validate.isTrue(this.equals(start.getWorld()), "Start location is from different world!");
		start.checkFinite();

		Validate.notNull(direction, "Direction is null!");
		direction.checkFinite();

		Validate.isTrue(direction.lengthSquared() > 0, "Direction's magnitude is 0!");

		if (maxDistance < 0.0D) {
			return null;
		}

		Vector startPos = start.toVector();
		Vector dir = direction.clone().normalize().multiply(maxDistance);
		BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(raySize);
		Collection<Entity> entities = this.getNearbyEntities(aabb, filter);

		Entity nearestHitEntity = null;
		RayTraceResult nearestHitResult = null;
		double nearestDistanceSq = Double.MAX_VALUE;

		for (Entity entity : entities) {
			BoundingBox boundingBox = entity.getBoundingBox().expand(raySize);
			RayTraceResult hitResult = boundingBox.rayTrace(startPos, direction, maxDistance);

			if (hitResult != null) {
				double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());

				if (distanceSq < nearestDistanceSq) {
					nearestHitEntity = entity;
					nearestHitResult = hitResult;
					nearestDistanceSq = distanceSq;
				}
			}
		}

		return (nearestHitEntity == null) ? null
				: new RayTraceResult(nearestHitResult.getHitPosition(), nearestHitEntity,
						nearestHitResult.getHitBlockFace());
	}

	@Override
	public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance) {
		return this.rayTraceBlocks(start, direction, maxDistance, FluidCollisionMode.NEVER, false);
	}

	@Override
	public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance,
			FluidCollisionMode fluidCollisionMode) {
		return this.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, false);
	}

	@Override
	public RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance,
			FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks) {
		Validate.notNull(start, "Start location is null!");
		Validate.isTrue(this.equals(start.getWorld()), "Start location is from different world!");
		start.checkFinite();

		Validate.notNull(direction, "Direction is null!");
		direction.checkFinite();

		Validate.isTrue(direction.lengthSquared() > 0, "Direction's magnitude is 0!");
		Validate.notNull(fluidCollisionMode, "Fluid collision mode is null!");

		if (maxDistance < 0.0D) {
			return null;
		}

		Vector dir = direction.clone().normalize().multiply(maxDistance);
		Vec3D startPos = new Vec3D(start.getX(), start.getY(), start.getZ());
		Vec3D endPos = new Vec3D(start.getX() + dir.getX(), start.getY() + dir.getY(), start.getZ() + dir.getZ());
		MovingObjectPosition nmsHitResult = this.getHandle()
				.rayTrace(new RayTrace(startPos, endPos,
						ignorePassableBlocks ? RayTrace.BlockCollisionOption.COLLIDER
								: RayTrace.BlockCollisionOption.OUTLINE,
						CraftFluidCollisionMode.toNMS(fluidCollisionMode), null));

		return CraftRayTraceResult.fromNMS(this, nmsHitResult);
	}

	@Override
	public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance,
			FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize,
			Predicate<Entity> filter) {
		RayTraceResult blockHit = this.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode,
				ignorePassableBlocks);
		Vector startVec = null;
		double blockHitDistance = maxDistance;

		// limiting the entity search range if we found a block hit:
		if (blockHit != null) {
			startVec = start.toVector();
			blockHitDistance = startVec.distance(blockHit.getHitPosition());
		}

		RayTraceResult entityHit = this.rayTraceEntities(start, direction, blockHitDistance, raySize, filter);
		if (blockHit == null) {
			return entityHit;
		}

		if (entityHit == null) {
			return blockHit;
		}

		// Cannot be null as blockHit == null returns above
		double entityHitDistanceSquared = startVec.distanceSquared(entityHit.getHitPosition());
		if (entityHitDistanceSquared < (blockHitDistance * blockHitDistance)) {
			return entityHit;
		}

		return blockHit;
	}

	@Override
	public List<Player> getPlayers() {
		List<Player> list = new ArrayList<Player>(world.getPlayers().size());

		for (EntityHuman human : world.getPlayers()) {
			HumanEntity bukkitEntity = human.getBukkitEntity();

			if ((bukkitEntity != null) && (bukkitEntity instanceof Player)) {
				list.add((Player) bukkitEntity);
			}
		}

		return list;
	}

	// Paper start - getEntity by UUID API
	@Override
	public Entity getEntity(UUID uuid) {
		Validate.notNull(uuid, "UUID cannot be null");
		net.minecraft.server.Entity entity = world.getEntity(uuid);
		return entity == null ? null : entity.getBukkitEntity();
	}
	// Paper end

	@Override
	public void save() {
		org.spigotmc.AsyncCatcher.catchOp("world save"); // Spigot
		this.server.checkSaveState();
		try {
			boolean oldSave = world.savingDisabled;

			world.savingDisabled = false;
			world.save(null, false, false);

			world.savingDisabled = oldSave;
		} catch (ExceptionWorldConflict ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public boolean isAutoSave() {
		return !world.savingDisabled;
	}

	@Override
	public void setAutoSave(boolean value) {
		world.savingDisabled = !value;
	}

	@Override
	public void setDifficulty(Difficulty difficulty) {
		this.getHandle().worldData.setDifficulty(EnumDifficulty.getById(difficulty.getValue()));
	}

	@Override
	public Difficulty getDifficulty() {
		return Difficulty.getByValue(this.getHandle().getDifficulty().ordinal());
	}

	public BlockMetadataStore getBlockMetadata() {
		return blockMetadata;
	}

	@Override
	public boolean hasStorm() {
		return world.worldData.hasStorm();
	}

	@Override
	public void setStorm(boolean hasStorm) {
		world.worldData.setStorm(hasStorm);
		setWeatherDuration(0); // Reset weather duration (legacy behaviour)
	}

	@Override
	public int getWeatherDuration() {
		return world.worldData.getWeatherDuration();
	}

	@Override
	public void setWeatherDuration(int duration) {
		world.worldData.setWeatherDuration(duration);
	}

	@Override
	public boolean isThundering() {
		return world.worldData.isThundering();
	}

	@Override
	public void setThundering(boolean thundering) {
		world.worldData.setThundering(thundering);
		setThunderDuration(0); // Reset weather duration (legacy behaviour)
	}

	@Override
	public int getThunderDuration() {
		return world.worldData.getThunderDuration();
	}

	@Override
	public void setThunderDuration(int duration) {
		world.worldData.setThunderDuration(duration);
	}

	@Override
	public long getSeed() {
		return world.worldData.getSeed();
	}

	@Override
	public boolean getPVP() {
		return world.pvpMode;
	}

	@Override
	public void setPVP(boolean pvp) {
		world.pvpMode = pvp;
	}

	public void playEffect(Player player, Effect effect, int data) {
		playEffect(player.getLocation(), effect, data, 0);
	}

	@Override
	public void playEffect(Location location, Effect effect, int data) {
		playEffect(location, effect, data, 64);
	}

	@Override
	public <T> void playEffect(Location loc, Effect effect, T data) {
		playEffect(loc, effect, data, 64);
	}

	@Override
	public <T> void playEffect(Location loc, Effect effect, T data, int radius) {
		if (data != null) {
			Validate.isTrue(effect.getData() != null && effect.getData().isAssignableFrom(data.getClass()),
					"Wrong kind of data for this effect!");
		} else {
			Validate.isTrue(effect.getData() == null, "Wrong kind of data for this effect!");
		}

		int datavalue = data == null ? 0 : CraftEffect.getDataValue(effect, data);
		playEffect(loc, effect, datavalue, radius);
	}

	@Override
	public void playEffect(Location location, Effect effect, int data, int radius) {
		Validate.notNull(location, "Location cannot be null");
		Validate.notNull(effect, "Effect cannot be null");
		Validate.notNull(location.getWorld(), "World cannot be null");
		int packetData = effect.getId();
		PacketPlayOutWorldEvent packet = new PacketPlayOutWorldEvent(packetData,
				new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), data, false);
		int distance;
		radius *= radius;

		for (Player player : getPlayers()) {
			if (((CraftPlayer) player).getHandle().playerConnection == null)
				continue;
			if (!location.getWorld().equals(player.getWorld()))
				continue;

			distance = (int) player.getLocation().distanceSquared(location);
			if (distance <= radius) {
				((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	@Override
	public <T extends Entity> T spawn(Location location, Class<T> clazz) throws IllegalArgumentException {
		return spawn(location, clazz, null, SpawnReason.CUSTOM);
	}

	@Override
	public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<T> function)
			throws IllegalArgumentException {
		return spawn(location, clazz, function, SpawnReason.CUSTOM);
	}

	@Override
	public FallingBlock spawnFallingBlock(Location location, MaterialData data) throws IllegalArgumentException {
		Validate.notNull(data, "MaterialData cannot be null");
		return spawnFallingBlock(location, data.getItemType(), data.getData());
	}

	@Override
	public FallingBlock spawnFallingBlock(Location location, org.bukkit.Material material, byte data)
			throws IllegalArgumentException {
		Validate.notNull(location, "Location cannot be null");
		Validate.notNull(material, "Material cannot be null");
		Validate.isTrue(material.isBlock(), "Material must be a block");

		EntityFallingBlock entity = new EntityFallingBlock(world, location.getX(), location.getY(), location.getZ(),
				CraftMagicNumbers.getBlock(material).getBlockData());
		entity.ticksLived = 1;

		world.addEntity(entity, SpawnReason.CUSTOM);
		return (FallingBlock) entity.getBukkitEntity();
	}

	@Override
	public FallingBlock spawnFallingBlock(Location location, BlockData data) throws IllegalArgumentException {
		Validate.notNull(location, "Location cannot be null");
		Validate.notNull(data, "Material cannot be null");

		EntityFallingBlock entity = new EntityFallingBlock(world, location.getX(), location.getY(), location.getZ(),
				((CraftBlockData) data).getState());
		entity.ticksLived = 1;

		world.addEntity(entity, SpawnReason.CUSTOM);
		return (FallingBlock) entity.getBukkitEntity();
	}

	@SuppressWarnings("unchecked")
	public net.minecraft.server.Entity createEntity(Location location, Class<? extends Entity> clazz)
			throws IllegalArgumentException {
		if (location == null || clazz == null) {
			throw new IllegalArgumentException("Location or entity class cannot be null");
		}

		net.minecraft.server.Entity entity = null;

		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		float pitch = location.getPitch();
		float yaw = location.getYaw();

		// order is important for some of these
		if (RPGEntity.class.isAssignableFrom(clazz)) {
			System.out.println("FOUND ASSIGNABLE");
			entity = EntityTypes.CUSTOM_MONSTER.a(world);
			System.out.println(entity == null);
		} else if (Boat.class.isAssignableFrom(clazz)) {
			entity = new EntityBoat(world, x, y, z);
			entity.setPositionRotation(x, y, z, yaw, pitch);
			// Paper start
		} else if (org.bukkit.entity.Item.class.isAssignableFrom(clazz)) {
			entity = new EntityItem(world, x, y, z, new net.minecraft.server.ItemStack(
					net.minecraft.server.Item.getItemOf(net.minecraft.server.Blocks.DIRT)));
			// Paper end
		} else if (FallingBlock.class.isAssignableFrom(clazz)) {
			entity = new EntityFallingBlock(world, x, y, z, world.getType(new BlockPosition(x, y, z)));
		} else if (Projectile.class.isAssignableFrom(clazz)) {
			if (Snowball.class.isAssignableFrom(clazz)) {
				entity = new EntitySnowball(world, x, y, z);
			} else if (Egg.class.isAssignableFrom(clazz)) {
				entity = new EntityEgg(world, x, y, z);
			} else if (AbstractArrow.class.isAssignableFrom(clazz)) {
				if (TippedArrow.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.ARROW.a(world);
					((EntityTippedArrow) entity)
							.setType(CraftPotionUtil.fromBukkit(new PotionData(PotionType.WATER, false, false)));
				} else if (SpectralArrow.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.SPECTRAL_ARROW.a(world);
				} else if (Trident.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.TRIDENT.a(world);
				} else {
					entity = EntityTypes.ARROW.a(world);
				}
				entity.setPositionRotation(x, y, z, 0, 0);
			} else if (ThrownExpBottle.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.EXPERIENCE_BOTTLE.a(world);
				entity.setPositionRotation(x, y, z, 0, 0);
			} else if (EnderPearl.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.ENDER_PEARL.a(world);
				entity.setPositionRotation(x, y, z, 0, 0);
			} else if (ThrownPotion.class.isAssignableFrom(clazz)) {
				if (LingeringPotion.class.isAssignableFrom(clazz)) {
					entity = new EntityPotion(world, x, y, z);
					((EntityPotion) entity)
							.setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.LINGERING_POTION, 1)));
				} else {
					entity = new EntityPotion(world, x, y, z);
					((EntityPotion) entity)
							.setItem(CraftItemStack.asNMSCopy(new ItemStack(org.bukkit.Material.SPLASH_POTION, 1)));
				}
			} else if (Fireball.class.isAssignableFrom(clazz)) {
				if (SmallFireball.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.SMALL_FIREBALL.a(world);
				} else if (WitherSkull.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.WITHER_SKULL.a(world);
				} else if (DragonFireball.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.DRAGON_FIREBALL.a(world);
				} else {
					entity = EntityTypes.FIREBALL.a(world);
				}
				entity.setPositionRotation(x, y, z, yaw, pitch);
				Vector direction = location.getDirection().multiply(10);
				((EntityFireball) entity).setDirection(direction.getX(), direction.getY(), direction.getZ());
			} else if (ShulkerBullet.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.SHULKER_BULLET.a(world);
				entity.setPositionRotation(x, y, z, yaw, pitch);
			} else if (LlamaSpit.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.LLAMA_SPIT.a(world);
				entity.setPositionRotation(x, y, z, yaw, pitch);
			}
		} else if (Minecart.class.isAssignableFrom(clazz)) {
			if (PoweredMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartFurnace(world, x, y, z);
			} else if (StorageMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartChest(world, x, y, z);
			} else if (ExplosiveMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartTNT(world, x, y, z);
			} else if (HopperMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartHopper(world, x, y, z);
			} else if (SpawnerMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartMobSpawner(world, x, y, z);
			} else if (CommandMinecart.class.isAssignableFrom(clazz)) {
				entity = new EntityMinecartCommandBlock(world, x, y, z);
			} else { // Default to rideable minecart for pre-rideable compatibility
				entity = new EntityMinecartRideable(world, x, y, z);
			}
		} else if (EnderSignal.class.isAssignableFrom(clazz)) {
			entity = new EntityEnderSignal(world, x, y, z);
		} else if (EnderCrystal.class.isAssignableFrom(clazz)) {
			entity = EntityTypes.END_CRYSTAL.a(world);
			entity.setPositionRotation(x, y, z, 0, 0);
		} else if (LivingEntity.class.isAssignableFrom(clazz)) {
			if (Chicken.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.CHICKEN.a(world);
			} else if (Cow.class.isAssignableFrom(clazz)) {
				if (MushroomCow.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.MOOSHROOM.a(world);
				} else {
					entity = EntityTypes.COW.a(world);
				}
			} else if (Golem.class.isAssignableFrom(clazz)) {
				if (Snowman.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.SNOW_GOLEM.a(world);
				} else if (IronGolem.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.IRON_GOLEM.a(world);
				} else if (Shulker.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.SHULKER.a(world);
				}
			} else if (Creeper.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.CREEPER.a(world);
			} else if (Ghast.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.GHAST.a(world);
			} else if (Pig.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.PIG.a(world);
			} else if (Player.class.isAssignableFrom(clazz)) {
				// need a net server handler for this one
			} else if (Sheep.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.SHEEP.a(world);
			} else if (AbstractHorse.class.isAssignableFrom(clazz)) {
				if (ChestedHorse.class.isAssignableFrom(clazz)) {
					if (Donkey.class.isAssignableFrom(clazz)) {
						entity = EntityTypes.DONKEY.a(world);
					} else if (Mule.class.isAssignableFrom(clazz)) {
						entity = EntityTypes.MULE.a(world);
					} else if (Llama.class.isAssignableFrom(clazz)) {
						if (TraderLlama.class.isAssignableFrom(clazz)) {
							entity = EntityTypes.TRADER_LLAMA.a(world);
						} else {
							entity = EntityTypes.LLAMA.a(world);
						}
					}
				} else if (SkeletonHorse.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.SKELETON_HORSE.a(world);
				} else if (ZombieHorse.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.ZOMBIE_HORSE.a(world);
				} else {
					entity = EntityTypes.HORSE.a(world);
				}
			} else if (Skeleton.class.isAssignableFrom(clazz)) {
				if (Stray.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.STRAY.a(world);
				} else if (WitherSkeleton.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.WITHER_SKELETON.a(world);
				} else {
					entity = EntityTypes.SKELETON.a(world);
				}
			} else if (Slime.class.isAssignableFrom(clazz)) {
				if (MagmaCube.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.MAGMA_CUBE.a(world);
				} else {
					entity = EntityTypes.SLIME.a(world);
				}
			} else if (Spider.class.isAssignableFrom(clazz)) {
				if (CaveSpider.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.CAVE_SPIDER.a(world);
				} else {
					entity = EntityTypes.SPIDER.a(world);
				}
			} else if (Squid.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.SQUID.a(world);
			} else if (Tameable.class.isAssignableFrom(clazz)) {
				if (Wolf.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.WOLF.a(world);
				} else if (Parrot.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.PARROT.a(world);
				} else if (Cat.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.CAT.a(world);
				}
			} else if (PigZombie.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.ZOMBIE_PIGMAN.a(world);
			} else if (Zombie.class.isAssignableFrom(clazz)) {
				if (Husk.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.HUSK.a(world);
				} else if (ZombieVillager.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.ZOMBIE_VILLAGER.a(world);
				} else if (Drowned.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.DROWNED.a(world);
				} else {
					entity = new EntityZombie(world);
				}
			} else if (Giant.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.GIANT.a(world);
			} else if (Silverfish.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.SILVERFISH.a(world);
			} else if (Enderman.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.ENDERMAN.a(world);
			} else if (Blaze.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.BLAZE.a(world);
			} else if (AbstractVillager.class.isAssignableFrom(clazz)) {
				if (Villager.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.VILLAGER.a(world);
				} else if (WanderingTrader.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.WANDERING_TRADER.a(world);
				}
			} else if (Witch.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.WITCH.a(world);
			} else if (Wither.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.WITHER.a(world);
			} else if (ComplexLivingEntity.class.isAssignableFrom(clazz)) {
				if (EnderDragon.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.ENDER_DRAGON.a(world);
				}
			} else if (Ambient.class.isAssignableFrom(clazz)) {
				if (Bat.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.BAT.a(world);
				}
			} else if (Rabbit.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.RABBIT.a(world);
			} else if (Endermite.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.ENDERMITE.a(world);
			} else if (Guardian.class.isAssignableFrom(clazz)) {
				if (ElderGuardian.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.ELDER_GUARDIAN.a(world);
				} else {
					entity = EntityTypes.GUARDIAN.a(world);
				}
			} else if (ArmorStand.class.isAssignableFrom(clazz)) {
				entity = new EntityArmorStand(world, x, y, z);
			} else if (PolarBear.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.POLAR_BEAR.a(world);
			} else if (Vex.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.VEX.a(world);
			} else if (Illager.class.isAssignableFrom(clazz)) {
				if (Spellcaster.class.isAssignableFrom(clazz)) {
					if (Evoker.class.isAssignableFrom(clazz)) {
						entity = EntityTypes.EVOKER.a(world);
					} else if (Illusioner.class.isAssignableFrom(clazz)) {
						entity = EntityTypes.ILLUSIONER.a(world);
					}
				} else if (Vindicator.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.VINDICATOR.a(world);
				} else if (Pillager.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.PILLAGER.a(world);
				}
			} else if (Turtle.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.TURTLE.a(world);
			} else if (Phantom.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.PHANTOM.a(world);
			} else if (Fish.class.isAssignableFrom(clazz)) {
				if (Cod.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.COD.a(world);
				} else if (PufferFish.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.PUFFERFISH.a(world);
				} else if (Salmon.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.SALMON.a(world);
				} else if (TropicalFish.class.isAssignableFrom(clazz)) {
					entity = EntityTypes.TROPICAL_FISH.a(world);
				}
			} else if (Dolphin.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.DOLPHIN.a(world);
			} else if (Ocelot.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.OCELOT.a(world);
			} else if (Ravager.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.RAVAGER.a(world);
			} else if (Panda.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.PANDA.a(world);
			} else if (Fox.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.FOX.a(world);
			} else if (Bee.class.isAssignableFrom(clazz)) {
				entity = EntityTypes.BEE.a(world);
			}

			if (entity != null) {
				entity.setLocation(x, y, z, yaw, pitch);
				entity.setHeadRotation(yaw); // SPIGOT-3587
			}
		} else if (Hanging.class.isAssignableFrom(clazz)) {
			BlockFace face = BlockFace.SELF;

			int width = 16; // 1 full block, also painting smallest size.
			int height = 16; // 1 full block, also painting smallest size.

			if (ItemFrame.class.isAssignableFrom(clazz)) {
				width = 12;
				height = 12;
			} else if (LeashHitch.class.isAssignableFrom(clazz)) {
				width = 9;
				height = 9;
			}

			// Paper start - In addition to d65a2576e40e58c8e446b330febe6799d13a604f do not
			// check UP/DOWN for non item frames
			// BlockFace[] faces = new BlockFace[]{BlockFace.EAST, BlockFace.NORTH,
			// BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};
			BlockFace[] faces = (ItemFrame.class.isAssignableFrom(clazz))
					? new BlockFace[] { BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP,
							BlockFace.DOWN }
					: new BlockFace[] { BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH };
			// Paper end
			final BlockPosition pos = new BlockPosition(x, y, z);
			for (BlockFace dir : faces) {
				IBlockData nmsBlock = world.getType(pos.shift(CraftBlock.blockFaceToNotch(dir)));
				if (nmsBlock.getMaterial().isBuildable() || BlockDiodeAbstract.isDiode(nmsBlock)) {
					boolean taken = false;
					AxisAlignedBB bb = (ItemFrame.class.isAssignableFrom(clazz))
							? EntityItemFrame.calculateBoundingBox(null, pos,
									CraftBlock.blockFaceToNotch(dir).opposite(), width, height)
							: EntityHanging.calculateBoundingBox(null, pos, CraftBlock.blockFaceToNotch(dir).opposite(),
									width, height);
					List<net.minecraft.server.Entity> list = (List<net.minecraft.server.Entity>) world.getEntities(null,
							bb);
					for (Iterator<net.minecraft.server.Entity> it = list.iterator(); !taken && it.hasNext();) {
						net.minecraft.server.Entity e = it.next();
						if (e instanceof EntityHanging) {
							taken = true; // Hanging entities do not like hanging entities which intersect them.
						}
					}

					if (!taken) {
						face = dir;
						break;
					}
				}
			}

			if (LeashHitch.class.isAssignableFrom(clazz)) {
				entity = new EntityLeash(world, new BlockPosition(x, y, z));
				entity.attachedToPlayer = true;
			} else {
				// No valid face found
				Preconditions.checkArgument(face != BlockFace.SELF,
						"Cannot spawn hanging entity for %s at %s (no free face)", clazz.getName(), location);

				EnumDirection dir = CraftBlock.blockFaceToNotch(face).opposite();
				if (Painting.class.isAssignableFrom(clazz)) {
					entity = new EntityPainting(world, new BlockPosition(x, y, z), dir);
				} else if (ItemFrame.class.isAssignableFrom(clazz)) {
					entity = new EntityItemFrame(world, new BlockPosition(x, y, z), dir);
				}
			}

			if (entity != null && !((EntityHanging) entity).survives()) {
				throw new IllegalArgumentException(
						"Cannot spawn hanging entity for " + clazz.getName() + " at " + location);
			}
		} else if (TNTPrimed.class.isAssignableFrom(clazz)) {
			entity = new EntityTNTPrimed(world, x, y, z, null);
		} else if (ExperienceOrb.class.isAssignableFrom(clazz)) {
			entity = new EntityExperienceOrb(world, x, y, z, 0, org.bukkit.entity.ExperienceOrb.SpawnReason.CUSTOM,
					null, null); // Paper
		} else if (LightningStrike.class.isAssignableFrom(clazz)) {
			entity = new EntityLightning(world, x, y, z, false);
		} else if (Firework.class.isAssignableFrom(clazz)) {
			entity = new EntityFireworks(world, x, y, z, net.minecraft.server.ItemStack.a);
		} else if (AreaEffectCloud.class.isAssignableFrom(clazz)) {
			entity = new EntityAreaEffectCloud(world, x, y, z);
		} else if (EvokerFangs.class.isAssignableFrom(clazz)) {
			entity = new EntityEvokerFangs(world, x, y, z, (float) Math.toRadians(yaw), 0, null);
		}

		if (entity != null) {
			return entity;
		}

		throw new IllegalArgumentException("Cannot spawn an entity for " + clazz.getName());
	}

	public CraftRPGEntity spawnCustom(Location loc, RPGEntityData data) {
		EntityCustomMonster baseEntity = new EntityCustomMonster(EntityTypes.CUSTOM_MONSTER, world, data);
		baseEntity.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw());
		CraftRPGEntity cbEntity = new CraftRPGEntity(server, baseEntity);
		world.addEntity(baseEntity);
		return cbEntity;
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> T addEntity(net.minecraft.server.Entity entity, SpawnReason reason)
			throws IllegalArgumentException {
		return addEntity(entity, reason);
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> T addEntity(net.minecraft.server.Entity entity, SpawnReason reason, Consumer<T> function)
			throws IllegalArgumentException {
		System.out.println("INSIDE ADD ENTITY");
		Preconditions.checkArgument(entity != null, "Cannot spawn null entity");
		System.out.println("After check argument");
		if (entity instanceof EntityInsentient) {
			System.out.println("INSENTIENT");
			((EntityInsentient) entity).prepare(getHandle(), getHandle().getDamageScaler(new BlockPosition(entity)),
					EnumMobSpawn.COMMAND, (GroupDataEntity) null, null);
		}
		System.out.println("BEFORE FUNCTION");
		if (function != null) {
			System.out.println("FUNCTION ACCEPT");
			function.accept((T) entity.getBukkitEntity());
			System.out.println("FUNCTION END");
		}
		System.out.println("WORLD DOT ADD ENTITY");
		world.addEntity(entity, reason);
		System.out.println("Done adding");
		return (T) entity.getBukkitEntity();
	}

	public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<T> function, SpawnReason reason)
			throws IllegalArgumentException {
		net.minecraft.server.Entity entity = createEntity(location, clazz);
		return addEntity(entity, reason, function);
	}

	@Override
	public ChunkSnapshot getEmptyChunkSnapshot(int x, int z, boolean includeBiome, boolean includeBiomeTempRain) {
		return CraftChunk.getEmptyChunkSnapshot(x, z, this, includeBiome, includeBiomeTempRain);
	}

	@Override
	public void setSpawnFlags(boolean allowMonsters, boolean allowAnimals) {
		world.setSpawnFlags(allowMonsters, allowAnimals);
	}

	@Override
	public boolean getAllowAnimals() {
		return world.getChunkProvider().allowAnimals;
	}

	@Override
	public boolean getAllowMonsters() {
		return world.getChunkProvider().allowMonsters;
	}

	@Override
	public int getMaxHeight() {
		return world.getBuildHeight();
	}

	@Override
	public int getSeaLevel() {
		return world.getSeaLevel();
	}

	@Override
	public boolean getKeepSpawnInMemory() {
		return world.keepSpawnInMemory;
	}

	@Override
	public void setKeepSpawnInMemory(boolean keepLoaded) {
		// Paper start - Configurable spawn radius
		if (keepLoaded == world.keepSpawnInMemory) {
			// do nothing, nothing has changed
			return;
		}
		world.keepSpawnInMemory = keepLoaded;
		// Grab the worlds spawn chunk
		BlockPosition prevSpawn = this.world.getSpawn();
		if (keepLoaded) {
			world.addTicketsForSpawn(world.paperConfig.keepLoadedRange, prevSpawn);
		} else {
			// TODO: doesn't work well if spawn changed.... // paper - resolved
			world.removeTicketsForSpawn(world.paperConfig.keepLoadedRange, prevSpawn);
		}
		// Paper end
	}

	@Override
	public int hashCode() {
		return getUID().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		final CraftWorld other = (CraftWorld) obj;

		return this.getUID() == other.getUID();
	}

	@Override
	public File getWorldFolder() {
		return world.getDataManager().getDirectory();
	}

	@Override
	public void sendPluginMessage(Plugin source, String channel, byte[] message) {
		StandardMessenger.validatePluginMessage(server.getMessenger(), source, channel, message);

		for (Player player : getPlayers()) {
			player.sendPluginMessage(source, channel, message);
		}
	}

	@Override
	public Set<String> getListeningPluginChannels() {
		Set<String> result = new HashSet<String>();

		for (Player player : getPlayers()) {
			result.addAll(player.getListeningPluginChannels());
		}

		return result;
	}

	@Override
	public org.bukkit.WorldType getWorldType() {
		return org.bukkit.WorldType.getByName(world.getWorldData().getType().name());
	}

	@Override
	public boolean canGenerateStructures() {
		return world.getWorldData().shouldGenerateMapFeatures();
	}

	@Override
	public boolean isHardcore() {
		return world.getWorldData().isHardcore();
	}

	@Override
	public void setHardcore(boolean hardcore) {
		world.getWorldData().setHardcore(hardcore);
	}

	@Override
	public long getTicksPerAnimalSpawns() {
		return world.ticksPerAnimalSpawns;
	}

	@Override
	public void setTicksPerAnimalSpawns(int ticksPerAnimalSpawns) {
		world.ticksPerAnimalSpawns = ticksPerAnimalSpawns;
	}

	@Override
	public long getTicksPerMonsterSpawns() {
		return world.ticksPerMonsterSpawns;
	}

	@Override
	public void setTicksPerMonsterSpawns(int ticksPerMonsterSpawns) {
		world.ticksPerMonsterSpawns = ticksPerMonsterSpawns;
	}

	@Override
	public long getTicksPerWaterSpawns() {
		return world.ticksPerWaterSpawns;
	}

	@Override
	public void setTicksPerWaterSpawns(int ticksPerWaterSpawns) {
		world.ticksPerWaterSpawns = ticksPerWaterSpawns;
	}

	@Override
	public long getTicksPerAmbientSpawns() {
		return world.ticksPerAmbientSpawns;
	}

	@Override
	public void setTicksPerAmbientSpawns(int ticksPerAmbientSpawns) {
		world.ticksPerAmbientSpawns = ticksPerAmbientSpawns;
	}

	@Override
	public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
		server.getWorldMetadata().setMetadata(this, metadataKey, newMetadataValue);
	}

	@Override
	public List<MetadataValue> getMetadata(String metadataKey) {
		return server.getWorldMetadata().getMetadata(this, metadataKey);
	}

	@Override
	public boolean hasMetadata(String metadataKey) {
		return server.getWorldMetadata().hasMetadata(this, metadataKey);
	}

	@Override
	public void removeMetadata(String metadataKey, Plugin owningPlugin) {
		server.getWorldMetadata().removeMetadata(this, metadataKey, owningPlugin);
	}

	@Override
	public int getMonsterSpawnLimit() {
		if (monsterSpawn < 0) {
			return server.getMonsterSpawnLimit();
		}

		return monsterSpawn;
	}

	@Override
	public void setMonsterSpawnLimit(int limit) {
		monsterSpawn = limit;
	}

	@Override
	public int getAnimalSpawnLimit() {
		if (animalSpawn < 0) {
			return server.getAnimalSpawnLimit();
		}

		return animalSpawn;
	}

	@Override
	public void setAnimalSpawnLimit(int limit) {
		animalSpawn = limit;
	}

	@Override
	public int getWaterAnimalSpawnLimit() {
		if (waterAnimalSpawn < 0) {
			return server.getWaterAnimalSpawnLimit();
		}

		return waterAnimalSpawn;
	}

	@Override
	public void setWaterAnimalSpawnLimit(int limit) {
		waterAnimalSpawn = limit;
	}

	@Override
	public int getAmbientSpawnLimit() {
		if (ambientSpawn < 0) {
			return server.getAmbientSpawnLimit();
		}

		return ambientSpawn;
	}

	@Override
	public void setAmbientSpawnLimit(int limit) {
		ambientSpawn = limit;
	}

	@Override
	public void playSound(Location loc, Sound sound, float volume, float pitch) {
		playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
	}

	@Override
	public void playSound(Location loc, String sound, float volume, float pitch) {
		playSound(loc, sound, org.bukkit.SoundCategory.MASTER, volume, pitch);
	}

	@Override
	public void playSound(Location loc, Sound sound, org.bukkit.SoundCategory category, float volume, float pitch) {
		if (loc == null || sound == null || category == null)
			return;

		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();

		getHandle().playSound(null, x, y, z, CraftSound.getSoundEffect(CraftSound.getSound(sound)),
				SoundCategory.valueOf(category.name()), volume, pitch);
	}

	@Override
	public void playSound(Location loc, String sound, org.bukkit.SoundCategory category, float volume, float pitch) {
		if (loc == null || sound == null || category == null)
			return;

		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();

		PacketPlayOutCustomSoundEffect packet = new PacketPlayOutCustomSoundEffect(new MinecraftKey(sound),
				SoundCategory.valueOf(category.name()), new Vec3D(x, y, z), volume, pitch);
		world.getMinecraftServer().getPlayerList().sendPacketNearby(null, x, y, z,
				volume > 1.0F ? 16.0F * volume : 16.0D, this.world, packet); // Paper - this.world.dimension ->
																				// this.world
	}

	private static Map<String, GameRules.GameRuleKey<?>> gamerules;

	public static synchronized Map<String, GameRules.GameRuleKey<?>> getGameRulesNMS() {
		if (gamerules != null) {
			return gamerules;
		}

		Map<String, GameRules.GameRuleKey<?>> gamerules = new HashMap<>();
		GameRules.a(new GameRules.GameRuleVisitor() {
			@Override
			public <T extends GameRules.GameRuleValue<T>> void a(GameRules.GameRuleKey<T> gamerules_gamerulekey,
					GameRules.GameRuleDefinition<T> gamerules_gameruledefinition) {
				gamerules.put(gamerules_gamerulekey.a(), gamerules_gamerulekey);
			}
		});

		return CraftWorld.gamerules = gamerules;
	}

	private static Map<String, GameRules.GameRuleDefinition<?>> gameruleDefinitions;

	public static synchronized Map<String, GameRules.GameRuleDefinition<?>> getGameRuleDefinitions() {
		if (gameruleDefinitions != null) {
			return gameruleDefinitions;
		}

		Map<String, GameRules.GameRuleDefinition<?>> gameruleDefinitions = new HashMap<>();
		GameRules.a(new GameRules.GameRuleVisitor() {
			@Override
			public <T extends GameRules.GameRuleValue<T>> void a(GameRules.GameRuleKey<T> gamerules_gamerulekey,
					GameRules.GameRuleDefinition<T> gamerules_gameruledefinition) {
				gameruleDefinitions.put(gamerules_gamerulekey.a(), gamerules_gameruledefinition);
			}
		});

		return CraftWorld.gameruleDefinitions = gameruleDefinitions;
	}

	@Override
	public String getGameRuleValue(String rule) {
		// In method contract for some reason
		if (rule == null) {
			return null;
		}

		GameRules.GameRuleValue<?> value = getHandle().getGameRules().get(getGameRulesNMS().get(rule));
		return value != null ? value.toString() : "";
	}

	@Override
	public boolean setGameRuleValue(String rule, String value) {
		// No null values allowed
		if (rule == null || value == null)
			return false;

		if (!isGameRule(rule))
			return false;

		GameRules.GameRuleValue<?> handle = getHandle().getGameRules().get(getGameRulesNMS().get(rule));
		handle.setValue(value);
		handle.onChange(getHandle().getMinecraftServer());
		return true;
	}

	@Override
	public String[] getGameRules() {
		return getGameRulesNMS().keySet().toArray(new String[getGameRulesNMS().size()]);
	}

	@Override
	public boolean isGameRule(String rule) {
		Validate.isTrue(rule != null && !rule.isEmpty(), "Rule cannot be null nor empty");
		return getGameRulesNMS().containsKey(rule);
	}

	@Override
	public <T> T getGameRuleValue(GameRule<T> rule) {
		Validate.notNull(rule, "GameRule cannot be null");
		return convert(rule, getHandle().getGameRules().get(getGameRulesNMS().get(rule.getName())));
	}

	@Override
	public <T> T getGameRuleDefault(GameRule<T> rule) {
		Validate.notNull(rule, "GameRule cannot be null");
		return convert(rule, getGameRuleDefinitions().get(rule.getName()).getValue());
	}

	@Override
	public <T> boolean setGameRule(GameRule<T> rule, T newValue) {
		Validate.notNull(rule, "GameRule cannot be null");
		Validate.notNull(newValue, "GameRule value cannot be null");

		if (!isGameRule(rule.getName()))
			return false;

		GameRules.GameRuleValue<?> handle = getHandle().getGameRules().get(getGameRulesNMS().get(rule.getName()));
		handle.setValue(newValue.toString());
		handle.onChange(getHandle().getMinecraftServer());
		return true;
	}

	private <T> T convert(GameRule<T> rule, GameRules.GameRuleValue<?> value) {
		if (value == null) {
			return null;
		}

		if (value instanceof GameRules.GameRuleBoolean) {
			return rule.getType().cast(((GameRules.GameRuleBoolean) value).a());
		} else if (value instanceof GameRules.GameRuleInt) {
			return rule.getType().cast(value.getIntValue());
		} else {
			throw new IllegalArgumentException("Invalid GameRule type (" + value + ") for GameRule " + rule.getName());
		}
	}

	@Override
	public WorldBorder getWorldBorder() {
		if (this.worldBorder == null) {
			this.worldBorder = new CraftWorldBorder(this);
		}

		return this.worldBorder;
	}

	@Override
	public void spawnParticle(Particle particle, Location location, int count) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count);
	}

	@Override
	public void spawnParticle(Particle particle, double x, double y, double z, int count) {
		spawnParticle(particle, x, y, z, count, null);
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, T data) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, data);
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, T data) {
		spawnParticle(particle, x, y, z, count, 0, 0, 0, data);
	}

	@Override
	public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
			double offsetZ) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ);
	}

	@Override
	public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
			double offsetY, double offsetZ) {
		spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, null);
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
			double offsetZ, T data) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ,
				data);
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
			double offsetY, double offsetZ, T data) {
		spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, 1, data);
	}

	@Override
	public void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
			double offsetZ, double extra) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ,
				extra);
	}

	@Override
	public void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
			double offsetY, double offsetZ, double extra) {
		spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, null);
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
			double offsetZ, double extra, T data) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ,
				extra, data);
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
			double offsetY, double offsetZ, double extra, T data) {
		spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, false);
	}

	@Override
	public <T> void spawnParticle(Particle particle, Location location, int count, double offsetX, double offsetY,
			double offsetZ, double extra, T data, boolean force) {
		spawnParticle(particle, location.getX(), location.getY(), location.getZ(), count, offsetX, offsetY, offsetZ,
				extra, data, force);
	}

	@Override
	public <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX,
			double offsetY, double offsetZ, double extra, T data, boolean force) {
		// Paper start - Particle API Expansion
		spawnParticle(particle, null, null, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, force);
	}

	public <T> void spawnParticle(Particle particle, List<Player> receivers, Player sender, double x, double y,
			double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data, boolean force) {
		// Paper end
		if (data != null && !particle.getDataType().isInstance(data)) {
			throw new IllegalArgumentException("data should be " + particle.getDataType() + " got " + data.getClass());
		}
		getHandle().sendParticles(
				receivers == null ? getHandle().players
						: receivers.stream().map(player -> ((CraftPlayer) player).getHandle())
								.collect(java.util.stream.Collectors.toList()), // Paper - Particle API Expansion
				sender != null ? ((CraftPlayer) sender).getHandle() : null, // Sender // Paper - Particle API Expansion
				CraftParticle.toNMS(particle, data), // Particle
				x, y, z, // Position
				count, // Count
				offsetX, offsetY, offsetZ, // Random offset
				extra, // Speed?
				force);

	}

	@Override
	public Location locateNearestStructure(Location origin, StructureType structureType, int radius,
			boolean findUnexplored) {
		BlockPosition originPos = new BlockPosition(origin.getX(), origin.getY(), origin.getZ());
		BlockPosition nearest = getHandle().getChunkProvider().getChunkGenerator().findNearestMapFeature(getHandle(),
				structureType.getName(), originPos, radius, findUnexplored);
		return (nearest == null) ? null : new Location(this, nearest.getX(), nearest.getY(), nearest.getZ());
	}

	@Override
	public Raid locateNearestRaid(Location location, int radius) {
		Validate.notNull(location, "Location cannot be null");
		Validate.isTrue(radius >= 0, "Radius cannot be negative");

		PersistentRaid persistentRaid = world.getPersistentRaid();
		net.minecraft.server.Raid raid = persistentRaid.getNearbyRaid(
				new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), radius * radius);
		return (raid == null) ? null : new CraftRaid(raid);
	}

	@Override
	public List<Raid> getRaids() {
		PersistentRaid persistentRaid = world.getPersistentRaid();
		return persistentRaid.raids.values().stream().map(CraftRaid::new).collect(Collectors.toList());
	}

	@Override
	public DragonBattle getEnderDragonBattle() {
		WorldProvider worldProvider = getHandle().worldProvider;
		if (!(worldProvider instanceof WorldProviderTheEnd)) {
			return null;
		}

		return new CraftDragonBattle(((WorldProviderTheEnd) worldProvider).o()); // PAIL rename getDragonBattle
	}

	// Paper start
	@Override
	public CompletableFuture<Chunk> getChunkAtAsync(int x, int z, boolean gen, boolean urgent) {
		if (Bukkit.isPrimaryThread()) {
			net.minecraft.server.Chunk immediate = this.world.getChunkProvider().getChunkAtIfLoadedImmediately(x, z);
			if (immediate != null) {
				return CompletableFuture.completedFuture(immediate.getBukkitChunk());
			}
		} else {
			CompletableFuture<Chunk> future = new CompletableFuture<Chunk>();
			world.getMinecraftServer().execute(() -> {
				getChunkAtAsync(x, z, gen, urgent).whenComplete((chunk, err) -> {
					if (err != null) {
						future.completeExceptionally(err);
					} else {
						future.complete(chunk);
					}
				});
			});
			return future;
		}

		if (!urgent) {
			// if not urgent, at least use a slightly boosted priority
			world.getChunkProvider().markHighPriority(new ChunkCoordIntPair(x, z), 1);
		}
		return this.world.getChunkProvider().getChunkAtAsynchronously(x, z, gen, urgent).thenComposeAsync((either) -> {
			net.minecraft.server.Chunk chunk = (net.minecraft.server.Chunk) either.left().orElse(null);
			if (chunk != null)
				addTicket(x, z); // Paper
			return CompletableFuture.completedFuture(chunk == null ? null : chunk.getBukkitChunk());
		}, MinecraftServer.getServer());
	}
	// Paper end

	// Spigot start
	@Override
	public int getViewDistance() {
		return getHandle().getChunkProvider().playerChunkMap.getEffectiveViewDistance(); // Paper - no-tick view
																							// distance
	}
	// Spigot end

	// Paper start - per player view distance
	@Override
	public void setViewDistance(int viewDistance) {
		if (viewDistance < 2 || viewDistance > 32) {
			throw new IllegalArgumentException("View distance " + viewDistance + " is out of range of [2, 32]");
		}
		net.minecraft.server.PlayerChunkMap chunkMap = getHandle().getChunkProvider().playerChunkMap;
		if (viewDistance != chunkMap.getEffectiveViewDistance()) {
			chunkMap.setViewDistance(viewDistance);
		}
	}

	@Override
	public int getNoTickViewDistance() {
		return getHandle().getChunkProvider().playerChunkMap.getEffectiveNoTickViewDistance();
	}

	@Override
	public void setNoTickViewDistance(int viewDistance) {
		if ((viewDistance < 2 || viewDistance > 32) && viewDistance != -1) {
			throw new IllegalArgumentException("View distance " + viewDistance + " is out of range of [2, 32]");
		}
		net.minecraft.server.PlayerChunkMap chunkMap = getHandle().getChunkProvider().playerChunkMap;
		if (viewDistance != chunkMap.getRawNoTickViewDistance()) {
			chunkMap.setNoTickViewDistance(viewDistance);
		}
	}
	// Paper end - per player view distance

	// Spigot start
	private final Spigot spigot = new Spigot() {

		@Override
		public LightningStrike strikeLightning(Location loc, boolean isSilent) {
			EntityLightning lightning = new EntityLightning(world, loc.getX(), loc.getY(), loc.getZ(), false, isSilent);
			world.strikeLightning(lightning);
			return new CraftLightningStrike(server, lightning);
		}

		@Override
		public LightningStrike strikeLightningEffect(Location loc, boolean isSilent) {
			EntityLightning lightning = new EntityLightning(world, loc.getX(), loc.getY(), loc.getZ(), true, isSilent);
			world.strikeLightning(lightning);
			return new CraftLightningStrike(server, lightning);
		}
	};

	public Spigot spigot() {
		return spigot;
	}
	// Spigot end
}
