package src.spring.ioc.circular;

public class TestA {
    TestB testB;

    public TestB getTestB() {
        return testB;
    }

    public void setTestB(TestB testB) {
        this.testB = testB;
    }

}
