package demo.parser;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import sgw.core.data_convertor.annotations.RequestParser;
import sgw.core.data_convertor.annotations.ResponseGenerator;

public class EchoConvertor{

    @RequestParser
    public Object[] parse(FullHttpRequest request) {
        return new Object[] {request.content().toString(CharsetUtil.UTF_8)};
    }

    @ResponseGenerator
    public String generate(String result) {
        return result;
    }
}
