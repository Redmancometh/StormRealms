package org.stormrealms.stormcore.util;

import java.util.function.Function;

public interface Monad<A> extends Applicative<A> {
	default <B> Monad<? super B> bind(Function<A, Monad<B>> f) {
		return (Monad<? super B>) apply(pure((Function<A, B>) a -> (B) f.apply(a).undo()));
	}
}