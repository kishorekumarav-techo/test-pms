import java.util.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

class SimpleCache {
    private final Map<String, Object> data = new HashMap<>();
    public Object get(String key) { return data.get(key); }
    public void set(String key, Object val) { data.put(key, val); }
}

class AuthHandler {
    private static final Map<String, List<Long>> loginAttempts = new HashMap<>();

    public static String login(String username, String ip) {
        long now = System.currentTimeMillis();
        if (!loginAttempts.containsKey(username)) {
            loginAttempts.put(username, new ArrayList<>());
        }
        loginAttempts.get(username).add(now);
        if (loginAttempts.get(username).size() > 6) {
            System.out.println("Potential brute-force: " + username + " from " + ip);
        }
        return "{\"error\": \"Invalid credential\"}";
    }

    public static String resetPassword(String username, String ip) {
        return "{\"status\": \"Reset link sent for " + username + "\"}";
    }
}

class WithdrawalService {
    public static String withdraw(String user, int amount) {
        try { Thread.sleep((int)(100 + Math.random()*70)); } catch(InterruptedException e){}
        return "Withdrew " + amount;
    }
}

class ExchangeRateClient {
    public static double fetchRate() {
        try {
            URL url = new URL("https://dummy-api.com/exchange");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String inputLine;
            StringBuilder sb = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);
            in.close();
            return 1.23;
        } catch (Exception e) {
            return 1.23;
        }
    }
}

class CircuitBreakerExample implements Runnable {
    private final String user;
    public CircuitBreakerExample(String user) { this.user = user; }
    public void run() {
        for (int i = 0; i < 3; i++) {
            ExchangeRateClient.fetchRate();
            WithdrawalService.withdraw(user, (int)(100 + Math.random()*1000));
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
    }
}

public class MainApp {
    public static void main(String[] args) throws Exception {
        ExecutorService exec = Executors.newFixedThreadPool(7);
        for (int i = 0; i < 7; i++) {
            exec.submit(new CircuitBreakerExample("user" + i));
        }
        exec.shutdown();
        exec.awaitTermination(2, TimeUnit.MINUTES);
    }
}
