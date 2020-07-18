package org.stormrealms.stormmenus.util;

import java.util.Objects;

@FunctionalInterface
public interface SextFunction<T, U, V, Y, Z, S>
{
    default SextFunction<T, U, V, Y, Z, S> andThen(SextFunction<? super T, ? super U, ? super V, ? super Y, ? super Z, ? super S> after)
    {
        Objects.requireNonNull(after);
        return (a, b, c, d, e, f) -> {
            accept(a, b, c, d, e, f);
            after.accept(a, b, c, d, e, f);
        };
    }

    void accept(T t, U u, V v, Y y, Z z, S s);
}