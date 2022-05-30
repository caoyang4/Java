package src.rhino.onelimiter.matcher;

import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import src.rhino.util.Preconditions;

/**
 * Created by zhanjun on 2018/6/28.
 */
public class ParamRule {

    private String name;
    private String pattern;
    private ParamMatchMode matchMode;

    /**
     * 参数匹配器
     */
    private ParamMatcher matcher;

    /**
     * 初始化匹配器
     */
    public void init() {
        Preconditions.checkArgument(matchMode != null, "matchMode can't be null");
        Preconditions.checkArgument(pattern != null, "pattern can't be null");

        switch (matchMode){
            case KEY_NOT_EXIST:
                this.matcher = matchMode.createMatcher(name);
                break;
            default:
                //对参数值进行字符串匹配
                this.matcher = matchMode.createMatcher(pattern);
        }
    }

    public boolean match(Map<String, String> reqParams){
        switch (matchMode){
            case KEY_NOT_EXIST:
                if(CollectionUtils.isEmpty(reqParams)){
                    return true;
                }
                return matchKeysNotExist(reqParams.keySet());
            default:
                if(CollectionUtils.isEmpty(reqParams)){
                    return false;
                }
                return matchStringValue(reqParams.get(name));
        }
    }

    /**
     * 匹配请求参数
     * @param value
     * @return
     */
    private boolean matchStringValue(String value) {
        if (value == null) {
            return false;
        }
        return matcher.match(value);
    }

    /**
     * 匹配某些参数是否存在
     * @param currentKeys
     * @return
     */
    private boolean matchKeysNotExist(Set<String> currentKeys){
        return matcher.match(currentKeys);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public ParamMatchMode getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(ParamMatchMode matchMode) {
        this.matchMode = matchMode;
    }
}
