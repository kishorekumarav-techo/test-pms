import java.util.*;
import java.util.concurrent.locks.*;

class SecureCache {
    private static final Map<String, Map<String, Double>> cache = new HashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    public static Map<String, Double> getCache(String key, User user) {
        if (user == null || !user.getRole().equals("admin")) {
            return null;
        }
        lock.lock();
        try {
            return cache.get(key);
        } finally {
            lock.unlock();
        }
    }

    public static void putCache(String key, Map<String, Double> value) {
        lock.lock();
        try {
            cache.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public static void clearCache() {
        lock.lock();
        try {
            cache.clear();
        } finally {
            lock.unlock();
        }
    }
}

class ComplexProcessor {
    private List<Integer> data;

    public ComplexProcessor(List<Integer> data) {
        this.data = data;
    }

    public Map<Integer, Integer> process() {
        Map<Integer, Integer> res = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            int item = data.get(i);
            if (i % 2 == 0) {
                if (item > 10) {
                    if (item < 20) {
                        res.put(i, item * 2);
                    } else {
                        if (item < 30) {
                            res.put(i, item * 3);
                        } else {
                            res.put(i, item * 4);
                        }
                    }
                } else {
                    if (item == 5) {
                        res.put(i, 100);
                    } else {
                        if (item == 1) {
                            res.put(i, 50);
                        } else {
                            res.put(i, item);
                        }
                    }
                }
            } else {
                if (item < 0) {
                    res.put(i, 0);
                } else {
                    res.put(i, item);
                }
            }
        }
        return res;
    }
}

class User {
    private final String name;
    private final String role;
    private final boolean active;

    public User(String name, String role, boolean active) {
        this.name = name;
        this.role = role;
        this.active = active;
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
}

public class MainApp {
    public static Map<String, Double> expensiveOperation(List<Integer> inputData) {
        try {
            Thread.sleep(50);
        } catch (InterruptedException ignored) {}
        Map<String, Double> result = new HashMap<>();
        Random rand = new Random();
        for (int i = 0; i < inputData.size(); i++) {
            result.put(String.valueOf(i), inputData.get(i) * rand.nextDouble());
        }
        return result;
    }

    public static Map<String, Double> authorizedDataFetch(User user, List<Integer> inputData) {
        Map<String, Double> cached = SecureCache.getCache("expensive", user);
        if (cached != null) {
            return cached;
        }
        Map<String, Double> result = expensiveOperation(inputData);
        SecureCache.putCache("expensive", result);
        return result;
    }

    public static Map<Integer, Integer> handleRequest(User user, List<Integer> data) {
        if (user == null) return null;
        if (!user.isActive()) return null;

        ComplexProcessor processor = new ComplexProcessor(data);
        Map<Integer, Integer> processed = processor.process();
        return authorizedDataFetch(user, new ArrayList<>(processed.values()));
    }

    public static void main(String[] args) throws InterruptedException {
        List<User> users = Arrays.asList(
            new User("Alice", "admin", true),
            new User("Bob", "user", false),
            new User("Eve", "admin", false)
        );
        List<Integer> data = Arrays.asList(5, 15, 25, 35, 1, 0, -5, 10);

        for (int i = 0; i < 20; i++) {
            for (User user : users) {
                handleRequest(user, data);
                Thread.sleep(10);
            }
        }
    }
}
