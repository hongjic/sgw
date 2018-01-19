package examples.thrift_service;

import org.apache.thrift.TException;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.transport.TFastFramedTransport;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;

public class ThriftEchoServer {

    public void start() {
        try {
            TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(9090);
            final EchoService.Processor processor = new EchoService.Processor(new EchoServiceHandler());
            TNonblockingServer.Args arg = new TNonblockingServer.Args(serverSocket);

            arg.protocolFactory(new TCompactProtocol.Factory());
            arg.transportFactory(new TFastFramedTransport.Factory());
            arg.processorFactory(new TProcessorFactory(processor));
            TNonblockingServer server = new TNonblockingServer(arg);

            server.serve();
            System.out.println("server starting...");
        } catch (TException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ThriftEchoServer server = new ThriftEchoServer();
        server.start();
    }
}
