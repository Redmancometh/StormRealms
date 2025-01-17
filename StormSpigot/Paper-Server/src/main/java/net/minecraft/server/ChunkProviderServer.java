package net.minecraft.server;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import com.destroystokyo.paper.exception.ServerInternalException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkProviderServer extends IChunkProvider {

    private static final int b = (int) Math.pow(17.0D, 2.0D);
    private static final List<ChunkStatus> c = ChunkStatus.a(); static final List<ChunkStatus> getPossibleChunkStatuses() { return ChunkProviderServer.c; } // Paper - OBFHELPER
    private final ChunkMapDistance chunkMapDistance;
    public final ChunkGenerator<?> chunkGenerator;
    private final WorldServer world;
    public final Thread serverThread; // Paper - private -> public
    private final LightEngineThreaded lightEngine;
    public final ChunkProviderServer.a serverThreadQueue; // Paper private -> public
    public final PlayerChunkMap playerChunkMap;
    private final WorldPersistentData worldPersistentData;
    private long lastTickTime;
    public boolean allowMonsters = true;
    public boolean allowAnimals = true;
    private final long[] cachePos = new long[4];
    private final ChunkStatus[] cacheStatus = new ChunkStatus[4];
    private final IChunkAccess[] cacheChunk = new IChunkAccess[4];

    // Paper start
    final com.destroystokyo.paper.util.concurrent.WeakSeqLock loadedChunkMapSeqLock = new com.destroystokyo.paper.util.concurrent.WeakSeqLock();
    final it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<Chunk> loadedChunkMap = new it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap<>(8192, 0.5f);

    private final Chunk[] lastLoadedChunks = new Chunk[4 * 4];
    private final long[] lastLoadedChunkKeys = new long[4 * 4];

    {
        java.util.Arrays.fill(this.lastLoadedChunkKeys, MCUtil.INVALID_CHUNK_KEY);
    }

    private static int getCacheKey(int x, int z) {
        return x & 3 | ((z & 3) << 2);
    }

    void addLoadedChunk(Chunk chunk) {
        this.loadedChunkMapSeqLock.acquireWrite();
        try {
            this.loadedChunkMap.put(chunk.coordinateKey, chunk);
        } finally {
            this.loadedChunkMapSeqLock.releaseWrite();
        }

        // rewrite cache if we have to
        // we do this since we also cache null chunks
        int cacheKey = getCacheKey(chunk.getPos().x, chunk.getPos().z);

        long cachedKey = this.lastLoadedChunkKeys[cacheKey];
        if (cachedKey == chunk.coordinateKey) {
            this.lastLoadedChunks[cacheKey] = chunk;
        }
    }

    void removeLoadedChunk(Chunk chunk) {
        this.loadedChunkMapSeqLock.acquireWrite();
        try {
            this.loadedChunkMap.remove(chunk.coordinateKey);
        } finally {
            this.loadedChunkMapSeqLock.releaseWrite();
        }

        // rewrite cache if we have to
        // we do this since we also cache null chunks
        int cacheKey = getCacheKey(chunk.getPos().x, chunk.getPos().z);

        long cachedKey = this.lastLoadedChunkKeys[cacheKey];
        if (cachedKey == chunk.coordinateKey) {
            this.lastLoadedChunks[cacheKey] = null;
        }
    }

    public Chunk getChunkAtIfLoadedMainThread(int x, int z) {
        int cacheKey = getCacheKey(x, z);
        long chunkKey = MCUtil.getCoordinateKey(x, z);

        long cachedKey = this.lastLoadedChunkKeys[cacheKey];
        if (cachedKey == chunkKey) {
            return this.lastLoadedChunks[cacheKey];
        }

        Chunk ret = this.loadedChunkMap.get(chunkKey);

        this.lastLoadedChunkKeys[cacheKey] = chunkKey;
        this.lastLoadedChunks[cacheKey] = ret;

        return ret;
    }

    public Chunk getChunkAtIfLoadedMainThreadNoCache(int x, int z) {
        return this.loadedChunkMap.get(MCUtil.getCoordinateKey(x, z));
    }

    public Chunk getChunkAtMainThread(int x, int z) {
        Chunk ret = this.getChunkAtIfLoadedMainThread(x, z);
        if (ret != null) {
            return ret;
        }
        return (Chunk)this.getChunkAt(x, z, ChunkStatus.FULL, true);
    }

    private long chunkFutureAwaitCounter;

    public void getEntityTickingChunkAsync(int x, int z, java.util.function.Consumer<Chunk> onLoad) {
        if (Thread.currentThread() != this.serverThread) {
            this.serverThreadQueue.execute(() -> {
                ChunkProviderServer.this.getEntityTickingChunkAsync(x, z, onLoad);
            });
            return;
        }
        this.getChunkFutureAsynchronously(x, z, 31, PlayerChunk::getEntityTickingFuture, onLoad);
    }

    public void getTickingChunkAsync(int x, int z, java.util.function.Consumer<Chunk> onLoad) {
        if (Thread.currentThread() != this.serverThread) {
            this.serverThreadQueue.execute(() -> {
                ChunkProviderServer.this.getTickingChunkAsync(x, z, onLoad);
            });
            return;
        }
        this.getChunkFutureAsynchronously(x, z, 32, PlayerChunk::getTickingFuture, onLoad);
    }

    public void getFullChunkAsync(int x, int z, java.util.function.Consumer<Chunk> onLoad) {
        if (Thread.currentThread() != this.serverThread) {
            this.serverThreadQueue.execute(() -> {
                ChunkProviderServer.this.getFullChunkAsync(x, z, onLoad);
            });
            return;
        }
        this.getChunkFutureAsynchronously(x, z, 33, PlayerChunk::getFullChunkFuture, onLoad);
    }

    private void getChunkFutureAsynchronously(int x, int z, int ticketLevel, Function<PlayerChunk, CompletableFuture<Either<Chunk, PlayerChunk.Failure>>> futureGet, java.util.function.Consumer<Chunk> onLoad) {
        if (Thread.currentThread() != this.serverThread) {
            throw new IllegalStateException();
        }
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(x, z);
        Long identifier = Long.valueOf(this.chunkFutureAwaitCounter++);
        this.addTicketAtLevel(TicketType.FUTURE_AWAIT, chunkPos, ticketLevel, identifier);
        this.tickDistanceManager();

        PlayerChunk chunk = this.playerChunkMap.getUpdatingChunk(chunkPos.pair());

        if (chunk == null) {
            throw new IllegalStateException("Expected playerchunk " + chunkPos + " in world '" + this.world.getWorld().getName() + "'");
        }

        CompletableFuture<Either<Chunk, PlayerChunk.Failure>> future = futureGet.apply(chunk);

        future.whenCompleteAsync((either, throwable) -> {
            try {
                if (throwable != null) {
                    if (throwable instanceof ThreadDeath) {
                        throw (ThreadDeath)throwable;
                    }
                    MinecraftServer.LOGGER.fatal("Failed to complete future await for chunk " + chunkPos.toString() + " in world '" + ChunkProviderServer.this.world.getWorld().getName() + "'", throwable);
                } else if (either.right().isPresent()) {
                    MinecraftServer.LOGGER.fatal("Failed to complete future await for chunk " + chunkPos.toString() + " in world '" + ChunkProviderServer.this.world.getWorld().getName() + "': " + either.right().get().toString());
                }

                try {
                    if (onLoad != null) {
                        playerChunkMap.callbackExecutor.execute(() -> {
                            onLoad.accept(either == null ? null : either.left().orElse(null)); // indicate failure to the callback.
                        });
                    }
                } catch (Throwable thr) {
                    if (thr instanceof ThreadDeath) {
                        throw (ThreadDeath)thr;
                    }
                    MinecraftServer.LOGGER.fatal("Load callback for future await failed " + chunkPos.toString() + " in world '" + ChunkProviderServer.this.world.getWorld().getName() + "'", thr);
                    return;
                }
            } finally {
                // due to odd behaviour with CB unload implementation we need to have these AFTER the load callback.
                ChunkProviderServer.this.addTicketAtLevel(TicketType.UNKNOWN, chunkPos, ticketLevel, chunkPos);
                ChunkProviderServer.this.removeTicketAtLevel(TicketType.FUTURE_AWAIT, chunkPos, ticketLevel, identifier);
            }
        }, this.serverThreadQueue);
    }
    // Paper end

    // Paper start - rewrite ticklistserver
    public final boolean isTickingReadyMainThread(BlockPosition pos) {
        PlayerChunk chunk = this.playerChunkMap.getUpdatingChunk(MCUtil.getCoordinateKey(pos));
        return chunk != null && chunk.isTickingReady();
    }
    // Paper end - rewrite ticklistserver


    public ChunkProviderServer(WorldServer worldserver, File file, DataFixer datafixer, DefinedStructureManager definedstructuremanager, Executor executor, ChunkGenerator<?> chunkgenerator, int i, WorldLoadListener worldloadlistener, Supplier<WorldPersistentData> supplier) {
        this.world = worldserver;
        this.serverThreadQueue = new ChunkProviderServer.a(worldserver);
        this.chunkGenerator = chunkgenerator;
        this.serverThread = Thread.currentThread();
        File file1 = worldserver.getWorldProvider().getDimensionManager().a(file);
        File file2 = new File(file1, "data");

        file2.mkdirs();
        this.worldPersistentData = new WorldPersistentData(file2, datafixer);
        this.playerChunkMap = new PlayerChunkMap(worldserver, file, datafixer, definedstructuremanager, executor, this.serverThreadQueue, this, this.getChunkGenerator(), worldloadlistener, supplier, i);
        this.lightEngine = this.playerChunkMap.a();
        this.chunkMapDistance = this.playerChunkMap.e();
        this.clearCache();
    }

    // CraftBukkit start - properly implement isChunkLoaded
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        PlayerChunk chunk = this.playerChunkMap.getUpdatingChunk(ChunkCoordIntPair.pair(chunkX, chunkZ));
        if (chunk == null) {
            return false;
        }
        return chunk.getFullChunk() != null;
    }
    // CraftBukkit end

    @Override
    public LightEngineThreaded getLightEngine() {
        return this.lightEngine;
    }

    @Nullable
    private PlayerChunk getChunk(long i) {
        return this.playerChunkMap.getVisibleChunk(i);
    }

    public int b() {
        return this.playerChunkMap.c();
    }

    private void a(long i, IChunkAccess ichunkaccess, ChunkStatus chunkstatus) {
        for (int j = 3; j > 0; --j) {
            this.cachePos[j] = this.cachePos[j - 1];
            this.cacheStatus[j] = this.cacheStatus[j - 1];
            this.cacheChunk[j] = this.cacheChunk[j - 1];
        }

        this.cachePos[0] = i;
        this.cacheStatus[0] = chunkstatus;
        this.cacheChunk[0] = ichunkaccess;
    }

    // Paper start - "real" get chunk if loaded
    // Note: Partially copied from the getChunkAt method below
    @Nullable
    public Chunk getChunkAtIfCachedImmediately(int x, int z) {
        long k = ChunkCoordIntPair.pair(x, z);

        // Note: Bypass cache since we need to check ticket level, and to make this MT-Safe

        PlayerChunk playerChunk = this.getChunk(k);
        if (playerChunk == null) {
            return null;
        }

        return playerChunk.getFullChunkIfCached();
    }

    @Nullable
    public Chunk getChunkAtIfLoadedImmediately(int x, int z) {
        long k = ChunkCoordIntPair.pair(x, z);

        if (Thread.currentThread() == this.serverThread) {
            return this.getChunkAtIfLoadedMainThread(x, z);
        }

        Chunk ret = null;
        long readlock;
        do {
            readlock = this.loadedChunkMapSeqLock.acquireRead();
            try {
                ret = this.loadedChunkMap.get(k);
            } catch (Throwable thr) {
                if (thr instanceof ThreadDeath) {
                    throw (ThreadDeath)thr;
                }
                // re-try, this means a CME occurred...
                continue;
            }
        } while (!this.loadedChunkMapSeqLock.tryReleaseRead(readlock));

        return ret;
    }

    @Nullable
    public IChunkAccess getChunkAtImmediately(int x, int z) {
        long k = ChunkCoordIntPair.pair(x, z);

        // Note: Bypass cache to make this MT-Safe

        PlayerChunk playerChunk = this.getChunk(k);
        if (playerChunk == null) {
            return null;
        }

        return playerChunk.getAvailableChunkNow();

    }

    private long asyncLoadSeqCounter;

    public CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> getChunkAtAsynchronously(int x, int z, boolean gen, boolean isUrgent) {
        if (Thread.currentThread() != this.serverThread) {
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> future = new CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>>();
            this.serverThreadQueue.execute(() -> {
                this.getChunkAtAsynchronously(x, z, gen, isUrgent).whenComplete((chunk, ex) -> {
                    if (ex != null) {
                        future.completeExceptionally(ex);
                    } else {
                        future.complete(chunk);
                    }
                });
            });
            return future;
        }

        if (!com.destroystokyo.paper.PaperConfig.asyncChunks) {
            world.getWorld().loadChunk(x, z, gen);
            Chunk chunk = getChunkAtIfLoadedMainThread(x, z);
            return CompletableFuture.completedFuture(chunk != null ? Either.left(chunk) : PlayerChunk.UNLOADED_CHUNK_ACCESS);
        }

        long k = ChunkCoordIntPair.pair(x, z);
        ChunkCoordIntPair chunkPos = new ChunkCoordIntPair(x, z);

        IChunkAccess ichunkaccess;

        // try cache
        for (int l = 0; l < 4; ++l) {
            if (k == this.cachePos[l] && ChunkStatus.FULL == this.cacheStatus[l]) {
                ichunkaccess = this.cacheChunk[l];
                if (ichunkaccess != null) { // CraftBukkit - the chunk can become accessible in the meantime TODO for non-null chunks it might also make sense to check that the chunk's state hasn't changed in the meantime

                    // move to first in cache

                    for (int i1 = 3; i1 > 0; --i1) {
                        this.cachePos[i1] = this.cachePos[i1 - 1];
                        this.cacheStatus[i1] = this.cacheStatus[i1 - 1];
                        this.cacheChunk[i1] = this.cacheChunk[i1 - 1];
                    }

                    this.cachePos[0] = k;
                    this.cacheStatus[0] = ChunkStatus.FULL;
                    this.cacheChunk[0] = ichunkaccess;

                    return CompletableFuture.completedFuture(Either.left(ichunkaccess));
                }
            }
        }

        if (gen) {
            return this.bringToFullStatusAsync(x, z, chunkPos, isUrgent);
        }

        IChunkAccess current = this.getChunkAtImmediately(x, z); // we want to bypass ticket restrictions
        if (current != null) {
            if (!(current instanceof ProtoChunkExtension) && !(current instanceof net.minecraft.server.Chunk)) {
                return CompletableFuture.completedFuture(PlayerChunk.UNLOADED_CHUNK_ACCESS);
            }
            // we know the chunk is at full status here (either in read-only mode or the real thing)
            return this.bringToFullStatusAsync(x, z, chunkPos, isUrgent);
        }

        ChunkStatus status = world.getChunkProvider().playerChunkMap.getStatusOnDiskNoLoad(x, z);

        if (status != null && status != ChunkStatus.FULL) {
            // does not exist on disk
            return CompletableFuture.completedFuture(PlayerChunk.UNLOADED_CHUNK_ACCESS);
        }

        if (status == ChunkStatus.FULL) {
            return this.bringToFullStatusAsync(x, z, chunkPos, isUrgent);
        }

        // status is null here

        // here we don't know what status it is and we're not supposed to generate
        // so we asynchronously load empty status
        return this.bringToStatusAsync(x, z, chunkPos, ChunkStatus.EMPTY, isUrgent).thenCompose((either) -> {
            IChunkAccess chunk = either.left().orElse(null);
            if (!(chunk instanceof ProtoChunkExtension) && !(chunk instanceof Chunk)) {
                // the chunk on disk was not a full status chunk
                return CompletableFuture.completedFuture(PlayerChunk.UNLOADED_CHUNK_ACCESS);
            }
            ; // bring to full status if required
            return this.bringToFullStatusAsync(x, z, chunkPos, isUrgent);
        });
    }

    private CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> bringToFullStatusAsync(int x, int z, ChunkCoordIntPair chunkPos, boolean isUrgent) {
        return this.bringToStatusAsync(x, z, chunkPos, ChunkStatus.FULL, isUrgent);
    }

    private CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> bringToStatusAsync(int x, int z, ChunkCoordIntPair chunkPos, ChunkStatus status, boolean isUrgent) {
        CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> future = this.getChunkFutureMainThread(x, z, status, true, isUrgent);
        Long identifier = Long.valueOf(this.asyncLoadSeqCounter++);
        int ticketLevel = MCUtil.getTicketLevelFor(status);
        this.addTicketAtLevel(TicketType.ASYNC_LOAD, chunkPos, ticketLevel, identifier);

        return future.thenComposeAsync((Either<IChunkAccess, PlayerChunk.Failure> either) -> {
            // either left -> success
            // either right -> failure

            this.removeTicketAtLevel(TicketType.ASYNC_LOAD, chunkPos, ticketLevel, identifier);
            this.addTicketAtLevel(TicketType.UNKNOWN, chunkPos, ticketLevel, chunkPos); // allow unloading

            Optional<PlayerChunk.Failure> failure = either.right();

            if (failure.isPresent()) {
                // failure
                throw new IllegalStateException("Chunk failed to load: " + failure.get().toString());
            }

            return CompletableFuture.completedFuture(either);
        }, this.serverThreadQueue);
    }

    public <T> void addTicketAtLevel(TicketType<T> ticketType, ChunkCoordIntPair chunkPos, int ticketLevel, T identifier) {
        this.chunkMapDistance.addTicketAtLevel(ticketType, chunkPos, ticketLevel, identifier);
    }

    public <T> void removeTicketAtLevel(TicketType<T> ticketType, ChunkCoordIntPair chunkPos, int ticketLevel, T identifier) {
        this.chunkMapDistance.removeTicketAtLevel(ticketType, chunkPos, ticketLevel, identifier);
    }

    public boolean markUrgent(ChunkCoordIntPair coords) {
        return this.chunkMapDistance.markUrgent(coords);
    }

    public boolean markHighPriority(ChunkCoordIntPair coords, int priority) {
        return this.chunkMapDistance.markHighPriority(coords, priority);
    }

    public void markAreaHighPriority(ChunkCoordIntPair center, int priority, int radius) {
        this.chunkMapDistance.markAreaHighPriority(center, priority, radius);
    }

    public void clearAreaPriorityTickets(ChunkCoordIntPair center, int radius) {
        this.chunkMapDistance.clearAreaPriorityTickets(center, radius);
    }

    public void clearPriorityTickets(ChunkCoordIntPair coords) {
        this.chunkMapDistance.clearPriorityTickets(coords);
    }
    // Paper end

    @Nullable
    @Override
    public IChunkAccess getChunkAt(int i, int j, ChunkStatus chunkstatus, boolean flag) {
        final int x = i; final int z = j; // Paper - conflict on variable change
        if (Thread.currentThread() != this.serverThread) {
            return (IChunkAccess) CompletableFuture.supplyAsync(() -> {
                return this.getChunkAt(i, j, chunkstatus, flag);
            }, this.serverThreadQueue).join();
        } else {
            // Paper start - optimise for loaded chunks
            Chunk ifLoaded = this.getChunkAtIfLoadedMainThread(i, j);
            if (ifLoaded != null) {
                return ifLoaded;
            }
            // Paper end
            GameProfilerFiller gameprofilerfiller = this.world.getMethodProfiler();

            gameprofilerfiller.c("getChunk");
            long k = ChunkCoordIntPair.pair(i, j);

            IChunkAccess ichunkaccess;

            for (int l = 0; l < 4; ++l) {
                if (k == this.cachePos[l] && chunkstatus == this.cacheStatus[l]) {
                    ichunkaccess = this.cacheChunk[l];
                    if (ichunkaccess != null) { // CraftBukkit - the chunk can become accessible in the meantime TODO for non-null chunks it might also make sense to check that the chunk's state hasn't changed in the meantime
                        return ichunkaccess;
                    }
                }
            }

            gameprofilerfiller.c("getChunkCacheMiss");
            CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> completablefuture = this.getChunkFutureMainThread(i, j, chunkstatus, flag, true); // Paper

            if (!completablefuture.isDone()) { // Paper
                // Paper start - async chunk io/loading
                ChunkCoordIntPair pair = new ChunkCoordIntPair(x, z);
                this.chunkMapDistance.markUrgent(pair);
                this.world.asyncChunkTaskManager.raisePriority(x, z, com.destroystokyo.paper.io.PrioritizedTaskQueue.HIGHEST_PRIORITY);
                com.destroystokyo.paper.io.chunk.ChunkTaskManager.pushChunkWait(this.world, x, z);
                // Paper end
                com.destroystokyo.paper.io.SyncLoadFinder.logSyncLoad(this.world, x, z); // Paper - sync load info
                this.world.timings.syncChunkLoad.startTiming(); // Paper
            this.serverThreadQueue.awaitTasks(completablefuture::isDone);
                com.destroystokyo.paper.io.chunk.ChunkTaskManager.popChunkWait(); // Paper - async chunk debug
                this.world.timings.syncChunkLoad.stopTiming(); // Paper
                this.chunkMapDistance.clearPriorityTickets(pair); // Paper
                this.chunkMapDistance.clearUrgent(pair); // Paper
            } // Paper
            ichunkaccess = (IChunkAccess) ((Either) completablefuture.join()).map((ichunkaccess1) -> {
                return ichunkaccess1;
            }, (playerchunk_failure) -> {
                if (flag) {
                    throw (IllegalStateException) SystemUtils.c(new IllegalStateException("Chunk not there when requested: " + playerchunk_failure));
                } else {
                    return null;
                }
            });
            this.a(k, ichunkaccess, chunkstatus);
            return ichunkaccess;
        }
    }

    @Nullable
    @Override
    public Chunk a(int i, int j) {
        if (Thread.currentThread() != this.serverThread) {
            return null;
        } else {
            return this.getChunkAtIfLoadedMainThread(i, j); // Paper - optimise for loaded chunks
        }
    }

    private void clearCache() {
        Arrays.fill(this.cachePos, ChunkCoordIntPair.a);
        Arrays.fill(this.cacheStatus, (Object) null);
        Arrays.fill(this.cacheChunk, (Object) null);
    }

    private CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> getChunkFutureMainThread(int i, int j, ChunkStatus chunkstatus, boolean flag) {
        // Paper start - add isUrgent - old sig left in place for dirty nms plugins
        return getChunkFutureMainThread(i, j, chunkstatus, flag, false);
    }
    private CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> getChunkFutureMainThread(int i, int j, ChunkStatus chunkstatus, boolean flag, boolean isUrgent) {
        // Paper end
        ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(i, j);
        long k = chunkcoordintpair.pair();
        int l = 33 + ChunkStatus.a(chunkstatus);
        PlayerChunk playerchunk = this.getChunk(k);

        // CraftBukkit start - don't add new ticket for currently unloading chunk
        boolean currentlyUnloading = false;
        if (playerchunk != null) {
            PlayerChunk.State oldChunkState = PlayerChunk.getChunkState(playerchunk.oldTicketLevel);
            PlayerChunk.State currentChunkState = PlayerChunk.getChunkState(playerchunk.getTicketLevel());
            currentlyUnloading = (oldChunkState.isAtLeast(PlayerChunk.State.BORDER) && !currentChunkState.isAtLeast(PlayerChunk.State.BORDER));
        }
        if (flag && !currentlyUnloading) {
            // CraftBukkit end
            this.chunkMapDistance.a(TicketType.UNKNOWN, chunkcoordintpair, l, chunkcoordintpair);
            if (isUrgent) this.chunkMapDistance.markUrgent(chunkcoordintpair); // Paper
            if (this.a(playerchunk, l)) {
                GameProfilerFiller gameprofilerfiller = this.world.getMethodProfiler();

                gameprofilerfiller.enter("chunkLoad");
                chunkMapDistance.delayDistanceManagerTick = false; // Paper - ensure this is never false
                this.tickDistanceManager();
                playerchunk = this.getChunk(k);
                gameprofilerfiller.exit();
                if (this.a(playerchunk, l)) {
                    throw (IllegalStateException) SystemUtils.c(new IllegalStateException("No chunk holder after ticket has been added"));
                }
            }
        }
        // Paper start
        CompletableFuture<Either<IChunkAccess, PlayerChunk.Failure>> future = this.a(playerchunk, l) ? PlayerChunk.UNLOADED_CHUNK_ACCESS_FUTURE : playerchunk.a(chunkstatus, this.playerChunkMap);
        if (isUrgent) {
            future.thenAccept(either -> this.chunkMapDistance.clearUrgent(chunkcoordintpair));
        }
        return future;
        // Paper end
    }

    private boolean a(@Nullable PlayerChunk playerchunk, int i) {
        return playerchunk == null || playerchunk.oldTicketLevel > i; // CraftBukkit using oldTicketLevel for isLoaded checks
    }

    public boolean isLoaded(int i, int j) {
        PlayerChunk playerchunk = this.getChunk((new ChunkCoordIntPair(i, j)).pair());
        int k = 33 + ChunkStatus.a(ChunkStatus.FULL);

        return !this.a(playerchunk, k);
    }

    @Override
    public IBlockAccess c(int i, int j) {
        long k = ChunkCoordIntPair.pair(i, j);
        PlayerChunk playerchunk = this.getChunk(k);

        if (playerchunk == null) {
            return null;
        } else {
            int l = ChunkProviderServer.c.size() - 1;

            while (true) {
                ChunkStatus chunkstatus = (ChunkStatus) ChunkProviderServer.c.get(l);
                Optional<IChunkAccess> optional = ((Either) playerchunk.getStatusFutureUnchecked(chunkstatus).getNow(PlayerChunk.UNLOADED_CHUNK_ACCESS)).left();

                if (optional.isPresent()) {
                    return (IBlockAccess) optional.get();
                }

                if (chunkstatus == ChunkStatus.LIGHT.e()) {
                    return null;
                }

                --l;
            }
        }
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    public boolean runTasks() {
        return this.serverThreadQueue.executeNext();
    }

    public boolean tickDistanceManager() { // Paper - public
        if (chunkMapDistance.delayDistanceManagerTick) return false; // Paper
        boolean flag = this.chunkMapDistance.a(this.playerChunkMap);
        boolean flag1 = this.playerChunkMap.b();

        if (!flag && !flag1) {
            return false;
        } else {
            this.clearCache();
            return true;
        }
    }

    public final boolean isInEntityTickingChunk(Entity entity) { return this.a(entity); } // Paper - OBFHELPER
    @Override public boolean a(Entity entity) {
        // Paper start - optimize is ticking ready type functions
        // entity ticking
        PlayerChunk playerChunk = this.getChunk(MCUtil.getCoordinateKey(entity));
        return playerChunk != null && playerChunk.isEntityTickingReady();
        // Paper end - optimize is ticking ready type functions
    }

    public final boolean isEntityTickingChunk(ChunkCoordIntPair chunkcoordintpair) { return this.a(chunkcoordintpair); } // Paper - OBFHELPER
    @Override public boolean a(ChunkCoordIntPair chunkcoordintpair) {
        // Paper start - optimize is ticking ready type functions
        // is entity ticking ready
        PlayerChunk playerChunk = this.getChunk(MCUtil.getCoordinateKey(chunkcoordintpair));
        return playerChunk != null && playerChunk.isEntityTickingReady();
        // Paper end - optimize is ticking ready type functions
    }

    @Override
    public boolean a(BlockPosition blockposition) {
        // Paper start - optimize is ticking ready type functions
        // is ticking ready
        PlayerChunk playerChunk = this.getChunk(MCUtil.getCoordinateKey(blockposition));
        return playerChunk != null && playerChunk.isTickingReady();
        // Paper end - optimize is ticking ready type functions
    }

    public boolean b(Entity entity) {
        // Paper start - optimize is ticking ready type functions
        // is full chunk ready
        PlayerChunk playerChunk = this.getChunk(MCUtil.getCoordinateKey(entity));
        return playerChunk != null && playerChunk.isFullChunkReady();
        // Paper end - optimize is ticking ready type functions
    }

    private boolean a(long i, Function<PlayerChunk, CompletableFuture<Either<Chunk, PlayerChunk.Failure>>> function) {
        PlayerChunk playerchunk = this.getChunk(i);

        if (playerchunk == null) {
            return false;
        } else {
            Either<Chunk, PlayerChunk.Failure> either = (Either) ((CompletableFuture) function.apply(playerchunk)).getNow(PlayerChunk.UNLOADED_CHUNK);

            return either.left().isPresent();
        }
    }

    public void save(boolean flag) {
        this.tickDistanceManager();
        try (co.aikar.timings.Timing timed = world.timings.chunkSaveData.startTiming()) { // Paper - Timings
        this.playerChunkMap.save(flag);
        } // Paper - Timings
    }

    // Paper start - duplicate save, but call incremental
    public void saveIncrementally() {
        this.tickDistanceManager();
        try (co.aikar.timings.Timing timed = world.timings.chunkSaveData.startTiming()) { // Paper - Timings
            this.playerChunkMap.saveIncrementally();
        } // Paper - Timings
    }
    // Paper end

    @Override
    public void close() throws IOException {
        // CraftBukkit start
        close(true);
    }

    public void close(boolean save) throws IOException {
        if (save) {
            this.save(true);
        }
        // CraftBukkit end
        this.lightEngine.close();
        this.playerChunkMap.close();
    }

    // CraftBukkit start - modelled on below
    public void purgeUnload() {
        this.world.getMethodProfiler().enter("purge");
        this.chunkMapDistance.purgeTickets();
        this.tickDistanceManager();
        this.world.getMethodProfiler().exitEnter("unload");
        this.playerChunkMap.unloadChunks(() -> true);
        this.world.getMethodProfiler().exit();
        this.clearCache();
    }
    // CraftBukkit end

    public void tick(BooleanSupplier booleansupplier) {
        this.world.getMethodProfiler().enter("purge");
        this.world.timings.doChunkMap.startTiming(); // Spigot
        this.chunkMapDistance.purgeTickets();
        this.world.getMinecraftServer().midTickLoadChunks(); // Paper
        this.tickDistanceManager();
        this.world.timings.doChunkMap.stopTiming(); // Spigot
        this.world.getMethodProfiler().exitEnter("chunks");
        this.world.timings.chunks.startTiming(); // Paper - timings
        this.tickChunks();
        this.world.timings.chunks.stopTiming(); // Paper - timings
        this.world.timings.doChunkUnload.startTiming(); // Spigot
        this.world.getMethodProfiler().exitEnter("unload");
        this.playerChunkMap.unloadChunks(booleansupplier);
        this.world.getMinecraftServer().midTickLoadChunks(); // Paper
        this.world.timings.doChunkUnload.stopTiming(); // Spigot
        this.world.getMethodProfiler().exit();
        this.clearCache();
    }

    private void tickChunks() {
        long i = this.world.getTime();
        long j = i - this.lastTickTime;

        this.lastTickTime = i;
        WorldData worlddata = this.world.getWorldData();
        boolean flag = worlddata.getType() == WorldType.DEBUG_ALL_BLOCK_STATES;
        boolean flag1 = this.world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && !world.getPlayers().isEmpty(); // CraftBukkit

        if (!flag) {
            // Paper start - optimize isOutisdeRange
            PlayerChunkMap playerChunkMap = this.playerChunkMap;
            for (EntityPlayer player : this.world.players) {
                if (!player.affectsSpawning || player.isSpectator()) {
                    playerChunkMap.playerMobSpawnMap.remove(player);
                    continue;
                }

                int viewDistance = this.playerChunkMap.getEffectiveViewDistance();

                // copied and modified from isOutisdeRange
                int chunkRange = world.spigotConfig.mobSpawnRange;
                chunkRange = (chunkRange > viewDistance) ? (byte)viewDistance : chunkRange;
                chunkRange = (chunkRange > ChunkMapDistance.MOB_SPAWN_RANGE) ? ChunkMapDistance.MOB_SPAWN_RANGE : chunkRange;

                com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent event = new com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent(player.getBukkitEntity(), (byte)chunkRange);
                event.callEvent();
                if (event.isCancelled() || event.getSpawnRadius() < 0 || playerChunkMap.playerChunkTickRangeMap.getLastViewDistance(player) == -1) {
                    playerChunkMap.playerMobSpawnMap.remove(player);
                    continue;
                }

                int range = Math.min(event.getSpawnRadius(), 32); // limit to max view distance
                int chunkX = net.minecraft.server.MCUtil.getChunkCoordinate(player.locX());
                int chunkZ = net.minecraft.server.MCUtil.getChunkCoordinate(player.locZ());

                playerChunkMap.playerMobSpawnMap.addOrUpdate(player, chunkX, chunkZ, range);
                player.lastEntitySpawnRadiusSquared = (double)((range << 4) * (range << 4)); // used in isOutsideRange
            }
            // Paper end - optimize isOutisdeRange
            this.world.getMethodProfiler().enter("pollingChunks");
            int k = this.world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
            BlockPosition blockposition = this.world.getSpawn();
            // CraftBukkit start - Other mob type spawn tick rate
            boolean spawnAnimalThisTick = world.ticksPerAnimalSpawns != 0L && worlddata.getTime() % world.ticksPerAnimalSpawns == 0L;
            boolean spawnMonsterThisTick = world.ticksPerMonsterSpawns != 0L && worlddata.getTime() % world.ticksPerMonsterSpawns == 0L;
            boolean spawnWaterThisTick = world.ticksPerWaterSpawns != 0L && worlddata.getTime() % world.ticksPerWaterSpawns == 0L;
            boolean spawnAmbientThisTick = world.ticksPerAmbientSpawns != 0L && worlddata.getTime() % world.ticksPerAmbientSpawns == 0L;
            boolean flag2 = spawnAnimalThisTick;
            // CraftBukkit end

            this.world.getMethodProfiler().enter("naturalSpawnCount");
            this.world.timings.countNaturalMobs.startTiming(); // Paper - timings
            int l = this.chunkMapDistance.b();
            EnumCreatureType[] aenumcreaturetype = EnumCreatureType.values();
            // Paper start - per player mob spawning
            int[] worldMobCount;
            if (this.playerChunkMap.playerMobDistanceMap != null) {
                // update distance map
                this.world.timings.playerMobDistanceMapUpdate.startTiming();
                this.playerChunkMap.playerMobDistanceMap.update(this.world.players, this.playerChunkMap.viewDistance);
                this.world.timings.playerMobDistanceMapUpdate.stopTiming();
                // re-set mob counts
                for (EntityPlayer player : this.world.players) {
                    Arrays.fill(player.mobCounts, 0);
                }
                worldMobCount = this.world.countMobs(true);
            } else {
                worldMobCount = this.world.countMobs(false);
            }
            // Paper end

            this.world.timings.countNaturalMobs.stopTiming(); // Paper - timings
            this.world.getMethodProfiler().exit();
            // Paper - replaced by above
            final int[] chunksTicked = {0}; this.playerChunkMap.forEachVisibleChunk((playerchunk) -> { // Paper - safe iterator incase chunk loads, also no wrapping
                Optional<Chunk> optional = ((Either) playerchunk.b().getNow(PlayerChunk.UNLOADED_CHUNK)).left();

                if (optional.isPresent()) {
                    Chunk chunk = (Chunk) optional.get();

                    this.world.getMethodProfiler().enter("broadcast");
                    this.world.timings.broadcastChunkUpdates.startTiming(); // Paper - timings
                    playerchunk.a(chunk);
                    this.world.timings.broadcastChunkUpdates.stopTiming(); // Paper - timings
                    this.world.getMethodProfiler().exit();
                    ChunkCoordIntPair chunkcoordintpair = playerchunk.i();

                    if (!this.playerChunkMap.isOutsideOfRange(playerchunk, chunkcoordintpair, false)) { // Paper - optimise isOutsideOfRange
                        // Paper end
                        chunk.setInhabitedTime(chunk.getInhabitedTime() + j);
                        if (flag1 && (this.allowMonsters || this.allowAnimals) && this.world.getWorldBorder().isInBounds(chunk.getPos()) && !this.playerChunkMap.isOutsideOfRange(playerchunk, chunkcoordintpair, true)) { // Spigot // Paper - optimise isOutsideOfRange
                            this.world.getMethodProfiler().enter("spawner");
                            this.world.timings.mobSpawn.startTiming(); // Spigot
                            EnumCreatureType[] aenumcreaturetype1 = aenumcreaturetype;
                            int i1 = aenumcreaturetype.length;

                            for (int j1 = 0; j1 < i1; ++j1) {
                                EnumCreatureType enumcreaturetype = aenumcreaturetype1[j1];

                                // CraftBukkit start - Use per-world spawn limits
                                boolean spawnThisTick = true;
                                int limit = enumcreaturetype.b();
                                switch (enumcreaturetype) {
                                    case MONSTER:
                                        spawnThisTick = spawnMonsterThisTick;
                                        limit = world.getWorld().getMonsterSpawnLimit();
                                        break;
                                    case CREATURE:
                                        spawnThisTick = spawnAnimalThisTick;
                                        limit = world.getWorld().getAnimalSpawnLimit();
                                        break;
                                    case WATER_CREATURE:
                                        spawnThisTick = spawnWaterThisTick;
                                        limit = world.getWorld().getWaterAnimalSpawnLimit();
                                        break;
                                    case AMBIENT:
                                        spawnThisTick = spawnAmbientThisTick;
                                        limit = world.getWorld().getAmbientSpawnLimit();
                                        break;
                                }

                                if (!spawnThisTick || limit == 0) {
                                    continue;
                                }
                                // CraftBukkit end

                                if (enumcreaturetype != EnumCreatureType.MISC && (!enumcreaturetype.c() || this.allowAnimals) && (enumcreaturetype.c() || this.allowMonsters) && (!enumcreaturetype.d() || flag2)) {
                                    int k1 = limit * l / ChunkProviderServer.b; // CraftBukkit - use per-world limits

                                    // Paper start - only allow spawns upto the limit per chunk and update count afterwards
                                    int currEntityCount = worldMobCount[enumcreaturetype.ordinal()];
                                    int difference = k1 - currEntityCount;

                                    if (this.world.paperConfig.perPlayerMobSpawns) {
                                        int minDiff = Integer.MAX_VALUE;
                                        for (EntityPlayer entityplayer : this.playerChunkMap.playerMobDistanceMap.getPlayersInRange(chunk.getPos())) {
                                            minDiff = Math.min(limit - this.playerChunkMap.getMobCountNear(entityplayer, enumcreaturetype), minDiff);
                                        }
                                        difference = (minDiff == Integer.MAX_VALUE) ? 0 : minDiff;
                                    }

                                    if (difference > 0) {
                                        int spawnCount = SpawnerCreature.spawnMobs(enumcreaturetype, this.world, chunk, blockposition, difference,
                                            this.world.paperConfig.perPlayerMobSpawns ? this.playerChunkMap::updatePlayerMobTypeMap : null);
                                        worldMobCount[enumcreaturetype.ordinal()] += spawnCount;
                                        // Paper end
                                    }
                                }
                            }

                            this.world.timings.mobSpawn.stopTiming(); // Spigot
                            this.world.getMethodProfiler().exit();
                        }

                        this.world.timings.chunkTicks.startTiming(); // Spigot // Paper
                        this.world.a(chunk, k);
                        this.world.timings.chunkTicks.stopTiming(); // Spigot // Paper
                        if (chunksTicked[0]++ % 10 == 0) this.world.getMinecraftServer().midTickLoadChunks(); // Paper
                    }
                }
            });
            this.world.getMethodProfiler().enter("customSpawners");
            if (flag1) {
                try (co.aikar.timings.Timing ignored = this.world.timings.miscMobSpawning.startTiming()) { // Paper - timings
                this.chunkGenerator.doMobSpawning(this.world, this.allowMonsters, this.allowAnimals);
                } // Paper - timings
            }

            this.world.getMethodProfiler().exit();
            this.world.getMethodProfiler().exit();
        }

        this.playerChunkMap.g();
    }

    @Override
    public String getName() {
        return "ServerChunkCache: " + this.h();
    }

    @VisibleForTesting
    public int f() {
        return this.serverThreadQueue.bh();
    }

    public ChunkGenerator<?> getChunkGenerator() {
        return this.chunkGenerator;
    }

    public int h() {
        return this.playerChunkMap.d();
    }

    public void flagDirty(BlockPosition blockposition) {
        int i = blockposition.getX() >> 4;
        int j = blockposition.getZ() >> 4;
        PlayerChunk playerchunk = this.getChunk(ChunkCoordIntPair.pair(i, j));

        if (playerchunk != null) {
            playerchunk.a(blockposition.getX() & 15, blockposition.getY(), blockposition.getZ() & 15);
        }

    }

    @Override
    public void a(EnumSkyBlock enumskyblock, SectionPosition sectionposition) {
        this.serverThreadQueue.execute(() -> {
            PlayerChunk playerchunk = this.getChunk(sectionposition.u().pair());

            if (playerchunk != null) {
                playerchunk.a(enumskyblock, sectionposition.b());
            }

        });
    }

    public <T> void addTicket(TicketType<T> tickettype, ChunkCoordIntPair chunkcoordintpair, int i, T t0) {
        this.chunkMapDistance.addTicket(tickettype, chunkcoordintpair, i, t0);
    }

    public <T> void removeTicket(TicketType<T> tickettype, ChunkCoordIntPair chunkcoordintpair, int i, T t0) {
        this.chunkMapDistance.removeTicket(tickettype, chunkcoordintpair, i, t0);
    }

    @Override
    public void a(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        this.chunkMapDistance.a(chunkcoordintpair, flag);
    }

    public void movePlayer(EntityPlayer entityplayer) {
        this.playerChunkMap.movePlayer(entityplayer);
    }

    public void removeEntity(Entity entity) {
        this.playerChunkMap.removeEntity(entity);
    }

    public void addEntity(Entity entity) {
        this.playerChunkMap.addEntity(entity);
    }

    public void broadcastIncludingSelf(Entity entity, Packet<?> packet) {
        this.playerChunkMap.broadcastIncludingSelf(entity, packet);
    }

    public void broadcast(Entity entity, Packet<?> packet) {
        this.playerChunkMap.broadcast(entity, packet);
    }

    public void setViewDistance(int i) {
        this.playerChunkMap.setViewDistance(i);
    }

    @Override
    public void a(boolean flag, boolean flag1) {
        this.allowMonsters = flag;
        this.allowAnimals = flag1;
    }

    public WorldPersistentData getWorldPersistentData() {
        return this.worldPersistentData;
    }

    public VillagePlace j() {
        return this.playerChunkMap.h();
    }

    final class a extends IAsyncTaskHandler<Runnable> {

        private a(World world) {
            super("Chunk source main thread executor for " + IRegistry.DIMENSION_TYPE.getKey(world.getWorldProvider().getDimensionManager()));
        }

        @Override
        protected Runnable postToMainThread(Runnable runnable) {
            return runnable;
        }

        @Override
        protected boolean canExecute(Runnable runnable) {
            return true;
        }

        @Override
        protected boolean isNotMainThread() {
            return true;
        }

        @Override
        protected Thread getThread() {
            return ChunkProviderServer.this.serverThread;
        }

        @Override
        protected void executeTask(Runnable runnable) {
            ChunkProviderServer.this.world.getMethodProfiler().c("runTask");
            super.executeTask(runnable);
        }

        // Paper start
        private long lastMidTickChunkTask = 0;
        public boolean pollChunkLoadTasks() {
            if (com.destroystokyo.paper.io.chunk.ChunkTaskManager.pollChunkWaitQueue() || ChunkProviderServer.this.world.asyncChunkTaskManager.pollNextChunkTask()) {
                try {
                    ChunkProviderServer.this.tickDistanceManager();
                } finally {
                    // from below: process pending Chunk loadCallback() and unloadCallback() after each run task
                    playerChunkMap.callbackExecutor.run();
                }
                return true;
            }
            return false;
        }
        public void midTickLoadChunks() {
            MinecraftServer server = ChunkProviderServer.this.world.getMinecraftServer();
            // always try to load chunks, restrain generation/other updates only. don't count these towards tick count
            //noinspection StatementWithEmptyBody
            while (pollChunkLoadTasks()) {}

            if (System.nanoTime() - lastMidTickChunkTask < 200000) {
                return;
            }

            for (;server.midTickChunksTasksRan < com.destroystokyo.paper.PaperConfig.midTickChunkTasks && server.canSleepForTick();) {
                if (this.executeNext()) {
                    server.midTickChunksTasksRan++;
                    lastMidTickChunkTask = System.nanoTime();
                } else {
                    break;
                }
            }
        }
        // Paper end

        @Override
        protected boolean executeNext() {
        // CraftBukkit start - process pending Chunk loadCallback() and unloadCallback() after each run task
        try {
            boolean execChunkTask = com.destroystokyo.paper.io.chunk.ChunkTaskManager.pollChunkWaitQueue() || ChunkProviderServer.this.world.asyncChunkTaskManager.pollNextChunkTask(); // Paper
            if (ChunkProviderServer.this.tickDistanceManager()) {
                return true;
            } else {
                //ChunkProviderServer.this.lightEngine.queueUpdate(); // Paper - not needed
                return super.executeNext() || execChunkTask; // Paper
            }
        } finally {
            playerChunkMap.chunkLoadConversionCallbackExecutor.run(); // Paper - Add chunk load conversion callback executor to prevent deadlock due to recursion in the chunk task queue sorter
            playerChunkMap.callbackExecutor.run();
        }
        // CraftBukkit end
        }
    }
}
