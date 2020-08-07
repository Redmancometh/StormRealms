package org.stormrealms.stormcore.util;

import java.util.function.Function;
import java.util.function.Supplier;

public class Just<A> extends Maybe<A> {
	protected A value;

	protected Just(A value) {
		this.value = value;
	}

	public static <T> Just<T> of(T value) {
		return new Just<>(value);
	}

	@Override
	public <B> Just<B> fmap(Function<A, B> f) {
		return Just.of(f.apply(value));
	}

	@Override
	public A undo() {
		return value;
	}

	@Override
	public Maybe<A> filter(Function<A, Boolean> f) {
		if (f.apply(value))
			return this;
		return Maybe.none();
	}

	@Override
	public boolean isJust() {
		return true;
	}

	@Override
	public <T> T match(Function<A, T> just, Supplier<T> none) {
		return just.apply(this.undo());
	}

	@Override
	public <T, U extends Throwable> T matchOrThrow(Function<A, T> just, Supplier<U> throwable) {
		return just.apply(this.undo());
	}

	@Override
	public <B> Monad<B> bind(Function<A, Monad<B>> f) {
		return f.apply(this.undo());
	}

	@Override
	public Applicative<? super A> pure(A value) {
		return Just.of(this.undo());
	}

	@Override
	public <B> IterableM<B> flat() {
		return Monad.<B>cast(value).match((Monad<B> m) -> m.flat(), (B v) -> IterableM.of(v));
	}

	@Override
	public <B> Applicative<? super B> apply(Applicative<Function<A, B>> f) {
		return this.fmap(f.undo());
	}
}