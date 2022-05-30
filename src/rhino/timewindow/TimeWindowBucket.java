package src.rhino.timewindow;

/**
 * Created by zmz on 2020/10/19.
 */
public class TimeWindowBucket<T extends TimeWindowData> {
    private long startTime;
    private long length;
    private T data;

    public TimeWindowBucket(long startTime, long length, T data){
        this.startTime = startTime;
        this.length = length;
        this.data = data;
    }

    public long getStartTime(){
        return this.startTime;
    }

    public T getData(){
        return data;
    }


    public boolean isValid(long time){
        if(this.startTime <= 0){
            return true;
        }

        return time - this.startTime > this.length;
    }

    public TimeWindowBucket reset(long startTime){
        data.reset();
        this.startTime = startTime;
        return this;
    }

    @Override
    public String toString() {
        return "{" +
                "\"startTime\":" + startTime +
                "; \"length\":" + length +
                "; \"data\":" + data.toString() +
                '}';
    }
}
