package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.Location;
import org.bukkit.event.block.BlockExplodeEvent;
// CraftBukkit end

public class Explosion {

    private final boolean a;
    private final Explosion.Effect b;
    private final Random c = new Random();
    private final World world;
    private final double posX;
    private final double posY;
    private final double posZ;
    @Nullable
    public final Entity source;
    private final float size;
    private DamageSource j;
    private final List<BlockPosition> blocks = Lists.newArrayList();
    private final Map<EntityHuman, Vec3D> l = Maps.newHashMap();
    public boolean wasCanceled = false; // CraftBukkit - add field

    public Explosion(World world, @Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag, Explosion.Effect explosion_effect) {
        this.world = world;
        this.source = entity;
        this.size = (float) Math.max(f, 0.0); // CraftBukkit - clamp bad values
        this.posX = d0;
        this.posY = d1;
        this.posZ = d2;
        this.a = flag;
        this.b = explosion_effect;
        this.j = DamageSource.explosion(this);
    }

    public static float a(Vec3D vec3d, Entity entity) {
        AxisAlignedBB axisalignedbb = entity.getBoundingBox();
        double d0 = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0D + 1.0D);
        double d1 = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0D + 1.0D);
        double d2 = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0D + 1.0D);
        double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;

        if (d0 >= 0.0D && d1 >= 0.0D && d2 >= 0.0D) {
            int i = 0;
            int j = 0;

            for (float f = 0.0F; f <= 1.0F; f = (float) ((double) f + d0)) {
                for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float) ((double) f1 + d1)) {
                    for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float) ((double) f2 + d2)) {
                        double d5 = MathHelper.d((double) f, axisalignedbb.minX, axisalignedbb.maxX);
                        double d6 = MathHelper.d((double) f1, axisalignedbb.minY, axisalignedbb.maxY);
                        double d7 = MathHelper.d((double) f2, axisalignedbb.minZ, axisalignedbb.maxZ);
                        Vec3D vec3d1 = new Vec3D(d5 + d3, d6, d7 + d4);

                        if (entity.world.rayTrace(new RayTrace(vec3d1, vec3d, RayTrace.BlockCollisionOption.OUTLINE, RayTrace.FluidCollisionOption.NONE, entity)).getType() == MovingObjectPosition.EnumMovingObjectType.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float) i / (float) j;
        } else {
            return 0.0F;
        }
    }

    public void a() {
        // CraftBukkit start
        if (this.size < 0.1F) {
            return;
        }
        // CraftBukkit end
        Set<BlockPosition> set = Sets.newHashSet();
        boolean flag = true;

        int i;
        int j;

        for (int k = 0; k < 16; ++k) {
            for (i = 0; i < 16; ++i) {
                for (j = 0; j < 16; ++j) {
                    if (k == 0 || k == 15 || i == 0 || i == 15 || j == 0 || j == 15) {
                        double d0 = (double) ((float) k / 15.0F * 2.0F - 1.0F);
                        double d1 = (double) ((float) i / 15.0F * 2.0F - 1.0F);
                        double d2 = (double) ((float) j / 15.0F * 2.0F - 1.0F);
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f = this.size * (0.7F + this.world.random.nextFloat() * 0.6F);
                        double d4 = this.posX;
                        double d5 = this.posY;
                        double d6 = this.posZ;

                        for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
                            BlockPosition blockposition = new BlockPosition(d4, d5, d6);
                            IBlockData iblockdata = this.world.getType(blockposition);
                            if (!iblockdata.isDestroyable()) continue; // Paper
                            Fluid fluid = iblockdata.getFluid(); // Paper

                            if (!iblockdata.isAir() || !fluid.isEmpty()) {
                                float f2 = Math.max(iblockdata.getBlock().getDurability(), fluid.k());

                                if (this.source != null) {
                                    f2 = this.source.a(this, this.world, blockposition, iblockdata, fluid, f2);
                                }

                                f -= (f2 + 0.3F) * 0.3F;
                            }

                            if (f > 0.0F && (this.source == null || this.source.a(this, this.world, blockposition, iblockdata, f)) && blockposition.getY() < 256 && blockposition.getY() >= 0) { // CraftBukkit - don't wrap explosions
                                set.add(blockposition);
                            }

                            d4 += d0 * 0.30000001192092896D;
                            d5 += d1 * 0.30000001192092896D;
                            d6 += d2 * 0.30000001192092896D;
                        }
                    }
                }
            }
        }

        this.blocks.addAll(set);
        float f3 = this.size * 2.0F;

        i = MathHelper.floor(this.posX - (double) f3 - 1.0D);
        j = MathHelper.floor(this.posX + (double) f3 + 1.0D);
        int l = MathHelper.floor(this.posY - (double) f3 - 1.0D);
        int i1 = MathHelper.floor(this.posY + (double) f3 + 1.0D);
        int j1 = MathHelper.floor(this.posZ - (double) f3 - 1.0D);
        int k1 = MathHelper.floor(this.posZ + (double) f3 + 1.0D);
        // Paper start - Fix lag from explosions processing dead entities
        List<Entity> list = this.world.getEntities(this.source, new AxisAlignedBB((double) i, (double) l, (double) j1, (double) j, (double) i1, (double) k1), new com.google.common.base.Predicate<Entity>() {
            @Override
            public boolean apply(Entity entity) {
                return IEntitySelector.canAITarget().test(entity) && !entity.dead;
            }
        });
        // Paper end
        Vec3D vec3d = new Vec3D(this.posX, this.posY, this.posZ);

        for (int l1 = 0; l1 < list.size(); ++l1) {
            Entity entity = (Entity) list.get(l1);

            if (!entity.ca()) {
                double d7 = (double) (MathHelper.sqrt(entity.c(vec3d)) / f3);

                if (d7 <= 1.0D) {
                    double d8 = entity.locX() - this.posX;
                    double d9 = entity.getHeadY() - this.posY;
                    double d10 = entity.locZ() - this.posZ;
                    double d11 = (double) MathHelper.sqrt(d8 * d8 + d9 * d9 + d10 * d10);

                    if (d11 != 0.0D) {
                        d8 /= d11;
                        d9 /= d11;
                        d10 /= d11;
                        double d12 = this.getBlockDensity(vec3d, entity); // Paper - Optimize explosions
                        double d13 = (1.0D - d7) * d12;

                        // CraftBukkit start
                        // entity.damageEntity(this.b(), (float) ((int) ((d13 * d13 + d13) / 2.0D * 7.0D * (double) f3 + 1.0D)));
                        CraftEventFactory.entityDamage = source;
                        entity.forceExplosionKnockback = false;
                        boolean wasDamaged = entity.damageEntity(this.b(), (float) ((int) ((d13 * d13 + d13) / 2.0D * 7.0D * (double) f3 + 1.0D)));
                        CraftEventFactory.entityDamage = null;
                        if (!wasDamaged && !(entity instanceof EntityTNTPrimed || entity instanceof EntityFallingBlock) && !entity.forceExplosionKnockback) {
                            continue;
                        }
                        // CraftBukkit end
                        double d14 = d13;

                        if (entity instanceof EntityLiving) {
                            d14 = entity instanceof EntityHuman && world.paperConfig.disableExplosionKnockback ? 0 : EnchantmentProtection.a((EntityLiving) entity, d13); // Paper - Disable explosion knockback
                        }

                        entity.setMot(entity.getMot().add(d8 * d14, d9 * d14, d10 * d14));
                        if (entity instanceof EntityHuman) {
                            EntityHuman entityhuman = (EntityHuman) entity;

                            if (!entityhuman.isSpectator() && (!entityhuman.isCreative() && !world.paperConfig.disableExplosionKnockback || !entityhuman.abilities.isFlying)) { // Paper - Disable explosion knockback
                                this.l.put(entityhuman, new Vec3D(d8 * d13, d9 * d13, d10 * d13));
                            }
                        }
                    }
                }
            }
        }

    }

    public void a(boolean flag) {
        if (this.world.isClientSide) {
            this.world.a(this.posX, this.posY, this.posZ, SoundEffects.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.world.random.nextFloat() - this.world.random.nextFloat()) * 0.2F) * 0.7F, false);
        }

        boolean flag1 = this.b != Explosion.Effect.NONE;

        if (flag) {
            if (this.size >= 2.0F && flag1) {
                this.world.addParticle(Particles.EXPLOSION_EMITTER, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D);
            } else {
                this.world.addParticle(Particles.EXPLOSION, this.posX, this.posY, this.posZ, 1.0D, 0.0D, 0.0D);
            }
        }

        if (flag1) {
            ObjectArrayList<Pair<ItemStack, BlockPosition>> objectarraylist = new ObjectArrayList();

            Collections.shuffle(this.blocks, this.world.random);
            Iterator iterator = this.blocks.iterator();
            // CraftBukkit start
            org.bukkit.World bworld = this.world.getWorld();
            org.bukkit.entity.Entity explode = this.source == null ? null : this.source.getBukkitEntity();
            Location location = new Location(bworld, this.posX, this.posY, this.posZ);

            List<org.bukkit.block.Block> blockList = Lists.newArrayList();
            for (int i1 = this.blocks.size() - 1; i1 >= 0; i1--) {
                BlockPosition cpos = (BlockPosition) this.blocks.get(i1);
                org.bukkit.block.Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());
                if (!bblock.getType().isAir()) {
                    blockList.add(bblock);
                }
            }

            boolean cancelled;
            List<org.bukkit.block.Block> bukkitBlocks;
            float yield;

            if (explode != null) {
                EntityExplodeEvent event = new EntityExplodeEvent(explode, location, blockList, this.b == Explosion.Effect.DESTROY ? 1.0F / this.size : 1.0F);
                this.world.getServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            } else {
                BlockExplodeEvent event = new BlockExplodeEvent(location.getBlock(), blockList, this.b == Explosion.Effect.DESTROY ? 1.0F / this.size : 1.0F);
                this.world.getServer().getPluginManager().callEvent(event);
                cancelled = event.isCancelled();
                bukkitBlocks = event.blockList();
                yield = event.getYield();
            }

            this.blocks.clear();

            for (org.bukkit.block.Block bblock : bukkitBlocks) {
                BlockPosition coords = new BlockPosition(bblock.getX(), bblock.getY(), bblock.getZ());
                blocks.add(coords);
            }

            if (cancelled) {
                this.wasCanceled = true;
                return;
            }
            // CraftBukkit end
            iterator = this.blocks.iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition = (BlockPosition) iterator.next();
                IBlockData iblockdata = this.world.getType(blockposition);
                Block block = iblockdata.getBlock();

                if (!iblockdata.isAir() && iblockdata.isDestroyable()) { // Paper
                    BlockPosition blockposition1 = blockposition.immutableCopy();

                    this.world.getMethodProfiler().enter("explosion_blocks");
                    if (block.a(this) && this.world instanceof WorldServer) {
                        TileEntity tileentity = block.isTileEntity() ? this.world.getTileEntity(blockposition) : null;
                        LootTableInfo.Builder loottableinfo_builder = (new LootTableInfo.Builder((WorldServer) this.world)).a(this.world.random).set(LootContextParameters.POSITION, blockposition).set(LootContextParameters.TOOL, ItemStack.a).setOptional(LootContextParameters.BLOCK_ENTITY, tileentity).setOptional(LootContextParameters.THIS_ENTITY, this.source);

                        if (this.b == Explosion.Effect.DESTROY || yield < 1.0F) { // CraftBukkit - add yield
                            loottableinfo_builder.set(LootContextParameters.EXPLOSION_RADIUS, 1.0F / yield); // CraftBukkit - add yield
                        }

                        iblockdata.a(loottableinfo_builder).forEach((itemstack) -> {
                            a(objectarraylist, itemstack, blockposition1);
                        });
                    }

                    this.world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
                    block.wasExploded(this.world, blockposition, this);
                    this.world.getMethodProfiler().exit();
                }
            }

            ObjectListIterator objectlistiterator = objectarraylist.iterator();

            while (objectlistiterator.hasNext()) {
                Pair<ItemStack, BlockPosition> pair = (Pair) objectlistiterator.next();

                Block.a(this.world, (BlockPosition) pair.getSecond(), (ItemStack) pair.getFirst());
            }
        }

        if (this.a) {
            Iterator iterator1 = this.blocks.iterator();

            while (iterator1.hasNext()) {
                BlockPosition blockposition2 = (BlockPosition) iterator1.next();

                if (this.c.nextInt(3) == 0 && this.world.getType(blockposition2).isAir() && this.world.getType(blockposition2.down()).g(this.world, blockposition2.down())) {
                    // CraftBukkit start - Ignition by explosion
                    if (!org.bukkit.craftbukkit.event.CraftEventFactory.callBlockIgniteEvent(this.world, blockposition2.getX(), blockposition2.getY(), blockposition2.getZ(), this).isCancelled()) {
                        this.world.setTypeUpdate(blockposition2, Blocks.FIRE.getBlockData());
                    }
                    // CraftBukkit end
                }
            }
        }

    }

    private static void a(ObjectArrayList<Pair<ItemStack, BlockPosition>> objectarraylist, ItemStack itemstack, BlockPosition blockposition) {
        if (itemstack.isEmpty()) return; // CraftBukkit - SPIGOT-5425
        int i = objectarraylist.size();

        for (int j = 0; j < i; ++j) {
            Pair<ItemStack, BlockPosition> pair = (Pair) objectarraylist.get(j);
            ItemStack itemstack1 = (ItemStack) pair.getFirst();

            if (EntityItem.a(itemstack1, itemstack)) {
                ItemStack itemstack2 = EntityItem.a(itemstack1, itemstack, 16);

                objectarraylist.set(j, Pair.of(itemstack2, pair.getSecond()));
                if (itemstack.isEmpty()) {
                    return;
                }
            }
        }

        objectarraylist.add(Pair.of(itemstack, blockposition));
    }

    public DamageSource b() {
        return this.j;
    }

    public void a(DamageSource damagesource) {
        this.j = damagesource;
    }

    public Map<EntityHuman, Vec3D> c() {
        return this.l;
    }

    @Nullable
    public EntityLiving getSource() {
        return this.source == null ? null : (this.source instanceof EntityTNTPrimed ? ((EntityTNTPrimed) this.source).getSource() : (this.source instanceof EntityLiving ? (EntityLiving) this.source : (this.source instanceof EntityFireball ? ((EntityFireball) this.source).shooter : null)));
    }

    public void clearBlocks() {
        this.blocks.clear();
    }

    public List<BlockPosition> getBlocks() {
        return this.blocks;
    }

    public static enum Effect {

        NONE, BREAK, DESTROY;

        private Effect() {}
    }
    // Paper start - Optimize explosions
    private float getBlockDensity(Vec3D vec3d, Entity entity) {
        if (!this.world.paperConfig.optimizeExplosions) {
            return a(vec3d, entity);
        }
        CacheKey key = new CacheKey(this, entity.getBoundingBox());
        Float blockDensity = this.world.explosionDensityCache.get(key);
        if (blockDensity == null) {
            blockDensity = a(vec3d, entity);
            this.world.explosionDensityCache.put(key, blockDensity);
        }

        return blockDensity;
    }

    static class CacheKey {
        private final World world;
        private final double posX, posY, posZ;
        private final double minX, minY, minZ;
        private final double maxX, maxY, maxZ;

        public CacheKey(Explosion explosion, AxisAlignedBB aabb) {
            this.world = explosion.world;
            this.posX = explosion.posX;
            this.posY = explosion.posY;
            this.posZ = explosion.posZ;
            this.minX = aabb.minX;
            this.minY = aabb.minY;
            this.minZ = aabb.minZ;
            this.maxX = aabb.maxX;
            this.maxY = aabb.maxY;
            this.maxZ = aabb.maxZ;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (Double.compare(cacheKey.posX, posX) != 0) return false;
            if (Double.compare(cacheKey.posY, posY) != 0) return false;
            if (Double.compare(cacheKey.posZ, posZ) != 0) return false;
            if (Double.compare(cacheKey.minX, minX) != 0) return false;
            if (Double.compare(cacheKey.minY, minY) != 0) return false;
            if (Double.compare(cacheKey.minZ, minZ) != 0) return false;
            if (Double.compare(cacheKey.maxX, maxX) != 0) return false;
            if (Double.compare(cacheKey.maxY, maxY) != 0) return false;
            if (Double.compare(cacheKey.maxZ, maxZ) != 0) return false;
            return world.equals(cacheKey.world);
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = world.hashCode();
            temp = Double.doubleToLongBits(posX);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(posY);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(posZ);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(minX);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(minY);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(minZ);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(maxX);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(maxY);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(maxZ);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }
    // Paper end
}
