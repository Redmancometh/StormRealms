package org.stormrealms.stormscript.api;

import org.stormrealms.stormscript.engine.Script;
import org.graalvm.polyglot.Value;

public class ImportAPI extends ScriptAPI {
	public ImportAPI(Script script) {
		super(script);
	}

	@ScriptFunction(memberName = "importJavaAs")
	public void importJavaAs(Value fullName_, Value importName_) throws ScriptImportException {
		var fullName = fullName_.asString();
		var importName = importName_.asString();
		Class<?> importClass = null;

		try {
			importClass = Class.forName(fullName);
		} catch (ClassNotFoundException e) {
			throw new ScriptImportException(script, fullName);
		}

		script.getGlobalObject().putMember(importName, importClass);
	}

	@ScriptFunction(memberName = "importJava")
	public void importJava(Value fullName_) throws ScriptImportException {
		var fullName = fullName_.asString();
		Class<?> importClass = null;

		try {
			importClass = Class.forName(fullName);
		} catch (ClassNotFoundException e) {
			throw new ScriptImportException(script, fullName);
		}

		script.getGlobalObject().putMember(importClass.getSimpleName(), importClass);
	}
}