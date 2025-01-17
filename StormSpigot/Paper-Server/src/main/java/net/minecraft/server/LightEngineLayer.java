package net.minecraft.server;

import java.util.Arrays;
import javax.annotation.Nullable;
import org.apache.commons.lang3.mutable.MutableInt;

public abstract class LightEngineLayer<M extends LightEngineStorageArray<M>, S extends LightEngineStorage<M>> extends LightEngineGraph implements LightEngineLayerEventListener {

    private static final EnumDirection[] e = EnumDirection.values();
    protected final ILightAccess a;
    protected final EnumSkyBlock b;
    protected final S c;
    private boolean f;
    protected final BlockPosition.MutableBlockPosition d = new BlockPosition.MutableBlockPosition(); protected final BlockPosition.MutableBlockPosition pos = d; // Paper
    private final long[] g = new long[2];
    private final IChunkAccess[] h = new IChunkAccess[2]; // Paper

    public LightEngineLayer(ILightAccess ilightaccess, EnumSkyBlock enumskyblock, S s0) {
        super(16, 256, 8192);
        this.a = ilightaccess;
        this.b = enumskyblock;
        this.c = s0;
        this.d();
    }

    @Override
    protected void f(long i) {
        this.c.d();
        if (this.c.g(SectionPosition.e(i))) {
            super.f(i);
        }

    }

    @Nullable
    private IChunkAccess a(int i, int j) { // Paper
        long k = ChunkCoordIntPair.pair(i, j);

        for (int l = 0; l < 2; ++l) {
            if (k == this.g[l]) {
                return this.h[l];
            }
        }

        IChunkAccess iblockaccess = (IChunkAccess) this.a.c(i, j); // Paper

        for (int i1 = 1; i1 > 0; --i1) {
            this.g[i1] = this.g[i1 - 1];
            this.h[i1] = this.h[i1 - 1];
        }

        this.g[0] = k;
        this.h[0] = iblockaccess;
        return iblockaccess;
    }

    private void d() {
        Arrays.fill(this.g, ChunkCoordIntPair.a);
        Arrays.fill(this.h, (Object) null);
    }

    // Paper start - unused, optimized versions below, comment out to detect changes
//    protected IBlockData a(long i, @Nullable MutableInt mutableint) {
//        if (i == Long.MAX_VALUE) {
//            if (mutableint != null) {
//                mutableint.setValue(0);
//            }
//
//            return Blocks.AIR.getBlockData();
//        } else {
//            int j = SectionPosition.a(BlockPosition.b(i));
//            int k = SectionPosition.a(BlockPosition.d(i));
//            IBlockAccess iblockaccess = this.a(j, k);
//
//            if (iblockaccess == null) {
//                if (mutableint != null) {
//                    mutableint.setValue(16);
//                }
//
//                return Blocks.BEDROCK.getBlockData();
//            } else {
//                this.d.g(i);
//                IBlockData iblockdata = iblockaccess.getType(this.d);
//                boolean flag = iblockdata.o() && iblockdata.g();
//
//                if (mutableint != null) {
//                    mutableint.setValue(iblockdata.b(this.a.getWorld(), (BlockPosition) this.d));
//                }
//
//                return flag ? iblockdata : Blocks.AIR.getBlockData();
//            }
//        }
//    }
    // optimized method with less branching for when scenarios arent needed.
    // avoid using mutable version if can
    protected final IBlockData getBlockOptimized(int x, int y, int z, MutableInt mutableint) {
        IChunkAccess iblockaccess = this.a(x >> 4, z >> 4);

        if (iblockaccess == null) {
            mutableint.setValue(16);
            return Blocks.BEDROCK.getBlockData();
        } else {
            this.pos.setValues(x, y, z);
            IBlockData iblockdata = iblockaccess.getType(x, y, z);
            mutableint.setValue(iblockdata.b(this.a.getWorld(), this.pos));
            return iblockdata.o() && iblockdata.g() ? iblockdata : Blocks.AIR.getBlockData();
        }
    }
    protected final IBlockData getBlockOptimized(int x, int y, int z) {
        IChunkAccess iblockaccess = this.a(x >> 4, z >> 4);

        if (iblockaccess == null) {
            return Blocks.BEDROCK.getBlockData();
        } else {
            IBlockData iblockdata = iblockaccess.getType(x, y, z);
            return iblockdata.o() && iblockdata.g() ? iblockdata : Blocks.AIR.getBlockData();
        }
    }
    // Paper end

