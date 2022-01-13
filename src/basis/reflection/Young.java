package src.basis.reflection;

/**
 * @author caoyang
 */
public class Young {
    public int no;
    private String name;
    protected String className;
    boolean sex;

    public Young() {
    }

    public Young(String className) {
        this.className = className;
    }

    public void m(String... args) {
    }

    public void n(int i, String... args) {
    }

    public int v1(int... args) {
        int sum = 0;
        for (int i : args) {
            sum += i;
        }
        return sum;
    }

    public int v1(int i, int j, int k) {
        return i + j + k;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

