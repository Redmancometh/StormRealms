package org.stormrealms.stormcore.util;

import java.util.function.Function;

public class Right<$L, R> implements Either<$L, R> {
	protected R value;

	protected Right(R value) {
		this.value = value;
	}

	@Override
	public <B> Right<B, R> fmap(Function<$L, B> f) {
		return Either.right(value);
	}

	@Override
	public <B> Right<B, R> bind(Function<$L, Either<B, R>> f) {
		return Either.right(value);
	}

    @Override
    public <B> B match(Function<$L, B> $, Function<R, B> right) {
        return right.apply(value);
    }
}