package sgw.core.service_channel;

import java.util.HashMap;

public abstract class RpcChannelContext extends HashMap<String, Object> {

    protected static final String HTTP_REQUEST_ID = "http_request_id";
    protected static final String RPC_SEND_TIME = "rpc_send_time";
    protected static final String RPC_RECV_TIME = "rpc_recv_time";
    protected static final String RPC_INVOKER = "rpc_invoker";
    protected static final String RPC_INVOKER_DEF = "rpc_invoker_def";

    public long getHttpRequestId() {
        return (long) get(HTTP_REQUEST_ID);
    }

    public void setHttpRequestId(long id) {
        put(HTTP_REQUEST_ID, id);
    }

    /**
     *
     * @param time The time rpc request (in milliseconds) is sent.
     */
    public void setRpcSendTime(long time) {
        put(RPC_SEND_TIME, time);
    }

    /**
     *
     * @return The time rpc request is sent (in milliseconds).
     */
    public long getRpcSentTime() {
        return (long) get(RPC_SEND_TIME);
    }

    /**
     *
     * @param time The time rpc response is received (in milliseconds).
     */
    public void setRpcRecvTime(long time) {
        put(RPC_RECV_TIME, time);
    }

    /**
     *
     * @return The time rpc response is received (in milliseconds).
     */
    public long getRpcRecvTime() {
        return (long) get(RPC_RECV_TIME);
    }

    public RpcInvoker getRpcInvoker() {
        return (RpcInvoker) get(RPC_INVOKER);
    }

    public void setRpcInvoker(RpcInvoker invoker) {
        put(RPC_INVOKER, invoker);
    }

    public RpcInvokerDef getRpcInvokerDef() {
        return (RpcInvokerDef) get(RPC_INVOKER_DEF);
    }

    public void setRpcInvokerDef(RpcInvokerDef invokerDef) {
        put(RPC_INVOKER_DEF, invokerDef);
    }
}
