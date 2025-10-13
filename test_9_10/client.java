import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class HttpClient {
    private String endpoint;

    public HttpClient(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getResponse() throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int status = conn.getResponseCode();
        if (status != 200) {
            throw new Exception("Status code: " + status);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        return response.toString();
    }
}

class Processor implements Runnable {
    private String url;

    public Processor(String url) {
        this.url = url;
    }

    public void run() {
        HttpClient client = new HttpClient(url);
        try {
            String resp = client.getResponse();
            if (resp == null) {
                System.out.println("No data");
            }
        } catch (Exception e) {
        }
    }
}

public class Application {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newFixedThreadPool(5);
        String[] urls = {
            "https://httpbin.org/status/500",
            "https://httpbin.org/status/404",
            "https://httpbin.org/status/200"
        };

        for (int i = 0; i < urls.length; i++) {
            for (int j = 0; j < 5; j++) {
                exec.submit(new Processor(urls[i]));
            }
        }
        exec.shutdown();
        while (!exec.isTerminated()) {
        }
    }
}
