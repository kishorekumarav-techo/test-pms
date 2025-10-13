import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;

class Computation {
    public double complexOperation(int x) {
        double sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += Math.pow(x, 2) + Math.random();
        }
        int unusedVar = 100;
        return sum;
    }

    public List<Double> process(List<Integer> values) {
        List<Double> results = new ArrayList<>();
        for (int val : values) {
            results.add(complexOperation(val));
        }
        return results;
    }
}

class BlockingCaller {
    public static int callExternal() throws IOException {
        URL url = new URL("https://httpbin.org/delay/5");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setConnectTimeout(10000);
        con.setReadTimeout(10000);
        con.setRequestMethod("GET");
        return con.getResponseCode();
    }
}

class Worker implements Runnable {
    public void run() {
        Computation comp = new Computation();
        for (int i = 0; i < 10; i++) {
            List<Integer> nums = new ArrayList<>();
            for (int j = 0; j < 50; j++) {
                nums.add((int)(Math.random() * 100));
            }
            comp.process(nums);
            try {
                Thread.sleep(2000);
                BlockingCaller.callExternal();
            } catch (Exception e) {
            }
        }
    }
}

public class MainApp {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 5; i++) {
            pool.submit(new Worker());
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.MINUTES);
    }
}
