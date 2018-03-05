package demo.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import demo.gen.echoplus.struct.Input;
import demo.gen.echoplus.struct.Output;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.CharsetUtil;
import sgw.core.data_convertor.annotations.RequestParser;
import sgw.core.data_convertor.annotations.ResponseGenerator;
import sgw.core.data_convertor.annotations.ResponseHeaders;

import java.util.HashMap;
import java.util.Map;

public class EchoplusConvertor {

    @ResponseHeaders
    final Map<CharSequence, String> headers = new HashMap<>();

    public EchoplusConvertor() {
        headers.put(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8");
    }

    @RequestParser
    public Object[] parse(FullHttpRequest request) {
        String content = request.content().toString(CharsetUtil.UTF_8);
        JSONObject json = JSON.parseObject(content);
        Input input1 = json.getJSONObject("input1").toJavaObject(Input.class);
        String input2 = json.getString("input2");
        return new Object[] {input1, input2};
    }

    @ResponseGenerator
    public String generate(Output output) {
        return JSON.toJSONString(output, SerializerFeature.IgnoreNonFieldGetter);
    }

}
