package sgw.monitors;

import java.util.concurrent.atomic.AtomicLong;

public class NumCounter implements Counter<Long> {

    private String name;
    private AtomicLong count;

    public NumCounter(String name) {
        this(name, 0);
    }

    public NumCounter(String name, long initValue) {
        this.count = new AtomicLong(initValue);
        this.name = name;
    }

    @Override
    public Long getValue() {
        return count.get();
    }

    @Override
    public String getName() {
        return name;
    }

    public long increase() {
        return count.incrementAndGet();
    }

    public long decrease() {
        return count.decrementAndGet();
    }

}
