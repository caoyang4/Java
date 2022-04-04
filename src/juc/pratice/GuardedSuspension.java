package src.juc.pratice;

import lombok.extern.slf4j.Slf4j;
import src.basis.io.Downloader;
import src.juc.JucUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 保护性暂停
 * join、Future 实现保护性暂停
 * @author caoyang
 */
@Slf4j
public class GuardedSuspension {
    public static List<String> names = new ArrayList<>();
    public static List<String> contents = new ArrayList<>();
    static {
        names.add("kobe");
        names.add("james");
        names.add("paul");
        contents.add("闷声发大财");
        contents.add("图样图森破");
        contents.add("谈笑风生");
    }
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Citizen("citizen"+i).start();
        }
        JucUtils.sleepSeconds(1);
        for (Integer id : MailBox.getIds()) {
            int index = new Random().nextInt(3);
            new Postman(id, names.get(index), contents.get(index)).start();
        }
    }

    public static void scrapy(){
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
@Slf4j(topic = "Citizen")
class Citizen extends Thread{
    private String name;

    public Citizen(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        GuardedObject obj = MailBox.createGuardedObject();
        log.info("{}等待收信...", name);
        Object response = obj.getResponse(5000);
        log.info("{}收到信:{}，内容：{}", name, obj.getId(), response);
    }
}

@Slf4j(topic = "Postman")
class Postman extends Thread{
    private int id;
    private String name;
    private String content;

    public Postman(int id, String name, String content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }

    @Override
    public void run() {
        GuardedObject obj = MailBox.getGuardedObject(id);
        log.info("{}送信:{}, 内容:{}", name, id, content);
        obj.setResponse(content);
    }
}

class MailBox{
    public static Map<Integer, GuardedObject> map = new ConcurrentHashMap<>();
    public static AtomicInteger id = new AtomicInteger(0);
    public static int generateId(){
        return id.getAndIncrement();
    }
    public static GuardedObject createGuardedObject(){
        int id = generateId();
        GuardedObject obj = new GuardedObject(id);
        map.put(obj.getId(), obj);
        return obj;
    }
    public static GuardedObject getGuardedObject(int id){
        // 取完就丢弃了
        return map.remove(id);
    }
    public static Set<Integer> getIds(){
        return map.keySet();
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
