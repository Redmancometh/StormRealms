package net.minecraft.server;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BehaviorFindPosition extends Behavior<EntityCreature> {

    private final VillagePlaceType a;
    private final MemoryModuleType<GlobalPos> b;
    private final boolean c;
    private long d;
    private final Long2LongMap e = new Long2LongOpenHashMap();
    private int f;

    public BehaviorFindPosition(VillagePlaceType villageplacetype, MemoryModuleType<GlobalPos> memorymoduletype, boolean flag) {
        super(ImmutableMap.of(memorymoduletype, MemoryStatus.VALUE_ABSENT));
        this.a = villageplacetype;
        this.b = memorymoduletype;
        this.c = flag;
    }

    protected boolean a(WorldServer worldserver, EntityCreature entitycreature) {
        return this.c && entitycreature.isBaby() ? false : worldserver.getTime() - this.d >= 20L;
    }

    protected void a(WorldServer worldserver, EntityCreature entitycreature, long i) {
        this.f = 0;
        this.d = worldserver.getTime() + (long) java.util.concurrent.ThreadLocalRandom.current().nextInt(20); // Paper
        VillagePlace villageplace = worldserver.B();

        // Paper start - replace implementation completely
        BlockPosition blockposition2 = new BlockPosition(entitycreature);
        int dist = 48;
        int requiredDist = dist * dist;
        int cdist = Math.floorDiv(dist, 16);
        Predicate<VillagePlaceType> predicate = this.a.c();
        int maxPoiAttempts = 4;
        int poiAttempts = 0;
        OUT:
        for (ChunkCoordIntPair chunkcoordintpair : MCUtil.getSpiralOutChunks(blockposition2, cdist)) {
            for (int i1 = 0; i1 < 16; i1++) {
                java.util.Optional<VillagePlaceSection> section = villageplace.getSection(SectionPosition.a(chunkcoordintpair, i1).v());
                if (section == null || !section.isPresent()) continue;
                for (java.util.Map.Entry<VillagePlaceType, java.util.Set<VillagePlaceRecord>> e : section.get().getRecords().entrySet()) {
                    if (!predicate.test(e.getKey())) continue;
                    for (VillagePlaceRecord record : e.getValue()) {
                        if (!record.hasVacancy()) continue;

                        BlockPosition pos = record.getPosition();
                        long key = pos.asLong();
                        if (this.e.containsKey(key)) {
                            continue;
                        }
                        double poiDist = pos.distanceSquared(blockposition2);
                        if (poiDist <= (double) requiredDist) {
                            this.e.put(key, (long) (this.d + Math.sqrt(poiDist) * 4)); // use dist instead of 40 to blacklist longer if farther distance
                            ++poiAttempts;
                            PathEntity pathentity = entitycreature.getNavigation().a(com.google.common.collect.ImmutableSet.of(pos), 8, false, this.a.d());

                            if (pathentity != null && pathentity.h()) {
                                record.decreaseVacancy();
                                GlobalPos globalPos = GlobalPos.create(worldserver.getWorldProvider().getDimensionManager(), pos);
                                entitycreature.getBehaviorController().setMemory(this.b, globalPos);
                                break OUT;
                            }
                            if (poiAttempts >= maxPoiAttempts) {
                                break OUT;
                            }
                        }
                    }
                }
            }
        }
        // Clean up - vanilla does this only when it runs out, but that would push it to try farther POI's...
        this.e.long2LongEntrySet().removeIf((entry) -> entry.getLongValue() < this.d);
        /*
        Predicate<BlockPosition> predicate = (blockposition) -> {
            long j = blockposition.asLong();

            if (this.e.containsKey(j)) {
                return false;
            } else if (++this.f >= 5) {
                return false;
            } else {
                this.e.put(j, this.d + 40L);
                return true;
            }
        };
        Stream<BlockPosition> stream = villageplace.a(this.a.c(), predicate, new BlockPosition(entitycreature), 48, VillagePlace.Occupancy.HAS_SPACE);
        PathEntity pathentity = entitycreature.getNavigation().a(stream, this.a.d());

        if (pathentity != null && pathentity.h()) {
            BlockPosition blockposition = pathentity.k();

            villageplace.c(blockposition).ifPresent((villageplacetype) -> {
                villageplace.a(this.a.c(), (blockposition1) -> {
                    return blockposition1.equals(blockposition);
                }, blockposition, 1);
                entitycreature.getBehaviorController().setMemory(this.b, GlobalPos.create(worldserver.getWorldProvider().getDimensionManager(), blockposition));
                PacketDebug.c(worldserver, blockposition);
            });
        } else if (this.f < 5) {
            this.e.long2LongEntrySet().removeIf((entry) -> {
                return entry.getLongValue() < this.d;
            });
        }
        */ // Paper end
    }
}
