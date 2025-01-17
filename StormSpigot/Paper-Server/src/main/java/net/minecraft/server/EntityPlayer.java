package net.minecraft.server;

import com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent;
import com.google.common.collect.Lists;
import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent; // Paper
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import io.netty.util.concurrent.Future;
import java.util.ArrayDeque; // Paper
import java.util.Collection;
import java.util.Deque; // Paper
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.MainHand;
// CraftBukkit end

public class EntityPlayer extends EntityHuman implements ICrafting {

    private static final Logger LOGGER = LogManager.getLogger();
    public String locale = null; // CraftBukkit - lowercase // Paper - default to null
    public PlayerConnection playerConnection;
    public NetworkManager networkManager; // Paper
    public final MinecraftServer server;
    public final PlayerInteractManager playerInteractManager;
    public final Deque<Integer> removeQueue = new ArrayDeque<>(); // Paper
    private final AdvancementDataPlayer advancementDataPlayer;
    private final ServerStatisticManager serverStatisticManager;
    private float lastHealthScored = Float.MIN_VALUE;
    private int lastFoodScored = Integer.MIN_VALUE;
    private int lastAirScored = Integer.MIN_VALUE;
    private int lastArmorScored = Integer.MIN_VALUE;
    private int lastExpLevelScored = Integer.MIN_VALUE;
    private int lastExpTotalScored = Integer.MIN_VALUE;
    public long lastHighPriorityChecked; // Paper
    public void forceCheckHighPriority() {
        lastHighPriorityChecked = -1;
        getWorldServer().getChunkProvider().playerChunkMap.checkHighPriorityChunks(this);
    }
    public boolean isRealPlayer; // Paper
    private float lastHealthSent = -1.0E8F;
    private int lastFoodSent = -99999999;
    private boolean lastSentSaturationZero = true;
    public int lastSentExp = -99999999;
    public int invulnerableTicks = 60;
    private EnumChatVisibility ch;
    private boolean ci = true; public boolean hasChatColorsEnabled() { return this.ci; } // Paper - OBFHELPER
    private long cj = SystemUtils.getMonotonicMillis();
    private Entity spectatedEntity;
    public boolean worldChangeInvuln;
    private boolean cm; private void setHasSeenCredits(boolean has) { this.cm = has; } // Paper - OBFHELPER
    private final RecipeBookServer recipeBook;
    private Vec3D co;
    private int cp;
    private boolean cq;
    @Nullable
    private Vec3D cr;
    private SectionPosition cs = SectionPosition.a(0, 0, 0);
    private int containerCounter;
    public boolean e;
    public int ping;
    public boolean viewingCredits;
    private int containerUpdateDelay; // Paper
    public long loginTime; // Paper
    public int patrolSpawnDelay; // Paper - per player patrol spawns
    // Paper start - cancellable death event
    public boolean queueHealthUpdatePacket = false;
    public net.minecraft.server.PacketPlayOutUpdateHealth queuedHealthUpdatePacket;
    // Paper end
    // Paper start - mob spawning rework
    public static final int ENUMCREATURETYPE_TOTAL_ENUMS = EnumCreatureType.values().length;
    public final int[] mobCounts = new int[ENUMCREATURETYPE_TOTAL_ENUMS]; // Paper
    public final com.destroystokyo.paper.util.PooledHashSets.PooledObjectLinkedOpenHashSet<EntityPlayer> cachedSingleMobDistanceMap;
    // Paper end

    // CraftBukkit start
    public String displayName;
    public IChatBaseComponent listName;
    public org.bukkit.Location compassTarget;
    public int newExp = 0;
    public int newLevel = 0;
    public int newTotalExp = 0;
    public boolean keepLevel = false;
    public double maxHealthCache;
    public boolean joining = true;
    public boolean sentListPacket = false;
    public boolean supressTrackerForLogin = false; // Paper
    public boolean didPlayerJoinEvent = false; // Paper
    public Integer clientViewDistance;
    // CraftBukkit end
    public PlayerNaturallySpawnCreaturesEvent playerNaturallySpawnedEvent; // Paper

    public final com.destroystokyo.paper.util.misc.PooledLinkedHashSets.PooledObjectLinkedOpenHashSet<EntityPlayer> cachedSingleHashSet; // Paper

    double lastEntitySpawnRadiusSquared; // Paper - optimise isOutsideRange, this field is in blocks

    boolean needsChunkCenterUpdate; // Paper - no-tick view distance

    public EntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super((World) worldserver, gameprofile);
        playerinteractmanager.player = this;
        this.playerInteractManager = playerinteractmanager;
        this.server = minecraftserver;
        this.recipeBook = new RecipeBookServer(minecraftserver.getCraftingManager());
        this.serverStatisticManager = minecraftserver.getPlayerList().getStatisticManager(this);
        this.advancementDataPlayer = minecraftserver.getPlayerList().f(this);
        this.H = 1.0F;
        //this.a(worldserver); // Paper - don't move to spawn on login, only first join

        this.cachedSingleHashSet = new com.destroystokyo.paper.util.misc.PooledLinkedHashSets.PooledObjectLinkedOpenHashSet<>(this); // Paper

