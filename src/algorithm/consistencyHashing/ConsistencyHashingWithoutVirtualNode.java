package src.algorithm.consistencyHashing;


import com.sun.istack.internal.NotNull;

import java.util.*;

/**
 * 不带虚拟节点的一致性Hash算法
 * @author caoyang
 */
public class ConsistencyHashingWithoutVirtualNode {
    private static List<String> serverList = new ArrayList<>();
    private static TreeMap<Long, String> treeMap = new TreeMap<>();

    static {
        serverList.add("192.168.0.1:3306");
        serverList.add("192.168.0.2:3306");
        serverList.add("192.168.0.3:3306");
        serverList.add("192.168.0.4:3306");

        serverList.forEach(server -> {
            Long hash = getHash(server);
            System.out.println("["+server+"]加入Map中，其 Hash 值：" + hash);
            treeMap.put(hash, server);
        });
    }

    /**
     * 32位的 Fowler-Noll-Vo 哈希算法
     * https://en.wikipedia.org/wiki/Fowler–Noll–Vo_hash_function
     * @param server 服务器IP地址
     * @return hash
     */
    private static Long getHash(@NotNull String server){
        final int p = 16777619;
        long hash = 2166136261L;
        for (int i = 0; i < server.length(); i++) {
            hash = (hash ^ server.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        return hash < 0 ? Math.abs(hash) : hash;
    }

    public String getSeverNode(String object){
        long hash = getHash(object);
        // 得到大于该Hash值的所有Map
        SortedMap<Long, String> map = treeMap.tailMap(hash);
        //如果没有比该key的hash值大的，则取第一个node，否则顺时针取离node最近的那个结点
        return map.isEmpty() ? treeMap.get(treeMap.firstKey()) : map.get(map.firstKey());
    }

    public static void main(String[] args) {
        ConsistencyHashingWithoutVirtualNode consistencyHash = new ConsistencyHashingWithoutVirtualNode();

        String[] citys = new String[] {"上海", "北京", "长沙", "广州",
                                       "常德", "杭州", "成都", "重庆",
                                       "厦门", "深圳", "武汉", "南京"};

        for (String city : citys) {
            System.out.println("[" + city + "]的hash值为" + getHash(city) + ", 被路由到结点[" + consistencyHash.getSeverNode(city) + "]");
        }
    }


}