    protected VoxelShape a(IBlockData iblockdata, long i, EnumDirection enumdirection) {
        return iblockdata.o() ? iblockdata.a(this.a.getWorld(), this.d.g(i), enumdirection) : VoxelShapes.a();
    }

    public static int a(IBlockAccess iblockaccess, IBlockData iblockdata, BlockPosition blockposition, IBlockData iblockdata1, BlockPosition blockposition1, EnumDirection enumdirection, int i) {
        boolean flag = iblockdata.o() && iblockdata.g();
        boolean flag1 = iblockdata1.o() && iblockdata1.g();

        if (!flag && !flag1) {
            return i;
        } else {
            VoxelShape voxelshape = flag ? iblockdata.j(iblockaccess, blockposition) : VoxelShapes.a();
            VoxelShape voxelshape1 = flag1 ? iblockdata1.j(iblockaccess, blockposition1) : VoxelShapes.a();

            return VoxelShapes.b(voxelshape, voxelshape1, enumdirection) ? 16 : i;
        }
    }

    @Override
    protected boolean a(long i) {
        return i == Long.MAX_VALUE;
    }

    @Override
    protected int a(long i, long j, int k) {
        return 0;
    }

    @Override
    protected int c(long i) {
        return i == Long.MAX_VALUE ? 0 : 15 - this.c.i(i);
    }

    protected int getNibbleLightInverse(NibbleArray nibblearray, int x, int y, int z) { return 15 - nibblearray.a(x & 15, y & 15, z & 15); } // Paper - x/y/z version of below
    protected int a(NibbleArray nibblearray, long i) {
        return 15 - nibblearray.a((int) (i >> 38) & 15, (int) ((i << 52) >> 52) & 15, (int) ((i << 26) >> 38) & 15); // Paper
    }

    @Override
    protected void a(long i, int j) {
        this.c.b(i, Math.min(15, 15 - j));
    }

    @Override
    protected int b(long i, long j, int k) {
        return 0;
    }

    public boolean a() {
        return this.b() || this.c.b() || this.c.a();
    }

    public int a(int i, boolean flag, boolean flag1) {
        if (!this.f) {
            if (this.c.b()) {
                i = this.c.b(i);
                if (i == 0) {
                    return i;
                }
            }

            this.c.a(this, flag, flag1);
        }

        this.f = true;
        if (this.b()) {
            i = this.b(i);
            this.d();
            if (i == 0) {
                return i;
            }
        }

        this.f = false;
        this.c.e();
        return i;
    }

    protected void a(long i, @Nullable NibbleArray nibblearray) {
        this.c.a(i, nibblearray);
    }

    @Nullable
    @Override
    public NibbleArray a(SectionPosition sectionposition) {
        return this.c.h(sectionposition.v());
    }

    @Override
    public int b(BlockPosition blockposition) {
        return this.c.d(blockposition.asLong());
    }

    public void a(BlockPosition blockposition) {
        long i = blockposition.asLong();

        this.f(i);
        EnumDirection[] aenumdirection = LightEngineLayer.e;
        int j = aenumdirection.length;

        for (int k = 0; k < j; ++k) {
            EnumDirection enumdirection = aenumdirection[k];

            this.f(BlockPosition.a(i, enumdirection));
        }

    }

    public void a(BlockPosition blockposition, int i) {}

    @Override
    public void a(SectionPosition sectionposition, boolean flag) {
        this.c.d(sectionposition.v(), flag);
    }

    public void a(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        long i = SectionPosition.f(SectionPosition.b(chunkcoordintpair.x, 0, chunkcoordintpair.z));

        this.c.b(i, flag);
    }

    public void b(ChunkCoordIntPair chunkcoordintpair, boolean flag) {
        long i = SectionPosition.f(SectionPosition.b(chunkcoordintpair.x, 0, chunkcoordintpair.z));

        this.c.c(i, flag);
    }
}
