package org.stormrealms.stormcore.util;

import java.util.function.BiFunction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

public class Tuple {
	public static <T1, T2> Of2<T1, T2> of(T1 t1, T2 t2) {
		return new Of2<>(t1, t2);
	}

	public static <T1, T2, T3> Of3<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {
		return new Of3<>(t1, t2, t3);
	}

	public static <T1, T2, T3, T4> Of4<T1, T2, T3, T4> of(T1 t1, T2 t2, T3 t3, T4 t4) {
		return new Of4<>(t1, t2, t3, t4);
	}

	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Of2<T1, T2> {
		private T1 t1;
		private T2 t2;

		public <R> R match(BiFunction<T1, T2, R> f) {
			return f.apply(t1, t2);
		}

		public <T3> Of3<T1, T2, T3> and(T3 t3) {
			return of(t1, t2, t3);
		}
	}

	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Of3<T1, T2, T3> {
		private T1 t1;
		private T2 t2;
		private T3 t3;

		public <R> R match(TriFunction<T1, T2, T3, R> f) {
			return f.apply(t1, t2, t3);
		}

		public <T4> Of4<T1, T2, T3, T4> and(T4 t4) {
			return of(t1, t2, t3, t4);
		}
	}

	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	public static class Of4<T1, T2, T3, T4> {
		private T1 t1;
		private T2 t2;
		private T3 t3;
		private T4 t4;

		public <R> R match(QuadFunction<T1, T2, T3, T4, R> f) {
			return f.apply(t1, t2, t3, t4);
		}
	}
}