package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class Pathfinder {

    private final Path a = new Path();
    private final Set<PathPoint> b = Sets.newHashSet();
    private final PathPoint[] c = new PathPoint[32];
    private final int d;
    private final PathfinderAbstract e; public PathfinderAbstract getPathfinder() { return this.e; }  // Paper - OBFHELPER

    public Pathfinder(PathfinderAbstract pathfinderabstract, int i) {
        this.e = pathfinderabstract;
        this.d = i;
    }

    @Nullable
    public PathEntity a(ChunkCache chunkcache, EntityInsentient entityinsentient, Set<BlockPosition> set, float f, int i, float f1) {
        this.a.a();
        this.e.a(chunkcache, entityinsentient);
        PathPoint pathpoint = this.e.b();
        // Paper start - remove streams - and optimize collection
        List<Map.Entry<PathDestination, BlockPosition>> map = new java.util.ArrayList<>();
        for (BlockPosition blockposition : set) {
            // cast is important
            //noinspection RedundantCast
            PathDestination path = this.e.a((double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ());
            map.add(new java.util.AbstractMap.SimpleEntry<>(path, blockposition));
        }
        // Paper end
        PathEntity pathentity = this.a(pathpoint, map, f, i, f1);

        this.e.a();
        return pathentity;
    }

    @Nullable
    private PathEntity a(PathPoint pathpoint, java.util.List<java.util.Map.Entry<PathDestination, BlockPosition>> list, float f, int i, float f1) { // Paper - list instead of set
        //Set<PathDestination> set = map.keySet(); // Paper

        pathpoint.e = 0.0F;
        pathpoint.f = this.a(pathpoint, list); // Paper - list instead of map
        pathpoint.g = pathpoint.f;
        this.a.a();
        this.b.clear();
        this.a.a(pathpoint);
        int j = 0;
        int k = (int) ((float) this.d * f1);

        while (!this.a.e()) {
            ++j;
            if (j >= k) {
                break;
            }

            PathPoint pathpoint1 = this.a.c();

            pathpoint1.i = true;
            // Paper start - remove streams
            for (int i1 = 0, listSize = list.size(); i1 < listSize; i1++) {
                PathDestination pathdestination = list.get(i1).getKey();
                if (pathpoint1.c(pathdestination) <= (float) i) {
                    pathdestination.e();
                }
            }
            boolean result = false;
            for (int i1 = 0, listSize = list.size(); i1 < listSize; i1++) {
                PathDestination pathdestination = list.get(i1).getKey();
                if (pathdestination.f()) {
                    result = true;
                    break;
                }
            }
            if (result) {
                // Paper end
                break;
            }

            if (pathpoint1.a(pathpoint) < f) {
                int l = this.e.a(this.c, pathpoint1);

                for (int i1 = 0; i1 < l; ++i1) {
                    PathPoint pathpoint2 = this.c[i1];
                    float f2 = pathpoint1.a(pathpoint2);

                    pathpoint2.j = pathpoint1.j + f2;
                    float f3 = pathpoint1.e + f2 + pathpoint2.k;

                    if (pathpoint2.j < f && (!pathpoint2.c() || f3 < pathpoint2.e)) {
                        pathpoint2.h = pathpoint1;
                        pathpoint2.e = f3;
                        pathpoint2.f = this.a(pathpoint2, list) * 1.5F; // Paper - use list instead of map
                        if (pathpoint2.c()) {
                            this.a.a(pathpoint2, pathpoint2.e + pathpoint2.f);
                        } else {
                            pathpoint2.g = pathpoint2.e + pathpoint2.f;
                            this.a.a(pathpoint2);
                        }
                    }
                }
            }
        }


        // Paper start - remove streams
        boolean result = false;
        for (int i1 = 0, listSize = list.size(); i1 < listSize; i1++) {
            PathDestination pathDestination = list.get(i1).getKey(); // Paper
            if (pathDestination.f()) {
                result = true;
                break;
            }
        }
        List<PathEntity> candidates = new java.util.ArrayList<>();
        if (result) {
            for (int i1 = 0, listSize = list.size(); i1 < listSize; i1++) {
                Map.Entry<PathDestination, BlockPosition> entry = list.get(i1);
                PathDestination pathdestination = entry.getKey();
                if (pathdestination.f()) {
                    PathEntity pathEntity = this.a(pathdestination.d(), entry.getValue(), true);
                    candidates.add(pathEntity);
                }
            }
            if (candidates.isEmpty()) return null;
            candidates.sort(Comparator.comparingInt(PathEntity::e));
        } else {
            for (int i1 = 0, listSize = list.size(); i1 < listSize; i1++) {
                Map.Entry<PathDestination, BlockPosition> entry = list.get(i1);
                PathDestination pathdestination = entry.getKey();
                PathEntity pathEntity = this.a(pathdestination.d(), entry.getValue(), false);
                candidates.add(pathEntity);
            }
            if (candidates.isEmpty()) return null;
            candidates.sort(Comparator.comparingDouble(PathEntity::l).thenComparingInt(PathEntity::e));
        }
        return candidates.get(0);
        // Paper end
    }

    private float a(PathPoint pathpoint, java.util.List<java.util.Map.Entry<PathDestination, BlockPosition>> list) {
        float f = Float.MAX_VALUE;

        float f1;

        for (int i = 0, listSize = list.size(); i < listSize; f = Math.min(f1, f), i++) { // Paper
            PathDestination pathdestination = list.get(i).getKey(); // Paper

            f1 = pathpoint.a(pathdestination);
            pathdestination.a(f1, pathpoint);
        }

        return f;
    }

    private PathEntity a(PathPoint pathpoint, BlockPosition blockposition, boolean flag) {
        List<PathPoint> list = Lists.newArrayList();
        PathPoint pathpoint1 = pathpoint;

        list.add(0, pathpoint);

        while (pathpoint1.h != null) {
            pathpoint1 = pathpoint1.h;
            list.add(0, pathpoint1);
        }

        return new PathEntity(list, blockposition, flag);
    }
}
