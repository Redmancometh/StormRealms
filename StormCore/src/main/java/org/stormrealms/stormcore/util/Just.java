package org.stormrealms.stormcore.util;

import java.util.function.Function;
import java.util.function.Supplier;

public class Just<A> extends Maybe<A> {
	protected A value;

	protected Just(A value) {
		this.value = value;
	}

	@Override
	public <B> Just<B> fmap(Function<A, B> f) {
		return just(f.apply(value));
	}

	@Override
	public <B> Maybe<B> bind(Function<A, Maybe<B>> f) {
		return f.apply(this.value);
	}

	@Override
	public boolean isJust() {
		return true;
	}

	@Override
	public <T> T match(Function<A, T> just, Supplier<T> none) {
		return just.apply(this.value);
	}

	@Override
	public <T, U extends Throwable> T matchOrThrow(Function<A, T> just, Supplier<U> throwable) {
		return just.apply(this.value);
	}
}