package org.stormrealms.stormcore.util;

import java.util.function.Function;

public abstract class Monad<A> extends Applicative<A> {
	public Monad<? super A> create(A value) {
		return (Monad<? super A>) pure(value);
	}

	public abstract <B> Monad<B> bind(Function<A, Monad<B>> f);

	public <B> Monad<? super B> then(Monad<B> m) {
		return bind($ -> m);
	}

	public abstract <B> IterableM<B> flat();

	public <B> IterableM<B> flatMap(Function<A, Monad<B>> f) {
		return this.bind(f).flat();
	}

	@SuppressWarnings("unchecked")
	protected static <T> Either<Monad<T>, T> cast(Object obj) {
		return Either.when(obj instanceof Monad<?>, () -> (Monad<T>) obj, () -> (T) obj);
	}
}