package org.stormrealms.stormcore.util;

public class Unit {
	private static Unit instance = new Unit();

	private Unit() { }

	public static Unit it() {
		return instance;
	}
}