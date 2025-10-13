import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class ThreadCreator extends Thread {
    private int id;

    public ThreadCreator(int id) {
        this.id = id;
    }

    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                performQuery(i);
                Thread.sleep(2000);
            }
        } catch (Exception e) {
        }
    }

    private void performQuery(int index) {
        for (int j = 0; j < 10; j++) {
            try {
                Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM test_table WHERE id = ?");
                ps.setInt(1, j);
                ps.executeQuery();
                ps.close();
                conn.close();
            } catch (Exception e) {
            }
        }
    }
}

class TaskWorker implements Runnable {
    private int id;

    public TaskWorker(int id) {
        this.id = id;
    }

    public void run() {
        try {
            for (int i = 0; i < 5; i++) {
                queryDatabase(i);
                Thread.sleep(1500);
            }
        } catch (Exception e) {
        }
    }

    private void queryDatabase(int param) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
            PreparedStatement ps = conn.prepareStatement("SELECT name FROM test_table WHERE key = ?");
            ps.setInt(1, param);
            ps.executeQuery();
            ps.close();
            conn.close();
        } catch (Exception e) {
        }
    }
}

public class Application {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            new ThreadCreator(i).start();
        }

        ExecutorService pool = Executors.newFixedThreadPool(3);

        for (int i = 0; i < 5; i++) {
            pool.submit(new TaskWorker(i));
        }

        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.MINUTES);
    }
}
