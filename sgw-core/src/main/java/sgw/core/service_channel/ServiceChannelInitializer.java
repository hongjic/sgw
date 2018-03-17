package sgw.core.service_channel;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.redis.RedisEncoder;
import sgw.core.http_channel.HttpChannelInitializer;
import sgw.core.service_channel.thrift.ThriftChannelContext;
import sgw.core.service_channel.thrift.ThriftRequestContext;
import sgw.core.service_channel.thrift.ThriftDecoder;
import sgw.core.service_channel.thrift.ThriftEncoder;


/**
 * Unlike {@link HttpChannelInitializer}, it is not shared among threads.
 * Every new RPC connection will create a brand new {@link ServiceChannelInitializer}
 * backend channel works as a client to the backend withServices.
 */
public class ServiceChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RpcType protocol;

    public ServiceChannelInitializer(RpcType protocol) {
        this.protocol = protocol;
    }

    @Override
    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        switch (protocol) {
            case thrift: {
                ThriftChannelContext thriftChanCtx = new ThriftChannelContext();

                // outbound handlers: encode and send request
                pipeline.addLast("thriftEncoder", new ThriftEncoder(thriftChanCtx));

                // inbound handlers: receive and decode response
                pipeline.addLast("thriftDecoder", new ThriftDecoder(thriftChanCtx));
                /**
                 * receive and send message from and back to http channel.
                 */
                pipeline.addLast("server", new ServiceHandler(thriftChanCtx));
                break;
            }
            default:
                break;
        }
    }
}
