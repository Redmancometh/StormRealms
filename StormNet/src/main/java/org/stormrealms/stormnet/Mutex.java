package org.stormrealms.stormnet;

import java.util.concurrent.locks.ReentrantLock;

public class Mutex {
    ReentrantLock lock = new ReentrantLock();

    public void enter(Runnable func) {
        lock.lock();
        func.run();
        lock.unlock();
    }
}
