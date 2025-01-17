package net.minecraft.server;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import javax.annotation.Nullable;

// Spigot start
import com.google.common.base.Predicate;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.util.concurrent.Futures;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.authlib.Agent;
import com.mojang.authlib.ProfileLookupCallback;
import java.util.concurrent.Callable;
// Spigot end

public class TileEntitySkull extends TileEntity /*implements ITickable*/ { // Paper - remove tickable

    public GameProfile gameProfile;
    private int b;
    private boolean c;
    private static UserCache userCache;
    private static MinecraftSessionService sessionService;
    // Spigot start
    public static final ExecutorService executor = Executors.newFixedThreadPool(3,
            new ThreadFactoryBuilder()
                    .setNameFormat("Head Conversion Thread - %1$d")
                    .build()
    );
    public static final LoadingCache<String, GameProfile> skinCache = CacheBuilder.newBuilder()
            .maximumSize( 5000 )
            .expireAfterAccess( 60, TimeUnit.MINUTES )
            .build( new CacheLoader<String, GameProfile>()
            {
                @Override
                public GameProfile load(String key) throws Exception
                {
                    final GameProfile[] profiles = new GameProfile[1];
                    ProfileLookupCallback gameProfileLookup = new ProfileLookupCallback() {

                        @Override
                        public void onProfileLookupSucceeded(GameProfile gp) {
                            profiles[0] = gp;
                        }

                        @Override
                        public void onProfileLookupFailed(GameProfile gp, Exception excptn) {
                            profiles[0] = gp;
                        }
                    };

                    MinecraftServer.getServer().getGameProfileRepository().findProfilesByNames(new String[] { key }, Agent.MINECRAFT, gameProfileLookup);

                    GameProfile profile = profiles[ 0 ];
                    if (profile == null) {
                        UUID uuid = EntityHuman.a(new GameProfile(null, key));
                        profile = new GameProfile(uuid, key);

                        gameProfileLookup.onProfileLookupSucceeded(profile);
                    } else
                    {

                        Property property = Iterables.getFirst( profile.getProperties().get( "textures" ), null );

                        if ( property == null )
                        {
                            profile = TileEntitySkull.sessionService.fillProfileProperties( profile, true );
                        }
                    }


                    return profile;
                }
            } );
    // Spigot end

    public TileEntitySkull() {
        super(TileEntityTypes.SKULL);
    }

    public static void a(UserCache usercache) {
        TileEntitySkull.userCache = usercache;
    }

    public static void a(MinecraftSessionService minecraftsessionservice) {
        TileEntitySkull.sessionService = minecraftsessionservice;
    }

    @Override
    public NBTTagCompound save(NBTTagCompound nbttagcompound) {
        super.save(nbttagcompound);
        if (this.gameProfile != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            GameProfileSerializer.serialize(nbttagcompound1, this.gameProfile);
            nbttagcompound.set("Owner", nbttagcompound1);
        }

        return nbttagcompound;
    }

    @Override
    public void load(NBTTagCompound nbttagcompound) {
        super.load(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("Owner", 10)) {
            this.setGameProfile(GameProfileSerializer.deserialize(nbttagcompound.getCompound("Owner")));
        } else if (nbttagcompound.hasKeyOfType("ExtraType", 8)) {
            String s = nbttagcompound.getString("ExtraType");

            if (!UtilColor.b(s)) {
                this.setGameProfile(new GameProfile((UUID) null, s));
            }
        }

    }

    // Paper - remove override
    public void tick() {
        Block block = this.getBlock().getBlock();

        if (block == Blocks.DRAGON_HEAD || block == Blocks.DRAGON_WALL_HEAD) {
            if (this.world.isBlockIndirectlyPowered(this.position)) {
                this.c = true;
                ++this.b;
            } else {
                this.c = false;
            }
        }

    }

