package org.stormrealms.stormcore.util;

import java.util.function.Function;

public class Left<A> extends Either<A, A> {
	protected A value;

	protected Left(A value) {
		this.value = value;
	}

	public static <L, R> Left<L> of(L value) {
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
	public <B> Monad<B> bind(Function<A, Monad<B>> f) {
		return f.apply(this.undo());
	}

	@Override
	public <B> Applicative<? super B> apply(Applicative<Function<A, B>> f) {
		return this.fmap(f.undo());
	}

	@Override
	public <B> IterableM<B> flat() {
		return Monad.<B>cast(value).match((Monad<B> m) -> m.flat(), (B v) -> IterableM.of(v));
	}

    @Override
    public <B> B match(Function<A, B> left, Function<A, B> $) {
        return left.apply(value);
    }
}