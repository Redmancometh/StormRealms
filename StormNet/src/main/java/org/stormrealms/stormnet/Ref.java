package org.stormrealms.stormnet;

public class Ref<T> {
    protected T value;

    private Ref(T value) {
        this.value = value;
    }

    public static <U> Ref<U> toNull() {
        return new Ref<U>(null);
    }

    public static <U> Ref<U> to(U value) {
        return new Ref<U>(value);
    }

    public T assign(T value) {
        this.value = value;
        return value;
    }

    public T deref() {
        return value;
    }
}
