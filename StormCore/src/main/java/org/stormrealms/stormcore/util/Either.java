package org.stormrealms.stormcore.util;

import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<A, R> extends Monad<A> {
    @Override
    public Either<? super A, ? super R> pure(A value) {
        return Left.of(value);
    }

    public static <L, R> Either<? super L, ? super R> when(boolean b, Supplier<L> f, Supplier<R> g) {
		return b ? Left.of(f.get()) : Right.of(g.get());
	}

	public abstract <T> T match(Function<A, T> left, Function<R, T> right);
}