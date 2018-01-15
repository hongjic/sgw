package sgw.core.services;

/**
 * has to be thread-safe
 */
public interface RpcInvoker {

    Object invoke(Object param);

}
