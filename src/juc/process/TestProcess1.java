package src.juc.process;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * java 进程创建1
 * ProcessBuilder创建
 */
public class TestProcess1 {
    public static void main(String[] args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("cat", "IdeaProjects.iml");
        Process process = builder.start();
        BufferedInputStream bis = new BufferedInputStream(process.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(bis));
        String line;
        while ((line = br.readLine()) != null){
            System.out.println(line);
        }

    }
}
