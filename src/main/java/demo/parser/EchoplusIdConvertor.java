package demo.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import demo.gen.echoplus.service.EchoplusService;
import demo.gen.echoplus.struct.Input;
import demo.gen.echoplus.struct.Output;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.CharsetUtil;
import sgw.core.data_convertor.annotations.*;

import java.util.HashMap;
import java.util.Map;

@ThriftRouter(http = {"POST", "/echoplus/{id}"}, service = "echoplusservice", method = "echo",
        args = EchoplusService.echo_args.class, result = EchoplusService.echo_result.class)
public class EchoplusIdConvertor {

    @ResponseHeaders
    final Map<CharSequence, String> headers = new HashMap<>();

    public EchoplusIdConvertor() {
        headers.put(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
    }

    @RequestParser
    public Object[] parse(FullHttpRequest request, @PathVar("id") int id) {
        String content = request.content().toString(CharsetUtil.UTF_8);
        JSONObject json = JSON.parseObject(content);
        Input input1 = json.getJSONObject("input1").toJavaObject(Input.class);
        input1.id = id;
        String input2 = json.getString("input2");
        return new Object[] {input1, input2};
    }

    @ResponseGenerator
    public String generate(Output output) {
        return JSON.toJSONString(output, SerializerFeature.IgnoreNonFieldGetter);
    }

}