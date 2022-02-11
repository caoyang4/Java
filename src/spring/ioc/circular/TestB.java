package src.spring.ioc.circular;

public class TestB {
    TestC testC;

    public TestC getTestC() {
        return testC;
    }

    public void setTestC(TestC testC) {
        this.testC = testC;
    }
}
