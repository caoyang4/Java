package src.basis.annotation;

import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author caoyang
 */
public class TestAnnotation {
    @Override
    @MyAnnotation(name = "toStringMethod", description = "override toString method")
    public String toString() {
        return "Override toString method";
    }

    @Deprecated
    @MyAnnotation(name = "old static method", description = "deprecated old static method")
    public static void oldMethod() {
        System.out.println("old method, don't use it.");
    }

    /**
     * SuppressWarnings 忽略编译告警
     * @throws FileNotFoundException
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    @MyAnnotation(name = "test method", description = "suppress warning static method")
    public static void genericsTest() throws FileNotFoundException {
        List l = new ArrayList();
        l.add("abc");
        oldMethod();
    }

    public static void main(String[] args) {
        TestAnnotation testAnnotation = new TestAnnotation();
        Method[] methods = testAnnotation.getClass().getMethods();
        for (Method method : methods) {
            if(method.isAnnotationPresent(MyAnnotation.class)){
                Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    System.out.println("Annotation in Method '" + method.getName() + "' : " + annotation);
                }
                MyAnnotation myAnnotation = method.getAnnotation(MyAnnotation.class);
                System.out.println(myAnnotation.name());
            }
            System.out.println();
        }
    }
}
