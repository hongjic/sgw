package examples.thrift_service;

import org.apache.thrift.TException;

public class EchoServiceHandler implements EchoService.Iface {

    @Override
    public String echo(String param) throws TException {
        return "This is return result: " + param;
    }
}
