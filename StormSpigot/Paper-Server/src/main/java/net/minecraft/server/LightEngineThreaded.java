package net.minecraft.server;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LightEngineThreaded extends LightEngine implements AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger();
    private final ThreadedMailbox<Runnable> b; ThreadedMailbox<Runnable> mailbox; // Paper
    // Paper start
    private static final int MAX_PRIORITIES = PlayerChunkMap.GOLDEN_TICKET + 2;

    public void changePriority(long pair, int currentPriority, int priority) {
        this.mailbox.queue(() -> {
            ChunkLightQueue remove = this.queue.buckets[currentPriority].remove(pair);
            if (remove != null) {
                ChunkLightQueue existing = this.queue.buckets[priority].put(pair, remove);
                if (existing != null) {
                    remove.pre.addAll(existing.pre);
                    remove.post.addAll(existing.post);
                }
            }
        });
    }

    static class ChunkLightQueue {
        public boolean shouldFastUpdate;
        java.util.ArrayDeque<Runnable> pre = new java.util.ArrayDeque<Runnable>();
        java.util.ArrayDeque<Runnable> post = new java.util.ArrayDeque<Runnable>();

        ChunkLightQueue(long chunk) {}
    }


    // Retain the chunks priority level for queued light tasks
    private static class LightQueue {
        private int size = 0;
        private int lowestPriority = MAX_PRIORITIES;
        private final it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap<ChunkLightQueue>[] buckets = new it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap[MAX_PRIORITIES];

        private LightQueue() {
            for (int i = 0; i < buckets.length; i++) {
                buckets[i] = new it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap<>();
            }
        }

        public final void add(long chunkId, int priority, LightEngineThreaded.Update type, Runnable run) {
            add(chunkId, priority, type, run, false);
        }
        public final void add(long chunkId, int priority, LightEngineThreaded.Update type, Runnable run, boolean shouldFastUpdate) {
            ChunkLightQueue lightQueue = this.buckets[priority].computeIfAbsent(chunkId, ChunkLightQueue::new);
            this.size++;
            if (type == Update.PRE_UPDATE) {
                lightQueue.pre.add(run);
            } else {
                lightQueue.post.add(run);
            }
            if (shouldFastUpdate) {
                lightQueue.shouldFastUpdate = true;
            }

            if (this.lowestPriority > priority) {
                this.lowestPriority = priority;
            }
        }

        public final boolean isEmpty() {
            return this.size == 0;
        }

        public final int size() {
            return this.size;
        }

        public boolean poll(java.util.List<Runnable> pre, java.util.List<Runnable> post) {
            boolean hasWork = false;
            it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap<ChunkLightQueue>[] buckets = this.buckets;
            while (lowestPriority < MAX_PRIORITIES && !isEmpty()) {
                it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap<ChunkLightQueue> bucket = buckets[lowestPriority];
                if (bucket.isEmpty()) {
                    lowestPriority++;
                    continue;
                }
                ChunkLightQueue queue = bucket.removeFirst();
                this.size -= queue.pre.size() + queue.post.size();
                pre.addAll(queue.pre);
                post.addAll(queue.post);
                queue.pre.clear();
                queue.post.clear();
                hasWork = true;
                if (queue.shouldFastUpdate) {
                    return true;
                }
            }
            return hasWork;
        }
    }

    private final LightQueue queue = new LightQueue();
    // Paper end
    private final PlayerChunkMap d;
    private final Mailbox<ChunkTaskQueueSorter.a<Runnable>> e;
    private volatile int f = 5;
    private final AtomicBoolean g = new AtomicBoolean();

    public LightEngineThreaded(ILightAccess ilightaccess, PlayerChunkMap playerchunkmap, boolean flag, ThreadedMailbox<Runnable> threadedmailbox, Mailbox<ChunkTaskQueueSorter.a<Runnable>> mailbox) {
        super(ilightaccess, true, flag);
        this.d = playerchunkmap;
        this.e = mailbox;
        this.mailbox = this.b = threadedmailbox; // Paper
    }

    public void close() {}

    @Override
    public int a(int i, boolean flag, boolean flag1) {
        throw (UnsupportedOperationException) SystemUtils.c(new UnsupportedOperationException("Ran authomatically on a different thread!"));
    }

    @Override
    public void a(BlockPosition blockposition, int i) {
        throw (UnsupportedOperationException) SystemUtils.c(new UnsupportedOperationException("Ran authomatically on a different thread!"));
    }

    @Override
    public void a(BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.immutableCopy();

        this.a(blockposition.getX() >> 4, blockposition.getZ() >> 4, LightEngineThreaded.Update.POST_UPDATE, SystemUtils.a(() -> {
            super.a(blockposition1);
        }, () -> {
            return "checkBlock " + blockposition1;
        }));
    }

    protected void a(ChunkCoordIntPair chunkcoordintpair) {
        this.a(chunkcoordintpair.x, chunkcoordintpair.z, () -> {
            return 0;
        }, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.a(() -> {
            super.b(chunkcoordintpair, false);
            super.a(chunkcoordintpair, false);

            int i;

            for (i = -1; i < 17; ++i) {
                super.a(EnumSkyBlock.BLOCK, SectionPosition.a(chunkcoordintpair, i), (NibbleArray) null);
                super.a(EnumSkyBlock.SKY, SectionPosition.a(chunkcoordintpair, i), (NibbleArray) null);
            }

            for (i = 0; i < 16; ++i) {
                super.a(SectionPosition.a(chunkcoordintpair, i), true);
            }

        }, () -> {
            return "updateChunkStatus " + chunkcoordintpair + " " + true;
        }));
    }

    @Override
    public void a(SectionPosition sectionposition, boolean flag) {
        this.a(sectionposition.a(), sectionposition.c(), () -> {
            return 0;
        }, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.a(() -> {
            super.a(sectionposition, flag);
        }, () -> {
            return "updateSectionStatus " + sectionposition + " " + flag;
        }));
    }

    @Override
    public void a(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        this.a(chunkcoordintpair.x, chunkcoordintpair.z, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.a(() -> {
            super.a(chunkcoordintpair, flag);
        }, () -> {
            return "enableLight " + chunkcoordintpair + " " + flag;
        }));
    }

    @Override
    public void a(EnumSkyBlock enumskyblock, SectionPosition sectionposition, @Nullable NibbleArray nibblearray) {
        this.a(sectionposition.a(), sectionposition.c(), () -> {
            return 0;
        }, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.a(() -> {
            super.a(enumskyblock, sectionposition, nibblearray);
        }, () -> {
            return "queueData " + sectionposition;
        }));
    }

    private void a(int i, int j, LightEngineThreaded.Update lightenginethreaded_update, Runnable runnable) {
        this.a(i, j, this.d.c(ChunkCoordIntPair.pair(i, j)), lightenginethreaded_update, runnable);
    }

    private void a(int i, int j, IntSupplier intsupplier, LightEngineThreaded.Update lightenginethreaded_update, Runnable runnable) {
        this.e.a(ChunkTaskQueueSorter.a(() -> { // Paper - decompile error
            // Paper start
            int priority = intsupplier.getAsInt();
            this.queue.add(ChunkCoordIntPair.pair(i, j), priority, lightenginethreaded_update, runnable); // Paper
            if (priority <= 25) { // don't auto kick off unless priority
                // Paper end
                this.b();
            }

        }, ChunkCoordIntPair.pair(i, j), intsupplier));
    }

    @Override
    public void b(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        this.a(chunkcoordintpair.x, chunkcoordintpair.z, () -> {
            return 0;
        }, LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.a(() -> {
            super.b(chunkcoordintpair, flag);
        }, () -> {
            return "retainData " + chunkcoordintpair;
        }));
    }

    public CompletableFuture<IChunkAccess> a(IChunkAccess ichunkaccess, boolean flag) {
        ChunkCoordIntPair chunkcoordintpair = ichunkaccess.getPos();

        ichunkaccess.b(false);
        // Paper start
        long pair = chunkcoordintpair.pair();
        CompletableFuture<IChunkAccess> future = new CompletableFuture<>();
        IntSupplier prioritySupplier1 = d.getPrioritySupplier(pair);
        IntSupplier prioritySupplier = flag ? () -> Math.max(1, prioritySupplier1.getAsInt() - 10) : prioritySupplier1;
        this.e.a(ChunkTaskQueueSorter.a(() -> {
            this.queue.add(pair, prioritySupplier.getAsInt(), LightEngineThreaded.Update.PRE_UPDATE, SystemUtils.a(() -> {
                // Paper end
            ChunkSection[] achunksection = ichunkaccess.getSections();

            for (int i = 0; i < 16; ++i) {
                ChunkSection chunksection = achunksection[i];

                if (!ChunkSection.a(chunksection)) {
                    super.a(SectionPosition.a(chunkcoordintpair, i), false);
                }
            }

            super.a(chunkcoordintpair, true);
            if (!flag) {
                ichunkaccess.m().forEach((blockposition) -> {
                    super.a(blockposition, ichunkaccess.h(blockposition));
                });
            }

            this.d.c(chunkcoordintpair);
        }, () -> {
            return "lightChunk " + chunkcoordintpair + " " + flag;
            // Paper start  - merge the 2 together
        }));

        this.queue.add(pair, prioritySupplier.getAsInt(), LightEngineThreaded.Update.POST_UPDATE, () -> {
            ichunkaccess.b(true);
            super.b(chunkcoordintpair, false);
            // Paper start
            future.complete(ichunkaccess);
        });
            queueUpdate(); // run queue now
        }, pair, prioritySupplier));
        return future;
        // Paper end
    }

    public void queueUpdate() {
        if ((!this.queue.isEmpty() || super.a()) && this.g.compareAndSet(false, true)) { // Paper
            this.b.a((() -> { // Paper - decompile error
                this.b();
                this.g.set(false);
                queueUpdate(); // Paper - if we still have work to do, do it!
            }));
        }

    }

    // Paper start - replace impl
    private final java.util.List<Runnable> pre = new java.util.ArrayList<>();
    private final java.util.List<Runnable> post = new java.util.ArrayList<>();
    private void b() {
        int i = Math.min(queue.size(), 4);
        boolean ran = false;
        while (i-- > 0 && queue.poll(pre, post)) {
            pre.forEach(Runnable::run);
            pre.clear();
            super.a(Integer.MAX_VALUE, true, true);
            post.forEach(Runnable::run);
            post.clear();
            ran = true;
        }
        if (!ran) {
            // might have level updates to go still
            super.a(Integer.MAX_VALUE, true, true);
        }
        // Paper end
    }

    public void a(int i) {
        this.f = i;
    }

    static enum Update {

        PRE_UPDATE, POST_UPDATE;

        private Update() {}
    }
}
