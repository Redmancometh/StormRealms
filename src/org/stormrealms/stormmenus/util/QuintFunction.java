package org.stormrealms.stormmenus.util;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface QuintFunction<T, U, V, X, Y, R>
{

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <W>   the type of output of the {@code after} function, and of the
     *              composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <W> QuintFunction<T, U, V, X, Y, W> andThen(Function<? super R, ? extends W> after)
    {
        Objects.requireNonNull(after);
        return (T t, U u, V v, X x, Y y) -> after.apply(apply(t, u, v, x, y));
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param u the second function argument
     * @param v the third function argument
     * @return the function result
     */
    R apply(T t, U u, V v, X x, Y y);
}