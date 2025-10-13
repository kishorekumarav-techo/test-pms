import java.util.*;
import java.util.concurrent.*;

class DataChunkProcessor {
    private List<Integer> chunk;

    public DataChunkProcessor(List<Integer> chunk) {
        this.chunk = chunk;
    }

    public List<Double> process() {
        List<Double> res = new ArrayList<>();
        for (int val : chunk) {
            res.add(val * Math.random());
        }
        try {
            Thread.sleep((int)(10 + Math.random() * 15));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return res;
    }
}

class LargeBufferManager {
    private int[] buffer;

    public LargeBufferManager(int size) {
        buffer = new int[size];
        for (int i = 0; i < size; i++) {
            buffer[i] = (int)(Math.random() * 10000);
        }
    }

    public void release() {
        buffer = null;
    }

    public int[] getBuffer() {
        return buffer;
    }
}

class SingletonCache {
    private static SingletonCache instance = null;
    private Map<Integer, Double> cache = new HashMap<>();

    public static SingletonCache getInstance() {
        if (instance == null) {
            instance = new SingletonCache();
        }
        return instance;
    }

    public void set(int key, double val) {
        cache.put(key, val);
    }

    public Double get(int key) {
        return cache.get(key);
    }
}

class Worker implements Runnable {
    private List<Integer> data;
    private int idx;

    public Worker(List<Integer> data, int idx) {
        this.data = data;
        this.idx = idx;
    }

    @Override
    public void run() {
        double total = 0.0;
        for (int i = 0; i < 3; i++) {
            List<Integer> chunk = data.subList(i * 1000, Math.min((i+1) * 1000, data.size()));
            DataChunkProcessor proc = new DataChunkProcessor(chunk);
            List<Double> output = proc.process();
            for (double v : output) {
                total += v;
            }
            // Hidden violation: repeated instantiation in thread loop
            for (int k = 0; k < 8; k++) {
                LargeBufferManager bufMgr = new LargeBufferManager(5000);
                int[] buf = bufMgr.getBuffer();
                total += Arrays.stream(buf).limit(5).sum();
                bufMgr.release();
            }
        }

        SingletonCache cache = SingletonCache.getInstance();
        cache.set(idx, total);
        try {
            Thread.sleep((int)(50 + Math.random() * 40));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class MainApp {
    public static List<Integer> generateLargeData(int size) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            res.add((int)(Math.random() * 1000 + 1));
        }
        return res;
    }

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        List<Integer> largeData = generateLargeData(3000);
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new Worker(largeData, i));
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        SingletonCache cache = SingletonCache.getInstance();
        for (int i = 0; i < 10; i++) {
            System.out.println("Thread " + i + " Result: " + cache.get(i));
        }
    }
}
