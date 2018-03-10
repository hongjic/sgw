package sgw.core.http_channel;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import sgw.core.NettyGatewayServerConfig;
import sgw.core.service_channel.RpcChannelContext;
import sgw.core.util.FastMessage;
import sgw.core.routing.Router;
import sgw.core.service_channel.RpcInvoker;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_discovery.RpcInvokerDiscoverer;
import sgw.core.data_convertor.FullHttpRequestParser;
import sgw.core.data_convertor.FullHttpResponseGenerator;

import java.util.HashMap;

/**
 * share data between handlers in the same httpChannel.
 * this class will never be shared among threads.
 */
public final class HttpChannelContext extends HashMap<String, Object> {

    private static final String REQUEST_ID = "%request_id%";
    private static final String HTTP_REQUEST = "http_request";
    private static final String HTTP_RESPONSE = "http_response";
    private static final String SEND_FAST_RESPONSE = "send_fast_response";
    private static final String FAST_MESSAGE = "fast_message";
    private static final String ROUTER = "router";
    private static final String INVOKER_DISCOVERER = "invoker_discoverer";
    private static final String INVOKER = "invoker";
    private static final String INVOKER_DEF = "invoker_def";
    private static final String REQUEST_PARSER = "request_parser";
    private static final String RESPONSE_GENERATOR = "response_generator";
    private static final String GATEWAY_SERVER_CONFIG = "gateway_server_config";
    private static final String HTTP_CHANNEL = "http_channel";
    private static final String RPC_CHANNEL_CONTEXT = "rpc_channel_context";
    private static final String RPC_INVOKE_RESULT = "rpc_invoke_result";

    public FullHttpRequest getHttpRequest() {
        return (FullHttpRequest) get(HTTP_REQUEST);
    }

    public void setHttpRequest(FullHttpRequest httpRequest) {
        put(HTTP_REQUEST, httpRequest);
    }

    public FullHttpResponse getHttpResponse() {
        return (FullHttpResponse) get(HTTP_RESPONSE);
    }

    public void setHttpResponse(FullHttpResponse httpResponse) {
        put(HTTP_RESPONSE, httpResponse);
    }

    public boolean getSendFastMessage() {
        return (boolean) get(SEND_FAST_RESPONSE);
    }

    public void setSendFastMessage(boolean con) {
        put(SEND_FAST_RESPONSE, con);
    }

    public void setFastMessage(FastMessage message) {
        put(FAST_MESSAGE, message);
    }

    public FastMessage getFastMessage() {
        return (FastMessage) get(FAST_MESSAGE);
    }

    public Router getRouter() {
        return (Router) get(ROUTER);
    }

    public RpcInvokerDiscoverer getInvokerDiscoverer() {
        return (RpcInvokerDiscoverer) get(INVOKER_DISCOVERER);
    }

    public void setInvokerDiscoverer(RpcInvokerDiscoverer invokerDiscoverer) {
        put(INVOKER_DISCOVERER, invokerDiscoverer);
    }

    public void setRouter(Router router) {
        put(ROUTER, router);
    }

    public RpcInvoker getInvoker() {
        return (RpcInvoker) get(INVOKER);
    }

    public void setInvoker(RpcInvoker invoker) {
        put(INVOKER, invoker);
    }

    public FullHttpRequestParser getFullHttpRequestParser() {
        return (FullHttpRequestParser) get(REQUEST_PARSER);
    }

    public void setFullHttpRequestParser(FullHttpRequestParser parser) {
        put(REQUEST_PARSER, parser);
    }

    public NettyGatewayServerConfig getConfig() {
        return (NettyGatewayServerConfig) get(GATEWAY_SERVER_CONFIG);
    }

    public void setConfig(NettyGatewayServerConfig config) {
        put(GATEWAY_SERVER_CONFIG, config);
    }

    public Channel getHttpChannel() {
        return (Channel) get(HTTP_CHANNEL);
    }

    public void setHttpChannel(Channel httpChannel) {
        put(HTTP_CHANNEL, httpChannel);
    }

    public FullHttpResponseGenerator getResponseGenerator() {
        return (FullHttpResponseGenerator) get(RESPONSE_GENERATOR);
    }

    public void setFullHttpResponseGenerator(FullHttpResponseGenerator responseGenerator) {
        put(RESPONSE_GENERATOR, responseGenerator);
    }

    public void setInvokerDef(RpcInvokerDef invokerDef) {
        put(INVOKER_DEF, invokerDef);
    }

    public RpcInvokerDef getInvokerDef() {
        return (RpcInvokerDef) get(INVOKER_DEF);
    }

    public void setRpcChannelContext(RpcChannelContext rpcCtx) {
        put(RPC_CHANNEL_CONTEXT, rpcCtx);
    }

    public long getRpcSentTime() {
        return ((RpcChannelContext) get(RPC_CHANNEL_CONTEXT)).getRpcSentTime();
    }

    public long getRpcRecvTime() {
        return ((RpcChannelContext) get(RPC_CHANNEL_CONTEXT)).getRpcRecvTime();
    }

    public void setInvokeResult(Object invokeResult) {
        put(RPC_INVOKE_RESULT, invokeResult);
    }

    public Object getInvokeResult() {
        return get(RPC_INVOKE_RESULT);
    }

    public void setRequestId(long id) {
        put(REQUEST_ID, id);
    }

    public long getRequestId() {
        return (long) get(REQUEST_ID);
    }
}
