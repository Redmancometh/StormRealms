package net.minecraft.server;

import java.util.Iterator;
import java.util.Random;

// CraftBukkit start
import org.bukkit.TreeType;
// CraftBukkit end

public class BlockMushroom extends BlockPlant implements IBlockFragilePlantElement {

    protected static final VoxelShape a = Block.a(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

    public BlockMushroom(Block.Info block_info) {
        super(block_info);
    }

    @Override
    public VoxelShape a(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        return BlockMushroom.a;
    }

    @Override
    public void tick(IBlockData iblockdata, WorldServer worldserver, BlockPosition blockposition, Random random) {
        if (random.nextInt(Math.max(1, (int) (100.0F / worldserver.spigotConfig.mushroomModifier) * 25)) == 0) { // Spigot
            int i = 5;
            boolean flag = true;
            Iterator iterator = BlockPosition.a(blockposition.b(-4, -1, -4), blockposition.b(4, 1, 4)).iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition1 = (BlockPosition) iterator.next();

                if (worldserver.getType(blockposition1).getBlock() == this) {
                    --i;
                    if (i <= 0) {
                        return;
                    }
                }
            }

            BlockPosition blockposition2 = blockposition.b(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

            for (int j = 0; j < 4; ++j) {
                if (worldserver.isEmpty(blockposition2) && iblockdata.canPlace(worldserver, blockposition2)) {
                    blockposition = blockposition2;
                }

                blockposition2 = blockposition.b(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
            }

            if (worldserver.isEmpty(blockposition2) && iblockdata.canPlace(worldserver, blockposition2)) {
                org.bukkit.craftbukkit.event.CraftEventFactory.handleBlockSpreadEvent(worldserver, blockposition, blockposition2, iblockdata, 2); // CraftBukkit
            }
        }

    }

    @Override
    protected boolean a_(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return iblockdata.g(iblockaccess, blockposition);
    }

    @Override
    public boolean canPlace(IBlockData iblockdata, IWorldReader iworldreader, BlockPosition blockposition) {
        BlockPosition blockposition1 = blockposition.down();
        IBlockData iblockdata1 = iworldreader.getType(blockposition1);
        Block block = iblockdata1.getBlock();

        return block != Blocks.MYCELIUM && block != Blocks.PODZOL ? iworldreader.getLightLevel(blockposition, 0) < 13 && this.a_(iblockdata1, iworldreader, blockposition1) : true;
    }

    public boolean a(WorldServer worldserver, BlockPosition blockposition, IBlockData iblockdata, Random random) {
        worldserver.a(blockposition, false);
        WorldGenFeatureConfigured worldgenfeatureconfigured;

        if (this == Blocks.BROWN_MUSHROOM) {
            BlockSapling.treeType = TreeType.BROWN_MUSHROOM; // CraftBukkit
            worldgenfeatureconfigured = WorldGenerator.HUGE_BROWN_MUSHROOM.b(BiomeDecoratorGroups.HUGE_BROWN_MUSHROOM); // CraftBukkit - decompile error
        } else {
            if (this != Blocks.RED_MUSHROOM) {
                worldserver.setTypeAndData(blockposition, iblockdata, 3);
                return false;
            }

            BlockSapling.treeType = TreeType.RED_MUSHROOM; // CraftBukkit
            worldgenfeatureconfigured = WorldGenerator.HUGE_RED_MUSHROOM.b(BiomeDecoratorGroups.HUGE_RED_MUSHROOM); // CraftBukkit - decompile error
        }

        if (worldgenfeatureconfigured.a(worldserver, worldserver.getChunkProvider().getChunkGenerator(), random, blockposition)) {
            return true;
        } else {
            worldserver.setTypeAndData(blockposition, iblockdata, 3);
            return false;
        }
    }

    @Override
    public boolean a(IBlockAccess iblockaccess, BlockPosition blockposition, IBlockData iblockdata, boolean flag) {
        return true;
    }

    @Override
    public boolean a(World world, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        return (double) random.nextFloat() < 0.4D;
    }

    @Override
    public void a(WorldServer worldserver, Random random, BlockPosition blockposition, IBlockData iblockdata) {
        this.a(worldserver, blockposition, iblockdata, random);
    }

    @Override
    public boolean h(IBlockData iblockdata, IBlockAccess iblockaccess, BlockPosition blockposition) {
        return true;
    }
}
