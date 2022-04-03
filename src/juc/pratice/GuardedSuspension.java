package src.juc.pratice;

import lombok.extern.slf4j.Slf4j;
import src.basis.io.Downloader;

import java.io.IOException;
import java.util.List;

/**
 * 保护性暂停
 * join、Future 实现保护性暂停
 * @author caoyang
 */
@Slf4j
public class GuardedSuspension {
    public static void main(String[] args) {
        GuardedObject guardedObject = new GuardedObject(1);
        new Thread(()-> guardedObject.getResponse(5000),"t1").start();
        new Thread(() -> {
            try {
                log.info("执行爬虫");
                List<String> contents = Downloader.download();
                guardedObject.setResponse(contents);
            } catch (IOException e) {
                e.printStackTrace();
            }
        },"t2").start();
    }

}

@Slf4j(topic = "GuardedObject")
class GuardedObject{
    private int id;

    public GuardedObject(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    Object response;

    public Object getResponse(long timeout) {
        long base = System.currentTimeMillis();
        long passTime = 0;
        synchronized (this) {
            while (response == null){
                long delay = timeout - passTime;
                if (delay < 0){
                    break;
                }
                try {
                    this.wait(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                passTime = System.currentTimeMillis() - base;
            }
        }
        log.info("收到响应...");
        return response;
    }


    public void setResponse(Object response) {
        synchronized (this) {
            this.response = response;
            this.notifyAll();
        }
    }
}
