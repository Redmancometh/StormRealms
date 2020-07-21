package net.minecraft.server;

import com.google.common.collect.Streams;
import java.util.Collections;
import java.util.Set;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

public interface ICollisionAccess extends IBlockAccess {

    WorldBorder getWorldBorder();

    @Nullable
    IBlockAccess c(int i, int j);

    default boolean a(@Nullable Entity entity, VoxelShape voxelshape) {
        return true;
    }

    default boolean a(IBlockData iblockdata, BlockPosition blockposition, VoxelShapeCollision voxelshapecollision) {
        VoxelShape voxelshape = iblockdata.b((IBlockAccess) this, blockposition, voxelshapecollision);

        return voxelshape.isEmpty() || this.a((Entity) null, voxelshape.a((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ()));
    }

    default boolean i(Entity entity) {
        return this.a(entity, VoxelShapes.a(entity.getBoundingBox()));
    }

    default boolean a(AxisAlignedBB axisalignedbb) {
        return this.a((Entity) null, axisalignedbb, Collections.emptySet());
    }

    default boolean getCubes(Entity entity) {
        return this.a(entity, entity.getBoundingBox(), Collections.emptySet());
    }

    default boolean getCubes(Entity entity, AxisAlignedBB axisalignedbb) {
        return this.a(entity, axisalignedbb, Collections.emptySet());
    }

    default boolean a(@Nullable Entity entity, AxisAlignedBB axisalignedbb, Set<Entity> set) {
        try { if (entity != null) entity.collisionLoadChunks = true; // Paper
        // Paper start - reduce stream usage
        java.util.List<VoxelShape> blockCollisions = getBlockCollision(entity, axisalignedbb, true);
        for (int i = 0; i < blockCollisions.size(); i++) {
            VoxelShape blockCollision = blockCollisions.get(i);
            if (!blockCollision.isEmpty()) {
                return false;
            }
        }
        return getEntityCollisions(entity, axisalignedbb, set, true).isEmpty();
        // Paper end
        } finally { if (entity != null) entity.collisionLoadChunks = false; } // Paper
    }

    default java.util.List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AxisAlignedBB axisalignedbb, Set<Entity> set, boolean returnFast) { return java.util.Collections.emptyList(); } // Paper
    default Stream<VoxelShape> b(@Nullable Entity entity, AxisAlignedBB axisalignedbb, Set<Entity> set) {
        return Stream.empty();
    }

    default Stream<VoxelShape> c(@Nullable Entity entity, AxisAlignedBB axisalignedbb, Set<Entity> set) {
        // Paper start - reduce stream usage
        java.util.List<VoxelShape> blockCollisions = getBlockCollision(entity, axisalignedbb, false);
        java.util.List<VoxelShape> entityCollisions = getEntityCollisions(entity, axisalignedbb, set, false);
        return Stream.concat(blockCollisions.stream(), entityCollisions.stream());
        // Paper end
    }

    default Stream<VoxelShape> b(@Nullable final Entity entity, AxisAlignedBB axisalignedbb) {
        // Paper start - reduce stream usage
        java.util.List<VoxelShape> collision = getBlockCollision(entity, axisalignedbb, false);
        return !collision.isEmpty() ? collision.stream() : Stream.empty();
    }

