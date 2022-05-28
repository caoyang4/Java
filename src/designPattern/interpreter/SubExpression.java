package src.designPattern.interpreter;

import java.util.HashMap;

/**
 * @author caoyang
 * @create 2022-05-28 16:55
 */
public class SubExpression implements Expression{
    protected Expression left;
    protected Expression right;

    @Override
    public int interpreter(HashMap<String, Integer> map) {
        return left.interpreter(map) - right.interpreter(map);
    }
}
