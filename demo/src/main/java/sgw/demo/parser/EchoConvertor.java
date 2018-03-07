package sgw.demo.parser;

import sgw.demo.gen.echo.service.EchoService;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import sgw.core.data_convertor.annotations.RequestParser;
import sgw.core.data_convertor.annotations.ResponseGenerator;
import sgw.core.data_convertor.annotations.ThriftRouter;

@ThriftRouter(http = {"POST", "/echo"}, service = "echoservice", method = "echo",
        args = EchoService.echo_args.class, result = EchoService.echo_result.class)
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
