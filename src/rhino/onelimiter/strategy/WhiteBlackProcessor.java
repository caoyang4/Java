package src.rhino.onelimiter.strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.util.CollectionUtils;

/**
 * Created by zhanjun on 2018/4/20.
 */
public class WhiteBlackProcessor {

    /**
     * 处理后的名单（普通和正则）
     */
    private Set<String> normalList;
    private List<Pattern> regexList;

    public WhiteBlackProcessor(List<WhiteBlackEntry> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (WhiteBlackEntry entry : list) {
            String name = entry.getName();
            if (entry.isRegex()) {
                if (regexList == null) {
                    regexList = new ArrayList<>();
                }
                regexList.add(Pattern.compile(name));
            } else {
                if (normalList == null) {
                    normalList = new HashSet<>();
                }
                normalList.add(name);
            }
        }
    }

    /**
     * 检查是否包含
     *
     * @param path
     * @return
     */
    public boolean contains(String path) {
        if (normalList != null && normalList.contains(path)) {
            return true;
        }

        if (regexList != null) {
            for (Pattern pattern : regexList) {
                if (pattern.matcher(path).matches()) {
                    return true;
                }
            }
        }
        return false;
    }
}
