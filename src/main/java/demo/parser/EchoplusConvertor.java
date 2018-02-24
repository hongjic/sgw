package demo.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import demo.gen.echoplus.struct.Input;
import sgw.core.data_convertor.HttpJSONConvertor;

public class EchoplusConvertor extends HttpJSONConvertor {

    @Override
    public String generateJSONResponse(Object[] results) {
        return JSON.toJSONString(results[0], SerializerFeature.IgnoreNonFieldGetter);
    }

    @Override
    public Object[] parseJSONRequest(JSONObject json) {
        Input input1 = json.getJSONObject("input1").toJavaObject(Input.class);
        String input2 = json.getString("input2");
        return new Object[] {input1, input2};
    }
}
