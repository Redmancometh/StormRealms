package org.stormrealms.stormcore.util;

import java.util.function.Function;

public abstract class Applicative<A> implements Functor<A> {
	public abstract Applicative<? super A> pure(A value);

	public abstract <B> Applicative<? super B> apply(Applicative<Function<A, B>> f);

	/* public <B, C> Applicative<? super C> liftA2(Applicative<B> operand, BiFunction<A, B, C> operator) {
		var mapping = fmap(Fn.curry(operator));
		return operand.apply(mapping);
	} */
}