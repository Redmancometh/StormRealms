package net.minecraft.server;

import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class IAsyncTaskHandler<R extends Runnable> implements Mailbox<R>, Executor {

    private final String b;
    private static final Logger LOGGER = LogManager.getLogger();
    private final Queue<R> d = Queues.newConcurrentLinkedQueue();
    private int e;

    protected IAsyncTaskHandler(String s) {
        this.b = s;
    }

    protected abstract R postToMainThread(Runnable runnable);

    protected abstract boolean canExecute(R r0);

    public boolean isMainThread() {
        return Thread.currentThread() == this.getThread();
    }

    protected abstract Thread getThread();

    protected boolean isNotMainThread() {
        return !this.isMainThread();
    }

    public int bh() {
        return this.d.size();
    }

    @Override
    public String bi() {
        return this.b;
    }

    private CompletableFuture<Void> executeFuture(Runnable runnable) {
        return CompletableFuture.supplyAsync(() -> {
            runnable.run();
            return null;
        }, this);
    }

    public CompletableFuture<Void> f(Runnable runnable) {
        if (this.isNotMainThread()) {
            return this.executeFuture(runnable);
        } else {
            runnable.run();
            return CompletableFuture.completedFuture(null); // Paper - decompile fix
        }
    }

    public void executeSync(Runnable runnable) {
        if (!this.isMainThread()) {
            this.executeFuture(runnable).join();
        } else {
            runnable.run();
        }

    }

    // Paper start
    public void scheduleOnMain(Runnable r0) {
        // postToMainThread does not work the same as older versions of mc
        // This method is actually used to create a TickTask, which can then be posted onto main
        this.addTask(this.postToMainThread(r0));
    }
    // Paper end

    public void addTask(R r0) { a(r0); }; // Paper - OBFHELPER
    public void a(R r0) {
        this.d.add(r0);
        LockSupport.unpark(this.getThread());
    }

    public void execute(Runnable runnable) {
        if (this.isNotMainThread()) {
            this.a(this.postToMainThread(runnable));
        } else {
            runnable.run();
        }

    }

    public void executeAll() { // Paper - protected -> public
        while (this.executeNext()) {
            ;
        }

    }

    protected boolean executeNext() {
        R r0 = this.d.peek(); // Paper - decompile fix

        if (r0 == null) {
            return false;
        } else if (this.e == 0 && !this.canExecute(r0)) {
            return false;
        } else {
            this.executeTask(this.d.remove()); // Paper - decompile fix
            return true;
        }
    }

    public void awaitTasks(BooleanSupplier booleansupplier) {
        ++this.e;

        try {
            while (!booleansupplier.getAsBoolean()) {
                if (!this.executeNext()) {
                    this.bl();
                }
            }
        } finally {
            --this.e;
        }

    }

    protected void bl() {
        Thread.yield();
        LockSupport.parkNanos("waiting for tasks", 100000L);
    }

    protected void executeTask(R r0) {
        try {
            r0.run();
        } catch (Exception exception) {
            if (exception.getCause() instanceof ThreadDeath) throw exception; // Paper
            IAsyncTaskHandler.LOGGER.fatal("Error executing task on {}", this.bi(), exception);
        }

    }
}
