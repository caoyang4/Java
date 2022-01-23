package src.designPattern.iterator;

public class Main {
    public static void main(String[] args) {
        MyCollection myCollection = new MyList();
        for (int i = 0; i < 25; i++) {
            myCollection.add("james " + i);
        }

        System.out.println("容器长度：" + myCollection.size());


        MyIterator iterator = myCollection.iterator();
        while (iterator.hasNext()){
            System.out.println(iterator.next());
        }
    }
}