    @Nullable
    @Override
    public PacketPlayOutTileEntityData getUpdatePacket() {
        return new PacketPlayOutTileEntityData(this.position, 4, sanitizeTileEntityUUID(this.b())); // Paper
    }

    // Paper start
    static NBTTagCompound sanitizeTileEntityUUID(NBTTagCompound cmp) {
        NBTTagCompound owner = cmp.getCompound("Owner");
        if (!owner.isEmpty()) {
            sanitizeUUID(owner);
        }
        return cmp;
    }

    static void sanitizeUUID(NBTTagCompound owner) {
        NBTTagCompound properties = owner.getCompound("Properties");
        NBTTagList list = null;
        if (!properties.isEmpty()) {
            list = properties.getList("textures", 10);
        }

        if (list != null && !list.isEmpty()) {
            String textures = ((NBTTagCompound)list.get(0)).getString("Value");
            if (textures != null && textures.length() > 3) {
                String uuid = UUID.nameUUIDFromBytes(textures.getBytes()).toString();
                owner.setString("Id", uuid);
                return;
            }
        }
        owner.setString("Id", UUID.randomUUID().toString());
    }
    // Paper end

    @Override
    public NBTTagCompound b() {
        return this.save(new NBTTagCompound());
    }

    public void setGameProfile(@Nullable GameProfile gameprofile) {
        this.gameProfile = gameprofile;
        this.f();
    }

    private void f() {
        // Spigot start
        GameProfile profile = this.gameProfile;
        if (profile != null && profile.isComplete() && profile.getProperties().containsKey("textures")) return; // Paper
        b(profile, new Predicate<GameProfile>() {

            @Override
            public boolean apply(GameProfile input) {
                gameProfile = input;
                update();
                return false;
            }
        }, false);
        // Spigot end
    }

    // Spigot start - Support async lookups
    public static Future<GameProfile> b(final GameProfile gameprofile, final Predicate<GameProfile> callback, boolean sync) {
        if (gameprofile != null && !UtilColor.b(gameprofile.getName())) {
            if (gameprofile.isComplete() && gameprofile.getProperties().containsKey("textures")) {
                callback.apply(gameprofile);
            } else if (MinecraftServer.getServer() == null) {
                callback.apply(gameprofile);
            } else {
                // Paper start
                com.destroystokyo.paper.profile.CraftPlayerProfile paperProfile = new com.destroystokyo.paper.profile.CraftPlayerProfile(gameprofile);
                if (sync) {
                    // might complete by cache, but if not, go ahead and do it now, avoid the code below
                    paperProfile.complete(true, true);
                } else {
                    paperProfile.completeFromCache(false, true);
                }
                GameProfile profile = paperProfile.getGameProfile();
                // Paper end
                if (profile != null && Iterables.getFirst(profile.getProperties().get("textures"), (Object) null) != null) {
                    callback.apply(profile);

                    return Futures.immediateFuture(profile);
                } else {
                    Callable<GameProfile> callable = new Callable<GameProfile>() {
                        @Override
                        public GameProfile call() {
                            // Paper start
                            paperProfile.complete(true, true);
                            final GameProfile profile = paperProfile.getGameProfile();
                            // Paper end
                            MinecraftServer.getServer().processQueue.add(new Runnable() {
                                @Override
                                public void run() {
                                    if (profile == null) {
                                        callback.apply(gameprofile);
                                    } else {
                                        callback.apply(profile);
                                    }
                                }
                            });
                            return profile;
                        }
                    };
                    if (sync) {
                        try {
                            return Futures.immediateFuture(callable.call());
                        } catch (Exception ex) {
                            com.google.common.base.Throwables.throwIfUnchecked(ex);
                            throw new RuntimeException(ex); // Not possible
                        }
                    } else {
                        return executor.submit(callable);
                    }
                }
            }
        } else {
            callback.apply(gameprofile);
        }

        return Futures.immediateFuture(gameprofile);
    }
    // Spigot end
}
