package sgw.core.data_convertor;

import io.netty.handler.codec.http.FullHttpRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public class RequestParserImpl implements FullHttpRequestParser{

    private ConvertorInfo cinfo;
    private Map<String, String> pathParams;

    public RequestParserImpl(ConvertorInfo cinfo, Map<String, String> pathParams) {
        this.cinfo = cinfo;
        this.pathParams = pathParams;
    }

    @Override
    public Object[] parse(FullHttpRequest request) throws Exception {
        Method requestParser = cinfo.getRequestParser();
        Parameter[] parserParams = cinfo.getParserParams();
        String[] parserPathVarNames = cinfo.getParserPathVarNames();
        int paramCount = cinfo.getParserParams().length;
        Object[] paramValues = new Object[paramCount];

        for (int i = 0; i < paramCount; i ++) {
            if (parserParams[i].getType().isAssignableFrom(FullHttpRequest.class))
                paramValues[i] = request;
            else {
                if (parserPathVarNames[i] != null) {
                    String key = parserPathVarNames[i];
                    String value = pathParams.get(key);
                    if (parserParams[i].getType() == int.class)
                        paramValues[i] = Integer.valueOf(value);
                    else if (parserParams[i].getType() == long.class)
                        paramValues[i] = Long.valueOf(value);
                    else if (parserParams[i].getType() == String.class)
                        paramValues[i] = value;
                    else
                        throw new InvalidConvertorException(parserParams[i].getType().getName() +
                                "is not supported for @PathVar parameters.");
                }
            }
        }

        return (Object[]) requestParser.invoke(cinfo.getConvertor(), paramValues);
    }
}
