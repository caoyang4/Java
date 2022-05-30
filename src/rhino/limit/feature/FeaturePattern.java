package src.rhino.limit.feature;

/**
 * Created by zhanjun on 2017/8/13.
 */
public class FeaturePattern {

    /**
     * feature name
     */
    private String name;

    /**
     * feature value
     */
    private String value;

    /**
     * pattern
     * @see FeaturePatternEnum
     */
    private int patternMode;

    public FeaturePattern() {
    }

    public FeaturePattern(String name, String value, int patternMode) {
        this.name = name;
        this.value = value;
        this.patternMode = patternMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getPatternMode() {
        return patternMode;
    }

    public void setPatternMode(int patternMode) {
        this.patternMode = patternMode;
    }
}
