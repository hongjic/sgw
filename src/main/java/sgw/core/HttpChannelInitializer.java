package sgw.core;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;


public class HttpChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // HttpServerCodec is both inbound and outbound
        ch.pipeline().addLast("codec", new HttpServerCodec());

        // ChannelInboundHandler
        ch.pipeline().addLast("server", new HttpServerHandler());
//        ch.pipeline().addLast("routing", new RoutingHandler());
    }
}
