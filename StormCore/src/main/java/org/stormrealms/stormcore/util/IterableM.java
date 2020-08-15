package org.stormrealms.stormcore.util;

import static org.stormrealms.stormcore.util.Fn.doWhileMaybe;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.Getter;

public class IterableM<A> {
	@Getter(value = AccessLevel.PROTECTED)
	private Iterator<A> iterator;

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
		this.forEach(e -> result.add(e));
		return result;
	}

	public <B> IterableM<B> fmap(Function<A, B> f) {
		return of(new MapIterator<>(f));
	}

	public <B> IterableM<B> map(Function<A, B> f) {
		return this.fmap(f);
	}

	public boolean isEmpty() {
		return !iterator.hasNext();
	}

	public <B> IterableM<B> bind(Function<A, IterableM<B>> f) {
		var nestedIterable = this.fmap(f);
		var nestedIterator = nestedIterable.fmap(it -> it.getIterator()).getIterator();
		return of(new BindIterator<>(nestedIterator));
	}

	public <B> IterableM<B> apply(IterableM<Function<A, B>> it) {
		return it.bind(f -> this.fmap(a -> f.apply(a)));
	}

	public IterableM<A> filter(Predicate<A> p) {
		return this.bind(a -> p.test(a) ? IterableM.of(a) : IterableM.of());
	}

	protected <B> Supplier<Maybe<B>> maybeIterator(Iterator<B> iterator) {
		return () -> Maybe.when(iterator.hasNext(), iterator::next);
	}

	public void forEach(Consumer<A> f) {
		while(iterator.hasNext()) {
			f.accept(iterator.next());
		}
	}

	class MapIterator<B> implements Iterator<B> {
		Function<A, B> f;

		public MapIterator(Function<A, B> f) {
			this.f = f;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public B next() {
			return f.apply(iterator.next());
		}
	}

	class BindIterator<B> implements Iterator<B> {
		Supplier<Maybe<Iterator<B>>> nextIterator;
		Maybe<Iterator<B>> maybeCurrentIterator = Maybe.none();

		public BindIterator(Iterator<Iterator<B>> iterOfIters) {
			this.nextIterator = maybeIterator(iterOfIters);
			searchNonEmptyIterator();
		}

		// Search for a non-empty iterator if our current iterator is empty.
		void searchNonEmptyIterator() {
			maybeCurrentIterator = maybeCurrentIterator.orElse(() ->
				doWhileMaybe(
					nextIterator,
					it -> !it.hasNext()));
		}

		// Check if there are any iterators that are non-empty
		@Override
		public boolean hasNext() {
			// If the current iterator is empty, try to advance to a non-empty one.
			searchNonEmptyIterator();
			return maybeCurrentIterator.isJust();
		}

		@Override
		public B next() {
			// Try to ensure that we have a non-empty iterator to pull from.
			searchNonEmptyIterator();

			return maybeCurrentIterator.matchOrThrow(current -> {
				B result = current.next();

				// If current iterator is finished, set it to None to represent that a new
				// iterator must be searched for.
				if(!current.hasNext()) maybeCurrentIterator = Maybe.none();

				return result;
			}, () ->
				new NoSuchElementException("Attempted to iterate on an empty iterator."));
		}
	}
}