package src.juc.process;

import java.io.*;

/**
 * java 进程创建
 * Runtime创建
 */
public class TestProcess2 {
    public static void main(String[] args) throws IOException {
        final Runtime run = Runtime.getRuntime();
        Process process = run.exec("ls -rtl");
        //获取子进程输出流
        BufferedInputStream in = new BufferedInputStream(process.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String s;
        while ((s = br.readLine()) != null) {
            System.out.println(s);
        }

    }
}
