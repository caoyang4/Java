package src.rhino.onelimiter.cluster.data;


public class FrequencyRequest extends TokenRequest {

    private String uniqKey;
    
    public FrequencyRequest() {
    	
    }
    
    public FrequencyRequest(long strategyId,int count, boolean test) {
    	super(strategyId,count,test);
    }
    
    public FrequencyRequest(long strategyId,int count, boolean test,String uniqKey) {
    	super(strategyId,count,test);
    	this.uniqKey = uniqKey;
    }

	public String getUniqKey() {
		return uniqKey;
	}

	public void setUniqKey(String uniqKey) {
		this.uniqKey = uniqKey;
	}

	@Override
    public String toString() {
        return "FlowRequestData{" +
            "strategyId=" + strategyId +
            ", uniqKey=" + uniqKey +
            ", count=" + count +
            ", test=" + test +
            '}';
    }
}
