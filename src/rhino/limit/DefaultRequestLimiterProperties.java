package src.rhino.limit;

import java.io.IOException;

import src.rhino.RhinoType;
import src.rhino.annotation.RateLimit;
import src.rhino.config.ConfigChangedListener;
import src.rhino.config.Configuration;
import src.rhino.util.AppUtils;
//import src.rhino.util.SerializerUtils;

/**
 * Created by zhanjun on 2017/4/24.
 */
public class DefaultRequestLimiterProperties extends AbstractRequestLimiterProperties {
    private volatile RequestLimiterBean requestLimiterBean;

    public DefaultRequestLimiterProperties(String rhinoKey) {
        this(AppUtils.getAppName(), rhinoKey, null);
    }

    public DefaultRequestLimiterProperties(String appKey, String rhinoKey,
                                           Configuration configuration) {
        this(appKey, rhinoKey, null, configuration);
    }

    public DefaultRequestLimiterProperties(RateLimit limit) {
        this(AppUtils.getAppName(), limit.rhinoKey(), limit, null);
    }

    public DefaultRequestLimiterProperties(String appKey, String rhinoKey, RateLimit rateLimit, Configuration configuration) {
        super(appKey, rhinoKey, RhinoType.SingleLimiter, configuration);
        RequestLimiterBean defaultRequestLimiterBean = createDefaultBean(rateLimit);
//        this.requestLimiterBean = getBeanValue(configKeySuffix, RequestLimiterBean.class, defaultRequestLimiterBean, true);
    }

    @Override
    public boolean getIsActive() {
        return requestLimiterBean.isActive();
    }

    @Override
    public int getRate() {
        return requestLimiterBean.getRate();
    }

    @Override
    public int getStrategy() {
        return requestLimiterBean.getStrategy();
    }

    @Override
    public long getTimeoutInMilliseconds() {
        return requestLimiterBean.getTimeoutInMilliseconds();
    }

    @Override
    public RequestLimiterProperties setActive(boolean isActive) {
        requestLimiterBean.setActive(isActive);
        return this;
    }

    @Override
    public RequestLimiterProperties setRate(int rate) {
        requestLimiterBean.setRate(rate);
        return this;
    }

    @Override
    public RequestLimiterProperties setStrategy(int strategy) {
        requestLimiterBean.setStrategy(strategy);
        return this;
    }

    @Override
    public RequestLimiterProperties setTimeoutInMilliseconds(long timeoutInMilliseconds) {
        requestLimiterBean.setTimeoutInMilliseconds(timeoutInMilliseconds);
        return this;
    }

    @Override
    public void addConfigChangedListener(ConfigChangedListener listener) {
        addPropertiesChangedListener(configKeySuffix, new ConfigChangedListener() {
            private final Object lock = new Object();

            @Override
            public void invoke(String key, String oldValue, String newValue) {
                synchronized (lock) {
                    RequestLimiterBean prevBean = requestLimiterBean;
//                    requestLimiterBean = getBeanValue(configKeySuffix, RequestLimiterBean.class, prevBean, true);
                }
            }
        });
    }

    @Override
    public String toJson() {
       /* try {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            builder.append("\"");
            builder.append(configKeySuffix);
            builder.append("\"");
            builder.append(":");
            builder.append(SerializerUtils.write(requestLimiterBean));
            builder.append("}");
            return builder.toString();
        } catch (IOException e) {
            logger.warn("DefaultRequestLimiterProperties toJson error" + e.getMessage());
        }*/
        return "";
    }

    private RequestLimiterBean createDefaultBean(RateLimit rateLimit) {
        RequestLimiterBean defaultBean = new RequestLimiterBean();
        if (rateLimit != null) {
            defaultBean.setActive(rateLimit.isActive());
            defaultBean.setTimeoutInMilliseconds(rateLimit.timeoutInMilliseconds());
            defaultBean.setRate(rateLimit.rate());
            defaultBean.setStrategy(rateLimit.strategy().getType());
        }
        return defaultBean;
    }

}
