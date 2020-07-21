package net.minecraft.server;

import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerWorkerThread extends Thread {
    private static final AtomicInteger threadId = new AtomicInteger(1);
    public ServerWorkerThread(Runnable target) {
        super(target, "Server-Worker-" + threadId.getAndIncrement());
        setPriority(Thread.NORM_PRIORITY-1); // Deprioritize over main
        this.setUncaughtExceptionHandler((thread, throwable) -> {
            if (throwable instanceof CompletionException) {
                throwable = throwable.getCause();
            }

            if (throwable instanceof ReportedException) {
                DispenserRegistry.a(((ReportedException) throwable).a().e());
                System.exit(-1);
            }

            MinecraftServer.LOGGER.error(String.format("Caught exception in thread %s", thread), throwable);
        });
    }
}
