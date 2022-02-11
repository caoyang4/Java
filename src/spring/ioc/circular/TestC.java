package src.spring.ioc.circular;

public class TestC {
    TestA testA;

    public TestA getTestA() {
        return testA;
    }

    public void setTestA(TestA testA) {
        this.testA = testA;
    }
}
