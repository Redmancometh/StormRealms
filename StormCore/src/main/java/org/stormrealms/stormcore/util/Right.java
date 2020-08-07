package org.stormrealms.stormcore.util;

import java.util.function.Function;

public class Right<A> extends Either<A, A> {
	protected A value;

	protected Right(A value) {
		this.value = value;
	}

	public static <L, R> Right<R> of(R value) {
		return new Right<>(value);
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
		return (Applicative<? super B>) this.fmap(f.undo());
	}

    @Override
    public <B> Right<? super B> fmap(Function<A, B> f) {
        return Right.of(f.apply(this.undo()));
    }

    @Override
    public <B> B match(Function<A, B> $, Function<A, B> right) {
        return right.apply(value);
    }

	@Override
	public <B> IterableM<B> flat() {
		// TODO Auto-generated method stub
		return null;
	}
}