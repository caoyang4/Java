package src;

public class WelcomeJavaWorld {
  // JVM在运行Java应用程序的时候，首先会调用main方法，
  // 调用时不实例化这个类的对象，需要加载类，即会进行类初始化，但不需要类对象初始化
  // 而是通过类名直接调用因此需要是限制为public static
  // 并且jvm有限制，不能有返回值，因此返回值类型为void。遵循java的规范，main()方法中必须有一个入参，类型必须String[]
  public static void main(String[] args){
    // 欢迎入坑Java，开启从入门到放弃之路
    System.out.println("Hi. Young, welcome to java world!"
    );
  }
}
