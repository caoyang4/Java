package src.rhino.onelimiter.cluster.data;

/**
  * @author feng 
  * @date 2021年11月17日
  * @desc 
  */
public class TokenRequest {
	
	protected long strategyId;
	protected int count = 1;
	protected boolean test = false;
	
	TokenRequest(){
		
	}
	
	TokenRequest(long strategyId,int count,boolean test){
		this.strategyId = strategyId;
		this.count = count;
		this.test = test;
	}
	
	public long getStrategyId() {
		return strategyId;
	}
	public void setStrategyId(long strategyId) {
		this.strategyId = strategyId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public boolean isTest() {
		return test;
	}
	public void setTest(boolean test) {
		this.test = test;
	}
    
    

}