        // CraftBukkit start
        this.displayName = this.getName();
        this.canPickUpLoot = true;
        this.maxHealthCache = this.getMaxHealth();
        this.cachedSingleMobDistanceMap = new com.destroystokyo.paper.util.PooledHashSets.PooledObjectLinkedOpenHashSet<>(this); // Paper
    }
    // Paper start
    public BlockPosition getPointInFront(double inFront) {
        double rads = Math.toRadians(MCUtil.normalizeYaw(this.yaw+90)); // MC rotates yaw 90 for some odd reason
        final double x = locX() + inFront * Math.cos(rads);
        final double z = locZ() + inFront * Math.sin(rads);
        return new BlockPosition(x, locY(), z);
    }

    public ChunkCoordIntPair getChunkInFront(double inFront) {
        double rads = Math.toRadians(MCUtil.normalizeYaw(this.yaw+90)); // MC rotates yaw 90 for some odd reason
        final double x = locX() + (inFront * 16) * Math.cos(rads);
        final double z = locZ() + (inFront * 16) * Math.sin(rads);
        return new ChunkCoordIntPair(MathHelper.floor(x) >> 4, MathHelper.floor(z) >> 4);
    }
    // Paper end

    // Yes, this doesn't match Vanilla, but it's the best we can do for now.
    // If this is an issue, PRs are welcome
    public final BlockPosition getSpawnPoint(WorldServer worldserver) {
        BlockPosition blockposition = worldserver.getSpawn();

        if (worldserver.worldProvider.f() && worldserver.getWorldData().getGameType() != EnumGamemode.ADVENTURE) { // Paper
            int i = Math.max(0, this.server.a(worldserver));
            int j = MathHelper.floor(worldserver.getWorldBorder().b((double) blockposition.getX(), (double) blockposition.getZ()));

            if (j < i) {
                i = j;
            }

            if (j <= 1) {
                i = 1;
            }

            int k = (i * 2 + 1) * (i * 2 + 1);
            int l = this.u(k);
            int i1 = (new Random()).nextInt(k);

            for (int j1 = 0; j1 < k; ++j1) {
                int k1 = (i1 + l * j1) % k;
                int l1 = k1 % (i * 2 + 1);
                int i2 = k1 / (i * 2 + 1);
                BlockPosition blockposition1 = worldserver.getWorldProvider().a(blockposition.getX() + l1 - i, blockposition.getZ() + i2 - i, false);

                if (blockposition1 != null) {
                    return blockposition1;
                }
            }
        }

        return blockposition;
    }
    // CraftBukkit end

    public void moveToSpawn(WorldServer worldserver) { a(worldserver); } // Paper - OBFHELPER
    private void a(WorldServer worldserver) {
        BlockPosition blockposition = worldserver.getSpawn();

        if (worldserver.worldProvider.f() && worldserver.getWorldData().getGameType() != EnumGamemode.ADVENTURE) {
            int i = Math.max(0, this.server.a(worldserver));
            int j = MathHelper.floor(worldserver.getWorldBorder().b((double) blockposition.getX(), (double) blockposition.getZ()));

            if (j < i) {
                i = j;
            }

            if (j <= 1) {
                i = 1;
            }

            long k = (long) (i * 2 + 1);
            long l = k * k;
            int i1 = l > 2147483647L ? Integer.MAX_VALUE : (int) l;
            int j1 = this.u(i1);
            int k1 = (new Random()).nextInt(i1);

            for (int l1 = 0; l1 < i1; ++l1) {
                int i2 = (k1 + j1 * l1) % i1;
                int j2 = i2 % (i * 2 + 1);
                int k2 = i2 / (i * 2 + 1);
                BlockPosition blockposition1 = worldserver.getWorldProvider().a(blockposition.getX() + j2 - i, blockposition.getZ() + k2 - i, false);

                if (blockposition1 != null) {
                    this.setPositionRotation(blockposition1, 0.0F, 0.0F);
                    if (worldserver.getCubes(this)) {
                        break;
                    }
                }
            }
        } else {
            this.setPositionRotation(blockposition, 0.0F, 0.0F);

            while (!worldserver.getCubes(this) && this.locY() < 255.0D) {
                this.setPosition(this.locX(), this.locY() + 1.0D, this.locZ());
            }
        }

    }

    private int u(int i) {
        return i <= 16 ? i - 1 : 17;
    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (this.locY() > 300) this.setPositionRaw(locX(), 257, locZ()); // Paper - bring down to a saner Y level if out of world
        if (nbttagcompound.hasKeyOfType("playerGameType", 99)) {
            if (this.getMinecraftServer().getForceGamemode()) {
                this.playerInteractManager.setGameMode(this.getMinecraftServer().getGamemode());
            } else {
                this.playerInteractManager.setGameMode(EnumGamemode.getById(nbttagcompound.getInt("playerGameType")));
            }
        }

        if (nbttagcompound.hasKeyOfType("enteredNetherPosition", 10)) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("enteredNetherPosition");

            this.cr = new Vec3D(nbttagcompound1.getDouble("x"), nbttagcompound1.getDouble("y"), nbttagcompound1.getDouble("z"));
        }

        this.cm = nbttagcompound.getBoolean("seenCredits");
        if (nbttagcompound.hasKeyOfType("recipeBook", 10)) {
            this.recipeBook.a(nbttagcompound.getCompound("recipeBook"));
        }
        this.getBukkitEntity().readExtraData(nbttagcompound); // CraftBukkit

        if (this.isSleeping()) {
            this.entityWakeup();
        }

    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("playerGameType", this.playerInteractManager.getGameMode().getId());
        nbttagcompound.setBoolean("seenCredits", this.cm);
        if (this.cr != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            nbttagcompound1.setDouble("x", this.cr.x);
            nbttagcompound1.setDouble("y", this.cr.y);
            nbttagcompound1.setDouble("z", this.cr.z);
            nbttagcompound.set("enteredNetherPosition", nbttagcompound1);
        }

        Entity entity = this.getRootVehicle();
        Entity entity1 = this.getVehicle();

        // CraftBukkit start - handle non-persistent vehicles
        boolean persistVehicle = true;
        if (entity1 != null) {
            Entity vehicle;
            for (vehicle = entity1; vehicle != null; vehicle = vehicle.getVehicle()) {
                if (!vehicle.persist) {
                    persistVehicle = false;
                    break;
                }
            }
        }

        if (persistVehicle && entity1 != null && entity != this && entity.hasSinglePlayerPassenger()) {
            // CraftBukkit end
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();

            entity.d(nbttagcompound3);
            nbttagcompound2.a("Attach", entity1.getUniqueID());
            nbttagcompound2.set("Entity", nbttagcompound3);
            nbttagcompound.set("RootVehicle", nbttagcompound2);
        }

        nbttagcompound.set("recipeBook", this.recipeBook.save());
        this.getBukkitEntity().setExtraData(nbttagcompound); // CraftBukkit
    }

    // CraftBukkit start - World fallback code, either respawn location or global spawn
    public void spawnIn(World world) {
        super.spawnIn(world);
        if (world == null) {
            this.dead = false;
            Vec3D position = null;
            if (this.spawnWorld != null && !this.spawnWorld.equals("")) {
                CraftWorld cworld = (CraftWorld) Bukkit.getServer().getWorld(this.spawnWorld);
                if (cworld != null && this.getBed() != null) {
                    world = cworld.getHandle();
                    position = EntityHuman.getBed(cworld.getHandle(), this.getBed(), false).orElse(null);
                }
            }
            if (world == null || position == null) {
                world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
                position = new Vec3D(world.getSpawn());
            }
            this.world = world;
            this.setPositionRaw(position.getX(), position.getY(), position.getZ()); // Paper - don't register to chunks yet
        }
        this.dimension = ((WorldServer) this.world).getWorldProvider().getDimensionManager();
        this.playerInteractManager.a((WorldServer) world);
    }
    // CraftBukkit end

    public void a(int i) {
        float f = (float) this.getExpToLevel();
        float f1 = (f - 1.0F) / f;

        this.exp = MathHelper.a((float) i / f, 0.0F, f1);
        this.lastSentExp = -1;
    }

    public void b(int i) {
        this.expLevel = i;
        this.lastSentExp = -1;
    }

    @Override
    public void levelDown(int i) {
        super.levelDown(i);
        this.lastSentExp = -1;
    }

    @Override
    public void enchantDone(ItemStack itemstack, int i) {
        super.enchantDone(itemstack, i);
        this.lastSentExp = -1;
    }

    public void syncInventory() {
        this.activeContainer.addSlotListener(this);
    }

    @Override
    public void enterCombat() {
        super.enterCombat();
        this.playerConnection.sendPacket(new PacketPlayOutCombatEvent(this.getCombatTracker(), PacketPlayOutCombatEvent.EnumCombatEventType.ENTER_COMBAT));
    }

    @Override
    public void exitCombat() {
        super.exitCombat();
        this.playerConnection.sendPacket(new PacketPlayOutCombatEvent(this.getCombatTracker(), PacketPlayOutCombatEvent.EnumCombatEventType.END_COMBAT));
    }

    @Override
    protected void a(IBlockData iblockdata) {
        CriterionTriggers.d.a(this, iblockdata);
    }

    @Override
    protected ItemCooldown g() {
        return new ItemCooldownPlayer(this);
    }

    @Override
    public void tick() {
        // CraftBukkit start
        if (this.joining) {
            this.joining = false;
        }
        // CraftBukkit end
        this.playerInteractManager.a();
        --this.invulnerableTicks;
        if (this.noDamageTicks > 0) {
            --this.noDamageTicks;
        }

        // Paper start - Configurable container update tick rate
        if (--containerUpdateDelay <= 0) {
            this.activeContainer.c();
            containerUpdateDelay = world.paperConfig.containerUpdateTickRate;
        }
        // Paper end
        if (!this.world.isClientSide && this.activeContainer != this.defaultContainer && (isFrozen() || !this.activeContainer.canUse(this))) { // Paper - auto close while frozen
            this.closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason.CANT_USE); // Paper
            this.activeContainer = this.defaultContainer;
        }

        while (!this.removeQueue.isEmpty()) {
            int i = Math.min(this.removeQueue.size(), Integer.MAX_VALUE);
            int[] aint = new int[i];
            //Iterator<Integer> iterator = this.removeQueue.iterator(); // Paper
            int j = 0;

            // Paper start
            /* while (iterator.hasNext() && j < i) {
                aint[j++] = (Integer) iterator.next();
                iterator.remove();
            } */

            Integer integer;
            while (j < i && (integer = this.removeQueue.poll()) != null) {
                aint[j++] = integer.intValue();
            }
            // Paper end

            this.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(aint));
        }

        Entity entity = this.getSpecatorTarget();

        if (entity != this) {
            if (entity.isAlive()) {
                this.setLocation(entity.locX(), entity.locY(), entity.locZ(), entity.yaw, entity.pitch);
                this.getWorldServer().getChunkProvider().movePlayer(this);
                if (this.dU()) {
                    this.setSpectatorTarget(this);
                }
            } else {
                this.setSpectatorTarget(this);
            }
        }

        CriterionTriggers.w.a(this);
        if (this.co != null) {
            CriterionTriggers.u.a(this, this.co, this.ticksLived - this.cp);
        }

        this.advancementDataPlayer.b(this);
    }

    public void playerTick() {
        try {
            if (valid && (!this.isSpectator() || this.world.isLoaded(new BlockPosition(this)))) { // Paper - don't tick dead players that are not in the world currently (pending respawn)
                super.tick();
            }
            if (valid && isAlive() && playerConnection != null) ((WorldServer)world).getChunkProvider().playerChunkMap.checkHighPriorityChunks(this); // Paper

            for (int i = 0; i < this.inventory.getSize(); ++i) {
                ItemStack itemstack = this.inventory.getItem(i);

                if (itemstack.getItem().R_()) {
                    Packet<?> packet = ((ItemWorldMapBase) itemstack.getItem()).a(itemstack, this.world, (EntityHuman) this);

                    if (packet != null) {
                        this.playerConnection.sendPacket(packet);
                    }
                }
            }

            if (this.getHealth() != this.lastHealthSent || this.lastFoodSent != this.foodData.getFoodLevel() || this.foodData.getSaturationLevel() == 0.0F != this.lastSentSaturationZero) {
                this.playerConnection.sendPacket(new PacketPlayOutUpdateHealth(this.getBukkitEntity().getScaledHealth(), this.foodData.getFoodLevel(), this.foodData.getSaturationLevel())); // CraftBukkit
                this.lastHealthSent = this.getHealth();
                this.lastFoodSent = this.foodData.getFoodLevel();
                this.lastSentSaturationZero = this.foodData.getSaturationLevel() == 0.0F;
            }

            if (this.getHealth() + this.getAbsorptionHearts() != this.lastHealthScored) {
                this.lastHealthScored = this.getHealth() + this.getAbsorptionHearts();
                this.a(IScoreboardCriteria.HEALTH, MathHelper.f(this.lastHealthScored));
            }

            if (this.foodData.getFoodLevel() != this.lastFoodScored) {
                this.lastFoodScored = this.foodData.getFoodLevel();
                this.a(IScoreboardCriteria.FOOD, MathHelper.f((float) this.lastFoodScored));
            }

            if (this.getAirTicks() != this.lastAirScored) {
                this.lastAirScored = this.getAirTicks();
                this.a(IScoreboardCriteria.AIR, MathHelper.f((float) this.lastAirScored));
            }

            if (this.getArmorStrength() != this.lastArmorScored) {
                this.lastArmorScored = this.getArmorStrength();
                this.a(IScoreboardCriteria.ARMOR, MathHelper.f((float) this.lastArmorScored));
            }

            if (this.expTotal != this.lastExpTotalScored) {
                this.lastExpTotalScored = this.expTotal;
                this.a(IScoreboardCriteria.XP, MathHelper.f((float) this.lastExpTotalScored));
            }

            // CraftBukkit start - Force max health updates
            if (this.maxHealthCache != this.getMaxHealth()) {
                this.getBukkitEntity().updateScaledHealth();
            }
            // CraftBukkit end

            if (this.expLevel != this.lastExpLevelScored) {
                this.lastExpLevelScored = this.expLevel;
                this.a(IScoreboardCriteria.LEVEL, MathHelper.f((float) this.lastExpLevelScored));
            }

            if (this.expTotal != this.lastSentExp) {
                this.lastSentExp = this.expTotal;
                this.playerConnection.sendPacket(new PacketPlayOutExperience(this.exp, this.expTotal, this.expLevel));
            }

            if (this.ticksLived % 20 == 0) {
                CriterionTriggers.p.a(this);
            }

            // CraftBukkit start - initialize oldLevel and fire PlayerLevelChangeEvent
            if (this.oldLevel == -1) {
                this.oldLevel = this.expLevel;
            }

            if (this.oldLevel != this.expLevel) {
                CraftEventFactory.callPlayerLevelChangeEvent(this.world.getServer().getPlayer((EntityPlayer) this), this.oldLevel, this.expLevel);
                this.oldLevel = this.expLevel;
            }
            // CraftBukkit end
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Ticking player");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Player being ticked");

            this.appendEntityCrashDetails(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    private void a(IScoreboardCriteria iscoreboardcriteria, int i) {
        // CraftBukkit - Use our scores instead
        this.world.getServer().getScoreboardManager().getScoreboardScores(iscoreboardcriteria, this.getName(), (scoreboardscore) -> {
            scoreboardscore.setScore(i);
        });
    }

    // Paper start - process inventory
    private static void processKeep(org.bukkit.event.entity.PlayerDeathEvent event, NonNullList<ItemStack> inv) {
        List<org.bukkit.inventory.ItemStack> itemsToKeep = event.getItemsToKeep();
        if (inv == null) {
            // remainder of items left in toKeep - plugin added stuff on death that wasn't in the initial loot?
            if (!itemsToKeep.isEmpty()) {
                for (org.bukkit.inventory.ItemStack itemStack : itemsToKeep) {
                    event.getEntity().getInventory().addItem(itemStack);
                }
            }

            return;
        }

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack item = inv.get(i);
            if (EnchantmentManager.shouldNotDrop(item) || itemsToKeep.isEmpty() || item.isEmpty()) {
                inv.set(i, ItemStack.NULL_ITEM);
                continue;
            }

            final org.bukkit.inventory.ItemStack bukkitStack = item.getBukkitStack();
            boolean keep = false;
            final Iterator<org.bukkit.inventory.ItemStack> iterator = itemsToKeep.iterator();
            while (iterator.hasNext()) {
                final org.bukkit.inventory.ItemStack itemStack = iterator.next();
                if (bukkitStack.equals(itemStack)) {
                    iterator.remove();
                    keep = true;
                    break;
                }
            }

            if (!keep) {
                inv.set(i, ItemStack.NULL_ITEM);
            }
        }
    }
    // Paper end

    @Override
    public void die(DamageSource damagesource) {
        boolean flag = this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES);
        // CraftBukkit start - fire PlayerDeathEvent
        if (this.dead) {
            return;
        }
        java.util.List<org.bukkit.inventory.ItemStack> loot = new java.util.ArrayList<org.bukkit.inventory.ItemStack>(this.inventory.getSize());
        boolean keepInventory = this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || this.isSpectator();

        if (!keepInventory) {
            for (ItemStack item : this.inventory.getContents()) {
                if (!item.isEmpty() && !EnchantmentManager.shouldNotDrop(item)) {
                    loot.add(CraftItemStack.asCraftMirror(item));
                }
            }
        }
        // SPIGOT-5071: manually add player loot tables (SPIGOT-5195 - ignores keepInventory rule)
        this.a(damagesource, this.lastDamageByPlayerTime > 0);
        for (org.bukkit.inventory.ItemStack item : this.drops) {
            loot.add(item);
        }
        this.drops.clear(); // SPIGOT-5188: make sure to clear

        IChatBaseComponent defaultMessage = this.getCombatTracker().getDeathMessage();

        String deathmessage = defaultMessage.getString();
        org.bukkit.event.entity.PlayerDeathEvent event = CraftEventFactory.callPlayerDeathEvent(this, loot, deathmessage, keepInventory);
        // Paper start - cancellable death event
        if (event.isCancelled()) {
            // make compatible with plugins that might have already set the health in an event listener
            if (this.getHealth() <= 0) {
                this.setHealth((float) event.getReviveHealth());
            }
            return;
        }
        // Paper end

        // SPIGOT-943 - only call if they have an inventory open
        if (this.activeContainer != this.defaultContainer) {
            this.closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason.DEATH); // Paper
        }

        String deathMessage = event.getDeathMessage();

        if (deathMessage != null && deathMessage.length() > 0 && flag) { // TODO: allow plugins to override?
            IChatBaseComponent ichatbasecomponent;
            if (deathMessage.equals(deathmessage)) {
                ichatbasecomponent = this.getCombatTracker().getDeathMessage();
            } else {
                ichatbasecomponent = org.bukkit.craftbukkit.util.CraftChatMessage.fromStringOrNull(deathMessage);
            }

            this.playerConnection.a((Packet) (new PacketPlayOutCombatEvent(this.getCombatTracker(), PacketPlayOutCombatEvent.EnumCombatEventType.ENTITY_DIED, ichatbasecomponent)), (future) -> {
                if (!future.isSuccess()) {
                    boolean flag1 = true;
                    String s = ichatbasecomponent.a(256);
                    ChatMessage chatmessage = new ChatMessage("death.attack.message_too_long", new Object[]{(new ChatComponentText(s)).a(EnumChatFormat.YELLOW)});
                    IChatBaseComponent ichatbasecomponent1 = (new ChatMessage("death.attack.even_more_magic", new Object[]{this.getScoreboardDisplayName()})).a((chatmodifier) -> {
                        chatmodifier.setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_TEXT, chatmessage));
                    });

                    this.playerConnection.sendPacket(new PacketPlayOutCombatEvent(this.getCombatTracker(), PacketPlayOutCombatEvent.EnumCombatEventType.ENTITY_DIED, ichatbasecomponent1));
                }

            });
            ScoreboardTeamBase scoreboardteambase = this.getScoreboardTeam();

            if (scoreboardteambase != null && scoreboardteambase.getDeathMessageVisibility() != ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS) {
                if (scoreboardteambase.getDeathMessageVisibility() == ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OTHER_TEAMS) {
                    this.server.getPlayerList().a((EntityHuman) this, ichatbasecomponent);
                } else if (scoreboardteambase.getDeathMessageVisibility() == ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OWN_TEAM) {
                    this.server.getPlayerList().b(this, ichatbasecomponent);
                }
            } else {
                this.server.getPlayerList().sendMessage(ichatbasecomponent);
            }
        } else {
            this.playerConnection.sendPacket(new PacketPlayOutCombatEvent(this.getCombatTracker(), PacketPlayOutCombatEvent.EnumCombatEventType.ENTITY_DIED));
        }

        this.releaseShoulderEntities();
        // SPIGOT-5478 must be called manually now
        if (event.shouldDropExperience()) this.dropExperience(); // Paper - tie to event
        // we clean the player's inventory after the EntityDeathEvent is called so plugins can get the exact state of the inventory.
        if (!event.getKeepInventory()) {
            // Paper start - replace logic
            for (NonNullList<ItemStack> inv : this.inventory.getComponents()) {
                processKeep(event, inv);
            }
            processKeep(event, null);
            // Paper end
        }

        this.setSpectatorTarget(this); // Remove spectated target
        // CraftBukkit end

        // CraftBukkit - Get our scores instead
        this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreboardCriteria.DEATH_COUNT, this.getName(), ScoreboardScore::incrementScore);
        EntityLiving entityliving = this.getKillingEntity();

        if (entityliving != null) {
            this.b(StatisticList.ENTITY_KILLED_BY.b(entityliving.getEntityType()));
            entityliving.a(this, this.aW, damagesource);
            this.f(entityliving);
        }

        this.world.broadcastEntityEffect(this, (byte) 3);
        this.a(StatisticList.DEATHS);
        this.a(StatisticList.CUSTOM.b(StatisticList.TIME_SINCE_DEATH));
        this.a(StatisticList.CUSTOM.b(StatisticList.TIME_SINCE_REST));
        this.extinguish();
        this.setFlag(0, false);
        this.getCombatTracker().g();
    }

    @Override
    public void a(Entity entity, int i, DamageSource damagesource) {
        if (entity != this) {
            super.a(entity, i, damagesource);
            this.addScore(i);
            String s = this.getName();
            String s1 = entity.getName();

            // CraftBukkit - Get our scores instead
            this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreboardCriteria.TOTAL_KILL_COUNT, s, ScoreboardScore::incrementScore);
            if (entity instanceof EntityHuman) {
                this.a(StatisticList.PLAYER_KILLS);
                // CraftBukkit - Get our scores instead
                this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreboardCriteria.PLAYER_KILL_COUNT, s, ScoreboardScore::incrementScore);
            } else {
                this.a(StatisticList.MOB_KILLS);
            }

            this.a(s, s1, IScoreboardCriteria.m);
            this.a(s1, s, IScoreboardCriteria.n);
            CriterionTriggers.b.a(this, entity, damagesource);
        }
    }

    private void a(String s, String s1, IScoreboardCriteria[] aiscoreboardcriteria) {
        ScoreboardTeam scoreboardteam = this.getScoreboard().getPlayerTeam(s1);

        if (scoreboardteam != null) {
            int i = scoreboardteam.getColor().b();

            if (i >= 0 && i < aiscoreboardcriteria.length) {
                // CraftBukkit - Get our scores instead
                this.world.getServer().getScoreboardManager().getScoreboardScores(aiscoreboardcriteria[i], s, ScoreboardScore::incrementScore);
            }
        }

    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        } else {
            boolean flag = this.server.m() && this.canPvP() && "fall".equals(damagesource.translationIndex);

            if (!flag && this.invulnerableTicks > 0 && damagesource != DamageSource.OUT_OF_WORLD) {
                return false;
            } else {
                if (damagesource instanceof EntityDamageSource) {
                    Entity entity = damagesource.getEntity();

                    if (entity instanceof EntityHuman && !this.a((EntityHuman) entity)) {
                        return false;
                    }

                    if (entity instanceof EntityArrow) {
                        EntityArrow entityarrow = (EntityArrow) entity;
                        Entity entity1 = entityarrow.getShooter();

                        if (entity1 instanceof EntityHuman && !this.a((EntityHuman) entity1)) {
                            return false;
                        }
                    }
                }
                // Paper start - cancellable death events
                //return super.damageEntity(damagesource, f);
                this.queueHealthUpdatePacket = true;
                boolean damaged = super.damageEntity(damagesource, f);
                this.queueHealthUpdatePacket = false;
                if (this.queuedHealthUpdatePacket != null) {
                    this.playerConnection.sendPacket(this.queuedHealthUpdatePacket);
                    this.queuedHealthUpdatePacket = null;
                }
                return damaged;
                // Paper end
            }
        }
    }

    @Override
    public boolean a(EntityHuman entityhuman) {
        return !this.canPvP() ? false : super.a(entityhuman);
    }

    private boolean canPvP() {
        // CraftBukkit - this.server.getPvP() -> this.world.pvpMode
        return this.world.pvpMode;
    }

    @Nullable
    @Override
    public Entity a(DimensionManager dimensionmanager) {
        // CraftBukkit start
        return a(dimensionmanager, TeleportCause.UNKNOWN);
    }

    @Nullable
    public Entity a(DimensionManager dimensionmanager, PlayerTeleportEvent.TeleportCause cause) {
        // CraftBukkit end
        if (this.isSleeping()) return this; // CraftBukkit - SPIGOT-3154
        // this.worldChangeInvuln = true; // CraftBukkit - Moved down and into PlayerList#changeDimension
        DimensionManager dimensionmanager1 = this.dimension;

        if (dimensionmanager1.getType() == DimensionManager.THE_END && dimensionmanager.getType() == DimensionManager.OVERWORLD) { // CraftBukkit - getType()
            this.worldChangeInvuln = true; // CraftBukkit - Moved down from above
            this.decouple();
            this.getWorldServer().removePlayer(this);
            if (!this.viewingCredits) {
                this.viewingCredits = true;
                if (world.paperConfig.disableEndCredits) this.setHasSeenCredits(true); // Paper - Toggle to always disable end credits
                this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(4, this.cm ? 0.0F : 1.0F));
                this.cm = true;
            }

            return this;
        } else {
            WorldServer worldserver = this.server.getWorldServer(dimensionmanager1);

            // this.dimension = dimensionmanager; // CraftBukkit
            WorldServer worldserver1 = this.server.getWorldServer(dimensionmanager);
            // CraftBukkit start
            /*
            WorldData worlddata = worldserver1.getWorldData();

            this.playerConnection.sendPacket(new PacketPlayOutRespawn(dimensionmanager, WorldData.c(worlddata.getSeed()), worlddata.getType(), this.playerInteractManager.getGameMode()));
            this.playerConnection.sendPacket(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
            PlayerList playerlist = this.server.getPlayerList();

            playerlist.d(this);
            worldserver.removePlayer(this);
            this.dead = false;
            */
            // CraftBukkit end
            double d0 = this.locX();
            double d1 = this.locY();
            double d2 = this.locZ();
            float f = this.pitch;
            float f1 = this.yaw;
            double d3 = 8.0D;
            float f2 = f1;

            worldserver.getMethodProfiler().enter("moving");
            if (worldserver1 == null) { } else // CraftBukkit - empty to fall through to null to event
            if (dimensionmanager1 == DimensionManager.OVERWORLD && dimensionmanager == DimensionManager.NETHER) {
                this.cr = this.getPositionVector();
                d0 /= 8.0D;
                d2 /= 8.0D;
            } else if (dimensionmanager1 == DimensionManager.NETHER && dimensionmanager == DimensionManager.OVERWORLD) {
                d0 *= 8.0D;
                d2 *= 8.0D;
            } else if (dimensionmanager1 == DimensionManager.OVERWORLD && dimensionmanager == DimensionManager.THE_END) {
                BlockPosition blockposition = worldserver1.getDimensionSpawn();

                d0 = (double) blockposition.getX();
                d1 = (double) blockposition.getY();
                d2 = (double) blockposition.getZ();
                f1 = 90.0F;
                f = 0.0F;
            }

            // CraftBukkit start
            Location enter = this.getBukkitEntity().getLocation();
            Location exit = (worldserver1 == null) ? null : new Location(worldserver1.getWorld(), d0, d1, d2, f1, f);
            int configuredSearchRadius = (worldserver1 == null ? worldserver : worldserver1).paperConfig.portalSearchRadius;
            int configuredCreateRadius = (worldserver1 == null ? worldserver : worldserver1).paperConfig.portalCreateRadius;
            PlayerPortalEvent event = new PlayerPortalEvent(this.getBukkitEntity(), enter, exit, cause, configuredSearchRadius, true, dimensionmanager.getType() == DimensionManager.THE_END ? 0 : configuredCreateRadius); // Paper - configurable portal search radius
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled() || event.getTo() == null) {
                return null;
            }

            exit = event.getTo();
            if (exit == null) {
                return null;
            }
            worldserver1 = ((CraftWorld) exit.getWorld()).getHandle();
            d0 = exit.getX();
            d1 = exit.getY();
            d2 = exit.getZ();
            // CraftBukkit end

            // this.setPositionRotation(d0, d1, d2, f1, f); // CraftBukkit - PlayerTeleportEvent handles position changes
            worldserver.getMethodProfiler().exit();
            worldserver.getMethodProfiler().enter("placing");
            // Spigot start - SPIGOT-5677, MC-114796: Fix portals generating outside world border
            double d4 = Math.max(-2.9999872E7D, worldserver1.getWorldBorder().c() + 16.0D);
            double d5 = Math.max(-2.9999872E7D, worldserver1.getWorldBorder().d() + 16.0D);
            // Spigot end
            double d6 = Math.min(2.9999872E7D, worldserver1.getWorldBorder().e() - 16.0D);
            double d7 = Math.min(2.9999872E7D, worldserver1.getWorldBorder().f() - 16.0D);

            d0 = MathHelper.a(d0, d4, d6);
            d2 = MathHelper.a(d2, d5, d7);
            // this.setPositionRotation(d0, d1, d2, f1, f); // CraftBukkit - PlayerTeleportEvent handles position changes
            // CraftBukkit start - PlayerPortalEvent implementation
            Vec3D exitVelocity = Vec3D.a;
            BlockPosition exitPosition = new BlockPosition(d0, d1, d2);
            if (dimensionmanager.getType() == DimensionManager.THE_END) { // CraftBukkit - getType()
                int i = exitPosition.getX();
                int j = exitPosition.getY() - 1;
                int k = exitPosition.getZ();
                if (event.getCanCreatePortal()) {
                // CraftBukkit end
                boolean flag = true;
                boolean flag1 = false;
                org.bukkit.craftbukkit.util.BlockStateListPopulator blockList = new org.bukkit.craftbukkit.util.BlockStateListPopulator(worldserver1); // CraftBukkit - Use BlockStateListPopulator

                for (int l = -2; l <= 2; ++l) {
                    for (int i1 = -2; i1 <= 2; ++i1) {
                        for (int j1 = -1; j1 < 3; ++j1) {
                            int k1 = i + i1 * 1 + l * 0;
                            int l1 = j + j1;
                            int i2 = k + i1 * 0 - l * 1;
                            boolean flag2 = j1 < 0;

                            blockList.setTypeAndData(new BlockPosition(k1, l1, i2), flag2 ? Blocks.OBSIDIAN.getBlockData() : Blocks.AIR.getBlockData(), 3); // CraftBukkit
                        }
                    }
                }

                // CraftBukkit start
                org.bukkit.World bworld = worldserver1.getWorld();
                org.bukkit.event.world.PortalCreateEvent portalEvent = new org.bukkit.event.world.PortalCreateEvent((List<org.bukkit.block.BlockState>) (List) blockList.getList(), bworld, this.getBukkitEntity(), org.bukkit.event.world.PortalCreateEvent.CreateReason.END_PLATFORM);

                this.world.getServer().getPluginManager().callEvent(portalEvent);
                if (!portalEvent.isCancelled()) {
                    blockList.updateList();
                }
                }
                // handled below for PlayerTeleportEvent
                // this.setPositionRotation((double) i, (double) j, (double) k, f1, 0.0F);
                exit.setX(i);
                exit.setY(j);
                exit.setZ(k);
                // this.setMot(Vec3D.a);
                exitVelocity = Vec3D.a;
            } else {
                ShapeDetector.Shape portalShape = worldserver1.getTravelAgent().findAndTeleport(this, exitPosition, f2, event.getSearchRadius(), true);
                if (portalShape == null && event.getCanCreatePortal()) {
                    if (worldserver1.getTravelAgent().createPortal(this, exitPosition, event.getCreationRadius())) { // Only check for new portal if creation succeeded
                        portalShape = worldserver1.getTravelAgent().findAndTeleport(this, exitPosition, f2, event.getSearchRadius(), true);
                    }
                }
                // Check if portal was found
                if (portalShape == null) {
                    return null;
                }
                // Teleport handling - logic from PortalTravelAgent#findAndTeleport
                exitVelocity = portalShape.velocity;
                exit.setX(portalShape.position.getX());
                exit.setY(portalShape.position.getY());
                exit.setZ(portalShape.position.getZ());
                exit.setYaw(f2 + (float) portalShape.yaw);
                // CraftBukkit end
            }

            worldserver.getMethodProfiler().exit();
            // CraftBukkit start - PlayerTeleportEvent
            PlayerTeleportEvent tpEvent = new PlayerTeleportEvent(this.getBukkitEntity(), enter, exit, cause);
            Bukkit.getServer().getPluginManager().callEvent(tpEvent);
            if (tpEvent.isCancelled() || tpEvent.getTo() == null) {
                return null;
            }

            exit = tpEvent.getTo();
            if (exit == null) {
                return null;
            }
            worldserver1 = ((CraftWorld) exit.getWorld()).getHandle();
            this.worldChangeInvuln = true; // CraftBukkit - Set teleport invulnerability only if player changing worlds
            dimensionmanager = worldserver1.getWorldProvider().getDimensionManager();

            this.dimension = dimensionmanager;

            this.playerConnection.sendPacket(new PacketPlayOutRespawn(worldserver1.worldProvider.getDimensionManager().getType(), WorldData.c(this.world.getWorldData().getSeed()), this.world.getWorldData().getType(), this.playerInteractManager.getGameMode()));
            this.playerConnection.sendPacket(new PacketPlayOutServerDifficulty(this.world.getDifficulty(), this.world.getWorldData().isDifficultyLocked()));
            PlayerList playerlist = this.server.getPlayerList();

            playerlist.d(this);
            worldserver.removePlayer(this);
            this.dead = false;

            this.setMot(exitVelocity);
            // CraftBukkit end
            this.spawnIn(worldserver1);
            worldserver1.addPlayerPortal(this);
            this.triggerDimensionAdvancements(worldserver);
            this.playerConnection.teleport(exit); // CraftBukkit - use internal teleport without event
            this.playerConnection.syncPosition(); // CraftBukkit - sync position after changing it (from PortalTravelAgent#findAndteleport)
            this.playerInteractManager.a(worldserver1);
            this.playerConnection.sendPacket(new PacketPlayOutAbilities(this.abilities));
            playerlist.a(this, worldserver1);
            playerlist.updateClient(this);
            Iterator iterator = this.getEffects().iterator();

            while (iterator.hasNext()) {
                MobEffect mobeffect = (MobEffect) iterator.next();

                this.playerConnection.sendPacket(new PacketPlayOutEntityEffect(this.getId(), mobeffect));
            }

            this.playerConnection.sendPacket(new PacketPlayOutWorldEvent(1032, BlockPosition.ZERO, 0, false));
            this.lastSentExp = -1;
            this.lastHealthSent = -1.0F;
            this.lastFoodSent = -1;

            setSneaking(false); // Paper - fix MC-10657

            // CraftBukkit start
            PlayerChangedWorldEvent changeEvent = new PlayerChangedWorldEvent(this.getBukkitEntity(), worldserver.getWorld());
            this.world.getServer().getPluginManager().callEvent(changeEvent);
            // CraftBukkit end
            return this;
        }
    }

    public void triggerDimensionAdvancements(WorldServer worldserver) {
        DimensionManager dimensionmanager = worldserver.worldProvider.getDimensionManager();
        DimensionManager dimensionmanager1 = this.world.worldProvider.getDimensionManager();

        CriterionTriggers.v.a(this, dimensionmanager, dimensionmanager1);
        if (dimensionmanager == DimensionManager.NETHER && dimensionmanager1 == DimensionManager.OVERWORLD && this.cr != null) {
            CriterionTriggers.C.a(this, this.cr);
        }

        if (dimensionmanager1 != DimensionManager.NETHER) {
            this.cr = null;
        }

    }

    @Override
    public boolean a(EntityPlayer entityplayer) {
        return entityplayer.isSpectator() ? this.getSpecatorTarget() == this : (this.isSpectator() ? false : super.a(entityplayer));
    }

    private void a(TileEntity tileentity) {
        if (tileentity != null) {
            PacketPlayOutTileEntityData packetplayouttileentitydata = tileentity.getUpdatePacket();

            if (packetplayouttileentitydata != null) {
                this.playerConnection.sendPacket(packetplayouttileentitydata);
            }
        }

    }

    @Override
    public void receive(Entity entity, int i) {
        super.receive(entity, i);
        this.activeContainer.c();
    }

        @Override
    public Either<EntityHuman.EnumBedResult, Unit> sleep(BlockPosition blockposition) {
        // CraftBukkit start - add force parameter
        return this.sleep(blockposition, false);
    }

    @Override
    public Either<EntityHuman.EnumBedResult, Unit> sleep(BlockPosition blockposition, boolean force) {
        // CraftBukkit end
        return super.sleep(blockposition, force).ifRight((unit) -> {
            this.a(StatisticList.SLEEP_IN_BED);
            CriterionTriggers.q.a(this);
        });
    }

    @Override
    public void wakeup(boolean flag, boolean flag1) {
        if (!this.isSleeping()) return; // CraftBukkit - Can't leave bed if not in one!
        if (this.isSleeping()) {
            this.getWorldServer().getChunkProvider().broadcastIncludingSelf(this, new PacketPlayOutAnimation(this, 2));
        }

        super.wakeup(flag, flag1);
        if (this.playerConnection != null) {
            this.playerConnection.a(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
        }

    }

    @Override
    public boolean a(Entity entity, boolean flag) {
        Entity entity1 = this.getVehicle();

        if (!super.a(entity, flag)) {
            return false;
        } else {
            Entity entity2 = this.getVehicle();

            if (entity2 != entity1 && this.playerConnection != null) {
                this.playerConnection.a(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
            }

            return true;
        }
    }

    // Paper start
    @Override public void stopRiding() { stopRiding(false); }
    @Override public void stopRiding(boolean suppressCancellation) {
        // paper end
        Entity entity = this.getVehicle();

        super.stopRiding(suppressCancellation); // Paper
        Entity entity1 = this.getVehicle();

        if (entity1 != entity && this.playerConnection != null) {
            this.playerConnection.a(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
        }

    }

    @Override
    public boolean isInvulnerable(DamageSource damagesource) {
        return super.isInvulnerable(damagesource) || this.H() || this.abilities.isInvulnerable && damagesource == DamageSource.WITHER;
    }

    @Override
    protected void a(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {}

    @Override
    protected void b(BlockPosition blockposition) {
        if (!this.isSpectator()) {
            super.b(blockposition);
        }

    }

    public void a(double d0, boolean flag) {
        BlockPosition blockposition = this.ag();

        if (this.world.isLoaded(blockposition)) {
            IBlockData iblockdata = this.world.getType(blockposition);

            super.a(d0, flag, iblockdata, blockposition);
        }
    }

    @Override
    public void openSign(TileEntitySign tileentitysign) {
        tileentitysign.a((EntityHuman) this);
        this.playerConnection.sendPacket(new PacketPlayOutOpenSignEditor(tileentitysign.getPosition()));
    }

    public int nextContainerCounter() { // CraftBukkit - void -> int
        this.containerCounter = this.containerCounter % 100 + 1;
        return containerCounter; // CraftBukkit
    }

    @Override
    public OptionalInt openContainer(@Nullable ITileInventory itileinventory) {
        if (itileinventory == null) {
            return OptionalInt.empty();
        } else {
            if (this.activeContainer != this.defaultContainer) {
                this.closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason.OPEN_NEW); // Paper
            }

            this.nextContainerCounter();
            Container container = itileinventory.createMenu(this.containerCounter, this.inventory, this);

            // CraftBukkit start - Inventory open hook
            if (container != null) {
                container.setTitle(itileinventory.getScoreboardDisplayName());

                boolean cancelled = false;
                container = CraftEventFactory.callInventoryOpenEvent(this, container, cancelled);
                if (container == null && !cancelled) { // Let pre-cancelled events fall through
                    // SPIGOT-5263 - close chest if cancelled
                    if (itileinventory instanceof IInventory) {
                        ((IInventory) itileinventory).closeContainer(this);
                    } else if (itileinventory instanceof BlockChest.DoubleInventory) {
                        // SPIGOT-5355 - double chests too :(
                        ((BlockChest.DoubleInventory) itileinventory).inventorylargechest.closeContainer(this);
                    }
                    return OptionalInt.empty();
                }
            }
            // CraftBukkit end
            if (container == null) {
                if (this.isSpectator()) {
                    this.a((new ChatMessage("container.spectatorCantOpen", new Object[0])).a(EnumChatFormat.RED), true);
                }

                return OptionalInt.empty();
            } else {
                // CraftBukkit start
                this.activeContainer = container;
                if (!isFrozen()) this.playerConnection.sendPacket(new PacketPlayOutOpenWindow(container.windowId, container.getType(), container.getTitle())); // Paper
                // CraftBukkit end
                container.addSlotListener(this);
                return OptionalInt.of(this.containerCounter);
            }
        }
    }

    @Override
    public void openTrade(int i, MerchantRecipeList merchantrecipelist, int j, int k, boolean flag, boolean flag1) {
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindowMerchant(i, merchantrecipelist, j, k, flag, flag1));
    }

    @Override
    public void openHorseInventory(EntityHorseAbstract entityhorseabstract, IInventory iinventory) {
        // CraftBukkit start - Inventory open hook
        this.nextContainerCounter();
        Container container = new ContainerHorse(this.containerCounter, this.inventory, iinventory, entityhorseabstract);
        container.setTitle(entityhorseabstract.getScoreboardDisplayName());
        container = CraftEventFactory.callInventoryOpenEvent(this, container);

        if (container == null) {
            iinventory.closeContainer(this);
            return;
        }
        // CraftBukkit end
        if (this.activeContainer != this.defaultContainer) {
            this.closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason.OPEN_NEW); // Paper
        }

        // this.nextContainerCounter(); // CraftBukkit - moved up
        this.playerConnection.sendPacket(new PacketPlayOutOpenWindowHorse(this.containerCounter, iinventory.getSize(), entityhorseabstract.getId()));
        this.activeContainer = container; // CraftBukkit
        this.activeContainer.addSlotListener(this);
    }

    @Override
    public void openBook(ItemStack itemstack, EnumHand enumhand) {
        Item item = itemstack.getItem();

        if (item == Items.WRITTEN_BOOK) {
            if (ItemWrittenBook.a(itemstack, this.getCommandListener(), (EntityHuman) this)) {
                this.activeContainer.c();
            }

            this.playerConnection.sendPacket(new PacketPlayOutOpenBook(enumhand));
        }

    }

    @Override
    public void a(TileEntityCommand tileentitycommand) {
        tileentitycommand.c(true);
        this.a((TileEntity) tileentitycommand);
    }

    @Override
    public void a(Container container, int i, ItemStack itemstack) {
        if (!(container.getSlot(i) instanceof SlotResult)) {
            if (container == this.defaultContainer) {
                CriterionTriggers.e.a(this, this.inventory);
            }

            if (!this.e) {
                this.playerConnection.sendPacket(new PacketPlayOutSetSlot(container.windowId, i, itemstack));
            }
        }
    }

    public void updateInventory(Container container) {
        this.a(container, container.b());
    }

    @Override
    public void a(Container container, NonNullList<ItemStack> nonnulllist) {
        this.playerConnection.sendPacket(new PacketPlayOutWindowItems(container.windowId, nonnulllist));
        this.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.inventory.getCarried()));
        // CraftBukkit start - Send a Set Slot to update the crafting result slot
        if (java.util.EnumSet.of(InventoryType.CRAFTING,InventoryType.WORKBENCH).contains(container.getBukkitView().getType())) {
            this.playerConnection.sendPacket(new PacketPlayOutSetSlot(container.windowId, 0, container.getSlot(0).getItem()));
        }
        // CraftBukkit end
    }

    @Override
    public void setContainerData(Container container, int i, int j) {
        this.playerConnection.sendPacket(new PacketPlayOutWindowData(container.windowId, i, j));
    }

    @Override
    public void closeInventory() {
        // Paper start
        closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason.UNKNOWN);
    }
    public void closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason reason) {
        CraftEventFactory.handleInventoryCloseEvent(this, reason); // CraftBukkit
        // Paper end
        this.playerConnection.sendPacket(new PacketPlayOutCloseWindow(this.activeContainer.windowId));
        this.m();
    }

    public void broadcastCarriedItem() {
        if (!this.e) {
            this.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.inventory.getCarried()));
        }
    }

    public void m() {
        this.activeContainer.b((EntityHuman) this);
        this.activeContainer = this.defaultContainer;
    }

    public void a(float f, float f1, boolean flag, boolean flag1) {
        if (this.isPassenger()) {
            if (f >= -1.0F && f <= 1.0F) {
                this.aZ = f;
            }

            if (f1 >= -1.0F && f1 <= 1.0F) {
                this.bb = f1;
            }

            this.jumping = flag;
            this.setSneaking(flag1);
        }

    }

    @Override
    public void a(Statistic<?> statistic, int i) {
        this.serverStatisticManager.b(this, statistic, i);
        this.world.getServer().getScoreboardManager().getScoreboardScores(statistic, this.getName(), (scoreboardscore) -> { // CraftBukkit - Get our scores instead
            scoreboardscore.addScore(i);
        });
    }

    @Override
    public void a(Statistic<?> statistic) {
        this.serverStatisticManager.setStatistic(this, statistic, 0);
        this.world.getServer().getScoreboardManager().getScoreboardScores(statistic, this.getName(), ScoreboardScore::c); // CraftBukkit - Get our scores instead
    }

    @Override
    public int discoverRecipes(Collection<IRecipe<?>> collection) {
        return this.recipeBook.a(collection, this);
    }

    @Override
    public void a(MinecraftKey[] aminecraftkey) {
        List<IRecipe<?>> list = Lists.newArrayList();
        MinecraftKey[] aminecraftkey1 = aminecraftkey;
        int i = aminecraftkey.length;

        for (int j = 0; j < i; ++j) {
            MinecraftKey minecraftkey = aminecraftkey1[j];

            this.server.getCraftingManager().a(minecraftkey).ifPresent(list::add);
        }

        this.discoverRecipes(list);
    }

    @Override
    public int undiscoverRecipes(Collection<IRecipe<?>> collection) {
        return this.recipeBook.b(collection, this);
    }

    @Override
    public void giveExp(int i) {
        super.giveExp(i);
        this.lastSentExp = -1;
    }

    public void n() {
        this.cq = true;
        this.ejectPassengers();

        // Paper start - Workaround an issue where the vehicle doesn't track the passenger disconnection dismount.
        if (this.isPassenger() && this.getVehicle() instanceof EntityPlayer) {
            this.stopRiding();
        }
        // Paper end

        if (this.isSleeping()) {
            this.wakeup(true, false);
        }

    }

    public boolean o() {
        return this.cq;
    }

    public void triggerHealthUpdate() {
        this.lastHealthSent = -1.0E8F;
        this.lastSentExp = -1; // CraftBukkit - Added to reset
    }

    // CraftBukkit start - Support multi-line messages
    public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {
        for (IChatBaseComponent component : ichatbasecomponent) {
            this.sendMessage(component);
        }
    }
    // CraftBukkit end

    @Override
    public void a(IChatBaseComponent ichatbasecomponent, boolean flag) {
        this.playerConnection.sendPacket(new PacketPlayOutChat(ichatbasecomponent, flag ? ChatMessageType.GAME_INFO : ChatMessageType.CHAT));
    }

    @Override
    protected void q() {
        if (!this.activeItem.isEmpty() && this.isHandRaised()) {
            this.playerConnection.sendPacket(new PacketPlayOutEntityStatus(this, (byte) 9));
            super.q();
        }

    }

    @Override
    public void a(ArgumentAnchor.Anchor argumentanchor_anchor, Vec3D vec3d) {
        super.a(argumentanchor_anchor, vec3d);
        this.playerConnection.sendPacket(new PacketPlayOutLookAt(argumentanchor_anchor, vec3d.x, vec3d.y, vec3d.z));
    }

    public void a(ArgumentAnchor.Anchor argumentanchor_anchor, Entity entity, ArgumentAnchor.Anchor argumentanchor_anchor1) {
        Vec3D vec3d = argumentanchor_anchor1.a(entity);

        super.a(argumentanchor_anchor, vec3d);
        this.playerConnection.sendPacket(new PacketPlayOutLookAt(argumentanchor_anchor, entity, argumentanchor_anchor1));
    }

    public void copyFrom(EntityPlayer entityplayer, boolean flag) {
        if (flag) {
            this.inventory.a(entityplayer.inventory);
            this.setHealth(entityplayer.getHealth());
            this.foodData = entityplayer.foodData;
            this.expLevel = entityplayer.expLevel;
            this.expTotal = entityplayer.expTotal;
            this.exp = entityplayer.exp;
            this.setScore(entityplayer.getScore());
            this.ai = entityplayer.ai;
            this.aj = entityplayer.aj;
            this.ak = entityplayer.ak;
        } else if (this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || entityplayer.isSpectator()) {
            this.inventory.a(entityplayer.inventory);
            this.expLevel = entityplayer.expLevel;
            this.expTotal = entityplayer.expTotal;
            this.exp = entityplayer.exp;
            this.setScore(entityplayer.getScore());
        }

        this.bO = entityplayer.bO;
        this.enderChest = entityplayer.enderChest;
        this.getDataWatcher().set(EntityPlayer.bq, entityplayer.getDataWatcher().get(EntityPlayer.bq));
        this.lastSentExp = -1;
        this.lastHealthSent = -1.0F;
        this.lastFoodSent = -1;
        // this.recipeBook.a((RecipeBook) entityplayer.recipeBook); // CraftBukkit
        // Paper start - Optimize remove queue - vanilla copies player objects, but CB doesn't. This method currently only
        // Applies to the same player, so we need to not duplicate our removal queue. The rest of this method does "resetting"
        // type logic so it does need to be called, maybe? This is silly.
        //this.removeQueue.addAll(entityplayer.removeQueue);
        if (this.removeQueue != entityplayer.removeQueue) {
            this.removeQueue.addAll(entityplayer.removeQueue);
        }
        // Paper end
        this.cm = entityplayer.cm;
        this.cr = entityplayer.cr;
        this.setShoulderEntityLeft(entityplayer.getShoulderEntityLeft());
        this.setShoulderEntityRight(entityplayer.getShoulderEntityRight());

        this.inLava = false; // SPIGOT-4767
    }

    @Override
    protected void a(MobEffect mobeffect) {
        super.a(mobeffect);
        this.playerConnection.sendPacket(new PacketPlayOutEntityEffect(this.getId(), mobeffect));
        if (mobeffect.getMobEffect() == MobEffects.LEVITATION) {
            this.cp = this.ticksLived;
            this.co = this.getPositionVector();
        }

        CriterionTriggers.A.a(this);
    }

    @Override
    protected void a(MobEffect mobeffect, boolean flag) {
        super.a(mobeffect, flag);
        this.playerConnection.sendPacket(new PacketPlayOutEntityEffect(this.getId(), mobeffect));
        CriterionTriggers.A.a(this);
    }

    @Override
    protected void b(MobEffect mobeffect) {
        super.b(mobeffect);
        this.playerConnection.sendPacket(new PacketPlayOutRemoveEntityEffect(this.getId(), mobeffect.getMobEffect()));
        if (mobeffect.getMobEffect() == MobEffects.LEVITATION) {
            this.co = null;
        }

        CriterionTriggers.A.a(this);
    }

    @Override
    public void enderTeleportTo(double d0, double d1, double d2) {
        this.playerConnection.a(d0, d1, d2, this.yaw, this.pitch);
    }

    @Override
    public void teleportAndSync(double d0, double d1, double d2) {
        this.playerConnection.a(d0, d1, d2, this.yaw, this.pitch);
        this.playerConnection.syncPosition();
    }

    @Override
    public void a(Entity entity) {
        this.getWorldServer().getChunkProvider().broadcastIncludingSelf(this, new PacketPlayOutAnimation(entity, 4));
    }

    @Override
    public void b(Entity entity) {
        this.getWorldServer().getChunkProvider().broadcastIncludingSelf(this, new PacketPlayOutAnimation(entity, 5));
    }

    @Override
    public void updateAbilities() {
        if (this.playerConnection != null) {
            this.playerConnection.sendPacket(new PacketPlayOutAbilities(this.abilities));
            this.C();
        }
    }

    public WorldServer getWorldServer() {
        return (WorldServer) this.world;
    }

    @Override
    public void a(EnumGamemode enumgamemode) {
        // CraftBukkit start
        if (enumgamemode == this.playerInteractManager.getGameMode()) {
            return;
        }

        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent(getBukkitEntity(), GameMode.getByValue(enumgamemode.getId()));
        world.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        // CraftBukkit end

        this.playerInteractManager.setGameMode(enumgamemode);
        this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(3, (float) enumgamemode.getId()));
        if (enumgamemode == EnumGamemode.SPECTATOR) {
            this.releaseShoulderEntities();
            this.stopRiding();
        } else {
            this.setSpectatorTarget(this);
        }

        this.updateAbilities();
        this.dz();
    }

    @Override
    public boolean isSpectator() {
        return this.playerInteractManager.getGameMode() == EnumGamemode.SPECTATOR;
    }

    @Override
    public boolean isCreative() {
        return this.playerInteractManager.getGameMode() == EnumGamemode.CREATIVE;
    }

    @Override
    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        this.a(ichatbasecomponent, ChatMessageType.SYSTEM);
    }

    public void a(IChatBaseComponent ichatbasecomponent, ChatMessageType chatmessagetype) {
        this.playerConnection.a((Packet) (new PacketPlayOutChat(ichatbasecomponent, chatmessagetype)), (future) -> {
            if (!future.isSuccess() && (chatmessagetype == ChatMessageType.GAME_INFO || chatmessagetype == ChatMessageType.SYSTEM)) {
                boolean flag = true;
                String s = ichatbasecomponent.a(256);
                IChatBaseComponent ichatbasecomponent1 = (new ChatComponentText(s)).a(EnumChatFormat.YELLOW);

                this.playerConnection.sendPacket(new PacketPlayOutChat((new ChatMessage("multiplayer.message_not_delivered", new Object[]{ichatbasecomponent1})).a(EnumChatFormat.RED), ChatMessageType.SYSTEM));
            }

        });
    }

    public String v() {
        String s = this.playerConnection.networkManager.getSocketAddress().toString();

        s = s.substring(s.indexOf("/") + 1);
        s = s.substring(0, s.indexOf(":"));
        return s;
    }

    public void a(PacketPlayInSettings packetplayinsettings) {
        new PlayerClientOptionsChangeEvent(getBukkitEntity(), packetplayinsettings.getLocale(), packetplayinsettings.viewDistance, com.destroystokyo.paper.ClientOption.ChatVisibility.valueOf(packetplayinsettings.getChatVisibility().name()), packetplayinsettings.hasChatColorsEnabled(), new com.destroystokyo.paper.PaperSkinParts(packetplayinsettings.getSkinParts()), packetplayinsettings.getMainHand() == EnumMainHand.LEFT ? MainHand.LEFT : MainHand.RIGHT).callEvent(); // Paper - settings event
        // CraftBukkit start
        if (getMainHand() != packetplayinsettings.getMainHand()) {
            PlayerChangedMainHandEvent event = new PlayerChangedMainHandEvent(getBukkitEntity(), getMainHand() == EnumMainHand.LEFT ? MainHand.LEFT : MainHand.RIGHT);
            this.server.server.getPluginManager().callEvent(event);
        }
        if (this.locale == null || !this.locale.equals(packetplayinsettings.b())) { // Paper - check for null
            PlayerLocaleChangeEvent event = new PlayerLocaleChangeEvent(getBukkitEntity(), packetplayinsettings.b());
            this.server.server.getPluginManager().callEvent(event);
        }
        this.clientViewDistance = packetplayinsettings.viewDistance;
        // CraftBukkit end
        // Paper start - add PlayerLocaleChangeEvent
        // Since the field is initialized to null, this event should always fire the first time the packet is received
        String oldLocale = this.locale;
        this.locale = packetplayinsettings.b();
        if (!this.locale.equals(oldLocale)) {
            new com.destroystokyo.paper.event.player.PlayerLocaleChangeEvent(this.getBukkitEntity(), oldLocale, this.locale).callEvent();
        }
        // Paper end
        this.ch = packetplayinsettings.d();
        this.ci = packetplayinsettings.e();
        this.getDataWatcher().set(EntityPlayer.bq, (byte) packetplayinsettings.f());
        this.getDataWatcher().set(EntityPlayer.br, (byte) (packetplayinsettings.getMainHand() == EnumMainHand.LEFT ? 0 : 1));
    }

    public EnumChatVisibility getChatFlags() {
        return this.ch;
    }

    public void setResourcePack(String s, String s1) {
        this.playerConnection.sendPacket(new PacketPlayOutResourcePackSend(s, s1));
    }

    @Override
    protected int y() {
        return this.server.b(this.getProfile());
    }

    public void resetIdleTimer() {
        this.cj = SystemUtils.getMonotonicMillis();
    }

    public ServerStatisticManager getStatisticManager() {
        return this.serverStatisticManager;
    }

    public RecipeBookServer B() {
        return this.recipeBook;
    }

    public void c(Entity entity) {
        if (entity instanceof EntityHuman) {
            this.playerConnection.sendPacket(new PacketPlayOutEntityDestroy(new int[]{entity.getId()}));
        } else {
            this.removeQueue.add((Integer) entity.getId()); // CraftBukkit - decompile error
        }

    }

    public void d(Entity entity) {
        this.removeQueue.remove((Integer) entity.getId()); // CraftBukkit - decompile error
    }

    @Override
    protected void C() {
        if (this.isSpectator()) {
            this.cN();
            this.setInvisible(true);
        } else {
            super.C();
        }

    }

    public Entity getSpecatorTarget() {
        return (Entity) (this.spectatedEntity == null ? this : this.spectatedEntity);
    }

    public void setSpectatorTarget(Entity newSpectatorTarget) {
        // Paper start - Add PlayerStartSpectatingEntityEvent and PlayerStopSpectatingEntity Event and improve implementation
        Entity entity1 = this.getSpecatorTarget();

        if (newSpectatorTarget == null) {
            newSpectatorTarget = this;
        }

        if (entity1 == newSpectatorTarget) return; // new spec target is the current spec target

        if (newSpectatorTarget == this) {
            com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent playerStopSpectatingEntityEvent = new com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent(this.getBukkitEntity(), entity1.getBukkitEntity());

            if (!playerStopSpectatingEntityEvent.callEvent()) {
                return;
            }
        } else {
            com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent playerStartSpectatingEntityEvent = new com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent(this.getBukkitEntity(), entity1.getBukkitEntity(), newSpectatorTarget.getBukkitEntity());

            if (!playerStartSpectatingEntityEvent.callEvent()) {
                return;
            }
        }
        // Validate
        if (newSpectatorTarget != this) {
            if (newSpectatorTarget.dead || newSpectatorTarget.shouldBeRemoved || !newSpectatorTarget.valid || newSpectatorTarget.world == null) {
                MinecraftServer.LOGGER.info("Blocking player " + this.toString() + " from spectating invalid entity " + newSpectatorTarget.toString());
                return;
            }
            if (this.isFrozen()) {
                // use debug: clients might maliciously spam this
                MinecraftServer.LOGGER.debug("Blocking frozen player " + this.toString() + " from spectating entity " + newSpectatorTarget.toString());
                return;
            }
        }

        this.spectatedEntity = newSpectatorTarget; // only set after validating state

        if (newSpectatorTarget != this) {
            // Make sure we're in the right place
            this.ejectPassengers(); // teleport can fail if we have passengers...
            this.getBukkitEntity().teleport(new Location(newSpectatorTarget.getWorld().getWorld(), newSpectatorTarget.locX(), newSpectatorTarget.locY(), newSpectatorTarget.locZ(), this.yaw, this.pitch), TeleportCause.SPECTATE); // Correctly handle cross-world entities from api calls by using CB teleport

            // Make sure we're tracking the entity before sending
            PlayerChunkMap.EntityTracker tracker = ((WorldServer)newSpectatorTarget.world).getChunkProvider().playerChunkMap.trackedEntities.get(newSpectatorTarget.getId());
            if (tracker != null) { // dumb plugins...
                tracker.updatePlayer(this);
            }
        } else {
            this.playerConnection.teleport(this.spectatedEntity.locX(), this.spectatedEntity.locY(), this.spectatedEntity.locZ(), this.yaw, this.pitch, TeleportCause.SPECTATE); // CraftBukkit
        }
        this.playerConnection.sendPacket(new PacketPlayOutCamera(newSpectatorTarget));
        // Paper end
    }

    @Override
    protected void E() {
        if (this.portalCooldown > 0 && !this.worldChangeInvuln) {
            --this.portalCooldown;
        }

    }

    @Override
    public void attack(Entity entity) {
        if (this.playerInteractManager.getGameMode() == EnumGamemode.SPECTATOR) {
            this.setSpectatorTarget(entity);
        } else {
            super.attack(entity);
        }

    }

    public long F() {
        return this.cj;
    }

    @Nullable
    public IChatBaseComponent getPlayerListName() {
        return listName; // CraftBukkit
    }

    @Override
    public void a(EnumHand enumhand) {
        super.a(enumhand);
        this.ey();
    }

    public boolean H() {
        return this.worldChangeInvuln;
    }

    public void I() {
        this.worldChangeInvuln = false;
    }

    public AdvancementDataPlayer getAdvancementData() {
        return this.advancementDataPlayer;
    }

    // CraftBukkit start
    public void a(WorldServer worldserver, double d0, double d1, double d2, float f, float f1) {
        this.a(worldserver, d0, d1, d2, f, f1, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    public void a(WorldServer worldserver, double d0, double d1, double d2, float f, float f1, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        // CraftBukkit end
        this.setSpectatorTarget(this);
        this.stopRiding();
        /* CraftBukkit start - replace with bukkit handling for multi-world
        if (worldserver == this.world) {
            this.playerConnection.a(d0, d1, d2, f, f1);
        } else {
            WorldServer worldserver1 = this.getWorldServer();

            this.dimension = worldserver.worldProvider.getDimensionManager();
            WorldData worlddata = worldserver.getWorldData();

            this.playerConnection.sendPacket(new PacketPlayOutRespawn(this.dimension, WorldData.c(worlddata.getSeed()), worlddata.getType(), this.playerInteractManager.getGameMode()));
            this.playerConnection.sendPacket(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
            this.server.getPlayerList().d(this);
            worldserver1.removePlayer(this);
            this.dead = false;
            this.setPositionRotation(d0, d1, d2, f, f1);
            this.spawnIn(worldserver);
            worldserver.addPlayerCommand(this);
            this.triggerDimensionAdvancements(worldserver1);
            this.playerConnection.a(d0, d1, d2, f, f1);
            this.playerInteractManager.a(worldserver);
            this.server.getPlayerList().a(this, worldserver);
            this.server.getPlayerList().updateClient(this);
        }
        */
        this.getBukkitEntity().teleport(new Location(worldserver.getWorld(), d0, d1, d2, f, f1), cause);
        // CraftBukkit end

    }

    public void a(ChunkCoordIntPair chunkcoordintpair, Packet<?> packet, Packet<?> packet1) {
        this.playerConnection.sendPacket(packet1);
        this.playerConnection.sendPacket(packet);
    }

    public void a(ChunkCoordIntPair chunkcoordintpair) {
        if (this.isAlive()) {
            this.playerConnection.sendPacket(new PacketPlayOutUnloadChunk(chunkcoordintpair.x, chunkcoordintpair.z));
        }

    }

    public SectionPosition getPlayerMapSection() { return this.K(); } // Paper - OBFHELPER
    public SectionPosition K() {
        return this.cs;
    }

    public void a(SectionPosition sectionposition) {
        this.cs = sectionposition;
    }

    @Override
    public void a(SoundEffect soundeffect, SoundCategory soundcategory, float f, float f1) {
        this.playerConnection.sendPacket(new PacketPlayOutNamedSoundEffect(soundeffect, soundcategory, this.locX(), this.locY(), this.locZ(), f, f1));
    }

    @Override
    public Packet<?> L() {
        return new PacketPlayOutNamedEntitySpawn(this);
    }

    @Override
    public EntityItem a(ItemStack itemstack, boolean flag, boolean flag1) {
        EntityItem entityitem = super.a(itemstack, flag, flag1);

        if (entityitem == null) {
            return null;
        } else {
            this.world.addEntity(entityitem);
            ItemStack itemstack1 = entityitem.getItemStack();

            if (flag1) {
                if (!itemstack1.isEmpty()) {
                    this.a(StatisticList.ITEM_DROPPED.b(itemstack1.getItem()), itemstack.getCount());
                }

                this.a(StatisticList.DROP);
            }

            return entityitem;
        }
    }

    // CraftBukkit start - Add per-player time and weather.
    public long timeOffset = 0;
    public boolean relativeTime = true;

    public long getPlayerTime() {
        if (this.relativeTime) {
            // Adds timeOffset to the current server time.
            return this.world.getDayTime() + this.timeOffset;
        } else {
            // Adds timeOffset to the beginning of this day.
            return this.world.getDayTime() - (this.world.getDayTime() % 24000) + this.timeOffset;
        }
    }

    public WeatherType weather = null;

    public WeatherType getPlayerWeather() {
        return this.weather;
    }

    public void setPlayerWeather(WeatherType type, boolean plugin) {
        if (!plugin && this.weather != null) {
            return;
        }

        if (plugin) {
            this.weather = type;
        }

        if (type == WeatherType.DOWNFALL) {
            this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(2, 0));
        } else {
            this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(1, 0));
        }
    }

    private float pluginRainPosition;
    private float pluginRainPositionPrevious;

    public void updateWeather(float oldRain, float newRain, float oldThunder, float newThunder) {
        if (this.weather == null) {
            // Vanilla
            if (oldRain != newRain) {
                this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, newRain));
            }
        } else {
            // Plugin
            if (pluginRainPositionPrevious != pluginRainPosition) {
                this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, pluginRainPosition));
            }
        }

        if (oldThunder != newThunder) {
            if (weather == WeatherType.DOWNFALL || weather == null) {
                this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(8, newThunder));
            } else {
                this.playerConnection.sendPacket(new PacketPlayOutGameStateChange(8, 0));
            }
        }
    }

    public void tickWeather() {
        if (this.weather == null) return;

        pluginRainPositionPrevious = pluginRainPosition;
        if (weather == WeatherType.DOWNFALL) {
            pluginRainPosition += 0.01;
        } else {
            pluginRainPosition -= 0.01;
        }

        pluginRainPosition = MathHelper.a(pluginRainPosition, 0.0F, 1.0F);
    }

    public void resetPlayerWeather() {
        this.weather = null;
        this.setPlayerWeather(this.world.getWorldData().hasStorm() ? WeatherType.DOWNFALL : WeatherType.CLEAR, false);
    }

    @Override
    public String toString() {
        return super.toString() + "(" + this.getName() + " at " + this.locX() + "," + this.locY() + "," + this.locZ() + ")";
    }

    // SPIGOT-1903, MC-98153
    public void forceSetPositionRotation(double x, double y, double z, float yaw, float pitch) {
        this.setPositionRotation(x, y, z, yaw, pitch);
        this.playerConnection.syncPosition();
    }

    @Override
    public boolean isFrozen() { // Paper - protected > public
        return super.isFrozen() || (this.playerConnection != null && this.playerConnection.isDisconnected()); // Paper
    }

    @Override
    public Scoreboard getScoreboard() {
        return getBukkitEntity().getScoreboard().getHandle();
    }

    public void reset() {
        float exp = 0;
        boolean keepInventory = this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY);

        if (this.keepLevel || keepInventory) {
            exp = this.exp;
            this.newTotalExp = this.expTotal;
            this.newLevel = this.expLevel;
        }

        this.setHealth(this.getMaxHealth());
        this.setAirTicks(this.getMaxAirTicks()); // Paper
        this.fireTicks = 0;
        this.fallDistance = 0;
        this.foodData = new FoodMetaData(this);
        this.expLevel = this.newLevel;
        this.expTotal = this.newTotalExp;
        this.exp = 0;
        this.deathTicks = 0;
        this.setArrowCount(0);
        this.removeAllEffects(org.bukkit.event.entity.EntityPotionEffectEvent.Cause.DEATH);
        this.updateEffects = true;
        this.activeContainer = this.defaultContainer;
        this.killer = null;
        this.lastDamager = null;
        this.combatTracker = new CombatTracker(this);
        this.lastSentExp = -1;
        if (this.keepLevel || keepInventory) {
            this.exp = exp;
        } else {
            this.giveExp(this.newExp);
        }
        this.keepLevel = false;
    }

    @Override
    public CraftPlayer getBukkitEntity() {
        return (CraftPlayer) super.getBukkitEntity();
    }
    // CraftBukkit end
}
