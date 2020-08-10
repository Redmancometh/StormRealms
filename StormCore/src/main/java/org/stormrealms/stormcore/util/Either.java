package org.stormrealms.stormcore.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Either<L, R> {
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
	public abstract <B> Either<B, R> fmap(Function<L, B> f);

	/* Applicative (Either L) */
	public <B, $> Either<B, R> apply(Left<Function<L, B>, $> f) {
		return this.fmap(f.value);
	}

	public <B, $> Either<L, R> apply(Right<Function<L, B>, $> f) {
		return this;
	}

	/* Monad (Either L) */
	public abstract <B> Either<? super B, R> bind(Function<L, Either<B, R>> f);

	public <B> Either<? super B, R> then(Supplier<Either<B, R>> m) {
		return this.bind($ -> m.get());
	}

	public abstract <B> B match(Function<L, B> left, Function<R, B> right);

	public Unit match(Consumer<L> left, Consumer<R> right) {
		return this.match(Fn.unit(left), Fn.unit(right));
	}
}