package src.jvm;

/**
 * @author caoyang
 */
public class SlotGcTest2 {
    public static void main(String[] args) {
        {
            byte[] holder = new byte[64 * 1024 * 1024];
        }
        // [GC (System.gc())  70786K->66440K(251392K), 0.0020808 secs]
        // [Full GC (System.gc())  66440K->66198K(251392K), 0.0069505 secs]
        // holder的变量槽没有被其他变量复用，不会被gc
        System.gc();
    }
}
