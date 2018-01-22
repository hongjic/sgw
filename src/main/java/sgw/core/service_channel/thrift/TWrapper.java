package sgw.core.service_channel.thrift;

import org.apache.thrift.TBase;

public class TWrapper<T extends TBase> {

    private T value;
    private String method;

    public TWrapper(T value, String method) {
        this.value = value;
        this.method = method;
    }

    public T getValue() {
        return value;
    }

    public String getMethod() {
        return method;
    }
}
