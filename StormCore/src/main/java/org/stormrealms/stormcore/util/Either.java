package org.stormrealms.stormcore.util;

public interface Either<A> extends Monad<A> {
	@Override
	default <T> Applicative<T> pure(T value) {
		return Left.of(value);
	}
}