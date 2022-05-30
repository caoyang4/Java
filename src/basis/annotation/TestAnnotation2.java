package src.basis.annotation;

public class TestAnnotation2 {
    // 检查是否有 getter，没有编译不通过
//    @CheckGetter
    int v;

    public int getV() {
        return v;
    }

    public static void main(String[] args) {

    }
}
