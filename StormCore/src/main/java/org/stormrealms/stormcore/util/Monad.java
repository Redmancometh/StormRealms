package org.stormrealms.stormcore.util;

import java.util.function.Function;

public abstract class Monad<A> extends Applicative<A> {
	public Monad<? super A> create(A value) {
		return (Monad<? super A>) pure(value);
	}

	public abstract <B> Monad<? super B> bind(Function<A, Monad<B>> f);

	public <B> Monad<? super B> then(Monad<B> m) {
		return bind($ -> m);
	}
}