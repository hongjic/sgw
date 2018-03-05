package sgw.core.service_channel.thrift;

import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.service_channel.RpcInvokerDef;
import sgw.core.service_channel.RpcType;

public class ThriftInvokerDef extends RpcInvokerDef {

    private final Logger logger = LoggerFactory.getLogger(ThriftInvokerDef.class);
    private String argsClazzName;
    private String resultClazzName;
    private Class<? extends TBase> argsClazz;
    private Class<? extends TBase> resultClazz;

    public ThriftInvokerDef(RpcType protocol,
                            String serviceName,
                            String methodName,
                            String thriftClazzName,
                            String requestParser,
                            String responseGenerator) throws ClassNotFoundException {
        super(protocol, serviceName, methodName, requestParser, responseGenerator);
        this.argsClazzName = getThriftArgsClazzName(thriftClazzName, methodName);
        this.resultClazzName = getThriftResultClazzName(thriftClazzName, methodName);
        this.argsClazz = getThriftArgsClazz();
        this.resultClazz = getThriftResultClazz();
    }

    public ThriftInvokerDef(RpcType protocol,
                            String serviceName,
                            String methodName,
                            Class<? extends TBase> thriftArgs,
                            Class<? extends TBase> thriftResult,
                            String requestParser,
                            String responseGenerator) {
        super(protocol, serviceName, methodName, requestParser, responseGenerator);
        this.argsClazz = thriftArgs;
        this.resultClazz = thriftResult;
    }

    public Class<? extends TBase> getThriftArgsClazz() throws ClassNotFoundException {
        if (argsClazz == null) {
            try {
                argsClazz = Class.forName(argsClazzName).asSubclass(TBase.class);

            } catch (ClassNotFoundException e) {
                logger.error("Thrift class named as {} can not be found.", argsClazzName);
                throw e;
            }
        }
        return argsClazz;
    }

    public Class<? extends TBase> getThriftResultClazz() throws ClassNotFoundException {
        if (resultClazz == null) {
            try {
                resultClazz = Class.forName(resultClazzName).asSubclass(TBase.class);
            } catch (ClassNotFoundException e) {
                logger.error("Thrift class named as {} can not be found.", resultClazzName);
                throw e;
            }
        }
        return resultClazz;
    }

    private static String getThriftArgsClazzName(String thriftClazzName, String methodName) {
        return thriftClazzName + "$" + methodName + "_args";
    }

    private static String getThriftResultClazzName(String thriftClazzName, String methodName) {
        return thriftClazzName + "$" + methodName + "_result";
    }

}
