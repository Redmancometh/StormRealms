package org.stormrealms.stormscript.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.springframework.stereotype.Component;

import org.stormrealms.stormscript.engine.Script;

class APISpec<T> {
	private Map<String, Method> apiMethods = new HashMap<>();

	protected APISpec(Class<T> class_) {
		for (var declaredMethod : class_.getDeclaredMethods()) {
			var annotation = declaredMethod.getAnnotation(ScriptFunction.class);

			if (annotation != null) {
				apiMethods.put(annotation.memberName(), declaredMethod);
			}
		}
	}

	public static <U> APISpec<U> of(Class<U> apiClass) {
		return new APISpec<>(apiClass);
	}

	public ProxyExecutable getProxy(ScriptAPI api, String functionName) {
		var method = apiMethods.get(functionName);
		if(method == null) throw new RuntimeException("No such API function exists.");

		return args -> {
			Value result = null;

			try {
				method.invoke(api, (Object[]) args);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}

			return result;
		};
	}

	public Set<Map.Entry<String, Method>> getAPIEntries() {
		return apiMethods.entrySet();
	}
}

@Component
public class APIManager {
	private Map<Class<?>, APISpec<?>> apiMap = new HashMap<>();

	private APISpec<?> getAPISpec(Class<?> apiClass) {
		var spec = apiMap.get(apiClass);

		if(spec == null) {
			spec = APISpec.of(apiClass);
			apiMap.put(apiClass, spec);
		}

		return spec;
	}

	public ProxyExecutable getAPIFunction(ScriptAPI api, String functionName) {
		var spec = getAPISpec(api.getClass());
		return spec.getProxy(api, functionName);
	}

	public void bindAPI(ScriptAPI api, Script script) {
		var apiClass = api.getClass();
		var spec = getAPISpec(apiClass);

		for(var apiEntry : spec.getAPIEntries()) {
			var ident = apiEntry.getKey();
			script.getGlobalObject().putMember(ident, spec.getProxy(api, ident));
		}
	}
}