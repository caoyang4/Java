package src.rhino.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author zhanjun
 */
public class PropertiesHolder {

    private static Properties properties = new Properties();

    static {
        InputStream inputStream = PropertiesHolder.class.getClassLoader().getResourceAsStream("rhino-version.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            //ignore exception
        } finally {
        	if(inputStream != null) {
        		try {
					inputStream.close();
				} catch (IOException e) {
					//ignore exception
				}
        	}
        }
    }

    /**
     * @return
     */
    public static String getVersion() {
        String version = properties.getProperty("version");
        if (version == null || "".equals(version)) {
            throw new IllegalArgumentException("version获取失败，请确认rhino-version.properties文件是否存在");
        }
        return version;
    }
}
