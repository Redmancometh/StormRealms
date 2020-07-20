package org.stormrealms.stormscript.test;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.stormrealms.stormscript.engine.ScriptManager;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Unit test for simple App.
 */
public class StormScriptTest {
    @Data
    @AllArgsConstructor
    class HostObject {
        private int x;
        private int y;
    }

    @Test
    public void graalJS() {
        var context = Context.create("js");
        // .engine(Engine.create().newBuilder().option(key, value));
        var bindings = context.getBindings("js");
        bindings.putMember("foo", 1000);
        assert context.eval("js", "foo").asInt() == 1000;

        bindings.putMember("hostObject", new HostObject(7, 77));
        assert context.eval("js", "hostObject").asHostObject().equals(new HostObject(7, 77));
    }

    @Test
    public void scriptManagerTest() {
        var context = Context.newBuilder("js").allowAllAccess(true).build();
        var bindings = context.getBindings("js");
        bindings.putMember("println", (Consumer<Object>) System.out::println);
        bindings.putMember("echo", (BiConsumer<String, Consumer<String>>) (echo, callback) -> {
            callback.accept(echo);
        });

        context.eval("js", "echo('hello!', println)");
    }

    @Test
    public void engineOptions() {
        var engine = Engine.create();

        System.out.printf(
            "Engine implementation: %s\n" +
            "Engine instruments:\n%s" +
            "Engine options:\n%s",
            
            engine.getImplementationName(),

            engine.getInstruments().entrySet().stream().map(entry ->
                String.format(" -  %s: %s\n", entry.getKey(), entry.getValue())
            ).reduce(String::concat).get(),
            
            engine.getLanguages().entrySet().stream().map(entry ->
                String.format(" -  %s: %s\n", entry.getKey(), entry.getValue().getId())
            ).reduce(String::concat).get(),

            StreamSupport.stream(engine.getOptions().spliterator(), false).map(option ->
                String.format("%s.%s: %s", option.getCategory(), option.getName(), option.getHelp())
            ).reduce(String::concat).get()
        );
    }
}
