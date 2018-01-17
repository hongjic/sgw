package examples.thrift_server;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import sun.reflect.annotation.ExceptionProxy;

public class ThriftEchoServer {

    public EchoServiceHandler handler;
    public EchoService.Processor processor;

    public ThriftEchoServer() {
        handler = new EchoServiceHandler();
        processor = new EchoService.Processor(handler);
    }

    public void start() {
        new Thread(() -> simple(processor)).start();
    }

    private void simple(EchoService.Processor processor) {
        try {
            TServerTransport serverTransport = new TServerSocket(9090);
            TServer server = new TSimpleServer(new TServer.Args(serverTransport).processor(processor));
            System.out.println("Starting the echo server...");

            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ThriftEchoServer server = new ThriftEchoServer();

        try {
            server.start();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
