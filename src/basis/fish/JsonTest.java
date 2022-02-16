package src.basis.fish;

import com.alibaba.fastjson.JSONObject;

/**
 * @author caoyang
 */
public class JsonTest {
    public static void main(String[] args) {
        Person p = new Person("james", 6, "La");
        System.out.println(JSONObject.toJSON(p));
    }
}
