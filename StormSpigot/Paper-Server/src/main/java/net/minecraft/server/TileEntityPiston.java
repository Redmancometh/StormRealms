package net.minecraft.server;

import java.util.Iterator;
import java.util.List;

public class TileEntityPiston extends TileEntity implements ITickable {

    private IBlockData a;
    private EnumDirection b;
    private boolean c;
    private boolean g;
    private static final ThreadLocal<EnumDirection> h = ThreadLocal.withInitial(() -> {
        return null;
    });
    private float i;
    private float j;
    private long k;

    public TileEntityPiston() {
        super(TileEntityTypes.PISTON);
    }

    public TileEntityPiston(IBlockData iblockdata, EnumDirection enumdirection, boolean flag, boolean flag1) {
        this();
        this.a = iblockdata;
        this.b = enumdirection;
        this.c = flag;
        this.g = flag1;
    }

    @Override
    public NBTTagCompound b() {
        return this.save(new NBTTagCompound());
    }

    public boolean d() {
        return this.c;
    }

    public EnumDirection f() {
        return this.b;
    }

    public boolean h() {
        return this.g;
    }

    public float a(float f) {
        if (f > 1.0F) {
            f = 1.0F;
        }

        return MathHelper.g(f, this.j, this.i);
    }

    private float e(float f) {
        return this.c ? f - 1.0F : 1.0F - f;
    }

    private IBlockData x() {
        return !this.d() && this.h() && this.a.getBlock() instanceof BlockPiston ? (IBlockData) ((IBlockData) Blocks.PISTON_HEAD.getBlockData().set(BlockPistonExtension.TYPE, this.a.getBlock() == Blocks.STICKY_PISTON ? BlockPropertyPistonType.STICKY : BlockPropertyPistonType.DEFAULT)).set(BlockPistonExtension.FACING, this.a.get(BlockPiston.FACING)) : this.a;
    }

