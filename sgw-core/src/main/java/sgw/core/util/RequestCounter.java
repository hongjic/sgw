package sgw.core.util;

import java.util.concurrent.atomic.AtomicLong;

public class RequestCounter {

    public static final RequestCounter Instance = new RequestCounter();

    RequestCounter() {}

    private AtomicLong counter = new AtomicLong(0);

    public long incrementAndGet() {
        return counter.incrementAndGet();
    }
}
