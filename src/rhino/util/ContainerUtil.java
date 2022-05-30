package src.rhino.util;


import java.io.File;

/**
 * @description: ContainerUtil
 * @author: zhangxiudong
 * @date: 2021-05-31
 **/
public class ContainerUtil {

    public static boolean isDocker() {
        String hostname ="localhost";
        if (hostname.startsWith("set-")) {
            return true;
        }
        return new File("/data/webapps/hulk").exists();
    }
}
