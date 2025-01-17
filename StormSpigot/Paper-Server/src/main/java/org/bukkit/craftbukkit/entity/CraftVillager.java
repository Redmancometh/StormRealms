package org.bukkit.craftbukkit.entity;

import com.destroystokyo.paper.entity.villager.Reputation; // Paper
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps; // Paper
import java.util.Locale;
import net.minecraft.server.BlockBed;
import net.minecraft.server.BlockPosition;
import java.util.Map; // Paper
import java.util.UUID; // Paper
import net.minecraft.server.EntityVillager;
import net.minecraft.server.IBlockData;
import net.minecraft.server.IRegistry;
import net.minecraft.server.VillagerProfession;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

public class CraftVillager extends CraftAbstractVillager implements Villager {

    public CraftVillager(CraftServer server, EntityVillager entity) {
        super(server, entity);
    }

    @Override
    public EntityVillager getHandle() {
        return (EntityVillager) entity;
    }

    @Override
    public String toString() {
        return "CraftVillager";
    }

    @Override
    public EntityType getType() {
        return EntityType.VILLAGER;
    }

    @Override
    public Profession getProfession() {
        return CraftVillager.nmsToBukkitProfession(getHandle().getVillagerData().getProfession());
    }

    @Override
    public void setProfession(Profession profession) {
        Validate.notNull(profession);
        getHandle().setVillagerData(getHandle().getVillagerData().withProfession(CraftVillager.bukkitToNmsProfession(profession)));
    }

    @Override
    public Type getVillagerType() {
        return Type.valueOf(IRegistry.VILLAGER_TYPE.getKey(getHandle().getVillagerData().getType()).getKey().toUpperCase(Locale.ROOT));
    }

    @Override
    public void setVillagerType(Type type) {
        Validate.notNull(type);
        getHandle().setVillagerData(getHandle().getVillagerData().withType(IRegistry.VILLAGER_TYPE.get(CraftNamespacedKey.toMinecraft(type.getKey()))));
    }

    @Override
    public int getVillagerLevel() {
        return getHandle().getVillagerData().getLevel();
    }

    @Override
    public void setVillagerLevel(int level) {
        Preconditions.checkArgument(1 <= level && level <= 5, "level must be between [1, 5]");

        getHandle().setVillagerData(getHandle().getVillagerData().withLevel(level));
    }

    @Override
    public int getVillagerExperience() {
        return getHandle().getExperience();
    }

    @Override
    public void setVillagerExperience(int experience) {
        Preconditions.checkArgument(experience >= 0, "Experience must be positive");

        getHandle().setExperience(experience);
    }

    // Paper start
    @Override
    public int getRestocksToday() {
        return getHandle().getRestocksToday();
    }

    @Override
    public void setRestocksToday(int restocksToday) {
        getHandle().setRestocksToday(restocksToday);
    }
    // Paper end

    @Override
    public boolean sleep(Location location) {
        Preconditions.checkArgument(location != null, "Location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "Location needs to be in a world");
        Preconditions.checkArgument(location.getWorld().equals(getWorld()), "Cannot sleep across worlds");

        BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IBlockData iblockdata = getHandle().world.getType(position);
        if (!(iblockdata.getBlock() instanceof BlockBed)) {
            return false;
        }

        getHandle().entitySleep(position);
        return true;
    }

    @Override
    public void wakeup() {
        Preconditions.checkState(isSleeping(), "Cannot wakeup if not sleeping");

        getHandle().entityWakeup();
    }

    public static Profession nmsToBukkitProfession(VillagerProfession nms) {
        return Profession.valueOf(IRegistry.VILLAGER_PROFESSION.getKey(nms).getKey().toUpperCase(Locale.ROOT));
    }

    public static VillagerProfession bukkitToNmsProfession(Profession bukkit) {
        return IRegistry.VILLAGER_PROFESSION.get(CraftNamespacedKey.toMinecraft(bukkit.getKey()));
    }

    // Paper start - Add villager reputation API
    @Override
    public Reputation getReputation(UUID uniqueId) {
        net.minecraft.server.Reputation.a rep = getHandle().getReputation().getReputations().get(uniqueId);
        if (rep == null) {
            return new Reputation(Maps.newHashMap());
        }

        return rep.getPaperReputation();
    }

    @Override
    public Map<UUID, Reputation> getReputations() {
        return getHandle().getReputation().getReputations().entrySet()
            .stream()
            .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getPaperReputation()));
    }

    @Override
    public void setReputation(UUID uniqueId, Reputation reputation) {
        net.minecraft.server.Reputation.a nmsReputation =
            getHandle().getReputation().getReputations().computeIfAbsent(
                uniqueId,
                key -> new net.minecraft.server.Reputation.a()
            );
        nmsReputation.assignFromPaperReputation(reputation);
    }

    @Override
    public void setReputations(Map<UUID, Reputation> reputations) {
        for (Map.Entry<UUID, Reputation> entry : reputations.entrySet()) {
            setReputation(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clearReputations() {
        getHandle().getReputation().getReputations().clear();
    }
    // Paper end
}
