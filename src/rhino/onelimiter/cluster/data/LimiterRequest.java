package src.rhino.onelimiter.cluster.data;


public class LimiterRequest extends TokenRequest{

    public LimiterRequest() {
    	
    }
    
    public LimiterRequest(long strategyId,int count, boolean test) {
    	super(strategyId,count,test);
    }
    
    @Override
    public String toString() {
        return "FlowRequestData{" +
            "strategyId=" + strategyId +
            ", count=" + count +
            ", test=" + test +
            '}';
    }
}
