package org.stormrealms.stormcore.util;

import java.util.function.Function;

public class Right<RightType> implements Either<Object> {
	protected RightType value;

	protected Right(RightType value) {
		this.value = value;
	}

	public static <R> Right<R> of(R value) {
		return new Right<>(value);
	}

	@Override
	public <B> Either<? super B> fmap(Function<Object, B> f) {
		return this;
	}

	@Override
	public RightType undo() {
		return value;
	}
}