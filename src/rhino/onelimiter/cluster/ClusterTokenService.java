package src.rhino.onelimiter.cluster;

import src.rhino.onelimiter.OneLimiterStrategy;

public interface ClusterTokenService extends TokenService {

	void setStrategy(OneLimiterStrategy strategy);
}