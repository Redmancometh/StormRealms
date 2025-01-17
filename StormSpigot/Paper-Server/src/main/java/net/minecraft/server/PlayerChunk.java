package net.minecraft.server;

import com.mojang.datafixers.util.Either;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class PlayerChunk {

    public static final Either<IChunkAccess, PlayerChunk.Failure> UNLOADED_CHUNK_ACCESS = Either.right(PlayerChunk.Failure.b);
    public static final CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> UNLOADED_CHUNK_ACCESS_FUTURE = CompletableFuture.completedFuture(PlayerChunk.UNLOADED_CHUNK_ACCESS);
    public static final Either<Chunk, PlayerChunk.Failure> UNLOADED_CHUNK = Either.right(PlayerChunk.Failure.b);
    private static final CompletableFuture<Either<Chunk, PlayerChunk.Failure>> UNLOADED_CHUNK_FUTURE = CompletableFuture.completedFuture(PlayerChunk.UNLOADED_CHUNK);
    private static final List<ChunkStatus> CHUNK_STATUSES = ChunkStatus.a();
    private static final PlayerChunk.State[] CHUNK_STATES = PlayerChunk.State.values();
    boolean isUpdateQueued = false; // Paper
    private final AtomicReferenceArray<CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>>> statusFutures;
    private volatile CompletableFuture<Either<Chunk, PlayerChunk.Failure>> fullChunkFuture; private int fullChunkCreateCount; private volatile boolean isFullChunkReady; // Paper - cache chunk ticking stage
    private volatile CompletableFuture<Either<Chunk, PlayerChunk.Failure>> tickingFuture; private volatile boolean isTickingReady; // Paper - cache chunk ticking stage
    private volatile CompletableFuture<Either<Chunk, PlayerChunk.Failure>> entityTickingFuture; private volatile boolean isEntityTickingReady; // Paper - cache chunk ticking stage
    private CompletableFuture<IChunkAccess> chunkSave;
    public int oldTicketLevel;
    private int ticketLevel;
    volatile int n; public final int getCurrentPriority() { return n; } // Paper - OBFHELPER - make volatile since this is concurrently accessed
    public final ChunkCoordIntPair location; // Paper - private -> public
    private final short[] dirtyBlocks;
    private int dirtyCount;
    private int r;
    private int s;
    private int t;
    private int u;
    private final LightEngine lightEngine;
    private final PlayerChunk.c w;
    public final PlayerChunk.d players;
    private boolean hasBeenLoaded;

    private final PlayerChunkMap chunkMap; // Paper
    public WorldServer getWorld() { return chunkMap.world; } // Paper

    long lastAutoSaveTime; // Paper - incremental autosave
    long inactiveTimeStart; // Paper - incremental autosave

    // Paper start - optimise isOutsideOfRange
    // cached here to avoid a map lookup
    com.destroystokyo.paper.util.misc.PooledLinkedHashSets.PooledObjectLinkedOpenHashSet<EntityPlayer> playersInMobSpawnRange;
    com.destroystokyo.paper.util.misc.PooledLinkedHashSets.PooledObjectLinkedOpenHashSet<EntityPlayer> playersInChunkTickRange;

    void updateRanges() {
        long key = net.minecraft.server.MCUtil.getCoordinateKey(this.location);
        this.playersInMobSpawnRange = this.chunkMap.playerMobSpawnMap.getObjectsInRange(key);
        this.playersInChunkTickRange = this.chunkMap.playerChunkTickRangeMap.getObjectsInRange(key);
    }
    // Paper end - optimise isOutsideOfRange
    // Paper start - optimize chunk status progression without jumping through thread pool
    public boolean canAdvanceStatus() {
        ChunkStatus status = getChunkHolderStatus();
        IChunkAccess chunk = getAvailableChunkNow();
        return chunk != null && (status == null || chunk.getChunkStatus().isAtLeastStatus(getNextStatus(status)));
    }
    // Paper end

    // Paper start - no-tick view distance
    public final Chunk getSendingChunk() {
        // it's important that we use getChunkAtIfLoadedImmediately to mirror the chunk sending logic used
        // in Chunk's neighbour callback
        Chunk ret = this.chunkMap.world.getChunkProvider().getChunkAtIfLoadedImmediately(this.location.x, this.location.z);
        if (ret != null && ret.areNeighboursLoaded(1)) {
            return ret;
        }
        return null;
    }
    // Paper end - no-tick view distance
    // Paper start - Chunk gen/load priority system
    volatile int neighborPriority = -1;
    volatile int priorityBoost = 0;
    public final java.util.concurrent.ConcurrentHashMap<PlayerChunk, ChunkStatus> neighbors = new java.util.concurrent.ConcurrentHashMap<>();
    public final it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<Integer> neighborPriorities = new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<>();

    private int getDemandedPriority() {
        int priority = neighborPriority; // if we have a neighbor priority, use it
        int myPriority = getMyPriority();

        if (priority == -1 || (ticketLevel <= 33 && priority > myPriority)) {
            priority = myPriority;
        }

        return Math.max(1, Math.min(Math.max(ticketLevel, PlayerChunkMap.GOLDEN_TICKET), priority));
    }

    private int getMyPriority() {
        if (priorityBoost == ChunkMapDistance.URGENT_PRIORITY) {
            return 2; // Urgent - ticket level isn't always 31 so 33-30 = 3, but allow 1 more tasks to go below this for dependents
        }
        return ticketLevel - priorityBoost;
    }

    private int getNeighborsPriority() {
        return neighborPriorities.isEmpty() ? getMyPriority() : getDemandedPriority();
    }

    public void onNeighborRequest(PlayerChunk neighbor, ChunkStatus status) {
        neighbor.setNeighborPriority(this, getNeighborsPriority());
        this.neighbors.compute(neighbor, (playerChunk, currentWantedStatus) -> {
            if (currentWantedStatus == null || !currentWantedStatus.isAtLeastStatus(status)) {
                //System.out.println(this + " request " + neighbor + " at " + status + " currently " + currentWantedStatus);
                return status;
            } else {
                //System.out.println(this + " requested " + neighbor + " at " + status + " but thats lower than other wanted status " + currentWantedStatus);
                return currentWantedStatus;
            }
        });

    }

    public void onNeighborDone(PlayerChunk neighbor, ChunkStatus chunkstatus, IChunkAccess chunk) {
        this.neighbors.compute(neighbor, (playerChunk, wantedStatus) -> {
            if (wantedStatus != null && chunkstatus.isAtLeastStatus(wantedStatus)) {
                //System.out.println(this + " neighbor done at " + neighbor + " for status " + chunkstatus + " wanted " + wantedStatus);
                neighbor.removeNeighborPriority(this);
                return null;
            } else {
                //System.out.println(this + " neighbor finished our previous request at " + neighbor + " for status " + chunkstatus + " but we now want instead " + wantedStatus);
                return wantedStatus;
            }
        });
    }

    private void removeNeighborPriority(PlayerChunk requester) {
        synchronized (neighborPriorities) {
            neighborPriorities.remove(requester.location.pair());
            recalcNeighborPriority();
        }
        checkPriority();
    }


    private void setNeighborPriority(PlayerChunk requester, int priority) {
        synchronized (neighborPriorities) {
            neighborPriorities.put(requester.location.pair(), Integer.valueOf(priority));
            recalcNeighborPriority();
        }
        checkPriority();
    }

    private void recalcNeighborPriority() {
        neighborPriority = -1;
        if (!neighborPriorities.isEmpty()) {
            synchronized (neighborPriorities) {
                for (Integer neighbor : neighborPriorities.values()) {
                    if (neighbor < neighborPriority || neighborPriority == -1) {
                        neighborPriority = neighbor;
                    }
                }
            }
        }
    }
    private void checkPriority() {
        if (getCurrentPriority() != getDemandedPriority()) this.chunkMap.queueHolderUpdate(this);
    }

    public final double getDistance(EntityPlayer player) {
        return getDistance(player.locX(), player.locZ());
    }
    public final double getDistance(double blockX, double blockZ) {
        int cx = MCUtil.fastFloor(blockX) >> 4;
        int cz = MCUtil.fastFloor(blockZ) >> 4;
        final double x = location.x - cx;
        final double z = location.z - cz;
        return (x * x) + (z * z);
    }

    public final double getDistanceFrom(BlockPosition pos) {
        return getDistance(pos.getX(), pos.getZ());
    }

    @Override
    public String toString() {
        return "PlayerChunk{" +
            "location=" + location +
            ", ticketLevel=" + ticketLevel + "/" + getChunkStatus(this.ticketLevel) +
            ", chunkHolderStatus=" + getChunkHolderStatus() +
            ", neighborPriority=" + getNeighborsPriority() +
            ", priority=(" + ticketLevel + " - " + priorityBoost +" vs N " + neighborPriority + ") = " + getDemandedPriority() + " A " + getCurrentPriority() +
            '}';
    }
    // Paper end

    public PlayerChunk(ChunkCoordIntPair chunkcoordintpair, int i, LightEngine lightengine, PlayerChunk.c playerchunk_c, PlayerChunk.d playerchunk_d) {
        this.statusFutures = new AtomicReferenceArray(PlayerChunk.CHUNK_STATUSES.size());
        this.fullChunkFuture = PlayerChunk.UNLOADED_CHUNK_FUTURE;
        this.tickingFuture = PlayerChunk.UNLOADED_CHUNK_FUTURE;
        this.entityTickingFuture = PlayerChunk.UNLOADED_CHUNK_FUTURE;
        this.chunkSave = CompletableFuture.completedFuture(null); // CraftBukkit - decompile error
        this.dirtyBlocks = new short[64];
        this.location = chunkcoordintpair;
        this.lightEngine = lightengine;
        this.w = playerchunk_c;
        this.players = playerchunk_d;
        this.oldTicketLevel = PlayerChunkMap.GOLDEN_TICKET + 1;
        this.ticketLevel = this.oldTicketLevel;
        this.n = this.oldTicketLevel;
        this.a(i);
        this.chunkMap = (PlayerChunkMap)playerchunk_d; // Paper
        this.updateRanges(); // Paper - optimise isOutsideOfRange
    }

    // Paper start
    @Nullable
    public final Chunk getEntityTickingChunk() {
        CompletableFuture<Either<Chunk, PlayerChunk.Failure>> completablefuture = this.entityTickingFuture;
        Either<Chunk, PlayerChunk.Failure> either = completablefuture.getNow(null);

        return either == null ? null : either.left().orElse(null);
    }

    @Nullable
    public final Chunk getTickingChunk() {
        CompletableFuture<Either<Chunk, PlayerChunk.Failure>> completablefuture = this.tickingFuture;
        Either<Chunk, PlayerChunk.Failure> either = completablefuture.getNow(null);

        return either == null ? null : either.left().orElse(null);
    }

    @Nullable
    public final Chunk getFullReadyChunk() {
        CompletableFuture<Either<Chunk, PlayerChunk.Failure>> completablefuture = this.fullChunkFuture;
        Either<Chunk, PlayerChunk.Failure> either = completablefuture.getNow(null);

        return either == null ? null : either.left().orElse(null);
    }

    public final boolean isEntityTickingReady() {
        return this.isEntityTickingReady;
    }

    public final boolean isTickingReady() {
        return this.isTickingReady;
    }

    public final boolean isFullChunkReady() {
        return this.isFullChunkReady;
    }
    // Paper end

    // CraftBukkit start
    public Chunk getFullChunk() {
        if (!getChunkState(this.oldTicketLevel).isAtLeast(PlayerChunk.State.BORDER)) return null; // note: using oldTicketLevel for isLoaded checks
        CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> statusFuture = this.getStatusFutureUnchecked(ChunkStatus.FULL);
        Either<IChunkAccess, PlayerChunk.Failure> either = (Either<IChunkAccess, PlayerChunk.Failure>) statusFuture.getNow(null);
        return either == null ? null : (Chunk) either.left().orElse(null);
    }
    // CraftBukkit end
    // Paper start - "real" get full chunk immediately
    public Chunk getFullChunkIfCached() {
        // Note: Copied from above without ticket level check
        CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> statusFuture = this.getStatusFutureUnchecked(ChunkStatus.FULL);
        Either<IChunkAccess, PlayerChunk.Failure> either = (Either<IChunkAccess, PlayerChunk.Failure>) statusFuture.getNow(null);
        return either == null ? null : (Chunk) either.left().orElse(null);
    }

    public IChunkAccess getAvailableChunkNow() {
        // TODO can we just getStatusFuture(EMPTY)?
        for (ChunkStatus curr = ChunkStatus.FULL, next = curr.getPreviousStatus(); curr != next; curr = next, next = next.getPreviousStatus()) {
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> future = this.getStatusFutureUnchecked(curr);
            Either<IChunkAccess, PlayerChunk.Failure> either = future.getNow(null);
            if (either == null || !either.left().isPresent()) {
                continue;
            }
            return either.left().get();
        }
        return null;
    }

    public ChunkStatus getChunkHolderStatus() {
        for (ChunkStatus curr = ChunkStatus.FULL, next = curr.getPreviousStatus(); curr != next; curr = next, next = next.getPreviousStatus()) {
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> future = this.getStatusFutureUnchecked(curr);
            Either<IChunkAccess, PlayerChunk.Failure> either = future.getNow(null);
            if (either == null || !either.left().isPresent()) {
                continue;
            }
            return curr;
        }
        return null;
    }
    public static ChunkStatus getNextStatus(ChunkStatus status) {
        if (status == ChunkStatus.FULL) {
            return status;
        }
        return CHUNK_STATUSES.get(status.getStatusIndex() + 1);
    }
    public CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> getStatusFutureUncheckedMain(ChunkStatus chunkstatus) {
        return ensureMain(getStatusFutureUnchecked(chunkstatus));
    }
    public <T> CompletableFuture<T> ensureMain(CompletableFuture<T> future) {
        return future.thenApplyAsync(r -> r, chunkMap.mainInvokingExecutor);
    }
    // Paper end

    public CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> getStatusFutureUnchecked(ChunkStatus chunkstatus) {
        CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = (CompletableFuture) this.statusFutures.get(chunkstatus.c());

        return completablefuture == null ? PlayerChunk.UNLOADED_CHUNK_ACCESS_FUTURE : completablefuture;
    }

    public CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> b(ChunkStatus chunkstatus) {
        return getChunkStatus(this.ticketLevel).b(chunkstatus) ? this.getStatusFutureUnchecked(chunkstatus) : PlayerChunk.UNLOADED_CHUNK_ACCESS_FUTURE;
    }

    public final CompletableFuture<Either<Chunk, PlayerChunk.Failure>> getTickingFuture() { return this.a(); } // Paper - OBFHELPER
    public CompletableFuture<Either<Chunk, PlayerChunk.Failure>> a() {
        return this.tickingFuture;
    }

    public final CompletableFuture<Either<Chunk, PlayerChunk.Failure>> getEntityTickingFuture() { return this.b(); } // Paper - OBFHELPER
    public CompletableFuture<Either<Chunk, PlayerChunk.Failure>> b() {
        return this.entityTickingFuture;
    }

    public final CompletableFuture<Either<Chunk, PlayerChunk.Failure>> getFullChunkFuture() { return this.c(); } // Paper - OBFHELPER
    public CompletableFuture<Either<Chunk, PlayerChunk.Failure>> c() {
        return this.fullChunkFuture;
    }

    @Nullable
    public Chunk getChunk() {
        CompletableFuture<Either<Chunk, PlayerChunk.Failure>> completablefuture = this.a();
        Either<Chunk, PlayerChunk.Failure> either = (Either) completablefuture.getNow(null); // CraftBukkit - decompile error

        return either == null ? null : (Chunk) either.left().orElse(null); // CraftBukkit - decompile error
    }

    @Nullable
    public IChunkAccess f() {
        for (int i = PlayerChunk.CHUNK_STATUSES.size() - 1; i >= 0; --i) {
            ChunkStatus chunkstatus = (ChunkStatus) PlayerChunk.CHUNK_STATUSES.get(i);
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = this.getStatusFutureUnchecked(chunkstatus);

            if (!completablefuture.isCompletedExceptionally()) {
                Optional<IChunkAccess> optional = ((Either) completablefuture.getNow(PlayerChunk.UNLOADED_CHUNK_ACCESS)).left();

                if (optional.isPresent()) {
                    return (IChunkAccess) optional.get();
                }
            }
        }

        return null;
    }

    public CompletableFuture<IChunkAccess> getChunkSave() {
        return this.chunkSave;
    }

    public void a(int i, int j, int k) {
        Chunk chunk = this.getSendingChunk(); // Paper - no-tick view distance

        if (chunk != null) {
            this.r |= 1 << (j >> 4);
            if (this.dirtyCount < 64) {
                short short0 = (short) (i << 12 | k << 8 | j);

                for (int l = 0; l < this.dirtyCount; ++l) {
                    if (this.dirtyBlocks[l] == short0) {
                        return;
                    }
                }

                this.dirtyBlocks[this.dirtyCount++] = short0;
            }

        }
    }

    public void a(EnumSkyBlock enumskyblock, int i) {
        Chunk chunk = this.getSendingChunk(); // Paper - no-tick view distance

        if (chunk != null) {
            chunk.setNeedsSaving(true);
            if (enumskyblock == EnumSkyBlock.SKY) {
                this.u |= 1 << i - -1;
            } else {
                this.t |= 1 << i - -1;
            }

        }
    }

    public void a(Chunk chunk) {
        if (this.dirtyCount != 0 || this.u != 0 || this.t != 0) {
            World world = chunk.getWorld();

            if (this.dirtyCount == 64) {
                this.s = -1;
            }

            int i;
            int j;

            if (this.u != 0 || this.t != 0) {
                this.a(new PacketPlayOutLightUpdate(chunk.getPos(), this.lightEngine, this.u & ~this.s, this.t & ~this.s), true);
                i = this.u & this.s;
                j = this.t & this.s;
                if (i != 0 || j != 0) {
                    this.a(new PacketPlayOutLightUpdate(chunk.getPos(), this.lightEngine, i, j), false);
                }

                this.u = 0;
                this.t = 0;
                this.s &= ~(this.u & this.t);
            }

            int k;

            if (this.dirtyCount == 1) {
                i = (this.dirtyBlocks[0] >> 12 & 15) + this.location.x * 16;
                j = this.dirtyBlocks[0] & 255;
                k = (this.dirtyBlocks[0] >> 8 & 15) + this.location.z * 16;
                BlockPosition blockposition = new BlockPosition(i, j, k);

                this.a(new PacketPlayOutBlockChange(world, blockposition), false);
                if (world.getType(blockposition).getBlock().isTileEntity()) {
                    this.a(world, blockposition);
                }
            } else if (this.dirtyCount == 64) {
                this.a(new PacketPlayOutMapChunk(chunk, this.r), false);
            } else if (this.dirtyCount != 0) {
                this.a(new PacketPlayOutMultiBlockChange(this.dirtyCount, this.dirtyBlocks, chunk), false);

                for (i = 0; i < this.dirtyCount; ++i) {
                    j = (this.dirtyBlocks[i] >> 12 & 15) + this.location.x * 16;
                    k = this.dirtyBlocks[i] & 255;
                    int l = (this.dirtyBlocks[i] >> 8 & 15) + this.location.z * 16;
                    BlockPosition blockposition1 = new BlockPosition(j, k, l);

                    if (world.getType(blockposition1).getBlock().isTileEntity()) {
                        this.a(world, blockposition1);
                    }
                }
            }

            this.dirtyCount = 0;
            this.r = 0;
        }
    }

    private void a(World world, BlockPosition blockposition) {
        TileEntity tileentity = world.getTileEntity(blockposition);

        if (tileentity != null) {
            PacketPlayOutTileEntityData packetplayouttileentitydata = tileentity.getUpdatePacket();

            if (packetplayouttileentitydata != null) {
                this.a(packetplayouttileentitydata, false);
            }
        }

    }

    public void sendPacketToTrackedPlayers(Packet<?> packet, boolean flag) { a(packet, flag); } // Paper - OBFHELPER
    private void a(Packet<?> packet, boolean flag) {
        // Paper start - per player view distance
        // there can be potential desync with player's last mapped section and the view distance map, so use the
        // view distance map here.
        com.destroystokyo.paper.util.misc.PlayerAreaMap viewDistanceMap = this.chunkMap.playerViewDistanceBroadcastMap;
        com.destroystokyo.paper.util.misc.PooledLinkedHashSets.PooledObjectLinkedOpenHashSet<EntityPlayer> players = viewDistanceMap.getObjectsInRange(this.location);
        if (players == null) {
            return;
        }

        if (flag) { // flag -> border only
            Object[] backingSet = players.getBackingSet();
            for (int i = 0, len = backingSet.length; i < len; ++i) {
                Object temp = backingSet[i];
                if (!(temp instanceof EntityPlayer)) {
                    continue;
                }
                EntityPlayer player = (EntityPlayer)temp;

                int viewDistance = viewDistanceMap.getLastViewDistance(player);
                long lastPosition = viewDistanceMap.getLastCoordinate(player);

                int distX = Math.abs(MCUtil.getCoordinateX(lastPosition) - this.location.x);
                int distZ = Math.abs(MCUtil.getCoordinateZ(lastPosition) - this.location.z);

                if (Math.max(distX, distZ) == viewDistance) {
                    player.playerConnection.sendPacket(packet);
                }
            }
        } else {
            Object[] backingSet = players.getBackingSet();
            for (int i = 0, len = backingSet.length; i < len; ++i) {
                Object temp = backingSet[i];
                if (!(temp instanceof EntityPlayer)) {
                    continue;
                }
                EntityPlayer player = (EntityPlayer)temp;
                player.playerConnection.sendPacket(packet);
            }
        }

        return;
        // Paper end - per player view distance
    }

    public CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> a(ChunkStatus chunkstatus, PlayerChunkMap playerchunkmap) {
        int i = chunkstatus.c();
        CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = (CompletableFuture) this.statusFutures.get(i);

        if (completablefuture != null) {
            Either<IChunkAccess, PlayerChunk.Failure> either = (Either) completablefuture.getNow(null); // CraftBukkit - decompile error

            if (either == null || either.left().isPresent()) {
                return completablefuture;
            }
        }

        if (getChunkStatus(this.ticketLevel).b(chunkstatus)) {
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture1 = playerchunkmap.a(this, chunkstatus);

            this.a(completablefuture1);
            this.statusFutures.set(i, completablefuture1);
            return completablefuture1;
        } else {
            return completablefuture == null ? PlayerChunk.UNLOADED_CHUNK_ACCESS_FUTURE : completablefuture;
        }
    }

    private void a(CompletableFuture<? extends Either<? extends IChunkAccess, PlayerChunk.Failure>> completablefuture) {
        this.chunkSave = this.chunkSave.thenCombine(completablefuture, (ichunkaccess, either) -> {
            return (IChunkAccess) either.map((ichunkaccess1) -> {
                return ichunkaccess1;
            }, (playerchunk_failure) -> {
                return ichunkaccess;
            });
        });
    }

    public ChunkCoordIntPair i() {
        return this.location;
    }

    public int getTicketLevel() {
        return this.ticketLevel;
    }

    public int k() {
        return this.n;
    }

    private void setPriority(int i) { d(i); } // Paper - OBFHELPER
    private void d(int i) {
        this.n = i;
    }

    public void a(int i) {
        this.ticketLevel = i;
    }

    protected void a(PlayerChunkMap playerchunkmap) {
        ChunkStatus chunkstatus = getChunkStatus(this.oldTicketLevel);
        ChunkStatus chunkstatus1 = getChunkStatus(this.ticketLevel);
        boolean flag = this.oldTicketLevel <= PlayerChunkMap.GOLDEN_TICKET;
        boolean flag1 = this.ticketLevel <= PlayerChunkMap.GOLDEN_TICKET; // Paper - diff on change: (flag1 = new ticket level is in loadable range)
        PlayerChunk.State playerchunk_state = getChunkState(this.oldTicketLevel);
        PlayerChunk.State playerchunk_state1 = getChunkState(this.ticketLevel);
        // CraftBukkit start
        // ChunkUnloadEvent: Called before the chunk is unloaded: isChunkLoaded is still true and chunk can still be modified by plugins.
        if (playerchunk_state.isAtLeast(PlayerChunk.State.BORDER) && !playerchunk_state1.isAtLeast(PlayerChunk.State.BORDER)) {
            this.getStatusFutureUncheckedMain(ChunkStatus.FULL).thenAccept((either) -> { // Paper - ensure main
                Chunk chunk = (Chunk)either.left().orElse(null);
                if (chunk != null) {
                    playerchunkmap.callbackExecutor.execute(() -> {
                        // Minecraft will apply the chunks tick lists to the world once the chunk got loaded, and then store the tick
                        // lists again inside the chunk once the chunk becomes inaccessible and set the chunk's needsSaving flag.
                        // These actions may however happen deferred, so we manually set the needsSaving flag already here.
                        chunk.setNeedsSaving(true);
                        chunk.unloadCallback();
                    });
                }
            }).exceptionally((throwable) -> {
                // ensure exceptions are printed, by default this is not the case
                MinecraftServer.LOGGER.fatal("Failed to schedule unload callback for chunk " + PlayerChunk.this.location, throwable);
                return null;
            });

            // Run callback right away if the future was already done
            playerchunkmap.callbackExecutor.run();
        }
        // CraftBukkit end
        CompletableFuture completablefuture;

        if (flag) {
            Either<IChunkAccess, PlayerChunk.Failure> either = Either.right(new PlayerChunk.Failure() {
                public String toString() {
                    return "Unloaded ticket level " + PlayerChunk.this.location.toString();
                }
            });

            // Paper start
            if (!flag1) {
                playerchunkmap.world.asyncChunkTaskManager.cancelChunkLoad(this.location.x, this.location.z);
            }
            // Paper end

            for (int i = flag1 ? chunkstatus1.c() + 1 : 0; i <= chunkstatus.c(); ++i) {
                completablefuture = (CompletableFuture) this.statusFutures.get(i);
                if (completablefuture != null) {
                    completablefuture.complete(either);
                } else {
                    this.statusFutures.set(i, CompletableFuture.completedFuture(either));
                }
            }
        }

        boolean flag2 = playerchunk_state.isAtLeast(PlayerChunk.State.BORDER);
        boolean flag3 = playerchunk_state1.isAtLeast(PlayerChunk.State.BORDER);

        boolean prevHasBeenLoaded = this.hasBeenLoaded; // Paper
        this.hasBeenLoaded |= flag3;
        // Paper start - incremental autosave
        if (this.hasBeenLoaded & !prevHasBeenLoaded) {
            long timeSinceAutoSave = this.inactiveTimeStart - this.lastAutoSaveTime;
            if (timeSinceAutoSave < 0) {
                // safest bet is to assume autosave is needed here
                timeSinceAutoSave = this.chunkMap.world.paperConfig.autoSavePeriod;
            }
            this.lastAutoSaveTime = this.chunkMap.world.getTime() - timeSinceAutoSave;
            this.chunkMap.autoSaveQueue.add(this);
        }
        // Paper end
        if (!flag2 && flag3) {
            // Paper start - cache ticking ready status
            int expectCreateCount = ++this.fullChunkCreateCount;
            this.fullChunkFuture = playerchunkmap.b(this); ensureMain(this.fullChunkFuture).thenAccept((either) -> { // Paper - ensure main
                if (either.left().isPresent() && PlayerChunk.this.fullChunkCreateCount == expectCreateCount) {
                    // note: Here is a very good place to add callbacks to logic waiting on this.
                    Chunk fullChunk = either.left().get();
                    PlayerChunk.this.isFullChunkReady = true;
                    fullChunk.playerChunk = PlayerChunk.this;
                    this.chunkMap.chunkDistanceManager.clearPriorityTickets(location);


                }
            });
            // Paper end
            this.a(this.fullChunkFuture);
        }

        if (flag2 && !flag3) {
            completablefuture = this.fullChunkFuture;
            this.fullChunkFuture = PlayerChunk.UNLOADED_CHUNK_FUTURE;
            ++this.fullChunkCreateCount; // Paper - cache ticking ready status
            this.isFullChunkReady = false; // Paper - cache ticking ready status
            this.a(((CompletableFuture<Either<Chunk, PlayerChunk.Failure>>) completablefuture).thenApply((either1) -> { // CraftBukkit - decompile error
                playerchunkmap.getClass();
                return either1.ifLeft(playerchunkmap::a);
            }));
        }

        boolean flag4 = playerchunk_state.isAtLeast(PlayerChunk.State.TICKING);
        boolean flag5 = playerchunk_state1.isAtLeast(PlayerChunk.State.TICKING);

        if (!flag4 && flag5) {
            // Paper start - cache ticking ready status
            this.tickingFuture = playerchunkmap.a(this); ensureMain(this.tickingFuture).thenAccept((either) -> { // Paper - ensure main
                if (either.left().isPresent()) {
                    // note: Here is a very good place to add callbacks to logic waiting on this.
                    Chunk tickingChunk = either.left().get();
                    PlayerChunk.this.isTickingReady = true;


                    // Paper start - rewrite ticklistserver
                    PlayerChunk.this.chunkMap.world.onChunkSetTicking(PlayerChunk.this.location.x, PlayerChunk.this.location.z);
                    // Paper end - rewrite ticklistserver

                }
            });
            // Paper end
            this.a(this.tickingFuture);
        }

        if (flag4 && !flag5) {
            this.tickingFuture.complete(PlayerChunk.UNLOADED_CHUNK); this.isTickingReady = false; // Paper - cache chunk ticking stage
            this.tickingFuture = PlayerChunk.UNLOADED_CHUNK_FUTURE;
        }

        boolean flag6 = playerchunk_state.isAtLeast(PlayerChunk.State.ENTITY_TICKING);
        boolean flag7 = playerchunk_state1.isAtLeast(PlayerChunk.State.ENTITY_TICKING);

        if (!flag6 && flag7) {
            if (this.entityTickingFuture != PlayerChunk.UNLOADED_CHUNK_FUTURE) {
                throw (IllegalStateException) SystemUtils.c(new IllegalStateException());
            }

            // Paper start - cache ticking ready status
            this.entityTickingFuture = playerchunkmap.b(this.location); ensureMain(this.entityTickingFuture).thenAccept((either) -> { // Paper ensureMain
                if (either.left().isPresent()) {
                    // note: Here is a very good place to add callbacks to logic waiting on this.
                    Chunk entityTickingChunk = either.left().get();
                    PlayerChunk.this.isEntityTickingReady = true;




                }
            });
            // Paper end
            this.a(this.entityTickingFuture);
        }

        if (flag6 && !flag7) {
            this.entityTickingFuture.complete(PlayerChunk.UNLOADED_CHUNK); this.isEntityTickingReady = false; // Paper - cache chunk ticking stage
            this.entityTickingFuture = PlayerChunk.UNLOADED_CHUNK_FUTURE;
        }
        // Paper start - raise IO/load priority if priority changes, use our preferred priority
        priorityBoost = chunkMap.chunkDistanceManager.getChunkPriority(location);
        int priority = getDemandedPriority();
        if (getCurrentPriority() > priority) {
            int ioPriority = com.destroystokyo.paper.io.PrioritizedTaskQueue.NORMAL_PRIORITY;
            if (priority <= 10) {
                ioPriority = com.destroystokyo.paper.io.PrioritizedTaskQueue.HIGHEST_PRIORITY;
            } else if (priority <= 20) {
                ioPriority = com.destroystokyo.paper.io.PrioritizedTaskQueue.HIGH_PRIORITY;
            }
            chunkMap.world.asyncChunkTaskManager.raisePriority(location.x, location.z, ioPriority);
            chunkMap.world.getChunkProvider().getLightEngine().changePriority(location.pair(), getCurrentPriority(), priority);
        }
        if (getCurrentPriority() != priority) {
            this.w.a(this.location, this::getCurrentPriority, priority, this::setPriority); // use preferred priority
            int neighborsPriority = getNeighborsPriority();
            this.neighbors.forEach((neighbor, neighborDesired) -> neighbor.setNeighborPriority(this, neighborsPriority));
        }
        // Paper end
        this.oldTicketLevel = this.ticketLevel;
        // CraftBukkit start
        // ChunkLoadEvent: Called after the chunk is loaded: isChunkLoaded returns true and chunk is ready to be modified by plugins.
        if (!playerchunk_state.isAtLeast(PlayerChunk.State.BORDER) && playerchunk_state1.isAtLeast(PlayerChunk.State.BORDER)) {
            this.getStatusFutureUncheckedMain(ChunkStatus.FULL).thenAccept((either) -> { // Paper - ensure main
                Chunk chunk = (Chunk)either.left().orElse(null);
                if (chunk != null) {
                    playerchunkmap.callbackExecutor.execute(() -> {
                        chunk.loadCallback();
                    });
                }
            }).exceptionally((throwable) -> {
                // ensure exceptions are printed, by default this is not the case
                MinecraftServer.LOGGER.fatal("Failed to schedule load callback for chunk " + PlayerChunk.this.location, throwable);
                return null;
            });

            // Run callback right away if the future was already done
            playerchunkmap.callbackExecutor.run();
        }
        // CraftBukkit end
    }

    public static ChunkStatus getChunkStatus(int i) {
        return i < 33 ? ChunkStatus.FULL : ChunkStatus.a(i - 33);
    }

    public static PlayerChunk.State getChunkState(int i) {
        return PlayerChunk.CHUNK_STATES[MathHelper.clamp(33 - i + 1, 0, PlayerChunk.CHUNK_STATES.length - 1)];
    }

    public boolean hasBeenLoaded() {
        return this.hasBeenLoaded;
    }

    public void m() {
        boolean prev = this.hasBeenLoaded; // Paper
        this.hasBeenLoaded = getChunkState(this.ticketLevel).isAtLeast(PlayerChunk.State.BORDER);
        // Paper start - incremental autosave
        if (prev != this.hasBeenLoaded) {
            if (this.hasBeenLoaded) {
                long timeSinceAutoSave = this.inactiveTimeStart - this.lastAutoSaveTime;
                if (timeSinceAutoSave < 0) {
                    // safest bet is to assume autosave is needed here
                    timeSinceAutoSave = this.chunkMap.world.paperConfig.autoSavePeriod;
                }
                this.lastAutoSaveTime = this.chunkMap.world.getTime() - timeSinceAutoSave;
                this.chunkMap.autoSaveQueue.add(this);
            } else {
                this.inactiveTimeStart = this.chunkMap.world.getTime();
                this.chunkMap.autoSaveQueue.remove(this);
            }
        }
        // Paper end
    }

    // Paper start - incremental autosave
    public boolean setHasBeenLoaded() {
        this.hasBeenLoaded = getChunkState(this.ticketLevel).isAtLeast(PlayerChunk.State.BORDER);
        return this.hasBeenLoaded;
    }
    // Paper end

    public void a(ProtoChunkExtension protochunkextension) {
        for (int i = 0; i < this.statusFutures.length(); ++i) {
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = (CompletableFuture) this.statusFutures.get(i);

            if (completablefuture != null) {
                Optional<IChunkAccess> optional = ((Either) completablefuture.getNow(PlayerChunk.UNLOADED_CHUNK_ACCESS)).left();

                if (optional.isPresent() && optional.get() instanceof ProtoChunk) {
                    this.statusFutures.set(i, CompletableFuture.completedFuture(Either.left(protochunkextension)));
                }
            }
        }

        this.a(CompletableFuture.completedFuture(Either.left(protochunkextension.u())));
    }

    public interface d {

        Stream<EntityPlayer> a(ChunkCoordIntPair chunkcoordintpair, boolean flag);
    }

    public interface c {

        default void changePriority(ChunkCoordIntPair chunkcoordintpair, IntSupplier intsupplier, int i, IntConsumer intconsumer) { a(chunkcoordintpair, intsupplier, i, intconsumer); } // Paper - OBFHELPER
        void a(ChunkCoordIntPair chunkcoordintpair, IntSupplier intsupplier, int i, IntConsumer intconsumer);
    }

    public interface Failure {

        PlayerChunk.Failure b = new PlayerChunk.Failure() {
            public String toString() {
                return "UNLOADED";
            }
        };
    }

    public static enum State {

        INACCESSIBLE, BORDER, TICKING, ENTITY_TICKING;

        private State() {}

        public boolean isAtLeast(PlayerChunk.State playerchunk_state) {
            return this.ordinal() >= playerchunk_state.ordinal();
        }
    }
}
