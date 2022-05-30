package src.rhino.util;

//import src.lion.Environment;
import com.mysql.cj.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by zhanjun on 2017/7/7.
 */
public class AppUtils {

    private static String originalAppName = "rhino";
    private static String forceAppName = null;
    private static String defaultAppName = null;
    private static String ip = getFirstLocalIp();
    private static String hostName = doGetHostName();
    public static final String DEFAULT_CELL = "default_cell";
    private static String setName;
    private static String defaultAppNamePath = "/data/webapps/appkeys";

    static {
        /*try {
            setName = Environment.getCell();
        } catch (Exception e) {
            //ignore exception
        }*/

        if (StringUtils.isNullOrEmpty(setName)) {
            setName = DEFAULT_CELL;
        }
        readAppKey(defaultAppNamePath);
    }

    private static void readAppKey(String path) {
        try {
            File file = new File(path);
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(file));) {
                // 建立一个输入流对象reader
                BufferedReader br = new BufferedReader(reader);
                defaultAppName = br.readLine();
            }
        } catch (Exception e) {
            //ignore exception
        }
    }


    public static String getDefaultAppName() {
        return defaultAppName;
    }

    /**
     * 强制设置appKey
     * 这笔保证这行代码在应用启动最先执行，否则无效
     *
     * @param appName
     */
    public static void initializeAppKeyForce(String appName) {
        if (StringUtils.isNullOrEmpty(appName)) {
            forceAppName = appName;
        }
    }

    /**
     * 获取rhinokey的set suffix
     * 熔断器，限流器，资源隔离的分set配置
     *
     * @return
     */
    public static String getSetSuffix() {
        return DEFAULT_CELL.equals(setName) ? "" : "-" + setName;
    }

    /**
     * 本机set名称
     *
     * @return
     */
    public static String getSet() {
        return setName;
    }

    public static boolean isDefaultCell() {
        return DEFAULT_CELL.equals(setName);
    }

    /**
     * 获取appKey
     *
     * @return
     */
    public static String getAppName() {
        if (forceAppName != null) {
            return forceAppName;
        }
        return originalAppName;
    }

    /**
     * 本机地址
     *
     * @return
     */
    public static String getLocalIp() {
        return ip;
    }

    /**
     * 主机名称
     *
     * @return
     */
    public static String getHostName() {
        return hostName;
    }


    private static String doGetHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            //ignore exception
            return "UnknownHost";
        }
    }

    /**
     * 如果有ipv4 优先获取ipv4
     * @return
     */
    private static String getFirstLocalIp() {
        List<String> allNoLoopbackAddresses = getAllLocalIp();
        if (allNoLoopbackAddresses.isEmpty()) {
            throw new IllegalStateException("Sorry, seems you don't have a network card :( ");
        }
        return allNoLoopbackAddresses.get(allNoLoopbackAddresses.size() - 1);
    }

    private static List<InetAddress> getAllLocalAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            List<InetAddress> addresses = new ArrayList<InetAddress>();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    addresses.add(inetAddress);
                }
            }

            return addresses;
        } catch (SocketException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 多网卡机器 ipv6在前  ipv4在后
     * @return
     */
    private static List<String> getAllLocalIp() {
        List<String> noLoopbackAddressesIpv4 = new ArrayList<String>();
        List<String> noLoopbackAddressesOther = new ArrayList<String>();
        List<InetAddress> allInetAddresses = getAllLocalAddress();

        for (InetAddress address : allInetAddresses) {
            if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                if (address instanceof Inet4Address) {
                    noLoopbackAddressesIpv4.add(address.getHostAddress());
                } else {
                    noLoopbackAddressesOther.add(address.getHostAddress());
                }
            }
        }
        noLoopbackAddressesOther.addAll(noLoopbackAddressesIpv4);
        return noLoopbackAddressesOther;
    }
}
