package src.rhino.util;

import src.rhino.config.ConfigFactory;
import src.rhino.config.Configuration;

/**
 * Created by zhanjun on 2018/1/23.
 */
public class PlaceHolderSupport {

    private static PlaceHolderSupport instance = new PlaceHolderSupport();
    private Configuration configuration = ConfigFactory.getInstance();

    private String placeholderPrefix = "${";
    private String placeholderSuffix = "}";

    private PlaceHolderSupport() {

    }

    public static PlaceHolderSupport getInstance() {
        return instance;
    }

    public String getValue(String key) {
        if (key != null && key.startsWith(placeholderPrefix) && key.endsWith(placeholderSuffix)) {
            String lionKey = AppUtils.getAppName() + "." + key.substring(2, key.length() - 1);
            return configuration.getStringValue(lionKey, "");
        }
        return key;
    }
}
