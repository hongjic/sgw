package sgw.core.service_channel;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import sgw.core.http_channel.HttpChannelInitializer;
import sgw.core.http_channel.HttpParamConvertor;
import sgw.core.http_channel.ResultHttpConvertor;
import sgw.core.service_channel.thrift.ThriftDecoder;
import sgw.core.service_channel.thrift.ThriftEncoder;


/**
 * Unlike {@link HttpChannelInitializer}, it is not shared among threads.
 * Every new RPC connection will create a brand new {@link ServiceChannelInitializer}
 * backend channel works as a client to the backend withServices.
 */
public class ServiceChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RpcInvokerDef invokerDef;
    private Channel httpChannel;

    public ServiceChannelInitializer(RpcInvokerDef invokerDef, Channel httpChannel) {
        this.invokerDef = invokerDef;
        this.httpChannel = httpChannel;
    }

    @Override
    public void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        RpcType protocol = invokerDef.getProtocol();

        /**
         * temporarily hard coded for experiment.
         * TODO: init different pipeline according to protocol (and maybe request type like file upload).
         */
        switch (protocol) {
            case Thrift: {
                // outbound handlers: encode and send request
                pipeline.addLast("thriftEncoder", new ThriftEncoder(true));

                // inbound handlers: receive and decode response
                pipeline.addLast("thriftDecoder", new ThriftDecoder(invokerDef));
                /**
                 * Send the generated http response back to the http channel.
                 * Probably its the always the same despite the rpc protocol we use.
                 */
                pipeline.addLast("final", new RpcFinalHandler(httpChannel));
                break;
            }
            default:
                break;
        }
    }
}
