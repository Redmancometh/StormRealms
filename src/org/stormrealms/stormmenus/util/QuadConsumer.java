package org.stormrealms.stormmenus.util;

import java.util.Objects;

@FunctionalInterface
public interface QuadConsumer<T, U, V, Y>
{
    default QuadConsumer<T, U, V, Y> andThen(QuadConsumer<? super T, ? super U, ? super V, ? super Y> after)
    {
        Objects.requireNonNull(after);
        return (a, b, c, d) -> {
            accept(a, b, c, d);
            after.accept(a, b, c, d);
        };
    }

    void accept(T t, U u, V v, Y y);
}