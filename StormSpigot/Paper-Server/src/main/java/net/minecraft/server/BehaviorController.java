package net.minecraft.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class BehaviorController<E extends EntityLiving> implements MinecraftSerializable {

    private final Map<MemoryModuleType<?>, Optional<?>> memories = Maps.newHashMap();
    private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
    private final Map<Integer, Map<Activity, Set<Behavior<? super E>>>> c = Maps.newTreeMap();
    private Schedule schedule;
    private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> e;
    private Set<Activity> f;
    private final Set<Activity> g;
    private Activity h;
    private long i;

    public <T> BehaviorController(Collection<MemoryModuleType<?>> collection, Collection<SensorType<? extends Sensor<? super E>>> collection1, Dynamic<T> dynamic) {
        this.schedule = Schedule.EMPTY;
        this.e = Maps.newHashMap();
        this.f = Sets.newHashSet();
        this.g = Sets.newHashSet();
        this.h = Activity.IDLE;
        this.i = -9999L;
        // Paper start - Port 20w15a pathfinder optimizations
        for (final MemoryModuleType<?> memoryModuleType : collection) {
            this.memories.put(memoryModuleType, Optional.empty());
        }
        for (final SensorType<? extends Sensor<? super E>> sensorType : collection1) {
            this.sensors.put(sensorType, sensorType.a());
        }
        for (final Sensor<? super E> sensor : this.sensors.values()) {
            for (final MemoryModuleType<?> memoryModuleType : sensor.a()) {
                this.memories.put(memoryModuleType, Optional.empty());
            }
        }
        for (final Map.Entry<Dynamic<T>, Dynamic<T>> entry : dynamic.get("memories").asMap(Function.identity(), Function.identity()).entrySet()) {
            this.a((MemoryModuleType) IRegistry.MEMORY_MODULE_TYPE.get(new MinecraftKey((entry.getKey()).asString(""))), entry.getValue());
        }
        // Paper end - Port 20w15a pathfinder optimizations
    }

    public boolean hasMemory(MemoryModuleType<?> memorymoduletype) {
        return this.a(memorymoduletype, MemoryStatus.VALUE_PRESENT);
    }

    private <T, U> void a(MemoryModuleType<U> memorymoduletype, Dynamic<T> dynamic) {
        this.setMemory(memorymoduletype, (memorymoduletype.getSerializer().orElseThrow(RuntimeException::new)).apply(dynamic)); // Paper - decompile fix
    }

    public <U> void removeMemory(MemoryModuleType<U> memorymoduletype) {
        this.setMemory(memorymoduletype, Optional.empty());
    }

    public <U> void setMemory(MemoryModuleType<U> memorymoduletype, @Nullable U u0) {
        this.setMemory(memorymoduletype, Optional.ofNullable(u0));
    }

    public <U> void setMemory(MemoryModuleType<U> memorymoduletype, Optional<U> optional) {
        if (this.memories.containsKey(memorymoduletype)) {
            if (optional.isPresent() && this.a(optional.get())) {
                this.removeMemory(memorymoduletype);
            } else {
                this.memories.put(memorymoduletype, optional);
            }
        }

    }

    public <U> Optional<U> getMemory(MemoryModuleType<U> memorymoduletype) {
        return (Optional) this.memories.get(memorymoduletype);
    }

    public boolean a(MemoryModuleType<?> memorymoduletype, MemoryStatus memorystatus) {
        Optional<?> optional = (Optional) this.memories.get(memorymoduletype);

        return optional == null ? false : memorystatus == MemoryStatus.REGISTERED || memorystatus == MemoryStatus.VALUE_PRESENT && optional.isPresent() || memorystatus == MemoryStatus.VALUE_ABSENT && !optional.isPresent();
    }

    public Schedule getSchedule() {
        return this.schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void a(Set<Activity> set) {
        this.f = set;
    }

    // Paper start - Port 20w15a pathfinder optimizations
    @Deprecated
    public java.util.List<Behavior<? super E>> d() {
        final java.util.List<Behavior<? super E>> behaviorList = (java.util.List<Behavior<? super E>>) new it.unimi.dsi.fastutil.objects.ObjectArrayList();
        for (final Map<Activity, Set<Behavior<? super E>>> map : this.c.values()) {
            for (final Set<Behavior<? super E>> set : map.values()) {
                for (final Behavior<? super E> behavior : set) {
                    if (behavior.a() == Behavior.Status.RUNNING) {
                        behaviorList.add(behavior);
                    }
                }
            }
        }
        return behaviorList;
        // Paper end - Port 20w15a pathfinder optimizations
    }

    public void a(Activity activity) {
        this.g.clear();
        this.g.addAll(this.f);
        boolean flag = this.e.keySet().contains(activity) && this.d(activity);

        this.g.add(flag ? activity : this.h);
    }

    public void a(long i, long j) {
        if (j - this.i > 20L) {
            this.i = j;
            Activity activity = this.getSchedule().a((int) (i % 24000L));

            if (!this.g.contains(activity)) {
                this.a(activity);
            }
        }

    }

    public void b(Activity activity) {
        this.h = activity;
    }

    public void a(Activity activity, ImmutableList<Pair<Integer, ? extends Behavior<? super E>>> immutablelist) {
        this.a(activity, immutablelist, (Set) ImmutableSet.of());
    }

    public void a(Activity activity, ImmutableList<Pair<Integer, ? extends Behavior<? super E>>> immutablelist, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set) {
        this.e.put(activity, set);
        immutablelist.forEach((pair) -> {
            ((Set) ((Map) this.c.computeIfAbsent(pair.getFirst(), (integer) -> {
                return Maps.newHashMap();
            })).computeIfAbsent(activity, (activity1) -> {
                return Sets.newLinkedHashSet();
            })).add(pair.getSecond());
        });
    }

    public boolean hasActivity(Activity activity) { return c(activity); } // Paper - OBFHELPER
    public boolean c(Activity activity) {
        return this.g.contains(activity);
    }

    public BehaviorController<E> f() {
        BehaviorController<E> behaviorcontroller = new BehaviorController<>(this.memories.keySet(), this.sensors.keySet(), new Dynamic(DynamicOpsNBT.a, new NBTTagCompound()));
        // Paper start - Port 20w15a pathfinder optimizations
        for (final Entry<MemoryModuleType<?>, Optional<?>> entry : this.memories.entrySet()) {
            final MemoryModuleType<?> memoryModuleType = entry.getKey();
            if (entry.getValue().isPresent()) {
                behaviorcontroller.memories.put(memoryModuleType, entry.getValue());
            }
        }
        // Paper end - Port 20w15a pathfinder optimizations
        return behaviorcontroller;
    }

    public void a(WorldServer worldserver, E e0) {
        this.c(worldserver, e0);
        this.d(worldserver, e0);
        this.e(worldserver, e0);
    }

    public void b(WorldServer worldserver, E e0) {
        long i = e0.world.getTime();

        for(Behavior<? super E> behavior : this.d()) { // Paper - Port 20w15a pathfinder optimizations
            behavior.e(worldserver, e0, i);
        }
    }

    @Override
    public <T> T a(DynamicOps<T> dynamicops) {
        T t0 = dynamicops.createMap(this.memories.entrySet().stream().filter((entry) -> { // Paper - decompile fix
            return ((MemoryModuleType) entry.getKey()).getSerializer().isPresent() && ((Optional) entry.getValue()).isPresent();
        }).map((entry) -> {
            return Pair.of(dynamicops.createString(IRegistry.MEMORY_MODULE_TYPE.getKey(entry.getKey()).toString()), ((MinecraftSerializable) ((Optional) entry.getValue()).get()).a(dynamicops));
        }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));

        return dynamicops.createMap(ImmutableMap.of(dynamicops.createString("memories"), t0));
    }

    private void c(WorldServer worldserver, E e0) {
        this.sensors.values().forEach((sensor) -> {
            sensor.b(worldserver, e0);
        });
    }

    private void d(WorldServer worldserver, E e0) {
        long i = worldserver.getTime();
        // Paper start - Port 20w15a pathfinder optimizations
        for (final Map<Activity, Set<Behavior<? super E>>> map : this.c.values()) {
            for (final Map.Entry<Activity, Set<Behavior<? super E>>> entry : map.entrySet()) {
                final Activity activity = entry.getKey();
                if (this.g.contains(activity)) {
                    final Set<Behavior<? super E>> set = entry.getValue();
                    for (final Behavior<? super E> behavior : set) {
                        if (behavior.a() == Behavior.Status.STOPPED) {
                            behavior.b(worldserver, e0, i);
                        }
                    }
                }
            }
        }
        // Paper end - Port 20w15a pathfinder optimizations
    }

    private void e(WorldServer worldserver, E e0) {
        long i = worldserver.getTime();

        for (final Behavior<? super E> behavior : this.d()) { // Paper - Port 20w15a pathfinder optimizations
            behavior.c(worldserver, e0, i);
        }
    }

    private boolean d(Activity activity) {
        // Paper start - Port 20w15a pathfinder optimizations
        if (!this.e.containsKey(activity)) {
            return false;
        }
        for (final Pair<MemoryModuleType<?>, MemoryStatus> pair : this.e.get(activity)) {
            MemoryModuleType<?> memorymoduletype = pair.getFirst();
            MemoryStatus memorystatus = pair.getSecond();
            if (!this.a(memorymoduletype, memorystatus)) {
                return false;
            }
        }
        return true;
        // Paper end - Port 20w15a pathfinder optimizations
    }

    private boolean a(Object object) {
        return object instanceof Collection && ((Collection) object).isEmpty();
    }
}
