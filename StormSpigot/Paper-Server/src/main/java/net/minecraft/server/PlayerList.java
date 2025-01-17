package net.minecraft.server;

import co.aikar.timings.MinecraftTimings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
// CraftBukkit end
import org.stormrealms.stormspigot.PlayerConnectionOverride;

public abstract class PlayerList {

    public static final File b = new File("banned-players.json");
    public static final File c = new File("banned-ips.json");
    public static final File d = new File("ops.json");
    public static final File e = new File("whitelist.json");
    private static final Logger LOGGER = LogManager.getLogger();
    private static final SimpleDateFormat g = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
    private final MinecraftServer server;
    public final List<EntityPlayer> players = new java.util.concurrent.CopyOnWriteArrayList(); // CraftBukkit - ArrayList -> CopyOnWriteArrayList: Iterator safety
    private final Map<UUID, EntityPlayer> j = Maps.newHashMap();Map<UUID, EntityPlayer> getUUIDMap() { return j; } // Paper - OBFHELPER
    private final GameProfileBanList k;
    private final IpBanList l;
    private final OpList operators;
    private final WhiteList whitelist;
    private final Map<UUID, EntityPlayer> pendingPlayers = Maps.newHashMap(); // Paper
    // CraftBukkit start
    // private final Map<UUID, ServerStatisticManager> o;
    // private final Map<UUID, AdvancementDataPlayer> p;
    // CraftBukkit end
    public IPlayerFileData playerFileData;
    //private boolean hasWhitelist;
    protected final int maxPlayers;
    private int viewDistance;
    private EnumGamemode t;
    private boolean u;
    private int v;

    // CraftBukkit start
    private CraftServer cserver;
    private final Map<String,EntityPlayer> playersByName = new java.util.HashMap<>();
    @Nullable String collideRuleTeamName; // Paper - Team name used for collideRule

    public PlayerList(MinecraftServer minecraftserver, int i) {
        this.cserver = minecraftserver.server = new CraftServer((DedicatedServer) minecraftserver, this);
        minecraftserver.console = new com.destroystokyo.paper.console.TerminalConsoleCommandSender(); // Paper
        // CraftBukkit end

        this.k = new GameProfileBanList(PlayerList.b);
        this.l = new IpBanList(PlayerList.c);
        this.operators = new OpList(PlayerList.d);
        this.whitelist = new WhiteList(PlayerList.e);
        // CraftBukkit start
        // this.o = Maps.newHashMap();
        // this.p = Maps.newHashMap();
        // CraftBukkit end
        this.server = minecraftserver;
        this.maxPlayers = i;
        this.getProfileBans().a(true);
        this.getIPBans().a(true);
    }

    public void a(NetworkManager networkmanager, EntityPlayer entityplayer) {
        EntityPlayer prev = pendingPlayers.put(entityplayer.getUniqueID(), entityplayer);// Paper
        if (prev != null) {
            disconnectPendingPlayer(prev);
        }
        entityplayer.networkManager = networkmanager; // Paper
        entityplayer.loginTime = System.currentTimeMillis(); // Paper
        GameProfile gameprofile = entityplayer.getProfile();
        UserCache usercache = this.server.getUserCache();
        GameProfile gameprofile1 = usercache.getProfile(gameprofile.getId());
        String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();

        usercache.a(gameprofile);
        NBTTagCompound nbttagcompound = this.a(entityplayer);
        WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);
        // CraftBukkit start - Better rename detection
        if (nbttagcompound != null && nbttagcompound.hasKey("bukkit")) {
            NBTTagCompound bukkit = nbttagcompound.getCompound("bukkit");
            s = bukkit.hasKeyOfType("lastKnownName", 8) ? bukkit.getString("lastKnownName") : s;
        }String lastKnownName = s; // Paper
        if (nbttagcompound == null) entityplayer.moveToSpawn(worldserver); // Paper - only move to spawn on first login, otherwise, stay where you are....
        // CraftBukkit end

        entityplayer.spawnIn(worldserver);
        entityplayer.playerInteractManager.a((WorldServer) entityplayer.world);
        String s1 = "local";

        if (networkmanager.getSocketAddress() != null) {
            s1 = networkmanager.getSocketAddress().toString();
        }

        // Spigot start - spawn location event
        Player bukkitPlayer = entityplayer.getBukkitEntity();
        PlayerSpawnLocationEvent ev = new com.destroystokyo.paper.event.player.PlayerInitialSpawnEvent(bukkitPlayer, bukkitPlayer.getLocation()); // Paper use our duplicate event
        Bukkit.getPluginManager().callEvent(ev);

        Location loc = ev.getSpawnLocation();
        worldserver = ((CraftWorld) loc.getWorld()).getHandle();

        entityplayer.spawnIn(worldserver);
        entityplayer.setPositionRaw(loc.getX(), loc.getY(), loc.getZ()); // Paper - set raw so we aren't fully joined to the world (not added to chunk or world)
        entityplayer.setYawPitch(loc.getYaw(), loc.getPitch());
        // Spigot end

        // CraftBukkit - Moved message to after join
        // PlayerList.LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", entityplayer.getDisplayName().getString(), s1, entityplayer.getId(), entityplayer.locX(), entityplayer.locY(), entityplayer.locZ());
        WorldData worlddata = worldserver.getWorldData();

        this.a(entityplayer, (EntityPlayer) null, worldserver);
        PlayerConnection playerconnection = new PlayerConnectionOverride(this.server, networkmanager, entityplayer);
        GameRules gamerules = worldserver.getGameRules();
        boolean flag = gamerules.getBoolean(GameRules.DO_IMMEDIATE_RESPAWN);
        boolean flag1 = gamerules.getBoolean(GameRules.REDUCED_DEBUG_INFO);

