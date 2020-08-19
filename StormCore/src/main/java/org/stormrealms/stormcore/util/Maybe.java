package org.stormrealms.stormcore.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface Maybe<A> {
	public static <T> Just<T> just(T value) {
		return new Just<>(value);
	}

	public static <T> None<T> none() {
		return new None<>();
	}

	/* Functor Maybe */
	<B> Maybe<B> fmap(Function<A, B> f);

	/* Applicative Maybe */
	default <B> Maybe<B> apply(Just<Function<A, B>> f) {
		return this.fmap(f.value);
	}

	default <B> None<B> apply(None<Function<A, B>> f) {
		return none();
	}

	/* Monad Maybe */
	<B> Maybe<B> bind(Function<A, Maybe<B>> f);

	default <B> Maybe<B> then(Supplier<Maybe<B>> m) {
		return this.bind($ -> m.get());
	}

	public static <T> Maybe<T> when(boolean b, Supplier<T> f) {
		return b ? just(f.get()) : none();
	}

	public static <T> Maybe<T> notNull(Supplier<T> value) {
		return Maybe.when(value != null, value);
	}

	boolean isJust();

	default boolean isNone() {
		return !isJust();
	}

	<B> B match(Function<A, B> just, Supplier<B> none);

	default Maybe<A> orElse(Supplier<Maybe<A>> f) {
		return this.match(a -> just(a), f);
	}

	default Unit match(Consumer<A> just, Runnable none) {
		return this.match(Fn.unit(just), Fn.unit(none));
	}

	<B, U extends Throwable> B matchOrThrow(Function<A, B> just, Supplier<U> throwable) throws U;

	default Maybe<A> whilePred(Predicate<A> p, Function<A, Maybe<A>> f) {
		var maybeCurrent = this;

		while(maybeCurrent.match(current -> p.test(current), () -> false)) {
			maybeCurrent = maybeCurrent.bind(f);
		}

		return maybeCurrent;
	}

	default Maybe<A> doWhile(Function<A, Maybe<A>> f, Predicate<A> p) {
		var maybeCurrent = this;

		do {
			maybeCurrent = maybeCurrent.bind(f);
		} while(maybeCurrent.match(current -> p.test(current), () -> false));

		return maybeCurrent;
	}

	default Maybe<A> doWhile(Supplier<Maybe<A>> f, Predicate<A> p) {
		var maybeCurrent = this;

		do {
			maybeCurrent = maybeCurrent.bind($ -> f.get());
		} while(maybeCurrent.match(p::test, () -> false));

		return maybeCurrent;
	}

	default Maybe<A> filter(Predicate<A> p) {
		return this.bind(a -> p.test(a) ? just(a) : none());
	}
}