    private void f(float f) {
        EnumDirection enumdirection = this.j();
        double d0 = (double) (f - this.i);
        VoxelShape voxelshape = this.x().getCollisionShape(this.world, this.getPosition());

        if (!voxelshape.isEmpty()) {
            List<AxisAlignedBB> list = voxelshape.d();
            AxisAlignedBB axisalignedbb = this.a(this.a(list));
            List<Entity> list1 = this.world.getEntities((Entity) null, PistonUtil.a(axisalignedbb, enumdirection, d0).b(axisalignedbb));

            if (!list1.isEmpty()) {
                boolean flag = this.a.getBlock() == Blocks.SLIME_BLOCK;
                Iterator iterator = list1.iterator();

                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();

                    if (entity.getPushReaction() != EnumPistonReaction.IGNORE) {
                        if (flag) {
                            Vec3D vec3d = entity.getMot();
                            double d1 = vec3d.x;
                            double d2 = vec3d.y;
                            double d3 = vec3d.z;

                            switch (enumdirection.m()) {
                                case X:
                                    d1 = (double) enumdirection.getAdjacentX();
                                    break;
                                case Y:
                                    d2 = (double) enumdirection.getAdjacentY();
                                    break;
                                case Z:
                                    d3 = (double) enumdirection.getAdjacentZ();
                            }

                            entity.setMot(d1, d2, d3);
                        }

                        double d4 = 0.0D;
                        Iterator iterator1 = list.iterator();

                        while (true) {
                            if (iterator1.hasNext()) {
                                AxisAlignedBB axisalignedbb1 = (AxisAlignedBB) iterator1.next();
                                AxisAlignedBB axisalignedbb2 = PistonUtil.a(this.a(axisalignedbb1), enumdirection, d0);
                                AxisAlignedBB axisalignedbb3 = entity.getBoundingBox();

                                if (!axisalignedbb2.c(axisalignedbb3)) {
                                    continue;
                                }

                                d4 = Math.max(d4, a(axisalignedbb2, enumdirection, axisalignedbb3));
                                if (d4 < d0) {
                                    continue;
                                }
                            }

                            if (d4 > 0.0D) {
                                d4 = Math.min(d4, d0) + 0.01D;
                                a(enumdirection, entity, d4, enumdirection);
                                if (!this.c && this.g) {
                                    this.a(entity, enumdirection, d0);
                                }
                            }
                            break;
                        }
                    }
                }

            }
        }
    }

    private static void a(EnumDirection enumdirection, Entity entity, double d0, EnumDirection enumdirection1) {
        TileEntityPiston.h.set(enumdirection);
        entity.move(EnumMoveType.PISTON, new Vec3D(d0 * (double) enumdirection1.getAdjacentX(), d0 * (double) enumdirection1.getAdjacentY(), d0 * (double) enumdirection1.getAdjacentZ()));
        TileEntityPiston.h.set(null); // Paper - decompile fix
    }

    private void g(float f) {
        if (this.y()) {
            EnumDirection enumdirection = this.j();

            if (enumdirection.m().c()) {
                double d0 = this.a.getCollisionShape(this.world, this.position).c(EnumDirection.EnumAxis.Y);
                AxisAlignedBB axisalignedbb = this.a(new AxisAlignedBB(0.0D, d0, 0.0D, 1.0D, 1.5000000999999998D, 1.0D));
                double d1 = (double) (f - this.i);
                List<Entity> list = this.world.getEntities((Entity) null, axisalignedbb, (entity) -> {
                    return a(axisalignedbb, entity);
                });
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    Entity entity = (Entity) iterator.next();

                    a(enumdirection, entity, d1, enumdirection);
                }

            }
        }
    }

    private static boolean a(AxisAlignedBB axisalignedbb, Entity entity) {
        return entity.getPushReaction() == EnumPistonReaction.NORMAL && entity.onGround && entity.locX() >= axisalignedbb.minX && entity.locX() <= axisalignedbb.maxX && entity.locZ() >= axisalignedbb.minZ && entity.locZ() <= axisalignedbb.maxZ;
    }

    private boolean y() {
        return this.a.getBlock() == Blocks.HONEY_BLOCK;
    }

    public EnumDirection j() {
        return this.c ? this.b : this.b.opposite();
    }

    private AxisAlignedBB a(List<AxisAlignedBB> list) {
        double d0 = 0.0D;
        double d1 = 0.0D;
        double d2 = 0.0D;
        double d3 = 1.0D;
        double d4 = 1.0D;
        double d5 = 1.0D;

        AxisAlignedBB axisalignedbb;

        for (Iterator iterator = list.iterator(); iterator.hasNext(); d5 = Math.max(axisalignedbb.maxZ, d5)) {
            axisalignedbb = (AxisAlignedBB) iterator.next();
            d0 = Math.min(axisalignedbb.minX, d0);
            d1 = Math.min(axisalignedbb.minY, d1);
            d2 = Math.min(axisalignedbb.minZ, d2);
            d3 = Math.max(axisalignedbb.maxX, d3);
            d4 = Math.max(axisalignedbb.maxY, d4);
        }

        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    private static double a(AxisAlignedBB axisalignedbb, EnumDirection enumdirection, AxisAlignedBB axisalignedbb1) {
        switch (enumdirection) {
            case EAST:
                return axisalignedbb.maxX - axisalignedbb1.minX;
            case WEST:
                return axisalignedbb1.maxX - axisalignedbb.minX;
            case UP:
            default:
                return axisalignedbb.maxY - axisalignedbb1.minY;
            case DOWN:
                return axisalignedbb1.maxY - axisalignedbb.minY;
            case SOUTH:
                return axisalignedbb.maxZ - axisalignedbb1.minZ;
            case NORTH:
                return axisalignedbb1.maxZ - axisalignedbb.minZ;
        }
    }

    private AxisAlignedBB a(AxisAlignedBB axisalignedbb) {
        double d0 = (double) this.e(this.i);

        return axisalignedbb.d((double) this.position.getX() + d0 * (double) this.b.getAdjacentX(), (double) this.position.getY() + d0 * (double) this.b.getAdjacentY(), (double) this.position.getZ() + d0 * (double) this.b.getAdjacentZ());
    }

    private void a(Entity entity, EnumDirection enumdirection, double d0) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        AxisAlignedBB axisalignedbb1 = VoxelShapes.b().getBoundingBox().a(this.position);

        if (axisalignedbb.c(axisalignedbb1)) {
            EnumDirection enumdirection1 = enumdirection.opposite();
            double d1 = a(axisalignedbb1, enumdirection1, axisalignedbb) + 0.01D;
            double d2 = a(axisalignedbb1, enumdirection1, axisalignedbb.a(axisalignedbb1)) + 0.01D;

            if (Math.abs(d1 - d2) < 0.01D) {
                d1 = Math.min(d1, d0) + 0.01D;
                a(enumdirection, entity, d1, enumdirection1);
            }
        }

    }

    public IBlockData k() {
        return this.a;
    }

    public void l() {
        if (this.j < 1.0F && this.world != null) {
            this.i = 1.0F;
            this.j = this.i;
            this.world.removeTileEntity(this.position);
            this.ab_();
            if (this.world.getType(this.position).getBlock() == Blocks.MOVING_PISTON) {
                IBlockData iblockdata;

                if (this.g) {
                    iblockdata = Blocks.AIR.getBlockData();
                } else {
                    iblockdata = Block.b(this.a, (GeneratorAccess) this.world, this.position);
                }

                this.world.setTypeAndData(this.position, iblockdata, 3);
                this.world.a(this.position, iblockdata.getBlock(), this.position);
            }
        }

    }

    @Override
    public void tick() {
        this.k = this.world.getTime();
        this.j = this.i;
        if (this.j >= 1.0F) {
            this.world.removeTileEntity(this.position);
            this.ab_();
            if (this.a != null && this.world.getType(this.position).getBlock() == Blocks.MOVING_PISTON) {
                IBlockData iblockdata = Block.b(this.a, (GeneratorAccess) this.world, this.position);

                if (iblockdata.isAir()) {
                    this.world.setTypeAndData(this.position, this.a, com.destroystokyo.paper.PaperConfig.allowPistonDuplication ? 84 : (84 | 2)); // Paper - force notify (flag 2), it's possible the set type by the piston block (which doesn't notify) set this block to air
                    Block.a(this.a, iblockdata, this.world, this.position, 3);
                } else {
                    if (iblockdata.b((IBlockState) BlockProperties.C) && (Boolean) iblockdata.get(BlockProperties.C)) {
                        iblockdata = (IBlockData) iblockdata.set(BlockProperties.C, false);
                    }

                    this.world.setTypeAndData(this.position, iblockdata, 67);
                    this.world.a(this.position, iblockdata.getBlock(), this.position);
                }
            }

        } else {
            float f = this.i + 0.5F;

            this.f(f);
            this.g(f);
            this.i = f;
            if (this.i >= 1.0F) {
                this.i = 1.0F;
            }

        }
    }

    @Override
    public void load(NBTTagCompound nbttagcompound) {
        super.load(nbttagcompound);
        this.a = GameProfileSerializer.d(nbttagcompound.getCompound("blockState"));
        this.b = EnumDirection.fromType1(nbttagcompound.getInt("facing"));
        this.i = nbttagcompound.getFloat("progress");
        this.j = this.i;
        this.c = nbttagcompound.getBoolean("extending");
        this.g = nbttagcompound.getBoolean("source");
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);
        nbttagcompound.set("blockState", GameProfileSerializer.a(this.a));
        nbttagcompound.setInt("facing", this.b.b());
        nbttagcompound.setFloat("progress", this.j);
        nbttagcompound.setBoolean("extending", this.c);
        nbttagcompound.setBoolean("source", this.g);
        return nbttagcompound;
    }

    public VoxelShape a(IBlockAccess iblockaccess, BlockPosition blockposition) {
        VoxelShape voxelshape;

        if (!this.c && this.g) {
            voxelshape = ((IBlockData) this.a.set(BlockPiston.EXTENDED, true)).getCollisionShape(iblockaccess, blockposition);
        } else {
            voxelshape = VoxelShapes.a();
        }

        EnumDirection enumdirection = (EnumDirection) TileEntityPiston.h.get();

        if ((double) this.i < 1.0D && enumdirection == this.j()) {
            return voxelshape;
        } else {
            IBlockData iblockdata;

            if (this.h()) {
                iblockdata = (IBlockData) ((IBlockData) Blocks.PISTON_HEAD.getBlockData().set(BlockPistonExtension.FACING, this.b)).set(BlockPistonExtension.SHORT, this.c != 1.0F - this.i < 4.0F);
            } else {
                iblockdata = this.a;
            }

            float f = this.e(this.i);
            double d0 = (double) ((float) this.b.getAdjacentX() * f);
            double d1 = (double) ((float) this.b.getAdjacentY() * f);
            double d2 = (double) ((float) this.b.getAdjacentZ() * f);

            return VoxelShapes.a(voxelshape, iblockdata.getCollisionShape(iblockaccess, blockposition).a(d0, d1, d2));
        }
    }

    public long m() {
        return this.k;
    }
}
