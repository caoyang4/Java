package src.rhino.onelimiter;

/**
 * @author Feng
 * @date 2021-04-25
 */

public enum LimiterAlgorithmEnum {
	
	/**
	 * guava的令牌桶算法，以固定的速率发放令牌
	 */
	GUAVA(0),
	
	/**
	 * 滑动窗口算法
	 */
	SLIDEWINDOW(1);
	
	private int algorithm;
	
	LimiterAlgorithmEnum(int algorithm){
		this.algorithm = algorithm;
	}

	public int getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(int algorithm) {
		this.algorithm = algorithm;
	}

}
