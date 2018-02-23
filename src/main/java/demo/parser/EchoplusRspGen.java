package demo.parser;

import com.alibaba.fastjson.JSON;
import demo.gen.echoplus.struct.Output;
import sgw.core.data_convertor.JSONHttpResponseGenerator;

import java.util.HashMap;

public class EchoplusRspGen extends JSONHttpResponseGenerator {

    @Override
    public String generateJSONBody(Object[] results) {
        Output output = (Output) results[0];
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", output.id);
        map.put("message", output.message);
        map.put("append", output.append);
        return JSON.toJSONString(map);
    }

    public static void main(String[] args) {
        Output output = new Output();
        output.id = 1;
        output.message = "mmm";
        output.append = "aaa";
        System.out.println(JSON.toJSONString(output));
        output.setId(1);
        output.setMessage("mmm");
        output.setAppend("aaaa");
        System.out.println(JSON.toJSONString(output));
    }
}
