package net.minecraft.server;

import javax.annotation.Nullable;
import org.bukkit.event.entity.ExplosionPrimeEvent; // CraftBukkit

public class EntityTNTPrimed extends Entity {

    private static final DataWatcherObject<Integer> FUSE_TICKS = DataWatcher.a(EntityTNTPrimed.class, DataWatcherRegistry.b);
    @Nullable
    private EntityLiving source;
    private int fuseTicks;
    public float yield = 4; // CraftBukkit - add field
    public boolean isIncendiary = false; // CraftBukkit - add field

    public EntityTNTPrimed(EntityTypes<? extends EntityTNTPrimed> entitytypes, World world) {
        super(entitytypes, world);
        this.fuseTicks = 80;
        this.i = true;
    }

    public EntityTNTPrimed(World world, double d0, double d1, double d2, @Nullable EntityLiving entityliving) {
        this(EntityTypes.TNT, world);
        this.setPosition(d0, d1, d2);
        double d3 = world.random.nextDouble() * 6.2831854820251465D;

        this.setMot(-Math.sin(d3) * 0.02D, 0.20000000298023224D, -Math.cos(d3) * 0.02D);
        this.setFuseTicks(80);
        this.lastX = d0;
        this.lastY = d1;
        this.lastZ = d2;
        this.source = entityliving;
    }

    @Override
    protected void initDatawatcher() {
        this.datawatcher.register(EntityTNTPrimed.FUSE_TICKS, 80);
    }

    @Override
    protected boolean playStepSound() {
        return false;
    }

    @Override
    public boolean isInteractable() {
        return !this.dead;
    }

    @Override
    public void tick() {
        if (world.spigotConfig.currentPrimedTnt++ > world.spigotConfig.maxTntTicksPerTick) { return; } // Spigot
        if (!this.isNoGravity()) {
            this.setMot(this.getMot().add(0.0D, -0.04D, 0.0D));
        }

        this.move(EnumMoveType.SELF, this.getMot());
        // Paper start - Configurable TNT entity height nerf
        if (this.world.paperConfig.entityTNTHeightNerf != 0 && this.locY() > this.world.paperConfig.entityTNTHeightNerf) {
            this.die();
            return;
        }
        // Paper end
        this.setMot(this.getMot().a(0.98D));
        if (this.onGround) {
            this.setMot(this.getMot().d(0.7D, -0.5D, 0.7D));
        }

        --this.fuseTicks;
        if (this.fuseTicks <= 0) {
            // CraftBukkit start - Need to reverse the order of the explosion and the entity death so we have a location for the event
            // this.die();
            if (!this.world.isClientSide) {
                this.explode();
            }
            this.die();
            // CraftBukkit end
        } else {
            this.aC();
            if (this.world.isClientSide) {
                this.world.addParticle(Particles.SMOKE, this.locX(), this.locY() + 0.5D, this.locZ(), 0.0D, 0.0D, 0.0D);
            }
        }
        // Paper start - Optional prevent TNT from moving in water
        if (!this.dead && this.inWater && this.world.paperConfig.preventTntFromMovingInWater) {
            /*
             * Author: Jedediah Smith <jedediah@silencegreys.com>
             */
            // Send position and velocity updates to nearby players on every tick while the TNT is in water.
            // This does pretty well at keeping their clients in sync with the server.
            PlayerChunkMap.EntityTracker ete = this.tracker;
            if (ete != null) {
                PacketPlayOutEntityVelocity velocityPacket = new PacketPlayOutEntityVelocity(this);
                PacketPlayOutEntityTeleport positionPacket = new PacketPlayOutEntityTeleport(this);

                ete.trackedPlayers.stream()
                    .filter(viewer -> (viewer.locX() - this.locX()) * (viewer.locY() - this.locY()) * (viewer.locZ() - this.locZ()) < 16 * 16)
                    .forEach(viewer -> {
                        viewer.playerConnection.sendPacket(velocityPacket);
                        viewer.playerConnection.sendPacket(positionPacket);
                    });
            }
        }
        // Paper end
    }

    private void explode() {
        // CraftBukkit start
        // float f = 4.0F;

        ExplosionPrimeEvent event = new ExplosionPrimeEvent((org.bukkit.entity.Explosive) this.getBukkitEntity());
        this.world.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            this.world.createExplosion(this, this.locX(), this.e(0.0625D), this.locZ(), event.getRadius(), event.getFire(), Explosion.Effect.BREAK);
        }
        // CraftBukkit end
    }

    @Override
    protected void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setShort("Fuse", (short) this.getFuseTicks());
    }

    @Override
    protected void a(NBTTagCompound nbttagcompound) {
        this.setFuseTicks(nbttagcompound.getShort("Fuse"));
        // Paper start - Try and load origin location from the old NBT tags for backwards compatibility
        if (nbttagcompound.hasKey("SourceLoc_x")) {
            int srcX = nbttagcompound.getInt("SourceLoc_x");
            int srcY = nbttagcompound.getInt("SourceLoc_y");
            int srcZ = nbttagcompound.getInt("SourceLoc_z");
            origin = new org.bukkit.Location(world.getWorld(), srcX, srcY, srcZ);
        }
        // Paper end
    }

    @Nullable
    public EntityLiving getSource() {
        return this.source;
    }

    @Override
    protected float getHeadHeight(EntityPose entitypose, EntitySize entitysize) {
        return 0.0F;
    }

    public void setFuseTicks(int i) {
        this.datawatcher.set(EntityTNTPrimed.FUSE_TICKS, i);
        this.fuseTicks = i;
    }

    @Override
    public void a(DataWatcherObject<?> datawatcherobject) {
        if (EntityTNTPrimed.FUSE_TICKS.equals(datawatcherobject)) {
            this.fuseTicks = this.i();
        }

    }

    public int i() {
        return (Integer) this.datawatcher.get(EntityTNTPrimed.FUSE_TICKS);
    }

    public int getFuseTicks() {
        return this.fuseTicks;
    }

    @Override
    public Packet<?> L() {
        return new PacketPlayOutSpawnEntity(this);
    }

    // Paper start - Optional prevent TNT from moving in water
    @Override
    public boolean pushedByWater() {
        return !world.paperConfig.preventTntFromMovingInWater && super.pushedByWater();
    }
    // Paper end
}
