package net.minecraft.server;

import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SectionPosition extends BaseBlockPosition {

    private SectionPosition(int i, int j, int k) {
        super(i, j, k);
    }

    public static SectionPosition a(int i, int j, int k) {
        return new SectionPosition(i, j, k);
    }

    public static SectionPosition a(BlockPosition blockposition) {
        return new SectionPosition(blockposition.getX() >> 4, blockposition.getY() >> 4, blockposition.getZ() >> 4); // Paper
    }

    public static SectionPosition a(ChunkCoordIntPair chunkcoordintpair, int i) {
        return new SectionPosition(chunkcoordintpair.x, i, chunkcoordintpair.z);
    }

    public static SectionPosition a(Entity entity) {
        return new SectionPosition(a(MathHelper.floor(entity.locX())), a(MathHelper.floor(entity.locY())), a(MathHelper.floor(entity.locZ())));
    }

    public static SectionPosition a(long i) {
        return new SectionPosition((int) (i >> 42), (int) (i << 44 >> 44), (int) (i << 22 >> 42)); // Paper
    }

    public static long a(long i, EnumDirection enumdirection) {
        return a(i, enumdirection.getAdjacentX(), enumdirection.getAdjacentY(), enumdirection.getAdjacentZ());
    }

    // Paper start
    public static long getAdjacentFromBlockPos(int x, int y, int z, EnumDirection enumdirection) {
        return (((long) ((x >> 4) + enumdirection.getAdjacentX()) & 4194303L) << 42) | (((long) ((y >> 4) + enumdirection.getAdjacentY()) & 1048575L)) | (((long) ((z >> 4) + enumdirection.getAdjacentZ()) & 4194303L) << 20);
    }
    public static long getAdjacentFromSectionPos(int x, int y, int z, EnumDirection enumdirection) {
        return (((long) (x + enumdirection.getAdjacentX()) & 4194303L) << 42) | (((long) ((y) + enumdirection.getAdjacentY()) & 1048575L)) | (((long) (z + enumdirection.getAdjacentZ()) & 4194303L) << 20);
    }
    // Paper end
    public static long a(long i, int j, int k, int l) {
        return (((long) ((int) (i >> 42) + j) & 4194303L) << 42) | (((long) ((int) (i << 44 >> 44) + k) & 1048575L)) | (((long) ((int) (i << 22 >> 42) + l) & 4194303L) << 20); // Simplify to reduce instruction count
    }

    public static int a(int i) {
        return i >> 4;
    }

    public static int b(int i) {
        return i & 15;
    }

    public static short b(BlockPosition blockposition) {
        return (short) ((blockposition.x & 15) << 8 | (blockposition.z & 15) << 4 | blockposition.y & 15); // Paper - simplify/inline
    }

    public static int c(int i) {
        return i << 4;
    }

    public static int b(long i) {
        return (int) (i >> 42); // Paper
    }

    public static int c(long i) {
        return (int) (i << 44 >> 44);
    }

    public static int d(long i) {
        return (int) (i << 22 >> 42);
    }

    public final int a() { // Paper
        return x; // Paper
    }

    public final int b() { // Paper
        return y; // Paper
    }

    public final int c() { // Paper
        return z; // Paper
    }

    public final int d() { // Paper
        return x << 4; // Paper
    }

    public final int e() { // Paper
        return y << 4; // Paper
    }

    public final int f() { // Paper
        return z << 4; // Paper
    }

    public final int g() { // Paper
        return (x << 4) + 15; // Paper
    }

    public final int h() { // Paper
        return (y << 4) + 15; // Paper
    }

    public final int r() { // Paper
        return (z << 4) + 15; // Paper
    }

    public static long blockToSection(long i) { return e(i); } // Paper - OBFHELPER
    public static long e(long i) {
        // b(a(BlockPosition.b(i)), a(BlockPosition.c(i)), a(BlockPosition.d(i)));
        return (((long) (int) (i >> 42) & 4194303L) << 42) | (((long) (int) ((i << 52) >> 56) & 1048575L)) | (((long) (int) ((i << 26) >> 42) & 4194303L) << 20); // Simplify to reduce instruction count
    }

    public static long f(long i) {
        return i & -1048576L;
    }

    public BlockPosition s() {
        return new BlockPosition(x << 4, y << 4, z << 4); // Paper
    }

    public BlockPosition t() {
        boolean flag = true;

        return this.s().b(8, 8, 8);
    }

    public ChunkCoordIntPair u() {
        return new ChunkCoordIntPair(this.a(), this.c());
    }

    // Paper start
    public static long blockPosAsSectionLong(int i, int j, int k) {
        return (((long) (i >> 4) & 4194303L) << 42) | (((long) (j >> 4) & 1048575L)) | (((long) (k >> 4) & 4194303L) << 20);
    }
    // Paper end
    public static long asLong(int i, int j, int k) { return b(i, j, k); } // Paper - OBFHELPER
    public static long b(int i, int j, int k) {
        return (((long) i & 4194303L) << 42) | (((long) j & 1048575L)) | (((long) k & 4194303L) << 20); // Paper - Simplify to reduce instruction count
    }

    public long v() {
        return (((long) x & 4194303L) << 42) | (((long) y & 1048575L)) | (((long) z & 4194303L) << 20); // Paper - Simplify to reduce instruction count
    }

    public Stream<BlockPosition> w() {
        return BlockPosition.a(x << 4, y << 4, z << 4, (x << 4) + 15, (y << 4) + 15, (z << 4) + 15); // Paper - simplify/inline
    }

    public static Stream<SectionPosition> a(SectionPosition sectionposition, int i) {
        return a(sectionposition.x - i, sectionposition.y - i, sectionposition.z - i, sectionposition.x + i, sectionposition.y + i, sectionposition.z + i); // Paper - simplify/inline
    }

    public static Stream<SectionPosition> b(ChunkCoordIntPair chunkcoordintpair, int i) {
        return a(chunkcoordintpair.x - i, 0, chunkcoordintpair.z - i, chunkcoordintpair.x + i, 15, chunkcoordintpair.z + i); // Paper - simplify/inline
    }

    public static Stream<SectionPosition> a(final int i, final int j, final int k, final int l, final int i1, final int j1) {
        return StreamSupport.stream(new AbstractSpliterator<SectionPosition>((long) ((l - i + 1) * (i1 - j + 1) * (j1 - k + 1)), 64) {
            final CursorPosition a = new CursorPosition(i, j, k, l, i1, j1);

            public boolean tryAdvance(Consumer<? super SectionPosition> consumer) {
                if (this.a.a()) {
                    consumer.accept(new SectionPosition(this.a.b(), this.a.c(), this.a.d()));
                    return true;
                } else {
                    return false;
                }
            }
        }, false);
    }
}
