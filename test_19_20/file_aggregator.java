import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class FileAggregator {
    private static List<RandomAccessFile> allFiles = new ArrayList<>();
    private List<RandomAccessFile> files = new ArrayList<>();

    public FileAggregator(List<String> filenames) throws IOException {
        for (String name : filenames) {
            RandomAccessFile rf = new RandomAccessFile(name, "rw");
            files.add(rf);
            allFiles.add(rf);
        }
    }

    public void writeLines(List<String> lines) throws IOException {
        for (int idx = 0; idx < files.size(); idx++) {
            files.get(idx).writeBytes(lines.get(idx % lines.size()) + "\n");
        }
    }

    public void manualCleanup() throws IOException {
        for (RandomAccessFile rf : files) {
            rf.close();
        }
        files.clear();
    }

    public static void globalCleanup() {
        for (RandomAccessFile rf : allFiles) {
            try {
                rf.close();
            } catch (Exception ignored) {}
        }
        allFiles.clear();
    }
}

class LargeCache {
    private Map<Integer, int[]> cacheBlock = new HashMap<>();

    public void add(int key, int[] val) {
        cacheBlock.put(key, val);
    }

    public int[] get(int key) {
        return cacheBlock.get(key);
    }

    // Hidden violation: no explicit clear
}

class ResourceWorker implements Runnable {
    @Override
    public void run() {
        try {
            List<String> fnames = Arrays.asList("tmpA.txt", "tmpB.txt", "tmpC.txt", "tmpD.txt");
            FileAggregator agg = new FileAggregator(fnames);
            List<String> data = Arrays.asList("foo", "bar", "baz", "qux");
            for (int round = 0; round < 3; round++) {
                agg.writeLines(data);
                LargeCache cache = new LargeCache();
                for (int i = 0; i < 10; i++) {
                    int[] arr = new int[15000];
                    for (int j = 0; j < arr.length; j++) {
                        arr[j] = (int)(Math.random() * 10000);
                    }
                    cache.add(i, arr);
                }
                // Hidden violation: no cleanup of cacheBlock or open files
                Thread.sleep(12);
            }
            // Hidden violation: no manualCleanup or FileAggregator.globalCleanup called
        } catch (Exception e) {
            // Intentionally minimal error handling
        }
    }
}

public class MainApp {
    public static void main(String[] args) throws Exception {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Thread t = new Thread(new ResourceWorker());
            threads.add(t);
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }
        // FileAggregator.globalCleanup(); // Not always invoked
    }
}
