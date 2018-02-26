package sgw.core.service_channel;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import sgw.core.http_channel.HttpChannelInitializer;
import sgw.core.service_channel.thrift.ThriftChannelContext;
import sgw.core.service_channel.thrift.ThriftDecoder;
import sgw.core.service_channel.thrift.ThriftEncoder;


/**
 * Unlike {@link HttpChannelInitializer}, it is not shared among threads.
 * Every new RPC connection will create a brand new {@link ServiceChannelInitializer}
 * backend channel works as a client to the backend withServices.
 */
public class ServiceChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RpcInvoker invoker;
    private RpcInvokerDef invokerDef;

    public ServiceChannelInitializer(RpcInvokerDef invokerDef, RpcInvoker invoker) {
        this.invokerDef = invokerDef;
        this.invoker = invoker;
    }

    @Override
    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        RpcType protocol = invokerDef.getProtocol();

        switch (protocol) {
            case Thrift: {
                ThriftChannelContext thriftCtx = new ThriftChannelContext();
                thriftCtx.setInvoker(invoker);
                thriftCtx.setInvokerDef(invokerDef);

                // outbound handlers: encode and send request
                pipeline.addLast("thriftEncoder", new ThriftEncoder(thriftCtx));

                // inbound handlers: receive and decode response
                pipeline.addLast("thriftDecoder", new ThriftDecoder(thriftCtx));
                /**
                 * Send the generated http response back to the http channel.
                 * Probably its the always the same despite the rpc protocol we use.
                 */
                pipeline.addLast("final", new RpcFinalHandler(thriftCtx));
                break;
            }
            default:
                break;
        }
    }
}
