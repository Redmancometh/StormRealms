package org.stormrealms.stormnet;

import java.util.function.Consumer;

public class PacketSubscription<T> extends Promise<T> {
    private boolean isCancelled = false;

    @Override
    public Promise<T> then(Consumer<T> callback) {
        super.callback = callback;
        return this;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    public void unsubscribe() {
        isCancelled = true;
    }
}
