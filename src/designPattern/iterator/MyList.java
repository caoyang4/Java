package src.designPattern.iterator;

/**
 * @author caoyang
 */
public class MyList implements MyCollection{
    private Object[] list = new Object[10];
    private volatile int index = 0;
    private volatile int expandCount;

    @Override
    public void add(Object o) {
        if(index == size()){
            Object[] newList = new Object[size() * 2];
            System.arraycopy(list,0,newList,0,size());
            System.out.println("数组扩容 " + (++expandCount) + "次");
            list = newList;
        }
        list[index] = o;
        index++;
    }

    @Override
    public MyIterator iterator(){
        return new MyListItr();
    }

    @Override
    public int size() {
        return list.length;
    }

    private class MyListItr implements MyIterator{
        private int cursor;
        @Override
        public boolean hasNext() {
            return cursor < index;
        }

        @Override
        public Object next() {
            return list[cursor++];
        }
    }
}
