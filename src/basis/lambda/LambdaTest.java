package src.basis.lambda;

/**
 * @author caoyang
 */
public class LambdaTest {
    public static void main(String[] args) {
        FunctionInterfaceTest test = input -> getInstance(input);
        System.out.println(test.getInfo("你好"));
        System.out.println("======================");

        FunctionInterfaceTest test1 = input -> input + "从入门到放弃！";
        System.out.println(test1.getInfo("java"));
        System.out.println("======================");

        // 本质上是函数指针
        FunctionInterfaceTest test2 = LambdaTest::getMessage;
        System.out.println(test2.getInfo("滚蛋"));
        System.out.println("======================");

        String s = joinStr("很惭愧，", str -> str + "就做了一点微小的工作");
        System.out.println(s);


    }

    public static String getInstance(String item){
        return item+"！世界";
    }

    public static String getMessage(String massage){
        return "世界,"+ massage+"!";
    }

    public static String joinStr(String str, FunctionInterfaceTest test){
        return test.getInfo(str);
    }

}
