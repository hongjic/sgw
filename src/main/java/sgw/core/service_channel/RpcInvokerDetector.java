package sgw.core.service_channel;

import io.netty.channel.ChannelFuture;

/**
 * An interface for the basic functionality of service discovery.
 * Enable different implementations. All implementations should be
 * thread-safe.
 */
public interface RpcInvokerDetector {

     /**
      * Always call connect before find.
      * @return a ChannelFuture representing the connect request
      */
     ChannelFuture connectAsync(RpcInvokerDef invokerDef) throws Exception;

     /**
      * @return a ChannelFuture representing the find request.
      */
     ChannelFuture findAsync(RpcInvokerDef invokerDef) throws Exception;

     RpcInvoker find(RpcInvokerDef invokerDef) throws Exception;

}
