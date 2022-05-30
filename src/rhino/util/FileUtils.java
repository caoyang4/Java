package src.rhino.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.Properties;

public class FileUtils {

    public static Properties readFile(InputStream is) throws IOException {
        Properties properties = new Properties();
        if (is != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    int idx = line.indexOf("=");
                    if (idx != -1) {
                        String key = line.substring(0, idx);
                        String value = line.substring(idx + 1);
                        properties.put(key.trim(), value.trim());
                    }
                }
            }
        }
        return properties;
    }

    public static void writeFile(File file, Properties properties) throws IOException {
        if (!file.exists()) {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));) {
            for (Entry<Object, Object> entry : properties.entrySet()) {
                pw.println(entry.getKey() + "=" + entry.getValue());
            }
        }
    }
}
