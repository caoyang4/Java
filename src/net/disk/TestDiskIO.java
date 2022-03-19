package src.net.disk;

import java.io.*;

/**
 * 磁盘IO
 * @author caoyang
 */
public class TestDiskIO {
    public static void printAllFilesName(File dir, int index){
        if(dir == null || !dir.exists()){
            return;
        }
        if(dir.isFile()){
            System.out.println(getTab(index)+dir.getName());
        }else {
            System.out.println(getTab(index)+dir.getName()+":");
            for (File file : dir.listFiles()) {
                printAllFilesName(file, index+1);
            }
        }
    }
    public static String getTab(int index){
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < index; i++) {
            sb.append("\t");
        }
        return sb.toString();
    }

    public static void printFileContent(String pathName) throws IOException {
        try(
            BufferedReader bufferedReader = new BufferedReader(new FileReader(pathName));
        ){
            String line = null;
            while ((line = bufferedReader.readLine()) != null){
                System.out.println(line);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        File file = new File("/Users/caoyang/IdeaProjects/Java/src/designPattern");
        printAllFilesName(file, 0);
        System.out.println();
        printFileContent("/Users/caoyang/IdeaProjects/Java/src/WelcomeJavaWorld.java");
    }
}
