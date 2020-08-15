package org.stormrealms.stormcore.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Maybe<A> {
	public static <T> Just<T> just(T value) {
		return new Just<>(value);
	}

	public static <T> None<T> none() {
		return new None<T>();
	}

	/* Functor Maybe */
	public abstract <B> Maybe<B> fmap(Function<A, B> f);

	/* Applicative Maybe */
	public <B> Maybe<B> apply(Just<Function<A, B>> f) {
		return this.fmap(f.value);
	}

	public <B> None<B> apply(None<Function<A, B>> f) {
		return none();
	}

	/* Monad Maybe */
	public abstract <B> Maybe<B> bind(Function<A, Maybe<B>> f);

	public <B> Maybe<B> then(Supplier<Maybe<B>> m) {
		return this.bind($ -> m.get());
	}

	public static <T> Maybe<T> when(boolean b, Supplier<T> f) {
		return b ? just(f.get()) : none();
	}

	public static <T> Maybe<T> notNull(Supplier<T> value) {
		return Maybe.when(value != null, value);
	}

	public abstract boolean isJust();

	public boolean isNone() {
		return !isJust();
	}

	public abstract <B> B match(Function<A, B> just, Supplier<B> none);

	public Maybe<A> orElse(Supplier<Maybe<A>> f) {
		return this.match(a -> just(a), f);
	}

	public Unit match(Consumer<A> just, Runnable none) {
		return this.match(Fn.unit(just), Fn.<A>unit(none));
	}

	public abstract <B, U extends Throwable> B matchOrThrow(Function<A, B> just, Supplier<U> throwable) throws U;

	public Maybe<A> whilePred(Function<A, Boolean> p, Function<A, Maybe<A>> f) {
		var maybeCurrent = this;

		while(maybeCurrent.match(current -> p.apply(current), () -> false)) {
			maybeCurrent = maybeCurrent.bind(f);
		}

		return maybeCurrent;
	}

	public Maybe<A> doWhile(Function<A, Maybe<A>> f, Function<A, Boolean> p) {
		var maybeCurrent = this;

		do {
			maybeCurrent = maybeCurrent.bind(f);
		} while(maybeCurrent.match(current -> p.apply(current), () -> false));

		return maybeCurrent;
	}

	public Maybe<A> doWhile(Supplier<Maybe<A>> f, Function<A, Boolean> p) {
		var maybeCurrent = this;

		do {
			maybeCurrent = maybeCurrent.bind($ -> f.get());
		} while(maybeCurrent.match(current -> p.apply(current), () -> false));

		return maybeCurrent;
	}

	public Maybe<A> filter(Function<A, Boolean> p) {
		return this.bind(a -> p.apply(a) ? just(a) : none());
	}
}