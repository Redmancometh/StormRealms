package org.stormrealms.stormcore.util;

import java.util.function.Function;

public class Box<A> extends Monad<A> {
	private A value;

	protected Box(A value) {
		this.value = value;
	}

	public static <T> Box<T> of(T value) {
		return new Box<>(value);
	}

	@Override
	public <B> Box<? super B> fmap(Function<A, B> f) {
		return Box.of(f.apply(value));
	}

	@Override
	public A undo() {
		return value;
	}

	@Override
	public <B> Monad<B> bind(Function<A, Monad<B>> f) {
		return f.apply(value);
	}

	@Override
	public <B> IterableM<B> flat() {
		return Monad.<B>cast(value).match((Monad<B> m) -> m.flat(), (B v) -> IterableM.of(v));
	}

	@Override
	public Box<? super A> pure(A value) {
		return Box.of(value);
	}

	@Override
	public <B> Applicative<? super B> apply(Applicative<Function<A, B>> f) {
		return this.fmap(f.undo());
	}
	
}