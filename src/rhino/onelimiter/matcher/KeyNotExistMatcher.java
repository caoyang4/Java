package src.rhino.onelimiter.matcher;

import java.util.Set;

/**
 * 判断参数是否存在
 * Created by zmz on 2019/8/27.
 */
public class KeyNotExistMatcher extends AbstractParamMatcher {

    private Set<String> keys;

    public KeyNotExistMatcher(Set<String> keys){
        this.keys = keys;
    }

    @Override
    public boolean match(Set<String> values) {
        for(String key : keys){
          if(values.contains(key)){
              return false;
          }
        }
        return true;
    }
}
