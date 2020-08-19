package org.stormrealms.stormcore.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Either<L, R> {
	public static <L, $R> Left<L, $R> left(L value) {
		return new Left<>(value);
	}

	public static <$L, R> Right<$L, R> right(R value) {
		return new Right<>(value);
	}

	public static <L, R> Either<L, R> when(boolean b, Supplier<L> f, Supplier<R> g) {
		return b ? left(f.get()) : right(g.get());
	}

	public static <S> Either<S, Throwable> leftOrCatch(SupplierThrows<S, Throwable> left) {
		try {
			return left(left.get());
		} catch (Throwable t) {
			return right(t);
		}
	}

	/* Functor (Either L) */
	<B> Either<B, R> fmap(Function<L, B> f);

	/* Applicative (Either L) */
	default <B, $> Either<B, R> apply(Left<Function<L, B>, $> f) {
		return this.fmap(f.value);
	}

	default <B, $> Either<L, R> apply(Right<Function<L, B>, $> f) {
		return this;
	}

	/* Monad (Either L) */
	<B> Either<B, R> bind(Function<L, Either<B, R>> f);

	default <B> Either<B, R> then(Supplier<Either<B, R>> m) {
		return this.bind($ -> m.get());
	}

	<B> B match(Function<L, B> left, Function<R, B> right);

	default Unit match(Consumer<L> left, Consumer<R> right) {
		return this.match(Fn.unit(left), Fn.unit(right));
	}
}