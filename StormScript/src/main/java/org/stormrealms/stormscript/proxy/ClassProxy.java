package org.stormrealms.stormscript.proxy;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.stormrealms.stormcore.util.IterableM;

import lombok.Getter;

public class ClassProxy<T> implements ProxyExecutable, ProxyObject {
	@Getter
	private Class<T> hostClass;

	private MethodHandles.Lookup lookup;
	private Map<String, VarHandle> fieldByName = new HashMap<>();
	private Map<String, Method[]> methodsByName = new HashMap<>();

	private boolean checkFlags(int value, int flags) {
		return (value & flags) != 0;
	}

	public ClassProxy(Class<T> hostClass) {
		this.hostClass = hostClass;
		lookup = MethodHandles.lookup().in(hostClass);

		var publicStaticFields = IterableM.of(hostClass.getFields())
				.filter(field -> checkFlags(field.getModifiers(), Modifier.STATIC | Modifier.PUBLIC));

		publicStaticFields.forEach(field -> {
			try {
				fieldByName.put(field.getName(), lookup.unreflectVarHandle(field));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public Object getMember(String key) {
		if (fieldByName.containsKey(key)) {
			return fieldByName.get(key).get();
		}

		return null;
	}

	@Override
	public Object getMemberKeys() {
		return fieldByName.keySet().toArray();
	}

	@Override
	public boolean hasMember(String key) {
		return fieldByName.containsKey(key);
	}

	@Override
	public void putMember(String key, Value value) {
		if (fieldByName.containsKey(key)) {
			var fieldHandle = fieldByName.get(key);
			var fieldType = fieldHandle.varType();
			fieldHandle.set(value.as(fieldType));
		} else {
			throw new RuntimeException(
				String.format("No accessible static member exists for class %s.", hostClass.getName()));
		}
	}

	@Override
	public Object execute(Value... arguments) {
		var castedArguments = IterableM.of(arguments)
			.map(a -> a.as(Object.class))
			.toArray(Object.class);

		List<Class<?>> argumentTypes = new ArrayList<>(arguments.length);
		IterableM.of(arguments)
			.map(a -> a.as(Object.class))
			.forEach(a -> argumentTypes.add(a.getClass()));

		try {
			var constructor = lookup.findConstructor(hostClass, MethodType.methodType(hostClass, argumentTypes));
			return constructor.invokeWithArguments(castedArguments);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}	
}