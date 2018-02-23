package demo.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import gen.echoplus.struct.Input;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;
import sgw.core.data_convertor.FullHttpRequestParser;

public class EchoplusReqParser implements FullHttpRequestParser {

    @Override
    public Object[] parse(FullHttpRequest request) {
        String content = request.content().toString(CharsetUtil.UTF_8);
        JSONObject json = JSON.parseObject(content);
        Input input1 = new Input();
        input1.setId(json.getJSONObject("input1").getInteger("id"));
        input1.setMessage(json.getJSONObject("input1").getString("message"));
        String input2 = json.getString("input2");

        return new Object[] {input1, input2};
    }
}
