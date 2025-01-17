package net.minecraft.server;

import java.util.Random;

public class MobSpawnerPatrol {

    private int getSpawnDelay() { return a; } // Paper - OBFHELPER
    private void setSpawnDelay(int spawnDelay) { this.a = spawnDelay; } // Paper - OBFHELPER
    private int a;

    public MobSpawnerPatrol() {}

    public int a(WorldServer worldserver, boolean flag, boolean flag1) {
        if (worldserver.paperConfig.disablePillagerPatrols || worldserver.paperConfig.patrolSpawnChance == 0) return 0; // Paper
        if (!flag) {
            return 0;
        } else if (!worldserver.getGameRules().getBoolean(GameRules.DO_PATROL_SPAWNING)) {
            return 0;
        } else {
            Random random = worldserver.random;

            // Paper start - Patrol settings
            // Random player selection moved up for per player spawning and configuration
            int j = worldserver.getPlayers().size();
            if (j < 1) {
                return 0;
            }

            EntityPlayer entityhuman = worldserver.getPlayers().get(random.nextInt(j));
            if (entityhuman.isSpectator()) {
                return 0;
            }

            int patrolSpawnDelay;
            if (worldserver.paperConfig.patrolPerPlayerDelay) {
                --entityhuman.patrolSpawnDelay;
                patrolSpawnDelay = entityhuman.patrolSpawnDelay;
            } else {
                setSpawnDelay(getSpawnDelay() - 1);
                patrolSpawnDelay = getSpawnDelay();
            }

            if (patrolSpawnDelay > 0) {
                return 0;
            } else {
                long days;
                if (worldserver.paperConfig.patrolPerPlayerStart) {
                    days = entityhuman.getStatisticManager().getStatisticValue(StatisticList.CUSTOM.get(StatisticList.PLAY_ONE_MINUTE)) / 24000L; // PLAY_ONE_MINUTE is actually counting in ticks, a misnomer by Mojang
                } else {
                    days = worldserver.getDayTime() / 24000L;
                }
                if (worldserver.paperConfig.patrolPerPlayerDelay) {
                    entityhuman.patrolSpawnDelay += worldserver.paperConfig.patrolDelay + random.nextInt(1200);
                } else {
                    setSpawnDelay(getSpawnDelay() + worldserver.paperConfig.patrolDelay + random.nextInt(1200));
                }

                if (days >= worldserver.paperConfig.patrolStartDay && worldserver.isDay()) {
                    if (random.nextDouble() >= worldserver.paperConfig.patrolSpawnChance) {
                        // Paper end
                        return 0;
                    } else {

                        if (j < 1) {
                            return 0;
                        } else {

                            if (entityhuman.isSpectator()) {
                                return 0;
                            } else if (worldserver.b_(entityhuman.getChunkCoordinates())) {
                                return 0;
                            } else {
                                int k = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                                int l = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
                                BlockPosition.MutableBlockPosition blockposition_mutableblockposition = (new BlockPosition.MutableBlockPosition(entityhuman)).e(k, 0, l);

                                if (!worldserver.isAreaLoaded(blockposition_mutableblockposition.getX() - 10, blockposition_mutableblockposition.getY() - 10, blockposition_mutableblockposition.getZ() - 10, blockposition_mutableblockposition.getX() + 10, blockposition_mutableblockposition.getY() + 10, blockposition_mutableblockposition.getZ() + 10)) {
                                    return 0;
                                } else {
                                    BiomeBase biomebase = worldserver.getBiome(blockposition_mutableblockposition);
                                    BiomeBase.Geography biomebase_geography = biomebase.q();

                                    if (biomebase_geography == BiomeBase.Geography.MUSHROOM) {
                                        return 0;
                                    } else {
                                        int i1 = 0;
                                        int j1 = (int) Math.ceil((double) worldserver.getDamageScaler(blockposition_mutableblockposition).b()) + 1;

                                        for (int k1 = 0; k1 < j1; ++k1) {
                                            ++i1;
                                            blockposition_mutableblockposition.p(worldserver.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, blockposition_mutableblockposition).getY());
                                            if (k1 == 0) {
                                                if (!this.a(worldserver, blockposition_mutableblockposition, random, true)) {
                                                    break;
                                                }
                                            } else {
                                                this.a(worldserver, blockposition_mutableblockposition, random, false);
                                            }

                                            blockposition_mutableblockposition.o(blockposition_mutableblockposition.getX() + random.nextInt(5) - random.nextInt(5));
                                            blockposition_mutableblockposition.q(blockposition_mutableblockposition.getZ() + random.nextInt(5) - random.nextInt(5));
                                        }

                                        return i1;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    return 0;
                }
            }
        }
    }

    private boolean a(World world, BlockPosition blockposition, Random random, boolean flag) {
        IBlockData iblockdata = world.getType(blockposition);

        if (!SpawnerCreature.a((IBlockAccess) world, blockposition, iblockdata, iblockdata.getFluid())) {
            return false;
        } else if (!EntityMonsterPatrolling.b(EntityTypes.PILLAGER, world, EnumMobSpawn.PATROL, blockposition, random)) {
            return false;
        } else {
            EntityMonsterPatrolling entitymonsterpatrolling = (EntityMonsterPatrolling) EntityTypes.PILLAGER.a(world);

            if (entitymonsterpatrolling != null) {
                if (flag) {
                    entitymonsterpatrolling.setPatrolLeader(true);
                    entitymonsterpatrolling.ey();
                }

                entitymonsterpatrolling.setPosition((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
                entitymonsterpatrolling.prepare(world, world.getDamageScaler(blockposition), EnumMobSpawn.PATROL, (GroupDataEntity) null, (NBTTagCompound) null);
                world.addEntity(entitymonsterpatrolling, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.PATROL); // CraftBukkit
                return true;
            } else {
                return false;
            }
        }
    }
}
