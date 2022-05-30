package src.rhino.onelimiter.cluster;

import src.rhino.onelimiter.cluster.data.FrequencyRequest;
import src.rhino.onelimiter.cluster.data.LimiterRequest;

/**
 * @author Feng
 * @date 2020-06-17
 */
public interface TokenService {

    /**
     * 限流
     */
    TokenResult requestLimiterToken(LimiterRequest req);
    
    /**
     * 限频
     */
    TokenResult requestFrequencyToken(FrequencyRequest req);

   
}
