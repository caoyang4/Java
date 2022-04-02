package src.basis.io;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 简单爬虫
 * @author caoyang
 */
@Slf4j
public class Downloader {
    public static List<String> download() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL("http://www.baidu.com").openConnection();
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))){
            String line;
            while ((line = br.readLine()) != null){
                log.info(line);
                lines.add(line);
            }
        }
        return lines;
    }

    public static void main(String[] args) throws IOException {
        download();
    }
}
