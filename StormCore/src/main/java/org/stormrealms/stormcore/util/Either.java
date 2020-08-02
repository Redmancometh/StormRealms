package org.stormrealms.stormcore.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<A, R> extends Monad<A> {
	@Override
	public Either<A, Object> pure(A value) {
		return Left.of(value);
	}

	// NOTE(Yevano): We know this is fine, because it is not possible to reference
	// the incorrect type outside of our interfaces.
	@SuppressWarnings("unchecked")
	private static <L, R> Either<L, R> leftOf(L value) {
		return (Either<L, R>) Left.of(value);
	}

	@SuppressWarnings("unchecked")
	private static <L, R> Either<L, R> rightOf(R value) {
		return (Either<L, R>) Right.of(value);
	}

	public static <L, R> Either<L, R> when(boolean b, Supplier<L> f, Supplier<R> g) {
		return b ? leftOf(f.get()) : rightOf(g.get());
	}

	public static <S> Either<S, Throwable> leftOrCatch(SupplierThrows<S, Throwable> left) {
		try {
			return leftOf(left.get());
		} catch (Throwable t) {
			return rightOf(t);
		}
	}

	public abstract <T> T match(Function<A, T> left, Function<R, T> right);

	public Unit match(Consumer<A> left, Consumer<R> right) {
		return this.match(Fn.unit(left), Fn.unit(right));
	}
}