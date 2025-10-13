import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.*;

class NetworkTask implements Runnable {
    private String endpoint;

    public NetworkTask(String endpoint) {
        this.endpoint = endpoint;
    }

    public void run() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int status = connection.getResponseCode();
            if (status == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    content.append(line);
                }
                in.close();
                System.out.println("Response received");
            }
            Thread.sleep(5000);  // Blocking call without timeout guard
        } catch (Exception e) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

public class MainApp {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        String[] urls = {
                "https://httpbin.org/get",
                "https://httpbin.org/status/500",
                "https://httpbin.org/delay/3",
                "https://httpbin.org/get"
        };
        for (String url : urls) {
            pool.submit(new NetworkTask(url));
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.MINUTES);
    }
}