        // CraftBukkit - getType()
        // Spigot - view distance
        networkmanager.queueImmunity = true; // Paper
        playerconnection.sendPacket(new PacketPlayOutLogin(entityplayer.getId(), entityplayer.playerInteractManager.getGameMode(), WorldData.c(worlddata.getSeed()), worlddata.isHardcore(), worldserver.worldProvider.getDimensionManager().getType(), this.getMaxPlayers(), worlddata.getType(), worldserver.getChunkProvider().playerChunkMap.getLoadViewDistance(), flag1, !flag)); // Paper - no-tick view distance
        entityplayer.getBukkitEntity().sendSupportedChannels(); // CraftBukkit
        playerconnection.sendPacket(new PacketPlayOutCustomPayload(PacketPlayOutCustomPayload.a, (new PacketDataSerializer(Unpooled.buffer())).a(this.getServer().getServerModName())));
        playerconnection.sendPacket(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        playerconnection.sendPacket(new PacketPlayOutAbilities(entityplayer.abilities));
        playerconnection.sendPacket(new PacketPlayOutHeldItemSlot(entityplayer.inventory.itemInHandIndex));
        playerconnection.sendPacket(new PacketPlayOutRecipeUpdate(this.server.getCraftingManager().b()));
        playerconnection.sendPacket(new PacketPlayOutTags(this.server.getTagRegistry()));
        playerconnection.sendPacket(new PacketPlayOutEntityStatus(entityplayer, (byte) (worldserver.getGameRules().getBoolean(GameRules.REDUCED_DEBUG_INFO) ? 22 : 23))); // Paper - fix this rule not being initialized on the client
        networkmanager.queueImmunity = false; // Paper
        this.d(entityplayer);
        entityplayer.getStatisticManager().c();
        entityplayer.B().a(entityplayer);
        this.sendScoreboard(worldserver.getScoreboard(), entityplayer);
        this.server.invalidatePingSample();
        // Paper start - async load spawn in chunk
        WorldServer finalWorldserver = worldserver;
        int chunkX = loc.getBlockX() >> 4;
        int chunkZ = loc.getBlockZ() >> 4;
        final ChunkCoordIntPair pos = new ChunkCoordIntPair(chunkX, chunkZ);
        PlayerChunkMap playerChunkMap = finalWorldserver.getChunkProvider().playerChunkMap;
        playerChunkMap.chunkDistanceManager.addTicketAtLevel(TicketType.LOGIN, pos, 31, pos.pair());
        worldserver.getChunkProvider().markAreaHighPriority(pos, 28, 3);
        worldserver.getChunkProvider().getChunkAtAsynchronously(chunkX, chunkZ, true, false).thenApply(chunk -> {
            PlayerChunk updatingChunk = playerChunkMap.getUpdatingChunk(pos.pair());
            if (updatingChunk != null) {
                return updatingChunk.getEntityTickingFuture();
            } else {
                return CompletableFuture.completedFuture(chunk);
            }
        }).thenAccept(chunk -> {
            playerconnection.playerJoinReady = () -> {
                postChunkLoadJoin(
                    entityplayer, finalWorldserver, networkmanager, playerconnection,
                    nbttagcompound, networkmanager.getSocketAddress().toString(), lastKnownName
                );
            };
        });
    }

    EntityPlayer getActivePlayer(UUID uuid) {
        EntityPlayer player = this.getUUIDMap().get(uuid);
        return player != null ? player : pendingPlayers.get(uuid);
    }

    void disconnectPendingPlayer(EntityPlayer entityplayer) {
        ChatMessage msg = new ChatMessage("multiplayer.disconnect.duplicate_login", new Object[0]);
        entityplayer.networkManager.sendPacket(new PacketPlayOutKickDisconnect(msg), (future) -> {
            entityplayer.networkManager.close(msg);
            entityplayer.networkManager = null;
        });
    }

    private void postChunkLoadJoin(EntityPlayer entityplayer, WorldServer worldserver, NetworkManager networkmanager, PlayerConnection playerconnection, NBTTagCompound nbttagcompound, String s1, String s) {
        pendingPlayers.remove(entityplayer.getUniqueID(), entityplayer);
        if (!networkmanager.isConnected()) {
            return;
        }
        entityplayer.didPlayerJoinEvent = true;
        // Paper end
        ChatMessage chatmessage;

        if (entityplayer.getProfile().getName().equalsIgnoreCase(s)) {
            chatmessage = new ChatMessage("multiplayer.player.joined", new Object[]{entityplayer.getScoreboardDisplayName()});
        } else {
            chatmessage = new ChatMessage("multiplayer.player.joined.renamed", new Object[]{entityplayer.getScoreboardDisplayName(), s});
        }
        // CraftBukkit start
        chatmessage.a(EnumChatFormat.YELLOW);
        String joinMessage = CraftChatMessage.fromComponent(chatmessage);

        playerconnection.a(entityplayer.locX(), entityplayer.locY(), entityplayer.locZ(), entityplayer.yaw, entityplayer.pitch);
        this.players.add(entityplayer);
        this.playersByName.put(entityplayer.getName().toLowerCase(java.util.Locale.ROOT), entityplayer); // Spigot
        this.j.put(entityplayer.getUniqueID(), entityplayer);
        // this.sendAll(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new EntityPlayer[]{entityplayer})); // CraftBukkit - replaced with loop below

        // Paper start - correctly register player BEFORE PlayerJoinEvent, so the entity is valid and doesn't require tick delay hacks
        entityplayer.supressTrackerForLogin = true;
        worldserver.addPlayerJoin(entityplayer);
        this.server.getBossBattleCustomData().a(entityplayer); // see commented out section below worldserver.addPlayerJoin(entityplayer);
        mountSavedVehicle(entityplayer, worldserver, nbttagcompound);
        // Paper end
        // CraftBukkit start
        PlayerJoinEvent playerJoinEvent = new PlayerJoinEvent(cserver.getPlayer(entityplayer), joinMessage);
        cserver.getPluginManager().callEvent(playerJoinEvent);

        if (!entityplayer.playerConnection.networkManager.isConnected()) {
            return;
        }

        joinMessage = playerJoinEvent.getJoinMessage();

        if (joinMessage != null && joinMessage.length() > 0) {
            // Paper start - Removed sendAll for loop and broadcasted to console also
            server.getPlayerList().sendMessage(CraftChatMessage.fromString(joinMessage));
            // Paper end
        }
        // CraftBukkit end

        // CraftBukkit start - sendAll above replaced with this loop
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, entityplayer);

