package org.stormrealms.stormcore.util;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Fn {
	public static <T> T id(T x) {
		return x;
	}
	
	public static <A, B> Function<A, B> always(Supplier<B> f) {
		return a -> f.get();
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
}