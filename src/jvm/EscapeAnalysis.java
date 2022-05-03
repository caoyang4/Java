package src.jvm;

/**
 * 逃逸分析
 * 如何快速判断是否发生逃逸，就看 new 的对象实体是否有可能在方法外使用
 */
public class EscapeAnalysis {
    public EscapeAnalysis obj;

    /**
     * 返回EscapeAnalysis对象，发生逃逸
     */
    public EscapeAnalysis getInstance(){
        return obj == null ? new EscapeAnalysis() : obj;
    }

    /**
     * 成员变量赋值，发生逃逸
     */
    public void setObj(EscapeAnalysis obj) {
        this.obj = new EscapeAnalysis();
    }

    /**
     * 栈上分配，栈上使用，没有发生逃逸
     */
    public void useEscapeAnalysis(){
        EscapeAnalysis e = new EscapeAnalysis();
    }

    /**
     * 引用成员变量，发生逃逸
     */
    public void useInstance(){
        EscapeAnalysis e = getInstance();
    }

}
