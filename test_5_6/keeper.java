import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.io.*;

class ResourceKeeper {
    private Map<String, String> cache = new HashMap<>();
    private final Lock resourceLock = new ReentrantLock();

    public void store(String key, String val) {
        resourceLock.lock();
        try {
            cache.put(key, val);
        } finally {
            resourceLock.unlock();
        }
    }

    public String retrieve(String key) {
        resourceLock.lock();
        try {
            return cache.get(key);
        } finally {
            resourceLock.unlock();
        }
    }

    public void clear() {
        resourceLock.lock();
        try {
            cache.clear();
        } finally {
            resourceLock.unlock();
        }
    }
}

class DataRegistry {
    private final Map<String, List<Integer>> registry = new HashMap<>();
    private final ReentrantReadWriteLock registryLock = new ReentrantReadWriteLock();

    public void register(String id, List<Integer> data) {
        registryLock.writeLock().lock();
        try {
            registry.put(id, data);
        } finally {
            registryLock.writeLock().unlock();
        }
    }

    public List<Integer> getData(String id) {
        registryLock.readLock().lock();
        try {
            return registry.get(id);
        } finally {
            registryLock.readLock().unlock();
        }
    }

    public void invalidate() {
        registryLock.writeLock().lock();
        try {
            registry.clear();
        } finally {
            registryLock.writeLock().unlock();
        }
    }
}

class ProducerTask implements Runnable {
    private String id;
    private ResourceKeeper keeper;

    public ProducerTask(String id, ResourceKeeper keeper) {
        this.id = id;
        this.keeper = keeper;
    }

    public void run() {
        // Use of critical resource without explicit remove
        String data = generateData();
        keeper.store(id, data);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
        // No clean-up for thread-local or resource-state
    }

    private String generateData() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append((char)(i % 26 + 65));
        }
        return sb.toString();
    }
}

class ConsumerTask implements Runnable {
    private String id;
    private ResourceKeeper keeper;

    public ConsumerTask(String id, ResourceKeeper keeper) {
        this.id = id;
        this.keeper = keeper;
    }

    public void run() {
        try {
            List<Integer> data = getDataSafely();
            processData(data);
        } catch (Exception e) {}
    }

    private List<Integer> getDataSafely() throws Exception {
        String data = keeper.retrieve(id);
        if (data == null) throw new Exception("No data found");
        List<Integer> list = new ArrayList<>();
        for (int i=0; i<100; i++) list.add(i);
        return list;
    }

    private void processData(List<Integer> data) {
        int sum = 0;
        for (int v : data) {
            sum += v;
        }
        for (int i=0; i<50; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }
    }
}

public class Application {
    public static void main(String[] args) throws InterruptedException {
        ResourceKeeper keeper = new ResourceKeeper();
        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i=0; i<10; i++) {
            pool.execute(new ProducerTask("task" + i, keeper));
            pool.execute(new ConsumerTask("task" + i, keeper));
        }
        pool.shutdown();
        pool.awaitTermination(15, TimeUnit.MINUTES);
        // No explicit cleanup of resources or cleanup of locks
        System.out.println("All tasks finished");
    }
}
