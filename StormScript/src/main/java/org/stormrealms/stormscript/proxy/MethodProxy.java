package org.stormrealms.stormscript.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;

public class MethodProxy<T> extends MethodHandleProxy {
	private Class<T> hostClass;
	private String methodName;
	private MethodHandles.Lookup lookup;

	public MethodProxy(Class<T> hostClass, String methodName) {
		this.hostClass = hostClass;
		this.methodName = methodName;
		lookup = MethodHandles.lookup().in(hostClass);
	}

	@Override
	protected MethodHandle findMethodHandle(List<Class<?>> argumentTypes)
		throws NoSuchMethodException, IllegalAccessException
	{
		return lookup.findSpecial(
			hostClass,
			methodName,
			MethodType.methodType(hostClass, argumentTypes),
			MethodProxy.class);
	}
}