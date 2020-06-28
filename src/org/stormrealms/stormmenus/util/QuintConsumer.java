package org.stormrealms.stormmenus.util;

import java.util.Objects;

@FunctionalInterface
public interface QuintConsumer<T, U, V, Y, Z>
{
    default QuintConsumer<T, U, V, Y, Z> andThen(QuintConsumer<? super T, ? super U, ? super V, ? super Y, ? super Z> after)
    {
        Objects.requireNonNull(after);
        return (a, b, c, d, e) -> {
            accept(a, b, c, d, e);
            after.accept(a, b, c, d, e);
        };
    }

    void accept(T t, U u, V v, Y y, Z z);
}