    default java.util.List<VoxelShape> getBlockCollision(@Nullable final Entity entity, AxisAlignedBB axisalignedbb, boolean returnFast) {
        // Paper end
        int i = MathHelper.floor(axisalignedbb.minX - 1.0E-7D) - 1;
        int j = MathHelper.floor(axisalignedbb.maxX + 1.0E-7D) + 1;
        int k = MathHelper.floor(axisalignedbb.minY - 1.0E-7D) - 1;
        int l = MathHelper.floor(axisalignedbb.maxY + 1.0E-7D) + 1;
        int i1 = MathHelper.floor(axisalignedbb.minZ - 1.0E-7D) - 1;
        int j1 = MathHelper.floor(axisalignedbb.maxZ + 1.0E-7D) + 1;
        final VoxelShapeCollision voxelshapecollision = entity == null ? VoxelShapeCollision.a() : VoxelShapeCollision.a(entity);
        final CursorPosition cursorposition = new CursorPosition(i, k, i1, j, l, j1);
        final BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition();
        final VoxelShape voxelshape = VoxelShapes.a(axisalignedbb);

        // Paper start - reduce stream usage (this part done by Aikar)
        java.util.List<VoxelShape> collisions = new java.util.ArrayList<>();
        if (true) {//return StreamSupport.stream(new AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, 1280) {
            if (true) { //public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {*/ // Paper
                if (entity != null) {
                    // Paper end
                    //VoxelShape voxelshape1 = ICollisionAccess.this.getWorldBorder().a(); // Paper - only make if collides
                    boolean flag = !ICollisionAccess.this.getWorldBorder().isInBounds(entity.getBoundingBox().shrink(1.0E-7D)); // Paper
                    boolean flag1 = !ICollisionAccess.this.getWorldBorder().isInBounds(entity.getBoundingBox().g(1.0E-7D)); // Paper

                    if (!flag && flag1) {
                        collisions.add(ICollisionAccess.this.getWorldBorder().a());// Paper
                        if (returnFast) return collisions;
                    }
                }

                while (cursorposition.a()) {
                    int k1 = cursorposition.b();int x = k1; // Paper
                    int l1 = cursorposition.c();int y = l1; // Paper
                    int i2 = cursorposition.d();int z = i2; // Paper
                    int j2 = cursorposition.e();

                    if (j2 != 3) {
                        // Paper start - ensure we don't load chunks
                        //int k2 = k1 >> 4;
                        //int l2 = i2 >> 4;
                        boolean far = entity != null && MCUtil.distanceSq(entity.locX(), y, entity.locZ(), x, y, z) > 14;
                        blockposition_mutableblockposition.setValues(x, y, z);

                        boolean isRegionLimited = ICollisionAccess.this instanceof RegionLimitedWorldAccess;
                        IBlockData iblockdata = isRegionLimited ? Blocks.VOID_AIR.getBlockData() : ((!far && entity instanceof EntityPlayer) || (entity != null && entity.collisionLoadChunks)
                            ? ICollisionAccess.this.getType(blockposition_mutableblockposition)
                            : ICollisionAccess.this.getTypeIfLoaded(blockposition_mutableblockposition)
                        );
                        if (iblockdata == null) {
                            if (!(entity instanceof EntityPlayer) || entity.world.paperConfig.preventMovingIntoUnloadedChunks) {
                                collisions.add(VoxelShapes.of(far ? entity.getBoundingBox() : new AxisAlignedBB(new BlockPosition(x, y, z))));
                                if (returnFast) return collisions;
                            }
                        } else {
                            //blockposition_mutableblockposition.d(k1, l1, i2); // moved up
                            //IBlockData iblockdata = iblockaccess.getType(blockposition_mutableblockposition); // moved up
                            // Paper end

                            if (!iblockdata.isAir() && (j2 != 1 || iblockdata.f()) && (j2 != 2 || iblockdata.getBlock() == Blocks.MOVING_PISTON)) { // Paper - fast track air
                                VoxelShape voxelshape2 = iblockdata.b((IBlockAccess) ICollisionAccess.this, blockposition_mutableblockposition, voxelshapecollision);

                                // Paper start - Lithium Collision Optimizations
                                if (voxelshape2 == VoxelShapes.empty()) {
                                    continue;
                                }

                                if (voxelshape2 == VoxelShapes.fullCube()) {
                                    if (axisalignedbb.intersects(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D)) {
                                        collisions.add(voxelshape2.offset(x, y, z));
                                        if (returnFast) return collisions;
                                    }
                                } else {
                                    VoxelShape shape = voxelshape2.offset(x, y, z);
                                    if (VoxelShapes.applyOperation(shape, voxelshape, OperatorBoolean.AND)) {
                                        collisions.add(shape);
                                        if (returnFast) return collisions;
                                    }
                                    // Paper end
                                }
                            }
                        }
                    }
                }

                //return false; // Paper
            }
        } //}, false);
        return collisions; // Paper
    }
}
