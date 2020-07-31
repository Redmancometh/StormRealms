package org.stormrealms.stormcore.util;

import java.util.function.Function;

public interface Applicative<A> extends Functor<A> {
	<T> Applicative<T> pure(T value);

	default <B> Applicative<? super B> apply(Applicative<Function<A, B>> f) {
		return (Applicative<? super B>) fmap(f.undo());
	}
}