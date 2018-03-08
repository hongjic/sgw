package sgw.monitors;

import java.util.concurrent.ConcurrentHashMap;

public class GatewayMonitor {

    private static GatewayMonitor instance;
    public static final String RCV_REQ = "receive_request_counter";
    public static final String SND_RES = "send_response_counter";

    public static GatewayMonitor getInstance() {
        if (instance == null)
            instance = new GatewayMonitor();
        return instance;
    }

    private ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    public GatewayMonitor() {
        addCounter(new NumCounter(RCV_REQ));
        addCounter(new NumCounter(SND_RES));
    }

    public void addCounter(Counter counter) {
        counters.put(counter.getName(), counter);
    }

    public Counter getCounter(String name) {
        return counters.get(name);
    }

    public <T extends Counter> T getCounter(String name, Class<T> clazz) {
        return clazz.cast(counters.get(name));
    }
}
