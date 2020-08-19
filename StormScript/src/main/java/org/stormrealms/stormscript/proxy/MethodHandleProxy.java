package org.stormrealms.stormscript.proxy;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.stormrealms.stormcore.util.Fn;
import org.stormrealms.stormcore.util.IterableM;

public abstract class MethodHandleProxy implements ProxyExecutable {
	protected abstract MethodHandle findMethodHandle(List<Class<?>> argumentTypes) throws NoSuchMethodException, IllegalAccessException;

	@Override
	public Object execute(Value... arguments) {
		var castedArguments = IterableM.of(arguments).map(a -> a.as(Object.class)).toArray(Object.class);

		List<Class<?>> argumentTypes = new ArrayList<>(arguments.length);
		IterableM.of(arguments).map(a -> a.as(Object.class)).forEach(a -> argumentTypes.add(a.getClass()));

		return Fn.forwardException(() -> findMethodHandle(argumentTypes).invokeWithArguments(castedArguments));
	}	
}