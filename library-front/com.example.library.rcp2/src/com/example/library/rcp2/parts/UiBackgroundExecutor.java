package com.example.library.rcp2.parts;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class UiBackgroundExecutor {

    private static final ThreadFactory FACTORY = new ThreadFactory() {
        private final AtomicInteger n = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "library-rcp-api-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    };

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(FACTORY);

    private UiBackgroundExecutor() {
    }

    static void execute(Runnable task) {
        EXECUTOR.execute(task);
    }
}
