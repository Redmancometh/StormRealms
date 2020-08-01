package org.stormrealms.stormcore.util;

import java.util.function.Function;

public class Right<A> extends Either<Object, A> {
	protected A value;

	protected Right(A value) {
		this.value = value;
	}

	public static <R> Right<R> of(R value) {
		return new Right<>(value);
	}

	@Override
	public A undo() {
		return value;
	}

	@Override
	public <B> Right<? super A> bind(Function<Object, Monad<B>> f) {
		return this;
	}

	@Override
	public <B> Right<? super A> apply(Applicative<Function<Object, B>> f) {
		return this;
	}

    @Override
    public <B> Right<? super A> fmap(Function<Object, B> f) {
        return this;
    }

    @Override
    public <T> T match(Function<Object, T> $, Function<A, T> right) {
        return right.apply(value);
    }
}