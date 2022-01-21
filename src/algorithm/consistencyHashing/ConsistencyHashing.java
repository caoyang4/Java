package src.algorithm.consistencyHashing;

import com.sun.istack.internal.NotNull;

import java.util.*;

/**
 * @author caoyang
 */
public class ConsistencyHashing {
    /**
     * 物理节点
     */
    private Set<String> serverNodes = new TreeSet<>();
    private TreeMap<Long, String> nodeMap = new TreeMap<>();
    /**
     * 物理节点至虚拟节点的复制倍数
     * virtualCopies = 1 表示无虚拟节点
     */
    private static final int VIRTUAL_COPIES = 32;

    {
        serverNodes.add("192.168.1.101");
        serverNodes.add("192.168.1.102");
        serverNodes.add("192.168.1.103");
        serverNodes.add("192.168.1.104");

        serverNodes.forEach(this::addServerNode);
    }

    private Long getHash(@NotNull String serverNode){
        final int p = 16777619;
        Long hash = 2166136261L;
        for (int i = 0; i < serverNode.length(); i++) {
            hash = (hash ^ serverNode.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash << 17;
        hash += hash << 5;
        return hash < 0 ? Math.abs(hash) : hash;
    }

    public void addServerNode(@NotNull String node){
        for (int i = 0; i < VIRTUAL_COPIES; i++) {
            long hash = getHash(node + "#" + i);
            nodeMap.put(hash, node);
        }
    }

    public void removeServerNode(@NotNull String node){
        for (int i = 0; i < VIRTUAL_COPIES; i++) {
            long hash = getHash(node + "#" + i);
            nodeMap.remove(hash);
        }
    }

    public String getNode(String object){
        long hash = getHash(object);
        SortedMap<Long, String> tailMap = nodeMap.tailMap(hash);
        long key = tailMap.isEmpty() ? nodeMap.firstKey() : tailMap.firstKey();
        return nodeMap.get(key);
    }

    public void showNodeDistribution(String tag, int min, int max){
        Map<String, Integer> objMap = new TreeMap<>();
        for (int i = min; i < max; i++) {
            String node = getNode(Integer.toString(i));
            Integer count = objMap.get(node);
            objMap.put(node, count == null ? 0 : ++count);
        }

        double totalCount = max - min + 1;
        System.out.println("======== " + tag + " ========");
        for(Map.Entry<String, Integer> entry : objMap.entrySet()){
            long percent = (int) (100 * entry.getValue() / totalCount);
            System.out.println("IP=" + entry.getKey() + ": Rate=" + percent + "%");
        }
    }

    public static void main(String[] args) {
        ConsistencyHashing consistencyHashing = new ConsistencyHashing();
        // 初始情况
        consistencyHashing.showNodeDistribution("初始情况", 0, 65536);

        // 删除物理节点
        consistencyHashing.removeServerNode("192.168.1.103");
        consistencyHashing.showNodeDistribution("删除物理节点", 0, 65536);

        // 添加物理节点
        consistencyHashing.addServerNode("192.168.1.108");
        consistencyHashing.showNodeDistribution("添加物理节点", 0, 65536);
    }

}
