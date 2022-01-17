package src.socket.tcp;

import java.io.Serializable;

/**
 * @author caoyang
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String chineseName;

    public User(String name, String chineseName) {
        this.name = name;
        this.chineseName = chineseName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", chineseName='" + chineseName + '\'' +
                '}';
    }
}
