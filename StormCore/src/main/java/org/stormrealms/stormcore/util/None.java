package org.stormrealms.stormcore.util;

import java.util.function.Function;
import java.util.function.Supplier;

public class None<A> extends Maybe<A> {
	protected None() { }

	@Override
	public <B> None<B> fmap(Function<A, B> f) {
		return none();
	}

	@Override
	public A undo() {
		return null;
	}

	@Override
	public None<A> filter(Function<A, Boolean> f) {
		return this;
	}

	@Override
	public <T> T match(Function<A, T> just, Supplier<T> none) {
		return none.get();
	}

	@Override
	public <T, U extends Throwable> T matchOrThrow(Function<A, T> just, Supplier<U> throwable) throws U {
		throw throwable.get();
	}

	@Override
	public <B> Monad<? super B> bind(Function<A, Monad<B>> f) {
		return none();
	}

	@Override
	public Applicative<? super A> pure(A value) {
		return none();
	}

	@Override
	public <B> Applicative<? super B> apply(Applicative<Function<A, B>> f) {
		return none();
	}
}