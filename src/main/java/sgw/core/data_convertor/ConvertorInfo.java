package sgw.core.data_convertor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sgw.core.data_convertor.annotations.PathVar;
import sgw.core.data_convertor.annotations.RequestParser;
import sgw.core.data_convertor.annotations.ResponseGenerator;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ConvertorInfo {

    private static final Logger logger = LoggerFactory.getLogger(ConvertorInfo.class);

    private Class<?> convertorClazz;
    /**
     * singleton
     */
    private final Object convertor;
    private Method requestParser;
    private Method responseGenerator;
    private Parameter[] parserParams;
    private String[] parserPathVarNames;

    ConvertorInfo(Class<?> convertorClazz, Object convertor, Method requestParser, Method responseGenerator,
                  Parameter[] parserParams, String[] parserPathVarNames) {
        this.convertorClazz = convertorClazz;
        this.convertor = convertor;
        this.requestParser = requestParser;
        this.responseGenerator = responseGenerator;
        this.parserParams = parserParams;
        this.parserPathVarNames = parserPathVarNames;
    }

    public Class<?> getConvertorClazz() {
        return convertorClazz;
    }

    public Object getConvertor() {
        return convertor;
    }

    public Method getRequestParser() {
        return requestParser;
    }

    public Method getResponseGenerator() {
        return responseGenerator;
    }

    public Parameter[] getParserParams() {
        return parserParams;
    }

    public String[] getParserPathVarNames() {
        return parserPathVarNames;
    }

    public static ConvertorInfo create(String httpConvertorClazzName) throws Exception {
        Class<?> convertorClazz;
        Object convertor;
        Method requestParser = null;
        Method responseGenerator = null;
        Parameter[] parserParams;
        String[] parserPathVarNames;
        try {
            convertorClazz = Class.forName(httpConvertorClazzName);
            convertor = convertorClazz.newInstance();
        } catch (ClassNotFoundException e) {
            logger.error("Convertor class named as {} can not be found.", httpConvertorClazzName);
            throw e;
        }

        // get methods
        Method[] methods = convertorClazz.getDeclaredMethods();
        for (Method method: methods) {
            if (method.getAnnotationsByType(RequestParser.class).length > 0)
                requestParser = method;
            if (method.getAnnotationsByType(ResponseGenerator.class).length > 0)
                responseGenerator = method;
        }

        // validate methods
        if (requestParser == null || responseGenerator == null) {
            String message = "@RequestParser or @ResponseGenerator is missing for convertor class " +
                    httpConvertorClazzName + ".";
            logger.error(message);
            throw new InvalidConvertorException(message);
        }

        // validate method return type
        if (!Object[].class.isAssignableFrom(requestParser.getReturnType()))
            throw new InvalidConvertorException("Return type of @RequestParser in class " +
                    httpConvertorClazzName + " is not valid.");
        if (!String.class.isAssignableFrom(responseGenerator.getReturnType()))
            throw new InvalidConvertorException("Return type of @ResponseGenerator in class " +
                    httpConvertorClazzName + " is not valid.");

        // find annotated path variables
        parserParams = requestParser.getParameters();
        parserPathVarNames = new String[parserParams.length];
        for (int i = 0; i < parserParams.length; i ++) {
            PathVar an = parserParams[i].getAnnotation(PathVar.class);
            if (an != null)
                parserPathVarNames[i] = an.value();
        }

        // create new instance
        return new ConvertorInfo(
                convertorClazz,
                convertor,
                requestParser,
                responseGenerator,
                parserParams,
                parserPathVarNames
        );
    }
}
