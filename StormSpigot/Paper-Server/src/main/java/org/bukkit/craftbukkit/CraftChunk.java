package org.bukkit.craftbukkit;

import com.google.common.base.Preconditions;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.server.BiomeStorage;
import net.minecraft.server.BlockPosition;
import net.minecraft.server.Blocks;
import net.minecraft.server.ChunkCoordIntPair;
import net.minecraft.server.ChunkSection;
import net.minecraft.server.DataPaletteBlock;
import net.minecraft.server.EnumSkyBlock;
import net.minecraft.server.GameProfileSerializer;
import net.minecraft.server.HeightMap;
import net.minecraft.server.IBlockData;
import net.minecraft.server.LightEngine;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NibbleArray;
import net.minecraft.server.SectionPosition;
import net.minecraft.server.SeededRandom;
import net.minecraft.server.WorldChunkManager;
import net.minecraft.server.WorldServer;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public class CraftChunk implements Chunk {
    private WeakReference<net.minecraft.server.Chunk> weakChunk;
    private final WorldServer worldServer;
    private final int x;
    private final int z;
    private static final DataPaletteBlock<IBlockData> emptyBlockIDs = new ChunkSection(0, null, null, true).getBlocks(); // Paper - Anti-Xray - Add parameters
    private static final byte[] emptyLight = new byte[2048];

    public CraftChunk(net.minecraft.server.Chunk chunk) {
        this.weakChunk = new WeakReference<net.minecraft.server.Chunk>(chunk);

        worldServer = (WorldServer) getHandle().world;
        x = getHandle().getPos().x;
        z = getHandle().getPos().z;
    }

    @Override
    public World getWorld() {
        return worldServer.getWorld();
    }

    public CraftWorld getCraftWorld() {
        return (CraftWorld) getWorld();
    }

    public net.minecraft.server.Chunk getHandle() {
        net.minecraft.server.Chunk c = weakChunk.get();

        if (c == null) {
            c = worldServer.getChunkAt(x, z);

            weakChunk = new WeakReference<net.minecraft.server.Chunk>(c);
        }

        return c;
    }

    void breakLink() {
        weakChunk.clear();
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "CraftChunk{" + "x=" + getX() + "z=" + getZ() + '}';
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        validateChunkCoordinates(x, y, z);

        return new CraftBlock(worldServer, new BlockPosition((this.x << 4) | x, y, (this.z << 4) | z));
    }

    @Override
    public Entity[] getEntities() {
        if (!isLoaded()) {
            getWorld().getChunkAt(x, z); // Transient load for this tick
        }
        int count = 0, index = 0;
        net.minecraft.server.Chunk chunk = getHandle();

        for (int i = 0; i < 16; i++) {
            count += chunk.entitySlices[i].size();
        }

        Entity[] entities = new Entity[count];

        for (int i = 0; i < 16; i++) {
            // Paper start - speed up (was with chunk.entitySlices[i].toArray() and cast checks which costs a lot of performance if called often)
            for (net.minecraft.server.Entity entity : chunk.entitySlices[i]) {
                if (entity == null) {
                    continue;
                }
                entities[index++] = entity.getBukkitEntity();
            }
            // Paper end
        }

        return entities;
    }

    @Override
    public BlockState[] getTileEntities() {
        // Paper start
        return getTileEntities(true);
    }

    @Override
    public BlockState[] getTileEntities(boolean useSnapshot) {
        if (!isLoaded()) {
            getWorld().getChunkAt(x, z); // Transient load for this tick
        }
        // Paper end
        int index = 0;
        net.minecraft.server.Chunk chunk = getHandle();

        BlockState[] entities = new BlockState[chunk.tileEntities.size()];

        for (Object obj : chunk.tileEntities.keySet().toArray()) {
            if (!(obj instanceof BlockPosition)) {
                continue;
            }

            BlockPosition position = (BlockPosition) obj;
            entities[index++] = worldServer.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ()).getState(useSnapshot); // Paper
        }

        return entities;
    }

    @Override
    public boolean isLoaded() {
        return getWorld().isChunkLoaded(this);
    }

    @Override
    public boolean load() {
        return getWorld().loadChunk(getX(), getZ(), true);
    }

    @Override
    public boolean load(boolean generate) {
        return getWorld().loadChunk(getX(), getZ(), generate);
    }

    @Override
    public boolean unload() {
        return getWorld().unloadChunk(getX(), getZ());
    }

    @Override
    public boolean isSlimeChunk() {
        // 987234911L is deterimined in EntitySlime when seeing if a slime can spawn in a chunk
        return SeededRandom.a(getX(), getZ(), getWorld().getSeed(), worldServer.spigotConfig.slimeSeed).nextInt(10) == 0;
    }

    @Override
    public boolean unload(boolean save) {
        return getWorld().unloadChunk(getX(), getZ(), save);
    }

    @Override
    public boolean isForceLoaded() {
        return getWorld().isChunkForceLoaded(getX(), getZ());
    }

    @Override
    public void setForceLoaded(boolean forced) {
        getWorld().setChunkForceLoaded(getX(), getZ(), forced);
    }

    @Override
    public boolean addPluginChunkTicket(Plugin plugin) {
        return getWorld().addPluginChunkTicket(getX(), getZ(), plugin);
    }

    @Override
    public boolean removePluginChunkTicket(Plugin plugin) {
        return getWorld().removePluginChunkTicket(getX(), getZ(), plugin);
    }

    @Override
    public Collection<Plugin> getPluginChunkTickets() {
        return getWorld().getPluginChunkTickets(getX(), getZ());
    }

    @Override
    public long getInhabitedTime() {
        return getHandle().getInhabitedTime();
    }

    @Override
    public void setInhabitedTime(long ticks) {
        Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");

        getHandle().setInhabitedTime(ticks);
    }

    @Override
    public boolean contains(BlockData block) {
        Preconditions.checkArgument(block != null, "Block cannot be null");

        IBlockData nms = ((CraftBlockData) block).getState();
        for (ChunkSection section : getHandle().getSections()) {
            if (section != null && section.getBlocks().contains(nms)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ChunkSnapshot getChunkSnapshot() {
        return getChunkSnapshot(true, false, false);
    }

    @Override
    public ChunkSnapshot getChunkSnapshot(boolean includeMaxBlockY, boolean includeBiome, boolean includeBiomeTempRain) {
        net.minecraft.server.Chunk chunk = getHandle();

        ChunkSection[] cs = chunk.getSections();
        DataPaletteBlock[] sectionBlockIDs = new DataPaletteBlock[cs.length];
        byte[][] sectionSkyLights = new byte[cs.length][];
        byte[][] sectionEmitLights = new byte[cs.length][];
        boolean[] sectionEmpty = new boolean[cs.length];

        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == null) { // Section is empty?
                sectionBlockIDs[i] = emptyBlockIDs;
                sectionSkyLights[i] = emptyLight;
                sectionEmitLights[i] = emptyLight;
                sectionEmpty[i] = true;
            } else { // Not empty
                NBTTagCompound data = new NBTTagCompound();
                cs[i].getBlocks().a(data, "Palette", "BlockStates");

                DataPaletteBlock blockids = new DataPaletteBlock<>(ChunkSection.GLOBAL_PALETTE, net.minecraft.server.Block.REGISTRY_ID, GameProfileSerializer::d, GameProfileSerializer::a, Blocks.AIR.getBlockData(), null, false); // TODO: snapshot whole ChunkSection // Paper - Anti-Xray - Add no predefined block data and don't initialize because it's done in the line below internally
                blockids.a(data.getList("Palette", CraftMagicNumbers.NBT.TAG_COMPOUND), data.getLongArray("BlockStates"));

                sectionBlockIDs[i] = blockids;

                LightEngine lightengine = chunk.world.getChunkProvider().getLightEngine();
                NibbleArray skyLightArray = lightengine.a(EnumSkyBlock.SKY).a(SectionPosition.a(x, i, z));
                if (skyLightArray == null) {
                    sectionSkyLights[i] = emptyLight;
                } else {
                    sectionSkyLights[i] = new byte[2048];
                    System.arraycopy(skyLightArray.getIfSet(), 0, sectionSkyLights[i], 0, 2048); // Paper
                }
                NibbleArray emitLightArray = lightengine.a(EnumSkyBlock.BLOCK).a(SectionPosition.a(x, i, z));
                if (emitLightArray == null) {
                    sectionEmitLights[i] = emptyLight;
                } else {
                    sectionEmitLights[i] = new byte[2048];
                    System.arraycopy(emitLightArray.getIfSet(), 0, sectionEmitLights[i], 0, 2048); // Paper
                }
            }
        }

        HeightMap hmap = null;

        if (includeMaxBlockY) {
            hmap = new HeightMap(null, HeightMap.Type.MOTION_BLOCKING);
            hmap.a(chunk.heightMap.get(HeightMap.Type.MOTION_BLOCKING).a());
        }

        BiomeStorage biome = null;

        if (includeBiome || includeBiomeTempRain) {
            biome = chunk.getBiomeIndex().b();
        }

        World world = getWorld();
        return new CraftChunkSnapshot(getX(), getZ(), world.getName(), world.getFullTime(), sectionBlockIDs, sectionSkyLights, sectionEmitLights, sectionEmpty, hmap, biome);
    }

    public static ChunkSnapshot getEmptyChunkSnapshot(int x, int z, CraftWorld world, boolean includeBiome, boolean includeBiomeTempRain) {
        BiomeStorage biome = null;

        if (includeBiome || includeBiomeTempRain) {
            WorldChunkManager wcm = world.getHandle().getChunkProvider().getChunkGenerator().getWorldChunkManager();
            biome = new BiomeStorage(new ChunkCoordIntPair(x, z), wcm);
        }

        /* Fill with empty data */
        int hSection = world.getMaxHeight() >> 4;
        DataPaletteBlock[] blockIDs = new DataPaletteBlock[hSection];
        byte[][] skyLight = new byte[hSection][];
        byte[][] emitLight = new byte[hSection][];
        boolean[] empty = new boolean[hSection];

        for (int i = 0; i < hSection; i++) {
            blockIDs[i] = emptyBlockIDs;
            skyLight[i] = emptyLight;
            emitLight[i] = emptyLight;
            empty[i] = true;
        }

        return new CraftChunkSnapshot(x, z, world.getName(), world.getFullTime(), blockIDs, skyLight, emitLight, empty, new HeightMap(null, HeightMap.Type.MOTION_BLOCKING), biome);
    }

    static void validateChunkCoordinates(int x, int y, int z) {
        Preconditions.checkArgument(0 <= x && x <= 15, "x out of range (expected 0-15, got %s)", x);
        Preconditions.checkArgument(0 <= y && y <= 255, "y out of range (expected 0-255, got %s)", y);
        Preconditions.checkArgument(0 <= z && z <= 15, "z out of range (expected 0-15, got %s)", z);
    }

    static {
        Arrays.fill(emptyLight, (byte) 0xFF);
    }
}
