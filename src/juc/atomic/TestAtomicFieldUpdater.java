package src.juc.atomic;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 内部利用 CAS+反射
 * @author caoyang
 */
public class TestAtomicFieldUpdater {
    static class Address{
        volatile String street;

        @Override
        public String toString() {
            return "Address{" +
                    "street='" + street + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) {
        AtomicReferenceFieldUpdater ref = AtomicReferenceFieldUpdater.newUpdater(
                Address.class, String.class,"street"
        );
        Address addr = new Address();
        System.out.println(addr);
        System.out.println(ref.compareAndSet(addr, null, "luffy"));
        System.out.println(addr);
    }
}
