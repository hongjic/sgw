package sgw.core.util;

import java.util.concurrent.atomic.AtomicLong;

public enum RequestCounter {

    Instance;

    private AtomicLong counter = new AtomicLong(0);

    public long incrementAndGet() {
        return counter.incrementAndGet();
    }
}
