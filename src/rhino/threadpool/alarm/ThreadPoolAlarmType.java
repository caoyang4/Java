package src.rhino.threadpool.alarm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zmz on 2020/11/12.
 */
public enum ThreadPoolAlarmType {
    TOO_MANNY_THREAD(0, "线程数量（PoolSize）太多"),
    QUEUE_TOO_BIG(1, "阻塞任务数太多"),
    TOO_MANNY_REJECT(2, "拒绝任务数太多"),
    TOO_MANNY_ACTIVE_THREAD(3, "活跃线程数量（ActiveCount）太多");

    private int bit;
    private String desc;

    private ThreadPoolAlarmType(int bit, String desc) {
        this.bit = bit;
        this.desc = desc;
    }

    public int getValue(){
        return 1 << this.bit;
    }

    public String getDesc(){
        return this.desc;
    }

    /**
     * 将告警编码翻译成告警类型
     * @param source
     * @return
     */
    public static List<ThreadPoolAlarmType> readValue(int source){
        if(source <= 0){
            return new ArrayList<>();
        }

        List<ThreadPoolAlarmType> types = new ArrayList<>();
        if((source & TOO_MANNY_THREAD.getValue()) > 0){
            types.add(TOO_MANNY_THREAD);
        }
        if((source & QUEUE_TOO_BIG.getValue()) > 0){
            types.add(QUEUE_TOO_BIG);
        }
        if((source & TOO_MANNY_REJECT.getValue()) > 0){
            types.add(TOO_MANNY_REJECT);
        }
        if((source & TOO_MANNY_ACTIVE_THREAD.getValue()) > 0){
            types.add(TOO_MANNY_ACTIVE_THREAD);
        }
        return types;
    }
}
