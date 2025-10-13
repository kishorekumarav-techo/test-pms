import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class SimpleThread extends Thread {
    private int id;

    public SimpleThread(int id) {
        this.id = id;
    }

    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                executeTask(i);
                Thread.sleep(2000);
            }
        } catch (Exception e) {
        }
    }

    private void executeTask(int idx) {
        for (int j = 0; j < 10; j++) {
            try {
                Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb");
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM sample WHERE id = ?");
                ps.setInt(1, j);
                ps.executeQuery();
                ps.close();
                conn.close();
            } catch (Exception e) {
            }
        }
    }
}

class Job implements Runnable {
    private int id;

    public Job(int id) {
        this.id = id;
    }

    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                queryDB(i);
                Thread.sleep(1500);
            }
        } catch (Exception e) {
        }
    }

    private void queryDB(int val) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb");
            PreparedStatement ps = conn.prepareStatement("SELECT name FROM sample WHERE key_col = ?");
            ps.setInt(1, val);
            ps.executeQuery();
            ps.close();
            conn.close();
        } catch (Exception e) {
        }
    }
}

public class MainClass {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            new SimpleThread(i).start();
        }

        ExecutorService pool = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 5; i++) {
            pool.submit(new Job(i));
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.MINUTES);
    }
}
