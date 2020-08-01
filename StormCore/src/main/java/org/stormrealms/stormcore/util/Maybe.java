package org.stormrealms.stormcore.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Maybe<A> extends Monad<A> implements Filterable<A> {
	public static <T> None<T> none() {
		return new None<T>();
	}

	public static <T> Maybe<T> when(boolean b, Supplier<T> f) {
		return b ? Just.of(f.get()) : none();
	}

	public static <T> Maybe<T> when(boolean b, T f) {
		return b ? Just.of(f) : none();
	}

	public abstract <T> T match(Function<A, T> just, Supplier<T> none);

	public Unit match(Consumer<A> just, Runnable none) {
		return this.match(Fn.unit(just), Fn.<A>unit(none));
	}

	public abstract <T, U extends Throwable> T matchOrThrow(Function<A, T> just, Supplier<U> throwable) throws U;
}