package org.stormrealms.stormcore.util;

import java.util.function.Function;

public class None<A> implements Maybe<A> {
    protected None() { }

    @Override
    public <B> None<B> fmap(Function<A, B> f) {
        return Maybe.none();
    }

    @Override
    public A undo() {
        return null;
    }

    @Override
    public None<A> filter(Function<A, Boolean> f) {
        return this;
    }
}