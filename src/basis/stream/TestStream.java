package src.basis.stream;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author caoyang
 */
public class TestStream {

    public static void createStream(){
        /*
           通过java.util.Collection.stream()方法用集合创建流
         */
        List<String> stringList = Arrays.asList("a", "b", "c");
        // 串行流
        Stream<String> stringStream = stringList.stream();
        stringStream.forEach(s -> System.out.print(s + "\t"));
        System.out.println();
        // 并行流
        Stream<String> stringParallelStream = stringList.parallelStream();
        stringParallelStream.forEach(s -> System.out.print(s + "\t"));
        /*
            使用java.util.Arrays.stream(T[] array)方法用数组创建流
         */
        System.out.println();
        int[] ints = new int[] {1, 2, 3};
        IntStream intStream = Arrays.stream(ints);
        intStream.forEach(s -> System.out.print(s + "\t"));

        /*
            使用Stream的静态方法：of()、iterate()、generate()
         */
        System.out.println();
        Stream<Integer> integerStream = Stream.of(1,2,3,4,5);
        integerStream.forEach(s -> System.out.print(s + "\t"));
        System.out.println();
        Stream<Integer> integerStream1 = Stream.iterate(0, (x) -> x+5).limit(5);
        integerStream1.forEach(s -> System.out.print(s + "\t"));
        System.out.println();
        Stream<Double> doubleStream = Stream.generate(Math::random).limit(4);
        doubleStream.forEach(s -> System.out.print(s + "\t"));

    }

    public static void findAndMatch(){
        List<Integer> list = Arrays.asList(7, 6, 9, 3, 8, 2, 1);

        // 遍历输出符合条件的元素
        list.stream().filter(x -> x > 6).forEach(s -> System.out.print(s + "\t"));
        System.out.println();
        // 匹配第一个
        Optional<Integer> findFirst = list.stream().filter(x -> x > 6).findFirst();
        // 匹配任意（适用于并行流）
        Optional<Integer> findAny = list.parallelStream().filter(x -> x > 6).findAny();
        // 是否包含符合特定条件的元素
        boolean anyMatch = list.stream().anyMatch(x -> x < 6);
        System.out.println("匹配第一个值：" + findFirst.get());
        System.out.println("匹配任意一个值：" + findAny.get());
        System.out.println("是否存在大于6的值：" + anyMatch);
    }

    public static void filter(){
        /*
            筛选（filter）
         */
        List<Integer> list = Arrays.asList(6, 7, 3, 8, 1, 2, 9);
        Stream<Integer> stream = list.stream();
        stream.filter(x -> x > 7).forEach(s -> System.out.print(s + "\t"));
        System.out.println();
    }