        for (int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer1 = (EntityPlayer) this.players.get(i);

            if (entityplayer1.getBukkitEntity().canSee(entityplayer.getBukkitEntity())) {
                entityplayer1.playerConnection.sendPacket(packet);
            }

            if (!entityplayer.getBukkitEntity().canSee(entityplayer1.getBukkitEntity())) {
                continue;
            }

            entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, new EntityPlayer[] { entityplayer1}));
        }
        entityplayer.sentListPacket = true;
        entityplayer.supressTrackerForLogin = false; // Paper
        // CraftBukkit end

        entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityplayer.getId(), entityplayer.datawatcher, true)); // CraftBukkit - BungeeCord#2321, send complete data to self on spawn

        // CraftBukkit start - Only add if the player wasn't moved in the event
        if (entityplayer.world == worldserver && !worldserver.getPlayers().contains(entityplayer)) {
            worldserver.addPlayerJoin(entityplayer);
            this.server.getBossBattleCustomData().a(entityplayer);
        }

        worldserver = server.getWorldServer(entityplayer.dimension);  // CraftBukkit - Update in case join event changed it
        // CraftBukkit end
        this.a(entityplayer, worldserver);
        if (!this.server.getResourcePack().isEmpty()) {
            entityplayer.setResourcePack(this.server.getResourcePack(), this.server.getResourcePackHash());
        }

        Iterator iterator = entityplayer.getEffects().iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            playerconnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer.getId(), mobeffect));
        }

        // Paper start - move vehicle into method so it can be called above - short circuit around that code
        ((WorldServer)entityplayer.world).getChunkProvider().playerChunkMap.addEntity(entityplayer); // track entity now
        onPlayerJoinFinish(entityplayer, worldserver, s1);
    }
    private void mountSavedVehicle(EntityPlayer entityplayer, WorldServer worldserver, NBTTagCompound nbttagcompound) {
        // Paper end
        if (nbttagcompound != null && nbttagcompound.hasKeyOfType("RootVehicle", 10)) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("RootVehicle");
            // CraftBukkit start
            WorldServer finalWorldServer = worldserver;
            Entity entity = EntityTypes.a(nbttagcompound1.getCompound("Entity"), finalWorldServer, (entity1) -> {
                return !finalWorldServer.addEntitySerialized(entity1, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.MOUNT) ? null : entity1; // Paper
                // CraftBukkit end
            });

            if (entity != null) {
                UUID uuid = nbttagcompound1.a("Attach");
                Iterator iterator1;
                Entity entity1;

                if (entity.getUniqueID().equals(uuid)) {
                    entityplayer.a(entity, true);
                } else {
                    iterator1 = entity.getAllPassengers().iterator();

                    while (iterator1.hasNext()) {
                        entity1 = (Entity) iterator1.next();
                        if (entity1.getUniqueID().equals(uuid)) {
                            entityplayer.a(entity1, true);
                            break;
                        }
                    }
                }

                if (!entityplayer.isPassenger()) {
                    PlayerList.LOGGER.warn("Couldn't reattach entity to player");
                    worldserver.removeEntity(entity);
                    iterator1 = entity.getAllPassengers().iterator();

                    while (iterator1.hasNext()) {
                        entity1 = (Entity) iterator1.next();
                        worldserver.removeEntity(entity1);
                    }
                }
            }
        }

        // Paper start
    }
    public void onPlayerJoinFinish(EntityPlayer entityplayer, WorldServer worldserver, String s1) {
        // Paper end
        entityplayer.syncInventory();
        // Paper start - Add to collideRule team if needed
        final Scoreboard scoreboard = this.getServer().getWorldServer(DimensionManager.OVERWORLD).getScoreboard();
        final ScoreboardTeam collideRuleTeam = scoreboard.getTeam(collideRuleTeamName);
        if (this.collideRuleTeamName != null && collideRuleTeam != null && entityplayer.getScoreboardTeam() == null) {
            scoreboard.addPlayerToTeam(entityplayer.getName(), collideRuleTeam);
        }
        // Paper end
        // CraftBukkit - Moved from above, added world
        PlayerList.LOGGER.info("{}[{}] logged in with entity id {} at ([{}]{}, {}, {})", entityplayer.getDisplayName().getString(), s1, entityplayer.getId(), entityplayer.world.worldData.getName(), entityplayer.locX(), entityplayer.locY(), entityplayer.locZ());
    }

    public void sendScoreboard(ScoreboardServer scoreboardserver, EntityPlayer entityplayer) {
        Set<ScoreboardObjective> set = Sets.newHashSet();
        Iterator iterator = scoreboardserver.getTeams().iterator();

        while (iterator.hasNext()) {
            ScoreboardTeam scoreboardteam = (ScoreboardTeam) iterator.next();

            entityplayer.playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(scoreboardteam, 0));
        }

        for (int i = 0; i < 19; ++i) {
            ScoreboardObjective scoreboardobjective = scoreboardserver.getObjectiveForSlot(i);

            if (scoreboardobjective != null && !set.contains(scoreboardobjective)) {
                List<Packet<?>> list = scoreboardserver.getScoreboardScorePacketsForObjective(scoreboardobjective);
                Iterator iterator1 = list.iterator();

                while (iterator1.hasNext()) {
                    Packet<?> packet = (Packet) iterator1.next();

                    entityplayer.playerConnection.sendPacket(packet);
                }

                set.add(scoreboardobjective);
            }
        }

    }

    public void setPlayerFileData(WorldServer worldserver) {
        if (playerFileData != null) return; // CraftBukkit
        this.playerFileData = worldserver.getDataManager();
        worldserver.getWorldBorder().a(new IWorldBorderListener() {
            @Override
            public void a(WorldBorder worldborder, double d0) {
                PlayerList.this.sendAll(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE), worldborder.world);
            }

            @Override
            public void a(WorldBorder worldborder, double d0, double d1, long i) {
                PlayerList.this.sendAll(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.LERP_SIZE), worldborder.world);
            }

            @Override
            public void a(WorldBorder worldborder, double d0, double d1) {
                PlayerList.this.sendAll(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER), worldborder.world);
            }

            @Override
            public void a(WorldBorder worldborder, int i) {
                PlayerList.this.sendAll(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_TIME), worldborder.world);
            }

            @Override
            public void b(WorldBorder worldborder, int i) {
                PlayerList.this.sendAll(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS), worldborder.world);
            }

            @Override
            public void b(WorldBorder worldborder, double d0) {}

            @Override
            public void c(WorldBorder worldborder, double d0) {}
        });
    }

    @Nullable
    public NBTTagCompound a(EntityPlayer entityplayer) {
        NBTTagCompound nbttagcompound = this.server.getWorldServer(DimensionManager.OVERWORLD).getWorldData().h();
        NBTTagCompound nbttagcompound1;

        if (entityplayer.getDisplayName().getString().equals(this.server.getSinglePlayerName()) && nbttagcompound != null) {
            nbttagcompound1 = nbttagcompound;
            entityplayer.f(nbttagcompound);
            PlayerList.LOGGER.debug("loading single player");
        } else {
            nbttagcompound1 = this.playerFileData.load(entityplayer);
        }

        return nbttagcompound1;
    }

    protected void savePlayerFile(EntityPlayer entityplayer) {
        if (!entityplayer.getBukkitEntity().isPersistent()) return; // CraftBukkit
        if (!entityplayer.didPlayerJoinEvent) return; // Paper - If we never fired PJE, we disconnected during login. Data has not changed, and additionally, our saved vehicle is not loaded! If we save now, we will lose our vehicle (CraftBukkit bug)
        this.playerFileData.save(entityplayer);
        ServerStatisticManager serverstatisticmanager = (ServerStatisticManager) entityplayer.getStatisticManager(); // CraftBukkit

        if (serverstatisticmanager != null) {
            serverstatisticmanager.a();
        }

        AdvancementDataPlayer advancementdataplayer = (AdvancementDataPlayer) entityplayer.getAdvancementData(); // CraftBukkit

        if (advancementdataplayer != null) {
            advancementdataplayer.c();
        }

    }

    public String disconnect(EntityPlayer entityplayer) { // CraftBukkit - return string
        WorldServer worldserver = entityplayer.getWorldServer();

        entityplayer.a(StatisticList.LEAVE_GAME);

        // CraftBukkit start - Quitting must be before we do final save of data, in case plugins need to modify it
        entityplayer.closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason.DISCONNECT); // Paper

        PlayerQuitEvent playerQuitEvent = new PlayerQuitEvent(cserver.getPlayer(entityplayer), "\u00A7e" + entityplayer.getName() + " left the game");
        if (entityplayer.didPlayerJoinEvent) cserver.getPluginManager().callEvent(playerQuitEvent); // Paper - if we disconnected before join ever fired, don't fire quit
        entityplayer.getBukkitEntity().disconnect(playerQuitEvent.getQuitMessage());

        if (server.isMainThread()) entityplayer.playerTick(); // SPIGOT-924 // Paper - don't tick during emergency shutdowns (Watchdog)
        // CraftBukkit end

        // Paper start - Remove from collideRule team if needed
        if (this.collideRuleTeamName != null) {
            final Scoreboard scoreBoard = this.server.getWorldServer(DimensionManager.OVERWORLD).getScoreboard();
            final ScoreboardTeam team = scoreBoard.getTeam(this.collideRuleTeamName);
            if (entityplayer.getScoreboardTeam() == team && team != null) {
                scoreBoard.removePlayerFromTeam(entityplayer.getName(), team);
            }
        }
        // Paper end

        this.savePlayerFile(entityplayer);
        if (entityplayer.isPassenger()) {
            Entity entity = entityplayer.getRootVehicle();

            if (entity.hasSinglePlayerPassenger()) {
                PlayerList.LOGGER.debug("Removing player mount");
                entityplayer.stopRiding();
                worldserver.removeEntity(entity);
                Iterator iterator = entity.getAllPassengers().iterator();

                while (iterator.hasNext()) {
                    Entity entity1 = (Entity) iterator.next();

                    worldserver.removeEntity(entity1);
                }

                worldserver.getChunkAt(entityplayer.chunkX, entityplayer.chunkZ).markDirty();
            }
        }

        entityplayer.decouple();
        worldserver.removePlayer(entityplayer);
        entityplayer.getAdvancementData().a();
        this.players.remove(entityplayer);
        this.playersByName.remove(entityplayer.getName().toLowerCase(java.util.Locale.ROOT)); // Spigot
        this.server.getBossBattleCustomData().b(entityplayer);
        UUID uuid = entityplayer.getUniqueID();
        EntityPlayer entityplayer1 = (EntityPlayer) this.j.get(uuid);

        if (entityplayer1 == entityplayer) {
            this.j.remove(uuid);
            // CraftBukkit start
            // this.o.remove(uuid);
            // this.p.remove(uuid);
            // CraftBukkit end
        }
        // Paper start
        entityplayer1 = pendingPlayers.get(uuid);
        if (entityplayer1 == entityplayer) {
            pendingPlayers.remove(uuid);
        }
        entityplayer.networkManager = null;
        // Paper end

        // CraftBukkit start
        //  this.sendAll(new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, new EntityPlayer[]{entityplayer}));
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, entityplayer);
        for (int i = 0; i < players.size(); i++) {
            EntityPlayer entityplayer2 = (EntityPlayer) this.players.get(i);

            if (entityplayer2.getBukkitEntity().canSee(entityplayer.getBukkitEntity())) {
                entityplayer2.playerConnection.sendPacket(packet);
            } else {
                entityplayer2.getBukkitEntity().removeDisconnectingPlayer(entityplayer.getBukkitEntity());
            }
        }
        // This removes the scoreboard (and player reference) for the specific player in the manager
        cserver.getScoreboardManager().removePlayer(entityplayer.getBukkitEntity());
        // CraftBukkit end

        return entityplayer.didPlayerJoinEvent ? playerQuitEvent.getQuitMessage() : null; // CraftBukkit // Paper - don't print quit if we never printed join
    }

    // CraftBukkit start - Whole method, SocketAddress to LoginListener, added hostname to signature, return EntityPlayer
    public EntityPlayer attemptLogin(LoginListener loginlistener, GameProfile gameprofile, String hostname) {
        ChatMessage chatmessage;

        // Moved from processLogin
        UUID uuid = EntityHuman.a(gameprofile);
        List<EntityPlayer> list = Lists.newArrayList();

        EntityPlayer entityplayer;

        for (int i = 0; i < this.players.size(); ++i) {
            entityplayer = (EntityPlayer) this.players.get(i);
            if (entityplayer.getUniqueID().equals(uuid)) {
                list.add(entityplayer);
            }
        }
        // Paper start - check pending players too
        entityplayer = pendingPlayers.get(uuid);
        if (entityplayer != null) {
            this.pendingPlayers.remove(uuid);
            disconnectPendingPlayer(entityplayer);
        }
        // Paper end

        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            entityplayer = (EntityPlayer) iterator.next();
            savePlayerFile(entityplayer); // CraftBukkit - Force the player's inventory to be saved
            entityplayer.playerConnection.disconnect(new ChatMessage("multiplayer.disconnect.duplicate_login", new Object[0]));
        }

        // Instead of kicking then returning, we need to store the kick reason
        // in the event, check with plugins to see if it's ok, and THEN kick
        // depending on the outcome.
        SocketAddress socketaddress = loginlistener.networkManager.getSocketAddress();

        EntityPlayer entity = new EntityPlayer(this.server, this.server.getWorldServer(DimensionManager.OVERWORLD), gameprofile, new PlayerInteractManager(this.server.getWorldServer(DimensionManager.OVERWORLD)));
        entity.isRealPlayer = true; // Paper
        Player player = entity.getBukkitEntity();
        PlayerLoginEvent event = new PlayerLoginEvent(player, hostname, ((java.net.InetSocketAddress) socketaddress).getAddress(), ((java.net.InetSocketAddress) loginlistener.networkManager.getRawAddress()).getAddress());

        // Paper start - Fix MC-158900
        GameProfileBanEntry gameprofilebanentry;
        if (getProfileBans().isBanned(gameprofile) && (gameprofilebanentry = getProfileBans().get(gameprofile)) != null) {
            // Paper end

            chatmessage = new ChatMessage("multiplayer.disconnect.banned.reason", new Object[]{gameprofilebanentry.getReason()});
            if (gameprofilebanentry.getExpires() != null) {
                chatmessage.addSibling(new ChatMessage("multiplayer.disconnect.banned.expiration", new Object[]{PlayerList.g.format(gameprofilebanentry.getExpires())}));
            }

            // return chatmessage;
            if (!gameprofilebanentry.hasExpired()) event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(chatmessage)); // Spigot
        } else if (!this.isWhitelisted(gameprofile, event)) { // Paper
            chatmessage = new ChatMessage("multiplayer.disconnect.not_whitelisted", new Object[0]);
            //event.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, org.spigotmc.SpigotConfig.whitelistMessage); // Spigot // Paper - moved to isWhitelisted
        } else if (getIPBans().isBanned(socketaddress) && getIPBans().get(socketaddress) != null && !getIPBans().get(socketaddress).hasExpired()) { // Paper - fix NPE with temp ip bans
            IpBanEntry ipbanentry = this.l.get(socketaddress);

            chatmessage = new ChatMessage("multiplayer.disconnect.banned_ip.reason", new Object[]{ipbanentry.getReason()});
            if (ipbanentry.getExpires() != null) {
                chatmessage.addSibling(new ChatMessage("multiplayer.disconnect.banned_ip.expiration", new Object[]{PlayerList.g.format(ipbanentry.getExpires())}));
            }

            // return chatmessage;
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, CraftChatMessage.fromComponent(chatmessage));
        } else {
            // return this.players.size() >= this.maxPlayers && !this.f(gameprofile) ? new ChatMessage("multiplayer.disconnect.server_full", new Object[0]) : null;
            if (this.players.size() >= this.maxPlayers && !this.f(gameprofile)) {
                event.disallow(PlayerLoginEvent.Result.KICK_FULL, org.spigotmc.SpigotConfig.serverFullMessage); // Spigot
            }
        }

        cserver.getPluginManager().callEvent(event);
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            loginlistener.disconnect(event.getKickMessage());
            return null;
        }
        return entity;
    }

    public EntityPlayer processLogin(GameProfile gameprofile, EntityPlayer player) { // CraftBukkit - added EntityPlayer
        /* CraftBukkit startMoved up
        UUID uuid = EntityHuman.a(gameprofile);
        List<EntityPlayer> list = Lists.newArrayList();

        for (int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer) this.players.get(i);

            if (entityplayer.getUniqueID().equals(uuid)) {
                list.add(entityplayer);
            }
        }

        EntityPlayer entityplayer1 = (EntityPlayer) this.j.get(gameprofile.getId());

        if (entityplayer1 != null && !list.contains(entityplayer1)) {
            list.add(entityplayer1);
        }

        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer2 = (EntityPlayer) iterator.next();

            entityplayer2.playerConnection.disconnect(new ChatMessage("multiplayer.disconnect.duplicate_login", new Object[0]));
        }

        Object object;

        if (this.server.isDemoMode()) {
            object = new DemoPlayerInteractManager(this.server.getWorldServer(DimensionManager.OVERWORLD));
        } else {
            object = new PlayerInteractManager(this.server.getWorldServer(DimensionManager.OVERWORLD));
        }

        return new EntityPlayer(this.server, this.server.getWorldServer(DimensionManager.OVERWORLD), gameprofile, (PlayerInteractManager) object);
        */
        return player;
        // CraftBukkit end
    }

    // CraftBukkit start
    public EntityPlayer moveToWorld(EntityPlayer entityplayer, DimensionManager dimensionmanager, boolean flag) {
        return this.moveToWorld(entityplayer, dimensionmanager, flag, null, true);
    }

    public EntityPlayer moveToWorld(EntityPlayer entityplayer, DimensionManager dimensionmanager, boolean flag, Location location, boolean avoidSuffocation) {
        entityplayer.stopRiding(); // CraftBukkit
        this.players.remove(entityplayer);
        this.playersByName.remove(entityplayer.getName().toLowerCase(java.util.Locale.ROOT)); // Spigot
        entityplayer.getWorldServer().removePlayer(entityplayer);
        BlockPosition blockposition = entityplayer.getBed();
        boolean flag1 = entityplayer.isRespawnForced();

        /* CraftBukkit start
        entityplayer.dimension = dimensionmanager;
        Object object;

        if (this.server.isDemoMode()) {
            object = new DemoPlayerInteractManager(this.server.getWorldServer(entityplayer.dimension));
        } else {
            object = new PlayerInteractManager(this.server.getWorldServer(entityplayer.dimension));
        }

        EntityPlayer entityplayer1 = new EntityPlayer(this.server, this.server.getWorldServer(entityplayer.dimension), entityplayer.getProfile(), (PlayerInteractManager) object);
        // */
        EntityPlayer entityplayer1 = entityplayer;
        org.bukkit.World fromWorld = entityplayer.getBukkitEntity().getWorld();
        entityplayer.viewingCredits = false;
        // CraftBukkit end

        entityplayer1.playerConnection = entityplayer.playerConnection;
        entityplayer1.copyFrom(entityplayer, flag);
        entityplayer1.e(entityplayer.getId());
        entityplayer1.a(entityplayer.getMainHand());
        Iterator iterator = entityplayer.getScoreboardTags().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            entityplayer1.addScoreboardTag(s);
        }

        // WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);  // CraftBukkit - handled later

        // this.a(entityplayer1, entityplayer, worldserver); // CraftBukkit - removed

        // Paper start
        boolean isBedSpawn = false;
        boolean isRespawn = false;
        // Paper end

        // CraftBukkit start - fire PlayerRespawnEvent
        if (location == null) {
            //boolean isBedSpawn = false; Paper - moved up
            CraftWorld cworld = (CraftWorld) this.server.server.getWorld(entityplayer.spawnWorld);
            if (cworld != null && blockposition != null) {
            Optional<Vec3D> optional = EntityHuman.getBed(cworld.getHandle(), blockposition, flag1);

                if (optional.isPresent()) {
                    Vec3D vec3d = (Vec3D) optional.get();

                    isBedSpawn = true;
                    location = new Location(cworld, vec3d.x, vec3d.y, vec3d.z);
                } else {
                    entityplayer1.setRespawnPosition(null, true, false);
                    entityplayer1.playerConnection.sendPacket(new PacketPlayOutGameStateChange(0, 0.0F));
                }
            }

            if (location == null) {
                cworld = (CraftWorld) this.server.server.getWorlds().get(0);
                blockposition = entityplayer1.getSpawnPoint(cworld.getHandle());
                location = new Location(cworld, (double) ((float) blockposition.getX() + 0.5F), (double) ((float) blockposition.getY() + 0.1F), (double) ((float) blockposition.getZ() + 0.5F));
            }

            Player respawnPlayer = cserver.getPlayer(entityplayer1);
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn);
            cserver.getPluginManager().callEvent(respawnEvent);
            // Spigot Start
            if (entityplayer.playerConnection.isDisconnected()) {
                return entityplayer;
            }
            // Spigot End

            location = respawnEvent.getRespawnLocation();
            if (!flag) entityplayer.reset(); // SPIGOT-4785
            isRespawn = true; // Paper
        } else {
            location.setWorld(server.getWorldServer(dimensionmanager).getWorld());
        }
        WorldServer worldserver = ((CraftWorld) location.getWorld()).getHandle();
        entityplayer1.forceSetPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        // CraftBukkit end

        worldserver.getChunkProvider().addTicket(TicketType.POST_TELEPORT, new ChunkCoordIntPair(location.getBlockX() >> 4, location.getBlockZ() >> 4), 1, entityplayer.getId()); // Paper
        entityplayer1.forceCheckHighPriority(); // Player
        while (avoidSuffocation && !worldserver.getCubes(entityplayer1) && entityplayer1.locY() < 256.0D) {
            entityplayer1.setPosition(entityplayer1.locX(), entityplayer1.locY() + 1.0D, entityplayer1.locZ());
        }
        // CraftBukkit start
        // Force the client to refresh their chunk cache
        if (fromWorld.getEnvironment() == worldserver.getWorld().getEnvironment()) {
            entityplayer1.playerConnection.sendPacket(new PacketPlayOutRespawn(worldserver.worldProvider.getDimensionManager().getDimensionID() >= 0 ? DimensionManager.NETHER : DimensionManager.OVERWORLD, WorldData.c(worldserver.getWorldData().getSeed()), worldserver.getWorldData().getType(), entityplayer.playerInteractManager.getGameMode()));
        }

        WorldData worlddata = worldserver.getWorldData();

        entityplayer1.playerConnection.sendPacket(new PacketPlayOutRespawn(worldserver.worldProvider.getDimensionManager().getType(),  WorldData.c(worldserver.getWorldData().getSeed()), worldserver.getWorldData().getType(), entityplayer1.playerInteractManager.getGameMode()));
        entityplayer1.playerConnection.sendPacket(new PacketPlayOutViewDistance(worldserver.getChunkProvider().playerChunkMap.getLoadViewDistance())); // Paper - no-tick view distance
        entityplayer1.spawnIn(worldserver);
        entityplayer1.dead = false;
        entityplayer1.playerConnection.teleport(new Location(worldserver.getWorld(), entityplayer1.locX(), entityplayer1.locY(), entityplayer1.locZ(), entityplayer1.yaw, entityplayer1.pitch));
        entityplayer1.setSneaking(false);
        BlockPosition blockposition1 = worldserver.getSpawn();

        // entityplayer1.playerConnection.a(entityplayer1.locX(), entityplayer1.locY(), entityplayer1.locZ(), entityplayer1.yaw, entityplayer1.pitch);
        entityplayer1.playerConnection.sendPacket(new PacketPlayOutSpawnPosition(blockposition1));
        entityplayer1.playerConnection.sendPacket(new PacketPlayOutServerDifficulty(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        entityplayer1.playerConnection.sendPacket(new PacketPlayOutExperience(entityplayer1.exp, entityplayer1.expTotal, entityplayer1.expLevel));
        this.a(entityplayer1, worldserver);
        this.d(entityplayer1);
        if (!entityplayer.playerConnection.isDisconnected()) {
            worldserver.addPlayerRespawn(entityplayer1);
            this.players.add(entityplayer1);
            this.playersByName.put(entityplayer1.getName().toLowerCase(java.util.Locale.ROOT), entityplayer1); // Spigot
            this.j.put(entityplayer1.getUniqueID(), entityplayer1);
        }
        // entityplayer1.syncInventory();
        entityplayer1.setHealth(entityplayer1.getHealth());
        // Added from changeDimension
        updateClient(entityplayer); // Update health, etc...
        entityplayer.updateAbilities();
        for (Object o1 : entityplayer.getEffects()) {
            MobEffect mobEffect = (MobEffect) o1;
            entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer.getId(), mobEffect));
        }

        entityplayer.setSneaking(false); // Paper - fix MC-10657

        // Fire advancement trigger
        entityplayer.triggerDimensionAdvancements(((CraftWorld) fromWorld).getHandle());

        // Don't fire on respawn
        if (fromWorld != location.getWorld()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent(entityplayer.getBukkitEntity(), fromWorld);
            server.server.getPluginManager().callEvent(event);
        }

        // Save player file again if they were disconnected
        if (entityplayer.playerConnection.isDisconnected()) {
            this.savePlayerFile(entityplayer);
        }

        // Paper start
        if (isRespawn) {
            cserver.getPluginManager().callEvent(new com.destroystokyo.paper.event.player.PlayerPostRespawnEvent(entityplayer.getBukkitEntity(), location, isBedSpawn));
        }
        // Paper end

        // CraftBukkit end
        return entityplayer1;
    }

    public void d(EntityPlayer entityplayer) {
        GameProfile gameprofile = entityplayer.getProfile();
        int i = this.server.b(gameprofile);

        this.a(entityplayer, i);
    }

    public void tick() {
        if (++this.v > 600) {
            // CraftBukkit start
            for (int i = 0; i < this.players.size(); ++i) {
                final EntityPlayer target = (EntityPlayer) this.players.get(i);

                target.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_LATENCY, Iterables.filter(this.players, new Predicate<EntityPlayer>() {
                    @Override
                    public boolean apply(EntityPlayer input) {
                        return target.getBukkitEntity().canSee(input.getBukkitEntity());
                    }
                })));
            }
            // CraftBukkit end
            this.v = 0;
        }

    }

    public void sendAll(Packet<?> packet) {
        for (int i = 0; i < this.players.size(); ++i) {
            ((EntityPlayer) this.players.get(i)).playerConnection.sendPacket(packet);
        }

    }

    // CraftBukkit start - add a world/entity limited version
    public void sendAll(Packet packet, EntityHuman entityhuman) {
        for (int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer =  this.players.get(i);
            if (entityhuman != null && entityhuman instanceof EntityPlayer && !entityplayer.getBukkitEntity().canSee(((EntityPlayer) entityhuman).getBukkitEntity())) {
                continue;
            }
            ((EntityPlayer) this.players.get(i)).playerConnection.sendPacket(packet);
        }
    }

    public void sendAll(Packet packet, World world) {
        for (int i = 0; i < world.getPlayers().size(); ++i) {
            ((EntityPlayer) world.getPlayers().get(i)).playerConnection.sendPacket(packet);
        }

    }
    // CraftBukkit end

    public void a(Packet<?> packet, DimensionManager dimensionmanager) {
        for (int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer = (EntityPlayer) this.players.get(i);

            if (entityplayer.dimension == dimensionmanager) {
                entityplayer.playerConnection.sendPacket(packet);
            }
        }

    }

    public void a(EntityHuman entityhuman, IChatBaseComponent ichatbasecomponent) {
        ScoreboardTeamBase scoreboardteambase = entityhuman.getScoreboardTeam();

        if (scoreboardteambase != null) {
            Collection<String> collection = scoreboardteambase.getPlayerNameSet();
            Iterator iterator = collection.iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();
                EntityPlayer entityplayer = this.getPlayer(s);

                if (entityplayer != null && entityplayer != entityhuman) {
                    entityplayer.sendMessage(ichatbasecomponent);
                }
            }

        }
    }

    public void b(EntityHuman entityhuman, IChatBaseComponent ichatbasecomponent) {
        ScoreboardTeamBase scoreboardteambase = entityhuman.getScoreboardTeam();

        if (scoreboardteambase == null) {
            this.sendMessage(ichatbasecomponent);
        } else {
            for (int i = 0; i < this.players.size(); ++i) {
                EntityPlayer entityplayer = (EntityPlayer) this.players.get(i);

                if (entityplayer.getScoreboardTeam() != scoreboardteambase) {
                    entityplayer.sendMessage(ichatbasecomponent);
                }
            }

        }
    }

    public String[] e() {
        String[] astring = new String[this.players.size()];

        for (int i = 0; i < this.players.size(); ++i) {
            astring[i] = ((EntityPlayer) this.players.get(i)).getProfile().getName();
        }

        return astring;
    }

    public GameProfileBanList getProfileBans() {
        return this.k;
    }

    public IpBanList getIPBans() {
        return this.l;
    }

    public void addOp(GameProfile gameprofile) {
        this.operators.add(new OpListEntry(gameprofile, this.server.j(), this.operators.b(gameprofile)));
        EntityPlayer entityplayer = this.a(gameprofile.getId());

        if (entityplayer != null) {
            this.d(entityplayer);
        }

    }

    public void removeOp(GameProfile gameprofile) {
        this.operators.remove(gameprofile);
        EntityPlayer entityplayer = this.a(gameprofile.getId());

        if (entityplayer != null) {
            this.d(entityplayer);
        }

    }

    private void a(EntityPlayer entityplayer, int i) {
        if (entityplayer.playerConnection != null) {
            byte b0;

            if (i <= 0) {
                b0 = 24;
            } else if (i >= 4) {
                b0 = 28;
            } else {
                b0 = (byte) (24 + i);
            }

            entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityStatus(entityplayer, b0));
        }

        entityplayer.getBukkitEntity().recalculatePermissions(); // CraftBukkit
        this.server.getCommandDispatcher().a(entityplayer);
    }

    // Paper start
    public boolean isWhitelisted(GameProfile gameprofile) {
        return isWhitelisted(gameprofile, null);
    }
    public boolean isWhitelisted(GameProfile gameprofile, org.bukkit.event.player.PlayerLoginEvent loginEvent) {
        boolean isOp = this.operators.d(gameprofile);
        boolean isWhitelisted = !this.getHasWhitelist() || isOp || this.whitelist.d(gameprofile);
        final com.destroystokyo.paper.event.profile.ProfileWhitelistVerifyEvent event;
        event = new com.destroystokyo.paper.event.profile.ProfileWhitelistVerifyEvent(MCUtil.toBukkit(gameprofile), this.getHasWhitelist(), isWhitelisted, isOp, org.spigotmc.SpigotConfig.whitelistMessage);
        event.callEvent();
        if (!event.isWhitelisted()) {
            if (loginEvent != null) {
                loginEvent.disallow(PlayerLoginEvent.Result.KICK_WHITELIST, event.getKickMessage() == null ? org.spigotmc.SpigotConfig.whitelistMessage : event.getKickMessage());
            }
            return false;
        }
        return true;
    }
    // Paper end

    public boolean isOp(GameProfile gameprofile) {
        return this.operators.d(gameprofile) || this.server.a(gameprofile) && this.server.getWorldServer(DimensionManager.OVERWORLD).getWorldData().t() || this.u;
    }

    @Nullable
    public EntityPlayer getPlayer(String s) {
        return this.playersByName.get(s.toLowerCase(java.util.Locale.ROOT)); // Spigot
    }

    public void sendPacketNearby(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, double d3, DimensionManager dimensionmanager, Packet<?> packet) {
        // Paper start - Use world list instead of server list where preferable
        sendPacketNearby(entityhuman, d0, d1, d2, d3, dimensionmanager, null, packet); // Retained for compatibility
    }

    public void sendPacketNearby(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, double d3, WorldServer world, Packet<?> packet) {
        sendPacketNearby(entityhuman, d0, d1, d2, d3, world.worldProvider.getDimensionManager(), world, packet);
    }

    public void sendPacketNearby(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, double d3, DimensionManager dimensionmanager, @Nullable WorldServer world, Packet<?> packet) {
        if (world == null && entityhuman != null && entityhuman.world instanceof WorldServer) {
            world = (WorldServer) entityhuman.world;
        }

        // Paper start
        if ((world == null || world.chunkProvider == null) && dimensionmanager != null) {
            world = dimensionmanager.world;
        }
        if (world == null) {
            LOGGER.error("Sending packet to invalid world" + entityhuman + " " + dimensionmanager + " - " + packet.getClass().getName(), new Throwable());
            return; // ??? shouldn't happen...
        }
        PlayerChunkMap chunkMap = world.chunkMap;
        Object[] backingSet;
        if (chunkMap == null) {
            // Really shouldn't happen...
            backingSet = world.players.toArray();
        } else {
            com.destroystokyo.paper.util.misc.PooledLinkedHashSets.PooledObjectLinkedOpenHashSet<EntityPlayer> nearbyPlayers = chunkMap.playerViewDistanceBroadcastMap.getObjectsInRange(MCUtil.fastFloor(d0) >> 4, MCUtil.fastFloor(d2) >> 4);
            if (nearbyPlayers == null) {
                return;
            }
            backingSet = nearbyPlayers.getBackingSet();
        }

        for (Object object : backingSet) {
            if (!(object instanceof EntityPlayer)) continue;
            EntityPlayer entityplayer = (EntityPlayer) object;
            // Paper end

            // CraftBukkit start - Test if player receiving packet can see the source of the packet
            if (entityhuman != null && entityhuman instanceof EntityPlayer && !entityplayer.getBukkitEntity().canSee(((EntityPlayer) entityhuman).getBukkitEntity())) {
               continue;
            }
            // CraftBukkit end

            if (entityplayer != entityhuman && (world != null || entityplayer.dimension == dimensionmanager)) { // Paper
                double d4 = d0 - entityplayer.locX();
                double d5 = d1 - entityplayer.locY();
                double d6 = d2 - entityplayer.locZ();

                if (d4 * d4 + d5 * d5 + d6 * d6 < d3 * d3) {
                    entityplayer.playerConnection.sendPacket(packet);
                }
            }
        }

    }

    public void savePlayers() {
        MCUtil.ensureMain("Save Players" , () -> { // Paper - Ensure main
        MinecraftTimings.savePlayers.startTiming(); // Paper
        for (int i = 0; i < this.players.size(); ++i) {
            this.savePlayerFile((EntityPlayer) this.players.get(i));
        }
        MinecraftTimings.savePlayers.stopTiming(); // Paper
        return null; }); // Paper - ensure main
    }

    public WhiteList getWhitelist() {
        return this.whitelist;
    }

    public String[] getWhitelisted() {
        return this.whitelist.getEntries();
    }

    public OpList getOPs() {
        return this.operators;
    }

    public String[] m() {
        return this.operators.getEntries();
    }

    public void reloadWhitelist() {}

    public void a(EntityPlayer entityplayer, WorldServer worldserver) {
        WorldBorder worldborder = entityplayer.world.getWorldBorder(); // CraftBukkit

        entityplayer.playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldborder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
        entityplayer.playerConnection.sendPacket(new PacketPlayOutUpdateTime(worldserver.getTime(), worldserver.getDayTime(), worldserver.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)));
        BlockPosition blockposition = worldserver.getSpawn();

        entityplayer.playerConnection.sendPacket(new PacketPlayOutSpawnPosition(blockposition));
        if (worldserver.isRaining()) {
            // CraftBukkit start - handle player weather
            // entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(1, 0.0F));
            // entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, worldserver.d(1.0F)));
            // entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(8, worldserver.b(1.0F)));
            entityplayer.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL, false);
            entityplayer.updateWeather(-worldserver.rainLevel, worldserver.rainLevel, -worldserver.thunderLevel, worldserver.thunderLevel);
            // CraftBukkit end
        }

    }

    public void updateClient(EntityPlayer entityplayer) {
        entityplayer.updateInventory(entityplayer.defaultContainer);
        // entityplayer.triggerHealthUpdate();
        entityplayer.getBukkitEntity().updateScaledHealth(); // CraftBukkit - Update scaled health on respawn and worldchange
        entityplayer.playerConnection.sendPacket(new PacketPlayOutHeldItemSlot(entityplayer.inventory.itemInHandIndex));
        // CraftBukkit start - from GameRules
        int i = entityplayer.world.getGameRules().getBoolean(GameRules.REDUCED_DEBUG_INFO) ? 22 : 23;
        entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityStatus(entityplayer, (byte) i));
        float immediateRespawn = entityplayer.world.getGameRules().getBoolean(GameRules.DO_IMMEDIATE_RESPAWN) ? 1.0F: 0.0F;
        entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(11, immediateRespawn));
        // CraftBukkit end
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public boolean getHasWhitelist() {
        return this.whitelist.isEnabled(); // Paper
    }

    public void setHasWhitelist(boolean flag) {
        new com.destroystokyo.paper.event.server.WhitelistToggleEvent(flag).callEvent();
        this.whitelist.setEnabled(flag); // Paper
    }

    public List<EntityPlayer> b(String s) {
        List<EntityPlayer> list = Lists.newArrayList();
        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (entityplayer.v().equals(s)) {
                list.add(entityplayer);
            }
        }

        return list;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public NBTTagCompound save() {
        return null;
    }

    private void a(EntityPlayer entityplayer, EntityPlayer entityplayer1, GeneratorAccess generatoraccess) {
        if (entityplayer1 != null) {
            entityplayer.playerInteractManager.setGameMode(entityplayer1.playerInteractManager.getGameMode());
        } else if (this.t != null) {
            entityplayer.playerInteractManager.setGameMode(this.t);
        }

        entityplayer.playerInteractManager.b(generatoraccess.getWorldData().getGameType());
    }

    // Paper start - Extract method to allow for restarting flag
    public void shutdown() {
        this.shutdown(false);
    }

    public void shutdown(boolean isRestarting) {
        // CraftBukkit start - disconnect safely
        for (EntityPlayer player : this.players) {
            player.playerConnection.disconnect(!isRestarting ? this.server.server.getShutdownMessage() : org.spigotmc.SpigotConfig.restartMessage); // CraftBukkit - add custom shutdown message // Paper - add isRestarting flag
        }
        // CraftBukkit end

        // Paper start - Remove collideRule team if it exists
        if (this.collideRuleTeamName != null) {
            final Scoreboard scoreboard = this.getServer().getWorldServer(DimensionManager.OVERWORLD).getScoreboard();
            final ScoreboardTeam team = scoreboard.getTeam(this.collideRuleTeamName);
            if (team != null) scoreboard.removeTeam(team);
        }
        // Paper end
    }
    // Paper end

    // CraftBukkit start
    public void sendMessage(IChatBaseComponent[] iChatBaseComponents) {
        for (IChatBaseComponent component : iChatBaseComponents) {
            sendMessage(component, true);
        }
    }
    // CraftBukkit end

    public void sendMessage(IChatBaseComponent ichatbasecomponent, boolean flag) {
        this.server.sendMessage(ichatbasecomponent);
        ChatMessageType chatmessagetype = flag ? ChatMessageType.SYSTEM : ChatMessageType.CHAT;

        // CraftBukkit start - we run this through our processor first so we can get web links etc
        this.sendAll(new PacketPlayOutChat(CraftChatMessage.fixComponent(ichatbasecomponent), chatmessagetype));
        // CraftBukkit end
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        this.sendMessage(ichatbasecomponent, true);
    }

    // CraftBukkit start
    public ServerStatisticManager getStatisticManager(EntityPlayer entityhuman) {
        ServerStatisticManager serverstatisticmanager = entityhuman.getStatisticManager();
        return serverstatisticmanager == null ? getStatisticManager(entityhuman.getUniqueID(), entityhuman.getDisplayName().getString()) : serverstatisticmanager;
    }

    public ServerStatisticManager getStatisticManager(UUID uuid, String displayName) {
        EntityPlayer entityHuman = this.a(uuid);
        ServerStatisticManager serverstatisticmanager = entityHuman == null ? null : (ServerStatisticManager) entityHuman.getStatisticManager();
        // CraftBukkit end

        if (serverstatisticmanager == null) {
            File file = new File(this.server.getWorldServer(DimensionManager.OVERWORLD).getDataManager().getDirectory(), "stats");
            File file1 = new File(file, uuid + ".json");

            if (!file1.exists()) {
                File file2 = new File(file, displayName + ".json"); // CraftBukkit

                if (file2.exists() && file2.isFile()) {
                    file2.renameTo(file1);
                }
            }

            serverstatisticmanager = new ServerStatisticManager(this.server, file1);
            // this.o.put(uuid, serverstatisticmanager); // CraftBukkit
        }

        return serverstatisticmanager;
    }

    public AdvancementDataPlayer f(EntityPlayer entityplayer) {
        UUID uuid = entityplayer.getUniqueID();
        AdvancementDataPlayer advancementdataplayer = (AdvancementDataPlayer) entityplayer.getAdvancementData(); // CraftBukkit

        if (advancementdataplayer == null) {
            File file = new File(this.server.getWorldServer(DimensionManager.OVERWORLD).getDataManager().getDirectory(), "advancements");
            File file1 = new File(file, uuid + ".json");

            advancementdataplayer = new AdvancementDataPlayer(this.server, file1, entityplayer);
            // this.p.put(uuid, advancementdataplayer); // CraftBukkit
        }

        advancementdataplayer.a(entityplayer);
        return advancementdataplayer;
    }

    public void a(int i) {
        this.viewDistance = i;
        //this.sendAll(new PacketPlayOutViewDistance(i)); // Paper - move into setViewDistance
        Iterator iterator = this.server.getWorlds().iterator();

        while (iterator.hasNext()) {
            WorldServer worldserver = (WorldServer) iterator.next();

            if (worldserver != null) {
                worldserver.getChunkProvider().setViewDistance(i);
            }
        }

    }

    public List<EntityPlayer> getPlayers() {
        return this.players;
    }

    @Nullable
    public EntityPlayer a(UUID uuid) {
        return (EntityPlayer) this.j.get(uuid);
    }

    public boolean f(GameProfile gameprofile) {
        return false;
    }

    public void reload() {
        // CraftBukkit start
        /*Iterator iterator = this.p.values().iterator();

        while (iterator.hasNext()) {
            AdvancementDataPlayer advancementdataplayer = (AdvancementDataPlayer) iterator.next();

            advancementdataplayer.b();
        }*/

        for (EntityPlayer player : players) {
            player.getAdvancementData().b();
            player.getAdvancementData().b(player); // CraftBukkit - trigger immediate flush of advancements
        }
        // CraftBukkit end

        this.sendAll(new PacketPlayOutTags(this.server.getTagRegistry()));
        PacketPlayOutRecipeUpdate packetplayoutrecipeupdate = new PacketPlayOutRecipeUpdate(this.server.getCraftingManager().b());
        Iterator iterator1 = this.players.iterator();

        while (iterator1.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator1.next();

            entityplayer.playerConnection.sendPacket(packetplayoutrecipeupdate);
            entityplayer.B().a(entityplayer);
        }

    }

    public boolean v() {
        return this.u;
    }
}
