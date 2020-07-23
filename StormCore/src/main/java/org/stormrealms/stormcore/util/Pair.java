package org.stormrealms.stormcore.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pair<T, U> {
	private T key;
	private U value;
}
