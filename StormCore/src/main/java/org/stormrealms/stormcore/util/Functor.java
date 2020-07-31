package org.stormrealms.stormcore.util;

import java.util.function.Function;

public interface Functor<A> extends Structure<A> {
    <B> Functor<? super B> fmap(Function<A, B> f);
}