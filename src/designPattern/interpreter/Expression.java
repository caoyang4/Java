package src.designPattern.interpreter;

import java.util.HashMap;

/**
 * 解释器模式:
 *   一些重复发生的问题可以使用解释器模式。例如，多个应用服务器，每天产生大量的日志，需要对日志文件进行分析处理，
 *   由于各个服务器的日志格式不同，但是数据元素都是相同的，按照解释器的说法就是终结符表达式都是相同的
 * @author caoyang
 * @create 2022-05-28 16:42
 */
public interface Expression {
    int interpreter(HashMap<String,Integer> map);
}
