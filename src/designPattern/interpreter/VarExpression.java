package src.designPattern.interpreter;

import java.util.HashMap;

/**
 * @author caoyang
 * @create 2022-05-28 16:49
 */
public class VarExpression implements Expression{
    private String key;

    public VarExpression(String key) {
        this.key = key;
    }

    @Override
    public int interpreter(HashMap<String, Integer> map) {
        return map.get(key);
    }
}
