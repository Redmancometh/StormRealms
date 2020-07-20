package org.stormrealms.stormcore.util;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.springframework.data.util.Pair;

public class StreamExtensions {
	public static <L, R, Z> Stream<Z> zip(Stream<Pair<L, R>> it, BiFunction<L, R, Z> mapping) {
		return it.map(pair -> mapping.apply(pair.getFirst(), pair.getSecond()));
	}

	public static <L, R> void zipForEach(Stream<Pair<L, R>> it, BiConsumer<L, R> each) {
		it.forEach(pair -> each.accept(pair.getFirst(), pair.getSecond()));
	}
}