package src.rhino.Switch;

import src.rhino.onelimiter.matcher.ParamMatchMode;
import src.rhino.onelimiter.matcher.ParamMatcher;

/**
 * Created by zhen on 2019/1/11.
 */
public class RhinoSwitchBean {

    private boolean active;
    private ParamMatchMode matchMode;
    private String pattern;
    private String grayValue;
    private ParamMatcher matcher;

    public RhinoSwitchBean(boolean active) {
        this.active = active;
    }

    public RhinoSwitchBean() {
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ParamMatchMode getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(ParamMatchMode matchMode) {
        this.matchMode = matchMode;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getGrayValue() {
        return grayValue;
    }

    public void setGrayValue(String grayValue) {
        this.grayValue = grayValue;
    }

    public boolean match() {
        try {
            return matcher.match("");
        } catch (Exception e) {
            return false;
        }
    }

    public void createMatcher() {
        if (isActive() && matchMode != null) {
            this.matcher = matchMode.createMatcher(pattern);
        }
    }

    public String toJson() {
        return "{\"active\":" + active +
                ",\"matchMode\":" + "\"" + matchMode + "\"" +
                ",\"pattern\":" + "\"" + pattern + "\"" +
                ",\"grayValue\":" + "\"" + grayValue + "\"" + "}";
    }
}
