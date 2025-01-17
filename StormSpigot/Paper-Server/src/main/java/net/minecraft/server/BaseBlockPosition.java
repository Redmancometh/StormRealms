package net.minecraft.server;

import com.google.common.base.MoreObjects;
import javax.annotation.concurrent.Immutable;

@Immutable
public class BaseBlockPosition implements Comparable<BaseBlockPosition> {

    public static final BaseBlockPosition ZERO = new BaseBlockPosition(0, 0, 0);
    // Paper start
    protected int x;
    protected int y;
    protected int z;

    public final boolean isValidLocation() {
        return x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000 && y >= 0 && y < 256;
    }
    public final boolean isInvalidYLocation() {
        return y < 0 || y >= 256;
    }
    // Paper end

    public BaseBlockPosition(int i, int j, int k) {
        this.x = i;
        this.y = j;
        this.z = k;
    }

    public BaseBlockPosition(double d0, double d1, double d2) {
        this(MathHelper.floor(d0), MathHelper.floor(d1), MathHelper.floor(d2));
    }

    public final boolean equals(Object object) { // Paper
        if (this == object) {
            return true;
        } else if (!(object instanceof BaseBlockPosition)) {
            return false;
        } else {
            BaseBlockPosition baseblockposition = (BaseBlockPosition) object;

            return x == baseblockposition.x && z == baseblockposition.z && y == baseblockposition.y; // Paper
        }
    }

    public final int hashCode() { // Paper
        return (this.y + this.z * 31) * 31 + this.x; // Paper
    }

    public int compareTo(BaseBlockPosition baseblockposition) {
        return this.getY() == baseblockposition.getY() ? (this.getZ() == baseblockposition.getZ() ? this.getX() - baseblockposition.getX() : this.getZ() - baseblockposition.getZ()) : this.getY() - baseblockposition.getY();
    }

    // Paper start
    public final int getX() {
        return this.x;
    }

    public final int getY() {
        return this.y;
    }

    public final int getZ() {
        return this.z;
    }
    // Paper end

    public BaseBlockPosition down() {
        return this.down(1);
    }

    public BaseBlockPosition down(int i) {
        return this.shift(EnumDirection.DOWN, i);
    }

    public BaseBlockPosition shift(EnumDirection enumdirection, int i) {
        return i == 0 ? this : new BaseBlockPosition(this.getX() + enumdirection.getAdjacentX() * i, this.getY() + enumdirection.getAdjacentY() * i, this.getZ() + enumdirection.getAdjacentZ() * i);
    }
    // Paper end

    public BaseBlockPosition d(BaseBlockPosition baseblockposition) {
        return new BaseBlockPosition(this.getY() * baseblockposition.getZ() - this.getZ() * baseblockposition.getY(), this.getZ() * baseblockposition.getX() - this.getX() * baseblockposition.getZ(), this.getX() * baseblockposition.getY() - this.getY() * baseblockposition.getX());
    }

    public boolean a(BaseBlockPosition baseblockposition, double d0) {
        return this.distanceSquared((double) baseblockposition.x, (double) baseblockposition.y, (double) baseblockposition.z, false) < d0 * d0; // Paper
    }

    public boolean a(IPosition iposition, double d0) {
        return this.distanceSquared(iposition.getX(), iposition.getY(), iposition.getZ(), true) < d0 * d0;
    }

    public final double distanceSquared(BaseBlockPosition baseblockposition) { return m(baseblockposition); } // Paper - OBFHELPER
    public double m(BaseBlockPosition baseblockposition) {
        return this.distanceSquared((double) baseblockposition.getX(), (double) baseblockposition.getY(), (double) baseblockposition.getZ(), true);
    }

    public double a(IPosition iposition, boolean flag) {
        return this.distanceSquared(iposition.getX(), iposition.getY(), iposition.getZ(), flag);
    }

    public double distanceSquared(double d0, double d1, double d2, boolean flag) {
        double d3 = flag ? 0.5D : 0.0D;
        double d4 = (double) this.getX() + d3 - d0;
        double d5 = (double) this.getY() + d3 - d1;
        double d6 = (double) this.getZ() + d3 - d2;

        return d4 * d4 + d5 * d5 + d6 * d6;
    }

    public int n(BaseBlockPosition baseblockposition) {
        float f = (float) Math.abs(baseblockposition.getX() - this.x); // Paper
        float f1 = (float) Math.abs(baseblockposition.getY() - this.y); // Paper
        float f2 = (float) Math.abs(baseblockposition.getZ() - this.z); // Paper

        return (int) (f + f1 + f2);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }
}
