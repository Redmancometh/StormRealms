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
	public boolean isJust() {
		return false;
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
	public <B> None<B> bind(Function<A, Maybe<B>> f) {
		return none();
	}
}