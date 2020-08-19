package org.stormrealms.stormcore.util;

import java.util.function.Function;

public class Left<L, $R> implements Either<L, $R> {
	protected L value;

	protected Left(L value) {
		this.value = value;
	}

	@Override
	public <B> Left<B, $R> fmap(Function<L, B> f) {
		return Either.left(f.apply(value));
	}

	@Override
	public <B> Either<B, $R> bind(Function<L, Either<B, $R>> f) {
		return f.apply(this.value);
	}

    @Override
    public <B> B match(Function<L, B> left, Function<$R, B> $) {
        return left.apply(value);
    }
}