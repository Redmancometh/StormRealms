package org.stormrealms.stormcore.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import lombok.Getter;

import static org.stormrealms.stormcore.util.Fn.*;

public class IterableM<A> extends Monad<A> implements Filterable<A> {
	@Getter
	protected Iterator<A> iterator;

	protected IterableM(Iterator<A> iterator) {
		this.iterator = iterator;
	}

	protected IterableM(Iterable<A> iterable) {
		this.iterator = iterable.iterator();
	}

	public static <E> IterableM<E> of(Iterable<E> iterable) {
		return of(iterable.iterator());
	}

	@SafeVarargs
	public static <E> IterableM<E> of(E... array) {
		return of(Arrays.asList(array).iterator());
	}

	public static <E> IterableM<E> of(Iterator<E> iterator) {
		return new IterableM<>(iterator);
	}

	public A[] toArray(Class<A> componentClass) {
		var list = this.toList();

		// Thanks Java
		@SuppressWarnings("unchecked")
		var array = list.toArray((A[]) Array.newInstance(componentClass, list.size()));

		return array;
	}

	public List<A> toList() {
		var result = new ArrayList<A>();
		this.fmap(unit(e -> result.add(e)));
		return result;
	}

	@Override
	public <B> IterableM<B> fmap(Function<A, B> f) {
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
	public <B> IterableM<B> flat() {
		return of(new Iterator<B>() {
			Maybe<Iterator<B>> currentIterator = tryNext();

			// Get the next element from the outter iterator, and convert it to a monadic
			// value if necessary.
			private Maybe<Iterator<B>> tryNext() {
				return Maybe.when(iterator.hasNext(), () -> iterator.next())
					.fmap(Monad::<B>cast)
					.fmap(a -> a.match((Monad<B> m) -> m.<B>flat(), (B v) -> IterableM.of(v)))
					.fmap(IterableM::getIterator);
			}

			// Check if the current iterator is finished. If so, try to get the next one. If
			// this fails, then this iterator is done.
			@Override
			public boolean hasNext() {
				return currentIterator.match(
					m -> m.hasNext(),

					() -> {
						currentIterator = tryNext();
						return currentIterator.isJust();
					});
			}

			@Override
			public B next() {
				return currentIterator.matchOrThrow(
					Iterator::next,
					() -> new NoSuchElementException("Attempted to call next on an empty Iterator."));
			}
		});
	}

	@Override
	public A undo() {
		return iterator.next();
	}

	@Override
	public IterableM<A> pure(A value) {
		return of(Arrays.asList(value));
	}

	@Override
	public IterableM<A> filter(Function<A, Boolean> f) {
		return this.fmap(e -> Maybe.when(f.apply(e), () -> e)).flat();
	}

	@Override
	public <B> IterableM<B> bind(Function<A, Monad<B>> f) {
		return this.fmap(a -> f.apply(a).undo());
	}

	@Override
	public <B> IterableM<B> apply(Applicative<Function<A, B>> f) {
		return this.fmap(f.undo());
	}
}