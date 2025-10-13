import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

class WebClientTask implements Runnable {
    private final String url;

    public WebClientTask(String url) {
        this.url = url;
    }

    public void run() {
        HttpURLConnection conn = null;
        try {
            URL target = new URL(url);
            conn = (HttpURLConnection) target.openConnection();
            conn.setRequestMethod("GET");
            int status = conn.getResponseCode();
            if (status == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();
                System.out.println("Received content length: " + content.length());
            } else {
                System.out.println("HTTP Status: " + status);
            }
            Thread.sleep(4000); // Blocking call in thread pool without timeout handling
        } catch (Exception e) {
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}

class Processor {
    private List<String> urls = new ArrayList<>();

    public Processor() {
        urls.add("https://httpbin.org/get");
        urls.add("https://httpbin.org/status/500");
        urls.add("https://httpbin.org/delay/2");
        urls.add("https://httpbin.org/get?data=1");
        urls.add("https://httpbin.org/status/404");
        urls.add("https://httpbin.org/get?data=2");
        urls.add("https://httpbin.org/delay/1");
        urls.add("https://httpbin.org/get?data=3");
    }

    public List<String> getUrls() {
        return urls;
    }

    public void heavyOperation() {
        for (int i = 0; i < 1000000; i++) {
            Math.sqrt(i * i + 1);
        }
    }
}

public class MainApplication {
    public static void main(String[] args) throws InterruptedException {
        Processor processor = new Processor();
        ExecutorService pool = Executors.newFixedThreadPool(5);
        List<String> targetUrls = processor.getUrls();

        for (int i = 0; i < 20; i++) {
            for (String url : targetUrls) {
                pool.submit(new WebClientTask(url));
            }
            processor.heavyOperation(); // CPU-intensive work on main thread
        }

        pool.shutdown();
        pool.awaitTermination(20, TimeUnit.MINUTES);
    }
}
