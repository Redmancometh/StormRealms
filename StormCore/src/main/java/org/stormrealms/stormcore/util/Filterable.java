package org.stormrealms.stormcore.util;

import java.util.function.Function;

public interface Filterable<A> {
    Structure<? super A> filter(Function<A, Boolean> f);

    default Structure<? super A> filter(Boolean b) {
        return filter(a -> b);
    }
}