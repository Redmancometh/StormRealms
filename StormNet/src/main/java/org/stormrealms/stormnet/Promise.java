package org.stormrealms.stormnet;

import java.util.function.Consumer;
import java.util.function.Function;

public class Promise<T> {
    protected Consumer<T> callback = (x) -> { };

    public Promise<T> then(Consumer<T> callback) {
        this.callback = callback;
        return this;
    }

    class Ref<U> { public U value; }
    public <U> Promise<U> then(Function<T, Promise<U>> chainedCallback) {
        Ref<Promise<U>> nextPromise = new Ref<Promise<U>>();

        this.callback = (T response) -> {
            nextPromise.value = chainedCallback.apply(response);
        };

        return nextPromise.value;
    }

    public void fulfill(T response) {
        if(callback == null) return;
        callback.accept(response);
    }
}