    public static void aggregate(){
        /*
        聚合（max/min/count)
         */
        System.out.println("case 1");
        List<String> list = Arrays.asList("james", "kobe", "wade", "paul", "kevin", "michael");
        Optional<String> max = list.stream().max(Comparator.comparing(String::length));
        System.out.println("最长的字符串：" + max.get());

        List<Integer> integers = Arrays.asList(7, 6, 9, 4, 11, 6);

        System.out.println("case 2");
        // 自然排序
        Optional<Integer> integerMax = integers.stream().max(Integer::compareTo);
        // 自定义排序
        Optional<Integer> integerMax2 = integers.stream().max(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        System.out.println("自然排序的最大值：" + integerMax.get());
        System.out.println("自定义排序的最大值：" + integerMax2.get());

        long count = integers.stream().filter(x -> x > 6).count();
        System.out.println("list中大于6的元素个数：" + count);
    }

    public static void map(){
        /*
            映射 (map/flatMap)
         */
        System.out.println("case 1");
        String[] strArr = { "beijing", "shanghai", "changsha", "changde" };
        List<String> strList = Arrays.stream(strArr).map(String::toUpperCase).collect(Collectors.toList());

        List<Integer> intList = Arrays.asList(1, 3, 5, 7, 9, 11);
        List<Integer> intListNew = intList.stream().map(x -> x + 3).collect(Collectors.toList());

        System.out.println("每个元素大写：" + strList);
        System.out.println("每个数字加3：" + intListNew);

        System.out.println("case 2");
        List<String> list = Arrays.asList("m,k,l,a", "1,3,5,7");
        List<String> listNew = list.stream().flatMap(s -> {
            // 将每个元素转换成一个stream
            String[] split = s.split(",");
            Stream<String> s2 = Arrays.stream(split);
            return s2;
        }).collect(Collectors.toList());

        System.out.println("处理前的集合：" + list);
        System.out.println("处理后的集合：" + listNew);
    }

    public static void reduce(){
        /*
        归约 (reduce)
         */
        List<Integer> list = Arrays.asList(1,2,3,4,5);
        // 求和方式1
        Optional<Integer> sum = list.stream().reduce((x, y) -> x + y);
        // 求和方式2
        Optional<Integer> sum2 = list.stream().reduce(Integer::sum);
        // 求和方式3
        Integer sum3 = list.stream().reduce(0, Integer::sum);

        // 求乘积
        Optional<Integer> product = list.stream().reduce((x, y) -> x * y);

        // 求最大值方式1
        Optional<Integer> max = list.stream().reduce((x, y) -> x > y ? x : y);
        // 求最大值写法2
        Integer max2 = list.stream().reduce(1, Integer::max);

        System.out.println("list求和：" + sum.get() + "," + sum2.get() + "," + sum3);
        System.out.println("list求积：" + product.get());
        System.out.println("list求最大值：" + max.get() + "," + max2);
    }

    public static void mergeAndSkip(){
        /*
        流也可以进行合并、去重、限制、跳过等操作
         */
        String[] arr1 = { "a", "b", "c", "d" };
        String[] arr2 = { "d", "e", "f", "g" };

        Stream<String> stream1 = Stream.of(arr1);
        Stream<String> stream2 = Stream.of(arr2);
        // concat:合并两个流 distinct：去重
        List<String> newList = Stream.concat(stream1, stream2).distinct().collect(Collectors.toList());
        // limit：限制从流中获得前n个数据
        List<Integer> collect = Stream.iterate(1, x -> x + 2).limit(10).collect(Collectors.toList());
        // skip：跳过前n个数据
        List<Integer> collect2 = Stream.iterate(1, x -> x + 2).skip(1).limit(5).collect(Collectors.toList());

        System.out.println("流合并：" + newList);
        System.out.println("limit：" + collect);
        System.out.println("skip：" + collect2);
    }

    public static void collect(){
        /*
            collect
         */
        // 归集 (toList/toSet/toMap)
        System.out.println("归集 (toList/toSet/toMap)");
        List<Integer> list = Arrays.asList(1, 6, 3, 4, 6, 7, 9, 6, 20);
        List<Integer> listNew = list.stream().filter(x -> x % 2 == 0).collect(Collectors.toList());
        Set<Integer> set = list.stream().filter(x -> x % 2 == 0).collect(Collectors.toSet());

        List<Person> personList = new ArrayList<Person>();
        personList.add(new Person("Tom", 8900, 23, "male", "New York"));
        personList.add(new Person("Jack", 7000, 25, "male", "Washington"));
        personList.add(new Person("Lily", 7800, 21, "female", "Washington"));
        personList.add(new Person("Anni", 8200, 24, "female", "New York"));

        Map<?, Person> map = personList.stream().filter(p -> p.getSalary() > 8000)
                .collect(Collectors.toMap(Person::getName, p -> p));
        System.out.println("toList:" + listNew);
        System.out.println("toSet:" + set);
        System.out.println("toMap:" + map);

        System.out.println("接合 (joining)");
        String names = personList.stream().map(person -> person.getName()).collect(Collectors.joining(","));
        System.out.println("所有员工的姓名: "+names);

        List<String> list1 = Arrays.asList("A", "B", "C");
        String string = list1.stream().collect(Collectors.joining("-"));
        System.out.println("拼接后的字符串：" + string);
    }

    public static void main(String[] args) {
        System.out.println("【1】创建流......");
        createStream();
        System.out.println();

        System.out.println("【2】遍历/匹配（foreach/find/match）");
        findAndMatch();
        System.out.println();

        System.out.println("【3】筛选（filter）");
        filter();
        System.out.println();

        System.out.println("【4】聚合（max/min/count)");
        aggregate();
        System.out.println();

        System.out.println("【5】映射 (map/flatMap)");
        map();
        System.out.println();

        System.out.println("【6】归约 (reduce)");
        reduce();
        System.out.println();

        System.out.println("【7】流也可以进行合并、去重、限制、跳过等操作");
        mergeAndSkip();
        System.out.println();

        System.out.println("【8】收集 (collect)");
        collect();
        System.out.println();

        System.out.println("======Person类流式操作=======");
        List<Person> personList = new ArrayList<>();
        personList = createPersonList(personList);
        filterPerson(personList);
        System.out.println();
        getMaxSalary(personList);
        System.out.println();
        addSalary(personList);
        System.out.println();
        sortedPerson(personList);
        System.out.println();
        staticsPerson(personList);
        System.out.println();
        groupPerson(personList);
    }

    public static List<Person> createPersonList(List<Person> personList){
        personList.add(new Person("Tom", 8900, 18, "male", "New York"));
        personList.add(new Person("Jack", 7000, 30,"male", "Washington"));
        personList.add(new Person("Lily", 7800, 35,"female", "Washington"));
        personList.add(new Person("Anni", 8200, 50,"female", "New York"));
        personList.add(new Person("Owen", 9500, 70,"male", "New York"));
        personList.add(new Person("Alisa", 7900, 10,"female", "New York"));
        return personList;
    }

    public static void filterPerson(List<Person> personList){
        System.out.println("筛选员工中工资高于 8000 的人，并形成新的集合");
        List<String> nameList = personList.stream().filter(
                person -> person.getSalary() > 8000
        ).map(Person::getName).collect(Collectors.toList());
        System.out.print("高于8000的员工姓名：" + nameList);
    }

    public static void getMaxSalary(List<Person> personList){
        Optional<Person> max = personList.stream().max(Comparator.comparingInt(Person::getSalary));
        System.out.println("员工工资最高的人：" + max.get());
    }

    public static void addSalary(List<Person> personList){

        System.out.println("【方式 1】不改变原来员工集合的方式");
        List<Person> lists1 = personList.stream().map(
                person -> {
                    Person personNew = new Person(person.getName(), 0, 0, null, null);
                    personNew.setSalary(person.getSalary() + 1000);
                    return personNew;
                }).collect(Collectors.toList());
        System.out.println("\torigin Salary:" + personList.get(0).getName() + "-->" + personList.get(0).getSalary());
        System.out.println("\tafter addSalary:" + lists1.get(0).getName() + "-->" + lists1.get(0).getSalary());


        System.out.println("【方式 2】改变原来员工集合的方式");
        List<Person> lists2 = personList.stream().map(
                person -> {
                    person.setSalary(person.getSalary() + 1000);
                    return person;
                }).collect(Collectors.toList());
        System.out.println("\torigin Salary:" + personList.get(0).getName() + "-->" + personList.get(0).getSalary());
        System.out.println("\tafter addSalary:" + lists1.get(0).getName() + "-->" + lists2.get(0).getSalary());
    }

    public static void sortedPerson(List<Person> personList){
        /**
         * 将员工按工资由高到低（工资一样则按年龄由大到小）排序
         */
        // 按工资升序排序（自然排序）
        List<String> newList = personList.stream().sorted(Comparator.comparing(Person::getSalary)).map(Person::getName)
                .collect(Collectors.toList());
        // 按工资倒序排序
        List<String> newList2 = personList.stream().sorted(Comparator.comparing(Person::getSalary).reversed())
                .map(Person::getName).collect(Collectors.toList());
        // 先按工资再按年龄升序排序
        List<String> newList3 = personList.stream()
                .sorted(Comparator.comparing(Person::getSalary).thenComparing(Person::getAge)).map(Person::getName)
                .collect(Collectors.toList());
        // 先按工资再按年龄自定义排序（降序）
        List<String> newList4 = personList.stream().sorted((p1, p2) -> {
            if (p1.getSalary() == p2.getSalary()) {
                return p2.getAge() - p1.getAge();
            } else {
                return p2.getSalary() - p1.getSalary();
            }
        }).map(Person::getName).collect(Collectors.toList());

        System.out.println("按工资升序排序：" + newList);
        System.out.println("按工资降序排序：" + newList2);
        System.out.println("先按工资再按年龄升序排序：" + newList3);
        System.out.println("先按工资再按年龄自定义降序排序：" + newList4);
    }

    public static void staticsPerson(List<Person> personList){
        // 求总数
        Long count = personList.stream().collect(Collectors.counting());
        // 求平均工资
        Double average = personList.stream().collect(Collectors.averagingDouble(Person::getSalary));
        // 求最高工资
        Optional<Integer> max = personList.stream().map(Person::getSalary).collect(Collectors.maxBy(Integer::compare));
        // 求工资之和
        Integer sum = personList.stream().collect(Collectors.summingInt(Person::getSalary));
        // 一次性统计所有信息
        DoubleSummaryStatistics collect = personList.stream().collect(Collectors.summarizingDouble(Person::getSalary));

        System.out.println("员工总数：" + count);
        System.out.println("员工平均工资：" + average);
        System.out.println("员工最高工资：" + max);
        System.out.println("员工工资总和：" + sum);
        System.out.println("员工工资所有统计：" + collect);
    }

    public static void groupPerson(List<Person> personList){
        // 将员工按薪资是否高于8000分组
        Map<Boolean, List<Person>> part = personList.stream().collect(Collectors.partitioningBy(x -> x.getSalary() > 8000));
        // 将员工按性别分组
        Map<String, List<Person>> group = personList.stream().collect(Collectors.groupingBy(Person::getSex));
        // 将员工先按性别分组，再按地区分组
        Map<String, Map<String, List<Person>>> group2 = personList.stream().collect(Collectors.groupingBy(Person::getSex, Collectors.groupingBy(Person::getArea)));
        System.out.println("员工按薪资是否大于8000分组情况：" + part);
        System.out.println("员工按性别分组情况：" + group);
        System.out.println("员工按性别、地区：" + group2);
    }

}

class Person {
    private String name;
    private int salary;
    private int age;
    private String sex;
    private String area;

    // 构造方法
    public Person(String name, int salary, int age, String sex, String area) {
        this.name = name;
        this.salary = salary;
        this.age = age;
        this.sex = sex;
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public int getSalary() {
        return salary;
    }

    public int getAge() {
        return age;
    }

    public String getSex() {
        return sex;
    }

    public String getArea() {
        return area;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setArea(String area) {
        this.area = area;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", salary=" + salary +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", area='" + area + '\'' +
                '}';
    }
}