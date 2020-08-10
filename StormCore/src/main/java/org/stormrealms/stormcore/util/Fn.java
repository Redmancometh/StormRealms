package org.stormrealms.stormcore.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Fn {
	public static <T> T id(T x) {
		return x;
	}
	
	public static <A, B> Function<A, B> always(Supplier<B> f) {
		return a -> f.get();
	}

	public static <A> Consumer<A> discard(Runnable f) {
		return a -> f.run();
	}

	public static <A, B> BiConsumer<A, B> discard(Consumer<B> f) {
		return (a, b) -> f.accept(b);
	}

	public static <A, B, C> TriConsumer<A, B, C> discard(BiConsumer<B, C> f) {
		return (a, b, c) -> f.accept(b, c);
	}

	public static <A> Supplier<Unit> unit(Runnable f) {
		return () -> {
			f.run();
			return Unit.it();
		};
	}

	public static <A> Function<A, Unit> unit(Consumer<A> f) {
		return a -> {
			f.accept(a);
			return Unit.it();
		};
	}

	public static <A> Runnable discardResult(Supplier<A> f) {
		return () -> f.get();
	}

	public static <A, B> Supplier<B> partial(Function<A, B> f, Supplier<A> a) {
		return () -> f.apply(a.get());
	}

	public static <A, B> Supplier<B> partial(Function<A, B> f, A a) {
		return () -> f.apply(a);
	}

	public static <A, B, C> Function<B, C> partial(BiFunction<A, B, C> f, Supplier<A> a) {
		return (b) -> f.apply(a.get(), b);
	}

	public static <A, B, C> Function<B, C> partial(BiFunction<A, B, C> f, A a) {
		return (b) -> f.apply(a, b);
	}

	public static <A, B, C, D> BiFunction<B, C, D> partial(TriFunction<A, B, C, D> f, Supplier<A> a) {
		return (b, c) -> f.apply(a.get(), b, c);
	}

	public static <A, B, C, D> BiFunction<B, C, D> partial(TriFunction<A, B, C, D> f, A a) {
		return (b, c) -> f.apply(a, b, c);
	}

	public static <A, B, C> Function<A, Function<B, C>> curry(BiFunction<A, B, C> f) {
		return a -> b -> f.apply(a, b);
	}

	public static <A, B, C, D> Function<A, Function<B, Function<C, D>>> curry(TriFunction<A, B, C, D> f) {
		return a -> b -> c -> f.apply(a, b, c);
	}

	public static <A, B, C> BiFunction<A, B, C> uncurryBi(Function<A, Function<B, C>> f) {
		return (a, b) -> f.apply(a).apply(b);
	}

	public static <A, B, C, D> TriFunction<A, B, C, D> uncurryTri(Function<A, Function<B, Function<C, D>>> f) {
		return (a, b, c) -> f.apply(a).apply(b).apply(c);
	}

	public static <A> A whilePred(A seed, Function<A, Boolean> p, Function<A, A> f) {
		while(p.apply(seed)) {
			seed = f.apply(seed);
		}

		return seed;
	}

	public static <A> A doWhile(A seed, Function<A, A> f, Function<A, Boolean> p) {
		do {
			seed = f.apply(seed);
		} while(p.apply(seed));

		return seed;
	}

	public static <A> A doWhile(Supplier<A> f, Function<A, Boolean> p) {
		A current = null;

		do {
			current = f.get();
		} while(p.apply(current));

		return current;
	}

	public static <A> Maybe<A> doWhileMaybe(Supplier<Maybe<A>> f, Function<A, Boolean> p) {
		Maybe<A> maybeCurrent = Maybe.just(null);

		do {
			maybeCurrent = maybeCurrent.bind($ -> f.get());
		} while(maybeCurrent.match(current -> p.apply(current), () -> false));

		return maybeCurrent;
	}

	public static <A, B, C> Function<A, C> compose(Function<A, B> f, Function<B, C> g) {
		return a -> g.apply(f.apply(a));
	}
}