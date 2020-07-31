package org.stormrealms.stormcore.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;

public class IterableM<A, T extends Iterator<A>> implements Monad<A>, Filterable<A> {
    protected T iterator;

    protected IterableM(T iterator) {
        this.iterator = iterator;
    }

    public static <E> IterableM<E, Iterator<E>> of(Iterable<E> iterable) {
        return of(iterable.iterator());
    }

    public static <E> IterableM<E, Iterator<E>> of(Iterator<E> iterator) {
        return new IterableM<>(iterator);
    }

    @Override
    public <B> IterableM<? super B, ? extends Iterator<B>> fmap(Function<A, B> f) {
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
    public <U> IterableM<U, Iterator<U>> pure(U value) {
        return of(Arrays.asList(value));
    }

    @Override
    public IterableM<? super A, ? extends Iterator<A>> filter(Function<A, Boolean> f) {
        return of(new Iterator<A>() {
            A lookAhead = Just.of(iterator);

            @Override
            public boolean hasNext() {
                return iterator.hasNext() && f.apply(iterator.next());
            }

            @Override
            public A next() {
                //return f.apply(iterator.next());
                var a = iterator.next();

                if(a instanceof Filterable) {

                }
            }
        });
    }
}