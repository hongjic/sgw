package examples.thrift_service;

import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;

public class ThriftEchoClient {

    public void perform() {
        try {
            TAsyncClientManager clientManager = new TAsyncClientManager();
            TNonblockingTransport transport = new TNonblockingSocket("127.0.0.1", 9090, 30000);
            TProtocolFactory protocolFactory = new TCompactProtocol.Factory();
            EchoService.AsyncClient client = new EchoService.AsyncClient(protocolFactory, clientManager, transport);
            System.out.println("Client calls...");
            client.echo("hello", new AsyncMethodCallback<String>() {
                @Override
                public void onComplete(String response) {
                    System.out.println("Result: " + response);
                }

                @Override
                public void onError(Exception exception) {
                    System.out.println("error");
                }
            });
            while (true) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ThriftEchoClient client = new ThriftEchoClient();
        client.perform();
    }
}
