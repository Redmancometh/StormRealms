package org.stormrealms.stormcore.util;

import java.util.function.Function;
import java.util.function.Supplier;

public class Just<A> implements Maybe<A> {
    protected A value;

    protected Just(A value) {
        this.value = value;
    }

    public static <T> Just<T> of(T value) {
        return new Just<>(value);
    }

    @Override
    public <B> Just<? super B> fmap(Function<A, B> f) {
        return Just.of(f.apply(value));
    }

    @Override
    public A undo() {
        return value;
    }

    @Override
    public Maybe<A> filter(Function<A, Boolean> f) {
        if (f.apply(value))
            return this;
        return Maybe.none();
    }

    @Override
    public <T> T match(Function<A, T> just, Supplier<T> none) {
        return just.apply(value);
    }
}