package net.minecraft.server;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;

public class VillagerProfession {

    public static final VillagerProfession NONE = a("none", VillagePlaceType.b, (SoundEffect) null);
    public static final VillagerProfession ARMORER = a("armorer", VillagePlaceType.c, SoundEffects.ENTITY_VILLAGER_WORK_ARMORER);
    public static final VillagerProfession BUTCHER = a("butcher", VillagePlaceType.d, SoundEffects.ENTITY_VILLAGER_WORK_BUTCHER);
    public static final VillagerProfession CARTOGRAPHER = a("cartographer", VillagePlaceType.e, SoundEffects.ENTITY_VILLAGER_WORK_CARTOGRAPHER);
    public static final VillagerProfession CLERIC = a("cleric", VillagePlaceType.f, SoundEffects.ENTITY_VILLAGER_WORK_CLERIC);
    public static final VillagerProfession FARMER = a("farmer", VillagePlaceType.g, ImmutableSet.of(Items.WHEAT, Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS), ImmutableSet.of(Blocks.FARMLAND), SoundEffects.ENTITY_VILLAGER_WORK_FARMER);
    public static final VillagerProfession FISHERMAN = a("fisherman", VillagePlaceType.h, SoundEffects.ENTITY_VILLAGER_WORK_FISHERMAN);
    public static final VillagerProfession FLETCHER = a("fletcher", VillagePlaceType.i, SoundEffects.ENTITY_VILLAGER_WORK_FLETCHER);
    public static final VillagerProfession LEATHERWORKER = a("leatherworker", VillagePlaceType.j, SoundEffects.ENTITY_VILLAGER_WORK_LEATHERWORKER);
    public static final VillagerProfession LIBRARIAN = a("librarian", VillagePlaceType.k, SoundEffects.ENTITY_VILLAGER_WORK_LIBRARIAN);
    public static final VillagerProfession MASON = a("mason", VillagePlaceType.l, SoundEffects.ENTITY_VILLAGER_WORK_MASON);
    public static final VillagerProfession NITWIT = a("nitwit", VillagePlaceType.m, (SoundEffect) null);
    public static final VillagerProfession SHEPHERD = a("shepherd", VillagePlaceType.n, SoundEffects.ENTITY_VILLAGER_WORK_SHEPHERD);
    public static final VillagerProfession TOOLSMITH = a("toolsmith", VillagePlaceType.o, SoundEffects.ENTITY_VILLAGER_WORK_TOOLSMITH);
    public static final VillagerProfession WEAPONSMITH = a("weaponsmith", VillagePlaceType.p, SoundEffects.ENTITY_VILLAGER_WORK_WEAPONSMITH);
    private final String p;
    private final VillagePlaceType q;
    private final ImmutableSet<Item> r;
    private final ImmutableSet<Block> s;
    @Nullable
    private final SoundEffect t;

    private VillagerProfession(String s, VillagePlaceType villageplacetype, ImmutableSet<Item> immutableset, ImmutableSet<Block> immutableset1, @Nullable SoundEffect soundeffect) {
        this.p = s;
        this.q = villageplacetype;
        this.r = immutableset;
        this.s = immutableset1;
        this.t = soundeffect;
    }

    public VillagePlaceType b() {
        return this.q;
    }

    public ImmutableSet<Item> c() {
        return this.r;
    }

    public ImmutableSet<Block> d() {
        return this.s;
    }

    @Nullable
    public SoundEffect e() {
        return this.t;
    }

    public String toString() {
        return this.p;
    }

    static VillagerProfession a(String s, VillagePlaceType villageplacetype, @Nullable SoundEffect soundeffect) {
        return a(s, villageplacetype, ImmutableSet.of(), ImmutableSet.of(), soundeffect);
    }

    static VillagerProfession a(String s, VillagePlaceType villageplacetype, ImmutableSet<Item> immutableset, ImmutableSet<Block> immutableset1, @Nullable SoundEffect soundeffect) {
        VillagePlaceType.professionCache = null; // Paper
        return (VillagerProfession) IRegistry.a((IRegistry) IRegistry.VILLAGER_PROFESSION, new MinecraftKey(s), (Object) (new VillagerProfession(s, villageplacetype, immutableset, immutableset1, soundeffect)));
    }
}
