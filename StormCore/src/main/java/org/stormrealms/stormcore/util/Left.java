package org.stormrealms.stormcore.util;

import java.util.function.Function;

public class Left<A> implements Either<A> {
	protected A value;

	protected Left(A value) {
		this.value = value;
	}

	public static <L> Left<L> of(L value) {
		return new Left<>(value);
	}

	@Override
	public <B> Left<B> fmap(Function<A, B> f) {
		return Left.of(f.apply(value));
	}

	@Override
	public A undo() {
		return value;
	}
}