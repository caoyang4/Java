package src.rhino.onelimiter.matcher;

import src.rhino.util.MtraceUtils;

/**
 * Created by zhen on 2019/1/11.
 */
public class TestRequestMather extends AbstractParamMatcher {

    @Override
    public boolean match(String value) {
        return MtraceUtils.isTest();
    }

}
