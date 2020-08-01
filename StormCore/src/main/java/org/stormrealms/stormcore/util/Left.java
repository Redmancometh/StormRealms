package org.stormrealms.stormcore.util;

import java.util.function.Function;

public class Left<A> extends Either<A, Object> {
	protected A value;

	protected Left(A value) {
		this.value = value;
	}

	public static <L> Left<? super L> of(L value) {
		return new Left<>(value);
	}

	@Override
	public <B> Left<? super B> fmap(Function<A, B> f) {
		return Left.of(f.apply(value));
	}

	@Override
	public A undo() {
		return value;
	}

	@Override
	public <B> Monad<? super B> bind(Function<A, Monad<B>> f) {
		return f.apply(this.undo());
	}

	@Override
	public Left<? super A> pure(A value) {
		return Left.of(value);
	}

	@Override
	public <B> Applicative<? super B> apply(Applicative<Function<A, B>> f) {
		return this.fmap(f.undo());
	}

    @Override
    public <T> T match(Function<A, T> left, Function<Object, T> $) {
        return left.apply(value);
    }
}