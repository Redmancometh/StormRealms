package org.stormrealms.stormcore.util;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Maybe<A> extends Monad<A>, Filterable<A> {
    public static <T> None<T> none() {
        return new None<T>();
    }

    public static <T> Maybe<T> when(boolean b, Supplier<T> f) {
        return b ? Just.of(f.get()) : none();
    }

    public <T> T match(Function<A, T> just, Supplier<T> none);

    @Override
    default <T> Monad<T> pure(T value) {
        return Just.of(value);
    }
}