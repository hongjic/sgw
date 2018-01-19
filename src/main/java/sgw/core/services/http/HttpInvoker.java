package sgw.core.services.http;

import io.netty.channel.ChannelFuture;
import sgw.core.services.RpcInvoker;

import java.util.List;

public abstract class HttpInvoker implements RpcInvoker{

    @Override
    public ChannelFuture invoke(List<Object> param) {
        return null;
    }
}
