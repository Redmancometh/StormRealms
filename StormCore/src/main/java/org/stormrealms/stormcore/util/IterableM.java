package org.stormrealms.stormcore.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class IterableM<A, T extends Iterator<A>> extends Monad<A> implements Filterable<A> {
	protected Iterator<A> iterator;

	protected IterableM(T iterator) {
		this.iterator = iterator;
	}

	protected IterableM(Iterable<A> iterable) {
		this.iterator = iterable.iterator();
	}

	public static <E> IterableM<E, Iterator<E>> of(Iterable<E> iterable) {
		return of(iterable.iterator());
	}

	public static <E> IterableM<E, Iterator<E>> of(E[] array) {
		return of(Arrays.asList(array).iterator());
	}

	public static <E> IterableM<E, Iterator<E>> of(Iterator<E> iterator) {
		return new IterableM<>(iterator);
	}

	@Override
	public <B> IterableM<B, Iterator<B>> fmap(Function<A, B> f) {
		return of(new Iterator<B>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public B next() {
				return f.apply(iterator.next());
			}
		});
	}

	@Override
	public A undo() {
		return iterator.next();
	}

	@Override
	public IterableM<A, Iterator<A>> pure(A value) {
		return of(Arrays.asList(value));
	}

	@Override
	public IterableM<A, Iterator<A>> filter(Function<A, Boolean> f) {
		return of(new Iterator<A>() {
			Maybe<A> lookAhead = tryNext();

			private Maybe<A> tryNext() {
				return Maybe.when(iterator.hasNext(), () -> iterator.next());
			}

			@Override
			public boolean hasNext() {
				return lookAhead.match($ -> true, () -> false);
			}

			@Override
			public A next() {
				var result = lookAhead;
				lookAhead = tryNext();

				return result.matchOrThrow(a -> a, () -> new NoSuchElementException("Attempted to call next on an empty Iterator."));
			}
		});
	}

	@Override
	public <B> IterableM<B, Iterator<B>> bind(Function<A, Monad<B>> f) {
		return this.fmap(a -> f.apply(a).undo());
	}

	@Override
	public <B> IterableM<B, Iterator<B>> apply(Applicative<Function<A, B>> f) {
		return this.fmap(f.undo());
	}
}